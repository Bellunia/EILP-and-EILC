package prediction_type.hermitkem;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;

public class CheckConcistency {

	/**
	 * /2020-Schema Aware Iterative Knowledge Graph Completion/SAIKGC-master/dbpedia_2016-04.owl--2016-04 data and schema
	 * @param args
	 */
	public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException{
		long startTime = System.nanoTime();
		System.out.println("Application for checking the inconsistencies");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		//load the ontology
		//OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("/home/ktgroup/Documents/kems/experiment RQ1/temporaryFiles/step5/MergerPra.ttl"));
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("./type/ontology/dbpedia_2016-04.owl"
				//"C:\\Users\\r01krw16\\Documents\\_Experiments\\DBPedia\\2016-04 data and schema\\dbpedia_2016-04.owl"
		));
		ReasonerFactory factory = new ReasonerFactory();
		OWLReasoner reasoner=factory.createReasoner(ontology);
		System.out.println("The ontology is " + reasoner.isConsistent());
		
		
		long endTime = System.nanoTime();
		System.out.println("It took "+((endTime-startTime)/1000000)+" ms");
		
	}

}
