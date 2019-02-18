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

/**
 * The Selector class is dedicated to handle the selection of the SE either through a selection
 * command with AID (AtrSelector) or through a matching test between the SE ATR and a regular
 * expression (AtrSelector).
 *
 */
public class Selector {
    /** Data for the processing of an application selection by AID */
    private final AidSelector aidSelector;
    /** Data for ATR based filtering */
    private final AtrFilter atrFilter;

    /**
     * Create a Selector to perform the SE selection
     * <p>
     * if aidSelector is null, no 'select application' command is generated. In this case the SE
     * must have a default application selected. (e.g. SAM or Rev1 Calypso cards)
     * <p>
     * if aidSelector is not null, a 'select application' command is generated and performed.
     * Furthermore, the status code is checked against the list of successful status codes in the
     * {@link AidSelector} to determine if the SE matched or not the selection data.
     * <p>
     * if atrFilter is null, no check of the ATR is performed. All SE will match.
     * <p>
     * if atrFilter is not null, the ATR of the SE is compared with the regular expression provided
     * in the {@link AtrFilter} in order to determine if the SE match or not the expected ATR.
     *
     * @param aidSelector an {@link AidSelector}, may be null
     * @param atrFilter an {@link AtrFilter}, may be null
     */
    public Selector(AidSelector aidSelector, AtrFilter atrFilter) {
        this.aidSelector = aidSelector;
        this.atrFilter = atrFilter;
    }

    /**
     * Get the {@link AidSelector}
     *
     * @return an {@link AidSelector}
     */
    public AidSelector getAidSelector() {
        return aidSelector;
    }

    /**
     * Get the {@link AtrFilter}
     *
     * @return an {@link AtrFilter}
     */
    public AtrFilter getAtrFilter() {
        return atrFilter;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
