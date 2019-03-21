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
package org.eclipse.keyple.calypso.transaction.sam;

import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Holds the needed data to proceed a SAM selection.
 * <p>
 * SAM Revision (see {@link SamRevision})
 * <p>
 * Serial Number (may be a regular expression)
 * <p>
 * Group reference (key group reference)
 */
public class SamIdentifier {
    SamRevision samRevision;
    String serialNumber;
    String groupReference;

    public SamIdentifier(SamRevision samRevision, String serialNumber, String groupReference) {
        this.samRevision = samRevision;
        this.serialNumber = serialNumber;
        this.groupReference = groupReference;
    }

    public SamRevision getSamRevision() {
        return samRevision;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getGroupReference() {
        return groupReference;
    }
}
