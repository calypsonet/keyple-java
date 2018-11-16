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


import static org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo.RECORD_NUMBER_1;
import static org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo.SFI_EnvironmentAndHolder;
import static org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo.SFI_EventLog;
import java.io.IOException;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.transaction.CalypsoUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UseCase_1_CalypsoExplicitSelection_Pcsc {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_1_CalypsoExplicitSelection_Pcsc.class);
    private static String poAid = "A0000004040125090101";


    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException, NoStackTraceThrowable {

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
        ProxyReader poReader = CalypsoUtilities.getDefaultPoReader(seProxyService);

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());

        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {
            /*
             * Initialize the selection process for the poReader
             */
            SeSelection seSelection = new SeSelection(poReader);

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            PoSelector poSelector = new PoSelector(ByteArrayUtils.fromHex(poAid), false, true, null,
                    PoSelector.RevisionTarget.TARGET_REV3, "AID: " + poAid);

            ReadRecordsRespPars readEnvironmentParser = poSelector.prepareReadRecordsCmd(
                    SFI_EnvironmentAndHolder, ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1,
                    (byte) 0x00,
                    String.format("EnvironmentAndHolder (SFI=%02X))", SFI_EnvironmentAndHolder));

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            CalypsoPo calypsoPo = (CalypsoPo) seSelection.prepareSelection(poSelector);

            /* Operate through a single SeRequestSet the Calypso PO selection and the file read */
            if (seSelection.processExplicitSelection()) {
                logger.info("The selection of the PO has succeeded.");

                /* Retrieve the data read */
                byte environmentAndHolder[] =
                        (readEnvironmentParser.getRecords()).get((int) RECORD_NUMBER_1);

                logger.info("Environment file data: {}",
                        ByteArrayUtils.toHex(environmentAndHolder));

                /* Go on with the reading of the first record of the EventLog file */

                PoTransaction poTransaction = new PoTransaction(poReader, calypsoPo);

                ReadRecordsRespPars readEventLogParser = poTransaction.prepareReadRecordsCmd(
                        SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA, RECORD_NUMBER_1,
                        (byte) 0x00, String.format("EventLog (SFI=%02X, recnbr=%d))", SFI_EventLog,
                                RECORD_NUMBER_1));

                if (poTransaction.processPoCommands()) {
                    logger.info("The reading of the EventLog has succeeded.");

                    /* Retrieve the data read */
                    byte eventLog[] = (readEventLogParser.getRecords()).get((int) RECORD_NUMBER_1);

                    logger.info("EventLog file data: {}", ByteArrayUtils.toHex(eventLog));
                }
            } else {
                logger.error("The selection of the PO has failed.");
            }
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
