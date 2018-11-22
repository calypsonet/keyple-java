package org.eclipse.keyple.plugin.remotese.pluginse.method;

import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;

public class RmEventExecutor extends RemoteMethodExecutor {

    RemoteSePlugin plugin;

    public RmEventExecutor(RemoteSePlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto =  transportDto.getKeypleDTO();

        // parseResponse body
        ReaderEvent event =
                JsonParser.getGson().fromJson(keypleDto.getBody(), ReaderEvent.class);

        // dispatch reader event
        plugin.onReaderEvent(event, keypleDto.getSessionId());

        try{
            VirtualReader virtualReader = (VirtualReader) plugin.getReaderByRemoteName(keypleDto.getNativeReaderName());

            // chain response with a seRequest if needed
            if ((virtualReader.getSession()).hasSeRequestSet()) {

                // send back seRequestSet
                return transportDto.nextTransportDTO(
                        new KeypleDto(
                                RemoteMethod.READER_TRANSMIT.getName(),
                                JsonParser.getGson().toJson((virtualReader.getSession()).getSeRequestSet()),
                                true,
                                virtualReader.getSession().getSessionId()));
            }else{
                return transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
            }

        }catch (KeypleReaderNotFoundException e){
           return transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(
                    keypleDto.getAction(),
                    e,
                    keypleDto.getSessionId(),
                    keypleDto.getNativeReaderName(),
                    keypleDto.getVirtualReaderName(),
                    keypleDto.getNodeId()));
        }
    }
}
