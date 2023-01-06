package gilp.GILLearn_correction;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.Triple;
import gilp.similarityMeasure.SimilarityAlgorithms;
import gilp.sparql.Sparql;
import gilp.sparql.wikidataSparql;
import gilp.utils.AuxiliaryParameter;
import info.debatty.java.stringsimilarity.JaroWinkler;
import javatools.parsers.NumberFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * in the closed world, using the type range to find all errors in the KB,
 * then using the owl:sameAs to do the correction in the wikidata.
 * the main function is CorrectionSingleErrors().
 * for single errors to correction.
 */

public class SingleErrorCorrection {
    /*
    based on the type to get the errors.
     */




    public HashSet<Triple> filterOtherNegativeTriples(int number) {

        String queryRough = "select distinct ?subject ?object  ?wrongObjectType from <http://dbpedia.org> where {"
                + " ?subject <" + Property.PREDICATE_NAME + "> ?object."
                + "				 ?subject rdf:type <" + Property.DOMAIN + ">. "
                + "				 ?object rdf:type ?wrongObjectType."
                + "			 FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\"))"
                + "				FILTER NOT EXISTS {?object a <" + Property.RANGE + ">.} }";

        String queryNegative1 = "select distinct * from <http://dbpedia.org> where {" + " ?subject <" + Property.PREDICATE_NAME
                + "> ?object." + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                + "	 ?object rdf:type ?wrongObjectType. "
                + "FILTER NOT EXISTS {"
                + "{?wrongObjectType a  <" + Property.RANGE + ">.} union"
                + "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + Property.RANGE + ">. } union"
                + "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + Property.RANGE
                + ">.} union "
                + "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C. " +
                "?C rdfs:subClassOf <" + Property.RANGE + ">.} union "
                + "{?wrongObjectType owl:equivalentClass <" + Property.RANGE
                + ">.} union " + "{ <" + Property.RANGE + "> owl:equivalentClass ?wrongObjectType.} union "
                + "{ ?wrongObjectType owl:equivalentClass <" + Property.RANGE + ">.}}"
                + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") && ?wrongObjectType != <"
                + Property.RANGE + ">) } ORDER BY RAND() limit " + number;

