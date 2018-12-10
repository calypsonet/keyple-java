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
package org.eclipse.keyple.plugin.remotese.pluginse.method;

import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RmConnectReaderExecutor implements RemoteMethodExecutor {


    private final RemoteSePlugin plugin;
    private final DtoSender dtoSender;

    public RmConnectReaderExecutor(RemoteSePlugin plugin, DtoSender dtoSender) {
        this.plugin = plugin;
        this.dtoSender = dtoSender;
    }


    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        // parseResponse msg
        String nativeReaderName = keypleDto.getNativeReaderName();
        String clientNodeId = keypleDto.getNodeId();

        // create a virtual Reader
        VirtualReader virtualReader = null;
        try {
            virtualReader = (VirtualReader) this.plugin.createVirtualReader(clientNodeId,
                    nativeReaderName, this.dtoSender);
            // response
            JsonObject respBody = new JsonObject();
            respBody.add("statusCode", new JsonPrimitive(0));
            return transportDto.nextTransportDTO(new KeypleDto(keypleDto.getAction(),
                    respBody.toString(), false, virtualReader.getSession().getSessionId(),
                    nativeReaderName, virtualReader.getName(), clientNodeId));
        } catch (KeypleReaderException e) {
            // virtual reader for remote reader already exists
            e.printStackTrace();
            // send the exception
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(),
                    e, null, nativeReaderName, null, clientNodeId));

        }
    }
}
