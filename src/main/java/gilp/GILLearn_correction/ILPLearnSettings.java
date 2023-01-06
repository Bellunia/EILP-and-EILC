package gilp.GILLearn_correction;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ILPLearnSettings {

    public static String RDF3X_PATH = "/home/wy/Documents/gh-rdf3x/bin/";

    public static final int DEFAULT_LEVEL = 2;
    public static final int DEFAULT_METHODS = 1;
    public static final int intervalNumber = 100;
    public static final int initialNumber = 100;
    public static final int initialNumber2 = 100;
    public static final int initialNumberNeg = 50;
    public static final int inteval = 100;
    public static final int Database = 1;
    //1:dbpedia  2:yago_type

    public static final int condition = 1;
    //  1: rdf database local 2: dbpedia on line

    public static final int conditionInSearchSpace = 1;
    //1: level 2: neighbor

    //public static String _positive_data_file = "./data/searchSpace/positiveSearchSpace.tsv";
   // public static String _negative_data_file = "./data/searchSpace/negaitveSearchSpace.tsv";

    public String[] parameters(Boolean decision, int iterationTimes) {//amie+ --parameters
        if (decision)

            return new String[]{"./data/searchSpace/positiveSearchSpace" + iterationTimes + ".tsv","-maxad", "3",
                      "-mins", String.valueOf(ILPLearnSettings.initialNumber2), "-minis", String.valueOf(ILPLearnSettings.initialNumber2),
                    "-bexr", "<" + Property.PREDICATE_NAME + ">", "-htr", " <" + Property.PREDICATE_NAME + ">",
                      "-minhc", "0.01", "-minpca", "0.01",
                    "-dpr", "-optimfh"
                   // "-const",
                    };



        else
            return new String[]{"-maxad", "3",
                   "-mins", String.valueOf(ILPLearnSettings.initialNumberNeg), "-minis", String.valueOf(ILPLearnSettings.initialNumberNeg),
                    "-bexr", "<" + Property.PREDICATE_NAME + ">", "-htr", " <" + Property.PREDICATE_NAME + ">",
                    "-minhc", "0.01", "-minpca", "0.01",
                    "-dpr", "-optimfh",
                    "-const",
                    "./data/searchSpace/negativeSearchSpace" + iterationTimes + ".tsv"};

    }

    public String[] parameters_amie2(Boolean decision, int iterationTimes) {//amie+ --parameters
        if (decision)

            return new String[]{"-maxad", "3",
                    "-mins", String.valueOf(ILPLearnSettings.initialNumber2), "-minis", String.valueOf(ILPLearnSettings.initialNumber2),
                    "-bexr", "<" + Property.PREDICATE_NAME + ">", "-htr", " <" + Property.PREDICATE_NAME + ">",
                    "-minhc", "0.01", "-minpca", "0.01",
                    "-dpr", "-optimfh",
                    // "-const",
                    "./data/searchSpace/positiveSearchSpace" + iterationTimes + ".tsv"};



        else
            return new String[]{"-maxad", "3",
                    "-mins", String.valueOf(ILPLearnSettings.initialNumberNeg), "-minis", String.valueOf(ILPLearnSettings.initialNumberNeg),
                    "-bexr", "<" + Property.PREDICATE_NAME + ">", "-htr", " <" + Property.PREDICATE_NAME + ">",
                    "-minhc", "0.01", "-minpca", "0.01",
                    "-dpr", "-optimfh",
                    "-const",
                    "./data/searchSpace/negativeSearchSpace" + iterationTimes + ".tsv"};

    }

    public static final List<String> headers = Arrays.asList("Rule", "Head Coverage", "Std Confidence",
            "PCA Confidence", "Positive Examples", "Body size", "PCA Body size",
            "Functional variable", "Std. Lower Bound", "PCA Lower Bound", "PCA Conf estimation");

    private static void filterAllPredicates() {
        // String predicate = "select ?predicate (count (?predicate) as ?number) where{
        // ?subject ?predicate ?object. } group by ?predicate";
        // Because of number of dbpedia's predicate is more than 1300, we only choose
        // some predicate for the test.

        String predicate = "select distinct ?predicate  where{ ?subject ?predicate ?object. } group by ?predicate";
        // HashMap<String, String> subjectTypeResults = new
        // Sparql().countResultsFromQuery( predicate) ;
        ArrayList<String> subjectTypeResults = new Sparql().getSingleResultsFromQuery(predicate);
        System.out.println(subjectTypeResults.size());
        for (String key : subjectTypeResults) {
            System.out.println(key);
            if (key.contains("http://dbpedia.org/"))
                System.out.println(key);
        }
    }

    public void combineTwofiles(String path1,String path2,String path3) throws IOException {

        FileReader f=new FileReader(path1);
        BufferedReader br = new BufferedReader(f);

        FileReader f1=new FileReader(path2);
        BufferedReader br1 = new BufferedReader(f1);


        Writer writer = new OutputStreamWriter(new FileOutputStream(path3), Charset.forName("UTF-8"));

        try {
            String st;
            int i=0;
            while ((st = br.readLine()) != null) {
                writer.write(st + "\n");
                i++;
            }
            System.out.println("step1--numbers:"+i);
            String st1;
            while ((st1 = br1.readLine()) != null) {
                writer.write(st1 + "\n");
                i++;
            }
            System.out.println("step2--numbers:"+i);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        br.close();

        f.close();
        br1.close();

        f1.close();

    }

    public static HashSet<Triple> readTriples( String path) throws Exception {

        HashSet<Triple> triples = new HashSet<>();

        Scanner scanner = new Scanner(path);
        File file = new File(scanner.nextLine());
        Scanner input = new Scanner(file);

        while (input.hasNextLine()) {
            String line1 = input.nextLine();

            String[] line = line1.split("\t");
            if(line.length==3) {

                String subject, predicate, object;
                subject = line[0];
                predicate = line[1];
                object = line[2];

                Triple triple = new Triple(subject, predicate, object);

                triples.add(triple);
            }else
                System.out.println(line1);

        }
        input.close();
        scanner.close();

        return triples;
    }

    public void writeSearchSpace(int iterationTimes) throws Exception {
        String pathPos ="./data/searchSpace/positiveSearchSpace" + iterationTimes + "-old.tsv";
        String pathNeg = "./data/searchSpace/negativeSearchSpace" + iterationTimes + "-old.tsv";

        HashSet<Triple> positiveTriples = readTriples(pathPos);
        HashSet<Triple> negativeTriples = readTriples(pathNeg);

        negativeTriples.removeAll(positiveTriples);
        new ILPLearnSettings().writeOutSearchSpace(positiveTriples, true, iterationTimes);
        new ILPLearnSettings().writeOutSearchSpace(negativeTriples, false, iterationTimes);

    }

    public void writeOutSearchSpace(HashSet<Triple> expandedTriples, Boolean decision, int iterationTimes) throws IOException {
        String path = null;
        if (decision)
            path = "./data/searchSpace/positiveSearchSpace" + iterationTimes + ".tsv";
        else path = "./data/searchSpace/negativeSearchSpace" + iterationTimes + ".tsv";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));

        try {
            for (Triple t : expandedTriples) {
                writer.write(("<" + substitute(t.get_subject()) + ">\t"));
                writer.write(("<" + substitute(t.get_predicate()) + ">\t"));
                writer.write(("<" + substitute(t.get_obj()) + ">\n"));

            }
            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeOutFeedbacks(HashSet<Triple> positiveTriples,HashSet<Triple> negativeTriples,  int numbers) throws IOException {
        String path = "./data/searchSpace/feedback-" + numbers + ".txt";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));

        try {
            for (Triple t : positiveTriples)
                writer.write(t + "\t"+"1"+"\n");
            for (Triple t : negativeTriples)
                writer.write(t + "\t"+"0"+"\n");

            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeOutRandomObjects(HashSet<String> expandedTriples, HashSet<String> negativeThing, int numbers) throws IOException {
        String path = "./data/searchSpace/feedback-" + numbers + ".tsv";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));

        try {
            for (String t : expandedTriples)
                writer.write(t + "\t" + "1\n");

            for (String t : negativeThing)
                writer.write(t + "\t" + "0\n");

            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }

    private static String substitute(String str) {
        str = str.replace("-", "@@").replace("–", "##");
        return str;
    }

    public void writeOutRule(HashSet<Triple> list, Boolean decision) {

        HashSet<String> types = new HashSet<String>();
        BufferedReader read = null;

        try {
            File file = new File("/home/wy/Desktop/test-correction/extendTriples-1-3--" + decision + ".txt");
            FileWriter fileReader = new FileWriter(file); // A stream that connects to the text file
            BufferedWriter bufferedWriter = new BufferedWriter(fileReader); // Connect the FileWriter to the BufferedWriter

            for (Triple s : list) {
                bufferedWriter.write(s + "\n");
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeOutCountry() {
        HashSet<String> types = new HashSet<String>();
        HashSet<String> countries =  RuleLearnerHelper.readTypes("/home/wy/Desktop/experiment-files/country-id.txt");
        for (String key : countries) {
            String query = "select ?a where{ ?a owl:sameAs <" + key + ">.}";
            System.out.println(query);
            ArrayList<String> countryInDBpedia = new Sparql().getSingleResultsFromQuery(query);
            if (!countryInDBpedia.isEmpty())
                types.add(countryInDBpedia.get(0));
        }



        try {
            File file = new File("/home/wy/Desktop/experiment-files/country-dbpedia.txt");
            FileWriter fileReader = new FileWriter(file); // A stream that connects to the text file
            BufferedWriter bufferedWriter = new BufferedWriter(fileReader); // Connect the FileWriter to the BufferedWriter

            for (String s : types) {
                bufferedWriter.write(s + "\n");
            }

            bufferedWriter.close(); // Close the stream
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            //  System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
