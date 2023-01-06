package gilp.cycleCompletion;

import gilp.GILLearn_correction.Property;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.Triple;
import gilp.similarityMeasure.SimilarityAlgorithms;
import gilp.sparql.Sparql;
import gilp.sparql.wikidataSparql;
import gilp.utils.KVPair;
import javatools.parsers.NumberFormatter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * in the closed world, using the type range to find all errors in the KB,
 * then using the owl:sameAs to find related entity as correction in the wikidata.
 * the main function is CorrectionSingleErrors().
 * for single errors to correction.
 * for example: Germans linked to Germany
 *
 */

public class SmallSampleErrorCorrection {

    public KVPair<String, Double> sameAsProperty2(String errorEntity) {
        String correction = null;
        String errorQuery = " select ?a where {{<" + errorEntity + "> dbo:wikiPageRedirects ?b. ?b owl:sameAs ?a.}" +
                " union{<" + errorEntity + ">  owl:sameAs ?a. } " +
                "filter(regex(?a,\"wikidata.org\"))}";


        System.out.println(errorQuery);
//        String errorQuery = " select ?a where {<" + new CorrectionInDBPedia().redictEntity(errorEntity) + "> owl:sameAs ?a." +
//                "filter(regex(?a,\"wikidata.org\"))}";
        HashSet<String> matchError = new Sparql().getSingleVariable(errorQuery);
        System.out.println(errorQuery + "\n");
        String error = null;
        ArrayList<String> finalResults = new ArrayList<>();
        for (String key1 : matchError) {
            error = key1;
            //    break;
            // }

            if (error != null) {
                String correctProperty = "select distinct ?itemLabel where{" +
                        "{<" + error + "> " + Property.relationPropertyInWikidata + " ?item.}" +
                        "UNION {<" + error + "> p:P31 ?A. ?A pq:P642 ?item. }" +
                        "UNION {<" + error + "> wdt:P495 ?item.}" +
                        "SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }" +
                        "" +
                        "}";
                //    "  ?A ps:P31 wd:Q231002. " +// property:nationality of
                System.out.println("wikidata--correctProperty:" + correctProperty);
                ArrayList<String[]> propertyResults = new wikidataSparql().getCommands(correctProperty);

                for (int i = 1; i < propertyResults.size(); i++) {
                    String[] ke = propertyResults.get(i);
                    for (String key : ke) {
                        if (key.contains(" ")) {
                            System.out.printf("space-entity", key);
                            String element = "http://dbpedia.org/resource/" + key.replace(" ", "_");
                            finalResults.add(element);
                        } else
                            finalResults.add("http://dbpedia.org/resource/" + key);
                    }
                }
                //  String correctPropertyQuery = "select distinct ?a where{<" + error + "> " + Property.relationPropertyInWikidata + " ?a.}";//wdt:P17
                //   System.out.println("wikidata--correctQuery1:" + correctPropertyQuery);
                //  ArrayList<String> finalResults = new ArrayList<>();
                //  ArrayList<String[]> propertyResults = new wikidataSparql().getCommands(correctPropertyQuery);
//            for (int i = 1; i < propertyResults.size(); i++) {
//                String[] ke = propertyResults.get(i);
//                finalResults.addAll(Arrays.asList(ke));
//            }
//            String instanceQuery = "select distinct ?a where{<" + error + "> p:P31 ?A. ?A pq:P642 ?a. }";
//            //    "  ?A ps:P31 wd:Q231002. " +// property:nationality of
//            System.out.println("wikidata--correctQuery2:" + instanceQuery);
//            ArrayList<String[]> resultsOfInstance = new wikidataSparql().getCommands(instanceQuery);
//            for (int i = 1; i < resultsOfInstance.size(); i++) {
//                String[] ke = resultsOfInstance.get(i);
//                Collections.addAll(finalResults, ke);
//            }


//                String corresondQuery = "select ?a where {?a owl:sameAs " + new wikidataSparql().replaceQueryHeader(oneResult) + " }";
//                System.out.println("reverse-query-dbpedia:" + corresondQuery + "\n");
//                HashSet<String> corresondCorrection = new Sparql().getSingleVariable(corresondQuery);
//              //  ArrayList<String> corresondCorrection = new Sparql().getSingleResultsFromQuery(corresondQuery);
//                if (corresondCorrection != null) {
//                    for(String key: corresondCorrection) {
//                        correction = key;
//                        break;
//                    }
//                }
                }
            }
        KVPair<String, Double> correctionValue= new KVPair<>();
        System.out.println("finalResults: " + finalResults + "\n");
        if (!finalResults.isEmpty()) {
            if (finalResults.size() > 1)
                correctionValue = compareSimilarity(finalResults, errorEntity);
            else
                correctionValue.put(finalResults.get(0),1.0);
        }else
            return null;

        return correctionValue;

    }

