/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.calypso.commands.csm;

import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.seproxy.ApduRequest;

/**
 *
 * This abstract class extends ApduCommandBuilder, it has to be extended by all CSM command builder
 * classes, it manages the current default revision for PO commands
 *
 * @author IXXI
 *
 */
public abstract class CsmCommandBuilder extends ApduCommandBuilder {

    protected CsmRevision defaultRevision = CsmRevision.S1D;// 94

    public CsmCommandBuilder(CalypsoCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
