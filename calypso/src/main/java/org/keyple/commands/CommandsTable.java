package org.keyple.commands;

// TODO: Drop it ? pterr's removed it
public interface CommandsTable {

    public String getName();

    public byte getInstructionByte();

    public Class<?> getCommandBuilderClass();

    public Class<?> getResponseParserClass();

}
