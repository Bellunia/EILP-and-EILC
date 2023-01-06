package gilp.sparql;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


//import asu.edu.rule_miner.rudik.RuleMinerException;
//import asu.edu.rule_miner.rudik.configuration.Constant;
//import asu.edu.rule_miner.rudik.sparql.jena.QueryJenaLibrary;

public class QuerySparqlRemoteEndpoint  {

	 protected QueryExecution openResource;
	 String sparqlEndpoint ="https://dbpedia.org/sparql";

	public ResultSet executeQuery(String sparqlQuery) {
		final QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery);
		final ResultSet results = qexec.execSelect();
		qexec.close();
		return results;
	}
	
	public void closeResources() {
	    if (this.openResource != null) {
	      this.openResource.close();
	      this.openResource = null;
	    }
	  }
	
	public HashMap<String, String> getResultsFromQuery(String queryStr) {
		ArrayList<HashMap<String, String>> queryResults = new ArrayList<HashMap<String, String>>();
		
		 // try to execute normal query
	    ResultSet results = null;
	    if (this.openResource != null) {
	        this.openResource.close();
	      }

	      results = this.executeQuery(queryStr);
	      HashMap<String, String> element = new HashMap<String, String>();
	    
	    while (results.hasNext()) {
	      final QuerySolution oneResult = results.next();
	      System.out.println(" oneResult Result: " + oneResult.toString() + "\n");
	      final String secondResult = oneResult.get("?x").toString();
	      
	      final String firstResult = oneResult.get("?xcount").toString();
	      System.out.println(" secondResult Result: " + secondResult.toString() + "\n");
	      System.out.println(" firstResult Result: " + firstResult.toString() + "\n");
	      
	      element.put(secondResult, firstResult);
	    }

	    this.closeResources();
	    
	  

		return element;
	}
	
	public void filterPositiveSample(String predicate) {
		// http://dbpedia.org/ontology/birthPlace
//subject-type:	http://dbpedia.org/resource/Person
		// object-type: http://dbpedia.org/resource/place
		String objectType = "select distinct ?x  (count(?x) as ?xcount) where{?a <"+predicate+"> ?b.?b a ?x."
				+ " FILTER(regex(str(?x),\"http://dbpedia.org/ontology/\"))} GROUP BY ?x ORDER BY DESC(?xcount) limit 1" ;
		
		String subjectType = "select distinct ?x  (count(?x) as ?count) where{?a <"+predicate+"> ?b.?a a ?x."
				+ " FILTER(regex(str(?x),\"http://dbpedia.org/ontology/\"))}";
		
ArrayList<HashMap<String, String>> objectTypeResults=new Sparql().getResultsFromQuery(objectType);

System.out.println(" objectTypeResults Result: " + objectTypeResults + "\n");
		
	//HashMap<String, Double> subjectcTypeResults=new Sparql().countResultsFromQuery(subjectType);

	}

	public static void main(String[] args) {
		String predicate = "http://dbpedia.org/ontology/birthPlace";
		new QuerySparqlRemoteEndpoint().filterPositiveSample(predicate);

	}

}
