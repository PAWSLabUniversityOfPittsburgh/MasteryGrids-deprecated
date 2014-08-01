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
@WebServlet("/TrackAction")
public class TrackAction extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TrackAction() {
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
	 * TrackAction?
	 * usr=peterb&grp=IS172012Fall&sid=TEST001&action_type=pick_topic&
	 * action_target=Variables&
	 * action_target_sub=&action_src=group&action_src_sub=average
	 */

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		String output = "";

		String usr = request.getParameter("usr"); // user name
		String grp = request.getParameter("grp"); // the class mnemonic (as defined in KT)
		String sid = request.getParameter("sid"); // session id

		String action = request.getParameter("action");

		ConfigManager cm = new ConfigManager(this); // this object gets the
		AggregateDB agg_db = new AggregateDB(cm.agg_dbstring,cm.agg_dbuser, cm.agg_dbpass);
		agg_db.openConnection();
		if(agg_db.insertTrackAction(usr, grp, sid, action, "")) 
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
