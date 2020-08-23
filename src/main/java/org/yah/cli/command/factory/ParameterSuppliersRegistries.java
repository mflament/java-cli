package org.yah.cli.command.factory;

import org.jline.terminal.Terminal;
import org.yah.cli.command.Command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public final class ParameterSuppliersRegistries {

    public static ParameterSuppliersRegistry defaultSuppliers(Terminal terminal) {
        return parameterType -> {
            ParameterSupplier supplier = null;
            if (Command.class.isAssignableFrom(parameterType)) {
                supplier = (command, parameters) -> command;
            }
            if (Terminal.class.isAssignableFrom(parameterType)) {
                supplier = (command, parameters) -> terminal;
            }
            return Optional.ofNullable(supplier);
        };
    }

    public static ParameterSuppliersRegistry composite(ParameterSuppliersRegistry... registries) {
        return composite(Arrays.asList(registries));
    }

    public static ParameterSuppliersRegistry composite(Collection<ParameterSuppliersRegistry> registries) {
        return parameterType -> registries.stream()
                                          .map(r -> r.get(parameterType))
                                          .filter(Optional::isPresent)
                                          .map(Optional::get)
                                          .findFirst();
    }

    private ParameterSuppliersRegistries() {
    }

}
