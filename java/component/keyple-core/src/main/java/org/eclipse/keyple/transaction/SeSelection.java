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

import java.util.*;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.DefaultSelectionRequest;
import org.eclipse.keyple.seproxy.event.SelectionResponse;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelection class handles the SE selection process.
 * <p>
 * It provides a way to do explicit SE selection or to post process a default SE selection.
 */
public final class SeSelection {
    private static final Logger logger = LoggerFactory.getLogger(SeSelection.class);

    /*
     * list of target classes and selection requests used to build the MatchingSe list in return of
     * processSelection methods
     */
    private List<Class> seTargetClassList = new ArrayList<Class>();
    private List<SeSelectionRequest> seSelectionRequestSet = new ArrayList<SeSelectionRequest>();
    private SeRequestSet selectionRequestSet = new SeRequestSet(new LinkedHashSet<SeRequest>());

    /**
     * Initializes the SeSelection
     */
    public SeSelection() {}

    /**
     * Prepare a selection: add the selection request from the provided selector to the selection
     * request set.
     * <p>
     * Create a MatchingSe, retain it in a list and return it. The MatchingSe may be an extended
     * class
     * 
     * @param seSelectionRequest the selector to prepare
     * @param seTargetClass the target SE class (MatchingSe or one of its derived classes)
     * @return true or false according to the execution status. true if success.
     */
    public boolean prepareSelection(SeSelectionRequest seSelectionRequest, Class seTargetClass) {
        if (logger.isTraceEnabled()) {
            logger.trace("SELECTORREQUEST = {}, EXTRAINFO = {}",
                    seSelectionRequest.getSelectionRequest(),
                    seSelectionRequest.getSeSelector().getExtraInfo());
        }
        /* keep request data for further use when building MatchingSe */
        seSelectionRequestSet.add(seSelectionRequest);
        seTargetClassList.add(seTargetClass);
        /* build the SeRequest set transmitted to the SE */
        selectionRequestSet.add(seSelectionRequest.getSelectionRequest());
        return true;
    }

    /**
     * Process the selection response either from a
     * {@link org.eclipse.keyple.seproxy.event.ReaderEvent} (default selection) or from an explicit
     * selection.
     * <p>
     * The responses from the {@link SeResponseSet} is parsed and checked.
     * <p>
     * A {@link MatchingSe} list is build and returned. Non matching SE are signaled by a null
     * element in the list
     * 
     * @param selectionResponse the selection response
     * @return the MatchingSe list, some may be null and void.
     */
    private List<MatchingSe> processSelection(SelectionResponse selectionResponse) {
        List<MatchingSe> matchingSeList = new ArrayList<MatchingSe>();

        /* null pointer exception protection */
        if (selectionResponse == null) {
            logger.error("selectionResponse shouldn't be null in processSelection.");
            return null;
        }
        int index = 0;
        Iterator<SeSelectionRequest> requestIterator = seSelectionRequestSet.iterator();

        /* Check SeResponses */
        for (SeResponse seResponse : selectionResponse.getSelectionSeResponseSet().getResponses()) {
            if (seResponse != null) {
                /* test if the selection is successful: we should have either a FCI or an ATR */
                if (seResponse.getSelectionStatus() != null) {
                    SeSelectionRequest seSelectionRequest = requestIterator.next();
                    MatchingSe matchingSe = new MatchingSe(seSelectionRequest);
                    matchingSeList.add((MatchingSe) seTargetClassList.get(index).cast(matchingSe));
                    matchingSe.setSelectionResponse(seResponse);
                } else {
                    matchingSeList.add(null);
                    /* skip request */
                    if (requestIterator.hasNext()) {
                        requestIterator.next();
                    }
                }
            } else {
                matchingSeList.add(null);
                /* skip request */
                if (requestIterator.hasNext()) {
                    requestIterator.next();
                }
            }
            index++;
        }
        return matchingSeList;
    }

    /**
     * Parses the response to a selection operation sent to a SE and return a list of
     * {@link MatchingSe}
     * <p>
     * Selection cases that have not matched the current SE are set to null.
     *
     * @param selectionResponse the response from the reader to the {@link DefaultSelectionRequest}
     * @return the MatchingSe list, some may be null and void.
     */
    public List<MatchingSe> processDefaultSelection(SelectionResponse selectionResponse) {
        if (logger.isTraceEnabled()) {
            logger.trace("Process default SELECTIONRESPONSE ({} response(s))",
                    selectionResponse.getSelectionSeResponseSet().getResponses().size());
        }

        return processSelection(selectionResponse);
    }

    /**
     * Execute the selection process and return a list of {@link MatchingSe}.
     * <p>
     * Selection requests are transmitted to the SE through the supplied SeReader.
     * <p>
     * The process stops in the following cases:
     * <ul>
     * <li>All the selection requests have been transmitted</li>
     * <li>A selection request matches the current SE and the keepChannelOpen flag was true</li>
     * </ul>
     * <p>
     *
     * @param seReader the SeReader on which the selection is made
     * @return the MatchingSe list, some may be null and void.
     * @throws KeypleReaderException if the requests transmission failed
     */
    public List<MatchingSe> processExplicitSelection(SeReader seReader)
            throws KeypleReaderException {
        if (logger.isTraceEnabled()) {
            logger.trace("Transmit SELECTIONREQUEST ({} request(s))",
                    selectionRequestSet.getRequests().size());
        }

        /* Communicate with the SE to do the selection */
        SeResponseSet seResponseSet = ((ProxyReader) seReader).transmitSet(selectionRequestSet);

        return processSelection(new SelectionResponse(seResponseSet));
    }

    /**
     * The SelectionOperation is the DefaultSelectionRequest to process in ordered to select a SE
     * among others through the selection process. This method is useful to build the prepared
     * selection to be executed by a reader just after a SE insertion.
     * 
     * @return the {@link DefaultSelectionRequest} previously prepared with prepareSelection
     */
    public DefaultSelectionRequest getSelectionOperation() {
        return new DefaultSelectionRequest(selectionRequestSet);
    }
}
