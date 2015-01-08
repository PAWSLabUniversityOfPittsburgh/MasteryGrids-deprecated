

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PAWSRecInterface implements RecInterface {

	//TODO later change the server to adapt2
	private String server = "http://adapt2.sis.pitt.edu";
	//private String server = "http://localhost:8080";
	
    private String RecServiceURL = server + "/recommendation/GetRecommendations";
   
	@Override
	public ArrayList<ArrayList<String[]>> getRecommendations(String usr,
            String grp, String sid, String cid, String domain, String lastContentId,
            String lastContentResult, String lastContentProvider,
            HashMap<String, String[]> contentList, int maxReactiveRec,int maxProactiveRec,
            double reactiveRecThreshold, double proactiveRecThreshold, 
            String reactiveRecMethod, String proactiveRecMethod) {
		
		ArrayList<ArrayList<String[]>> result = new ArrayList<ArrayList<String[]>>();
		ArrayList<String[]> reactive_list = new ArrayList<String[]>();
    	ArrayList<String[]> proactive_list = new ArrayList<String[]>();
		InputStream in = null;
		// A JSON object is created to pass the required parameter to the recommendation service implemented by GetRecommendations.java
		try {
			HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(RecServiceURL);

            method.addParameter("usr", URLEncoder.encode(usr, "UTF-8"));
            method.addParameter("grp", URLEncoder.encode(grp, "UTF-8"));
            method.addParameter("sid", URLEncoder.encode(sid, "UTF-8"));
            method.addParameter("cid", URLEncoder.encode(cid, "UTF-8"));
            method.addParameter("domain", URLEncoder.encode(domain, "UTF-8"));
            method.addParameter("lastContentId", URLEncoder.encode(lastContentId, "UTF-8"));
            method.addParameter("lastContentResult", URLEncoder.encode(lastContentResult, "UTF-8"));
            method.addParameter("lastContentProvider", URLEncoder.encode(lastContentProvider, "UTF-8"));
            if(maxReactiveRec > -1) method.addParameter("reactive_max", maxReactiveRec+"");
            if(maxProactiveRec > -1) method.addParameter("proactive_max", maxProactiveRec+"");
            if(reactiveRecThreshold > -1) method.addParameter("reactive_threshold", reactiveRecThreshold+"");
            if(proactiveRecThreshold > -1) method.addParameter("proactive_threshold", proactiveRecThreshold+"");
            if(reactiveRecMethod != null && reactiveRecMethod.length()>0) method.addParameter("reactive_method", reactiveRecMethod);
            if(proactiveRecMethod != null && proactiveRecMethod.length()>0) method.addParameter("proactive_method", proactiveRecMethod);
            method.addParameter("contents", getContents(contentList));
            //System.out.println(method.getRequestBodyAsString());  
            int statusCode = client.executeMethod(method);

            if (statusCode != -1) {
            	
                in = method.getResponseBodyAsStream();
//                InputStreamReader is = new InputStreamReader(in);
//                BufferedReader br = new BufferedReader(is);
//                String read = br.readLine();
//
//                while(read != null) {
//                    System.out.println(read);
//                    read =br.readLine();
//                }
                JSONObject json =  readJsonFromStream(in);
                in.close();
                if(json != null){
	                if (json.has("error")) {
	                    System.out.println("Error:[" + json.getString("errorMsg") + "]");
	                    System.out.println(json.toString());
	                } else {
	                	//System.out.println(json.toString());
	                	JSONObject reactive = json.getJSONObject("reactive");
	                	JSONObject proactive = json.getJSONObject("proactive");
	                	
	                	
	                	String r_recid = "";
	                	String p_recid = "";
	                	if (reactive.has("id")) r_recid = reactive.getString("id");
	                	if (proactive.has("id")) p_recid = proactive.getString("id");
	                	
	                	JSONArray json_reactive_list = null;
	                	JSONArray json_proactive_list = null;
	                	if (reactive.has("contentScores")) json_reactive_list = reactive.getJSONArray("contentScores");
	                	if (proactive.has("contentScores")) json_proactive_list = proactive.getJSONArray("contentScores");
	                	//System.out.println("Reactive rec");
	                	if(json_reactive_list != null){
	                		for (int i = 0; i < json_reactive_list.length(); i++) {
	                            JSONObject jsonrec = json_reactive_list.getJSONObject(i);
	                            String[] rec = new String[4];
	                            rec[0] = jsonrec.getString("rec_item_id");
	                            rec[1] = jsonrec.getString("approach");
	                            rec[2] = jsonrec.getString("content");
	                            rec[3] = jsonrec.getDouble("score") + "";
	                            reactive_list.add(rec);
	                            //System.out.println("  "+rec[2]);
	                		}
	                	}
	                	//System.out.println("Proactive rec");
	                	if(json_proactive_list != null){
	                		for (int i = 0; i < json_proactive_list.length(); i++) {
	                            JSONObject jsonrec = json_proactive_list.getJSONObject(i);
	                            String[] rec = new String[4];
	                            rec[0] = jsonrec.getString("rec_item_id");
	                            rec[1] = jsonrec.getString("approach");
	                            rec[2] = jsonrec.getString("content");
	                            rec[3] = jsonrec.getDouble("score") + "";
	                            proactive_list.add(rec);
	                            //System.out.println("  "+rec[2]);
	                		}
	                	}
	                }
                }else{
                	// json null
                }
               
            }

        } catch (Exception e) {
        	result = null;
            e.printStackTrace();
            return result;
        }finally{
        	
        }
		result.add(reactive_list);
    	result.add(proactive_list);
		return result;
	}

//	private String getTopicContents(HashMap<String, ArrayList<String>[]> topicContent) {
//		String text = "";
//		ArrayList<String>[] contents;
//		for (String t : topicContent.keySet())
//		{
//			text += t + ","; // the first element in each commar separated list is the topic name;  topics are separated by ~
//			contents = topicContent.get(t);
//			for (ArrayList<String> c : contents)
//			{
//				text += c+ "," ;
//			}
//			text.substring(0, text.length()-1); // this is for ignoring the last comma 
//			text += "~"; // this is used for separating topics
//		}
//		text.substring(0, text.length()-1); // this is for ignoring the last ~ 
//		return text;
//	}

	private String getContents(HashMap<String, String[]> contentList) {
		String contents = "";
		for (String c : contentList.keySet())
			contents += c + ",";
		if(contents.length()>0) contents = contents.substring(0, contents.length()-1); //this is for ignoring the last ,
		return contents;
	}

	private ArrayList<ArrayList<String[]>> processRecommendations(URL url) {
		// TODO this method should be implemented later
		return null;
	}
	
	public static JSONObject readJsonFromStream(InputStream is)  throws Exception{
		JSONObject json = null;
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			json = new JSONObject(jsonText);
		}catch(Exception e){
			e.printStackTrace();
		}
		return json;
	}
	
    private static String readAll(Reader rd) throws Exception {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
