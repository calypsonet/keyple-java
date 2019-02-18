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
package org.eclipse.keyple.transaction;

import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.keyple.util.ByteArrayUtils;

public class AidSelector {

    public static final int AID_MIN_LENGTH = 5;
    public static final int AID_MAX_LENGTH = 16;
    protected SeSelector.SelectMode selectMode = SeSelector.SelectMode.FIRST;

    /**
     * - AID’s bytes of the SE application to select. In case the SE application is currently not
     * selected, a logical channel is established and the corresponding SE application is selected
     * by the SE reader, otherwise keep the current channel.
     *
     * - Could be missing when operating SE which don’t support the Select Application command (as
     * it is the case for SAM).
     */
    private byte[] aidToSelect;

    /**
     * List of status codes in response to the select application command that should be considered
     * successful although they are different from 9000
     */
    Set<Integer> successfulSelectionStatusCodes = new LinkedHashSet<Integer>();

    /**
     * AID based selector without successfulSelectionStatusCodes
     *
     * @param aidToSelect byte array
     */
    public AidSelector(byte[] aidToSelect) {
        this.aidToSelect = aidToSelect;
    }

    /**
     * AID based selector with selection mode
     * <p>
     * The selectMode parameter defines the selection options P2 of the SELECT command message
     * <ul>
     * <li>false: first or only occurrence</li>
     * <li>true: next occurrence</li>
     * </ul>
     *
     * @param aidToSelect byte array
     * @param selectMode selection mode FIRST or NEXT
     */
    public AidSelector(byte[] aidToSelect, SeSelector.SelectMode selectMode) {
        this(aidToSelect);
        this.selectMode = selectMode;
    }


    /**
     * Getter for the AID provided at construction time
     *
     * @return byte array containing the AID
     */
    public byte[] getAidToSelect() {
        return aidToSelect;
    }

    /**
     * Indicates whether the selection command is targeting the first or the next occurrence
     *
     * @return true or false
     */
    public boolean isSelectNext() {
        return selectMode == SeSelector.SelectMode.NEXT;
    }

    /**
     * Sets the list of successful selection status codes
     * 
     * @param successfulSelectionStatusCodes
     */
    public void setSuccessfulSelectionStatusCodes(Set<Integer> successfulSelectionStatusCodes) {
        this.successfulSelectionStatusCodes = successfulSelectionStatusCodes;
    }

    /**
     * Gets the list of successful selection status codes
     *
     * @return the list of status codes
     */
    public Set<Integer> getSuccessfulSelectionStatusCodes() {
        return successfulSelectionStatusCodes;
    }


    /**
     * Print out the AID in hex
     *
     * @return a string
     */
    public String toString() {
        return String.format("AID:%s",
                aidToSelect == null ? "null" : ByteArrayUtils.toHex(aidToSelect));
    }
}
