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

import java.util.Set;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleApplicationSelectionException;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
        "PMD.StdCyclomaticComplexity"})
/**
 * Local reader class implementing the logical channel opening based on the selection of the SE
 * application
 */
public abstract class AbstractSelectionLocalReader extends AbstractLocalReader
        implements ObservableReader {
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractSelectionLocalReader.class);

    protected AbstractSelectionLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    /**
     * Gets the SE Answer to reset
     *
     * @return ATR returned by the SE or reconstructed by the reader (contactless)
     */
    protected abstract byte[] getATR();

    /**
     * Tells if the physical channel is open or not
     *
     * @return true is the channel is open
     */
    protected abstract boolean isPhysicalChannelOpen();

    /**
     * Attempts to open the physical channel
     *
     * @throws KeypleChannelStateException if the channel opening fails
     */
    protected abstract void openPhysicalChannel() throws KeypleChannelStateException;

    /**
     * Starts the monitoring thread
     * <p>
     * This method has to be implemented by the class that handle the monitoring thread. It will be
     * called when a first observer is added.
     */
    protected abstract void startObservation();

    /**
     * Ends the monitoring thread
     * <p>
     * This method has to be implemented by the class that handle the monitoring thread. It will be
     * called when the observer is removed.
     */
    protected abstract void stopObservation();

    /**
     * Opens a logical channel
     * 
     * @param selector the SE Selector: AID of the application to select or ATR regex
     * @param successfulSelectionStatusCodes the list of successful status code for the select
     *        command
     * @return a {@link SelectionStatus} object containing the SE ATR, the SE FCI and a flag giving
     *         the selection process result. When ATR or FCI are not available, they are set to null
     * @throws KeypleChannelStateException - if a channel state exception occurred
     * @throws KeypleIOReaderException - if an IO exception occurred
     * @throws KeypleApplicationSelectionException - if the application selection is not successful
     */
    protected final SelectionStatus openLogicalChannelAndSelect(SeRequest.Selector selector,
            Set<Integer> successfulSelectionStatusCodes) throws KeypleChannelStateException,
            KeypleApplicationSelectionException, KeypleIOReaderException {
        byte atr[], fci[] = null;
        boolean selectionHasMatched;

        if (!isLogicalChannelOpen()) {
            /*
             * init of the physical SE channel: if not yet established, opening of a new physical
             * channel
             */
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            if (!isPhysicalChannelOpen()) {
                throw new KeypleChannelStateException("Fail to open physical channel.");
            }
        }

        /* get the ATR bytes */
        atr = getATR();
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] openLogicalChannelAndSelect => ATR: {}", this.getName(),
                    ByteArrayUtils.toHex(atr));
        }

        /* selector may be null, in this case we consider the logical channel open */
        if (selector != null) {
            if (selector instanceof SeRequest.AidSelector) {
                byte[] aid = ((SeRequest.AidSelector) selector).getAidToSelect();
                if (aid != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                                "[{}] openLogicalChannelAndSelect => Select Application with AID = {}",
                                this.getName(), ByteArrayUtils.toHex(aid));
                    }
                    /*
                     * build a get response command the actual length expected by the SE in the get
                     * response command is handled in transmitApdu
                     */
                    byte[] selectApplicationCommand = new byte[6 + aid.length];
                    selectApplicationCommand[0] = (byte) 0x00; // CLA
                    selectApplicationCommand[1] = (byte) 0xA4; // INS
                    selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
                    if (!((SeRequest.AidSelector) selector).isSelectNext()) {
                        selectApplicationCommand[3] = (byte) 0x00; // P2: requests the first
                                                                   // occurrence
                    } else {
                        selectApplicationCommand[3] = (byte) 0x02; // P2: requests the next
                                                                   // occurrence
                    }
                    selectApplicationCommand[4] = (byte) (aid.length); // Lc
                    System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data
                    selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le

                    /*
                     * we use here processApduRequest to manage case 4 hack. The successful status
                     * codes list for this command is provided.
                     */
                    ApduResponse fciResponse = processApduRequest(
                            new ApduRequest("Internal Select Application", selectApplicationCommand,
                                    true, successfulSelectionStatusCodes));

                    /* get the FCI bytes */
                    fci = fciResponse.getBytes();

                    if (fciResponse.isSuccessful()) {
                        selectionHasMatched = true;
                    } else {
                        logger.trace(
                                "[{}] openLogicalChannelAndSelect => Application Selection failed. SELECTOR = {}",
                                this.getName(), selector);
                        selectionHasMatched = false;
                    }
                } else {
                    throw new IllegalArgumentException("AID must not be null for an AidSelector.");
                }
            } else {
                if (((SeRequest.AtrSelector) selector).atrMatches(atr)) {
                    selectionHasMatched = true;
                } else {
                    logger.trace(
                            "[{}] openLogicalChannelAndSelect => ATR Selection failed. SELECTOR = {}",
                            this.getName(), selector);
                    selectionHasMatched = false;
                }
            }
        } else {
            selectionHasMatched = true;
        }

        return new SelectionStatus(new AnswerToReset(atr), new ApduResponse(fci, null),
                selectionHasMatched);
    }

    /**
     * Add a reader observer.
     * <p>
     * The observer will receive all the events produced by this reader (card insertion, removal,
     * etc.)
     * <p>
     * The monitoring thread is started when the first observer is added.
     * 
     * @param observer the observer object
     */
    public final void addObserver(ReaderObserver observer) {
        super.addObserver(observer);
        if (super.countObservers() == 1) {
            logger.debug("Start the reader monitoring.");
            startObservation();
        }
    }

    /**
     * Remove a reader observer.
     * <p>
     * The observer will do not receive any of the events produced by this reader.
     * <p>
     * The monitoring thread is ended when the last observer is removed.
     * 
     * @param observer the observer object
     */
    public final void removeObserver(ReaderObserver observer) {
        super.removeObserver(observer);
        if (super.countObservers() == 0) {
            logger.debug("Stop the reader monitoring.");
            stopObservation();
        }
    }
}
