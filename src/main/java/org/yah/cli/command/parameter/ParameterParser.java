package org.yah.cli.command.parameter;

import org.yah.cli.CommandLine;

@FunctionalInterface
public interface ParameterParser {
    Object parse(CommandParameter parameter, CommandLine commandLine);
}
