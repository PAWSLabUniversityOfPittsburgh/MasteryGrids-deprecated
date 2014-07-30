import java.io.InputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class ConfigManager {
    public String agg_dbstring;
    public String agg_dbuser;
    public String agg_dbpass;
    public String agg_uminterface_classname;
    public String agg_uminterface_key;
    public String agg_recinterface_classname;
    
    
    public boolean agg_proactiverec_enabled;
    public double agg_proactiverec_threshold;
    public String agg_proactiverec_method;
    public int agg_proactiverec_max;
    public boolean agg_reactiverec_enabled;
    public double agg_reactiverec_threshold;
    public String agg_reactiverec_method;
    public int agg_reactiverec_max;
    
    public String agg_verbose;
    public String agg_include_null_users;
    private static String config_string = "./WEB-INF/config.xml";

    public ConfigManager(HttpServlet servlet) {
        try {
            ServletContext context = servlet.getServletContext();
            // System.out.println(context.getContextPath());
            InputStream input = context.getResourceAsStream(config_string);
            if (input != null) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(input);
                doc.getDocumentElement().normalize();

                agg_dbstring = doc.getElementsByTagName("agg_dbstring").item(0).getTextContent().trim().toLowerCase();
                agg_dbuser = doc.getElementsByTagName("agg_dbuser").item(0).getTextContent().trim().toLowerCase();
                agg_dbpass = doc.getElementsByTagName("agg_dbpass").item(0).getTextContent().trim().toLowerCase();
                agg_uminterface_classname = doc.getElementsByTagName("agg_uminterface_classname").item(0).getTextContent().trim();
                agg_recinterface_classname = doc.getElementsByTagName("agg_recinterface_classname").item(0).getTextContent().trim();
                agg_uminterface_key = doc.getElementsByTagName("agg_uminterface_key").item(0).getTextContent().trim();
                agg_proactiverec_enabled = doc.getElementsByTagName("agg_proactiverec_enabled").item(0).getTextContent().trim().equalsIgnoreCase("yes");
                agg_reactiverec_enabled = doc.getElementsByTagName("agg_reactiverec_enabled").item(0).getTextContent().trim().equalsIgnoreCase("yes");
                agg_proactiverec_method = doc.getElementsByTagName("agg_proactiverec_method").item(0).getTextContent().trim();
                agg_reactiverec_method = doc.getElementsByTagName("agg_reactiverec_method").item(0).getTextContent().trim();
                try{agg_reactiverec_threshold = Double.parseDouble(doc.getElementsByTagName("agg_reactiverec_threshold").item(0).getTextContent().trim());}catch(Exception e){agg_reactiverec_threshold = -1.0;}
                try{agg_proactiverec_threshold = Double.parseDouble(doc.getElementsByTagName("agg_proactiverec_threshold").item(0).getTextContent().trim());}catch(Exception e){agg_proactiverec_threshold = -1.0;}
                try{agg_reactiverec_max = Integer.parseInt(doc.getElementsByTagName("agg_reactiverec_max").item(0).getTextContent().trim());}catch(Exception e){agg_reactiverec_max = -1;}
                try{agg_proactiverec_max = Integer.parseInt(doc.getElementsByTagName("agg_proactiverec_max").item(0).getTextContent().trim());}catch(Exception e){agg_proactiverec_max = -1;}

                agg_verbose = doc.getElementsByTagName("agg_verbose").item(0).getTextContent().trim().toLowerCase();
                agg_include_null_users = doc.getElementsByTagName("agg_include_null_users").item(0).getTextContent().trim().toLowerCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
