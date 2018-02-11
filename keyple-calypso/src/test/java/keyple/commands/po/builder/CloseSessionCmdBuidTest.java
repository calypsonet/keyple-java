/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package keyple.commands.po.builder;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.CloseSessionCmdBuild;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

public class CloseSessionCmdBuidTest {

    Logger logger = LogManager.getLogger(CloseSessionCmdBuidTest.class);

    @Test
    public void closeSessionCmdBuild() throws InconsistentCommandException {
        byte[] request2_4 = {(byte) 0x94, (byte) 0x8E, 0x00, 0x00, (byte) 0x04, (byte) 0xA8, 0x31,
                (byte) 0xC3, 0x3E};
        byte[] request3_1 = {(byte) 0x00, (byte) 0x8E, (byte) 0x80, 0x00, (byte) 0x04, (byte) 0xA8,
                0x31, (byte) 0xC3, 0x3E};
        byte[] terminalSessionSiganture = {(byte) 0xA8, 0x31, (byte) 0xC3, 0x3E};
        ApduCommandBuilder apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV2_4, false, terminalSessionSiganture);
        ApduRequest ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request2_4, ApduRequest.getbytes());

        apduCommandBuilder =
                new CloseSessionCmdBuild(PoRevision.REV3_1, true, terminalSessionSiganture);
        ApduRequest = apduCommandBuilder.getApduRequest();

        Assert.assertArrayEquals(request3_1, ApduRequest.getbytes());
    }
}
