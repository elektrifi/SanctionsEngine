package com.elektrifi.sanctions.services;

import java.io.StringWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.elektrifi.xml.screeninglist.ScreeningList;
import com.elektrifi.xml.screeninglist.ScreeningList.ScreeningEntry;

import com.elektrifi.sanctions.engine.SdnSearcher;

@Path("/sdnscreening")
public class SdnRunService {
	
	@Context private HttpHeaders headers;		// contains executed_on_host
	@Context private HttpServletRequest hsr;	// contains request info
	
	// Set up log4j logger
	private static Logger logger = Logger.getLogger(SdnRunService.class);
	// JF
	SdnSearcher sdnSearcher = SdnSearcher.getSdnSearcherSingleton();
	//SdnSearcher sdnSearcher = new SdnSearcher();

	// Constructor
	public SdnRunService() {
		super();
	}
		
	@POST
	@Consumes("application/xml")
	public Response consumeSdnScreeningRequest(ScreeningList screeningList) {
		
		int requestId	= 0;
		String hostName	= "";
		String userName = "";
		String userIp	= "";
		StringWriter auditXmlSw = new StringWriter(); 
		String space	= " ";

		// Puts a datestamp in the logs
		logger.info("\n\n");
		logger.info("################################");
		logger.info("### Service request received ###");
		logger.info("################################");
		
		// HttpHeaders info
		List<String> hostData = headers.getRequestHeaders().get("host");
		Iterator<String> it = hostData.iterator();
		while(it.hasNext()) {
			hostName = (String) it.next();
			logger.info("Service request was fielded by: " + hostName);			
		}		
		
		// HttpServletRequest info
		userIp = hsr.getRemoteAddr() + ":" + hsr.getRemotePort();
		if (hsr.getRemoteUser() != null) {
			userName = hsr.getRemoteUser();
		} else {
			userName = "No username assigned";
		}
				
		// Other useful HSR output written to logs
		logger.debug("Request received from remote address: " 
				+ hsr.getRemoteAddr() + ":" + hsr.getRemotePort()); // IP number:port
		logger.debug("Request received from remote host: " 
				+ hsr.getRemoteHost() + ":" + hsr.getRemotePort());	// FQ hostname:port
		logger.debug("Request received from remote user: " + userName); // Login of user
				
		// Marshall POJOs to XML and store in audit table (for the record)
		try {
			JAXBContext jc = JAXBContext.newInstance("com.elektrifi.xml.screeninglist");			
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.marshal(screeningList, auditXmlSw);
			
		} catch( MarshalException me ) {
			logger.fatal( "Caught MarshalException: " );
			me.printStackTrace();
		} catch( JAXBException je ) { 
			logger.fatal( "Caught JAXBException: " );
			je.printStackTrace();
		} 
		
		// Write XML to BLOB in audit table (along with timestamp and user details)
		try {
			  javax.naming.Context ctx = new InitialContext();
			  DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");
			  
			  Connection conn = ds.getConnection();
			  PreparedStatement preparedStatement 	= null;
			  ResultSet resultSet 					= null;
			  String statementString 				= "";
			  			  
			  statementString = "INSERT INTO audit (USER_NAME, USER_IP, EXECUTED_ON_HOST, MESSAGE, MESSAGE_SIZE, LAST_UPDATED) "  
				  				+ " VALUES (?, ?, ?, ?, ?, ?)";
			  preparedStatement = conn.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			  preparedStatement.setString(1, userName); 		//user_name 
			  preparedStatement.setString(2, userIp);	 		//user_ip
			  preparedStatement.setString(3, hostName); 		//executed_on_host
			  
			  // Zip up the auditXmlSw to save space before storing
			  // Orig version (no compression)			  
			  //byte[] auditXmlBytes = auditXmlSw.toString().getBytes();
			  byte[] auditXmlBytes = null;
			  int messageSize = auditXmlSw.toString().getBytes().length;
			  logger.debug("auditXMLString original length is " + messageSize + " bytes.");
			  
			  try {
				  auditXmlBytes = zipStringToBytes(auditXmlSw.toString());
				  logger.debug("auditXMLString zipped length is " + auditXmlBytes.length + " bytes.");
			  } catch (IOException ioe) {
				  logger.error("Zipping IOException: " + ioe.getMessage());		  
				  throw new WebApplicationException(Response.Status.NOT_FOUND);				  
			  }
			  preparedStatement.setBytes(4, auditXmlBytes); // Message
			  preparedStatement.setInt(5, messageSize); 	// Message Size
			  
			  java.util.Date now = new java.util.Date(); 			// Local time
			  preparedStatement.setTimestamp(6, new Timestamp(now.getTime())); //timestamp
			  preparedStatement.executeUpdate();
			  resultSet = preparedStatement.getGeneratedKeys();
	          if ( resultSet != null && resultSet.next() ) 
	          { 
	              requestId = resultSet.getInt(1); 
	          }          		  
	          conn.close();
			  
		} catch (NamingException nae) {
			logger.error("POST Naming Exception: " + nae.getMessage());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("POST SQLState: " + sqe.getSQLState());
			logger.error("POST VendorError: " + sqe.getErrorCode());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);	         
	   	} 
				
