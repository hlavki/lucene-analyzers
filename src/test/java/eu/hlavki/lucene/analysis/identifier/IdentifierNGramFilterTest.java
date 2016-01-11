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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michal Hlavac
 */
public class IdentifierNGramFilterTest {

    public IdentifierNGramFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void courtFileId() {
        assertThat(analyze("I. ÚS 22/2015", 3, 3, true, false).size(), is(3));
        assertThat(analyze("I. ÚS 22/2015", 3, 3, false, false).size(), is(2));
        assertThat(analyze("I. ÚS 22/2015", 2, 5, true, false).size(), is(6));
        assertThat(analyze("I. ÚS 22/2015", 3, 8, true, false).size(), is(3));
        assertThat(analyze("I. ÚS 22/2015", 2, 4, true, false).size(), is(analyze("I. ÚS 22/2015", 2, 4, false, false).size()));
        assertThat(analyze("I. ÚS 22/2015", 1, 2, false, false).size(), is(7));
        assertThat(analyze("I. ÚS 22/2015", 1, 1, false, false).size(), is(4));
        assertThat(analyze("I. ÚS 22/2015", 1, 1, true, false).size(), is(5));
    }

    @Test
    public void noDelimiter() {
        System.out.println(analyze("I. ÚS 22/2015", 3, 3, false, false));
        System.out.println(analyze("I. ÚS 22/2015", 3, 3, false, true));
        System.out.println(analyze("I. ÚS 22/2015", 4, 4, true, true));
    }

    @Test
    public void ecli() {
        assertThat(analyze("ECLI:SK:USSR:2015:1.US.14.2015.1", 3, 3, true, false).size(), is(8));
        assertThat(analyze("ECLI:SK:USSR:2015:1.US.14.2015.1", 2, 2, false, true).size(), is(8));

        List<Term> terms = analyze("ECLI:SK:USSR:2015:1.US.14.2015.1", 3, 8, false, false);
        Set<Term> termsSet = new HashSet<>(terms);
        assertThat(terms.size(), is(termsSet.size()));
        assertThat(analyze("ECLI:SK:USSR:2015:1.US.14.2015.1", 3, 8, false, false).size(), is(27));
        assertThat(analyze("ECLI:SK:USSR:2015:1.US.14.2015.1", 1, 1, false, true).size(), is(9));
        
        System.out.println(analyze("ECLI:SK:USSR:2015:1.US.14.2015.1", 1, 1, true, false));
//        assertThat(analyze("ECLI:SK:USSR:2015:1.US.14.2015.1", 1, 1, true, true).size(), is(1));
    }

    private static List<Term> analyze(final String text, final int minNGramSize, final int maxNGramSize,
            final boolean includeIdentifier, final boolean ignoreDelimiter) {
        final List<Term> result = new ArrayList<>();
        Analyzer analyzer = new Analyzer() {
            @Override
            protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
                final Tokenizer src = new IdentifierTokenizer();
                TokenStream tok = new IdentifierNGramFilter(src, minNGramSize, maxNGramSize, includeIdentifier, ignoreDelimiter);
                tok = new ASCIIFoldingFilter(tok);
                tok = new LowerCaseFilter(tok);
                return new Analyzer.TokenStreamComponents(src, tok);
            }
        };
        try (TokenStream stream = analyzer.tokenStream(null, new StringReader(text))) { // získaj inštanciu prúdu tokenov
            stream.reset();
            // iteruj cez tokeny
            while (stream.incrementToken()) {
                final CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
                final OffsetAttribute offsetAtt = stream.getAttribute(OffsetAttribute.class);
                final TypeAttribute typeAtt = stream.getAttribute(TypeAttribute.class);
                if (termAtt.length() > 0) {
                    result.add(new Term(termAtt.toString(), offsetAtt.startOffset(), offsetAtt.endOffset()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
