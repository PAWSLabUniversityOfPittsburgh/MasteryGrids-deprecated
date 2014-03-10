import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.net.URL;

import javax.servlet.http.HttpServlet;


public class Aggregate {
	private boolean contrainOutcomeLevel = true; // knowledge levels will be computed only on outcome concepts
	
	private String usr;
	private String grp;
	private String sid;
	private String domain;
	public HttpServlet servlet;
	private String usr_name;
	private String usr_email;
	private String grp_name;
	private String course_id;
	public String[] rec_methods;
	
	public ArrayList<String[]> topic_list;
	public HashMap<String,String[]> content_list;
	
	public ArrayList<String[]> class_list;
	public HashMap<String,String> non_students;
	
	public HashMap<String,ArrayList<String[]>> content_concepts;

	public HashMap<String,ArrayList<String>[]> topic_content; 
	
	public HashMap<String,String[]> examples_activity;
	public HashMap<String,String[]> questions_activity;
	public HashMap<String,Double> content_sequencing_score;
	public HashMap<String,double[]> topic_sequencing_score;
	
	public Map<String,Double> user_concept_knowledge_levels;
	public Map<String,double[]> userTopicLevels;
	public Map<String,double[]> userContentLevels; 
	
	public Map<String,double[]> aggs1_topic_levels; 
	public Map<String,double[]> aggs2_topic_levels;
	public Map<String,double[]> aggs1_content_levels; 
	public Map<String,double[]> aggs2_content_levels;
	public ArrayList<String> top_students_ids;
	
	public Map<String,Map<String,double[]>> peers_topic_levels;
	public Map<String,Map<String,double[]>> peers_content_levels;
	
	// recommendation set
	public ArrayList<ArrayList<String>> recommendation_list;
	// feedback set
	public ArrayList<ArrayList<String>> activity_feedback_form_items; // @@@@ do it later
	public String activity_feedback_id;
	
	//public um2DBInterface um2_db;
	public UMInterface um_interface;
	public AggregateDB agg_db;
	
	public static DecimalFormat df = new DecimalFormat("#.##");
	
	
	public Aggregate(String usr, String grp, String cid, String sid, HttpServlet servlet){
		this.usr = usr;
		this.grp = grp;
		this.sid = sid;
		this.servlet = servlet;
		
		um_interface = new PAWSUMInterface();
		
		openDBConnections();
		
		grp_name = agg_db.getGrpName(grp);
		
		if (cid == null || cid.length()==0) course_id = agg_db.getCourseId(grp);
		else {
			domain = agg_db.getDomain(cid);
			if (domain.length()>0) course_id = cid;
			else course_id = agg_db.getCourseId(grp);
		}
		domain = agg_db.getDomain(course_id);
		
		String[] userdata = um_interface.getUserInfo(usr);
		usr_name = userdata[0];
		usr_email = userdata[1];
		
		//System.out.println("CID: "+course_id+"\nGROUP: "+grp_name+"\nDomain: "+domain+"\nUser: " + usr_name);
		
		class_list = um_interface.getClassList(grp);
		//System.out.println("Class list got!");
		non_students = agg_db.getNonStudents(grp); // special non students (instructor, researcher)
		//System.out.println("Non-students got");
		topic_list = agg_db.getTopicList(course_id);
		ArrayList<String> hidden_topics = agg_db.getHiddenTopics(grp);
		
		hideTopics(hidden_topics); // set the visibility attribute (topic[3])
		
		
		content_list = agg_db.getContent(course_id);	
		//System.out.println("content got");
		content_concepts = agg_db.getContentConcepts(course_id);
		//System.out.println("concepts got");
		
		//topic_examples = agg_db.getTopicExamples(grp); // @@@@
		//System.out.println("topic examples got");
		topic_content = agg_db.getTopicContent(course_id);
		//System.out.println("topic content");
		
		rec_methods = agg_db.getRecMethods();
		
		computeUserLevels(usr, domain);
		//System.out.println("user model computed");
		storePrecomputedModel(usr);
		//System.out.println("user model stored");
		
		closeDBConnections();
	}
	
	// @@@@ REVIEW!!!!
	public Aggregate(String grp, HttpServlet servlet){
		this.usr = null;
		this.grp = grp;
		this.sid = null;
		this.servlet = servlet;
		um_interface = new PAWSUMInterface();
		openDBConnections();
		
		grp_name = agg_db.getGrpName(grp);
		course_id = agg_db.getCourseId(grp);
		usr_name = null;
		usr_email = null;
		
		class_list = um_interface.getClassList(grp);

		topic_list = agg_db.getTopicList(course_id);
		
		content_list = agg_db.getContent(course_id);	
		content_concepts = agg_db.getContentConcepts(course_id);

		//topic_examples = agg_db.getTopicExamples(grp); // @@@@
		topic_content = agg_db.getTopicContent(course_id);
		
		closeDBConnections();
	}
	
	public void openDBConnections(){
		ConfigManager cm = new ConfigManager(servlet); // this object gets the database connections values
		//um2_db = new um2DBInterface(cm.um2_dbstring,cm.um2_dbuser,cm.um2_dbpass);
		agg_db = new AggregateDB(cm.agg_dbstring,cm.agg_dbuser,cm.agg_dbpass);
		//um2_db.openConnection();
		agg_db.openConnection();
	}
	
	public void closeDBConnections(){
		//um2_db.closeConnection();
		agg_db.closeConnection();
	}

	
	public void hideTopics(ArrayList<String> hidden_topics){
		if (hidden_topics != null && hidden_topics.size()>0){
			for (String hidden : hidden_topics){
				for (String[] topic : topic_list) {
					if (topic[0].equalsIgnoreCase(hidden)) topic[3] = "0";
				}
			}
			
		}
	}
	
