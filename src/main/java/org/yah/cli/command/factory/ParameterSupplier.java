package org.yah.cli.command.factory;

import org.yah.cli.command.Command;
import org.yah.cli.command.parameter.ParsedParameter;

import java.util.List;

public interface ParameterSupplier {

    Object get(Command command, List<ParsedParameter> parsedParameters);

}
