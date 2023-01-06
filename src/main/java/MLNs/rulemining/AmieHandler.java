package MLNs.rulemining;

import amie.mining.AMIE;
import amie.mining.assistant.MiningAssistant;
import amie.rules.Rule;
import javatools.parsers.NumberFormatter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

public class AmieHandler {
	
	private final static Logger logger = LogManager.getLogger(AmieHandler.class);
	
	public enum MiningStrategy {
		HEAD_COVERAGE, SUPPORT
	}

	private String ontology;
	private List<Rule> rules = null;
	private Double miningThr = 0.01;

	public AmieHandler(String ontology) {
		super();
		this.ontology = ontology;
	}

	public void run(MiningStrategy ms) throws Exception {

		AMIE miner;
		switch(ms) {
		case HEAD_COVERAGE:
			miner =	AMIE.getInstance(new String[] { ontology, "-minhc", String.valueOf(miningThr) });
			break;
		case SUPPORT:
			miner =	AMIE.getInstance(new String[] { ontology, "-pm", "support", "-mins", "0" });
			break;
		default:
			throw new RuntimeException("MiningStrategy does not exist: " + ms.name());
		}
		
		logger.info("Starting the mining phase");

		long time = System.currentTimeMillis();

		rules = miner.mine();
		MiningAssistant assistant = miner.getAssistant();
		if (!miner.isRealTime()) {
			printRuleHeaders(); //	Rule.printRuleHeaders();
			for (Rule rule : rules) {
				logger.info(assistant.formatRule(rule));//rule.getFullRuleString()
			}
		}

		long miningTime = System.currentTimeMillis() - time;
		logger.info("Mining done in "
				+ NumberFormatter.formatMS(miningTime));
		logger.info(rules.size() + " rules mined.");

	}

	public static void printRuleHeaders() {
		System.out.println("Rule\tHead Coverage\tStd Confidence\tPCA Confidence\tPositive Examples\tBody size\tPCA Body size\tFunctional variable\tStd. Lower Bound\tPCA Lower Bound\tPCA Conf estimation");
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setMiningThr(Double mining) {
		this.miningThr = mining;
	}

}
