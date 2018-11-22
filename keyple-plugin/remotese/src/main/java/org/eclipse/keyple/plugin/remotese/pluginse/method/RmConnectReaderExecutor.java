package org.eclipse.keyple.plugin.remotese.pluginse.method;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;

public class RmConnectReaderExecutor extends RemoteMethodExecutor {

    RemoteSePlugin plugin;
    DtoSender dtoSender;

    public RmConnectReaderExecutor(RemoteSePlugin plugin, DtoSender dtoSender){
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
            //send the exception
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(),
                    e, null, nativeReaderName, null , clientNodeId));

        }
    }
}
