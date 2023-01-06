package gilp.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Generate input for AMIE.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Rdf2tsv {
	private final static Logger logger = LogManager.getLogger(Rdf2tsv.class);

	public static void main(String[] args) throws Exception {
		String filePath="./knowledgeClean-data/data_MLN/benchmark/wn18/wordnet-mlj12-valid.nt";

		String targetPath="./knowledgeClean-data/data_MLN/rdf2tsv/wordnet-mlj12-valid-model.tsv";

		run(filePath,targetPath);
		
	}
	
	public static void run(String filePath,String targetPath)
			throws FileNotFoundException {

		PrintWriter pw = new PrintWriter(targetPath);

		StreamRDF stream = new StreamRDF() {

			@Override
			public void triple(Triple triple) {
				pw.write(parse(triple.getSubject()) + "\t"
						+ triple.getPredicate().getURI() + "\t"
						+ triple.getObject().toString() + "\n");
			}

			@Override
			public void start() {
			}

			@Override
			public void quad(Quad quad) {
			}

			@Override
			public void prefix(String prefix, String iri) {
			}

			@Override
			public void finish() {
			}

			@Override
			public void base(String base) {// base is part of path---????
			}

		};

		RDFDataMgr.parse(stream, filePath);

		pw.close();

	}

	public static String parse(Node r) {
		String s;
		try {
			s = r.getURI();
		} catch (UnsupportedOperationException e) {
			logger.debug(e.getMessage());
			s = r.getBlankNodeLabel();
			logger.debug("Changing to "+s);
		}
		return s;
	}


}
