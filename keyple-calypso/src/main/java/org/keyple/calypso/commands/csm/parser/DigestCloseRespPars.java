/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.calypso.commands.csm.parser;

import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * a Digest Close response.
 *
 * @author Ixxi
 *
 */
public class DigestCloseRespPars extends ApduResponseParser {

    /** The SAM signture */
    private byte[] signature;

    /**
     * Instantiates a new DigestCloseRespPars.
     *
     * @param response from the DigestCloseCmdBuild
     */
    public DigestCloseRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            signature = response.getbytes();
        }
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Successful execution."));
    }

    /**
     * Gets the sam signature.
     *
     * @return the sam half session signature
     */
    public byte[] getSignature() {
        if (signature != null) {
            return signature.clone();
        } else {
            return new byte[0];
        }
    }

}
