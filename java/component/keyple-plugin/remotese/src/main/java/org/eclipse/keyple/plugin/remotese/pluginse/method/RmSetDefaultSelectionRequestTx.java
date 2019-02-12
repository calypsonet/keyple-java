/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.pluginse.method;

import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.transaction.SelectionRequest;
import com.google.gson.JsonObject;

public class RmSetDefaultSelectionRequestTx extends RemoteMethodTx {

    private SelectionRequest selectionRequest;
    private ObservableReader.NotificationMode notificationMode;


    public RmSetDefaultSelectionRequestTx(SelectionRequest selectionRequest,
            ObservableReader.NotificationMode notificationMode, String nativeReaderName,
            String virtualReaderName, String sessionId, String clientNodeId) {
        super(sessionId, nativeReaderName, virtualReaderName, clientNodeId);
        this.selectionRequest = selectionRequest;
        this.notificationMode = notificationMode;

    }


    @Override
    public Object parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
        return new Object();

    }

    @Override
    public KeypleDto dto() {
        JsonObject body = new JsonObject();
        body.addProperty("selectionRequest", JsonParser.getGson().toJson(selectionRequest));
        body.addProperty("notificationMode", notificationMode.getName());

        return new KeypleDto(RemoteMethod.DEFAULT_SELECTION_REQUEST.getName(),
                JsonParser.getGson().toJson(body, JsonObject.class), true, sessionId,
                nativeReaderName, virtualReaderName, clientNodeId);

    }
}