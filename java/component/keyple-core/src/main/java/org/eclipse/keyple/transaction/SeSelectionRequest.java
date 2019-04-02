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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelectionRequest class combines a SeSelector with additional helper methods useful to the
 * selection process done in {@link SeSelection}.
 * <p>
 * This class may also be extended to add particular features specific to a SE family.
 */
public class SeSelectionRequest {
    private static final Logger logger = LoggerFactory.getLogger(SeSelectionRequest.class);

    private SeSelector seSelector;
    private Class<? extends MatchingSe> matchingClass = MatchingSe.class;
    /** optional apdu requests list to be executed following the selection process */
    private final List<ApduRequest> seSelectionApduRequestList = new ArrayList<ApduRequest>();
    /**
     * the channelState and protocolFlag may be accessed from derived classes. Let them with the
     * protected access level.
     */
    protected final ChannelState channelState;
    protected final SeProtocol protocolFlag;

    public SeSelectionRequest(SeSelector seSelector, ChannelState channelState,
            SeProtocol protocolFlag) {
        this.seSelector = seSelector;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
        if (logger.isTraceEnabled()) {
            logger.trace("SeSelection");
        }
    }

    /**
     * Returns a selection SeRequest built from the information provided in the constructor and
     * possibly completed with the seSelectionApduRequestList
     *
     * @return the selection SeRequest
     */
    protected final SeRequest getSelectionRequest() {
        SeRequest seSelectionRequest = null;
        seSelectionRequest =
                new SeRequest(seSelector, seSelectionApduRequestList, channelState, protocolFlag);
        return seSelectionRequest;
    }

    public SeSelector getSeSelector() {
        return seSelector;
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
     * The default value for the matchingClass (unless setMatchingClass is used) is MatchingSe.class
     *
     * @return the current matchingClass
     */
    protected final Class<? extends MatchingSe> getMatchingClass() {
        return matchingClass;
    }

    /**
     * Add an additional {@link ApduRequest} to be executed after the selection process if it
     * succeeds.
     * <p>
     * If more than one {@link ApduRequest} is added, all will be executed in the order in which
     * they were added.
     * 
     * @param apduRequest an {@link ApduRequest}
     */
    protected final void addApduRequest(ApduRequest apduRequest) {
        seSelectionApduRequestList.add(apduRequest);
    }

    /**
     * Return the parser corresponding to the command whose index is provided.
     *
     * @param seResponse the received SeResponse containing the commands raw responses
     * @param commandIndex the command index
     * @return a parser of the type matching the command
     */
    public AbstractApduResponseParser getCommandParser(SeResponse seResponse, int commandIndex) {
        /* not implemented in keyple-core */
        throw new IllegalStateException("No parsers available for this request.");
    }

    @Override
    public String toString() {
        return "SeSelectionRequest: MATCHING_CLASS = " + matchingClass.toString();
    }
}
