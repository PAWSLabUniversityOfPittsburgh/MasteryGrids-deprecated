import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

public class AggregateDB extends dbInterface {
    public static DecimalFormat df4 = new DecimalFormat("#.####");

    public AggregateDB(String connurl, String user, String pass) {
        super(connurl, user, pass);
    }

    // returns the name of the grp
    public String getGrpName(String grp) {
        try {
            String res = "";
            stmt = conn.createStatement();
            String query = "select G.group_name from ent_group G where G.group_id = '"
                    + grp + "';";
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                res = rs.getString("group_name");
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            this.releaseStatement(stmt, rs);
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }

    public String getDomain(String course_id) {
        try {
            String res = "";
            stmt = conn.createStatement();
            String query = "select domain from ent_course  where course_id = '"
                    + course_id + "';";
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                res = rs.getString("domain");
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            this.releaseStatement(stmt, rs);
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }

    // returns the name of the grp
    public String getCourseId(String grp) {
        try {
            String res = "";
            stmt = conn.createStatement();
            String query = "select G.course_id from ent_group G where G.group_id = '"
                    + grp + "';";
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                res = rs.getString("course_id");
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            this.releaseStatement(stmt, rs);
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }

    public HashMap<String, String> getNonStudents(String group_id) {
        HashMap<String, String> res = new HashMap<String, String>();
        try {

            stmt = conn.createStatement();
            String query = "SELECT user_id, user_role FROM ent_non_student WHERE group_id = '"
                    + group_id + "';";

            rs = stmt.executeQuery(query);
            while (rs.next()) {
                res.put(rs.getString("user_id"), rs.getString("user_role"));
                // System.out.println(rs.getString("user_id"));
            }
            this.releaseStatement(stmt, rs);

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());

        } finally {
            this.releaseStatement(stmt, rs);

        }
        return res;
    }

    // returns the ordered list of topics of a course corresponding to the group
    // (class)
    public ArrayList<String[]> getTopicList(String course_id) {
        try {
            ArrayList<String[]> res = new ArrayList<String[]>();
            stmt = conn.createStatement();
            String query = "SELECT T.topic_name, T.display_name, T.order, T.visible FROM ent_topic T "
                    + " WHERE T.course_id = '" + course_id + "' AND T.active=1 ORDER BY T.`order` ASC;";

            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String[] topic = new String[5];
                topic[0] = rs.getString("topic_name");
                topic[1] = rs.getString("display_name");
                topic[2] = rs.getString("order");
                topic[3] = rs.getString("visible"); // visibility
                topic[4] = ""; // current / covered
                res.add(topic);
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }
    }

    public ArrayList<String> getHiddenTopics(String group_id) {
        try {
            ArrayList<String> res = new ArrayList<String>();
            stmt = conn.createStatement();
            String query = "SELECT topic_name " + " FROM ent_hidden_topics "
                    + " WHERE group_id = '" + group_id + "'";

            rs = stmt.executeQuery(query);
            while (rs.next()) {
                res.add(rs.getString("topic_name"));
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            this.releaseStatement(stmt, rs);
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return null;
        }
    }

    // returns all content for each topic -> questions, examples, readings as
    // arrays of string
    // @@@@ JG FIXED
    public ArrayList<String[]> getContentByTopic(String topic) {
        try {
            ArrayList<String[]> res = new ArrayList<String[]>();
            stmt = conn.createStatement();
            String query = "SELECT C.content_id,C.content_name,C.content_type,C.display_name,C.url, C.desc, C.comment "
                    + " FROM ent_content C, rel_topic_content TC, ent_topic T "
                    + " WHERE T.topic_name='"
                    + topic
                    + "' and T.topic_id = TC.topic_id and TC.content_id=C.content_id and C.visible = 1 and TC.visible = 1 and T.active = 1 "
                    + " ORDER by C.content_type desc, TC.display_order asc";
            rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                String[] content = new String[7];
                content[0] = rs.getString("content_id");
                content[1] = rs.getString("content_name");
                content[2] = rs.getString("content_type");
                content[3] = rs.getString("display_name");
                content[4] = rs.getString("url");
                content[5] = rs.getString("desc");
                content[6] = rs.getString("comment");
                res.add(content);
                // res.put(content);
                // System.out.println(content[0]+" "+content[2]);
                i++;
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }

    
    // now get data with resource instead of content_type
    public HashMap<String, String[]> getContent2(String course_id) {
        try {
            HashMap<String, String[]> res = new HashMap<String, String[]>();
            stmt = conn.createStatement();
            String query = "SELECT C.content_id,C.content_name,R.resource_name,C.display_name,C.url, C.desc, C.comment, C.provider_id "
                    + " FROM ent_content C, rel_topic_content TC, ent_topic T, ent_resource R "
                    + " WHERE T.course_id='"
                    + course_id
                    + "' and T.topic_id=TC.topic_id and TC.content_id=C.content_id and C.visible = 1 and TC.visible = 1 and T.active = 1 "
                    + " and R.resource_id = TC.resource_id "
                    + " ORDER by T.`order`, R.`order` desc, TC.display_order asc";
            rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                String[] content = new String[6];
                String content_name = rs.getString("content_name");
                content[0] = rs.getString("resource_name");
                content[1] = rs.getString("display_name");
                content[2] = rs.getString("url");
                content[3] = rs.getString("desc");
                content[4] = rs.getString("comment");
                content[5] = rs.getString("provider_id");
                res.put(content_name, content);
                //System.out.println(content_name + " " + content[0]+" "+content[2]);
                i++;
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }


    // Now with resource instead of content_type
    // @@@@ #### TEST!!
    public HashMap<String, ArrayList<String>[]> getTopicContent2(String course_id, HashMap<String, Integer> resourceMap) {
        try {
            int n = resourceMap.size();

            HashMap<String, ArrayList<String>[]> res = new HashMap<String, ArrayList<String>[]>();
            stmt = conn.createStatement();
            String query = "SELECT T.topic_name, group_concat(C.content_name , ',' , R.resource_name order by C.content_type, TC.display_order separator ';') as content "
                    + "FROM ent_topic T, rel_topic_content TC, ent_content C, ent_resource R   "
                    + "WHERE T.course_id = '"
                    + course_id
                    + "' "
                    + "and T.active=1 "
                    + "and TC.topic_id=T.topic_id and C.content_id = TC.content_id and R.resource_id = TC.resource_id "
                    + "group by T.topic_id";
            
            //System.out.println("query:");
            //System.out.println(query);
            //System.out.println();
            rs = stmt.executeQuery(query);
            String topic = "";

            while (rs.next()) {
                topic = rs.getString("topic_name");
                String allcontent = rs.getString("content");
                //System.out.println(" "+topic+" : ");
                ArrayList<String>[] all_content = new ArrayList[n];
                for(int i = 0;i<n;i++){
                    all_content[i] = new ArrayList<String>();
                }

                if (allcontent == null || allcontent.equalsIgnoreCase("[null]")
                        || allcontent.length() == 0) {
                    //
                } else {
                    String[] content = allcontent.split(";");
                    for (int i = 0; i < content.length; i++) {
                        String[] item = content[i].split(",");
                        int resourceIndex = resourceMap.get(item[1]);
                        if (resourceIndex>=0 && resourceIndex<n) all_content[resourceIndex].add(item[0]);
                    }
                }
                res.put(topic, all_content);
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }


    public void insertPrecomputedModel(String user, String course_id,
            String group_id, String sid, String model4topics,
            String model4content) {
        try {
            stmt = conn.createStatement();
            String query = "INSERT INTO ent_precomputed_models (user_id,course_id,group_id,session_id,computedon,model4topics,model4content) values ('"
                    + user
                    + "',"
                    + course_id
                    + ",'"
                    + group_id
                    + "','"
                    + sid
                    + "',now(),'"
                    + model4topics
                    + "','"
                    + model4content
                    + "');";
            // System.out.println(query);
            if (stmt.execute(query)) {
                // ;
            }
            this.releaseStatement(stmt, rs);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }

    public void updatePrecomputedModel(String user, String course_id,
            String group_id, String sid, String model4topics,
            String model4content) {
        try {
            stmt = conn.createStatement();
            String query = "UPDATE ent_precomputed_models SET model4topics='"
                    + model4topics + "', model4content='" + model4content
                    + "', computedon=now() WHERE user_id = '" + user
                    + "' and course_id='" + course_id + "' and group_id = '"
                    + group_id + "' and session_id = '" + sid + "';";
            // System.out.println(query);
            if (stmt.execute(query)) {
                // ;
            }
            this.releaseStatement(stmt, rs);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
        } finally {
            this.releaseStatement(stmt, rs);
        }

    }

    public boolean existPrecomputedModelForSession(String user,
            String course_id, String group_id, String sid) {
        int n = 0;
        try {
            stmt = conn.createStatement();
            String query = "SELECT count(*) as npm "
                    + "FROM ent_precomputed_models  " + "WHERE user_id='" + user
                    + "' and group_id='" + group_id + "' and course_id='"
                    + course_id + "' and session_id='" + sid + "';";
            // System.out.println(query);
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                n = rs.getInt("npm");
            }
            this.releaseStatement(stmt, rs);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
        } finally {
            this.releaseStatement(stmt, rs);
        }
        return n > 0;
    }

    // give usr == null or usr == "" to look for all precomputed models within a course
    public HashMap<String, String[]> getPrecomputedModels(String course_id, String usr) {
        try {
            HashMap<String, String[]> res = new HashMap<String, String[]>();
            stmt = conn.createStatement();
            String query = "SELECT user_id,model4topics,model4content FROM ent_precomputed_models WHERE id in "
                    + "(select max(id) from ent_precomputed_models where course_id='"
                    + course_id + "'";
            if (usr != null) query += " and user_id = '"+usr+"' ";
            query += " group by user_id);";
            rs = stmt.executeQuery(query);
            //System.out.println(query);
            String user = "";
            String[] models;
            while (rs.next()) {
                user = rs.getString("user_id");
                models = new String[2];
                models[0] = rs.getString("model4topics");
                models[1] = rs.getString("model4content");
                res.put(user, models);
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        } finally {
            this.releaseStatement(stmt, rs);
        }
    }

    public boolean precomputedModelExist(String course_id, String usr) {
        try {
            HashMap<String, String[]> res = new HashMap<String, String[]>();
            stmt = conn.createStatement();
            String query = "SELECT count(id) as npm FROM ent_precomputed_models WHERE course_id='"
                    + course_id + "' and user_id = '" + usr + "';";
            rs = stmt.executeQuery(query);
            // System.out.println(query);
            int npm = 0;
            String[] models;
            while (rs.next()) {
                npm = rs.getInt("npm");
            }
            this.releaseStatement(stmt, rs);
            return (npm>0);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return false;
        } finally {
            this.releaseStatement(stmt, rs);
        }
    }


    public boolean insertUsrFeedback(String usr, String grp, String sid,
            String srcActivityId, String srcActivityRes, String fbId,
            String fbItemIds, String responses, String recId) {
        String query = "";
        String[] fbItemArray = { "" };
        String[] resArray = { "" };
        // System.out.println(responses);
        try {
            stmt = conn.createStatement();
            if (fbItemIds != null && fbItemIds.length() != 0) {
                fbItemArray = fbItemIds.split("\\|");
                resArray = responses.split("\\|");
                if (fbItemArray.length != resArray.length) {
                    //
                }
            }
            for (int i = 0; i < fbItemArray.length; i++) {
                query = "INSERT INTO ent_user_feedback (user_id,session_id,group_id,src_content_name, src_content_res, fb_id, fb_item_id, fb_response_value, item_rec_id, datentime) values ('"
                        + usr
                        + "','"
                        + sid
                        + "','"
                        + grp
                        + "','"
                        + srcActivityId
                        + "','"
                        + srcActivityRes
                        + "','"
                        + fbId
                        + "','"
                        + fbItemArray[i]
                        + "','"
                        + resArray[i]
                        + "','"
                        + recId + "'," + "now());";
                // System.out.println(query);
                stmt.executeUpdate(query);
            }

            // System.out.println(System.nanoTime()/1000);
            this.releaseStatement(stmt, rs);
            return true;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.out.println(query);
            releaseStatement(stmt, rs);
            return false;
        }
    }

    public boolean insertTrackAction(String usr, String grp, String sid,
            String action, String comment) {
        String query = "";
        try {
            stmt = conn.createStatement();
            query = "INSERT INTO ent_tracking (datentime, user_id, session_id, group_id, action, comment) values ("
                    + "now(), '"
                    + usr
                    + "','"
                    + sid
                    + "','"
                    + grp
                    + "','"
                    + action + "','" + comment + "');";

            stmt.executeUpdate(query);
            // System.out.println(query);
            this.releaseStatement(stmt, rs);
            // System.out.println(query);
            return true;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            releaseStatement(stmt, rs);
            return false;
        }
    }
    
    
    
    // resource name , resource display name,desc,visible,update_state_on 
    // Example:  qz , question, "this is the description" , 1, 101
    // update_state_on: digits represent in order the options for updating the user model: 
    //          1: activity done, 2: in window close, and 3: window close if activity done. 
    //          For example 010 will update UM when the content window is closed.
    public ArrayList<String[]> getResourceList(String course_id) {
        try {
            ArrayList<String[]> res = new ArrayList<String[]>();
            stmt = conn.createStatement();
            String query = "select resource_name,display_name,`desc`,visible,update_state_on,`order` from ent_resource " +
            		   "where course_id=\'"+course_id+"\' order by `order`;";
            rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                String[] resource = new String[6];
                resource[0] = rs.getString("resource_name");
                resource[1] = rs.getString("display_name");
                resource[2] = rs.getString("desc");
                resource[3] = rs.getString("visible");
                resource[4] = rs.getString("update_state_on");
                resource[5] = rs.getString("order");
                res.add(resource);
                //System.out.println(resource[0]+" | "+resource[1]+" | "+resource[2]+" | "+resource[3]+" | "+resource[4]);
                i++;
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        }

    }   
    
    public ArrayList<String[]> getSubGroups(String group_id) {
        try {
            ArrayList<String[]> res = new ArrayList<String[]>();
            stmt = conn.createStatement();
            String query = "SELECT subgroup_name,subgroup_users from ent_subgroups " +
            		   "where group_id=\'"+group_id+"\';";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String[] subgroup = new String[2];
                subgroup[0] = rs.getString("subgroup_name");
                subgroup[1] = rs.getString("subgroup_users");
                res.add(subgroup);
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        }

    }
    
    public ArrayList<String[]> getParameters(String user_id, String group_id) {
        try {
            ArrayList<String[]> res = new ArrayList<String[]>();
            stmt = conn.createStatement();
            String query = "SELECT level, params_vis, params_svcs from ent_parameters WHERE " +
            		   " (group_id=\'"+group_id+"\' AND level='group') OR (user_id=\'"+user_id+"\' AND group_id=\'"+group_id+"\') ;";
            // (user_id='dguerra' AND group_id='ADL') or ((isnull(user_id) or user_id='') AND group_id='ADL')
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String[] parameters = new String[4];
                parameters[0] = rs.getString("level");
                parameters[1] = rs.getString("params_vis");
                parameters[2] = rs.getString("params_svcs");
                res.add(parameters);
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        }

    }
    
    public String[] getTimeLine(String group_id) {
        try {
            String[] res = new String[2];
            stmt = conn.createStatement();
            String query = "SELECT currentTopics, coveredTopics FROM ent_timeline WHERE " +
            		   " group_id=\'"+group_id+"\' ;";
            // (user_id='dguerra' AND group_id='ADL') or ((isnull(user_id) or user_id='') AND group_id='ADL')
            rs = stmt.executeQuery(query);
            while (rs.next()) {
            	res[0] = rs.getString("currentTopics");
            	res[1] = rs.getString("coveredTopics");
            }
            this.releaseStatement(stmt, rs);
            return res;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            this.releaseStatement(stmt, rs);
            return null;
        }

    }
    
}
