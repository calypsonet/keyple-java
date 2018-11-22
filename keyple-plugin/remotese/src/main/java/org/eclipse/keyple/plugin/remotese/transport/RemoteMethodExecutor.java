package org.eclipse.keyple.plugin.remotese.transport;

import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;

abstract public class RemoteMethodExecutor{

    abstract public TransportDto execute(TransportDto transportDto);

//    protected TransportDto isSeRequestToSendBack(TransportDto transportDto) {
//        TransportDto out = null;
//        try {
//            // retrieve reader by session
//            VirtualReader virtualReader = (VirtualReader) plugin
//                    .getReaderByRemoteName(transportDto.getKeypleDTO().getNativeReaderName());
//
//            if ((virtualReader.getSession()).hasSeRequestSet()) {
//
//                // send back seRequestSet
//                out = transportDto.nextTransportDTO(new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
//                        JsonParser.getGson().toJson((virtualReader.getSession()).getSeRequestSet()),
//                        true, virtualReader.getSession().getSessionId()));
//            } else {
//                // no response
//                out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
//            }
//
//        } catch (KeypleReaderNotFoundException e) {
//            KeypleDto keypleDto = transportDto.getKeypleDTO();
//            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(keypleDto.getAction(), e, keypleDto.getSessionId(),
//                    keypleDto.getNativeReaderName(),keypleDto.getVirtualReaderName(), keypleDto.getNodeId()));
//        }
//
//        return out;
//    }


}