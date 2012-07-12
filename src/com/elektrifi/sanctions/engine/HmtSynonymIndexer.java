package com.elektrifi.sanctions.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import com.elektrifi.sanctions.beans.HmtBean;
import com.elektrifi.util.ApplicationConfig;
import com.elektrifi.sanctions.analyzers.SynonymAnalyzer;
//import com.elektrifi.sanctions.analyzers.SynonymEngine;
import com.elektrifi.sanctions.analyzers.SanctionsSynonymEngine;
//import com.elektrifi.sanctions.analyzers.SynonymFilter;


public class HmtSynonymIndexer {

    private static IndexWriter 			writer;
    private static SynonymAnalyzer 		synonymAnalyzer =
        new SynonymAnalyzer(new SanctionsSynonymEngine());
    
    // Set up log4j logger
	private static Logger logger = Logger.getLogger(HmtSynonymIndexer.class);
    
    public static void main(String[] args) throws IOException, SAXException    {
    	
		// Log4J default config
		logger.setLevel(Level.DEBUG);	
		
		// Read in properties (location of SDN file)
		ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		String hmtFileLocation = config.getProperty("HmtFileLocation");
		String indexDir = config.getProperty("HmtSynonymIndexDirectory");
		
		// Set up analyzer
    	//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		//Analyzer analyzer = new SynonymAnalyzer();
        boolean createFlag = true;

        // IndexWriter to use for adding contacts to the index 
        Directory dir = FSDirectory.open(new File(indexDir));
        writer = new IndexWriter(dir, synonymAnalyzer, createFlag, IndexWriter.MaxFieldLength.UNLIMITED);

        // Load in the HMT consolidated Sanctions List        
		try {
			CSVReader reader = new CSVReader(
					new FileReader(hmtFileLocation),';', '\"', 2); // skip first 2 lines
			ColumnPositionMappingStrategy<HmtBean> strat = 
				new ColumnPositionMappingStrategy<HmtBean>();			
			String[] hmtColumns = new String[] {
					"name6","name1","name2",
					"name3","name4","name5",
					"title","dob","townOfBirth",
					"countryOfBirth","nationality",
					"passportDetails","niNumber","position",
					"address1","address2","address3",
					"address4","address5","address6",
					"postZipCode","country","otherInformation",
					"groupType","aliasType","regime",
					"listedOn","lastUpdated","groupId"};
			
			strat.setColumnMapping(hmtColumns);
			strat.setType(HmtBean.class);
			@SuppressWarnings("rawtypes")
			CsvToBean csv = new CsvToBean();
			@SuppressWarnings("unchecked")
			List<HmtBean> hmtList = csv.parse(strat, reader);			
			
			// Now go through the list and check unmarshalled data
			Iterator<HmtBean> it=hmtList.iterator();
	        while(it.hasNext())
	        {
	          HmtBean hmtBean =(HmtBean)it.next();	          
	          //logger.debug("Test value for Name 6:"+hmtBean.getName6());
	          //logger.debug("\tTest value for Name 1:"+hmtBean.getName1());
	          
	          addHmtEntry(hmtBean);
	        }	        
		} catch (FileNotFoundException fnfe) {
			logger.error("Caught FNFException:");
			fnfe.printStackTrace();
		}		
               
		logger.info("Indexing finished.");

        // Optimize and close the index
        writer.optimize();
        writer.close();
    }

