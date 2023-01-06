package gilp.GILLearn_correction;

import Exception.experiment.Utils;
import Exception.rulemining.patternmining.PatternForm1Miner;
import amie.data.KB;
import amie.mining.AMIE;
import amie.mining.assistant.MiningAssistant;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import javatools.administrative.Announce;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class test_rules {

    public static String[] equivalentProperty = {"http://dbpedia.org/ontology/nationality",
            //    "http://dbpedia.org/ontology/citizenship",
            //    "http://dbpedia.org/ontology/stateOfOrigin"
    };

    public static void main(String[] args) throws Exception {

        //   cleanFile();
     //   new test_rules().run();


        int number = 100;
        String subVariable = "subject";
        String objVariable = "object";
        int filterCondition = 0;
        int iterationTimes = 1;


        new test_rules().selectTriples(subVariable, objVariable, number, filterCondition, true);
        String posPath = "./prediction/triples/positive.tsv";
        HashSet<Triple> positiveTriple = readTriples(posPath);
        new test_rules(). extendTriplesByLevel(positiveTriple, ILPLearnSettings.DEFAULT_LEVEL, true, iterationTimes);


        //   indexFacts( "knowledgeCorrection-data/exception/positiveSearchSpace1.txt");

//      String test=  "\"2006-12-31\"^^<http://dbpedia.org/resource/Barnes,_Tyne_and_Wear>";
//String query="select * where{<http://dbpedia.org/resource/Speyer> ?a ?c.}";

//        HashSet<Triple> getTriplesBySubject = new RDF3XEngine().getTriplesInSubject("<http://dbpedia.org/resource/Speyer>");
//        for(Triple tri: getTriplesBySubject){
//            System.out.println(tri);
//            String newEntity = new GetSparql().sparqlInExtendTriple(tri.get_obj());
//            System.out.println(newEntity);
//
//        //    <http://dbpedia.org/ontology/populationTotal> "50648"^^<http://dbpedia.org/resource/Ontario_Electricity_Policy>

    }


    public static void indexFacts(String fileName) {

        int count = 0;
        try {
            BufferedReader factReader = new BufferedReader(new FileReader(fileName));
            String line;//<Love_Is_Colder_Than_Death_(film)>	<hasImdb>	<0064588> ---rdf triple
            int i = 0;
            while ((line = factReader.readLine()) != null) {
                line = line.substring(1, line.length() - 1);
                System.out.println(++i + "\t" + line);
                String[] parts = line.split(">\t<");
                indexFact(parts);
                count++;

            }

            factReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void indexFact(String[] parts) {

        if (!parts[1].equals("type")) {
            String xpy = parts[0] + "\t" + parts[1] + "\t" + parts[2];
            System.out.println("instance:" + xpy);
        }
    }


    private static void cleanFile() {
        String posPath = "./prediction/triples/positive.tsv";
        String negPath = "./prediction/triples/negative.tsv";

        File tempFile = new File(posPath);
        File tempFile2 = new File(negPath);

        try {
            if (tempFile.exists()) FileUtils.forceDelete(new File(posPath));
            if (tempFile2.exists()) FileUtils.forceDelete(new File(negPath));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String[] parameters(Boolean decision, int iterationTimes) {//amie+ --parameters
        //  miner =	AMIE.getInstance(new String[] { ontology, "-pm", "support", "-mins", "0" });
        if (decision)
            return new String[]{"./prediction/triples/positiveSearchSpace" + iterationTimes + ".tsv",
                    "-mins", "1", "-minis", "1",
                    "-maxad", "3",
                    "-bexr", array2String(equivalentProperty),
                    "-htr", array2String(equivalentProperty),
                    "-minhc", "0", "-minpca", "0",
                    "-dpr", "-optimfh",
                    // "-const"
            };

        else
            return new String[]{"./prediction/triples/negativeSearchSpace" + iterationTimes + ".tsv",
                    "-maxad", "3",
                    "-mins", "1", //String.valueOf(ILPLearnSettings.initialNumberNeg),
                    "-minis", "1", //String.valueOf(ILPLearnSettings.initialNumberNeg),
                    "-bexr", array2String(equivalentProperty),
                    "-htr", array2String(equivalentProperty),
                    "-minhc", "0", "-minpca", "0",
                    "-dpr", "-optimfh",
                    //     "-const"
            };

    }

    private HashSet<Triple> filterTriples(String query, String subVariable, String objVariable, String relation, int filterCondition) {
        //i=1;contain same objects(more); others--different objects(less)
        ArrayList<HashMap<String, String>> pairs = new Sparql().getResultsFromQuery(query);
        MultiValueMap newPairs = map2MultiMap(pairs, subVariable, objVariable);
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

    public void selectTriples(String subVariable, String objVariable, int number, int filterCondition, Boolean decision) {
        String fileName = null;
        for (String name : equivalentProperty) {
            String query = null;
            if (decision) {
                fileName = "./prediction/triples/positive.tsv";
                query = "select distinct ?subject ?object where{ ?subject <" + name + "> ?object." +
                        " ?subject rdf:type <" + Property.DOMAIN + ">."//   + "?object rdf:type <" + Property.RANGE + ">."
                        + "?object rdf:type <" + Property.rangePropertyInwikiData + ">."
                        + "} ORDER BY RAND() limit " + number;
                System.out.println("positive query:" + query);

            } else {
                fileName = "./prediction/triples/negative.tsv";
                query = "select distinct ?subject ?object where {"
                        + " ?subject <" + name + "> ?object."
                        + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                        + "?object rdf:type <" + Property.rangeNegativePropertyInwikiData + ">."
                        //  + "FILTER NOT EXISTS { ?object  rdf:type <" + Property.rangePropertyInwikiData + ">} } "
                        + "} ORDER BY RAND() limit " + number;
                System.out.println("negative query:" + query);

            }

            HashSet<Triple> triples = filterTriples(query, subVariable, objVariable, name, filterCondition);
            System.out.println(name + decision + "---query--size:" + triples.size());

            try {

                FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8, true);
                for (Triple tri : triples) {
                    writer.write(tri + "\n");
                }
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void run() throws Exception {

        int number = 100;
        String subVariable = "subject";
        String objVariable = "object";
        int filterCondition = 0;
        int iterationTimes = 1;


        new test_rules().selectTriples(subVariable, objVariable, number, filterCondition, true);
        String posPath = "./prediction/triples/positive.tsv";
        HashSet<Triple> positiveTriple = readTriples(posPath);
        extendTriplesByLevel(positiveTriple, ILPLearnSettings.DEFAULT_LEVEL, true, iterationTimes);
        //extendTriplesFromObjects

           new test_rules().selectTriples(subVariable, objVariable, number, filterCondition, false);
        String negPath = "./prediction/triples/negative.tsv";
        HashSet<Triple> negativeTriple = readTriples(negPath);

        extendTriplesByLevel(negativeTriple, ILPLearnSettings.DEFAULT_LEVEL, false, iterationTimes);
        //extendTriplesFromObjects

        pruneSearchSpace(iterationTimes);//focus on here.

        System.out.println("\n------- finish pruned search space-----------------\n");


        //  HashMap<amie.rules.Rule, Double> positiveRule = amieRules(true, iterationTimes);
        //  HashMap<amie.rules.Rule, Double> negativeRule = amieRules(false, iterationTimes);
        ArrayList<Rule> positiveRule = filterAmieRules(true, iterationTimes);
        ArrayList<Rule> negativeRule = filterAmieRules(false, iterationTimes);


        System.out.println("\n------- negative rule-----------------\n");
//        Writer writer1 = new OutputStreamWriter(
//                new FileOutputStream("./prediction/triples/amieRules--negative.tsv"),
//                StandardCharsets.UTF_8);
//        int i = 0;
//        for (amie.rules.Rule r : negativeRule.keySet())
//            if(!positiveRule.containsKey(r))
//            writer1.write(i++ + replace(r.toString()) +"\t"+ negativeRule.get(r) + "\n");
//        writer1.close();

    }

    private void filterExceptionRules(Boolean decision, int iterationTimes) {
        String trainFileName = null;
        if (decision)
            trainFileName = "./prediction/triples/positiveSearchSpace" + iterationTimes + ".tsv";
        else
            trainFileName = "./prediction/triples/negativeSearchSpace" + iterationTimes + ".tsv";
        // String trainFileName = commandLine.getOptionValue("learn");
        PatternForm1Miner.minePatterns(trainFileName);


    }

    public ArrayList<Rule> filterAmieRules(Boolean decision, int iterationTimes) {

        ArrayList<Rule> datalogRules = new ArrayList<>();

        try {
            Announce.doing("\n Starting the mining phase \n");
            AMIE miner = AMIE.getInstance(parameters(decision, iterationTimes));
            List<amie.rules.Rule> amieRules = miner.mine();

            Announce.done("\n finish the mining phase \n");
            Announce.close();

            //------------------------

            HashMap<amie.rules.Rule, Double> filterRulesSort = new HashMap<amie.rules.Rule, Double>();
            HashMap<amie.rules.Rule, Double> filterRules = new HashMap<amie.rules.Rule, Double>();
            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("./prediction/triples/gilpRules-" + iterationTimes + "-" + decision + ".tsv"),
                    StandardCharsets.UTF_8);
            Writer writer1 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/triples/amieRules-" + iterationTimes + "-" + decision + ".tsv"),
                    StandardCharsets.UTF_8);

            MiningAssistant assistant = miner.getAssistant();
            if (!amieRules.isEmpty()) {
                for (amie.rules.Rule rule : amieRules) {

                    filterRules.put(rule, rule.getPcaConfidence());
                }
            }

            filterRulesSort = new RuleLearnerHelper().reverseOrderByValue(filterRules);
            ArrayList<amie.rules.Rule> finalRules = new ArrayList<>(filterRulesSort.keySet());

            for (amie.rules.Rule rule : finalRules) {

                Rule rdfRule = new Rule();
                if (!rule.isEmpty()) {
                    Clause clause = new Clause();
                    for (int[] bs : rule.getBody()) {
                        clause.addPredicate(extractComplexRDFPredicate(bs));
                    }
                    rdfRule.set_body(clause);
                    RDFPredicate head = extractRDFPredicate(rule.getHead());
                    rdfRule.set_head(head);
                }

                datalogRules.add(rdfRule);
                writer1.write(rule + "\t" + filterRules.get(rule) + "\n");
                writer.write(rdfRule + "\t" + filterRules.get(rule) + "\n");
            }

            writer.close();
            writer1.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return datalogRules;
    }

    public HashMap<amie.rules.Rule, Double> amieRules(Boolean decision, int iterationTimes) {

        ArrayList<amie.rules.Rule> datalogRules = new ArrayList<>();

        ArrayList<Rule> newRules = new ArrayList<>();
        HashMap<amie.rules.Rule, Double> filterRulesSort = new HashMap<>();
        try {
            Announce.doing("\n Starting the mining phase \n");
            AMIE miner = AMIE.getInstance(parameters(decision, iterationTimes));
            List<amie.rules.Rule> amieRules = miner.mine();

            Announce.done("\n finish the mining phase \n");
            Announce.close();

            //------------------------


            HashMap<amie.rules.Rule, Double> filterRules = new HashMap<>();

            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("./prediction/triples/amieRules-" + iterationTimes + "-" + decision + ".tsv"),
                    StandardCharsets.UTF_8);

            Writer writer1 = new OutputStreamWriter(
                    new FileOutputStream("./prediction/triples/gilpRules-" + iterationTimes + "-" + decision + ".tsv"),
                    StandardCharsets.UTF_8);

            if (!amieRules.isEmpty())
                for (amie.rules.Rule amierule : amieRules) {
                    writer.write(simpleReplace(amierule.toString()) + "\t" + amierule.getPcaConfidence() + "\n");
                    filterRules.put(amierule, amierule.getPcaConfidence());
                    Rule rule = transformRule(amierule);
                    //      newRules.add(rule);
                    //         }

                    filterRulesSort = new RuleLearnerHelper().reverseOrderByValue(filterRules);
                    //    datalogRules.addAll(filterRulesSort.keySet());

                    //     for (Rule rule : newRules){

                    String sparql = new GetSparql().ruleToSparql(rule);
                    HashSet<String> subjects1 = new RDF3XEngine().getDistinctEntity(sparql);

                    RDFPredicate exception = new RDFPredicate();
                    exception.setPredicateName("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                    exception.setSubject(rule.get_head().getObject());
                    if (decision)
                        exception.setObject(Property.rangePropertyInwikiData);
                    else
                        exception.setObject(Property.rangeNegativePropertyInwikiData);

                    Clause body = rule.get_body();
                    //    body.removePredicate(exception);
                    body.addPredicate(exception);
                    Rule newRule = new Rule();
                    newRule.set_head(rule.get_head());
                    newRule.set_body(body);
                    String sparql2 = new GetSparql().ruleToSparql(newRule);
                    HashSet<String> subjects2 = new RDF3XEngine().getDistinctEntity(sparql2);

                    double precision = (double) subjects2.size() / subjects1.size();


                    if (precision == 1.00)
                        writer1.write(rule + "\t" + amierule.getPcaConfidence() + "\t" + precision + "\n");
                    else
                        writer1.write(newRule + "\t" + amierule.getPcaConfidence() + "\t" + precision + "\n");

                }

            //   writer1.write(replace(rule.toString()) + "\t" + filterRules.get(rule) + "\n");

            writer1.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filterRulesSort;
    }


    public Rule transformRule(amie.rules.Rule rule) {
        Rule rdfRule = new Rule();
        if (!rule.isEmpty()) {
            Clause clause = new Clause();
            for (int[] bs : rule.getBody()) {
                clause.addPredicate(extractComplexRDFPredicate(bs));
            }
            rdfRule.set_body(clause);
            RDFPredicate head = extractRDFPredicate(rule.getHead());
            rdfRule.set_head(head);
        }

        return rdfRule;
    }

    public Rule addFeature2Rule(amie.rules.Rule rule, Boolean decision) {
        Rule rdfRule = new Rule();
        if (!rule.isEmpty()) {

            RDFPredicate head = extractRDFPredicate(rule.getHead());
            rdfRule.set_head(head);
            String object = rdfRule.get_head().getObject();
            Clause clause = new Clause();
            for (int[] bs : rule.getBody()) {
                clause.addPredicate(extractComplexRDFPredicate(bs));
            }


            RDFPredicate exception = new RDFPredicate();
            exception.setPredicateName("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            exception.setSubject(object);
            if (decision)
                exception.setObject("http://www.wikidata.org/entity/Q6256");
            else
                exception.setObject("http://www.wikidata.org/entity/Q41710");


            clause.addPredicate(exception);
            rdfRule.set_body(clause);

        }

        return rdfRule;
    }

    public Rule addFeature2Rule2(amie.rules.Rule rule, Boolean decision) {
        Rule rdfRule = new Rule();
        if (!rule.isEmpty()) {

            RDFPredicate head = extractRDFPredicate(rule.getHead());
            rdfRule.set_head(head);
            String object = rdfRule.get_head().getObject();
            Clause clause = new Clause();
            for (int[] bs : rule.getBody()) {
                clause.addPredicate(extractComplexRDFPredicate(bs));
            }


            RDFPredicate exception = new RDFPredicate();
            exception.setPredicateName("not_" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            exception.setSubject(object);
            if (!decision)
                exception.setObject("http://www.wikidata.org/entity/Q6256");
            else
                exception.setObject("http://www.wikidata.org/entity/Q41710");


            clause.addPredicate(exception);
            rdfRule.set_body(clause);

        }

        return rdfRule;
    }


    public void extendTriplesByLevel
            (HashSet<Triple> filterTriples, int level, Boolean decision, int iterationTimes) throws IOException {

        String path = null;
        if (decision)
            path = "./prediction/newSearchSpace/positiveSearchSpace" + iterationTimes + "-old.tsv";
        else
            path = "./prediction/newSearchSpace/negativeSearchSpace" + iterationTimes + "-old.tsv";
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        HashSet<Triple> firstLevelTriples = new HashSet<>();
        HashSet<String> extractSubject = new HashSet<>();
        for (Triple triple : filterTriples) {
            extractSubject.add(triple.get_subject());
        }
        for (String subject : extractSubject) {
            String newEntity = new GetSparql().sparqlInExtendTriple(subject);

            HashSet<Triple> getTriplesBySubject = new HashSet<>();

            if (ILPLearnSettings.condition == 1)
                getTriplesBySubject = new RDF3XEngine().getTriplesInSubject(newEntity);
            else
                getTriplesBySubject = extendAllTriplesBySubject(subject);

            for (Triple key : getTriplesBySubject) {
                writer.write(key + "\n");
            }

            HashSet<Triple> changedTriples = new HashSet<>(getTriplesBySubject);
            firstLevelTriples.addAll(changedTriples);
        }

        HashSet<Triple> otherLevelTriples = new HashSet<>(firstLevelTriples);

        for (int i = 1; i <= level - 1; i++) {

            HashSet<Triple> eachLevelTriples = new HashSet<>();
            HashSet<String> allObjects = new HashSet<>();

            for (Triple tri : otherLevelTriples) {
                allObjects.add(tri.get_obj());
            }
            for (String obj : allObjects) {
                String newEntity = new GetSparql().sparqlInExtendTriple(obj);
                System.out.println(newEntity);
                HashSet<Triple> newLevelTriples = new HashSet<>();

                if (ILPLearnSettings.condition == 1)
                    newLevelTriples = new RDF3XEngine().getTriplesInSubject(newEntity);
                else
                    newLevelTriples = extendAllTriplesBySubject(obj);

                if (newLevelTriples != null) {
                    for (Triple key : newLevelTriples)
                        writer.write(key + "\n");
                    eachLevelTriples.addAll(newLevelTriples);
                }
            }
            if (eachLevelTriples.isEmpty())
                break;
            otherLevelTriples.clear();
            otherLevelTriples.addAll(eachLevelTriples);
        }
        writer.close();
    }

    private void extendTriplesFromObjects
            (HashSet<Triple> filterTriples, int level, Boolean decision, int iterationTimes) throws IOException {

        String path = null;
        if (decision)
            path = "./prediction/triples/positiveSearchSpace" + iterationTimes + "-old.tsv";
        else
            path = "./prediction/triples/negativeSearchSpace" + iterationTimes + "-old.tsv";
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        HashSet<Triple> firstLevelTriples = new HashSet<>();
        HashSet<String> extractObject = new HashSet<>();
        for (Triple triple : filterTriples) {
            extractObject.add(triple.get_obj());
        }
        for (String object : extractObject) {
            String newEntity = new GetSparql().sparqlInExtendTriple(object);
            System.out.println(newEntity);

            HashSet<Triple> getTriplesByObject = new HashSet<>();

            if (ILPLearnSettings.condition == 1)
                getTriplesByObject = new RDF3XEngine().getTriplesByObject(newEntity.substring(1, newEntity.length() - 1));
            else
                getTriplesByObject = extendAllTriplesByObject(object);

            for (Triple key : getTriplesByObject) {
                writer.write(key + "\n");
            }

            HashSet<Triple> changedTriples = new HashSet<>(getTriplesByObject);
            firstLevelTriples.addAll(changedTriples);
        }

        HashSet<Triple> otherLevelTriples = new HashSet<>(firstLevelTriples);

        for (int i = 1; i <= level - 1; i++) {

            HashSet<Triple> eachLevelTriples = new HashSet<>();
            HashSet<String> allSubjects = new HashSet<>();

            for (Triple tri : otherLevelTriples) {
                allSubjects.add(tri.get_subject());
            }
            for (String subject : allSubjects) {
                String newEntity = new GetSparql().sparqlInExtendTriple(subject);
                //       System.out.println(newEntity);
                HashSet<Triple> newLevelTriples = new HashSet<>();

                if (ILPLearnSettings.condition == 1)
                    newLevelTriples = new RDF3XEngine().getTriplesByObject(newEntity.substring(1, newEntity.length() - 1));
                else
                    newLevelTriples = extendAllTriplesByObject(subject);

                if (newLevelTriples != null) {
                    for (Triple key : newLevelTriples)
                        writer.write(key + "\n");
                    eachLevelTriples.addAll(newLevelTriples);
                }
            }
            if (eachLevelTriples.isEmpty())
                break;
            otherLevelTriples.clear();
            otherLevelTriples.addAll(eachLevelTriples);
        }
        writer.close();
    }

    private HashSet<Triple> extendAllTriplesByObject(String object) {
        // not only consider the contents in the dbpedia--from <http://dbpedia.org>
        // how to limit the predicate and target to do the search space.
        //here, there are 4 conditions.

        String newEntity = new GetSparql().sparqlInExtendTriple(object);

        String query1 = "select * from <http://dbpedia.org> " + "where{ ?target ?predicate " + newEntity
                + ". " + "FILTER("
                + "regex(str(?target ),\"http://dbpedia.org/resource\") "

                + "&& !regex(str(?predicate),\"wiki\") "
                + "&& regex(str(?predicate ),\"http://dbpedia.org/ontology\") "
                + "&& ?predicate NOT IN (<http://dbpedia.org/ontology/abstract>, <http://dbpedia.org/ontology/deathDate>,"
                + "<http://dbpedia.org/ontology/birthDate> ,<http://dbpedia.org/ontology/wikiPageExternalLink>, "
                + "<http://dbpedia.org/ontology/wikiPageWikiLink>)"
                + ")}";
        System.out.println(query1);

        ArrayList<HashMap<String, String>> extendNegativeEntities = new Sparql().getResultsFromQuery(query1);
        HashSet<String> samekey = new HashSet<>();
        HashSet<Triple> triplesExtends = new HashSet<>();
        if (extendNegativeEntities != null) {
            for (HashMap<String, String> key : extendNegativeEntities) {
                String predicate = key.get("predicate");
                String subject = key.get("target");

                if (!samekey.contains(object)) {
                    samekey.add(object);
                    Triple negativeElement = new Triple(subject, predicate, object);
                    triplesExtends.add(negativeElement);
                }
            }
        }
        return triplesExtends;
    }


    private HashSet<Triple> extendAllTriplesBySubject(String subject) {
        // not only consider the contents in the dbpedia--from <http://dbpedia.org>
        // how to limit the predicate and target to do the search space.
        //here, there are 4 conditions.

        String newEntity = new GetSparql().sparqlInExtendTriple(subject);

        String query1 = "select * from <http://dbpedia.org> " + "where{ " + newEntity
                + " ?predicate ?target. " + "FILTER("
                + "regex(str(?target ),\"http://dbpedia.org/resource\") "

                + "&& !regex(str(?predicate),\"wiki\") "
                + "&& regex(str(?predicate ),\"http://dbpedia.org/ontology\") "
                + "&& ?predicate NOT IN (<http://dbpedia.org/ontology/abstract>, <http://dbpedia.org/ontology/deathDate>,"
                + "<http://dbpedia.org/ontology/birthDate> ,<http://dbpedia.org/ontology/wikiPageExternalLink>, "
                + "<http://dbpedia.org/ontology/wikiPageWikiLink>)"
                + ")}";
        System.out.println(query1);

        ArrayList<HashMap<String, String>> extendNegativeEntities = new Sparql().getResultsFromQuery(query1);
        HashSet<String> samekey = new HashSet<>();
        HashSet<Triple> triplesExtends = new HashSet<>();
        if (extendNegativeEntities != null) {
            for (HashMap<String, String> key : extendNegativeEntities) {
                String predicate = key.get("predicate");
                String object = key.get("target");

                if (!samekey.contains(object)) {
                    samekey.add(object);

                    Triple negativeElement = new Triple(subject, predicate, object);

                    triplesExtends.add(negativeElement);

                }

            }
        }
        return triplesExtends;
    }


    //------------------------------------------------------------------------------------------------------------------
    public static HashSet<Triple> readTriples(String path) {

        HashSet<Triple> triples = new HashSet<>();

        try (BufferedReader TSVReader = new BufferedReader(new FileReader(path))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t");
                Triple triple = new Triple(lineItems[0], lineItems[1], lineItems[2]);
                triples.add(triple);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return triples;
    }

    public void pruneSearchSpace(int iterationTimes) {

        String pathPos = "./prediction/triples/positiveSearchSpace" + iterationTimes + "-old.tsv";
        String pathNeg = "./prediction/triples/negativeSearchSpace" + iterationTimes + "-old.tsv";

        HashSet<Triple> positiveSearch = readTriples(pathPos);
        HashSet<Triple> negativeSearch = readTriples(pathNeg);
        HashSet<Triple> positiveSearchCopy = new HashSet<>(positiveSearch);
        HashSet<Triple> negativeSearchCopy = new HashSet<>(negativeSearch);
        positiveSearch.removeAll(negativeSearchCopy);
        negativeSearch.removeAll(positiveSearchCopy);
        HashSet<Triple> allSearchSpace = new HashSet<>(negativeSearchCopy);
        allSearchSpace.addAll(positiveSearchCopy);
        writePrunedSearchSpace(positiveSearch, true, iterationTimes);
        writePrunedSearchSpace(negativeSearch, false, iterationTimes);

        writeAllSearchSpace(allSearchSpace, iterationTimes);
    }

    private void writePrunedSearchSpace(HashSet<Triple> expandedTriples, Boolean decision, int iterationTimes) {
        String path = null;
        if (decision)
            path = "./prediction/triples/positiveSearchSpace" + iterationTimes + ".tsv";
        else
            path = "./prediction/triples/negativeSearchSpace" + iterationTimes + ".tsv";

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            for (Triple t : expandedTriples) {
                if (!Objects.equals(t.get_predicate(), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                    writer.write(("<" + substitute(t.get_subject()) + ">\t"));
                    writer.write(("<" + substitute(t.get_predicate()) + ">\t"));
                    writer.write(("<" + substitute(t.get_obj()) + ">\n"));
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void writeAllSearchSpace(HashSet<Triple> expandedTriples, int iterationTimes) {

        try {
            String path = "./prediction/triples/allSearchSpace" + iterationTimes + ".tsv";

            Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            for (Triple t : expandedTriples) {
                writer.write(("<" + substitute(t.get_subject()) + ">\t"));
                writer.write(("<" + substitute(t.get_predicate()) + ">\t"));
                writer.write(("<" + substitute(t.get_obj()) + ">\n"));
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String substitute(String str) {
        str = str.replace("-", "@@").replace("–", "##");
        return str;
    }

    private String simpleOtherReplace(String str) {
        str = str.replaceAll("<", "").replaceAll(">", "");
        return str;
    }

    private String simpleReplace(String str) {
        str = str.replaceAll("<", "").replaceAll(">", "")
                .replaceAll("@@", "-").replaceAll("##", "–");
        return str;
    }

    private String replace(String str) {
        str = str.replaceAll("@@", "-").replaceAll("##", "–");
        return str;
    }


    private RDFPredicate extractRDFPredicate(int[] byteStrings) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName(simpleOtherReplace(KB.unmap(byteStrings[1]).replace("<", "").replace(">", "")));
        rdfPredicate.setSubject(simpleOtherReplace(KB.unmap(byteStrings[0])));
        rdfPredicate.setObject(simpleOtherReplace(KB.unmap(byteStrings[2])));
        return rdfPredicate;
    }

    private RDFPredicate extractComplexRDFPredicate(int[] bs) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName(simpleReplace(KB.unmap(bs[1]).replace("<", "").replace(">", "")));
        rdfPredicate.setSubject(simpleReplace(KB.unmap(bs[0])));
        rdfPredicate.setObject(simpleReplace(KB.unmap(bs[2])));
        return rdfPredicate;
    }

    private MultiValueMap map2MultiMap(ArrayList<HashMap<String, String>> pairs, String subVariable, String objVariable) {
        MultiValueMap newPairs = new MultiValueMap();
        for (HashMap<String, String> key : pairs) {
            String subject = key.get(subVariable);
            String object = key.get(objVariable);
            newPairs.put(object, subject);
        }
        return newPairs;
    }

    private String getRandomElement(List<String> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    public String array2String(String[] arrays) {
        StringBuilder targetName = new StringBuilder();
        for (String name : arrays)
            targetName.append("<").append(name).append(">").append(",");
        targetName.deleteCharAt(targetName.length() - 1);
        return targetName.toString();
    }
}
