package MLNs.model;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import MLNs.util.URIHandler;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Generate input for AMIE.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RDFToTSV {

	public static void main(String[] args) throws Exception {
		
		run("./data/benchmark/wn18");//"eval/0001"

	//	String base--the path location
		
	}
	
	public static void run(String base)
			throws FileNotFoundException {

		PrintWriter pw = new PrintWriter(base + "/model.tsv");

		StreamRDF stream = new StreamRDF() {

			@Override
			public void triple(Triple triple) {
				pw.write(URIHandler.parse(triple.getSubject()) + "\t"
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
			public void base(String base) {
			}

		};

		RDFDataMgr.parse(stream, base + "/model-fwc.nt");///model-fwc.nt

		pw.close();

	}

}
