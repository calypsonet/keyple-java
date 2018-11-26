package org.eclipse.keyple.plugin.remotese.nativese.method;

import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmTransmitExecutor extends RemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitExecutor.class);

    NativeReaderServiceImpl nativeReaderService;

    public RmTransmitExecutor(NativeReaderServiceImpl nativeReaderService){
        this.nativeReaderService = nativeReaderService;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();
        TransportDto out = null;
        SeResponseSet seResponseSet = null;

        //Extract info from keypleDto
        SeRequestSet seRequestSet =
                JsonParser.getGson().fromJson(keypleDto.getBody(), SeRequestSet.class);
        String nativeReaderName = keypleDto.getNativeReaderName();

        try {
            //find native reader by name
            ProxyReader reader = nativeReaderService.findLocalReader(nativeReaderName);

            // execute transmitSet
            seResponseSet = reader.transmitSet(seRequestSet);

            // prepare response
            String parseBody = JsonParser.getGson().toJson(seResponseSet, SeResponseSet.class);
            out = transportDto.nextTransportDTO(new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(), parseBody,
                    false, keypleDto.getSessionId(), nativeReaderName,
                    keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));

        } catch (KeypleReaderException e) {
            //if an exception occurs, send it into a keypleDto to the Master
            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(RemoteMethod.READER_TRANSMIT.getName(), e,
                    keypleDto.getSessionId(), nativeReaderName, keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
        }

        return out;
    }
}
