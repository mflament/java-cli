package org.yah.cli.command.parameter;

public class ParsedParameter {
    private final CommandParameter parameter;
    private final Object parsedValue;

    ParsedParameter(CommandParameter parameter, Object parsedValue) {
        this.parameter = parameter;
        this.parsedValue = parsedValue;
    }

    public CommandParameter getParameter() {
        return parameter;
    }

    public Object getParsedValue() {
        return parsedValue;
    }

    @Override
    public String toString() {
        return "ParsedParameter{" +
                "parameter=" + parameter +
                ", parsedValue=" + parsedValue +
                '}';
    }
}
