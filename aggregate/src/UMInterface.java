/**
 * Defines the methods used for retrieve content progress and knowledge levels
 * from User Model service
 * @author Julio Guerra (PAWS lab)
 * @date 2014/03/07
*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface UMInterface {
	
	/**
	 * Gets a map with each concept identifier and the knowledge level of the user in it
	 * @author Julio Guerra (PAWS lab)
	 * @date 2014/03/07 
	 * @param  usr     the user
	 * @param  domain  additional information for UM to determine which concepts to consider
	 * @param  grp     additional information required by UM to identify the group (class) 
	 * @return         a map between each concept and the knowledge level of the user in it.
	 */
	public HashMap<String,Double> getConceptLevels(String usr, String domain, String grp);
	
	/**
	 * Gets name and email of the user
	 * @author Julio Guerra (PAWS lab)
	 * @date 2014/03/07 
	 * @param  usr   the username
	 * @return       a string array with the name and email of the user.
	 */	
	public String[] getUserInfo(String usr);
	
	/**
	 * Accesses the list of students/users in a class (grp)
	 * @author Julio Guerra (PAWS lab)
	 * @date 2014/03/07 
	 * @param  grp   the group identifier (or mnemonic)
	 * @return       and ArrayList in which each element is a string array containing user information: username, name, email.
	 */ 
	public ArrayList<String[]> getClassList(String grp);
	
	
	/**
	 * Gets the activity of the user in content of type question
	 * @author Julio Guerra (PAWS lab)
	 * @date 2014/03/07 
	 * @param  usr     the username
	 * @param  domain  additional information for UM to determine which content to include
	 * @return         a hash map in which the key is the content identifier (string) and 
	 *                 the value is a string array of length 3 containing: the content 
	 *                 identifier, the number of attempts, and the number of succeeded attempts 
	 */	
	public HashMap<String, String[]> getUserQuestionsActivity(String usr, String domain);

	
	/**
	 * Gets the activity of the user in content of type example
	 * @author Julio Guerra (PAWS lab)
	 * @date 2014/03/07 
	 * @param  usr     the username
	 * @param  domain  additional information for UM to determine which content to include
	 * @return         a hash map in which the key is the content identifier (string) and 
	 *                 the value is a string array of length 4 containing: the content 
	 *                 identifier, the number of lines displayed, the number of distinct 
	 *                 lines displayed by the user, and the total number of commented lines 
	 *                 the example has. 
	 */	
	// 1: number of actions performed by the student in the example (lines displayed) 
	// 2: number of distinct actions performed by the student in the example
	// 3: total number of commented lines in the example
	public HashMap<String, String[]> getUserExamplesActivity(String usr, String domain);
	
	/**
	 * Gets the activity of the user all kind of content
	 * @author Julio Guerra (PAWS lab)
	 * @date 2014/03/07 
	 * @param  usr     the username
	 * @param  domain  additional information for UM to determine which content to include
	 * @return         a hash map in which the key is the content identifier (string) and 
	 *                 the value is a string array of length 8 containing: (0) the content 
	 *                 identifier, (1) the content type (question, example, etc), (2) the 
	 *                 total number of sub activities the content has (in questions it is 0,
	 *                 in examples is the total number of commented lines), (3) number of 
	 *                 attempts (not defined for examples), (4) the number of succeeded 
	 *                 attempts (not defined for examples), (5) the number of sub activities 
	 *                 done by the user (not defined in questions, lines displayed in examples),
	 *                 (6) the distinct sub activities done (not defined for questions, 
	 *                 distinct lines viewed in examples), and (7) additional activity 
	 *                 information (for other kind of content).  
	 */		
	public HashMap<String, String[]> getUserContentActivity(String usr, String domain);
}
