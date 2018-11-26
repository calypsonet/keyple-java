package org.eclipse.keyple.plugin.remotese.pluginse.method;

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;

public class RmTransmitParser implements RemoteMethodParser<SeResponseSet> {

    public RmTransmitParser() {}

    @Override
    public SeResponseSet parseResponse(KeypleDto keypleDto) throws KeypleRemoteReaderException {
        if(KeypleDtoHelper.containsException(keypleDto)){
            KeypleReaderException ex = JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteReaderException("An exception occurs while calling the remote method transmitSet", ex);
        }else{
            return JsonParser.getGson().fromJson(keypleDto.getBody(), SeResponseSet.class);
        }
    }
}