	// computes user levels aggregating by topics and content 
	// topic -> [0]: question K; [1]: question P; [2]: example  K; [3]: example  P; [4]: reading  K; [5]: reading  P
	// param usr is local allowing to compute levels for different users
	public void computeUserLevels(String usr, String domain){
		if (usr == null || grp == null || usr.length()==0 || grp.length()==0) return;
		userTopicLevels = new HashMap<String,double[]>();
		userContentLevels = new HashMap<String,double[]>();
		
		// 1. GET THE LEVELS OF KNOWLEDGE OF THE USER IN CONCEPTS 
		//    FROM USER MODEL USING THE USER MODEL INTERFACE
		user_concept_knowledge_levels = um_interface.getConceptLevels(usr, domain, grp); // Concept Knowledge levels
		
		// 2. GET PROGRESS ON EXAMPLES ADN QUESTIONS FROM USER MODEL
		examples_activity = um_interface.getUserExamplesActivity(usr,domain);		
		questions_activity =  um_interface.getUserQuestionsActivity(usr, domain);
		
		//
		// 3. COMPUTE AGGREGATE LEVELS FOR CONTENT
		// initialize the resulting data structure 
		for (Map.Entry<String, String[]> content : content_list.entrySet()) {
			double[] kpvalues = new double[2];
			
			// compute Knowledge levels for content from knowledge levels of the concepts
			String content_name = content.getKey();
			String[] content_data = content.getValue();
			String content_type = content_data[0];

			// the array of concepts for the content item (content item can be a question, example or reading)
			ArrayList<String[]> c_concepts = content_concepts.get(content_name);
			double sum_weights = 0.0;
			double user_concept_k = 0.0;
			double user_content_k = 0.0;
			//System.out.println(topic[0]); //
			if (c_concepts != null && user_concept_knowledge_levels != null){
				for(int j=0;j<c_concepts.size();j++){
					String[] _concept = c_concepts.get(j);
					//if (_concept.length<2) System.out.println(_concept[0]+" : "+_concept.length);

					String direction = _concept[2]; // outcome or prerequisite
					// only compute levels in outcome concepts (how much already know that is new in the content?)
					if (direction.equalsIgnoreCase("outcome") || !contrainOutcomeLevel){
						String concept = _concept[0];
						double weight = Double.parseDouble(_concept[1]);
						if(user_concept_knowledge_levels.get(concept)==null) user_concept_k = -1.0;
						else {
							user_concept_k = user_concept_knowledge_levels.get(concept);
							sum_weights += weight;
							user_content_k += user_concept_k*weight;
						}
						
					}				
				}
				
			}
			if (sum_weights == 0.0) user_content_k = 0.0;
			else user_content_k = user_content_k/sum_weights;
			
			kpvalues[0] = user_content_k;
			
			double user_content_p = 0.0;
			if (content_type.equalsIgnoreCase("example")){
				//System.out.println("K for example "+content_name+" : "+kpvalues[0]);
				if (examples_activity ==  null || examples_activity.get(content_name) == null){
					user_content_p = 0.0;
				}else{
					String[] example_activity = examples_activity.get(content_name);
					
					double distinct_actions = Double.parseDouble(example_activity[2]);
					double total_lines = Double.parseDouble(example_activity[3]);
					if(total_lines == 0) total_lines = 1.0;
					user_content_p = distinct_actions / total_lines;
				}
				
			}
			// Progress level related with Questions
			if (content_type.equalsIgnoreCase("question")) {
				if (questions_activity == null || questions_activity.get(content_name) == null) {
					user_content_p = 0.0;
				}
				else {

					String[] question_activity = questions_activity.get(content_name);
					double nattemtps = Double.parseDouble(question_activity[1]);
					double nsuccess = Double.parseDouble(question_activity[2]);
					if (nsuccess > 0)
					user_content_p = 1.0;
				}
			}
			
			kpvalues[1] = user_content_p;
			
			userContentLevels.put(content_name, kpvalues);
		}

		// 4. COMPUTE AGGREGATE LEVELS FOR TOPICS
		// initialize the resulting data structure 
		
		for (String[] topic : topic_list) {
			
			double[] kpvalues = new double[6];

            // Topic knowledge and progress levels in concepts related with questions
            // using userContentLevels and topic_content
            double user_topic_oneType_k = 0.0;
            double user_topic_oneType_p = 0.0;
            int i = 0;
            int contentsSize = 0;
            // oneTypeContents.size() = 3, for question, example and reading
            ArrayList<String>[] oneTypeContents = topic_content.get(topic[0]);
            if (oneTypeContents != null)
            for (ArrayList<String> contents : oneTypeContents) {
            	user_topic_oneType_k = 0.0;
                user_topic_oneType_p = 0.0;

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
                kpvalues[i] = user_topic_oneType_k;
                kpvalues[i + 1] = user_topic_oneType_p;
                i += 2;
                if (i >= 6)
                	break;
            }
            userTopicLevels.put(topic[0], kpvalues);
		}
		// compute sequencing 
		sequenceContent();
	}

	
	public void sequenceContent(){
		SequenceContent SQ = new SequenceContent(content_concepts, user_concept_knowledge_levels,
					examples_activity, questions_activity, topic_content);
		content_sequencing_score = SQ.getContentSequencing();
		topic_sequencing_score = SQ.getTopicSequencing();
		
	}
	
