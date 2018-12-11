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
package org.eclipse.keyple.plugin.remotese.nativese;


import org.eclipse.keyple.plugin.remotese.nativese.method.RmConnectReaderInvoker;
import org.eclipse.keyple.plugin.remotese.nativese.method.RmConnectReaderParser;
import org.eclipse.keyple.plugin.remotese.nativese.method.RmDisconnectReaderInvoker;
import org.eclipse.keyple.plugin.remotese.nativese.method.RmTransmitExecutor;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Native Service to manage local reader and connect them to Remote Service
 *
 */
public class NativeReaderServiceImpl
        implements NativeReaderService, DtoHandler, ObservableReader.ReaderObserver {

    private static final Logger logger = LoggerFactory.getLogger(NativeReaderServiceImpl.class);

    private final DtoSender dtoSender;
    private final SeProxyService seProxyService;
    // private final NseSessionManager nseSessionManager;

    /**
     * Constructor
     * 
     * @param dtoSender : Define which DTO sender will be called when a DTO needs to be sent.
     */
    public NativeReaderServiceImpl(DtoSender dtoSender) {
        this.seProxyService = SeProxyService.getInstance();
        this.dtoSender = dtoSender;
        // this.nseSessionManager = new NseSessionManager();
    }


    /**
     * HandleDTO from a TransportNode onDto() method will be called by the transportNode
     * 
     * @param node : network entry point that receives DTO
     */
    public void bindDtoEndpoint(TransportNode node) {
        node.setDtoHandler(this);// incoming traffic
    }

    /**
     * Dispatch a Keyple DTO to the right Native Reader. {@link DtoHandler}
     * 
     * @param transportDto to be processed
     * @return Keyple DTO to be sent back
     */
    @Override
    public TransportDto onDTO(TransportDto transportDto) {

        KeypleDto keypleDTO = transportDto.getKeypleDTO();
        TransportDto out;

        logger.trace("onDto {}", KeypleDtoHelper.toJson(keypleDTO));

        RemoteMethod method = RemoteMethod.get(keypleDTO.getAction());
        logger.debug("Remote Method called : {} - isRequest : {}", method, keypleDTO.isRequest());

        switch (method) {
            case READER_CONNECT:
                // process READER_CONNECT response
                if (keypleDTO.isRequest()) {
                    throw new IllegalStateException(
                            "a READER_CONNECT request has been received by NativeReaderService");
                } else {
                    try {
                        RemoteMethodParser<String> rmConnectReaderParser =
                                new RmConnectReaderParser(this);
                        String sessionId = rmConnectReaderParser.parseResponse(keypleDTO);
                        logger.info(
                                "Native Reader {} has been connected to Master with sessionId {}",
                                keypleDTO.getNativeReaderName(), sessionId);
                    } catch (KeypleRemoteReaderException e) {
                        e.printStackTrace();
                    }
                    out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
                }
                break;

            case READER_DISCONNECT:
                // process READER_DISCONNECT response
                if (keypleDTO.isRequest()) {
                    throw new IllegalStateException(
                            "a READER_DISCONNECT request has been received by NativeReaderService");
                } else {
                    logger.info("Native Reader {} has been disconnected from Master",
                            keypleDTO.getNativeReaderName());
                    out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
                }
                break;


            case READER_TRANSMIT:
                // must be a request
                if (keypleDTO.isRequest()) {
                    RemoteMethodExecutor rmTransmit = new RmTransmitExecutor(this);
                    out = rmTransmit.execute(transportDto);
                } else {
                    throw new IllegalStateException(
                            "a READER_TRANSMIT response has been received by NativeReaderService");
                }
                break;


            default:

                logger.warn("**** ERROR - UNRECOGNIZED ****");
                logger.warn("Receive unrecognized message action : {} {} {} {}",
                        keypleDTO.getAction(), keypleDTO.getSessionId(), keypleDTO.getBody(),
                        keypleDTO.isRequest());
                throw new IllegalStateException(
                        "a  ERROR - UNRECOGNIZED request has been received by NativeReaderService");
        }

        logger.debug("onDto response to be sent {}", KeypleDtoHelper.toJson(out.getKeypleDTO()));
        return out;


    }


    /**
     * Connect a local reader to Remote SE Plugin {@link NativeReaderService}
     * 
     * @param clientNodeId : a chosen but unique terminal id (i.e AndroidDevice2)
     * @param localReader : native reader to be connected
     */
    @Override
    public void connectReader(ProxyReader localReader, String clientNodeId)
            throws KeypleRemoteException {
        logger.info("connectReader {} from device {}", localReader.getName(), clientNodeId);
        dtoSender.sendDTO(new RmConnectReaderInvoker(localReader, clientNodeId).dto());
    }

    @Override
    public void disconnectReader(ProxyReader localReader, String clientNodeId)
            throws KeypleRemoteException {
        logger.info("disconnectReader {} from device {}", localReader.getName(), clientNodeId);

        dtoSender.sendDTO(new RmDisconnectReaderInvoker(localReader, clientNodeId).dto());

        // stop propagating the local reader events
        ((AbstractObservableReader) localReader).removeObserver(this);

    }

    /**
     * Internal method to find a local reader by its name across multiple plugins
     * 
     * @param nativeReaderName : name of the reader to be found
     * @return found reader if any
     * @throws KeypleReaderNotFoundException if not reader were found with this name
     */
    public ProxyReader findLocalReader(String nativeReaderName)
            throws KeypleReaderNotFoundException {
        logger.debug("Find local reader by name {} in {} plugin(s)", nativeReaderName,
                seProxyService.getPlugins().size());
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            try {
                return (ProxyReader) plugin.getReader(nativeReaderName);
            } catch (KeypleReaderNotFoundException e) {
                // continue
            }
        }
        throw new KeypleReaderNotFoundException(nativeReaderName);
    }

    // NativeReaderService

    /**
     * Do not call this method directly This method is called by a Observable<{@link ReaderEvent}>
     * 
     * @param event event to be propagated to master device
     */
    @Override
    public void update(ReaderEvent event) {
        logger.info("update Reader Event {}", event.getEventType());

        // retrieve last sessionId known for this reader
        // String sessionId = nseSessionManager.getLastSession(event.getReaderName());

        // construct json data
        String data = JsonParser.getGson().toJson(event);

        try {
            dtoSender.sendDTO(new KeypleDto(RemoteMethod.READER_EVENT.getName(), data, true, null,
                    event.getReaderName(), null, this.dtoSender.getNodeId()));
        } catch (KeypleRemoteException e) {
            logger.error("Event " + event.toString()
                    + " could not be sent though Remote Service Interface", e);
        }

    }


}
