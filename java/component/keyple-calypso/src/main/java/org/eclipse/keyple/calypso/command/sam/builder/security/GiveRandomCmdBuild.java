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
package org.eclipse.keyple.calypso.command.sam.builder.security;

import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommands;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Builder for the SAM Give Random APDU command.
 */
public class GiveRandomCmdBuild extends AbstractSamCommandBuilder {

    /** The command reference. */
    private static final CalypsoSamCommands command = CalypsoSamCommands.GIVE_RANDOM;

    /**
     * Instantiates a new DigestUpdateCmdBuild.
     *
     * @param revision of the SAM
     * @param random the random data
     * @throws java.lang.IllegalArgumentException - if the random data is null or has a length not
     *         equal to 8
     *
     *         TODO implement specific settings for rev < 3
     */
    public GiveRandomCmdBuild(SamRevision revision, byte[] random) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = this.defaultRevision.getClassByte();
        byte p1 = (byte) 0x00;
        byte p2 = (byte) 0x00;

        if (random != null && random.length != 8) {
            throw new IllegalArgumentException("Random value should be an 8 bytes long");
        }

        request = setApduRequest(cla, command, p1, p2, random, null);
    }
}
