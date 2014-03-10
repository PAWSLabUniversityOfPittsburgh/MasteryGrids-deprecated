

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetExamplesToRate
 */
@WebServlet("/GetExamplesToRate")
public class GetExamplesToRate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetExamplesToRate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		String grp = request.getParameter("grp"); // the class mnemonic (as defined in KT)
		String usr = request.getParameter("usr"); // user name
		String sid = request.getParameter("sid"); // session id
		String cid = request.getParameter("cid"); // course id (for content)
		
		String last_content_id = request.getParameter("lastActivityId"); 
		if (last_content_id == null) last_content_id="";
		
		if (cid != null && cid.length()>0) try{ int c = Integer.parseInt(cid);}catch(Exception e){cid="-1";}

		// the main object
		Aggregate aggregate; 
		String output = "";
		
		aggregate = new Aggregate(usr,grp,cid,sid,this);
		aggregate.getClassLevels();
		//String last_content_type = guanjie.getContentType(last_content_id);
		String feedback_id = ""+(System.nanoTime()/1000);
		//guanjie.getRecommendation(feedback_id,last_content_id, last_content_type, last_content_res, number_recommendation);
		//guanjie.getFeedbackForm(last_content_id, last_content_type, last_content_res, feedback_id);
		aggregate.openDBConnections();
		aggregate.getAllRecommendations(feedback_id,last_content_id, 2);
		aggregate.closeDBConnections();
		
		//output = guanjie.genUserJSON(last_content_id,last_content_res);
		output = "{\n "+aggregate.genJSONRecommendation()+"\n}";
		
		out.print(output);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
