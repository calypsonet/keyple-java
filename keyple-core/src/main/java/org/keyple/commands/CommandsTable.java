package org.keyple.commands;

public interface CommandsTable {

    String getName();

    byte getInstructionByte();

    Class<? extends ApduCommandBuilder> getCommandBuilderClass();

    Class<? extends ApduResponseParser> getResponseParserClass();

}
