package org.yah.cli;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.yah.cli.command.CommandsRegistry;
import org.yah.cli.command.help.EmptyResourceBundle;

import java.nio.file.Path;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public interface Cli extends AutoCloseable {

    default String name() {
        return getClass().getSimpleName();
    }

    default void configure(TerminalBuilder builder) {
        builder.system(true).jansi(true);
    }

    default void configure(LineReaderBuilder builder) {
        builder.variable(LineReader.HISTORY_FILE, Path.of("." + name().toLowerCase() + "-history"));
    }

    void createCommands(Terminal terminal, CommandsRegistry registry);

    @SuppressWarnings("RedundantThrows")
    default void close() throws Exception {
    }

    default boolean isStrict() {
        return true;
    }

    default ResourceBundle commandsResources() {
        try {
            return ResourceBundle.getBundle(getClass().getName());
        } catch (MissingResourceException e) {
            return EmptyResourceBundle.INSTANCE;
        }
    }

}
