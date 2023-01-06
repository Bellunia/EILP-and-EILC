package gilp.main;

import gilp.knowledgeClean.DatabaseGenerator;
import gilp.knowledgeClean.FilterYagoTriples;
import gilp.knowledgeClean.GILPSettings;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.comments.Comment;
import gilp.rules.Rule;
import javatools.parsers.NumberFormatter;
import org.apache.commons.cli.*;

import java.util.HashSet;

/**
 *
 * @author martin.theobald
 *
 *         Main GILP entry point.
 *         ----Guided Inductive Logic Programming: Cleaning Knowledge Bases with Iterative User Feedback-2020
 */

public class GILP_CWA_Yago {

	private static HashSet<Comment> comments;

	@SuppressWarnings("unused")
	private static void simulate(String[] args) throws Exception {
		Options options = new Options();

		Option gdOption = new Option(
				"gd", "generateDatabase", false, "Generate new database file from TSV file");
		gdOption.setRequired(false);
		options.addOption(gdOption);

		Option grOption = new Option(
				"gnf", "generateRules", false, "Run GILP with newly generated feedback.");
		grOption.setRequired(false);
		options.addOption(grOption);

		Option grefOption = new Option(
				"gef", "generateRulesEF", false, "Run GILP with existing feedback.");
		grefOption.setRequired(false);
		options.addOption(grefOption);

		HashSet<Rule> posQualifiedRules = new HashSet<Rule>();
		HashSet<Rule> negQualifiedRules = new HashSet<Rule>();
		HashSet<String> allPrediction = new HashSet<String>();

		try {
			CommandLine commandLine = new DefaultParser().parse(options, args);
			if (commandLine.hasOption("generateDatabase"))
				DatabaseGenerator.createDatabase();

			if (commandLine.hasOption("generateRules"))
				comments = new FilterYagoTriples().generateComments(GILPSettings.DEFAULT_PREDICATE_NAME);

			if (commandLine.hasOption("generateRulesEF"))
				comments = RuleLearnerHelper.fileToHashSet(
						GILPSettings.getRootPath()
								+ "/data/feedback/existing/GILP_feedback_subjects-noBracket.txt");

			do {

				int selectNumber = 100;// the test for the interval

				int numberOfComments = 50;
				HashSet<Comment> randomComments = new FilterYagoTriples().getRandomComments(numberOfComments, comments);

//				HashSet<String> finalAllPredictions = new RuleInYago().getAllRules(randomComments, posQualifiedRules,
//						negQualifiedRules, selectNumber, allPrediction);

			} while (posQualifiedRules.isEmpty() || negQualifiedRules.isEmpty());

		} catch (ParseException pex) {
			pex.printStackTrace();
			
		}
	}

	public static void main(String[] args) throws Exception {

		long time = System.currentTimeMillis();

		simulate(args);

		long miningTime = System.currentTimeMillis() - time;
		System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

	}
}