package com.elektrifi.sanctions.client;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;
import com.elektrifi.sanctions.beans.ObjectFactory;
import com.elektrifi.sanctions.beans.ScreeningList;
import com.elektrifi.sanctions.beans.ScreeningList.ScreeningEntry;
import com.elektrifi.util.ApplicationConfig;
import com.elektrifi.util.Stopwatch;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

public class SupercededTestClientAllScreening {

	// Set up log4j logger
	private static Logger logger = Logger.getLogger(SupercededTestClientAllScreening.class);
	
	// Create ScreeningXXX objects
	private static ObjectFactory screeningFactory = new ObjectFactory();
	//private static ScreeningList screeningList = screeningFactory.createScreeningList();
	private static ScreeningList.ScreeningEntry screeningEntry = screeningFactory.createScreeningListScreeningEntry();		
	
	public static void main(String[] args) {

		// Read in properties (location of HMT file)
		ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		String screeningServiceUrl = config.getProperty("ScreeningServiceUrl");
		
		// List of test customers...
		String testCustomerFileLocation = config.getProperty("TestCustomerFileLocation");
		logger.info("\n--------");
		logger.info("ScreeningServiceUrl read in as " + screeningServiceUrl);
		logger.info("TestCustomerFileLocation read in as " + testCustomerFileLocation);
		String semicolon = ";";
		String newline = "\n";
		
		StringBuffer requestXml = new StringBuffer();	    
		try {
			CSVReader reader = new CSVReader(
					new FileReader(testCustomerFileLocation), ';', '\"'); // uses ; as sep and skips first two lines
			ColumnPositionMappingStrategy<ScreeningEntry> strat = 
				new ColumnPositionMappingStrategy<ScreeningEntry>();			
			String[] testCustomerColumns = new String[] {
					"uid","lastName","firstName","screeningType"
			};
			
			strat.setColumnMapping(testCustomerColumns);
			strat.setType(ScreeningEntry.class);
			@SuppressWarnings("rawtypes")
			CsvToBean csv = new CsvToBean();
			@SuppressWarnings("unchecked")
			List<ScreeningEntry> screeningList = csv.parse(strat, reader);								
			// Iterate through the list and tag with xml attributes
			requestXml = new StringBuffer();
			Iterator<ScreeningEntry> iterator = screeningList.iterator();
						
			while (iterator.hasNext()) {
				screeningEntry = (ScreeningEntry)iterator.next();
				requestXml.append(screeningEntry.getUid());
				requestXml.append(semicolon);
				requestXml.append(screeningEntry.getLastName());
				requestXml.append(semicolon);
				requestXml.append(screeningEntry.getFirstName());
				requestXml.append(semicolon);
				requestXml.append(screeningEntry.getScreeningType());
				requestXml.append(newline);
			}

		} catch (FileNotFoundException fnfe) {
			logger.error("Caught FNFException:");
			fnfe.printStackTrace();			
		}

		// Prepare to send the XML to the RESTful service		   	
		DefaultHttpClient client = new DefaultHttpClient();

		// Add GZIP for request and response
		try {
			client.addRequestInterceptor(new HttpRequestInterceptor() {
				public void process(
					final HttpRequest request,
	        		final HttpContext context) throws HttpException, IOException {
	               	if (!request.containsHeader("Accept-Encoding")) {
	               		request.addHeader("Accept-Encoding", "gzip");
	               	}
	        	}
			});			
	            
	        client.addResponseInterceptor(new HttpResponseInterceptor() {
	        	public void process(
	    			final HttpResponse response,
	            	final HttpContext context) throws HttpException, IOException {
	            		HttpEntity entity = response.getEntity();
	            		Header ceheader = entity.getContentEncoding();
	            	if (ceheader != null) {
	            		HeaderElement[] codecs = ceheader.getElements();
	            		for (int i = 0; i < codecs.length; i++) {
	            			if (codecs[i].getName().equalsIgnoreCase("gzip")) {
	            				response.setEntity(
                                    new GzipDecompressingEntity(response.getEntity()));
	            				return;
	            			}
	            		}
	            	}
	            }
	        });	
	        	            
	        // POST	    
	        logger.debug("==============");	    
	        logger.debug("POST");
	        logger.debug("==============");
				
	        HttpPost post = new HttpPost(screeningServiceUrl);
	        StringEntity entity = null;	   
	        try {
	        	entity = new StringEntity(requestXml.toString());
	        } catch (UnsupportedEncodingException uee) {
	           logger.fatal( "Caught UnsupportedEncodingException: " );
	           uee.printStackTrace();
	        }	    
	        entity.setContentType("application/xml");
	        post.setEntity(entity);	
	        HttpClientParams.setRedirecting(post.getParams(),false);
	        HttpResponse response = null; 
	        try {
	           // Start timer
	           Stopwatch stopwatch = new Stopwatch().start();
	           // Send the XML to the RESTful service
	           response = client.execute(post);
	           stopwatch = new Stopwatch().stop();
	           logger.info("--------------------");
	           logger.info("Elapsed time for response is " + stopwatch.getElapsedTimeMs() + " ms.");	    	
	           logger.debug("--------------------");
	           logger.info("Response Status Line: " + response.getStatusLine());
	           logger.debug("Response Content Encoding: " + response.getLastHeader("Content-Encoding"));
	           logger.debug(response.getLastHeader("Content-Length"));
	           logger.info("--------------------");
	           stopwatch.reset();
	        } catch (IOException ioe) {
	        	logger.fatal( "Caught IOException: " );
	        	ioe.printStackTrace();				
	        } 
			
	        if (entity != null) {
	           try {
	        	   String content = EntityUtils.toString(entity);
	        	   logger.debug("Content: " + content);
	        	   logger.info("Uncompressed size: "+content.length());
	           } catch (IOException ioe) {
	        	   logger.error("Caught IOException: ");
	        	   ioe.printStackTrace();
	           }
	        }
			
	        if (response.getStatusLine().getStatusCode() != 201) {
	           throw new RuntimeException("Operation failed: " +
	        		   response.getStatusLine().getStatusCode());
	        }
			
	        String location = response.getLastHeader("Location").getValue();
	        logger.info("Object created at: " + location);
	        logger.info("\n\n");
	    
	        client.getConnectionManager().shutdown();
	        client = new DefaultHttpClient();
	    
	        getXmlResponse(location, client);
	        getJsonResponse(location, client);
    
	    // Now do another GET and retrieve the resultsBeanList in JSON format
		} finally {
			client.getConnectionManager().shutdown();
		}						
	}  

