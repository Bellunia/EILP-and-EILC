package gilp.knowledgeClean;

import gilp.comments.Comment;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;

import java.io.*;
import java.util.*;

/*
 * store the configuration settings of our GILP system.
 * and the global information, such as all predicates, database name. 
 * */

public class GILPSettings {

	public static String RDF3X_PATH = "/home/wy/Documents/gh-rdf3x/bin/";
	public static String RDF3X_DBFILE = "yago_type";

	public static int	MAXIMUM_RULE_LENGTH=3;
	public static double EPSILON = 1.0e-7;
	public static int MINIMUM_MAX_MATCHED = 1;
	public static int NUM_RDFTYPE_PARTITIONS = 9;
	//there are NUM_RDFTYPE_PARTITIONS tables in the kb. E.g., NUM_RDFTYPE_PARTITIONS=9, rdftype0, ..., rdftype8
	public static double MINIMUM_FOIL_GAIN = -1.0e7;
	public static boolean IS_DEBUG=true;
	public static double MAXIMUM_FOIL_GAIN = 1.0e7;
	public static double MINIMUM_PRECISION = EPSILON;
	//minimum requirement for an intermediate rule
	public static  double THRESHOLD_OF_PR=0.95;
	// threshold for the probability that r is correct

	//settings for connecting PG
	public static String DB_URL="jdbc:postgresql://127.0.0.1:5432/";
	public static String DB_NAME="rdfkb";
	public static String DB_USER="wy";
	public static String DB_DHPASS="123456";
	public static double CONFIDENCE_Z=1.96;
	// 1 - \alpha/2 quantile of a standard normal distribution
	public static double LAMBDA=0.5;
	public static double FREQUENT_CONST_IN_FB=0.3;
	public static double FREQUENT_CONST_IN_DB=0.1;



	public static HashSet<String> informationEntropyNameList = null;
	public static HashSet<String> chineseSurnameList = null;
	public static HashSet<String> asianSurnameList = null;
	public static HashSet<String> specialRegionsList = null;
	public static HashSet<Comment> allSubjectsFeedback = null;
	public static HashSet<Triple> allFalseTriples = null;
	public static double CONFIDENCE_LEVEL = 0.95;
	public static double intervalConfidence = 0.01;
	public static final int DEFAULT_LEVEL = 2;
	public static final String DEFAULT_PREDICATE_NAME = "isCitizenOf";//"hasGivenName";
	public static final String DEFAULT_CONDITIONAL_NAME = "isCitizenOf";
	public static String _temp_data_file =  "./data/yago_correction/searchspaceTriples_extend.tsv";
	// the name of the file to store the temp data
	/*
	  usage: AMIE [OPTIONS] <TSV FILES>
	 -auta <avoid-unbound-type-atoms> --||-- Avoid unbound type atoms, e.g., type(x, y), i.e., bind always 'y' to a type
     -bexr <body-excluded-relations> --||-- Do not use these relations as atoms in the body of rules. Example: <livesIn>,<bornIn>
	  -bias <e-name> --||-- Syntactic/semantic bias: oneVar|default|[Path to a subclass of amie.mining.assistant.MiningAssistant]Default: default (defines support and confidence in terms of 2 head variables (given an order, cf -vo ) )
	  -btr <body-target-relations> --||-- Allow only these relations in the body.  Provide a list of relation names separated by commas (incompatible with  body-excluded-relations). Example: <livesIn>,<bornIn>
	  -caos <count-always-on-subject>--||-- If a single variable bias is used (oneVar), force to count  support always on the subject position.
	  -const <allow-constants> --||-- Enable rules with constants. Default: false
	  -deml <do-not-exploit-max-length> --||-- Do not exploit max length for speedup (requested by the  reviewers of AMIE+). False by default.
	  -dpr <disable-perfect-rules>--||-- Disable perfect rules.
	  -dqrw <disable-query-rewriting> --||-- Disable query rewriting and caching.
	  -ef <extraFile> --||-- An additional text file whose interpretation depends  on the selected mining assistant (bias)
	  -fconst <only-constants> --||-- Enforce constants in all atoms. Default: false
	  -full <full>--||-- It enables all enhancements: lossless heuristics and confidence  approximation and upper bounds It overrides any other configuration that is  incompatible.
	  -hexr <head-excluded-relations> --||-- Do not use these relations as atoms in  the head of rules (incompatible with head-target-relations). Example:  <livesIn>,<bornIn>
	  -htr <head-target-relations> --||-- Mine only rules with these relations in  the head. Provide a list of relation names separated by commas (incompatible  with head-excluded-relations). Example: <livesIn>,<bornIn>
	  -maxad <max-depth> --||-- Maximum number of atoms in the antecedent and  succedent of rules. Default: 3
	  -minc <min-std-confidence> --||-- Minimum standard confidence threshold. This  value is not used for pruning, only for filtering of the results. Default:  0.0
	  -minhc <min-head-coverage> --||-- Minimum head coverage. Default: 0.01
	  -minis <min-initial-support> --||-- Minimum size of the relations to be considered as head relations. Default: 100 (facts or entities depending on the bias)
	  -minpca <min-pca-confidence> --||-- Minimum PCA confidence threshold. This value is not used for pruning, only for filtering of the results. Default: 0.0
	  -mins <min-support> --||-- Minimum absolute support. Default: 100 positive examples
	  -mt <mining-technique> --||-- AMIE offers 2 multi-threading strategies: standard (traditional) and solidary (experimental)
	  -nc <n-threads> --||-- Preferred number of cores. Round down to the actual number of cores in the system if a higher value is provided.
	  -oout <only-output>--||-- If enabled, it activates only the output enhacements, that is, the confidence approximation and upper bounds. It overrides any other configuration that is incompatible.
	  -optimcb <optim-confidence-bounds>--||-- Enable the calculation of confidence upper bounds to prune rules.
	  -optimfh <optim-func-heuristic> --||-- Enable functionality heuristic to identify potential low confident rules for pruning.
	  -oute <output-at-end>--||-- Print the rules at the end and not while they are discovered. Default: false
	  -pm <pruning-metric> --||-- Metric used for pruning of intermediate queries:  support|headcoverage. Default: headcoverage
	  -rl <recursivity-limit> --||-- Recursivity limit
	  -verbose <verbose>--||-- Maximal verbosity
	 */

