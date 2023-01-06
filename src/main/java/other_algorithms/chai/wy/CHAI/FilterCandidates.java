package other_algorithms.chai.wy.CHAI;

import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.RDFSubGraphSet;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;

import java.util.*;

/**
 * Note that a criterion is a predicate that assigns a True/False binary label
 * to a candidate triple.
 * 
 * four criteria to filter candidates
 * 
 * in the CHAI model, the given triples that has the binary label in the
 * train.txt and test.txt.
 * 
 * So, given one labeled triples, using the criteria to filtering candidates.
 * 
 * in the CHAI, the candidate as a triple(s,r,t) has a chance of representing
 * real -world knowledge, even if it does not exist in T(the set of triples).
 * 
 * if the triple is true, then the filtered candidate is also true.
 * 
 * @author wy
 *
 */

public class FilterCandidates {

	// criteria 1: existing source entity and relation
	// <s,r,t> <=> <s,r,e> in the T
	// for the yago, we use the sparql to filter the candidates.

	public HashSet<Triple> criteria1ChangedObjects(Triple triple) {

		HashSet<Triple> filterCandidates = new HashSet<>();
		String sub = triple.get_subject();
		String obj = triple.get_obj();
		String relation = triple.get_predicate();

		String sparql = "select distinct ?c where{ ?a ?p ?c. filter(?a =<" + sub + "> && ?p=<" + relation
				+ "> && ?c !=<" + obj + ">)}";

		HashSet<String> distinctTargets = new RDF3XEngine().getDistinctEntity(sparql);

		if (!distinctTargets.isEmpty()) {

			for (String key : distinctTargets) {

				Triple target = new Triple(sub, relation, key);
				filterCandidates.add(target);
			}
		}

		return filterCandidates;

	}

	/**
	 * target is the object, Target is in the domain of a relation
	 * 
	 * <s,r,t> <=> <t,rel,e> in T
	 * 
	 * @param triple
	 * @return
	 */
	public HashSet<Triple> criteria2TargetIsDomain(Triple triple) {

		HashSet<Triple> filterCandidates = new RDF3XEngine().getTriplesBySubject(triple.get_obj());
		filterCandidates.remove(triple);

		return filterCandidates;

	}

	/**
	 * target is the object, Target is in the range of a relation
	 * 
	 * <s,r,t> <=> <e,rel,t> in T
	 * 
	 * @param triple
	 * @return
	 */
	public HashSet<Triple> criteria3TargetIsRange(Triple triple) {

		HashSet<Triple> filterCandidates = new RDF3XEngine().getTriplesByObject(triple.get_obj());
		filterCandidates.remove(triple);

		return filterCandidates;

	}

