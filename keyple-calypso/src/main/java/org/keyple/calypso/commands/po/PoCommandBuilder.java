/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.calypso.commands.po;

import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.seproxy.ApduRequest;

/**
 * This abstract class extends ApduCommandBuilder, it has to be extended by all PO command builder
 * classes, it manages the current default revision for PO commands.
 *
 * @author Ixxi
 *
 */
public abstract class PoCommandBuilder extends ApduCommandBuilder {

    protected PoRevision defaultRevision = PoRevision.REV3_1;

    public PoCommandBuilder(CalypsoCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
