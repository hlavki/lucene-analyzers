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

import java.util.Objects;

/**
 *
 * @author Michal Hlavac
 */
final class Term {

    private final String term;
    private final int startOffset, endOffset;

    public Term(String term, int startOffset, int endOffset) {
        this.term = term;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public String getTerm() {
        return term;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.term);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Term other = (Term) obj;
        if (!Objects.equals(this.term, other.term)) return false;
        return true;
    }

    @Override
    public String toString() {
        return term + " [" + startOffset + ", " + endOffset + "]";
    }
}
