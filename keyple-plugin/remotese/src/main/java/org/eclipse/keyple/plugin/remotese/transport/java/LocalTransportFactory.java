package org.eclipse.keyple.plugin.remotese.transport.java;


import org.eclipse.keyple.plugin.remotese.transport.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.TransportFactory;

import java.io.IOException;

public class LocalTransportFactory extends TransportFactory {

    /*
    static private LocalTransportFactory instance = new LocalTransportFactory();
    private LocalTransportFactory(){}
    static public LocalTransportFactory instance(){
        return instance;
    }
    */

    LocalClient theClient;
    LocalServer theServer;

    public LocalTransportFactory(){
        theServer = new LocalServer();
        theClient = new LocalClient(theServer);
    }

    @Override
    public ClientNode getClient() {
        return theClient;
    }

    @Override
    public ServerNode getServer()  {
        return theServer;
    }
}