	// Get the precomputed models at level of content and topics for each student in the list 
	public void getClassLevels(){
		if (class_list == null || class_list.size()==0){ return;}
		peers_topic_levels = new HashMap<String,Map<String,double[]>>();
		peers_content_levels = new HashMap<String,Map<String,double[]>>();
		openDBConnections();
		HashMap<String,String[]> precomp_models = agg_db.getPrecomputedModels(course_id);
		//for(String[] learner: class_list){
		//System.out.println("total students: "+class_list.size());	
		for(Iterator<String[]> i = class_list.iterator(); i.hasNext();) {
			String[] learner = i.next(); 
			
			String learnerid = learner[0]; // the user login (username)
			String[] models = precomp_models.get(learnerid);
			if (models != null){
				String model4topics = models[0];
				String model4content = models[1];
				if(model4topics != null && model4topics.length()>0){
					HashMap<String,double[]> learner_topic_levels = formatLevels(model4topics);
					peers_topic_levels.put(learnerid,learner_topic_levels);
				}
				if(model4content != null && model4content.length()>0){
					HashMap<String,double[]> learner_content_levels = formatLevels(model4content);
					peers_content_levels.put(learnerid,learner_content_levels);
				}
			}else{
				// take the non-activity users out
				i.remove();
			}
			
		}
		//System.out.println("students with activity: "+class_list.size());
		closeDBConnections();
	}
	
	// 
	public HashMap<String,double[]> nullTopicLevels(){
		HashMap<String,double[]> res = new HashMap<String,double[]>();
		for (String[] topic : topic_list) {
			String topic_id = topic[0];
			double[] levels = new double[6]; // @@@@
			res.put(topic_id, levels);
		}
		return res;
	}

	// take a string representing the levels in topics or contents (precomputed model) and
	// returns a hashmap with the levels per topic/content
	public HashMap<String,double[]> formatLevels(String model){
		HashMap<String,double[]> res = new HashMap<String,double[]>();
		String[] model_arr = model.split("\\|");
		//System.out.println("  formatting model: "+model);
		for(int i=0;i<model_arr.length;i++){
			//System.out.println(model_arr[i]);
			String[] parts = model_arr[i].split(":");
			String topic = parts[0];
			String[] str_levels = parts[1].split(",");
			double[] levels = new double[str_levels.length];
			for(int j=0;j<str_levels.length;j++){
				levels[j] = Double.parseDouble(str_levels[j]);
			}
			res.put(topic, levels);
		}
		return res;
	}
	
	// include {question K, question P, example K, example P, readings K, reading P}
	public void orderClassByScore(boolean[] include){
		String learner1;
		String learner2;
		Map<String,double[]> learner1_levels;
		Map<String,double[]> learner2_levels;
		double learner1_sum = 0.0;
		double learner2_sum = 0.0;
		for(int i=0;i<class_list.size()-1;i++){
			for(int j=0;j<class_list.size()-1;j++){
				learner1 = class_list.get(j)[0];
				learner2 = class_list.get(j+1)[0];
				learner1_levels = peers_topic_levels.get(learner1);
				learner2_levels = peers_topic_levels.get(learner2);
				learner1_sum = 0.0;
				learner2_sum = 0.0;
				// average across all topics. if no topic levels for the students, returns 6 zeros
				double[] avgs1 = averageTopicLevels(learner1_levels);
				double[] avgs2 = averageTopicLevels(learner2_levels);
				
				for(int k=0;k<include.length;k++){
					if(include[k]){
						learner1_sum += avgs1[k];
						learner2_sum += avgs2[k];
						
					}
				}
				// if learner 1 has lower average score tan learner 2, swap
				if (learner1_sum < learner2_sum){ 
					//String[] tmp = class_list.get(j);
					//class_list.remove(j);
					//class_list.add(tmp);
					Collections.swap(class_list, j, j+1);
				}
			}
		}
	}

	public static double[] averageTopicLevels(Map<String,double[]> topics){
		double[] res = new double[6];
		if (topics == null) return res;
		int i=0;
		for (double[] levels : topics.values()) {
			// @@@@ add support for computing when there are topics without one or more kind of resources
			// @@@@ example: a topic without examples
			res[0] += levels[0];
			res[1] += levels[1];
			res[2] += levels[2];
			res[3] += levels[3];
			res[4] += levels[4];
			res[5] += levels[5];
			i++;
		}
		if (i==0) i=1;
		res[0] = res[0]/(1.0*i);
		res[1] = res[1]/(1.0*i);
		res[2] = res[2]/(1.0*i);
		res[3] = res[3]/(1.0*i);
		res[4] = res[4]/(1.0*i);
		res[5] = res[5]/(1.0*i);
		return res;
	}
	
	// TODO
	// @@@@ what to do with students who never entered the system? include them in the average?
	public void computeAverageClassTopicLevels(){
		aggs1_topic_levels = new HashMap<String,double[]>(); 
		int n = class_list.size();
		int m = non_students.size();
		int div = n-m;
		if (div<=0) div=1;
		for (String[] topic : topic_list) {
			double[] avglevels = new double[6];
			for(String[] learner: class_list){
				if (non_students.get(learner[0]) == null){
					double[] levels = null;
					Map<String,double[]> learner_levels = peers_topic_levels.get(learner[0]);
					if(learner_levels != null) levels = learner_levels.get(topic[0]);
					if(levels == null || levels.length==0){
						avglevels[0] += 0.0;
						avglevels[1] += 0.0;
						avglevels[2] += 0.0;
						avglevels[3] += 0.0;
						avglevels[4] += 0.0;
						avglevels[5] += 0.0;
					}else{
						avglevels[0] += levels[0];
						avglevels[1] += levels[1];
						avglevels[2] += levels[2];
						avglevels[3] += levels[3];
						avglevels[4] += levels[4];
						avglevels[5] += levels[5];
					}
					
				}			
			}
			avglevels[0] = avglevels[0]/div;
			avglevels[1] = avglevels[1]/div;
			avglevels[2] = avglevels[2]/div;
			avglevels[3] = avglevels[3]/div;
			avglevels[4] = avglevels[4]/div;
			avglevels[5] = avglevels[5]/div;
			aggs1_topic_levels.put(topic[0], avglevels);
		}
	}
	
