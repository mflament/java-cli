package org.yah.cli;

import org.fusesource.jansi.Ansi;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yah.cli.command.Command;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    private final Terminal terminal;
    private final ExecutorService executorService;

    private Thread thread;
    private boolean interrupted;

    public CommandExecutor(Terminal terminal) {
        this.terminal = terminal;
        terminal.handle(Terminal.Signal.INT, s -> interrupt());
        executorService = Executors.newSingleThreadExecutor(r -> thread = new Thread(r, "command-executor"));
    }

    public void shutdown() {
        executorService.shutdown();
        interrupt();
    }

    public CompletableFuture<?> run(Command command, CommandLine commandLine) {
        return CompletableFuture.runAsync(() -> {
            try {
                command.getAction().run(command, commandLine);
            } catch (Exception e) {
                if (interrupted) return;
                LOGGER.error("Error executing command '{}'", command.getQualifiedName(), e);
                final String message = new Ansi()
                        .fgRed()
                        .format("Error executing '%s(%s)': %s", command.getQualifiedName(), commandLine, e.getMessage())
                        .reset()
                        .newline()
                        .toString();
                terminal.writer().write(message);
                e.printStackTrace(terminal.writer());
            }
        }, executorService);
    }

    public synchronized void interrupt() {
        if (thread == null) return;
        interrupted = true;
        thread.interrupt();
    }

}
