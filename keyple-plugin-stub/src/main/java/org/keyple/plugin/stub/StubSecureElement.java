/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.stub;


import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;

/**
 * Inject custom behaviour in Stub Reader by implementing {@link #process(ApduRequest request)}
 */
public abstract class StubSecureElement {

    /**
     * Retrieve Technology of Secure Element
     * 
     * @param : String representation of SE Technology
     */
    abstract public String getTech();

    /**
     * Implement this function to simulate responses to requests
     * 
     * @param request :
     * @return simulated response
     */
    abstract public ApduResponse process(ApduRequest request);

    /**
     * Simulate INSERT SE event from {@link org.keyple.seproxy.ProxyReader}
     * 
     * @param poReader
     */
    public void insertInto(StubReader poReader) {
        poReader.connect(this);
    };

    /**
     * Simulate REMOVE SE event from {@link org.keyple.seproxy.ProxyReader}
     * 
     * @param poReader
     */
    public void removeFrom(StubReader poReader) {
        poReader.disconnect(this);
    };



}
