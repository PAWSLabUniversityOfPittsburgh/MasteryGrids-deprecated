

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RefreshPreModels
 */
@WebServlet("/RefreshPreModels")
public class RefreshPreModels extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RefreshPreModels() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		String grp = request.getParameter("grp"); // the class mnemonic (as defined in KT)
		
		// the main object
		Aggregate aggregate; 
		String output = "Computing user models:\n";
		ConfigManager cm = new ConfigManager(this);
		aggregate = new Aggregate(grp, cm);
		output += aggregate.precomputeClassModels(); // precompute all students models
			
		out.print(output);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
