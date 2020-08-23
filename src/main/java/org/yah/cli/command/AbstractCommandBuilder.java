package org.yah.cli.command;

import org.yah.cli.command.BuilderActions.CommandLineOnlyAction;
import org.yah.cli.command.BuilderActions.ParsedParametersCommandAction;
import org.yah.cli.command.BuilderActions.RunnableCommandAction;
import org.yah.cli.command.Command.CompleterFactory;
import org.yah.cli.command.parameter.CommandParameter;
import org.yah.cli.command.parameter.ParsedParameter;

import java.util.*;
import java.util.function.Consumer;

import static org.yah.cli.command.CommandAction.UNIMPLENTED_COMMAND_ACTION;

abstract class AbstractCommandBuilder<SELF extends AbstractCommandBuilder<SELF>> {

    protected final String name;
    protected final Set<String> names = new HashSet<>();
    protected final CommandsRegistry subCommands;
    protected String description;
    protected String help;
    protected CommandAction action;
    protected CompleterFactory completerFactory;

    protected final List<CommandParameter> parameters = new ArrayList<>();

    protected AbstractCommandBuilder(String name, boolean strict) {
        this.name = Objects.requireNonNull(name);
        this.names.add(name);
        this.subCommands = new CommandsRegistry(strict);
    }

    public SELF withOtherNames(String... names) {
        this.names.addAll(Arrays.asList(names));
        return getThis();
    }

    public SELF withDescription(String description) {
        this.description = description;
        return getThis();
    }

    public SELF withHelp(String help) {
        this.help = help;
        return getThis();
    }

    public SELF withAction(CommandAction action) {
        this.action = action;
        return getThis();
    }

    public SELF withAction(RunnableCommandAction action) {
        this.action = (command, commandLine) -> action.run();
        return getThis();
    }

    public SELF withAction(CommandLineOnlyAction action) {
        this.action = (command, commandLine) -> action.run(commandLine);
        return getThis();
    }

    public SELF withAction(ParsedParametersCommandAction action) {
        this.action = (command, commandLine) -> {
            final List<ParsedParameter> parsedParameters = command.parseParameters(commandLine);
            if (parsedParameters != null)
                action.run(command, parsedParameters);
        };
        return getThis();
    }

    public SELF withCompleterFactory(CompleterFactory completerFactory) {
        this.completerFactory = completerFactory;
        return getThis();
    }

    public SELF withParameter(Consumer<CommandParameter.Builder> parameterBuilder) {
        final CommandParameter.Builder builder = CommandParameter.builder(parameters.size());
        parameterBuilder.accept(builder);
        parameters.add(builder.build());
        return getThis();
    }

    public SELF withParameters(List<CommandParameter> commandParameters) {
        this.parameters.addAll(commandParameters);
        return getThis();
    }

    public SELF withSubCommands(Command... commands) {
        this.subCommands.merge(Arrays.asList(commands));
        return getThis();
    }

    protected abstract SELF getThis();

    protected final void prepare() {
        if (action == null) action = UNIMPLENTED_COMMAND_ACTION;
        if (description == null) description = "";
        if (help == null) help = "";
    }

    protected final boolean isStrict() {
        return subCommands.isStrict();
    }

}