	public static void getXmlResponse(String location, DefaultHttpClient client) {
	    // Now do a GET and retrieve the resultsBeanList in XML format
	    logger.debug("==============");	    
	    logger.debug("GET XML");
	    logger.debug("==============");
	    logger.info("GET XML from " + location);
	    
	    HttpGet getXml = new HttpGet(location);
	    getXml.addHeader("accept", "application/xml");
	    HttpEntity getXmlEntity = null;	 	    
	    HttpResponse getXmlResponse = null; 
	    try { 
	    	//HttpResponse getResponse = client.execute(get);
	    	Stopwatch getXmlStopwatch = new Stopwatch().start();
	    	getXmlResponse = client.execute(getXml);
	    	getXmlStopwatch.stop();
	    	logger.info("--------------------");
	    	logger.info("Elapsed time for response is " + getXmlStopwatch.getElapsedTimeMs() + " ms.");	    	
	        logger.debug("--------------------");
	        logger.info("Response Status Line: " + getXmlResponse.getStatusLine());
	        logger.debug("Response Content Type: " + getXmlResponse.getEntity().getContentType().getValue());	        
	        logger.debug("Response Content Encoding: " + getXmlResponse.getLastHeader("Content-Encoding"));
	        logger.debug(getXmlResponse.getLastHeader("Content-Length"));
	        logger.info("--------------------");
	    } catch (IOException ioe) {
	    	logger.fatal( "Caught IOException: " );
	        ioe.printStackTrace();				
	    } 
			
	    getXmlEntity = getXmlResponse.getEntity(); 
	    if (getXmlEntity != null) {
	    	try {
	    		String xmlContent = EntityUtils.toString(getXmlEntity);
	            logger.debug("Content:\n" + xmlContent);
	            logger.debug("Uncompressed size: "+xmlContent.length());
	        } catch (IOException ioe) {
	        	logger.error("Caught IOException: ");
	            ioe.printStackTrace();
	        }
	    } else {
	    	logger.info("GET XML response for location " 
	    					+ location 
	    					+ "was null.");
	    }
	    	    
	    if (getXmlResponse.getStatusLine().getStatusCode() != 200) {
	    	throw new RuntimeException("Operation failed: " +
	    			getXmlResponse.getStatusLine().getStatusCode());
	    }			
	}
	
	public static void getJsonResponse(String location, DefaultHttpClient client) {
	    logger.debug("==============");	    
	    logger.debug("GET JSON");
	    logger.debug("==============");
	    logger.info("GET JSON from " + location);
	    
	    HttpGet getJson = new HttpGet(location);
	    getJson.addHeader("accept", "application/json");
	    HttpEntity getJsonEntity = null;	 	    
	    HttpResponse getJsonResponse = null; 
	    try { 
	    	Stopwatch getJsonStopwatch = new Stopwatch().start();	    		    	
	    	getJsonResponse = client.execute(getJson);
	    	getJsonStopwatch.stop();
	    	logger.info("--------------------");
	    	logger.info("Elapsed time for response is " + getJsonStopwatch.getElapsedTimeMs() + " ms.");	    		    	
	        logger.debug("--------------------");
	        logger.info("Response Status Line: " + getJsonResponse.getStatusLine());
	        logger.debug("Response Content Type: " + getJsonResponse.getEntity().getContentType().getValue());	        
	        logger.debug("Response Content Encoding: " + getJsonResponse.getLastHeader("Content-Encoding"));
	        logger.debug(getJsonResponse.getLastHeader("Content-Length"));
	        logger.info("--------------------");
	    } catch (IOException ioe) {
	    	logger.fatal( "Caught IOException: " );
	        ioe.printStackTrace();				
	    } 
			
	    getJsonEntity = getJsonResponse.getEntity(); 
	    if (getJsonEntity != null) {
	    	try {
	    		String jsonContent = EntityUtils.toString(getJsonEntity);
	            logger.info("Content:\n" + jsonContent);
	            logger.debug("Uncompressed size: "+jsonContent.length());
	            	    	    
	        } catch (IOException ioe) {
	        	logger.error("Caught IOException: ");
	            ioe.printStackTrace();
	        }
	    } else {
	    	logger.info("GET JSON response for location " 
	    					+ location 
	    					+ "was null.");
	    }
	    	    
	    if (getJsonResponse.getStatusLine().getStatusCode() != 200) {
	    	throw new RuntimeException("Operation failed: " +
	    			getJsonResponse.getStatusLine().getStatusCode());
	    }	    
	    
	}
	
	/**
	* Gzip the input string into a byte[].
	* @param input
	* @return
	* @throws IOException 
	*/
	public static byte[] zipStringToBytes( String input  ) throws IOException {

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
	public static String unzipStringFromBytes( byte[] bytes ) throws IOException {
		
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

    static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent()
            throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }

    }	
	
}