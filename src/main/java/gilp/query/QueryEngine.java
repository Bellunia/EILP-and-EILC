package gilp.query;

import gilp.rdf3x.RDFSubGraphSet;
import gilp.rdf3x.Triple;

import gilp.rules.Clause;

import java.util.ArrayList;

public interface QueryEngine {
	
	public Triple getTripleByTid (String tid);
	
	public ArrayList<Triple>  getTriplesBySubject (String subject);
	
	public ArrayList<Triple>  getTriplesByPredicate (String predicate);
	
	public ArrayList<Triple>  getAllTriples ();
	
	public RDFSubGraphSet getTriplesByCNF (Clause cls);
	
	 
}
