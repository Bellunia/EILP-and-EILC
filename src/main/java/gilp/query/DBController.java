package gilp.query;

import MLNs.util.Bundle;
import MLNs.util.PostgreNotStartedException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;

public class DBController {
  //  private final static Logger logger = LogManager.getLogger(DBController.class);

    public static Connection conn = null;
    public static Statement st = null;

    public static void getConn() {

        String db = Bundle.getString("gilp_database");

        String host = Bundle.getString("pgsql_url");

        String url = "jdbc:postgresql://" + host + "/" + db;
        String user = Bundle.getString("pgsql_username");
        String password = Bundle.getString("pgsql_password");

        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.createStatement();

        } catch (SQLException ex) {
         //   logger.fatal(ex.getMessage() + "\n\n" + "Maybe PostgreSQL was not started?" + "\n");
            throw new PostgreNotStartedException();
        }
    }
    //--------------------------------------------------------------

    public static boolean exec_update(String sql) {
        try {
            getConn();
            conn.createStatement().executeUpdate(sql);
            conn.close();
            return true;

        } catch (Exception e) {
            //GILPSettings.log( "Error in DBController when executing: " + sql);
            e.printStackTrace(System.out);
            return false;
        }
    }

    public ArrayList<Object> exec_query(String sql) {

        ArrayList<Object> hotelResultList = null;

        try {
            getConn();
            ResultSet results = st.executeQuery(sql);

            //Stores properties of a ResultSet object, including column count
            ResultSetMetaData rsmd = results.getMetaData();
            int columnCount = rsmd.getColumnCount();

            hotelResultList = new ArrayList<>(columnCount);
            while (results.next()) {
                int i = 1;
                while (i <= columnCount) {
                    hotelResultList.add(results.getString(i++));
                }
            }

            results.close();


        } catch (SQLException ex) {
           // logger.warn(ex.getMessage(), ex);
        }
        return hotelResultList;
    }

    public ResultSet execQuery(String sql) {

        ResultSet results =  null;

        try {
            getConn();
             results = st.executeQuery(sql);
            st.close();


        } catch (SQLException ex) {
         //   logger.warn(ex.getMessage(), ex);
        }
        return results;
    }


    public static String getSingleValue(String qry) {
        ArrayList<ArrayList<String>> rlts = getTuples(qry);
        if (rlts == null)
            return null;
        if (rlts.size() == 0)
            return null;
        return rlts.get(0).get(0);
    }

    public static ArrayList<ArrayList<String>> getTuples(String qry) {

//        Connection con = DBPool.getConnection();
    //    PreparedStatement pstmt = null;
        ArrayList<ArrayList<String>> rlts = new ArrayList<>();
        ResultSet rs = null;
        try {
            getConn();
         //   pstmt = conn.prepareStatement(qry);
            rs = st.executeQuery(qry);
            int num = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                ArrayList<String> tuple = new ArrayList<>();
                for (int i = 1; i <= num; i++) {
                    tuple.add(rs.getString(i));
                }
                rlts.add(tuple);
            }
        } catch (Exception ex) {
            //logger.log("DBController.getTuples: " + ex.getMessage());
            //logger.log("Query: " + qry);
            return null;
        } finally {
          close();
         //  DBPool.closeAll(conn, pstmt, rs);
        }
        return rlts;
    }

    public static void close() {
        try {

            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }

        } catch (SQLException ex) {
            //logger.warn(ex.getMessage(), ex);
        }
    }


}
