import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetContentLevels
 * usr, grp, sid should be provided by the log in (authentication) <br />
 * mod can take the values:  <br />
 * 
 * - all: retrieve all initial information when loading MG 
 * including peers and averages and top students groups <br />
 * http://localhost:8080/aggregate/GetContentLevels?usr=adl01&grp=ADL&sid=TESTADL01&cid=1&mod=all&models=-1&avgtop=4
 * avgtop indicates how many top students to include in the top group average
 * models indicates how many individual models will be inluded in the response. Since
 * a class can have many students, we can set a bound. -1 means everybody, 0 will at least include the current user <br />
 * 
 * -user: retrieve the current user updated model. Lasta activity id and result should be provided to let the server know
 * if recommendations are needed 
 * http://localhost:8080/aggregate/GetContentLevels?usr=adl01&grp=ADL&sid=TESTADL01&cid=1&mod=user&lastActivityId=jDouble1&res=0
 */
@WebServlet("/GetContentLevels")
public class GetContentLevels extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static int number_recommendation = 5;
    private boolean verbose = true;
    private long time0;
    private long time1;

    public GetContentLevels() {
        super();
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        time0 = Calendar.getInstance().getTimeInMillis();
        ConfigManager cm = new ConfigManager(this);
        verbose = cm.agg_verbose.equalsIgnoreCase("yes");
        if(verbose) System.out.println("-------------------------------------------------------");

        PrintWriter out = response.getWriter();

        String mod = request.getParameter("mod"); // all / user / class
        String grp = request.getParameter("grp"); // the class mnemonic (as
                                                  // defined in KT)
        String usr = request.getParameter("usr"); // user name
        String sid = request.getParameter("sid"); // session id
        String cid = request.getParameter("cid"); // course id (for content)

        String nmodels = request.getParameter("models"); // how many models to retrieve (-1: all, 0: only the user)
        String ntop = request.getParameter("avgtop"); // how many top students
                                                      // for the top group

        // these parameters are in the case the mod=user
        String last_content_id = request.getParameter("lastActivityId");
        String last_content_res = request.getParameter("res");
        if (last_content_id == null)
            last_content_id = "";
        if (last_content_res == null)
            last_content_res = "-1";

        // if problems to get the variables, defaults are nmodels=-1 (retrieve all), top=3
        int n = -1; // this variable controls how many models will be retrieved
        int top = 3; // this variable controls how many top students to consider
                     // in the top N group
        try {
            n = Integer.parseInt(nmodels);
        } catch (Exception e) {
            n = -1;
        }
        try {
            top = Integer.parseInt(ntop);
        } catch (Exception e) {
            top = 3;
        }
        if (cid != null && cid.length() > 0)
            try {
                int c = Integer.parseInt(cid);
            } catch (Exception e) {
                cid = "-1";
            }

        // the main object
        Aggregate aggregate;
        String output = "";
        if (mod == null || mod.length() == 0 || mod.equalsIgnoreCase("all")) {
            // this crates all structures, fill the information and computes the
            // up to date user model
            time1 = Calendar.getInstance().getTimeInMillis();
            aggregate = new Aggregate(usr, grp, cid, sid, false, cm);
            if(verbose) System.out.println("Construct Aggregate      " + (Calendar.getInstance().getTimeInMillis()-time1));
            
            
            // Get stores models for class peers
            time1 = Calendar.getInstance().getTimeInMillis();
            aggregate.fillClassLevels(null, cm.agg_include_null_users.equalsIgnoreCase("yes"));
            if(verbose) System.out.println("Get class levels         " + (Calendar.getInstance().getTimeInMillis()-time1));
            
            time1 = Calendar.getInstance().getTimeInMillis();
            aggregate.computeGroupLevels(false, top);
            if(verbose) System.out.println("Compute group levels     " + (Calendar.getInstance().getTimeInMillis()-time1));
            
            if(cm.agg_proactiverec_enabled || cm.agg_reactiverec_enabled){
                time1 = Calendar.getInstance().getTimeInMillis();
                aggregate.fillRecommendations("", "", 0); // with these parameters, only proactive recommendations are included (sequencing)
                if(verbose) System.out.println("Recommendations            " + (Calendar.getInstance().getTimeInMillis()-time1));
            }
            
            time1 = Calendar.getInstance().getTimeInMillis();
            output = aggregate.genAllJSON(n, top);
            if(verbose) System.out.println("Gen JSON                 " + (Calendar.getInstance().getTimeInMillis()-time1));


        } else if (mod.equalsIgnoreCase("user")) {
            // parameter true indicate that the user model should be constructed 
            time1 = Calendar.getInstance().getTimeInMillis();
            aggregate = new Aggregate(usr, grp, cid, sid, true, cm);
            if(verbose) System.out.println("Construct Aggregate+UM   " + (Calendar.getInstance().getTimeInMillis()-time1));
            
            time1 = Calendar.getInstance().getTimeInMillis();
            aggregate.fillClassLevels(usr, cm.agg_include_null_users.equalsIgnoreCase("yes"));
            if(verbose) System.out.println("Get class levels         " + (Calendar.getInstance().getTimeInMillis()-time1));
            
            // compute sequencing
//            if(cm.agg_sequencing.equalsIgnoreCase("yes")){
//                time1 = Calendar.getInstance().getTimeInMillis();
//                aggregate.sequenceContent();
//                if(verbose) System.out.println("Sequencing               " + (Calendar.getInstance().getTimeInMillis()-time1));
//            }
            if(cm.agg_proactiverec_enabled || cm.agg_reactiverec_enabled){
	            time1 = Calendar.getInstance().getTimeInMillis();
	            aggregate.fillRecommendations(last_content_id, last_content_res, number_recommendation);
	            if(verbose) System.out.println("Recommendations          " + (Calendar.getInstance().getTimeInMillis()-time1));
            }
            
            time1 = Calendar.getInstance().getTimeInMillis();
            aggregate.fillFeedbackForm(last_content_id, last_content_res);
            if(verbose) System.out.println("Feedback form            " + (Calendar.getInstance().getTimeInMillis()-time1));

            time1 = Calendar.getInstance().getTimeInMillis();
            output = aggregate.genUserJSON(last_content_id, last_content_res);
            if(verbose) System.out.println("Generate JSON            " + (Calendar.getInstance().getTimeInMillis()-time1));


        } else if (mod.equalsIgnoreCase("class")) {

        }
        time1 = Calendar.getInstance().getTimeInMillis();
        out.print(output);
        if(verbose) System.out.println("Printing output          " + (Calendar.getInstance().getTimeInMillis()-time1));
        if(verbose) System.out.println("TOTAL                    " + (Calendar.getInstance().getTimeInMillis()-time0));
        if(verbose) System.out.println("-------------------------------------------------------");


    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
