package com.elektrifi.util;

import org.apache.log4j.Logger;

public class StringUtils {

	// Set up log4j logger
	private static 	Logger logger = Logger.getLogger(StringUtils.class);
	
	public StringUtils() {
		
	}
	
	public String getInitials(String inputName) {
				
		String initialisedString = "";
		StringBuffer initialsSb = new StringBuffer();
		String space = " ";
		
		logger.debug("StringUtils.getInitials received inputTerm..." + inputName);
		
		if (!inputName.isEmpty()) {
			
			// Trim inputStr
			inputName = inputName.trim();
			inputName = removeSpecialCharsAndExcessSpaces(inputName);
			logger.debug("Cleaned up inputTerm is..." + inputName + "...");			
		
			String[] splitStr = inputName.split(" ");
			// Loop over tokens and reduce them to one initial char
			for (int j = 0; j < splitStr.length; j++) {
				
				logger.debug("-----> splitStr[" + j + "] is " + splitStr[j]);
				initialsSb.append(splitStr[j].substring(0, 1));
				initialsSb.append(space);
			}        		
			initialisedString = initialsSb.toString();
			
		} else {
			initialisedString = "";
		}
		
		return initialisedString.trim();
	}

	public String getFirstNames(String inputName) {
		
		String outputStr = "";
		String space = " ";
		StringBuffer nameSb = new StringBuffer();
		
		if (!inputName.isEmpty()) {
				
			if (inputName.contains(space)) {
		
				// Trim inputStr
				inputName = inputName.trim();
				inputName = removeSpecialCharsAndExcessSpaces(inputName);
		
				// Trim inputStr
				//inputName = inputName.trim();
				//System.out.println("\tInput trimmed is..." + inputName + "...");
				// Strip special chars from searchTerm (".,-'" etc)
				//String pattern = "[^A-Za-z]";
				//inputName = inputName.replaceAll(pattern, space);
				//System.out.println("\tInput no spec chars is..." + inputName + "...");
				// Strip out excess spaces
				//pattern = "\\b\\s{2,}\\b";
				//inputName = inputName.replaceAll(pattern, space);
				//System.out.println("\tInput no excess spaces is..." + inputName + "...");
			
				String[] splitStr = inputName.split(" ");
				// Loop over tokens and reduce them to one initial char
				for (int j = 0; j < splitStr.length-1; j++) {
					nameSb.append(splitStr[j]);
					nameSb.append(space);
				}        		
				outputStr = nameSb.toString().trim();
			
			} else if (!inputName.contains(space)) {
				outputStr = inputName;
			}
			
		} else {
				outputStr = "";
		}			
		
		return outputStr.trim();
	}
	
	public String getLastName(String inputName) {
		
		String outputStr = "";
		String space = " ";
		//StringBuffer nameSb = new StringBuffer();
		
		if (!inputName.isEmpty()) {
				
			if (inputName.contains(space)) {
		
				// Trim inputStr
				inputName = inputName.trim();
				inputName = removeSpecialCharsAndExcessSpaces(inputName);
		
				String[] splitStr = inputName.split(" ");
				// Loop over tokens and reduce them to one initial char
				//for (int j = 0; j < splitStr.length-1; j++) {
				//	nameSb.append(splitStr[j]);
				//	nameSb.append(space);
				//}        		
				outputStr = splitStr[splitStr.length-1].toString().trim();
			
			} else if (!inputName.contains(space)) {
				outputStr = inputName;
			}
			
		} else {
				outputStr = "";
		}			
		
		return outputStr.trim();
	}
	
	public String removeSpecialChars(String inputName) {
		
		String outputStr = "";
		String space = " ";
		
        // Strip special chars from searchTerm (".,-'" etc)
        String pattern = "[^A-Za-z]";
        outputStr = inputName.replaceAll(pattern, space);
				
		return outputStr.trim();
		
	}

	public String removeExcessSpaces(String inputName) {

		String outputStr = "";
		String space = " ";
		
      	// Strip out excess spaces (e.g. R.  G. Mugabe
        String pattern = "\\b\\s{2,}\\b";        
        outputStr = inputName.replaceAll(pattern, space);
				
		return outputStr.trim();
		
	}
	
	public String removeSpecialCharsAndExcessSpaces(String inputName) {

		String outputStr = "";
		
		logger.debug("RmSpecCharsAndExSpaces received..." + inputName);
		inputName = removeSpecialChars(inputName);
		logger.debug("After removeSpecialChars, inputName is..." + inputName);
		outputStr = removeExcessSpaces(inputName);
		logger.debug("After removeExcessSpaces, inputName is..." + inputName);		

		return outputStr.trim();
		
	}	
	
	public static void main(String[] args) {
	
		String [] testStrings = { 	"'Abd Al Rahim", 
									"Robert Gabriel",
									"Mohamad Iqbal",									
									"Robert-Gabriel",
									"Robert  Gabriel",
									"Robert Winston Gabriel",
									"R. Gabriel",
									"R. G.",
									"R.      -    G.",
									"RG",									
									"R O'G",
									"R O\\G",
									};		
		String outputStr 	= "";
		StringUtils strUtils = new StringUtils();
		
		System.out.println("--- GetInitials ---");
		for (int i = 0; i<testStrings.length; i++) {
			System.out.println(i + ". " + "Input is..." + testStrings[i]);
			outputStr = strUtils.getInitials(testStrings[i]);
			System.out.println("...and output is..." + outputStr + "...");
		}		
		
		System.out.println("--- GetFirstNames ---");
		for (int i = 0; i<testStrings.length; i++) {
			System.out.println(i + ". " + "Input is..." + testStrings[i]);
			outputStr = strUtils.getFirstNames(testStrings[i]);
			System.out.println("...and output is..." + outputStr + "...");			
		}
		
		System.out.println("--- GetLastName ---");
		for (int i = 0; i<testStrings.length; i++) {
			System.out.println(i + ". " + "Input is..." + testStrings[i]);
			outputStr = strUtils.getLastName(testStrings[i]);
			System.out.println("...and output is..." + outputStr + "...");			
		}
		
		
	}
	
}
