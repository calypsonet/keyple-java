package org.keyple.commands;

public interface CommandsTable {

    String getName();

    byte getInstructionByte();

    Class<ApduCommandBuilder> getCommandBuilderClass();

    Class<ApduResponseParser> getResponseParserClass();

}
