package org.eclipse.keyple.seproxy.plugin;


import org.eclipse.keyple.CoreBaseTest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.*;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SelectionStatus;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.doAnswer;

/**
 * Test methods linked to observability
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractObservableReaderTest extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractObservableReaderTest.class);


    final String PLUGIN_NAME = "abstractObservablePluginTest";
    final String READER_NAME = "abstractObservableReaderTest";

    final ObservableReader.ReaderObserver obs1 = getObserver();
    final ObservableReader.ReaderObserver obs2 = getObserver();

    AbstractObservableReader spyReader;

    CountDownLatch startObservationCall;
    CountDownLatch stopObservationCall;

    @Before
    public void setUp(){
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
        spyReader = Mockito.spy(getBlankAbstractObservableReader(PLUGIN_NAME, READER_NAME));
        initSpyReader();
    }

    /*
     * TESTS
     */

    @Test
    public void testAddObserver() throws Exception {
        startObservationCall = new CountDownLatch(5);
        stopObservationCall = new CountDownLatch(5);
        spyReader.addObserver(obs1);
        Assert.assertEquals(1, spyReader.countObservers());
        Assert.assertEquals(4, startObservationCall.getCount());//should be called once
        Assert.assertEquals(5, stopObservationCall.getCount());//should not be called

    }

    @Test
    public void testRemoveObserver() throws Exception {
        startObservationCall = new CountDownLatch(5);
        stopObservationCall = new CountDownLatch(5);
        spyReader.addObserver(obs1);
        spyReader.removeObserver(obs1);
        Assert.assertEquals(0, spyReader.countObservers());
        Assert.assertEquals(4, startObservationCall.getCount());//should be called once
        Assert.assertEquals(4, stopObservationCall.getCount());//should be called once

    }

    @Test
    public void testAddRemoveObserver() throws Exception {
        startObservationCall = new CountDownLatch(5);
        stopObservationCall = new CountDownLatch(5);
        spyReader.addObserver(obs1);
        spyReader.addObserver(obs2);
        spyReader.removeObserver(obs2);
        Assert.assertEquals(1, spyReader.countObservers());
        Assert.assertEquals(4, startObservationCall.getCount());//should be called once
        Assert.assertEquals(5, stopObservationCall.getCount());//should not be called
    }




    /*
     * HELPERS
     */



    AbstractObservableReader getBlankAbstractObservableReader(String pluginName, String readerName) {

        return new AbstractLocalReader(pluginName, readerName) {
            @Override
            protected SelectionStatus openLogicalChannelAndSelect(SeRequest.Selector selector, Set<Integer> successfulSelectionStatusCodes) throws KeypleApplicationSelectionException, KeypleReaderException {
                return null;
            }

            @Override
            protected void closePhysicalChannel() throws KeypleChannelStateException {

            }

            @Override
            protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
                return new byte[0];
            }

            @Override
            protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws KeypleReaderException {
                return false;
            }

            @Override
            public boolean isSePresent() throws NoStackTraceThrowable {
                return false;
            }

            @Override
            public TransmissionMode getTransmissionMode() {
                return null;
            }

            @Override
            public Map<String, String> getParameters() {
                return null;
            }

            @Override
            public void setParameter(String key, String value) throws IllegalArgumentException, KeypleBaseException {

            }
        };
    }



    ObservableReader.ReaderObserver getObserver () {
        return new ObservableReader.ReaderObserver(){
            @Override
            public void update (ReaderEvent readerEvent){}
        } ;
    }


    void initSpyReader(){

        //track when startObservation is called
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                startObservationCall.countDown();
                return null;
            }
        }).when(spyReader).startObservation();

        //track when stopObservation is called
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                stopObservationCall.countDown();
                return null;
            }
        }).when(spyReader).stopObservation();
    }


}
