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
import com.elektrifi.sanctions.beans.HmtBean;
import com.elektrifi.util.ApplicationConfig;

public class TestClientHmtScreening {

	// Set up log4j logger
	private static Logger logger = Logger.getLogger(TestClientHmtScreening.class);

	public static void main(String[] args) {

		// Read in properties (location of HMT file)
		ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		String serviceUrl = config.getProperty("HmtServiceUrl");				
		String hmtFileLocation = config.getProperty("HmtFileLocation");
		logger.info("\n--------");
		logger.info("ServiceUrl read in as " + serviceUrl);
		logger.info("HmtFileLocation read in as " + hmtFileLocation);
		String semicolon = ";";
		String newline = "\n";
		
		StringBuffer requestXml = new StringBuffer();	    
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
			@SuppressWarnings("unchecked")
			List<HmtBean> hmtList = csv.parse(strat, reader);								
			// Iterate through the list and tag with xml attributes
			requestXml = new StringBuffer();
			Iterator<HmtBean> iterator = hmtList.iterator();
			HmtBean hmtBean = new HmtBean();
			while (iterator.hasNext()) {
				hmtBean = iterator.next();
				requestXml.append(hmtBean.getName6());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getName1());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getName2());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getName3());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getName4());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getName5());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getTitle());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getDob());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getTownOfBirth());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getCountryOfBirth());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getNationality());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getPassportDetails());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getNiNumber());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getPosition());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getAddress1());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getAddress2());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getAddress3());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getAddress4());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getAddress5());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getAddress6());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getPostZipCode());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getCountry());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getOtherInformation());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getGroupType());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getAliasType());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getRegime());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getListedOn());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getLastUpdated());
				requestXml.append(semicolon);
				requestXml.append(hmtBean.getGroupId());		
				requestXml.append(newline);
			}

		} catch (FileNotFoundException fnfe) {
			logger.error("Caught FNFException:");
			fnfe.printStackTrace();			
		}

		// Send the XML to the RESTful service		   						
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
				
	    HttpPost post = new HttpPost(serviceUrl);
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
	    	response = client.execute(post);
	        logger.info("--------------------");
	        logger.info("Response Status Line: " + response.getStatusLine());
	        logger.info("Response Content Encoding: " + response.getLastHeader("Content-Encoding"));
	        logger.info(response.getLastHeader("Content-Length"));
	        logger.info("--------------------");
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
	            
		} finally {
			client.getConnectionManager().shutdown();
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
