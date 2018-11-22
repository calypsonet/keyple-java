package org.eclipse.keyple.plugin.remotese.transport.java;

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side of the 1 to 1 local transport for unit testing purposes
 * only one server, only one client initied by the {@link LocalTransportFactory}
 */
public class LocalClient implements ClientNode {

    private static final Logger logger = LoggerFactory.getLogger(LocalClient.class);
    private LocalServer theServer;
    DtoHandler dtoHandler;

    public LocalClient(LocalServer server){
        this.theServer = server;
    }

    public void onLocalMessage(KeypleDto keypleDto){
        if (dtoHandler != null) {
            TransportDto response = dtoHandler.onDTO(new LocalTransportDto(keypleDto, this));
            //send back response
            this.sendDTO(response);
        }else{
            throw new IllegalStateException("no DtoHanlder defined");
        }
    }

    @Override
    public void setDtoHandler(DtoHandler handler) {
        this.dtoHandler = handler;
    }

    @Override
    public void sendDTO(TransportDto transportDto) {
        if (KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {
            //send keypleDto to the server
            theServer.onLocalMessage(transportDto);
        }
    }

    @Override
    public void sendDTO(KeypleDto keypleDto) {
        if (KeypleDtoHelper.isNoResponse(keypleDto)) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {
            //send keypleDto to the server
            theServer.onLocalMessage(new LocalTransportDto(keypleDto,this));
        }
    }

    @Override
    public String getNodeId() {
        return "localClient1";
    }

    @Override
    public void update(KeypleDto event) {
        sendDTO(event);
    }


    @Override
    public void connect() {
        //dummy
        logger.info("Connect Local Client");
    }

    @Override
    public void disconnect() {
        //dummy
        logger.info("Disconnect Local Client");
    }

}
