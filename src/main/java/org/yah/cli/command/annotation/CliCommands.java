package org.yah.cli.command.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface CliCommands {
    /**
     * @return alias of name
     */
    String value() default  "";

    String name() default  "";

    String description() default "";

    String help() default "";

}
