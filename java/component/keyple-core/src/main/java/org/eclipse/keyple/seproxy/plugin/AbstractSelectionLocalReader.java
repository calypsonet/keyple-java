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



import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.message.*;
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

    /** ==== ATR and AID based generic methods ============================= */

    /**
     * The AtrSelector provided in argument holds all the needed data to handle the ATR matching
     * process and build the resulting SelectionStatus.
     *
     * @param atrSelector the ATR matching data (a regular expression used to compare the SE ATR to
     *        the expected one)
     * @return the SelectionStatus containing the actual ATR and the matching status flag.
     */
    /**
     * Retrieve the SE ATR and compare it with the regular expression provided in the AtrSelector.
     *
     * @param atrSelector the ATR matching data (a regular expression used to compare the SE ATR to
     *        the expected one)
     * @return the SelectionStatus
     */
    protected final SelectionStatus openLogicalChannelByAtr(SeRequest.AtrSelector atrSelector)
            throws KeypleIOReaderException {
        boolean selectionHasMatched;
        byte[] atr = getATR();

        if (atr == null) {
            throw new KeypleIOReaderException("Didn't get an ATR from the SE.");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("[{}] openLogicalChannelByAtr => ATR: {}", this.getName(),
                    ByteArrayUtils.toHex(atr));
        }
        if (atrSelector.atrMatches(atr)) {
            selectionHasMatched = true;
        } else {
            logger.trace("[{}] openLogicalChannelByAtr => ATR Selection failed. SELECTOR = {}",
                    this.getName(), atrSelector);
            selectionHasMatched = false;
        }
        return new SelectionStatus(new AnswerToReset(atr), new ApduResponse(null, null),
                selectionHasMatched);
    }

    /**
     * Build a select application command, transmit it to the SE and deduct the SelectionStatus.
     * 
     * @param aidSelector the targeted application selector
     * @return the SelectionStatus
     * @throws KeypleIOReaderException if a reader error occurs
     */
    protected SelectionStatus openLogicalChannelByAid(SeRequest.AidSelector aidSelector)
            throws KeypleIOReaderException {
        ApduResponse fciResponse;
        byte[] atr;
        byte[] aid = aidSelector.getAidToSelect();
        if (aid != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] openLogicalChannelByAid => Select Application with AID = {}",
                        this.getName(), ByteArrayUtils.toHex(aid));
            }
            /*
             * build a get response command the actual length expected by the SE in the get response
             * command is handled in transmitApdu
             */
            byte[] selectApplicationCommand = new byte[6 + aid.length];
            selectApplicationCommand[0] = (byte) 0x00; // CLA
            selectApplicationCommand[1] = (byte) 0xA4; // INS
            selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
            if (!aidSelector.isSelectNext()) {
                selectApplicationCommand[3] = (byte) 0x00; // P2: requests the first occurrence
            } else {
                selectApplicationCommand[3] = (byte) 0x02; // P2: requests the next occurrence
            }
            selectApplicationCommand[4] = (byte) (aid.length); // Lc
            System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data
            selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le

            /*
             * we use here processApduRequest to manage case 4 hack. The successful status codes
             * list for this command is provided.
             */
            fciResponse = processApduRequest(
                    new ApduRequest("Internal Select Application", selectApplicationCommand, true,
                            aidSelector.getSuccessfulSelectionStatusCodes()));

            if (!fciResponse.isSuccessful()) {
                logger.trace(
                        "[{}] openLogicalChannelByAid => Application Selection failed. SELECTOR = {}",
                        this.getName(), aidSelector);
            }

            /* get the ATR bytes */
            atr = getATR();
        } else {
            throw new IllegalArgumentException("AID must not be null for an AidSelector.");
        }
        return new SelectionStatus(new AnswerToReset(atr), fciResponse, fciResponse.isSuccessful());
    }
}