	public void computeAverageClassContentLevels(){
		aggs1_content_levels = new HashMap<String,double[]>(); 
		int n = class_list.size();
		int m = non_students.size();
		int div = n-m;
		if (div<=0) div=1;
		for (Map.Entry<String, String[]> content : content_list.entrySet()) {
			String content_name = content.getKey();
			double[] avglevels = new double[2];
			for(String[] learner: class_list){
				if (non_students.get(learner[0]) == null){
					double[] levels = null;
					Map<String,double[]> learner_levels = peers_content_levels.get(learner[0]);
					if(learner_levels != null) levels = learner_levels.get(content_name);
					if(levels == null || levels.length==0){
						avglevels[0] += 0.0;
						avglevels[1] += 0.0;
					}else{
						avglevels[0] += levels[0];
						avglevels[1] += levels[1];
					}
				}			
			}
			avglevels[0] = avglevels[0]/div;
			avglevels[1] = avglevels[1]/div;
			aggs1_content_levels.put(content_name, avglevels);
		}
	}

	public void computeAverageTopStudentsTopicLevels(int n){
		aggs2_topic_levels = new HashMap<String,double[]>(); 
		this.top_students_ids = new ArrayList<String>();
		int m = non_students.size();
		if ((class_list.size()-m) < n) n = (class_list.size()-m);
		if (n < 0) n=0;
		int i = 0;
		while(top_students_ids.size()<n && i<class_list.size()){
			String[] learner = class_list.get(i);
			if (non_students.get(learner[0]) == null){
				top_students_ids.add(learner[0]);
			}
			i++;
		}
					
		int div = n;
		if (div==0) div=1;
		for (String[] topic : topic_list) {
			double[] avglevels = new double[6];
			i = 0;
			while(i<top_students_ids.size()){
				Map<String,double[]> learner_levels = peers_topic_levels.get(top_students_ids.get(i));
				double[] levels = null;
				if(learner_levels != null) levels = learner_levels.get(topic[0]);
				if(levels == null || levels.length==0){
					avglevels[0] += 0.0;
					avglevels[1] += 0.0;
					avglevels[2] += 0.0;
					avglevels[3] += 0.0;
					avglevels[4] += 0.0;
					avglevels[5] += 0.0;
				}else{
					avglevels[0] += levels[0];
					avglevels[1] += levels[1];
					avglevels[2] += levels[2];
					avglevels[3] += levels[3];
					avglevels[4] += levels[4];
					avglevels[5] += levels[5];
				}
				i++;
				
			}

			avglevels[0] = avglevels[0]/div;
			avglevels[1] = avglevels[1]/div;
			avglevels[2] = avglevels[2]/div;
			avglevels[3] = avglevels[3]/div;
			avglevels[4] = avglevels[4]/div;
			avglevels[5] = avglevels[5]/div;
			aggs2_topic_levels.put(topic[0], avglevels);
		}
	}
	
	public void computeAverageTopStudentsContentLevels(int n){
		aggs2_content_levels = new HashMap<String,double[]>(); 
		int m = non_students.size();
		if ((class_list.size()-m) < n) n = (class_list.size()-m);
		if (n < 0) n=0;
		
		int div = n;
		if (div==0) div=1;
		//System.out.println(n+" top");
		int i = 0; 
		for (Map.Entry<String, String[]> content : content_list.entrySet()) {
			String content_name = content.getKey();
			double[] avglevels = new double[2];
			//System.out.println(content_name+": ");
			i = 0;
			while(i<top_students_ids.size()){
				String learner = top_students_ids.get(i);
				//System.out.print("   "+learner+" ");
				Map<String,double[]> learner_levels = peers_content_levels.get(learner);
				double[] levels = null;
				if(learner_levels != null) levels = learner_levels.get(content_name);
				if(levels == null || levels.length==0){
					avglevels[0] += 0.0;
					avglevels[1] += 0.0;
					//System.out.print("---,---\n");
				}else{
					//System.out.print(levels[0]+","+levels[1]+"\n");
					avglevels[0] += levels[0];
					avglevels[1] += levels[1];
				}
				i++;
			}
			
			avglevels[0] = avglevels[0]/div;
			avglevels[1] = avglevels[1]/div;
			aggs2_content_levels.put(content_name, avglevels);
		}
	}
	
	public String precomputedTopicModel(){
		String user_levels  = "";
		
		for (String[] topic : topic_list) {
			
			double[] levels = userTopicLevels.get(topic[0]);
			if (levels != null && levels.length>0){
				user_levels += topic[0]+":"+df.format(levels[0])+","+df.format(levels[1])+","+df.format(levels[2])+","+df.format(levels[3])+","+df.format(levels[4])+","+df.format(levels[5])+"|";				
			}
		}
		
		user_levels = user_levels.substring(0,user_levels.length()-1);
		return user_levels;
	}
	
	public String precomputedContentModel(){
		String user_levels  = "";
		for (Map.Entry<String, double[]> content : userContentLevels.entrySet()) {
			String content_name = content.getKey();
			double[] levels = content.getValue();
			if (levels != null && levels.length>0){
				user_levels += content_name+":"+df.format(levels[0])+","+df.format(levels[1])+"|";				
			}
		}
		
		user_levels = user_levels.substring(0,user_levels.length()-1);
		return user_levels;
	}
	
