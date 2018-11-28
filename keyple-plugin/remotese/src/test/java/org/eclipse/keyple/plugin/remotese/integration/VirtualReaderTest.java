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
package org.eclipse.keyple.plugin.remotese.integration;

import java.io.IOException;
import org.eclipse.keyple.plugin.remotese.common.json.SampleFactory;
import org.eclipse.keyple.plugin.remotese.nativese.NativeReaderServiceImpl;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderService;
import org.eclipse.keyple.plugin.remotese.transport.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubReaderTest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.Observable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualReaderTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderTest.class);

    // Real objects
    TransportFactory factory;
    Observable.Observer observer;
    NativeReaderServiceImpl nativeReaderService;

    // Spy Object
    VirtualReaderService virtualReaderService;

    @Before
    public void setTup() throws IOException {
        logger.info("*** Init LocalTransportFactory");
        // use a local transport factory for testing purposes (only java calls between client and
        // server)
        // only one client and one server
        factory = new LocalTransportFactory();
        observer = new Observable.Observer() {
            @Override
            public void update(Object o) {
                logger.debug("event received {}", o);
            }
        };

        logger.info("*** Bind Master Services");
        // bind Master services to server
        virtualReaderService = Integration.bindMaster(factory.getServer(), observer);

        logger.info("*** Bind Slave Services");
        // bind Slave services to client
        nativeReaderService = Integration.bindSlave(factory.getClient());

    }


    /*
    SE EVENTS
     */


    @Test
    public void testInsert() throws Exception {
        final String NATIVE_READER_NAME = "testInsert";
        final String CLIENT_NODE_ID = "testInsert_NodeId";


        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // test virtual reader
        final VirtualReader virtualReader = getVirtualReader();

        // add observer
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), virtualReader.getName());
                Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());

                logger.info("SE events is well formed");


            }
        });

        // insert SE
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        Thread.sleep(1000);

        logger.info("wait for SE event to be thrown");


    }









    /*
    TRANSMITS
     */

    @Test
    public void testKOTransmitSet_NoSE() throws Exception {
        final String NATIVE_READER_NAME = "testKOTransmitSet_NoSE";
        final String CLIENT_NODE_ID = "testKOTransmitSet_NoSENodeId";

        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        ProxyReader virtualReader = getVirtualReader();


        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmitSet(SampleFactory.getASeRequestSet());
            // should throw KeypleReaderException
            Assert.assertTrue(false);

        } catch (KeypleReaderException e) {
            logger.info("KeypleReaderException was thrown as expected");
            // assert exception is thrown
            Assert.assertNotNull(e);
            Assert.assertNotNull(e.getSeResponseSet());
            Assert.assertNull(e.getSeResponse());
        }
    }

    @Test
    public void testKOTransmit_NoSE() throws Exception {
        final String NATIVE_READER_NAME = "testKOTransmit_NoSE";
        final String CLIENT_NODE_ID = "testKOTransmit_NoSENodeId";

        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        ProxyReader virtualReader = getVirtualReader();


        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmit(SampleFactory.getASeRequest());
            // should throw KeypleReaderException
            Assert.assertTrue(false);

        } catch (KeypleReaderException e) {
            logger.info("KeypleReaderException was thrown as expected");
            // assert exception is thrown
            Assert.assertNotNull(e);
            //Assert.assertNotNull(e.getSeResponseSet());
            //Assert.assertNull(e.getSeResponse());
            // should not be null but transmit is using transmitSet, this is the reason I guess
            // todo : VirtualReader transmit should not be using transmitSet
        }
    }

    /**
     * Successful Transmit with a Calypso command to a Calypso SE
     * 
     * @throws Exception
     */
    @Test
    public void rse_transmit_Hoplink_Sucessfull() throws Exception {
        final String NATIVE_READER_NAME = "rse_transmit_Hoplink_Sucessfull";
        final String CLIENT_NODE_ID = "rse_transmit_Hoplink_SucessfullNodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        // test virtual reader
        ProxyReader virtualReader = getVirtualReader();

        SeRequestSet requests = StubReaderTest.getRequestIsoDepSetSample();

        StubReaderTest.selectSe(virtualReader);

        SeResponseSet seResponse = virtualReader.transmitSet(requests);

        // assert
        Assert.assertTrue(seResponse.getSingleResponse().getApduResponses().get(0).isSuccessful());
    }

    @Test(expected = KeypleReaderException.class)
    public void rse_transmit_no_response() throws Exception {
        final String NATIVE_READER_NAME = "rse_transmit_no_response";
        final String CLIENT_NODE_ID = "rse_transmit_no_responseNodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.noApduResponseSE());

        // test virtual reader
        ProxyReader virtualReader = getVirtualReader();

        // init Request
        SeRequestSet requests = StubReaderTest.getNoResponseRequest();

        StubReaderTest.selectSe(virtualReader);

        // test
        SeResponseSet seResponse = virtualReader.transmitSet(requests);
    }


    @Test
    public void transmit_partial_response_set_0() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_set_0";
        final String CLIENT_NODE_ID = "transmit_partial_response_set_0NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(0);

        ProxyReader virtualReader = getVirtualReader();



        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponseSet seResponseSet = virtualReader.transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 1);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_1() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_set_1";
        final String CLIENT_NODE_ID = "transmit_partial_response_set_1NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(1);

        ProxyReader virtualReader = getVirtualReader();



        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponseSet seResponseSet = virtualReader.transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 2);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 2);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_2() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_set_2";
        final String CLIENT_NODE_ID = "transmit_partial_response_set_2NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(2);

        ProxyReader virtualReader = getVirtualReader();


        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponseSet seResponseSet = virtualReader.transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 3);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(2).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_3() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_set_3";
        final String CLIENT_NODE_ID = "transmit_partial_response_set_3NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(3);

        ProxyReader virtualReader = getVirtualReader();


        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponseSet seResponseSet = virtualReader.transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 3);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(2).getApduResponses().size(), 4);
        }
    }

    @Test
    public void transmit_partial_response_0() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_0";
        final String CLIENT_NODE_ID = "transmit_partial_response_0NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(0);

        ProxyReader virtualReader = getVirtualReader();


        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponse seResponse = virtualReader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 0);
        }
    }

    @Test
    public void transmit_partial_response_1() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_1";
        final String CLIENT_NODE_ID = "transmit_partial_response_1NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(1);

        ProxyReader virtualReader = getVirtualReader();


        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponse seResponse = virtualReader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 1);
        }
    }

    @Test
    public void transmit_partial_response_2() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_2";
        final String CLIENT_NODE_ID = "transmit_partial_response_2NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(2);

        ProxyReader virtualReader = getVirtualReader();


        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponse seResponse = virtualReader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_3() throws Exception {
        final String NATIVE_READER_NAME = "transmit_partial_response_3";
        final String CLIENT_NODE_ID = "transmit_partial_response_3NodeId";

        // configure and connect a Stub Native reader
        StubReader nativeReader = connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(3);

        ProxyReader virtualReader = getVirtualReader();


        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            SeResponse seResponse = virtualReader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 3);
        }
    }


    private StubReader connectStubReader(String readerName, String nodeId) throws Exception {
        // configure native reader
        StubReader nativeReader = (StubReader) Integration.createStubReader(readerName);
        nativeReader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
        this.nativeReaderService.connectReader(nativeReader, nodeId);
        return nativeReader;
    }

    private VirtualReader getVirtualReader() {
        Assert.assertEquals(1, this.virtualReaderService.getPlugin().getReaders().size());
        return (VirtualReader) this.virtualReaderService.getPlugin().getReaders().first();
    }

}
