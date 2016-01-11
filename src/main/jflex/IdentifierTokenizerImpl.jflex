package eu.hlavki.lucene.analysis.identifier;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*

WARNING: if you change IdentifierTokenizerImpl.jflex and need to regenerate
      the tokenizer, only use the trunk version of JFlex 1.5 at the moment!

*/

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * This class implements the IdentifierTokenizer
 */
%%

%class IdentifierTokenizerImpl
%unicode 3.0
%integer
%function getNextToken
%pack
%char
%buffer 4096

%{

public static final int ALPHANUM          = IdentifierTokenizer.ALPHANUM;
public static final int PUNCTATION        = IdentifierTokenizer.PUNCTATION;

public static final String [] TOKEN_TYPES = IdentifierTokenizer.TOKEN_TYPES;

public final int yychar()
{
    return yychar;
}

/**
 * Fills CharTermAttribute with the current token text.
 */
public final void getText(CharTermAttribute t) {
  t.copyBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

   public final void setBufferSize(int numChars) {
     throw new UnsupportedOperationException();
   }
%}

// basic word: a sequence of digits & letters
ALPHANUM   = ({LETTER}|[:digit:])+

// punctuation
PUNCTATION = ("."|","|"_"|"-"|"/"|";"|":"|"|"|"+"|"!"|"@"|"#"|"$"|"%"|"^"|"&"|"*"|"("|")"|"{"|"}"|"["|"]"|"<"|">"|"?")

// From the JFlex manual: "the expression that matches everything of <a> not matched by <b> is !(!<a>|<b>)"
LETTER     = !(![:letter:])

%%

{ALPHANUM}                                                     { return ALPHANUM; }
{PUNCTATION}                                                   { return PUNCTATION; }

/** Ignore the rest */
[^]                                                            { /* Break so we don't hit fall-through warning: */ break;/* ignore */ }
