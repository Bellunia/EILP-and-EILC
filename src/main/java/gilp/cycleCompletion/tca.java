package gilp.cycleCompletion;

import gilp.GILLearn_correction.Property;
import gilp.rdf3x.Triple;
import gilp.similarityMeasure.SimilarityAlgorithms;
import gilp.sparql.Sparql;
import gilp.sparql.wikidataSparql;
import gilp.utils.AuxiliaryParameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class tca {
    /*
    validation exception-correction
     */
    /*search space-final 20 loops
    positive /negative
     */
    static String test_location = "./TCA/test-TCA/TCA-feedabck/4-20/";

    public static void main(String[] args) throws Exception {
        new tca().TCA_test();
        new tca().countQuantity();
        String expertAnalysisPath = test_location + "expertAnalysis.tsv";
        String expertAnalysisProbability = test_location + "correctionProbability.tsv";
        new tca().correctionCompareProbability(expertAnalysisPath, expertAnalysisProbability);
    }
    public void TCA_test() throws IOException {
        /*TCA do the correction test */
        // read negative feedback--get different negative objects
        String test_location = "/home/wy/Desktop/test-4-12-searchSpace/4-20/";
        String negativeFeedbackPath = test_location + "negative-20.tsv";
        String negObjPath = test_location + "negative-obj.tsv";
        String negObjNumPath = test_location + "negative-obj-num.tsv";

        countNegative(negativeFeedbackPath, negObjPath, negObjNumPath);

        HashSet<String> errorEntities = AuxiliaryParameter.singleLineTsv(negObjPath);
        System.out.println(errorEntities.size());

        //---------------- negative objects into wikidata

        String testErrorsTCA = test_location + "negative-obj-tca-repairs.tsv";
        String emptyPath = test_location + "empty-repairs.tsv";
        String multipleValuePath = test_location + "multipleValue-repairs.tsv";
        String singleValuePath = test_location + "singleValue-repairs.tsv";
        String multipleValueSimplePath = test_location + "multipleValueSimple.tsv";
        separateRepairs(errorEntities, testErrorsTCA, emptyPath, multipleValuePath, singleValuePath, multipleValueSimplePath);

        // multipleValueAnalysis-----
        ArrayList<String[]> manyRepairs = AuxiliaryParameter.tsvr(multipleValueSimplePath);

        String multipleValueAllSimilarityPath = test_location + "multipleValueSimple-AllSimilarity.tsv";
        String multipleValueSimple_filterCorrection = test_location + "multipleValueSimple-filterCorrection.tsv";

        filterMultipleValueSimilarity(manyRepairs, multipleValueAllSimilarityPath, multipleValueSimple_filterCorrection);
    }
    private void countNegative(String negPath, String negObjPath, String negObjNumPath) {
        HashSet<Triple> negativeTriple = new AuxiliaryParameter().readTriples(negPath);
        HashMap<String, Integer> negNumber = new HashMap<>();
        HashSet<String> negObj = new HashSet<>();
        for (Triple t : negativeTriple) {
            String obj = t.get_obj();
            //write out different objects
            negObj.add(obj);
            if (!negNumber.containsKey(obj))
                negNumber.put(obj, 1);
            else {
                int num = negNumber.get(obj);
                negNumber.put(obj, num + 1);
            }
        }

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(negObjPath), StandardCharsets.UTF_8);
            for (String t : negObj)
                writer.write(t + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(negObjNumPath), StandardCharsets.UTF_8);
            for (String t : negNumber.keySet())
                writer.write(t + "\t" + negNumber.get(t) + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    public ArrayList<String> sameAsProperty(String errorEntity) {
        String correction = null;
        String errorQuery = " select ?a where {{<" + errorEntity + "> dbo:wikiPageRedirects ?b. ?b owl:sameAs ?a.}" +
                " union{<" + errorEntity + ">  owl:sameAs ?a. } " + "filter(regex(?a,\"wikidata.org\"))}";

        HashSet<String> matchError = new Sparql().getSingleVariable(errorQuery);
        System.out.println(errorQuery + "\n");
        String error = null;//errorQuery:http://www.wikidata.org/entity/Q3476361
        for (String key : matchError) {
            error = key;//error=http://www.wikidata.org/entity/Q3476361
            break;
        }
        ArrayList<String> finalResults = new ArrayList<>();
        if (error != null) {
            String correctProperty = "select distinct ?itemLabel where{" +
                    "{<" + error + "> " + Property.relationPropertyInWikidata + " ?item.}" +
                    "UNION {<" + error + "> p:P31 ?A. ?A pq:P642 ?item. }" +
                    "SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }}";
            //    "  ?A ps:P31 wd:Q231002. " +// property:nationality of
            System.out.println("wikidata--correctProperty:" + correctProperty);
            ArrayList<String[]> propertyResults = new wikidataSparql().getCommands(correctProperty);

            for (int i = 1; i < propertyResults.size(); i++) {
                String[] ke = propertyResults.get(i);
                for (String key : ke) {
                    if (key.contains(" ")) {
                        String element = "http://dbpedia.org/resource/" + key.replace(" ", "_");
                        finalResults.add(element);
                    } else
                        finalResults.add("http://dbpedia.org/resource/" + key);
                }
            }
            System.out.println("finalResults: " + finalResults + "\n");
        }
        return finalResults;
    }
    private void countQuantity() {
        HashMap<String, Integer> errors = AuxiliaryParameter.readCount(test_location + "negative-obj-num.tsv");

        String allPairsPath = test_location + "negative-obj-tca-repairs.tsv";

        String emptyPath = test_location + "empty-repairs-num.tsv";
        String singleValuePath = test_location + "singleValue-repairs-num.tsv";
        String multipleValuePath = test_location + "multipleValue-repairs-num.tsv";
        String multipleValueSimplePath = test_location + "multipleValueSimple-num.tsv";

        ArrayList<String[]> manyRepairs = AuxiliaryParameter.tsvr(allPairsPath);

        try {
            Writer writer1 = new OutputStreamWriter(new FileOutputStream(emptyPath), StandardCharsets.UTF_8);
            Writer writer2 = new OutputStreamWriter(new FileOutputStream(singleValuePath), StandardCharsets.UTF_8);
            Writer writer3 = new OutputStreamWriter(new FileOutputStream(multipleValuePath), StandardCharsets.UTF_8);
            Writer writer4 = new OutputStreamWriter(new FileOutputStream(multipleValueSimplePath), StandardCharsets.UTF_8);

            for (String[] key : manyRepairs) {
                if (key.length == 1) {//empty
                    writer1.write(key[0] + "\t" + errors.get(key[0]) + "\n");
                } else if (key.length == 2) {//single value
                    for (String ke : key) {
                        writer2.write(ke + "\t");
                    }
                    writer2.write(errors.get(key[0]) + "\n");
                } else {//multiple value
                    writer3.write(key[0] + "\t" + errors.get(key[0]) + "\t");
                    writer4.write(key[0].replace("http://dbpedia.org/resource/", "") + "\t" + errors.get(key[0]) + "\t");
                    for (int i = 1; i < key.length; i++) {
                        writer3.write(key[i] + "\t");
                        writer4.write(key[i].replace("http://dbpedia.org/resource/", "") + "\t");
                    }

                    writer3.write("\n");
                    writer4.write("\n");
                }
            }
            writer1.close();
            writer2.close();
            writer3.close();
            writer4.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    private void separateRepairs(HashSet<String> errorEntities, String testErrorsTCA, String emptyPath,
                                 String multipleValuePath, String singleValuePath, String multipleValueSimplePath) {
        //---------------- negative objects into wikidata
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(testErrorsTCA), StandardCharsets.UTF_8);
            Writer writer1 = new OutputStreamWriter(new FileOutputStream(emptyPath), StandardCharsets.UTF_8);
            Writer writer2 = new OutputStreamWriter(new FileOutputStream(multipleValuePath), StandardCharsets.UTF_8);
            Writer writer3 = new OutputStreamWriter(new FileOutputStream(singleValuePath), StandardCharsets.UTF_8);
            Writer writer4 = new OutputStreamWriter(new FileOutputStream(multipleValueSimplePath), StandardCharsets.UTF_8);
            for (String key : errorEntities) {
                ArrayList<String> correction = new tca().sameAsProperty(key);

                writer.write(key + "\t");
                for (String t : correction) {
                    writer.write(t + "\t");
                }
                writer.write("\n");

                if (correction.isEmpty())
                    writer1.write(key + "\n");
                else if (correction.size() == 1) {
                    writer3.write(key + "\t");
                    writer3.write(correction.get(0) + "\n");
                } else {//multiple value
                    writer2.write(key + "\t");
                    writer4.write(key.replace("http://dbpedia.org/resource/", "") + "\t");
                    for (String t : correction) {
                        writer2.write(t + "\t");
                        writer4.write(t.replace("http://dbpedia.org/resource/", "") + "\t");
                    }
                    writer2.write("\n");
                    writer4.write("\n");
                }

            }
            writer.close();
            writer1.close();
            writer2.close();
            writer3.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    private void filterMultipleValueSimilarity(ArrayList<String[]> manyRepairs,
                                               String multipleValueAllSimilarityPath, String multipleValueSimple_filterCorrection) throws IOException {
        // multipleValueAnalysis-----
        BufferedWriter out = new BufferedWriter(new FileWriter(
                multipleValueAllSimilarityPath));

        BufferedWriter writer = new BufferedWriter(new FileWriter(multipleValueSimple_filterCorrection));

        for (String[] ke : manyRepairs) {
            String error = ke[0];
            List<String> list = Arrays.asList(ke);
            System.out.println(list);
            ArrayList<String> listCopy = new ArrayList<>(list);
            listCopy.remove(error);
            HashMap<String, double[]> correctionAllWeight = new HashMap<>();
            for (String item : listCopy) {
                HashMap<String, double[]> correctionWeight =
                        new SimilarityAlgorithms().externalSimilarity(error, item);
                correctionAllWeight.putAll(correctionWeight);
            }
            out.write(ke[0] + "\t");
            for (String key : correctionAllWeight.keySet()) {
                out.write(key + "\t");
                double[] rates = correctionAllWeight.get(key);
                for (double ratio : rates)
                    out.write(ratio + "\t");
            }
            out.write("\n");

            String[] fiterAllCorrection = new tca().compareSimilarity(correctionAllWeight);
            writer.write(ke[0] + "\t");
            for (String corre : fiterAllCorrection)
                writer.write(corre + "\t");

            writer.write("\n");
        }
        writer.close();
        out.close();
    }
    private void correctionCompareProbability(String expertAnalysisPath, String expertAnalysisProbability) {
        //--in the multipleValueSimple-filterCorrection.tsv, add expert analysis, get expertAnalysis.tsv,
        File tempFile = new File(expertAnalysisPath);

        boolean exists = tempFile.exists();

        if (exists) {
            double[] values = readExpertAnalysis(expertAnalysisPath);
            String title = "external_sim\tJaro\tharmony\taverage\tcosine\tCosine(2)\tJaccard(2)\tJaroWinkler\tSorensenDice(2)\tLevenshtein\t" +
                    "Normalized Levenshtein\tDamerau\tOptimalStringAlignment\tLongestCommonSubsequence\tNGram(2)\tQGram(2)\texpert";
            String[] args = title.split("\t");
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(expertAnalysisProbability), StandardCharsets.UTF_8);
                int i = 0;
                for (double ke : values) {
                    writer.write(args[i] + "\t" + ke + "\n");
                    i++;
                    System.out.println(args[i] + "\t" + ke + "\n");
                }
                writer.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
    }
    public String[] compareSimilarity(HashMap<String, double[]> correctionAllWeight) {
        HashMap<String, double[]> copy = new HashMap<>(correctionAllWeight);
        Map.Entry<String, double[]> entry = correctionAllWeight.entrySet().iterator().next();
        String keyFirst = entry.getKey();
        double[] valueFirst = entry.getValue();
        int num = valueFirst.length;

        String[] isChosen = new String[num];
        for (int i = 0; i < num; i++)
            isChosen[i] = keyFirst;

        for (String key : copy.keySet()) {
            double[] rates = copy.get(key);
            for (int i = 0; i < num; i++) {
                if (rates[i] > valueFirst[i] && rates[i] < 1.0) {
                    isChosen[i] = key;
                    valueFirst[i] = rates[i];
                } else if (rates[i] < valueFirst[i] && rates[i] > 1.0) {
                    isChosen[i] = key;
                    valueFirst[i] = rates[i];
                }
            }
        }
        return isChosen;
    }
    public static double[] readExpertAnalysis(String test2) {
        //--------------------------ANALYSIS correction rates
        // add experts compare analysis
        ArrayList<String[]> Data = new ArrayList<>(); //initializing a new ArrayList out of String[]'s

        try (BufferedReader TSVReader = new BufferedReader(new FileReader(new File(test2)))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
                Data.add(lineItems); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        int num = Data.get(0).length - 2;//remove error and expertCorrection
        int size = Data.size();
        int[] isChosen = new int[num];
        for (int i = 0; i < num; i++)
            isChosen[i] = 0;

        for (int i = 1; i < Data.size(); i++) {//remove first line
            String[] lineItems = Data.get(i);
            //  String error = lineItems[0];
            String expertCorrection = lineItems[lineItems.length - 1];
            for (int j = 1; j < lineItems.length - 1; j++) {
                if (lineItems[j].equals(expertCorrection))
                    isChosen[j - 1] = isChosen[j - 1] + 1;
            }
        }
        double[] isValue = new double[num];
        for (int i = 0; i < num; i++)
            isValue[i] = (double) isChosen[i] / size;

        return isValue;
    }


}
