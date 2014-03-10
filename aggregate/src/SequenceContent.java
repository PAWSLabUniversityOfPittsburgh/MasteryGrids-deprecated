import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*
 * This class is for calculating a rank for sequencing the contents and topics.
 * @author: Roya Hosseini
 */
public class SequenceContent {
	private HashMap<String,Double> contentSequencing;
	private HashMap<String,double[]> topicSequencing;
	
	
	public SequenceContent(
			HashMap<String,ArrayList<String[]>> content_concepts,
			Map<String, Double> user_concept_knowledge_levels,
			HashMap<String,String[]> examples_activity,
			HashMap<String,String[]> questions_activity,
			HashMap<String,ArrayList<String>[]> topic_content){

		contentSequencing = new HashMap<String,Double>();
		topicSequencing = new HashMap<String,double[]>();
		
		calculateSequenceRank(content_concepts, user_concept_knowledge_levels,
				examples_activity, questions_activity, topic_content);
	}

	private void calculateSequenceRank(
			HashMap<String, ArrayList<String[]>> content_concepts,
			Map<String, Double> user_concept_knowledge_levels,
			HashMap<String, String[]> examples_activity,
			HashMap<String, String[]> questions_activity,
			HashMap<String, ArrayList<String>[]> topic_content) {
		
		HashMap<String,Double> contentPrerequisiteKnowledgeMap = new HashMap<String,Double>();
		HashMap<String,Double> contentImpactMap = new HashMap<String,Double>();
		HashMap<String,Double> contentUnlearnedRatioMap = new HashMap<String,Double>();
		
		/* step1: calculate the prerequisite knowledge of the student in the
		 * contents; The results will be stored in the map
		 * contentPrerequisiteKnowledgeMap. 
		 * Also calculate the impact of the content, here by the impact we focus on the concepts that
		   are outcome of the content. We calculate how much the learner still need to know in each of the outcomes
		   and this will form the content impact. The results will be stored in the map contentImpactMap 
		 *  @see document of optimizer */		
		double prerequisiteKnowledgeRatio = 0.0;
		double dividendPrerequisite = 0.0;
		double denominatorPrerequisite = 0.0;
		
		double impactRatio = 0.0;
		double dividendImpact = 0.0;
		double denominatorImpact = 0.0;
		
		double weight = 0.0;		
		String content_name;
		ArrayList<String[]> conceptList;
		
		for (Entry<String, ArrayList<String[]>> entry : content_concepts.entrySet())
		{
			content_name = entry.getKey();
			conceptList = entry.getValue(); //[0] concept, [1] weight, [2] direction
			
			dividendPrerequisite = 0.0;
			denominatorPrerequisite = 0.0;
			dividendImpact = 0.0;
			denominatorImpact = 0.0;
			prerequisiteKnowledgeRatio = 0.0;
			impactRatio = 0.0;
			
			for (String[] concept : conceptList)
			{
				weight = 0.0;
				double klevel = 0.0;
				if(user_concept_knowledge_levels != null && user_concept_knowledge_levels.get(concept[0]) != null) 
					klevel = user_concept_knowledge_levels.get(concept[0]);
				try
				{
					weight = Double.parseDouble(concept[1]);
				}catch(Exception e){}
				
				if (concept[2].equals("prerequisite"))
				{					
					dividendPrerequisite += klevel * Math.log10(weight);
					denominatorPrerequisite += Math.log10(weight);					
				}
				else if (concept[2].equals("outcome"))
				{
					dividendImpact += (1-klevel) * Math.log10(weight);
					denominatorImpact += Math.log10(weight);					
				}				
			}
			
			if (denominatorPrerequisite != 0)
				prerequisiteKnowledgeRatio = dividendPrerequisite / denominatorPrerequisite;
			contentPrerequisiteKnowledgeMap.put(content_name,prerequisiteKnowledgeRatio);	
			
			if (denominatorImpact != 0)
				impactRatio = dividendImpact / denominatorImpact;
			contentImpactMap.put(content_name,impactRatio);	
		}		
		
		/* step2: calculate the value of how much the student has not learned about each content.
		   The results will be stored in the contentUnlearnedRatioMap.
		   To this end, for questions we use the total number of times the user tried
		   each of the questions and also the number of success in each of the them.
		   For examples, we use the distinct lines viewed and the total lines in each of the examples.
		 */
		
		double unlearnedRatio = 0.0;
		
		//for questions
		double attempt = 0.0;
		double success = 0.0;
		for(Entry<String, String[]> entry: questions_activity.entrySet()){
			String question = entry.getKey();
			String[] questionInfo = entry.getValue();
			attempt = 0.0;
			success = 0.0;
			try
			{
				attempt = Double.parseDouble(questionInfo[1]); //[1] nattempts
				success = Double.parseDouble(questionInfo[2]);//[2] nsuccess
			}catch(Exception e){}
			unlearnedRatio = 1 - (success+1)/(attempt+1);
			contentUnlearnedRatioMap.put(question, unlearnedRatio);
		}
		
		// for examples
		double distinctLines = 0.0;
		double totalLines = 0.0;
		for (Entry<String, String[]> entry : examples_activity.entrySet()) {
			String example = entry.getKey();
			String[] exampleInfo = entry.getValue();
			distinctLines = 0.0;
			totalLines = 0.0;
			try {
				distinctLines = Double.parseDouble(exampleInfo[2]); //[2] distinctactions
				totalLines = Double.parseDouble(exampleInfo[3]);//[3] totallines
			} catch (Exception e) {
			}
			unlearnedRatio = 1 - (distinctLines + 1) / (totalLines + 1);
			contentUnlearnedRatioMap.put(example, unlearnedRatio);
		}
		
		/* Step3: all contents can be ranked by aggregating their corresponding values in these three maps and dividing by 3:
		 * 1) contentPrerequisiteKnowledgeMap 
		 * 2)contentImpactMap 
		 * 3)contentUnlearnedRatioMap
		 * The result is the final rank of content in the contentRankMap. Rank is between 0 and 1. */
		double rank = 0.0;
		for (String content : content_concepts.keySet())
		{		
			prerequisiteKnowledgeRatio = 0.0;
			impactRatio = 0.0;
			unlearnedRatio = 0.0;
			if (contentPrerequisiteKnowledgeMap.get(content) != null)
				prerequisiteKnowledgeRatio = contentPrerequisiteKnowledgeMap.get(content);
			if(contentImpactMap.get(content) != null)
				impactRatio = contentImpactMap.get(content);
			if(contentUnlearnedRatioMap.get(content) != null)
				unlearnedRatio = contentUnlearnedRatioMap.get(content);
			rank = (prerequisiteKnowledgeRatio + impactRatio + unlearnedRatio)/3.0; // rank is between 0 and 1 for each content
			contentSequencing.put(content, rank);
		}
		
		/* Step 4: calculate the rank of topics by aggregating the rank of its contents in contentRankMap 
		 * Currently we only consider examples and questions.
		 * The results are stored in topicSequencing map<String,double[]). The first value in double[] is obtained by calculating the 
		 * ratio of the questions' rank. The second value is obtained by calculating the ratio of examples' rank. 
		 * Ratio is obtained by sum of contents' rank over number of contents in each topic.
		 * Ratio is between 0 and 1.*/
		String topic;
		List<String>[] list;
		List<String> questionList;
		List<String> exampleList;
		for (Entry<String, ArrayList<String>[]> entry : topic_content.entrySet())
		{
			double[] topicRank = new double[2];//[0] questions, [1] examples
			topic = entry.getKey();
			list = entry.getValue();
			questionList = list[0]; //for questions
			exampleList = list[1]; //for examples
			rank = 0.0;
			for (String question : questionList)
			{
				rank += contentSequencing.get(question);
			}
			if (rank != 0.0)
				rank /= questionList.size();
			topicRank[0] = rank;
			rank = 0.0;
			for (String example : exampleList)
			{
				rank += contentSequencing.get(example);
			}
			if (rank != 0.0)
				rank /= exampleList.size();
			topicRank[1] = rank;
			topicSequencing.put(topic, topicRank);
			//System.out.println(topic+"\t"+rank);
		}
	}
	
	public HashMap<String, Double> getContentSequencing(){
		return contentSequencing;
	}
	public HashMap<String,double[]> getTopicSequencing(){
		return topicSequencing;
	}
		
}

