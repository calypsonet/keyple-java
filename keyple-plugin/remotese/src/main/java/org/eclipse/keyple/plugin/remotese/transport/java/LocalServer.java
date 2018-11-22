package org.eclipse.keyple.plugin.remotese.transport.java;

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side of the 1 to 1 local transport for unit testing purposes
 * only one server, only one client initied by the {@link LocalTransportFactory}
 */
public class LocalServer implements ServerNode {

    private static final Logger logger = LoggerFactory.getLogger(LocalServer.class);

    LocalClient theClient;
    DtoHandler dtoHandler;

    public LocalServer(){
    }

    public void onLocalMessage(TransportDto transportDto){
        theClient = ((LocalTransportDto) transportDto).getTheClient();

        if (dtoHandler != null) {
            TransportDto response = dtoHandler.onDTO(new LocalTransportDto(transportDto.getKeypleDTO(), theClient));
            //send back response
            this.sendDTO(response);
        }else{
            throw new IllegalStateException("no DtoHanlder defined");
        }
    }

    @Override
    public void start() {
        logger.info("Local server start");
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
            //send keypleDto to the unique client
            theClient.onLocalMessage(transportDto.getKeypleDTO());
        }
    }

    @Override
    public void sendDTO(KeypleDto keypleDto) {
        if (KeypleDtoHelper.isNoResponse(keypleDto)) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {
            //send keypleDto to the unique client
            theClient.onLocalMessage(keypleDto);
        }
    }

    @Override
    public String getNodeId() {
        return "localServer1";
    }

    @Override
    public void update(KeypleDto event) {
        sendDTO(event);
    }
}
