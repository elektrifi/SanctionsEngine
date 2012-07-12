package com.elektrifi.sanctions.engine;

import org.xml.sax.SAXException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.util.Version;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.tempuri.sdnlist.SdnList;
import org.tempuri.sdnlist.SdnList.SdnEntry;
//import org.tempuri.sdnlist.SdnList.SdnEntry.AddressList.Address;

import com.elektrifi.util.ApplicationConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import com.elektrifi.sanctions.analyzers.SynonymAnalyzer;
import com.elektrifi.sanctions.analyzers.SanctionsSynonymEngine;

/**
 * Parses the contents of SDN XML file and indexes all
 * entries found in it.  The name of the file is derived 
 * from the entry ion the config.properties file.
 */
public class SdnSynonymIndexer {

    private static IndexWriter 			writer;
    private static SynonymAnalyzer 		synonymAnalyzer =
        new SynonymAnalyzer(new SanctionsSynonymEngine());
    
    // Set up log4j logger
	private static Logger logger = Logger.getLogger(SdnSynonymIndexer.class);
    
    public static void main(String[] args) throws IOException, SAXException
    {
    	//String space = " ";
		// Log4J default config
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);	
		
		// Read in properties (location of SDN file)
		ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		String sdnFile = config.getProperty("SdnFileLocation");
		String indexDir = config.getProperty("SdnSynonymIndexDirectory");
		
		// Set up analyzer
    	//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        boolean createFlag = true;

        // IndexWriter to use for adding contacts to the index 
        Directory dir = FSDirectory.open(new File(indexDir));
        writer = new IndexWriter(dir, synonymAnalyzer, createFlag, IndexWriter.MaxFieldLength.UNLIMITED);
         
        // Load in the SDN file
		try {
			
			JAXBContext jc = JAXBContext.newInstance( "org.tempuri.sdnlist" );
			Unmarshaller u = jc.createUnmarshaller();

			//u.getSchema();
			SdnList sdnList = (SdnList)u.unmarshal( new FileInputStream(sdnFile));
			System.out.println("Sdnlist publish info: " + sdnList.getPublshInformation().getPublishDate());
			System.out.println("Sdnlist record count: " + sdnList.getPublshInformation().getRecordCount());

			SdnList.SdnEntry sdnEntry = new SdnList.SdnEntry();
			//SdnList.SdnEntry.AddressList addressList = new SdnList.SdnEntry.AddressList(); 
			//SdnList.SdnEntry.AddressList.Address address = new SdnList.SdnEntry.AddressList.Address();
			
	        int entryCount = 0; 	        
	        List <SdnEntry>sdnEntrys = sdnList.getSdnEntry();
	        if (sdnEntrys == null)
	        	System.out.println("No SDN entries.");
	        else {
	        	for (@SuppressWarnings("rawtypes")
				Iterator i = sdnEntrys.iterator(); i.hasNext(); ) {
	        		entryCount++;	        		
	        		sdnEntry = (SdnList.SdnEntry)i.next();
	        		addSdnEntry(sdnEntry);

	        		/**
			        System.out.println("Sdn Entry " + entryCount + ": uid is " + sdnEntry.getUid());			        
			        System.out.println("\t type is " + sdnEntry.getSdnType());
			        System.out.println("\t title is " + sdnEntry.getTitle());
			        System.out.println("\t first name is " + sdnEntry.getFirstName());
			        System.out.println("\t last name is " + sdnEntry.getLastName());
			        **/	
	        		/**
			        // Handle addresses
			        addressList = sdnEntry.getAddressList();
			        if ( addressList == null ) {
			        	//System.out.println("No Address list entries.");
			        } else {
			        	List<Address> addresses = addressList.getAddress();
			        	for (@SuppressWarnings("rawtypes")
						Iterator j = addresses.iterator(); j.hasNext() ; ) {
			        		
			        		//address = (SdnList.SdnEntry.AddressList.Address)j.next();
			        **/
			        		/**
			        		System.out.println("\t address (" + address.getUid() + ") is: "
			        			+ address.getAddress1() + space
			        			+ address.getAddress2() + space
			        			+ address.getAddress3() + space
			        			+ address.getCity() + space
			        			+ address.getStateOrProvince() + space
			        			+ address.getPostalCode() + space
			        			+ address.getCountry());
			        			        		
			        	}
			        	**/
	        		
			    }
			}	       
					
		} catch( UnmarshalException ue ) {
			System.out.println( "Caught UnmarshalException" );
			ue.printStackTrace();
		} catch( JAXBException je ) { 
			System.out.println( "Caught JAXBException" );
			je.printStackTrace();
		} catch( IOException ioe ) {
			System.out.println( "Caught IOException" );
			ioe.printStackTrace();
		}  
		logger.info("Indexing finished.");

