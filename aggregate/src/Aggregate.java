import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * This is the main class. An Aggregate object obtains the course structure
 * (topics, content) from aggregate DB and the levels of knowledge and progress
 * from the User Model using the UMInterface methods.<br />
 * First instantiate the Aggregate object, second use the method fillClassLevels
 * to load the precomputed UM models stored, third compute group levels (class 
 * average, top students) using computeGroupLevels, then sequence using method 
 * sequenceContent and load recommendations using fillRecommendations. Finally,
 * generate the JSON response using genAllJSON or genUserJSON
 * 
 * @author Julio Guerra, PAWS lab
 * 
 * TO DO
 * - 
 */
public class Aggregate {
    private boolean verbose; // for "old-fashioned" debugging purposes
    
    public ConfigManager cm;
    
    private String usr;
    private String grp;
    private String sid;
    private String domain;
    private String usr_name;
    private String usr_email;
    private String grp_name;
    private String cid;

    // STRUCTURES STORING THE COURSE STRUCTURE
    // some use HashMap to speed up computations
    public ArrayList<String[]> topicList; // each has: topic name (string id), display name (string), order (int), visibility (1/0)
    public HashMap<String, String[]> contentList; // each content has: resource name (string id), display name, url, description, comment, provider_id (string id)
    public HashMap<String, String> mapContentTopic; // maps content id and topic id (the first topic containing the content)
    public ArrayList<String[]> resourceList; // resource name , resource display name (ex: qz , question)
    public HashMap<String, Integer> resourceMap; // store resource name and position in the resourceList
    public HashMap<String, ArrayList<String>[]> topicContent; // topic name , one arraylist for each resource (type) in the order of resorceList 
    public int nTopicLevels; // NResource X 2: 2 levels (K,P) for each resource type
    public int nContentLevels; // 2 levels (K,P) 

    // Ordered ArrayList of students in the class. Each elements has (strings): 
    // learner id (or user id), name, email
    public ArrayList<String[]> class_list;
    public HashMap<String, String> non_students;
    
    // proactive recommendation scores per content and per topic
    public HashMap<String, Double> contentSequencingScores;
    public HashMap<String, double[]> topicSequencingScores; // array of double corresponding to the dimension of resources

    public HashMap<String, double[]> userTopicLevels;
    public HashMap<String, double[]> userContentLevels;

    public Map<String, double[]> aggs1_topic_levels;
    public Map<String, double[]> aggs2_topic_levels;
    public Map<String, double[]> aggs1_content_levels;
    public Map<String, double[]> aggs2_content_levels;
    public ArrayList<String> top_students_ids;

    public Map<String, Map<String, double[]>> peers_topic_levels;
    public Map<String, Map<String, double[]>> peers_content_levels;

    // recommendation set (reactive recommendations)
    public ArrayList<ArrayList<String>> recommendation_list;
    // feedback set
    public ArrayList<ArrayList<String>> activity_feedback_form_items; 
    public String activity_feedback_id;

    // public um2DBInterface um2_db;
    public UMInterface um_interface;
    public RecInterface rec_interface;
    public AggregateDB agg_db;

    public static DecimalFormat df = new DecimalFormat("#.##");
    
    
    /**
     * The constructor load the course structure and content from DB
     * and compute the aggregated user model levels (progress and 
     * knowledge) if parameter updateUM is true. 
     * 
     * @param usr               user id
     * @param grp               group id
     * @param cid               course id 
     * @param sid               session id
     * @param updateUM          true: build the user model, false: build a 
     *                          null user model if the user has no precomputed 
     *                          model stird in DB
     * @param cm                a ConfigManager object contains 
     *                          configuration variables 
     */
    public Aggregate(String usr, String grp, String cid, String sid,
            boolean updateUM, ConfigManager cm) {
        this.usr = usr;
        this.grp = grp;
        this.sid = sid;
        this.cm = cm;
        
        verbose = cm.agg_verbose.equalsIgnoreCase("yes");        
        
        openDBConnections();

        grp_name = agg_db.getGrpName(grp);

        if (cid == null || cid.length() == 0)
            this.cid = agg_db.getCourseId(grp);
        else {
            this.cid = cid;            
        }
        domain = agg_db.getDomain(cid);
        if (domain == null || domain.length()==0){
            this.cid = agg_db.getCourseId(grp);
            this.domain = "UNKNOWN";
        }
        try{
            um_interface = (UMInterface) Class.forName(cm.agg_uminterface_classname).newInstance();
            
        }catch(Exception e){
            // @@@@ um_interface = new NullUMInterface();
            e.printStackTrace();
        }

        try{
            rec_interface = (RecInterface) Class.forName(cm.agg_recinterface_classname).newInstance();
            
        }catch(Exception e){
            // @@@@ um_interface = new NullUMInterface();
            e.printStackTrace();
        }
        
        // the userdata array contains user name, email and parameters for configuration
        String[] userdata = um_interface.getUserInfo(usr, cm.agg_uminterface_key);
        usr_name = userdata[0];
        usr_email = userdata[1];
        if(userdata.length > 2) overwriteConfigForUser(userdata[2]);
        

        class_list = um_interface.getClassList(grp, cm.agg_uminterface_key);
        non_students = agg_db.getNonStudents(grp); // special non students (instructor, researcher)
        
        resourceList = agg_db.getResourceList(cid);
        genResourceMap();
        
        nTopicLevels = resourceList.size() * 2; // the dimension of the topic levels array. For each resource there is a level of knowledge and a level of progress
        nContentLevels = 2; // the dimension of the content levels array. For each resource there is a level of knowledge and a level of progress
        
        topicList = agg_db.getTopicList(cid);
        ArrayList<String> hidden_topics = agg_db.getHiddenTopics(grp);
        hideTopics(hidden_topics); // set the visibility attribute (topic[3]) to topics being invisible for this group

        contentList = agg_db.getContent2(cid);
        // System.out.println("content got");

        topicContent = agg_db.getTopicContent2(cid, resourceMap);
        mapContentToTopic();
        // System.out.println("topic content");

        // This part computed the user model if updateUM = true or if the user
        // has no pre-computed model stored in the db (first log in)
        if (updateUM) {
            computeUserLevels(usr, grp, sid, cid, domain);
            storePrecomputedModel(usr);            
        }else if(!agg_db.precomputedModelExist(cid, usr)) {
            computeNullLevels();
            storePrecomputedModel(usr);
        }
        
        
        closeDBConnections();
    }
    
