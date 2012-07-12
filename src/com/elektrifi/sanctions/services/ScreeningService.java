package com.elektrifi.sanctions.services;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.StreamingOutput;
import javax.servlet.http.HttpServletRequest;
//import com.google.gson.Gson;

import org.apache.log4j.Logger;

import com.elektrifi.sanctions.engine.HmtSearcher;
import com.elektrifi.sanctions.engine.SdnSearcher;
import com.elektrifi.sanctions.beans.ObjectFactory;
import com.elektrifi.sanctions.beans.ResultsBean;
import com.elektrifi.sanctions.beans.ResultsBeanList;
import com.elektrifi.sanctions.beans.ResultsCarrierBean;
import com.elektrifi.sanctions.beans.ScreeningList;
import com.elektrifi.sanctions.beans.ScreeningList.ScreeningEntry;

@Path("/screening")
public class ScreeningService {

	@Context private HttpHeaders headers;		// contains executed_on_host
	@Context private HttpServletRequest hsr;	// contains request info
	
	// Set up log4j logger
	private static Logger logger = Logger.getLogger(ScreeningService.class);
	
	// Create ScreeningXXX objects
	private ObjectFactory screeningFactory = new ObjectFactory();
	private ScreeningList screeningList = screeningFactory.createScreeningList();
	private ScreeningList.ScreeningEntry screeningEntry = screeningFactory.createScreeningListScreeningEntry();		
	private List<ScreeningList.ScreeningEntry> screeningEntrys = screeningList.getScreeningEntry();
		
	// Create searchers for incoming requests
	// JF Restored call to singleton
	private HmtSearcher hmtSearcher = HmtSearcher.getHmtSearcherSingleton();
	private SdnSearcher sdnSearcher = SdnSearcher.getSdnSearcherSingleton();

	// Create a ResultsBeanList
	private ResultsBeanList resultsBeanList = new ResultsBeanList(); 
	
	// Constructor
	public ScreeningService() {
		super();
	}

