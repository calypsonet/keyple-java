/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class KIF. KIF:Key Identifier. Value identifying the type of key.
 *
 * @deprecated Replace it by a byte directly
 */
public class KIF {

    /** The value. */
    private byte value;

    /**
     * Instantiates a new KIF.
     *
     * @param value the value
     */
    public KIF(byte value) {
        super();
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte getValue() {
        return value;
    }
}