	/**
	 * entities are within distance
	 * 
	 * select all candidates whose source and target entities have a distance
	 * between them that is at most i, at most, distance =2 or 3
	 * 
	 * closed-path way, the triple's subject and object are not changed.
	 * 
	 * @param triple
	 * @return
	 */
	public HashSet<Triple> criteria4ClosedPathDistance(Triple triple, int distance) {

		HashSet<Triple> filterCandidates = new HashSet<>();

		if (distance == 1) {

			String sparql = "select ?p where{ ?a ?p ?c. filter (?a=<" + triple.get_subject() + "> && ?c=<"
					+ triple.get_obj() + "> && ?p!=<" + triple.get_predicate() + ">) }";

			HashSet<String> distinctTargets = new RDF3XEngine().getDistinctEntity(sparql);

			if (!distinctTargets.isEmpty()) {

				for (String key : distinctTargets) {

					Triple candidate = new Triple(triple.get_subject(), key, triple.get_obj());
					filterCandidates.add(candidate);
				}
			}

		} else if (distance > 1) {

			char relationVariable = 'b';
			String[] relationVariablesList = new String[distance * 2];

			int k = 1;

			for (int i = 0; i < distance * 2;) {

				relationVariablesList[i] = "?" + relationVariable + k;
				relationVariablesList[i + 1] = ".";
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

			String joinFilter = "filter(?a=<" + triple.get_subject() + "> && ?z=<" + triple.get_obj() + ">)";
			String unionFilter = "filter(?a=<" + triple.get_subject() + "> || ?z=<" + triple.get_obj() + ">)";

			StringBuilder joinSparql = new StringBuilder("select * where { ");

			StringBuilder unionSparql = new StringBuilder("select * where { ");

			for (String finalElement : finalElements) {

				joinSparql.append(" ").append(finalElement);

				unionSparql.append(" ").append(finalElement);

			}

			joinSparql.append(joinFilter).append(" }");

			unionSparql.append(unionFilter).append(" }");

			System.out.println("\n joinSparql: " + joinSparql);

			// System.out.println("\n unionSparql: " + unionSparql);
			// union Sparql has too many results, here,we don't consider this way.

			Clause clause = new Clause();

			for (int i = 0; i < distance * 4; i = i + 4) {

				clause.addPredicate(new RDFPredicate(finalElements[i], finalElements[i + 1], finalElements[i + 2]));

			}

			RDFSubGraphSet sg_set = new RDF3XEngine().doYagoQuery(clause, joinSparql.toString());

			HashSet<Triple> subTriple = sg_set.getAllTriples();

			filterCandidates.addAll(subTriple);

		}

		filterCandidates.remove(triple);

		System.out.println("\n group: " + filterCandidates.size() / distance);

		return filterCandidates;

	}

	/**
	 * source entity is the triple's subject, find the ID of the target entities
	 * based on the distance, the ID from the entities.txt file, using the train.txt
	 * to find the target's ID in the CHAI model. the ID of the target entities
	 * 
	 * in the yago database, we do some changing.
	 * 
	 * @param triple
	 * @param distance
	 * @return
	 */

	public HashMap<String, Double> criteria4FixedSourceDistance(Triple triple, int distance) {
		// select count ?e4 where { ?a ?b1 ?b2 . ?b2 ?c2 ?c3 . ?c3 ?d3 ?d4 . ?d4 ?e4 ?z
		// .filter(?a=<Tristram_Hillier>) }
		String subject = triple.get_subject();

		HashMap<String, Double> allResources = new HashMap<>();

		if (distance == 1) {

			String sparql = "select count ?c where{ ?a ?p ?c. filter (?a=<" + subject + ">)  }";
			// select count ?b where{ ?a ?b ?c. filter(?a =<Joseph_S._O-Leary>)}

			System.out.println("\n sparql: " + sparql);

			HashMap<String, Double> subjectsResources = new RDF3XEngine().getCountSingleEntity(sparql);
			allResources.putAll(subjectsResources);

		} else if (distance > 1) {

			HashSet<String> finalVariables = new HashSet<>();
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

			List<String> al = new ArrayList<>(finalVariables);

			Collections.sort(al);

			String joinFilter = "filter(?a=<" + triple.get_subject() + ">)";

			for (String key : al) {

				System.out.println("\n key: " + key);

				StringBuilder singleSparql = new StringBuilder("select count " + key + " where { ");

				for (String finalElement : finalElements) {

					singleSparql.append(" ").append(finalElement);

				}

				singleSparql.append(joinFilter).append(" }");

				System.out.println("\n singleSparql: " + singleSparql);

				HashMap<String, Double> subjectsResources = new RDF3XEngine().getCountSingleEntity(singleSparql.toString());

				allResources.putAll(subjectsResources);

				System.out.println("\n subjectsResources: " + subjectsResources);
				System.out.println("\n subjectsResources--size: " + subjectsResources.size());

			}
		}

		System.out.println("\n group: " + allResources.size() / distance);
		System.out.println("\n allResources: " + allResources);

		return allResources;

	}

	public HashMap<String, Double> criteria4FindTargetsDistance(Triple triple, int distance) {
		String subject = triple.get_subject();

		HashMap<String, Double> allResources = new HashMap<>();

		if (distance == 1) {

			String sparql = "select count ?c where{ ?a ?p ?c. filter (?a=<" + subject + ">)  }";
			// select count ?b where{ ?a ?b ?c. filter(?a =<Joseph_S._O-Leary>)}

			System.out.println("\n sparql: " + sparql);

			HashMap<String, Double> subjectsResources = new RDF3XEngine().getCountSingleEntity(sparql);
			allResources.putAll(subjectsResources);

		} else if (distance > 1) {

			HashSet<String> finalVariables = new HashSet<>();
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

			StringBuilder singleSparql = new StringBuilder("select count ?z where { ");

			for (String finalElement : finalElements) {

				singleSparql.append(" ").append(finalElement);

			}

			singleSparql.append(joinFilter).append(" }");

			System.out.println("\n singleSparql: " + singleSparql);

			HashMap<String, Double> subjectsResources = new RDF3XEngine().getCountSingleEntity(singleSparql.toString());

			allResources.putAll(subjectsResources);

		}

		System.out.println("\n group: " + allResources.size() / distance);

		return allResources;

	}

	// the rule is the criteria1 join (criteria2 or criteria3 or ....)
	public HashSet<Triple> filterRules(Triple triple, int distance) {
		// enforcing the exists ,the criteria1

		HashSet<Triple> filterTriples = new HashSet<>();

		HashSet<Triple> filterOtherTriples = new HashSet<>();
		HashSet<Triple> criteria1 = criteria1ChangedObjects(triple);
		HashSet<Triple> criteria2 = criteria2TargetIsDomain(triple);
		HashSet<Triple> criteria3 = criteria3TargetIsRange(triple);

		HashSet<Triple> criteria4 = criteria4ClosedPathDistance(triple, distance);

		filterOtherTriples.addAll(criteria2);
		filterOtherTriples.addAll(criteria3);
		filterOtherTriples.addAll(criteria4);
		if (!criteria1.isEmpty()) {
			filterTriples.addAll(criteria1);
			filterTriples.removeAll(filterOtherTriples);

		} else {

			filterTriples.addAll(criteria1);

		}

		return filterTriples;

	}

	public double get_fitness(double recall, double rr) {

		return (2 * recall * rr) / (recall + rr);
	}

	public static void main(String[] args) {

	//	Triple candidate3 = new Triple("Joseph_S._O-Leary", "isInterestedIn", "Jansenism");
		Triple candidate = new Triple("Tristram_Hillier", "isCitizenOf", "Korean_War");// United_Kingdom

		Triple candidate1 = new Triple("Tristram_Hillier", "isCitizenOf", "United_Kingdom");

		int distance = 4;

		// HashSet<Triple> criteriaDistance = new
		// FilterCandidates().criteria4ClosedPathDistance(candidate, distance);

		// HashSet<Triple> asq = new
		// FilterCandidates().criteria1ChangedObjects(candidate3);

		HashMap<String, Double> as = new FilterCandidates().criteria4FixedSourceDistance(candidate1, distance);

		System.out.println(as.size());

	}

}
