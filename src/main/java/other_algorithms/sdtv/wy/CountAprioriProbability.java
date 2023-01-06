package other_algorithms.sdtv.wy;

import gilp.rdf3x.RDF3XEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Initializes the database, put the result in the postgreSQL,
 * using the rdf3x database, all elements are 3-tuples.
 * 
 * @author Heiko
 *
 */
public class CountAprioriProbability {
	private static int chunkSize = 100000;


	public static HashSet<String> getAllResources() {
		String sql = "select distinct ?a where{ {?a ?b ?c.} union {?e ?f ?a.} "
				+ "Filter(?b != <rdftype> && ?f != <rdftype> && ?b != <wikiPageDisambiguates> && ?f != <wikiPageDisambiguates>)} ";
		HashSet<String> allResources = new RDF3XEngine().getDistinctEntity(sql);	
//		for(String ke:allResources)
//			System.out.println(ke+"\n");
		return allResources;
	}

	public static HashMap<String, Double> getrdftypeCount() {
		String countSql = "select count ?b where{?a <rdftype> ?b.} ";// count the type, ?a is the resources
		HashMap<String, Double> rdftypeCount = new RDF3XEngine().getCountSingleEntity(countSql);		
		return rdftypeCount;
	}

	public static void loadAllResources(){

		System.out.println("Computing global type distribution");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;

	//	System.out.println("if we succeed in connecting the database:" + conn);

		Util.removeTableIfExisting("stat_resource");
		String createStatement = "CREATE TABLE stat_resource (resource VARCHAR(1000) UNIQUE NOT NULL)";
		try {
			stmt = conn.createStatement();
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating global resource count table");
			e.printStackTrace();
		}

		HashSet<String> allResources = getAllResources();
		double stat_resource = allResources.size();

		String insertWhole = "INSERT INTO stat_resource SELECT distinct resource FROM "
				+ "(SELECT subject AS resource FROM dbpedia_properties UNION SELECT object AS resource FROM dbpedia_properties) AS resource";
		System.out.println("\n insertWhole \n" + insertWhole);


		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing insert");
			e.printStackTrace();
		}

		long count = 0;
		long errors = 0;

