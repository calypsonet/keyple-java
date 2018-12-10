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
package org.eclipse.keyple.plugin.stub;


import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPluginTest {

    StubPlugin stubPlugin;
    Logger logger = LoggerFactory.getLogger(StubPluginTest.class);

    @Before
    public void setUp() throws InterruptedException, KeypleReaderException {
        logger.info("setUp, assert stubplugin is empty");
        stubPlugin = StubPlugin.getInstance(); // singleton

        logger.info("Stubplugin readers size {}", stubPlugin.getReaders().size());
        Assert.assertEquals(0, stubPlugin.getReaders().size());

        logger.info("Stubplugin observers size {}", stubPlugin.countObservers());
        Assert.assertEquals(0, stubPlugin.countObservers());

        Thread.sleep(100);
    }

    @After
    public void tearDown() throws InterruptedException {
        stubPlugin = StubPlugin.getInstance(); // singleton
        stubPlugin.clearObservers();
        Thread.sleep(100);

    }


    @Test
    public void testA_PlugReaders() throws InterruptedException, KeypleReaderException {

        ObservablePlugin.PluginObserver connected_obs = new ObservablePlugin.PluginObserver() {
            boolean first = true;

            @Override
            public void update(PluginEvent event) {
                if (first) {
                    Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED,
                            event.getEventType());
                    first = false;
                } else {
                    Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                            event.getEventType());
                }
            }
        };

        // add READER_CONNECTED assert observer
        stubPlugin.addObserver(connected_obs);

        // connect reader
        stubPlugin.plugStubReader("testA_PlugReaders");

        Thread.sleep(200);
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());

        assert (stubPlugin.getReaders().size() == 1);

        // clean
        stubPlugin.unplugReader("testA_PlugReaders");
        Thread.sleep(200);
        stubPlugin.removeObserver(connected_obs);
        Thread.sleep(200);
    }

    @Test
    public void testB_UnplugReaders() throws InterruptedException, KeypleReaderException {

        ObservablePlugin.PluginObserver disconnected_obs = new ObservablePlugin.PluginObserver() {
            boolean first = true;

            @Override
            public void update(PluginEvent event) {
                if (first) {
                    Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED,
                            event.getEventType());
                    first = false;
                } else {
                    Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                            event.getEventType());
                }
            }
        };

        // add READER_CONNECTED assert observer
        stubPlugin.addObserver(disconnected_obs);

        // add a reader
        stubPlugin.plugStubReader("testB_UnplugReaders");

        // let the monitor thread work
        Thread.sleep(100);

        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());
        assert (stubPlugin.getReaders().size() == 1);

        stubPlugin.unplugReader("testB_UnplugReaders");

        Thread.sleep(100);

        Assert.assertEquals(0, stubPlugin.getReaders().size());

        // clean
        stubPlugin.removeObserver(disconnected_obs);
    }

    @Test
    public void testC_PlugSameReaderTwice() throws InterruptedException, KeypleReaderException {

        ObservablePlugin.PluginObserver observer = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {}
        };

        // add observer to have the reader management done by the monitoring thread
        stubPlugin.addObserver(observer);

        stubPlugin.plugStubReader("testC_PlugSameReaderTwice");
        stubPlugin.plugStubReader("testC_PlugSameReaderTwice");
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());

        // let the monitor thread work
        Thread.sleep(100);

        assert (stubPlugin.getReaders().size() == 1);
        stubPlugin.unplugReader("testC_PlugSameReaderTwice");

        // let the monitor thread work
        Thread.sleep(100);

        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());
        assert (stubPlugin.getReaders().size() == 0);
    }

    @Test
    public void testD_GetName() {
        assert (stubPlugin.getName() != null);
    }
}
