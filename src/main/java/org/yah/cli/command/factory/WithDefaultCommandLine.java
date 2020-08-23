package org.yah.cli.command.factory;

import org.apache.commons.lang3.StringUtils;
import org.yah.cli.CommandLine;

public class WithDefaultCommandLine implements CommandLine {
    private final CommandLine delegate;
    private final String defaultValue;
    private boolean fetched;

    public WithDefaultCommandLine(CommandLine delegate, String defaultValue) {
        this.delegate = delegate;
        this.defaultValue = defaultValue;
    }

    @Override
    public String peek() {
        return orDefault(delegate.peek());
    }

    @Override
    public String flush() {
        fetched = true;
        return orDefault(delegate.flush());
    }

    @Override
    public boolean hasNext() {
        if (fetched)
            return delegate.hasNext();
        return true;
    }

    @Override
    public String next() {
        final String res = orDefault(delegate.next());
        fetched = true;
        return res;
    }

    private String orDefault(String s) {
        if (fetched) return s;
        if (StringUtils.trimToNull(s) == null)
            return defaultValue;
        return s;
    }

}
