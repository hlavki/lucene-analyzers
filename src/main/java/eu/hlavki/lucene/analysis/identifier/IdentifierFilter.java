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
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeImpl;

/**
 *
 * @author Michal Hlavac
 */
public class IdentifierFilter extends TokenFilter {

    public static final boolean DEFAULT_IGNORE_DELIMITER = false;
    public static final char EMPTY_CHAR = 0x0;
    private final PackedTokenAttributeImpl compositionTermAtt = new PackedTokenAttributeImpl();
    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    protected final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private boolean finished;
    private final char customDelimiter;


    protected IdentifierFilter(TokenStream input, char customDelimiter) {
        super(input);
        finished = false;
        this.customDelimiter = customDelimiter;
    }


    public IdentifierFilter(TokenStream input) {
        this(input, EMPTY_CHAR);
    }


    private boolean overwriteDelimiter() {
        return customDelimiter != EMPTY_CHAR;
    }


    @Override
    public final boolean incrementToken() throws IOException {
        boolean read, hasDelim = false;
        while (read = input.incrementToken()) {
            boolean punctation = typeAtt.type().equals(IdentifierTokenizer.TOKEN_TYPES[IdentifierTokenizer.PUNCTATION]);
            if (!punctation || !overwriteDelimiter()) {
                appendComposition(compositionTermAtt, termAtt, offsetAtt);
                hasDelim = false;
            }
            if (!hasDelim && customDelimiter != EMPTY_CHAR) {
                compositionTermAtt.append(customDelimiter);
                hasDelim = true;
            }
        }

        // remove last delimiter
        if (hasDelim && compositionTermAtt.length() > 0 && customDelimiter != EMPTY_CHAR) {
            compositionTermAtt.setLength(compositionTermAtt.length() - 1);
        }

        if (!finished) {
            markComposition();
            finished = read = true;
        }
        return read;
    }


    @Override
    public void reset() throws IOException {
        super.reset();
        finished = false;
        compositionTermAtt.clear();
    }


    private void markComposition() {
        compositionTermAtt.setType(IdentifierTokenizer.TOKEN_TYPES[IdentifierTokenizer.ALPHANUM]);
        ((AttributeImpl) compositionTermAtt).copyTo((AttributeImpl) termAtt);
        compositionTermAtt.clear();
    }


    private void appendComposition(PackedTokenAttributeImpl target, CharTermAttribute source, OffsetAttribute offsetAtt) {
        // pridaj atribúty do kompozície
        int startOffset = target.length() == 0 ? offsetAtt.startOffset() : target.startOffset();
        target.append(source);
        target.setOffset(startOffset, offsetAtt.endOffset());
    }
}