    // @@@@ REVIEW!!!!
    // DEV: initialize the object with just the information needed
    // to retrieve class precomputed models stored
    public Aggregate(String grp, ConfigManager cm) {
        this.usr = null;
        this.grp = grp;
        this.sid = null;
        this.cm = cm;

        verbose = cm.agg_verbose.equalsIgnoreCase("yes");        
        
        um_interface = new PAWSUMInterface();
        openDBConnections();

        grp_name = agg_db.getGrpName(grp);
        cid = agg_db.getCourseId(grp);
        domain = agg_db.getDomain(cid);
        
        usr_name = null;
        usr_email = null;
        
        resourceList = agg_db.getResourceList(cid);
        genResourceMap();
        
        nTopicLevels = resourceList.size() * 2; // the dimension of the topic levels array. For each resource there is a level of knowledge and a level of progress
        nContentLevels = 2; // the dimension of the content levels array. For each resource there is a level of knowledge and a level of progress

        String[] userdata = um_interface.getUserInfo(usr, cm.agg_uminterface_key);
        if(userdata.length > 2) overwriteConfigForUser(userdata[2]);
        
        class_list = um_interface.getClassList(grp,cm.agg_uminterface_key);

        topicList = agg_db.getTopicList(cid);

        contentList = agg_db.getContent2(cid);
        mapContentToTopic();

        topicContent = agg_db.getTopicContent2(cid, resourceMap);

        closeDBConnections();
    }

    public void openDBConnections() {
        agg_db = new AggregateDB(cm.agg_dbstring, cm.agg_dbuser, cm.agg_dbpass);
        agg_db.openConnection();
    }

    public void closeDBConnections() {
        agg_db.closeConnection();
    }

    // REVIEW
    public void hideTopics(ArrayList<String> hidden_topics) {
        if (hidden_topics != null && hidden_topics.size() > 0) {
            for (String hidden : hidden_topics) {
                for (String[] topic : topicList) {
                    if (topic[0].equalsIgnoreCase(hidden))
                        topic[3] = "0";
                }
            }

        }
    }

    public void computeNullLevels(){
        userTopicLevels = nullTopicLevels();
        userContentLevels = nullContentLevels();
    }
    
    public void computeUserLevels(){
        computeUserLevels(usr, grp, sid, cid, domain);
        storePrecomputedModel(usr);
    }
    
    
    // 
    // computes user levels aggregating by topics and content
    public void computeUserLevels(String usr, String grp, String sid, String cid, String domain) {
        if (usr == null || domain == null || usr.length() == 0 || domain.length() == 0) return;
        
        userTopicLevels = new HashMap<String, double[]>();
        userContentLevels = new HashMap<String, double[]>();
        
        // fill the hash map with the knowledge and progress computations from UM interface
        // contentSummary, each double[]: knowledge, progress, attempts/loads, success rate, completion, other 1, other 2
        long time1 = Calendar.getInstance().getTimeInMillis();
        userContentLevels = um_interface.getContentSummary(usr, grp, sid, cid, domain, contentList, null);
        if(verbose) System.out.println("  Get all form UM   " + (Calendar.getInstance().getTimeInMillis()-time1));
        
        // COMPUTE AGGREGATE LEVELS FOR TOPICS
        for (String[] topic : topicList) {

            double[] kpvalues = new double[nTopicLevels];

            // Topic knowledge and progress levels in concepts related with
            // questions
            // using userContentLevels and topic_content
            double user_topic_oneType_k = 0.0;
            double user_topic_oneType_p = 0.0;
            int i = 0;
            int contentsSize = 0;
            
            ArrayList<String>[] oneTypeContents = topicContent.get(topic[0]);
            if (oneTypeContents != null)
                for (ArrayList<String> contents : oneTypeContents) {
                    user_topic_oneType_k = 0.0;
                    user_topic_oneType_p = 0.0;
                    if(contents != null){
                        for (String content : contents) {
                            double[] contentKP = userContentLevels.get(content);
                            if (contentKP != null) {
                                user_topic_oneType_k += contentKP[0];
                                user_topic_oneType_p += contentKP[1];
                            }
                        }                        
                        contentsSize = contents.size();
                        if (contents.size() == 0)
                            contentsSize = 1;
                        user_topic_oneType_k = user_topic_oneType_k / contentsSize;
                        user_topic_oneType_p = user_topic_oneType_p / contentsSize;
                    }
                    kpvalues[i] = user_topic_oneType_k;
                    kpvalues[i + 1] = user_topic_oneType_p;
                    i += 2;
                    if (i >= nTopicLevels)
                        break;
                }
            userTopicLevels.put(topic[0], kpvalues);
        }

    }

    
    public double getTopicSequenceScore(String topic, String src) {
        if (topicSequencingScores == null)
            return 0;
        double[] scores = topicSequencingScores.get(topic);
        if (scores == null)
            return 0;
        double s = 0;
        Integer i = resourceMap.get(src);
        if (i != null) s = scores[i];
        
//        if (s > cm.agg_proactiverec_threshold)
//            s = 1.0;
//        else
//            s = 0.0;
        return s;
    }

    public double getContentSequenceScore(String content_name) {
        if (contentSequencingScores == null)
            return 0;
        Double score = contentSequencingScores.get(content_name);
        if (score == null)
            return 0;
//        if (score > cm.agg_proactiverec_threshold)
//            score = 1.0;
//        else
//            score = 0.0;
        return score;
    }
    
