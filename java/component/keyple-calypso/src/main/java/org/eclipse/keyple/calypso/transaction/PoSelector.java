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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.keyple.seproxy.SeSelector;

/**
 * The {@link PoSelector} class extends {@link SeSelector} to handle specific PO features such as
 * the additional successful status codes list (in response to a select application command)
 */
public final class PoSelector extends SeSelector {
    public enum InvalidatedPoAcceptance {
        REJECT_INVALIDATED, ACCEPT_INVALIDATED
    }

    /**
     * Create a PoSelector to perform the PO selection. See {@link SeSelector}
     * 
     * @param aidSelector the AID selection data
     * @param atrFilter the ATR filter
     * @param extraInfo information string (to be printed in logs)
     */
    public PoSelector(AidSelector aidSelector, AtrFilter atrFilter, String extraInfo) {
        super(aidSelector, atrFilter, extraInfo);
    }

    /**
     * AidSelector embedding the Calypo PO additional successful codes list
     */
    public static class AidSelector extends SeSelector.AidSelector {

        private final static Set<Integer> successfulSelectionStatusCodes = new HashSet<Integer>() {
            {
                add(0x6283);
            }
        };;

        public AidSelector(byte[] aidToSelect, InvalidatedPoAcceptance invalidatedPoAcceptance,
                FileOccurrence fileOccurrence, FileControlInformation fileControlInformation) {
            super(aidToSelect,
                    invalidatedPoAcceptance == InvalidatedPoAcceptance.ACCEPT_INVALIDATED
                            ? successfulSelectionStatusCodes
                            : null,
                    fileOccurrence, fileControlInformation);
        }

        public AidSelector(byte[] aidToSelect, InvalidatedPoAcceptance invalidatedPoAcceptance) {
            super(aidToSelect,
                    invalidatedPoAcceptance == InvalidatedPoAcceptance.ACCEPT_INVALIDATED
                            ? successfulSelectionStatusCodes
                            : null);
        }
    }

    /**
     * AtrFilter
     * <p>
     * Could be completed to handle Calypso specific ATR filtering process.
     */
    public static class AtrFilter extends SeSelector.AtrFilter {

        /**
         * Regular expression based filter
         *
         * @param atrRegex String hex regular expression
         */
        public AtrFilter(String atrRegex) {
            super(atrRegex);
        }
    }
}
