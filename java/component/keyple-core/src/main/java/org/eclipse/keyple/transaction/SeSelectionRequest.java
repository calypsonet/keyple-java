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


import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
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
public class SeSelectionRequest extends AbstractSeSelectionRequest<MatchingSe> {
    private static final Logger logger = LoggerFactory.getLogger(SeSelectionRequest.class);

    public SeSelectionRequest(SeSelector seSelector, ChannelState channelState,
            SeProtocol protocolFlag) {
        super(seSelector, channelState, protocolFlag);
    }

    /**
     * Return the parser corresponding to the command whose index is provided.
     *
     * @param seResponse the received SeResponse containing the commands raw responses
     * @param commandIndex the command index
     * @return a parser of the type matching the command
     */
    @Override
    public AbstractApduResponseParser getCommandParser(SeResponse seResponse, int commandIndex) {
        /* not yet implemented in keyple-core */
        // TODO add a generic command parser
        throw new IllegalStateException("No parsers available for this request.");
    }

    @Override
    public String toString() {
        // TODO
        return "";
    }

    @Override
    protected MatchingSe parse(SeResponse seResponse) {
        return new MatchingSe(seResponse, seSelector.getExtraInfo());
    }
}
