package other_algorithms.chai.wy.CHAI;

import gilp.rdf3x.Triple;

import java.util.ArrayList;
import java.util.HashMap;

public class CHAItest {
	
	static public void main(String... argv) {
		Triple candidate3 = new Triple("Joseph_S._O-Leary", "isInterestedIn", "Jansenism");
		Triple candidate = new Triple("Tristram_Hillier", "isCitizenOf", "Korean_War");// United_Kingdom

		Triple candidate1 = new Triple("Tristram_Hillier", "isCitizenOf", "United_Kingdom");

		int distance = 3;

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
