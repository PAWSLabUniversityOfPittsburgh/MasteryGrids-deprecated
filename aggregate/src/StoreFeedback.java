import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class StoreFeedback
 */
@WebServlet("/StoreFeedback")
public class StoreFeedback extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StoreFeedback() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	/*
	 * input URL:
	 * 
	 * StoreFeedback?
	 * usr=peterb&grp=IS172012Fall&sid=TEST001&srcActivityId=jVariables1&
	 * srcActivityRes=0&
	 * fbId=2&fbItemsIds=ques_difficulty|ques_learn_exp&responses=0|1& recId=
	 * 
	 * StoreFeedback? usr=peterb&grp=IS172012Fall&sid=TEST001&
	 * srcActivityId=jVariables1& srcActivityRes=0& fbId=& fbItemsIds=&
	 * responses=1& recId=2
	 */

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		String output = "";

		String usr = request.getParameter("usr"); // user name
		String grp = request.getParameter("grp"); // the class mnemonic (as defined
		// in KT)
		String sid = request.getParameter("sid"); // session id
		String srcActivityId = request.getParameter("srcActivityId");
		String srcActivityRes = request.getParameter("srcActivityRes");
		String fbId = request.getParameter("feedbackId");
		String fbItemIds = request.getParameter("feedbackItemsIds");
		if (fbItemIds == null || fbItemIds.length() == 0) fbItemIds="-1";
		String responses = request.getParameter("responses");
		String recId = request.getParameter("recommendationId");

		ConfigManager cm = new ConfigManager(this); // this object gets the
		
		AggregateDB agg_db = new AggregateDB(cm.agg_dbstring, cm.agg_dbuser, cm.agg_dbpass);
		agg_db.openConnection();
		if(agg_db.insertUsrFeedback(usr, grp, sid, srcActivityId, srcActivityRes, fbId, fbItemIds, responses, recId))
			output="{res=1}";
		else 
			output="{res=0}";
		agg_db.closeConnection();
		out.print(output);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
