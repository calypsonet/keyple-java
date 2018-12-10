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

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.RemoteMethodTxEngine;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.plugin.AbstractObservablePlugin;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Remote SE Plugin Creates a virtual reader when a remote readers connect Manages the dispatch of
 * events received from remote readers
 */
public final class RemoteSePlugin extends AbstractObservablePlugin {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePlugin.class);
    static final String PLUGIN_NAME = "RemoteSePlugin";

    // private final VirtualReaderSessionFactory sessionManager;

    private final VirtualReaderSessionFactory sessionManager;
    private final DtoSender sender;

    /**
     * Only {@link VirtualReaderService} can instanciate a RemoteSePlugin
     */
    RemoteSePlugin(VirtualReaderSessionFactory sessionManager, DtoSender sender) {
        super(PLUGIN_NAME);
        this.sessionManager = sessionManager;
        logger.info("Init RemoteSePlugin");
        this.sender = sender;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {}



    public SeReader getReaderByRemoteName(String remoteName) throws KeypleReaderNotFoundException {
        for (AbstractObservableReader virtualReader : readers) {
            if (((VirtualReader) virtualReader).getNativeReaderName().equals(remoteName)) {
                return virtualReader;
            }
        }
        throw new KeypleReaderNotFoundException(remoteName);
    }

    /**
     * Create a virtual reader
     *
     */
    public ProxyReader createVirtualReader(String clientNodeId, String nativeReaderName,
            DtoSender dtoSender) throws KeypleReaderException {
        logger.debug("createVirtualReader for nativeReader {}", nativeReaderName);

        // create a new session for the new reader
        VirtualReaderSession session = sessionManager.createSession(nativeReaderName, clientNodeId);

        // DtoSender sends Dto when the session requires to
        ((VirtualReaderSessionImpl) session).addObserver(dtoSender);

        // check if reader is not already connected (by localReaderName)
        if (!isReaderConnected(nativeReaderName)) {
            logger.info("Create a new Virtual Reader with localReaderName {} with session {}",
                    nativeReaderName, session.getSessionId());

            RemoteMethodTxEngine rmTxEngine = new RemoteMethodTxEngine(sender);

            final VirtualReader virtualReader =
                    new VirtualReader(session, nativeReaderName, rmTxEngine);
            readers.add(virtualReader);

            // notify that a new reader is connected in a separated thread
            new Thread() {
                public void run() {
                    notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                            PluginEvent.EventType.READER_CONNECTED));
                }
            }.start();

            return virtualReader;
        } else {
            throw new KeypleReaderException("Virtual Reader already exists");
        }
    }

    /**
     * Delete a virtual reader
     * 
     * @param nativeReaderName name of the virtual reader to be deleted
     */
    public void disconnectRemoteReader(String nativeReaderName)
            throws KeypleReaderNotFoundException {
        logger.debug("Disconnect Virtual reader {}", nativeReaderName);


        // retrieve virtual reader to delete
        final VirtualReader virtualReader =
                (VirtualReader) this.getReaderByRemoteName(nativeReaderName);

        logger.info("Disconnect VirtualReader with name {} with session {}", nativeReaderName);

        // remove observers
        ((VirtualReaderSessionImpl) virtualReader.getSession()).clearObservers();

        // remove reader
        readers.remove(virtualReader);

        // send event READER_DISCONNECTED in a separate thread
        new Thread() {
            public void run() {
                notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                        PluginEvent.EventType.READER_DISCONNECTED));
            }
        }.start();

    }

    /**
     * Propagate a received event from slave device
     * 
     * @param event
     * @param sessionId : not used yet
     */
    public void onReaderEvent(ReaderEvent event, String sessionId) {
        logger.debug("OnReaderEvent {}", event);
        logger.debug("Dispatch ReaderEvent to the appropriate Reader : {} sessionId : {}",
                event.getReaderName(), sessionId);
        try {
            // TODO : dispatch events is only managed by remote reader name, should take sessionId
            // also
            VirtualReader virtualReader =
                    (VirtualReader) getReaderByRemoteName(event.getReaderName());
            virtualReader.onRemoteReaderEvent(event);

        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

    }


    private Boolean isReaderConnected(String name) {
        for (AbstractObservableReader virtualReader : readers) {
            if (((VirtualReader) virtualReader).getNativeReaderName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected SortedSet<AbstractObservableReader> getNativeReaders() throws KeypleReaderException {
        // not necessary
        return new TreeSet<AbstractObservableReader>();
    }

    @Override
    protected AbstractObservableReader getNativeReader(String s) throws KeypleReaderException {
        // should not be call
        throw new IllegalArgumentException("Use getReader method instead of getNativeReader");
    }
}
