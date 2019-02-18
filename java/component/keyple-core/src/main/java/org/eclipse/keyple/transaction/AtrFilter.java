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

import java.util.regex.Pattern;
import org.eclipse.keyple.util.ByteArrayUtils;

public class AtrFilter {
    /**
     * Regular expression dedicated to handle SE logical channel opening based on ATR pattern
     */
    private String atrRegex;

    /**
     * Regular expression based filter
     *
     * @param atrRegex String hex regular expression
     */
    public AtrFilter(String atrRegex) {
        this.atrRegex = atrRegex;
    }

    /**
     * Getter for the regular expression provided at construction time
     *
     * @return Regular expression string
     */
    public String getAtrRegex() {
        return atrRegex;
    }

    /**
     * Tells if the provided ATR matches the registered regular expression
     *
     * If the registered regular expression is empty, the ATR is always matching.
     *
     * @param atr a buffer containing the ATR to be checked
     * @return a boolean true the ATR matches the current regex
     */
    public boolean atrMatches(byte[] atr) {
        boolean m;
        if (atrRegex.length() != 0) {
            Pattern p = Pattern.compile(atrRegex);
            String atrString = ByteArrayUtils.toHex(atr);
            m = p.matcher(atrString).matches();
        } else {
            m = true;
        }
        return m;
    }

    /**
     * Print out the ATR regex
     *
     * @return a string
     */
    public String toString() {
        return String.format("ATR regex:%s", atrRegex.length() != 0 ? atrRegex : "empty");
    }
}
