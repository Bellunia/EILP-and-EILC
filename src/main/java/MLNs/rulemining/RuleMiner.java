package MLNs.rulemining;

import MLNs.Controller.NameMapper;
import amie.rules.Rule;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


import MLNs.rulemining.AmieHandler.MiningStrategy;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import amie.data.KB;

import java.util.*;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class RuleMiner {
	
	private final static Logger logger = LogManager.getLogger(RuleMiner.class);

	/**
	 * @param map
	 * @param base
	 * @param mining
	 * @param maxRules
	 * @throws Exception
	 */
	public static void run(NameMapper map, String base, Double mining, Integer maxRules) throws Exception {

		boolean support = (mining == null);
		
		AmieHandler h = new AmieHandler(base + "/model.tsv");// model got from rdfto tsv
		
		if(!support)  {
			h.setMiningThr(mining);
			h.run(MiningStrategy.HEAD_COVERAGE);
			if(h.getRules().isEmpty())
				support = true;
		}
		
		if(support) {
			h.run(MiningStrategy.SUPPORT);
			if(h.getRules().isEmpty()) {
				logger.fatal("Rules size = 0");
				throw new RuntimeException("Mandolin cannot continue without MLN rules!");
			}
		}
		
		List<Rule> rules = h.getRules();
		if(rules.isEmpty()) {
			logger.fatal("Rules size = 0");
			throw new RuntimeException("Mandolin cannot continue without MLN rules!");
		}
		
		TreeSet<String> topNRules = new TreeSet<>();
		if(maxRules != null) {
			HashMap<String, Double> rank = new HashMap<>();
			for(Rule rule : rules)
				rank.put(rule.toString(), rule.getPcaConfidence());
			ValueComparator bvc = new ValueComparator(rank);
	        TreeMap<String, Double> sortedRank = new TreeMap<>(bvc);
	        sortedRank.putAll(rank);
	        int i=0;
	        for(String key : sortedRank.keySet()) {
	        	topNRules.add(key);
	        	logger.trace(key + ", " + rank.get(key));
	        	if(++i == maxRules)
	        		break;
	        }
		}
		
		RuleDriver driver = new RuleDriver(map, base);
		
		for(Rule rule : rules) {
			
			if(maxRules != null)
				if(!topNRules.contains(rule.toString()))
					continue;
			
			// filter out RDF/RDFS/OWL-only rules
			if(isUpper(rule.getHeadRelation())) {
				boolean skip = true;
				for(int[] bs : rule.getBody())
					if(!isUpper(KB.unmap(bs[1]).replace("<", "").replace(">", "") )) {
	//bs[1].toString()
						skip = false;
						break;
					}
				if(skip) {
					logger.trace("Skipping upper-ontology rule...");
					continue;
				}
			}
						
			// send rule to driver
			driver.process(rule);
			// print rule information
			printInfo(rule);
		}
		
		// make CSVs
		driver.buildCSV();

	}

	/**
	 * @param rule
	 */
	private static void printInfo(Rule rule) {
		String str = "";
		for(int[] bs : rule.getBody()) {
			String bstr = "";
			for(int b : bs)
				bstr += KB.unmap(b).replace("<", "").replace(">", "") + ",";
			str += bstr + " | ";
		}
		logger.info(rule.getHeadRelation() + "\t" + str + "\t" + rule.getPcaConfidence());		
	}

	/**
	 * @param headRelation
	 * @return
	 */
	private static boolean isUpper(String headRelation) {
		if(headRelation.startsWith(OWL.NS))
			return true;
		if(headRelation.startsWith(RDF.getURI()))
			return true;
		if(headRelation.startsWith(RDFS.getURI()))
			return true;
		return false;
	}
	
}

class ValueComparator implements Comparator<String> {
	
    Map<String, Double> base;

    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
    
}
