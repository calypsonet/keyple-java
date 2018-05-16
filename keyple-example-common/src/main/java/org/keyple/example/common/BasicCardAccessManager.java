/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.example.common;

import java.util.ArrayList;
import java.util.List;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;

public class BasicCardAccessManager extends AbstractLogicManager {

    private ProxyReader poReader;

    public void setPoReader(ProxyReader poReader) {
        this.poReader = poReader;
    }

    @Override
    public void run() {
        super.run();

        // Hoplink simple read requests builder object
        HoplinkSimpleRead hoplinkSimpleRead = new HoplinkSimpleRead();

        // Create request elements list
        List<SeRequestElement> poRequestElements = new ArrayList<SeRequestElement>();

        // Add Aid and apdu requests from Hoplink builder
        poRequestElements.add(new SeRequestElement(hoplinkSimpleRead.getPoAid(),
                hoplinkSimpleRead.getPoApduRequestList(), false));

        // Create SeRequest
        SeRequest poRequest = new SeRequest(poRequestElements);

        // Execute and get result
        try {
            SeResponse poResponse = poReader.transmit(poRequest);
            SeResponseElement poResponseElement = poResponse.getElements().get(0);
            getTopic().post(new Event("Got a response", "poResponse",
                    poResponseElement.getApduResponses()));
        } catch (IOReaderException e) {
            e.printStackTrace();
            getTopic().post(new Event("Got an error", "error", e.getMessage()));
        }
    }
}
