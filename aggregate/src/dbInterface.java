import java.sql.*; 
import java.util.*;
public class dbInterface {
	protected String dbstring;
	protected String dbuser;
	protected String dbpass;
	
	protected Connection conn;
	protected Statement stmt = null; 
	protected ResultSet rs = null;
	
	public dbInterface(String connurl, String user, String pass){
		dbstring = connurl;
		dbuser = user;
		dbpass = pass;
	}
	
	public boolean openConnection(){
		try{
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//System.out.println(dbstring+"?"+ "user="+dbuser+"&password="+dbpass);
			conn = DriverManager.getConnection(dbstring+"?"+ "user="+dbuser+"&password="+dbpass);
			if (conn!=null){
				return true;
			}
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage()); 
			System.out.println("SQLState: " + ex.getSQLState()); 
			System.out.println("VendorError: " + ex.getErrorCode());
			return false;
		}catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true; 
	}
	
	public  void closeConnection(){
		releaseStatement(stmt, rs);
		if (conn != null){
			try{
				conn.close();
			}catch (SQLException sqlEx) { } 
		}
	}
	
	
	public  void releaseStatement(Statement stmt, ResultSet rs){
		if (rs != null) {
			try { 
				rs.close();
			}catch (SQLException sqlEx) { sqlEx.printStackTrace(); } 
			rs = null;
		}
		if (stmt != null) {
			try{
				stmt.close();
			}catch (SQLException sqlEx) { sqlEx.printStackTrace(); } 
			stmt = null;
		}
	}
	
	
	
}
