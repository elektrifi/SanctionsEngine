package com.elektrifi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationConfig {

    private static Properties properties;
    private static ApplicationConfig config;

    static {
    	
        config = new ApplicationConfig();
        
    }

    private ApplicationConfig () {

        try {

            InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
            properties = new Properties();
            properties.load(in);

        } catch (IOException ex) {
            Logger.getLogger(ApplicationConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static ApplicationConfig getApplicationConfig () {

            return config;

    }

    public String getProperty (String propName) {

        return properties.getProperty(propName, "Null");

    }      

}