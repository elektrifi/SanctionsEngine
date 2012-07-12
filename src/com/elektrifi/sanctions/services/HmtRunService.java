package com.elektrifi.sanctions.services;

//import java.io.StringWriter;
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
import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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

import org.apache.log4j.Logger;

import com.elektrifi.sanctions.engine.HmtSearcher;
import com.elektrifi.sanctions.beans.HmtBean;

@Path("/hmtscreening")
public class HmtRunService {

	@Context private HttpHeaders headers;		// contains executed_on_host
	@Context private HttpServletRequest hsr;	// contains request info
	
	// Set up log4j logger
	private static Logger logger = Logger.getLogger(HmtRunService.class);	   
	// Create a searcher for incomning requests	
	// JF Removed call to singleton method...
	private HmtSearcher hmtSearcher = HmtSearcher.getHmtSearcherSingleton();
	//HmtSearcher hmtSearcher = new HmtSearcher();

	// Constructor
	public HmtRunService() {
		super();
	}

	@POST
	@Consumes("application/xml")
	public Response consumeTextHmtScreeningRequest(InputStream message) {

		int requestId		= 0;
		String hostName		= "";
		String userName 	= "";
		String userIp		= "";
		String space 		= " ";
		String semicolon	= ";";
		String newline		= "\n";

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
		
		// Unmarshall the input string into POJOs to make processing more structured
		logger.debug("Populating the HMT screeningEntrys...");	
		// Create a list of screeningEntrys (HmtBeans)
		List<HmtBean> screeningEntrys = populateScreeningList(message);		
		logger.debug("Populated the HMT screeningEntrys...");
		logger.debug("screeningEntrys List has size: " + screeningEntrys.size());

		// Process in the list and store it for auditing before processing
		StringBuilder messageSb = new StringBuilder();
        HmtBean hmtAuditBean = new HmtBean();
        //if (screeningEntrys == null)
        //	logger.info("No screening entries.");
        //else {
        	for (Iterator<HmtBean> i = screeningEntrys.iterator(); i.hasNext(); ) {	        		
        		hmtAuditBean = (HmtBean)i.next();
        		messageSb.append(hmtAuditBean.getName6());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getName1());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getName2());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getName3());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getName4());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getName5());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getTitle());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getDob());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getTownOfBirth());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getCountryOfBirth());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getNationality());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getPassportDetails());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getNiNumber());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getPosition());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getAddress1());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getAddress2());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getAddress3());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getAddress4());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getAddress5());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getAddress6());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getPostZipCode());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getCountry());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getOtherInformation());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getGroupType());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getAliasType());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getRegime());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getListedOn());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getLastUpdated());
        		messageSb.append(semicolon);
        		messageSb.append(hmtAuditBean.getGroupId());		
        		messageSb.append(newline);
		    }
		//}					

		// Write inbound request to BLOB in audit table (along with timestamp and user details)
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
			  // This is the InputStream message version
			  byte[] auditMessageBytes = null;
			  int messageSize = messageSb.toString().getBytes().length;
			  logger.debug("Message original length is " 
					  		+ messageSize 
					  		+ " bytes.");
			  try {
				  auditMessageBytes = zipStringToBytes(messageSb.toString());
				  logger.debug("auditMessageString zipped length is " + auditMessageBytes.length + " bytes.");
			  } catch (IOException ioe) {
				  logger.error("Zipping IOException: " + ioe.getMessage());		  
				  throw new WebApplicationException(Response.Status.NOT_FOUND);				  
			  }
			  preparedStatement.setBytes(4, auditMessageBytes); //Inbound message			  
			  preparedStatement.setInt(5, messageSize); 		//Inbound message zipped size
			  java.util.Date now = new java.util.Date(); 		// Local time
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
		
		// Now iterate through the list of screeningEntrys and execute the searches
		logger.info("Starting HmtSearch...");
        int entryCount = 0;
        HmtBean hmtBean = new HmtBean();
        //if (screeningEntrys == null)
        //	logger.info("No screening entries.");
        //else {
        	for (Iterator<HmtBean> i = screeningEntrys.iterator(); i.hasNext(); ) {
        		entryCount++;	        		
        		hmtBean = (HmtBean)i.next();
		        			        
		        // Check Exact Name Match
		        String customerNameRecord = 
		        		hmtBean.getName1() + space
		        		+ hmtBean.getName2() + space
		        		+ hmtBean.getName3() + space
		        		+ hmtBean.getName4() + space
		        		+ hmtBean.getName5() + space		        		
		        		+ hmtBean.getName6();
		        
		        // Strip out any excess spaces
		        customerNameRecord = customerNameRecord.replaceAll("\\b\\s{2,}\\b", " ");
		        customerNameRecord.trim();
		        
		        logger.info("--------------------------------------------------------");
		        logger.info("---> Query is: " + customerNameRecord.toLowerCase());
		        logger.info("--------------------------------------------------------");

		        // Start the local checking process...(must use lowercase as indexed that way)
		        hmtSearcher.checkExactNameMatch(customerNameRecord.toLowerCase());
				// Call Lucene standard searcher to see if there are indexed, spanned matches (with score)		        
		        hmtSearcher.getStandardSearchResults(customerNameRecord.toLowerCase());
				// Call initials searcher (to look for e.g. R.G. Mugabe)		        
		        //hmtSearcher.getInitialsSearchResults(customerNameRecord.toLowerCase());
		        // Call Lucene synonym searcher (to look for e.g. Bob Mugabe)
		        //hmtSearcher.getSynonymSearchResults(customerNameRecord.toLowerCase());
		        
		        // Check addresses
		    }
		//}					
		
		// Create a result (result format TBC - XML *and* then store it as BLOB in MySQL)
		        
		logger.info("#################################");
		logger.info("### End of service fulfilment ###");
		logger.info("#################################");		
		
		return Response.created(URI.create("/" + requestId)).build();
	}
	
	// This method is called if HTML is requested (e.g. via http://localhost:8080/com.elektrifi.sanctions/rest/hmtscreening)
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
		}		
        
        String bgColour;
        if (hostName.contains("localhost")) {
        	bgColour = "#FFddFF"; // pale yellow
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
				+ hmtSearcher.getHmtLoadStatus()
				+ "<p />" 
				+ checkDatabaseAvailability()
				+ "<p />"
				+ "Thanks for your interest."
				+ "</basefont>"
				+ "</body>" 
				+ "</html> ";
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

	private List<HmtBean> populateScreeningList(InputStream is) {
		
		List<HmtBean>screeningEntrys = new ArrayList<HmtBean>();
		// Process in the inputStream and populate a list of HmtBeans
		String delimiter = ";";
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			String [] beanValues;
			while ( (line = br.readLine()) != null)
			{				
				logger.debug("populateScreeningList: " + line);
				beanValues = line.split(delimiter);
				HmtBean hmtBean = new HmtBean();
				hmtBean.setName6(beanValues[0]);
				hmtBean.setName1(beanValues[1]);
				hmtBean.setName2(beanValues[2]);
				hmtBean.setName3(beanValues[3]);
				hmtBean.setName4(beanValues[4]);
				hmtBean.setName5(beanValues[5]);
				hmtBean.setTitle(beanValues[6]);
				hmtBean.setDob(beanValues[7]);
				hmtBean.setTownOfBirth(beanValues[8]);
				hmtBean.setCountryOfBirth(beanValues[9]);
				hmtBean.setNationality(beanValues[10]);
				hmtBean.setPassportDetails(beanValues[11]);
				hmtBean.setNiNumber(beanValues[12]);
				hmtBean.setPosition(beanValues[13]);
				hmtBean.setAddress1(beanValues[14]);
				hmtBean.setAddress2(beanValues[15]);
				hmtBean.setAddress3(beanValues[16]);
				hmtBean.setAddress4(beanValues[17]);
				hmtBean.setAddress5(beanValues[18]);
				hmtBean.setAddress6(beanValues[19]);
				hmtBean.setPostZipCode(beanValues[20]);
				hmtBean.setCountry(beanValues[21]);
				hmtBean.setOtherInformation(beanValues[22]);
				hmtBean.setGroupType(beanValues[23]);
				hmtBean.setAliasType(beanValues[24]);
				hmtBean.setRegime(beanValues[25]);
				hmtBean.setListedOn(beanValues[26]);
				hmtBean.setLastUpdated(beanValues[27]);
				hmtBean.setGroupId(beanValues[28]);	
				logger.debug("populateScreeningList hmtBean is : " 
								+ hmtBean.getName6() + " " 
								+ hmtBean.getName1());
				screeningEntrys.add(hmtBean);
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		}				
		return screeningEntrys;
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

}
