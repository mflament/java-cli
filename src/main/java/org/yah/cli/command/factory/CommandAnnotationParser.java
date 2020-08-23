package org.yah.cli.command.factory;

import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.yah.cli.PassthroughCompleter;
import org.yah.cli.command.Command;
import org.yah.cli.command.CommandBuilder;
import org.yah.cli.command.CommandsRegistry;
import org.yah.cli.command.annotation.CliCommand;
import org.yah.cli.command.annotation.CliCommands;
import org.yah.cli.command.annotation.CliParameter;
import org.yah.cli.command.annotation.ParameterCompleter;
import org.yah.cli.command.annotation.ParameterCompleter.DefaultParameterCompleter;
import org.yah.cli.command.parameter.CommandParameter;
import org.yah.cli.command.parameter.ParameterParser;
import org.yah.cli.command.parameter.ParameterParsersRegistries;
import org.yah.cli.command.parameter.ParameterParsersRegistry;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.yah.cli.command.factory.ParameterSuppliersRegistries.defaultSuppliers;
import static org.yah.cli.command.parameter.ParameterParsersRegistries.defaultParsers;

public class CommandAnnotationParser {

    private final Function<Method, String> commandNameStrategy;

    private final ParameterSuppliersRegistry parameterSuppliersRegistry;

    private final ParameterParsersRegistry parameterParsersRegistry;

    public CommandAnnotationParser(Terminal terminal,
                                   Function<Method, String> commandNameStrategy,
                                   ParameterSuppliersRegistry parameterSuppliersRegistry,
                                   ParameterParsersRegistry parameterParsersRegistry) {
        this.commandNameStrategy = requireNonNullElse(commandNameStrategy,
                CommandAnnotationParser::defaultCommandName);
        if (parameterSuppliersRegistry != null) {
            this.parameterSuppliersRegistry = ParameterSuppliersRegistries.composite(
                    parameterSuppliersRegistry,
                    defaultSuppliers(terminal)
            );
        } else {
            this.parameterSuppliersRegistry = defaultSuppliers(terminal);
        }

        if (parameterParsersRegistry != null) {
            this.parameterParsersRegistry = ParameterParsersRegistries.composite(
                    parameterParsersRegistry,
                    defaultParsers()
            );
        } else {
            this.parameterParsersRegistry = defaultParsers();
        }
    }

    public void parse(Object object, CommandsRegistry registry) {
        Objects.requireNonNull(object);

        final List<CliCommands> cliCommands = collectCommandsAnnotations(object.getClass());
        ListIterator<CliCommands> iterator = cliCommands.listIterator(cliCommands.size());
        while (iterator.hasPrevious()) {
            final CliCommands annotation = iterator.previous();
                String name = trimToNull(annotation.name());
            if (name == null)
                name = trimToNull(annotation.value());
            if (name == null)
                name = object.toString().toLowerCase();

            Command currentCommand = registry.get(name);
            if (currentCommand == null) {
                currentCommand = createGroupCommand(name, annotation);
                registry.merge(currentCommand);
            }
            registry = currentCommand.getSubCommands();
        }

        final Set<Class<?>> hierarchy = classHierarchy(object.getClass());

        List<AnnotatedMethod> commandAnnotations = collectCommands(hierarchy);
        for (AnnotatedMethod am : commandAnnotations) {
            registry.merge(am.createCommand(object));
        }
    }

    private Command createGroupCommand(String name, CliCommands annotation) {
        final String description = trimToNull(annotation.description());
        final String help = trimToNull(annotation.help());
        return Command.cmd(name)
                .withDescription(description)
                .withHelp(help)
                .build();
    }

    private List<CliCommands> collectCommandsAnnotations(Class<?> aClass) {
        List<CliCommands> res = new ArrayList<>();
        Class<?> current = aClass;
        while (current != null) {
            final CliCommands annotation = current.getAnnotation(CliCommands.class);
            if (annotation != null)
                res.add(annotation);
            current = current.getEnclosingClass();
        }
        return res;
    }

    private List<AnnotatedMethod> collectCommands(Set<Class<?>> hierarchy) {
        List<AnnotatedMethod> res = new ArrayList<>();
        hierarchy.forEach(c -> collectCommands(c.getDeclaredMethods(), res));
        return res;
    }

