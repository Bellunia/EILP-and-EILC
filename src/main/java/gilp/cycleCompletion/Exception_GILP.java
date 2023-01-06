package gilp.cycleCompletion;

import amie.mining.AMIE;
import gilp.GILLearn_correction.*;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import javatools.administrative.Announce;

import javatools.parsers.NumberFormatter;
import org.apache.commons.collections.map.MultiValueMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Exception_GILP {

    public static String[] equivalentProperty = {"http://dbpedia.org/ontology/nationality",
            //    "http://dbpedia.org/ontology/citizenship",
            //    "http://dbpedia.org/ontology/stateOfOrigin"
    };

    public void selectTriples(int number, int filterCondition, Boolean decision, int iterTime) {
        String fileName = null;

        String allFeedbackPath = null;

        for (String name : equivalentProperty) {
            String query = null;
            if (decision) {
                allFeedbackPath = "./prediction/newSearchSpace/positive-all.tsv";
                fileName = "./prediction/newSearchSpace/positive-" + iterTime + ".tsv";
                query = "select distinct ?subject ?object where{ ?subject <" + name + "> ?object." +
                        " ?subject rdf:type <" + Property.DOMAIN + ">."//   + "?object rdf:type <" + Property.RANGE + ">."
                        + "?object rdf:type <" + Property.rangePropertyInwikiData + ">."
                        + "} ORDER BY RAND() limit " + number;
                //        System.out.println("positive query:" + query);

            } else {
                allFeedbackPath = "./prediction/newSearchSpace/negative-all.tsv";
                fileName = "./prediction/newSearchSpace/negative-" + iterTime + ".tsv";
                query = "select distinct ?subject ?object where {"
                        + " ?subject <" + name + "> ?object."
                        + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                        + "?object rdf:type <" + Property.rangeNegativePropertyInwikiData + ">."
                        //  + "FILTER NOT EXISTS { ?object  rdf:type <" + Property.rangePropertyInwikiData + ">} } "
                        + "} ORDER BY RAND() limit " + number;
                //      System.out.println("negative query:" + query);

            }

            HashSet<Triple> triples = filterTriples(query, name, filterCondition);


            try {
                FileWriter writer1 = new FileWriter(allFeedbackPath, StandardCharsets.UTF_8, true);
                FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8);
                for (Triple tri : triples) {
                    writer.write(tri + "\n");
                    writer1.write(tri + "\n");
                }
                writer.close();
                writer1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public HashSet<Triple> filterTriples(String query, String relation, int filterCondition) {
        //i=1;contain different subjects(more); others--different objects(less)
        ArrayList<HashMap<String, String>> pairs = new Sparql().getResultsFromQuery(query);

        MultiValueMap newPairs = new MultiValueMap();
        for (HashMap<String, String> key : pairs)
            newPairs.put(key.get("object"), key.get("subject"));

        HashSet<Triple> triples = new HashSet<>();
        for (Object object : newPairs.keySet()) {
            List<String> subValues = (List<String>) newPairs.get(object);
            if (filterCondition > 0) {
                for (String sub : subValues) {
                    Triple tri1 = new Triple(sub, relation, (String) object);
                    triples.add(tri1);
                }
            } else {
                String randomSub = getRandomElement(subValues);
                Triple tri2 = new Triple(randomSub, relation, (String) object);
                triples.add(tri2);
            }
        }
        return triples;
    }

    private String getRandomElement(List<String> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }


    public void filterAllRules(int number) throws Exception {//

        int filterCondition = 1;//different subjects ---0 :different objects
        int iterationTimes = 15;


        while (true) {

            if (iterationTimes > 20)
                break;
            else
                iterationTimes++;//<=20

            System.out.println(iterationTimes + "------------------");

            String posPath = "./prediction/newSearchSpace/search/positiveSearchSpace" + iterationTimes + "_new.tsv";
            HashSet<Triple> positiveSearch = new test_rules().readTriples(posPath);

            String negPath = "./prediction/newSearchSpace/search/negativeSearchSpace" + iterationTimes + "_new.tsv";
            HashSet<Triple> negativeSearch = new test_rules().readTriples(negPath);

            writePrunedSearchSpace(positiveSearch, true, iterationTimes);
            writePrunedSearchSpace(negativeSearch, false, iterationTimes);

            System.out.println("\n------- finish pruned search space-----------------\n");


            //  HashMap<amie.rules.Rule, Double> positiveRule = amieRules(true, iterationTimes);
            //   HashMap<amie.rules.Rule, Double> negativeRule = amieRules(false, iterationTimes);
            ArrayList<Rule> positiveRule = filterAmieRules(true, iterationTimes);
            ArrayList<Rule> negativeRule = filterAmieRules(false, iterationTimes);

            ArrayList<Rule> negRule = new ArrayList<>(negativeRule);
            negRule.removeAll(positiveRule);

            Writer writer1 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/amienegRules-" + iterationTimes + ".tsv"),
                    StandardCharsets.UTF_8);

            System.out.println("\n------- negative rule-----------------\n");
            for (Rule r : negRule)
                writer1.write(r + "\n");
            writer1.close();

            //    number=number+100;
        }

    }

    public void analysisAllRules() throws Exception {//

        int iterationTimes = 0;

        HashMap<Rule, Integer> newPosPairs = new HashMap<>();
        HashMap<Rule, Integer> newNegPairs = new HashMap<>();
        while (true) {

            if (iterationTimes > 19)
                break;
            else
                iterationTimes++;//<=20

            System.out.println(iterationTimes + "------------------");


            ArrayList<String> positiveString = readStringAmieRule("./prediction/newSearchSpace/amieRules-" + iterationTimes + "-" + true + ".tsv");
            ArrayList<String> negativeString = readStringAmieRule("./prediction/newSearchSpace/amieRules-" + iterationTimes + "-" + false + ".tsv");

            for (String key : positiveString) {
                Rule rdfRule = new RulesOthers().transformAmieRule(key);
                if (!newPosPairs.containsKey(rdfRule))
                    newPosPairs.put(rdfRule, 1);
                else {
                    int num = newPosPairs.get(rdfRule) + 1;
                    newPosPairs.put(rdfRule, num);
                }
            }

            for (String key : negativeString) {
                Rule rdfRule = new RulesOthers().transformAmieRule(key);
                if (!newNegPairs.containsKey(rdfRule))
                    newNegPairs.put(rdfRule, 1);
                else {
                    int num = newNegPairs.get(rdfRule) + 1;
                    newNegPairs.put(rdfRule, num);
                }
            }


        }

        Writer writer1 = new OutputStreamWriter(
                new FileOutputStream("./prediction/newSearchSpace/analysis/positiveRules-all1.tsv"),
                StandardCharsets.UTF_8);

        System.out.println("\n------- negative rule-----------------\n");
        for (Rule r : newPosPairs.keySet())
            writer1.write(r.toAmieString() + "\t" + newPosPairs.get(r) + "\n");
        writer1.close();

        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./prediction/newSearchSpace/analysis/negativeRules-all1.tsv"),
                StandardCharsets.UTF_8);

        System.out.println("\n------- positive rule-----------------\n");
        for (Rule r : newNegPairs.keySet())
            writer2.write(r.toAmieString() + "\t" + newNegPairs.get(r) + "\n");
        writer2.close();


    }

    public ArrayList<Rule> readGilpRule(String PATH) {

        ArrayList<Rule> Data = new ArrayList<>();

        try {
            BufferedReader TSVReader = new BufferedReader(new FileReader(PATH));
            String line = TSVReader.readLine();

            while (line != null) {

                Rule rdfRule = new Rule();

                String[] ruleItems = line.split("\t=>\t");
                String[] headItems = ruleItems[1].split("\t");
                String[] bodyItems = ruleItems[0].split("\t");

                Clause clause = new Clause();
                for (int i = 0; i < bodyItems.length / 3; i++) {
                    RDFPredicate rdfPredicate = new RDFPredicate();
                    rdfPredicate.setSubject(bodyItems[3 * i]);
                    rdfPredicate.setPredicateName(bodyItems[3 * i + 1]);
                    rdfPredicate.setObject(bodyItems[3 * i + 2]);
                    clause.addPredicate(rdfPredicate);
                }
                rdfRule.set_body(clause);

                RDFPredicate rdfPredicate = new RDFPredicate();
                rdfPredicate.setPredicateName(headItems[1]);
                rdfPredicate.setSubject(headItems[0]);
                rdfPredicate.setObject(headItems[2]);

                rdfRule.set_head(rdfPredicate);


                Data.add(rdfRule);

                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }
        return Data;

    }

    public HashMap<Rule,Integer> readEGilpRule(String PATH) {

        HashMap<Rule,Integer> Data = new HashMap<>();

        try {
            BufferedReader TSVReader = new BufferedReader(new FileReader(PATH));
            String line = TSVReader.readLine();

            while (line != null) {

                Rule rdfRule = new Rule();

                String[] ruleItems = line.split("\t=>\t");
                String[] headItems = ruleItems[1].split("\t");
                String[] bodyItems = ruleItems[0].split("\t");

                Clause clause = new Clause();
                for (int i = 0; i < bodyItems.length / 3; i++) {
                    RDFPredicate rdfPredicate = new RDFPredicate();
                    rdfPredicate.setSubject(bodyItems[3 * i]);
                    rdfPredicate.setPredicateName(bodyItems[3 * i + 1]);
                    rdfPredicate.setObject(bodyItems[3 * i + 2]);
                    clause.addPredicate(rdfPredicate);
                }
                rdfRule.set_body(clause);

                RDFPredicate rdfPredicate = new RDFPredicate();
                rdfPredicate.setPredicateName(headItems[1]);
                rdfPredicate.setSubject(headItems[0]);
                rdfPredicate.setObject(headItems[2]);

                rdfRule.set_head(rdfPredicate);


                Data.put(rdfRule, Integer.valueOf(headItems[3]));

                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }
        return Data;

    }

    public void filterIterativeSearchSpace(int number) throws Exception {//

        int filterCondition = 1;//different subjects ---0 :different objects
        int iterationTimes = 0;


        while (true) {
            iterationTimes++;

            System.out.println(iterationTimes + "------------------");
            selectTriples(number, filterCondition, true, iterationTimes);
            String posPath = "./prediction/newSearchSpace/positive-" + iterationTimes + ".tsv";
            HashSet<Triple> positiveTriple = new test_rules().readTriples(posPath);
            //   new test_rules().extendTriplesByLevel(positiveTriple, ILPLearnSettings.DEFAULT_LEVEL, true, iterationTimes);
            //extendTriplesFromObjects
            if (iterationTimes > 1) {
                String posPath2 = "./prediction/newSearchSpace/positive-all.tsv";
                HashSet<Triple> positiveTriple2 = new test_rules().readTriples(posPath2);
                positiveTriple2.removeAll(positiveTriple);
                positiveTriple.removeAll(positiveTriple2);
            }
            new newSearchSpace().filterSearchSpace(positiveTriple, true, iterationTimes);


            selectTriples(number, filterCondition, false, iterationTimes);
            String negPath = "./prediction/newSearchSpace/negative-" + iterationTimes + ".tsv";
            HashSet<Triple> negativeTriple = new test_rules().readTriples(negPath);

            if (iterationTimes > 1) {
                String negPath2 = "./prediction/newSearchSpace/negative-all.tsv";
                HashSet<Triple> negativeTriple2 = new test_rules().readTriples(negPath2);
                negativeTriple2.removeAll(negativeTriple);
                negativeTriple.removeAll(negativeTriple2);
            }

            // new test_rules(). extendTriplesByLevel(negativeTriple, ILPLearnSettings.DEFAULT_LEVEL, false, iterationTimes);
            //extendTriplesFromObjects
            new newSearchSpace().filterSearchSpace(negativeTriple, false, iterationTimes);


            //      pruneSearchSpace(iterationTimes);//focus on here.


            if (iterationTimes > 1) {
                int num = iterationTimes - 1;

                String pathP1 = "./prediction/newSearchSpace/positiveSearchSpace" + num + "_new.tsv";
                String pathP2 = "./prediction/newSearchSpace/positiveSearchSpace" + iterationTimes + ".tsv";
                HashSet<Triple> search1 = new test_rules().readTriples(pathP1);
                HashSet<Triple> search2 = new test_rules().readTriples(pathP2);
                search2.removeAll(search1);

                String pathN1 = "./prediction/newSearchSpace/negativeSearchSpace" + num + "_new.tsv";
                String pathN2 = "./prediction/newSearchSpace/negativeSearchSpace" + iterationTimes + ".tsv";
                HashSet<Triple> search3 = new test_rules().readTriples(pathN1);
                HashSet<Triple> search4 = new test_rules().readTriples(pathN2);
                search4.removeAll(search3);

                if (search2.isEmpty() || search4.isEmpty())
                    break;


            }
            mergeSearchSpace(iterationTimes, true);

            mergeSearchSpace(iterationTimes, false);


//          System.out.println("\n------- finish pruned search space-----------------\n");
//
//
//          //  HashMap<amie.rules.Rule, Double> positiveRule = amieRules(true, iterationTimes);
//          //   HashMap<amie.rules.Rule, Double> negativeRule = amieRules(false, iterationTimes);
//          ArrayList<Rule> positiveRule = filterAmieRules(true, iterationTimes);
//          ArrayList<Rule> negativeRule = filterAmieRules(false, iterationTimes);
//
//          ArrayList<Rule> negRule = new ArrayList<>(negativeRule);
//          negRule.removeAll(positiveRule);
//
//          Writer writer1 = new OutputStreamWriter(
//                  new FileOutputStream("./prediction/newSearchSpace/amienegRules-" + iterationTimes + ".tsv"),
//                  StandardCharsets.UTF_8);
//
//          System.out.println("\n------- negative rule-----------------\n");
//          for (Rule r : negRule)
//              writer1.write(r + "\n");
//          writer1.close();

            number = number + 100;
        }

    }


    public String[] getParameters(Boolean decision, int iterationTimes) {
        //amie+ --parameters
        //  miner =	AMIE.getInstance(new String[] { ontology, "-pm", "support", "-mins", "0" });
        if (decision)
            return new String[]{"./prediction/newSearchSpace/finalSearchSpace/positiveSearchSpace" + iterationTimes + "_new.tsv",
                    "-mins", "1", "-minis", "1",
                    "-maxad", "3",
                    "-bexr", new test_rules().array2String(equivalentProperty),
                    "-htr", new test_rules().array2String(equivalentProperty),
                    "-minhc", "0", "-minpca", "0",
                    "-dpr", "-optimfh",
                    // "-const"
            };

        else
            return new String[]{"./prediction/newSearchSpace/finalSearchSpace/negativeSearchSpace" + iterationTimes + "_new.tsv",
                    "-maxad", "3",
                    "-mins", "1", //String.valueOf(ILPLearnSettings.initialNumberNeg),
                    "-minis", "1", //String.valueOf(ILPLearnSettings.initialNumberNeg),
                    "-bexr", new test_rules().array2String(equivalentProperty),
                    "-htr", new test_rules().array2String(equivalentProperty),
                    "-minhc", "0", "-minpca", "0",
                    "-dpr", "-optimfh",
                    //     "-const"
            };

    }

    public ArrayList<Rule> filterAmieRules(Boolean decision, int iterationTimes) {


        try {
            Announce.doing("\n Starting the mining phase \n");
            AMIE miner = AMIE.getInstance(getParameters(decision, iterationTimes));
            List<amie.rules.Rule> amieRules = miner.mine();

            Announce.done("\n finish the mining phase \n");
            Announce.close();

            //------------------------

            Writer writer1 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/amieRules-" + iterationTimes + "-" + decision + ".tsv"),
                    StandardCharsets.UTF_8);

            if (!amieRules.isEmpty()) {
                for (amie.rules.Rule rule : amieRules) {
                    writer1.write(rule.getFullRuleString() + "\n");
                }
            }
            writer1.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Rule> datalogRules = readAmieRule("./prediction/newSearchSpace/amieRules-" + iterationTimes + "-" + decision + ".tsv");


        return datalogRules;
    }


    public void mergeSearchSpace(int iterationTimes, Boolean decision) {
        HashSet<Triple> mergeSearch = new HashSet<>();
        for (int i = 1; i < iterationTimes + 1; i++) {
            String path = null;
            if (decision) {
                path = "./prediction/newSearchSpace/positiveSearchSpace" + i + ".tsv";
            } else {
                path = "./prediction/newSearchSpace/negativeSearchSpace" + i + ".tsv";
            }
            HashSet<Triple> search = new test_rules().readTriples(path);
            mergeSearch.addAll(search);
        }

        try {
            String path = null;
            if (decision)
                path = "./prediction/newSearchSpace/positiveSearchSpace" + iterationTimes + "_new.tsv";
            else
                path = "./prediction/newSearchSpace/negativeSearchSpace" + iterationTimes + "_new.tsv";
            Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            for (Triple t : mergeSearch) {
                //   if (!Objects.equals(t.get_predicate(), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                writer.write(t + "\n");

                // }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public void pruneSearchSpace(int iterationTimes) {

        String pathPos = "./prediction/newSearchSpace/positiveSearchSpace" + iterationTimes + ".tsv";
        String pathNeg = "./prediction/newSearchSpace/negativeSearchSpace" + iterationTimes + ".tsv";

        HashSet<Triple> positiveSearch = new test_rules().readTriples(pathPos);
        HashSet<Triple> negativeSearch = new test_rules().readTriples(pathNeg);
        //   HashSet<Triple> positiveSearchCopy = new HashSet<>(positiveSearch);
        //   HashSet<Triple> negativeSearchCopy = new HashSet<>(negativeSearch);
        //   positiveSearch.removeAll(negativeSearchCopy);
        //   negativeSearch.removeAll(positiveSearchCopy);
        //    HashSet<Triple> allSearchSpace = new HashSet<>(negativeSearchCopy);
        //     allSearchSpace.addAll(positiveSearchCopy);
        writePrunedSearchSpace(positiveSearch, true, iterationTimes);
        writePrunedSearchSpace(negativeSearch, false, iterationTimes);

        //   writeAllSearchSpace(allSearchSpace, iterationTimes);
    }

    private void writePrunedSearchSpace(HashSet<Triple> expandedTriples, Boolean decision, int iterationTimes) {
        String path = null;
        if (decision)
            path = "./prediction/newSearchSpace/positiveSearchSpace" + iterationTimes + "_new.tsv";
        else
            path = "./prediction/newSearchSpace/negativeSearchSpace" + iterationTimes + "_new.tsv";

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            for (Triple t : expandedTriples) {
                //   if (!Objects.equals(t.get_predicate(), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                writer.write("<" + new test_rules().substitute(t.get_subject()) + ">\t");
                writer.write("<" + new test_rules().substitute(t.get_predicate()) + ">\t");
                writer.write("<" + new test_rules().substitute(t.get_obj()) + ">\n");
                // }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    private void writeAllSearchSpace(HashSet<Triple> expandedTriples, int iterationTimes) {

        try {
            String path = "./prediction/newSearchSpace/allSearchSpace" + iterationTimes + ".tsv";

            Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            for (Triple t : expandedTriples) {
                writer.write(("<" + new test_rules().substitute(t.get_subject()) + ">\t"));
                writer.write(("<" + new test_rules().substitute(t.get_predicate()) + ">\t"));
                writer.write(("<" + new test_rules().substitute(t.get_obj()) + ">\n"));
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<Rule> readAmieRule(String test2) {

        ArrayList<Rule> Data = new ArrayList<>();

        try {
            BufferedReader TSVReader = new BufferedReader(new FileReader(test2));
            String line = TSVReader.readLine();

            while (line != null) {

                String[] lineItems = line.split("\t");
                String rule = lineItems[0];
                Rule rdfRule = new RulesOthers().transformAmieRule(rule);
                //  Rule rdfRule = new RulesOthers().transformAmieRule2(rule);

                Data.add(rdfRule);

                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }


    public static ArrayList<String> readStringAmieRule(String test2) {

        ArrayList<String> Data = new ArrayList<>();

        try {
            BufferedReader TSVReader = new BufferedReader(new FileReader(test2));
            String line = TSVReader.readLine();

            while (line != null) {

                String[] lineItems = line.split("\t");
                String rule = lineItems[0];
                Data.add(rule);

                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }

    public void validateGILPRules() throws IOException {
        String path = "./prediction/newSearchSpace/analysis/negativeRules-all1.tsv";
        ArrayList<Rule> negativeRules = new Exception_GILP().readGilpRule(path);

        ArrayList<Rule> newNegativeRules = new ArrayList<>();
        for (Rule r : negativeRules) {
            Rule newR = singleValidate(r, false);
            newNegativeRules.add(newR);
        }
        Writer writer1 = new OutputStreamWriter(
                new FileOutputStream("./prediction/newSearchSpace/analysis/positiveRules-all--new.tsv"),
                StandardCharsets.UTF_8);

        System.out.println("\n------- negative rule-----------------\n");
        for (Rule r : newNegativeRules)
            writer1.write(r.toAmieString() + "\n");
        writer1.close();


        String path2 = "./prediction/newSearchSpace/analysis/positiveRules-all1.tsv";
        ArrayList<Rule> positiveRules = new Exception_GILP().readGilpRule(path2);
        ArrayList<Rule> newPositiveRules = new ArrayList<>();
        for (Rule r : positiveRules) {
            Rule newR = singleValidate(r, true);
            newPositiveRules.add(newR);
        }


        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./prediction/newSearchSpace/analysis/negativeRules-all--new.tsv"),
                StandardCharsets.UTF_8);

        System.out.println("\n------- positive rule-----------------\n");
        for (Rule r : newPositiveRules)
            writer2.write(r.toAmieString() + "\n");
        writer2.close();

    }

    public Rule singleValidate(Rule r, Boolean decision) {
        Rule rCopy = r.clone();
        String exception = null;
        if (decision) {
            exception = Property.rangePropertyInwikiData;
        } else {
            exception = Property.rangeNegativePropertyInwikiData;
        }

        String objVar = rCopy.get_head().getObject();

        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        rdfPredicate.setSubject(objVar);
        rdfPredicate.setObject(exception);
        rCopy.get_body().addPredicate(rdfPredicate);

        return rCopy;
    }
    private void analysisIterationResults() throws IOException {
        for(int i=1;i<=20;i++){

            String readPositivePath="./prediction/newSearchSpace/rules/positive-distinct/positiveRules-"+i+".tsv";
            String readNegativePath="./prediction/newSearchSpace/rules/negative-distinct/negativeRules-"+i+".tsv";

            String writeOutPositivePath="./prediction/newSearchSpace/rules/positive-distinct/positiveRules-"+i+"-analysis.tsv";
            String writeOutNegativePath="./prediction/newSearchSpace/rules/negative-distinct/negativeRules-"+i+"-analysis.tsv";

            String readRoughPath="./prediction/newSearchSpace/rules/rough/roughRules-"+i+".tsv";

            String writeOutRoughPathPositive="./prediction/newSearchSpace/rules/rough/roughPositiveRules-"+i+".tsv";
            String writeOutRoughPathNegative="./prediction/newSearchSpace/rules/rough/roughNegativeRules-"+i+".tsv";


            new Exception_GILP(). analysisSingleRules(true, 1, writeOutPositivePath,readPositivePath);
            new Exception_GILP(). analysisSingleRules(false, 2, writeOutNegativePath,readNegativePath);
            new Exception_GILP(). analysisSingleRules(true, 1, writeOutRoughPathPositive,readRoughPath);
            new Exception_GILP(). analysisSingleRules(false, 2, writeOutRoughPathNegative,readRoughPath);


        }
    }


    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();


        int number = 100;
        //   new Exception_GILP().filterAllRules(number);
        int iterationTimes = 1;
        //  new Exception_GILP().analysisAllRules();
        //  new Exception_GILP().validateGILPRules();

      //  new Exception_GILP().  simpleAllRules();




            String originalPath="./prediction/amie-exception/roughPositiveRules-1.tsv";
            String amiePath="./prediction/newSearchSpace/rules/positive/amieRules-1-true.tsv";
            String finalPath="./prediction/amie-exception/exception/rough-positive-exception-1.tsv";

            new Exception_GILP().amieException(originalPath, amiePath,finalPath);


   //     new Exception_GILP().  analysisRoughRules();
//        String writeOutPath="./prediction/newSearchSpace/analysis/rough-negative-1.tsv";
//        String readRulePath="./prediction/newSearchSpace/analysis/sameRules.tsv";
//        new Exception_GILP(). analysisSingleRules(true, 1, writeOutPath, readRulePath);

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

    }

    public void amieException(String originalPath,String amiePath,String finalPath) throws IOException {
        String resultNegative="./prediction/amie-exception/negativeRules-1-analysis.tsv";
        String amieNegative="./prediction/newSearchSpace/rules/negative/amieRules-1-false.tsv";
        String outpath="./prediction/amie-exception/exception/negative-exception-1.tsv";


        HashMap<Rule,Integer> negativeRules = new Exception_GILP().readEGilpRule(originalPath);

        HashMap<Rule,  String[]> negativeAnalysis= new HashMap<>();


        try {
            BufferedReader TSVReader = new BufferedReader(new FileReader(amiePath));
            String line = TSVReader.readLine();

            while (line != null) {

                String[] lineItems = line.split("\t");
                String rule = lineItems[0];

                String[] yourArray = Arrays.copyOfRange(lineItems, 1, lineItems.length);


                Rule rdfRule = new RulesOthers().transformAmieRule(rule);

                if(negativeRules.containsKey(rdfRule))
                    negativeAnalysis.put(rdfRule,yourArray);


                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }

        Writer writer1 = new OutputStreamWriter(
                new FileOutputStream(finalPath),
                StandardCharsets.UTF_8);

        for(Rule r : negativeRules.keySet()) {
            writer1.write(r.toAmieString()+ "\t");
            String[] yourArray =negativeAnalysis.get(r);
            for(String key: yourArray)
                writer1.write(key+ "\t");

            writer1.write(negativeRules.get(r)+ "\n");
         //   writer1.write("\n");

        }
        writer1.write("\n");


        writer1.close();

    }

   public void  analysisRoughRules() throws IOException {

        for(int i=1;i<=20;i++){
            String readPositivePath="./prediction/newSearchSpace/rules/positive/amieRules-"+i+"-true.tsv";
            String readNegativePath="./prediction/newSearchSpace/rules/negative/amieRules-"+i+"-false.tsv";

            ArrayList<Rule> positiveRules =  readAmieRule(readPositivePath);

            ArrayList<Rule> positiveRulesCopy =  new ArrayList<>(positiveRules) ;
            ArrayList<Rule> negativeRules = readAmieRule(readNegativePath);

            ArrayList<Rule> negativeRulesCopy =  new ArrayList<>(negativeRules) ;

            ArrayList<Rule> roughRules= new ArrayList<>(positiveRules);
            roughRules.retainAll(negativeRules);

            positiveRules.removeAll(negativeRulesCopy);

            negativeRules.removeAll(positiveRulesCopy);

            Writer writer1 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/rules/rough/roughRules-"+i+".tsv"),
                    StandardCharsets.UTF_8);
            for(Rule R : roughRules) {
                writer1.write(R.toAmieString() + "\n");
            }
            writer1.close();

            Writer writer2 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/rules/positive-distinct/positiveRules-"+i+".tsv"),
                    StandardCharsets.UTF_8);
            for(Rule R : positiveRules) {
                writer2.write(R.toAmieString() + "\n");
            }
            writer2.close();
            Writer writer3 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/rules/negative-distinct/negativeRules-"+i+".tsv"),
                    StandardCharsets.UTF_8);
            for(Rule R : negativeRules) {
                writer3.write(R.toAmieString() + "\n");
            }
            writer3.close();
        }





    }


    public void  simpleAllRules() throws IOException {

        ArrayList<Integer> countsP= new ArrayList<>();
        ArrayList<Integer> countsN= new ArrayList<>();
        ArrayList<Integer> roughS= new ArrayList<>();

        for(int i=1;i<=20;i++){
            String readPositivePath="./prediction/newSearchSpace/rules/positive/amieRules-"+i+"-true.tsv";
            String readNegativePath="./prediction/newSearchSpace/rules/negative/amieRules-"+i+"-false.tsv";

            ArrayList<Rule> positiveRules =  readAmieRule(readPositivePath);

            countsP.add(positiveRules.size());

            ArrayList<Rule> negativeRules = readAmieRule(readNegativePath);

            countsN.add(negativeRules.size());

            ArrayList<Rule> roughRules= new ArrayList<>(positiveRules);
            roughRules.retainAll(negativeRules);

            roughS.add(roughRules.size());


            Writer writer1 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/rules/simple/roughRules-"+i+".tsv"),
                    StandardCharsets.UTF_8);
            for(Rule R : roughRules) {
                writer1.write(R.toAmieString() + "\n");
            }
            writer1.close();

            Writer writer2 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/rules/simple/positiveRules-"+i+".tsv"),
                    StandardCharsets.UTF_8);
            for(Rule R : positiveRules) {
                writer2.write(R.toAmieString() + "\n");
            }
            writer2.close();
            Writer writer3 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/newSearchSpace/rules/simple/negativeRules-"+i+".tsv"),
                    StandardCharsets.UTF_8);
            for(Rule R : negativeRules) {
                writer3.write(R.toAmieString() + "\n");
            }
            writer3.close();
        }

        Writer writer3 = new OutputStreamWriter(
                new FileOutputStream("./prediction/newSearchSpace/rules/counts.tsv"),
                StandardCharsets.UTF_8);
        writer3.write("positive-----\n");
        int i=0;
        for(int R : countsP) {
            writer3.write(++i +"\t"+R+ "\n");
        }

        writer3.write("negative-----\n");
        int j=0;
        for(int R : countsN) {
            writer3.write(++j +"\t"+R+ "\n");
        }
        writer3.write("rough-----\n");
        int k=0;
        for(int R : roughS) {
            writer3.write(++k +"\t"+R+ "\n");
        }

        writer3.close();



    }

    public void analysisSingleRules(Boolean decision, int condition,String writeOutPath,String readRulePath) throws IOException {
        Writer writer2 = new OutputStreamWriter(new FileOutputStream(writeOutPath), StandardCharsets.UTF_8);

        ArrayList<Rule> rules = new Exception_GILP().readGilpRule(readRulePath);

        int i = 0;
        for (Rule r : rules) {
            Rule newRule = null;
            int oldNum = 0;
            int newNum = 0;

            if (condition != 2) {

                if (decision)//true
                    newRule = new Exception_GILP().singleValidate(r, false);
                else
                    newRule = new Exception_GILP().singleValidate(r, true);


            } else// rough negative=original-false
            {


                String originalQuery = new GetSparql().ruleToSparqlRDF(r);
                HashSet<String> original = new RDF3XEngine().getDistinctEntity(originalQuery);
                oldNum = original.size();

                newRule = new Exception_GILP().singleValidate(r, false);

            }

            String query2 = new GetSparql().ruleToSparqlRDF(newRule);
            HashSet<String> newRuleResults = new RDF3XEngine().getDistinctEntity(query2);
            if (condition != 2)
                newNum = newRuleResults.size();
            else
                newNum = oldNum - newRuleResults.size();

            writer2.write(r.toAmieString() + "\t" + newNum + "\n");
            System.out.println("number ---- " + ++i + "\t " + newNum);
        }


        writer2.close();


    }


}