    //Adds the HMT entry to the index
    public static void addHmtEntry(HmtBean hmtEntry) throws IOException
    {
		Document hmtDocument  = new Document();
    	String space 		= " ";
    	@SuppressWarnings("unused")
		String title 		= null;
    	String firstName 	= null;
    	String lastName 	= null;
    	String hmtType		= null; 
    	
    	hmtType = hmtEntry.getGroupType();
    	
    	if (hmtType.contains("Individual")) {
    	
    		title 			= hmtEntry.getTitle();
    		firstName 		= hmtEntry.getName1();
    		lastName		= hmtEntry.getName6();
    		StringBuffer nameSb	= new StringBuffer();
    		if (firstName != null)
    		{
    			nameSb.append(firstName);    		
    			nameSb.append(space);
    		}
    		if (hmtEntry.getName2() != null ) {
    			nameSb.append(hmtEntry.getName2());
    			nameSb.append(space);
    		}
    		if (hmtEntry.getName3() != null) {
        		nameSb.append(hmtEntry.getName3());
        		nameSb.append(space);    			
    		}
    		if (hmtEntry.getName4() != null) {
    			nameSb.append(hmtEntry.getName4());
    			nameSb.append(space);
    		}
    		if (hmtEntry.getName5() != null) {    		
    			nameSb.append(hmtEntry.getName5());
    			nameSb.append(space);
    		}
    		nameSb.append(lastName.trim());
    		// Strip out nulls and multiple spaces...
    		String tmpNameStr = nameSb.toString();
    		nameSb.setLength(0);
    		//logger.debug(">>> nameStr going in is..." + tmpNameStr);
    		String tmpName2Str = tmpNameStr.replaceAll("null ", "");
    		String nameStr = tmpName2Str.replaceAll("\\b\\s{2,}\\b", " ");
    		//logger.debug("<<< nameStr coming out is..." + nameStr);
    		nameStr = nameStr.trim();
    	
    		if (hmtEntry.getTitle() != null) {    	
    			hmtDocument.add(new Field("title", hmtEntry.getTitle(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    		}

    		if (hmtEntry.getName1() != null) { 
    			hmtDocument.add(new Field("firstname", hmtEntry.getName1(), Field.Store.YES, Field.Index.ANALYZED));
    		}
    	
    		if (hmtEntry.getName6() != null) {
    			hmtDocument.add(new Field("lastname", hmtEntry.getName6(), Field.Store.YES, Field.Index.ANALYZED));
    		}
    	
    		if (nameStr != null) {
    			hmtDocument.add(new Field("individualName", nameStr, Field.Store.YES, Field.Index.ANALYZED));
    		}

    		logger.info("Adding HMT entry for type: " 
    			+ hmtType + space
    			+ "with value..."
    			+ nameStr + "...");
    	    	
    		// Now write to index
    		//writer.addDocument(hmtDocument);
    		
    	} else if (hmtType.contains("Entity")) {
    		
    		if (hmtEntry.getName6() != null) {
    			hmtDocument.add(new Field("entityName", hmtEntry.getName6(), Field.Store.YES, Field.Index.ANALYZED));
    		}

    		logger.info("Adding SDN entry for type: " 
        			+ hmtType + space
        			+ "with value..."
        			+ hmtEntry.getName6() + "...");
        	    	
        	// Now write to index
        	//writer.addDocument(hmtDocument);
    		
    	} else if (hmtType.contains("Vessel")) {

    		if (hmtEntry.getName6() != null) {
    			hmtDocument.add(new Field("vesselName", hmtEntry.getName6(), Field.Store.YES, Field.Index.ANALYZED));
    		}
    		
    		logger.info("Adding SDN entry for type: " 
        			+ hmtType + space
        			+ "with value..."        			
        			+ hmtEntry.getName6() + "...");
        	    	
        	// Now write to index
        	//writer.addDocument(hmtDocument);
    		
    	}
    	
		// Now write to index
		writer.addDocument(hmtDocument);
    	
    }
    
    public static boolean containsNone(String str, String invalidChars) {
        if (str == null || invalidChars == null) {
            return true;
        }
        return containsNone(str, invalidChars.toCharArray());
    }
    // ContainsNone
    //-----------------------------------------------------------------------
    /**
     * Checks that the String does not contain certain characters.
     *
     * A <code>null</code> String will return <code>true</code>.
     * A <code>null</code> invalid character array will return <code>true</code>.
     * An empty String ("") always returns true.
     *
     * <pre>
     * StringUtils.containsNone(null, *)       = true
     * StringUtils.containsNone(*, null)       = true
     * StringUtils.containsNone("", *)         = true
     * StringUtils.containsNone("ab", '')      = true
     * StringUtils.containsNone("abab", 'xyz') = true
     * StringUtils.containsNone("ab1", 'xyz')  = true
     * StringUtils.containsNone("abz", 'xyz')  = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param invalidChars  an array of invalid chars, may be null
     * @return true if it contains none of the invalid chars, or is null
     * @since 2.0
     */
    public static boolean containsNone(String str, char[] invalidChars) {
        if (str == null || invalidChars == null) {
            return true;
        }
        int strSize = str.length();
        int validSize = invalidChars.length;
        for (int i = 0; i < strSize; i++) {
            char ch = str.charAt(i);
            for (int j = 0; j < validSize; j++) {
                if (invalidChars[j] == ch) {
                    return false;
                }
            }
        }
        return true;
    }    

}