	 /*
	 amie3--new addition
	  -datalog <datalog-output> --||-- Print rules using the datalog notation Default: false
	  -noHeuristics <noHeuristics>  --||-- Disable functionality heuristic, should be used with the -full option
	  -noKbRewrite <noKbRewrite>  --||-- Prevent the KB to rewrite query when counting pairs
	  -noKbExistsDetection  <noKbExistsDetection> --||-- Prevent the KB to detect existential variable on-the-fly and to optimize the query
	  -vo <variableOrder>  --||-- Define the order of the variable in counting query among: app, fun (default), ifun
	  -ostd <ommit-std-conf>  --||-- Do not calculate standard confidence
	  -cc  <count-cache> --||-- Cache count results
	  -optimai  <adaptive-instantiations>--||-- Prune instantiated rules that decrease too much the support of their parent rule (ratio 0.2)
	   -mlg <multilingual> --||-- Parse labels language as new facts
	   -d <delimiter>--||-- Separator in input files (default: TAB)
	   */
	public String[] parameters() {
		return new String[]{ "-maxad", "3", "-mins", "1", "-minis", "1", "-bexr",
				"<skos_prefLabel>,<hasGender>,<" + DEFAULT_PREDICATE_NAME + ">", "-htr",
				" <" + DEFAULT_PREDICATE_NAME + ">", "-minhc", "0.01", "-minpca", "0.01", "-dpr", "-optimfh", "-const",
				_temp_data_file };
	}
	public static HashSet<Comment> getAllSubjectsFeedback() {
		if (allSubjectsFeedback == null) {
			try {
				RandomAccessFile file = new RandomAccessFile(
						GILPSettings.getRootPath() + "/data/feedback/existing/GILP_feedback_subjects.txt", "r");
				allSubjectsFeedback = new HashSet<Comment>();
				String line = null;
				while ((line = file.readLine()) != null) {

					line = line.replace("<", "").replace(">", "");

					StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
					String subject, predicate, object;
					subject = stringTokenizer.nextToken();// extract the subject
					predicate = stringTokenizer.nextToken();// extract the predicate
					object = stringTokenizer.nextToken();// extract the object

					String value = stringTokenizer.nextToken();
					boolean decision = Boolean.parseBoolean(value);// parse"true"
					Triple triple = new Triple(subject, predicate, object);

					Comment comment = new Comment(triple, decision);

					allSubjectsFeedback.add(comment);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

		return allSubjectsFeedback;
	}
	// get the names of all predicates stored in DB selected by InformationEntropy
	public static HashSet<String> getInformationEntropyNames() {

		if (informationEntropyNameList == null) {
			try {
				RandomAccessFile file = new RandomAccessFile(getRootPath() + "/data/yago/InformationEntropyName", "r");
				informationEntropyNameList = new HashSet<String>();
				String line = null;
				while ((line = file.readLine()) != null) {
					informationEntropyNameList.add(line);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		return informationEntropyNameList;

	}
	public static HashSet<String> getChineseSurnameList() {
		if (chineseSurnameList == null) {
			try {
				RandomAccessFile file = new RandomAccessFile(getRootPath() + "/data/yago/ChineseSurname", "r");
				chineseSurnameList = new HashSet<String>();
				String line = null;
				while ((line = file.readLine()) != null) {
					chineseSurnameList.add(line);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

		return chineseSurnameList;

	}
	public static HashSet<Triple> getAllFalseTriples() {
		if (allFalseTriples == null) {
			try {
				RandomAccessFile file = new RandomAccessFile(getRootPath() + "/data/yago/allFalseTriples.txt", "r");
				allFalseTriples = new HashSet<Triple>();
				String line = null;
				while ((line = file.readLine()) != null) {

					String[] array = line.replace("<", "").replace(">", "").split(" ");

					String subject, predicate, object;
					subject = array[0];
					predicate = array[1];
					object = array[2];

					Triple triple = new Triple(subject, predicate, object);

					allFalseTriples.add(triple);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

		return allFalseTriples;

	}
	public static Double getNumberOfPredicate(String name) {
		String query = " select count ?p where{?a ?p ?c.filter(?p=<" + name + ">)}";
		HashMap<String, Double> objectsWithNumbers = new RDF3XEngine().getCountSingleEntity(query);
		return (double) objectsWithNumbers.values().toArray()[0];

	}
	public static HashSet<String> getSpecialRegionsList() {
		if (specialRegionsList == null) {
			try {
				RandomAccessFile file = new RandomAccessFile(getRootPath() + "/data/yago/SpecialRegions", "r");
				specialRegionsList = new HashSet<String>();
				String line = null;
				while ((line = file.readLine()) != null) {
					specialRegionsList.add(line);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

		return specialRegionsList;

	}
	public static HashSet<String> getAsianSurnameList() {
		if (asianSurnameList == null) {
			try {
				RandomAccessFile file = new RandomAccessFile(getRootPath() + "/data/yago/familyNameInAsian", "r");
				asianSurnameList = new HashSet<String>();
				String line = null;
				while ((line = file.readLine()) != null) {
					asianSurnameList.add(line);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

		return asianSurnameList;

	}
	public static String getRootPath() {
		return System.getenv("GILP_HOME");
	}
	static int rand(int size) {
		 Random random = new Random();
		return random.nextInt(size);
	}
	static int rand_max(int x,int size) {
		// get a random number between (0, x)
		int res = (rand(size) * rand(size)) % x;
		while (res < 0) {
			res += x;
		}
		return res;
	}

	//-----@2021.11.11
	public static String[] NUMERICAL_PREDICATES = { "hasArea", "hasBudget", "hasDuration", "hasEconomicGrowth",
			"hasExpenses", "hasExport", "hasGDP", "hasGini", "hasHeight", "hasImport", "hasInflation", "hasLatitude",
			"hasLength", "hasLongitude", "hasNumberOfPeople", "hasPages", "hasPopulationDensity","hasPoverty",
			"hasRevenue", "hasUnemployment", "hasWeight",
	};
	//TODO current implementation does not deal with dates
	//"diedOnDate", "happenedOnDate", "startedOnDate","wasBornOnDate", "wasCreatedOnDate", "wasDestroyedOnDate"
//get the statistics of yago facts


	static ArrayList<String> _all_predicates = null;
	//store all predicates' names
	static HashMap<String, Double> _yago_stat = null;

	public static HashMap<String, Double> getYagoStat(){
		if (_yago_stat == null){
			try {
				RandomAccessFile file = new RandomAccessFile("yago_stat","r");
				_yago_stat = new HashMap<String, Double>();
				String line = null;
				while((line = file.readLine())!=null){
					String pred = line.substring(0, line.indexOf('\t'));
					double pr = Double.parseDouble(line.substring(line.indexOf('\t')+1));
					_yago_stat.put(pred, pr);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		return _yago_stat;
	}
	//get the names of all predicates stored in DB
	public static ArrayList<String> getAllPredicateNames(){
		if (_all_predicates == null){
			try {
				RandomAccessFile file = new RandomAccessFile("predicates","r");
				_all_predicates = new ArrayList<String>();
				String line = null;
				while((line = file.readLine())!=null){
					/*if (line.startsWith("rdftype")){
						for (int i=0;i<GILPSettings.NUM_RDFTYPE_PARTITIONS;i++)
							_all_predicates.add(line + i);
					}
					else*/
					_all_predicates.add(line);
				}
				file.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		return _all_predicates;
	}

//	//----gilp-rule learning @11.30
//	static private PrintWriter log;
//
//	static{
//		init();
//	}
//
//	/**
//	 * Writes a message to the log file.
//	 */
//	public static void log(String msg) {
//		log.println(new Date() + ": " + msg);
//	}
//
//	/**
//	 * Writes a message with an Exception to the log file.
//	 */
//	public static void log(Throwable e, String msg) {
//		log.println(new Date() + ": " + msg);
//		e.printStackTrace(log);
//	}
//	static private void init() {
//		Properties sim_prop = new Properties();
//		try {
//
//			InputStream is = new FileInputStream(new File("config.txt"));
//			sim_prop.load(is);
//		} catch (Exception e) {
//			System.err.println("Can't read the properties file. ");
//			return;
//		}
//
//		String logFile = sim_prop.getProperty("logfile");
//		Date d = new Date();
//
//		logFile += d.getDate() + "-" + d.getHours() + "-" + d.getMinutes() + ".log";
//
//		try {
//			log = new PrintWriter(new FileWriter(logFile, true), true);
//		} catch (IOException e) {
//			System.err.println("Can't open the log file: " + logFile);
//			log = new PrintWriter(System.err);
//		}
//
//		FREQUENT_CONST_IN_FB = Double.parseDouble(sim_prop.getProperty("FREQUENT_CONST_IN_FB"));
//		FREQUENT_CONST_IN_DB = Double.parseDouble(sim_prop.getProperty("FREQUENT_CONST_IN_DB"));
//		LAMBDA = Double.parseDouble(sim_prop.getProperty("LAMBDA"));
//		CONFIDENCE_Z = Double.parseDouble(sim_prop.getProperty("CONFIDENCE_Z"));
//		THRESHOLD_OF_PR = Double.parseDouble(sim_prop.getProperty("THRESHOLD_OF_PR"));
//		MAXIMUM_RULE_LENGTH = Integer.parseInt(sim_prop.getProperty("MAXIMUM_RULE_LENGTH"));
//		IS_DEBUG = (sim_prop.getProperty("IS_DEBUG").equals("true"));
//		MINIMUM_PRECISION = Double.parseDouble(sim_prop.getProperty("MINIMUM_PRECISION"));
//
//		DB_ENGINE = Integer.parseInt(sim_prop.getProperty("DB_ENGINE"));
//		//1: RDF3X; 2: PostgreSQL;
//		switch(DB_ENGINE){
//			case 1:
//				RDF3X_PATH = sim_prop.getProperty("RDF3X_PATH");
//				RDF3X_DBFILE = sim_prop.getProperty("RDF3X_DBFILE");
//				break;
//			case 2:
//				DB_URL = sim_prop.getProperty("DB_URL");
//				DB_NAME = sim_prop.getProperty("DB_NAME");
//				DB_USER = sim_prop.getProperty("DB_USER");
//				DB_DHPASS = sim_prop.getProperty("DB_DHPASS");
//				break;
//			default:
//				GILPSettings.log("Error: the prop value not valid for  DB_ENGINE:" + DB_ENGINE);
//		}
//
//
//
//		System.out.println("Settings:");
//		System.out.println("LogFile:" + logFile);
//		System.out.println("FREQUENT_CONST_IN_FB" + FREQUENT_CONST_IN_FB);
//		System.out.println("FREQUENT_CONST_IN_DB:" + FREQUENT_CONST_IN_DB);
//		System.out.println("LAMBDA:" + LAMBDA);
//		System.out.println("CONFIDENCE_Z:" + CONFIDENCE_Z);
//		System.out.println("THRESHOLD_OF_PR:" + THRESHOLD_OF_PR);
//		System.out.println("MAXIMUM_RULE_LENGTH:" + MAXIMUM_RULE_LENGTH);
//		System.out.println("IS_DEBUG:" + IS_DEBUG);
//		System.out.println("MINIMUM_PRECISION:" + MINIMUM_PRECISION);
//
//		switch(DB_ENGINE){
//			case 1:
//				System.out.println("RDF3X_PATH:" + RDF3X_PATH);
//				System.out.println("RDF3X_DBFILE:" + RDF3X_DBFILE);
//				break;
//			case 2:
//				System.out.println("DB_URL:" + DB_URL);
//				System.out.println("DB_NAME:" + DB_NAME);
//				System.out.println("DB_USER:" + DB_USER);
//				System.out.println("DB_DHPASS:" + DB_DHPASS);
//		}
//	}


}