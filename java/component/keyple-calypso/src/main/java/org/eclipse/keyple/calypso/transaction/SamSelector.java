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
package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.seproxy.SeSelector;

/**
 * The {@link SamSelector} class extends {@link SeSelector} to handle specific Calypso SAM needs
 * such as model identification.
 */
public class SamSelector extends SeSelector {
    /**
     * Create a SeSelector to perform the SAM selection
     * <p>
     * Two optional parameters
     *
     * @param subtype the expected SAM subtype
     * @param serialNumber the expected serial number as an hex string (padded with 0 on the left).
     *        May be a sub regex (e.g. "AEC0....")
     * @param extraInfo information string (to be printed in logs)
     */
    public SamSelector(byte subtype, String serialNumber, String extraInfo) {
        super(null, new AtrFilter(null), null);
        String atrRegex;
        String snRegex;
        /* check if serialNumber is defined */
        if (serialNumber == null || serialNumber.isEmpty()) {
            /* match all serial numbers */
            snRegex = ".{8}";
        } else {
            /* match the provided serial number (could be a regex substring) */
            snRegex = serialNumber;
        }
        /*
         * build the final Atr regex according to the SAM subtype and serial number if any.
         *
         * The header is starting with 3B, its total length is 4 or 6 bytes (8 or 10 hex digits)
         */
        switch (subtype) {
            case CalypsoSam.C1:
                atrRegex = "3B(.{6}|.{10})805A..80C120.{4}" + snRegex + "829000";
                break;
            case CalypsoSam.E1:
                atrRegex = "3B(.{6}|.{10})805A..80E120.{4}" + snRegex + "829000";
                break;
            case CalypsoSam.ANY:
                /* match any ATR */
                atrRegex = ".*";
                break;
            default:
                throw new IllegalArgumentException("Unknown SAM subtype.");
        }
        this.getAtrFilter().setAtrRegex(atrRegex);
    }
}
