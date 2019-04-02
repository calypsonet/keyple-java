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
package org.eclipse.keyple.example.remote.application;

import org.eclipse.keyple.example.remote.transport.wspolling.client.WsPollingFactory;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;

/**
 * Demo Web Service with jdk http client library The master device uses the webservice client
 * whereas the slave device uses the webservice server
 */
public class Demo_Webservice_MasterClient {

    public static void main(String[] args) throws Exception {


        final String CLIENT_NODE_ID =  "Demo_Webservice_MasterClient1";
        final String SERVER_NODE_ID =  "Demo_Webservice_MasterClientServer1";

        // Create a HTTP Web Polling factory
        TransportFactory factory = new WsPollingFactory(SERVER_NODE_ID);

        // Launch the Server thread
        // Server is slave
        Demo_Threads.startServer(false, factory,CLIENT_NODE_ID);

        Thread.sleep(1000);

        // Launch the client thread
        // Client is Master
        Demo_Threads.startClient(true, factory,CLIENT_NODE_ID);
    }
}
