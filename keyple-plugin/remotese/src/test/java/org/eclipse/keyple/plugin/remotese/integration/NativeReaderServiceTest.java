package org.eclipse.keyple.plugin.remotese.integration;

import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderService;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalClient;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportFactory;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.util.Observable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class NativeReaderServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(NativeReaderServiceTest.class);

    //Real objects
    TransportFactory factory;
    Observable.Observer observer;
    VirtualReaderService virtualReaderService;

    //Spy Object
    NativeReaderServiceImpl nativeReaderSpy;

    @Before
    public void setTup() throws IOException {
        logger.info("*** Init LocalTransportFactory");
        //use a local transport factory for testing purposes (only java calls between client and server)
        //only one client and one server
        factory = new LocalTransportFactory();
        observer = new Observable.Observer() {
            @Override
            public void update(Object o) {
                logger.debug("event received {}", o);
            }
        };


        logger.info("*** Bind Master Services");
        //bind Master services to server
        virtualReaderService = Integration.bindMaster(factory.getServer(), observer);

        logger.info("*** Bind Slave Services");
        //bind Slave services to client
        nativeReaderSpy = Integration.bindSlaveSpy(factory.getClient());


    }
    /*
    CONNECT METHOD
     */


    /**
     * Connect successfully a reader
     * @throws Exception
     */
    @Test
    public void testOKConnect() throws Exception{
        final String NATIVE_READER_NAME = "testConnect";
        final String CLIENT_NODE_ID = "testConnectNodeId";

        ProxyReader nativeReader = Integration.createStubReader(NATIVE_READER_NAME);

        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);

        //assert that a virtual reader has been created
        VirtualReader virtualReader = (VirtualReader) virtualReaderService.getPlugin().getReaderByRemoteName(NATIVE_READER_NAME);
        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());

    }

    /**
     * Connect error : reader already exists
     * @throws Exception
     */
    @Test
    public void testKOConnectError() throws Exception{
        final String NATIVE_READER_NAME = "testConnectError";
        final String CLIENT_NODE_ID = "testConnectErrorNodeId";

        ProxyReader nativeReader = Integration.createStubReader(NATIVE_READER_NAME);

        //first connectReader is successful
        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);

        //assert an exception will be contained into keypleDto response
        doAnswer(Integration.assertContainsException()).when(nativeReaderSpy).onDTO(ArgumentMatchers.<TransportDto>any());

        //should throw a DTO with an exception in master side KeypleReaderException
        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);
    }

    /**
     * Connect error : impossible to send DTO
     * @throws Exception
     */
    @Test(expected = KeypleRemoteException.class)
    public void testKOConnectServerError() throws Exception{
        final String NATIVE_READER_NAME = "testConnectServerError";
        final String CLIENT_NODE_ID = "testConnectServerErrorNodeId";

        ProxyReader nativeReader = Integration.createStubReader(NATIVE_READER_NAME);

        //bind Slave to faulty client
        nativeReaderSpy = Integration.bindSlaveSpy(new LocalClient(null));

        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);
        //should throw a KeypleRemoteException in slave side
    }

    /*
    DISCONNECT METHOD
     */

    /**
     * Disconnect successfully a reader
     * @throws Exception
     */
    @Test
    public void testOKConnectDisconnect() throws Exception{
        final String NATIVE_READER_NAME = "testDisconnect";
        final String CLIENT_NODE_ID = "testDisconnectNodeId";

        ProxyReader nativeReader = Integration.createStubReader(NATIVE_READER_NAME);

        //connect
        nativeReaderSpy.connectReader(nativeReader, CLIENT_NODE_ID);

        VirtualReader virtualReader = (VirtualReader) virtualReaderService.getPlugin().getReaderByRemoteName(NATIVE_READER_NAME);
        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());

        //disconnect
        nativeReaderSpy.disconnectReader(nativeReader, CLIENT_NODE_ID);

        //assert that the virtual reader has been destroyed
        Assert.assertEquals(0, virtualReaderService.getPlugin().getReaders().size());
    }


    /**
     * Disconnect Error : reader not connected
     * @throws Exception
     */
    @Test
    public void testKODisconnectNotFoundError() throws Exception{
        final String NATIVE_READER_NAME = "testDisconnectNotFoundError";
        final String CLIENT_NODE_ID = "testDisconnectNotFoundErrorNodeId";

        ProxyReader nativeReader = Integration.createStubReader(NATIVE_READER_NAME);

        //assert an exception will be contained into keypleDto response
        doAnswer(Integration.assertContainsException()).when(nativeReaderSpy).onDTO(ArgumentMatchers.<TransportDto>any());

        //disconnect
        nativeReaderSpy.disconnectReader(nativeReader, CLIENT_NODE_ID);
        //should throw exception in master side KeypleNotFound

    }


    /**
     * Disconnect error : impossible to send DTO
     * @throws Exception
     */
    @Test(expected = KeypleRemoteException.class)
    public void testKODisconnectServerError() throws Exception{
        final String NATIVE_READER_NAME = "testDisconnectServerError";
        final String CLIENT_NODE_ID = "testDisconnectServerErrorNodeId";

        ProxyReader nativeReader = Integration.createStubReader(NATIVE_READER_NAME);

        //bind Slave to faulty client
        nativeReaderSpy = Integration.bindSlaveSpy(new LocalClient(null));

        nativeReaderSpy.disconnectReader(nativeReader, CLIENT_NODE_ID);
        //should throw a KeypleRemoteException in slave side
    }


    /*
    HELPERS
     */




}