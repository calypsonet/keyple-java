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
package org.eclipse.keyple.example.generic.pc;


import java.io.IOException;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UseCase_Generic1_ExplicitSelectionAid_Pcsc {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Generic1_ExplicitSelectionAid_Pcsc.class);
    private static String seAid = "A0000004040125090101"; /* Here a Calypso AID */


    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /*
         * Get a SE reader ready to work with generic SE. Use the getReader helper method from the
         * ReaderUtilities class.
         */
        ProxyReader seReader = ReaderUtilities.getDefaultContactLessSeReader(seProxyService);

        /* Check if the reader exists */
        if (seReader == null) {
            throw new IllegalStateException("Bad SE reader setup");
        }

        logger.info(
                "=============== UseCase Generic #1: AID based explicit selection ==================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        /* Check if a SE is present in the reader */
        if (seReader.isSePresent()) {

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= AID based selection.                                                           =");
            logger.info(
                    "==================================================================================");

            /*
             * Prepare the SE selection
             */
            SeSelection seSelection = new SeSelection(seReader);

            /*
             * Setting of an AID based selection (in this example a Calypso REV3 PO)
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Generic selection: configures a SeSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            SeSelector seSelector = new SeSelector(ByteArrayUtils.fromHex(seAid),
                    SeSelector.SelectMode.FIRST, SeRequest.ChannelState.KEEP_OPEN,
                    /* protocolFlag */ null, "AID: " + seAid);

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            MatchingSe matchingSe = seSelection.prepareSelection(seSelector);

            /*
             * Actual SE communication: operate through a single request the SE selection
             */
            if (seSelection.processExplicitSelection()) {
                logger.info("The selection of the SE has succeeded.");
                logger.info("Application FCI = {}",
                        matchingSe.getSelectionSeResponse().getSelectionStatus().getFci());

                logger.info(
                        "==================================================================================");
                logger.info(
                        "= End of the generic SE processing.                                              =");
                logger.info(
                        "==================================================================================");
            } else {
                logger.error("The selection of the SE has failed.");
            }
        } else {
            logger.error("No SE were detected.");
        }
        System.exit(0);
    }
}
