/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.example.pc.calypso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.eclipse.keyple.calypso.transaction.CalypsoPO;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.common.generic.DemoHelpers;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;



public class Demo_CalypsoAuthenticationLeve3_Pcsc extends DemoHelpers {
    private static Properties properties;

    @Override
    public void operatePoTransactions() {

    }

    @SuppressWarnings("unused")
    static class CalypsoAuthenticationLeve3TransactionEngine extends DemoHelpers
            implements ObservableReader.ReaderObserver {
        private final Logger logger =
                LoggerFactory.getLogger(CalypsoAuthenticationLeve3TransactionEngine.class);

        private final ProxyReader poReader, csmReader;
        private boolean csmChannelOpen;

        /* define the CSM parameters to provide when creating PoTransaction */
        final EnumMap<PoTransaction.CsmSettings, Byte> csmSetting =
                new EnumMap<PoTransaction.CsmSettings, Byte>(PoTransaction.CsmSettings.class) {
                    {
                        put(PoTransaction.CsmSettings.CS_DEFAULT_KIF_PERSO,
                                PoTransaction.DEFAULT_KIF_PERSO);
                        put(PoTransaction.CsmSettings.CS_DEFAULT_KIF_LOAD,
                                PoTransaction.DEFAULT_KIF_LOAD);
                        put(PoTransaction.CsmSettings.CS_DEFAULT_KIF_DEBIT,
                                PoTransaction.DEFAULT_KIF_DEBIT);
                        put(PoTransaction.CsmSettings.CS_DEFAULT_KEY_RECORD_NUMBER,
                                PoTransaction.DEFAULT_KEY_RECORD_NUMER);
                    }
                };

        public CalypsoAuthenticationLeve3TransactionEngine(ProxyReader poReader,
                ProxyReader csmReader) {
            this.poReader = poReader;
            this.csmReader = csmReader;
        }

        public void operatePoTransactions() {
            Profiler profiler;
            try {
                /* first time: check CSM */
                if (!this.csmChannelOpen) {
                    /* the following method will throw an exception if the CSM is not available. */
                    checkCsmAndOpenChannel(csmReader);
                    this.csmChannelOpen = true;
                }

                profiler = new Profiler("Entire transaction");

                /* operate PO selection */
                String poAid = properties.getProperty("po.aid");

                /*
                 * Prepare the selection using the SeSelection class
                 */
                SeSelection seSelection = new SeSelection(poReader);

                /* AID based selection */
                seSelection.addSelector(new PoSelector(ByteArrayUtils.fromHex(poAid), true, null,
                        PoSelector.RevisionTarget.TARGET_REV3));

                /* Time measurement */
                profiler.start("Initial selection");

                List<SeResponse> seResponses = seSelection.processSelection().getResponses();

                /*
                 * If the Calypso selection succeeded we should have 2 responses and the 2nd one not
                 * null
                 */
                if (seResponses.size() == 1 && seResponses.get(0) != null) {

                    profiler.start("Calypso1");

                    PoTransaction poTransaction = new PoTransaction(poReader,
                            new CalypsoPO(seResponses.get(0)), csmReader, csmSetting);
                    /*
                     * Open Session for the debit key
                     */
                    SeResponse seResponse = poTransaction.processOpening(
                            PoTransaction.ModificationMode.ATOMIC,
                            PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0, (byte) 0);
                    if (!poTransaction.wasRatified()) {
                        logger.info(
                                "========= Previous Secure Session was not ratified. =====================");
                    }
                    /*
                     * Close the Secure Session.
                     */

                    if (logger.isInfoEnabled()) {
                        logger.info(
                                "========= PO Calypso session ======= Closing ============================");
                    }

                    /*
                     * A ratification command will be sent (CONTACTLESS_MODE).
                     */
                    seResponse = poTransaction.processClosing(
                            PoTransaction.CommunicationMode.CONTACTLESS_MODE, false);
                } else {
                    logger.error(
                            "No Calypso transaction. SeResponse to Calypso selection was null.");
                }
                profiler.stop();
                logger.warn(System.getProperty("line.separator") + "{}", profiler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final Object waitForEnd = new Object();

    public static void main(String[] args)
            throws KeypleBaseException, InterruptedException, IOException {

        final Logger logger = LoggerFactory.getLogger(Demo_CalypsoBasic_Pcsc.class);

        properties = new Properties();

        String propertiesFileName = "config.properties";

        InputStream inputStream = Demo_CalypsoAuthenticationLeve3_Pcsc.class.getClassLoader()
                .getResourceAsStream(propertiesFileName);

        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException(
                    "property file '" + propertiesFileName + "' not found!");
        }

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /*
         * Get PO and CSM readers. Apply regulars expressions to reader names to select PO / CSM
         * readers. Use the getReader helper method from the transaction engine.
         */
        ProxyReader poReader =
                getReaderByName(seProxyService, properties.getProperty("po.reader.regex"));
        ProxyReader csmReader =
                getReaderByName(seProxyService, properties.getProperty("csm.reader.regex"));

        /* Both readers are expected not null */
        if (poReader == csmReader || poReader == null || csmReader == null) {
            throw new IllegalStateException("Bad PO/CSM setup");
        }

        logger.info("PO Reader  NAME = {}", poReader.getName());
        logger.info("CSM Reader  NAME = {}", csmReader.getName());

        /* Set PcSc settings per reader */
        poReader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
        poReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T1);
        csmReader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
        csmReader.setParameter(PcscReader.SETTING_KEY_PROTOCOL, PcscReader.SETTING_PROTOCOL_T0);

        /*
         * PC/SC card access mode:
         *
         * The CSM is left in the SHARED mode (by default) to avoid automatic resets due to the
         * limited time between two consecutive exchanges granted by Windows.
         *
         * The PO reader is set to EXCLUSIVE mode to avoid side effects during the selection step
         * that may result in session failures.
         *
         * These two points will be addressed in a coming release of the Keyple PcSc reader plugin.
         */
        csmReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);
        poReader.setParameter(PcscReader.SETTING_KEY_MODE, PcscReader.SETTING_MODE_SHARED);

        /* Set the PO reader protocol flag */
        poReader.addSeProtocolSetting(
                new SeProtocolSetting(PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        /* Setting up the transaction engine (implements Observer) */
        CalypsoAuthenticationLeve3TransactionEngine transactionEngine =
                new CalypsoAuthenticationLeve3TransactionEngine(poReader, csmReader);

        /* Set terminal as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(transactionEngine);

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }
}
