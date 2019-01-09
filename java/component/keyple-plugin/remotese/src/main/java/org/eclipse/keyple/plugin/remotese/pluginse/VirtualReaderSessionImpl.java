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
package org.eclipse.keyple.plugin.remotese.pluginse;


import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage RSE Reader Session
 */
public class VirtualReaderSessionImpl extends Observable<KeypleDto>
        implements VirtualReaderSession {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderSessionImpl.class);

    private final String sessionId;
    private final String slaveNodeId;



    // constructor
    public VirtualReaderSessionImpl(String sessionId, String slaveNodeId) {

        this.sessionId = sessionId;
        this.slaveNodeId = slaveNodeId;
    }


    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getSlaveNodeId() {
        return slaveNodeId;
    }


}