        // Optimize and close the index
        writer.optimize();
        writer.close();
    }

    //Adds the SDN entry to the index
    public static void addSdnEntry(SdnList.SdnEntry sdnEntry) throws IOException
    {
		Document sdnDocument  = new Document();
    	String space 		= " ";
    	@SuppressWarnings("unused")
		String title 		= null;
    	String firstName 	= null;
    	String lastName 	= null;
    	String sdnType		= null; 
    	
    	sdnType = sdnEntry.getSdnType();
    	
    	if (sdnType.contains("Individual")) {
    	
    		title 			= sdnEntry.getTitle();
    		firstName 		= sdnEntry.getFirstName();
    		lastName		= sdnEntry.getLastName();
    		StringBuffer nameSb	= new StringBuffer();
    		nameSb.append(firstName);
    		nameSb.append(space);
    		nameSb.append(lastName);
    		// Strip out nulls
    		String tmpNameStr = nameSb.toString();
    		//logger.debug(">>> nameStr going in is..." + tmpNameStr);
    		String nameStr = tmpNameStr.replaceAll("null ", "");
    		//logger.debug("<<< nameStr coming out is..." + nameStr);
    	
    		if (sdnEntry.getTitle() != null) {    	
    			sdnDocument.add(new Field("title", sdnEntry.getTitle(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    		}

    		if (sdnEntry.getFirstName() != null) { 
    			sdnDocument.add(new Field("firstname", sdnEntry.getFirstName(), Field.Store.YES, Field.Index.ANALYZED));
    		}
    	
    		if (sdnEntry.getLastName() != null) {
    			sdnDocument.add(new Field("lastname", sdnEntry.getLastName(), Field.Store.YES, Field.Index.ANALYZED));
    		}
    	
    		if (nameStr != null) {
    			sdnDocument.add(new Field("individualName", nameStr, Field.Store.YES, Field.Index.ANALYZED));
    		}

    		logger.info("Adding SDN entry for type: " 
    			+ sdnType + space
    			+ sdnEntry.getUid() + space
    			+ nameStr + "\n");
    	    	
    		// Now write to index
    		//writer.addDocument(sdnDocument);
    		
    	} else if (sdnType.contains("Entity")) {
    		
    		if (sdnEntry.getLastName() != null) {
    			sdnDocument.add(new Field("entityName", sdnEntry.getLastName(), Field.Store.YES, Field.Index.ANALYZED));
    		}

    		logger.info("Adding SDN entry for type: " 
        			+ sdnType + space
        			+ sdnEntry.getUid() + space
        			+ sdnEntry.getLastName() + "\n");
        	    	
        	// Now write to index
        	//writer.addDocument(sdnDocument);
    		
    	} else if (sdnType.contains("Vessel")) {

    		if (sdnEntry.getLastName() != null) {
    			sdnDocument.add(new Field("vesselName", sdnEntry.getLastName(), Field.Store.YES, Field.Index.ANALYZED));
    		}
    		
    		logger.info("Adding SDN entry for type: " 
        			+ sdnType + space
        			+ sdnEntry.getUid() + space
        			+ sdnEntry.getLastName() + "\n");
        	    	
        	// Now write to index
        	//writer.addDocument(sdnDocument);
    		
    	}
    	
		// Now write to index
		writer.addDocument(sdnDocument);
    	
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