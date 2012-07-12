package com.elektrifi.sanctions.services;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.elektrifi.sanctions.beans.UserBean;
import com.google.gson.Gson;

@Path("/login")
public class LoginService {

	@Context private HttpHeaders headers;		// contains executed_on_host
	@Context private HttpServletRequest hsr;	// contains request info
	
	// Set up log4j logger
	private static Logger logger = Logger.getLogger(LoginService.class);
	
	// Constructor
	public LoginService() {
		super();
	}

	@POST
	@Consumes("application/json")
	public Response consumeLoginRequest(InputStream message) {

		String 	hostName		= "";
		String 	userName 		= "";
		//String 	password 		= "";		
		int		session_id		= -1;
		String	sessionKey		= "error-do-not-continue";
		
		logger.info("\n\n");
		logger.info("######################################");
		logger.info("### Login service request received ###");
		logger.info("######################################");
		
		// HttpHeaders info
		List<String> hostData = headers.getRequestHeaders().get("host");
		Iterator<String> it = hostData.iterator();
		while(it.hasNext()) {
			hostName = (String) it.next();
			logger.info("Service request was fielded by: " + hostName);			
		}		
		
		// Other useful HSR output written to logs
		logger.debug("Request received from remote address: " 
				+ hsr.getRemoteAddr() + ":" + hsr.getRemotePort()); // IP number:port
		logger.debug("Request received from remote host: " 
				+ hsr.getRemoteHost() + ":" + hsr.getRemotePort());	// FQ hostname:port
		logger.debug("Request received from remote user: " + userName); // Login of user
		
		// Turn inputStream (userBean JSON) into something we can work with...
		UserBean userBean = populateUserBean(message); 
		
		// Check credentials
		try {
			  javax.naming.Context ctx = new InitialContext();
			  DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");
			  
			  Connection conn = ds.getConnection();
			  PreparedStatement preparedStatement 	= null;
			  ResultSet resultSet 					= null;
			  String statementString 				= "";
			  			  
			  statementString = "SELECT user_id from USERS "
				  + "WHERE username = ? AND password = ?";
			  preparedStatement = conn.prepareStatement(statementString);
			  preparedStatement.setString(1, userBean.getUserName()); 	//userName 
			  preparedStatement.setString(2, userBean.getPassword());	//password	
			  logger.info("PreparedStatement is " + preparedStatement.toString());
			  resultSet = preparedStatement.executeQuery();			  
	          if ( resultSet != null && resultSet.next() ) 
	          {          
	        	  logger.info("Validated " + userBean.getUserName() 
	        			  		+ "/" + userBean.getPassword());
	        	  logger.info("They match user_id..." + resultSet.getInt(1));	
	        	  
	              sessionKey = persistSession(resultSet.getInt(1));
	              logger.info("Returned session key is " + sessionKey);
	          } else {
	        	  logger.info("Username/password validation failed...returned no valid session_id.");
	          }
	          conn.close();
			  
		} catch (NamingException nae) {
			logger.error("POST Login Naming Exception: " + nae.getMessage());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("POST Login SQLState: " + sqe.getSQLState());
			logger.error("POST Login VendorError: " + sqe.getErrorCode());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);	         
	   	} 		
		
		// Create a result (result format TBC - XML *and* then store it as BLOB in MySQL)
        //int resultsRequestId = -1; 

        logger.info("#######################################");
		logger.info("### End of login service fulfilment ###");
		logger.info("#######################################");		

		// Return an identifying URL for the search 
		// So to access results, need to make a GET call to the returned URL
		
		return Response.created(URI.create("/" + sessionKey)).build();
		
		//Gson gson = new Gson();
		//String responseJson = gson.toJson(loginSuccess);
		
		//return Response.ok(responseJson).type("application/json").build();
	}
	
	private static String persistSession(int fk_user_id) {
		
		int session_id = -1;
		
		// Create a session_id based on userName, password, timestamp and IP address
		String sessionKey = createSessionKey();
				
		try {
			  javax.naming.Context ctx = new InitialContext();
			  DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");
			  
			  Connection conn = ds.getConnection();
			  PreparedStatement preparedStatement 	= null;
			  ResultSet resultSet 					= null;
			  String statementString 				= "";
			  			  
			  statementString = "INSERT INTO sessions (SESSION_KEY, SESSION_CREATED, SESSION_UPDATED, FK_USER_ID) "  
				  				+ " VALUES (?, ?, ?, ?)";
			  preparedStatement = conn.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			  preparedStatement.setString(1, sessionKey); 	//session_key
			  java.util.Date now = new java.util.Date();
			  preparedStatement.setTimestamp(2, new Timestamp(now.getTime())); 	//session_created
			  preparedStatement.setTimestamp(3, new Timestamp(now.getTime()));	//session_updated			  
			  preparedStatement.setInt(4, fk_user_id); 			//user_id			  
			  preparedStatement.executeUpdate();
			  resultSet = preparedStatement.getGeneratedKeys();
	          if ( resultSet != null && resultSet.next() ) 
	          { 
	              session_id = resultSet.getInt(1); 
	          }          		  
	          conn.close();
			  
		} catch (NamingException nae) {
			logger.error("POST Login Sessions Naming Exception: " + nae.getMessage());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("POST Login Sessions SQLState: " + sqe.getSQLState());
			logger.error("POST Login Sessions VendorError: " + sqe.getErrorCode());		  
		    throw new WebApplicationException(Response.Status.NOT_FOUND);	         
	   	} 		
		
		return sessionKey;
	}

	private static UserBean populateUserBean(InputStream is) {
		
		//String delimiter = ";";
		UserBean userBean = new UserBean();
		logger.debug(userBean.toString());
		
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			//String [] beanValues;
			Gson gson = new Gson();
			while ( (line = br.readLine()) != null)
			{				
				logger.debug("populateUserBean input line is: " + line);
				userBean = gson.fromJson(line, UserBean.class);
								
				logger.debug(userBean.toString());
				//beanValues = line.split(delimiter);
				//userBean.setUserName(beanValues[0]); 	//userName
				//userBean.setPassword(beanValues[1]);  	//password		
				logger.debug("populateUserBean userBean is thus: " 
								+ userBean.getUserName() + " " 
								+ userBean.getPassword());				
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		}				
		return userBean;
	} 

	private static String createSessionKey() {
		
		// Method 1
		String uuid = UUID.randomUUID().toString();
		
		// Method 2
		SecureRandom random = new SecureRandom();
		String sessionKey = new BigInteger(130, random).toString(64);
				
		logger.debug(uuid + "-" + sessionKey);
		
		return uuid + "-" + sessionKey; 
	}

}