    // Get the precomputed models at level of content and topics for each
    // student in the list
    // set usr to null to get all, or a user is to get only the user model
    public void fillClassLevels(String usr, boolean includeNullStudents) {
        if (class_list == null || class_list.size() == 0) {
            return;
        }
        peers_topic_levels = new HashMap<String, Map<String, double[]>>();
        peers_content_levels = new HashMap<String, Map<String, double[]>>();
        openDBConnections();
        HashMap<String, String[]> precomp_models = agg_db.getPrecomputedModels(cid, usr);
        // for(String[] learner: class_list){
        // System.out.println("total students: "+class_list.size());
        for (Iterator<String[]> i = class_list.iterator(); i.hasNext();) {
            String[] learner = i.next();

            String learnerid = learner[0]; // the user login (username)
            String[] models = precomp_models.get(learnerid);
            if (models != null) {
                String model4topics = models[0];
                String model4content = models[1];
                if (model4topics != null && model4topics.length() > 0) {
                    HashMap<String, double[]> learner_topic_levels = formatLevels(model4topics, nTopicLevels);
                    peers_topic_levels.put(learnerid, learner_topic_levels);
                }
                if (model4content != null && model4content.length() > 0) {
                    HashMap<String, double[]> learner_content_levels = formatLevels(model4content, nContentLevels);
                    peers_content_levels.put(learnerid, learner_content_levels);
                }
            } else {
                // take the non-activity users out, but leave the current user
                if(!includeNullStudents && !learnerid.equalsIgnoreCase(usr)) i.remove();
            }

        }
        closeDBConnections();
    }

    public void computeGroupLevels(boolean includeNullStudents, int top){
        orderClassByProgress();
        computeAverageClassTopicLevels();
        computeAverageTopStudentsTopicLevels(top);
        computeAverageClassContentLevels();
        computeAverageTopStudentsContentLevels(top);
    }
    
    //
    public HashMap<String, double[]> nullTopicLevels() {
        HashMap<String, double[]> res = new HashMap<String, double[]>();
        for (String[] topic : topicList) {
            String topic_id = topic[0];
            double[] levels = new double[nTopicLevels]; // @@@@
            res.put(topic_id, levels);
        }
        return res;
    }
    public HashMap<String, double[]> nullContentLevels() {
        HashMap<String, double[]> res = new HashMap<String, double[]>();
        for (Map.Entry<String, String[]> content : contentList.entrySet()) {
            String content_name = content.getKey();
            double[] levels = new double[nContentLevels]; // @@@@
            res.put(content_name, levels);
        }
        return res;
    }

    // take a string representing the levels in topics or contents (precomputed
    // model) and
    // returns a hashmap with the levels per topic/content
    public HashMap<String, double[]> formatLevels(String model, int nlevels) {
        HashMap<String, double[]> res = new HashMap<String, double[]>();
        String[] model_arr = model.split("\\|");
        // System.out.println("  formatting model: "+model);
        for (int i = 0; i < model_arr.length; i++) {
            // System.out.println(model_arr[i]);
            String[] parts = model_arr[i].split(":");
            String topic = parts[0];
            String[] str_levels = parts[1].split(",");
            double[] levels = new double[nlevels];
            if(str_levels.length >= nlevels){
                for (int j = 0; j < nlevels; j++) {
                    levels[j] = Double.parseDouble(str_levels[j]);
                }
                
            }
            res.put(topic, levels);
        }
        return res;
    }

    public void orderClassByProgress(){
        orderClassByScore(false);
    }
    public void orderClassByKnowledge(){
        orderClassByScore(true);
    }
    public void orderClassByScore(boolean usingProgress) {
        String learner1;
        String learner2;
        Map<String, double[]> learner1_levels;
        Map<String, double[]> learner2_levels;
        double learner1_sum = 0.0;
        double learner2_sum = 0.0;
        for (int i = 0; i < class_list.size() - 1; i++) {
            for (int j = 0; j < class_list.size() - 1; j++) {
                learner1 = class_list.get(j)[0];
                learner2 = class_list.get(j + 1)[0];
                learner1_levels = peers_topic_levels.get(learner1);
                learner2_levels = peers_topic_levels.get(learner2);
                learner1_sum = 0.0;
                learner2_sum = 0.0;
                // average across all topics. if no topic levels for the
                // students, returns 6 zeros
                double[] avgs1 = averageTopicLevels(learner1_levels, nTopicLevels);
                double[] avgs2 = averageTopicLevels(learner2_levels, nTopicLevels);

                for (int k = 0; k < nTopicLevels; k++) {
                    if ((usingProgress && k%2==1) || (!usingProgress && k%2==0)) {
                        learner1_sum += avgs1[k];
                        learner2_sum += avgs2[k];

                    }
                }
                // if learner 1 has lower average score tan learner 2, swap
                if (learner1_sum < learner2_sum) {
                    // String[] tmp = class_list.get(j);
                    // class_list.remove(j);
                    // class_list.add(tmp);
                    Collections.swap(class_list, j, j + 1);
                }
            }
        }
    }

    public static double[] averageTopicLevels(Map<String, double[]> topics, int nLevels) {
        double[] res = new double[nLevels];
        if (topics == null)
            return res;
        int i = 0;
        for (double[] levels : topics.values()) {
            for(int j=0;j<nLevels;j++){
                res[j] += levels[j];
            }
            i++;
        }
        if (i == 0)
            i = 1;
        for(int j=0;j<nLevels;j++){
            res[j] = res[j] / (1.0 * i);
        }
        
        return res;
    }

