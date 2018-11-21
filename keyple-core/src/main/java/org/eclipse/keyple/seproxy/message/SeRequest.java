/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.message;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * List of APDU requests that will result in a {@link SeResponse}
 * 
 * @see SeResponse
 */
public final class SeRequest implements Serializable {

    /**
     * indicate if the logical channel should be closed or not at the end of the request
     * transmission
     */
    public enum ChannelState {
        KEEP_OPEN, CLOSE_AFTER
    }

    static final long serialVersionUID = 6018469841127325812L;

    /**
     * The Selector inner class is dedicated to handle the selection of the SE either through a
     * selection command with AID (AtrSelector) or through a matching test between the SE ATR and a
     * regular expression (AtrSelector).
     *
     */
    public static abstract class Selector {
    }

    public static final class AidSelector extends SeRequest.Selector {

        public static final int AID_MIN_LENGTH = 5;
        public static final int AID_MAX_LENGTH = 16;
        protected SeSelector.SelectMode selectMode = SeSelector.SelectMode.FIRST;

        /**
         * - AID’s bytes of the SE application to select. In case the SE application is currently
         * not selected, a logical channel is established and the corresponding SE application is
         * selected by the SE reader, otherwise keep the current channel.
         *
         * - Could be missing when operating SE which don’t support the Select Application command
         * (as it is the case for SAM).
         */
        private byte[] aidToSelect;

        /**
         * AID based selector
         * 
         * @param aidToSelect byte array
         */
        public AidSelector(byte[] aidToSelect) {
            if (aidToSelect.length < AID_MIN_LENGTH || aidToSelect.length > AID_MAX_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("Bad AID length: %d", aidToSelect.length));
            }
            this.aidToSelect = aidToSelect;
        }

        /**
         * AID based selector with selection mode
         * <p>
         * The selectMode parameter defines the selection options P2 of the SELECT command message
         * <ul>
         * <li>false: first or only occurrence</li>
         * <li>true: next occurrence</li>
         * </ul>
         * 
         * @param aidToSelect byte array
         * @param selectMode selection mode FIRST or NEXT
         */
        public AidSelector(byte[] aidToSelect, SeSelector.SelectMode selectMode) {
            this(aidToSelect);
            this.selectMode = selectMode;
        }

        /**
         * Getter for the AID provided at construction time
         * 
         * @return byte array containing the AID
         */
        public byte[] getAidToSelect() {
            return aidToSelect;
        }

        /**
         * Indicates whether the selection command is targeting the first or the next occurrence
         * 
         * @return true or false
         */
        public boolean isSelectNext() {
            return selectMode == SeSelector.SelectMode.NEXT;
        }

