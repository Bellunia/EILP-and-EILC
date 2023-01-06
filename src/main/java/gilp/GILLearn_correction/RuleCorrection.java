package gilp.GILLearn_correction;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import gilp.utils.AuxiliaryParameter;
import javatools.parsers.NumberFormatter;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import gilp.similarityMeasure.SimilarityAlgorithms;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class RuleCorrection {
    public List<HashMap<Triple, Triple>> correctTripleByRule(ArrayList<Rule> positiveRules, ArrayList<Rule> negativeRules) throws Exception {


        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/test/correction-rules-new.tsv"),
                Charset.forName("UTF-8"));

        List<HashMap<Triple, Triple>> allCorrectionsForNegative = new ArrayList<>();

        MultiValueMap correctionTriples = new MultiValueMap();
        MultiValueMap correctionRefineTriples = new MultiValueMap();

        if (!negativeRules.isEmpty()) {

            for (Rule r : negativeRules) {

                //    correctionTriples.putAll(negativeRuleCorrection(r, positiveRules));
                correctionRefineTriples.putAll(negativeRuleRefineCorrection(r, positiveRules));

            }
        }

//        if (correctionTriples != null) {
//            // Set correctionSubjects = correctionTriples.keySet();
//            for (Object originalSubject : correctionTriples.keySet()) {
//                ArrayList<String> values = (ArrayList<String>) correctionTriples.get(originalSubject);
//
//                writer2.write(originalSubject + "\t \t");
//                for (String tri : values) {
//                    writer2.write(tri + "\t");
//                }
//                writer2.write(values.size() + "\n");
//            }
//        }
//        writer2.write("\n-----------------------\n");
        if (correctionRefineTriples != null) {
            //  Set refinecorrectionSubjects = correctionRefineTriples.keySet();
            for (Object originalSubject : correctionRefineTriples.keySet()) {
                ArrayList<String> values = (ArrayList<String>) correctionTriples.get(originalSubject);
                if (values != null) {
                    writer2.write(originalSubject + "\t \t");

                    for (String tri : values) {

                        writer2.write(tri + "\t");
                    }
                    writer2.write(values.size() + "\n");
                }
            }
        }

        writer2.close();

        return allCorrectionsForNegative;
    }

    public MultiValueMap negativeRuleCorrection(Rule r, ArrayList<Rule> positiveRules) throws IOException {
        MultiValueMap correctionTriples = new MultiValueMap();
        if (!r.isEmpty()) {
            int label = 1;
            String headObject = r.get_head().getObject();
            ArrayList<String> allQueries = correctRuleQueries(r, positiveRules);
            correctionTriples.putAll(correctionTriples(headObject, allQueries, label, "general"));
            label++;
        }
        return correctionTriples;
    }

    public MultiValueMap negativeRuleRefineCorrection(Rule r, ArrayList<Rule> positiveRules) throws IOException {

        MultiValueMap correctionRefineTriples = new MultiValueMap();
        if (!r.isEmpty()) {
            int label = 1;
            String headObject = r.get_head().getObject();
            ArrayList<String> allQueries = correctRuleQueries(r, positiveRules);
            ArrayList<String> allRefineQueries = refineRuleQueries(allQueries);
            correctionRefineTriples.putAll(correctionTriples(headObject, allRefineQueries, label, "refine"));
            label++;
        }
        return correctionRefineTriples;
    }

    public HashMap<Triple, Triple> correctNegRuleInWikidata(Rule r) {
        HashMap<Triple, Triple> allCorrectionsForNegative = new HashMap<>();
        HashMap<Triple, Triple> correctionPairs = new HashMap<>();
        RDFPredicate head = r.get_head();
        String object = head.getObject();
        if (!head.isObjectVariable()) {//head object is constant
            String correction = new CorrectionInDBPedia().correctErrors(object);
            HashSet<String> subjects = new RuleLearnerHelper().ruleToSubjects(r);

            for (String subject : subjects) {
                Triple errorTriple = new Triple(subject, Property.PREDICATE_NAME, object);
                Triple correctTriple = new Triple(subject, Property.PREDICATE_NAME, correction);
                correctionPairs.put(errorTriple, correctTriple);
            }
        } else {
            String sparql = new GetSparql().ruleToSparqlIndbpedia(r, object);//object is variable.
            HashSet<String> objects = new RDF3XEngine().getDistinctEntity(sparql);
            if (objects != null) {
                for (String obj : objects) {
                    String objCorrection = new CorrectionInDBPedia().correctErrors(obj);
                    String query = "select ?a where{?a <" + Property.PREDICATE_NAME + "> <" + obj + ">.}";
                    HashSet<String> subs = new RDF3XEngine().getDistinctEntity(query);
                    for (String subject : subs) {
                        Triple errorTriple = new Triple(subject, Property.PREDICATE_NAME, object);
                        Triple correctTriple = new Triple(subject, Property.PREDICATE_NAME, objCorrection);
                        correctionPairs.put(errorTriple, correctTriple);
                    }
                }
            }
            allCorrectionsForNegative.putAll(correctionPairs);
        }
        return allCorrectionsForNegative;
    }

    public ArrayList<String> countConstantRuleQueries(Rule r, ArrayList<Rule> positiveRules) {
        ArrayList<String> allQueries = new ArrayList<>();
        String subject = r.get_head().getSubject();
// on line ,need to redirect
//        String redirectString ="select distinct ?a where {<"+r.get_head().getObject()+"> dbo:wikiPageRedirects ?a }";
//        ArrayList<String> redirectObject = new Sparql().getSingleResultsFromQuery(redirectString);
//        if (!redirectObject.isEmpty()) {
//            r.set_head(new RDFPredicate(subject,r.get_head().getPredicateName(),redirectObject.get(0)));
//        }


        for (Rule posRule : positiveRules) {

            Clause clause = r.getCorrespondingClause();
            String sub = posRule.get_head().getSubject();
            String obj = posRule.get_head().getObject();

            ArrayList<RDFPredicate> myIter = posRule.get_body().getIterator();
            //String newEntity = sparqlInExtendTriple(subject);
            for (RDFPredicate tp : myIter) {
                clause.addPredicate(new RulesOthers().replaceRDFVariables(tp, sub, subject));
            }

            StringBuffer sb = new StringBuffer();
            sb.append(" select distinct ")
                    .append(subject).append(" ").append(obj).append("1")
                    .append(" where {");

            StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);

            allQueries.add(sb1.toString());
            //     System.out.println(sb1.toString());
        }
        return allQueries;
    }

    public HashMap<Triple, Triple> correctionSingleRule(String headObject, ArrayList<String> allQueries) {

        HashMap<Triple, Triple> correctionTriples = new HashMap<Triple, Triple>();
        for (String query : allQueries) {
            String varNum = query.split("where")[0];
            int number = StringUtils.countMatches(varNum, "?");
            HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, number);
            for (String[] elements : getAll) {
                Triple tri;
                Triple triCorrection;

                if (number == 2) {//head's onject is constant.
                    tri = new Triple(elements[0], Property.PREDICATE_NAME, headObject);
                    triCorrection = new Triple(elements[0], Property.PREDICATE_NAME, elements[1]);


                } else {
                    tri = new Triple(elements[0], Property.PREDICATE_NAME, elements[1]);
                    triCorrection = new Triple(elements[0], Property.PREDICATE_NAME, elements[2]);


                }
                correctionTriples.put(tri, triCorrection);
            }
        }

        return correctionTriples;
    }

    public MultiValueMap correctionTriples(String headObject, ArrayList<String> allQueries, int labelTimes, String label) throws IOException {
        //define the triple<subject,errorObject,correctObject>
        Writer writer1 = new OutputStreamWriter(new FileOutputStream("./data/RuleCorrections/test/rules-" + labelTimes + label + "-results.txt"),
                Charset.forName("UTF-8"));

        MultiValueMap correctionTriples = new MultiValueMap();
        //  HashSet<Triple> correctionTriples = new HashSet<>();
        for (String query : allQueries) {
            String varNum = query.split("where")[0];
            int number = StringUtils.countMatches(varNum, "?");
            HashSet<String[]> getAll = new HashSet<>();
            if (ILPLearnSettings.condition == 1) {
                // HashSet<String[]>
                getAll = new RDF3XEngine().getMultipleElements(query, number);
            } else {

                ArrayList<HashMap<String, String>> items = new Sparql().getResultsFromQuery(query);

                if (items != null) {
                    String[] varNum1 = (query.split("where")[0]).split("\\?");
                    String var1 = null;
                    String var2 = null;
                    String var3 = null;

                    var1 = varNum1[1].trim();
                    var2 = varNum1[2].trim();
                    if (number == 2) {
                        for (HashMap<String, String> key : items) {
                            String[] elements = new String[2];
                            elements[0] = key.get(var1);
                            elements[1] = key.get(var2);
                            getAll.add(elements);
                        }
                    } else if (number == 3) {
                        var3 = varNum1[3].trim();
                        for (HashMap<String, String> key : items) {
                            String[] elements = new String[2];
                            elements[0] = key.get(var1);
                            if (key.get(var3) != null)
                                elements[1] = key.get(var3);
                            else
                                elements[1] = key.get(var2);
                            getAll.add(elements);
                        }
                    }

                }
            }
            if (getAll != null) {
                for (String[] elements : getAll) {

                    HashMap<String, String> oldpairs = new HashMap<>();
                    if (elements.length == 2) {// ?b ?b1
                        oldpairs.put(elements[0], headObject);
                        correctionTriples.put(oldpairs, elements[1]);

                    } else if (elements.length == 4) {
                        oldpairs.put(elements[0], elements[1]);
                        correctionTriples.put(oldpairs, elements[3]);

                    } else {//number=3, ?b ?b1 ?b2(refine)

                        //   oldpairs.put(elements[0], elements[1]);
                        oldpairs.put(elements[0], headObject);
                        if (elements[2] != null)
                            correctionTriples.put(oldpairs, elements[2]);
                        else
                            correctionTriples.put(oldpairs, elements[1]);
                    }

                    for (String ele : elements)
                        if (ele != null)
                            writer1.write(ele.replace("http://dbpedia.org/ontology/", "dbo:")
                                    .replace("http://dbpedia.org/resource/", "dbr:") + "\t");

                    writer1.write("\n");
                }
            }
        }

        writer1.close();

        return correctionTriples;
    }


    public ArrayList<String> refineRuleQueries(ArrayList<String> allQueries) {
        //refine : add the union
        ArrayList<String> allRefineQueries = new ArrayList<>();

        for (String query : allQueries) {
            String queryHead = query.split("where")[0];
            String queryTail = query.split("where")[1];
            String[] vars = queryHead.split("\\?");
            String var = vars[vars.length - 1];
            String newVar = var.trim() + "2";

            StringBuilder sb1 = new StringBuilder();
            sb1.append(queryHead).append("?").append(newVar).append(" where").append(queryTail);

            int index = sb1.length() - 2;

            String newString = "{?" + var + " a <" + Property.RANGE + ">} " +
                    "union {?" + var + " <" + Property.samePropertyOfRelation + "> ?" + newVar + "}";
            sb1.insert(index + 1, newString);

            allRefineQueries.add(sb1.toString());
            System.out.println("refine-query: " + sb1.toString());
        }


        return allRefineQueries;
    }

    public ArrayList<String> correctRuleQueries(Rule r, ArrayList<Rule> positiveRules) {
        ArrayList<String> allQueries = new ArrayList<>();

        String subject = r.get_head().getSubject();
        String object = r.get_head().getObject();

        for (Rule posRule : positiveRules) {
            Clause clause = r.getCorrespondingClause();

            String sub = posRule.get_head().getSubject();
            String obj = posRule.get_head().getObject();

            ArrayList<RDFPredicate> myIter = posRule.get_body().getIterator();
            //String newEntity = sparqlInExtendTriple(subject);
            for (RDFPredicate tp : myIter) {
                clause.addPredicate(new RulesOthers().replaceRDFVariables(tp, sub, subject));
            }
            StringBuffer sb = new StringBuffer();
            if (object.startsWith("?")) {
                sb.append(" select distinct ")
                        // .append(subject).append(" ")
                        .append(object).append(" ").append(obj).append("1")
                        .append(" where {");
            } else {
                sb.append(" select distinct ")
                        // .append(subject).append(" ")
                        .append(obj).append("1")
                        .append(" where {");
            }
            StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);
            allQueries.add(sb1.toString());
            System.out.println(sb1.toString());

        }
        return allQueries;
    }

    public ArrayList<String> countConstantsQueries(Rule r, ArrayList<Rule> positiveRules) {
        ArrayList<String> allQueries = new ArrayList<>();

        String subject = r.get_head().getSubject();
        //  String predicate = head[1].toString();
        String object = r.get_head().getObject();


        for (Rule posRule : positiveRules) {
            Clause clause = r.getCorrespondingClause();
            String sub = posRule.get_head().getSubject();
            String obj = posRule.get_head().getObject();

            ArrayList<RDFPredicate> myIter = posRule.get_body().getIterator();
            //String newEntity = sparqlInExtendTriple(subject);
            for (RDFPredicate tp : myIter) {
                clause.addPredicate(new RulesOthers().replaceRDFVariables(tp, sub, subject));
            }

            StringBuffer sb = new StringBuffer();
            sb.append(" select count ")
                    //   .append(subject).append(" ")
                    .append(obj).append("1")
                    .append(" where {");

            StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);

            allQueries.add(sb1.toString());
            System.out.println(sb1.toString());
        }
        return allQueries;
    }

    public static ArrayList<Rule> readAmieRule(String test2) throws IOException {

        BufferedReader TSVReader = new BufferedReader(new FileReader(test2));
        String line = TSVReader.readLine();
        ArrayList<Rule> Data = new ArrayList<>();
        int i = 0;
        try {
            while (line != null) {

                String[] lineItems = line.split("\t");
                String rule = lineItems[0];
                // Rule rdfRule =  new RulesOthers().transformAmieRule( rule);
                Rule rdfRule = new RulesOthers().transformAmieRule2(rule);


                Data.add(rdfRule);

                //    System.out.println(i++ + "\t "+rdfRule+"\n");

                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }

    public void readRuleCorrection(String test2) throws IOException {
        HashMap<String, String> singleCorrection = new HashMap<>();
        HashMap<String, HashMap<String, double[]>> multiCorrections = new HashMap<>();
      //  HashMap<String, double[]> optimalAllCorrection = new HashMap<>();
        BufferedReader TSVReader = new BufferedReader(new FileReader(test2));
        String line = TSVReader.readLine();

        try {
            while (line != null) {
                System.out.println("test" + line);
                String[] lineItems = line.split("\t");
//                for (String ke : lineItems)
//                    System.out.println(ke.replace("\"",""));
                String error = lineItems[0];

                ArrayList<String> elements =
                        new ArrayList<>(Arrays.asList(lineItems).subList(1, (lineItems.length - 1)));
                HashMap<String, Integer> repairs = new SimilarityAlgorithms().countFrequencies(elements);

                if (repairs.size() == 1)
                    singleCorrection.put(error, lineItems[1]);
                else {
                  //  HashMap<String, double[]> optimalCorrection = filterCorrection(error, repairs);

                    HashMap<String, double[]> optimalCorrection =  new SimilarityAlgorithms().crossNewSimilarity(error, repairs);
                    HashMap<String, double[]> firstFilter =  new SimilarityAlgorithms().judgeSimilarity(optimalCorrection, 15);
                    //type=11, the sum_cosine(2)= sim_in +sim_out// type=15, distance(sim_in)
                    multiCorrections.put(error, firstFilter);
               //     optimalAllCorrection.putAll(optimalCorrection);
                }

                line = TSVReader.readLine();
            }

        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        TSVReader.close();


        writeCorrection(singleCorrection);
        writeMultiCorrection(multiCorrections);
      //  writeCorrectionPrecision(optimalAllCorrection);


    }

    public void writeMultiCorrection(HashMap<String, HashMap<String, double[]>> optimalCOrrection) throws IOException {
        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/test/final-repairs-analysis-2020.tsv"),
                Charset.forName("UTF-8"));
       // writer2.write("correction\tratio\tweightedSimilarity\tweightedOutSim\n");
        if (optimalCOrrection != null) {
            for (String correction : optimalCOrrection.keySet()) {

                writer2.write(correction + "\t");
                HashMap<String, double[]> values = optimalCOrrection.get(correction);

                for (String correct : values.keySet()) {
                    writer2.write(correct + "\t");
                    for (double pre : values.get(correct))
                        writer2.write(pre + "\t");

                }
                writer2.write("\n");
            }
        }
        writer2.close();
    }

    public void writeCorrection(HashMap<String, String> allCorrections) throws IOException {
        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/test/singleCorrection1.tsv"),
                Charset.forName("UTF-8"));
        for (String correction : allCorrections.keySet())

            writer2.write(correction + "\t" + allCorrections.get(correction) + "\n");
        writer2.close();
    }

    public void writeCorrectionPrecision(HashMap<String, double[]> optimalAllCorrection) throws IOException {
        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/test/optimalAllCorrection1.tsv"),
                Charset.forName("UTF-8"));
        for (String correction : optimalAllCorrection.keySet()) {

            writer2.write(correction + "\t");
            for (double pre : optimalAllCorrection.get(correction))
                writer2.write(pre + "\t");

            writer2.write("\n");
        }
        writer2.close();
    }



    public static void main(String[] args) throws Exception {
        //  String path = "./data/estimation/error-old/roughCorrections.tsv";
        // new Examples().readRoughCorrection(path);hen y

        long time = System.currentTimeMillis();
        String path = "./data/RuleCorrections/final results/negative-final-rules.txt";
        String path2 = "./data/RuleCorrections/final results/positive-final-rules.txt";
        //   ArrayList<Rule> positiveRules = readAmieRule(path2);
        //  ArrayList<Rule> negativeRules = readAmieRule(path);
//       ArrayList<Rule> negativeRules = new getRules().getAmieRules(false,iterationTimes);
        //       ArrayList<Rule> positiveRules =  new getRules().getAmieRules(true,iterationTimes);
        //    new RuleCorrection().correctTripleByRule(positiveRules, negativeRules);
        String path3 = "./data/RuleCorrections/final results/correction-rules-final.tsv";//2016
        String path2020 = "./data/RuleCorrections/dbpedia-2020/correction-rules-2020-simple.tsv";
        new RuleCorrection().readRuleCorrection(path2020);

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));
        //    String path3="./data/RuleCorrections/dbpedia-2020/correction-rules.tsv";
        //    new RuleCorrection().readRuleCorrection(path3);

        //    new RuleCorrection().compareRules();
        //   new RuleCorrection().countOldEntity();
        //   new RuleCorrection().countNewEntity();

    }

    private void countOldEntity() throws IOException {
        HashSet<String> countries = RuleLearnerHelper.readTypes
                ("/home/wy/gilp_learn/data/RuleCorrections/dbpedia-2016/rdfobjects-new.txt");

        int num1 = 0;
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/count--correction-rules-entity-2016-new.tsv"),
                Charset.forName("UTF-8"));
        for (String correction : countries) {
            String query = "select count ?c where{ ?a <http://dbpedia.org/ontology/nationality> ?c. filter(?c=<" + correction + ">)}";

            HashMap<String, Double> subjectsResources = new RDF3XEngine().getCountSingleEntity(query);
            writer.write(correction + "\t" + subjectsResources.get(correction) + "\n");
            if (subjectsResources.get(correction) != null)
                num1 = (int) (num1 + subjectsResources.get(correction));
            else
                System.out.println("null---2 " + correction + "\n");
        }
        writer.close();
        System.out.println("num1 " + num1 + "\n");
    }

    private void countNewEntity() throws IOException {
        HashSet<String> countries = RuleLearnerHelper.readTypes
                ("/home/wy/gilp_learn/data/RuleCorrections/error-entity-2020.txt");

        String countQuery = " select  ?b (count(?b) as ?count)  where " +
                "{?a  <http://dbpedia.org/ontology/nationality>  ?b.  ?a a dbo:Person.  } group by ?b ";
        HashMap<String, Double> values = new Sparql().countResults(countQuery);

        int num1 = 0;
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/count--correction-rules-entity-2020-new.tsv"),
                Charset.forName("UTF-8"));
        for (String correction : countries) {

            writer.write(correction + "\t" + values.get(correction) + "\n");
            if (values.get(correction) != null)
                num1 = (int) (num1 + values.get(correction));
            else
                System.out.println("null---2 " + correction + "\n");
        }
        writer.close();
        System.out.println("num1 " + num1 + "\n");

    }

    public void compareRules() throws IOException {
        String path2 = "/home/wy/Desktop/3-2/counts-positive-numbers.txt";
        String path3 = "/home/wy/Desktop/3-2/negative-objects-numbers.txt";

        HashMap<String, Integer> counts = AuxiliaryParameter.importNumbers(path3);

        HashMap<String, Integer> counts1 = AuxiliaryParameter.importNumbers(path2);
        counts.putAll(counts1);

        String path = "./data/RuleCorrections/negative-final-rules.txt";

        ArrayList<Rule> negativeRules = readAmieRule(path);
        HashSet<String> objectsAll = new HashSet<>();
        HashSet<String> objectsAll2 = new HashSet<>();
        for (Rule r : negativeRules) {
            String sparql = new GetSparql().ruleToSparqlIndbpedia(r, "?b");//object is variable.
            System.out.println(sparql + "\n");
            HashSet<String> objects = new RDF3XEngine().getDistinctEntity(sparql);
            objectsAll.addAll(objects);

            HashSet<String> objects2 = new Sparql().getSingleVariable(sparql);
            objectsAll2.addAll(objects2);
        }
        int num = 0;
        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/allDistinctObjects.tsv"),
                Charset.forName("UTF-8"));
        for (String correction : objectsAll) {
            writer2.write(correction + "\t" + counts.get(correction) + "\n");
            if (counts.get(correction) != null)
                num = num + counts.get(correction);
            else
                System.out.println("null---1 " + correction + "\n");
        }
        writer2.close();
        int num1 = 0;
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/allDistinctObjects2.tsv"),
                Charset.forName("UTF-8"));
        for (String correction : objectsAll2) {
            writer.write(correction + "\t" + counts.get(correction) + "\n");
            if (counts.get(correction) != null)
                num1 = num1 + counts.get(correction);
            else
                System.out.println("null---2 " + correction + "\n");
        }
        writer.close();
        System.out.println("objectsAll.size " + objectsAll.size() + "\n");
        System.out.println("objectsAll2.size " + objectsAll2.size() + "\n");
        objectsAll.retainAll(objectsAll2);
        System.out.println("same part: " + objectsAll.size() + "\n");

        System.out.println("same num: " + num + "\n");
        System.out.println("same num1: " + num1 + "\n");


    }


}
