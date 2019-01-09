package org.eclipse.keyple.plugin.remotese.nativese.method;

import com.google.gson.JsonObject;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderService;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.plugin.AbstractSelectionLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmConnectReaderTx extends RemoteMethodTx<String> {


    private final ProxyReader localReader;
    private final String clientNodeId;
    private final NativeReaderService nativeReaderService;


    public RmConnectReaderTx(String sessionId, String nativeReaderName, String virtualReaderName,
                             String clientNodeId,
                             ProxyReader localReader,
                             String clientNodeId1,
                             NativeReaderService nativeReaderService) {
        super(sessionId, nativeReaderName, virtualReaderName, clientNodeId);
        this.localReader = localReader;
        this.clientNodeId = clientNodeId1;
        this.nativeReaderService = nativeReaderService;
    }

    private static final Logger logger = LoggerFactory.getLogger(RmConnectReaderTx.class);

    @Override
    public String parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
        String nativeReaderName = keypleDto.getNativeReaderName();

        // if reader connection thrown an exception
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method connecReader", ex);
        } else {
            //if dto does not contain an exception
            try {
                // configure nativeReaderService to propagate reader events if the reader is observable

                //find the local reader by name
                ProxyReader localReader = (ProxyReader) nativeReaderService.findLocalReader(nativeReaderName);

                if (localReader instanceof AbstractSelectionLocalReader) {
                    logger.debug("Register NativeReaderService as an observer for native reader {}",
                            localReader.getName());
                    ((AbstractSelectionLocalReader) localReader).addObserver((ObservableReader.ReaderObserver) nativeReaderService);
                }

                //retrieve sessionId from keypleDto
                JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
                //Integer statusCode = body.get("statusCode").getAsInt();
                String sessionId = body.get("sessionId").getAsString();

                // sessionId is returned here
                return sessionId;

            } catch (KeypleReaderNotFoundException e) {
                logger.warn(
                        "While receiving a confirmation of Rse connection, local reader was not found");
                throw  new KeypleRemoteException("While receiving a confirmation of Rse connection, local reader was not found");
            }
        }
    }

    @Override
    public KeypleDto dto() {
        return new KeypleDto(RemoteMethod.READER_CONNECT.getName(), "{}", true, null,
                localReader.getName(), null, clientNodeId);
    }
}
