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
package org.eclipse.keyple.example.calypso.common.transaction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Properties;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.example.generic.pc.ReaderUtilities;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;

public class CalypsoUtilities {
    private static Properties properties;

    static {
        properties = new Properties();

        String propertiesFileName = "config.properties";

        InputStream inputStream =
                CalypsoUtilities.class.getClassLoader().getResourceAsStream(propertiesFileName);

        try {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException(
                        "property file '" + propertiesFileName + "' not found!");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the default reader for PO communications
     * 
     * @param seProxyService the current ProxyService
     * @return a ProxyReader object
     * @throws KeypleBaseException if an error occurred
     */
    public static ProxyReader getDefaultPoReader(SeProxyService seProxyService)
            throws KeypleBaseException {
        ProxyReader poReader = ReaderUtilities.getReaderByName(seProxyService,
                properties.getProperty("po.reader.regex"));

        ReaderUtilities.setContactlessSettings(poReader);

        return poReader;
    }

    /**
     * Get the default reader for SAM communications
     * 
     * @param seProxyService the current ProxyService
     * @return a ProxyReader object
     * @throws KeypleBaseException if an error occurred
     */
    public static ProxyReader getDefaultSamReader(SeProxyService seProxyService)
            throws KeypleBaseException {
        ProxyReader samReader = ReaderUtilities.getReaderByName(seProxyService,
                properties.getProperty("sam.reader.regex"));

        ReaderUtilities.setContactsSettings(samReader);

        /*
         * Open logical channel for the SAM inserted in the reader
         *
         * (We expect the right is inserted)
         */
        checkSamAndOpenChannel(samReader);

        return samReader;
    }

    public static EnumMap<PoTransaction.SamSettings, Byte> getSamSettings() {
        /* define the SAM parameters to provide when creating PoTransaction */
        return new EnumMap<PoTransaction.SamSettings, Byte>(PoTransaction.SamSettings.class) {
            {
                put(PoTransaction.SamSettings.SAM_DEFAULT_KIF_PERSO,
                        PoTransaction.DEFAULT_KIF_PERSO);
                put(PoTransaction.SamSettings.SAM_DEFAULT_KIF_LOAD, PoTransaction.DEFAULT_KIF_LOAD);
                put(PoTransaction.SamSettings.SAM_DEFAULT_KIF_DEBIT,
                        PoTransaction.DEFAULT_KIF_DEBIT);
                put(PoTransaction.SamSettings.SAM_DEFAULT_KEY_RECORD_NUMBER,
                        PoTransaction.DEFAULT_KEY_RECORD_NUMER);
            }
        };
    }

    /**
     * Check SAM presence and consistency
     * <p>
     * Throw an exception if the expected SAM is not available
     *
     * @param samReader the SAM reader
     */
    public static void checkSamAndOpenChannel(ProxyReader samReader) {
        /*
         * check the availability of the SAM doing a ATR based selection, open its physical and
         * logical channels and keep it open
         */
        SeSelection samSelection = new SeSelection(samReader);

        SeSelector samSelector = new SeSelector(CalypsoClassicInfo.SAM_C1_ATR_REGEX,
                ChannelState.KEEP_OPEN, Protocol.ANY, "Selection SAM C1");

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelection(samSelector);

        try {
            if (!samSelection.processExplicitSelection()) {
                throw new IllegalStateException("Unable to open a logical channel for SAM!");
            } else {
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());

        }
    }
}
