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
public class IdentifierFilterFactory extends TokenFilterFactory {

    private final boolean ignoreDelimiter;

    /**
     * Creates a new CodeFilterFactory
     *
     * @param args
     */
    public IdentifierFilterFactory(Map<String, String> args) {
        super(args);
        ignoreDelimiter = getBoolean(args, "ignoreDelimiter", IdentifierFilter.DEFAULT_IGNORE_DELIMITER);

        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public IdentifierFilter create(TokenStream input) {
        return new IdentifierFilter(input, ignoreDelimiter);
    }
}
