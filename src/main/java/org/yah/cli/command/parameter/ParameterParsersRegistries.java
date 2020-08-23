package org.yah.cli.command.parameter;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static org.yah.cli.command.parameter.ParameterParsers.passthrough;

public final class ParameterParsersRegistries {

    public static ParameterParsersRegistry defaultParsers() {
        return composite(new PrimitiveParsersRegistry(),
                new JavaObjectsParsersRegistry(),
                new StringConstructorParsersRegistry(),
                new EnumParserRegistry()
        );
    }

    public static ParameterParsersRegistry composite(ParameterParsersRegistry... registries) {
        return composite(Arrays.asList(registries));
    }

    public static ParameterParsersRegistry composite(Collection<ParameterParsersRegistry> registries) {
        return type -> registries.stream().map(r -> r.get(type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private ParameterParsersRegistries() {
    }

    private static ParameterParser parser(Function<String, Object> function) {
        return (param, commandLine) -> {
            final String next = commandLine.next();
            if (next == null)
                throw new NoSuchElementException();
            return function.apply(next);
        };
    }

    private static ParameterParser nullableParser(Function<String, Object> function) {
        return ParameterParsers.nullableParser((param, commandLine) -> function.apply(commandLine.next()));
    }

    private static class JavaObjectsParsersRegistry implements ParameterParsersRegistry {

        private final Map<Class<?>, ParameterParser> parsersMap = new HashMap<>();

        public JavaObjectsParsersRegistry() {
            parsersMap.put(String.class, passthrough());
            parsersMap.put(File.class, nullableParser(File::new));
            parsersMap.put(Path.class, nullableParser(Path::of));
        }

        @Override
        public Optional<ParameterParser> get(Class<?> type) {
            return Optional.ofNullable(parsersMap.get(type));
        }

    }

    private static class EnumParserRegistry implements ParameterParsersRegistry {
        @Override
        public Optional<ParameterParser> get(Class<?> type) {
            if (type.isEnum()) {
                //noinspection unchecked,rawtypes
                return Optional.of(ParameterParsers.enumParser((Class<Enum>) type));
            }
            return Optional.empty();
        }
    }

    private static class StringConstructorParsersRegistry implements ParameterParsersRegistry {
        @Override
        public Optional<ParameterParser> get(Class<?> type) {
            final Constructor<?> constructor = getConstructor(type);
            if (constructor == null)
                return Optional.empty();
            return Optional.of(ParameterParsers.objectFromStringParser(constructor));
        }

        private static Constructor<?> getConstructor(Class<?> type) {
            try {
                return type.getConstructor(String.class);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }

    private static class PrimitiveParsersRegistry implements ParameterParsersRegistry {

        private final Map<Class<?>, ParameterParser> parsersMap = new HashMap<>();

        public PrimitiveParsersRegistry() {
            parsersMap.put(Byte.TYPE, parser(Byte::parseByte));
            parsersMap.put(Short.TYPE, parser(Short::parseShort));
            parsersMap.put(Integer.TYPE, parser(Integer::parseInt));
            parsersMap.put(Long.TYPE, parser(Long::parseLong));
            parsersMap.put(Float.TYPE, parser(Float::parseFloat));
            parsersMap.put(Double.TYPE, parser(Double::parseDouble));
            parsersMap.put(Boolean.TYPE, parser(Boolean::parseBoolean));
            parsersMap.put(Character.TYPE, parser(s -> s.charAt(0)));

            parsersMap.put(Byte.class, nullableParser(Byte::parseByte));
            parsersMap.put(Short.class, nullableParser(Short::parseShort));
            parsersMap.put(Integer.class, nullableParser(Integer::parseInt));
            parsersMap.put(Long.class, nullableParser(Long::parseLong));
            parsersMap.put(Float.class, nullableParser(Float::parseFloat));
            parsersMap.put(Double.class, nullableParser(Double::parseDouble));
            parsersMap.put(Boolean.class, nullableParser(Boolean::parseBoolean));
            parsersMap.put(Character.class, nullableParser(s -> s.charAt(0)));
        }

        @Override
        public Optional<ParameterParser> get(Class<?> type) {
            return Optional.ofNullable(parsersMap.get(type));
        }

    }
}
