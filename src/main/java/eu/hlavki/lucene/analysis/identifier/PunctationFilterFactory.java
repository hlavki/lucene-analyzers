package eu.hlavki.lucene.analysis.identifier;

/*
 * Copyright 2022 hlavki.
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
import java.util.Map;
import org.apache.lucene.analysis.TokenFilterFactory;
import org.apache.lucene.analysis.TokenStream;

public class PunctationFilterFactory extends TokenFilterFactory {

    /**
     * Creates a new CodeFilterFactory
     *
     * @param args
     */
    public PunctationFilterFactory(Map<String, String> args) {
        super(args);
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }


    @Override
    public PunctationFilter create(TokenStream input) {
        return new PunctationFilter(input);
    }
}
