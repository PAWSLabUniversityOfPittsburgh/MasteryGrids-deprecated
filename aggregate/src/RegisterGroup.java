

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RegisterGroup
 */
@WebServlet("/RegisterGroup")
public class RegisterGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private AggregateDB agg_db;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterGroup() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8888");
        ConfigManager cm = new ConfigManager(this);

        PrintWriter out = response.getWriter();

        String argGrp = request.getParameter("grp"); // the class mnemonic
        String argName = request.getParameter("grp_name"); // the group name
        String argCid = request.getParameter("cid"); // course id
        String argTerm = request.getParameter("term"); 
        String argYear = request.getParameter("year"); 
        String argCreator = request.getParameter("creator"); 
        
        if(argGrp == null || argCid == null){
        	out.write("{error:1, message:\"group null or course id null\"}");
        }else{
        	agg_db = new AggregateDB(cm.agg_dbstring, cm.agg_dbuser, cm.agg_dbpass);
            agg_db.openConnection();
            
            String existingCID = agg_db.getCourseId(argGrp);
            if(existingCID != null && existingCID.length() > 0){
            	out.write("{error:2, message:\"group already in the database with course id = "+existingCID+"\"}");
            }else{
            	agg_db.registerGroup(argGrp, argName, argCid, argTerm, argYear, argCreator);
            	out.write("{status:\"ok\"}");
            }
            agg_db.closeConnection();
        }

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
