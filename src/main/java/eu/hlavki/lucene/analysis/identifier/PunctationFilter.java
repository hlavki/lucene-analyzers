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
package eu.hlavki.lucene.analysis.identifier;

import static eu.hlavki.lucene.analysis.identifier.PunctationTokenizer.PUNCTATION;
import static eu.hlavki.lucene.analysis.identifier.PunctationTokenizer.TOKEN_TYPES;
import java.io.IOException;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class PunctationFilter extends FilteringTokenFilter {

    protected final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);


    public PunctationFilter(TokenStream input) {
        super(input);
    }


    @Override
    protected boolean accept() throws IOException {
        return !typeAtt.type().equals(TOKEN_TYPES[PUNCTATION]);
    }
}
