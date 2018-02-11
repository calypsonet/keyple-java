/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package keyple.commands.csm.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.csm.CsmRevision;
import org.keyple.calypso.commands.csm.builder.DigestCloseCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class DigestCloseCmdBuildTest {

    @Test
    public void digestCloseCmdBuild() throws InconsistentCommandException {

        byte[] request = {(byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04};
        ApduCommandBuilder apduCommandBuilder =
                new DigestCloseCmdBuild(CsmRevision.S1D, (byte) 0x04);// 94
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

        byte[] request1 = {(byte) 0x80, (byte) 0x8E, 0x00, 0x00, (byte) 0x04};
        apduCommandBuilder = new DigestCloseCmdBuild(CsmRevision.C1, (byte) 0x04);// 94
        ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request1, ApduRequest.getbytes());

    }
}
