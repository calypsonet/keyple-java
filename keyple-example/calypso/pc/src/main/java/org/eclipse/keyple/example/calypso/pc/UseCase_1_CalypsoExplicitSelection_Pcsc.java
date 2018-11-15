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


import java.io.IOException;
import org.eclipse.keyple.calypso.transaction.PoSelector;
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
    private static SeSelection seSelection;
    private ProxyReader poReader;
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

        if (poReader.isSePresent()) {
            /*
             * Initialize the selection process for the poReader
             */
            seSelection = new SeSelection(poReader);

            /* AID based selection */
            seSelection.prepareSelection(new PoSelector(ByteArrayUtils.fromHex(poAid), false, true,
                    null, PoSelector.RevisionTarget.TARGET_REV3, "AID: " + poAid));

            if (seSelection.processExplicitSelection()) {
                logger.info("The selection of the PO has succeeded.");
            } else {
                logger.error("The selection of the PO has failed.");
            }
        } else {
            logger.error("No PO were detected.");
        }
        System.exit(0);
    }
}
