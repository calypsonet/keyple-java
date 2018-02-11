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
import org.keyple.calypso.commands.po.SendableInSession;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class UpdateRecordCmdBuild. This class provides the dedicated constructor to build the Update
 * Record APDU command.
 *
 * @author Ixxi
 *
 */
public class UpdateRecordCmdBuild extends PoCommandBuilder implements SendableInSession {

    /** The command. */
    private static CalypsoCommands command = CalypsoCommands.PO_UPDATE_RECORD;

    /**
     * Instantiates a new UpdateRecordCmdBuild.
     *
     * @param revision the revision of the PO
     * @param recordNumber the record number to update
     * @param sfi the sfi to select
     * @param newRecordData the new record data to write
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public UpdateRecordCmdBuild(PoRevision revision, byte recordNumber, byte sfi,
            byte[] newRecordData) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (recordNumber < 1) {
            throw new InconsistentCommandException();
        }
        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = recordNumber;
        byte p2 = (sfi == 0) ? (byte) 0x04 : (byte) ((byte) (sfi * 8) + 4);
        byte[] dataIn = newRecordData;
        CalypsoRequest request = new CalypsoRequest(cla, command, p1, p2, dataIn);
        ApduRequest apduRequest = RequestUtils.constructAPDURequest(request);

        this.request = apduRequest;

    }

    /**
     * Instantiates a new update record cmd build.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public UpdateRecordCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}
