package org.yah.cli.parser;

import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface MappedParsedLine extends ParsedLine {

    int[] getRawWordsLengths();

    default Collection<MappedWord> mappedWords() {
        final String line = line();
        final int[] lengths = getRawWordsLengths();
        final List<String> words = words();
        int index = 0;
        int rawWordStart = 0;
        List<MappedWord> res = new ArrayList<>();
        for (String word : words) {
            int rawWordLength = lengths[index];
            res.add(new MappedWord(word, line.substring(rawWordStart, rawWordStart + rawWordLength),rawWordStart));
            rawWordStart += rawWordLength;
            index++;
        }
        return res;
    }

    final class MappedWord {
        private final String word;
        private final String rawWord;
        private final int rawWordOffset;
        public MappedWord(String word, String rawWord, int rawWordOffset) {
            this.word = word;
            this.rawWord = rawWord;
            this.rawWordOffset = rawWordOffset;
        }

        public String getWord() {
            return word;
        }

        public String getRawWord() {
            return rawWord;
        }

        public int getRawWordOffset() {
            return rawWordOffset;
        }

        @Override
        public String toString() {
            return "MappedWord{" +
                    "word='" + word + '\'' +
                    ", rawWord='" + rawWord + '\'' +
                    ", rawWordOffset=" + rawWordOffset +
                    '}';
        }
    }
}
