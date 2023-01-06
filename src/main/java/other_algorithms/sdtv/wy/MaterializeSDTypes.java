package other_algorithms.sdtv.wy;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * in the dbpedia database, we put the dbpedia resource to the postgresql, find
 * the untype resource. in the yago database, we can also use the
 * resource(contain the subject and object) to find the untype resource( do the
 * completion in the propertity of untype).
 * 
 * find the untype resource using the sql in the postgresql more easy.
 * 
 * 
 * filter types, the condition1:
 * 
 * SELECT type,SUM(tf*percentage*weight)/SUM(tf*weight) AS score FROM
 * random_resource_hasGivenName_type WHERE resource='******' GROUP BY type
 * HAVING SUM(tf*percentage*weight)/SUM(tf*weight)>=threshold
 * 
 * condition2: ????
 * 
 * 
 * @author wy
 *
 */
public class MaterializeSDTypes {
//	private static int chunkSize = 100000;

	public static void main(String[] args) throws IOException {
		String predicateName = "hasGivenName";
		String path = "/home/wy/Documents/gilp-extend/gilp/result-sdtv";

		// new MaterializeSDTypes().computeUntypes();
		// new MaterializeSDTypes().computeRandomResourcePredicateType(predicateName);
		new MaterializeSDTypes().writeRandomTypeFile(path + "/sdtypes.ttl", 0.001f, predicateName);

	}

//----------------------@wy	
	public void computeUntypes() {
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

		try {
			stmt.execute(insert);
		} catch (SQLException e) {
			System.out.println("Error inserting into untyped instance table");
			e.printStackTrace();
		}
		Util.createIndex("dbpedia_untyped_instance", "resource");
		Util.checkTable("dbpedia_untyped_instance");
	}

	public void writeRandomTypeFile(String fileName, float threshold, String predicateName) throws IOException {
		try {
			JWNL.initialize(new FileInputStream(
					"/home/wy/Documents/gilp-extend/sd-type-validate/WordNet-2.1/file_properties.xml"));
			// path+"/wordnet/file_properties.xml"));
		} catch (JWNLException e1) {
			System.out.println("Error initializing WordNet");
			e1.printStackTrace();
		}

		System.out.println("Writing types to file " + fileName);
		FileWriter FW = new FileWriter(fileName);
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error accessing sd_types table");
			e.printStackTrace();
		}