    public void computeAverageClassTopicLevels() {
        aggs1_topic_levels = new HashMap<String, double[]>();
        int n = class_list.size();
        int m = non_students.size();
        int div = n - m;
        if (div <= 0)
            div = 1;
        for (String[] topic : topicList) {
            double[] avglevels = new double[nTopicLevels];
            for (String[] learner : class_list) {
                if (non_students.get(learner[0]) == null) {
                    double[] levels = null;
                    Map<String, double[]> learner_levels = peers_topic_levels
                            .get(learner[0]);
                    if (learner_levels != null)
                        levels = learner_levels.get(topic[0]);
                    if (levels == null || levels.length == 0) {
                        for(int j=0;j<nTopicLevels;j++) avglevels[j] += 0.0;
                    } else {
                        for(int j=0;j<nTopicLevels;j++) avglevels[j] += levels[j];
                    }

                }
            }
            for(int j=0;j<nTopicLevels;j++) avglevels[j] = avglevels[j]/div;
            
            aggs1_topic_levels.put(topic[0], avglevels);
        }
    }

    public void computeAverageClassContentLevels() {
        aggs1_content_levels = new HashMap<String, double[]>();
        int n = class_list.size();
        int m = non_students.size();
        int div = n - m;
        if (div <= 0)
            div = 1;
        for (Map.Entry<String, String[]> content : contentList.entrySet()) {
            String content_name = content.getKey();
            double[] avglevels = new double[nContentLevels];
            for (String[] learner : class_list) {
                if (non_students.get(learner[0]) == null) {
                    double[] levels = null;
                    Map<String, double[]> learner_levels = peers_content_levels
                            .get(learner[0]);
                    if (learner_levels != null)
                        levels = learner_levels.get(content_name);
                    if (levels == null || levels.length == 0) {
                        for(int j=0;j<nContentLevels;j++) avglevels[j] += 0.0;
                    } else {
                        for(int j=0;j<nContentLevels;j++) avglevels[j] += levels[j];
                    }
                }
            }
            for(int j=0;j<nContentLevels;j++) avglevels[j] = avglevels[j]/div;
            aggs1_content_levels.put(content_name, avglevels);
        }
    }

    public void computeAverageTopStudentsTopicLevels(int n) {
        aggs2_topic_levels = new HashMap<String, double[]>();
        this.top_students_ids = new ArrayList<String>();
        int m = non_students.size();
        if ((class_list.size() - m) < n)
            n = (class_list.size() - m);
        if (n < 0)
            n = 0;
        int i = 0;
        while (top_students_ids.size() < n && i < class_list.size()) {
            String[] learner = class_list.get(i);
            if (non_students.get(learner[0]) == null) {
                top_students_ids.add(learner[0]);
            }
            i++;
        }

        int div = n;
        if (div == 0)
            div = 1;
        for (String[] topic : topicList) {
            double[] avglevels = new double[nTopicLevels];
            i = 0;
            while (i < top_students_ids.size()) {
                Map<String, double[]> learner_levels = peers_topic_levels
                        .get(top_students_ids.get(i));
                double[] levels = null;
                if (learner_levels != null)
                    levels = learner_levels.get(topic[0]);
                if (levels == null || levels.length == 0) {
                    for(int j=0;j<nTopicLevels;j++) avglevels[j] += 0.0;
                } else {
                    for(int j=0;j<nTopicLevels;j++) avglevels[j] += levels[j];
                }
                i++;
            }
            for(int j=0;j<nTopicLevels;j++) avglevels[j] = avglevels[j]/div;
            aggs2_topic_levels.put(topic[0], avglevels);
        }
    }

    public void computeAverageTopStudentsContentLevels(int n) {
        aggs2_content_levels = new HashMap<String, double[]>();
        int m = non_students.size();
        if ((class_list.size() - m) < n)
            n = (class_list.size() - m);
        if (n < 0)
            n = 0;

        int div = n;
        if (div == 0)
            div = 1;
        // System.out.println(n+" top");
        int i = 0;
        for (Map.Entry<String, String[]> content : contentList.entrySet()) {
            String content_name = content.getKey();
            double[] avglevels = new double[nContentLevels];
            // System.out.println(content_name+": ");
            i = 0;
            while (i < top_students_ids.size()) {
                String learner = top_students_ids.get(i);
                // System.out.print("   "+learner+" ");
                Map<String, double[]> learner_levels = peers_content_levels
                        .get(learner);
                double[] levels = null;
                if (learner_levels != null)
                    levels = learner_levels.get(content_name);
                if (levels == null || levels.length == 0) {
                    for(int j=0;j<nContentLevels;j++) avglevels[j] += 0.0;
                } else {
                    for(int j=0;j<nContentLevels;j++) avglevels[j] += levels[j];
                }
                i++;
            }

            for(int j=0;j<nContentLevels;j++) avglevels[j] = avglevels[j]/div;
            aggs2_content_levels.put(content_name, avglevels);
        }
    }

    public String precomputedTopicModel() {
        String user_levels = "";

        for (String[] topic : topicList) {

            double[] levels = userTopicLevels.get(topic[0]);
            if (levels != null && levels.length > 0) {
                user_levels += topic[0] + ":";
                for(int j=0;j<nTopicLevels;j++) user_levels += df.format(levels[j]) + ",";
                user_levels = user_levels.substring(0, user_levels.length() - 1);
                user_levels += "|";
            }
        }

        if (user_levels.length()>0){
            user_levels = user_levels.substring(0, user_levels.length() - 1);   
        }
        return user_levels;
    }

    public String precomputedContentModel() {
        String user_levels = "";
        for (Map.Entry<String, double[]> content : userContentLevels.entrySet()) {
            String content_name = content.getKey();
            double[] levels = content.getValue();
            if (levels != null && levels.length > 0) {
                user_levels += content_name + ":";
                for(int j=0;j<nContentLevels;j++) user_levels += df.format(levels[j]) + ",";
                user_levels = user_levels.substring(0, user_levels.length() - 1);
                user_levels += "|";
            }
        }
        if (user_levels.length()>0){
            user_levels = user_levels.substring(0, user_levels.length() - 1);   
        }
        
        return user_levels;
    }

