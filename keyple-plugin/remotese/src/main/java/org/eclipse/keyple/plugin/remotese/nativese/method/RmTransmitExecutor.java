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
package org.eclipse.keyple.plugin.remotese.nativese.method;

import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmTransmitExecutor extends RemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitExecutor.class);

    NativeReaderServiceImpl nativeReaderService;

    public RmTransmitExecutor(NativeReaderServiceImpl nativeReaderService) {
        this.nativeReaderService = nativeReaderService;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();
        TransportDto out = null;
        SeResponseSet seResponseSet = null;

        // Extract info from keypleDto
        SeRequestSet seRequestSet =
                JsonParser.getGson().fromJson(keypleDto.getBody(), SeRequestSet.class);
        String nativeReaderName = keypleDto.getNativeReaderName();

        try {
            // find native reader by name
            ProxyReader reader =
                    (ProxyReader) nativeReaderService.findLocalReader(nativeReaderName);

            // execute transmitSet
            seResponseSet = reader.transmitSet(seRequestSet);

            // prepare response
            String parseBody = JsonParser.getGson().toJson(seResponseSet, SeResponseSet.class);
            out = transportDto
                    .nextTransportDTO(new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
                            parseBody, false, keypleDto.getSessionId(), nativeReaderName,
                            keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));

        } catch (KeypleReaderException e) {
            // if an exception occurs, send it into a keypleDto to the Master
            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(
                    RemoteMethod.READER_TRANSMIT.getName(), e, keypleDto.getSessionId(),
                    nativeReaderName, keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
        }

        return out;
    }
}