		String query = "SELECT distinct(resource) FROM random_resource_" + predicateName + "_type ";// only one resource
		try {
			ResultSet RS = stmt.executeQuery(query);
			while (RS.next()) {
				String resource = RS.getString(1);
				try {
					if (testCommonNoun(resource)) {
						System.out.println("Skipping " + resource + " after WordNet test");
					} else {
						String resourceEscaped = resource;
						resourceEscaped = resourceEscaped.replace("'", "''");
						String query2 =
								// ""
								"SELECT type,SUM(tf*percentage*weight)/SUM(tf*weight) AS score FROM random_resource_"
										+ predicateName + "_type WHERE resource='" + resourceEscaped
										+ "' GROUP BY type HAVING SUM(tf*percentage*weight)/SUM(tf*weight)>="
										+ threshold;
						// System.out.println("\n query2:"+query2);
						//
						/**
						 * SELECT type, SUM(tf*percentage*weight)/SUM(tf*weight) as score FROM
						 * stat_resource_predicate_type WHERE
						 * resource='http://dbpedia.org/resource/Dogsled_racing' GROUP BY type HAVING
						 * score>=0.001
						 *
						 * use the having score, meet the problem : ERROR: column "score" does not exist
						 */

						Statement stmt2 = conn.createStatement();
						ResultSet RS2 = stmt2.executeQuery(query2);
						while (RS2.next()) {
							String type = RS2.getString(1);
							// FW.write("<" + resource + ">
							// <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + type
							FW.write("<" + resource + "> <rdftype> <" + type + ">" + System.lineSeparator());
						}
						RS2.close();
					}
				} catch (JWNLException e) {
					System.out.println("WordNet error");
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			System.out.println("Error reading SDTypes");
			e.printStackTrace();
		}

		FW.close();

	}

	public void computeRandomResourcePredicateType(String predicateName) {
		// randomly select one resource and get its predicate and types in the fixed
		// predicate

//		String sql = "select distinct ?p where{ ?a ?p ?c.filter(?p != <rdftype> && ?p != <wikiPageDisambiguates>)} order by ?p";
//		HashSet<String> predicateNames = new RDF3XEngine().getDistinctEntity(sql);

		System.out.println("Computing resource_predicate_type");
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

		create = "CREATE TABLE random_resource_" + predicateName
				+ "_type (resource VARCHAR(1000), predicate VARCHAR(1000), type VARCHAR(1000), tf INT, percentage FLOAT, weight FLOAT)";
		Util.removeTableIfExisting("random_resource_" + predicateName + "_type");
		try {
			stmt.execute(create);
		} catch (SQLException e) {
			System.out.println("Error creating untyped instance table");
			e.printStackTrace();
		}
// insert1 from the yago_untyped_predicate
		String insert1 = "INSERT INTO random_resource_" + predicateName
				+ "_type select resource,perc.predicate,tap.type,tf,percentage,weight from"
				+ "(SELECT resource,predicate,tf,outin FROM public.yago_untyped_predicate  where public.yago_untyped_predicate.predicate='"
				+ predicateName + "'" + "order by random() limit 1 ) as tf "
				+ "LEFT JOIN stat_type_predicate_percentage as perc on tf.predicate = perc.predicate and tf.outin = perc.outin  "
				+ "LEFT JOIN stat_predicate_weight_apriori as weight on tf.predicate = weight.predicate and tf.outin = weight.outin "
				+ "LEFT JOIN stat_type_apriori_probability as tap on perc.type = tap.type";
		// insert1 from the dbpedia_untyped_instance
		insert = "INSERT INTO random_resource_" + predicateName
				+ "_type select tf1.resource,perc.predicate,tap.type,tf,percentage,weight from "
				+ "	(SELECT tf2.resource,tf2.predicate,tf2.tf,tf2.outin from(SELECT tf.resource,tf.predicate,tf.tf,tf.outin FROM public.dbpedia_untyped_instance as instance"
				+ "	 LEFT JOIN stat_resource_predicate_tf as tf on instance.resource = tf.resource and tf.predicate='"
				+ predicateName + "'"
				+ "	 order by tf.resource ) as tf2 where tf2.resource is not null order by random() limit 1) as tf1"
				+ "		LEFT JOIN stat_type_predicate_percentage as perc on tf1.predicate = perc.predicate and tf1.outin = perc.outin"
				+ "		LEFT JOIN stat_predicate_weight_apriori as weight on tf1.predicate = weight.predicate and tf1.outin = weight.outin"
				+ "		LEFT JOIN stat_type_apriori_probability as tap on perc.type = tap.type";

		try {
			stmt.execute(insert);
		} catch (SQLException e) {
			System.out.println("Error inserting into SDType basic stats table");
			e.printStackTrace();
		}

		Util.createIndex("random_resource_" + predicateName + "_type", "resource");
		Util.createIndex("random_resource_" + predicateName + "_type", "predicate");
		Util.createIndex("random_resource_" + predicateName + "_type", "type");
		Util.checkTable("random_resource_" + predicateName + "_type");
	}
	// ----------------------------------------------@wy

//------------------ the original codes
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

		try {
			stmt.execute(insert);
		} catch (SQLException e) {
			System.out.println("Error inserting into untyped instance table");
			e.printStackTrace();
		}
		Util.createIndex("dbpedia_untyped_instance", "resource");
		Util.checkTable("dbpedia_untyped_instance");

//----------------------------------------------
		// stat_resource_predicate_type in the yago, has too many results, so in the
		// yago, we only consider one resource of fixed predicate.

		create = "CREATE TABLE stat_resource_predicate_type (resource VARCHAR(1000), predicate VARCHAR(1000), type VARCHAR(1000), tf INT, percentage FLOAT, weight FLOAT)";
		Util.removeTableIfExisting("stat_resource_predicate_type");
		try {
			stmt.execute(create);
		} catch (SQLException e) {
			System.out.println("Error creating untyped instance table");
			e.printStackTrace();
		}

		insert = "INSERT INTO stat_resource_predicate_type SELECT instance.resource,tf.predicate,perc.type,tf.tf,perc.percentage,weight.weight "
				+ "FROM dbpedia_untyped_instance as instance "
				+ "LEFT JOIN stat_resource_predicate_tf as tf on instance.resource = tf.resource "
				+ "LEFT JOIN stat_type_predicate_percentage as perc on tf.predicate = perc.predicate and tf.outin = perc.outin  "
				+ "LEFT JOIN stat_predicate_weight_apriori as weight on tf.predicate = weight.predicate and tf.outin = weight.outin "
				+ "LEFT JOIN stat_type_apriori_probability as tap on perc.type = tap.type";
		try {
			stmt.execute(insert);
		} catch (SQLException e) {
			System.out.println("Error inserting into SDType basic stats table");
			e.printStackTrace();
		}
		Util.createIndex("stat_resource_predicate_type", "resource");
		Util.checkTable("stat_resource_predicate_type");
	}