    private Set<Class<?>> classHierarchy(Class<?> aClass) {
        Set<Class<?>> hierarchy = new LinkedHashSet<>();
        Class<?> current = aClass;
        while (current != Object.class) {
            hierarchy.add(current);
            hierarchy.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        return hierarchy;
    }

    private void collectCommands(Method[] methods, List<AnnotatedMethod> res) {
        for (Method method : methods) {
            final AnnotatedMethod am = createMethod(method);
            if (am != null)
                res.add(am);
        }
    }

    private AnnotatedMethod createMethod(Method method) {
        final CliCommand annotation = method.getAnnotation(CliCommand.class);
        if (annotation != null) {
            return new AnnotatedMethod(annotation, method);
        }
        return null;
    }

    private static String defaultCommandName(Method method) {
        final String s = method.getName();
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('-');
                c = Character.toLowerCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static <T> T newInstance(Class<T> aClass) {
        try {
            return aClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create " + aClass.getName() + " instance", e);
        }
    }

    private Optional<ParameterSupplier> getParameterSupplier(Parameter parameter, CliParameter annotation) {
        if (annotation != null && annotation.supplier() != CliParameter.DefaultParameterSupplier.class) {
            return Optional.of(newInstance(annotation.supplier()));
        }
        return parameterSuppliersRegistry.get(parameter.getType());
    }

    private ParameterParser getParameterParser(Parameter parameter, CliParameter annotation) {
        final ParameterParser delegateParser;
        if (annotation != null && annotation.parser() != CliParameter.DefaultParameterParser.class) {
            delegateParser = newInstance(annotation.parser());
        } else {
            delegateParser = parameterParsersRegistry.get(parameter.getType())
                    .orElseThrow(unsupportedParameter(parameter));
        }

        ParameterParser parser = delegateParser;
        if (annotation != null && !annotation.defaultValue().isBlank()) {
            final String defaultValue = annotation.defaultValue();
            parser = (param, commandLine) -> delegateParser
                    .parse(param, new WithDefaultCommandLine(commandLine, defaultValue));
        }
        return parser;
    }

    private Supplier<UnsupportedOperationException> unsupportedParameter(Parameter parameter) {
        return () -> new UnsupportedOperationException("Unsupported parameter " + parameter.getName() + " (" + parameter
                .getType() + ")");
    }

    private class AnnotatedMethod {

        private final CliCommand annotation;
        private final Method method;

        private AnnotatedMethod(CliCommand annotation, Method method) {
            this.annotation = annotation;
            this.method = method;
        }

        public Command createCommand(Object instance) {
            String name = getName();
            final String[] parts = name.split("\\.");
            String cmdName = parts[parts.length - 1];

            final CommandBuilder builder = Command.cmd(cmdName);
            builder.withOtherNames(annotation.otherNames())
                    .withDescription(trimToNull(annotation.description()))
                    .withHelp(trimToNull(annotation.help()));
            if (annotation.completerFactory() != CliCommand.DefaultCompleterFactory.class) {
                builder.withCompleterFactory(newInstance(annotation.completerFactory()));
            }

            ParameterSupplier[] suppliers = new ParameterSupplier[method.getParameterCount()];
            final Parameter[] parameters = method.getParameters();
            List<CommandParameter> commandParameters = new ArrayList<>();
            for (int methodParameterIndex = 0; methodParameterIndex < parameters.length; methodParameterIndex++) {
                Parameter parameter = parameters[methodParameterIndex];
                final CliParameter parameterAnnotation = parameter.getAnnotation(CliParameter.class);
                ParameterSupplier supplier = getParameterSupplier(parameter, parameterAnnotation).orElse(null);
                if (supplier == null) {
                    CommandParameter commandParameter = createCommandParameter(commandParameters
                            .size(), parameter, parameterAnnotation);
                    commandParameters.add(commandParameter);
                    supplier = parsedParameterSupplier(commandParameter);
                }
                suppliers[methodParameterIndex] = supplier;
            }
            builder.withParameters(commandParameters)
                    .withAction(new MethodCommandAction(instance,
                            method,
                            suppliers));
            final Command command = builder.build();

            Command res = command;
            CommandsRegistry currentRegistry = null;
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                Command current = Command.cmd(part).build();
                if (currentRegistry != null) {
                    currentRegistry.merge(current);
                } else {
                    res = current;
                }
                currentRegistry = current.getSubCommands();
            }
            if (currentRegistry != null)
                currentRegistry.merge(command);
            return res;
        }

        private ParameterSupplier parsedParameterSupplier(CommandParameter commandParameter) {
            return (command, parsedParameters) -> parsedParameters.get(commandParameter.getIndex()).getParsedValue();
        }

        private CommandParameter createCommandParameter(int index, Parameter parameter, CliParameter annotation) {
            ParameterParser parameterParser = getParameterParser(parameter, annotation);
            return CommandParameter.builder(index)
                    .withParser(parameterParser)
                    .withCompleter(createParameterCompleter(parameter, annotation))
                    .withName(parameter.isNamePresent() ? parameter.getName() : null)
                    .withDescription(getParameterDescription(annotation))
                    .build();
        }

        private Completer createParameterCompleter(Parameter parameter, CliParameter annotation) {
            if (annotation != null) {
                final ParameterCompleter completer = annotation.completer();
                if (completer.factory() != DefaultParameterCompleter.class) {
                    return newInstance(completer.factory()).get();
                }

                if (completer.value().length > 0) {
                    return new StringsCompleter(completer.value());
                }
            }
            return createParameterCompleter(parameter.getType());
        }

        private Completer createParameterCompleter(Class<?> type) {
            Completer completer;
            if (type.isEnum()) {
                final Object[] enumConstants = type.getEnumConstants();
                completer = new StringsCompleter(Arrays.stream(enumConstants)
                        .map(o -> ((Enum<?>) o).name())
                        .toArray(String[]::new));
            } else if (File.class.isAssignableFrom(type) || Path.class.isAssignableFrom(type)) {
                completer = new Completers.FileNameCompleter();
            } else if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE.isAssignableFrom(type)) {
                completer = new StringsCompleter(Boolean.TRUE.toString(), Boolean.FALSE.toString());
            } else {
                completer = PassthroughCompleter.INSTANCE;
            }

            if (!type.isPrimitive()) {
                completer = new NullableCompleter(completer);
            }

            return completer;
        }

        private String getParameterDescription(CliParameter annotation) {
            return annotation == null ? null : trimToNull(annotation.description());
        }

        private String getName() {
            if (annotation.value().isBlank() && annotation.name().isBlank())
                return commandNameStrategy.apply(method);
            if (annotation.name().isBlank())
                return annotation.value();
            return annotation.name();
        }

    }

}
