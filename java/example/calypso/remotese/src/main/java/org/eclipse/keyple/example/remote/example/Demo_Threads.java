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
package org.eclipse.keyple.example.remote.example;


import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.transport.TransportFactory;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_Threads {

    private static final Logger logger = LoggerFactory.getLogger(Demo_Threads.class);


    static public void startServer(final Boolean isMaster, final TransportFactory factory) {
        Thread server = new Thread() {
            @Override
            public void run() {
                try {

                    logger.info("**** Starting Server Thread ****");

                    if (isMaster) {
                        Demo_Master master = new Demo_Master(factory, true);
                        master.boot();

                    } else {
                        Demo_Slave slave = new Demo_Slave(factory, true);
                        logger.info("Wait 5 seconds, then connectAReader to master");
                        Thread.sleep(5000);
                        slave.connectAReader();
                        logger.info("Wait 5 seconds, then insert SE");
                        Thread.sleep(5000);
                        slave.insertSe();
                        logger.info("Wait 5 seconds, then remove SE");
                        Thread.sleep(5000);
                        slave.removeSe();
                        logger.info("Wait 5 seconds, then disconnect reader");
                        Thread.sleep(5000);
                        slave.disconnect();
                        Thread.sleep(5000);

                        logger.info("Wait 5 seconds, then shutdown jvm");
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exit(0);
                    }

                } catch (KeypleReaderNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeypleReaderException e) {
                    e.printStackTrace();
                } catch (KeypleRemoteException e) {
                    e.printStackTrace();
                }

            }
        };

        server.start();
    }

    static public void startClient(final Boolean isMaster, final TransportFactory factory) {
        Thread client = new Thread() {
            @Override
            public void run() {
                logger.info("**** Starting Client Thread ****");

                try {
                    if (isMaster) {
                        Demo_Master master = new Demo_Master(factory, false);
                        master.boot();
                    } else {
                        Demo_Slave slave = new Demo_Slave(factory, false);
                        slave.connectAReader();
                        logger.info("Wait 5 seconds, then insert SE");
                        Thread.sleep(5000);
                        slave.insertSe();
                        logger.info("Wait 5 seconds, then remove SE");
                        Thread.sleep(5000);
                        slave.removeSe();
                        logger.info("Wait 5 seconds, then disconnect reader");
                        Thread.sleep(5000);
                        slave.disconnect();

                        logger.info("Wait 5 seconds, then shutdown jvm");
                        Thread.sleep(5000);
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exit(0);

                    }

                } catch (KeypleReaderNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeypleReaderException e) {
                    e.printStackTrace();
                } catch (KeypleRemoteException e) {
                    e.printStackTrace();
                }

            }
        };
        client.start();
    }


}
