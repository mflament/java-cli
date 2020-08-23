package org.yah.cli.command;

import org.fusesource.jansi.Ansi;
import org.jline.builtins.Completers.TreeCompleter;
import org.jline.builtins.Completers.TreeCompleter.Node;
import org.jline.reader.Completer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yah.cli.CommandLine;
import org.yah.cli.PassthroughCompleter;
import org.yah.cli.command.parameter.CommandParameter;
import org.yah.cli.command.parameter.ParsedParameter;

import java.util.*;
import java.util.stream.Collectors;

import static org.yah.cli.command.CommandAction.UNIMPLENTED_COMMAND_ACTION;

public final class Command {

    public static CommandBuilder cmd(String name) {
        return cmd(name, true);
    }

    public static CommandBuilder cmd(String name, boolean strict) {
        return new CommandBuilder(name, strict);
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);
    private final String name;
    private final Set<String> names;
    private final CommandAction action;
    private final CommandsRegistry subCommands;
    private final CompleterFactory completerFactory;
    private final List<CommandParameter> parameters;
    private final String description;
    private final String help;
    private Command parent;
    private String qualifiedName;

    Command(AbstractCommandBuilder<?> builder) {
        this.name = builder.name;
        this.names = Set.copyOf(builder.names);
        this.description = Objects.requireNonNull(builder.description);
        this.help = Objects.requireNonNull(builder.help);
        this.action = Objects.requireNonNull(builder.action);
        this.subCommands = builder.subCommands;
        this.parameters = List.copyOf(builder.parameters);
        this.completerFactory = builder.completerFactory;
        subCommands.setParent(this);
    }

    public CommandAction getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    public Set<String> getNames() {
        return names;
    }

    public String getQualifiedName() {
        if (qualifiedName == null) {
            qualifiedName = getPath().stream()
                    .map(Command::getName)
                    .collect(Collectors.joining("."));
        }
        return qualifiedName;
    }

    public List<Command> getPath() {
        List<Command> path = new ArrayList<>();
        Command current = this;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getHelp() {
        return help;
    }

    public List<CommandParameter> getParameters() {
        return parameters;
    }

    public CommandsRegistry getSubCommands() {
        return subCommands;
    }

    public boolean hasAction() {
        return action != UNIMPLENTED_COMMAND_ACTION;
    }

    public List<ParsedParameter> parseParameters(CommandLine commandLine) {
        if (parameters.isEmpty())
            return Collections.emptyList();

        List<ParsedParameter> parsedParameters = new ArrayList<>(parameters.size());
        for (CommandParameter parameter : parameters) {
            try {
                parsedParameters.add(parameter.parse(commandLine));
            } catch (RuntimeException e) {
                LOGGER.error("Error parsing parameter {}", parameter.getName(), e);
                System.out.println(Ansi.ansi().a("Error parsing parameter ").fgBrightDefault().a(parameter.getName())
                        .reset().a(": ").a(e.getMessage()));
                return null;
            }
        }
        return parsedParameters;
    }

    void setParent(Command command) {
        parent = command;
    }

    Node createNode() {
        List<Object> nodes = new ArrayList<>(names);
        if (completerFactory != null) {
            completerFactory.create(nodes);
        } else {
            final ListIterator<CommandParameter> parameterIterator = parameters.listIterator(parameters.size());
            Node parameterNode = null;
            while (parameterIterator.hasPrevious()) {
                parameterNode = createParameterNode(parameterIterator.previous(), parameterNode);
            }

            if (parameterNode != null)
                nodes.add(parameterNode);

            nodes.addAll(subCommands.createTreeNodes());
        }
        return TreeCompleter.node(nodes.toArray());
    }

    private Node createParameterNode(CommandParameter parameter, Node nextParameter) {
        Completer completer = parameter.getCompleter();
        if (completer == null)
            completer = PassthroughCompleter.INSTANCE;
        if (nextParameter != null)
            return TreeCompleter.node(completer, nextParameter);
        return TreeCompleter.node(completer);
    }

    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface CompleterFactory {
        void create(List<Object> nodes);
    }

}
