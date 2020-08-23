package org.yah.cli.command.annotation;

import java.util.function.Supplier;

public @interface ParameterCompleter {

    /**
     * @return values proposal for this parameter
     */
    String[] value() default {};

    /**
     * @return a Completer factory class (must have a public not arg constructor)
     */
    Class<? extends Supplier<org.jline.reader.Completer>> factory() default DefaultParameterCompleter.class;

    final class DefaultParameterCompleter implements Supplier<org.jline.reader.Completer> {
        @Override
        public org.jline.reader.Completer get() {
            throw new UnsupportedOperationException();
        }
    }

}
