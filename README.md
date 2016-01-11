# Collection of Apache Lucene tokenizers, filters and analyzers

This library is collection of my lucene components which I use in projects

### IdentifierNGramFilter
IdentifierNGramFilter tokenizes the input into n-grams delimited by punctation. N-grams are units of
various length. It differs from lucene's NGramTokenFilter where n-grams are fixed-length tokens. 
Punctation is defined in IdentifierTokenizer's jflex grammar (IdentifierTokenizerImpl.jflex) 
and can be included or excluded from ngrams. You can also define minimum and maximum length.

This filter is mostly used in index time.

You can use it in highlighting because it modifies offset and sorts n-grams by their offset in the original token first, then
increasing length (meaning that "192.168.1" will give "192", "192.168", "192.168.1", "168", "168.1", "1").

### IdentifierFilter

Use this filter in query time to fields that use index time filter IdentifierNGramFilter.
