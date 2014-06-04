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
    public String agg_sequencing;
    public double agg_sequencing_threshold;
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

                agg_dbstring = doc.getElementsByTagName("agg_dbstring").item(0)
                        .getTextContent().trim().toLowerCase();
                agg_dbuser = doc.getElementsByTagName("agg_dbuser").item(0)
                        .getTextContent().trim().toLowerCase();
                agg_dbpass = doc.getElementsByTagName("agg_dbpass").item(0)
                        .getTextContent().trim().toLowerCase();
                agg_uminterface_classname = doc
                        .getElementsByTagName("agg_uminterface_classname")
                        .item(0).getTextContent().trim();
                agg_uminterface_key = doc
                        .getElementsByTagName("agg_uminterface_key").item(0)
                        .getTextContent().trim();
                agg_sequencing = doc.getElementsByTagName("agg_sequencing")
                        .item(0).getTextContent().trim();
                try{
                    agg_sequencing_threshold = Double.parseDouble(doc.getElementsByTagName("agg_sequencing_threshold")
                            .item(0).getTextContent().trim());                    
                }catch(Exception e){
                    agg_sequencing_threshold = 1.0;
                }
                agg_verbose = doc.getElementsByTagName("agg_verbose").item(0)
                        .getTextContent().trim().toLowerCase();
                agg_include_null_users = doc
                        .getElementsByTagName("agg_include_null_users").item(0)
                        .getTextContent().trim().toLowerCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
