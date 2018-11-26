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
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.SeResponseSet;

public class RmTransmitParser implements RemoteMethodParser<SeResponseSet> {

    public RmTransmitParser() {}

    @Override
    public SeResponseSet parseResponse(KeypleDto keypleDto) throws KeypleRemoteReaderException {
        if (KeypleDtoHelper.containsException(keypleDto)) {
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteReaderException(
                    "An exception occurs while calling the remote method transmitSet", ex);
        } else {
            return JsonParser.getGson().fromJson(keypleDto.getBody(), SeResponseSet.class);
        }
    }
}
