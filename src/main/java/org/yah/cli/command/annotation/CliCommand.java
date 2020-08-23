package org.yah.cli.command.annotation;

import org.yah.cli.command.Command.CompleterFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CliCommand {
    /**
     * @return name alias
     */
    String value() default "";

    /**
     * @return command qualified name
     */
    String name() default "";

    /**
     * @return other command qualified name
     */
    String[] otherNames() default {};

    /**
     * @return command description
     */
    String description() default "";

    /**
     * @return command help
     */
    String help() default "";

    /**
     * @return a factory to create command "node" completers (TODO: add doc on command node)
     */
    Class<? extends CompleterFactory> completerFactory() default DefaultCompleterFactory.class;

    class DefaultCompleterFactory implements CompleterFactory {
        @Override
        public void create(List<Object> nodes) {
            throw new UnsupportedOperationException();
        }
    }
}
