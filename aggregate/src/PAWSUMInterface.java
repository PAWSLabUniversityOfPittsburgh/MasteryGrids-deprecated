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

public class PAWSUMInterface implements UMInterface{
	private String server = "http://adapt2.sis.pitt.edu";
	private String secureKey = "put_here_the_key"; // not critical, only will prevent to get personal information of users
	
	private String userInfoServiceURL = server+"/aggregateUMServices/GetUserInfo";
	private String classListServiceURL = server+"/aggregateUMServices/GetClassList";
	private String questionsActivityServiceURL = server+"/aggregateUMServices/GetQuestionsActivity";
	private String examplesActivityServiceURL = server+"/aggregateUMServices/GetExamplesActivity";
	private String conceptLevelsServiceURL = server+"/cbum/ReportManager";
	
	// SERVICE
	public String[] getUserInfo(String usr){
		String[] data = null;
		try{
			String url = userInfoServiceURL+"?usr=" + usr + "&key=" + secureKey;
			JSONObject json = readJsonFromUrl(url);
			//System.out.println(json.toString());
			if (json.has("error")){
				System.out.println("Error:["+json.getString("errorMsg")+"]");
			}else{
				data = new String[2];
				data[0] = json.getString("learnerName");
				data[1] = json.getString("learnerEmail");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return data;
	}
	
	// SERVICE
	// an arraylist with the students/users in the class. Each item has: userid, login (username), name, email 
	public ArrayList<String[]> getClassList(String grp){
		ArrayList<String[]> classList = null;
		try{
			String url = classListServiceURL+"?grp=" + grp + "&key=" + secureKey;
			JSONObject json = readJsonFromUrl(url);
			//System.out.println(json.toString());
			if (json.has("error")){
				System.out.println("Error:["+json.getString("errorMsg")+"]");
			}else{
				 classList = new ArrayList<String[]>();
				 JSONArray learners = json.getJSONArray("learners");
				 
				 for(int i=0;i<learners.length();i++){
					 JSONObject jsonobj = learners.getJSONObject(i);
					 String[] learner = new String[3];
					 learner[0] = jsonobj.getString("learnerId");
					 learner[1] = jsonobj.getString("name");
					 learner[2] = jsonobj.getString("email");
					 classList.add(learner);
					 //System.out.println(jsonobj.getString("name"));
				 }
				 
				 
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return classList;
	}
	
	// SERVICE
	public HashMap<String,Double> getConceptLevels(String usr, String domain, String grp){
		HashMap<String,Double> user_concept_knowledge_levels = new  HashMap<String,Double>();
		try{
			URL url = null;
			if (domain.equalsIgnoreCase("java")){
				url = new URL(conceptLevelsServiceURL + 
						  "?typ=con&dir=out&frm=xml&app=25&dom=java_ontology" + 
						  "&usr="+URLEncoder.encode(usr,"UTF-8")+"&grp=" + URLEncoder.encode(grp,"UTF-8"));
				
			}
			
			if (domain.equalsIgnoreCase("sql")){
				url = new URL(conceptLevelsServiceURL + 
						  "?typ=con&dir=out&frm=xml&app=23&dom=sql_ontology" + 
						  "&usr="+URLEncoder.encode(usr,"UTF-8")+"&grp=" + URLEncoder.encode(grp,"UTF-8"));
				
			}
			if (url != null) user_concept_knowledge_levels = processUserKnowledgeReport(url);
			//System.out.println(url.toString());
		}catch(Exception e){
			user_concept_knowledge_levels = null;
			e.printStackTrace();
		}
		return user_concept_knowledge_levels;

	}
	
	// SERVICE
	public HashMap<String, String[]> getUserQuestionsActivity(String usr, String domain){
		HashMap<String, String[]> qActivity = null;
		try{
			String url = questionsActivityServiceURL + "?usr=" + usr;
			JSONObject json = readJsonFromUrl(url);
			
			if (json.has("error")){
				System.out.println("Error:["+json.getString("errorMsg")+"]");
			}else{
				qActivity = new HashMap<String, String[]>();
				JSONArray activity = json.getJSONArray("activity");
				 
				for(int i=0;i<activity.length();i++){
					JSONObject jsonobj = activity.getJSONObject(i);
					String[] act = new String[3];
					act[0] = jsonobj.getString("content_name");
					act[1] = jsonobj.getDouble("nattempts")+"";
					act[2] = jsonobj.getDouble("nsuccesses")+"";
					qActivity.put(act[0], act);
					//System.out.println(jsonobj.getString("name"));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return qActivity;
	}
	
	// SERVICE
	public HashMap<String, String[]> getUserExamplesActivity(String usr, String domain){
		HashMap<String, String[]> eActivity = null;
		try{
			String url = examplesActivityServiceURL + "?usr=" + usr;
			JSONObject json = readJsonFromUrl(url);
			
			if (json.has("error")){
				System.out.println("Error:["+json.getString("errorMsg")+"]");
			}else{
				eActivity = new HashMap<String, String[]>();
				JSONArray activity = json.getJSONArray("activity");
				 
				for(int i=0;i<activity.length();i++){
					JSONObject jsonobj = activity.getJSONObject(i);
					String[] act = new String[4];
					act[0] = jsonobj.getString("content_name");
					act[1] = jsonobj.getDouble("nactions")+"";
					act[2] = jsonobj.getDouble("distinctactions")+"";
					act[3] = jsonobj.getDouble("totallines")+"";
					eActivity.put(act[0], act);
					
					//System.out.println(jsonobj.getString("name"));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return eActivity;
	}
	
	// SERVICE (work on progress!!)
	// return map of content_name String[]: 
	//			{content_name, content_type, totalsubact, nattempts, nsuccesses, subactviews, distsubactviews, additional}
	// first 2 have sense for exercises, next 3 have sense for examples (subact is a commented line), readings (subact is a page)
	public HashMap<String, String[]> getUserContentActivity(String usr, String domain){
		HashMap<String, String[]> userActivity = null;
		try{
			String url1 = questionsActivityServiceURL + "?usr=" + usr;
			String url2 = examplesActivityServiceURL + "?usr=" + usr;
			JSONObject jsonQ = readJsonFromUrl(url1);
			JSONObject jsonE = readJsonFromUrl(url2);
			userActivity = new HashMap<String, String[]>();
			if (jsonQ.has("error")){
				System.out.println("Error:["+jsonQ.getString("errorMsg")+"]");
			}else{
			
				JSONArray activity = jsonQ.getJSONArray("activity");
				 
				for(int i=0;i<activity.length();i++){
					JSONObject jsonobj = activity.getJSONObject(i);
					String[] act = new String[8];
					act[0] = jsonobj.getString("content_name");
					act[1] = "question";
					act[2] = "0";
					act[3] = jsonobj.getDouble("nattempts")+"";
					act[4] = jsonobj.getDouble("nsuccesses")+"";
					act[5] = "0";
					act[6] = "0";
					act[7] = "";
					userActivity.put(act[0], act);
					
					//System.out.println(jsonobj.getString("name"));
				}
			}
			if (jsonE.has("error")){
				System.out.println("Error:["+jsonE.getString("errorMsg")+"]");
			}else{
			
				JSONArray activity = jsonE.getJSONArray("activity");
				 
				for(int i=0;i<activity.length();i++){
					JSONObject jsonobj = activity.getJSONObject(i);
					String[] act = new String[8];
					act[0] = jsonobj.getString("content_name");
					act[1] = "example";
					act[2] = jsonobj.getDouble("totallines")+"";
					act[3] = "0";
					act[4] = "0";
					act[5] = jsonobj.getDouble("nactions")+"";
					act[6] = jsonobj.getDouble("distinctactions")+"";
					act[7] = "";
					userActivity.put(act[0], act);
					
					//System.out.println(jsonobj.getString("name"));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return userActivity;
	}

	
	// LOCAL METHODS
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException,JSONException {
		InputStream is = new URL(url).openStream();
		JSONObject json = null;
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			json = new JSONObject(jsonText);
		} finally {
			is.close();
		}
		return json;
	}
	private static HashMap<String,Double> processUserKnowledgeReport(URL url) {
		
		HashMap<String, Double> userKnowledgeMap = new HashMap<String, Double>();
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
								//System.out.println(getTagValue("name", eElement));
								userKnowledgeMap.put(getTagValue("name", eElement), Double.parseDouble(getTagValue("value", cogLevel).trim()));
							}
						}
					}
				}
			}

		} catch (Exception e) {
			
			e.printStackTrace();
			return null;
		}
		return userKnowledgeMap;
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
	}

	
	
}
