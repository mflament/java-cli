package org.yah.cli;

import org.yah.cli.command.CommandsRegistry;
import org.yah.cli.command.annotation.CliCommand;
import org.yah.cli.command.annotation.CliCommands;
import org.yah.cli.command.annotation.CliParameter;
import org.yah.cli.command.annotation.ParameterCompleter;
import org.yah.cli.command.factory.CommandAnnotationParser;
import org.yah.cli.command.factory.ParameterSupplier;
import org.yah.cli.command.factory.ParameterSuppliersRegistry;
import org.yah.cli.command.parameter.ParameterParsers;
import org.yah.cli.command.parameter.ParameterParsersRegistry;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@CliCommands(name = "test", description = "Some test commands", help = "Just some stupid test commands to play with cli API")
public class TestSandbox implements AnnotatedCli {

    public static void main(String[] args) throws Exception {
        CliApplication.start(TestSandbox::new);
    }

    private static final ParameterSupplier currentTimeSupplier = (command, parsedParameters) -> Instant.now();

    private final AnnotatedCommands annotatedCommands = new AnnotatedCommands();

    @Override
    public void parse(CommandAnnotationParser parser, CommandsRegistry registry) {
        AnnotatedCli.super.parse(parser, registry);
        parser.parse(annotatedCommands, registry);
    }

    @Override
    public ParameterSuppliersRegistry parameterSuppliersRegistry() {
        return parameterType -> {
            if (Instant.class.isAssignableFrom(parameterType))
                return Optional.of(currentTimeSupplier);
            return Optional.empty();
        };
    }

    @Override
    public ParameterParsersRegistry parameterParserRegistry() {
        return type -> {
            if (CustomParserObject.class.isAssignableFrom(type))
                return Optional.of((parameter, commandLine) -> CustomParserObject.parse(commandLine));
            return Optional.empty();
        };
    }

    @Override
    public String name() {
        return "test";
    }

    @CliCommand
    private void noparam() {
        System.out.println("noparam");
    }

    @CliCommand(help = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
    private void noParam() {
        System.out.println("noParam");
    }

    @CliCommand(otherNames = {"multi-name1", "multi-name2"})
    private void multiName() {
        System.out.println("multiName");
    }

    @CliCommand(name = "another-name")
    private void nameOverride() {
        System.out.println("nameOverride");
    }

    @CliCommand
    private void flushed(@CliParameter(parser = ParameterParsers.FlushCommandLine.class) String param) {
        System.out.println("flushed '" + param + "'");
    }

    @CliCommand
    private void defaultParsers(String stringParam,
                                int intParam,
                                boolean booleanParam,
                                Path pathParam,
                                File file,
                                ParsableObject po,
                                CustomParserObject cpo,
                                Instant now) {
        Map<String, Object> entries = new LinkedHashMap<>();
        entries.put("stringParam", stringParam);
        entries.put("intParam", intParam);
        entries.put("booleanParam", booleanParam);
        entries.put("pathParam", pathParam);
        entries.put("file", file);
        entries.put("po", po);
        entries.put("cpo", cpo);
        entries.put("now", now);
        int maxWidth = entries.keySet().stream().mapToInt(String::length).max().orElse(0);
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            System.out.printf("%-" + maxWidth + "s: %s%n", entry.getKey(), entry.getValue());
        }
    }

    @CliCommand
    private void testBoolean(boolean b, Boolean bObj) {
        System.out.println("testBoolean(" + b + "," + bObj + ")");
    }

    @CliCommand
    private void testEnum(TestEnum testEnum) {
        System.out.println("testEnum(" + testEnum + ")");
    }

    @CliCommands("cliparams")
    public static class AnnotatedCommands {
        @CliCommand
        private void completedParam(@CliParameter(name = "theParameter", description = "The parameter descritpion", completer = @ParameterCompleter({"a", "B", "CdeF"})) String stringParam) {
            System.out.println("completedParam(" + stringParam + ")");
        }

        @CliCommand
        private void suppliedParam(@CliParameter(completer = @ParameterCompleter({"a", "b", "c"})) String p1,
                                   @CliParameter(completer = @ParameterCompleter({"d", "e", "f"})) String p2,
                                   @CliParameter(completer = @ParameterCompleter({"a", "b", "c"})) String p3) {
            System.out.println("completedParam(" + p1 + "," + p2 + "," + p3 + ")");
        }
    }

    public static class ParsableObject {
        private final String text;

        public ParsableObject(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "ParsableObject{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    public static class CustomParserObject {
        public static CustomParserObject parse(CommandLine commandLine) {
            return new CustomParserObject(commandLine.next());
        }

        private final String text;

        private CustomParserObject(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "CustomParserObject{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    @SuppressWarnings("unused")
    public enum TestEnum {
        A, B, C
    }

}
