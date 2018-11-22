package org.eclipse.keyple.plugin.remotese.nativese.method;

import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethodInvoker;
import org.eclipse.keyple.seproxy.ProxyReader;

public class RmConnectReaderInvoker implements RemoteMethodInvoker {

    ProxyReader localReader;
    String clientNodeId;

    public RmConnectReaderInvoker(ProxyReader localReader, String clientNodeId){
        this.localReader = localReader;
        this.clientNodeId = clientNodeId;
    }

    @Override
    public KeypleDto dto() {
        return new KeypleDto(RemoteMethod.READER_CONNECT.getName(), "{}", true, null,
                localReader.getName(), null, clientNodeId);
    }
}
