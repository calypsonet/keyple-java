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

import static org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild.SelectControl.FIRST;
import static org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild.SelectControl.NEXT;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
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
        SelectFileRespPars selectFileParser1First, selectFileParser1Next,
                selectFileParser1NextTransction, selectFileParser2First, selectFileParser2Next;

        try {
            SeSelection seSelection = new SeSelection(poReader);

            logger.info(
                    "==================================================================================");
            logger.info("Searching for 1st application with AID::" + aid);

            PoSelectionRequest poSelectionRequest1 = new PoSelectionRequest(
                    new SeSelector(new SeSelector.AidSelector(ByteArrayUtils.fromHex(aid), null),
                            null, "firstApplication"),
                    ChannelState.KEEP_OPEN, Protocol.ANY);

            selectFileParser1First = poSelectionRequest1.prepareNavigateCmd(FIRST, "First EF");

            selectFileParser1Next = poSelectionRequest1.prepareNavigateCmd(NEXT, "Next EF");

            CalypsoPo firstApplication =
                    (CalypsoPo) seSelection.prepareSelection(poSelectionRequest1);

            if (!seSelection.processExplicitSelection() || !firstApplication.isSelected()) {
                logger.info("1st application not found.");
                return;
            }

            logger.info("Selected 1st application with AID:: "
                    + ByteArrayUtils.toHex(firstApplication.getDfName()));

            if (selectFileParser1First.isSelectionSuccessful()) {
                logger.info("1st EF in current DF: {}", String.format(
                        "FILE TYPE = %02X, EF TYPE = %02X, LID = %04X, SID = %02X, REC_SIZE = %d",
                        selectFileParser1First.getFileType(), selectFileParser1First.getEfType(),
                        selectFileParser1First.getLid(), selectFileParser1First.getSfi(),
                        selectFileParser1First.getRecSize()));
            } else {
                logger.info("The selection of 1st EF in the current DF failed.");
            }


            if (selectFileParser1First.isSelectionSuccessful()) {
                logger.info("2nd EF in current DF: {}", String.format(
                        "FILE TYPE = %02X, EF TYPE = %02X, LID = %04X, SID = %02X, REC_SIZE = %d",
                        selectFileParser1Next.getFileType(), selectFileParser1Next.getEfType(),
                        selectFileParser1Next.getLid(), selectFileParser1Next.getSfi(),
                        selectFileParser1Next.getRecSize()));
            } else {
                logger.info("The selection of 2nd EF in the current DF failed.");
            }

            // additional selection
            PoTransaction poTransaction = new PoTransaction(poReader, firstApplication);

            selectFileParser1NextTransction = poTransaction.prepareNavigateCmd(NEXT, "Next EF");

            poTransaction.processPoCommands(ChannelState.KEEP_OPEN);

            if (selectFileParser1NextTransction.isSelectionSuccessful()) {
                logger.info("3rd EF in current DF: {}", String.format(
                        "FILE TYPE = %02X, EF TYPE = %02X, LID = %04X, SID = %02X, REC_SIZE = %d",
                        selectFileParser1NextTransction.getFileType(),
                        selectFileParser1NextTransction.getEfType(),
                        selectFileParser1NextTransction.getLid(),
                        selectFileParser1NextTransction.getSfi(),
                        selectFileParser1NextTransction.getRecSize()));
            } else {
                logger.info("The selection of 3rd EF in the current DF failed.");
            }

            logger.info("Searching for 2nd application with AID::" + aid);

            seSelection = new SeSelection(poReader);

            PoSelectionRequest poSelectionRequest2 =
                    new PoSelectionRequest(
                            new SeSelector(
                                    new SeSelector.AidSelector(ByteArrayUtils.fromHex(aid), null,
                                            SeSelector.AidSelector.FileOccurrence.NEXT,
                                            SeSelector.AidSelector.FileControlInformation.FCI),
                                    null, "secondApplication"),
                            ChannelState.KEEP_OPEN, Protocol.ANY);

            selectFileParser2First = poSelectionRequest2.prepareNavigateCmd(FIRST, "First EF");

            selectFileParser2Next = poSelectionRequest2.prepareNavigateCmd(NEXT, "Next EF");

            CalypsoPo secondApplication =
                    (CalypsoPo) seSelection.prepareSelection(poSelectionRequest2);

            if (!seSelection.processExplicitSelection() || !secondApplication.isSelected()) {
                logger.info("2nd application not found.");
                return;
            }

            logger.info("Selected 2nd application with AID:: "
                    + ByteArrayUtils.toHex(secondApplication.getDfName()));

            if (selectFileParser1First.isSelectionSuccessful()) {
                logger.info("1st EF in current DF: {}", String.format(
                        "FILE TYPE = %02X, EF TYPE = %02X, LID = %04X, SID = %02X, REC_SIZE = %d",
                        selectFileParser2First.getFileType(), selectFileParser2First.getEfType(),
                        selectFileParser2First.getLid(), selectFileParser2First.getSfi(),
                        selectFileParser2First.getRecSize()));
            } else {
                logger.info("The selection of 1st EF in the current DF failed.");
            }

            if (selectFileParser1First.isSelectionSuccessful()) {
                logger.info("2nd EF in current DF: {}", String.format(
                        "FILE TYPE = %02X, EF TYPE = %02X, LID = %04X, SID = %02X, REC_SIZE = %d",
                        selectFileParser2Next.getFileType(), selectFileParser2Next.getEfType(),
                        selectFileParser2Next.getLid(), selectFileParser2Next.getSfi(),
                        selectFileParser2Next.getRecSize()));
            } else {
                logger.info("The selection of 2nd EF in the current DF failed.");
            }

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
