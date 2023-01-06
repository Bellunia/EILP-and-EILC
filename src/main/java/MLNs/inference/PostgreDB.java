package MLNs.inference;

import  MLNs.util.Bundle;
import  MLNs.util.PostgreNotStartedException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class PostgreDB {

	private final static Logger logger = LogManager.getLogger(PostgreDB.class);
	private Connection con = null;
	private Statement st = null;

	public PostgreDB() {
		super();
	}

	public void connect() {

		String db = Bundle.getString("pgsql_database");

		String host = Bundle.getString("pgsql_url");

		String url = "jdbc:postgresql://" + host + "/"+db;
		String user = Bundle.getString("pgsql_username");
		String password = Bundle.getString("pgsql_password");

	//	String url = "jdbc:postgresql://127.0.0.1:5432/probkb";
	//	String user = "postgres";
	//	String password = "123456";

		try {
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();

		} catch (SQLException ex) {
			logger.fatal(ex.getMessage() + "\n\n"
					+ "Maybe PostgreSQL was not started?" + "\n"
					+ "Open a console and run:" + "\n" + "\tsh pgsql-start.sh"
					+ "\n");
			throw new PostgreNotStartedException();
		}

	}

	/**
	 * A factor graph is composed by factors connected with one, two, or three
	 * clauses (i.e., relationships).
	 * 
	 * @param n
	 *            size of the restriction, i.e. number of clauses (1, 2, 3).
	 * @return
	 */
	public ResultSet factors(int n) {

		try {

			switch (n) {
			case 1:
				// one...
				return st
						.executeQuery("select rs1.rel as r1, rs1.ent1 as a1, rs1.ent2 as b1, "
								+ "f.weight from probkb.relationships as rs1, probkb.factors as f "
								+ "where f.id1 = rs1.id and f.id2 is null and f.id3 is null;");
			case 2:
				// two...
				return st
						.executeQuery("select rs1.rel as r1, rs1.ent1 as a1, rs1.ent2 as b1, "
								+ "rs2.rel as r2, rs2.ent1 as a2, rs2.ent2 as b2, "
								+ "f.weight from probkb.relationships as rs1, "
								+ "probkb.relationships as rs2, probkb.factors as f "
								+ "where f.id1 = rs1.id and f.id2 = rs2.id and f.id3 is null;");
			case 3:
				// three...
				return st
						.executeQuery("select rs1.rel as r1, rs1.ent1 as a1, rs1.ent2 as b1, "
								+ "rs2.rel as r2, rs2.ent1 as a2, rs2.ent2 as b2, "
								+ "rs3.rel as r3, rs3.ent1 as a3, rs3.ent2 as b3, "
								+ "f.weight from probkb.relationships as rs1, "
								+ "probkb.relationships as rs2, probkb.relationships as rs3, "
								+ "probkb.factors as f "
								+ "where f.id1 = rs1.id and f.id2 = rs2.id and f.id3 = rs3.id;");
			}
		} catch (SQLException ex) {
			logger.warn(ex.getMessage(), ex);
		}

		return null;

	}

	public void close() {
		try {

			if (st != null) {
				st.close();
			}
			if (con != null) {
				con.close();
			}

		} catch (SQLException ex) {
			logger.warn(ex.getMessage(), ex);
		}
	}

	public ResultSet evidence(int aimNumber) {
		
		ResultSet rs = null;
		try {
			 rs = st.executeQuery("select rel, ent1, ent2 from probkb.relationships where rel = "
					 + aimNumber + ";");

		} catch (SQLException ex) {
			logger.warn(ex.getMessage(), ex);
		}
		return rs;
		
	}
	
	public ResultSet evidence() {

		ResultSet rs = null;
		try {
			rs = st.executeQuery("select rel, ent1, ent2 from probkb.extractions;");

		} catch (SQLException ex) {
			logger.warn(ex.getMessage(), ex);
		}
		return rs;
	}

	public boolean exec_update(String sql) {
		try {
			con.createStatement().executeUpdate(sql);
			con.close();
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
			logger.warn(ex.getMessage(), ex);
		}
		return hotelResultList;
	}



}
