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

import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelectionRequest class groups the information data and methods used to build and handle a
 * selection request
 */
public class SeSelectionRequest {
    private static final Logger logger = LoggerFactory.getLogger(SeSelectionRequest.class);

    private SeSelector seSelector;
    private Class<? extends MatchingSe> matchingClass = MatchingSe.class;
    private Class<? extends SeSelectionRequest> selectionClass = SeSelectionRequest.class;

    public SeSelectionRequest(SeSelector seSelector) {
        this.seSelector = seSelector;
        if (logger.isTraceEnabled()) {
            logger.trace("SeSelection");
        }
    }

    /**
     * Returns a selection SeRequest built from the information provided in the constructor and
     * possibly completed with the seSelectionApduRequestList
     *
     * @return the selection SeRequest
     */
    protected final SeRequest getSelectorRequest() {
        SeRequest seSelectionRequest = null;
        // seSelectionRequest = new SeRequest(this);
        return seSelectionRequest;
    }

    public SeSelector getSeSelector() {
        return seSelector;
    }

    /**
     * /** The matchingClass is the MatchingSe class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of MatchingSe is to be instantiated.
     *
     * This method must be called in the classes that extend SeSelector in order to specify the
     * expected class derived from MatchingSe in return to the selection process.
     *
     * @param matchingClass the expected class for this SeSelector
     */
    protected final void setMatchingClass(Class<? extends MatchingSe> matchingClass) {
        this.matchingClass = matchingClass;
    }

    /**
     * The selectionClass is the SeSelector class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of SeSelector is to be used as argument to
     * the matchingClass constructor.
     *
     * This method must be called in the classes that extend SeSelector in order to specify the
     * expected class derived from SeSelector used as an argument to derived form of MatchingSe.
     *
     * @param selectionClass the argument for the constructor of the matchingClass
     */
    protected final void setSelectionClass(Class<? extends SeSelectionRequest> selectionClass) {
        this.selectionClass = selectionClass;
    }

    /**
     * The default value for the selectionClass (unless setSelectionClass is used) is
     * SeSelector.class
     * 
     * @return the current selectionClass
     */
    protected final Class<? extends SeSelectionRequest> getSelectionClass() {
        return selectionClass;
    }

    /**
     * The default value for the matchingClass (unless setMatchingClass is used) is MatchingSe.class
     *
     * @return the current matchingClass
     */
    protected final Class<? extends MatchingSe> getMatchingClass() {
        return matchingClass;
    }

    @Override
    public String toString() {
        return "SeSelectionRequest";
    }
}
