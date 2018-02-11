/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.seproxy;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Test;

public class SeRequestTest {

    @Test
    public void testSERequest() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertNotNull(request);
    }

    @Test
    public void testGetAidToSelect() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertArrayEquals(new byte[] {(byte) 0x01, (byte) 0x02}, request.getAidToSelect());
    }

    @Test
    public void testGetApduRequests() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertArrayEquals(new ArrayList<ApduRequest>().toArray(),
                request.getApduRequests().toArray());
    }

    @Test
    public void testAskKeepChannelOpen() {
        SeRequest request = new SeRequest(new byte[] {(byte) 0x01, (byte) 0x02},
                new ArrayList<ApduRequest>(), true);
        assertTrue(request.askKeepChannelOpen());
    }

}
