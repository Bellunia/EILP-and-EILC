package other_algorithms.chai.wy.CHAI;

import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InstanceCriteria {

	static String dbpediaWeb = "https://dbpedia.org/sparql";
	String wikiWeb = "https://query.wikidata.org/";

	public HashMap<String, ArrayList<String>> findWikidataID(Triple triple, int distance) {
		String subject = triple.get_subject();

		HashMap<String, Double> allResources = new HashMap<String, Double>();

		HashMap<String, ArrayList<String>> findWikidataIDs = new HashMap<String, ArrayList<String>>();

		if (distance == 1) {

			ArrayList<String> IDlists = new ArrayList<String>();

			String sparql = "select distinct ?c where{ ?a ?p ?c. filter (?a=<" + subject + ">)  }";
			// select distinct ?c where{ ?a ?b ?c. filter(?a =<Joseph_S._O-Leary>)}
			HashSet<String> distinctTargets = new RDF3XEngine().getDistinctEntity(sparql);

			// SELECT distinct ?a WHERE { ?b owl:sameAs ?a. FILTER(regex(str(?a),
			// "www.wikidata.org" ) && ?b=<http://dbpedia.org/resource/Tristram_Hillier>) }
			System.out.println("\n sparql: " + sparql);
			if (!distinctTargets.isEmpty()) {

				for (String key : distinctTargets) {

					key = key.substring(0, 1).toUpperCase() + key.substring(1);

					String sparqlDbpedia = "SELECT distinct ?b WHERE { ?a owl:sameAs ?b. "
							+ "FILTER(regex(str(?b),\"www.wikidata.org\")" + " && ?a=<http://dbpedia.org/resource/"
							+ key + ">)}";

					HashSet<String> typesInDbpedia = new Sparql().getSingleResultsFromQuery(sparqlDbpedia, dbpediaWeb);
					System.out.println("typesInDbpedia:" + typesInDbpedia);

					if (typesInDbpedia.size() == 1) {
						String[] array = typesInDbpedia.toString().split("/");
						String ID = array[array.length - 1].replace("]", "");
						IDlists.add(ID);

					}
				}

			}
			findWikidataIDs.put(subject, IDlists);

		} else if (distance > 1) {

			ArrayList<String> IDlists = new ArrayList<String>();

			HashSet<String> finalVariables = new HashSet<String>();
			// except the subject and object variables

			char relationVariable = 'b';
			String[] relationVariablesList = new String[distance * 2];

			int k = 1;

			for (int i = 0; i < distance * 2;) {

				relationVariablesList[i] = "?" + relationVariable + k;
				relationVariablesList[i + 1] = ".";

				finalVariables.add(relationVariablesList[i]);

				i = i + 2;
				k++;

				relationVariable = (char) (relationVariable + 1);
			}

			char otherVariable = 'b';// (char) (relationVariable+1);
			String[] otherVariablesList = new String[2 * distance];
			otherVariablesList[0] = "?a";

			otherVariablesList[2 * distance - 1] = "?z";

			int z = 2;
			for (int i = 1; i < 2 * distance - 1;) {

				otherVariablesList[i] = "?" + otherVariable + z;
				otherVariablesList[i + 1] = "?" + otherVariable + z;

				finalVariables.add(otherVariablesList[i]);

				i = i + 2;
				z++;

				otherVariable = (char) (otherVariable + 1);
			}
			String[] finalElements = new String[4 * distance];

			int j = 0;
			for (int i = 0; i < distance * 4;) {

				finalElements[i] = otherVariablesList[j];
				finalElements[i + 1] = relationVariablesList[j];
				i = i + 2;
				j++;

			}

			String joinFilter = "filter(?a=<" + triple.get_subject() + ">)";

			String singleSparql = "select distinct ?z where { ";

			for (int i = 0; i < finalElements.length; i++) {

				singleSparql = singleSparql + " " + finalElements[i];

			}

			singleSparql = singleSparql + joinFilter + " }";

			System.out.println("\n singleSparql: " + singleSparql);

			HashSet<String> distinctTargets = new RDF3XEngine().getDistinctEntity(singleSparql);

			// SELECT distinct ?a WHERE { ?b owl:sameAs ?a. FILTER(regex(str(?a),
			// "www.wikidata.org" ) && ?b=<http://dbpedia.org/resource/Tristram_Hillier>) }
			System.out.println("\n sparql: " + singleSparql);
			if (!distinctTargets.isEmpty()) {

				for (String key1 : distinctTargets) {

					String key = key1.substring(0, 1).toUpperCase() + key1.substring(1);

					String sparqlDbpedia = "SELECT distinct ?b WHERE { ?a owl:sameAs ?b. "
							+ "FILTER(regex(str(?b),\"www.wikidata.org\")" + " && ?a=<http://dbpedia.org/resource/"
							+ key + ">)}";

					HashSet<String> typesInDbpedia = new Sparql().getSingleResultsFromQuery(sparqlDbpedia, dbpediaWeb);
					// System.out.println("typesInDbpedia:" + typesInDbpedia);

					if (typesInDbpedia.size() == 1) {
						String[] array = typesInDbpedia.toString().split("/");
						String ID = array[array.length - 1].replace("]", "");
						IDlists.add(ID);

					} else if (typesInDbpedia.isEmpty()) {
						System.out.println("key1:" + key1);
						System.out.println("sparqlDbpedia:" + sparqlDbpedia);
					}
				}

			}
			findWikidataIDs.put(subject, IDlists);

			HashMap<String, Double> subjectsResources = new RDF3XEngine().getCountSingleEntity(singleSparql);

			allResources.putAll(subjectsResources);

		}

		System.out.println("\n group: " + allResources.size() / distance);

		return findWikidataIDs;

	}

	public String findTargetSparql(String subject, int distance) {

		String sparql = null;

		if (distance == 1) {

			sparql = "select distinct ?c where{ ?a ?p ?c. filter (?a=<" + subject + ">)  }";
			// select distinct ?c where{ ?a ?b ?c. filter(?a =<Joseph_S._O-Leary>)}

		} else if (distance > 1) {

			HashSet<String> finalVariables = new HashSet<String>();
			// except the subject and object variables

			char relationVariable = 'b';
			String[] relationVariablesList = new String[distance * 2];

			int k = 1;

			for (int i = 0; i < distance * 2;) {

				relationVariablesList[i] = "?" + relationVariable + k;
				relationVariablesList[i + 1] = ".";

				finalVariables.add(relationVariablesList[i]);

				i = i + 2;
				k++;

				relationVariable = (char) (relationVariable + 1);
			}

			char otherVariable = 'b';// (char) (relationVariable+1);
			String[] otherVariablesList = new String[2 * distance];
			otherVariablesList[0] = "?a";

			otherVariablesList[2 * distance - 1] = "?z";

			int z = 2;
			for (int i = 1; i < 2 * distance - 1;) {

				otherVariablesList[i] = "?" + otherVariable + z;
				otherVariablesList[i + 1] = "?" + otherVariable + z;

				finalVariables.add(otherVariablesList[i]);

				i = i + 2;
				z++;

				otherVariable = (char) (otherVariable + 1);
			}
			String[] finalElements = new String[4 * distance];

			int j = 0;
			for (int i = 0; i < distance * 4;) {

				finalElements[i] = otherVariablesList[j];
				finalElements[i + 1] = relationVariablesList[j];
				i = i + 2;
				j++;

			}

			String joinFilter = "filter(?a=<" + subject + ">)";

			sparql = "select distinct ?z where { ";

			for (int i = 0; i < finalElements.length; i++) {

				sparql = sparql + " " + finalElements[i];

			}

			sparql = sparql + joinFilter + " }";

			// System.out.println("\n singleSparql: " + singleSparql);

		}

		return sparql;
	}

	public HashMap<String, ArrayList<String>> findNoWikidataID(Triple triple, int distance) {
		String subject = triple.get_subject();

		HashMap<String, ArrayList<String>> findWikidataIDs = new HashMap<String, ArrayList<String>>();

		HashMap<String, ArrayList<String>> findNoneIDtarget = new HashMap<String, ArrayList<String>>();


		ArrayList<String> IDlists = new ArrayList<String>();

		ArrayList<String> noIDlists = new ArrayList<String>();
		
		String sparql =findTargetSparql( subject,  distance);
		System.out.println("sparql--test:"+sparql);

		HashSet<String> distinctTargets = new RDF3XEngine().getDistinctEntity(sparql);

		// SELECT distinct ?a WHERE { ?b owl:sameAs ?a. FILTER(regex(str(?a),
		// "www.wikidata.org" ) && ?b=<http://dbpedia.org/resource/Tristram_Hillier>) }
		System.out.println("\n sparql: " + sparql);

		if (!distinctTargets.isEmpty()) {

			for (String key1 : distinctTargets) {

				String key = key1.substring(0, 1).toUpperCase() + key1.substring(1);

				String sparqlDbpedia = "SELECT distinct ?b WHERE { ?a owl:sameAs ?b. "
						+ "FILTER(regex(str(?b),\"www.wikidata.org\")" + " && ?a=<http://dbpedia.org/resource/" + key
						+ ">)}";

				HashSet<String> typesInDbpedia = new Sparql().getSingleResultsFromQuery(sparqlDbpedia, dbpediaWeb);
				// System.out.println("typesInDbpedia:" + typesInDbpedia);

				if (typesInDbpedia.size() == 1) {
					String[] array = typesInDbpedia.toString().split("/");
					String ID = array[array.length - 1].replace("]", "");
					IDlists.add(ID);

				} else if (typesInDbpedia.isEmpty()) {
					// System.out.println("key1:" + key1);
					// System.out.println("sparqlDbpedia:" + sparqlDbpedia);
					noIDlists.add(key1);
				}
			}

			findWikidataIDs.put(subject, IDlists);
			findNoneIDtarget.put(subject, noIDlists);

		}

		System.out.println("\n no wikidata id size: " + findNoneIDtarget.size());

		return findNoneIDtarget;

	}
	
	public HashMap<String, ArrayList<String>> findWikidataIDs(Triple triple, int distance) {
		String subject = triple.get_subject();

		HashMap<String, ArrayList<String>> findWikidataIDs = new HashMap<String, ArrayList<String>>();

		HashMap<String, ArrayList<String>> findNoneIDtarget = new HashMap<String, ArrayList<String>>();


		ArrayList<String> IDlists = new ArrayList<String>();

		ArrayList<String> noIDlists = new ArrayList<String>();
		
		String sparql =findTargetSparql( subject,  distance);
		System.out.println("sparql--test:"+sparql);

		HashSet<String> distinctTargets = new RDF3XEngine().getDistinctEntity(sparql);
		System.out.println("sparql--test:"+distinctTargets.size());

		// SELECT distinct ?a WHERE { ?b owl:sameAs ?a. FILTER(regex(str(?a),
		// "www.wikidata.org" ) && ?b=<http://dbpedia.org/resource/Tristram_Hillier>) }
		System.out.println("\n sparql: " + sparql);

		if (!distinctTargets.isEmpty()) {

			for (String key1 : distinctTargets) {

				String key = key1.substring(0, 1).toUpperCase() + key1.substring(1);

				String sparqlDbpedia = "SELECT distinct ?b WHERE { ?a owl:sameAs ?b. "
						+ "FILTER(regex(str(?b),\"www.wikidata.org\")" + " && ?a=<http://dbpedia.org/resource/" + key
						+ ">)}";
				System.out.println("\n sparqlDbpedia: " + sparqlDbpedia);

				HashSet<String> typesInDbpedia = new Sparql().getSingleResultsFromQuery(sparqlDbpedia, dbpediaWeb);
				// System.out.println("typesInDbpedia:" + typesInDbpedia);

				if (typesInDbpedia.size() == 1) {
					String[] array = typesInDbpedia.toString().split("/");
					String ID = array[array.length - 1].replace("]", "");
					IDlists.add(ID);

				} else if (typesInDbpedia.isEmpty()) {
					// System.out.println("key1:" + key1);
					// System.out.println("sparqlDbpedia:" + sparqlDbpedia);
					noIDlists.add(key1);
				}
			}

			findWikidataIDs.put(subject, IDlists);
			findNoneIDtarget.put(subject, noIDlists);
			
			

		}

		System.out.println("\n has wikidata IDlists: " + IDlists.size());
		System.out.println("\n has wikidata noIDlists: " + noIDlists.size());

		return findWikidataIDs;

	}

	public static void main(String[] args) {

		
		Triple candidate3 = new Triple("Joseph_S._O-Leary", "isInterestedIn", "Jansenism");
		Triple candidate = new Triple("Tristram_Hillier", "isCitizenOf", "Korean_War");// United_Kingdom

		Triple candidate1 = new Triple("Tristram_Hillier", "isCitizenOf", "United_Kingdom");

		int distance = 4;

		// HashSet<Triple> criteriaDistance = new
		// FilterCandidates().criteria4ColsedPathDistance(candidate, distance);

		// HashSet<Triple> asq = new
		// FilterCandidates().criteria1ChangedObjects(candidate3);
		


		// HashSet<Triple> criteriaDistance = new
		// FilterCandidates().criteria4ColsedPathDistance(candidate, distance);

		// HashSet<Triple> asq = new
		// FilterCandidates().criteria1ChangedObjects(candidate3);

		HashMap<String, ArrayList<String>> as = new InstanceCriteria().findWikidataIDs(candidate1, distance);
		
		HashMap<String, ArrayList<String>> as1 = new InstanceCriteria().findNoWikidataID(candidate1, distance);

		System.out.println(as);
		System.out.println(as1);

	}

}
