/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.calypso.commands.po.builder;

import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.calypso.commands.dto.CalypsoRequest;
import org.keyple.calypso.commands.po.PoCommandBuilder;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * This class provides the dedicated constructor to build the Close Secure Session APDU command.
 *
 * @author Ixxi
 */
public class CloseSessionCmdBuild extends PoCommandBuilder {

    /** The command. */
    private static CalypsoCommands command = CalypsoCommands.PO_CLOSE_SESSION;

    /**
     * Instantiates a new CloseSessionCmdBuild depending of the revision of the PO.
     *
     * @param revision of the PO
     * @param ratificationAsked the ratification asked
     * @param terminalSessionSignature the sam half session signature
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CloseSessionCmdBuild(PoRevision revision, boolean ratificationAsked,
            byte[] terminalSessionSignature) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        // The optional parameter terminalSessionSignature could contain 4 or 8
        // bytes.
        if (terminalSessionSignature != null) {
            if (terminalSessionSignature.length != 4 && terminalSessionSignature.length != 8) {
                throw new InconsistentCommandException();
            }
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = ratificationAsked ? (byte) 0x80 : (byte) 0x00;
        CalypsoRequest calypsoRequest =
                new CalypsoRequest(cla, command, p1, (byte) 0x00, terminalSessionSignature);
        request = RequestUtils.constructAPDURequest(calypsoRequest, 0);
    }

    /**
     * Instantiates a new close session cmd build.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public CloseSessionCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}