    public void storePrecomputedModel(String user) {
        String model4topics = this.precomputedTopicModel();
        String model4content = this.precomputedContentModel();

        if (agg_db.existPrecomputedModelForSession(user, cid, grp, sid)) {
            agg_db.updatePrecomputedModel(user, cid, grp, sid,
                    model4topics, model4content);
        } else {
            agg_db.insertPrecomputedModel(user, cid, grp, sid,
                    model4topics, model4content);
        }
    }

    public double getTopicDifficulty(String topic) {
        return 0.0;
    }

    public double getTopicImportance(String topic) {
        return 0.0;
    }

    public String precomputeClassModels() {
        String output = "";
        openDBConnections();
        sid = "UNKNOWN";
        for (String[] learner : class_list) {
            output += learner[0] + "\n";
            computeUserLevels(learner[0], grp, sid, cid, domain);
            this.storePrecomputedModel(learner[0]);

        }
        closeDBConnections();
        return output;
    }

    // sequencing for the user (usr)
    // src: questions/examples
 

    public String getContentType(String content_name) {
        String[] content_data = contentList.get(content_name);
        if (content_data != null)
            return content_data[0];
        else
            return "";
    }
    
    public void genResourceMap(){
        if (resourceList == null) resourceMap = null;
        else{
            resourceMap = new HashMap<String, Integer>();
            
            for(int i=0;i<resourceList.size();i++){
                //System.out.println(resourceList.get(i)[0]+" "+ i);
                resourceMap.put(resourceList.get(i)[0], i);
            }
            
        }
    }

    // TODO review
    // @@@@ FEEDBACK generation. For now it is hardcoded
    public void fillFeedbackForm(String last_content_id, String last_content_res) {
        String feedback_id = "" + (System.nanoTime() / 1000);
        
        activity_feedback_form_items = new ArrayList<ArrayList<String>>();

        // decide when to get the feedback
        // when last content visited was a question and was succeed
        if (last_content_id != null && last_content_id.length() > 0
                && last_content_res.equals("1")) {
            activity_feedback_id = feedback_id; 
            
            ArrayList<String> item1 = new ArrayList<String>();
            item1.add("content_difficulty"); // question id
            item1.add("How difficult was the content?"); // text
            item1.add("one"); // type
            item1.add("false"); // required
            item1.add("0;easy|1;medium|2;hard"); // response
            activity_feedback_form_items.add(item1);
        }
    }

    
    // @@@@ RECOMMENDATIONS getting the recommendations from the recommendation interface
    // it will fill reactive recommendations and proactive scoring (sequencing)
    public void fillRecommendations(String last_content_id, String last_content_res, int n) {
        // @@@ get the content provider
        String last_content_provider = ""; 
        if (last_content_id != null && last_content_id.length()>0) 
        	last_content_provider = getProviderByContentName(last_content_id);
        //recommendation_list = um_interface.getRecommendations(usr, grp, sid, cid, domain, last_content_id, last_content_res, last_content_provider, n, contentList);
        
        ArrayList<ArrayList<String[]>> all_rec = rec_interface.getRecommendations(usr, grp, sid, cid, domain, last_content_id, last_content_res, last_content_provider, 
        		contentList, cm.agg_reactiverec_max, cm.agg_proactiverec_max, cm.agg_reactiverec_threshold, cm.agg_proactiverec_threshold,
        		cm.agg_reactiverec_method, cm.agg_proactiverec_method);
        if (all_rec != null){
            // reactive recommendations
        	recommendation_list = new ArrayList<ArrayList<String>>();
            if(cm.agg_reactiverec_enabled){
            	ArrayList<String[]> reactive_rec = all_rec.get(0);
                for(String[] rec : reactive_rec){
                	ArrayList<String> r = new ArrayList<String>();
                	r.add(rec[0]); // rec item id
                	String topic = "";
                	//System.out.println(rec[2]);
                	if(mapContentTopic != null) topic = mapContentTopic.get(rec[2]);
                	if(topic == null) topic = "";
                	r.add(topic);  // topic id
                	r.add(contentList.get(rec[2])[0]); // resource id
                	r.add(rec[2]);  // content id
                	r.add(rec[3].substring(0,5)); // score
                	r.add("Do you think the above example will help you to solve the original problem?"); // feedback question
                	r.add("-1"); // stored value of the feedback
                	recommendation_list.add(r);
                }            	
            }
            
            // proactive recommendations
            if(cm.agg_proactiverec_enabled){
            	ArrayList<String[]> proactive_rec = all_rec.get(1);
            	contentSequencingScores = new HashMap<String, Double>();
            	topicSequencingScores = new HashMap<String, double[]>();
            
            	for(String[] rec : proactive_rec){
                	double score = 0;
                	try {score = Double.parseDouble(rec[3]);}catch(Exception e){}
                	contentSequencingScores.put(rec[2],score);
                }

                for (String[] topic_data : topicList) {
                    ArrayList<String>[] topic_content = topicContent.get(topic_data[0]);
                    double[] seqScores = new double[topic_content.length];
                    for(int i=0;i<topic_content.length;i++){
                        ArrayList<String> contents = topic_content[i];
                        for(String content_name: contents){
                            Double s = contentSequencingScores.get(content_name);
                            if(s != null)
                            if (s>seqScores[i]){
                                seqScores[i] = s; 
                            }
                        }
                    }
                    topicSequencingScores.put(topic_data[0], seqScores);
                }
            }
        }
    }


