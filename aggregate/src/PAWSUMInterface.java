import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class PAWSUMInterface implements UMInterface {
    private String server = "http://adapt2.sis.pitt.edu";
    //private String server = "http://localhost:8080";

    private String userInfoServiceURL = server
            + "/aggregateUMServices/GetUserInfo";
    private String classListServiceURL = server
            + "/aggregateUMServices/GetClassList";
    private String questionsActivityServiceURL = server
            + "/aggregateUMServices/GetQuestionsActivity";
    private String examplesActivityServiceURL = server
            + "/aggregateUMServices/GetExamplesActivity";
    //private String conceptLevelsServiceURL = server + "/cbum/ReportManager";
    private String conceptLevelsServiceURL = "http://adapt2.sis.pitt.edu/cbum/ReportManager";

    private String contentKCURL = server
            + "/aggregateUMServices/GetContentConcepts";

    private boolean contrainOutcomeLevel = true; // knowledge levels will be computed only on outcome concepts
    
    private HashMap<String, double[]> kcSummary; // knowledge components (concepts) and the level of knowledge of the user in each of them
    private HashMap<String, ArrayList<String[]>> kcByContent; // for each content there is an array list of kc (concepts) with id, weight (double) and direction (prerequisite/outcome)
    private HashMap<String, double[]> contentSummary;

    
    // SERVICE
    public String[] getUserInfo(String usr, String key) {
        String[] data = null;
        try {
            String url = userInfoServiceURL + "?usr=" + usr + "&key="
                    + key;
            JSONObject json = readJsonFromUrl(url);
            // System.out.println(json.toString());
            if (json.has("error")) {
                System.out
                        .println("Error:[" + json.getString("errorMsg") + "]");
            } else {
                data = new String[2];
                data[0] = json.getString("learnerName");
                data[1] = json.getString("learnerEmail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // SERVICE
    // an arraylist with the students/users in the class. Each item has: userid,
    // login (username), name, email
    public ArrayList<String[]> getClassList(String grp, String key) {
        ArrayList<String[]> classList = null;
        try {
            String url = classListServiceURL + "?grp=" + grp + "&key="
                    + key;
            JSONObject json = readJsonFromUrl(url);
            // System.out.println(json.toString());
            if (json.has("error")) {
                System.out
                        .println("Error:[" + json.getString("errorMsg") + "]");
            } else {
                classList = new ArrayList<String[]>();
                JSONArray learners = json.getJSONArray("learners");

                for (int i = 0; i < learners.length(); i++) {
                    JSONObject jsonobj = learners.getJSONObject(i);
                    String[] learner = new String[3];
                    learner[0] = jsonobj.getString("learnerId");
                    learner[1] = jsonobj.getString("name");
                    learner[2] = jsonobj.getString("email");
                    classList.add(learner);
                    // System.out.println(jsonobj.getString("name"));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classList;
    }


    /**
     * This is the main method!!
     * @ return each double[]: knowledge, progress, attempts/loads, success rate, completion, other 1, other 2
     */
    public HashMap<String, double[]> getContentSummary(String usr, String grp, String sid, String cid, String domain, 
            HashMap<String, String[]> contentList, ArrayList<String> options) {

        //HashMap<String,String> contentWithProvider = generateContentList(contentByProvider);
        contentSummary = new HashMap<String, double[]>(); 
        
        // fill the hashmap kcByContent with content_name, arraylist of [concept_name, weight, direction]
        kcByContent = getContentKCs(domain,contentList);
        
        // 1. GET THE LEVELS OF KNOWLEDGE OF THE USER IN CONCEPTS
        // FROM USER MODEL USING THE USER MODEL INTERFACE
        kcSummary = this.getConceptLevels(usr, domain, grp); // Concept Knowledge levels
        //System.out.println(kcSummary.get("IntDataType")[0]);
        // 2. GET USERS LOGS FROM DIFFERENT CONTENT PROVIDERS UM
        HashMap<String, String[]> examples_activity = this.getUserExamplesActivity(usr, domain);
        HashMap<String, String[]> questions_activity = this.getUserQuestionsActivity(usr, domain);
//        HashMap<String, String[]> user_activity = getUserContentActivity(usr, domain);

        // 3. COMPUTE AGGREGATE LEVELS FOR CONTENT (knowledge and progress)
        // initialize the resulting data structure
        
        for (Map.Entry<String, String[]> content : contentList.entrySet()) {
            double[] kpvalues = new double[7];

            // compute Knowledge levels for content from knowledge levels of the
            // concepts
            String content_name = content.getKey();
            String[] content_data = content.getValue();
            String content_provider = content_data[5];

            // the array of concepts for the content item (content item can be a
            // question, example or reading)
            //System.out.println("CONTENT: "+content_name);
            ArrayList<String[]> c_concepts = kcByContent.get(content_name);
            double sum_weights = 0.0;
            double user_concept_k = 0.0;
            double user_content_k = 0.0;
            // System.out.println(topic[0]); //
            if (c_concepts != null && kcSummary != null) {
                for (int j = 0; j < c_concepts.size(); j++) {
                    String[] _concept = c_concepts.get(j);
                    // if (_concept.length<2)
                    //System.out.println(_concept[0]+" : "+_concept.length);

                    String direction = _concept[2]; // outcome or prerequisite
                    // only compute levels in outcome concepts (how much already
                    // know that is new in the content?)
                    // TODO
                    if (direction.equalsIgnoreCase("outcome") || !contrainOutcomeLevel) {
                        String concept = _concept[0];
                        double weight = Double.parseDouble(_concept[1]);
                        if (kcSummary.get(concept) == null)
                            user_concept_k = -1.0;
                        else {
                            user_concept_k = kcSummary.get(concept)[0];
                            sum_weights += weight;
                            user_content_k += user_concept_k * weight;
                        }

                    }
                }

            }
            if (sum_weights == 0.0)
                user_content_k = 0.0;
            else
                user_content_k = user_content_k / sum_weights;
            kpvalues[0] = user_content_k;
            
            // Progress level of examples and animated exmaples
            double user_content_p = 0.0;
            if (content_provider.equalsIgnoreCase("webex") || content_provider.equalsIgnoreCase("animatedexamples")) {
                
                if (examples_activity == null  ||  examples_activity.get(content_name) == null) {
                    user_content_p = 0.0;
                } else {
                    String[] example_activity = examples_activity.get(content_name);

                    double distinct_actions = Double.parseDouble(example_activity[2]);
                    double total_lines = Double.parseDouble(example_activity[3]);
                    if (total_lines == 0)
                        total_lines = 1.0;
                    user_content_p = distinct_actions / total_lines;
                    kpvalues[2] = Double.parseDouble(example_activity[1]);
                    kpvalues[3] = -1;
                    kpvalues[4] = user_content_p;
                    kpvalues[5] = distinct_actions;
                    kpvalues[6] = total_lines;
                }
             // System.out.println("K for example "+content_name+" : "+kpvalues[0]);	
                //knowledge, progress, attempts/loads, success rate, completion, other 1, other 2

            }
            // Progress level related with Questions
            if (content_provider.equalsIgnoreCase("quizjet") || content_provider.equalsIgnoreCase("sqlknot")) {
                if (questions_activity == null || questions_activity.get(content_name) == null) {
                    user_content_p = 0.0;
                } else {

                    String[] question_activity = questions_activity.get(content_name);
                    double nattemtps = Double.parseDouble(question_activity[1]);
                    double nsuccess = Double.parseDouble(question_activity[2]);
                    if (nsuccess > 0) user_content_p = 1.0;
                    kpvalues[2] = nattemtps;
                    if(nattemtps>0) kpvalues[3] = nsuccess/nattemtps;
                    kpvalues[4] = user_content_p;
                    kpvalues[5] = nattemtps;
                    kpvalues[6] = nsuccess;
                }
            }

            kpvalues[1] = user_content_p;

            contentSummary.put(content_name, kpvalues);
        }
        return contentSummary;
    }


    // ////////////////////////////////////////////
    // LOCAL CLASS METHODS
    // ////////////////////////////////////////////
    
    // HashMap<String,String> contentWithProvider specify which are the content we are interested on
    public HashMap<String, ArrayList<String[]>> getContentKCs(String domain, HashMap<String,String[]> contentList) {
        HashMap<String, ArrayList<String[]>> res = null;
        try {
            String url = contentKCURL + "?domain=" + domain;
            //System.out.println(url);
            JSONObject json = readJsonFromUrl(url);
            //System.out.println("\n\n"+json.toString()+"\n\n");
            if (json.has("error")) {
                //System.out.println("HERE ");
                System.out.println("Error:[" + json.getString("errorMsg") + "]");
            } else {
                res = new HashMap<String, ArrayList<String[]>>();
                JSONArray contents = json.getJSONArray("content");

                for (int i = 0; i < contents.length(); i++) {
                    JSONObject jsonobj = contents.getJSONObject(i);
                    String content_name = jsonobj.getString("content_name");
                    // if the content exist in the course
                    if (contentList.containsKey(content_name)){
//                        if(contentList.get(content_name)[5].equals("webex")){
//                        	System.out.println(content_name+" "+contentList.get(content_name)[5]);                        	
//                        }

                        String conceptListStr = jsonobj.getString("concepts");
//                        if(contentList.get(content_name)[5].equals("webex")){
//                        	System.out.println("  "+conceptListStr);
//                        }
                        ArrayList<String[]> conceptList;
                        if (conceptListStr == null
                                || conceptListStr.equalsIgnoreCase("[null]")
                                || conceptListStr.length() == 0) {
                            conceptList = null;
                        } else {
                            conceptList = new ArrayList<String[]>();
                            String[] concepts = conceptListStr.split(";");
                            for (int j = 0; j < concepts.length; j++) {
                                String[] concept = concepts[j].split(",");
                                //if(concept.length<3) System.out.println("  "+j+" "+content_name+"  "+concept[0]+"  "+concept.length);
                                conceptList.add(concept); // concept_name, weight, direction
//                                if(contentList.get(content_name)[5].equals("webex")){
//                                	System.out.println("  "+concepts[j]+"  " + concept[0] + " " + concept[1] + " " + concept[2]);
//                                	
//                                }
                            }
                        }
                        res.put(content_name, conceptList);
                        
                    }

                    // System.out.println(jsonobj.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


    // CALLING A UM SERVICE
    public HashMap<String, double[]> getConceptLevels(String usr, String domain,
            String grp) {
        HashMap<String, double[]> user_concept_knowledge_levels = new HashMap<String, double[]>();
        try {
            URL url = null;
            if (domain.equalsIgnoreCase("java")) {
                url = new URL(conceptLevelsServiceURL
                        + "?typ=con&dir=out&frm=xml&app=25&dom=java_ontology"
                        + "&usr=" + URLEncoder.encode(usr, "UTF-8") + "&grp="
                        + URLEncoder.encode(grp, "UTF-8"));

            }

            if (domain.equalsIgnoreCase("sql")) {
                url = new URL(conceptLevelsServiceURL
                        + "?typ=con&dir=out&frm=xml&app=23&dom=sql_ontology"
                        + "&usr=" + URLEncoder.encode(usr, "UTF-8") + "&grp="
                        + URLEncoder.encode(grp, "UTF-8"));

            }
            // TODO @@@@
            if (domain.equalsIgnoreCase("c")) {
                url = new URL(conceptLevelsServiceURL
                        + "?typ=con&dir=out&frm=xml&app=23&dom=c_programming"
                        + "&usr=" + URLEncoder.encode(usr, "UTF-8") + "&grp="
                        + URLEncoder.encode(grp, "UTF-8"));

            }
            if (url != null)
                user_concept_knowledge_levels = processUserKnowledgeReport(url);
            //System.out.println(url.toString());
        } catch (Exception e) {
            user_concept_knowledge_levels = null;
            System.out.println("UM: Error in reporting UM for user "+usr);
            //e.printStackTrace();
        }
        return user_concept_knowledge_levels;

    }

    // CALLING A UM SERVICE
    public HashMap<String, String[]> getUserQuestionsActivity(String usr,
            String domain) {
        HashMap<String, String[]> qActivity = null;
        try {
            String url = questionsActivityServiceURL + "?usr=" + usr;
            JSONObject json = readJsonFromUrl(url);

            if (json.has("error")) {
                System.out
                        .println("Error:[" + json.getString("errorMsg") + "]");
            } else {
                qActivity = new HashMap<String, String[]>();
                JSONArray activity = json.getJSONArray("activity");

                for (int i = 0; i < activity.length(); i++) {
                    JSONObject jsonobj = activity.getJSONObject(i);
                    String[] act = new String[3];
                    act[0] = jsonobj.getString("content_name");
                    act[1] = jsonobj.getDouble("nattempts") + "";
                    act[2] = jsonobj.getDouble("nsuccesses") + "";
                    qActivity.put(act[0], act);
                    // System.out.println(jsonobj.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return qActivity;
    }

    // CALLING A UM SERVICE
    public HashMap<String, String[]> getUserExamplesActivity(String usr,
            String domain) {
        HashMap<String, String[]> eActivity = null;
        try {
            String url = examplesActivityServiceURL + "?usr=" + usr;
            JSONObject json = readJsonFromUrl(url);

            if (json.has("error")) {
                System.out
                        .println("Error:[" + json.getString("errorMsg") + "]");
            } else {
                eActivity = new HashMap<String, String[]>();
                JSONArray activity = json.getJSONArray("activity");

                for (int i = 0; i < activity.length(); i++) {
                    JSONObject jsonobj = activity.getJSONObject(i);
                    String[] act = new String[4];
                    act[0] = jsonobj.getString("content_name");
                    act[1] = jsonobj.getDouble("nactions") + "";
                    act[2] = jsonobj.getDouble("distinctactions") + "";
                    act[3] = jsonobj.getDouble("totallines") + "";
                    eActivity.put(act[0], act);

                    // System.out.println(jsonobj.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eActivity;
    }

    
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException,
            JSONException {
        InputStream is = new URL(url).openStream();
        JSONObject json = null;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            json = new JSONObject(jsonText);
        } finally {
            is.close();
        }
        return json;
    }

    private static HashMap<String, double[]> processUserKnowledgeReport(URL url) {

        HashMap<String, double[]> userKnowledgeMap = new HashMap<String, double[]>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(url.openStream());
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("concept");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    NodeList cogLevels = eElement.getElementsByTagName("cog_level");
                    for (int i = 0; i < cogLevels.getLength(); i++) {
                        Node cogLevelNode = cogLevels.item(i);
                        if (cogLevelNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element cogLevel = (Element) cogLevelNode;
                            if (getTagValue("name", cogLevel).trim().equals("application")) {
                                // System.out.println(getTagValue("name",eElement));
                                double[] levels = new double[1];
                                levels[0] = Double.parseDouble(getTagValue("value",cogLevel).trim());
                                userKnowledgeMap.put(getTagValue("name", eElement),levels);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            //e.printStackTrace();
        	System.out.println("UM: Error in reporting UM. URL = "+url);
            return null;
        }
        return userKnowledgeMap;
    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                .getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

}
