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
	
	private static String config_string = "./WEB-INF/config.xml";
	
	public ConfigManager(HttpServlet servlet){
		try{
			ServletContext context = servlet.getServletContext();
			//System.out.println(context.getContextPath());
			InputStream input = context.getResourceAsStream(config_string);
			if (input != null){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(input);
				doc.getDocumentElement().normalize();

				agg_dbstring = doc.getElementsByTagName("agg_dbstring").item(0).getTextContent();
				agg_dbuser = doc.getElementsByTagName("agg_dbuser").item(0).getTextContent();
				agg_dbpass = doc.getElementsByTagName("agg_dbpass").item(0).getTextContent();	
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