	public void storePrecomputedModel(String user){
		String model4topics = this.precomputedTopicModel();
		String model4content = this.precomputedContentModel();
		
		if (agg_db.existPrecomputedModelForSession(user, course_id, grp, sid)){
			agg_db.updatePrecomputedModel(user, course_id, grp, sid, model4topics, model4content);
		}else{
			agg_db.insertPrecomputedModel(user, course_id, grp, sid, model4topics, model4content);
		}
	}
	
	public double getTopicDifficulty(String topic){
		return 0.0;
	}
	public double getTopicImportance(String topic){
		return 0.0;
	}
	
	public String precomputeClassModels(){
		String output = "";
		openDBConnections();
		sid = "UNKNOWN";
		for(String[] learner: class_list){
			output += learner[0] + "\n";
			computeUserLevels(learner[0],domain);
			this.storePrecomputedModel(learner[0]);
			
		}
		closeDBConnections();
		return output;
	}
	
	// sequencing for the user (usr)
	// src: questions/examples
	public double getTopicSequenceScore(String topic, String src){
		if (topic_sequencing_score==null) return 0;
		double[] scores = topic_sequencing_score.get(topic);
		if (scores==null) return 0;
		if(src.equalsIgnoreCase("question")) return scores[0];
		if(src.equalsIgnoreCase("example")) return scores[1];
		return 0;
	}
	public double getContentSequenceScore(String content_name){
		if (content_sequencing_score==null) return 0;
		Double score = content_sequencing_score.get(content_name);
		if (score==null) return 0;
		return score;
	}
	
	public String getContentType(String content_name){
		String[] content_data = content_list.get(content_name);
		if (content_data != null) return content_data[0];
		else return "";
	}
	
	// TODO review
	// @@@@ FEEDBACK generation. For now, it is static 
	public void getFeedbackForm(String last_content_id, String last_content_type, String last_content_res, String feedback_id){
		activity_feedback_form_items = new ArrayList<ArrayList<String>>();
		
		
		// decide when to get the feedback
		// when last content visited was a question and was failed
		if(last_content_type.equalsIgnoreCase("question") && 
				last_content_id != null && 
				last_content_id.length()>0 && 
				last_content_res.equals("0") ){
			activity_feedback_id = feedback_id; // @@@@ should be generated as a unique key!!!!
			ArrayList<String> item1 = new ArrayList<String>();
			item1.add("ques_difficulty"); // question id 
			item1.add("How difficult was the question?"); // text 
			item1.add("one"); // type 
			item1.add("false"); // required
			item1.add("0;easy|1;medium|2;hard"); // response 
			// not added for now
			//activity_feedback_form_items.add(item1);
		}
		// when last content visited was a question and was succeed
		if(last_content_type.equalsIgnoreCase("question") && 
				last_content_id != null && 
				last_content_id.length()>0 && 
				last_content_res.equals("1") && !grp.substring(0,5).equalsIgnoreCase("STUDY")){
			activity_feedback_id = feedback_id; // @@@@ should be generated as a unique key!!!! 
			ArrayList<String> item1 = new ArrayList<String>();
			item1.add("ques_difficulty"); // question id 
			item1.add("How difficult was the question?"); // text 
			item1.add("one"); // type 
			item1.add("false"); // required
			item1.add("0;easy|1;medium|2;hard"); // response 
			activity_feedback_form_items.add(item1);
		}
	}
	
	public void getRecommendation(String id, String last_content_id, String last_content_type, String last_content_res, int n){
		//the topic is use
		String[] topic_name_pos = getTopicByContentName(last_content_id);
		// decide when to get the recommendation
		if(last_content_id != null && last_content_id.length()>0 && last_content_type.equalsIgnoreCase("question") && last_content_res.equals("0") ){
			openDBConnections();
			
			
			// pick the method for recommendations randomly if the user has not received
			// any recommendation for the same activity in the past hour			
			int method = inStringArray(rec_methods,  agg_db.recentRecMethod(usr, last_content_id));
			//System.out.println("There is recent recommendation, method: "+method);
			if (method<0) method = pickMethodRandomly();
			
			
			//System.out.println("picking method "+method+" ("+rec_methods[method]+")");
			// for STUDY 2013, some activities are disabled
			boolean show_rec = true;
			if (grp.length()>5){
				if (grp.substring(0,5).equalsIgnoreCase("STUDY")){
					// check if the question is in the list of the questions with no recommendation
					if (topic_name_pos != null){
						show_rec = agg_db.isShowingRecommedation(usr,  topic_name_pos[0], last_content_id, Integer.parseInt(topic_name_pos[1]));	
					}
				}
			}
			//
			//System.out.println("position: "+topic_name_pos[1]+"    show_rec: "+show_rec);
			if(show_rec){	
				recommendation_list = agg_db.generateRecommendations(id, usr, grp, course_id, sid, last_content_id,last_content_res,n,rec_methods,method, topic_content,examples_activity,questions_activity,0);
			}else{
				// generate recommendation anyway, but with no method selected
				agg_db.generateRecommendations(id, usr, grp, course_id, sid, last_content_id,last_content_res,n,rec_methods,-1, topic_content, examples_activity, questions_activity,0);
			}
			closeDBConnections();
		}
	}
	
	public void getAllRecommendations(String id, String content_name, int maxn){
		recommendation_list = agg_db.getAllRecommended(usr, content_name, course_id, rec_methods, maxn);
		if(recommendation_list == null){
			// If there is no recommendations made, create them first
			//System.out.println(" -> Generating false recommendations for question "+content_name);
			recommendation_list = agg_db.generateRecommendations(id, usr, grp, course_id, sid, content_name, "0", 2, rec_methods, -1, topic_content,examples_activity,questions_activity,1);
		}else{
			//System.out.println("Question "+content_name+" had recommendations in the past for user "+usr);
		}
		//System.out.println(recommendation_list.size());
		removeDuplicateRecs(recommendation_list);
	}
	
