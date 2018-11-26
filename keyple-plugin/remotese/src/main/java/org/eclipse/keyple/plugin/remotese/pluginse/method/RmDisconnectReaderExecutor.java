package org.eclipse.keyple.plugin.remotese.pluginse.method;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmDisconnectReaderExecutor extends RemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmDisconnectReaderExecutor.class);


    RemoteSePlugin plugin;

    public RmDisconnectReaderExecutor(RemoteSePlugin plugin){
        this.plugin = plugin;
    }


    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();

        String nativeReaderName = keypleDto.getNativeReaderName();
        String clientNodeId = keypleDto.getNodeId();

        try {
            plugin.disconnectRemoteReader(nativeReaderName);// todo find by reader + nodeId
            return transportDto.nextTransportDTO(new KeypleDto(RemoteMethod.READER_DISCONNECT.getName(),
                    "{}", false, null,
                    nativeReaderName,null , clientNodeId));
        } catch (KeypleReaderNotFoundException e) {
            logger.error("Impossible to disconnect reader "+nativeReaderName,e);
            return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(RemoteMethod.READER_DISCONNECT.getName(),
                    e,keypleDto.getSessionId(),keypleDto.getNativeReaderName(),keypleDto.getVirtualReaderName(),keypleDto.getNodeId()));
        }

    }
}
