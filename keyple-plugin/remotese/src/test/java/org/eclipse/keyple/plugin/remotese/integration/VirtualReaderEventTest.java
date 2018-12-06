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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReaderTest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Virtual Reader Service with stub plugin and hoplink SE
 */
public class VirtualReaderEventTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderEventTest.class);


    /*
     * SE EVENTS
     */


    /**
     * Test SE_INSERTED Reader Event throwing and catching
     * 
     * @throws Exception
     */
    @Test
    public void testInsert() throws Exception {


        // lock test until message is received
        final CountDownLatch lock = new CountDownLatch(1);

        // add stubPluginObserver
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), nativeReader.getName());
                Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
                logger.debug("Reader Event is correct, release lock");
                lock.countDown();
            }
        });

        logger.info("Insert a Hoplink SE and wait 5 seconds for a SE event to be thrown");

        // insert SE
        nativeReader.insertSe(StubReaderTest.hoplinkSE());
        // wait 5 seconds
        lock.await(5, TimeUnit.SECONDS);

        Assert.assertEquals(0, lock.getCount());
    }


    /**
     * Test SE_REMOVED Reader Event throwing and catching
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveEvent() throws Exception {

        // lock test until two messages are received
        final CountDownLatch lock = new CountDownLatch(2);

        // add stubPluginObserver
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                    // we expect the first event to be SE_INSERTED
                    Assert.assertEquals(2, lock.getCount());
                    lock.countDown();
                } else {
                    // the next event should be SE_REMOVAL
                    Assert.assertEquals(1, lock.getCount());
                    Assert.assertEquals(event.getReaderName(), nativeReader.getName());
                    Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_REMOVAL, event.getEventType());
                    logger.debug("Reader Event is correct, release lock");
                    lock.countDown();

                }
            }
        });

        logger.info(
                "Insert and remove a Hoplink SE and wait 5 seconds for two SE events to be thrown");

        // insert SE
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        // wait 1 second
        Thread.sleep(1000);

        // remove SE
        nativeReader.removeSe();

        // wait 5 seconds
        lock.await(5, TimeUnit.SECONDS);

        Assert.assertEquals(0, lock.getCount());

        //https://github.com/calypsonet/keyple-java/issues/420
        //Assert.assertEquals(0, virtualReaderService.getPlugin().getReaders().size());
    }


}
