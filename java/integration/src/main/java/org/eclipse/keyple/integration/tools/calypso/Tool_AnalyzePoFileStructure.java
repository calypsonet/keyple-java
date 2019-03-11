/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.integration.tools.calypso;

import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.integration.example.pc.calypso.DemoUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tool_AnalyzePoFileStructure {

    private static final Logger logger = LoggerFactory.getLogger(Tool_AnalyzePoFileStructure.class);

    // private PoStructure poStructure = null;

    protected static void getApplicationData(String aid, SeReader poReader) {

        try {
            SeSelection seSelection = new SeSelection(poReader);

            logger.info(
                    "==================================================================================");
            logger.info("Searching for 1st application with AID::" + aid);

            CalypsoPo firstApplication =
                    (CalypsoPo) seSelection
                            .prepareSelection(
                                    new PoSelectionRequest(
                                            new SeSelector(
                                                    new SeSelector.AidSelector(
                                                            ByteArrayUtils.fromHex(aid), null),
                                                    null, "firstApplication"),
                                            ChannelState.KEEP_OPEN, Protocol.ANY));

            if (!seSelection.processExplicitSelection() || !firstApplication.isSelected()) {
                logger.info("1st application not found.");
                return;
            }

            logger.info("Selected 1st application with AID:: "
                    + ByteArrayUtils.toHex(firstApplication.getDfName()));

            logger.info("Searching for 2nd application with AID::" + aid);

            seSelection = new SeSelection(poReader);

            CalypsoPo secondApplication =
                    (CalypsoPo) seSelection.prepareSelection(new PoSelectionRequest(
                            new SeSelector(
                                    new SeSelector.AidSelector(ByteArrayUtils.fromHex(aid), null,
                                            SeSelector.AidSelector.FileOccurrence.NEXT,
                                            SeSelector.AidSelector.FileControlInformation.FCI),
                                    null, "secondApplication"),
                            ChannelState.KEEP_OPEN, Protocol.ANY));

            if (!seSelection.processExplicitSelection() || !secondApplication.isSelected()) {
                logger.info("2nd application not found.");
                return;
            }

            logger.info("Selected 2nd application with AID:: "
                    + ByteArrayUtils.toHex(secondApplication.getDfName()));

        } catch (Exception e) {
            logger.error("Exception: " + e.getCause());
        }
    }

    public static void main(String[] args) throws KeypleBaseException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        SeReader poReader =
                DemoUtilities.getReader(seProxyService, DemoUtilities.PO_READER_NAME_REGEX);

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        logger.info("= PO Reader  NAME = {}", poReader.getName());
        /* Check if a PO is present in the reader */
        if (poReader.isSePresent()) {

            /* Supported Base AID */
            String poMasterFileAid = "334D54522E";
            String poTransportFileAid = "315449432E";
            String poHoplinkAid = "A000000291";
            String poStoredValueAid = "304554502E";
            String nfcNdefAid = "D276000085";

            getApplicationData(poMasterFileAid, poReader);

            getApplicationData(poTransportFileAid, poReader);

            getApplicationData(poHoplinkAid, poReader);

            getApplicationData(poStoredValueAid, poReader);

            getApplicationData(nfcNdefAid, poReader);

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= End of the Calypso PO Analysis.                                                =");
            logger.info(
                    "==================================================================================");
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
