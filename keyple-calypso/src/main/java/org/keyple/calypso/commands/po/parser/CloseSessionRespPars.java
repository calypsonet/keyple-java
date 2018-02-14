/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import org.keyple.calypso.commands.utils.ResponseUtils;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * Close Secure Session (008E) response parser. See specs: Calypso / page 104 / 9.5.2 Close Secure
 * Session
 */
public class CloseSessionRespPars extends ApduResponseParser {

    /** The po half session signature. */
    private PoHalfSessionSignature poHalfSessionSignature;

    /**
     * Instantiates a new CloseSessionRespPars from the response.
     *
     * @param response from CloseSessionCmdBuild
     */
    public CloseSessionRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        poHalfSessionSignature = toPoHalfSessionSignature(response.getbytes());
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x67, (byte) 0x00}, new StatusProperties(false,
                "Lc value not supported (e.g. Lc=4 with a Revision 3.2 mode for Open Secure Session)."));
        statusTable.put(new byte[] {(byte) 0x6B, (byte) 0x00},
                new StatusProperties(false, "P1 or P2 value not supported."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x88},
                new StatusProperties(false, "incorrect signature."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x85},
                new StatusProperties(false, "No session was opened."));
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Successful execution."));
    }

    public boolean hasPostponedData() {
        return poHalfSessionSignature.getPostponedData().length != 0;
    }

    public byte[] getPostponedData() {
        return poHalfSessionSignature.getPostponedData();
    }

    public byte[] getSignatureLo() {
        return poHalfSessionSignature.getValue();
    }

    /**
     * Method to get the PO half session signature (the second half part of the signature necessary
     * to close the session properly) from the response.
     *
     * @param response the response
     * @return a PoHalfSessionSignature
     */
    public static PoHalfSessionSignature toPoHalfSessionSignature(byte[] response) {
        byte[] poHalfSessionSignatureTable = null;
        byte[] postponedData = null;

        // fclairamb(2018-02-14): Removed 2 bytes to the global response length;
        final int size = response.length - 2;

        if (size == 8) {
            poHalfSessionSignatureTable = ResponseUtils.subArray(response, 4, size);
            postponedData = ResponseUtils.subArray(response, 0, 4);
        } else if (size == 4) {
            poHalfSessionSignatureTable = ResponseUtils.subArray(response, 0, size);
        }

        return new PoHalfSessionSignature(poHalfSessionSignatureTable, postponedData);
    }

    /**
     * The Class PoHalfSessionSignature. Half session signature return by a close secure session
     * APDU command
     */
    public static class PoHalfSessionSignature {

        /** The value. */
        private byte[] value;

        /** The postponed data. */
        private byte[] postponedData;

        /**
         * Instantiates a new PoHalfSessionSignature.
         *
         * @param value the value
         * @param postponedData the postponed data
         */
        public PoHalfSessionSignature(byte[] value, byte[] postponedData) {
            super();
            this.value = (value == null) ? null : value.clone();
            this.postponedData = (postponedData == null ? null : postponedData.clone());
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public byte[] getValue() {
            if (value != null) {
                return value.clone();
            } else {
                return new byte[0];
            }
        }

        /**
         * Gets the postponed data.
         *
         * @return the postponed data
         */
        byte[] getPostponedData() {
            if (postponedData != null) {
                return postponedData.clone();
            } else {
                return new byte[0];
            }
        }

    }
}
