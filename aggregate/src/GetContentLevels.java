
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetContentLevels
 */
@WebServlet("/GetContentLevels")
public class GetContentLevels extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int number_recommendation = 5; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetContentLevels() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		String mod = request.getParameter("mod"); // all / user / class
		String grp = request.getParameter("grp"); // the class mnemonic (as defined in KT)
		String usr = request.getParameter("usr"); // user name
		String sid = request.getParameter("sid"); // session id
		String cid = request.getParameter("cid"); // course id (for content)
		
		String nmodels = request.getParameter("models"); // how many models to retrieve (-1: all)
		String ntop = request.getParameter("avgtop"); // how many top students for the top group
		
		// these parameters are in the case the mod=user
		String last_content_id = request.getParameter("lastActivityId"); 
		String last_content_res = request.getParameter("res"); 
		if (last_content_id == null) last_content_id="";
		if (last_content_res == null) last_content_res="-1";

		
		// if problems to get the variables, defaults are nmodels=-1 (retrieve all), top=3
		int n=-1; // this variable controls how many models will be retrieved
		int top = 3; // this variable controls how many top students to consider in the top N group 
		try{ n = Integer.parseInt(nmodels);}catch(Exception e){n=-1;}
		try{ top = Integer.parseInt(ntop);}catch(Exception e){top=3;}
		if (cid != null && cid.length()>0) try{ int c = Integer.parseInt(cid);}catch(Exception e){cid="-1";}
		// @ TODO
		//System.out.println("CID: "+cid);
		
		// the main object
		Aggregate aggregate; 
		String output = "";
		
		if(mod == null || mod.length() == 0 || mod.equalsIgnoreCase("all")){
			// this crates all structures, fill the information and computes the up to date user model
			aggregate = new Aggregate(usr,grp,cid,sid,this);
			// Get stores models for class peers
			aggregate.getClassLevels();
			
			// this tells which values to consider for computing global score for ranking students
			boolean[] include = {false,true,false,true,false,false}; // {qk, qp, ek, ep, rk, rp}
			aggregate.orderClassByScore(include);
			//System.out.println("class students ordered");
			
			aggregate.computeAverageClassTopicLevels();
			//System.out.println("class average topic computed");
			aggregate.computeAverageTopStudentsTopicLevels(top);
			//System.out.println("class top topic computed");
			aggregate.computeAverageClassContentLevels();
			//System.out.println("class average content computed");
			aggregate.computeAverageTopStudentsContentLevels(top);
			//System.out.println("class top content computed");
//			
			//guanjie.trackAction("request_all_data", "", "", "", "", "");
			
			output = aggregate.genAllJSON(n,top);
			
		}else if(mod.equalsIgnoreCase("user")){
			aggregate = new Aggregate(usr,grp,cid,sid,this);
			aggregate.getClassLevels();
			String last_content_type = aggregate.getContentType(last_content_id);
			//System.out.println(last_content_id+": "+last_content_type);
			String feedback_id = ""+(System.nanoTime()/1000);
			// @@@@
			aggregate.getRecommendation(feedback_id,last_content_id, last_content_type, last_content_res, number_recommendation);
			aggregate.getFeedbackForm(last_content_id, last_content_type, last_content_res, feedback_id);
			//String comment = "";
			//if (guanjie.recommendation_list != null && guanjie.recommendation_list.size()>0) comment += "recommendation|";
			//if (guanjie.activity_feedback_form_items != null && guanjie.activity_feedback_form_items.size()>0) comment += "feedbackId="+feedback_id;		
				
			output = aggregate.genUserJSON(last_content_id,last_content_res);
			// track that the user state is requested
			//guanjie.trackAction("request_user_data", "", "", last_content_id, last_content_res, comment);
		
		}else if(mod.equalsIgnoreCase("class")){
			
		}
		
		out.print(output);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
