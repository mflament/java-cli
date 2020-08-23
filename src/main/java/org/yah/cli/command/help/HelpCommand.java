package org.yah.cli.command.help;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.fusesource.jansi.Ansi;
import org.jline.builtins.Completers;
import org.jline.terminal.Terminal;
import org.yah.cli.CommandLine;
import org.yah.cli.command.Command;
import org.yah.cli.command.CommandsRegistry;
import org.yah.cli.command.parameter.CommandParameter;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.fusesource.jansi.Ansi.ansi;

public class HelpCommand {

    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static Command create(CommandsRegistry registry, Terminal terminal, ResourceBundle commandResources) {
        HelpCommand hc = new HelpCommand(registry, terminal, commandResources);
        final List<Completers.TreeCompleter.Node> helperNodes = registry.createtNodes();
        return Command.cmd("help")
                .withDescription("Print some help.")
                .withHelp("Print some help for a given a command or list the available commands")
                .withCompleterFactory(nodes -> nodes.addAll(helperNodes))
                .withAction(hc::printHelp)
                .build();
    }

    private final CommandsRegistry registry;
    private final Terminal terminal;
    private final ResourceBundle commandResources;

    public HelpCommand(CommandsRegistry registry, Terminal terminal, ResourceBundle commandResources) {
        this.registry = registry;
        this.terminal = terminal;
        this.commandResources = commandResources;
    }

    private void printHelp(CommandLine commandLine) {
        final Command command = registry.resolve(commandLine).orElse(null);
        final Ansi ansi = ansi();

        CommandsRegistry registry;
        if (command != null) {
            String description = getDescription(command, null);
            String help = getHelp(command);

            final List<Command> path = command.getPath();
            path.subList(0, path.size() - 1).stream()
                    .map(Command::getName)
                    .forEach(n -> ansi.a(n).a(" > "));
            String names = String.join(", ", command.getNames());
            ansi.fgBrightDefault().a(names).reset().a(LINE_SEPARATOR).a(LINE_SEPARATOR);
            if (description != null) {
                ansi.a("  ").a(description.trim()).a(LINE_SEPARATOR).a(LINE_SEPARATOR);
            }
            if (help != null) {
                help = help.replaceAll("\\n", "");
                help = WordUtils.wrap(help, terminal.getWidth() - 2, LINE_SEPARATOR + "  ", false);
                ansi.a("  ").a(help).a(LINE_SEPARATOR).a(LINE_SEPARATOR);
            }
            if (!command.getParameters().isEmpty())
                printParameters(command, ansi);
            registry = command.getSubCommands();
        } else {
            registry = this.registry;
        }
        if (!registry.isEmpty())
            printCommands(registry.commandsMap(), ansi);
        terminal.writer().write(ansi.toString());
    }

    private void printParameters(Command command, Ansi ansi) {
        final int maxLength = command.getParameters().stream()
                .map(this::parameterName)
                .mapToInt(String::length)
                .max()
                .orElse(0);
        ansi.a("Parameters:").a(LINE_SEPARATOR);
        for (CommandParameter parameter : command.getParameters()) {
            String description = getParameterDescription(command, parameter);
            ansi.a("    ").fgBrightDefault()
                    .format("  %-" + (maxLength + 1) + "s", parameterName(parameter)).reset()
                    .a(": ").a(description).a(LINE_SEPARATOR);
        }
    }

    private String parameterName(CommandParameter parameter) {
        return parameter.getName() == null ? Integer.toString(parameter.getIndex()) : parameter.getName();
    }

    private String getHelp(Command command) {
        String res;
        if (command.getHelp() != null) {
            res = command.getHelp();
        } else {
            String key = command.getQualifiedName() + ".help";
            try {
                res = commandResources.getString(key);
            } catch (MissingResourceException e) {
                res = null;
            }
        }
        return StringUtils.trimToNull(res);
    }

    private String getDescription(Command command, String defaultValue) {
        String res;
        if (command.getDescription() != null)
            res = command.getDescription();
        else {
            String key = command.getQualifiedName() + ".description";
            try {
                res = commandResources.getString(key);
            } catch (MissingResourceException e) {
                res = null;
            }
        }
        res = StringUtils.trimToNull(res);
        return res == null ? defaultValue : res;
    }


    private String getParameterDescription(Command command, CommandParameter parameter) {
        if (parameter.getDescription() != null)
            return parameter.getDescription();

        if (parameter.getName() != null) {
            String key = command.getQualifiedName() + "." + parameter.getName() + ".description";
            try {
                return commandResources.getString(key);
            } catch (MissingResourceException e) {
                // ignore
            }
        }
        return "No description";
    }

    private void printCommands(Map<String, Command> commands, Ansi ansi) {
        ansi.a("Commands:").a(LINE_SEPARATOR);
        final int maxLength = commands.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            ansi.fgBrightDefault().format("  %-" + (maxLength + 1) + "s", entry.getKey()).reset();
            ansi.a(": ").a(getDescription(entry.getValue(), "No description."));
            ansi.a(LINE_SEPARATOR);
        }
    }

}
