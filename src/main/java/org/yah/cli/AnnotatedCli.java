package org.yah.cli;

import org.jline.terminal.Terminal;
import org.yah.cli.command.CommandsRegistry;
import org.yah.cli.command.factory.CommandAnnotationParser;
import org.yah.cli.command.factory.ParameterSuppliersRegistry;
import org.yah.cli.command.parameter.ParameterParsersRegistry;

import java.lang.reflect.Method;
import java.util.function.Function;

public interface AnnotatedCli extends Cli {

    @Override
    default void createCommands(Terminal terminal, CommandsRegistry registry) {
        final CommandAnnotationParser parser = new CommandAnnotationParser(terminal, commandNameStrategy(),
                parameterSuppliersRegistry(),
                parameterParserRegistry());
        parse(parser, registry);
    }

    default void parse(CommandAnnotationParser parser, CommandsRegistry registry) {
        parser.parse(this, registry);
    }

    default Function<Method, String> commandNameStrategy() {
        return null;
    }

    default ParameterSuppliersRegistry parameterSuppliersRegistry() {
        return null;
    }

    default ParameterParsersRegistry parameterParserRegistry() {
        return null;
    }

}
