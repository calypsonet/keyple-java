/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.sam.builder.session;

import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommands;
import org.eclipse.keyple.calypso.command.sam.SamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Builder for the SAM Give Random APDU command.
 */
public class CardGenerateKeyCmdBuild extends SamCommandBuilder {
    /** The command reference. */
    private static final CalypsoSamCommands command = CalypsoSamCommands.CARD_GENERATE_KEY;

    /**
     * Instantiates a new DigestUpdateCmdBuild and generate the ciphered data for a key ciphered by
     * another.
     * 
     * @param revision of the SAM
     * @param cipherKeyKif
     * @param cipherKeyKvc
     * @param sourceKeyKif
     * @param sourceKeyKvc
     *
     *        TODO implement specific settings for rev < 3
     */
    public CardGenerateKeyCmdBuild(SamRevision revision, byte cipherKeyKif, byte cipherKeyKvc,
            byte sourceKeyKif, byte sourceKeyKvc) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = this.defaultRevision.getClassByte();
        byte p1 = (byte) 0xFF;
        byte p2 = (byte) 0xFF;

        byte[] data = new byte[5];
        data[0] = cipherKeyKif;
        data[1] = cipherKeyKvc;
        data[2] = sourceKeyKif;
        data[3] = sourceKeyKvc;
        data[4] = (byte) 0x90;

        request = setApduRequest(cla, command, p1, p2, data, null);
    }

    /**
     * Instantiates a new DigestUpdateCmdBuild and generate the ciphered data for a key ciphered by
     * the null key.
     * 
     * @param revision of the SAM
     * @param sourceKeyKif
     * @param sourceKeyKvc
     *
     *        TODO implement specific settings for rev < 3
     */
    public CardGenerateKeyCmdBuild(SamRevision revision, byte sourceKeyKif, byte sourceKeyKvc) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = this.defaultRevision.getClassByte();
        byte p1 = (byte) 0xFF;
        byte p2 = (byte) 0x00;

        byte[] data = new byte[3];
        data[0] = sourceKeyKif;
        data[1] = sourceKeyKvc;
        data[2] = (byte) 0x90;

        request = setApduRequest(cla, command, p1, p2, data, null);
    }
}
