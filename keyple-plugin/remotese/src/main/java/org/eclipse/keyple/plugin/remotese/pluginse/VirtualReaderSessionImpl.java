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
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.concurrent.CountDownLatch;
import org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitInvoker;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteReaderException;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethodInvoker;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage RSE Reader Session Manage SeRequestSet to transmitSet and receive SeResponseSet in
 * blocking and non blocking way
 */
public class VirtualReaderSessionImpl extends Observable<KeypleDto>
        implements VirtualReaderSession {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderSessionImpl.class);

    private final String sessionId;
    private SeRequestSet seRequestSet;
    private SeResponseSetCallback seResponseSetCallback;
    private CountDownLatch lock;
    private SeResponseSet seResponseSet;
    private KeypleRemoteReaderException remoteException;

    // constructor
    public VirtualReaderSessionImpl(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Manage an asynchronous transmitSet, call the callback to transmitSet the SeResponseSet when
     * received
     * 
     * @param seRequestSet : seRequestSet to be processed
     * @param seResponseSetCallback : callback that will be called once seResponseSet is received
     */
    private void asyncTransmit(String nativeReaderName, String virtualReaderName,
            SeRequestSet seRequestSet, SeResponseSetCallback seResponseSetCallback) {

        logger.debug("Session {} asyncTransmit {}", sessionId, seRequestSet);
        if (this.seRequestSet == null) {
            logger.debug("Set a new seRequestSet in Session {}", sessionId);

            // used for 1way communication
            this.seRequestSet = seRequestSet;
            this.seResponseSetCallback = seResponseSetCallback;

            // used for 2way communications
            // Send the keypleDto to the observable

            RemoteMethodInvoker rmTransmit = new RmTransmitInvoker(seRequestSet, sessionId,
                    nativeReaderName, virtualReaderName, null);

            // pass Transmit Dto to the Keyple Dto Interface
            notifyObservers(rmTransmit.dto());

        } else {
            logger.warn("SeRequestSet is already set in Session {}", sessionId);

        }
    }

    /**
     * Set the SeResponseSet back into session to be transmitted to virtual reader
     * 
     * @param seResponseSet
     */
    @Override
    public void asyncSetSeResponseSet(SeResponseSet seResponseSet,
            KeypleRemoteReaderException remoteException) {

        logger.debug("Session {} asyncSetSeResponseSet, responseSet : {} - remoteException : {}",
                sessionId, seResponseSet, remoteException);

        if (this.seRequestSet == null) {
            logger.warn("seRequestSet is missing while receiving seResponseSet {}", seResponseSet);
        }

        // release seRequestSet next work
        this.seRequestSet = null;

        // set SeResponseSet in session for syncTransmit
        this.seResponseSet = seResponseSet;

        // set SeResponseSet in session for syncTransmit
        this.remoteException = remoteException;

        // return seResponseSet by callback
        this.seResponseSetCallback.get(seResponseSet, remoteException);
    }

    @Override
    public Boolean hasSeRequestSet() {
        return seRequestSet != null;
    }


    /**
     * Blocking transmit (use internally the transmitSet)
     *
     * @param nativeReaderName : local reader to transmit to
     * @param virtualReaderName : virtual reader that receive the order the transmit to
     * @param seRequest : seRequest to transmit
     * @return seResponse
     */
    public SeResponse transmit(final String nativeReaderName, final String virtualReaderName,
            final SeRequest seRequest) throws KeypleRemoteReaderException {
        return transmitSet(nativeReaderName, virtualReaderName, new SeRequestSet(seRequest))
                .getSingleResponse();
    }

    /**
     * Blocking transmitSet
     * 
     * @param nativeReaderName : local reader to transmit to
     * @param virtualReaderName : virtual reader that receive the order the transmit to
     * @param seRequestSet : seRequestSet to transmit
     * @return seResponseSet
     */
    @Override
    public SeResponseSet transmitSet(final String nativeReaderName, final String virtualReaderName,
            final SeRequestSet seRequestSet) throws KeypleRemoteReaderException {

        logger.debug("Session {} sync transmitSet {}", sessionId, seRequestSet);
        Thread asyncTransmit = new Thread() {
            public void run() {

                // Run an async Transmit in a separate thread
                // Lock it until the callback is received
                asyncTransmit(nativeReaderName, virtualReaderName, seRequestSet,
                        new SeResponseSetCallback() {
                            @Override
                            public void get(SeResponseSet seResponseSet,
                                    KeypleRemoteReaderException exception) {
                                logger.debug(
                                        "Receive SeResponseSetCallback get call in synchronous transmitSet, release lock ");
                                // the call back is used to unleash the lock on the thread
                                // in blocking transmitSet, the seResponseSet is transmitted to the
                                // session without using the callback
                                lock.countDown();
                            }
                        });
            }
        };

        asyncTransmit.start();


        try {
            logger.debug("Set lock on thread");
            lock = new CountDownLatch(1);
            lock.await();
            logger.debug(
                    "Thread unlock in session {} asyncSetSeResponseSet, responseSet : {} - remoteException : {}",
                    sessionId, seResponseSet, remoteException);
            // if an exception was thrown remotely
            if (this.remoteException != null) {
                throw remoteException;
            } else {
                // if not return response set
                return this.seResponseSet;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Thread locking in blocking transmitSet has encountered an exception", e);
        }
    }

    @Override
    public SeRequestSet getSeRequestSet() {
        return this.seRequestSet;
    }


    @Override
    public String getSessionId() {
        return sessionId;
    }


}