	@POST
	@Consumes("application/xml")
	public Response consumeTextScreeningRequest(InputStream message) {

		int audit_id		= -1;
		String hostName		= "";
		String userName 	= "";
		String userIp		= "";
		String space 		= " ";
		String semicolon	= ";";
		String newline		= "\n";

		// Puts a datestamp in the logs
		logger.info("\n\n");
		logger.info("##########################################");
		logger.info("### Screening Service request received ###");
		logger.info("##########################################");
		
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
		logger.debug("Populating the screeningEntrys...");	
		screeningEntrys = populateScreeningList(message);
		
		logger.debug("Populated the HMT screeningEntrys...");	
		logger.debug("screeningEntrys List has size: " + screeningEntrys.size());

		// Process in the list and store it for auditing before processing
		StringBuilder messageSb = new StringBuilder();
		int entryCount = 0;
        if (screeningEntrys == null)
        	logger.info("No screening entries.");
        else {
        	logger.debug("Building message to store in database...");
        	for (Iterator<ScreeningEntry> i = screeningEntrys.iterator(); i.hasNext(); ) {

        		screeningEntry = (ScreeningEntry)i.next();
        		
        		messageSb.append(screeningEntry.getUid());
        		messageSb.append(semicolon);
        		messageSb.append(screeningEntry.getLastName());
        		messageSb.append(semicolon);
        		messageSb.append(screeningEntry.getFirstName());
        		messageSb.append(semicolon);
        		messageSb.append(screeningEntry.getScreeningType());        		
        		messageSb.append(newline);      
        		
        		logger.debug("Interim messageSb is..." + messageSb.toString());
		    }
        	logger.debug("Message to be stored in database is...");
        	logger.debug(messageSb.toString());
		}					
        
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
	              audit_id = resultSet.getInt(1); 
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
		logger.debug("Starting searches...");
        entryCount = 0;
                
        if (screeningEntrys == null)
        	logger.info("No screening entries.");
        else {
        	for (Iterator<ScreeningEntry> i = screeningEntrys.iterator(); i.hasNext(); ) {
        		
        		entryCount++;	        		
        		screeningEntry = (ScreeningEntry)i.next();
        		ResultsBean hmtResultsBean = new ResultsBean();
        		ResultsBean sdnResultsBean = new ResultsBean();   
        		
        		float hmtTotalScore = 0.0f;
        		float sdnTotalScore = 0.0f;
		        			        
		        // Check Exact Name Match
		        String customerNameRecord = 
		        	screeningEntry.getFirstName() + space
		        		+ screeningEntry.getLastName();
		        
		        // Strip out any excess spaces
		        customerNameRecord = customerNameRecord.replaceAll("\\b\\s{2,}\\b", " ");
		        customerNameRecord.trim();
		        
		        logger.info("--------------------------------------------------------");
		        logger.info("---> Query is: " + customerNameRecord.toLowerCase());
		        logger.info("--------------------------------------------------------");

		        ResultsCarrierBean resultsCarrierBean = new ResultsCarrierBean();		        
		        
		        // Start the local checking process...(must use lowercase as indexed that way)		        		        
		        //----- HMT EXACT SEARCHES
		        resultsCarrierBean = hmtSearcher.checkExactNameMatch(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        hmtResultsBean.setExactSearchList("HMT");
		        hmtResultsBean.setExactSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        hmtResultsBean.setExactSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        hmtResultsBean.setExactSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        hmtResultsBean.setExactSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        hmtTotalScore = hmtTotalScore + hmtResultsBean.getExactSearchScore();
		        
		        //----- SDN EXACT SEARCHES
		        resultsCarrierBean = sdnSearcher.checkExactNameMatch(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        sdnResultsBean.setExactSearchList("SDN");		        
		        sdnResultsBean.setExactSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        sdnResultsBean.setExactSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        sdnResultsBean.setExactSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        sdnResultsBean.setExactSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        sdnTotalScore = sdnTotalScore + sdnResultsBean.getExactSearchScore();		        

		        // Call Lucene standard searcher to see if there are indexed, spanned matches (with score)
		        //----- HMT LUCENCE SEARCHES
		        resultsCarrierBean = hmtSearcher.getStandardSearchResults(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        hmtResultsBean.setLuceneSearchList("HMT");		        
		        hmtResultsBean.setLuceneSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        hmtResultsBean.setLuceneSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        hmtResultsBean.setLuceneSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        hmtResultsBean.setLuceneSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        hmtTotalScore = hmtTotalScore + hmtResultsBean.getLuceneSearchScore();

		        //----- SDN LUCENE SEARCHES
		        sdnSearcher.getStandardSearchResults(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        sdnResultsBean.setLuceneSearchList("SDN");		        
		        sdnResultsBean.setLuceneSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        sdnResultsBean.setLuceneSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        sdnResultsBean.setLuceneSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        sdnResultsBean.setLuceneSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        sdnTotalScore = sdnTotalScore + sdnResultsBean.getLuceneSearchScore();

		        // Call initials searcher (to look for e.g. R.G. Mugabe)
		        //----- HMT INITIALS SEARCHES
		        resultsCarrierBean = hmtSearcher.getInitialsSearchResults(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        hmtResultsBean.setInitialsSearchList("HMT");		        
		        hmtResultsBean.setInitialsSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        hmtResultsBean.setInitialsSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        hmtResultsBean.setInitialsSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        hmtResultsBean.setInitialsSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        hmtTotalScore = hmtTotalScore + hmtResultsBean.getInitialsSearchScore();
		        
		        //----- SDN INITIALS SEARCHES
		        sdnSearcher.getInitialsSearchResults(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        sdnResultsBean.setInitialsSearchList("SDN");		        
		        sdnResultsBean.setInitialsSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        sdnResultsBean.setInitialsSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        sdnResultsBean.setInitialsSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        sdnResultsBean.setInitialsSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        sdnTotalScore = sdnTotalScore + sdnResultsBean.getInitialsSearchScore();

		        // Call Lucene synonym searcher (to look for e.g. Bob Mugabe)
		        //----- HMT SYNONYM SEARCHES
		        resultsCarrierBean = hmtSearcher.getSynonymSearchResults(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        hmtResultsBean.setSynonymSearchList("HMT");
		        hmtResultsBean.setSynonymSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        hmtResultsBean.setSynonymSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        hmtResultsBean.setSynonymSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        hmtResultsBean.setSynonymSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        hmtTotalScore = hmtTotalScore + hmtResultsBean.getSynonymSearchScore();
		        
		        //----- SDN SYNONYM SEARCHES
		        sdnSearcher.getSynonymSearchResults(customerNameRecord.toLowerCase());
		        logger.debug(resultsCarrierBean.toJson());
		        sdnResultsBean.setSynonymSearchList("SDN");		        
		        sdnResultsBean.setSynonymSearchTerm(resultsCarrierBean.getCarrierSearchTerm());
		        sdnResultsBean.setSynonymSearchResult(resultsCarrierBean.getCarrierSearchResult());
		        sdnResultsBean.setSynonymSearchScore(resultsCarrierBean.getCarrierSearchScore());
		        sdnResultsBean.setSynonymSearchTimestamp(resultsCarrierBean.getCarrierSearchTimestamp());
		        sdnTotalScore = sdnTotalScore + sdnResultsBean.getSynonymSearchScore();
		        
		        // Set totalScores...
		        hmtResultsBean.setTotalScore(hmtTotalScore);
		        sdnResultsBean.setTotalScore(sdnTotalScore);
		        
		        // Check addresses
		        		        
		        // Add ResultsBean to ResultsBeanList 
		        resultsBeanList.add(hmtResultsBean);
		        resultsBeanList.add(sdnResultsBean);
		        
		    }
		}					

        // Debug output
        logger.debug(resultsBeanList.printResultsBeanList());
        
		// Create a result (result format TBC - XML *and* then store it as BLOB in MySQL)
        int resultsRequestId = -1; 
        resultsRequestId = persistResultsBeanList(resultsBeanList, audit_id);
        
		logger.info("###########################################");
		logger.info("### End of screening service fulfilment ###");
		logger.info("###########################################");		

		// Return an identifying URL for the search 
		// So to access results, need to make a GET call to the returned URL
		return Response.created(URI.create("/" + resultsRequestId)).build();
	}
	
@GET
@Path("{id}")
@Produces("application/xml")
	public StreamingOutput getXmlResults(@PathParam("id") int id) {
		
		final int resultId = id;
		logger.debug("Received request to GET results record in XML format for id " + id); 
		//ResultsBeanList resultBeanList = resultsBeanList.get(id);
		
		//if (resultBeanList == null) {
		//	logger.info("No record found - ResultsBeanList is null!");
		//	throw new WebApplicationException(
		//			Response.Status.NOT_FOUND);
		//}
		
		// Output resultBeanList for debug		
		//logger.debug(resultBeanList.printResultsBeanList());
		
		return new StreamingOutput() {
			public void write(OutputStream outputStream)
				throws IOException, WebApplicationException {
					//outputResultsListXml(outputStream, resultId);
					outputResultsListXmlBytes(outputStream, resultId);
					}
			};
		}

@GET
@Path("{id}")
@Produces("application/json")
	public StreamingOutput getJsonResults(@PathParam("id") int id) {
		
		final int resultId = id;
		logger.debug("Received request to GET results record in JSON format for id " + id); 
		//ResultsBeanList resultBeanList = resultsBeanList.get(id);
		
		//logger.info("Retrieved resultsBeanList for id " + id + "...");
		
		/**
		if (resultBeanList == null) {
			logger.info("No record found - ResultsBeanList is null!");
			throw new WebApplicationException(
					Response.Status.NOT_FOUND);
		}
		**/
		// Output resultBeanList for debug		
		//logger.debug(resultBeanList.printResultsBeanList());
		
		return new StreamingOutput() {
			public void write(OutputStream outputStream)
				throws IOException, WebApplicationException {
					//outputResultsListJson(outputStream, resultBeanList);
					outputResultsListJsonBytes(outputStream, resultId);
					}
			};
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
        	bgColour = "#ddFFFF"; // green-ish
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
				+ "<h3>HMT Service Status</h3>"
				+ hmtSearcher.getHmtLoadStatus()
				+ "<p />"
				+ "<h3>SDN Service Status</h3>"
				+ sdnSearcher.getSdnLoadStatus()				
				+ "<p />" 
				+ "<h3>Database Status</h3>"
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

	private List<ScreeningList.ScreeningEntry> populateScreeningList(InputStream is) {
		
		ScreeningList screeningList = screeningFactory.createScreeningList();
		//ScreeningList.ScreeningEntry screeningEntry = screeningFactory.createScreeningListScreeningEntry();		
		List<ScreeningList.ScreeningEntry> screeningEntrys = screeningList.getScreeningEntry();
			
		//List<ScreeningList>screeningEntrys = new ArrayList<ScreeningList>();
		//List<ScreeningList>screeningEntrys;
		//ScreeningEntry screeningEntry = new ScreeningEntry();
		
		// Process in the inputStream and populate a list of ScreeningEntrys
		String delimiter = ";";
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			String [] beanValues;
			while ( (line = br.readLine()) != null)
			{				
				ScreeningList.ScreeningEntry screeningEntry = screeningFactory.createScreeningListScreeningEntry();				
				logger.debug("populateScreeningList input line is: " + line);
				beanValues = line.split(delimiter);
				screeningEntry.setUid(Integer.parseInt(beanValues[0].trim())); 	//Customer identifier
				screeningEntry.setLastName(beanValues[1]);  //Last name		
				screeningEntry.setFirstName(beanValues[2]); // All other names concatenated together
				screeningEntry.setTitle(beanValues[3]); // Titles if any
				logger.debug("populateScreeningList screeningEntry is thus: " 
								+ screeningEntry.getFirstName() + " " 
								+ screeningEntry.getLastName());
				
				//screeningEntrys.getScreeningEntry().add(screeningEntry);
				screeningEntrys.add(screeningEntry);				
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		}				
		return screeningEntrys;
	}
	
	/**
	protected void outputResultsListXml(OutputStream os, int id)
		throws IOException {
		PrintWriter out = new PrintWriter(os);
		//String newline		= "\n";	
		ResultsBeanList resultBeanList = resultsBeanList.get(id);
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<results>");	
		
		for (Iterator<ResultsBean> i = ResultsBeanList.getListOfResultsBeans().iterator(); i.hasNext(); ) {

			ResultsBean resultsBean = (ResultsBean) i.next();
			out.println(resultsBean.toXml());
			logger.debug(resultsBean.toXml());
		}
		out.println("</results>");
		out.close();
	}	
	 **/
	
	protected void outputResultsListXmlBytes(OutputStream os, int id)
		throws IOException {
		PrintWriter out = new PrintWriter(os);
		
		// Changed this on 20 March 2011 to include XML header
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" 
						+ ResultsBeanList.getBlobXml(id));
		out.close();
	}	
		
/**
	protected void outputResultsListJson(OutputStream os, int id)	
		throws IOException {
			PrintWriter out = new PrintWriter(os);
	
			for (Iterator<ResultsBean> i = ResultsBeanList.getListOfResultsBeans().iterator(); i.hasNext(); ) {
				ResultsBean resultsBean = (ResultsBean) i.next();
				out.println(resultsBean.toJson());
				logger.debug(resultsBean.toJson());
			}	
			out.close();
	}	
**/	

	protected void outputResultsListJsonBytes(OutputStream os, int id) 
		throws IOException {
		
			PrintWriter out = new PrintWriter(os);			
			out.println(ResultsBeanList.getBlobJson(id));
			out.close();			
	}
	
	private int persistResultsBeanList(ResultsBeanList resultsBeanList, int audit_id) {

		// Write results to BLOB in results table (along with timestamp)
		int resultsRequestId = -1;
		try {
			  javax.naming.Context ctx = new InitialContext();
			  DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");
			  
			  Connection conn = ds.getConnection();
			  PreparedStatement preparedStatement 	= null;
			  ResultSet resultSet 					= null;
			  String statementString 				= "";			  
			  			  
			  statementString = "INSERT INTO results " 
				  				+ "(RESULTS_LIST_STRING, RESULTS_LIST_XML,"
				  				+ "	RESULTS_LIST_JSON ,RESULTS_TIMESTAMP," 
				  				+ " FK_AUDIT_ID) "  
				  				+ " VALUES (?, ?, ?, ?, ?)";
			  preparedStatement = conn.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			  // This is the ResultsBeanList
			  byte[] resultsBeanListStringBytes = null;
			  byte[] resultsBeanListXmlBytes 	= null;
			  byte[] resultsBeanListJsonBytes 	= null;
			  try {
				  logger.debug("Storing the following results (String)...\n" + resultsBeanList.printResultsBeanList());
				  logger.debug("Storing the following results (XML)...\n" + resultsBeanList.printResultsBeanListXml());
				  logger.debug("Storing the following results (JSON)...\n" + resultsBeanList.printResultsBeanListJson());
				  logger.debug("resultsBeanListString unzipped length is " + resultsBeanList.printResultsBeanList().length() + " bytes.");				  
				  resultsBeanListStringBytes 	= zipStringToBytes(resultsBeanList.printResultsBeanList());
				  resultsBeanListXmlBytes 		= zipStringToBytes(resultsBeanList.printResultsBeanListXml());				  
				  resultsBeanListJsonBytes 		= zipStringToBytes(resultsBeanList.printResultsBeanListJson());				  
				  logger.debug("resultsBeanListBytes zipped length is " + resultsBeanListStringBytes.length + " bytes.");
			  } catch (IOException ioe) {
				  logger.error("Zipping IOException: " + ioe.getMessage());		  
				  throw new WebApplicationException(Response.Status.NOT_FOUND);				  
			  }
			  preparedStatement.setBytes(1, resultsBeanListStringBytes);	//Inbound string message			  
			  preparedStatement.setBytes(2, resultsBeanListXmlBytes); 		//Inbound XML message
			  preparedStatement.setBytes(3, resultsBeanListJsonBytes); 		//Inbound JSON message			  
			  java.util.Date now = new java.util.Date(); 					// Local time
			  preparedStatement.setTimestamp(4, new Timestamp(now.getTime())); //timestamp
			  preparedStatement.setInt(5, audit_id); 						// fk_audit_id
			  preparedStatement.executeUpdate();
			  resultSet = preparedStatement.getGeneratedKeys();
	          if ( resultSet != null && resultSet.next() ) 
	          { 
	              resultsRequestId = resultSet.getInt(1); 
	          }          		  
	          conn.close();
			  
		} catch (NamingException nae) {
			logger.error("PERSIST Naming Exception: " + nae.getMessage());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("PERSIST SQLState: " + sqe.getSQLState());
			logger.error("PERSIST VendorError: " + sqe.getErrorCode());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);	         
	   	} 
		
		return resultsRequestId;
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