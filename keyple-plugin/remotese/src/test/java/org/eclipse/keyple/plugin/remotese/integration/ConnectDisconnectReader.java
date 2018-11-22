package org.eclipse.keyple.plugin.remotese.integration;

import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderService;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderService;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderSession;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteReaderException;
import org.eclipse.keyple.plugin.remotese.transport.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.TransportNode;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalServer;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.util.Observable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

@RunWith(MockitoJUnitRunner.class)
public class ConnectDisconnectReader {

    private static final Logger logger = LoggerFactory.getLogger(ConnectDisconnectReader.class);

    TransportFactory factory;
    Observable.Observer observer;
    VirtualReaderService virtualReaderService;
    NativeReaderService nativeReaderService;

    @Before
    public void setTup(){
        logger.info("*** Init LocalTransportFactory");
        //use a local transport factory for testing purposes (only java calls between client and server)
        //only one client and one server
        factory = new LocalTransportFactory();

        logger.info("*** Bind Master Services");
        //bind Master services to server
        virtualReaderService = bindMaster(factory.getServer(), observer);

        logger.info("*** Bind Slave Services");
        //bind Slave services to client
        nativeReaderService = bindSlave(factory.getClient());

        observer = new Observable.Observer() {
            @Override
            public void update(Object o) {
                logger.debug("event received {}", o);
            }
        };

    }


    /**
     * Connect succesfully a reader
     * @throws Exception
     */
    @Test
    public void testConnect() throws Exception{
        final String NATIVE_READER_NAME = "testConnect";
        final String CLIENT_NODE_ID = "testConnectNodeId";

        ProxyReader nativeReader = createStubReader(NATIVE_READER_NAME);

        nativeReaderService.connectReader(nativeReader, CLIENT_NODE_ID);

        //assert that a virtual reader has been created
        VirtualReader virtualReader = (VirtualReader) virtualReaderService.getPlugin().getReaderByRemoteName(NATIVE_READER_NAME);
        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());

    }

    /**
     * Connect succesfully a reader
     * Disconnect successfully a reader
     * @throws Exception
     */
    @Test(expected = KeypleReaderNotFoundException.class)
    public void testConnectDisconnect() throws Exception{
        final String NATIVE_READER_NAME = "testDisconnect";
        final String CLIENT_NODE_ID = "testDisconnectNodeId";

        ProxyReader nativeReader = createStubReader(NATIVE_READER_NAME);

        //connect
        nativeReaderService.connectReader(nativeReader, CLIENT_NODE_ID);

        VirtualReader virtualReader = (VirtualReader) virtualReaderService.getPlugin().getReaderByRemoteName(NATIVE_READER_NAME);
        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());

        //disconnect
        nativeReaderService.disconnectReader(nativeReader, CLIENT_NODE_ID);

        //assert that the virtual reader has been destroyed
        VirtualReader virtualReader2 = (VirtualReader) virtualReaderService.getPlugin().getReaderByRemoteName(NATIVE_READER_NAME);
        //throws KeypleReaderNotFoundException
    }

    /**
     * Connect error : reader already exists
     * @throws Exception
     */
    @Test(expected = Throwable.class)
    public void testConnectError() throws Exception{
        final String NATIVE_READER_NAME = "testConnectError";
        final String CLIENT_NODE_ID = "testConnectErrorNodeId";

        ProxyReader nativeReader = createStubReader(NATIVE_READER_NAME);

        nativeReaderService.connectReader(nativeReader, CLIENT_NODE_ID);

        nativeReaderService.connectReader(nativeReader, CLIENT_NODE_ID);

    }





    /*
    HELPERS
     */

    public static VirtualReaderService bindMaster(TransportNode node, Observable.Observer observer){
        //Create Master services : virtualReaderService
        VirtualReaderService virtualReaderService =
                new VirtualReaderService(SeProxyService.getInstance(), node);

        // observe remote se plugin for events
        ReaderPlugin rsePlugin = virtualReaderService.getPlugin();
        ((Observable) rsePlugin).addObserver(observer);

        // Binds virtualReaderService to a
        virtualReaderService.bindDtoEndpoint(node);

        return virtualReaderService;
    }

    public static NativeReaderService bindSlave(TransportNode node){
        // Binds node for outgoing KeypleDto
        NativeReaderServiceImpl nativeReaderService = new NativeReaderServiceImpl(node);

        // Binds node for incoming KeypleDTo
        nativeReaderService.bindDtoEndpoint(node);

        return nativeReaderService;
    }

    public static ProxyReader createStubReader(String stubReaderName) throws InterruptedException, KeypleReaderNotFoundException {
        SeProxyService seProxyService = SeProxyService.getInstance();

        logger.info("Create Local StubPlugin");

        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = SeProxyService.getInstance().getPlugins();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        stubPlugin.plugStubReader(stubReaderName);

        Thread.sleep(1000);

        // get the created proxy reader
        return (StubReader) stubPlugin.getReader(stubReaderName);


    }


}