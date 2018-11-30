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

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.message.SeRequestSet;

public class RmTransmitInvoker implements RemoteMethodInvoker {

    SeRequestSet seRequestSet;
    String sessionId;
    String nativeReaderName;
    String virtualReaderName;
    String clientNodeId;


    public RmTransmitInvoker(SeRequestSet seRequestSet, String sessionId, String nativeReaderName,
            String virtualReaderName, String clientNodeId) {
        this.seRequestSet = seRequestSet;
        this.sessionId = sessionId;
        this.nativeReaderName = nativeReaderName;
        this.virtualReaderName = virtualReaderName;
        this.clientNodeId = clientNodeId;
    }

    @Override
    public KeypleDto dto() {
        return new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
                JsonParser.getGson().toJson(seRequestSet, SeRequestSet.class), true, this.sessionId,
                this.nativeReaderName, this.virtualReaderName, this.clientNodeId);
    }


}
