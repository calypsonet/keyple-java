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
package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.plugin.remotese.pluginse.method.RmConnectReaderExecutor;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmDisconnectReaderExecutor;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmEventExecutor;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitParser;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Service to setDtoSender a RSE Plugin to a Transport Node
 */
public class VirtualReaderService implements DtoHandler {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderService.class);

    private final DtoSender dtoSender;
    private final RemoteSePlugin plugin;

    /**
     * Build a new VirtualReaderService, Entry point for incoming DTO in Master Manages
     * RemoteSePlugin lifecycle Manages Master Session Dispatch KeypleDTO
     *
     * @param seProxyService : SeProxyService
     * @param dtoSender : outgoing node to send Dto to Slave
     */
    public VirtualReaderService(SeProxyService seProxyService, DtoSender dtoSender) {
        this.dtoSender = dtoSender;

        // Instantiate Session Manager
        VirtualReaderSessionFactory sessionManager = new VirtualReaderSessionFactory();

        // Instantiate Plugin
        this.plugin = new RemoteSePlugin(sessionManager);
        seProxyService.addPlugin(this.plugin);
    }

    /**
     * Set this service as the Dto Dispatcher in your {@link TransportNode} todo : can't it be the
     * transport node that set the dispatcher instead?
     * 
     * @param node : incoming Dto point
     */
    public void bindDtoEndpoint(TransportNode node) {
        node.setDtoHandler(this);
    }

    /**
     * Retrieve the Rse Plugin todo : can't it be the SeProxyService?
     * 
     * @return the Remote Se Plugin managing the Virtual Readers
     */
    public RemoteSePlugin getPlugin() {
        return plugin;
    }

    /**
     * Handles incoming transportDTO
     * 
     * @param transportDto an incoming TransportDto (embeds a KeypleDto)
     * @return a Response transportDto (can be a NoResponse KeypleDto)
     */
    @Override
    public TransportDto onDTO(TransportDto transportDto) {

        KeypleDto keypleDTO = transportDto.getKeypleDTO();
        RemoteMethod method = RemoteMethod.get(keypleDTO.getAction());
        logger.trace("onDTO, Remote Method called : {} - isRequest : {} - keypleDto : {}", method,
                keypleDTO.isRequest(), KeypleDtoHelper.toJson(keypleDTO));

        switch (method) {
            case READER_CONNECT:
                if (keypleDTO.isRequest()) {
                    return new RmConnectReaderExecutor(this.plugin, this.dtoSender)
                            .execute(transportDto);
                } else {
                    throw new IllegalStateException(
                            "a READER_CONNECT response has been received by VirtualReaderService");
                }
            case READER_DISCONNECT:
                if (keypleDTO.isRequest()) {
                    return new RmDisconnectReaderExecutor(this.plugin).execute(transportDto);
                } else {
                    throw new IllegalStateException(
                            "a READER_DISCONNECT response has been received by VirtualReaderService");
                }
            case READER_EVENT:
                return new RmEventExecutor(plugin).execute(transportDto);
            case READER_TRANSMIT:
                if (keypleDTO.isRequest()) {
                    throw new IllegalStateException(
                            "a READER_TRANSMIT request has been received by VirtualReaderService");
                } else {
                    RemoteMethodParser<SeResponseSet> parser = new RmTransmitParser();
                    try {
                        VirtualReader reader = null;
                        reader = getReaderBySessionId(keypleDTO.getSessionId());

                        try {
                            SeResponseSet seResponseSet = parser.parseResponse(keypleDTO);
                            logger.debug("Receive responseSet from transmitSet {}", seResponseSet);
                            // transfer SeResponseSet to Virtual Reader (through its session)
                            reader.getSession().asyncSetSeResponseSet(seResponseSet, null);

                            // chain response with a seRequest if needed
                            return isSeRequestToSendBack(transportDto);

                        } catch (KeypleRemoteReaderException e) {
                            // e.printStackTrace();
                            // propagate exception
                            reader.getSession().asyncSetSeResponseSet(null, e);
                            return transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
                        }

                    } catch (KeypleReaderNotFoundException e) {
                        // reader not found;
                        throw new IllegalStateException(
                                "Virtual Reader was not found while receiving a transmitSet response",
                                e);
                    }
                }
            default:
                logger.debug("Default case");
                return transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
        }
    }



    /**
     * Attach a SeRequestSet to keypleDto response object if a seRequestSet object is pending in the
     * virtual reader session If not, returns the same keypleDto
     *
     * @param transportDto : response to be sent
     * @return enriched response
     */
    private TransportDto isSeRequestToSendBack(TransportDto transportDto) {
        TransportDto out = null;
        try {
            // retrieve reader by session
            VirtualReader virtualReader = (VirtualReader) plugin
                    .getReaderByRemoteName(transportDto.getKeypleDTO().getNativeReaderName());

            if ((virtualReader.getSession()).hasSeRequestSet()) {

                // send back seRequestSet
                out = transportDto
                        .nextTransportDTO(new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
                                JsonParser.getGson()
                                        .toJson((virtualReader.getSession()).getSeRequestSet()),
                                true, virtualReader.getSession().getSessionId()));
            } else {
                // no response
                out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
            }

        } catch (KeypleReaderNotFoundException e) {
            logger.debug("Reader was not found by session", e);
            KeypleDto keypleDto = transportDto.getKeypleDTO();
            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(),
                    e, keypleDto.getSessionId(), keypleDto.getNativeReaderName(),
                    keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
        }

        return out;
    }


    /**
     * Retrieve reader by its session Id
     * 
     * @param sessionId
     * @return VirtualReader matching the sessionId
     * @throws KeypleReaderNotFoundException
     */
    private VirtualReader getReaderBySessionId(String sessionId)
            throws KeypleReaderNotFoundException {
        for (SeReader reader : plugin.getReaders()) {

            if (((VirtualReader) reader).getSession().getSessionId().equals(sessionId)) {
                return (VirtualReader) reader;
            }
        }
        throw new KeypleReaderNotFoundException(
                "Reader session was not found for session : " + sessionId);
    }

}
