package org.yah.cli;

import org.jline.builtins.Completers.TreeCompleter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.yah.cli.command.Command;
import org.yah.cli.command.CommandsRegistry;
import org.yah.cli.command.help.HelpCommand;
import org.yah.cli.parser.CliParser;
import org.yah.cli.parser.MappedParsedLine;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.fusesource.jansi.Ansi.ansi;

public final class CliApplication implements AutoCloseable {

    public static void start(CliFactory factory) throws Exception {
        Objects.requireNonNull(factory);
        try (CliApplication app = new CliApplication(factory)) {
            app.run();
        }
    }

    private final Cli cli;

    private final CommandExecutor executor;
    private final CommandsRegistry commands;

    private final Terminal terminal;
    private final LineReader lineReader;
    private final TreeCompleter completer;

    private boolean exitRequested;

    public CliApplication(CliFactory factory) throws Exception {
        cli = Objects.requireNonNull(factory.create());
        terminal = createTerminal();
        executor = new CommandExecutor(terminal);
        commands = createCommandRegistry();
        completer = new TreeCompleter(commands.createtNodes());
        lineReader = createLineReader();
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            lineReader.getHistory().save();
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
        closeQuietly(terminal);
        closeQuietly(cli);
    }

    private Terminal createTerminal() throws IOException {
        final TerminalBuilder builder = TerminalBuilder.builder();
        builder.jna(false).jansi(true).dumb(true).name(cli.name());
        cli.configure(builder);
        return builder.build();
    }

    private CommandsRegistry createCommandRegistry() {
        final CommandsRegistry commandsRegistry = new CommandsRegistry(cli.isStrict());
        cli.createCommands(terminal, commandsRegistry);
        commandsRegistry.merge(exitCommand());
        commandsRegistry.merge(HelpCommand.create(commandsRegistry, terminal, cli.commandsResources()));
        return commandsRegistry;
    }

    private Command exitCommand() {
        return Command.cmd("exit")
                .withOtherNames("quit")
                .withDescription("Exit " + cli.name())
                .withAction(() -> exitRequested = true)
                .build();
    }

    private LineReader createLineReader() {
        LineReaderBuilder builder = LineReaderBuilder.builder()
                .appName(cli.name())
                .terminal(terminal)
                .parser(new CliParser())
                .history(new DefaultHistory())
                .completer(completer)
                .option(LineReader.Option.HISTORY_BEEP, true);
        cli.configure(builder);
        return builder.build();
    }

    private void run() {
        String prompt = ansi().fgYellow().a(cli.name() + "$ ").reset().toString();
        while (!exitRequested) {
            try {
                lineReader.readLine(prompt);
                final MappedParsedLine parsedLine = (MappedParsedLine) lineReader.getParsedLine();
                ParsedCommandLine commandLine = new ParsedCommandLine(parsedLine);
                if (commandLine.hasNext()) {
                    final Optional<Command> resolved = commands.resolve(commandLine);
                    if (resolved.isPresent()) {
                        executor.run(resolved.get(), commandLine).join();
                    } else {
                        System.out.println("Unknown command");
                    }
                }
            } catch (UserInterruptException e) {
                // ignore
            } catch (EndOfFileException e) {
                break;
            }
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @FunctionalInterface
    public interface CliFactory {
        Cli create() throws Exception;
    }

}
