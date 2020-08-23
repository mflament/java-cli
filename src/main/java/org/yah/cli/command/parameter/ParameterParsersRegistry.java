package org.yah.cli.command.parameter;

import java.util.Optional;

public interface ParameterParsersRegistry {

    Optional<ParameterParser> get(Class<?> type);

}
