package com.elektrifi.sanctions.beans;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import org.apache.log4j.Logger;

public class ResultsBeanList {

	private static List<ResultsBean> listOfResultsBeans = new ArrayList<ResultsBean>();
	private static Logger logger = Logger.getLogger(ResultsBeanList.class);
	
	public ResultsBeanList() {
		
	}
	
	public static List<ResultsBean> getListOfResultsBeans() {
		return listOfResultsBeans;
	}

	public static void debugResultsBeanList(List<ResultsBean> resultsBeanList) {
		for (ResultsBean r:resultsBeanList) {
			logger.debug(r.toString());
		}
	}
	
	public String printResultsBeanList() {
		StringBuffer outputSb = new StringBuffer();
		String newline = "\n";
		for (ResultsBean r:ResultsBeanList.getListOfResultsBeans()) {
			outputSb.append(r.toString());
			outputSb.append(newline);
			logger.debug(r.toString());
			logger.debug(newline);
		}
		return outputSb.toString();
	}

	public String printResultsBeanListXml() {
		StringBuffer outputSb = new StringBuffer();
		String newline = "\n";
		String tab = "\t";
		outputSb.append("<resultList>");
		for (ResultsBean r:ResultsBeanList.getListOfResultsBeans()) {
			outputSb.append(tab);
			outputSb.append(r.toXml());
			outputSb.append(newline);
			logger.debug(r.toXml());
			logger.debug(newline);
		}
		outputSb.append("</resultList>");
		return outputSb.toString();		
	}

	public String printResultsBeanListJson() {
		
		Gson gson = new Gson();
		StringBuffer outputSb = new StringBuffer(); 
		
		for (ResultsBean r:ResultsBeanList.getListOfResultsBeans()) {
			outputSb.append(gson.toJson(r));
		}
		return outputSb.toString();		
	}
	
	public void add(ResultsBean resultsBean) {
		listOfResultsBeans.add(resultsBean);
	}
	
	public static String getBlobString(int id) {

		//ResultsBeanList resultsBeanList = new ResultsBeanList();
		Blob blob 					= null;
		Statement  stmt 			= null;
		ResultSet resultSet 		= null;
		Connection conn 			= null;
		
		// Select resultBeanList record from RESULTS table		
		try {
			javax.naming.Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");

			conn 					= ds.getConnection();
			stmt 					= null;
			resultSet 				= null;
			String statementString 	= "";
		  
			statementString = "SELECT results_list_string FROM results " 
							  + "WHERE results_id = " + id;
			logger.debug("Getting results_list_string blob for results_id = " + id);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(statementString);
			while (resultSet.next()) {
				blob = resultSet.getBlob("results_list_string");
				// TODO
				//resultsBeanList = resultSet.getBytes(1);
			}			  
			resultSet.close();
			stmt.close();
			conn.close();			
			
			logger.debug("results_list_string blob length is --->" + blob.length());
			byte[] bdata = blob.getBytes(1, (int) blob.length());
			String blobText = "";
			try {
				blobText = unzipStringFromBytes(bdata);
				logger.debug("results_list_string blobText is...\n" + blobText);
			} catch (IOException ioe) {
				logger.error("Caught IOException:");
				ioe.printStackTrace();
			}
					
			//return blob.getBytes(1, (int) blob.length());
			return blobText;
			
		} catch (NamingException nae) {
			logger.error("GET BLOB STRING Results Naming Exception: " + nae.getMessage());		  
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("GET BLOB STRING Results SQLState: " + sqe.getSQLState());
			logger.error("GET BLOB STRING Results VendorError: " + sqe.getErrorCode());		  
				throw new WebApplicationException(Response.Status.NOT_FOUND);	         
		} 

	}
	
