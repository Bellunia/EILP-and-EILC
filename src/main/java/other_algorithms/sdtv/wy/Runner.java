package other_algorithms.sdtv.wy;

import java.io.IOException;
//import java.util.HashSet;

//import gilp.rdf3x.RDF3XEngine;

/**
 * The main class that runs SDType&Validate the original files,put into the
 * RDF3X datasets, using the rdf3x to deal with the data more fast than sql,
 * then put the results in the pgadmin4.
 * 
 * @author Heiko
 */
public class Runner {
	static String path = "/home/wy/Documents/gilp-extend/gilp/result-sdtv";

	public static void main(String[] args) {

		// CountAprioriProbability aprioriProbability= new CountAprioriProbability();
		// ComputePerPredicateDistribution perPredicateDistribution =new
		// ComputePerPredicateDistribution();
		MaterializeSDTypes materializeSDTypes = new MaterializeSDTypes();
		MaterializeSDValidate materializeSDValidate = new MaterializeSDValidate();
		try {

			/** put the original triples to the postgresql. **/
			// CountAprioriProbability.loadAllResources();
			/** get the apriori probability of each predicate **/
			// perPredicateDistribution.computePerPredicateDistribution();
			// perPredicateDistribution.computePerPredicateWeightApriori();
			
			//-----------------finish loading the database

			// materializeSDTypes.computeSDTypes_predicate();
			// materializeSDTypes.computeSDTypes();// get the untype resource.
			materializeSDTypes.writeTypeFile(path + "/sdtypes.ttl", 0.001f);// ./sdtypes.ttl 0.4f
			// materializeSDValidate.computeSDValidateScores();
			// materializeSDValidate.writeWrongStatementsFile(path+"/sdinvalid.ttl",
			// 0.01f);//./sdinvalid.ttl

		} catch (IOException e) {
			System.out.println("Error loading input files");
			e.printStackTrace();
		}
	}
}
