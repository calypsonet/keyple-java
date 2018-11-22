package org.eclipse.keyple.plugin.remotese.pluginse.method;

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.SeResponseSet;

public class RmTransmitParser implements RemoteMethodParser<SeResponseSet> {

    public RmTransmitParser() {}

    @Override
    public SeResponseSet parseResponse(KeypleDto keypleDto) throws KeypleRemoteReaderException {
        if(KeypleDtoHelper.containsException(keypleDto)){
            Throwable ex = JsonParser.getGson().fromJson(keypleDto.getBody(), Throwable.class);
            throw new KeypleRemoteReaderException("An exception occurs while calling the remote method transmit", ex);
        }else{
            return JsonParser.getGson().fromJson(keypleDto.getBody(), SeResponseSet.class);
        }
    }
}
