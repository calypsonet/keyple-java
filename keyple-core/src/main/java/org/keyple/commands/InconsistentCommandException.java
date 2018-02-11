/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.commands;

public class InconsistentCommandException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -5941414549312296142L;

    public InconsistentCommandException() {
        super();
    }

    public InconsistentCommandException(String message) {
        super(message);
    }

}
