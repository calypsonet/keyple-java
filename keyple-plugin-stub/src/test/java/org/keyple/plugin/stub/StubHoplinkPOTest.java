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
public class StubHoplinkPOTest {

    private static final ILogger logger = SLoggerFactory.getLogger(StubHoplinkPOTest.class);

    StubReader stubReader;
    StubSecureElement csc;

    @Before
    public void SetUp() throws IOReaderException {
        stubReader = (StubReader) StubPlugin.getInstance().getReaders().get(0);
        stubReader.configureWillTimeout(false);
        stubReader.clearObservers();
        csc = new StubHoplinkPO();
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
    public void InsertRemoveCalypso() throws IOReaderException {

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            Boolean inserted = false;
            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                if (!inserted) {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
                    inserted = true;
                } else {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
                }
            }

        });

        csc.insertInto(stubReader);
        csc.removeFrom(stubReader);


    }


    @Test
    public void selectCalypsoApplication() throws IOReaderException {

        final IsodepCardAccessManager transmitTest = new IsodepCardAccessManager();
        transmitTest.setPoReader(stubReader);

        transmitTest.getTopic()
                .addSubscriber(new Topic.Subscriber<AbstractLogicManager.Event>() {
                    @Override
                    public void update(AbstractLogicManager.Event event) {
                        logger.debug("Event received from CardAccessManager" + event.toString());
                    }
                });

        stubReader.addObserver(new Observable.Observer<ReaderEvent>() {
            Boolean inserted = false;

            @Override
            public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                if (!inserted) {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_INSERTED);
                    inserted = true;
                    transmitTest.run();
                } else {
                    assert (event.getEventType() == ReaderEvent.EventType.SE_REMOVAL);
                }

            }

        });

        csc.insertInto(stubReader);

    }



}
