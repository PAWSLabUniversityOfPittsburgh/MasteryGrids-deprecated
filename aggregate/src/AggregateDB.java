import java.sql.*; 
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
public class AggregateDB extends dbInterface {
	public static DecimalFormat df4 = new DecimalFormat("#.####");
	
	public AggregateDB(String connurl, String user, String pass){
		super(connurl, user, pass);
	}
	
	// returns the name of the grp
	public String getGrpName(String grp){
		try{
			String res = "";
			stmt = conn.createStatement();
			String query = "select G.group_name from ent_group G where G.group_id = '" + grp + "';";
			rs = stmt.executeQuery(query);
	
			while(rs.next()){
				res = rs.getString("group_name");
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			this.releaseStatement(stmt,rs);
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}
	
	}
	public String getDomain(String course_id){
		try{
			String res = "";
			stmt = conn.createStatement();
			String query = "select domain from ent_course  where course_id = '" + course_id + "';";
			rs = stmt.executeQuery(query);
	
			while(rs.next()){
				res = rs.getString("domain");
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			this.releaseStatement(stmt,rs);
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}
	
	}
	
	// returns the name of the grp
	public String getCourseId(String grp){
		try{
			String res = "";
			stmt = conn.createStatement();
			String query = "select G.course_id from ent_group G where G.group_id = '" + grp + "';";
			rs = stmt.executeQuery(query);
	
			while(rs.next()){
				res = rs.getString("course_id");
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			this.releaseStatement(stmt,rs);
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}
	
	}	
	
	public HashMap<String,String> getNonStudents(String group_id){
		HashMap<String,String> res = new HashMap<String,String>();
		try{

			stmt = conn.createStatement();
			String query = "SELECT user_id, user_role FROM ent_non_student WHERE group_id = '"+group_id+"';";
		
			rs = stmt.executeQuery(query);
			while(rs.next()){
				res.put(rs.getString("user_id"),rs.getString("user_role"));
				//System.out.println(rs.getString("user_id"));
			}
			this.releaseStatement(stmt,rs);
			
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			
		}finally{
			this.releaseStatement(stmt,rs);
			
		}
		return res;
	}
	
	// returns the ordered list of topics of a course corresponding to the group (class)
	public ArrayList<String[]> getTopicList(String course_id){
		try{
			ArrayList<String[]> res = new ArrayList<String[]>();
			stmt = conn.createStatement();
			String query = "SELECT T.topic_name, T.display_name, TC.order " +
						   " FROM ent_topic T, rel_topic_course TC " +  
						   " WHERE TC.course_id = '" + course_id + "' " + 
						   " AND TC.topic_id=T.topic_id AND TC.active=1 ORDER BY TC.order ASC;";
		
			rs = stmt.executeQuery(query);
			while(rs.next()){
				String[] topic = new String[4];
				topic[0] = rs.getString("topic_name");
				topic[1] = rs.getString("display_name");
				topic[2] = rs.getString("order");
				topic[3] = "1"; // visibility
				res.add(topic);
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}
	}
	
	public ArrayList<String> getHiddenTopics(String group_id){
		try{
			ArrayList<String> res = new ArrayList<String>();
			stmt = conn.createStatement();
			String query = "SELECT topic_name " +
						   " FROM ent_hidden_topics " +  
						   " WHERE group_id = '" + group_id + "'";
		
			rs = stmt.executeQuery(query);
			while(rs.next()){
				res.add(rs.getString("topic_name"));
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			this.releaseStatement(stmt,rs);
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}
	}
	
	// returns all content for each topic -> questions, examples, readings as arrays of string
	// @@@@ JG FIXED
	public ArrayList<String[]> getContentByTopic(String topic){
		try{
			ArrayList<String[]> res = new ArrayList<String[]>();
			stmt = conn.createStatement();
			String query = "SELECT C.content_id,C.content_name,C.content_type,C.display_name,C.url, C.desc, C.comment " +
						   " FROM ent_content C, rel_topic_content TC, ent_topic T " +
						   " WHERE T.topic_name='" + topic + "' and T.topic_id = TC.topic_id and TC.content_id=C.content_id and C.visible = 1 and TC.visible = 1 " +
						   " ORDER by C.content_type desc, TC.display_order asc";
			rs = stmt.executeQuery(query);
			int i=0;
			while(rs.next()){
				String[] content = new String[7];
				content[0] = rs.getString("content_id");
				content[1] = rs.getString("content_name");
				content[2] = rs.getString("content_type");
				content[3] = rs.getString("display_name");
				content[4] = rs.getString("url");
				content[5] = rs.getString("desc");
				content[6] = rs.getString("comment");
				res.add(content);
				//res.put(content);
				//System.out.println(content[0]+" "+content[2]);
				i++;				
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}

	}
	
	// @@@@ JG REVIEWED: OK!
	public HashMap<String,String[]> getContent(String course_id){
		try{
			HashMap<String,String[]> res = new HashMap<String,String[]>();
			stmt = conn.createStatement();
			String query = "SELECT C.content_id,C.content_name,C.content_type,C.display_name,C.url, C.desc, C.comment" +
							" FROM ent_content C, rel_topic_content TC, rel_topic_course TCO "+ 
							" WHERE TCO.course_id='"+course_id+"' and TCO.topic_id=TC.topic_id and TC.content_id=C.content_id and C.visible = 1 and TC.visible = 1 "+ 
							" ORDER by TCO.order, C.content_type desc, TC.display_order asc";
			rs = stmt.executeQuery(query);
			int i=0;
			while(rs.next()){
				String[] content = new String[5];
				String content_name = rs.getString("content_name");
				content[0] = rs.getString("content_type");
				content[1] = rs.getString("display_name");
				content[2] = rs.getString("url");
				content[3] = rs.getString("desc");
				content[4] = rs.getString("comment");
				res.put(content_name,content);
				//res.put(content);
				//System.out.println(content[0]+" "+content[2]);
				i++;				
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}

	}

	// @@@@ JG DEPRECATED: delete after test!! 
	// returns the examples for topics of a course corresponding to the group (class)
	/*
	public HashMap<String,ArrayList<String>> getTopicExamples(String grp){
		try{
			HashMap<String,ArrayList<String>> res = new HashMap<String,ArrayList<String>>();
			stmt = conn.createStatement();
			String query = "select T.topic_name, group_concat(CC.content_name separator ';') as examples " + 
							"from ent_topic T, rel_topic_course TC, rel_topic_content CC  " + 
							"where TC.course_id = (select course_id from ent_group where group_id='"+grp+"') " +  
							"and TC.topic_id=T.topic_id and TC.active=1 " + 
							"and CC.topic_id=T.topic_id and CC.content_type='example' " + 
							"group by T.topic_id";
			rs = stmt.executeQuery(query);	
			String topic = "";
			ArrayList<String> t_e = null;
			while(rs.next()){
				topic = rs.getString("topic_name");
				String allexamples = rs.getString("examples");
				if (allexamples == null || allexamples.equalsIgnoreCase("[null]") || allexamples.length() == 0){
					t_e = null;
				}else{
					t_e = new ArrayList<String>();
					//System.out.println(topic);
					String[] examples = allexamples.split(";");
					for(int i=0;i<examples.length;i++){	
						t_e.add(examples[i]);
						//System.out.println("  "+examples[i]);
					}
				}
				res.put(topic, t_e);
				//System.out.println();
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}

	}
	*/
	
	
	// @@@@ JG REVIEW OK
	// returns the content for topics of a course
	// Each map is a topic content organized in 3 arrays: questions, examples, readings
	public HashMap<String,ArrayList<String>[]> getTopicContent(String course_id){
		try{

			HashMap<String,ArrayList<String>[]> res = new HashMap<String,ArrayList<String>[]>();
			stmt = conn.createStatement();
			String query = "SELECT T.topic_name, group_concat(C.content_name , ',' , C.content_type order by C.content_type, TC.display_order separator ';') as content " + 
							"FROM ent_topic T, rel_topic_course F, rel_topic_content TC, ent_content C  " + 
							"WHERE F.course_id = '"+course_id+"' " +  
							"and F.topic_id=T.topic_id and F.active=1 " + 
							"and TC.topic_id=T.topic_id and C.content_id = TC.content_id " + 
							"group by T.topic_id";
			rs = stmt.executeQuery(query);	
			String topic = "";
			
			//System.out.println("TOPIC CONTENTS:");
			
			while(rs.next()){
				topic = rs.getString("topic_name");
				String allcontent = rs.getString("content");
				//System.out.println(" "+topic+" : ");
				ArrayList<String>[] all_content = new ArrayList[3];
				all_content[0] = new ArrayList<String>(); // for questions
				all_content[1] = new ArrayList<String>(); // for examples
				all_content[2] = new ArrayList<String>(); // for readings
				
				if (allcontent == null || allcontent.equalsIgnoreCase("[null]") || allcontent.length() == 0){
					//
				}else{
					
					String[] content = allcontent.split(";");
					for(int i=0;i<content.length;i++){	
						String[] item = content[i].split(",");
						if(item[1].equalsIgnoreCase("question")) all_content[0].add(item[0]);
						if(item[1].equalsIgnoreCase("example")) all_content[1].add(item[0]);
						if(item[1].equalsIgnoreCase("reading")) all_content[2].add(item[0]);
					}
				}
				res.put(topic, all_content);
				//System.out.println("   QUESTIONS : ");
				//for(int i=0;i<all_content[0].size();i++) System.out.println("     "+all_content[0].get(i));
				//System.out.println("   EXAMPLES : ");
				//for(int i=0;i<all_content[1].size();i++) System.out.println("     "+all_content[1].get(i));
				//System.out.println("   READINGS : ");
				//for(int i=0;i<all_content[2].size();i++) System.out.println("     "+all_content[2].get(i));
				//System.out.println();
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}

	}

	
	// @@@@ JG: DEPRECATED DELETE AFTER TEST
	public HashMap<String,ArrayList<String[]>> OLD_getContentConcepts(String course_id){
		try{
			HashMap<String,ArrayList<String[]>> res = new HashMap<String,ArrayList<String[]>>();
			stmt = conn.createStatement();
			String query = "select TC.content_name, C.content_type, group_concat(CC.concept_name , ',', cast(CC.weight as char) order by CC.weight separator ';') as concepts " + 
							" from rel_content_concept CC, rel_topic_content TC, rel_topic_course TCO, ent_content C " +  
							" where TCO.course_id='"+course_id+"' and TCO.topic_id=TC.topic_id and TC.content_name=CC.content_name and CC.active=1 "+
							" and CC.direction='outcome' and C.content_name=CC.content_name " + 
							" group by TC.content_name order by TCO.order;";
			rs = stmt.executeQuery(query);
	
			String content_name = "";
			ArrayList<String[]> c_c = null;
			while(rs.next()){
				content_name = rs.getString("content_name");
				
				//System.out.println(topic);
				String allconcepts = rs.getString("concepts");
				if (allconcepts == null || allconcepts.equalsIgnoreCase("[null]") || allconcepts.length() == 0){
					c_c = null;
				}else{
					c_c = new ArrayList<String[]>();
					String[] concepts = allconcepts.split(";");
					for(int i=0;i<concepts.length;i++){
						String[] concept = concepts[i].split(",");
						c_c.add(concept);
						//System.out.println("  "+concept[0]+" "+concept[1]);
					}
				}
				res.put(content_name, c_c);
				//System.out.println();
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}
	}
	
	// @@@@ JG: new version contains outcome and prerequisite OK
	// returns the concepts and respective weights for all content of topics in a course. 
	// If the content is not related to any concept, it is not included
	public HashMap<String,ArrayList<String[]>> getContentConcepts(String course_id){
		try{
			HashMap<String,ArrayList<String[]>> res = new HashMap<String,ArrayList<String[]>>();
			stmt = conn.createStatement();
			String query = "SELECT C.content_name, C.content_type, " +
							" group_concat(CC.concept_name , ',', cast(CC.weight as char) , ',' , cast(CC.direction as char) order by CC.weight separator ';') as concepts " + 
							" FROM rel_content_concept CC, rel_topic_content TC, rel_topic_course TCO, ent_content C " +  
							" WHERE TCO.course_id="+course_id+" and TCO.topic_id=TC.topic_id and TC.content_id=CC.content_id and CC.active=1 "+
							" and C.content_id=CC.content_id " + 
							" group by TC.content_id order by TCO.order;";
			rs = stmt.executeQuery(query);
	
			String content_name = "";
			ArrayList<String[]> c_c = null;
			while(rs.next()){
				content_name = rs.getString("content_name");
				
				//System.out.println(topic);
				String allconcepts = rs.getString("concepts");
				if (allconcepts == null || allconcepts.equalsIgnoreCase("[null]") || allconcepts.length() == 0){
					c_c = null;
				}else{
					c_c = new ArrayList<String[]>();
					String[] concepts = allconcepts.split(";");
					for(int i=0;i<concepts.length;i++){
						String[] concept = concepts[i].split(",");
						c_c.add(concept); // concept_name, weight, direction
						//System.out.println("  "+concept[0]+" "+concept[1]);
					}
				}
				res.put(content_name, c_c);
				//System.out.println();
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}
	}
	
	
	public void insertPrecomputedModel(String user, String course_id, String group_id, String sid, String model4topics, String model4content){
		try{
			stmt = conn.createStatement();
			String query = "INSERT INTO ent_precomputed_models (user,course_id,group_id,session_id,computedon,model4topics,model4content) values ('"+ user + "',"+ course_id + ",'"+ group_id + "','"+ sid + "',now(),'"+ model4topics + "','"+ model4content + "');";
			//System.out.println(query);
			if(stmt.execute(query)){
				//;	
			}
			this.releaseStatement(stmt,rs);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
		}finally{
			this.releaseStatement(stmt,rs);
		}
		
	}
	public void updatePrecomputedModel(String user, String course_id, String group_id, String sid, String model4topics, String model4content){
		try{
			stmt = conn.createStatement();
			String query = "UPDATE ent_precomputed_models SET model4topics='"+model4topics+"', model4content='"+model4content+"', computedon=now() WHERE user = '" + user + "' and course_id='" + course_id + "' and group_id = '" + group_id + "' and session_id = '" + sid + "';";
			//System.out.println(query);
			if(stmt.execute(query)){
				//;	
			}
			this.releaseStatement(stmt,rs);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
		}finally{
			this.releaseStatement(stmt,rs);
		}
		
	}
	public boolean existPrecomputedModelForSession(String user, String course_id, String group_id, String sid){
		int n = 0;
		try{
			stmt = conn.createStatement();
			String query = "SELECT count(*) as npm " + 
							"FROM ent_precomputed_models  " + 
							"WHERE user='"+user+"' and group_id='"+group_id+"' and course_id='"+course_id+"' and session_id='"+sid+"';";
			//System.out.println(query);
			rs = stmt.executeQuery(query);	
			while(rs.next()){
				n = rs.getInt("npm");
			}
			this.releaseStatement(stmt,rs);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
		}finally{
			this.releaseStatement(stmt,rs);
		}
		return n>0;
	}
	
	public HashMap<String,String[]> getPrecomputedModels(String course_id){
		try{
			HashMap<String,String[]> res = new HashMap<String,String[]>();
			stmt = conn.createStatement();
			String query =  "SELECT user,model4topics,model4content FROM ent_precomputed_models WHERE id in " +
							"(select max(id) from ent_precomputed_models where course_id='"+course_id+"' group by user);";
			rs = stmt.executeQuery(query);	
			//System.out.println(query);
			String user = "";
			String[] models;
			while(rs.next()){
				user = rs.getString("user");
				models = new String[2];
				models[0] = rs.getString("model4topics");
				models[1] = rs.getString("model4content");
				res.put(user, models);
			}
			this.releaseStatement(stmt,rs);
			return res;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}finally{
			this.releaseStatement(stmt,rs);
		}
	}

	// returns the list of recommended algorithm ids
	public String[] getRecMethods(){	
		try{
			ArrayList<String> res = new ArrayList<String>();
			stmt = conn.createStatement();
			String query = "SELECT id " +
						   " FROM ent_recommendation_approach " +  
						   " WHERE active = 1; ";
		
			rs = stmt.executeQuery(query);
			while(rs.next()){
				res.add(rs.getString("id"));
				//System.out.println(rs.getString("id"));
			}
			this.releaseStatement(stmt,rs);
			String[] res2 = new String[res.size()];
			for(int i=0;i<res.size();i++) res2[i] = res.get(i);
			return res2;
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return null;
		}
	}
	
	
	public boolean insertUsrFeedback(String usr, String grp, String sid, String srcActivityId, String srcActivityRes, String fbId, String fbItemIds, String responses, String recId) {
		String query = "";
		String[] fbItemArray = { "" };
		String[] resArray = { "" };
		//System.out.println(responses);
		try {
			stmt = conn.createStatement();
			if (fbItemIds != null && fbItemIds.length() != 0) {
				fbItemArray = fbItemIds.split("\\|");
				resArray = responses.split("\\|");
				if (fbItemArray.length != resArray.length) {
					//
				}
			}
			for (int i = 0; i < fbItemArray.length; i++) {
				query = "INSERT INTO ent_user_feedback (user_id,session_id,group_id,src_content_name, src_content_res, fb_id, fb_item_id, fb_response_value, item_rec_id, datentime) values ('"
						+ usr
						+ "','"
						+ sid
						+ "','"
						+ grp
						+ "','"
						+ srcActivityId
						+ "','"
						+ srcActivityRes
						+ "','"
						+ fbId
						+ "','"
						+ fbItemArray[i]
								+ "','"
								+ resArray[i] + "','" + recId + "'," + "now());";
				//System.out.println(query);
				stmt.executeUpdate(query);
			}
			
			//System.out.println(System.nanoTime()/1000);
			this.releaseStatement(stmt, rs);
			return true;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			System.out.println(query);
			releaseStatement(stmt, rs);
			return false;
		}
	}

	public boolean insertTrackAction(String usr, String grp, String sid, String action, String comment) {
		String query = "";
		try {
			stmt = conn.createStatement();
			query = "INSERT INTO ent_tracking (datentime, user_id, session_id, group_id, action, comment) values ("
					+ "now(), '"+ usr + "','" + sid + "','" + grp + "','" + action + "','" + comment + "');";

			stmt.executeUpdate(query);
			//System.out.println(query);
			this.releaseStatement(stmt, rs);
			//System.out.println(query);
			return true;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			releaseStatement(stmt, rs);
			return false;
		}
	}

	
	
	public boolean isShowingRecommedation(String userid, String topic_name, String content_name, int position){
		// see if there is a record in STUDY_show_rec for user and the topic and retrieve show_odd
		int n = 0;
		int show_odd = -1;
		try{
			stmt = conn.createStatement();
			String query = "SELECT show_odd " + 
							"FROM study_show_rec  " + 
							"WHERE user_id='"+userid+"' and topic_name='"+topic_name+"';";
			//System.out.println(query);
			rs = stmt.executeQuery(query);	
			while(rs.next()){
				show_odd = rs.getInt("show_odd");
			}
			//System.out.println(query+"\nshow_odd "+show_odd);
			// if no record, random choose 0 or 1 for show_odd and insert in STUDY_show_rec
			if (show_odd == -1){
				show_odd = (int) Math.round(Math.random());
				query = "INSERT INTO study_show_rec (user_id,topic_name,show_odd) values " + 
						"('"+userid+"','"+topic_name+"',"+show_odd+");";
				//System.out.println(query+"\nshow_odd "+show_odd);
				stmt.executeUpdate(query);
				
				
			}
			//System.out.println("position : "+position);
			this.releaseStatement(stmt,rs);
			return (position % 2 == show_odd);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
			return false;
		}
		
	}
	
	// get the recommendation algorithm name for the last recommendation made to the usr for the specific content_name
	// and no older than 1 hour
	public String recentRecMethod(String usr, String content_name){
		String res = "";
		try{
			stmt = conn.createStatement();
			String query = "SELECT distinct(rec_approach) as recmethod " + 
							"FROM ent_recommendation  " + 
							"WHERE src_content_name='"+content_name+"' and user_id='"+usr+"' "+ 
									" and shown=1 and TIMESTAMPDIFF(SECOND,datentime,now())<3601;";
			//System.out.println(query);
			rs = stmt.executeQuery(query);	
			while(rs.next()){
				res = rs.getString("recmethod");
			}
			this.releaseStatement(stmt,rs);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt,rs);
		}finally{
			this.releaseStatement(stmt,rs);
		}
		return res;
	}
	
	// this method retrieves all recommendations made for the user in the content last_content_id
	// or create the recommendations if the user did not received recommendations
	// TODO @@@
	public ArrayList<ArrayList<String>> getAllRecommended(String user_id, String content_name, String course_id, String[] methods, int n){
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		ArrayList<String> ex;
		String id = "";
		try{
			stmt = conn.createStatement();
			String query = "SELECT if(max(seq_rec_id) is null,-1,max(seq_rec_id)) as seq_rec_id FROM ent_recommendation " + 
							"WHERE src_content_name='"+content_name+"' and user_id='"+user_id+"';";
			
			rs = stmt.executeQuery(query);	
			while(rs.next()){
				id = rs.getString("seq_rec_id");
			}
			if (id.equalsIgnoreCase("-1")){
				// there is no recommendation already generated
				this.releaseStatement(stmt,rs);
				return null;
				
			}else{
				res = new ArrayList<ArrayList<String>>();
				query = "";
				boolean first = true;
				for(String method: methods){
					if (!first) query += "\n UNION \n";
					query += "(SELECT item_rec_id, T.topic_name, rec_content_name , "+
							 "  \nCOALESCE((select fb_response_value from ent_user_feedback where user_id='"+user_id+"' and "+ 
							 "  item_rec_id in (select R2.item_rec_id from ent_recommendation R2 where R2.user_id='"+user_id+"' and R2.src_content_name='"+content_name+"' and R2.rec_content_name=R.rec_content_name) "+
							 "  and fb_response_value is not null order by item_rec_id desc limit 1),-1) as stored_feedback "+
							 "\n FROM ent_recommendation R, rel_topic_content TA, ent_topic T, ent_content C, rel_topic_course TC"+
							 " WHERE seq_rec_id="+id+" and rec_approach='"+method+"' "+
							 " and C.content_name = rec_content_name and C.content_id = TA.content_id and T.topic_id = TA.topic_id and TC.topic_id=T.topic_id and TC.course_id="+course_id+" "+
							 " order by rec_score desc limit "+n+") ";
					
					first= false;
					
				
				}
				query += " \n order by rand();";
				//System.out.println(query);
				rs = stmt.executeQuery(query);	
				while(rs.next()){
					ex = new ArrayList<String>();
					ex.add(rs.getString("item_rec_id")); 
					ex.add(rs.getString("topic_name"));
					ex.add("ex");
					ex.add(rs.getString("rec_content_name"));
					ex.add("0");
					ex.add("The above example is helpful for me to solve the exercise.");
					ex.add(rs.getString("stored_feedback"));
					res.add(ex);
				}
				this.releaseStatement(stmt,rs);
			}
			
			
		}catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage()); 
			System.out.println("SQLState: " + e.getSQLState()); 
			System.out.println("VendorError: " + e.getErrorCode());
			this.releaseStatement(stmt,rs);
		}
		
		
		return res;
	}
	
	
	/*
	 * @author : Roya Hosseini 
	 * This method generates the recommendations for the given user at the given time.
	 * Parameters:
	 * - seq_rec_id: is the the id for all recommendations generated by different methods for the given user at given time
	 * - user_id: the id of the user
	 * - group_id: the group id for the user's group
	 * - course_id: is the id of the course
	 * - session_id: is the id of the user's session
	 * - last_content_id: is the rdfid of the the activity (question) that the user has failed
	 * - last_content_res: is the result of the activity (question) with the rdfid equal to last_content_id 
	 * - n: is the number of recommendation generated by each method
	 * - topic_content: is the map containing the keys as topic_names and values as the rdfid of topic activities (questions,examples,readings). @see:guanjieDBInterface.getTopicContent(String)
	 * - examples_activity: is the maps containing the keys as examples and values as the user actions in example. @see um2DBInterface.getUserExamplesActivity(String)
	 * - questions_activity: is the map with the keys as activities and values as the number of success and attempts in the activity. @see: um2DBInterface.getUserQuestionsActivity(String)
	 * - forced :  0 when recommendations were generated in a real condition(the user failed a question), and 1 when the recommendations were generated for rating and the user never failed the question 
	 * Returns:
	 * - List of recommended example. Each element is a list with the following items:
	 * 1) item_rec_id from the ent_recommendation table
	 * 2) topic name  
	 * 3) content type ("ex"  is used for examples)
	 * 4) example rdfid 
	 * 5) similarity value
	 * 6) feedback question	 
	 */
	public ArrayList<ArrayList<String>> generateRecommendations(
			String seq_id, String user_id, String group_id, String course_id, 
			String session_id, String last_content_id, String last_content_res, int n, 
			String[] methods, int method_selected,
			HashMap<String,ArrayList<String>[]> topic_content,
			HashMap<String,String[]> examples_activity,
			HashMap<String,String[]> questions_activity,
			int forced){
		ArrayList<ArrayList<String>> recommendation_list = new ArrayList<ArrayList<String>>();
		try {
			String example_content_name = "";
			float sim = 0.0f;
			stmt = conn.createStatement();
			String query = "";
			String cid = "";
			String display_name;
			query = " SELECT course_id FROM guanjie.ent_group where group_id = '"+ group_id+"'";
			rs = stmt.executeQuery(query);
			while (rs.next())
			{
				cid = rs.getString(1);
			}
			this.releaseStatement(stmt, rs); //release the statement			
			for (String method : methods)
			{
				stmt = conn.createStatement(); //create a statement
				Map<String,Float> exampleMap = new HashMap<String,Float>();			
				Map<String,String> exampleDisplayNameMap = new HashMap<String,String>();
				List<String> recList = new ArrayList<String>();
				if ( method.equals("CONCSIM") | method.equals("CSSIM"))
				{
					query = " SELECT rs.example_content_name,rs.sim, c.display_name " +
							" FROM guanjie.rel_con_con_sim rs, ent_content c " +
							" WHERE rs.method = '"+method+"' and c.content_name = rs.example_content_name" +
							" and rs.question_content_name = '"+ last_content_id + "' " +
							" and " +
							" (SELECT count(RTC.topic_id) FROM rel_topic_course RTC, rel_topic_content TC, ent_content CO " +
							"  WHERE RTC.course_id = '"+cid+"' and RTC.topic_id=TC.topic_id " +
							"  and CO.content_name=rs.example_content_name and TC.content_id=CO.content_id and RTC.active=1) > 0  " +
							"  and c.visible = 1 order by rs.sim Desc limit "+n+";";	
					rs = stmt.executeQuery(query);
					while (rs.next()) {
						example_content_name = rs.getString(1);
						sim = (float) rs.getDouble(2);
						display_name = rs.getString(3);	
						exampleMap.put(example_content_name, sim);
						exampleDisplayNameMap.put(example_content_name, display_name);
						recList.add(example_content_name +"@"+sim);
					}					
				}
				//personalized approaches
				else if (method.equals("PCSSIM") | method.equals("PCONCSIM"))
				{
					String approach = "CSSIM";
					if (method.equals("PCONCSIM"))
						approach = "CONCSIM";
					query = " SELECT rs.example_content_name,rs.sim, c.display_name " +
							" FROM guanjie.rel_con_con_sim rs, ent_content c " +
							" WHERE rs.method = '"+approach+"' and c.content_name = rs.example_content_name" +
							" and rs.question_content_name = '"+ last_content_id + "' " +
							" and (SELECT count(RTC.topic_id) " +
							" FROM rel_topic_course RTC, rel_topic_content TC, ent_content CO " +
							" WHERE RTC.course_id = '"+cid+"' and RTC.topic_id=TC.topic_id " +
							" and CO.content_name=rs.example_content_name and TC.content_id=CO.content_id and RTC.active=1) > 0  " +
							" and c.visible = 1 order by rs.sim Desc limit 10;";//select top 10 examples using the CSSIM method //TODO one change 15-->10
					rs = stmt.executeQuery(query);
					
					while (rs.next()) {
						example_content_name = rs.getString(1);
						sim = (float) rs.getDouble(2);
						display_name = rs.getString(3);
						exampleMap.put(example_content_name, sim);
						exampleDisplayNameMap.put(example_content_name, display_name);
					}					
					ArrayList<String> questionTopic = getTopic(last_content_id,"question",topic_content);
					recList = getPersonalizedRecommendation(method,questionTopic,topic_content,exampleMap, questions_activity, n);			
				}			
				else 
					continue;
				
				int shown = 0;
				if (forced == 1)
					shown = -1; 
				else if (method_selected == -1)
					shown = 0;
				else if (method.equalsIgnoreCase(methods[method_selected]))
					shown = 1;
				createRecItem(seq_id, user_id, group_id,
							session_id, last_content_id, topic_content,
							recommendation_list, exampleDisplayNameMap,
							recList, method, shown);
				this.releaseStatement(stmt, rs); //release the statement
			}
		} catch (SQLException e) {
			this.releaseStatement(stmt, rs);
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			return null;
		} finally {
			this.releaseStatement(stmt, rs);			
		}		
		return recommendation_list;
	}

	
	private void createRecItem(String seq_rec_id, String user_id,
			String group_id, String session_id, String last_content_id,
			HashMap<String, ArrayList<String>[]> topic_content,
			ArrayList<ArrayList<String>> recommendation_list,
			Map<String, String> exampleDisplayNameMap, List<String> recList,
			String method, int shown) {
		String example_content_name;
		float sim;
		int id;
		String display_name;
		for (String rec : recList)
		{
			String[] item = rec.split("@");
			example_content_name = item[0];
			display_name = exampleDisplayNameMap.get(example_content_name);
			sim = Float.parseFloat(item[1]);
			id = storeRecommendation(seq_rec_id, user_id, group_id, session_id,
					last_content_id, example_content_name, method, sim, shown);
			//TODO double check
			if (shown == 1 | shown == -1)
			{
				ArrayList<String> rec_item = createRecommendationItem(id, topic_content, example_content_name, display_name,sim);
			    recommendation_list.add(rec_item);				
			}				
		}
	}
	
	public ArrayList<String> createRecommendationItem(int id,
			HashMap<String,ArrayList<String>[]> topic_content,String example_content_name, 
			String display_name, float sim) {
		
		ArrayList<String> recommendation = new ArrayList<String>();
		List<String> topicList = new ArrayList<String>(); 
		ArrayList<String>[] contents;
		for (Entry<String, ArrayList<String>[]> e : topic_content.entrySet())
		{
			contents = e.getValue();
			for (String c : contents[1]) //contents[1] has the examples
			{
				if (c.equals(example_content_name))
					topicList.add(e.getKey());							
			}							
		}							
		recommendation.add("" + id); // item_rec_id from the ent_recommendation table
		recommendation.add(topicList.get(0)); // topic name  //We currently have one topic for each content
		recommendation.add("ex"); // content type ("ex"  is used for examples)
		recommendation.add(example_content_name); // example rdfid 
		recommendation.add(df4.format(sim)); // similarity value
		
		recommendation.add("The above example ("+display_name+") is helpful for me to solve the exercise."); // question for collecting feedback
		recommendation.add("-1"); // the feedback stored @@@@ JULIO

		return recommendation;
	}
	
	public int storeRecommendation(String seq_rec_id,
			String user_id, String group_id, String session_id,
			String last_content_id, String example_content_name, String method,
			float sim, int shown) {
		int id = -1;
		Statement tempStmt = null;
		ResultSet rskeys = null;
		try{
			String query = "INSERT INTO ent_recommendation (seq_rec_id,user_id,group_id,session_id,src_content_name,rec_content_name,rec_approach,rec_score,datentime,shown) values ('"
					+ seq_rec_id + "','" + user_id + "','" + group_id + "','" + session_id 
					+ "','" + last_content_id + "','" + example_content_name + "','" + method + "'," + sim + ","
					+"now(), "+ shown +");";
			tempStmt = conn.createStatement();
			tempStmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			//TODO
			rskeys = tempStmt.getGeneratedKeys();
			if (rskeys.next()){
			    id=rskeys.getInt(1);
			}	
			
			this.releaseStatement(tempStmt,rskeys);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(tempStmt,rskeys);
		}finally{
			this.releaseStatement(tempStmt,rskeys);
		}
		return id;
	}
	
	/* @author: Roya Hosseini
	 * This method re-ranks the top examples selected by the CSS recommendation method using user model
	 * Parameters:
	 * - exampleMap: the map with the key as the top selected examples and the values as their corresponding similarity
	 * - questions_activity: @see guanjieDBInterface.generateRecommendations javaDoc
	 * - limit: @see guanjieDBInterface.generateRecommendations javaDoc
	 * Returns:
	 * - a descendingly ordered list of examples in terms of similarity - each element is the pair of example and its similarity value separated by '@'
	 */
	public List<String> getPersonalizedRecommendation(String method, ArrayList<String> questionTopicList, HashMap<String, ArrayList<String>[]> topic_content, Map<String, Float> exampleMap,
			   								  HashMap<String,String[]> questions_activity, int limit)
	{
		List<String> recommendationList = new ArrayList<String>();
		Map<String, Double> rankMap = new HashMap<String,Double>();
	    ValueComparator vc =  new ValueComparator(rankMap);
		TreeMap<String,Double> sortedRankMap = new TreeMap<String,Double>(vc);
		List<String> conceptList = new ArrayList<String>();
		
		int k = 0;
		int n = 0;
		int s = 0;
		for (String e : exampleMap.keySet())
		{
			k = 0; //number of known concepts in example
			n = 0;//number of new concepts in example
			s = 0;//number of shady concepts in example	
			conceptList = getExampleConcept(e);
			for (String c : conceptList) {
				double nsuccess = 0;
				double totalAtt = 0;
				boolean hasAttempt = false;
				List<String> activityList = getActivitiesWithConcept(c);

				for (String a : activityList) {
					if (questions_activity.containsKey(a)) {
						String[] x = questions_activity.get(a);
						totalAtt += Double.parseDouble(x[1]); // x[1] = nattempt
						nsuccess += Double.parseDouble(x[2]); // x[2] = nsuccess
						hasAttempt = true;
					}
				}
				if (hasAttempt == false)
				{
					ArrayList<String> exampleTopicList = getTopic(e,"example",topic_content);
					boolean isSameTopic = false;
					for (String qt : questionTopicList)
					{
						if (exampleTopicList.contains(qt))
							isSameTopic = true;
					}
					if (isSameTopic == false)
					{
					  n++;	
					}
				}
				else {
					if (nsuccess > (totalAtt/2))
						k++;
					else
						s++;
				}
			}
			double rank = 0.0;
			if (conceptList.size() == 0)
				rank = 0.0;
			else if (k==0 & (s+n)> 3.0)
			{
				rank = -(s+n);
			}
			else
				rank = (3 - (s+n)) * Math.pow((double) k / (double) conceptList.size(), (3 - (s+n)));
			double alpha = 0.0;
			if (method.equals("PCSSIM"))
				alpha = 0.5;
			else if (method.equals("PCONCSIM"))
				alpha = 0.5;
			double combinedMeasure = (1-alpha)*rank+(alpha*exampleMap.get(e));
			rankMap.put(e, combinedMeasure);
		}
		sortedRankMap.putAll(rankMap);
		recommendationList = putFirstEntries(limit, sortedRankMap);	
		return recommendationList;
	}
	
	
    private ArrayList<String> getTopic(String content_name,String content_type, HashMap<String, ArrayList<String>[]> topic_content) {
    	ArrayList<String> topicList = new ArrayList<String>();
    	ArrayList<String>[] contents;
    	for (Entry<String, ArrayList<String>[]> con : topic_content.entrySet())
		{
			contents = con.getValue();
			ArrayList<String> list = new ArrayList<String>();
			if (content_type.equals("question"))
				list = contents[0];//contents[0] has the questions
			else if (content_type.equals("example"))
				list = contents[1];//contents[1] has the examples
			if (list.contains(content_name))
				topicList.add(con.getKey());													
		}	
		return topicList;
	}

	private List<String> getActivitiesWithConcept(String c) {
		List<String> activities = new ArrayList<String>();
		Statement tmpstmt = null;
		ResultSet tmprs = null;
		try{
			tmpstmt = conn.createStatement();
			String query = "SELECT distinct rdfid from ent_jquiz_concept where concept = '"+c+"';";
			tmprs = tmpstmt.executeQuery(query);	
			while(tmprs.next()){
				activities.add(tmprs.getString(1));
			}
			this.releaseStatement(tmpstmt,tmprs);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(tmpstmt,tmprs);
		}finally{
			this.releaseStatement(tmpstmt,tmprs);
		}		
		return activities;
	}

	private List<String> getExampleConcept(String e) {
		List<String> concepts = new ArrayList<String>();
		Statement tmpstmt = null;
		ResultSet tmprs = null;
		try{
			tmpstmt = conn.createStatement();
			String query = "SELECT distinct concept from ent_jexample_concept where rdfid = '"+e+"';";
			tmprs = tmpstmt.executeQuery(query);	
			while(tmprs.next()){
				concepts.add(tmprs.getString(1));
			}
			this.releaseStatement(tmpstmt,tmprs);
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(tmpstmt,tmprs);
		}finally{
			this.releaseStatement(tmpstmt,tmprs);
		}		
		return concepts;
	}
	
	public static  List<String> putFirstEntries(int limit, SortedMap<String,Double> source) {
		  int count = 0;
		  List<String> list = new ArrayList<String>();
		  for (Map.Entry<String,Double> entry:source.entrySet())
		  {
		     if (count >= limit)
		    	 break;
		     list.add(entry.getKey()+"@"+entry.getValue());
		     count++;
		  }
		  return list;
		}

	static class ValueComparator implements Comparator<String> {

	    Map<String, Double> base;
	    public ValueComparator(Map<String, Double> base) {
	        this.base = base;
	    }

	    // Note: this comparator sorts the values descendingly, so that the best activity is in the first element.
	    public int compare(String a, String b) {
	    	if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // 
	    } // returning 0 would merge keys	   
	}
}