			for(String line :allResources) {
			boolean insert = false;
			String sqlInsert = "INSERT INTO stat_resource VALUES(";
			//String line = allResources.iterator().next();

			sqlInsert += "'" + line + "')";
			insert = true;

			if (insert) {
				try {
					stmt.addBatch(sqlInsert);
				} catch (SQLException e) {
					System.out.println("Error: could not add to batch");
					e.printStackTrace();
				}
				count++;
				if (count % chunkSize == 0) {
					try {
						int[] results = stmt.executeBatch();
						for (int i : results)
							if (i == 0)
								errors++;
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		try {
			int[] results = stmt.executeBatch();
			for (int i : results)
				if (i == 0)
					errors++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("stat_resource assertions: load finished. \n");

		System.out.println("stat_resource assertions: load finished. Loaded " + count + " statements, encountered "
				+ errors + " error(s).");
		Util.checkTable("stat_resource");

//-------------------------------------------------------
		Util.removeTableIfExisting("stat_type_count");
		 createStatement = "CREATE TABLE stat_type_count (type VARCHAR(1000) NOT NULL, type_count FLOAT NOT NULL);";
		try {
			stmt = conn.createStatement();
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating global type count table");
			e.printStackTrace();
		}

		// String insertStatement = "INSERT INTO stat_type_count SELECT
		// type,COUNT(resource) FROM dbpedia_types GROUP BY (type);";

		HashMap<String, Double> rdftypeCount = getrdftypeCount();

		long count1 = 0;
		long errors1 = 0;

		HashMap<String, Double> stat_type_apriori_probability = new HashMap<String, Double>();

		for (String key : rdftypeCount.keySet()) {
			// boolean insert = false;
			String sqlInsert = "INSERT INTO stat_type_count VALUES(";
			double valueCount = rdftypeCount.get(key);

			double probability = 1.0 * valueCount / stat_resource;

			stat_type_apriori_probability.put(key, probability);

			sqlInsert += "'" + key + "',";
			sqlInsert += "'" +  valueCount + "')";
			try {
				stmt.addBatch(sqlInsert);
			} catch (SQLException e) {
				System.out.println("Error: could not add to batch");
				e.printStackTrace();
			}
			count1++;
			if (count1 % chunkSize == 0) {
				try {
					int[] results = stmt.executeBatch();
					for (int i : results)
						if (i == 0)
							errors1++;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			int[] results = stmt.executeBatch();
			for (int i : results)
				if (i == 0)
					errors1++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("stat_type_count assertions: load finished. \n");

		System.out.println("stat_type_count assertions: load finished. Loaded " + count1 + " statements, encountered "
				+ errors1 + " error(s).");
		Util.createIndex("stat_type_count", "type");
		Util.checkTable("stat_type_count");
//----------------------------------------------------------
		Util.removeTableIfExisting("stat_type_apriori_probability");
		createStatement = "CREATE TABLE stat_type_apriori_probability (type VARCHAR(1000) NOT NULL, probability FLOAT NOT NULL);";
		try {
			stmt = conn.createStatement();
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating type apriori probability table");
			e.printStackTrace();
		}
		Util.createIndex("stat_type_apriori_probability", "type");

		// insertStatement = "INSERT INTO stat_type_apriori_probability select
		// type,1.0*type_count/(select count(resource) from stat_resource) AS rel_count
		// from stat_type_count;";
		long count2 = 0;
		long errors2 = 0;
		for (String key : stat_type_apriori_probability.keySet()) {
			// boolean insert = false;
			String sqlInsert = "INSERT INTO stat_type_apriori_probability VALUES(";
			double probability = stat_type_apriori_probability.get(key);

			sqlInsert += "'" + key + "',";
			sqlInsert += "'" + Double.toString(probability) + "')";
			try {
				stmt.addBatch(sqlInsert);
			} catch (SQLException e) {
				System.out.println("Error: could not add to batch");
				e.printStackTrace();
			}
			count2++;
			if (count2 % chunkSize == 0) {
				try {
					int[] results = stmt.executeBatch();
					for (int i : results)
						if (i == 0)
							errors2++;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			int[] results = stmt.executeBatch();
			for (int i : results)
				if (i == 0)
					errors2++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("stat_type_apriori_probability assertions: load finished. \n");

		Util.checkTable("stat_type_apriori_probability");

	}

	public void computeSDTypes() {
		System.out.println("Computing SDTypes");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing SDTypes");
			e.printStackTrace();
		}
		String create = "";
		String insert = "";
//----------------------------------------------

		create = "CREATE  TABLE dbpedia_untyped_instance (resource VARCHAR(1000))";
		Util.removeTableIfExisting("dbpedia_untyped_instance");
		try {
			stmt.execute(create);
		} catch (SQLException e) {
			System.out.println("Error creating untyped instance table");
			e.printStackTrace();
		}

		insert = "INSERT INTO dbpedia_untyped_instance SELECT resource"
				+ " FROM stat_resource EXCEPT (SELECT resource FROM dbpedia_types UNION SELECT resource FROM dbpedia_disambiguations)";
		// MINUS is supported by oracle, not the sql and postgresql. except is
		// equivalent to the minus in the postgresql.

		String sql="select distinct ?a where{{?a <rdftype> ?b.} union {?a <wikiPageDisambiguates> ?c.}} ";
		HashSet<String> allTypeResources = new RDF3XEngine().getDistinctEntity(sql);

		String sql1 = "select distinct ?a where{ {?a ?b ?c.} union {?e ?f ?a.} "
				+ "Filter(?b != <rdftype> && ?f != <rdftype> && ?b != <wikiPageDisambiguates> && ?f != <wikiPageDisambiguates>)} ";
		HashSet<String> allResources = new RDF3XEngine().getDistinctEntity(sql1);

		allResources.removeAll(allTypeResources);
		long count = 0;
		for(String line :allResources) {
			boolean insert1 = false;
			String sqlInsert = "INSERT INTO stat_resource VALUES(";
			//String line = allResources.iterator().next();

			sqlInsert += "'" + line + "')";
			insert1 = true;

			if (insert1) {
				try {
					stmt.addBatch(sqlInsert);
				} catch (SQLException e) {
					System.out.println("Error: could not add to batch");
					e.printStackTrace();
				}
				count++;
				if (count % chunkSize == 0) {
					try {
						int[] results = stmt.executeBatch();

					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		try {
			int[] results = stmt.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		try {
//			stmt.execute(insert);
//		} catch (SQLException e) {
//			System.out.println("Error inserting into untyped instance table");
//			e.printStackTrace();
//		}
		Util.createIndex("dbpedia_untyped_instance", "resource");
		Util.checkTable("dbpedia_untyped_instance");
	}

}
