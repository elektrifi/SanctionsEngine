package com.elektrifi.sanctions.engine;

import org.apache.log4j.Logger;
//import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tempuri.sdnlist.SdnList;
import org.tempuri.sdnlist.SdnList.SdnEntry;

import com.elektrifi.util.ApplicationConfig;
import com.elektrifi.sanctions.beans.ResultsCarrierBean;
import com.elektrifi.sanctions.analyzers.SanctionsSynonymEngine;
import com.elektrifi.sanctions.analyzers.SynonymAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SdnSearcher
{
	// Singleton pattern needed (TBC)
	private static SdnSearcher sdnSearcher;
	
	// unmarshalledSdn contains SDN data in POJOs (UnmarshalSdn uses a singleton pattern too)
	private UnmarshalSdn unmarshalledSdn = UnmarshalSdn.instance();  
	
	// Status of SDN load (defaults to false)
	private boolean sdnLoadStatus = false; 

	// Set up log4j logger
	private static 	Logger logger 			= Logger.getLogger(SdnSearcher.class);
	
	// Location of Lucene standard and synonym indices
	private static 	String standardIndexDir = null;		
	private static 	String synonymIndexDir 	= null;
		
	// ResultsBeanList
	//private ResultsBeanList resultsBeanList = ResultsBeanList.getResultsBeanListSingleton();
	
	// Custom stopwords
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<String> sdnStopSet			= new HashSet(Arrays.asList(StandardAnalyzer.STOP_WORDS_SET));	
		
	private SdnSearcher() { //Constructor

		// Read in properties (location of SDN file) - OLD WAY
		//ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		//indexDir = config.getProperty("SdnIndexDirectory");   
		
		// NEW WAY 
		String sdnStopwordFile = null;
		try {
			InputStream inputStream = this.getClass().getClassLoader()
										.getResourceAsStream("config.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			
			standardIndexDir = properties.getProperty("SdnStandardIndexDirectory");
			logger.info("SdnIndexDirectory read in as " + standardIndexDir);
			
			synonymIndexDir = properties.getProperty("SdnSynonymIndexDirectory");
			logger.info("SdnIndexDirectory read in as " + synonymIndexDir);
						
			sdnStopwordFile = properties.getProperty("StopwordFile");
			logger.info("StopwordFile read in as " + sdnStopwordFile);
			inputStream.close();			
		} catch(IOException ioe) {
			logger.error("Caught IOException: ");
			ioe.printStackTrace();
		}
		
		// Load up the SDN-HMT-IBAN-Customer data sets here (the unmarshalling code)
		@SuppressWarnings("unused")		
		SdnList sdnList = unmarshalledSdn.getSdnList();
		sdnLoadStatus = unmarshalledSdn.getSdnLoadStatus();
		
		// Note the construction of this object in the logs
		logger.info(getSdnLoadStatus());				
		
		File file = new File(sdnStopwordFile);
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    // repeat until all lines is read
		    while ((text = reader.readLine()) != null) {
		    	// Add to sdnStopwordsSet
		    	sdnStopSet.add(text);
		    	
		    	// Keep a record for debugging purposes
		    	contents.append(text).append(System.getProperty(
		        "line.separator"));
		    }
		} catch (FileNotFoundException fnfe) {
			logger.debug("Caught FileNotFoundException reading sdnStopwordFile");
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			logger.debug("Caught IOException reading sdnStopwordFile");
			ioe.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
		        }
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
		}
		        
		// show file contents here
		logger.debug("sdnStopwords are: ");
		logger.debug(contents.toString());
	}

	public static synchronized SdnSearcher getSdnSearcherSingleton() {
		
		if (sdnSearcher == null) {
			sdnSearcher = new SdnSearcher();
		}
		return sdnSearcher;
	}
	
	public ResultsCarrierBean getStandardSearchResults(String searchTerm) {
 
		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();
        try {
			Directory dir 			= FSDirectory.open(new File(standardIndexDir));
			IndexSearcher searcher	= new IndexSearcher(dir);			

        	QueryParser queryParser = 
            	new QueryParser(Version.LUCENE_30, 
				"individualName", 
				new StandardAnalyzer(Version.LUCENE_30, sdnStopSet)); // uses sdnStopSet
			
            queryParser.setDefaultOperator(QueryParser.AND_OPERATOR); // Needed for exact match        	

            logger.debug("QueryParser searchTerm is..." + searchTerm);
        	Query queryQp = queryParser.parse(searchTerm);
    		//logger.debug("Type of query: " + queryQp.getClass().getSimpleName());        	
            TopDocs docsQp = searcher.search(queryQp, 10);
            float topScore = 0;
            String topScoreDoc = "";
            for (int i = 0; i < docsQp.scoreDocs.length; i++) {
            	
            	ScoreDoc hitQp = docsQp.scoreDocs[i];
            	Document docQp = searcher.doc(hitQp.doc);
            	// Get top scoring hit...
            	if (hitQp.score > topScore) {
            		topScore = hitQp.score;
            		topScoreDoc = docQp.get("individualName");
            	}            	
            }   
            //if (docsQp.scoreDocs.length == 0) {
            if (topScore == 0 ) {
            	logger.debug("LUCENE XXX No match found for query..." + searchTerm);
    			Date now = new Date();
    			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
    			resultsCarrierBean.setCarrierSearchResult("no match found");
    			resultsCarrierBean.setCarrierSearchScore(topScore);
    			resultsCarrierBean.setCarrierSearchTimestamp(now);            	
            	
            } else if (topScore > 0 ) {
            	//logger.info("LUCENE Result found was " 
            	//		+ i 
            	//		+ ": " 
            	//		+ docQp.get("individualName"));
            	logger.info("LUCENE Result found for query " 
            			+ searchTerm
            			+ " was " 
            			+ topScoreDoc 
            			+ " with relative score of "
            			+ topScore);
    			Date now = new Date();
    			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
    			resultsCarrierBean.setCarrierSearchResult(topScoreDoc.toLowerCase());
    			resultsCarrierBean.setCarrierSearchScore(topScore);
    			resultsCarrierBean.setCarrierSearchTimestamp(now);            	            	
            }

            searcher.close();
            
        } catch (org.apache.lucene.queryParser.ParseException pe) {
			pe.printStackTrace();
		} catch (CorruptIndexException cie) {
			cie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return resultsCarrierBean;
	}

	// Note this test only works for sdnType.contains("Individual") 
	// and sdnFirstName is not null
	public ResultsCarrierBean getInitialsSearchResults(String searchTerm) {
		
		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();
		String origSearchTerm = searchTerm;
		String space = " ";
		String doubleSpaces = "  ";
		SdnList sdnList = unmarshalledSdn.getSdnList();
		// Loop over SDN first name + last name 
		SdnList.SdnEntry sdnEntry = new SdnList.SdnEntry();
        List <SdnEntry>sdnEntrys = sdnList.getSdnEntry();
        sdnEntrys = sdnList.getSdnEntry();
        String sdnEntryName = null; 
        String [] splitStr = null;
        StringBuffer initialsSb = new StringBuffer();
        
        // Strip special chars from searchTerm (".,-'" etc)
        String pattern = "[^A-Za-z]";
        searchTerm = searchTerm.replaceAll(pattern, space);
        searchTerm = searchTerm.replaceAll(doubleSpaces, space);
        logger.debug("searchTerm is..." + searchTerm + "...");
        
		if (sdnEntrys == null)
        	logger.info("INITIALS No SDN entries.");
        else { 
        	for (Iterator<SdnEntry> i = sdnEntrys.iterator(); i.hasNext(); ) {
        		
        		sdnEntry = (SdnList.SdnEntry)i.next();
        		
        		if (sdnEntry.getSdnType().toLowerCase().contains("individual") 
        				&& sdnEntry.getFirstName() != null ) {
        			// Take sdnEntry firstName and reduce it to its initials
        			logger.debug("sdnEntry.getSdnType is " + sdnEntry.getSdnType());
        			logger.debug("sdnEntry.getFirstName is " + sdnEntry.getFirstName());
        			logger.debug("sdnEntry.getLastName is " + sdnEntry.getLastName());
        		
        			if (sdnEntry.getFirstName().contains(space)) {
        				splitStr = sdnEntry.getFirstName().split(space);
        				for (int j = 0; j < splitStr.length; j++) {
        					initialsSb.append(splitStr[j].substring(0, 1));
        					initialsSb.append(space);
        				}        		
        				sdnEntryName = initialsSb.toString() + sdnEntry.getLastName();
        			} else {
        				sdnEntryName = sdnEntry.getFirstName().substring(0,1) + space + sdnEntry.getLastName();
        			}
        			
        			if (searchTerm.toLowerCase().equals(sdnEntryName.toLowerCase())) {
        				logger.info("INITIALS Found result for query: " + origSearchTerm);
            			Date now = new Date();
            			resultsCarrierBean.setCarrierSearchTerm(searchTerm.toLowerCase());
            			resultsCarrierBean.setCarrierSearchResult(sdnEntryName.toLowerCase());
            			resultsCarrierBean.setCarrierSearchScore(0.7f);
            			resultsCarrierBean.setCarrierSearchTimestamp(now);            	            	        				
        			} else {
        				//logger.info("INITIALS No match found for " + searchTerm);
        			}
        		}
        	}
        }	
		return resultsCarrierBean;
	}
	
	public ResultsCarrierBean getSynonymSearchResults(String searchTerm) {
		
		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();
		
        try {
			Directory dir 			= FSDirectory.open(new File(synonymIndexDir));
			IndexSearcher searcher	= new IndexSearcher(dir);			

        	QueryParser queryParser = 
            	new QueryParser(Version.LUCENE_30, 
				"individualName", 
				new SynonymAnalyzer(new SanctionsSynonymEngine()));
						
            queryParser.setDefaultOperator(QueryParser.AND_OPERATOR); // Needed for exact match        	

            logger.debug("QueryParser searchTerm is..." + searchTerm);
        	Query queryQp = queryParser.parse(searchTerm);
    		//logger.debug("Type of query: " + queryQp.getClass().getSimpleName());        	
            TopDocs docsQp = searcher.search(queryQp, 10);
            float topScore = 0;
            String topScoreDoc = "";
            for (int i = 0; i < docsQp.scoreDocs.length; i++) {
            	
            	ScoreDoc hitQp = docsQp.scoreDocs[i];
            	Document docQp = searcher.doc(hitQp.doc);
            	// Get top scoring hit...
            	if (hitQp.score > topScore) {
            		topScore = hitQp.score;
            		topScoreDoc = docQp.get("individualName");
            	}            	
            }   
            //if (docsQp.scoreDocs.length == 0) {
            if (topScore == 0 ) {
            	logger.debug("SYNONYMS XXX No match found for query..." + searchTerm);
    			Date now = new Date();
    			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
    			resultsCarrierBean.setCarrierSearchResult("no match found");
    			resultsCarrierBean.setCarrierSearchScore(topScore);
    			resultsCarrierBean.setCarrierSearchTimestamp(now);            	
            	
            } else if (topScore > 0 ) {
            	//logger.info("SYNONYMS Result found was " 
            	//		+ i 
            	//		+ ": " 
            	//		+ docQp.get("individualName"));
            	logger.info("SYNONYMS Result found for query " 
            			+ searchTerm
            			+ " was " 
            			+ topScoreDoc 
            			+ " with relative score of "
            			+ topScore);
    			Date now = new Date();
    			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
    			resultsCarrierBean.setCarrierSearchResult(topScoreDoc.toLowerCase());
    			resultsCarrierBean.setCarrierSearchScore(topScore);
    			resultsCarrierBean.setCarrierSearchTimestamp(now);            	            	
            }

            searcher.close();
            
        } catch (org.apache.lucene.queryParser.ParseException pe) {
			pe.printStackTrace();
		} catch (CorruptIndexException cie) {
			cie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
				
		return resultsCarrierBean;
	}
		
	public ResultsCarrierBean checkExactNameMatch(String searchTerm) {
		
		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();
		
		String space = " ";
		SdnList sdnList = unmarshalledSdn.getSdnList();
		// Loop over SDN first name + last name 
		SdnList.SdnEntry sdnEntry = new SdnList.SdnEntry();
		
        //int entryCount = 0; 	        
        List <SdnEntry>sdnEntrys = sdnList.getSdnEntry();
        sdnEntrys = sdnList.getSdnEntry();
        String sdnEntryName = null; 
        		
		if (sdnEntrys == null)
        	logger.info("EXACT No SDN entries.");
        else { 
        	for (Iterator<SdnEntry> i = sdnEntrys.iterator(); i.hasNext(); ) {
        		sdnEntry = (SdnList.SdnEntry)i.next();
        		sdnEntryName = sdnEntry.getFirstName() + space + sdnEntry.getLastName();

        		if (searchTerm.toLowerCase().contains(sdnEntryName.toLowerCase())) {
        			logger.info("EXACT Found result for " + searchTerm.toLowerCase());
        			Date now = new Date();
        			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
        			resultsCarrierBean.setCarrierSearchResult(sdnEntryName.toLowerCase());
        			resultsCarrierBean.setCarrierSearchScore(1.0f);
        			resultsCarrierBean.setCarrierSearchTimestamp(now);
        			break;        			
        		} else {
        			//logger.info("EXACT No match found for " + searchTerm);
        			Date now = new Date();
        			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
        			resultsCarrierBean.setCarrierSearchResult("no exact match");
        			resultsCarrierBean.setCarrierSearchScore(0.0f);
        			resultsCarrierBean.setCarrierSearchTimestamp(now);        			        			
        		}        		
        	}
        }
		return resultsCarrierBean;
	}

	public String getSdnLoadStatus() {
		String sdnStatus;  
		if (sdnLoadStatus) {
			sdnStatus = "SDN uploaded successfully. File contained published date " 
			   + "(US format, MM/DD/YYYY) of " 
			   + unmarshalledSdn.getSdnList().getPublshInformation().getPublishDate()
			   + " and number of records in sdn.xml file is " 
			   + unmarshalledSdn.getSdnList().getPublshInformation().getRecordCount() 
			   + ".";	
		} else {
		   sdnStatus = "SDN upload failed. "
			   + "Do not run screening services until resolved. "
			   + "Check location and filename format of sdn.xml in config.properties file.";
		}
		return sdnStatus; 
	   }   

	// Needed to support singleton pattern
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	// For localised testing only
    public static void main(String[] args) throws IOException
    {
		// Log4J default config
		//BasicConfigurator.configure();
		//logger.setLevel(Level.DEBUG);	
		
		// Read in properties (location of SDN file)
		ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		String indexDir = config.getProperty("SdnIndexDirectory");
		
		IndexSearcher searcher = null;
		try {
			Directory dir 	= FSDirectory.open(new File(indexDir));
			searcher		= new IndexSearcher(dir);			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

        String			searchTerm	= "robert gabriel mugabe"; // must be lowercase
        
        // TermQuery search
        logger.debug("\nStarting TermQuery search...");        
        Query queryTq = new TermQuery(new Term("individualName", searchTerm));        
        TopDocs docsTq = searcher.search(queryTq, 10);        
        for (int i = 0; i < docsTq.scoreDocs.length; i++) {
        	
        	ScoreDoc hitTq = docsTq.scoreDocs[i];
        	Document docTq = searcher.doc(hitTq.doc);
        
        	logger.debug("NAME: " + docTq.get("individualName"));
        }
        
        // QueryParser search
        logger.debug("\nStarting QueryParser search...");         
        try {
            QueryParser queryParser = new QueryParser(Version.LUCENE_30, 
					"individualName", new StandardAnalyzer(Version.LUCENE_30));
        	
            queryParser.setDefaultOperator(QueryParser.AND_OPERATOR); // Needed for exact match
        	Query queryQp = queryParser.parse(searchTerm);        	
            TopDocs docsQp = searcher.search(queryQp, 10);        
            for (int i = 0; i < docsQp.scoreDocs.length; i++) {
            	
            	ScoreDoc hitQp = docsQp.scoreDocs[i];
            	Document docQp = searcher.doc(hitQp.doc);
            
            	logger.debug("NAME: " + docQp.get("individualName"));
            }        	
        } catch (org.apache.lucene.queryParser.ParseException e) {
			e.printStackTrace();
		}
        		
        logger.debug("\nStarting Wildcard search...");
        // Wildcard search
        //searchTerm = searchTerm + "*";
        Term term = new Term("individualName", searchTerm);
		Query queryWc = new WildcardQuery(term);

        TopDocs docsWc = searcher.search(queryWc, 10);        
		//logger.debug("Type of query: " + queryWc.getClass().getSimpleName());
        for (int i = 0; i < docsWc.scoreDocs.length; i++) {
        	
        	ScoreDoc hitQp = docsWc.scoreDocs[i];
        	Document docQp = searcher.doc(hitQp.doc);
        
        	logger.debug("NAME: " + docQp.get("individualName"));
        }        	
        searcher.close();
        
   }
}