		// Use ScreeningList and ScreeningEntry(s) methods to screen against SDN list...
		// ...and loop over all SDN POJO's to find exact match		
		// First, get list of SdnEntrys
		ScreeningList.ScreeningEntry screeningEntry = new ScreeningList.ScreeningEntry();
        List <ScreeningEntry>screeningEntryList = screeningList.getScreeningEntry();

        int entryCount = 0; 
        if (screeningEntryList == null)
        	logger.info("No screening entries.");
        else {
        	for (Iterator<ScreeningEntry> i = screeningEntryList.iterator(); i.hasNext(); ) {
        		entryCount++;	        		
        		screeningEntry = (ScreeningList.ScreeningEntry)i.next();
		        			        
		        // Check Exact Name Match
		        String customerNameRecord = 
		        		screeningEntry.getFirstName() 
		        		+ space 
		        		+ screeningEntry.getLastName();
		        
		        logger.info("--------------------------------------------------------");
		        logger.info("---> Query is: " + customerNameRecord.toLowerCase());
		        logger.info("--------------------------------------------------------");

		        // Start the local checking process...(must use lowercase as indexed that way)
		        sdnSearcher.checkExactNameMatch(customerNameRecord.toLowerCase());
				// Call Lucene standard searcher to see if there are indexed, spanned matches (with score)		        
		        sdnSearcher.getStandardSearchResults(customerNameRecord.toLowerCase());
				// Call initials searcher (to look for e.g. R.G. Mugabe)		        
		        sdnSearcher.getInitialsSearchResults(customerNameRecord.toLowerCase());
		        // Call Lucene synonym searcher (to look for e.g. Bob Mugabe)
		        sdnSearcher.getSynonymSearchResults(customerNameRecord.toLowerCase());
		        
		        // Check 
		        // Handle addresses
		        /**
		        addressList = screeningEntry.getAddressList();
		        if ( addressList == null ) {
		        	logger.debug("No Address list entries.");
		        } else {
		        	List<ScreeningList.ScreeningEntry.AddressList.Address> addresses = addressList.getAddress();
		        	for (Iterator<?> j = addresses.iterator(); j.hasNext() ; ) {
		        		address = (ScreeningList.ScreeningEntry.AddressList.Address)j.next();
		        		logger.debug("\t address (" + address.getUid() + ") is: "
		        			+ address.getAddress1() + space
		        			+ address.getAddress2() + space
		        			+ address.getAddress3() + space
		        			+ address.getCity() + space
		        			+ address.getStateOrProvince() + space
		        			+ address.getPostalCode() + space
		        			+ address.getCountry());
		        	}
		        }
		        **/
		    }
		}					
		
		// Create a result (result format TBC - XML *and* then store it as BLOB in MySQL)
		        
		logger.info("#################################");
		logger.info("### End of service fulfilment ###");
		logger.info("#################################");

