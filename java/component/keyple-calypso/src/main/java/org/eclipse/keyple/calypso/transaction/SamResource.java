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
package org.eclipse.keyple.calypso.transaction;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.AUTO;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.transaction.sam.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.sam.SamIdentifier;
import org.eclipse.keyple.calypso.transaction.sam.SamSelectionRequest;
import org.eclipse.keyple.calypso.transaction.sam.SamSelector;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * The SamResource class provides an access to a SeReader in which a SAM is running. The SamResource
 * instances are produced by the {@link SamResourceManager}.
 * <p>
 * The main method to use the SAM resource is to send
 * {@link org.eclipse.keyple.seproxy.message.ApduRequest} and receive
 * {@link org.eclipse.keyple.seproxy.message.ApduResponse} to the SAM.
 * <p>
 * The SAM may be a physical chip inserted in a card reader or a virtual SAM provided by a HSM.
 * <p>
 * The SAM is selected at construction time and its characteristics are made available through a
 * CalypsoSam object member.
 * <p>
 * The SamResource integrates a status flag indicating whether the SAM is currently in use for a
 * transaction or not.
 * <p>
 * The {@link SamResourceManager} provides methods to get an available SamResource matching the
 * criteria defined by the application level and to free it after usage.
 */
public class SamResource {
    /** the free/busy enum status */
    public enum SamResourceStatus {
        FREE, BUSY;
    }

    /** the SeReader attached to the SamResource */
    private SeReader seReader;
    /** the CalypsoSam instantiated at construction time containing the SAM work data */
    private CalypsoSam calypsoSam;
    /** the reference of the group to which the SAM belongs (HSM) */
    private String groupReference;
    /** the free/busy status of the resource */
    private SamResourceStatus samResourceStatus;

    /**
     * Direct exchange of APDU with the SAM
     * 
     * @param seRequest a list of APDU to be sent to the SAM
     * @return the SeResponse to the request (list of APDU responses)
     * @throws KeypleReaderException if a reader error occurs
     */
    SeResponse transmit(SeRequest seRequest) throws KeypleReaderException {
        return ((ProxyReader) seReader).transmit(seRequest);
    }

    /**
     * Construction of a SamResource
     * 
     * @param seReader the SeReader with which one communicates with the SAM.
     * @param groupReference the target reference group to which the SAM should belong. May be null
     *        or empty if no group selection is required.
     * @throws KeypleReaderException if a reader error occurs
     * @throws IllegalStateException if the SAM selection failed.
     */
    public SamResource(SeReader seReader, String groupReference)
            throws KeypleReaderException, IllegalStateException {
        this.seReader = seReader;
        this.groupReference = groupReference;
        samResourceStatus = SamResourceStatus.FREE;
        SeSelection samSelection = new SeSelection(seReader);

        SamSelector samSelector = new SamSelector(new SamIdentifier(AUTO, null, null), "SAM");

        /* Prepare selector, ignore MatchingSe here */
        calypsoSam = (CalypsoSam) samSelection.prepareSelection(
                new SamSelectionRequest(samSelector, ChannelState.KEEP_OPEN, Protocol.ANY));

        if (!samSelection.processExplicitSelection()) {
            throw new IllegalStateException("Unable to open a logical channel for SAM!");
        }
    }

    /**
     * @return the SAM reader
     */
    public SeReader getSeReader() {
        return seReader;
    }

    /**
     * @return the CalypsoSam
     */
    public CalypsoSam getCalypsoSam() {
        return calypsoSam;
    }

    /**
     * @return the group reference
     */
    public String getGroupReference() {
        return groupReference;
    }

    /**
     * Indicates whether the SamResource is FREE or BUSY
     * 
     * @return the busy status
     */
    public boolean isSamResourceFree() {
        return samResourceStatus.equals(SamResourceStatus.FREE);
    }

    /**
     * Indicates whether the SamResource matches the provided SAM identifier.
     * <p>
     * The test includes the {@link org.eclipse.keyple.calypso.command.sam.SamRevision}, serial
     * number and group reference provided by the {@link SamIdentifier}.
     * <p>
     * The SAM serial number can be null or empty, in this case all serial numbers are accepted. It
     * can also be a regular expression target one or more specific serial numbers.
     * <p>
     * The groupe reference can be null or empty to let all group references match but not empty the
     * group reference must match the {@link SamIdentifier} to have the method returning true.
     * 
     * @param samIdentifier
     * @return true or false according to the result of the correspondence test
     */
    public boolean isSamMatching(SamIdentifier samIdentifier) {
        if (samIdentifier.getSamRevision() != AUTO
                && samIdentifier.getSamRevision() != calypsoSam.getSamRevision()) {
            return false;
        }
        if (samIdentifier.getSerialNumber() != null && !samIdentifier.getSerialNumber().isEmpty()) {
            Pattern p = Pattern.compile(samIdentifier.getSerialNumber());
            if (!p.matcher(ByteArrayUtils.toHex(calypsoSam.getSerialNumber())).matches()) {
                return false;
            }
        }
        if (samIdentifier.getGroupReference() != null
                && !samIdentifier.getGroupReference().equals(groupReference)) {
            return false;
        }
        return true;
    }

    /**
     * Sets the free/busy status of the SamResource
     * 
     * @param samResourceStatus FREE/BUSY enum value
     */
    void setSamResourceStatus(SamResourceStatus samResourceStatus) {
        this.samResourceStatus = samResourceStatus;
    }
}
