package org.yah.cli.command.parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PrimitiveParsersRegistry implements ParameterParsersRegistry {

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

        parsersMap.put(Byte.class, parser(Byte::parseByte));
        parsersMap.put(Short.class, parser(Short::parseShort));
        parsersMap.put(Integer.class, parser(Integer::parseInt));
        parsersMap.put(Long.class, parser(Long::parseLong));
        parsersMap.put(Float.class, parser(Float::parseFloat));
        parsersMap.put(Double.class, parser(Double::parseDouble));
        parsersMap.put(Boolean.class, parser(Boolean::parseBoolean));
        parsersMap.put(Character.class, parser(s -> s.charAt(0)));

        parsersMap.put(String.class, parser(s -> s));
        parsersMap.put(File.class, parser(File::new));
        parsersMap.put(Path.class, parser(Path::of));
    }

    @Override
    public Optional<ParameterParser> get(Class<?> type) {
        return Optional.ofNullable(parsersMap.get(type));
    }

    private static ParameterParser parser(Function<String, Object> function) {
        return ParameterParsers.nullableParser((param, commandLine) -> function.apply(commandLine.next()));
    }

}
