package MLNs.reasoner;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.*;
import org.apache.jena.reasoner.ValidityReport;

import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.XSD;
import MLNs.util.Timer;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;


/**
 * apache-Jena reasoner. The inferred closure model is saved in file; it will
 * not be available as an in-memory object.
 *
 */
public class RDFSReasoner {

	private final static Logger logger = LogManager.getLogger(RDFSReasoner.class);

	public static void main(String[] args) {
		BasicConfigurator.configure();
		testThis();
//		 run("eval/0001");
	}

	/**
	 * Add OWL rules and compute the forward chain.
	 *
	 * @param base
	// * @param datasetPaths
	 */
	public static void run(String base) {
		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		OntModel ontModel = ModelFactory.createOntologyModel();
		InfModel infModel = ModelFactory.createInfModel(reasoner,ontModel );


	//	String path = System.getProperty("user.dir");--path + "/" +
		RDFDataMgr.read(infModel,   base + "/model.nt");//"file://" +

		logger.info("Model size = " + ontModel.size());

		ValidityReport report = infModel.validate();
		printIterator(report.getReports(), "Validation Results");

		logger.info("Inferred model size = " + infModel.size());

		infModel.enterCriticalSection(Lock.READ);

		try {
			RDFDataMgr.write(new FileOutputStream(base + "/model-fwc.nt"), infModel, Lang.NT);//System.getProperty("user.dir")+
			logger.info("Model generated.");
		} catch (FileNotFoundException e) {
			logger.fatal(e.getMessage());
			throw new RuntimeException("Necessary file model-fwc.nt was not generated.");
		} finally {
			infModel.leaveCriticalSection();
		}

		new File(base + "/model.nt").delete();

	}

	public static void closure(String input, String output) {

		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		OntModel ontModel = ModelFactory.createOntologyModel();
		InfModel infModel = ModelFactory.createInfModel(reasoner,ontModel );

		//String path = System.getProperty("user.dir");
		RDFDataMgr.read(infModel,  input);

		logger.info("Model = "+input+", size = " + ontModel.size());

		ValidityReport report = infModel.validate();
		printIterator(report.getReports(), "Validation Results");

		logger.info("Inferred model size = " + infModel.size());

		infModel.enterCriticalSection(Lock.READ);

		try {
			RDFDataMgr.write(new FileOutputStream(output),
					infModel, Lang.NT);
			logger.info("Model generated at "+output);
		} catch (FileNotFoundException e) {
			logger.fatal(e.getMessage());
			throw new RuntimeException("Necessary file "+output+" was not generated.");
		} finally {
			infModel.leaveCriticalSection();
		}

	}

	private static void testThis() {

		Timer t = new Timer();

		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		OntModel ontModel = ModelFactory.createOntologyModel();
		InfModel infModel = ModelFactory.createInfModel(reasoner,ontModel );

		t.lap();

		String path = System.getProperty("user.dir")+"/knowledgeClean-data/data_MLN/";

		String[] paths = {  path + "dblp-acmDBLPL3S.nt",
				 path + "LinkedACM.nt",
			 path + "DBLPL3S-LinkedACM.nt" };

		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void start() {}

			@Override
			public void quad(Quad quad) {}

			@Override
			public void base(String base) {}

			@Override
			public void prefix(String prefix, String iri) {}

			@Override
			public void finish() {}

			@Override
			public void triple(Triple triple) {
				Node node = triple.getObject();
				if (node.isLiteral()) {
					if (!node.getLiteral().isWellFormed()) {
						// known issue: fix gYear literals
						if (node.getLiteralDatatypeURI() != null) {
							if (node.getLiteralDatatypeURI().equals(
									XSD.gYear.getURI())
									|| node.getLiteralDatatypeURI().equals(
									XSD.gYear.getLocalName())) {
								Node newNode = NodeFactory.createLiteral(node
										.getLiteral().toString()
										.substring(0, 4)
										+ "^^" + XSD.gYear);
								triple = new Triple(triple.getSubject(),
										triple.getPredicate(), newNode);
//								logger.warn("Bad-formed literal: "
//										+ node + " - Using: " + newNode);
							}
						}
					}
				}

				Resource s = infModel.createResource(triple.getSubject()
						.getURI());
				Property p = infModel.createProperty(triple.getPredicate()
						.getURI());
				RDFNode o = infModel.asRDFNode(triple.getObject());

				infModel.add(s, p, o);
			}

		};

		for (String p : paths)
			RDFDataMgr.parse(dataStream, p);

		t.lap();

		logger.info("Model size = " + ontModel.size());

		ValidityReport report = infModel.validate();
		printIterator(report.getReports(), "Validation Results");

		logger.info("Inferred model size = " + infModel.size());

		infModel.enterCriticalSection(Lock.READ);

		String f = "tmp/test-this.nt";
		try {
			RDFDataMgr.write(new FileOutputStream(f), infModel, Lang.NT);
			logger.info("Model generated.");
		} catch (FileNotFoundException e) {
			logger.fatal(e.getMessage());
			throw new RuntimeException("Necessary file "+f+" was not generated.");
		} finally {
			infModel.leaveCriticalSection();
		}

		t.lap();

		logger.info("Reasoner init (ms): " + t.getLapMillis(0));
		logger.info("Model load (ms): " + t.getLapMillis(1));
		logger.info("Model load (ms/triple): " + t.getLapMillis(1)
				/ infModel.size());
		logger.info("Validation (ms): " + t.getLapMillis(2));
		logger.info("Save inferred model (ms): " + t.getLapMillis(3));
		printIterator(report.getReports(), "Validation Results");

	}

	private static void printIterator(Iterator<?> i, String header) {
		logger.info(header);

		if (i.hasNext()) {
			while (i.hasNext())
				logger.info(i.next());
		} else
			logger.info("<Nothing to say.>");

		logger.info("");
	}

}
