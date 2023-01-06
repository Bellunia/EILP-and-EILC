package gilp.knowledgeClean;

import java.io.IOException;

/**
 * use the run function to get the database, so we have the closed world assumption
 * 
 * @author wy
 *
 */

public class DatabaseGenerator {

	public static void createDatabase() throws IOException, InterruptedException {

		String rdf3xLoadPath = GILPSettings.RDF3X_PATH + "rdf3xload";
		String databaseName = GILPSettings.RDF3X_PATH + GILPSettings.RDF3X_DBFILE;
		String originalData = GILPSettings.getRootPath() + "/data/yago/yago_cleaned.tsv";

		String[] command = { "/bin/sh", "-c", rdf3xLoadPath + " " + databaseName + " " + originalData };

		Process process = Runtime.getRuntime().exec(command);

		System.out.println("Creating the database... It may take 3-4 minutes depending on your machine");

		process.waitFor();

		System.out.println("The database file has been generated.");
	}

}
