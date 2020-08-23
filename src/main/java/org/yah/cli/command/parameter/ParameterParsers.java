package org.yah.cli.command.parameter;

import org.apache.commons.lang3.StringUtils;
import org.yah.cli.CommandLine;

import java.lang.reflect.Constructor;

public class ParameterParsers {

    public static <T extends Enum<T>> ParameterParser enumParser(Class<T> enumType) {
        if (!enumType.isEnum())
            throw new IllegalArgumentException(enumType.getName() + " is not an enum");
        return nullableParser((param, commandLine) -> Enum.valueOf(enumType, commandLine.next()));
    }

    public static ParameterParser objectFromStringParser(Constructor<?> constructor) {
        return nullableParser((parameter, commandLine) -> {
            final String next = commandLine.next();
            try {
                return constructor.newInstance(next);
            } catch (Exception e) {
                final String className = constructor.getDeclaringClass().getName();
                throw new UnsupportedOperationException("Error creating " + className +
                        " object from '" + next + "'", e);
            }
        });
    }

    public static ParameterParser passthrough() {
        return (parameter, commandLine) -> commandLine.next();
    }

    public static ParameterParser nullableParser(ParameterParser delegate) {
        return (parameter, commandLine) -> {
            String text = StringUtils.trimToNull(commandLine.peek());
            if (text == null || text.equals("null")) {
                commandLine.next();
                return null;
            }
            return delegate.parse(parameter, commandLine);
        };
    }

    public static final class FlushCommandLine implements ParameterParser {
        @Override
        public Object parse(CommandParameter parameter, CommandLine commandLine) {
            return commandLine.flush();
        }
    }
}
