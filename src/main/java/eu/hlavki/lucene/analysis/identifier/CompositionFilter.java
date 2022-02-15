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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.apache.lucene.analysis.TokenFilter;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeImpl;

public abstract class CompositionFilter extends TokenFilter {

    private static final String DEFAULT_WHITESPACE = " ";

    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    protected final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private final PackedTokenAttributeImpl compositionTermAtt = new PackedTokenAttributeImpl();
    private State previous;
    private final Deque<State> rollbackStack;
    private final Deque<PackedTokenAttributeImpl> tokenStack;
    private final List<PackedTokenAttributeImpl> compositionTerms;
    private boolean rollback;


    public CompositionFilter(TokenStream input) {
        super(input);
        rollbackStack = new ArrayDeque<>();
        tokenStack = new ArrayDeque<>();
        compositionTerms = new ArrayList<>();
    }


    @Override
    public final boolean incrementToken() throws IOException {
        boolean read;
        if (!tokenStack.isEmpty()) {
            tokenStack.pop().copyTo((AttributeImpl) termAtt);
            return true;
        } else if (previous == null && rollback) {
            if (!rollbackStack.isEmpty()) {
                restoreState(rollbackStack.pop());
                return true;
            } else {
                rollback = false;
            }
        } else {
            rollbackStack.clear();
        }
        if (previous != null) {
            restoreState(previous);
            previous = null;
            read = true;
        } else {
            read = input.incrementToken();
        }

        boolean isValidComposition = false;
        CState lastState = null;
        do {
            if (read) {
                rollbackStack.add(captureState());
                lastState = validateToken(termAtt.toString());
                if (lastState == CState.CONTINUE) {
                    appendComposition(termAtt, offsetAtt);
                } else if (lastState == CState.FINISH_INVALID) {
                    setPrevious();
                    markComposition();
                    isValidComposition = true;
                    break;
                } else if (lastState == CState.FINISH_VALID) {
                    appendComposition(termAtt, offsetAtt);
                    markComposition();
                    isValidComposition = true;
                    break;
                } else if (lastState == CState.ROLLBACK) {
                    // TODO: rollback all from stack;
                    setRollback();
                    break;
                }
            } else {
                break;
            }
        } while ((read = input.incrementToken()) == true);

        if (!read) {
            if (isValidComposition) {
                markComposition();
                read = true;
            } else {
                CState state = validateFinish(lastState);
                if (state != null || state != lastState) {
                    if (state == CState.ROLLBACK) {
                        read = setRollback();
                    } else {
                        markComposition();
                        read = true;
                    }
                }
            }
        }
        return read;
    }


    protected PackedTokenAttributeImpl getComposition() {
        return compositionTermAtt;
    }


    public List<PackedTokenAttributeImpl> getCompositionTerms() {
        return compositionTerms;
    }


    private boolean setRollback() {
        this.rollback = rollbackStack.size() > 1;
        boolean result = false;
        clear();
        if (rollback) {
            restoreState(rollbackStack.pop());
            result = true;
        }
        return result;
    }


    private void markComposition() {
        tokenStack.pop().copyTo((AttributeImpl) termAtt);
        clear();
    }


    private void setPrevious() {
        previous = captureState();
    }


    @Override
    public final void reset() throws IOException {
        super.reset();
        clear();
        rollbackStack.clear();
        tokenStack.clear();
    }


    protected void addToken(String token, int startOffset, int endOffset) {
        addToken(token, startOffset, endOffset, "word");
    }


    protected void addToken(PackedTokenAttributeImpl token) {
        tokenStack.add(token);
    }


    protected void addTokens(Collection<PackedTokenAttributeImpl> tokens) {
        tokenStack.addAll(tokens);
    }


    protected void addToken(String token, int startOffset, int endOffset, String type) {
        PackedTokenAttributeImpl attr = new PackedTokenAttributeImpl();
        attr.append(token);
        attr.setOffset(startOffset, endOffset);
        attr.setType(type);
        tokenStack.add(attr);
    }


    protected void clear() {
        compositionTermAtt.clear();
        compositionTerms.clear();
    }


    private void appendComposition(CharTermAttribute source, OffsetAttribute offsetAtt) {
        if (compositionTermAtt.length() > 0) {
            compositionTermAtt.append(getWhitespace());
        }
        // pridaj atribúty do kompozície
        int startOffset = compositionTermAtt.length() == 0 ? offsetAtt.startOffset() : compositionTermAtt.startOffset();
        compositionTermAtt.append(source);
        compositionTermAtt.setOffset(startOffset, offsetAtt.endOffset());

        PackedTokenAttributeImpl newAttr = new PackedTokenAttributeImpl();
        ((PackedTokenAttributeImpl) source).copyTo(newAttr);
        compositionTerms.add(newAttr);
    }


    protected abstract CState validateToken(String token);


    protected CState validateFinish(CState previousState) {
        return previousState;
    }


    /**
     * Get default whitespace character that separate tokens in private
     * {@link #appendComposition(PackedTokenAttributeImpl, CharTermAttribute, OffsetAttribute)} method. You
     * can override it if you need your own.
     *
     * @return whitespace delimiter
     */
    protected String getWhitespace() {
        return DEFAULT_WHITESPACE;
    }

//    protected abstract Annotation createAnnotation(String token, int startOffset, int endOffset);
    protected enum CState {

        /**
         * State when you can continue with compositing, but annotation is still not valid
         */
        CONTINUE,
        /**
         * State when you know that composition is finished and last token is part of composition
         */
        FINISH_VALID,
        /**
         * State when you know that composition is finished and last token is not part of composition
         */
        FINISH_INVALID,
        /**
         * Rollback all composition
         */
        ROLLBACK
    }
}
