package org.yah.cli.command;

import org.jline.builtins.Completers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yah.cli.CommandLine;

import java.util.*;
import java.util.stream.Collectors;

public class CommandsRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsRegistry.class);

    private final Map<String, Command> commandsMap;
    private final boolean strict;

    public CommandsRegistry(boolean strict) {
        commandsMap = new LinkedHashMap<>();
        this.strict = strict;
    }

    public void merge(Collection<Command> commands) {
        commands.forEach(this::merge);
    }

    public void merge(Command command) {
        for (String name : command.getNames()) {
            final Command previous = commandsMap.putIfAbsent(name, command);
            if (previous != null) {
                merge(previous, command, strict);
            }
        }
    }

    public Command get(String name) {
        return commandsMap.get(name);
    }

    public Optional<Command> resolve(CommandLine commandLine) {
        CommandsRegistry registry = this;
        Command command = null;
        while (commandLine.hasNext()) {
            String name = commandLine.peek();
            Command subCommand = registry.get(name);
            if (subCommand == null) {
                return Optional.ofNullable(command);
            }
            commandLine.next();

            registry = subCommand.getSubCommands();
            command = subCommand;
        }
        return Optional.ofNullable(command);
    }

    public List<Completers.TreeCompleter.Node> createTreeNodes() {
        return commandsMap.values().stream()
                .map(Command::createNode)
                .collect(Collectors.toList());
    }

    public boolean isStrict() {
        return strict;
    }

    public Map<String, Command> commandsMap() {
        return Collections.unmodifiableMap(commandsMap);
    }

    public Collection<Command> commands() {
        return commandsMap.values();
    }

    public Command command(String name) {
        return commandsMap.get(name);
    }

    public List<Completers.TreeCompleter.Node> createtNodes() {
        return commands().stream()
                .map(Command::createNode)
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return commandsMap.isEmpty();
    }

    void setParent(Command command) {
        commandsMap.values().forEach(c -> c.setParent(command));
    }

    private void merge(Command previous, Command command, boolean strict) {
        if (previous.hasAction() && command.hasAction()) {
            String message = "Command '" + command.getQualifiedName() + "' conflict with '" + previous
                    .getQualifiedName() + "'";
            if (strict)
                throw new IllegalArgumentException(message);
            else
                LOGGER.info(message);
        }
        final Collection<Command> newSubCommands = command.getSubCommands().commands();
        for (Command subCommand : newSubCommands) {
            subCommand.setParent(previous);
        }
        previous.getSubCommands().merge(newSubCommands);
    }
}
