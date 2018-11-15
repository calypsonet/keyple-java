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
package org.eclipse.keyple.integration.calypso;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;

public class TestEngine {

    public static ProxyReader poReader, samReader;

    public static PoFileStructureInfo selectPO()
            throws IllegalArgumentException, KeypleReaderException {

        try {
            if (!samReader.isSePresent()) {
                throw new IllegalStateException("No SAM present in the reader.");
            }
        } catch (NoStackTraceThrowable noStackTraceThrowable) {
            throw new KeypleReaderException("Exception raised while checking SE presence.");
        }

        // operate PO multiselection
        String SAM_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80[0-9a-fA-F]{16}829000";

        // check the availability of the SAM, open its physical and logical channels and keep it
        // open
        SeSelection samSelection = new SeSelection(samReader);

        SeSelector samSelector = new SeSelector(SAM_ATR_REGEX, true, null, "SAM Selection");

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelector(samSelector);

        try {
            if (!samSelection.processExplicitSelection()) {
                System.out.println("Unable to open a logical channel for SAM!");
                throw new IllegalStateException("SAM channel opening failure");
            } else {
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());

        }

        SeSelection seSelection = new SeSelection(poReader);

        // Add Audit C0 AID to the list
        seSelection.prepareSelector(new PoSelector(

                ByteArrayUtils.fromHex(PoFileStructureInfo.poAuditC0Aid), false, true, null,
                PoSelector.RevisionTarget.TARGET_REV3, "Audit C0"));

        // Add CLAP AID to the list
        seSelection
                .prepareSelector(new PoSelector(ByteArrayUtils.fromHex(PoFileStructureInfo.clapAid),
                        false, true, null, PoSelector.RevisionTarget.TARGET_REV3, "CLAP"));

        // Add cdLight AID to the list
        seSelection.prepareSelector(
                new PoSelector(ByteArrayUtils.fromHex(PoFileStructureInfo.cdLightAid), false, true,
                        null, PoSelector.RevisionTarget.TARGET_REV2_REV3, "CDLight"));

        if (seSelection.processExplicitSelection()) {
            return new PoFileStructureInfo(seSelection.getSelectedSe());
        }

        throw new IllegalArgumentException("No recognizable PO detected.");
    }

    private static ProxyReader getReader(SeProxyService seProxyService, String pattern)
            throws KeypleReaderException {

        Pattern p = Pattern.compile(pattern);
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            for (ProxyReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        return null;
    }

    public static void configureReaders()
            throws IOException, InterruptedException, KeypleBaseException {

        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> pluginsSet = new ConcurrentSkipListSet<ReaderPlugin>();
        pluginsSet.add(PcscPlugin.getInstance());
        seProxyService.setPlugins(pluginsSet);

        String PO_READER_NAME_REGEX = ".*(ASK|ACS).*";
        String SAM_READER_NAME_REGEX = ".*(Cherry TC|SCM Microsystems|Identive|HID).*";

        poReader = getReader(seProxyService, PO_READER_NAME_REGEX);
        samReader = getReader(seProxyService, SAM_READER_NAME_REGEX);


        if (poReader == samReader || poReader == null || samReader == null) {
            throw new IllegalStateException("Bad PO/SAM setup");
        }

        System.out.println(
                "\n==================================================================================");
        System.out.println("PO Reader  : " + poReader.getName());
        System.out.println("SAM Reader : " + samReader.getName());
        System.out.println(
                "==================================================================================");

        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        samReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        // provide the reader with the map
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
    }

}