	public void removeDuplicateRecs(ArrayList<ArrayList<String>> recommendation_list){
		if (recommendation_list.size()>1){
			for(int i=1;i<recommendation_list.size();i++){
				ArrayList<String> item_i = recommendation_list.get(i);
				for(int j=0;j<i;j++){
					ArrayList<String> item_j = recommendation_list.get(j);
					if (item_i.get(3).equalsIgnoreCase(item_j.get(3))){
						recommendation_list.remove(i);
						i--;
					}
				}
			}
		}
		
	}
	
	public static int inStringArray(String[] array, String s){
		for (int i=0;i<array.length;i++){
			if (array[i].equalsIgnoreCase(s)) return i;
		}
		return -1;
	}
	
	public int pickMethodRandomly(){
		//System.out.println(Math.random() + " " + rec_methods.length);
		return (int) (Math.random()*rec_methods.length);
	}
	
	// TODO debug
	// get the first topic name that have the content item and the position of this content item 
	// in the topic
	public String[] getTopicByContentName(String content_name){
		String[] res = null;
		for (String[] topic : topic_list) {
            ArrayList<String>[] oneTypeContents = topic_content.get(topic[0]);
            ArrayList<String> questions = oneTypeContents[0];
            int i=1;
            for (String question : questions) {
            	if (content_name.equalsIgnoreCase(question)){
            		res = new String[2];
            		res[0] = topic[0];
            		res[1] = ""+i;
            		return res;
            	}
            	i++;
            }
		}
		return null;
	}
	
	public boolean trackAction(String action, String comment){
		boolean connection_was_open = false;
		try {connection_was_open = !agg_db.conn.isClosed();}
		catch (Exception e){}
		boolean res = false;
		if (!connection_was_open) agg_db.openConnection();
		if(agg_db.insertTrackAction(usr, grp, sid, action, comment)) 
			res = true;
		if(!connection_was_open) agg_db.closeConnection();
		return res;
	}
	
	
	public String genJSONHeader(){
		String res  = "{\n  version:\"0.0.3\",\n" +
				"  context:{ learnerId:\""+usr+"\",group:{id:\""+grp+"\",name:\""+grp_name+"\"}},\n" +
		   	    "  reportLevels:[{id:\"p\",name:\"Progress\"},{id:\"k\",name:\"Knowledge\"}],\n" +
		   	    "  resources:[\n" +
		   	    "    {id:\"qz\",name:\"Questions\", " +
		   	    "updateStateOn: {done: true, winClose: false, winCloseIfAct: false}},\n" +
		   	    "    {id:\"ex\",name:\"Examples\", " +
		   	    "updateStateOn: {done: false, winClose: true, winCloseIfAct: false}}\n" +
		   	    "  ]";
		return res;
	}
	
	public String genJSONVisProperties(){
		String res  = "vis:{\n  topicSizeAttr:[\"difficulty\",\"importance\"],\n  color:{binCount:7,value2color:function (x) { var y = Math.log(x)*0.25 + 1;  return (y < 0 ? 0 : y); }}\n}";
		return res;
	}
	public String genJSONTopics(){
		String topics = "  topics:[\n";
		
		for (String[] topic : topic_list) {
			String visible = "true";
			if (topic[3].equalsIgnoreCase("0")) visible = "false"; 
			topics += "  {\n    id:\""+topic[0]+"\",name:\""+topic[1]+"\",difficulty:"+df.format(getTopicDifficulty(topic[1]))+",importance:"+df.format(getTopicImportance(topic[1]))+",order:"+topic[2]+",concepts:["; 
			// TODO : concepts
			topics += "],isVisible:"+visible+",\n";
			topics += "    activities:{ \n";
			//
			ArrayList<String>[] content =  topic_content.get(topic[0]);
			//
			ArrayList<String> t_questions = null;
			ArrayList<String> t_examples =  null;
			ArrayList<String> t_readings =  null;
			if(content != null){
				t_questions = content[0];
				t_examples = content[1];
				t_readings = content[2];				
			}
			// QUESTIONS
			topics += "      \"qz\":[\n";
			if(t_questions!=null && t_questions.size()>0){
				for(String q : t_questions){
					String[] content_data = this.content_list.get(q);
					topics += "        {id:\""+q+"\",name:\""+content_data[1]+"\",url:\""+content_data[2]+"\"},\n";
				}
				topics = topics.substring(0,topics.length()-2); // get rid of the last comma
			}			
			topics += "\n      ],\n";
			// EXAMPLES
			topics += "      \"ex\":[\n";
			if(t_examples!=null && t_examples.size()>0){
				for(String e : t_examples){
					String[] content_data = this.content_list.get(e);
					topics += "        {id:\""+e+"\",name:\""+content_data[1]+"\",url:\""+content_data[2]+"\"},\n";
				}
				topics = topics.substring(0,topics.length()-2); // get rid of the last comma
			}			
			topics += "\n      ]\n";
			
			topics += "    }\n  },\n";
		}
		
		topics = topics.substring(0,topics.length()-2); // get rid of the last comma
		//user_levels = user_levels.substring(0,user_levels.length()-1);
		topics += "\n]";
		return topics;
	} 

