package com.elektrifi.sanctions.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.elektrifi.sanctions.beans.HmtBean;
import com.elektrifi.util.StringUtils;
//import com.elektrifi.sanctions.beans.ResultsBeanList;
//import com.elektrifi.sanctions.beans.ResultsBean;
import com.elektrifi.sanctions.beans.ResultsCarrierBean;
import com.elektrifi.sanctions.beans.HmtBean.HmtMetaData;

public class NonSingletonHmtSearcher {

	// Singleton pattern needed (TBC)
	// JF private static HmtSearcher hmtSearcher;

	// unmarshalledSdn contains SDN data in POJOs (UnmarshalSdn uses a singleton pattern too)
	private UnmarshalHmt unmarshalledHmt = UnmarshalHmt.instance();  
	HmtMetaData hmtListMetaData = unmarshalledHmt.getHmtListMetaData();
	
	// Status of SDN load (defaults to false)
	private boolean hmtLoadStatus = false; 

	// Set up log4j logger
	private static 	Logger logger 			= Logger.getLogger(HmtSearcher.class);
	
	// Location of Lucene index
	private static 	String indexDir 		= null;		

	// ResultsBeanList
	//private ResultsBeanList resultsBeanList = ResultsBeanList.getResultsBeanListSingleton();
	
	// Custom stopwords
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<String> hmtStopSet			= new HashSet(Arrays.asList(StandardAnalyzer.STOP_WORDS_SET));	

