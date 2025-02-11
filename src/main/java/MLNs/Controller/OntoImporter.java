package MLNs.Controller;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import MLNs.util.PrettyRandom;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.TreeSet;

/**
 * Ontologies cannot be imported using Jena, because most datasets are not
 * actual OWL files, thus they do not provide meta-information about used and
 * imported ontologies. For instance, an N-Triples file could be using URIs
 * which are referred only in the file itself. Phisically visiting these URIs is
 * a method to retrieve their definition. In this version, we limit the URIs to
 * classes and properties.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 * @version 0.0.1
 *
 */
public class OntoImporter {
	
	private final static Logger logger = LogManager.getLogger(OntoImporter.class);
	
	private static final Lang[] LANG_ATTEMPTS = {Lang.RDFXML, Lang.TTL, Lang.NT};

	/**
	 * @param BASE
	 * @param paths
	 */
	public static void run(final String BASE, final String[] paths) {

		final CollectionCache properties = new CollectionCache();
		final CollectionCache classes = new CollectionCache();

		final FileOutputStream output;
		try {
			output = new FileOutputStream(BASE + "/model-tmp.nt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		final StreamRDF writer = StreamRDFWriter.getWriterStream(output, Lang.NT);

		// reader implementation
		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void base(String arg0) { }

			@Override
			public void finish() { }

			@Override
			public void prefix(String arg0, String arg1) { }

			@Override
			public void quad(Quad arg0) { }

			@Override
			public void start() {
				writer.start();
			}

			@Override
			public void triple(Triple triple) {
				String s = triple.getSubject().getURI();
				String p = triple.getPredicate().getURI();
				String o = triple.getObject().toString();

				// if property is rdf:type...
			//	System.out.println("rdf:type : "+RDF.type.getURI());//http://www.w3.org/1999/02/22-rdf-syntax-ns#type
				if (p.equals(RDF.type.getURI())) {
					// save class
					// TODO this could be extended to all properties with domain
					// or range = rdfs:Class
					classes.set.add(o);
					// as well as all super-classes of rdfs:Class
					System.out.println("rdfs:Class : "+RDFS.Class.getURI());


					if(o.equals(RDFS.Class.getURI()) || 
							o.equals(OWL.Class.getURI()))
						classes.set.add(s);
				}
				// save property
				properties.set.add(p);
				
				// write triple
				writer.triple(triple);

			}

		};
		
		for(String path : paths)// the train files is :<Hermann_Hreiðarsson>	<Reykjavík>	<wasBornIn>	. must be the .nt files
			RDFDataMgr.parse(dataStream, path);
		
		logger.info("# classes collected = "+classes.set.size());
		logger.info("# properties collected = "+properties.set.size());
		
		// ontology importer
		for(String uri : classes.set) {
			logger.trace("Crawling <"+uri+">...");
			Model model = ModelFactory.createDefaultModel();
			// visit URIs in classes and properties
			String path = BASE + "/temp-file-" + PrettyRandom.get(6) + "";
			File file = new File(path);
			try {
				FileUtils.copyURLToFile(new URL(uri), file);
			} catch (IOException e) {
				logger.warn("Cannot download <"+uri+">.");
				continue;
			}
			logger.trace("Saved to "+path+".");
			for(Lang lang : LANG_ATTEMPTS) {
				try {
					logger.trace("Trying with "+lang);
					RDFDataMgr.read(model, path, lang);
					break;
				} catch (RiotException e) {
					logger.warn("Cannot interpret <"+uri+"> using "+lang+".");
				}
			}
			logger.trace("# statements: "+model.size());
			StmtIterator list = model.listStatements();
			// append NT files to model...
			while(list.hasNext()) {
				// save wanted part of RDF files
				Statement stmt = list.next();
				
				logger.trace(stmt);
				
				boolean imprt = stmt.getPredicate().getURI().equals(uri);
				
				if(!imprt)
					if(stmt.getSubject().isURIResource())
						if(stmt.getSubject().getURI().equals(uri))
							imprt = true;
				if(!imprt)
					if(stmt.getObject().isURIResource())
						if(stmt.getObject().asResource().getURI().equals(uri))
							imprt = true;
				
				if(imprt) {
					Triple t = stmt.asTriple();
					logger.trace(t);
					writer.triple(t);
				}
			}
		}
		writer.finish();

	}

}

class CollectionCache {
	TreeSet<String> set = new TreeSet<>();
}