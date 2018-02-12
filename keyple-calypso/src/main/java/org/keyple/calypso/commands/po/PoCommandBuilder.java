/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
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
