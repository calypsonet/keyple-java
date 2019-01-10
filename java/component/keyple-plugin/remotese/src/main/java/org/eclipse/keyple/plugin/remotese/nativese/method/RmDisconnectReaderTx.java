package org.eclipse.keyple.plugin.remotese.nativese.method;

import com.google.gson.JsonObject;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmDisconnectReaderTx extends RemoteMethodTx<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(RmDisconnectReaderTx.class);


    public RmDisconnectReaderTx(String sessionId, String nativeReaderName, String slaveNodeId) {
        super(sessionId,nativeReaderName,"",slaveNodeId);
    }

    @Override
    public Boolean parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
        // if reader connection thrown an exception
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method disconnectReader", ex);
        }else{
            JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
            return body.get("status").getAsBoolean();
        }

    }

    @Override
    public KeypleDto dto() {
        JsonObject body = new JsonObject();
        body.addProperty("sessionId", sessionId);

        return new KeypleDto(RemoteMethod.READER_DISCONNECT.getName(),
                JsonParser.getGson().toJson(body, JsonObject.class),
                true,
                null,
                nativeReaderName,
                null,
                clientNodeId);
    }
}
