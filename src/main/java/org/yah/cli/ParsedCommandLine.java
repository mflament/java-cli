package org.yah.cli;

import org.yah.cli.parser.MappedParsedLine;
import org.yah.cli.parser.MappedParsedLine.MappedWord;

import java.util.Iterator;

public class ParsedCommandLine implements CommandLine {
    private final MappedParsedLine parsedLine;
    private final Iterator<MappedWord> wordIterator;
    private MappedWord nextWord;
    private boolean flushed;

    public ParsedCommandLine(MappedParsedLine parsedLine) {
        this.parsedLine = parsedLine;
        this.wordIterator = parsedLine.mappedWords().iterator();
    }

    @Override
    public boolean hasNext() {
        if (nextWord != null)
            return true;
        return !flushed && wordIterator.hasNext();
    }

    @Override
    public String next() {
        if (!hasNext())
            return null;
        MappedWord word = nextWord != null ? nextWord : wordIterator.next();
        nextWord = null;
        return word.getWord();
    }

    @Override
    public String peek() {
        if (!hasNext())
            return null;
        if (nextWord == null)
            nextWord = wordIterator.next();
        return nextWord.getWord();
    }

    @Override
    public String flush() {
        flushed = true;
        if (nextWord == null && hasNext())
            nextWord = wordIterator.next();
        if (nextWord != null) {
            final String rawLine = parsedLine.line();
            return rawLine.substring(nextWord.getRawWordOffset());
        }
        return "";
    }

    @Override
    public String toString() {
        return parsedLine.line();
    }
}
