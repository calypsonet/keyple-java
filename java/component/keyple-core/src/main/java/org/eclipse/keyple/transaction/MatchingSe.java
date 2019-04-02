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
package org.eclipse.keyple.transaction;

import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SelectionStatus;

/**
 * MatchingSe is the class to manage the elements of the result of a selection.
 *
 */
public class MatchingSe {
    private SelectionStatus selectionStatus;
    private final String selectionExtraInfo;

    /**
     * Constructor.
     */
    public MatchingSe(String extraInfo) {
        selectionStatus = null;
        this.selectionExtraInfo = extraInfo;
    }

    /**
     * Sets the SeResponse obtained in return from the selection process
     * 
     * @param selectionResponse the selection SeResponse
     */
    public void setSelectionResponse(SeResponse selectionResponse) {
        if (selectionResponse != null) {
            selectionStatus = selectionResponse.getSelectionStatus();
        }
    }

    /**
     * Indicates whether the current SE has been identified as selected: the logical channel is open
     * and the selection process returned either a FCI or an ATR
     * 
     * @return true or false
     */
    public final boolean isSelected() {
        boolean isSelected;
        if (selectionStatus != null) {
            isSelected = selectionStatus.hasMatched();
        } else {
            isSelected = false;
        }
        return isSelected;
    }

    /**
     * @return the SE {@link SelectionStatus}
     */
    public SelectionStatus getSelectionStatus() {
        return selectionStatus;
    }

    /**
     * @return the selection extra info string
     */
    public String getSelectionExtraInfo() {
        return selectionExtraInfo;
    }
}