	// JF
	public NonSingletonHmtSearcher() { //Constructor

		String hmtStopwordFile = "";
		try {
			InputStream inputStream = this.getClass().getClassLoader()
										.getResourceAsStream("config.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			indexDir = properties.getProperty("HmtIndexDirectory");
			logger.info("HmtIndexDirectory read in as " + indexDir);
			hmtStopwordFile = properties.getProperty("StopwordFile");
			logger.info("StopwordFile read in as " + hmtStopwordFile);
			inputStream.close();
			
		} catch(IOException ioe) {
			logger.error("Caught IOException: ");
			ioe.printStackTrace();
		}
		
		// Load up the SDN-HMT-IBAN-Customer data sets here (the unmarshalling code)
		@SuppressWarnings("unused")		
		List<HmtBean> hmtList = unmarshalledHmt.getHmtList();
		hmtLoadStatus = unmarshalledHmt.getHmtLoadStatus();
		
		// Note the construction of this object in the logs
		logger.info(getHmtLoadStatus());				
		
		File file = new File(hmtStopwordFile);
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
		    String text = null;

		    // repeat until all lines is read
		    while ((text = reader.readLine()) != null) {
		    	// Add to hmtStopwordsSet
		    	hmtStopSet.add(text);
		    	
		    	// Keep a record for debugging purposes
		    	contents.append(text).append(System.getProperty(
		        "line.separator"));
		    }
		} catch (FileNotFoundException fnfe) {
			logger.debug("Caught FileNotFoundException reading hmtStopwordFile");
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			logger.debug("Caught IOException reading hmtStopwordFile");
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
		//logger.debug("hmtStopwords are: ");
		//logger.debug(contents.toString());
	}

	public ResultsCarrierBean getStandardSearchResults(String searchTerm) {
		 
		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();
        try {
			Directory dir 			= FSDirectory.open(new File(indexDir));
			IndexSearcher searcher	= new IndexSearcher(dir);			

        	QueryParser queryParser = 
            	new QueryParser(Version.LUCENE_30, 
				"individualName", 
				new StandardAnalyzer(Version.LUCENE_30, hmtStopSet)); // uses hmtStopSet
			
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
            if (topScore == 0 ) {
            	//logger.debug("LUCENE XXX No match found for query..." + searchTerm);
    			Date now = new Date();
    			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
    			resultsCarrierBean.setCarrierSearchResult("no lucene match");
    			resultsCarrierBean.setCarrierSearchScore(0.0f);
    			resultsCarrierBean.setCarrierSearchTimestamp(now);            	            	
            } else if (topScore > 0 ) {
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

	// Note this test only works for hmtType.contains("Individual") 
	// and hmtFirstName is not null
	public ResultsCarrierBean getInitialsSearchResults(String searchTerm) {
				
		logger.debug("NEW INITIALS input searchTerm is..."+searchTerm);
		
		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();		
		String origSearchTerm = searchTerm;
		String initialisedSearchTerm = searchTerm;
		String space = " ";			
		List<HmtBean> hmtList = unmarshalledHmt.getHmtList();
		HmtBean hmtEntry = new HmtBean();
		StringUtils nameInitials = new StringUtils(); 
        String hmtEntryName = null; 
        
        searchTerm = nameInitials.removeSpecialCharsAndExcessSpaces(searchTerm);
		logger.debug("INITIALS cleaned-up searchTerm is..."+searchTerm);
		
		if (hmtList == null)
        	logger.info("INITIALS No HMT entries.");
        else { 
        	for (Iterator<HmtBean> i = hmtList.iterator(); i.hasNext(); ) {
        		
        		hmtEntry = (HmtBean)i.next();
        		logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        		logger.debug("hmtEntry is...");
        		logger.debug(hmtEntry.toString());
        		
        		// Check its an individual only
        		if (hmtEntry.getGroupType().toLowerCase().contains("individual")) { 

        			// Check that there is at least a first name 
        			if (!hmtEntry.getName1().isEmpty()) {

            			// Build full first name
            			String hmtEntryFirstName = 
            				hmtEntry.getName1() + space 
            				+ hmtEntry.getName2() + space
            				+ hmtEntry.getName3() + space
            				+ hmtEntry.getName4() + space
            				+ hmtEntry.getName5();        				

        				hmtEntryName = nameInitials.getInitials(hmtEntryFirstName) 
        								+ space 
        								+ hmtEntry.getName6();
        				
        				initialisedSearchTerm = nameInitials.getInitials(nameInitials.getFirstNames(searchTerm))
        											+ space
        											+ nameInitials.getLastName(searchTerm);

        				float holdingScore = 0.0f;
        				float searchScore = 0.0f;
        				//String holdingSearchTerm = "";
        				//String holdingResult = "";

        				logger.debug("\t---");
        				logger.debug("\thmtEntryName is..." + hmtEntryName.toLowerCase() + "...");          			
            			logger.debug("\tinitialisedSearchTerm is..." + initialisedSearchTerm.toLowerCase() + "...");
            			logger.debug("\tSearch score is..." + searchScore + " and "            			
            						+ "holding score is --> " + holdingScore);
        				        				        				
        				/**
            			//if (searchTerm.toLowerCase().equals(hmtEntryName.toLowerCase())) {
        				if (hmtEntryName.toLowerCase().equals(searchTerm.toLowerCase())) {        					
        					
        					searchScore = 1.0f;
        					
                			//Date now = new Date();
                			//resultsCarrierBean.setCarrierSearchTerm(origSearchTerm);
                			//resultsCarrierBean.setCarrierSearchResult(hmtEntryName.toLowerCase());
                			//resultsCarrierBean.setCarrierSearchScore(1.0f); // Always defaults to 0.7f
                			//resultsCarrierBean.setCarrierSearchTimestamp(now);            			
            				logger.info("INITIALS Found (searchTerm equals) result for query: " 
            						//+ origSearchTerm 
            						+ searchTerm
            						+ " with HMT entry "
            						+ hmtEntryName.toLowerCase());
            				//logger.debug(resultsCarrierBean.toJson());
            				//break; // come out as soon as we find a match 
            				
        				//} else if (searchTerm.toLowerCase().contains(hmtEntryName.toLowerCase())) {
        				} else if (hmtEntryName.toLowerCase().contains(searchTerm.toLowerCase())) {            				
        					
        					searchScore = 0.7f;
        					//Date now = new Date();
                			//resultsCarrierBean.setCarrierSearchTerm(origSearchTerm);
                			//resultsCarrierBean.setCarrierSearchResult(hmtEntryName.toLowerCase());
                			//resultsCarrierBean.setCarrierSearchScore(0.7f); // Lesser match than contains
                			//resultsCarrierBean.setCarrierSearchTimestamp(now);            			
            				logger.info("INITIALS Found (searchTerm contains) result for query: " 
            						//+ origSearchTerm
            						+ searchTerm
            						+ " with HMT entry "
            						+ hmtEntryName.toLowerCase());
            				//logger.debug(resultsCarrierBean.toJson());
            				//break; // come out as soon as we find a match 

        				//} else if (initialisedSearchTerm.toLowerCase().equals(hmtEntryName.toLowerCase())) {
        				} else if (hmtEntryName.toLowerCase().equals(initialisedSearchTerm.toLowerCase())) {        					
        					searchScore = 0.5f;
        					//Date now = new Date();
                			//resultsCarrierBean.setCarrierSearchTerm(origSearchTerm);
                			//resultsCarrierBean.setCarrierSearchResult(hmtEntryName.toLowerCase());
                			//resultsCarrierBean.setCarrierSearchScore(0.5f); // Arbitrary scoring
                			//resultsCarrierBean.setCarrierSearchTimestamp(now);            			
            				logger.info("INITIALS Found (initialisedSearchTerm equals) result for query: " 
            						//+ origSearchTerm
            						+ initialisedSearchTerm
            						+ " with HMT entry "
            						+ hmtEntryName.toLowerCase());
            				//logger.debug(resultsCarrierBean.toJson());
            				//break; // come out as soon as we find a match 

        				//} else if (initialisedSearchTerm.toLowerCase().contains(hmtEntryName.toLowerCase())) {
        				} else if (hmtEntryName.toLowerCase().contains(initialisedSearchTerm.toLowerCase())) {            				
        					
        					searchScore = 0.3f;
        					//Date now = new Date();
                			//resultsCarrierBean.setCarrierSearchTerm(origSearchTerm);
                			//resultsCarrierBean.setCarrierSearchResult(hmtEntryName.toLowerCase());
                			//resultsCarrierBean.setCarrierSearchScore(0.3f); // Arbitrary but stronger match than contains
                			//resultsCarrierBean.setCarrierSearchTimestamp(now);            			
            				logger.info("INITIALS Found (initialisedSearchTerm contains) result for query: " 
            						//+ origSearchTerm
            						+ initialisedSearchTerm
            						+ " with HMT entry "
            						+ hmtEntryName.toLowerCase());
            				//logger.debug(resultsCarrierBean.toJson());
            				//break; // come out as soon as we find a match 
            				
        				} else {
        					
        					searchScore = 0.0f;
            				logger.info("INITIALS No match found for query " 
            						+ searchTerm 
            						+ " and HmtEntry of "
            						+ hmtEntryName.toLowerCase());
                			//Date now = new Date();        				
                			//resultsCarrierBean.setCarrierSearchTerm(searchTerm);
                			//resultsCarrierBean.setCarrierSearchResult("no initials match");
                			//resultsCarrierBean.setCarrierSearchScore(0.0f);
                			//resultsCarrierBean.setCarrierSearchTimestamp(now);            	            	
        				}
						**/
            			/**
        				// Now compare the tests and select the winner so far
        				if (searchScore > holdingScore) {
    						holdingScore = searchScore;
    						holdingSearchTerm = origSearchTerm;
    						holdingResult = hmtEntryName.toLowerCase();
    						
            				logger.info("INITIALS Comparing test scores for " 
            						+ searchTerm 
            						+ " with HMT entry "
            						+ hmtEntryName.toLowerCase());
                			Date now = new Date();        				
                			resultsCarrierBean.setCarrierSearchTerm(holdingSearchTerm);
                			resultsCarrierBean.setCarrierSearchResult(holdingResult);
                			resultsCarrierBean.setCarrierSearchScore(holdingScore);
                			resultsCarrierBean.setCarrierSearchTimestamp(now);            	            	
    						
    					} 
        				**/
            			
            			if (hmtEntryName.toLowerCase().equals(initialisedSearchTerm.toLowerCase())) {
    						Date now = new Date();
            				resultsCarrierBean.setCarrierSearchTerm(origSearchTerm);
            				resultsCarrierBean.setCarrierSearchResult(hmtEntryName.toLowerCase());
            				resultsCarrierBean.setCarrierSearchScore(0.5f); // Arbitrary scoring
            				resultsCarrierBean.setCarrierSearchTimestamp(now);            			
        					logger.info("INITIALS Found (initialisedSearchTerm equals) result for query: " 
        						//+ origSearchTerm
        						+ initialisedSearchTerm
        						+ " with HMT entry "
        						+ hmtEntryName.toLowerCase());
        					logger.debug(resultsCarrierBean.toJson());
        					break; // come out as soon as we find a match 
            			
        				} else {
        				        				
        					//Date now = new Date();        				
        					//resultsCarrierBean.setCarrierSearchTerm(searchTerm);
        					//resultsCarrierBean.setCarrierSearchResult("no initials match");
        					//resultsCarrierBean.setCarrierSearchScore(0.0f);
        					//resultsCarrierBean.setCarrierSearchTimestamp(now);
        					// there is no first name in the HMT entry we're checking against            			
        				}
        			}
        			
        		} else { // initials check only applies to individuals (for now)
        			
        			//logger.info("HMT entry is not an individual..." 
        			//		+ hmtEntry.getName1() + "..." + hmtEntry.getName6());
        			
        			//Date now = new Date();        				
        			//resultsCarrierBean.setCarrierSearchTerm(searchTerm);
        			//resultsCarrierBean.setCarrierSearchResult("not an individual");
        			//resultsCarrierBean.setCarrierSearchScore(0.0f);
        			//resultsCarrierBean.setCarrierSearchTimestamp(now);            	            	        			
        		}  
        		
        	} // continue thru loop
        	
        }	
		return resultsCarrierBean;
	}

	public ResultsCarrierBean getSynonymSearchResults(String searchTerm) {

		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();
		
		Date now = new Date();
		resultsCarrierBean.setCarrierSearchTerm(searchTerm);
		resultsCarrierBean.setCarrierSearchResult("no synonyms found");
		resultsCarrierBean.setCarrierSearchScore(0.0f);
		resultsCarrierBean.setCarrierSearchTimestamp(now);
		
		return resultsCarrierBean;
	}
			
	public ResultsCarrierBean checkExactNameMatch(String searchTerm) {
		
		String space = " ";
		List<HmtBean> hmtList = unmarshalledHmt.getHmtList();
		// Loop over SDN first name + last name 
		HmtBean hmtEntry = new HmtBean();
		ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();
		
        String hmtEntryName = null; 
        		
		if (hmtList == null)
        	logger.info("EXACT No HMT entries.");
        else { 
        	for (Iterator<HmtBean> i = hmtList.iterator(); i.hasNext(); ) {
        		hmtEntry = (HmtBean)i.next();
        		hmtEntryName = 
        			hmtEntry.getName1() + space 
        			+ hmtEntry.getName2() + space
        			+ hmtEntry.getName3() + space
        			+ hmtEntry.getName4() + space
        			+ hmtEntry.getName5() + space
        			+ hmtEntry.getName6();
        		
        		// Strip out excess spaces
        		hmtEntryName = hmtEntryName.replaceAll("\\b\\s{2,}\\b", " ");
        		hmtEntryName.trim();

        		if (searchTerm.toLowerCase().contains(hmtEntryName.toLowerCase())) {
        			logger.info("EXACT Found result for " + searchTerm.toLowerCase());
        			Date now = new Date();
        			resultsCarrierBean.setCarrierSearchTerm(searchTerm);
        			resultsCarrierBean.setCarrierSearchResult(hmtEntryName.toLowerCase());
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
	
	//JF
	/**
	public static synchronized HmtSearcher getHmtSearcherSingleton() {
		
		if (hmtSearcher == null) {
			hmtSearcher = new HmtSearcher();
		}
		return hmtSearcher;
	}	
	**/
	
	public String getHmtLoadStatus() {
		String hmtStatus;  
		if (hmtLoadStatus) {
			hmtStatus = "HMT uploaded successfully. File contained published date " 
			   + "(British format, DD/MM/YYYY) of " 
			   + unmarshalledHmt.hmtListMetaData.getLastUpdated()
			   + " and number of records in hmt.txt file is " 
			   + unmarshalledHmt.hmtListMetaData.getRecordCount()
			   + ".";	
		} else {
		   hmtStatus = "HMT upload failed. "
			   + "Do not run screening services until resolved. "
			   + "Check location and filename format of hmt.txt in config.properties file.";
		}
		return hmtStatus; 
	   }   

	// Needed to support singleton pattern
	//JF
	//public Object clone() throws CloneNotSupportedException {
	//	throw new CloneNotSupportedException();
	//}
	
}