    public KVPair<String, Double> compareSimilarity(ArrayList<String> results, String errorEntity) {
        HashMap<String, Double> targets = new HashMap<>();

        String[] id2 = errorEntity.split("/");
        for (String ke : results) {
            String[] kSplits = ke.split("/");
         //   float similarityRatio =  new CompareStrSimUtil().getSimilarityRatio(id2[id2.length - 1], kSplits[kSplits.length - 1], true);

            double similarityRatio= SimilarityAlgorithms.getLevenshteinDistance(id2[id2.length - 1], kSplits[kSplits.length - 1]);
            targets.put(ke,  similarityRatio);
        }
        HashMap<String, Double>  reverseResults=  new RuleLearnerHelper().reverseOrderByValue(targets);
               String firstKey = reverseResults.keySet().iterator().next();
      double value = reverseResults.get(firstKey);
        return  new KVPair<>(firstKey,value);

    }



    public void CorrectionObjectsErrors(String errorPaths) {
        HashSet<String> errorObjectsEntities = RuleLearnerHelper.readTypes(errorPaths);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("./prediction/analysis/" + "errorExample" + ".tsv"));

        int truePositive = 0;
        int falsePositive = 0;
        int empty = 0;

        int trueNegative = 0;
        int falseNegative = 0;
        int emptyNeg = 0;

            int  allSize =errorObjectsEntities.size();
        HashMap<String, String> negativeCorrection = new HashMap<>();
        int iterationTime = 1;
        for (String key : errorObjectsEntities) {
            System.out.println("test------" + iterationTime++ + "------------\n");

            KVPair<String, Double> correction = sameAsProperty2(key);

            if (correction == null)
                emptyNeg = emptyNeg + 1;
            else if (correction.containsKey(key))
                falseNegative = falseNegative + 1;
            else
                truePositive = truePositive + 1;

            if (correction != null) {
                negativeCorrection.put(key, correction.getKey());

                out.write(key + "\t" + correction.getKey() + "\t" + correction.getValue()+ "\n");
            }
            else {
                negativeCorrection.put(key, null);
                out.write(key + "\t" + null+ "\n");
            }

        }

        double precision = (double) truePositive / (truePositive + falsePositive);
        double emptyRate = (double) (empty + emptyNeg) / allSize;
        double correctionRatePositive = (double) falsePositive / (falsePositive + truePositive);
        double correctionRateNegative = (double) trueNegative / (trueNegative + falseNegative);
        double accuracy = (double) (trueNegative + truePositive) / (allSize);
        System.out.println("allSize:" + allSize + "\n");
        System.out.println("tp:" + truePositive + "\n");
        System.out.println("fp:" + falsePositive + "\n");
        System.out.println("tn:" + trueNegative + "\n");
        System.out.println("fn:" + falseNegative + "\n");
        System.out.println("empty:" + empty + "\n");

        System.out.println("precision:" + precision + "\n");
       System.out.println("emptyRate:" + emptyRate + "\n");
        System.out.println("correctionRatePositive:" + correctionRatePositive + "\n");
        System.out.println("correctionRateNegative:" + correctionRateNegative + "\n");
        System.out.println("accuracy:" + accuracy + "\n");

            out.write("allSize:" + allSize + "\n");
            out.write("tp:" + truePositive + "\n");
            out.write("fp:" + falsePositive + "\n");
            out.write("tn:" + trueNegative + "\n");
            out.write("fn:" + falseNegative + "\n");
            out.write("empty:" + empty + "\n");

            out.write("precision:" + precision + "\n");
            out.write("emptyRate:" + emptyRate + "\n");
            out.write("correctionRatePositive:" + correctionRatePositive + "\n");
            out.write("correctionRateNegative:" + correctionRateNegative + "\n");
            out.write("accuracy:" + accuracy + "\n");

            out.close();

        } catch (IOException ignored) {
        }

    }

    public void filterSingleTypeErrors(){
        //all single type
        HashSet<Triple> all = new HashSet<>();
        int number= 100;
        int filterCondition=1;//filter different Subjects
        HashSet<String> negativeProperty = RuleLearnerHelper.readTypes("./prediction/analysis/singleError_type.txt");

        for (String property : negativeProperty) {

            String    query = "select distinct ?subject ?object where {"
                    + " ?subject <http://dbpedia.org/ontology/nationality> ?object."
                    + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                    + "?object rdf:type <" + property + ">."
                    + "} ORDER BY RAND() limit " + number;

            HashSet<Triple> triples = new Exception_GILP().filterTriples(query, "http://dbpedia.org/ontology/nationality", filterCondition);
            all.addAll(triples);
        }

        try {

            FileWriter writer = new FileWriter("./prediction/analysis/singleErrors.tsv", StandardCharsets.UTF_8);
            for (Triple tri : all) {
                writer.write(tri + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();

        String errorsObjects="./prediction/analysis/ethnicGroup/errorExamples.txt";

         new SmallSampleErrorCorrection().CorrectionObjectsErrors(errorsObjects);

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

    }

}
