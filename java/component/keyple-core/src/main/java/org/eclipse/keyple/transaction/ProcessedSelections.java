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

import java.util.ArrayList;
import java.util.List;

/**
 * The ProcessedSelections class holds the result of a selection process.
 * <p>
 * embeds a list of {@link ProcessedSelection}
 * <p>
 * provides a set of methods to retrieve the active selection (getActiveSelection) or a particular
 * selection specified by its index.
 */
public class ProcessedSelections {
    private List<ProcessedSelection> processedSelectionList = new ArrayList<ProcessedSelection>();

    /**
     * Append a {@link ProcessedSelection} to the internal list
     * 
     * @param processedSelection the item to add
     */
    public void addProcessedSelection(ProcessedSelection processedSelection) {
        processedSelectionList.add(processedSelection);
    }

    /**
     * @return the currently active (matching) selection
     */
    public ProcessedSelection getActiveSelection() {
        ProcessedSelection activeSelection = null;
        for (ProcessedSelection processedSelection : processedSelectionList) {
            if (processedSelection != null && processedSelection.getMatchingSe().isSelected()) {
                activeSelection = processedSelection;
                break;
            }
        }
        return activeSelection;
    }

    /**
     * Gets a selection result
     * 
     * @param selectionIndex the selection index
     * @return the {@link ProcessedSelection}
     */
    public ProcessedSelection getProcessedSelection(int selectionIndex) {
        if (processedSelectionList.size() == 0
                || selectionIndex > (processedSelectionList.size() - 1)) {
            throw new IllegalStateException("Bad selection index.");
        }
        return processedSelectionList.get(selectionIndex);
    }
}
