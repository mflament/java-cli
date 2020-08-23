package org.yah.cli.command.factory;

import java.util.Optional;

public interface ParameterSuppliersRegistry {

    Optional<ParameterSupplier> get(Class<?> parameterType);

}
