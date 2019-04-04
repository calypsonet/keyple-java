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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.builder.session.ChangeKeyCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.session.GetChallengeCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.session.GetChallengeRespPars;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.builder.session.CardGenerateKeyCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.session.GiveRandomCmdBuild;
import org.eclipse.keyple.calypso.command.sam.builder.session.SelectDiversifierCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.session.CardGenerateKeyRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.integration.example.pc.calypso.DemoUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelectionRequest;
import org.eclipse.keyple.transaction.SelectionsResult;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tool_LoadKeys {
    private static final Logger logger = LoggerFactory.getLogger(Tool_LoadKeys.class);

    /**
     * Load a key
     * @param poReader
     * @param samReader
     * @param calypsoPo
     * @param keyIndex
     * @param cipheringKeyKif if null the ciphering key is the null key
     * @param cipheringKeyKvc if null the ciphering key is the null key
     * @param sourceKeyKif
     * @param sourceKeyKvc
     * @return execution status of the change key command
     * @throws KeypleReaderException
     */
    private static boolean loadKey(ProxyReader poReader, ProxyReader samReader, CalypsoPo calypsoPo,
            byte keyIndex, Byte cipheringKeyKif, Byte cipheringKeyKvc, byte sourceKeyKif,
            byte sourceKeyKvc) throws KeypleReaderException {
        // create an apdu requests list to handle PO and SAM commands
        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();

        // get the challenge from the PO
        apduRequests.add(new GetChallengeCmdBuild(calypsoPo.getPoClass()).getApduRequest());

        SeRequest seRequest = new SeRequest(apduRequests, ChannelState.KEEP_OPEN);

        SeResponse seResponse = poReader.transmit(seRequest);

        if (seResponse == null || !seResponse.getApduResponses().get(0).isSuccessful()) {
            throw new IllegalStateException("PO get challenge command failed.");
        }

        GetChallengeRespPars getChallengeRespPars =
                new GetChallengeRespPars(seResponse.getApduResponses().get(0));
        byte[] poChallenge = getChallengeRespPars.getPoChallenge();

        // send diversifier, PO challenge and Card Generate key commands to the SAM (default
        // revision), get the ciphered data
        apduRequests.clear();

        apduRequests.add(new SelectDiversifierCmdBuild(SamRevision.C1,
                calypsoPo.getApplicationSerialNumber()).getApduRequest());
        apduRequests.add(new GiveRandomCmdBuild(SamRevision.C1, poChallenge).getApduRequest());
        if (cipheringKeyKif == null || cipheringKeyKvc == null) {
            apduRequests.add(new CardGenerateKeyCmdBuild(SamRevision.C1, sourceKeyKif, sourceKeyKvc)
                    .getApduRequest());
        } else {
            apduRequests.add(new CardGenerateKeyCmdBuild(SamRevision.C1, cipheringKeyKif,
                    cipheringKeyKvc, sourceKeyKif, sourceKeyKvc).getApduRequest());
        }

        seResponse = samReader.transmit(seRequest);

        if (seResponse == null || !seResponse.getApduResponses().get(2).isSuccessful()) {
            throw new IllegalStateException("PO get challenge command failed.");
        }

        CardGenerateKeyRespPars cardGenerateKeyRespPars =
                new CardGenerateKeyRespPars(seResponse.getApduResponses().get(2));
        byte[] cipheredData = cardGenerateKeyRespPars.getCipheredData();

        logger.info("Ciphered data: {}", ByteArrayUtils.toHex(cipheredData));

        // send change key command to the PO
        apduRequests.clear();

        apduRequests.add(new ChangeKeyCmdBuild(calypsoPo.getPoClass(), keyIndex, cipheredData)
                .getApduRequest());

        seResponse = poReader.transmit(seRequest);

        return (seResponse != null && seResponse.getApduResponses().get(0).isSuccessful()) ? true
                : false;
    }

    /**
     * Main entry
     * @param args
     * @throws KeypleBaseException
     * @throws NoStackTraceThrowable
     */
    public static void main(String[] args) throws KeypleBaseException, NoStackTraceThrowable {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        ProxyReader poReader = (ProxyReader) DemoUtilities.getReader(seProxyService,
                DemoUtilities.PO_READER_NAME_REGEX);

        ProxyReader samReader = (ProxyReader) DemoUtilities.getReader(seProxyService,
                DemoUtilities.SAM_READER_NAME_REGEX);

        /* Check if the readers exist */
        if (poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO/SAM reader setup");
        }

        logger.info("= PO Reader   NAME = {}", poReader.getName());
        logger.info("= SAM Reader  NAME = {}", samReader.getName());

        samReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the settings
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // do the SAM selection to open the logical channel
        final String SAM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        SeSelection samSelection = new SeSelection();

        SeSelectionRequest samSelectionRequest = new SeSelectionRequest(
                new SeSelector(null, new SeSelector.AtrFilter(SAM_ATR_REGEX), "SAM Selection"),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(samSelectionRequest);

        try {
            if (!samSelection.processExplicitSelection(samReader).hasActiveSelection()) {
                System.out.println("Unable to open a logical channel for SAM!");
                throw new IllegalStateException("SAM channel opening failure");
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());
        }

        // Check if a PO is present in the reader
        if (poReader.isSePresent()) {
            // do the PO selection
            byte[] aid = ByteArrayUtils.fromHex("315449432E49434131");

            SeSelection seSelection = new SeSelection();

            seSelection.prepareSelection(
                    new PoSelectionRequest(new SeSelector(new SeSelector.AidSelector(aid, null),
                            null, "Calypso Classic AID"), ChannelState.KEEP_OPEN, Protocol.ANY));

            SelectionsResult selectionsResult = seSelection.processExplicitSelection(poReader);

            if (selectionsResult == null || !selectionsResult.hasActiveSelection()) {
                throw new IllegalStateException("No recognizable PO detected.");
            }

            // the selection succeeded, get the CalypsoPo
            CalypsoPo calypsoPo = (CalypsoPo) selectionsResult.getActiveSelection().getMatchingSe();

            // load key 21/7E index 1 ciphered by the null key
            loadKey(poReader, samReader, calypsoPo, (byte) 1, null, null, (byte) 0x21, (byte) 0x7E);

            // load key 27/7E index 3 ciphered by the 21/7E
            loadKey(poReader, samReader, calypsoPo, (byte) 3, (byte) 0x21, (byte) 0x7E, (byte) 0x27,
                    (byte) 0x7E);

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= End of the Calypso PO key loading.                                                =");
            logger.info(
                    "==================================================================================");
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
