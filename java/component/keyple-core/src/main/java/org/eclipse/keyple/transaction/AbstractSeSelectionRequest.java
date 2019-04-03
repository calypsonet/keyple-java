/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
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

public abstract class AbstractSeSelectionRequest<T extends MatchingSe> {
    protected SeSelector seSelector;

    /** optional apdu requests list to be executed following the selection process */
    protected final List<ApduRequest> seSelectionApduRequestList = new ArrayList<ApduRequest>();

    /**
     * the channelState and protocolFlag may be accessed from derived classes. Let them with the
     * protected access level.
     */
    protected final ChannelState channelState;
    protected final SeProtocol protocolFlag;

    public AbstractSeSelectionRequest(SeSelector seSelector, ChannelState channelState,
            SeProtocol protocolFlag) {
        this.seSelector = seSelector;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
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

    protected abstract T parse(SeResponse seResponse);

    protected abstract AbstractApduResponseParser getCommandParser(SeResponse seResponse,
            int commandIndex);
}
