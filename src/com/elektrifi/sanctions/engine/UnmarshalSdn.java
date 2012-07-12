package com.elektrifi.sanctions.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.tempuri.sdnlist.SdnList;

public class UnmarshalSdn {

	// Set up log4j logger
	private static Logger logger = Logger.getLogger(UnmarshalSdn.class);	
	private Boolean sdnLoadStatus = false;  
	
	SdnList sdnList = new SdnList(); 
	//SdnEntry sdnEntry = new SdnEntry();
	//AddressList addressList = new AddressList();
	//Address address = new Address();
	//AkaList akalist = new AkaList();
	//CitizenshipList citizenshipList = new CitizenshipList();
	
	//List <SdnEntry>sdnEntrys = null;
	//List<Address> addresses = null;
		
	//static String space = " ";
	//static String separator = "|-|";
		
	protected UnmarshalSdn() {

		super(); 
		
		String sdnFileLocation = null; 
		try {
			InputStream inputStream = this.getClass().getClassLoader()
										.getResourceAsStream("config.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			sdnFileLocation = properties.getProperty("SdnFileLocation");
			logger.info("SdnFileLocation read in as " + sdnFileLocation);
			inputStream.close();
		} catch(IOException ioe) {
			logger.error("Caught IOException: ");
			ioe.printStackTrace();
		}
		
		// Unmarshall the sdn.xml file into a set of POJOs 
		// (a list - namely SdnList - of SdnEntrys)
		try {  
			
			JAXBContext jc = JAXBContext.newInstance( "org.tempuri.sdnlist" );
			Unmarshaller u = jc.createUnmarshaller();

			sdnList = (SdnList)u.unmarshal( new FileInputStream(sdnFileLocation));
			logger.info("Sdnlist publish date: " + sdnList.getPublshInformation().getPublishDate());
			logger.info("Sdnlist record count: " + sdnList.getPublshInformation().getRecordCount());			
			setSdnLoadStatus(true);
			logger.info("SDN load status has been set to " + this.getSdnLoadStatus());

		} catch( UnmarshalException ue ) {
			logger.fatal("Caught UnmarshalException: ");
			ue.printStackTrace();
		} catch( JAXBException je ) { 
			logger.error("Caught JAXBException: ");
			je.printStackTrace();
		} catch( IOException ioe ) {
			logger.error("Caught IOException: ");
			logger.error("SdnFileLocation was read in as " + sdnFileLocation);
			ioe.printStackTrace();
		}		
	}
	
	// A handle to the unique Singleton instance
	private static UnmarshalSdn _instance = null;
	
	// @return The unique instance of this class
	public static synchronized UnmarshalSdn instance() {
		if (null == _instance) {
			_instance = new UnmarshalSdn();
		}
		return _instance; 
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public SdnList getSdnList() {
		return this.sdnList;
	}
	
	private void setSdnLoadStatus(Boolean status){
		this.sdnLoadStatus = status;
	}
	
	public Boolean getSdnLoadStatus() {
		return this.sdnLoadStatus;
	}
	
}