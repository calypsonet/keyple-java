/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApduResponseTest {

    @Test
    public void testAPDUResponse() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertNotNull(response);
    }

    @Test
    public void testGetbytes() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertArrayEquals(new byte[] {(byte) 0x01, (byte) 0x02}, response.getbytes());
    }

    @Test
    public void testIsSuccessful() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertTrue(response.isSuccessful());
    }

    @Test
    public void testGetStatusCode() {
        ApduResponse response = new ApduResponse(new byte[] {(byte) 0x01, (byte) 0x02}, true,
                new byte[] {(byte) 0x03, (byte) 0x04});
        assertArrayEquals(new byte[] {(byte) 0x03, (byte) 0x04}, response.getStatusCode());
    }

    @Test
    public void niceFormat() {
        ApduResponse response = new ApduResponse("FEDCBA98 9000h");
        Assert.assertEquals("fedcba989000", Hex.encodeHexString(response.getbytes()));
    }

}
