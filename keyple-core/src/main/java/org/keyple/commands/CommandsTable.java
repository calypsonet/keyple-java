/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.commands;

public interface CommandsTable {

    String getName();

    byte getInstructionByte();

    Class<? extends ApduCommandBuilder> getCommandBuilderClass();

    Class<? extends ApduResponseParser> getResponseParserClass();

}
