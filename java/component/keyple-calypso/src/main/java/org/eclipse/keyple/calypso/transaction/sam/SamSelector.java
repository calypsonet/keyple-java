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
package org.eclipse.keyple.calypso.transaction.sam;

import org.eclipse.keyple.seproxy.SeSelector;

/**
 * The {@link SamSelector} class extends {@link SeSelector} to handle specific Calypso SAM needs
 * such as model identification.
 */
public class SamSelector extends SeSelector {
    /**
     * Create a SeSelector to perform the SAM selection
     * <p>
     * Two optional parameters.
     *
     * @param samIdentifier the expected SAM identification: revision (subtype), serial number as an
     *        hex string (padded with 0 on the left; can be a sub regex e.g. "AEC0....") and
     *        groupReference (not needed here).
     * @param extraInfo information string (to be printed in logs)
     */
    public SamSelector(SamIdentifier samIdentifier, String extraInfo) {
        super(null, new AtrFilter(null), extraInfo);
        String atrRegex;
        String snRegex;
        /* check if serialNumber is defined */
        if (samIdentifier.getSerialNumber() == null || samIdentifier.getSerialNumber().isEmpty()) {
            /* match all serial numbers */
            snRegex = ".{8}";
        } else {
            /* match the provided serial number (could be a regex substring) */
            snRegex = samIdentifier.getSerialNumber();
        }
        /*
         * build the final Atr regex according to the SAM subtype and serial number if any.
         *
         * The header is starting with 3B, its total length is 4 or 6 bytes (8 or 10 hex digits)
         */
        switch (samIdentifier.getSamRevision()) {
            case C1:
            case S1D:
            case S1E:
                atrRegex = "3B(.{6}|.{10})805A..80"
                        + samIdentifier.getSamRevision().getApplicationTypeMask() + "20.{4}"
                        + snRegex + "829000";
                break;
            case AUTO:
                /* match any ATR */
                atrRegex = ".*";
                break;
            default:
                throw new IllegalArgumentException("Unknown SAM subtype.");
        }
        this.getAtrFilter().setAtrRegex(atrRegex);
    }
}