        String queryNegative = "select distinct ?subject ?object from <http://dbpedia.org> where {" + " ?subject <" + Property.PREDICATE_NAME
                + "> ?object." + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                + "	 ?object rdf:type ?wrongObjectType. " + "FILTER NOT EXISTS {"
                + "{?wrongObjectType rdfs:subClassOf  <" + Property.RANGE + ">.} union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + Property.RANGE + ">. } union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + Property.RANGE
                + ">.} union " +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C. ?C rdfs:subClassOf <"
                + Property.RANGE + ">.} union " +

                "{?wrongObjectType owl:equivalentClass " + " <" + Property.RANGE + ">.} union " +

                "{ <" + Property.RANGE + "> owl:equivalentClass ?wrongObjectType.} union "
                + "{ ?wrongObjectType owl:equivalentClass <" + Property.RANGE + ">.}}"

                + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") )"

                + "FILTER NOT EXISTS {?object a <" + Property.RANGE + ">.} }ORDER BY RAND() limit " + number;

        System.out.println("\n queryNegative:" + queryNegative + "\n");


        ArrayList<HashMap<String, String>> negativeSubjectOBject = new Sparql().getResultsFromQuery(queryNegative);
        HashSet<Triple> negativeTriple = new HashSet<Triple>();
        //  System.out.println("\n negative---samples" + "\n");

        HashSet<String> samekey = new HashSet<String>();
        for (HashMap<String, String> key : negativeSubjectOBject) {
            String subject = key.get("subject");
            String object = key.get("object");
            //     String objectType = key.get("wrongObjectType");

            if (!samekey.contains(subject)) {
                samekey.add(subject);

                Triple negativeElement = new Triple(subject, Property.PREDICATE_NAME, object);

                negativeTriple.add(negativeElement);

                //   System.out.println(negativeElement  + "\n");
            }

        }
        return negativeTriple;
    }

    public HashSet<String> querySiblingByClass(String classString) {
        //range contain the dbo:
        String query = "select distinct (str(?s) as ?class) where {<" + classString + "> rdfs:subClassOf ?p. ?s rdfs:subClassOf ?p. \n" +
                "                FILTER(?s != <" + classString + "> " +
                "&& strstarts(str(?s), \"http://dbpedia.org/ontology/\"))}";
        System.out.println(query);
        HashSet<String> siblings = new Sparql().getSingleVariable(query);
        System.out.println(siblings.size());
        System.out.println(siblings);
        return siblings;
    }

    public HashSet<String> queryAncestorByClass(String classString) {
        //range contain the dbo:
        String query = "select distinct (str(?a) as ?class) where {<" + classString + "> rdfs:subClassOf* ?a.  \n" +
                "                FILTER(strstarts(str(?a), \"http://dbpedia.org/ontology/\"))}";


        System.out.println(query);
        HashSet<String> ancestors = new Sparql().getSingleVariable(query);
        System.out.println(ancestors.size());
        System.out.println(ancestors);
        return ancestors;
    }

    public HashSet<String> queryDescendantByClass(String classString) {
        //range contain the dbo:
        String query = "select distinct (str(?a) as ?class) where {?a rdfs:subClassOf*  <" + classString + "> .  \n" +
                "                FILTER(strstarts(str(?a), \"http://dbpedia.org/ontology/\"))}";


        System.out.println(query);
        HashSet<String> descendants = new Sparql().getSingleVariable(query);
        System.out.println(descendants.size());
        System.out.println(descendants);
        return descendants;
    }

    public HashSet<String> queryEquivalentClass(String classString) {

        String query = "    select distinct ?wrongObjectType   from <http://dbpedia.org> where" +
                " { {?wrongObjectType owl:equivalentClass  <" + classString + ">.}" +
                " union { <" + classString + "> owl:equivalentClass ?wrongObjectType.}}\n";

        System.out.println(query);
        HashSet<String> equivalentClass = new Sparql().getSingleVariable(query);
        System.out.println(equivalentClass.size());
        System.out.println(equivalentClass);
        return equivalentClass;
    }

    public void filterTripleByType() {
        String positiveQuery = "select distinct ?object  from <http://dbpedia.org>  where { ?subject <" + Property.PREDICATE_NAME + "> ?object." +
                " ?subject rdf:type <" + Property.DOMAIN + ">. ?object rdf:type <" + Property.RANGE + ">.} limit 50";

        String negativeQuery = "select distinct ?object  from <http://dbpedia.org>  where { ?subject <" + Property.PREDICATE_NAME + "> ?object." +
                " ?subject rdf:type <" + Property.DOMAIN + ">. " +
                "FILTER NOT EXISTS {?object rdf:type <" + Property.RANGE + ">.}} limit 50";
        HashSet<String> positiveTargets = new Sparql().getSingleVariable(positiveQuery);

        HashSet<String> negativeTargets = new Sparql().getSingleVariable(negativeQuery);

        int allSize = positiveTargets.size() + negativeTargets.size();
        int truePositive = 0;
        int falsePositive = 0;
        int empty = 0;

        int truePositive2 = 0;
        int falsePositive2 = 0;
        int empty2 = 0;
        HashMap<String, String> positiveCorrection = new HashMap<>();
        HashMap<String, String> positiveCorrection2 = new HashMap<>();
        for (String key : positiveTargets) {
            String correction1 = sameAsProperty(key);
            if (correction1 == null)
                empty = empty + 1;
            else if (correction1.equals(key))
                truePositive = truePositive + 1;
            else
                falsePositive = falsePositive + 1;
            positiveCorrection.put(key, correction1);
            String correction2 = new CorrectionInDBPedia().correctionEntityInDbpedia(key);

            if (correction2 == null)
                empty2 = empty2 + 1;
            else if (correction2.equals(key))
                truePositive2 = truePositive2 + 1;
            else
                falsePositive2 = falsePositive2 + 1;
            positiveCorrection2.put(key, correction2);

        }

        int trueNegative = 0;
        int falseNegative = 0;
        int emptyNeg = 0;

        int trueNegative2 = 0;
        int falseNegative2 = 0;
        int emptyNeg2 = 0;
        HashMap<String, String> negativeCorrection = new HashMap<>();
        HashMap<String, String> negativeCorrection2 = new HashMap<>();
        for (String key : negativeTargets) {

            String correction1 = sameAsProperty(key);

            if (correction1 == null)
                emptyNeg = emptyNeg + 1;
            else if (correction1.equals(key))
                trueNegative = trueNegative + 1;
            else
                falseNegative = falseNegative + 1;

            negativeCorrection.put(key, correction1);

            String correction2 = new CorrectionInDBPedia().correctionEntityInDbpedia(key);

            if (correction2 == null)
                emptyNeg2 = emptyNeg2 + 1;
            else if (correction2.equals(key))
                falseNegative2 = falseNegative2 + 1;
            else
                trueNegative2 = trueNegative2 + 1;

            negativeCorrection2.put(key, correction2);

        }

        double precision = (double) truePositive / (truePositive + falsePositive);
        double precision2 = (double) truePositive2 / (truePositive2 + falsePositive2);
        double emptyRate = (double) (empty + emptyNeg) / allSize;
        double emptyRate2 = (double) (empty2 + emptyNeg2) / allSize;
        double correctionRatePositive = (double) falsePositive / (falsePositive + truePositive);
        double correctionRatePositive2 = (double) falsePositive2 / (falsePositive2 + truePositive2);
        double correctionRateNegative = (double) trueNegative / (trueNegative + falseNegative);
        double correctionRateNegative2 = (double) trueNegative2 / (trueNegative2 + falseNegative2);
        double accuracy = (double) (trueNegative + truePositive) / (allSize - empty - emptyNeg);
        double accuracy2 = (double) (trueNegative2 + truePositive2) / (allSize - empty2 - emptyNeg2);
        System.out.println("allSize:" + allSize + "\n");
        System.out.println("tp:" + truePositive + "\t" + truePositive2 + "\n");
        System.out.println("fp:" + falsePositive + "\t" + falsePositive2 + "\n");
        System.out.println("tn:" + trueNegative + "\t" + trueNegative2 + "\n");
        System.out.println("fn:" + falseNegative + "\t" + falseNegative2 + "\n");
        System.out.println("empty:" + empty + "\t" + empty2 + "\n");

        System.out.println("precision:" + precision + "\t" + precision2 + "\n");
        System.out.println("emptyRate:" + emptyRate + "\t" + emptyRate2 + "\n");
        System.out.println("correctionRatePositive:" + correctionRatePositive + "\t" + correctionRatePositive2 + "\n");
        System.out.println("correctionRateNegative:" + correctionRateNegative + "\t" + correctionRateNegative2 + "\n");
        System.out.println("accuracy:" + accuracy + "\t" + accuracy2 + "\n");

        write(positiveCorrection, "positiveCorrection");
        write(positiveCorrection2, "positiveCorrection2");
        write(negativeCorrection, "negativeCorrection");
        write(negativeCorrection2, "negativeCorrection2");
    }

    public static ArrayList<String> labelsProperty(String label) {
        StringBuilder sb = new StringBuilder(label);
        sb.replace(1, 2, String.valueOf(Character.toLowerCase(label.charAt(1))));
        String newLabel = sb.toString();

        String labelQuery = "SELECT distinct ?itemLabel WHERE{  \n" +
                " { ?itemA ?label " + label + ".  } union { ?itemA ?label " + newLabel + ".} " +
                "  {?itemA  wdt:P17 ?item.}UNION {?itemA  p:P31 ?A. ?A pq:P642 ?item. } " +
                "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } }";

        System.out.println("wikidata--correctProperty:" + labelQuery);
        ArrayList<String[]> propertyResults = new wikidataSparql().getCommands(labelQuery);
        ArrayList<String> finalResults = new ArrayList<>();
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

        System.out.println("finalResults: " + finalResults + "\n");


        return finalResults;


    }

    public String sameAsProperty(String errorEntity) {
        String correction = null;
        String errorQuery = " select ?a where {{<" + errorEntity + "> dbo:wikiPageRedirects ?b. ?b owl:sameAs ?a.}" +
                " union{<" + errorEntity + ">  owl:sameAs ?a. } " +
                "filter(regex(?a,\"wikidata.org\"))}";


//        String errorQuery = " select ?a where {<" + new CorrectionInDBPedia().redictEntity(errorEntity) + "> owl:sameAs ?a." +
//                "filter(regex(?a,\"wikidata.org\"))}";
        HashSet<String> matchError = new Sparql().getSingleVariable(errorQuery);
        System.out.println(errorQuery + "\n");
        String error = null;
        for (String key : matchError) {
            error = key;
            break;
        }

        if (error != null) {
            String correctProperty = "select distinct ?itemLabel where{" +
                    "{<" + error + "> " + Property.relationPropertyInWikidata + " ?item.}" +
                    "UNION {<" + error + "> p:P31 ?A. ?A pq:P642 ?item. }" +
                    "SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }}";
            //    "  ?A ps:P31 wd:Q231002. " +// property:nationality of
            System.out.println("wikidata--correctProperty:" + correctProperty);
            ArrayList<String[]> propertyResults = new wikidataSparql().getCommands(correctProperty);
            ArrayList<String> finalResults = new ArrayList<>();
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

            System.out.println("finalResults: " + finalResults + "\n");
            if (!finalResults.isEmpty()) {
                if (finalResults.size() > 1)
                    correction = new CorrectionInDBPedia().compareSimilarity2(finalResults, errorEntity);
                else
                    correction = finalResults.get(0);
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
        return correction;

    }

    public void write(HashMap<String, String> Correction, String label) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("./data/estimation/" + label + ".tsv"));
            for (String key : Correction.keySet())
                out.write(key + "\t" + Correction.get(key) + "\n");
            out.close();

        } catch (IOException ignored) {
        }
    }

    public void CorrectionSingleErrors() {
        HashMap<String, Integer> errors = AuxiliaryParameter.readCount("./data/dbpedia-type/errors.txt");

        HashSet<String> errorEntities = new HashSet<>(errors.keySet());

        int allSize = 0;
        int truePositive = 0;
        int falsePositive = 0;
        int empty = 0;

        int trueNegative = 0;
        int falseNegative = 0;
        int emptyNeg = 0;

        HashMap<String, String> negativeCorrection = new HashMap<>();
        int iterationTime = 1;
        for (String key : errorEntities) {
            System.out.println("test------" + iterationTime++ + "------------\n");
            allSize = allSize + errors.get(key);

            String correction = sameAsProperty(key);

            if (correction == null)
                emptyNeg = emptyNeg + 1;
            else if (correction.equals(key))
                falseNegative = falseNegative + 1;
            else
                truePositive = truePositive + 1;

            negativeCorrection.put(key, correction);


        }

        double precision = (double) truePositive / (truePositive + falsePositive);
        double emptyRate = (double) (empty + emptyNeg) / allSize;
        double correctionRatePositive = (double) falsePositive / (falsePositive + truePositive);
        double correctionRateNegative = (double) trueNegative / (trueNegative + falseNegative);
        double accuracy = (double) (trueNegative + truePositive) / (allSize - empty - emptyNeg);
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

        write(negativeCorrection, "negativeCorrection");

    }

    public void readExcel(String path) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);
        HashMap<String, String> singleCorrections = new HashMap<>();
        HashSet<String> emptyCorrections = new HashSet<>();
        HashSet<ArrayList<String>> roughCorrections = new HashSet<>();
        for (Row row : sheet) {
            ArrayList<String> data = new ArrayList<>();
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case NUMERIC:
                        System.out.print(cell.getNumericCellValue() + "\t\t");
                        break;
                    case STRING:
                        data.add(cell.getStringCellValue());
                        break;
                }
            }
            if (data.size() == 2) {
                singleCorrections.put(data.get(0), data.get(1));
            } else if (data.size() == 1) {
                emptyCorrections.add(data.get(0));
            } else {
                roughCorrections.add(data);

            }

        }
        write(singleCorrections, "singleCorrections");


        writeCorrections(emptyCorrections, "emptyCorrections");
        writeCorrections2(roughCorrections, "roughCorrections");
        System.out.println(singleCorrections.size() + "singleCorrections\n");
        System.out.println(emptyCorrections.size() + "emptyCorrections\n");
        System.out.println(roughCorrections.size() + "roughCorrections\n");


    }

    public void writeCorrections(HashSet<String> Correction, String label) throws IOException {

        BufferedWriter out = new BufferedWriter(new FileWriter("./data/estimation/error-redirect/" + label + ".tsv"));
        for (String key : Correction)
            out.write(key + "\t" + "\n");
        out.close();

    }

    public void writeCorrections2(HashSet<ArrayList<String>> Correction, String label) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("./data/estimation/error-redirect/" + label + ".tsv"));
            for (ArrayList<String> key : Correction) {
                for (String corr : key)
                    out.write(corr + "\t");
                out.write("\n");
            }
            out.write("\n");
            out.close();

        } catch (IOException ignored) {
        }
    }

    public void readRoughCorrection(String path) throws IOException {
        //HashSet<ArrayList<String>>
        HashSet<ArrayList<String>> Data = new HashSet<>();
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(path))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t");
                ArrayList<String> items = new ArrayList<>();
                for(String it : lineItems) {
                    if(it!=null)
                        items.add(it);
                }
                Data.add(items);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        BufferedWriter out = new BufferedWriter(new FileWriter("./data/estimation/error-old/rough-similarity-precision.tsv"));

        for (ArrayList<String> items : Data) {
            String original = items.get(0);
            items.remove(original);
            out.write(original + "\t");
            HashMap<String, Double> correction = compareSimilarity(items, original);
          //  String firstKey = correction.keySet().iterator().next();
          //  double value = correction.get(firstKey);
            for (String key : correction.keySet())
                out.write(key +"\t"+ correction.get(key) + "\t");
            out.write("\n");

        }
        out.close();
    }

    public HashMap<String, Double> compareSimilarity(ArrayList<String> results, String errorEntity) {
        HashMap<String, Double> targets = new HashMap<>();

        String[] base = errorEntity.split("/");
        for (String ke : results) {
            String[] kSplits = ke.split("/");
        //    double similarityRatio =   SimilarityAlgorithms.getLevenshteinDistance(id2[id2.length - 1], kSplits[kSplits.length - 1]);
            JaroWinkler jw = new JaroWinkler(); // Jaro-Winkler
            double similarityRatio = jw.similarity(base[base.length - 1], kSplits[kSplits.length - 1]);



            targets.put(ke,  similarityRatio);
        }
        //        String firstKey = reverseResults.keySet().iterator().next();
//        double value = reverseResults.get(firstKey);
        return new RuleLearnerHelper().reverseOrderByValue(targets);

    }

    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();
        //  new SingleErrorCorrection().CorrectionSingleErrors();

      //  String path = "./data/results/single-correction/error-correction-redirect.xlsx";
       // new SingleErrorCorrection().readExcel(path);
        String path ="./data/estimation/error-old/roughCorrections.tsv";
        new SingleErrorCorrection().readRoughCorrection(path);

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

    }


}
