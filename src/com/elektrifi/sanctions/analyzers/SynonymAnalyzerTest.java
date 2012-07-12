package com.elektrifi.sanctions.analyzers;

import java.io.StringReader;

import junit.framework.TestCase;

import com.elektrifi.sanctions.analyzers.TestUtil;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

// From chapter 4
public class SynonymAnalyzerTest extends TestCase {
	
  private IndexSearcher searcher;
  private static SynonymAnalyzer synonymAnalyzer =
                      new SynonymAnalyzer(new SanctionsSynonymEngine());

  public void setUp() throws Exception {
	  
    RAMDirectory directory = new RAMDirectory();

    IndexWriter writer = new IndexWriter(directory,
                                         synonymAnalyzer,  //#1  
                                         IndexWriter.MaxFieldLength.UNLIMITED);
    Document doc = new Document();
    doc.add(new Field("content",
                      "robert gabriel mugabe",
                      Field.Store.YES,
                      Field.Index.ANALYZED));  //#2
    writer.addDocument(doc);
                                  
    writer.close();

    searcher = new IndexSearcher(directory, true);
  }

  public void tearDown() throws Exception {
    searcher.close();
  }

  public void testJumps() throws Exception {
    TokenStream stream =
      synonymAnalyzer.tokenStream("contents",                   // #A
                                  new StringReader("robert mugabe"));   // #A
    
    TermAttribute term = stream.addAttribute(TermAttribute.class);
    PositionIncrementAttribute posIncr 
    	= stream.addAttribute(PositionIncrementAttribute.class);

    int i = 0;
    String[] expected = new String[]{"robert",          // #B    		
            						 "roberta",         // #B
    								 "robbie",          // #B                                     
                                     "robby",           // #B                                                                          
                                     "bobbie",          // #B
                                     "bobby",           // #B
                                     "rabby",           // #B
                                     "rabbie",          // #B                                     
                                     "rab",           	// #B                                     
                                     "rob"};                                     
    
    while(stream.incrementToken()) {
      assertEquals(expected[i], term.term());

      int expectedPos;      // #C
      if (i == 0) {         // #C
        expectedPos = 1;    // #C
      } else {              // #C
        expectedPos = 0;    // #C
      }                     // #C
      assertEquals(expectedPos,                      // #C
                   posIncr.getPositionIncrement());  // #C
      i++;
    }
    //assertEquals(9, i);
  }

  /*
    #A Analyze with SynonymAnalyzer
    #B Check for correct synonyms
    #C Verify synonyms positions
  */

  public void testSearchByAPI() throws Exception {

    TermQuery tq = new TermQuery(new Term("content", "robert"));  //#1
    assertEquals(1, TestUtil.hitCount(searcher, tq));

    PhraseQuery pq = new PhraseQuery();    //#2
    pq.add(new Term("content", "robbie"));    //#2
    pq.add(new Term("content", "gab"));   //#2
    assertEquals(1, TestUtil.hitCount(searcher, pq));
  }

  /*
    #1 Search for "hops"
    #2 Search for "fox hops"
  */

  public void testWithQueryParser() throws Exception {
    Query query = new QueryParser(Version.LUCENE_30,                   // 1
                                  "content",                                // 1
                                  synonymAnalyzer).parse("\"robert gabriel mugabe\"");  // 1
    assertEquals(1, TestUtil.hitCount(searcher, query));                   // 1
    System.out.println("With SynonymAnalyzer, \"robert gabriel mugabe\" parses to " +
                                         query.toString("content"));

    System.out.println("\n");
    
    query = new QueryParser(Version.LUCENE_30,                         // 2
                            "content",                                      // 2
                            new StandardAnalyzer(Version.LUCENE_30)).parse("\"robert gabriel mugabe\""); // B
    assertEquals(1, TestUtil.hitCount(searcher, query));                   // 2
    System.out.println("With StandardAnalyzer, \"robert gabriel mugabe\" parses to " +
                                         query.toString("content"));
  }

  /*
    #1 SynonymAnalyzer finds the document
    #2 StandardAnalyzer also finds document
  */
}
