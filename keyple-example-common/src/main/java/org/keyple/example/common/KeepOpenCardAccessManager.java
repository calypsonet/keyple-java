/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.example.common;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.keyple.seproxy.*;


public class KeepOpenCardAccessManager extends AbstractLogicManager {

    private ProxyReader poReader;

    public void setPoReader(ProxyReader poReader) {
        this.poReader = poReader;
    }

    @Override
    public void run() {
        super.run();

        // Hoplink simple read requests builder object
        HoplinkSimpleRead hoplinkSimpleRead = new HoplinkSimpleRead();

        ByteBuffer poAid = hoplinkSimpleRead.getPoAid();

        List<ApduRequest> poApduRequestList = hoplinkSimpleRead.getPoApduRequestList();


        // Create request elements list
        List<SeRequestElement> poRequestElements = new ArrayList<SeRequestElement>();

        // Add Aid and apdu requests from Hoplink builder
        poRequestElements.add(new SeRequestElement(poAid, poApduRequestList, true));

        // Create SeRequest
        SeRequest poRequest = new SeRequest(poRequestElements);

        try {

            System.out.println("Transmit 1st SE Request, keep channel open");
            SeResponse poResponse = poReader.transmit(poRequest);
            SeResponseElement poResponseElement = poResponse.getElements().get(0);
            getTopic().post(new Event("Got a response", "poResponse",
                    poResponseElement.getApduResponses()));

            System.out.println("Sleeping for 3 seconds");
            Thread.sleep(3000);
            System.out.println("Transmit 2nd SE Request, close channel");

            // Create 2nd request elements list
            List<SeRequestElement> poRequestElements2 = new ArrayList<SeRequestElement>();

            // Add same Aid and apdu requests from Hoplink builder
            poRequestElements2.add(new SeRequestElement(poAid, poApduRequestList, false));

            // Create SeRequest
            SeRequest poRequest2 = new SeRequest(poRequestElements);

            SeResponse poResponse2 = poReader.transmit(poRequest2);
            SeResponseElement poResponseElement2 = poResponse2.getElements().get(0);
            getTopic().post(new Event("Got a 2nd response", "poResponse2",
                    poResponseElement2.getApduResponses()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
