/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.stub;



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.example.common.AbstractLogicManager;
import org.keyple.example.common.IsodepCardAccessManager;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.Observable;
import org.keyple.util.Topic;
import org.mockito.junit.MockitoJUnitRunner;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

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
    public void InsertCalypso() throws IOReaderException {

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
            }

        });

        csc.insertInto(stubReader);

    }



    @Test
    public void selectCalypsoApplication() throws IOReaderException {

        final IsodepCardAccessManager cardAccessManager = new IsodepCardAccessManager();
        cardAccessManager.setPoReader(stubReader);

        cardAccessManager.getTopic()
                .addSubscriber(new Topic.Subscriber<AbstractLogicManager.Event>() {
                    @Override
                    public void update(AbstractLogicManager.Event event) {
                        logger.debug("Event received from CardAccessManager" + event.toString());
                    }
                });

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            Integer i = 0;

            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                if (i == 0) {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
                    i++;
                    cardAccessManager.run();
                } else {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
                }

            }

        });



        csc.insertInto(stubReader);

    }



}
