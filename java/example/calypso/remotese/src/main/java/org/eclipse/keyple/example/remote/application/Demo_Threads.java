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
package org.eclipse.keyple.example.remote.application;


import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start Client or Server Threads that will act either as Master or Slave. Client and Server will be
 * created based on a {@link TransportFactory} Two modes are supported : - the server is Master and
 * the client is Slave - the server is Slave and the client is Master
 *
 * Slave behaviour is defined by {@link Demo_Slave} Master behaviour is defined by
 * {@link Demo_Master}
 */
public class Demo_Threads {

    private static final Logger logger = LoggerFactory.getLogger(Demo_Threads.class);


    /**
     * Start a server Thread based on the given {@link TransportFactory}
     * 
     * @param isMaster : true if the server should act like a Master, false if Slave
     * @param factory : transport factory that creates the server object
     */
    static public void startServer(final Boolean isMaster, final TransportFactory factory,
            final String masterNodeId, final Boolean killAtEnd) {
        Thread server = new Thread() {
            @Override
            public void run() {
                try {

                    logger.info("**** Starting Server Thread ****");

                    if (isMaster) {
                        // server is master
                        Demo_Master master = new Demo_Master(factory, true, null);
                        master.boot();
                    } else {
                        // server is slave
                        Demo_Slave slave = new Demo_Slave(factory, true, null);
                        executeSlaveScenario(slave, true, masterNodeId, killAtEnd);

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

    /**
     * Start a client Thread based on the given {@link TransportFactory}
     * 
     * @param isMaster : true if the client should act like a Master, false if Slave
     * @param factory : transport factory that creates the client object
     */
    static public void startClient(final Boolean isMaster, final TransportFactory factory,
            final String clientNodeId, final Boolean killAtEnd) {
        Thread threadClient = new Thread() {
            @Override
            public void run() {
                logger.info("**** Starting Client Thread ****");

                try {
                    if (isMaster) {
                        // client is master
                        Demo_Master master = new Demo_Master(factory, false, clientNodeId);
                        master.boot();
                    } else {
                        // client is slave
                        Demo_Slave slave = new Demo_Slave(factory, false, clientNodeId);
                        executeSlaveScenario(slave, false, null, killAtEnd);

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
        threadClient.start();
    }

    static private void executeSlaveScenario(Demo_Slave slave, Boolean isServer,
            final String masterNodeId, final Boolean killAtEnd)
            throws KeypleReaderNotFoundException, InterruptedException, KeypleReaderException,
            KeypleRemoteException {
        logger.info("------------------------");
        logger.info("Connect Reader to Master");
        logger.info("------------------------");

        Thread.sleep(2000);
        String sessionId = slave.connectAReader(isServer, masterNodeId);
        logger.info("--------------------------------------------------");
        logger.info("Session created on server {}", sessionId);
        // logger.info("Wait 2 seconds, then insert SE");
        logger.info("--------------------------------------------------");

        // Thread.sleep(2000);

        logger.info("Inserting SE");
        slave.insertSe();
        logger.info("Wait 2 seconds, then remove SE");
        Thread.sleep(2000);
        slave.removeSe();
        logger.info("Wait 2 seconds, then disconnect reader");
        Thread.sleep(2000);
        slave.disconnect(sessionId, null);

        if (killAtEnd) {
            logger.info("Wait 2 seconds, then shutdown jvm");
            Thread.sleep(2000);

            Runtime.getRuntime().exit(0);

        }

    }

}
