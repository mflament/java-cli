package org.yah.cli.command;

import org.yah.cli.CommandLine;
import org.yah.cli.command.parameter.ParsedParameter;

import java.util.List;

public final class BuilderActions {
    private BuilderActions() {
    }

    public interface RunnableCommandAction {
        void run() throws Exception;
    }

    public interface CommandLineOnlyAction {
        void run(CommandLine commandLine) throws Exception;
    }

    public interface ParsedParametersCommandAction {
        void run(Command command, List<ParsedParameter> parameters) throws Exception;
    }

}
