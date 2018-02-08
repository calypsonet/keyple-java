package org.keyple.commands;

import org.keyple.commands.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.commands.csm.builder.DigestAuthenticateCmdBuild;
import org.keyple.commands.csm.builder.DigestCloseCmdBuild;
import org.keyple.commands.csm.builder.DigestInitCmdBuild;
import org.keyple.commands.csm.builder.DigestUpdateCmdBuild;
import org.keyple.commands.csm.builder.DigestUpdateMultipleCmdBuild;
import org.keyple.commands.csm.builder.SelectDiversifierCmdBuild;
import org.keyple.commands.csm.parser.CsmGetChallengeRespPars;
import org.keyple.commands.csm.parser.DigestAuthenticateRespPars;
import org.keyple.commands.csm.parser.DigestCloseRespPars;
import org.keyple.commands.csm.parser.DigestInitRespPars;
import org.keyple.commands.csm.parser.DigestUpdateMultipleRespPars;
import org.keyple.commands.csm.parser.DigestUpdateRespPars;
import org.keyple.commands.csm.parser.SelectDiversifierRespPars;
import org.keyple.commands.po.builder.AppendRecordCmdBuild;
import org.keyple.commands.po.builder.CloseSessionCmdBuild;
import org.keyple.commands.po.builder.GetDataFciCmdBuild;
import org.keyple.commands.po.builder.OpenSessionCmdBuild;
import org.keyple.commands.po.builder.PoGetChallengeCmdBuild;
import org.keyple.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.commands.po.parser.AppendRecordRespPars;
import org.keyple.commands.po.parser.CloseSessionRespPars;
import org.keyple.commands.po.parser.GetDataFciRespPars;
import org.keyple.commands.po.parser.OpenSessionRespPars;
import org.keyple.commands.po.parser.PoGetChallengeRespPars;
import org.keyple.commands.po.parser.ReadRecordsRespPars;
import org.keyple.commands.po.parser.UpdateRecordRespPars;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.commands.ApduResponseParser;
import org.keyple.commands.CommandsTable;

/**
 * This enumeration registers all the PO and CSM commands, that have to be
 * extended by all PO and CSM command builder classes. It provides the generic
 * getters to retrieve: the name of the command, the built APDURequest and, the
 * corresponding ApduResponseParser class.
 *
 * @author Ixxi
 *
 */
public enum CalypsoCommands implements CommandsTable {

    /** The po get data. */
    PO_GET_DATA_FCI(CommandType.PO, "Get Data'FCI'", (byte) 0xCA, GetDataFciCmdBuild.class, GetDataFciRespPars.class),

    /** The po open session. */
    PO_OPEN_SESSION(CommandType.PO, "Open Secure Session", (byte) 0x8A, OpenSessionCmdBuild.class,
            OpenSessionRespPars.class),

    /** The po close session. */
    PO_CLOSE_SESSION(CommandType.PO, "Close Secure Session", (byte) 0x8E, CloseSessionCmdBuild.class,
            CloseSessionRespPars.class),

    /** The po read records. */
    PO_READ_RECORDS(CommandType.PO, "Read Records", (byte) 0xB2, ReadRecordsCmdBuild.class, ReadRecordsRespPars.class),

    /** The po update record. */
    PO_UPDATE_RECORD(CommandType.PO, "Update Record", (byte) 0xDC, UpdateRecordCmdBuild.class,
            UpdateRecordRespPars.class),

    /** The po append record. */
    PO_APPEND_RECORD(CommandType.PO, "Append Record", (byte) 0xE2, AppendRecordCmdBuild.class,
            AppendRecordRespPars.class),

    /** The po get challenge. */
    PO_GET_CHALLENGE(CommandType.PO, "Get Challenge", (byte) 0x84, PoGetChallengeCmdBuild.class,
            PoGetChallengeRespPars.class),

    /** The csm select diversifier. */
    CSM_SELECT_DIVERSIFIER(CommandType.CSM, "Select Diversifier", (byte) 0x14, SelectDiversifierCmdBuild.class,
            SelectDiversifierRespPars.class),

    /** The csm get challenge. */
    CSM_GET_CHALLENGE(CommandType.CSM, "Get Challenge", (byte) 0x84, CsmGetChallengeCmdBuild.class,
            CsmGetChallengeRespPars.class),

    /** The csm digest init. */
    CSM_DIGEST_INIT(CommandType.CSM, "Digest Init", (byte) 0x8A, DigestInitCmdBuild.class, DigestInitRespPars.class),

    /** The csm digest update. */
    CSM_DIGEST_UPDATE(CommandType.CSM, "Digest Update", (byte) 0x8C, DigestUpdateCmdBuild.class,
            DigestUpdateRespPars.class),

    /** The csm digest update multiple. */
    CSM_DIGEST_UPDATE_MULTIPLE(CommandType.CSM, "Digest Update Multiple", (byte) 0x8C,
            DigestUpdateMultipleCmdBuild.class, DigestUpdateMultipleRespPars.class),

    /** The csm digest close. */
    CSM_DIGEST_CLOSE(CommandType.CSM, "Digest Close", (byte) 0x8E, DigestCloseCmdBuild.class,
            DigestCloseRespPars.class),

    /** The csm digest authenticate. */
    CSM_DIGEST_AUTHENTICATE(CommandType.CSM, "Digest Authenticate", (byte) 0x82,  DigestAuthenticateCmdBuild.class,
            DigestAuthenticateRespPars.class);

    /** The command type. */
    private CommandType commandType;

    /** The name. */
    private String name;

    /** The instruction byte. */
    private byte instructionbyte;

    /** The command builder class. */
//    private Class<?> commandBuilderClass;
    private Class<ApduCommandBuilder> commandBuilderClass;

    /** The response parser class. */
//    private Class<?> responseParserClass;
    private Class<ApduResponseParser> responseParserClass;

    /**
     * The generic constructor of CalypsoCommands.
     *
     * @param commandType
     *            the command type
     * @param name
     *            the name
     * @param instructionbyte
     *            the instruction byte
     * @param commandBuilderClass
     *            the command builder class
     * @param responseParserClass
     *            the response parser class
     */
    private CalypsoCommands(CommandType commandType, String name, byte instructionbyte, Class<?> commandBuilderClass, Class<?> responseParserClass) { 
        this.commandType = commandType;
        this.name = name;
        this.instructionbyte = instructionbyte;
        this.commandBuilderClass = (Class<ApduCommandBuilder>) commandBuilderClass;
        this.responseParserClass = (Class<ApduResponseParser>) responseParserClass;
    }

    /**
     * Gets the type.
     *
     * @return the command type
     */
    public CommandType getType() {
        return commandType;
    }

    /**
     * Gets the name.
     *
     * @return the command name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the instruction byte.
     *
     * @return the value of INS byte
     */
    public byte getInstructionByte() {
        return instructionbyte;
    }

    /**
     * Gets the command builder class.
     *
     * @return the corresponding command builder class
     */
//    public Class<?> getCommandBuilderClass() {
      public Class<ApduCommandBuilder> getCommandBuilderClass() {
        return commandBuilderClass;
    }

    /**
     * Gets the response parser class.
     *
     * @return the corresponding response parser class
     */
//    public Class<?> getResponseParserClass() {
      public Class<ApduResponseParser> getResponseParserClass() {
        return responseParserClass;
    }

}
