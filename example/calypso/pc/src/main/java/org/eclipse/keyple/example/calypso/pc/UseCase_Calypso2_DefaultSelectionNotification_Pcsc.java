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
package org.eclipse.keyple.example.calypso.pc;


import static org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo.*;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.transaction.CalypsoUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UseCase_Calypso2_DefaultSelectionNotification_Pcsc implements ReaderObserver {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Calypso2_DefaultSelectionNotification_Pcsc.class);
    private ProxyReader poReader;
    private String poAid = "A0000004040125090101";
    private SeSelection seSelection;
    private ReadRecordsRespPars readEnvironmentParser;
    /**
     * This object is used to freeze the main thread while card operations are handle through the
     * observers callbacks. A call to the notify() method would end the program (not demonstrated
     * here).
     */
    private static final Object waitForEnd = new Object();

    public UseCase_Calypso2_DefaultSelectionNotification_Pcsc()
            throws KeypleBaseException, InterruptedException {
        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /*
         * Get a PO reader ready to work with Calypso PO. Use the getReader helper method from the
         * ReaderUtilities class.
         */
        poReader = CalypsoUtilities.getDefaultPoReader(seProxyService);

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        logger.info(
                "=============== UseCase Calypo #2: AID based default selection ===================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());

        /*
         * Prepare a Calypso PO selection
         */
        seSelection = new SeSelection(poReader);

        /*
         * Setting of an AID based selection of a Calypso REV3 PO
         *
         * Select the first application matching the selection AID whatever the SE communication
         * protocol keep the logical channel open after the selection
         */

        /*
         * Calypso selection: configures a PoSelector with all the desired attributes to make the
         * selection and read additional information afterwards
         */
        PoSelector poSelector = new PoSelector(ByteArrayUtils.fromHex(poAid),
                SeSelector.SelectMode.FIRST, SeRequest.ChannelState.KEEP_OPEN, Protocol.ANY,
                PoSelector.RevisionTarget.TARGET_REV3, "AID: " + poAid);

        /*
         * Prepare the reading order and keep the associated parser for later use once the selection
         * has been made.
         */
        readEnvironmentParser = poSelector.prepareReadRecordsCmd(SFI_EnvironmentAndHolder,
                ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1, (byte) 0x00,
                String.format("EnvironmentAndHolder (SFI=%02X))", SFI_EnvironmentAndHolder));

        /*
         * Add the selection case to the current selection (we could have added other cases here)
         */
        seSelection.prepareSelection(poSelector);

        /*
         * Provide the ProxyReader with the selection operation to be processed when a PO is
         * inserted.
         */
        ((ObservableReader) poReader).setDefaultSeRequests(seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        /* Set the current class as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(this);

        logger.info(
                "==================================================================================");
        logger.info(
                "= Wait for a PO. The default AID based selection with reading of Environment     =");
        logger.info(
                "= file is ready to be processed as soon as the PO is detected.                   =");
        logger.info(
                "==================================================================================");

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }

    /**
     * Method invoked upon reader event
     * 
     * @param event the reader event
     */
    @Override
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_MATCHED:
                if (seSelection.processDefaultSelection(event.getDefaultResponseSet())) {
                    MatchingSe selectedSe = seSelection.getSelectedSe();

                    logger.info("Observer notification: the selection of the PO has succeeded.");

                    /*
                     * Retrieve the data read from the parser updated during the selection process
                     */
                    byte environmentAndHolder[] =
                            (readEnvironmentParser.getRecords()).get((int) RECORD_NUMBER_1);

                    /* Log the result */
                    logger.info("Environment file data: {}",
                            ByteArrayUtils.toHex(environmentAndHolder));

                    /* Go on with the reading of the first record of the EventLog file */
                    logger.info(
                            "==================================================================================");
                    logger.info(
                            "= 2nd PO exchange: reading transaction of the EventLog file.                     =");
                    logger.info(
                            "==================================================================================");

                    PoTransaction poTransaction =
                            new PoTransaction(poReader, (CalypsoPo) selectedSe);

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the transaction has been processed.
                     */
                    ReadRecordsRespPars readEventLogParser = poTransaction.prepareReadRecordsCmd(
                            SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1,
                            (byte) 0x00, String.format("EventLog (SFI=%02X, recnbr=%d))",
                                    SFI_EventLog, RECORD_NUMBER_1));

                    /*
                     * Actual PO communication: send the prepared read order, then close the channel
                     * with the PO
                     */
                    try {
                        if (poTransaction.processPoCommands(true)) {
                            logger.info("The reading of the EventLog has succeeded.");

                            /*
                             * Retrieve the data read from the parser updated during the transaction
                             * process
                             */
                            byte eventLog[] =
                                    (readEventLogParser.getRecords()).get((int) RECORD_NUMBER_1);

                            /* Log the result */
                            logger.info("EventLog file data: {}", ByteArrayUtils.toHex(eventLog));
                        }
                    } catch (KeypleReaderException e) {
                        e.printStackTrace();
                    }
                    logger.info(
                            "==================================================================================");
                    logger.info(
                            "= End of the Calypso PO processing.                                              =");
                    logger.info(
                            "==================================================================================");
                } else {
                    logger.error("The selection of the PO has failed.");
                }
                break;
            case SE_INSERTED:
                logger.error("A PO was detected but the selection has failed.");
                break;
            case SE_REMOVAL:
                logger.error("The PO has been removed.");
                break;
            default:
                break;
        }
    }

    /**
     * main program entry
     */
    public static void main(String[] args) throws InterruptedException, KeypleBaseException {
        /* Create the observable object to handle the PO processing */
        UseCase_Calypso2_DefaultSelectionNotification_Pcsc m =
                new UseCase_Calypso2_DefaultSelectionNotification_Pcsc();
    }
}
