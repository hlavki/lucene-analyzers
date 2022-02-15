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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class IdentifierFilterTest {

    public IdentifierFilterTest() {
    }


    @Test
    public void customDelimiter() {
        Term term = analyze("I. ÚS 22/2015", '.');
        assertNotNull(term);
        assertThat(term.getTerm(), is("i.us.22.2015"));
    }


    @Test
    public void includeDelimiters() {
        Term term = analyze("I. ÚS 22/2015");
        assertNotNull(term);
        assertThat(term.getTerm(), is("i.us22/2015"));
    }


    private static Term analyze(final String text) {
        return analyze(text, IdentifierFilter.EMPTY_CHAR);
    }


    private static Term analyze(final String text, final char customDelimiter) {
        final List<Term> result = new ArrayList<>();
        Analyzer analyzer = new Analyzer() {
            @Override
            protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
                final Tokenizer src = new PunctationTokenizer();
                TokenStream tok = new IdentifierFilter(src, customDelimiter);
                tok = new ASCIIFoldingFilter(tok);
                tok = new LowerCaseFilter(tok);
                return new Analyzer.TokenStreamComponents(src, tok);
            }
        };
        try ( TokenStream stream = analyzer.tokenStream(null, new StringReader(text))) { // získaj inštanciu prúdu tokenov
            stream.reset();
            // iteruj cez tokeny
            while (stream.incrementToken()) {
                final CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
                final OffsetAttribute offsetAtt = stream.getAttribute(OffsetAttribute.class);
                if (termAtt.length() > 0) {
                    result.add(new Term(termAtt.toString(), offsetAtt.startOffset(), offsetAtt.endOffset()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.size() == 1 ? result.get(0) : null;
    }
}