		return Response.created(URI.create("/" + requestId)).build();	  
	}
	
	// This method is called if HTML is requested (e.g. via http://localhost:8080/com.elektrifi.sanctions/rest/sdnscreening)
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String serviceCheckHtml() {
		   
		Date dateNow	= new Date();
        SimpleDateFormat dateFormatStr = new SimpleDateFormat("E dd-MMM-yyyy HH:mm:ss z");
        StringBuilder dateStr = new StringBuilder( dateFormatStr.format( dateNow ) );		
		
        // Add some color if running on localhost (127.0.0.1)
		// HttpHeaders info
        String hostName = "";
		List<String> hostData = headers.getRequestHeaders().get("host");
		Iterator<String> it = hostData.iterator();
		while(it.hasNext()) {
			hostName = (String) it.next();
			//logger.info("Service request was fielded by: " + hostName);			
		}		
        
        String bgColour;
        if (hostName.contains("localhost")) {
        	bgColour = "#FFFFdd"; // pale yellow
        } else {
        	bgColour = "#FFFFFF"; // white
        }
        
		return "<html> " 
				+ "<head>"		
				+ "<title>" 
				+ "Hello from Elektrifi Sanctions Screening Rest Service"
				+ "</title>"
				+ "<head>"				
				+ "<body bgcolor=" + "\"" + bgColour + "\"" + ">"
				+ "<font face=\"arial\" size=\"2\">"
				+ "<h2>Hello from Elektrifi Sanctions Screening Rest Service</h2>"
				+ "<p />"
				+ "It is "
				+ dateStr.toString()
				//+ day + "/" + (month + 1) + "/" + year 
				//+ " " + hour + ":" + minute + ":" + second
				+ "<p />"
				+ sdnSearcher.getSdnLoadStatus()
				+ "<p />" 
				+ checkDatabaseAvailability()
				+ "<p />"
				+ "Thanks for your interest."
				+ "</basefont>"
				+ "</body>" 
				+ "</html> ";
	}
	
	/**
	* Gzip the input string into a byte[].
	* @param input
	* @return
	* @throws IOException 
	*/
	private static byte[] zipStringToBytes( String input  ) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
	    bufos.write( input.getBytes() );
	    bufos.close();
	    byte[] retval= bos.toByteArray();
	    bos.close();
	    return retval;
	}
	  
	/**
	 * Unzip a string out of the given gzipped byte array.
	 * @param bytes
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unused")
	private static String unzipStringFromBytes( byte[] bytes ) throws IOException {
		
	    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	    BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(bis));
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    byte[] buf = new byte[1024];
	    int len;
	    while( (len = bufis.read(buf)) > 0 )
	    {
	      bos.write(buf, 0, len);
	    }
	    String retval = bos.toString();
	    bis.close();
	    bufis.close();
	    bos.close();
	    return retval;
	}

	private String checkDatabaseAvailability() {
		
		String dbCheckStr 	= "The database is not available. " 
								+ "It may be asleep, so please try again.";
		int dbCheck 		= -1;
		
		// Select number of records from AUDIT table as check (can be zero)
		// Purpose is to wake database
		try {
			  javax.naming.Context ctx = new InitialContext();
			  DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");
			  
			  Connection conn 						= ds.getConnection();
			  Statement  stmt 						= null;
			  ResultSet resultSet 					= null;
			  String statementString 				= "";
			  			  
			  statementString = "SELECT COUNT(*) FROM audit;";
			  stmt = conn.createStatement();
			  resultSet = stmt.executeQuery(statementString);
			  while (resultSet.next()) {
				  dbCheck = resultSet.getInt(1);
			  }			  
			  resultSet.close();
			  stmt.close();
	          conn.close();
	          
	          if (dbCheck >= 0) {
	        	  dbCheckStr = "The database is awake and available.";
	          }
			  
		} catch (NamingException nae) {
			logger.error("POST Naming Exception: " + nae.getMessage());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("POST SQLState: " + sqe.getSQLState());
			logger.error("POST VendorError: " + sqe.getErrorCode());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);	         
	   	} 
				
		return dbCheckStr;
	}
}
