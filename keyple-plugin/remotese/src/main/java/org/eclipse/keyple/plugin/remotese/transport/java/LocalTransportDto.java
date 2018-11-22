package org.eclipse.keyple.plugin.remotese.transport.java;

import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;

public class LocalTransportDto implements TransportDto {

    KeypleDto keypleDto;
    LocalClient theClient;


    public LocalTransportDto(KeypleDto keypleDto, LocalClient theClient){
        this.keypleDto = keypleDto;
        this.theClient = theClient;
    }

    public LocalClient getTheClient(){
        return theClient;
    }

    @Override
    public KeypleDto getKeypleDTO() {
        return keypleDto;
    }

    @Override
    public TransportDto nextTransportDTO(KeypleDto keypleDto) {
        return new LocalTransportDto(keypleDto, theClient);
    }

    @Override
    public DtoSender getDtoSender() {
        //not used
        return null;
    }
}
