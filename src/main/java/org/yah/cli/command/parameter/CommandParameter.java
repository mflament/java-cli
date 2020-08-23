package org.yah.cli.command.parameter;

import org.jline.reader.Completer;
import org.jline.reader.impl.completer.NullCompleter;
import org.yah.cli.CommandLine;

import java.util.Objects;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class CommandParameter {

    public static Builder builder(int index) {
        return new Builder(index);
    }

    public static final ParameterParser DEFAULT_PARSER = (parameter, commandLine) -> commandLine.next();

    private final int index;
    private final String name;
    private final String description;
    private final Completer completer;
    private final ParameterParser parser;

    private CommandParameter(Builder builder) {
        this.index = builder.index;
        this.name = builder.name;
        this.description = builder.description;
        this.completer = builder.completer == null ? NullCompleter.INSTANCE : builder.completer;
        this.parser = builder.parser == null ? DEFAULT_PARSER : builder.parser;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Completer getCompleter() {
        return completer;
    }

    public ParsedParameter parse(CommandLine commandLine) {
        final Object parsed = parser.parse(this, commandLine);
        return new ParsedParameter(this, parsed);
    }

    @Override
    public String toString() {
        return "CommandParameter{" +
                "index=" + index +
                ", name='" + name + '\'' +
                '}';
    }

    public static final class Builder {
        private final int index;
        private String name;
        private String description;
        private Completer completer;
        private ParameterParser parser;

        private Builder(int index) {
            this.index = index;
        }

        public Builder withName(String name) {
            name = trimToNull(name);
            this.name = name == null ? Integer.toString(index) : name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = trimToNull(description);
            return this;
        }

        public Builder withCompleter(Completer completer) {
            this.completer = completer;
            return this;
        }

        public Builder withParser(ParameterParser parser) {
            this.parser = parser;
            return this;
        }

        public Builder withParser(Function<String, Object> parser) {
            this.parser = parser == null ? null : (parameter, commandLine) -> parser.apply(commandLine.next());
            return this;
        }

        public CommandParameter build() {
            return new CommandParameter(this);
        }
    }
}
