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
package org.eclipse.keyple.seproxy.plugin;


import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.eclipse.keyple.transaction.SelectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Abstract definition of an observable reader. Factorizes setSetProtocols and will factorize the
 * transmit method logging
 * 
 */

public abstract class AbstractObservableReader extends AbstractLoggedObservable<ReaderEvent>
        implements ProxyReader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractObservableReader.class);

    private long before; // timestamp recorder

    protected final String pluginName;

    /** the default SelectionRequest to be executed upon SE insertion */
    protected SelectionRequest defaultSelectionRequest;

    /** Indicate if all SE detected should be notified or only matching SE */
    protected ObservableReader.NotificationMode notificationMode;

    protected abstract SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws KeypleIOReaderException, KeypleChannelStateException, KeypleReaderException;

    protected abstract SeResponse processSeRequest(SeRequest seRequest)
            throws KeypleIOReaderException, KeypleChannelStateException, KeypleReaderException;

    /**
     * Reader constructor
     *
     * Force the definition of a name through the use of super method.
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    protected AbstractObservableReader(String pluginName, String readerName) {
        super(readerName);
        this.pluginName = pluginName;
        this.before = System.nanoTime();
    }


    /**
     * If defined, the prepared setDefaultSelectionRequest will be processed as soon as a SE is
     * inserted. The result of this request set will be added to the reader event.
     * <p>
     * Depending on the notification mode, the observer will be notified whenever an SE is inserted,
     * regardless of the selection status, or only if the current SE matches the selection criteria.
     *
     * @param defaultSelectionRequest the {@link SelectionRequest} to be executed when a SE is
     *        inserted
     * @param notificationMode the notification mode enum (ALWAYS or MATCHED_ONLY)
     */
    public void setDefaultSelectionRequest(SelectionRequest defaultSelectionRequest,
            ObservableReader.NotificationMode notificationMode) {
        this.defaultSelectionRequest = defaultSelectionRequest;
        this.notificationMode = notificationMode;
    };

    /**
     * Execute the transmission of a list of {@link SeRequest} and returns a list of
     * {@link SeResponse}
     *
     * @param requestSet the request set
     * @return responseSet the response set
     * @throws KeypleReaderException if a reader error occurs
     */
    public final SeResponseSet transmitSet(SeRequestSet requestSet) throws KeypleReaderException {
        if (requestSet == null) {
            throw new IllegalArgumentException("seRequestSet must not be null");
        }

        SeResponseSet responseSet;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET = {}, elapsed {} ms.", this.getName(),
                    requestSet.toString(), elapsedMs);
        }

        try {
            responseSet = processSeRequestSet(requestSet);
        } catch (KeypleChannelStateException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET channel failure. elapsed {}", elapsedMs);
            /* Throw an exception with the responses collected so far. */
            throw ex;
        } catch (KeypleIOReaderException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUESTSET IO failure. elapsed {}", elapsedMs);
            /* Throw an exception with the responses collected so far. */
            throw ex;
        }

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SERESPONSESET = {}, elapsed {} ms.", this.getName(),
                    responseSet.toString(), elapsedMs);
        }

        return responseSet;
    }

    /**
     * Execute the transmission of a {@link SeRequest} and returns a {@link SeResponse}
     * 
     * @param seRequest the request to be transmitted
     * @return the received response
     * @throws KeypleReaderException if a reader error occurs
     */
    public final SeResponse transmit(SeRequest seRequest) throws KeypleReaderException {
        if (seRequest == null) {
            throw new IllegalArgumentException("seRequest must not be null");
        }

        SeResponse seResponse = null;

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUEST = {}, elapsed {} ms.", this.getName(),
                    seRequest.toString(), elapsedMs);
        }

        try {
            seResponse = processSeRequest(seRequest);
        } catch (KeypleChannelStateException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUEST channel failure. elapsed {}", elapsedMs);
            /* Throw an exception with the responses collected so far (ex.getSeResponse()). */
            throw ex;
        } catch (KeypleIOReaderException ex) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SEREQUEST IO failure. elapsed {}", elapsedMs);
            /* Throw an exception with the responses collected so far (ex.getSeResponse()). */
            throw ex;
        }

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.debug("[{}] transmit => SERESPONSE = {}, elapsed {} ms.", this.getName(),
                    seResponse.toString(), elapsedMs);
        }

        return seResponse;
    }

    /**
     * @return Plugin name
     */
    protected final String getPluginName() {
        return pluginName;
    }

    /**
     * Compare the name of the current SeReader to the name of the SeReader provided in argument
     * 
     * @param seReader a SeReader object
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(SeReader seReader) {
        return this.getName().compareTo(seReader.getName());
    }
}