	public static String getBlobJson(int id) {

		//ResultsBeanList resultsBeanList = new ResultsBeanList();
		Blob blob 					= null;
		Statement  stmt 			= null;
		ResultSet resultSet 		= null;
		Connection conn 			= null;
		
		// Select resultBeanList record from RESULTS table		
		try {
			javax.naming.Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");

			//Connection conn 			= ds.getConnection();
			conn 			= ds.getConnection();
			//Statement stmt 				= null;
			stmt 				= null;
			//ResultSet resultSet 		= null;
			resultSet 		= null;
			String statementString 		= "";
		  
			statementString = "SELECT results_list_json FROM results " 
							  + "WHERE results_id = " + id;
			logger.debug("Getting results_list_json blob for results_id = " + id);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(statementString);
			while (resultSet.next()) {
				blob = resultSet.getBlob("results_list_json");
				// TODO
				//resultsBeanList = resultSet.getBytes(1);
			}			  
			resultSet.close();
			stmt.close();
			conn.close();			
			
			logger.debug("results_list_json blob length is --->" + blob.length());
			byte[] bdata = blob.getBytes(1, (int) blob.length());
			String blobText = "";
			try {
				blobText = unzipStringFromBytes(bdata);
				logger.debug("results_list_json blobText is...\n" + blobText);
			} catch (IOException ioe) {
				logger.error("Caught IOException:");
				ioe.printStackTrace();
			}
					
			return blobText;
			
		} catch (NamingException nae) {
			logger.error("GET BLOB JSON Results Naming Exception: " + nae.getMessage());		  
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("GET BLOB JSON Results SQLState: " + sqe.getSQLState());
			logger.error("GET BLOB JSON Results VendorError: " + sqe.getErrorCode());		  
				throw new WebApplicationException(Response.Status.NOT_FOUND);	         
		} 

	}

	public static String getBlobXml(int id) {

		//ResultsBeanList resultsBeanList = new ResultsBeanList();
		Blob blob 					= null;
		Statement  stmt 			= null;
		ResultSet resultSet 		= null;
		Connection conn 			= null;
		
		// Select resultBeanList record from RESULTS table		
		try {
			javax.naming.Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");

			conn 					= ds.getConnection();
			stmt 					= null;
			resultSet 				= null;
			String statementString 	= "";
		  
			statementString = "SELECT results_list_xml FROM results " 
							  + "WHERE results_id = " + id;
			logger.debug("Getting results_list_xml blob for results_id = " + id);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(statementString);
			while (resultSet.next()) {
				blob = resultSet.getBlob("results_list_xml");
				// TODO
				//resultsBeanList = resultSet.getBytes(1);
			}			  
			resultSet.close();
			stmt.close();
			conn.close();			
			
			logger.debug("results_list_xml blob length is --->" + blob.length());
			byte[] bdata = blob.getBytes(1, (int) blob.length());
			String blobText = "";
			try {
				blobText = unzipStringFromBytes(bdata);
				logger.debug("results_list_xml blobText is...\n" + blobText);
			} catch (IOException ioe) {
				logger.error("Caught IOException:");
				ioe.printStackTrace();
			}
					
			return blobText;
			
		} catch (NamingException nae) {
			logger.error("GET BLOB XML Results Naming Exception: " + nae.getMessage());		  
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("GET BLOB XML Results SQLState: " + sqe.getSQLState());
			logger.error("GET BLOB XML Results VendorError: " + sqe.getErrorCode());		  
				throw new WebApplicationException(Response.Status.NOT_FOUND);	         
		} 

	}
	
	public ResultsBeanList get(int id) {
		
		ResultsBeanList resultsBeanList = new ResultsBeanList();
	
		// Select resultBeanList record from RESULTS table		
		try {
			javax.naming.Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/sanctions_tx");

			Connection conn 			= ds.getConnection();
			Statement  stmt 			= null;
			ResultSet resultSet 		= null;
			String statementString 		= "";
		  
			statementString = "SELECT results_list FROM results " 
							  + "WHERE results_id = " + id;
			logger.info("Getting results_list message");
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(statementString);
			while (resultSet.next()) {
				// TODO
				//resultsBeanList = resultSet.getBytes(1);
			}			  
			resultSet.close();
			stmt.close();
			conn.close();

		} catch (NamingException nae) {
			logger.error("GET Results Naming Exception: " + nae.getMessage());		  
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		} catch (SQLException sqe) {		  
			logger.error("GET Results SQLState: " + sqe.getSQLState());
			logger.error("GET Results VendorError: " + sqe.getErrorCode());		  
				throw new WebApplicationException(Response.Status.NOT_FOUND);	         
		} 

		// return dbCheckStr;
		
		return resultsBeanList; 
	}

	public String toJson() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		
		return json; 
	}

	/**
	 * Unzip a string out of the given gzipped byte array.
	 * @param bytes
	 * @return
	 * @throws IOException 
	 */

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