package org.yah.cli.command.annotation;

import org.yah.cli.CommandLine;
import org.yah.cli.command.Command;
import org.yah.cli.command.factory.ParameterSupplier;
import org.yah.cli.command.parameter.CommandParameter;
import org.yah.cli.command.parameter.ParameterParser;
import org.yah.cli.command.parameter.ParsedParameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface CliParameter {

    String name() default "";

    String description() default "";

    String defaultValue() default "";

    Class<? extends ParameterSupplier> supplier() default DefaultParameterSupplier.class;

    Class<? extends ParameterParser> parser() default DefaultParameterParser.class;

    ParameterCompleter completer() default @ParameterCompleter();

    class DefaultParameterSupplier implements ParameterSupplier {
        @Override
        public Object get(Command command, List<ParsedParameter> parsedParameters) {
            throw new UnsupportedOperationException();
        }
    }

    class DefaultParameterParser implements ParameterParser {
        @Override
        public Object parse(CommandParameter parameter, CommandLine commandLine) {
            throw new UnsupportedOperationException();
        }
    }
}
