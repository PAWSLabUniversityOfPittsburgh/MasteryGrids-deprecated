

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SetNonStudents
 */
@WebServlet("/SetNonStudents")
public class SetNonStudents extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AggregateDB agg_db;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetNonStudents() {
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

        // to add as non-students
        String argInstructors = request.getParameter("instructors"); 
        String argTAs = request.getParameter("assistants");
        String argResearchers = request.getParameter("researchers");	
        if(argGrp == null ){
        	out.write("{error:1, message:\"group parameter is not defined\"}");
        }else{
        	agg_db = new AggregateDB(cm.agg_dbstring, cm.agg_dbuser, cm.agg_dbpass);
            agg_db.openConnection();
            String existingCID = agg_db.getCourseId(argGrp);
            if(existingCID == null || existingCID.length() == 0){
            	out.write("{error:1, message:\"group does not exist\"}");
            	
            }else{
                if(argInstructors != null){
            		String[] nonStudents = argInstructors.trim().split(",");
            		for(String nonStudent : nonStudents) 
            			if(nonStudent.length()>0) agg_db.addNonStudent(argGrp, nonStudent, "instructor");
            	}
                if(argTAs != null){
            		String[] nonStudents = argTAs.trim().split(",");
            		for(String nonStudent : nonStudents) 
            			if(nonStudent.length()>0) agg_db.addNonStudent(argGrp, nonStudent, "TA");
            	}
                if(argResearchers != null){
            		String[] nonStudents = argResearchers.trim().split(",");
            		for(String nonStudent : nonStudents) 
            			if(nonStudent.length()>0) agg_db.addNonStudent(argGrp, nonStudent, "researcher");
            	}
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
