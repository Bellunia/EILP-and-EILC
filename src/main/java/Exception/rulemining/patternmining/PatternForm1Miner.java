package Exception.rulemining.patternmining;

import Exception.indexing.FactIndexer;
import Exception.experiment.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 
 * This class is to mine patterns of the form: h(X, Z) <- p(X, Y) ^ q(Y, Z)
 */
public class PatternForm1Miner {

	private static final Logger LOG = LoggerFactory.getLogger(PatternForm1Miner.class);

	/**
	 * If type = 0, we mine patterns based on #(X, Z), otherwise #(X, Y, Z).
	 */
	public static void minePatterns(String factFileName) {
		FactIndexer facts = new FactIndexer(factFileName);//data/sample.imdb.txt---trainFileName
		Map<String, Long> pattern2Long = new HashMap<>();
		Map<String, Set<String>> pattern2Pair = new HashMap<>();
		for (String fact : facts.getXpySet()) {
			String[] parts = fact.split("\t");
			String y = parts[0];
			String q = parts[1];
			String z = parts[2];
			Set<String> pxSet = facts.getPxSetFromY(y);
			if (pxSet == null) {
				continue;
			}
			for (String px : pxSet) {
				String p = px.split("\t")[0];
				String x = px.split("\t")[1];
				Set<String> hSet = facts.getPSetFromXy(x + "\t" + z);
				if (hSet == null) {
					continue;
				}
				for (String h : hSet) {
					if (h.equals(p) && p.equals(q)) {
						continue;
					}
					Utils.addKeyLong(pattern2Long, h + "\t" + p + "\t" + q, 1);
				}
			}
		}
		try {
			BufferedWriter hornRuleWriter = new BufferedWriter(new FileWriter("knowledgeCorrection-data/exception/horn-rules.txt"));
			BufferedWriter hornRuleWithStatsWriter = new BufferedWriter(new FileWriter("knowledgeCorrection-data/exception/horn-rules-stats.txt"));
			List<String> topPatterns = Utils.getTopK(pattern2Long, pattern2Long.size());
			for (String pattern : topPatterns) {
				String[] parts = pattern.split("\t");
				hornRuleWriter.write(parts[0] + "(X, Z) :- " + parts[1] + "(X, Y), " + parts[2] + "(Y, Z)\n");
				hornRuleWithStatsWriter.write(parts[0] + "(X, Z) :- " + parts[1] + "(X, Y), " + parts[2] + "(Y, Z)\t" + parts[3] + "\n");
			}
			hornRuleWriter.close();
			hornRuleWithStatsWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.info("Done with Horn rule mining.");
	}

	/**
	 * mine all paths that connect the relation of type
	 * like: isCitizenOf(Person, Country)<- wasBornIn(Person, City), locatedIn(City, Country)
	 * source files <Person, isCitizenOf,Country> ...
	 */
	public static void mineSchemaPatterns(String schemaFileName) {
		FactIndexer facts = new FactIndexer(schemaFileName);//data/schema.txt-- new create all schema information
		Map<String, Long> pattern2Long = new HashMap<>();
		Map<String, Set<String>> pattern2Pair = new HashMap<>();
		for (String fact : facts.getXpySet()) {

			String[] parts = fact.split("\t");
			String y = parts[0];
			String q = parts[1];
			String z = parts[2];
			Set<String> pxSet = facts.getPxSetFromY(y);
			if (pxSet == null) {
				continue;
			}
			for (String px : pxSet) {
				String p = px.split("\t")[0];
				String x = px.split("\t")[1];
				Set<String> hSet = facts.getPSetFromXy(x + "\t" + z);
				if (hSet == null) {
					continue;
				}
				for (String h : hSet) {
					if (h.equals(p) && p.equals(q)) {
						continue;
					}
					Utils.addKeyLong(pattern2Long, h + "\t" + p + "\t" + q, 1);

					Utils.updateKeyString(pattern2Pair,h + "\t" + p + "\t" + q, x + "\t" + y + "\t" + z, true);

				}

			}

		}
		try {
			BufferedWriter schemaPathWriter = new BufferedWriter(new FileWriter("knowledgeCorrection-data/exception/schema-path.txt"));
			BufferedWriter schemaPathWithStatsWriter = new BufferedWriter(new FileWriter("knowledgeCorrection-data/exception/schema-path-stats.txt"));
			List<String> topPatterns = Utils.getTopK(pattern2Long, pattern2Long.size());
			for (String pattern : topPatterns) {
				String[] parts = pattern.split("\t");
				schemaPathWriter.write(parts[0] + "(X, Z) :- " + parts[1] + "(X, Y), " + parts[2] + "(Y, Z)\n");
				schemaPathWithStatsWriter.write(parts[0] + "(X, Z) :- " + parts[1] + "(X, Y), " + parts[2] + "(Y, Z)\t" + parts[3] + "\n");
			}
			schemaPathWriter.close();
			schemaPathWithStatsWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		HashSet<String> allSchemaPaths= new HashSet<>();
		for(String pattern : pattern2Pair.keySet()){

			String[] parts = pattern.split("\t");
			String h = parts[0];
			String p = parts[1];
			String q = parts[2];
			for (String pairs : pattern2Pair.get(pattern)) {
				String[] xyz = pairs.split("\t");
				String x = xyz[0];
				String y = xyz[1];
				String z = xyz[2];
				allSchemaPaths.add(h + "(" + x + "," + z+") :- "+p + "(" + x + "," + y+"), " +q + "(" + y + "," + z+")");
			}
		}

		try {
			BufferedWriter schemaPathWriter = new BufferedWriter(new FileWriter("knowledgeCorrection-data/exception/schema-path-instance.txt"));
			for (String pattern : allSchemaPaths) {
				schemaPathWriter.write(pattern + "\n");
			}
			schemaPathWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOG.info("Done with schema path mining.");
	}


}
