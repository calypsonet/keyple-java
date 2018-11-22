package org.eclipse.keyple.example.remote.local;

import org.eclipse.keyple.example.remote.calypso.DemoThreads;
import org.eclipse.keyple.example.remote.wspolling.WsPollingFactory;
import org.eclipse.keyple.plugin.remotese.transport.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportFactory;

public class DemoLocalMasterServer {

    public static void main(String[] args) throws Exception {

        Boolean isMasterServer = true; // DemoMaster is the server (and DemoSlave the Client)
        TransportFactory factory = new LocalTransportFactory();

        DemoThreads.startServer(isMasterServer, factory);
        Thread.sleep(1000);
        DemoThreads.startClient(!isMasterServer, factory);
    }

}
