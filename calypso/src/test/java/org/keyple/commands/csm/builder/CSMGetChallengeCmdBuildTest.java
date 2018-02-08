package org.keyple.commands.csm.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.commands.csm.CsmRevision;
import org.keyple.commands.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.seproxy.ApduRequest;

public class CSMGetChallengeCmdBuildTest {

    @Test
    public void getChallengeCmdBuild() throws InconsistentCommandException {

        byte[] request = { (byte) 0x94, (byte) 0x84, 0x00, 0x00, 0x04 };

        ApduCommandBuilder apduCommandBuilder = new CsmGetChallengeCmdBuild(CsmRevision.S1D, (byte) 0x04);// 94
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

    }
}
