package org.yah.cli;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class PassthroughCompleter implements Completer {

    public static final Completer INSTANCE = new PassthroughCompleter();

    private PassthroughCompleter() {
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        final String word = line.word();
        candidates.add(new Candidate(word));
    }
}
