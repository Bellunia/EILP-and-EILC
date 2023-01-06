package other_algorithms.sdtv.wy;

import gilp.rdf3x.RDF3XEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ComputePerPredicateDistribution {

	private int chunkSize = 100000;

	public void computePerPredicateDistribution() {// String predicateName

		String sql = "select distinct ?p where{ ?a ?p ?c.filter(?p != <rdftype> && ?p != <wikiPageDisambiguates>)} order by ?p";
		HashSet<String> predicateNames = new RDF3XEngine().getDistinctEntity(sql);

		System.out.println("\n size:" + predicateNames.size());

		System.out.println("Computing distribution per predicate");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;

		Util.removeTableIfExisting("stat_resource_predicate_tf");
		String createStatement = "CREATE TABLE stat_resource_predicate_tf (resource VARCHAR(1000), predicate VARCHAR(1000), tf FLOAT, outin FLOAT)";
		try {
			stmt = conn.createStatement();
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating resource predicate count table");
			e.printStackTrace();
		}

		int j = 0;
		for (String predicateName : predicateNames) {
			System.out.println("\n name:" + predicateName);

			System.out.println("\n j:" + j++);

			// String insert1 = "INSERT INTO stat_resource_predicate_tf "
			// + "SELECT subject, predicate, COUNT(object),0 FROM dbpedia_properties GROUP
			// BY subject, predicate;";
//		String insert2 = "INSERT INTO stat_resource_predicate_tf "
//				+ "SELECT object, predicate, COUNT(subject),1 FROM dbpedia_properties GROUP BY object, predicate;";

			String insert1 = "select count ?a where{ ?a <" + predicateName + "> ?c.}";
			System.out.print(insert1 + "\n");
			HashMap<String, Double> subjectsResources = new RDF3XEngine().getCountSingleEntity(insert1);

			String insert2 = "select count ?c where{ ?a <" + predicateName + "> ?c. } ";
			System.out.print(insert2 + "\n");

			HashMap<String, Double> objectsResources = new RDF3XEngine().getCountSingleEntity(insert2);

			long count = 0;
			long errors = 0;
			long counts = 0;

			for (String subject : subjectsResources.keySet()) {
				boolean insert = false;
				String sqlInsert = "INSERT INTO stat_resource_predicate_tf VALUES(";

				sqlInsert += "'" + subject + "',";
				sqlInsert += "'" + predicateName + "',";
				sqlInsert += "'" + subjectsResources.get(subject) + "',";

				sqlInsert += "'" + 0 + "')";
				insert = true;

				if (insert) {
					try {
						stmt.addBatch(sqlInsert);
					} catch (SQLException e) {
						System.out.println("Error: could not add to batch");
						e.printStackTrace();
					}
					counts++;
					if (counts % chunkSize == 0) {
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

			for (String object : objectsResources.keySet()) {
				boolean insert = false;
				String sqlInsert = "INSERT INTO stat_resource_predicate_tf VALUES(";

				sqlInsert += "'" + object + "',";

				sqlInsert += "'" + predicateName + "',";
				sqlInsert += "'" + objectsResources.get(object) + "',";

				sqlInsert += "'" + 1 + "')";
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

			System.out.println("Property assertions: load finished. Loaded " + count + " statements, encountered "
					+ errors + " error(s).");
		}

		Util.createIndex("stat_resource_predicate_tf", "resource");
		Util.createIndex("stat_resource_predicate_tf", "predicate");
		Util.checkTable("stat_resource_predicate_tf");

// **************************************************************************************************

		Util.removeTableIfExisting("stat_type_predicate_percentage");
		 createStatement = "CREATE TABLE stat_type_predicate_percentage (type VARCHAR(1000), predicate VARCHAR(1000), outin FLOAT, percentage FLOAT)";
		try {
			stmt = conn.createStatement();
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating type predicate distribution table");
			e.printStackTrace();
		}

		// -------------------------------
		String insert1 = "INSERT INTO stat_type_predicate_percentage "
				+ "SELECT types.type, res.predicate, 0, 1.0*COUNT(subject)/(SELECT COUNT(subject) "
				+ "FROM dbpedia_properties AS resinner WHERE res.predicate = resinner.predicate) FROM dbpedia_properties AS res, "
				+ "dbpedia_types AS types WHERE res.subject = types.resource GROUP BY res.predicate,types.type";
		String insert2 = "INSERT INTO stat_type_predicate_percentage SELECT types.type, res.predicate, 1, "
				+ "1.0*COUNT(object)/(SELECT COUNT(object) FROM dbpedia_properties AS resinner WHERE res.predicate = resinner.predicate)"
				+ " FROM dbpedia_properties AS res, dbpedia_types AS types WHERE res.object = types.resource GROUP BY res.predicate,types.type;";

		// ------------------------------------------
		int k = 0;
		for (String predicateName : predicateNames) {
			System.out.println("\n name:" + predicateName);

			System.out.println("\n k:" + k++ + "\n");

			String countSql1 = "select count ?p where{?a ?p ?c. filter(?p = <" + predicateName + ">)}";
			HashMap<String, Double> PredicateNumber = new RDF3XEngine().getCountSingleEntity(countSql1);
			double predicateCount = PredicateNumber.get(predicateName);

			String sql1 = "select count ?b where{?a <rdftype> ?b. ?a <" + predicateName + "> ?c.}";
			HashMap<String, Double> PredicateBySubject = new RDF3XEngine().getCountSingleEntity(sql1);

			long count2 = 0;
			long errors1 = 0;

			for (String type : PredicateBySubject.keySet()) {
				boolean insert = false;
				String sqlInsert = "INSERT INTO stat_type_predicate_percentage VALUES(";

				sqlInsert += "'" + type + "',";
				sqlInsert += "'" + predicateName + "',";
				sqlInsert += "'" + 0 + "',";
				double value = 1.0 * PredicateBySubject.get(type) / predicateCount;

				sqlInsert += "'" + value + "')";
				insert = true;

				if (insert) {
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
									errors1++;
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
						errors1++;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String sql2 = "select count ?b where{?a <rdftype> ?b. ?c <" + predicateName + "> ?a.}";
			HashMap<String, Double> PredicateByObject = new RDF3XEngine().getCountSingleEntity(sql2);


			long count1 = 0;
			for (String type : PredicateByObject.keySet()) {

				double value = 1.0 * PredicateByObject.get(type) / predicateCount;

				boolean insert = false;
				String sqlInsert = "INSERT INTO stat_type_predicate_percentage VALUES(";

				sqlInsert += "'" + type + "',";
				sqlInsert += "'" + predicateName + "',";
				sqlInsert += "'" + 1 + "',";
				sqlInsert += "'" + value + "')";

				insert = true;

				if (insert) {
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

			System.out.println("stat_resource assertions: load finished. \n" + errors1);
		}
		Util.createIndex("stat_type_predicate_percentage", "type");
		Util.createIndex("stat_type_predicate_percentage", "predicate");
		Util.checkTable("stat_type_predicate_percentage");

//-------------------------------------------------
	}

	public void computePerPredicateWeightApriori(Double allResourcesSize) {
		System.out.println("Computing predicate_weight_apriori");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;

		Util.removeTableIfExisting("stat_predicate_weight_apriori");
		String createStatement = "CREATE TABLE stat_predicate_weight_apriori (predicate VARCHAR(1000), outin INT, weight FLOAT)";
		try {
			stmt = conn.createStatement();
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating predicate weight table");
			e.printStackTrace();
		}

		String insertSql = "INSERT INTO stat_predicate_weight_apriori SELECT predicate,outin,"
				+ "SUM((percentage - probability)*(percentage - probability)) FROM stat_type_predicate_percentage "
				+ "LEFT JOIN stat_type_apriori_probability ON stat_type_predicate_percentage.type = stat_type_apriori_probability.type "
				+ "GROUP BY predicate,outin";

		String countSql = "select count ?b where{?a <rdftype> ?b.} ";// count the type, ?a is the resources
		HashMap<String, Double> rdftypeCount = new RDF3XEngine().getCountSingleEntity(countSql);
		// HashMap<String, Double> rdftypeCount = new CountAprioriProbability().
		// getrdftypeCount();

		HashMap<String, Double> stat_type_apriori_probability = new HashMap<String, Double>();

		for (String key : rdftypeCount.keySet()) {
			// boolean insert = false;

			double valueCount = rdftypeCount.get(key);

			double probability = 1.0 * valueCount / allResourcesSize;// stat_resource;

			stat_type_apriori_probability.put(key, probability);

		}

		String sql = "select distinct ?p where{ ?a ?p ?c.filter(?p != <rdftype> && ?p != <wikiPageDisambiguates>)} order by ?p";
		HashSet<String> predicateNames = new RDF3XEngine().getDistinctEntity(sql);

		System.out.println("\n size:" + predicateNames.size());

		int j = 0;
		for (String predicateName : predicateNames) {
			System.out.println("\n name:" + predicateName);

			System.out.println("\n j:" + j++);

			String sql1 = "select count ?b where{?a <rdftype> ?b. ?a <" + predicateName + "> ?c.}";
			HashMap<String, Double> PredicateBySubject = new RDF3XEngine().getCountSingleEntity(sql1);

			double sumSub = 0.0;
			for (String type : PredicateBySubject.keySet()) {

				double percentage = PredicateBySubject.get(type);

				double probability = 0.0;

				if (stat_type_apriori_probability.containsKey(type))
					probability = stat_type_apriori_probability.get(type);
				else
					probability = 0.0;

				double value = (percentage - probability) * (percentage - probability);

				sumSub = value + sumSub;
			}

			String sqlInsert = "INSERT INTO stat_predicate_weight_apriori VALUES(";
			sqlInsert += "'" + predicateName + "',";
			sqlInsert += "'" + 0 + "',";
			sqlInsert += "'" + sumSub + "')";

			try {
				stmt.addBatch(sqlInsert);
				stmt.executeBatch();
			} catch (SQLException e) {
				System.out.println("Error: could not add to batch");
				e.printStackTrace();
			}

			String sql2 = "select count ?b where{?a <rdftype> ?b. ?c ?p ?a. filter(<" + predicateName + ">)}";
			HashMap<String, Double> PredicateByObject = new RDF3XEngine().getCountSingleEntity(sql2);

			double sumObj = 0.0;
			for (String type : PredicateByObject.keySet()) {

				double percentage = PredicateByObject.get(type);

				double probability = 0.0;

				if (stat_type_apriori_probability.containsKey(type))
					probability = stat_type_apriori_probability.get(type);
				else
					probability = 0.0;

				double value = (percentage - probability) * (percentage - probability);

				sumObj = value + sumObj;
			}

			String sqlInsert1 = "INSERT INTO stat_predicate_weight_apriori VALUES(";
			sqlInsert1 += "'" + predicateName + "',";
			sqlInsert1 += "'" + 0 + "',";
			sqlInsert1 += "'" + sumObj + "')";

			try {
				stmt.addBatch(sqlInsert1);
				stmt.executeBatch();
			} catch (SQLException e) {
				System.out.println("Error: could not add to batch");
				e.printStackTrace();
			}
		}

		// -----------------------------------

		String insert = "INSERT INTO stat_predicate_weight_apriori SELECT predicate,outin,"
				+ "SUM((percentage - probability)*(percentage - probability)) FROM stat_type_predicate_percentage"
				+ " LEFT JOIN stat_type_apriori_probability ON stat_type_predicate_percentage.type = stat_type_apriori_probability.type GROUP BY predicate,outin";

		Util.createIndex("stat_predicate_weight_apriori", "predicate");
		Util.checkTable("stat_predicate_weight_apriori");
	}

	public void computePerPredicateWeightApriori() {
		System.out.println("Computing predicate_weight_apriori");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;

		Util.removeTableIfExisting("stat_predicate_weight_apriori");
		String createStatement = "CREATE TABLE stat_predicate_weight_apriori (predicate VARCHAR(1000), outin INT, weight FLOAT)";
		try {
			stmt = conn.createStatement();
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating predicate weight table");
			e.printStackTrace();
		}

		String insert = "INSERT INTO stat_predicate_weight_apriori SELECT predicate,outin,"
				+ "SUM((percentage - probability)*(percentage - probability)) FROM stat_type_predicate_percentage LEFT JOIN stat_type_apriori_probability ON stat_type_predicate_percentage.type = stat_type_apriori_probability.type GROUP BY predicate,outin";
		try {
			stmt.execute(insert);
		} catch (SQLException e) {
			System.out.println("Error inserting into predicate weight table");
			e.printStackTrace();
		}
		Util.createIndex("stat_predicate_weight_apriori", "predicate");
		Util.checkTable("stat_predicate_weight_apriori");
	}

	public HashMap<String, Double> getPredicateCount() {
		String countSql = "select count ?p where{?a ?p ?c. filter(?p != <rdftype> && ?p != <wikiPageDisambiguates>)} ";
		HashMap<String, Double> rdftypeCount = new RDF3XEngine().getCountSingleEntity(countSql);
		return rdftypeCount;
	}

	public HashMap<ArrayList<String>, Double> seperatePredicateBySubject() {
		String countSql = "select count ?b ?p where{?a <rdftype> ?b. ?a ?p ?c. filter(?p != <rdftype> && ?p != <wikiPageDisambiguates>)} ";
		HashMap<ArrayList<String>, Double> allResources = new RDF3XEngine().getCountDoubleEntity(countSql);

		return allResources;
	}

	public HashMap<ArrayList<String>, Double> seperatePredicateByObject() {
		String countSql = "select count ?b ?p where{?a <rdftype> ?b. ?c ?p ?a. filter(?p != <rdftype> && ?p != <wikiPageDisambiguates>)} ";
		HashMap<ArrayList<String>, Double> allResources = new RDF3XEngine().getCountDoubleEntity(countSql);

		return allResources;
	}

}
