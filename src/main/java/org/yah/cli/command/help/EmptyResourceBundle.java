package org.yah.cli.command.help;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class EmptyResourceBundle extends ResourceBundle {

    public static final ResourceBundle INSTANCE = new EmptyResourceBundle();

    @Override
    protected Object handleGetObject(@Nonnull String key) {
        throw new MissingResourceException("totally expected", getClass().getSimpleName(), key);
    }

    @Override
    @Nonnull
    public Enumeration<String> getKeys() {
        return Collections.emptyEnumeration();
    }
}
