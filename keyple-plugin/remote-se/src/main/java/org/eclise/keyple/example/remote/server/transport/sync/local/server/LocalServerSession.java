/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.sync.local.server;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.sync.SyncClientListener;
import org.eclise.keyple.example.remote.server.transport.sync.SyncReaderSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerSession implements SyncReaderSession {

    private static final Logger logger = LoggerFactory.getLogger(LocalServerSession.class);

    String sessionId;

    @Override
    public String getSessionId() {
        return sessionId;
    }

    SyncClientListener client;

    public LocalServerSession() {
        logger.info("Constructor empty");
    }

    public void setClientListener(SyncClientListener _client) {
        logger.info("Constructor with client listener {}", client);
        client = _client;
    }

    public SyncClientListener getClientListener() {
        return client;
    }


    @Override
    public String getName() {
        logger.debug("getName");
        return client.onGetName();
    }

    @Override
    public boolean isSePresent() {
        logger.debug("isSePresent");
        return client.onIsSePresent();
    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) {
        logger.debug("transmit {}", seApplicationRequest);
        return client.onTransmit(seApplicationRequest);
    }


    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        logger.debug("addSeProtocolSetting {}", seProtocolSetting);
        client.onAddSeProtocolSetting(seProtocolSetting);
    }

    @Override
    public Boolean hasSeRequestSet() {
        return null;
    }

}