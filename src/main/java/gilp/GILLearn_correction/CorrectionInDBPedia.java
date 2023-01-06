package gilp.GILLearn_correction;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.Triple;
import gilp.similarityMeasure.SimilarityAlgorithms;
import gilp.sparql.Sparql;
import gilp.sparql.wikidataSparql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class CorrectionInDBPedia {
    public HashSet<HashMap<Triple, Triple>> correctTriple(HashSet<Triple> errorTriples, String range) {
        HashMap<Triple, Triple> correctionPairs = new HashMap<>();
        HashSet<HashMap<Triple, Triple>> pairs = new HashSet<>();
        double correct = 0.0;
        double notCorrect = 0.0;
        for (Triple key : errorTriples) {
            System.out.println("triple: " + key + "\n");

            String errorObject = key.get_obj();
            System.out.println("errorObject: " + errorObject + "\n");

            String correction = correctErrors(errorObject);
            Triple correctTriple;
            if (correction != null) {
                correctTriple = new Triple(key.get_subject(), key.get_predicate(), correction);
                correct = correct + 1;
            } else
                correctTriple = new Triple(key.get_subject(), key.get_predicate(), null);
            notCorrect = notCorrect + 1;
            correctionPairs.put(key, correctTriple);
            pairs.add(correctionPairs);
            System.out.println(errorTriples + " : " + correctTriple + "\n");
        }
        double precision = correct / (correct + notCorrect);
        System.out.println("precision" + " : " + precision + "\n");
        return pairs;
    }

    public String correctErrors(String errorEntity) {

        //before processing, we need to wikiPageRedirects the entity.
        // condition1: owl:sameAs --- country property
        // condition2: instance of in wikidata--- results: country property
        //condition3: rdfs:sameAs in dbpedia, results have the type of country
        //condition4: rdfs:Comments--supervised learning keyword match--compare the results in the range of relation
        //conditions5: dbo:related

        //condition1: owl:sameAs
        String correctionCondition1 = sameAsProperty(errorEntity);
        String correctionCondition2 = seeAlsoProperty(errorEntity);
        String correctionCondition3 = populationPlaceProperty(errorEntity);

        if (correctionCondition1 != null) {

            return correctionCondition1;

        } else if (correctionCondition2 != null) {
            //condition2: rdfs:sameAs

            return correctionCondition2;
        } else if (correctionCondition3 != null) {
            return correctionCondition3;
        } else // name entity Recognition
            return null;

    }

    public String sameAsProperty(String errorEntity) {

        String errorQuery = " select ?a where {<" + redictEntity(errorEntity) + "> owl:sameAs ?a." +
                "filter(regex(?a,\"wikidata.org\"))}";

        System.out.println(errorQuery + "\n");
        ArrayList<String> matchError = new Sparql().getSingleResultsFromQuery(errorQuery);

        if (matchError != null) {

                String correctPropertyQuery = "select distinct ?a where{<" + matchError.get(0) + "> "
                        + Property.relationPropertyInWikidata + " ?a.}";

                System.out.println("wikidata--correctQuery:" + correctPropertyQuery);

                ArrayList<String> finalResults = new ArrayList<>();

                ArrayList<String[]> propertyResults = new wikidataSparql().getCommands(correctPropertyQuery);
                System.out.println("test");
                for (int i = 1; i < propertyResults.size(); i++) {
                    String[] ke = propertyResults.get(i);
                    for (String k : ke) {
                        finalResults.add(k);
                        System.out.println(k);
                    }
                }
                System.out.println("test---finally");

                String instanceQuery = "select distinct ?a where{<" + matchError.get(0) + "> p:P31 ?A.\n" +
                        //    "  ?A ps:P31 wd:Q231002. " +// property:nationality of
                        "?A pq:P642 ?a. }";

                ArrayList<String[]> resultsOfInstance = new wikidataSparql().getCommands(instanceQuery);
                System.out.println("test--value");
                for (int i = 1; i < resultsOfInstance.size(); i++) {
                    String[] ke = resultsOfInstance.get(i);
                    for (String k : ke) {
                        finalResults.add(k);
                        System.out.println(k);
                    }
                }

                System.out.println("test-value--finally");
                String oneResult = null;
                System.out.println(finalResults + "   test \n ");
                if (!finalResults.isEmpty()) {
                    oneResult = compareSimilarity2(finalResults, errorEntity);
                    String corresondQuery =
                            "select ?a where {?a owl:sameAs " + new wikidataSparql().replaceQueryHeader(oneResult) + " }";
                    System.out.println("reverse-query-dbpedia:" + corresondQuery + "\n");
                    ArrayList<String> correction = new Sparql().getSingleResultsFromQuery(corresondQuery);
                    if (!correction.isEmpty())
                        return correction.get(0);
                    else
                        return null;
                } else
                    return null;

        } else

            return null;
        }



    public Boolean sameAsPropertyContainRangeProperty(String errorEntity) {
        // in wikidata, the property of instance of has the results of country(wd: Q6256)
        String errorQuery = " select ?a where {<" + redictEntity(errorEntity) + "> owl:sameAs ?a." +
                "filter(regex(?a,\"wikidata.org\"))}";
        System.out.println("errorQuery  " + errorQuery);
        ArrayList<String> matchError = new Sparql().getSingleResultsFromQuery(errorQuery);
        String correctPropertyQuery = "select distinct ?a where{ BIND(wdt:P31 AS ?instanceOf)." +
                "<" + matchError.get(0) + "> " + "?instanceOf ?a." +
                "filter(?a in( <" + Property.rangePropertyInwikiData + ">)) }";
        System.out.println("wikidata--correctQuery:" + correctPropertyQuery);
        ArrayList<String[]> propertyResults2 = new wikidataSparql().getCommands(correctPropertyQuery);
        System.out.println("propertyResults2.size()  :" + propertyResults2.size());
        return propertyResults2.size() == 2;
    }

    public String seeAlsoProperty(String errorEntity) {

        String query = " select distinct ?a where {<" + redictEntity(errorEntity) + "> rdfs:seeAlso ?a. ?a a ?c." +
                " filter(regex(?c, \"" + Property.simpleLabel + "\")) }";
        System.out.println("seeAlso-query:" + query + "\n");
        ArrayList<String> correction = new Sparql().getSingleResultsFromQuery(query);
        System.out.println("seeAlso-results:" + correction + "\n");

        if (!correction.isEmpty())
            return compareSimilarity2(correction, errorEntity);
        else
            return null;
    }

    public String correctionEntityInDbpedia(String errorEntity){

        String correctionCondition2 = seeAlsoProperty(errorEntity);
        String correctionCondition3 = populationPlaceProperty(errorEntity);

        if (correctionCondition2 != null)
            return correctionCondition2;
         else
             return correctionCondition3;


    }

    //consider the Jaccard index to get the similarity results.
    public String compareSimilarity2(ArrayList<String> results, String errorEntity) {
        HashMap<String, Double> targets = new HashMap<>();

        String[] id2 = errorEntity.split("/");
        for (String ke : results) {
            String[] kSplits = ke.split("/");
            double similarityRatio =  SimilarityAlgorithms.getLevenshteinDistance(id2[id2.length - 1], kSplits[kSplits.length - 1]);
            targets.put(ke, similarityRatio);
        }
        HashMap<String, Double> reverseResults = new RuleLearnerHelper().reverseOrderByValue(targets);
        String firstKey = reverseResults.keySet().iterator().next();
        double value = reverseResults.get(firstKey);
        return firstKey;

    }

    public String compareSimilarity(ArrayList<String[]> results, String errorEntity) {
        HashMap<String, Double> targets = new HashMap<>();

        String[] id2 = errorEntity.split("/");
        for (int i = 1; i < results.size(); i++) {
            String[] ke = results.get(i);
            for (String k : ke) {
                String[] kSplits = k.split("/");
                double similarityRatio = SimilarityAlgorithms.getLevenshteinDistance(id2[id2.length - 1], kSplits[kSplits.length - 1]);
                targets.put(k, similarityRatio);
            }
        }
//        for (String[] ke : results) {
//            for (String k : ke) {
//                String[] kSplits = k.split("/");
//                float similarityRatio = +lt.getSimilarityRatio(id2[id2.length - 1], kSplits[kSplits.length - 1], true);
//                targets.put(k, (double) similarityRatio);
//            }
//        }

        HashMap<String, Double> reverseResults = new RuleLearnerHelper().reverseOrderByValue(targets);
        String firstKey = reverseResults.keySet().iterator().next();
        double value = reverseResults.get(firstKey);
        return firstKey;

    }

    public String redictEntity(String entity) {
        String redictQuery = " select ?a where {<" + entity + "> dbo:wikiPageRedirects ?a. }";

        ArrayList<String> redict = new Sparql().getSingleResultsFromQuery(redictQuery);
        if (redict.isEmpty())
            return entity;
        else
            return redict.get(0);
    }

    //for example:http://dbpedia.org/resource/Cherokee
    // use the dbo:populationPlace
    public String populationPlaceProperty(String errorEntity) {
        // String errorEntity="http://dbpedia.org/resource/Cherokee";
        //  String range="http://dbpedia.org/ontology/Country";
        String errorEntity1 = redictEntity(errorEntity);

        String query = "select ?c (count(?c) as ?count) where {<" + errorEntity1 + "> a ?a. ?b rdfs:domain ?a. <" + errorEntity1 + "> ?b ?c.\n" +
                "?c a <" + Property.RANGE + ">. } group by ?c";
        System.out.println("query  " + query);
        HashMap<String, Double> values = new Sparql().countResults(query);
      //  HashMap<String, Double> values = new HashMap<>(elements);
      //  System.out.println("elements  " + elements);

        String queryOther = "select ?d (count(?d) as ?count) where {<" + errorEntity1 + "> a ?a. ?b rdfs:domain ?a. <" + errorEntity1 + "> ?b ?c.\n" +
                "?c <" + Property.samePropertyOfRelation + "> ?d. } group by ?d";
        System.out.println("queryOther  " + queryOther);
        HashMap<String, Double> others = new Sparql().countResults(queryOther);
        System.out.println("others  " + others);

        for (String ele : others.keySet()) {
            if (values.containsKey(ele)) {
                double oldKey = values.get(ele);
                double ke = others.get(ele);
                double newKe = oldKey + ke;

                values.replace(ele, oldKey, newKe);

            } else {
                values.put(ele, others.get(ele));
            }

        }
        System.out.println("values  " + values);
        if (!values.isEmpty()) {
            HashMap<String, Double> reverseResults = new RuleLearnerHelper().reverseOrderByValue(values);

            String firstKey = reverseResults.keySet().iterator().next();
            // double value = reverseResults.get(firstKey);
            return firstKey;
        } else {
            return null;
        }
    }

    public String populationPlaceProperty2(String errorEntity) {
        // String errorEntity="http://dbpedia.org/resource/Cherokee";
        String errorEntity1 = redictEntity(errorEntity);
        String newQuery = "select distinct ?b ?c  where {?a dbo:populationPlace ?b." +
                " {?b <" + Property.samePropertyOfRelation + "> ?c.} union{ ?b a <" + Property.RANGE + ">} filter(?a=<" + errorEntity1 + ">)}";
        ArrayList<HashMap<String, String>> elements = new Sparql().getResultsFromQuery(newQuery);
        ArrayList<String> values = new ArrayList<>();
        HashMap<String, Double> countValues = new HashMap<>();
        if (elements != null) {
            for (HashMap<String, String> key : elements) {
                String predicate = key.get("b");
                String object = key.get("c");
                if (object != null)
                    values.add(object);
                 else
                    values.add(predicate);
            }
        }
        if (!values.isEmpty()) {
            for (String val : values) {
                if (!countValues.containsKey(val)) {
                    double occurrences = Collections.frequency(values, val);
                    countValues.put(val, occurrences);
                }
            }
            HashMap<String, Double> reverseResults = new RuleLearnerHelper().reverseOrderByValue(countValues);
            String firstKey = reverseResults.keySet().iterator().next();
            return firstKey;
        } else {
            return null;
        }
    }


    public static void main(String[] args) throws Exception {

        new CorrectionInDBPedia().populationPlaceProperty2("http://dbpedia.org/resource/Cherokee");
//        HashSet<Triple> negativeTriple = new CorrectionInDBPedia().filterNegativeTriples(domain, range, predicate,
//                number);
//        int i = 0;
//        for (Triple key : negativeTriple) {
//            System.out.println(++i + key.toString() + "\n");
//
//            System.out.println("correction-processing\n");
//            HashSet<HashMap<Triple, Triple>> correctPairs = new CorrectionInDBPedia().correctTriple(negativeTriple, range);
//            for (HashMap<Triple, Triple> element : correctPairs) {
//
//                try {
//                    File file = new File("/home/wy/Desktop/test-correction/file-12-10-2.txt");
//                    FileWriter fileReader = new FileWriter(file); // A stream that connects to the text file
//                    BufferedWriter bufferedWriter = new BufferedWriter(fileReader); // Connect the FileWriter to the BufferedWriter
//                    int j = 0;
//                    for (Triple s : element.keySet()) {
//                        bufferedWriter.write(++j + "\t" + s + "\t" + element.get(s) + "\n");
//                    }
//
//                    bufferedWriter.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        //       }


    }
}
