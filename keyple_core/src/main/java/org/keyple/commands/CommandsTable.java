package org.keyple.commands;

public interface CommandsTable {

    public String getName();

    public byte getInstructionByte();

//    public Class<?> getCommandBuilderClass();
    public Class<ApduCommandBuilder> getCommandBuilderClass();

//    public Class<?> getResponseParserClass();
    public Class<ApduResponseParser> getResponseParserClass();

}