    public static int inStringArray(String[] array, String s) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(s))
                return i;
        }
        return -1;
    }


    public void mapContentToTopic(){
    	mapContentTopic = new HashMap<String, String>();
    	for (String[] topic : topicList) {
            ArrayList<String>[] oneTypeContents = topicContent.get(topic[0]);
            for(int i=0;i<oneTypeContents.length;i++){
            	for (String content_name : oneTypeContents[i]) {
            		if(!mapContentTopic.containsKey(content_name)){
            			mapContentTopic.put(content_name,topic[0]);
            		}
            	}
            }
            
        }
    	
//    	for (Map.Entry<String, String> content : mapContentTopic.entrySet()) {
//            String content_name = content.getKey();
//            String topic_name = content.getValue();
//            System.out.println(content_name + " IN " + topic_name);
//        }
    }

    // DEPRECATED
    public String[] getTopicByContentName(String content_name) {
        String[] res = null;
        for (String[] topic : topicList) {
            ArrayList<String>[] oneTypeContents = topicContent.get(topic[0]);
            ArrayList<String> questions = oneTypeContents[0];
            int i = 1;
            for (String question : questions) {
                if (content_name.equalsIgnoreCase(question)) {
                    res = new String[2];
                    res[0] = topic[0];
                    res[1] = "" + i;
                    return res;
                }
                i++;
            }
        }
        return null;
    }

    public String getProviderByContentName(String content_name){
    	String provider = "unknown";
    	String[] contentData = contentList.get(content_name);
    	if(contentData != null){
    		provider = contentData[5];
    	} 
    	
    	return provider;
    }
    
    // parameters can be
    // proactive_rec=yes, reactive_rec=yes, 
    // proactive_rec_threshold, reactive_rec_threshold,
    // proactive_rec_max, reactive_rec_max,
    // proactive_rec_method, reactive_rec_method,
    //
    //
    public void overwriteConfigForUser(String parameters){
    	if (parameters == null || parameters.length()==0) return;
    	String[] params = parameters.split(",");
    	for(String param : params){
    		String[] pair = param.split("=");
    		if(pair[0].equalsIgnoreCase("proactive_rec")) cm.agg_proactiverec_enabled = pair[1].equalsIgnoreCase("yes");
    		if(pair[0].equalsIgnoreCase("reactive_rec")) cm.agg_reactiverec_enabled = pair[1].equalsIgnoreCase("yes");
    		if(pair[0].equalsIgnoreCase("proactive_rec_threshold")) 
    			try {cm.agg_proactiverec_threshold = Double.parseDouble(pair[1]);} catch(Exception e) {}
    		if(pair[0].equalsIgnoreCase("reactive_rec_threshold")) 
    			try {cm.agg_reactiverec_threshold = Double.parseDouble(pair[1]);} catch(Exception e) {}
    		if(pair[0].equalsIgnoreCase("proactive_rec_max")) 
   			    try {cm.agg_proactiverec_max  = Integer.parseInt(pair[1]);} catch(Exception e) {}
    		if(pair[0].equalsIgnoreCase("reactive_rec_max")) 
   			    try {cm.agg_reactiverec_max  = Integer.parseInt(pair[1]);} catch(Exception e) {}
    		if(pair[0].equalsIgnoreCase("proactive_rec_method")) cm.agg_proactiverec_method = pair[1].trim();
    		if(pair[0].equalsIgnoreCase("reactive_rec_method")) cm.agg_reactiverec_method = pair[1].trim();
    	}
    }
    
    
    
    public boolean trackAction(String action, String comment) {
        boolean connection_was_open = false;
        try {
            connection_was_open = !agg_db.conn.isClosed();
        } catch (Exception e) {
        }
        boolean res = false;
        if (!connection_was_open)
            agg_db.openConnection();
        if (agg_db.insertTrackAction(usr, grp, sid, action, comment))
            res = true;
        if (!connection_was_open)
            agg_db.closeConnection();
        return res;
    }

    public String genJSONHeader() {
        String res = "{\n  version:\"0.0.3\",\n"
                + "  context:{ learnerId:\""
                + usr
                + "\",group:{id:\""
                + grp
                + "\",name:\""
                + grp_name
                + "\"}},\n"
                + "  reportLevels:[{id:\"p\",name:\"Progress\"},{id:\"k\",name:\"Knowledge\"}],\n"
                + "  resources:[\n";
        for(String[] r : resourceList){
            if(r[4] == null || r[4].length()<3) r[4] = "010";
            res += "    {id:\"" + r[0] + "\",name:\"" + r[1] + "\", " +
                   "updateStateOn: {done: " + ((r[4].charAt(0)=='1')?"true":"false") +
                   ", winClose: " + ((r[4].charAt(1)=='1')?"true":"false") +
                   ", winCloseIfAct: " + ((r[4].charAt(2)=='1')?"true":"false") + "}},\n";
        }
        res = res.substring(0, res.length()-2);        
                
                
        res +=  "\n  ]";
        return res;
    }

    public String genJSONVisProperties() {
        String res = "vis:{\n  topicSizeAttr:[\"difficulty\",\"importance\"],\n  color:{binCount:7,value2color:function (x) { var y = Math.log(x)*0.25 + 1;  return (y < 0 ? 0 : y); }}\n}";
        return res;
    }

    public String genJSONTopics() {
        String topics = "  topics:[\n";

        for (String[] topic : topicList) {
            String visible = "true";
            if (topic[3].equalsIgnoreCase("0"))
                visible = "false";
            topics += "  {\n    id:\"" + topic[0] + "\",name:\"" + topic[1]
                    + "\",difficulty:"
                    + df.format(getTopicDifficulty(topic[1])) + ",importance:"
                    + df.format(getTopicImportance(topic[1])) + ",order:"
                    + topic[2] + ",concepts:[";
            // TODO : concepts
            topics += "],isVisible:" + visible + ",\n";
            topics += "    activities:{ \n";
            //
            
            ArrayList<String>[] content = topicContent.get(topic[0]);
            
            for(int i=0;i<content.length;i++){
                String resourceName = resourceList.get(i)[0];
                ArrayList<String> contentItems = content[i];
                topics += "      \""+resourceName+"\":[\n";
                if (contentItems != null && contentItems.size() > 0) {
                    for (String item : contentItems) {
                        String[] content_data = this.contentList.get(item);
                        topics += "        {id:\"" + item + "\",name:\""
                                + content_data[1] + "\",url:\"" + content_data[2]
                                + "\"},\n";
                    }
                    topics = topics.substring(0, topics.length() - 2); // get rid of
                                                                       // the last
                                                                       // comma
                }
                topics += "\n      ],\n";
            }
            topics = topics.substring(0, topics.length() - 2);

            topics += "\n    }\n  },\n";
        }

        topics = topics.substring(0, topics.length() - 2); // get rid of the
                                                           // last comma
        // user_levels = user_levels.substring(0,user_levels.length()-1);
        topics += "\n  ]";
        return topics;
    }

    public String genJSONLearnerState(String student) {
        String res = "  state:{\n";
        Map<String, double[]> student_t_l = null;
        if (peers_topic_levels != null)
            student_t_l = peers_topic_levels.get(student);

        Map<String, double[]> student_c_l = null;
        if (peers_content_levels != null)
            student_c_l = peers_content_levels.get(student);

        String topic_levels = "      topics:{\n";
        String content_levels = "      activities:{\n";

        String seq = "";
        boolean sequencing = (student.equalsIgnoreCase(usr));
        
        for (String[] topic : topicList) {
            String topic_name = topic[0];
            double[] levels = null;
            String resourceName = "";
            seq = "";
            if (sequencing){
                seq = ",sequencing:{";
                
                
                for(int i=0;i<resourceList.size();i++){
                    resourceName = resourceList.get(i)[0];
                    
                    seq += "\""+resourceName+"\":"
                            + df.format(getTopicSequenceScore(topic_name,resourceName))
                            + " ,";
                    
                }
                seq = seq.substring(0,seq.length() - 2);
                seq += "}";
            }
            if (student_t_l != null)
                levels = student_t_l.get(topic_name);
            if (levels != null) {
                topic_levels += "       \"" + topic_name + "\": {values:{";
                for(int i=0;i<resourceList.size();i++){
                    resourceName = resourceList.get(i)[0];
                    topic_levels += "\""+resourceName+"\":{\"k\":" + df.format(levels[2*i]) + ",\"p\":" + df.format(levels[2*i+1]) + "},";
                }
                topic_levels = topic_levels.substring(0,topic_levels.length() - 1);
                topic_levels += "}" + seq + "},\n";
                
            } else {
                topic_levels += "       \"" + topic_name + "\": {values:{";
                for(int i=0;i<resourceList.size();i++){
                    resourceName = resourceList.get(i)[0];
                    topic_levels += "\""+resourceName+"\":{\"k\":" + df.format(0) + ",\"p\":" + df.format(0) + "},";
                }
                topic_levels = topic_levels.substring(0,topic_levels.length() - 1);
                topic_levels += "}" + seq + "},\n";
            }

            content_levels += "       \"" + topic_name + "\": {\n";
            ArrayList<String>[] content = topicContent.get(topic_name);
            
            for(int i=0;i<content.length;i++){
                resourceName = resourceList.get(i)[0];
                ArrayList<String> contentItems = content[i];
                content_levels += "        \""+resourceName+"\":{";
                if (contentItems != null && contentItems.size() > 0) {
                    content_levels += "\n";
                    for (String item : contentItems) {
                        // System.out.println("Q:"+q);
                        seq = "";
                        if (sequencing)
                            seq = ",sequencing:"
                                    + df.format(getContentSequenceScore(item));
                        if (student_c_l == null)
                            levels = null;
                        else
                            levels = student_c_l.get(item);
                        if (levels != null) {
                            content_levels += "          \"" + item
                                    + "\": {values:{\"k\":" + df.format(levels[0])
                                    + ",\"p\":" + df.format(levels[1]) + "}" + seq
                                    + "},\n";
                        } else {
                            content_levels += "          \"" + item
                                    + "\": {values:{\"k\":0,\"p\":0}" + seq
                                    + "},\n";
                        }

                        // content_levels += "    {},\n";
                    }
                    content_levels = content_levels.substring(0,content_levels.length() - 2); // get rid of the last comma
                    content_levels += "\n        },\n";
                } else {
                    content_levels += "},\n";
                }
                
                
            }
            content_levels = content_levels.substring(0,content_levels.length() - 2);
            content_levels += "\n       },\n";
        }
 
        content_levels = content_levels.substring(0,content_levels.length() - 2);
        topic_levels = topic_levels.substring(0, topic_levels.length() - 2);

        topic_levels += "\n      },\n";
        content_levels += "\n      }\n";

        res += topic_levels + content_levels + "   }";

        return res;
    }

    // agg: 1: all class students, 2: only top N students
    public String genJSONGroupState(int agg) {
        String res = "  state:{\n";
        Map<String, double[]> aggs_t_l = aggs1_topic_levels;
        Map<String, double[]> aggs_c_l = aggs1_content_levels;
        if (agg == 2) {
            aggs_t_l = aggs2_topic_levels;
            aggs_c_l = aggs2_content_levels;
        }

        String topic_levels = "    topics:{\n";
        String content_levels = "    activities:{\n";
        
        String resourceName = null;

        for (String[] topic : topicList) {
            String topic_name = topic[0];
            double[] levels = null;
            if (aggs_t_l != null)
                levels = aggs_t_l.get(topic_name);
            // level k,p per topic
            if (levels != null) {
                topic_levels += "       \"" + topic_name + "\": {values:{";
                for(int i=0;i<resourceList.size();i++){
                    resourceName = resourceList.get(i)[0];
                    topic_levels += "\""+resourceName+"\":{\"k\":" + df.format(levels[2*i]) + ",\"p\":" + df.format(levels[2*i+1]) + "},";
                }
                topic_levels = topic_levels.substring(0,topic_levels.length() - 1);
                topic_levels += "}},\n";
                
            } else {
                topic_levels += "       \"" + topic_name + "\": {values:{";
                for(int i=0;i<resourceList.size();i++){
                    resourceName = resourceList.get(i)[0];
                    topic_levels += "\""+resourceName+"\":{\"k\":" + df.format(0) + ",\"p\":" + df.format(0) + "},";
                }
                topic_levels = topic_levels.substring(0,topic_levels.length() - 1);
                topic_levels += "}},\n";
            }
            

            content_levels += "      \"" + topic_name + "\": {\n";
            ArrayList<String>[] content = topicContent.get(topic_name);
            
            for(int i=0;i<content.length;i++){
                resourceName = resourceList.get(i)[0];
                ArrayList<String> contentItems = content[i];
                content_levels += "        \""+resourceName+"\":{";
                if (contentItems != null && contentItems.size() > 0) {
                    content_levels += "\n";
                    for (String item : contentItems) {
                        if (aggs_c_l == null)
                            levels = null;
                        else
                            levels = aggs_c_l.get(item);
                        if (levels != null) {
                            content_levels += "          " 
                                    + "\"" + item + "\": {values:{\"k\":" + df.format(levels[0]) + ",\"p\":" + df.format(levels[1]) + "}},\n";
                        } else {
                            content_levels += "          "
                                    + "\"" + item + "\": {values:{\"k\":0,\"p\":0}},\n";
                        }

                    }
                    content_levels = content_levels.substring(0,content_levels.length() - 2); // get rid of the last comma
                    content_levels += "\n        },\n";
                } else {
                    content_levels += "},\n";
                }
                
                
            }
            content_levels = content_levels.substring(0,content_levels.length() - 2);
            content_levels += "\n      },\n";
        }
        content_levels = content_levels.substring(0,content_levels.length() - 2);
        topic_levels = topic_levels.substring(0, topic_levels.length() - 2);

        topic_levels += "\n    },\n";
        content_levels += "\n    }\n";

        String learnersids = "learnerIds:[  ";
        if (agg == 2) {
            for (String studentid : top_students_ids) {
                learnersids += "\"" + studentid + "\", ";
            }
            learnersids = learnersids.substring(0, learnersids.length() - 2);
        } else {
            for (String[] studentdata : class_list) {
                if (non_students.get(studentdata[0]) == null) {
                    learnersids += "\"" + studentdata[0] + "\", ";
                }
            }
            learnersids = learnersids.substring(0, learnersids.length() - 2);
        }
        learnersids += "]";
        res += topic_levels + content_levels + "\n },\n  " + learnersids;

        return res;
    }

    public String genJSONRecommendation() {
        String res = "  recommendation:[\n";
        if (recommendation_list != null && recommendation_list.size() > 0) {
            for (ArrayList<String> rec : recommendation_list) {
                String stored_value = "-1";
                if (rec.get(6) != null)
                    stored_value = rec.get(6);
                res += "    {recommendationId:\"" + rec.get(0)
                        + "\",topicId:\"" + rec.get(1) + "\",resourceId:\""
                        + rec.get(2) + "\",activityId:\"" + rec.get(3)
                        + "\",score:" + rec.get(4) + ",feedback:{text:\""
                        + rec.get(5) + "\", storedValue:" + stored_value
                        + "}},\n";
            }
            res = res.substring(0, res.length() - 2);
        }

        res += "\n  ]";
        return res;
    }

    public String genJSONFeedback() {
        String res = "  feedback:{\n";
        if (activity_feedback_form_items != null
                && activity_feedback_form_items.size() > 0) {
            // the activity_feedback_form_id
            res += "    id:\"" + activity_feedback_id + "\",\n    items:[\n";
            for (ArrayList<String> fed : activity_feedback_form_items) {
                res += "      {id:\"" + fed.get(0) + "\",text:\"" + fed.get(1)
                        + "\",type:\"" + fed.get(2) + "\",required:\""
                        + fed.get(3) + "\",\n          response:[";
                String[] _response_items = fed.get(4).split("\\|");
                if (_response_items != null && _response_items.length > 0) {
                    for (int i = 0; i < _response_items.length; i++) {
                        String[] _response = _response_items[i].split(";");
                        if (_response != null && _response.length == 2) {
                            res += "{value:" + _response[0] + ",label:\""
                                    + _response[1] + "\"},";
                        }
                    }
                    res = res.substring(0, res.length() - 1);
                }
                res += "]},\n";
            }
            res = res.substring(0, res.length() - 2);
            res += "\n    ]\n";
        }

        res += "  }";
        return res;
    }

    // generate JSON output for all the data!!!!
    public String genAllJSON(int n, int top) {
        String header = genJSONHeader();
        String visprop = genJSONVisProperties();
        String topics = genJSONTopics();

        String learners = "learners:[ \n";
        int c = 0;
        for (String[] learner : class_list) {
            String ishidden = "false";
            if (non_students.get(learner[0]) != null) ishidden = "true";
            if(c<n-1 || learner[0].equalsIgnoreCase(usr) || n == -1){
                learners += "{\n  id:\"" + learner[0] + "\",name:\"" + learner[1]
                        + "\",isHidden:" + ishidden + ",\n  "
                        + genJSONLearnerState(learner[0]) + "\n},\n";                
            }
            c++;
        }
        learners = learners.substring(0, learners.length() - 2);
        learners += "\n]";

        String aggs_levels = "groups:[\n";

        String aggs_1 = "{\n  name:\"Class Average\",\n";
        String aggs_2 = "{\n  name:\"Top " + top + "\",\n";

        aggs_1 += genJSONGroupState(1) + "\n},\n";
        aggs_2 += genJSONGroupState(2) + "\n}\n";

        aggs_levels += aggs_1 + aggs_2 + "]";

        return header + ",\n" + topics + ",\n" + learners + ",\n" + aggs_levels
                + ",\n" + visprop + "\n}";
    }

    // REVIEW generate the main JSON response for the logged in user
    public String genUserJSON(String last_content_id, String last_content_res) {
        String output = "{\n  lastActivityId:\"" + last_content_id
                + "\",\n  lastActivityRes:" + last_content_res
                + ",\n  learner:{\n    id:\"" + usr + "\",name:\"" + usr_name
                + "\",\n";
        output += genJSONLearnerState(usr);
        output += "\n  },\n"; // closing learner object
        output += genJSONRecommendation() + ",\n";
        output += genJSONFeedback();
        output += "\n}";
        return output;
    }
}
