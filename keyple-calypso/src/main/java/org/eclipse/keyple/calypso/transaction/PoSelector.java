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
import java.util.Iterator;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoCustomModificationCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoCustomReadCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.message.ApduResponse;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.transaction.SeSelector;
import org.eclipse.keyple.util.ByteArrayUtils;
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
     */
    class CommandParser {
        private final AbstractApduResponseParser apduResponseParser;
        private final int numberOfRequests;

        public CommandParser(AbstractApduResponseParser apduResponseParser, int numberOfRequests) {
            this.apduResponseParser = apduResponseParser;
            this.numberOfRequests = numberOfRequests;
        }

        public AbstractApduResponseParser getApduResponseParser() {
            return apduResponseParser;
        }

        public int getNumberOfRequests() {
            return numberOfRequests;
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
     * @param channelState indicates whether the logical channel should remain open
     * @param protocolFlag the protocol flag to filter POs according to their communication protocol
     * @param extraInfo information string
     */
    public PoSelector(String atrRegex, ChannelState channelState, SeProtocol protocolFlag,
            RevisionTarget revisionTarget, String extraInfo) {
        super(atrRegex, channelState, protocolFlag, extraInfo);
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
     * @param selectMode a flag to indicate if the first or the next occurrence is requested
     * @param channelState indicates whether the logical channel should remain open
     * @param protocolFlag the protocol flag to filter POs according to their communication protocol
     * @param extraInfo information string
     */
    public PoSelector(byte[] aid, SelectMode selectMode, ChannelState channelState,
            SeProtocol protocolFlag, RevisionTarget revisionTarget, String extraInfo) {
        super(aid, selectMode, channelState, protocolFlag, extraInfo);
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
                /*
                 * we add here two readrecord commands because the two targeted PO differs with the
                 * expected apdu class (CLA)
                 *
                 * At most one response will be successful. The filling process of the parser will
                 * select the first one that succeeds.
                 */
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

        /*
         * keep the parser in a CommandParser list with the number of apduRequest associated with it
         */
        poResponseParserList.add(new CommandParser(poResponseParser,
                this.revisionTarget == RevisionTarget.TARGET_REV2_REV3 ? 2 : 1));

        return poResponseParser;
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     * <p>
     * 
     * @param path path from the MF (MF identifier excluded)
     * @param extraInfo extra information included in the logs (can be null or empty)
     */
    public SelectFileRespPars prepareSelectFileDfCmd(byte[] path, String extraInfo) {
        if (this.revisionTarget != RevisionTarget.TARGET_REV1) {
            throw new IllegalArgumentException("Select File not allowed here for REV2 or REV3 PO.");
        }
        seSelectionApduRequestList.add(new SelectFileCmdBuild(PoRevision.REV1_0,
                SelectFileCmdBuild.SelectControl.PATH_FROM_MF, SelectFileCmdBuild.SelectOptions.FCI,
                path).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("Select File: PATH = {}", ByteArrayUtils.toHex(path));
        }

        /* create a parser to be returned to the caller */
        SelectFileRespPars poResponseParser = new SelectFileRespPars();

        /*
         * keep the parser in a CommandParser list with the number of apduRequest associated with it
         */
        poResponseParserList.add(new CommandParser(poResponseParser,
                this.revisionTarget == RevisionTarget.TARGET_REV2_REV3 ? 2 : 1));

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


    /**
     * Loops on the SeResponse and updates the list of parsers previously memorized
     *
     * @param seResponse the seResponse from the PO
     */
    public void updateParsersWithResponses(SeResponse seResponse) {
        if (poResponseParserList.size() != 0) {
            /* attempt to update the parsers only if the list is not empty! */
            Iterator<CommandParser> parserIterator = poResponseParserList.iterator();
            /* double loop to set apdu responses to corresponding parsers */
            for (Iterator<ApduResponse> responseIterator =
                    seResponse.getApduResponses().iterator(); responseIterator.hasNext();) {
                if (!parserIterator.hasNext()) {
                    throw new IllegalStateException("Parsers list and responses list mismatch! ");
                }
                CommandParser parser = parserIterator.next();
                boolean parserUpdated = false;
                for (int i = 0; i < parser.getNumberOfRequests(); i++) {
                    if (!responseIterator.hasNext()) {
                        throw new IllegalStateException(
                                "Parsers list and responses list mismatch! ");
                    }
                    ApduResponse apduResponse = responseIterator.next();
                    if (!parserUpdated && apduResponse.isSuccessful()) {
                        parser.getApduResponseParser().setApduResponse(apduResponse);
                        parserUpdated = true;
                    }
                }
            }
        }
    }
}
