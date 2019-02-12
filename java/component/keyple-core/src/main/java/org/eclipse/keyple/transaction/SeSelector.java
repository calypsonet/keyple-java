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
package org.eclipse.keyple.transaction;

import java.util.*;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
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
    private final String atrRegex;
    private final byte[] aid;
    private final SelectMode selectMode;
    private final boolean selectionByAid;
    private String extraInfo;

    public String getAtrRegex() {
        return atrRegex;
    }

    public byte[] getAid() {
        return aid;
    }

    public SelectMode getSelectMode() {
        return selectMode;
    }

    public boolean isSelectionByAid() {
        return selectionByAid;
    }

    /**
     * SelectMode indicates how to carry out the application selection in accordance with ISO7816-4
     */
    public enum SelectMode {
        FIRST, NEXT
    }

    /**
     * Instantiate a SeSelector object with the selection data (atrRegex), dedicated to select a SE
     * that does not support the select application command, the channel management after the
     * selection and the protocol flag to possibly target a specific protocol
     *
     * @param atrRegex a regular expression to compare with the ATR of the targeted SE
     * @param channelState flag to tell if the logical channel should be left open at the end of the
     *        selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     * @param extraInfo information string (to be printed in logs)
     */
    public SeSelector(String atrRegex, ChannelState channelState, SeProtocol protocolFlag,
            String extraInfo) {
        this.atrRegex = atrRegex;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
        if (extraInfo != null) {
            this.extraInfo = extraInfo;
        } else {
            this.extraInfo = "";
        }
        aid = null;
        selectionByAid = false;
        selectMode = SelectMode.FIRST;
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "ATR based selection: ATRREGEX = {}, KEEPCHANNELOPEN = {}, PROTOCOLFLAG = {}",
                    atrRegex, channelState, protocolFlag);
        }
    }

    /**
     * Instantiate a SeSelector object with the selection data (AID), dedicated to select a SE that
     * does not support the select application command, the channel management after the selection
     * and the protocol flag to possibly target a specific protocol
     *
     * @param aid the target AID (end bytes can be truncated)
     * @param selectMode a flag to indicate if the first or the next occurrence is requested (see
     *        ISO7816-4 for a complete description of the select next mechanism)
     * @param channelState flag to tell if the logical channel should be left open at the end of the
     *        selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     * @param extraInfo information string (to be printed in logs)
     */
    public SeSelector(byte[] aid, SelectMode selectMode, ChannelState channelState,
            SeProtocol protocolFlag, String extraInfo) {
        this.aid = aid;
        this.selectMode = selectMode;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
        if (extraInfo != null) {
            this.extraInfo = extraInfo;
        } else {
            this.extraInfo = "";
        }
        atrRegex = null;
        selectionByAid = true;
        if (logger.isTraceEnabled()) {
            logger.trace("AID based selection: AID = {}, KEEPCHANNELOPEN = {}, PROTOCOLFLAG = {}",
                    ByteArrayUtils.toHex(aid), channelState, protocolFlag);
        }
    }

    /**
     * @return the protocolFlag defined by the constructor
     */
    public final SeProtocol getProtocolFlag() {
        return protocolFlag;
    }

    /**
     * Sets the list of ApduRequest to be executed following the selection operation at once
     * 
     * @param seSelectionApduRequestList the list of requests
     */
    public final void setSelectionApduRequestList(List<ApduRequest> seSelectionApduRequestList) {
        this.seSelectionApduRequestList = seSelectionApduRequestList;
    }

    /**
     * Returns a selection SeRequest built from the information provided in the constructor and
     * possibly completed with the seSelectionApduRequestList
     *
     * @return the selection SeRequest
     */
    protected final SeRequest getSelectorRequest() {
        SeRequest seSelectionRequest;
        if (!isSelectionByAid()) {
            seSelectionRequest = new SeRequest(new SeRequest.AtrSelector(getAtrRegex()),
                    seSelectionApduRequestList, channelState, protocolFlag);
        } else {
            seSelectionRequest = new SeRequest(
                    new SeRequest.AidSelector(getAid(), selectApplicationSuccessfulStatusCodes,
                            getSelectMode()),
                    seSelectionApduRequestList, channelState, protocolFlag);
        }
        return seSelectionRequest;
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
     * The matchingClass is the MatchingSe class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of MatchingSe is to be instantiated.
     *
     * This method must be called in the classes that extend SeSelector in order to specify the
     * expected class derived from MatchingSe in return to the selection process.
     * 
     * @param matchingClass the expected class for this SeSelector
     */
    protected final void setMatchingClass(Class<? extends MatchingSe> matchingClass) {
        this.matchingClass = matchingClass;
    }

    /**
     * The selectorClass is the SeSelector class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of SeSelector is to be used as argument to
     * the matchingClass constructor.
     *
     * This method must be called in the classes that extend SeSelector in order to specify the
     * expected class derived from SeSelector used as an argument to derived form of MatchingSe.
     * 
     * @param selectorClass the argument for the constructor of the matchingClass
     */
    protected final void setSelectorClass(Class<? extends SeSelector> selectorClass) {
        this.selectorClass = selectorClass;
    }

    /**
     * The default value for the matchingClass (unless setMatchingClass is used) is MatchingSe.class
     * 
     * @return the current matchingClass
     */
    protected final Class<? extends MatchingSe> getMatchingClass() {
        return matchingClass;
    }

    /**
     * The default value for the selectorClass (unless setSelectorClass is used) is SeSelector.class
     * 
     * @return the current selectorClass
     */
    protected final Class<? extends SeSelector> getSelectorClass() {
        return selectorClass;
    }
}
