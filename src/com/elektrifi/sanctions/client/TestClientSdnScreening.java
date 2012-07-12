package com.elektrifi.sanctions.client;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.MarshalException;

import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.text.SimpleDateFormat;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;

import com.elektrifi.xml.screeninglist.ObjectFactory;
import com.elektrifi.xml.screeninglist.ScreeningList;
import com.elektrifi.xml.screeninglist.ScreeningList.PublshInformation;
import com.elektrifi.xml.screeninglist.ScreeningList.ScreeningEntry;
import com.elektrifi.xml.screeninglist.ScreeningList.ScreeningEntry.AddressList;
import com.elektrifi.xml.screeninglist.ScreeningList.ScreeningEntry.AddressList.Address;

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
//import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.apache.log4j.Logger;

import com.elektrifi.util.ApplicationConfig;

public class TestClientSdnScreening {

	// Set up log4j logger
	private static Logger logger = Logger.getLogger(TestClientSdnScreening.class);
	private static ObjectFactory factory = new ObjectFactory();

	public static void main(String[] args) {

		// Read in properties (location of SDN file)
		ApplicationConfig config = ApplicationConfig.getApplicationConfig();
		String serviceUrl = config.getProperty("SdnServiceUrl");
		logger.info("\n--------");
		logger.info("ServiceUrl read in as " + serviceUrl);
		
		int recordCount = 0; 
		try {
			ScreeningList screeningList = factory.createScreeningList();
			// Set up a list of addresses
			AddressList addressList = factory.createScreeningListScreeningEntryAddressList();
			int addressId = 1;
			
			// Set up a collection of customers for screening
			for (int uid = 1; uid < 12; uid++) {

				// Set up screeningEntry (list of records to be screened)
				ScreeningEntry screeningEntry = factory.createScreeningListScreeningEntry();						
				// Set up address object to populate later
				Address address = factory.createScreeningListScreeningEntryAddressListAddress();
				
				if (uid == 1) {
					screeningEntry.setFirstName("Robert Gabriel");
					screeningEntry.setLastName("Mugabe");					
					/**
					address.setUid(addressId);
					address.setAddress1("Sunnyside Cottage");
					address.setAddress2("Meikle Wartle");
					address.setCity("Inverurie");
					address.setStateOrProvince("Aberdeenshire");
					address.setPostalCode("AB51 5AA");
					address.setCountry("UK");
					**/
					
				} else if (uid == 2) {
					screeningEntry.setFirstName("Robert");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");
					**/
				} else if (uid == 3) {
					screeningEntry.setFirstName("Ribert Gabriel");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);
					address.setAddress1("Sunnyside Cottage");
					address.setAddress2("Meikle Wartle");
					address.setCity("Inverurie");
					address.setStateOrProvince("Aberdeenshire");
					address.setPostalCode("AB51 5AA");
					address.setCountry("UK");			
					**/		
				} else if (uid == 4) {
					screeningEntry.setFirstName("Gabriel Robert");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");
					**/
				} else if (uid == 5) {
					screeningEntry.setFirstName("President Robert");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");			
					**/											
				} else if (uid == 6) {
					screeningEntry.setFirstName("Commander");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");		
					**/
				} else if (uid == 7) {
					screeningEntry.setFirstName("Cdr");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");	
					**/																		
				} else if (uid == 8) {
					screeningEntry.setFirstName("Robert");
					screeningEntry.setLastName("Mugabe, President");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");					
					**/														
				} else if (uid == 9) {
					screeningEntry.setFirstName("R.G.");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");	
					**/
				} else if (uid == 10) {
					screeningEntry.setFirstName("R.G.");
					screeningEntry.setLastName("O'Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");	
					**/
				} else if (uid == 11) {
					screeningEntry.setFirstName("R.");
					screeningEntry.setLastName("Gabriel-Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");	
					**/																											
					
				} else if (uid != 1 && uid%2 == 1 ){
					screeningEntry.setFirstName("Someone Else");
					screeningEntry.setLastName("Mugabe");	
					/**
					address.setUid(addressId);
					address.setAddress1("Sunnyside Cottage");
					address.setAddress2("Meikle Wartle");
					address.setCity("Inverurie");
					address.setStateOrProvince("Aberdeenshire");
					address.setPostalCode("AB51 5AA");
					address.setCountry("UK");
					**/
				} else if (uid != 2 && uid%2 == 0 ){
					screeningEntry.setFirstName("Another");
					screeningEntry.setLastName("Mugabe");
					/**
					address.setUid(addressId);					
					address.setAddress1("Caesar's Palace");
					address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
					address.setCity("Las Vegas");
					address.setStateOrProvince("Nevada");
					address.setPostalCode("89109");
					address.setCountry("USA");
					**/
				}
				
				// Set default address as we're focussing on identitying names for now
				address.setUid(1);					
				address.setAddress1("Caesar's Palace");
				address.setAddress2("E Flamingo Rd and Las Vegas Blvd South");
				address.setCity("Las Vegas");
				address.setStateOrProvince("Nevada");
				address.setPostalCode("89109");
				address.setCountry("USA");
				
				// Set uid...
				screeningEntry.setUid(uid);		
				// Set screeningType (Individual, Entity, Vessel)
				screeningEntry.setScreeningType("Individual");
				// Addresses				
				addressList.getAddress().add(recordCount, address);
				// Add addressList to sdnEntry				
				screeningEntry.setAddressList(addressList);
				// Add screeningEntry to screeningList
				screeningList.getScreeningEntry().add(recordCount, screeningEntry);
				recordCount++;
				addressId++;
			}			

			// Set record count and date
			PublshInformation publshInformation = factory.createScreeningListPublshInformation();			
			// Set object publication details (i.e. the date and time of the run)
			Date now = new Date();
			SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
			publshInformation.setPublishDate(formattedDate.format(now));
			publshInformation.setRecordCount(recordCount);
			screeningList.setPublshInformation(publshInformation);

			// Marshall POJOs to XML and post to screening service
			JAXBContext jc = JAXBContext.newInstance("com.elektrifi.xml.screeninglist");			
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			//m.marshal(screeningList, System.out);
			
			// Output to a StringWriter
			StringWriter inputXml = new StringWriter();
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.marshal(screeningList, inputXml);
			
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
	            	entity = new StringEntity(inputXml.toString());
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
		} catch( MarshalException me ) {
			logger.fatal( "Caught MarshalException: " );
			me.printStackTrace();
		} catch( JAXBException je ) { 
			logger.fatal( "Caught JAXBException: " );
			je.printStackTrace();
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