	public void writeTypeFile(String fileName, float threshold) throws IOException {
		try {
			JWNL.initialize(new FileInputStream(
					"/home/wy/Documents/gilp-extend/sd-type-validate/WordNet-2.1/file_properties.xml"));
			// path+"/wordnet/file_properties.xml"));
		} catch (JWNLException e1) {
			System.out.println("Error initializing WordNet");
			e1.printStackTrace();
		}

		System.out.println("Writing types to file " + fileName);
		FileWriter FW = new FileWriter(fileName);
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error accessing sd_types table");
			e.printStackTrace();
		}

		String query = "SELECT resource FROM dbpedia_untyped_instance";
		try {
			ResultSet RS = stmt.executeQuery(query);
			while (RS.next()) {
				String resource = RS.getString(1);
				try {
					if (testCommonNoun(resource)) {
						System.out.println("Skipping " + resource + " after WordNet test");
					} else {
						String resourceEscaped = resource;
						resourceEscaped = resourceEscaped.replace("'", "''");
						String query2 =
								// ""
								"SELECT type,SUM(tf*percentage*weight)/SUM(tf*weight) AS score FROM stat_resource_predicate_type WHERE resource='"
										+ resourceEscaped
										+ "' GROUP BY type HAVING SUM(tf*percentage*weight)/SUM(tf*weight)>="
										+ threshold;
						// System.out.println("\n query2:"+query2);
						//
						/**
						 * SELECT type, SUM(tf*percentage*weight)/SUM(tf*weight) as score FROM
						 * stat_resource_predicate_type WHERE
						 * resource='http://dbpedia.org/resource/Dogsled_racing' GROUP BY type HAVING
						 * score>=0.001
						 * 
						 * use the having score, meet the problem : ERROR: column "score" does not exist
						 */

						Statement stmt2 = conn.createStatement();
						ResultSet RS2 = stmt2.executeQuery(query2);
						while (RS2.next()) {
							String type = RS2.getString(1);
							// FW.write("<" + resource + ">
							// <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + type
							FW.write("<" + resource + "> <rdftype> <" + type + ">" + System.lineSeparator());
						}
						RS2.close();
					}
				} catch (JWNLException e) {
					System.out.println("WordNet error");
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			System.out.println("Error reading SDTypes");
			e.printStackTrace();
		}

		FW.close();

	}

	private String lastURI = "";
	private boolean lastResult = false;

	private boolean testCommonNoun(String uri) throws JWNLException {
		if (uri.equals(lastURI))
			return lastResult;
		String fragment = uri.substring(uri.lastIndexOf("/") + 1);

		lastResult = isCommonNoun(fragment);
		lastURI = uri;

		return lastResult;
	}

	private boolean isCommonNoun(String word) throws JWNLException {
		word = word.replace("_", " ");
		if (containsNonAlpha(word))
			return false;
		IndexWord iw = Dictionary.getInstance().lookupIndexWord(POS.NOUN, word);
		if (iw == null)
			return false;
		for (Synset set : iw.getSenses()) {
			for (Word w : set.getWords()) {
				String lemma = w.getLemma();
				if (lemma.equalsIgnoreCase(word)) {
					String first1 = lemma.substring(0, 1);
					if (first1.toLowerCase().equals(first1))
						return true;
				}
			}
		}
		return false;
	}

	private boolean containsNonAlpha(String s) {
		return !s.matches("[A-Za-z\\s]*");
	}

}
