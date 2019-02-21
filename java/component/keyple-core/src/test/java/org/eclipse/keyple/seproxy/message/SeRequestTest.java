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
package org.eclipse.keyple.seproxy.message;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class SeRequestTest {

    // object to test
    SeRequest seRequest;

    public List<ApduRequest> getApdus() {
        return apdus;
    }

    // attributes
    List<ApduRequest> apdus;
    ChannelState channelState;
    SeProtocol seProtocol;
    Set<Integer> selectionStatusCode;
    SeRequest.Selector selector;



    @Before
    public void setUp() {

        apdus = getAapduLists();
        channelState = ChannelState.KEEP_OPEN;
        seProtocol = getASeProtocol();
        selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
        selector = getAidSelector(selectionStatusCode);
        seRequest =
                new SeRequest(getAidSelector(selectionStatusCode), apdus, channelState, seProtocol);
    }

    @Test
    public void testSERequest() {
        assertNotNull(seRequest);
    }


    @Test
    public void getSelector() {
        // test
        assertEquals(getAidSelector(selectionStatusCode).toString(),
                seRequest.getSelector().toString());

    }

    @Test
    public void getApduRequests() {
        // test
        seRequest =
                new SeRequest(getAidSelector(null), apdus, ChannelState.CLOSE_AFTER, Protocol.ANY);
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
    }

    @Test
    public void isKeepChannelOpen() {
        assertTrue(seRequest.isKeepChannelOpen());
    }

    @Test
    public void getProtocolFlag() {
        seRequest = new SeRequest(getAidSelector(null), new ArrayList<ApduRequest>(),
                ChannelState.KEEP_OPEN, seProtocol);
        assertEquals(seProtocol, seRequest.getProtocolFlag());
    }

    @Test
    public void getSuccessfulSelectionStatusCodes() {
        seRequest = new SeRequest(getAidSelector(selectionStatusCode), new ArrayList<ApduRequest>(),
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_B_PRIME);
        assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSelector().getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void toStringNull() {
        seRequest = new SeRequest(null, null, ChannelState.KEEP_OPEN, Protocol.ANY);
        assertNotNull(seRequest.toString());
    }

    /*
     * Constructors
     */
    @Test
    public void constructor1() {
        seRequest = new SeRequest(getAidSelector(null), apdus, channelState, Protocol.ANY);
        assertEquals(getAidSelector(null).toString(), seRequest.getSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        //
        assertEquals(Protocol.ANY, seRequest.getProtocolFlag());
        assertNull(seRequest.getSelector().getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2() {
        seRequest = new SeRequest(getAidSelector(null), apdus, channelState, seProtocol);
        assertEquals(getAidSelector(null).toString(), seRequest.getSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getProtocolFlag());
        //
        assertNull(seRequest.getSelector().getSuccessfulSelectionStatusCodes());
    }

    @Test
    public void constructor2b() {
        seRequest = new SeRequest(getAidSelector(selectionStatusCode), apdus, channelState,
                Protocol.ANY);
        assertEquals(getAidSelector(selectionStatusCode).toString(),
                seRequest.getSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(Protocol.ANY, seRequest.getProtocolFlag());
        //
        assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSelector().getSuccessfulSelectionStatusCodes().toArray());
    }

    @Test
    public void constructor3() {
        seRequest =
                new SeRequest(getAidSelector(selectionStatusCode), apdus, channelState, seProtocol);
        assertEquals(getAidSelector(selectionStatusCode).toString(),
                seRequest.getSelector().toString());
        assertEquals(channelState == ChannelState.KEEP_OPEN, seRequest.isKeepChannelOpen());
        assertArrayEquals(apdus.toArray(), seRequest.getApduRequests().toArray());
        assertEquals(seProtocol, seRequest.getProtocolFlag());
        assertArrayEquals(selectionStatusCode.toArray(),
                seRequest.getSelector().getSuccessfulSelectionStatusCodes().toArray());
    }


    /*
     * HELPERS FOR OTHERS TESTS SUITE
     */

    static SeRequest getSeRequestSample() {

        List<ApduRequest> apdus = getAapduLists();
        ChannelState channelState = ChannelState.KEEP_OPEN;
        SeProtocol seProtocol = getASeProtocol();
        Set<Integer> selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();

        return new SeRequest(getAidSelector(selectionStatusCode), apdus, channelState, seProtocol);

    }

    static List<ApduRequest> getAapduLists() {
        List<ApduRequest> apdus;
        apdus = new ArrayList<ApduRequest>();
        apdus.add(ApduRequestTest.getApduSample());
        apdus.add(ApduRequestTest.getApduSample());
        return apdus;
    }

    static SeProtocol getASeProtocol() {
        return ContactlessProtocols.PROTOCOL_B_PRIME;
    }

    static SeRequest.Selector getAidSelector(Set<Integer> selectionStatusCode) {
        return new SeRequest.AidSelector(ByteArrayUtils.fromHex("A000000291A000000191"),
                selectionStatusCode);
    }

}