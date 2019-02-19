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
package org.eclipse.keyple.seproxy;

import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelector class groups the information and methods used to select a particular secure
 * element
 */
public class SeSelector {
    private static final Logger logger = LoggerFactory.getLogger(SeSelector.class);

    protected List<ApduRequest> seSelectionApduRequestList = new ArrayList<ApduRequest>();
    protected Set<Integer> selectApplicationSuccessfulStatusCodes = new HashSet<Integer>();
    private Class<? extends MatchingSe> matchingClass = MatchingSe.class;
    private Class<? extends SeSelector> selectorClass = SeSelector.class;
    private final ChannelState channelState;
    private final SeProtocol protocolFlag;
    private final AidSelector aidSelector;
    private final AtrFilter atrFilter;
    private String extraInfo;

    public static class AidSelector {

        /**
         * SelectMode indicates how to carry out the application selection in accordance with
         * ISO7816-4
         */
        public enum SelectMode {
            FIRST, NEXT
        }

        public static final int AID_MIN_LENGTH = 5;
        public static final int AID_MAX_LENGTH = 16;
        protected SelectMode selectMode = SelectMode.FIRST;

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
         * List of status codes in response to the select application command that should be
         * considered successful although they are different from 9000
         */
        Set<Integer> successfulSelectionStatusCodes = new LinkedHashSet<Integer>();

        /**
         * AID based aidSelector without successfulSelectionStatusCodes
         *
         * @param aidToSelect byte array
         */
        public AidSelector(byte[] aidToSelect) {
            this.aidToSelect = aidToSelect;
        }

        /**
         * AID based aidSelector with selection mode
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
        public AidSelector(byte[] aidToSelect, SelectMode selectMode) {
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
            return selectMode == SelectMode.NEXT;
        }

        /**
         * Sets the list of successful selection status codes
         *
         * @param successfulSelectionStatusCodes
         */
        public void setSuccessfulSelectionStatusCodes(Set<Integer> successfulSelectionStatusCodes) {
            this.successfulSelectionStatusCodes = successfulSelectionStatusCodes;
        }

        /**
         * Gets the list of successful selection status codes
         *
         * @return the list of status codes
         */
        public Set<Integer> getSuccessfulSelectionStatusCodes() {
            return successfulSelectionStatusCodes;
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

    public static class AtrFilter {
        /**
         * Regular expression dedicated to handle SE logical channel opening based on ATR pattern
         */
        private String atrRegex;

        /**
         * Regular expression based filter
         *
         * @param atrRegex String hex regular expression
         */
        public AtrFilter(String atrRegex) {
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
     * Instantiate a SeSelector object with the optional ATR filtering and selection data (AID),
     * dedicated to select a SE. The channel management after the selection and the protocol flag to
     * possibly target a specific protocol
     *
     * @param aidSelector the AID selection data
     * @param atrFilter the ATR filter
     * @param channelState flag to tell if the logical channel should be left open at the end of the
     *        selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     * @param extraInfo information string (to be printed in logs)
     */
    public SeSelector(AidSelector aidSelector, AtrFilter atrFilter, ChannelState channelState,
            SeProtocol protocolFlag, String extraInfo) {
        this.aidSelector = aidSelector;
        this.atrFilter = atrFilter;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
        if (extraInfo != null) {
            this.extraInfo = extraInfo;
        } else {
            this.extraInfo = "";
        }
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Selection data: AID = {}, ATRREGEX = {}, KEEPCHANNELOPEN = {}, PROTOCOLFLAG = {}",
                    ByteArrayUtils.toHex(this.aidSelector.getAidToSelect()),
                    this.atrFilter.getAtrRegex(), protocolFlag);
        }
    }

    public void addApduRequest(ApduRequest apduRequest) {
        seSelectionApduRequestList.add(apduRequest);
    }

    public AidSelector getAidSelector() {
        return aidSelector;
    }

    public AtrFilter getAtrFilter() {
        return atrFilter;
    }

    /**
     * Gets the information string
     *
     * @return a string to be printed in logs
     */
    public final String getExtraInfo() {
        return extraInfo;
    }

    /**
     * @return the protocolFlag defined by the constructor
     */
    public final SeProtocol getProtocolFlag() {
        return protocolFlag;
    }

    @Override
    public String toString() {
        return "SeSelector";
    }
}
