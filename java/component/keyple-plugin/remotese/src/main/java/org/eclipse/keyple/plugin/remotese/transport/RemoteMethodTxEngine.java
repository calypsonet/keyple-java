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
package org.eclipse.keyple.plugin.remotese.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Executor of RemoteMethodTx It manages : - send Dto - manage Dto Responses
 */
public class RemoteMethodTxEngine implements DtoHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoteMethodTxEngine.class);

    // waiting transaction, supports only one at the time
    private RemoteMethodTx remoteMethodTx;

    // Dto Sender
    private final DtoSender sender;

    public RemoteMethodTxEngine(DtoSender sender) {
        // this.queue = new LinkedList<RemoteMethodTx>();
        this.sender = sender;
    }


    /**
     * Set Response to a RemoteMethod Invocation
     * 
     * @param message to be processed
     * @return
     */
    @Override
    public TransportDto onDTO(TransportDto message) {
        KeypleDto keypleDto = message.getKeypleDTO();
        if (message.getKeypleDTO().isRequest()) {
            throw new IllegalArgumentException(
                    "RemoteMethodTxEngine expects a KeypleDto response. " + message.getKeypleDTO());
        }
        // only one operation is allowed at the time
        remoteMethodTx.asyncSetResponse(keypleDto);

        // re init remoteMethod
        remoteMethodTx = null;

        return message.nextTransportDTO(KeypleDtoHelper.NoResponse());
    }

    /**
     * Execute RemoteMethodTx
     * 
     * @param rm
     */
    public void register(final RemoteMethodTx rm) throws KeypleRemoteException {

        logger.debug("Register rm to engine : {}", rm);
        remoteMethodTx = rm;

        rm.setDto(sender);
    }

}