	public String genJSONLearnerState(String student){
		String res = "    state:{\n";
		Map<String,double[]> student_t_l = null;
		if (peers_topic_levels != null) student_t_l =  peers_topic_levels.get(student);
		
		Map<String,double[]> student_c_l = null;
		if (peers_content_levels != null) student_c_l = peers_content_levels.get(student);
		
		String topic_levels = "      topics:{\n";
		String content_levels = "      activities:{\n";
		
		String seq = "";
		boolean sequencing = (student.equalsIgnoreCase(usr)); 
		
		for (String[] topic : topic_list) {
			String topic_name = topic[0];
			double[] levels = null;
			seq = "";
			if (sequencing) seq = ",sequencing:{\"qz\":"+df.format(getTopicSequenceScore(topic_name,"question"))+",\"ex\":"+df.format(getTopicSequenceScore(topic_name,"example"))+"}";
			if (student_t_l != null) levels= student_t_l.get(topic_name);
			if (levels != null){
				topic_levels += "       \""+topic_name+"\": {values:{\"qz\":{\"k\":"+df.format(levels[0])+",\"p\":"+df.format(levels[1])+"},\"ex\":{\"k\":"+df.format(levels[2])+",\"p\":"+df.format(levels[3])+"}}"+seq+"},\n";
			}else{
				topic_levels += "       \""+topic_name+"\": {values:{\"qz\":{\"k\":0,\"p\":0},\"ex\":{\"k\":0,\"p\":0}}"+seq+"},\n";
			}
			
			content_levels += "       \""+topic_name+"\": {\n";
			ArrayList<String>[] content =  topic_content.get(topic_name);
			
			ArrayList<String> t_questions = null;
			ArrayList<String> t_examples =  null;
			ArrayList<String> t_readings =  null;
			
			if(content != null){
				t_questions = content[0];
				t_examples = content[1];
				t_readings = content[2];				
			}
			
			
			// QUESTIONS
			content_levels += "        \"qz\":{";
			if(t_questions != null && t_questions.size()>0){
				content_levels += "\n";
				for(String q : t_questions){
					//System.out.println("Q:"+q);
					seq = "";
					if (sequencing) seq = ",sequencing:"+df.format(getContentSequenceScore(q));
					if (student_c_l == null) levels = null;
					else levels = student_c_l.get(q);
					if (levels != null){
						content_levels += "          \""+q+"\": {values:{\"k\":"+df.format(levels[0])+",\"p\":"+df.format(levels[1])+"}"+seq+"},\n";
					}else{
						content_levels += "          \""+q+"\": {values:{\"k\":0,\"p\":0}"+seq+"},\n";
					}

					//content_levels += "    {},\n";
				}
				content_levels = content_levels.substring(0,content_levels.length()-2); // get rid of the last comma
				content_levels += "\n        },\n";
			}else{
				content_levels += "},\n";
			}			
			
			// EXAMPLES
			content_levels += "        \"ex\":{";
			if(t_examples != null && t_examples.size()>0){
				content_levels += "\n";
				for(String e : t_examples){
					seq = "";
					if (sequencing) seq = ",sequencing:"+df.format(getContentSequenceScore(e));
					if (student_c_l == null) levels = null;
					else levels = student_c_l.get(e);
					if (levels != null){
						content_levels += "          \""+e+"\": {values:{\"k\":"+df.format(levels[0])+",\"p\":"+df.format(levels[1])+"}"+seq+"},\n";
					}else{
						content_levels += "          \""+e+"\": {values:{\"k\":0,\"p\":0}"+seq+"},\n";
					}
				}
				content_levels = content_levels.substring(0,content_levels.length()-2); // get rid of the last comma
				content_levels += "\n        }\n";
			}else{
				content_levels += "}\n";
			}
			content_levels += "       },\n";
		}
		content_levels = content_levels.substring(0,content_levels.length()-2);
		topic_levels = topic_levels.substring(0,topic_levels.length()-2);
				
		topic_levels += "\n      },\n";
		content_levels += "\n      }\n";

		res += topic_levels + content_levels + "\n    }";
		
		return res;
	}

