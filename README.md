# Collection of [Apache Lucene](http://lucene.apache.org) tokenizers, filters and analyzers

This library is collection of my lucene components which I use in projects

### [IdentifierNGramFilter](src/main/java/eu/hlavki/lucene/analysis/identifier/IdentifierNGramFilter.java)
[IdentifierNGramFilter](src/main/java/eu/hlavki/lucene/analysis/identifier/IdentifierNGramFilter.java) tokenizes the input into n-grams delimited by punctation. N-grams are units of
various length. It differs from lucene's [NGramTokenFilter](http://lucene.apache.org/core/5_4_0/analyzers-common/org/apache/lucene/analysis/ngram/NGramTokenFilter.html) where n-grams are fixed-length tokens. 
Punctation is defined in [IdentifierTokenizer's](src/main/java/eu/hlavki/lucene/analysis/identifier/IdentifierTokenizer.java) jflex grammar ([IdentifierTokenizerImpl.jflex](src/main/jflex/IdentifierTokenizerImpl.jflex)) 
and can be included or excluded from ngrams. You can also define minimum and maximum length.

This filter is mostly used in index time.

You can use it in highlighting because it modifies offset and sorts n-grams by their offset in the original token first, then
increasing length (meaning that "192.168.1" will give "192", "192.168", "192.168.1", "168", "168.1", "1").

For more examples see [IdentifierNGramFilterTest](src/test/java/eu/hlavki/lucene/analysis/identifier/IdentifierNGramFilterTest.java)

### [IdentifierFilter](src/main/java/eu/hlavki/lucene/analysis/identifier/IdentifierFilter.java)

Use this filter in query time to fields that use index time filter IdentifierNGramFilter.
