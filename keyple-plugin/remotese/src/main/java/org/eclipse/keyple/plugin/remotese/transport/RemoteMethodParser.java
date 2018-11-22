package org.eclipse.keyple.plugin.remotese.transport;

import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteException;

public interface RemoteMethodParser<T> {

    T parseResponse(KeypleDto keypleDto) throws KeypleRemoteReaderException;
}
