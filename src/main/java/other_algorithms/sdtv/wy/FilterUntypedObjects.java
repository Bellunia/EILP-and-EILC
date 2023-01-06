package other_algorithms.sdtv.wy;

import gilp.sparql.Sparql;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class FilterUntypedObjects {
	String predicateName = "hasGivenName";

	public static String removePointBrackets(String str) {
		str = str.replace("<", "");
		str = str.replace(">", "");
		return str;
	}

	static String dbpediaWeb = "http://dbpedia.org/sparql";

	public static HashMap<String, Double> readUntypedObjects(String pathToFBfile) throws Exception {

		BufferedWriter writer = null;

		writer = new BufferedWriter(new FileWriter("/home/wy/Downloads/readUntypedObjects.tsv"));

		HashMap<String, Double> untypedObjects = new HashMap<String, Double>();

		HashSet<String> otherObjects = new HashSet<String>();

		HashSet<String> negativeObjects = new HashSet<String>();
		HashSet<String> positiveObjects = new HashSet<String>();
		HashSet<String> roughObjects = new HashSet<String>();

		Scanner scanner = new Scanner(pathToFBfile);
		File file = new File(scanner.nextLine());
		Scanner input = new Scanner(file);

		input.nextLine();// skip the first line

		while (input.hasNextLine()) {
			String line = removePointBrackets(input.nextLine());
			String[] pred_array = line.split(",");
			String object = pred_array[0];
			String value = pred_array[1];

			double counts = Double.parseDouble(value);

			untypedObjects.put(object, counts);
			//if (!object.contains("(")) {

				String sparqlDbpedia = "SELECT distinct ?b WHERE { ?a  ?b \"" + object + "\"@en. "
						+ "filter(regex(str(?b),\"name\") || regex(str(?b),\"surname\") || regex(str(?b),\"givenname\")  )}";

				HashSet<String> typesInDbpedia = new Sparql().getSingleResultsFromQuery(sparqlDbpedia, dbpediaWeb);
				//
				if (typesInDbpedia.isEmpty()) {

					otherObjects.add(object);
				} else {
					System.out.println("\n typesInDbpedia:" + typesInDbpedia);
					 System.out.println("\n typesInDbpedia--object:" + object);

					if (typesInDbpedia.contains("http://xmlns.com/foaf/0.1/surname")) {
						negativeObjects.add(object);
					} else if (typesInDbpedia.contains("http://xmlns.com/foaf/0.1/givenname")) {
						positiveObjects.add(object);
					} else {
						roughObjects.add(object);
					}

				}
		//	}

		}
		input.close();
		scanner.close();

		writer.write("all positiveObjects\n");
		for (String key : positiveObjects)
			writer.write(key + "\n");
		writer.write("all negativeObjects\n");
		for (String key : negativeObjects)
			writer.write(key + "\n");
		writer.write("all roughObjects\n");
		for (String key : roughObjects)
			writer.write(key + "\n");
		writer.write("all otherObjects\n");
		for (String key : otherObjects)
			writer.write(key + "\n");

		writer.close();

		System.out.println("READING all positiveObjects: " + positiveObjects.size());
		System.out.println("READING all negativeObjects: " + negativeObjects.size());
		System.out.println("READING all roughObjects: " + roughObjects.size());

		// System.out.println("\n READING all positiveObjects: \n" + positiveObjects);
		// System.out.println("\nREADING all negativeObjects:\n " + negativeObjects);
		// System.out.println("\n READING all roughObjects:\n " + roughObjects);

		return untypedObjects;
	}

	public static void main(String[] args) throws Exception {
		// addFeedbacks();

		// preprocessSingleData1();
		// filterPrediactes();
		String pathToFBfile = "/home/wy/Desktop/untyped_hasGivenName_objects.csv";
		readUntypedObjects(pathToFBfile);
	}

}
