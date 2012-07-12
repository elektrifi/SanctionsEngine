package com.elektrifi.sanctions.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

import java.io.Reader;

public class SynonymAnalyzer extends Analyzer {
  
  private SynonymEngine engine;

  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
  
  private final boolean enableStopPositionIncrements = 
  	StopFilter.getEnablePositionIncrementsVersionDefault(Version.LUCENE_30);;
  
  public SynonymAnalyzer(SynonymEngine engine) {
    this.engine = engine;
  }

  /**
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new SynonymFilter(
                          new StopFilter(
                            new LowerCaseFilter(
                              new StandardFilter(
                                new StandardTokenizer(Version.LUCENE_30, reader))),
                            StandardAnalyzer.STOP_WORDS_SET),
                          engine
                         );
    return result;
  }
  **/
  
  // JF
  /** Constructs a {@link StandardTokenizer} filtered by a {@link
  StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}. */
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
	  
    StandardTokenizer tokenStream = new StandardTokenizer(Version.LUCENE_30, reader);
    tokenStream.setMaxTokenLength(DEFAULT_MAX_TOKEN_LENGTH);
    
    
    TokenStream result = new SynonymFilter(
            new StopFilter(enableStopPositionIncrements,
              new LowerCaseFilter(
                new StandardFilter(
                  new StandardTokenizer(Version.LUCENE_30, reader))),                  
              StandardAnalyzer.STOP_WORDS_SET),
            engine
           );
        
    /**
    TokenStream result = new StandardFilter(tokenStream);
    result = new LowerCaseFilter(result);
    result = new StopFilter(enableStopPositionIncrements, result, StandardAnalyzer.STOP_WORDS_SET, true);
    result = new StandardFilter(result);
    result = new StandardTokenizer(Version.LUCENE_30, reader);
    **/
    
    return result;
  }  
  
}
