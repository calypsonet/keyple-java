/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package keyple.commands.po.builder;

import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.PoGetChallengeCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.seproxy.ApduRequest;

public class POGetChallengeCmdBuildTest {

    @Test
    public void POGetChallenge_Rev2_4() {

        byte[] request = {(byte) 0x94, (byte) 0x84, 0x01, 0x10, 0x08};

        ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV2_4);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

    }

    @Test
    public void POGetChallenge_Rev3_1() {

        byte[] request = {(byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08};

        ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV3_1);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

    }

    @Test
    public void POGetChallenge_Rev3_2() {

        byte[] request = {(byte) 0x00, (byte) 0x84, 0x01, 0x10, 0x08};

        ApduCommandBuilder apduCommandBuilder = new PoGetChallengeCmdBuild(PoRevision.REV3_2);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request, ApduRequest.getbytes());

    }


}
