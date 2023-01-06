package gilp.knowledgeClean;

import gilp.rdf3x.RDF3XEngine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EntropyName {
	// ***********entropyName***********************

	public HashSet<String> allRelatedPredicatesInKB(String initialPredicate) {
		String sql = "select distinct ?p where {?a <" + initialPredicate + "> ?b. ?a ?p ?c.}";
		HashSet<String> distinctName = new RDF3XEngine().getDistinctEntity(sql);
		System.out.println(distinctName);
		return distinctName;
	}

	public LinkedHashSet<String> entropyNames(String intialName) {

		LinkedHashSet<String> entropyNames = new LinkedHashSet<String>();

		HashMap<String, Double> countObject = new HashMap<String, Double>();
		HashMap<String, Double> doubleSizes = relatedPredicateSizes(intialName);

		HashMap<String, Double> objectEntropy = new HashMap<String, Double>();
		LinkedHashMap<String, Double> ObjEntropyOrder = new LinkedHashMap<String, Double>();

		HashSet<String> pred_names = allRelatedPredicatesInKB(intialName);

		pred_names.remove(intialName);
		pred_names.remove("skos_prefLabel");

		for (String name : pred_names) {

			double entropy = 0.0;

			String sparql = "select count ?c where{?a <" + intialName + "> ?b. ?a <" + name + "> ?c. }";
			countObject = new RDF3XEngine().getCountSingleEntity(sparql);

			double NameSize = doubleSizes.get(name);

			for (String key : countObject.keySet()) {
				Double value = countObject.get(key);

				double accuracy = (double) value / NameSize;

				double entropy1 = -accuracy * (Math.log(accuracy) / Math.log(2));

				entropy = entropy + entropy1;
			}

			objectEntropy.put(name, entropy);

		}
		final Map<String, Double> objectEntropyOrder = new RuleLearnerHelper().reverseOrderByValue(objectEntropy);
		ObjEntropyOrder = (LinkedHashMap<String, Double>) objectEntropyOrder;
		ObjEntropyOrder.remove("skos_prefLabel");
		for (String key : ObjEntropyOrder.keySet()) {
			entropyNames.add(key);
			// System.out.print(key.toString() + "," + ObjEntropyOrder.get(key) + "\n");

		}

		return entropyNames;

	}

	

	public HashMap<String, Double> relatedPredicateSizes(String intialPredicate) {

		HashMap<String, Double> relatedSizes = new HashMap<String, Double>();
		String sparql = "select count ?p where{?a <" + intialPredicate + "> ?b. ?a ?p ?c. }";
		relatedSizes = new RuleLearnerHelper().reverseOrderByValue(new RDF3XEngine().getCountSingleEntity(sparql));
		System.out.println(relatedSizes);
		return relatedSizes;

	}
	// ***********entropyName***********************

	public static void main(String[] args) throws IOException {
		String intialPredicate = "hasGivenName";

		LinkedHashSet<String> entropyNames = new EntropyName().entropyNames(intialPredicate);

		BufferedWriter writer2 = null;
		writer2 = new BufferedWriter(new FileWriter("/home/wy/Desktop/entropy.txt"));
		for (String key : entropyNames) {
			writer2.write(key + "\n");
		}
		writer2.close();

	}

}
