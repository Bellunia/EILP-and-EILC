package MLNs.util;

import org.apache.jena.graph.Node;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Add blank-node support.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class URIHandler {
	
	private final static Logger logger = LogManager.getLogger(URIHandler.class);
	
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
