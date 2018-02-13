/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import org.keyple.calypso.commands.dto.StartupInformation;
import org.keyple.calypso.commands.utils.ResponseUtils;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * data from response of a Get Data response.
 *
 * @author Ixxi
 */
public class GetDataFciRespPars extends ApduResponseParser {

    /** The fci. */
    private FCI fci;

    /**
     * Instantiates a new PoFciRespPars.
     *
     * @param response the response from Get Data APDU commmand
     */
    public GetDataFciRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            fci = ResponseUtils.toFCI(response.getbytes());
        }
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x6A, (byte) 0x88}, new StatusProperties(false,
                "Data object not found (optional mode not available)."));
        statusTable.put(new byte[] {(byte) 0x6B, (byte) 0x00}, new StatusProperties(false,
                "P1 or P2 value not supported (<>004fh, 0062h, 006Fh, 00C0h, 00D0h, 0185h and 5F52h, according to availabl optional modes)."));
        statusTable.put(new byte[] {(byte) 0x62, (byte) 0x83}, new StatusProperties(true,
                "Successful execution, FCI request and DF is invalidated."));
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Successful execution."));
    }

    public byte[] getDfName() {
        if (fci != null) {
            return fci.getDfName();
        }
        return null;
    }

    public byte[] getApplicationSerialNumber() {
        if (fci != null) {
            return fci.getApplicationSN();
        }
        return null;
    }

    public byte getBufferSizeByte() {
        if (fci != null) {
            return fci.getStartupInformation().getBufferSize();
        }
        return 0x00;
    }

    public int getBufferSizeValue() {
        if (fci != null) {
            return (int) fci.getStartupInformation().getBufferSize();
        }
        return 0;
    }

    public byte getPlatformByte() {
        if (fci != null) {
            return fci.getStartupInformation().getPlatform();
        }
        return 0x00;
    }

    public byte getApplicationTypeByte() {
        if (fci != null) {
            return fci.getStartupInformation().getApplicationType();
        }
        return 0x00;
    }

    public boolean isRev3Compliant() {
        if (fci != null) {
            return true;
        }
        return false;
    }

    public boolean isRev3_2ModeAvailable() {
        if (fci != null) {
            return fci.getStartupInformation().hasCalypsoRev32modeAvailable();
        }
        return false;
    }

    public boolean isRatificationCommandRequired() {
        if (fci != null) {
            return fci.getStartupInformation().hasRatificationCommandRequired();
        }
        return false;
    }

    public boolean hasCalypsoStoredValue() {
        if (fci != null) {
            return fci.getStartupInformation().hasCalypsoStoreValue();
        }
        return false;
    }

    public boolean hasCalypsoPin() {
        if (fci != null) {
            return fci.getStartupInformation().hasCalypsoPin();
        }
        return false;
    }

    public byte getApplicationSubtypeByte() {
        if (fci != null) {
            return fci.getStartupInformation().getApplicationSubtype();
        }
        return 0x00;
    }

    public byte getSoftwareIssuerByte() {
        if (fci != null) {
            return fci.getStartupInformation().getSoftwareIssuer();
        }
        return 0x00;
    }

    public byte getSoftwareVersionByte() {
        if (fci != null) {
            return fci.getStartupInformation().getSoftwareVersion();
        }
        return 0x00;
    }

    public byte getSoftwareRevisionByte() {
        if (fci != null) {
            return fci.getStartupInformation().getSoftwareRevision();
        }
        return 0x00;
    }

    public boolean isDfInvalidated() {
        if (fci != null) {
            return true;
        }
        return false;
    }

    /**
     * The Class FCI. FCI: file control information
     */
    public static class FCI {

        /** The DF Name. */
        private byte[] dfName;

        /** The fci proprietary template. */
        private byte[] fciProprietaryTemplate;

        /** The fci issuer discretionary data. */
        private byte[] fciIssuerDiscretionaryData;

        /** The application SN. */
        private byte[] applicationSN;

        /** The startup information. */
        private StartupInformation startupInformation;

        /**
         * Instantiates a new FCI.
         *
         * @param dfName the df name
         * @param fciProprietaryTemplate the fci proprietary template
         * @param fciIssuerDiscretionaryData the fci issuer discretionary data
         * @param applicationSN the application SN
         * @param startupInformation the startup information
         */
        public FCI(byte[] dfName, byte[] fciProprietaryTemplate, byte[] fciIssuerDiscretionaryData,
                byte[] applicationSN, StartupInformation startupInformation) {
            if (dfName != null) {
                this.dfName = dfName.clone();
            }

            this.fciProprietaryTemplate =
                    (fciProprietaryTemplate == null ? null : fciProprietaryTemplate.clone());
            this.fciIssuerDiscretionaryData =
                    (fciIssuerDiscretionaryData == null ? null : fciIssuerDiscretionaryData.clone());
            this.applicationSN = (applicationSN == null ? null : applicationSN.clone());
            this.startupInformation = startupInformation;
        }

        /**
         * Gets the fci proprietary template.
         *
         * @return the fci proprietary template
         */
        public byte[] getFciProprietaryTemplate() {
            return (this.fciProprietaryTemplate == null ? null : this.fciProprietaryTemplate.clone());
        }

        /**
         * Gets the fci issuer discretionary data.
         *
         * @return the fci issuer discretionary data
         */
        public byte[] getFciIssuerDiscretionaryData() {
            return (this.fciIssuerDiscretionaryData == null ? null
                    : this.fciIssuerDiscretionaryData.clone());
        }

        /**
         * Gets the application SN.
         *
         * @return the application SN
         */
        public byte[] getApplicationSN() {
            return (this.applicationSN == null ? null : this.applicationSN.clone());
        }

        /**
         * Gets the startup information.
         *
         * @return the startup information
         */
        public StartupInformation getStartupInformation() {
            return this.startupInformation;
        }

        /**
         * Gets the DF Name.
         *
         * @return the DF name
         */
        public byte[] getDfName() {
            if (dfName != null) {
                return dfName.clone();
            } else {
                return new byte[0];
            }
        }

    }
}
