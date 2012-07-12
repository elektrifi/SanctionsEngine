package com.elektrifi.sanctions.analyzers;

import java.util.HashMap;

public class SanctionsSynonymEngine implements SynonymEngine {
	
  private static HashMap<String, String[]> map = new HashMap<String, String[]>();

  static {

	// Individuals
	map.put("robert", 		new String[] {"rob", "rab", "rabbie", "rabby", "bobby", "bobbie", "robby", "robbie", "roberta"});	  
    map.put("bob", 			new String[] {"robert", "roberta", "rob", "rab", "rabbie", "rabby", "bobby", "bobbie", "robbie", "robby"});    
    map.put("rob", 			new String[] {"robert", "roberta", "bob", "rab", "rabbie", "rabby", "bobby", "bobbie", "robbie"});
    map.put("robbie", 		new String[] {"robert", "roberta", "bob", "rab", "rabbie", "rabby", "bobby", "bobbie", "rob", "robby"});
    
    map.put("gabriel", 		new String[] {"gabe", "gab", "gabrielle", "gabriella"});
    
    map.put("moustafa", 	new String[] {"mustafa", "mostafa", "moustapha", "mostefa"});
    map.put("moustapha", 	new String[] {"moustafa", "mustafa", "mostafa", "mostefa"});
    map.put("mustafa", 		new String[] {"moustafa", "mostafa", "moustapha", "mostefa"});
    map.put("mostafa", 		new String[] {"moustafa", "mustafa", "moustapha", "mostefa"});
    map.put("mostefa", 		new String[] {"moustafa", "mustafa", "moustapha", "mostafa"});
    
    map.put("muhammad", 	new String[] {"mohammad", "mohamed", "mohammed", "muhammed", "mahmoud", "muhamed", "muhannad" });
    map.put("mohammad", 	new String[] {"mohamed", "mohammed", "muhammed", "mahmoud", "muhamed", "muhannad", "muhammad" });    
    map.put("mohamed", 		new String[] {"mohammed", "muhammed", "mahmoud", "muhamed", "muhannad", "muhammad", "mohamma", "mohammad" });    
    map.put("mohammed", 	new String[] {"muhammed", "mahmoud", "muhamed", "muhannad", "muhammad", "mohamma", "mohammad", "mohamed" });    
    map.put("muhammed", 	new String[] {"mahmoud", "muhamed", "muhannad", "muhammad", "mohamma", "mohammad", "mohamed", "mohammed" });    
    map.put("mahmoud", 		new String[] {"muhamed", "muhannad", "muhammad", "mohamma", "mohammad", "mohamed", "mohammed", "muhammed" });    
    map.put("muhamed", 		new String[] {"muhannad", "muhammad", "mohamma", "mohammad", "mohamed", "mohammed", "muhammed", "mahmoud" });
    map.put("muhannad", 	new String[] {"muhammad", "mohamma", "mohammad", "mohamed", "mohammed", "muhammed", "mahmoud", "muhamed" });    
    
    map.put("sabine", 		new String[] {"sabina"});    
    map.put("sabina", 		new String[] {"sabine"});    
    
    map.put("grace", 		new String[] {"gracie", "gracey"});    
    map.put("gracey", 		new String[] {"gracie", "grace"});    
    map.put("gracie", 		new String[] {"grace", "gracey"});
    
    map.put("leo", 			new String[] {"leon", "lion"});    
    map.put("leon", 		new String[] {"leo", "lion"});
    
    map.put("osama", 		new String[] {"usama"});    
    map.put("usama", 		new String[] {"osama"});    

    map.put("abdallah", 	new String[] {"abdalla"});
    map.put("abdalla", 		new String[] {"abdallah"});    
    
    map.put("caesar", 		new String[] {"cesar"});
    
    // Entities
    map.put("ltd", 			new String[] {"limited", "plc"});    
    map.put("plc", 			new String[] {"limited", "ltd"});    
    map.put("limited", 		new String[] {"ltd", "ltd"});    
    
    map.put("company", 		new String[] {"co", "comp"});
    map.put("co", 			new String[] {"company", "comp"});    
    map.put("comp", 		new String[] {"company", "co"});   
    
  }

  public String[] getSynonyms(String s) {
    return (String[]) map.get(s);
  }
}
