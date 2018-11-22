package org.eclipse.keyple.plugin.remotese.nativese.method;

import com.google.gson.JsonObject;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteReaderException;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethodParser;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmConnectReaderParser implements RemoteMethodParser<String> {

    private static final Logger logger = LoggerFactory.getLogger(RmConnectReaderParser.class);


    NativeReaderServiceImpl nativeReaderService;

    public RmConnectReaderParser(NativeReaderServiceImpl nativeReaderService){
        this.nativeReaderService = nativeReaderService;
    }


    @Override
    public String parseResponse(KeypleDto keypleDto) throws KeypleRemoteReaderException {

        JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);

        Integer statusCode = body.get("statusCode").getAsInt();
        String nativeReaderName = keypleDto.getNativeReaderName();

        // reader connection was a success
        if (statusCode == 0) {
            try {
                // observe reader to propagate reader events
                ProxyReader localReader = nativeReaderService.findLocalReader(nativeReaderName);
                if (localReader instanceof AbstractObservableReader) {
                    logger.debug(
                            "Add NativeReaderServiceImpl as an observer for native reader {}",
                            localReader.getName());
                    ((AbstractObservableReader) localReader).addObserver(nativeReaderService);
                }
                // todo store sessionId in reader as a parameter?
                // nseSessionManager.addNewSession(sessionId, localReader.getName());

            } catch (KeypleReaderNotFoundException e) {
                logger.warn(
                        "While receiving a confirmation of Rse connection, local reader was not found");
            }
            return keypleDto.getSessionId();
        } else {
            logger.warn("Receive a error statusCode {} {}", statusCode,
                    KeypleDtoHelper.toJson(keypleDto));
            throw new KeypleRemoteReaderException("Receive a error statusCode from connect method");
        }
    }
}
