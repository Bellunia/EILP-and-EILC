package gilp.utils;

import gilp.rdf3x.RDF3XEngine;
import gilp.sparql.Sparql;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;

public class YagoPrecessing {

	static void generateIndex() {
		String fileName = "predicates";
		RandomAccessFile file = null;

		try {
			file = new RandomAccessFile(fileName, "r");

			String line = null;
			while ((line = file.readLine()) != null) {
				line = line.replace(":", "");
				String[] index_names = { "idx_" + line + "_S", "idx_" + line + "_O" };
				for (String str : index_names) {
					String sql = "DROP INDEX if exists " + str + " ;";
					System.out.println(sql);
					sql = "CREATE index ";
					sql += str + " on ";
					sql += line + "(" + str.substring(str.lastIndexOf("_") + 1) + ");";
					System.out.println(sql);
				}

			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	/* generate SQL for creating predicate tables */
	static void generatePredicateTables() {

		String fileName = "D:\\temp\\yago-predicates.txt";
		RandomAccessFile file = null;

		try {
			file = new RandomAccessFile(fileName, "r");

			String line = null;
			while ((line = file.readLine()) != null) {
				line = line.replace(":", "");
				String sql = "DROP TABLE if exists " + line + " ;";
				System.out.println(sql);
				sql += "CREATE TABLE ";
				sql += line + " ";
				sql += "( tid serial NOT NULL, ";
				sql += "s character varying(1024), ";
				sql += "o character varying(1024), ";
				sql += "CONSTRAINT \"" + line + "-pk\" PRIMARY KEY (tid) );";
				System.out.println(sql);
			}
		} catch (Exception e) {
			// e.printStackTrace(System.out);
		}
	}

	// remove < > ", and @
	static String formatYagoToken(String str) {
		// <id_1958v2w_l7i_10ev6mt> <Saatly_Rayon> <hasNumberOfPeople>
		// "95100"^^xsd:integer 95100
		String rlt = str.replaceAll("<", "");
		rlt = rlt.replaceAll(">", "");
		rlt = rlt.replaceAll("\'", "-");
		rlt = rlt.replaceAll("\"", "");
		rlt = rlt.replace(":", "");
		if (rlt.contains("@")) {
			rlt = rlt.substring(0, rlt.indexOf("@"));
		}
		return rlt;
	}

	// -------------------------@wy

	static void preprocessData() throws Exception {

		BufferedWriter writer1 = null;

		writer1 = new BufferedWriter(new FileWriter("/home/wy/Downloads/all_Data.tsv"));

		File dir = new File("/home/wy/Downloads/data/");

		for (File file : dir.listFiles()) {

			System.out.println("\ngetName: " + file.getName() + "\n");

		//	BufferedWriter writer = null;

		//	writer = new BufferedWriter(new FileWriter("/home/wy/Downloads/copy/" + file.getName() + ".tsv"));
			String line = null;
			Scanner scan = new Scanner(file, "UTF-8");

		//	scan.nextLine();// skip the first line

			while (scan.hasNextLine()) {
				line = scan.nextLine();
				// System.out.println("\ntest: " + line + "\n");

				StringTokenizer st = new StringTokenizer(line, "\t");
				// System.out.println("\n countTokens: " + st.countTokens() + "\n");
				if (st.countTokens() == 4)

					st.nextToken();// skip the first token
				else
					continue;

				// String id = st.nextToken();

				String s = formatYagoToken(st.nextToken());
				String p = formatYagoToken(st.nextToken());
				String o = formatYagoToken(st.nextToken());

				String o1 = null;
				if (o.contains("@"))
					o1 = o.replace("@", "__").split("__")[0];
				else
					o1 = o;

			//	writer.write("<" + s + ">" + "\t" + "<" + p + ">" + "\t" + "<" + o1 + ">\t.\n");
				writer1.write("<" + s + ">" + "\t" + "<" + p + ">" + "\t" + "<" + o1 + ">\t.\n");

			}
		//	writer.close();

			scan.close();
		}

		writer1.close();
	}
	

	static void combineData() throws Exception {

		BufferedWriter writer1 = null;

		writer1 = new BufferedWriter(new FileWriter("/home/wy/Downloads/all_Data1.tsv"));

		File dir = new File("/home/wy/Downloads/data/");

		for (File file : dir.listFiles()) {

			System.out.println("\ngetName: " + file.getName() + "\n");

		//	BufferedWriter writer = null;

		//	writer = new BufferedWriter(new FileWriter("/home/wy/Downloads/copy/" + file.getName() + ".tsv"));
			String line = null;
			Scanner scan = new Scanner(file, "UTF-8");

		//	scan.nextLine();// skip the first line

			while (scan.hasNextLine()) {
				line = scan.nextLine();
				// System.out.println("\ntest: " + line + "\n");

//				StringTokenizer st = new StringTokenizer(line, "\t");
//				// System.out.println("\n countTokens: " + st.countTokens() + "\n");
//				if (st.countTokens() == 4)
//
//					st.nextToken();// skip the first token
//				else
//					continue;
//
//				// String id = st.nextToken();
//
//				String s = formatYagoToken(st.nextToken());
//				String p = formatYagoToken(st.nextToken());
//				String o = formatYagoToken(st.nextToken());
//
//				String o1 = null;
//				if (o.contains("@"))
//					o1 = o.replace("@", "__").split("__")[0];
//				else
//					o1 = o;

			//	writer.write("<" + s + ">" + "\t" + "<" + p + ">" + "\t" + "<" + o1 + ">\t.\n");
			//	writer1.write("<" + s + ">" + "\t" + "<" + p + ">" + "\t" + "<" + o1 + ">\t.\n");
				
				writer1.write(line+"\n");

			}
		//	writer.close();

			scan.close();
		}

		writer1.close();
	}
	
	static void combineData1() throws Exception {

		BufferedWriter writer1 = null;

		writer1 = new BufferedWriter(new FileWriter("/home/wy/Downloads/select_dbpedia_Data.tsv"));

		File dir = new File("/home/wy/Downloads/dbpedia-data-selected1/");

		for (File file : dir.listFiles()) {

			System.out.println("\ngetName: " + file.getName() + "\n");

		//	BufferedWriter writer = null;

		//	writer = new BufferedWriter(new FileWriter("/home/wy/Downloads/copy/" + file.getName() + ".tsv"));
			String line = null;
			Scanner scan = new Scanner(file, "UTF-8");

			scan.nextLine();// skip the first line

			while (scan.hasNextLine()) {
				line = scan.nextLine();
				// System.out.println("\ntest: " + line + "\n");

//				StringTokenizer st = new StringTokenizer(line, "\t");
//				// System.out.println("\n countTokens: " + st.countTokens() + "\n");
//				if (st.countTokens() == 4)
//
//					st.nextToken();// skip the first token
//				else
//					continue;
//
//				// String id = st.nextToken();
//
//				String s = formatYagoToken(st.nextToken());
//				String p = formatYagoToken(st.nextToken());
//				String o = formatYagoToken(st.nextToken());
//
//				String o1 = null;
//				if (o.contains("@"))
//					o1 = o.replace("@", "__").split("__")[0];
//				else
//					o1 = o;

			//	writer.write("<" + s + ">" + "\t" + "<" + p + ">" + "\t" + "<" + o1 + ">\t.\n");
			//	writer1.write("<" + s + ">" + "\t" + "<" + p + ">" + "\t" + "<" + o1 + ">\t.\n");
				
				writer1.write(line+"\n");

			}
		//	writer.close();

			scan.close();
		}

		writer1.close();
	}



	static void preprocessSingleData() throws Exception {

		BufferedWriter writer1 = null;
		
		writer1 = new BufferedWriter(new FileWriter("/home/wy/Downloads/data/instance_types_en.ttl"));
		///home/wy/Downloads/data/yagoWikidataInstances1.tsv

		// File dir = new File("/home/wy/Downloads/original-data/");

		// for (File file : dir.listFiles()) {

		// System.out.println("\ngetName: " + file.getName() + "\n");

		// BufferedWriter writer = null;
		File dir = new File("/home/wy/Downloads/dbpedia-data-selected1/instance_types_en.ttl");
		// writer = new BufferedWriter(new FileWriter("/home/wy/Downloads/copy/" +
		// file.getName() + ".tsv"));
		String line = null;
		Scanner scan = new Scanner(dir, "UTF-8");

		 scan.nextLine();// skip the first line

		while (scan.hasNextLine()) {
			line = scan.nextLine();
			// System.out.println("\ntest: " + line + "\n");

			StringTokenizer st = new StringTokenizer(line, " ");// \t
			// System.out.println("\n countTokens: " + st.countTokens() + "\n");
//					if (st.countTokens() == 4)
			//
//						st.nextToken();// skip the first token
//					else
//						continue;

			// String id = st.nextToken();

			 String s = st.nextToken();//.replace("http://dbpedia.org/resource/", "");
			String p = st.nextToken();//.replace("http://dbpedia.org/ontology/", "");
			 String o = st.nextToken();//.replace("http://dbpedia.org/resource/", "");
		//	String line1 = line.replace("http://dbpedia.org/resource/", "").replace("http://dbpedia.org/ontology/", "");

			// System.out.println("\ntest: " + line1 + "\n");
			 writer1.write("<" + removePointBrackets(s) + ">" + "\t" + "<" +removePointBrackets( p )+ ">" + "\t" + "<" + removePointBrackets(o) +
			">\t.\n");
		//	writer1.write(line1 + "\n");

		}
		// writer.close();

		// }

		writer1.close();
		scan.close();
	}

	static void preprocessSingleData1() throws Exception {

		BufferedWriter writer1 = null;

		writer1 = new BufferedWriter(new FileWriter("/home/wy/Downloads/types.tsv"));

		// File dir = new File("/home/wy/Downloads/original-data/");

		// for (File file : dir.listFiles()) {

		// System.out.println("\ngetName: " + file.getName() + "\n");

		// BufferedWriter writer = null;
		File dir = new File("/home/wy/Downloads/yagoTransitiveType.tsv");
		// writer = new BufferedWriter(new FileWriter("/home/wy/Downloads/copy/" +
		// file.getName() + ".tsv"));
		String line = null;
		Scanner scan = new Scanner(dir, "UTF-8");

		scan.nextLine();// skip the first line

		while (scan.hasNextLine()) {
			line = scan.nextLine();
			// System.out.println("\ntest: " + line + "\n");

			StringTokenizer st = new StringTokenizer(line, "\t");
			// System.out.println("\n countTokens: " + st.countTokens() + "\n");
			if (st.countTokens() == 4)

				st.nextToken();// skip the first token
			else
				continue;

			// String id = st.nextToken();

			String s = formatYagoToken(st.nextToken());
			String p = formatYagoToken(st.nextToken());
			String o = formatYagoToken(st.nextToken());

			String o1 = null;
			if (o.contains("@"))
				o1 = o.replace("@", "__").split("__")[0];
			else
				o1 = o;

			writer1.write("<" + s + ">" + "\t" + "<" + p + ">" + "\t" + "<" + o1 + ">\t.\n");
			// writer1.write(line1+ "\n");

		}
		// writer.close();

		// }

		writer1.close();
		scan.close();
	}

	public static String removePointBrackets(String str) {
		str = str.replace("<", "");
		str = str.replace(">", "");
		return str;
	}

	static String dbpediaWeb = "http://dbpedia.org/sparql";

	public static void filterPrediactes() throws Exception {
//resource,indegree

		BufferedWriter writer = null;

		writer = new BufferedWriter(new FileWriter("/home/wy/Downloads/untyped_object_hasGivenName.txt"));
		// the number of object >10

		// BufferedWriter writer1 = null;

		// writer1 = new BufferedWriter(new
		// FileWriter("/home/wy/Downloads/sdv_interesting_resource_inDBPEDIA.txt"));

		// BufferedWriter writer2 = null;

		String predicateName = "hasGivenName";
		// writer2 = new BufferedWriter(new
		// FileWriter("/home/wy/Downloads/sdv_interesting_resource_"+predicateName+".txt"));

		Scanner scanner = new Scanner("/home/wy/Desktop/sdv_interesting_resource");
		File file = new File(scanner.nextLine());
		Scanner input = new Scanner(file);

		input.nextLine();// skip the first line
		writer.write("resource" + "\t" + "indegree\t" + "predicate\n");

		// writer1.write("resource"+"\t"+"indegree\t"+"predicate\n");
		// writer2.write("resource"+"\t"+"indegree\t"+"predicate\n");

		HashSet<String> differentInstance = new HashSet<String>();

		while (input.hasNextLine()) {
			// 07_Vestur,24
			int labels = 0;
			String line = input.nextLine();
			StringTokenizer stringTokenizer;

			// System.out.println(line);

			if (line.contains("\","))
				stringTokenizer = new StringTokenizer(line, "\",");
			else
				stringTokenizer = new StringTokenizer(line, ",");
			String instance = null;
			double indegree = 0.0;
			int number = stringTokenizer.countTokens();

			if (number > 2) {

				for (int i = 0; i < number - 1; i++) {

					String instance1 = stringTokenizer.nextToken().replace("\"", "");
//	 String  instance2 = stringTokenizer.nextToken().replace("\"", "");
					instance = instance + instance1 + ",";// +instance2;
				}
				instance = instance.substring(0, instance.length() - 1);
				indegree = Double.parseDouble(stringTokenizer.nextToken());
			} else {
				instance = stringTokenizer.nextToken().replace("\"", "");// extract the instance

				indegree = Double.parseDouble(stringTokenizer.nextToken());
			}

			// String entity, predicate, object;
			// double indegree=0.0;
			// instance = stringTokenizer.nextToken().replace("\"", "");// extract the
			// instance
			// System.out.println(line);
			// System.out.println(instance);

			// double indegree = Double.parseDouble(stringTokenizer.nextToken());

			// System.out.println(indegree);

			String sparql = "select distinct ?a where{ ?a ?p <" + instance + ">. filter (?p =<" + predicateName
					+ ">)} limit 1";
			// System.out.println(sparql);

			HashSet<String> predicateRDF3x = new RDF3XEngine().getDistinctEntity(sparql);
			// System.out.println(predicateRDF3x);
//----------------------------dbpedia
//			String sparqlDbpedia = "select distinct ?p where{ ?a ?p <http://dbpedia.org/resource/" + instance + ">.}";
//			System.out.println(sparqlDbpedia);
//
//			HashSet<String> getResultsFromQuery = new Sparql().getSingleResultsFromQuery(sparqlDbpedia, dbpediaWeb);
//			// [http://dbpedia.org/ontology/wikiPageWikiLink,..]
//			System.out.println(getResultsFromQuery);

			// ----------------------------dbpedia

			if (!predicateRDF3x.isEmpty()) {

				String sparqlType = "	select distinct ?p where{ ?a a ?p filter(?a= <http://dbpedia.org/resource/"
						+ instance + ">).}";
				HashSet<String> typesInDbpedia = new Sparql().getSingleResultsFromQuery(sparqlType, dbpediaWeb);

				if (typesInDbpedia.isEmpty()) {

					writer.write(instance + "\t" + indegree + "\n");
				}
			}

//			writer1.write(instance+"\t"+indegree+"\t");
//			for(String key: getResultsFromQuery)
//				writer1.write(key+"\t");
//			
//			writer1.write("\n");
//			
//			HashSet<String> allPredicate = new HashSet<String>();
//			allPredicate.addAll(predicateRDF3x);
//			allPredicate.addAll(getResultsFromQuery);
//			
//			for(String key: allPredicate) {
//				if(key.contains(predicateName))
//					labels=1;
//			}
//			
//			if(labels==1 ) {
//				writer2.write(instance+"\t"+indegree+"\t");
//				for(String key: allPredicate) {
//					writer2.write(key+"\t");
//				}
//				
//				writer2.write("\n");
//			}

		}
		input.close();
		scanner.close();

		writer.close();
		// writer1.close();
		// writer2.close();

		// System.out.println("READING all comments: " + comments.size());

	}

	static void combineData2(String path1, String path2,String writePath) throws Exception {

		BufferedWriter writer1 = null;

		writer1 = new BufferedWriter(new FileWriter(writePath));


			String line = null;
			Scanner scan = new Scanner(new File(path1), "UTF-8");
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				writer1.write(line+"\n");
			}
			scan.close();
		String line1 = null;
		Scanner scan1 = new Scanner(new File(path2), "UTF-8");
		while (scan1.hasNextLine()) {
			line1 = scan1.nextLine();
			writer1.write(line1+"\n");
		}
		scan.close();


		writer1.close();
	}


	private static int min(int one, int two, int three) {
		return (one = one < two ? one : two) < three ? one : three;
	}
	// -----------------------------@wy

	public static void main(String[] args) throws Exception {
		combineData2("/home/wy/Desktop/example-search/negativeSearchSpace20_new.tsv",
				"/home/wy/Desktop/example-search/positiveSearchSpace20_new.tsv",
				"/home/wy/Desktop/example-search/combineSearch/searchSpace20.tsv");
		// addFeedbacks();

		// preprocessSingleData1();
		//filterPrediactes();
		//combineData();
	//	combineData1();
		//preprocessData();
	//	preprocessSingleData();

//		String sql="select distinct ?p where{ ?a ?p <1941_(film)>. filter (?p =<hasGivenName> )} limit 1";
//		
//		HashSet<String> predicateRDF3x = new RDF3XEngine().getDistinctEntity(sql);
//		
//		if(!predicateRDF3x.isEmpty() ) {
//			
//			System.out.println("test---\n"+predicateRDF3x);
//			}

	}

}
