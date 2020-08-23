package org.yah.cli.command;

import org.yah.cli.CommandLine;

import java.util.List;

@FunctionalInterface
public interface CommandAction {

    CommandAction UNIMPLENTED_COMMAND_ACTION = (command, commandLine) -> {
        System.err.println(commandLine + " is not implemented");
    };

    void run(Command command, CommandLine commandLine) throws Exception;

}
