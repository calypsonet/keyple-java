/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
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



import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoCustomModificationCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoCustomReadCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.transaction.SeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized selector to manage the specific characteristics of Calypso POs
 */
public final class PoSelector extends SeSelector {
    private static final Logger logger = LoggerFactory.getLogger(PoSelector.class);

    private final RevisionTarget revisionTarget;
    /** The list to contain the parsers associated to the prepared commands */
    private List<CommandParser> poResponseParserList = new ArrayList<CommandParser>();

    /**
     * Inner class to hold a apduResponseParser and a flag indicating whether this parser is
     * associated with one or two command.
     * <p>
     * The goal of this class and its doubleParser flag is to handle the case where two commands
     * were sent to a Calypso PO (case TARGET_REV2_REV3 of prepareReadRecordsCmd)
     * <p>
     * This flag will be tested when parsing the response to the selection.
     * 
     * @param <AbstractApduResponseParser>
     * @param <Boolean>
     */
    class CommandParser<AbstractApduResponseParser, Boolean> {
        private final AbstractApduResponseParser apduResponseParser;
        private final Boolean doubleParser;

        public CommandParser(AbstractApduResponseParser apduResponseParser, Boolean doubleParser) {
            this.apduResponseParser = apduResponseParser;
            this.doubleParser = doubleParser;
        }

        public AbstractApduResponseParser getApduResponseParser() {
            return apduResponseParser;
        }

        public Boolean getDoubleParser() {
            return doubleParser;
        }
    }

    /**
     * Selection targets definition
     */
    public enum RevisionTarget {
        TARGET_REV1, TARGET_REV2, TARGET_REV3, TARGET_REV2_REV3
    }

    /**
     * Calypso PO revision 1 selector
     * 
     * @param atrRegex a regular expression to compare with the ATR of the targeted Rev1 PO
     * @param keepChannelOpen indicates whether the logical channel should remain open
     * @param protocolFlag the protocol flag to filter POs according to their communication protocol
     * @param extraInfo information string
     */
    public PoSelector(String atrRegex, Short dfLID, boolean keepChannelOpen,
            SeProtocol protocolFlag, RevisionTarget revisionTarget, String extraInfo) {
        super(atrRegex, keepChannelOpen, protocolFlag, extraInfo);
        setMatchingClass(CalypsoPo.class);
        setSelectorClass(PoSelector.class);
        this.revisionTarget = RevisionTarget.TARGET_REV1;
        if (logger.isTraceEnabled()) {
            logger.trace("Calypso {} selector", revisionTarget);
        }
    }

    /**
     * Calypso PO revision 2+ selector
     *
     * @param aid a regular expression to compare with the ATR of the targeted Rev1 PO
     * @param selectNext a flag to indicate if the first or the next occurrence is requested
     * @param keepChannelOpen indicates whether the logical channel should remain open
     * @param protocolFlag the protocol flag to filter POs according to their communication protocol
     * @param extraInfo information string
     */
    public PoSelector(byte[] aid, boolean selectNext, boolean keepChannelOpen,
            SeProtocol protocolFlag, RevisionTarget revisionTarget, String extraInfo) {
        super(aid, selectNext, keepChannelOpen, protocolFlag, extraInfo);
        setMatchingClass(CalypsoPo.class);
        setSelectorClass(PoSelector.class);
        this.revisionTarget = RevisionTarget.TARGET_REV1;
        if (logger.isTraceEnabled()) {
            logger.trace("Calypso {} selector", revisionTarget);
        }
    }

    /**
     * Prepare one or more read record ApduRequest based on the target revision to be executed
     * following the selection.
     * <p>
     * In the case of a mixed target (rev2 or rev3) two commands are prepared. The first one in rev3
     * format, the second one in rev2 format (mainly class byte)
     * 
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     */
    // public void prepareReadRecordsCmd(byte sfi, byte firstRecordNumber, boolean
    // readJustOneRecord,
    // byte expectedLength, String extraInfo)
    public ReadRecordsRespPars prepareReadRecordsCmd(byte sfi,
            ReadDataStructure readDataStructureEnum, byte firstRecordNumber, byte expectedLength,
            String extraInfo) {
        /*
         * the readJustOneRecord flag is set to false only in case of multiple read records, in all
         * other cases it is set to true
         */
        boolean readJustOneRecord =
                !(readDataStructureEnum == readDataStructureEnum.MULTIPLE_RECORD_DATA);

        switch (this.revisionTarget) {
            case TARGET_REV1:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV1_0, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
            case TARGET_REV2:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV2_4, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
            case TARGET_REV3:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV3_1, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
            case TARGET_REV2_REV3:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV3_1, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV2_4, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("ReadRecords: SFI = {}, RECNUMBER = {}, JUSTONE = {}, EXPECTEDLENGTH = {}",
                    sfi, firstRecordNumber, readJustOneRecord, expectedLength);
        }

        /* create a parser to be returned to the caller */
        ReadRecordsRespPars poResponseParser =
                new ReadRecordsRespPars(firstRecordNumber, readDataStructureEnum);

        poResponseParserList.add(new CommandParser(poResponseParser,
                this.revisionTarget == RevisionTarget.TARGET_REV2_REV3));

        return poResponseParser;
    }

    /**
     * Prepare a custom read ApduRequest to be executed following the selection.
     * 
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     */
    public void preparePoCustomReadCmd(String name, ApduRequest apduRequest) {
        seSelectionApduRequestList
                .add(new PoCustomReadCommandBuilder(name, apduRequest).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("CustomReadCommand: APDUREQUEST = {}", apduRequest);
        }
    }

    /**
     * Prepare a custom modification ApduRequest to be executed following the selection.
     *
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     */
    public void preparePoCustomModificationCmd(String name, ApduRequest apduRequest) {
        seSelectionApduRequestList
                .add(new PoCustomModificationCommandBuilder(name, apduRequest).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("CustomModificationCommand: APDUREQUEST = {}", apduRequest);
        }
    }
}