	// agg: 1: all class students, 2: only top N students
	public String genJSONGroupState(int agg){
		String res = "  state:{\n";
		Map<String,double[]> aggs_t_l = aggs1_topic_levels;
		Map<String,double[]> aggs_c_l = aggs1_content_levels;
		if (agg == 2) {
			aggs_t_l = aggs2_topic_levels;
			aggs_c_l = aggs2_content_levels;
		}
		
		String topic_levels = "    topics:{\n";
		String content_levels = "    activities:{\n";
		
		for (String[] topic : topic_list) {
			String topic_name = topic[0];
			double[] levels = null;
			if (aggs_t_l != null) levels= aggs_t_l.get(topic_name);
			if (levels != null){
				topic_levels += "      \""+topic_name+"\": {values:{\"qz\":{\"k\":"+df.format(levels[0])+",\"p\":"+df.format(levels[1])+"},\"ex\":{\"k\":"+df.format(levels[2])+",\"p\":"+df.format(levels[3])+"}}},\n";
			}else{
				topic_levels += "      \""+topic_name+"\": {values:{\"qz\":{\"k\":0,\"p\":0},\"ex\":{\"k\":0,\"p\":0}}},\n";
			}
			
			content_levels += "      \""+topic_name+"\": {\n";
			ArrayList<String>[] content =  topic_content.get(topic_name);
			ArrayList<String> t_questions = null;
			ArrayList<String> t_examples =  null;
			ArrayList<String> t_readings =  null;
			
			if(content != null){
				t_questions = content[0];
				t_examples = content[1];
				t_readings = content[2];				
			}
			
			// QUESTIONS
			content_levels += "      \"qz\":{";
			if(t_questions != null && t_questions.size()>0){
				content_levels += "\n";
				for(String q : t_questions){
					//System.out.println("Q:"+q);
					if (aggs_c_l == null) levels = null;
					else levels = aggs_c_l.get(q);
					if (levels != null){
						content_levels += "        \""+q+"\": {values:{\"k\":"+df.format(levels[0])+",\"p\":"+df.format(levels[1])+"}},\n";
					}else{
						content_levels += "        \""+q+"\": {values:{\"k\":0,\"p\":0}},\n";
					}

					//content_levels += "    {},\n";
				}
				content_levels = content_levels.substring(0,content_levels.length()-2); // get rid of the last comma
				content_levels += "\n      },\n";
			}else{
				content_levels += "},\n";
			}			
			
			// EXAMPLES
			content_levels += "      \"ex\":{";
			if(t_examples != null && t_examples.size()>0){
				content_levels += "\n";
				for(String e : t_examples){
					if (aggs_c_l == null) levels = null;
					else levels = aggs_c_l.get(e);
					if (levels != null){
						content_levels += "        \""+e+"\": {values:{\"k\":"+df.format(levels[0])+",\"p\":"+df.format(levels[1])+"}},\n";
					}else{
						content_levels += "        \""+e+"\": {values:{\"k\":0,\"p\":0}},\n";
					}
				}
				content_levels = content_levels.substring(0,content_levels.length()-2); // get rid of the last comma
				content_levels += "\n      }\n";
			}else{
				content_levels += "}\n";
			}
			content_levels += "      },\n";
		}
		content_levels = content_levels.substring(0,content_levels.length()-2);
		topic_levels = topic_levels.substring(0,topic_levels.length()-2);
				
		topic_levels += "\n    },\n";
		content_levels += "\n    }\n";

		String learnersids = "learnerIds:[  ";
		if(agg == 2){
			for(String studentid : top_students_ids){
				learnersids += "\""+studentid+"\", ";
			}
			learnersids = learnersids.substring(0,learnersids.length()-2);
		}else{
			for(String[] studentdata : class_list){
				if (non_students.get(studentdata[0]) == null){
					learnersids += "\""+studentdata[0]+"\", ";
				}
			}
			learnersids = learnersids.substring(0,learnersids.length()-2);
		}
		learnersids += "]";
		res += topic_levels + content_levels + "\n },\n  " + learnersids;
		
		return res;
	}

	
	public String genJSONRecommendation(){
		String res = "  recommendation:[\n";
		if (recommendation_list != null && recommendation_list.size()>0){
			for(ArrayList<String> rec : recommendation_list){
				String stored_value="-1";
				if (rec.get(6) != null) stored_value = rec.get(6);
				res += "    {recommendationId:\""+rec.get(0)+"\",topicId:\""+rec.get(1)+"\",resourceId:\""+rec.get(2)+"\",activityId:\""+rec.get(3)+"\",score:"+rec.get(4)+",feedback:{text:\""+rec.get(5)+"\", storedValue:"+stored_value+"}},\n";
			}
			res = res.substring(0,res.length()-2);
		}
		
		res += "\n  ]";
		return res;
	}
	
	public String genJSONFeedback(){
		String res = "  feedback:{\n";
		if (activity_feedback_form_items != null && activity_feedback_form_items.size()>0){
			// the activity_feedback_form_id
			res += "    id:\""+activity_feedback_id+"\",\n    items:[\n";
			for(ArrayList<String> fed : activity_feedback_form_items){
				res += "      {id:\""+fed.get(0)+"\",text:\""+fed.get(1)+"\",type:\""+fed.get(2)+"\",required:\""+fed.get(3)+"\",\n          response:[";
				String[] _response_items = fed.get(4).split("\\|"); 
				if (_response_items != null && _response_items.length>0){
					for(int i=0;i<_response_items.length;i++){
						String[] _response = _response_items[i].split(";");
						if (_response != null && _response.length==2){
							res += "{value:"+_response[0]+",label:\""+_response[1]+"\"},";
						}
					}
					res = res.substring(0,res.length()-1);
				}
				res += "]},\n";
			}
			res = res.substring(0,res.length()-2);
			res += "\n    ]\n";
		}
		
		res += "  }";
		return res;
	}
	
	// generate JSON output for all the data!!!!
	public String genAllJSON(int n, int top){
		String header = genJSONHeader();
		String visprop = genJSONVisProperties();
		String topics = genJSONTopics();
				
		String learners = "learners:[ \n";
		String learner_levels_json = "";
		for(String[] learner: class_list){
			String ishidden = "false";
			if (non_students.get(learner[0]) != null) ishidden = "true";
			learners += "{\n  id:\""+learner[0]+"\",name:\""+learner[1]+"\",isHidden:"+ishidden+",\n  "+ genJSONLearnerState(learner[0]) + "\n},\n";
		}
		learners = learners.substring(0,learners.length()-2); 
		learners += "\n]";
		// 
		int i = 0;

		// TODO aggregations!!
		String aggs_levels = "groups:[\n";

		String aggs_1 = "{\n  name:\"Class Average\",\n";
		String aggs_2 = "{\n  name:\"Top "+top+"\",\n";

		aggs_1 += genJSONGroupState(1) + "\n},\n";
		aggs_2 += genJSONGroupState(2) + "\n}\n";
		
		aggs_levels += aggs_1+aggs_2+"]";

		
		return header + ",\n" + topics + ",\n" + learners + ",\n" + aggs_levels + ",\n" +visprop + "\n}";
	}

	// REVIEW generate the main JSON response for the logged in user
	public String genUserJSON(String last_content_id, String last_content_res){
		String output = "{\n  lastActivityId:\""+last_content_id+"\",\n  lastActivityRes:"+last_content_res+",\n  learner:{\n    id:\""+usr+"\",name:\""+usr_name+"\",\n";
		output += genJSONLearnerState(usr);
		output += "\n  },\n"; // closing learner object
		output += genJSONRecommendation() + ",\n";
		output += genJSONFeedback();
		output += "\n}";
		return output;
	}
}
