package gilp.utils;
import javatools.parsers.NumberFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
public class terminalCommand {

    public static void createDatabase() throws IOException, InterruptedException {


        //String[] command = { "/bin/sh", "-c", rdf3xLoadPath + " " + databaseName + " " + originalData };
        String[] command = { "/bin/sh", "-c","/home/wy/.local/bin/wikidata-dl", " ",
                "/home/wy/PycharmProjects/wikidata-dl/tests/queries/instance-of-inner-planet.sparql"};
// + " " + databaseName + " " + originalData };

        Process process = Runtime.getRuntime().exec(command);

//		String[] args = new String[] {"/bin/bash", "-c", "wikidata-dl"
//				,"/home/wy/PycharmProjects/wikidata-dl/tests/queries/instance-of-inner-planet.sparql"};

        String[] args = new String[] {"/bin/bash", "-c", "curl"
                ,"'https://query.wikidata.org/sparql?query=SELECT+DISTINCT+%3Fitem+" +
                "WHERE+%7B+%3Chttp%3A%2F%2Fwww.wikidata.org%2Fentity%2FQ1196645%3E+%3Chttp%3A%2F%2F" +
                "www.wikidata.org%2Fprop%2Fdirect%2FP17%3E+%3Fitem++%7D'"};
        Process proc = new ProcessBuilder(args).start();

        //	String command1 = "echo $(/home/wy/.local/bin/wikidata-dl /home/wy/Desktop/test.sparql)";
        //	Process po = new ProcessBuilder("/bin/sh", "-c", command1).start();
        String command2="curl -X GET " +
                "'https://query.wikidata.org/sparql?query=SELECT+DISTINCT+%3Fitem+" +
                "WHERE+%7B+%3Chttp%3A%2F%2Fwww.wikidata.org%2Fentity%2FQ1196645%3E+%3Chttp%3A%2F%2F" +
                "www.wikidata.org%2Fprop%2Fdirect%2FP17%3E+%3Fitem++%7D'";
        //Process p = Runtime.getRuntime().exec(command);

        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println("***************************");
            System.out.println(line + "\n");
        }

        System.out.println("Creating the database... It may take 3-4 minutes depending on your machine");

        //process.waitFor();

        System.out.println("The database file has been generated.");
    }

    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();

        //	createDatabase();



        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

    }

}