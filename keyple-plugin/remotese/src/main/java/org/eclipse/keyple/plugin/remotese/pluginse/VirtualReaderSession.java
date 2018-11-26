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
package org.eclipse.keyple.plugin.remotese.pluginse;


import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.eclipse.keyple.plugin.remotese.transport.KeypleRemoteReaderException;

public interface VirtualReaderSession {

    /**
     * Retrieve sessionId
     * 
     * @return sessionId
     */
    String getSessionId();


    /**
     * Blocking transmitSet
     *
     * @param nativeReaderName : local reader to transmitSet to
     * @param virtualReaderName : virtual reader that receives the order the transmitSet to
     * @param seRequestSet : seRequestSet to transmit
     * @return SeResponseSet
     */
    SeResponseSet transmitSet(String nativeReaderName, String virtualReaderName,
                              SeRequestSet seRequestSet) throws KeypleRemoteReaderException;


    /**
     * Blocking transmit
     *
     * @param nativeReaderName : local reader to transmitSet to
     * @param virtualReaderName : virtual reader that receives the order the transmitSet to
     * @param seRequest : seApplicationRequest to transmitSet
     * @return SeResponseSet
     */
    SeResponse transmit(String nativeReaderName, String virtualReaderName,
                        SeRequest seRequest) throws KeypleRemoteReaderException;




    /**
     * Send response in callback
     * 
     * @param seResponseSet : receive seResponseSet to be callback
     */
    void asyncSetSeResponseSet(SeResponseSet seResponseSet, KeypleRemoteReaderException e);



    /**
     * Has a seRequestSet in session (being transmitted)
     * 
     * @return true if a seRequestSet is being transmitted
     */
    Boolean hasSeRequestSet();

    /**
     * Get the seRequestSet being transmitted
     * 
     * @return seRequestSet transmitted
     */
    SeRequestSet getSeRequestSet();



}
