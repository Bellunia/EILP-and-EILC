/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/

package prediction_type.hermitkem;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This example Shows how to use HermiT as an OWLReasoner for materialising inferences. 
 * The program loads the pizza ontology, computes implicit subclass relationships and
 * class assertion axioms and saves them into a new ontology in the same format as the 
 * input ontology. Further inferences can be added by adding more InferredAxiomGenerators.  
 */
public class MaterializeObjectProperty {
   
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
    	 OWLOntologyManager manager = OWLManager.createOWLOntologyManager();    	        	 
    	    //OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("/home/ktgroup/Documents/kems/experiment RQ1/ISWC2018/MateFlow2/Iteration3/Ite3OntoData.ttl"));
    	 	OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\r01krw16\\Documents\\_Experimentson3rdyear\\EKAW18 Experiments\\Mate-STransE\\MateSTMerge.ttl"));
    	 	//OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("/home/ktgroup/workspaceJan18/OWLAPIKem/msc/ontologies/small4Test.owl"));
    	    //OWLDataFactory df = manager.getOWLDataFactory();

    	    Configuration configuration = new Configuration();
    	    configuration.ignoreUnsupportedDatatypes = true;
    	    ReasonerFactory rf = new ReasonerFactory();

    	    OWLReasoner reasoner = rf.createReasoner(ontology, configuration);
    	    boolean consistencyCheck = reasoner.isConsistent();
    	    if (consistencyCheck) {
    	        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
    	            InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_HIERARCHY,
    	            InferenceType.DATA_PROPERTY_HIERARCHY, InferenceType.OBJECT_PROPERTY_ASSERTIONS);

    	        List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<>();
    	        generators.add(new InferredSubClassAxiomGenerator());
    	        generators.add(new InferredClassAssertionAxiomGenerator());
    	        generators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
    	        generators.add(new InferredEquivalentClassAxiomGenerator());
    	        generators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
    	        generators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
    	        generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
    	        generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());

    	        // NOTE: InferredPropertyAssertionGenerator significantly slows down
    	        // inference computation
    	        generators.add(new org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());

    	        generators.add(new InferredSubClassAxiomGenerator());
    	        generators.add(new InferredSubDataPropertyAxiomGenerator());
    	        generators.add(new InferredSubObjectPropertyAxiomGenerator());
    	        List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms =
    	            new ArrayList<>();
    	        generators.addAll(individualAxioms);

    	        generators.add(new InferredDisjointClassesAxiomGenerator());
    	        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, generators);
    	        OWLOntology inferredAxiomsOntology = manager.createOntology();
    	        iog.fillOntology ((OWLDataFactory) manager, inferredAxiomsOntology);
    	        File inferredOntologyFile = new File("C:\\Users\\r01krw16\\Documents\\_Experimentson3rdyear\\EKAW18 Experiments\\Mate-STransE\\MateSTMergeAfter.ttl");
    	        // Now we create a stream since the ontology manager can then write to that stream.
    	        try (OutputStream outputStream = new FileOutputStream(inferredOntologyFile)) {
    	            // We use the same format as for the input ontology.
    	            manager.saveOntology     (inferredAxiomsOntology, outputStream);
    	        }
    	    } // End if consistencyCheck
    	    else {
    	        System.out.println("Inconsistent input Ontology, Please check the OWL File");
    	    }
    }
}