package eu.hlavki.lucene.analysis.identifier;

/*
 * Copyright 2016 Michal Hlavac.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static eu.hlavki.lucene.analysis.identifier.IdentifierFilter.EMPTY_CHAR;
import java.io.IOException;
import java.util.*;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeImpl;

/**
 * IdentifierNGramFilter tokenizes the input into n-grams delimited by punctation. N-grams are units of
 * various length. It differs from lucene's {@link NGramTokenFilter} where n-grams are fixed-length tokens.
 * Punctation is defined in {@link IdentifierTokenizer}'s jflex grammar (IdentifierTokenizerImpl.jflex).
 *
 * You can use it in highlighting because it modifies offset and sorts n-grams by their offset in the original
 * token first, then increasing length (meaning that "192.168.1" will give "192", "192.168", "192.168.1",
 * "168", "168.1", "1").
 *
 */
public class IdentifierNGramFilter extends TokenFilter {

    public static final int DEFAULT_MIN_NGRAM_SIZE = 3;
    public static final int DEFAULT_MAX_NGRAM_SIZE = 8;
    public static final boolean DEFAULT_INCLUDE_IDENTIFIER = false;
    public static final boolean DEFAULT_IGNORE_DELIMITER = false;

    private final PackedTokenAttributeImpl compositionTermAtt = new PackedTokenAttributeImpl();
    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    protected final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private final int minGramSize, maxGramSize;
    private final boolean includeEdged;
    private final char customDelimiter;

    private final LinkedList<Item> items;
    private final Deque<PackedTokenAttributeImpl> queue;
    private int count;
    private boolean lastItem;
    private int termCount;
    private int maxSize;


    public IdentifierNGramFilter(TokenStream input, int minGramSize, int maxGramSize,
            boolean includeEdged, char customDelimiter) {
        super(input);
        this.minGramSize = minGramSize;
        this.maxGramSize = maxGramSize;
        this.includeEdged = includeEdged;
        this.customDelimiter = customDelimiter;

        count = 0;
        lastItem = false;
        termCount = 0;
        items = new LinkedList<>();
        queue = new ArrayDeque<>();
        maxSize = 0;
    }


    private boolean overwriteDelimiter() {
        return customDelimiter != EMPTY_CHAR;
    }


    @Override
    public final boolean incrementToken() throws IOException {
        boolean read;
        if (!queue.isEmpty()) {
            markComposition(queue.pop());
            read = true;
        } else {
            boolean hasDelim = false;
            do {
                read = input.incrementToken();
                if (read) {
                    boolean punctation = typeAtt.type().equals(PunctationTokenizer.TOKEN_TYPES[PunctationTokenizer.PUNCTATION]);
                    if (!punctation) {
                        count++;
                        termCount++;
                        items.add(new Item(((PackedTokenAttributeImpl) termAtt).clone()));
                    } else if (!items.isEmpty() && !overwriteDelimiter()) {
                        items.getLast().addDelimiter(((PackedTokenAttributeImpl) termAtt).clone());
                    }
                    if (!punctation || !overwriteDelimiter()) {
                        appendComposition(compositionTermAtt, termAtt, offsetAtt);
                        hasDelim = false;
                    }
                    if (!hasDelim && customDelimiter != EMPTY_CHAR) {
                        compositionTermAtt.append(customDelimiter);
                        hasDelim = true;
                    }
                }
            } while (count < maxGramSize && read);

            // remove last delimiter
            if (hasDelim && compositionTermAtt.length() > 0 && customDelimiter != EMPTY_CHAR) {
                compositionTermAtt.setLength(compositionTermAtt.length() - 1);
            }

            maxSize = items.size() > maxSize ? items.size() : maxSize;
            if (!items.isEmpty()) {
                Item[] itemArray = items.toArray(new Item[0]);
                for (int i = minGramSize; i < Math.min(maxGramSize, items.size()) + 1; i++) {
                    Item[] sub = Arrays.copyOfRange(itemArray, 0, i);
                    PackedTokenAttributeImpl attr = createComposition(sub);
                    queue.add(attr);
                }
                items.removeFirst();
                count--;
                if (!queue.isEmpty()) {
                    markComposition(queue.pop());
                    read = true;
                }
            }
        }

        if (includeEdged && !read && !lastItem && (termCount > maxGramSize || termCount < minGramSize)) {
            markComposition(compositionTermAtt);
            read = true;
            lastItem = true;
        }

        return read;
    }


    private void markComposition(PackedTokenAttributeImpl compositionTermAtt) {
        compositionTermAtt.setType(PunctationTokenizer.TOKEN_TYPES[PunctationTokenizer.ALPHANUM]);
        ((AttributeImpl) compositionTermAtt).copyTo((AttributeImpl) termAtt);
    }


    @Override
    public void reset() throws IOException {
        super.reset();
        count = 0;
        lastItem = false;
        termCount = 0;
        maxSize = 0;
        items.clear();
        queue.clear();
        compositionTermAtt.clear();
    }


    private PackedTokenAttributeImpl createComposition(Item[] items) {
        PackedTokenAttributeImpl result = new PackedTokenAttributeImpl();
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            appendComposition(result, item.attr, item.attr);
            if (i < items.length - 1) {
                for (PackedTokenAttributeImpl delim : item.delimiters) {
                    appendComposition(result, delim, delim);
                }
                if (customDelimiter != EMPTY_CHAR) {
                    result.append(customDelimiter);
                }
            }
        }
        return result;
    }


    private void appendComposition(PackedTokenAttributeImpl target, CharTermAttribute source, OffsetAttribute offsetAtt) {
        // pridaj atribúty do kompozície
        int startOffset = target.length() == 0 ? offsetAtt.startOffset() : target.startOffset();
        target.append(source);
        target.setOffset(startOffset, offsetAtt.endOffset());
    }

    private static class Item {

        private final PackedTokenAttributeImpl attr;
        private final List<PackedTokenAttributeImpl> delimiters;


        public Item(PackedTokenAttributeImpl attr) {
            this.attr = attr;
            delimiters = new ArrayList<>();
        }


        public void addDelimiter(PackedTokenAttributeImpl delimiter) {
            delimiters.add(delimiter);
        }


        public PackedTokenAttributeImpl getAttr() {
            return attr;
        }


        public List<PackedTokenAttributeImpl> getDelimiters() {
            return delimiters;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(attr);
            for (PackedTokenAttributeImpl delim : delimiters) {
                sb.append(delim);
            }
            return sb.toString();
        }
    }
}
