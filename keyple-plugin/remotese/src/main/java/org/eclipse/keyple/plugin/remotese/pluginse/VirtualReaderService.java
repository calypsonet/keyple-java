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
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
        TransportDto out = null;

        logger.debug("onDto {}", KeypleDtoHelper.toJson(keypleDTO));
        RemoteMethod method = RemoteMethod.get(keypleDTO.getAction());
        logger.debug("Remote Method {}", method);

        switch (method){
            case READER_CONNECT:
                if(keypleDTO.isRequest()){
                    logger.info("**** ACTION - READER_CONNECT ****");
                    out = new RmConnectReaderExecutor(this.plugin, this.dtoSender).execute(transportDto);
                }else{
                    throw new IllegalStateException("a READER_CONNECT response has been received by VirtualReaderService");
                }
                break;
            case READER_DISCONNECT:
                if(keypleDTO.isRequest()){
                    logger.info("**** ACTION - READER_DISCONNECT ****");
                    out = new RmDisconnectReaderExecutor(this.plugin).execute(transportDto);
                }else{
                    throw new IllegalStateException("a READER_DISCONNECT response has been received by VirtualReaderService");
                }
                break;
            case READER_EVENT:
                logger.info("**** ACTION - READER_EVENT ****");
                out = new RmEventExecutor(plugin).execute(transportDto);
                break;
            case READER_TRANSMIT:
                if(keypleDTO.isRequest()){
                    throw new IllegalStateException("a READER_TRANSMIT request has been received by VirtualReaderService");
                }else{
                    logger.info("**** RESPONSE RECEIVED - READER_TRANSMIT ****");
                    RemoteMethodParser<SeResponseSet> parser = new RmTransmitParser();
                    try {
                        VirtualReader reader = null;
                        reader = getReaderBySessionId(keypleDTO.getSessionId());

                        try{
                            SeResponseSet seResponseSet = parser.parseResponse(keypleDTO);
                            logger.debug("Receive responseSet from transmit {}", seResponseSet);
                            //transfer SeResponseSet to Virtual Reader (through its session)
                            reader.getSession().asyncSetSeResponseSet(seResponseSet, null);

                            // chain response with a seRequest if needed
                            out = isSeRequestToSendBack(transportDto);

                        }catch (KeypleRemoteReaderException e){
                            e.printStackTrace();
                            //propagate exception
                            reader.getSession().asyncSetSeResponseSet(null, e);
                        }

                    } catch (KeypleReaderNotFoundException e) {
                        //reader not found;
                        throw new IllegalStateException("Virtual Reader was not found while receiving a transmit response", e);
                    }
                }
                break;
            default:
                logger.debug("Default case");
        }
        return out;
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
                out = transportDto.nextTransportDTO(new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
                        JsonParser.getGson().toJson((virtualReader.getSession()).getSeRequestSet()),
                        true, virtualReader.getSession().getSessionId()));
            } else {
                // no response
                out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
            }

        } catch (KeypleReaderNotFoundException e) {
            logger.debug("Reader was not found by session", e);
            KeypleDto keypleDto = transportDto.getKeypleDTO();
            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(), e, keypleDto.getSessionId(),
                    keypleDto.getNativeReaderName(),keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
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
        for (ProxyReader reader : plugin.getReaders()) {

            if (((VirtualReader) reader).getSession().getSessionId().equals(sessionId)) {
                return (VirtualReader) reader;
            }
        }
        throw new KeypleReaderNotFoundException(
                "Reader session was not found for session : " + sessionId);
    }

}