        /**
         * Print out the AID in hex
         * 
         * @return a string
         */
        public String toString() {
            return String.format("AID:%s",
                    aidToSelect == null ? "null" : ByteArrayUtils.toHex(aidToSelect));
        }
    }

    public static final class AtrSelector extends SeRequest.Selector {
        /**
         * Regular expression dedicated to handle SE logical channel opening based on ATR pattern
         */
        private String atrRegex;

        /**
         * ATR based selection
         *
         * @param atrRegex String hex regular expression
         */
        public AtrSelector(String atrRegex) {
            this.atrRegex = atrRegex;
        }

        /**
         * Getter for the regular expression provided at construction time
         *
         * @return Regular expression string
         */
        public String getAtrRegex() {
            return atrRegex;
        }

        /**
         * Tells if the provided ATR matches the registered regular expression
         *
         * If the registered regular expression is empty, the ATR is always matching.
         *
         * @param atr a buffer containing the ATR to be checked
         * @return a boolean true the ATR matches the current regex
         */
        public boolean atrMatches(byte[] atr) {
            boolean m;
            if (atrRegex.length() != 0) {
                Pattern p = Pattern.compile(atrRegex);
                String atrString = ByteArrayUtils.toHex(atr);
                m = p.matcher(atrString).matches();
            } else {
                m = true;
            }
            return m;
        }

        /**
         * Print out the ATR regex
         * 
         * @return a string
         */
        public String toString() {
            return String.format("ATR regex:%s", atrRegex.length() != 0 ? atrRegex : "empty");
        }
    }

    /**
     * SE selector is either an AID or an ATR regular expression
     */
    private final Selector selector;

    /**
     * List of status codes in response to the select application command that should be considered
     * successful although they are different from 9000
     */
    private Set<Integer> successfulSelectionStatusCodes = new LinkedHashSet<Integer>();

    /**
     * contains a group of APDUCommand to operate on the selected SE application by the SE reader.
     */
    private List<ApduRequest> apduRequests;


    /**
     * the protocol flag is used to target specific SE technologies for a given request
     */
    private SeProtocol protocolFlag = Protocol.ANY;

    /**
     * the final logical channel status: the SE reader may kept active the logical channel of the SE
     * application after processing the group of APDU commands otherwise the SE reader will close
     * the logical channel of the SE application after processing the group of APDU commands (i.e.
     * after the receipt of the last APDU response).
     */
    private ChannelState channelState;

    /**
     * The constructor called by a ProxyReader in order to open a logical channel, to send a set of
     * APDU commands to a SE application, or both of them.
     * <ul>
     * <li>For SE requiring an AID based selection, the Selector should be defined with a non null
     * byte array.</li>
     * <li>For SE requiring an ATR based selection, the Selector should be defined with a non null
     * String regular expression.</li>
     * <li>For SE supporting neither AID selection nor ATR selection, the Selector should be defined
     * as null.</li>
     * <li>The protocolFlag parameter is optional.</li>
     * </ul>
     *
     * @param selector the SE selector
     * @param apduRequests the apdu requests
     * @param channelState the keep channel open
     * @param protocolFlag the expected protocol
     * @param successfulSelectionStatusCodes a list of successful status codes for the select
     *        application command
     */
    public SeRequest(Selector selector, List<ApduRequest> apduRequests, ChannelState channelState,
            SeProtocol protocolFlag, Set<Integer> successfulSelectionStatusCodes) {
        if (protocolFlag == null) {
            throw new IllegalArgumentException("¨protocolFlag can't be null");
        }
        this.selector = selector;
        this.apduRequests = apduRequests;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
        this.successfulSelectionStatusCodes = successfulSelectionStatusCodes;
    }

    /**
     * Constructor to be used when the SE is already selected
     * 
     * @param apduRequests a list of ApudRequest
     * @param channelState a flag to tell if the channel has to be closed at the end
     */
    public SeRequest(List<ApduRequest> apduRequests, ChannelState channelState) {
        this(null, apduRequests, channelState, Protocol.ANY, null);
    }


    /**
     * Gets the SE selector.
     *
     * @return the current SE selector
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * Gets the apdu requests.
     *
     * @return the group of APDUs to be transmitted to the SE application for this instance of
     *         SERequest.
     */
    public List<ApduRequest> getApduRequests() {
        return apduRequests;
    }

    /**
     * Define if the channel should be kept open after the the {@link SeRequestSet} has been
     * executed.
     *
     * @return If the channel should be kept open
     */
    public boolean isKeepChannelOpen() {
        return channelState == ChannelState.KEEP_OPEN;
    }

    /**
     * Gets the protocol flag of the request
     * 
     * @return protocolFlag
     */
    public SeProtocol getProtocolFlag() {
        return protocolFlag;
    }

    /**
     * Gets the list of successful selection status codes
     * 
     * @return the list of status codes
     */
    public Set<Integer> getSuccessfulSelectionStatusCodes() {
        return successfulSelectionStatusCodes;
    }

    @Override
    public String toString() {
        return String.format("SeRequest:{REQUESTS = %s, SELECTOR = %s, KEEPCHANNELOPEN = %s}",
                getApduRequests(), getSelector(), channelState);
    }
}
