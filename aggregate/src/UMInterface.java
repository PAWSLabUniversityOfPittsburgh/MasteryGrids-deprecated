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
     * Gets name and email of the user
     * 
     * @author Julio Guerra (PAWS lab)
     * @date 2014/03/07
     * @param usr
     *            the username
     * @return a string array with the name and email of the user.
     */
    public String[] getUserInfo(String usr, String key);

    /**
     * Accesses the list of students/users in a class (grp)
     * 
     * @author Julio Guerra (PAWS lab)
     * @date 2014/03/07
     * @param grp
     *            the group identifier (or mnemonic)
     * @return and ArrayList in which each element is a string array containing
     *         user information: username, name, email.
     */
    public ArrayList<String[]> getClassList(String grp, String key);

    /**
     * This method communicates with the User Model get or compute the knowledge
     * and progress levels of the user in all the content items of the course.
     * The contentList hashmap provides a finite list of content available to
     * the course and each content specify it's provider. For different
     * providers, the implementation of this method should change and eventually
     * incorporate additional steps. The method returns
     * 
     * @param usr
     *            the user name
     * @param grp
     *            the group id
     * @param sid
     *            the session id
     * @param cid
     *            the course id
     * @param domain
     *            the domain (java, sql, etc)
     * @param maxRecommendations
     *            the maximum number of recommendation to include in the list
     * @param contentList
     *            each key represent a content item, identified by the
     *            content_name and each value is an String[] with:<br />
     *            0: resource name (id) 1: display name 2: url 3: description 4:
     *            comment 5: provider id
     * @param usr
     *            the user name
     * @param contentList
     * @param options  
     * 
     */
    public HashMap<String, double[]> getContentSummary(
            String usr, String grp, String sid, String cid, String domain,
            HashMap<String, String[]> contentList,
            ArrayList<String> options);


    
}
