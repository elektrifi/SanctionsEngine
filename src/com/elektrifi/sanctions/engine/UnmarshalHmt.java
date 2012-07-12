package com.elektrifi.sanctions.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVReader; 
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;

import com.elektrifi.sanctions.beans.HmtBean;
import com.elektrifi.sanctions.beans.HmtBean.HmtMetaData;

import org.apache.log4j.Logger;

public class UnmarshalHmt {

	// Set up log4j logger
	private static Logger logger = Logger.getLogger(UnmarshalHmt.class);	
	private Boolean hmtLoadStatus = false;  
	
	public HmtBean hmtMeta = new HmtBean(); 
	public HmtMetaData hmtListMetaData = hmtMeta.new HmtMetaData();
	
	private List<HmtBean> hmtList = new ArrayList<HmtBean>();

	@SuppressWarnings("unchecked")
	protected UnmarshalHmt() {

		super(); 
		
		// Read in properties (location of HMT file)
		//ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		//String hmtFileLocation = config.getProperty("HmtFileLocation");
		//logger.info("HmtFileLocation read in as " + hmtFileLocation);
		
		String hmtFileLocation = null; 
		try {
			InputStream inputStream = this.getClass().getClassLoader()
										.getResourceAsStream("config.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			hmtFileLocation = properties.getProperty("HmtFileLocation");
			logger.info("HmtFileLocation read in as " + hmtFileLocation);
			inputStream.close();
		} catch(IOException ioe) {
			logger.error("Caught IOException: ");
			ioe.printStackTrace();
		}		
		
		// Prepare for HMT meta data (used later)
		String[] hmtMetaDataLastUpdated = null;
		// Now add file lastUpdated string to object. 
		int lineCount = 0; // Used later to set record count		
		try {
			// NOTE: Only need to read first line
			FileReader hmtFile = new FileReader(hmtFileLocation);
			BufferedReader hmtReader = new BufferedReader(hmtFile);
			String text; 
			while((text = hmtReader.readLine()) != null) {
				if (text.contains("Last Updated;")&&lineCount<1) {					
					hmtMetaDataLastUpdated = text.split(";");
					logger.debug("HMT METADATA (text):" + text);	
					logger.debug("HMT METADATA (hmtMetaDataLastUpdated):" + hmtMetaDataLastUpdated[0]);
					logger.debug("HMT METADATA (hmtMetaDataLastUpdated):" + hmtMetaDataLastUpdated[1]);					
				} 
				lineCount++;
			}
			
			hmtFile.close();
			hmtListMetaData.setLastUpdated(hmtMetaDataLastUpdated[1]);			
			hmtListMetaData.setRecordCount(Integer.toString(lineCount));
			
		} catch (FileNotFoundException fnfe) {
			logger.error("Caught FNFException:");
			fnfe.printStackTrace();			
		} catch (IOException ioe) {
			logger.error("Caught IOException:");
			ioe.printStackTrace();			
		}
			
		try {
			CSVReader reader = new CSVReader(
					new FileReader(hmtFileLocation), ';', '\"', 2); // uses ; as sep and skips first two lines
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
			this.hmtList = csv.parse(strat, reader);			
			// Now go through the list and check unmarshalled data
			Iterator<HmtBean> it=hmtList.iterator();
	        while(it.hasNext())
	        {
	          HmtBean hmtBean =(HmtBean)it.next();	  
	          hmtBean.setListLastUpdated(hmtMetaDataLastUpdated[1]);
	          hmtBean.setListRecordCount(Integer.toString(lineCount));
	        }
	        
	        setHmtLoadStatus(true);
	        logger.info("HMT load status has been set to " + this.getHmtLoadStatus());
	        
		} catch (FileNotFoundException fnfe) {
			logger.error("Caught FNFException:");
			fnfe.printStackTrace();
		}
		
	}
	
	// A handle to the unique Singleton instance
	private static UnmarshalHmt _instance = null;
	
	// @return The unique instance of this class
	public static synchronized UnmarshalHmt instance() {
		if (null == _instance) {
			_instance = new UnmarshalHmt();
		}
		return _instance; 
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public List<HmtBean> getHmtList() {
		return this.hmtList;
	}
	
	private void setHmtLoadStatus(Boolean status){
		this.hmtLoadStatus = status;
	}
	
	public Boolean getHmtLoadStatus() {
		return this.hmtLoadStatus;
	}
	
	public HmtMetaData getHmtListMetaData() {
		return this.hmtListMetaData;
	}
	
}
