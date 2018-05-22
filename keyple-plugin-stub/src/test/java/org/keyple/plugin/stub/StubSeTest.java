package org.keyple.plugin.stub;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.example.common.AbstractLogicManager;
import org.keyple.example.common.BasicCardAccessManager;
import org.keyple.example.common.IsodepCardAccessManager;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeRequestSet;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.Observable;
import org.keyple.util.Topic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class StubSeTest {

    private static final ILogger logger = SLoggerFactory.getLogger(StubSeTest.class);

    StubReader stubReader;
    StubSecureElement csc;

    @Before
    public void SetUp() throws IOReaderException {
        stubReader = (StubReader) StubPlugin.getInstance().getReaders().get(0);
        stubReader.configureWillTimeout(false);
        stubReader.clearObservers();
        csc = new StubCalypsoSE();
    }

    @Test
    public void InsertCalypso() throws IOReaderException{


        final SeRequestSet emptyRequestSet = new SeRequestSet(new ArrayList<SeRequest>());

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
            }

        });

        csc.insertInto(stubReader);

    }

    @Test
    public void RemoveCalypso() throws IOReaderException{

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {

            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
            }
        });

        csc.removeFrom(stubReader);

    }



    @Test
    public void selectCalypso() throws IOReaderException{

        final IsodepCardAccessManager cardAccessManager = new IsodepCardAccessManager();
        cardAccessManager.setPoReader(stubReader);

        cardAccessManager.getTopic().addSubscriber(new Topic.Subscriber<AbstractLogicManager.Event>() {
            @Override
            public void update(AbstractLogicManager.Event event) {
                logger.debug("Event received from CardAccessManager" + event.toString());
            }
        });

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            Integer i = 0;

            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                if(i == 0){
                    assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
                    i++;
                    cardAccessManager.run();
                }else{
                    assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
                }

            }

        });



        csc.insertInto(stubReader);

    }





}
