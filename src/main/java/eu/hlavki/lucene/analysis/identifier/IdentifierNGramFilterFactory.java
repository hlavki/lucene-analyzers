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
package eu.hlavki.lucene.analysis.identifier;

import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 *
 * @author Michal Hlavac
 */
public class IdentifierNGramFilterFactory extends TokenFilterFactory {

    private final int minGramSize;
    private final int maxGramSize;
    private final boolean includeIdentifier;
    private final char customDelimiter;

    /**
     * Creates a new CodeFilterFactory
     *
     * @param args
     */
    public IdentifierNGramFilterFactory(Map<String, String> args) {
        super(args);
        minGramSize = getInt(args, "minGramSize", IdentifierNGramFilter.DEFAULT_MIN_NGRAM_SIZE);
        maxGramSize = getInt(args, "maxGramSize", IdentifierNGramFilter.DEFAULT_MAX_NGRAM_SIZE);
        includeIdentifier = getBoolean(args, "includeIdentifier", IdentifierNGramFilter.DEFAULT_INCLUDE_IDENTIFIER);
        customDelimiter = getChar(args, "customDelimiter", IdentifierFilter.EMPTY_CHAR);
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public IdentifierNGramFilter create(TokenStream input) {
        return new IdentifierNGramFilter(input, minGramSize, maxGramSize, includeIdentifier, customDelimiter);
    }
}
