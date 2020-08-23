package org.yah.cli.command.factory;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class NullableCompleter implements Completer {
    private final Completer delegate;

    public NullableCompleter(Completer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        delegate.complete(reader, line, candidates);
        candidates.add(new Candidate("null"));
    }
}
