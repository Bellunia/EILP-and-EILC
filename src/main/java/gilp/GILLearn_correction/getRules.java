package gilp.GILLearn_correction;

import amie.mining.AMIE;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import gilp.utils.KVPair;
import javatools.administrative.Announce;
import javatools.parsers.NumberFormatter;
import gilp.measure.Evaluation;
import gilp.measure.Values;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class getRules {

    public KVPair<ArrayList<Rule>, ArrayList<Rule>> filterIterativeRules(int number) throws Exception {
        int iterationTimes = 0;
        KVPair<HashSet<Triple>, HashSet<Triple>> initialTriples = new FilterPediaTriples().initialFeedback(number);
        HashSet<Triple> positiveTriple = initialTriples.getKey();
        HashSet<Triple> negativeTriple = initialTriples.getValue();

        HashSet<String> allPrediction = new HashSet<>();
        while (true) {

            System.out.println("\n------- Iteration:" + iterationTimes + "-----------------\n");
//--------------------------------------------
            // KVPair<HashSet<Triple>, HashSet<Triple>> spaceTriples =
            new getSearchSpace().filterSearchSpace(positiveTriple, negativeTriple, iterationTimes);
            //HashSet<Triple> expandedPositiveTriples = spaceTriples.getKey();
            //   HashSet<Triple> expandedNegativeTriples = spaceTriples.getValue();

            System.out.println("\n------- finish extending search space-----------------\n");
            ArrayList<Rule> positiveRules = getAmieRules(true, iterationTimes);
            ArrayList<Rule> negativeRules = getAmieRules(false, iterationTimes);
//----------------------------------------
            HashSet<String> positivePrediction = filterRulesInDBPedia(positiveTriple, negativeTriple, true,
                    iterationTimes, positiveRules, number);
            HashSet<String> negativePrediction = filterRulesInDBPedia(positiveTriple, negativeTriple, false,
                    iterationTimes, negativeRules, number);


//            HashSet<String> positivePrediction = filterRulesInEvaluation(positiveRules, true,
//                    number);
//            HashSet<String> negativePrediction = filterRulesInEvaluation(negativeRules, false,
//                    number);

            System.out.println("\n------- finish filter rules-----------------\n");


            HashSet<Triple> newPositiveTriples = new HashSet<>();

            HashSet<Triple> newNegativeTriples = new HashSet<>();

            if (Property.ruleLabel) {

                HashSet<String> subjectsPos = new RulesOthers().triplesToSub(positiveTriple);
                HashSet<String> subjectsNeg = new RulesOthers().triplesToSub(negativeTriple);
                positivePrediction.removeAll(subjectsPos);
                negativePrediction.removeAll(subjectsNeg);

                KVPair<HashSet<Triple>, HashSet<Triple>> newTriples =
                        new RulesOthers().subjectsToTriples(positivePrediction, negativePrediction);
                newPositiveTriples = newTriples.getKey();
                newNegativeTriples = newTriples.getValue();

            } else {

                HashSet<String> objectsPos = new RulesOthers().triplesToObj(positiveTriple);
                HashSet<String> objectsNeg = new RulesOthers().triplesToObj(negativeTriple);
                positivePrediction.removeAll(objectsPos);
                negativePrediction.removeAll(objectsNeg);

                KVPair<HashSet<Triple>, HashSet<Triple>> newTriples =
                        new RulesOthers().objectsToTriples(positivePrediction, negativePrediction);
                newPositiveTriples = newTriples.getKey();
                newNegativeTriples = newTriples.getValue();
            }

            HashSet<String> predictionsEachLoop = new HashSet<String>();
            predictionsEachLoop.addAll(positivePrediction);
            predictionsEachLoop.addAll(negativePrediction);


            if (predictionsEachLoop.size() != allPrediction.size()) {

                positiveTriple.addAll(newPositiveTriples);
                negativeTriple.addAll(newNegativeTriples);

                allPrediction.addAll(predictionsEachLoop);


            } else {

                // rule to correct the negative rules

                HashSet<HashMap<Triple, Triple>> correctPairs = new CorrectionInDBPedia().correctTriple(negativeTriple, Property.RANGE);
                for (HashMap<Triple, Triple> element : correctPairs) {

                    try {
                        File file = new File("/home/wy/Desktop/test-correction/correction--" + number + ".txt");
                        FileWriter fileReader = new FileWriter(file); // A stream that connects to the text file
                        BufferedWriter bufferedWriter = new BufferedWriter(fileReader); // Connect the FileWriter to the BufferedWriter
                        int j = 0;
                        for (Triple s : element.keySet()) {
                            bufferedWriter.write(++j + "\t" + s + "\t" + element.get(s) + "\n");
                        }

                        bufferedWriter.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                System.out.println("\n Last allPrediction: " + allPrediction.size());
                return new KVPair<ArrayList<Rule>, ArrayList<Rule>>(positiveRules, negativeRules);

            }
        }

    }


    public HashSet<String> filterRulesInEvaluation(ArrayList<Rule> positiveRules, boolean decision, Integer iterationTimes
    ) throws Exception {
        HashSet<String> countriesInwiki = RuleLearnerHelper.readTypes("/home/wy/Desktop/experiment-files/nationality-wikidata.txt");
        HashSet<String> countriesInDbpedia = RuleLearnerHelper.readTypes("/home/wy/Desktop/experiment-files/nation-country-dbpedia.txt");


        int i = 1;

        HashSet<String> allPositiveObjects = new HashSet<String>();
        HashSet<String> allNegativeObjects = new HashSet<String>();
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("/home/wy/Desktop/test-correction/ILP_"
                        + "_" + iterationTimes + "_" + decision + ".tsv"),
                Charset.forName("UTF-8"));

        writer.write("id\t" + "rule\t" + "size\t" + "precision:\t"
                + "lowerBoundInAllFeedback:\t" + "upperBoundInAllFeedback:\t" + "interval:\t" + "\n");

        for (Rule rule : positiveRules) {
            HashSet<String> ruleObjects = new RulesOthers().ruleToObjects(rule);
            HashMap<String, Double> objectsDecision = new HashMap<>();

            double tp = 0;
            double fp = 0;
            double tn = 0;
            double fn = 0;

            if (ruleObjects != null) {
                for (String object : ruleObjects) {
                    if (!objectsDecision.containsKey(object)) {

                        if (countriesInDbpedia.contains(object)) {

                            if (countriesInwiki.contains(object)) {
                                objectsDecision.put(object, 1.0);
                                tp = tp + 1;
                                allPositiveObjects.add(object);

                            } else {

                                objectsDecision.put(object, 0.5);
                                fp = fp + 1;
                                allNegativeObjects.add(object);
                            }

                        } else {
                            if (countriesInwiki.contains(object)) {//|| generateFeedback2(object)generateFeedback(object, property)
                                objectsDecision.put(object, -0.5);
                                fn = fn + 1;
                                allPositiveObjects.add(object);

                            } else {
                                objectsDecision.put(object, -1.0);
                                tn = tn + 1;
                                allNegativeObjects.add(object);
                            }

                        }

                    } else {
                        double num = objectsDecision.get(object);

                        if (num == 1) {
                            tp = tp + 1;
                            allPositiveObjects.add(object);
                        } else if (num == 0.5) {
                            fp = fp + 1;
                            allNegativeObjects.add(object);
                        } else if (num == -0.5) {
                            fn = fn + 1;

                            allPositiveObjects.add(object);
                        } else {
                            tn = tn + 1;
                            allNegativeObjects.add(object);
                        }
                    }
                }
            }
            double accuracy = (tp + tn) / (tp + tn + fp + fn);
            double Precision = tp / (tp + fp);
            double Recall = tp / (tp + fn);
            double f_score = 2 * (Recall * Precision) / (Recall + Precision);
            System.out.println("accuracy " + accuracy);
            System.out.println("Precision " + Precision);
            System.out.println("Recall " + Recall);
            System.out.println("f_score " + f_score);

            writer.write("different objects: " + ruleObjects.size() + "\n");
            writer.write(i++ + "\t" + rule + "\t" + ruleObjects.size() + "\t");
            writer.write(accuracy + "\t");
            writer.write(Precision + "\t");
            writer.write(Recall + "\t");
            writer.write(f_score + "\n");
        }
        writer.write("ruleSubjectsAll.size:\t" + allPositiveObjects.size() + allNegativeObjects + "\n");
        writer.close();
        System.out.println("\n " + iterationTimes + " iteration--finish  " + decision + " analysis of rules in "
        );
        if (decision)
            return allPositiveObjects;
        else
            return allNegativeObjects;
    }


    public KVPair<HashSet<Triple>, HashSet<Triple>> filterPrediction(int number) throws Exception {

        KVPair<HashSet<Triple>, HashSet<Triple>> initialTriples = new FilterPediaTriples().initialFeedback(number);
        HashSet<Triple> positiveTriple = initialTriples.getKey();
        HashSet<Triple> negativeTriple = initialTriples.getValue();

        int iterationTimes = 0;

        HashSet<Triple> allPrediction = new HashSet<Triple>();
        while (true) {

            iterationTimes++;
            long time = System.currentTimeMillis();
            System.out.println("\n------- Iteration:" + iterationTimes + "-----------------\n");

            KVPair<HashSet<Triple>, HashSet<Triple>> predictions = getOneTimePrediction(positiveTriple, negativeTriple, iterationTimes);

            HashSet<Triple> positivePrediction = predictions.getKey();
            HashSet<Triple> negativePrediction = predictions.getValue();

            HashSet<Triple> predictionsEachLoop = new HashSet<>(positivePrediction);

            predictionsEachLoop.addAll(negativePrediction);

            System.out.println("\n test-predictionsEachLoop: " + predictionsEachLoop.size());
            System.out.println("\n test-allPrediction: " + allPrediction.size());

            if (!(predictionsEachLoop.containsAll(allPrediction) && allPrediction.containsAll(predictionsEachLoop))) {

                System.out.println("\n test-----!!!! ");
                positivePrediction.removeAll(positiveTriple);// extended positive triples.
                negativePrediction.removeAll(negativeTriple);// extended negative triples.
                HashSet<Triple> filterPositiveTriple = new RulesOthers().filterTripleByType(positivePrediction, true);
                HashSet<Triple> filterNegativeTriple = new RulesOthers().filterTripleByType(positivePrediction, false);
                //filterTripleByRangeResults
                //   HashSet<Triple> filterPositiveTriple = new RulesOthers().filterTripleByRangeResults(positivePrediction, true);
                //   HashSet<Triple> filterNegativeTriple = new RulesOthers().filterTripleByRangeResults(positivePrediction, false);

                positiveTriple.addAll(filterPositiveTriple);
                negativeTriple.addAll(filterNegativeTriple);
                allPrediction.clear();
                allPrediction.addAll(predictionsEachLoop);
                predictionsEachLoop.clear();

            } else {

                System.out.println("\n Last allPrediction finish! ");

                return new KVPair<>(positiveTriple, negativeTriple);
            }

            long miningTime = System.currentTimeMillis() - time;
            System.out.println("finish " + iterationTimes + " iteration time in " + NumberFormatter.formatMS(miningTime));

            number = number + ILPLearnSettings.intervalNumber;
        }


    }

    public KVPair<HashSet<Triple>, HashSet<Triple>> getOneTimePrediction(HashSet<Triple> positiveTriple, HashSet<Triple> negativeTriple, int iterationTimes) throws Exception {
        // KVPair<HashSet<Triple>, HashSet<Triple>> spaceTriples =
        new getSearchSpace().filterSearchSpace(positiveTriple, negativeTriple, iterationTimes);

        ArrayList<Rule> positiveRules = getAmieRules(true, iterationTimes);
        ArrayList<Rule> negativeRules = getAmieRules(false, iterationTimes);

        HashSet<Triple> positivePrediction = filterRulesInEvaluation(positiveTriple, negativeTriple, true,
                positiveRules, iterationTimes);
        HashSet<Triple> negativePrediction = filterRulesInEvaluation(positiveTriple, negativeTriple, false,
                negativeRules, iterationTimes);

        return new KVPair<>(positivePrediction, negativePrediction);
    }

    public ArrayList<Rule> getAmieRules(Boolean decision, int iterationTimes) throws Exception {

        ArrayList<Rule> datalogRules = new ArrayList<>();
        AMIE miner = AMIE.getInstance(new ILPLearnSettings().parameters(decision, iterationTimes));
        Announce.doing("\n Starting the mining phase \n");
        List<amie.rules.Rule> rules = miner.mine();
        Announce.done("\n finish the mining phase \n");
        Announce.close();

        HashMap<amie.rules.Rule, Double> filterRulesSort = new HashMap<amie.rules.Rule, Double>();
        HashMap<amie.rules.Rule, Double> filterRules = new HashMap<amie.rules.Rule, Double>();
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/gilpRules/gilpRules-" + iterationTimes + "-" + decision + ".tsv"),
                Charset.forName("UTF-8"));
        Writer writer1 = new OutputStreamWriter(
                new FileOutputStream("./data/gilpRules/amieRules-" + iterationTimes + "-" + decision + ".tsv"),
                Charset.forName("UTF-8"));

        if (!rules.isEmpty()) {
            for (amie.rules.Rule rule : rules) {
                if (rule.getLength() == 3) {
                   // if (rule.getPcaConfidence() == 1)
                  //  writer1.write(rule+"\t"+ rule.getPcaConfidence() + "\n");
                    filterRules.put(rule, rule.getPcaConfidence());

                }
            }
        }

        filterRulesSort = new RuleLearnerHelper().reverseOrderByValue(filterRules);
        ArrayList<amie.rules.Rule> finalRules = new ArrayList<>(filterRulesSort.keySet());

        for (amie.rules.Rule rule : finalRules) {
            Rule rdfRule = new RulesOthers().transformRule(rule);
            datalogRules.add(rdfRule);
            writer1.write(rule+"\t"+ filterRules.get(rule) + "\n");
            writer.write(rdfRule + "\t" + filterRules.get(rule) + "\n");
        }

        writer.close();
        writer1.close();
        return datalogRules;
    }


    public HashSet<String> getRulesByObjectPrecision(ArrayList<Rule> datalogRules, Boolean decision) throws Exception {
        HashSet<String> allObjects = new HashSet<String>();
        int i = 0;
        if (!datalogRules.isEmpty()) {
            for (Rule rule : datalogRules) {

                Clause cls = rule.getCorrespondingClause();
                RDFPredicate head = rule.get_head();

                if (head.isObjectVariable()) {
                    HashMap<String, Double> objects = new HashMap<>();

                    if (ILPLearnSettings.condition == 1) {
                        String sparql = new GetSparql().ruleToObjectSparql(cls, head);
                        System.out.println(sparql + "\n");
                        objects = new RDF3XEngine().getCountSingleEntity(sparql);
                    } else {
                        String sparql = new GetSparql().ruleToObjectSparqlOnLine(cls, head);//count ?b
                        System.out.println(sparql + "\n");
                        objects = new Sparql().countResults(sparql);
                    }

                    if (objects != null) {
                        KVPair<HashSet<String>, Values> filterObjects = new Evaluation().heuristicEvaluation(objects, decision);
                        Values measure = filterObjects.getValue();
                        double precision = measure.getPrecision();
                        double accuracy = measure.getAccuracy();
                        double FScores = measure.getF1();
                        System.out.println(measure.getTP() + "\t" + measure.getFp() + "\t" + measure.getTn() + "\t" + measure.getFn() + "\t");

                        System.out.println(precision + "\t" + accuracy + "\t" + FScores + "\n");

                        if (precision > 0.9) {
                            allObjects.addAll(filterObjects.getKey());
                        }
                    }
                }
                System.out.println("\n");
            }
        }

        return allObjects;
    }


    private HashSet<String> filterRulesInDBPedia(HashSet<Triple> positiveTriple, HashSet<Triple> negativeTriple,
                                                 boolean decision, Integer iterationTimes,
                                                 ArrayList<Rule> positiveRules,
                                                 int numbers) throws Exception {


        int i = 1;

        HashSet<String> ruleSubjectsAll = new HashSet<String>();
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/gilpRules/GILP_" + "_" + iterationTimes + "_" + decision + ".tsv"),
                Charset.forName("UTF-8"));

        writer.write("id\t" + "rule\t" + "size\t" + "precision:\t"
                + "lowerBoundInAllFeedback:\t" + "upperBoundInAllFeedback:\t" + "interval:\t" + "\n");

        for (Rule rule : positiveRules) {
            HashSet<String> predictions = new HashSet<>();
            if (Property.ruleLabel)
                predictions = new RulesOthers().ruleToSubjects(rule);
            else
                predictions = new RulesOthers().ruleToObjects(rule);

            ArrayList<Double> selectMeasure = new Evaluation().evaluation(predictions, positiveTriple, negativeTriple, decision, numbers);

            double precisionInFeedback = selectMeasure.get(0);
            double lowerBoundInAllFeedback = selectMeasure.get(4);
            double upperBoundInAllFeedback = selectMeasure.get(5);
            double interval = upperBoundInAllFeedback - lowerBoundInAllFeedback;

            if (interval > ILPLearnSettings.intervalNumber || precisionInFeedback < 0.9) {
                numbers = numbers + 100;
            }
            ruleSubjectsAll.addAll(predictions);

            //---------------------------choose the rules use the precision
//            HashSet<Triple> triples = new RulesOthers().subjectsToTriples(ruleSubjects);
//            HashSet<Comment> newPositiveComments =
//                    new RulesOthers().tripleToComment(triples);
//
//            HashSet<Comment> extendComments = new RuleLearnerHelper().filterComments(newPositiveComments, decision);
//            double precision = (double) extendComments.size() / newPositiveComments.size();
//
//            if (precision > 0.9)
//                ruleSubjectsAll.addAll(ruleSubjects);


            writer.write(i++ + "\t" + rule + "\t" + predictions.size() + "\t");
            writer.write(precisionInFeedback + "\t");
            writer.write(lowerBoundInAllFeedback + "\t");
            writer.write(upperBoundInAllFeedback + "\t");
            writer.write(interval + "\n");


        }

        ArrayList<Double> selectWholeMeasure =
                new Evaluation().evaluation(ruleSubjectsAll, positiveTriple, negativeTriple, decision, numbers);

        writer.write("ruleSubjectsAll.size:\t" + ruleSubjectsAll.size() + "\n");


        double precisionInFeedback = selectWholeMeasure.get(0);
        double lowerBoundInAllFeedback = selectWholeMeasure.get(4);
        double upperBoundInAllFeedback = selectWholeMeasure.get(5);
        double interval = upperBoundInAllFeedback - lowerBoundInAllFeedback;

        if (interval > ILPLearnSettings.intervalNumber || precisionInFeedback < 0.9) {
            numbers = numbers + 100;
        }

        writer.write("precision:\t" + precisionInFeedback + "\n");
        writer.write("lowerBoundInAllFeedback:\t" + lowerBoundInAllFeedback + "\n");
        writer.write("upperBoundInAllFeedback:\t" + upperBoundInAllFeedback + "\n");
        writer.write("interval:\t" + interval + "\n");

        writer.close();
        System.out.println("\n " + iterationTimes + " iteration--finish  " + decision + " analysis of rules in "
        );

        return ruleSubjectsAll;

    }

    public HashSet<Triple> filterRulesInEvaluation(HashSet<Triple> positiveTriple, HashSet<Triple> negativeTriple,
                                                   boolean decision, ArrayList<Rule> positiveRules, int iterationTimes) throws Exception {

        int i = 1;

        HashSet<Triple> allTriples = new HashSet<Triple>();
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/gilpRules/rule" + "_" + iterationTimes + "_" + decision + ".tsv"),
                Charset.forName("UTF-8"));

        writer.write("id\t" + "rule\t" + "size\t" + "precision:\t"
                + "lowerBoundInAllFeedback:\t" + "upperBoundInAllFeedback:\t" + "interval:\t" + "\n");

        for (Rule rule : positiveRules) {
            HashSet<String> ruleSubjects = new RulesOthers().ruleToSubjects(rule);
            if (ruleSubjects != null) {

                HashSet<Triple> rulesToTriples = new RulesOthers().subjectsToTriples(ruleSubjects);
                writer.write(rulesToTriples.size() + "\n");
                ArrayList<Double> selectMeasure = new Evaluation().evaluation(rulesToTriples,
                        positiveTriple, negativeTriple, decision);

                double precisionInFeedback = selectMeasure.get(0);
                double lowerBoundInAllFeedback = selectMeasure.get(4);
                double upperBoundInAllFeedback = selectMeasure.get(5);
                double interval = upperBoundInAllFeedback - lowerBoundInAllFeedback;


                allTriples.addAll(rulesToTriples);


                writer.write(i++ + "\t" + rule + "\t" + ruleSubjects.size() + "\t");
                writer.write(precisionInFeedback + "\t");
                writer.write(lowerBoundInAllFeedback + "\t");
                writer.write(upperBoundInAllFeedback + "\t");
                writer.write(interval + "\n");

            }
        }

        ArrayList<Double> selectWholeMeasure = new Evaluation().evaluation(allTriples,
                positiveTriple, negativeTriple, decision);


        writer.write("ruleSubjectsAll.size:\t" + allTriples.size() + "\n");


        double precisionInFeedback = selectWholeMeasure.get(0);
        double lowerBoundInAllFeedback = selectWholeMeasure.get(4);
        double upperBoundInAllFeedback = selectWholeMeasure.get(5);
        double interval = upperBoundInAllFeedback - lowerBoundInAllFeedback;


        writer.write("precision:\t" + precisionInFeedback + "\n");
        writer.write("lowerBoundInAllFeedback:\t" + lowerBoundInAllFeedback + "\n");
        writer.write("upperBoundInAllFeedback:\t" + upperBoundInAllFeedback + "\n");
        writer.write("interval:\t" + interval + "\n");

        writer.close();
        System.out.println("\n " + " iteration--finish  " + decision + " analysis of rules in "
        );

        return allTriples;

    }
}


/**
 *
 * public static void main(String[] args) throws IOException, Exception {
 * 		 long loadingStartTime = System.currentTimeMillis();
 * 		// new RuleInDbpedia().filterRules();
 * 		// String test1="Williams@en";
 *
 * //		String test="1305^^http://www.w3.org/2001/XMLSchema#gYear";
 * //		String newEntity= sparqlInExtendTriple( test);
 * //		System.out.print("\n newEntity"+newEntity + "\n");
 *
 * 		String[] paras2 =
 *                        { "-maxad", "3",
 *     				"-mins", "1", "-minis", "1", "-bexr","<http://dbpedia.org/ontology/nationality>",
 *     				 "-htr"," <http://dbpedia.org/ontology/nationality>",
 * 				 "-minhc", "0.01", "-minpca", "0.01", "-dpr", "-optimfh", "-const",
 * 				"/home/wy/Desktop/Triples_extend.tsv"};
 *
 * 		String[] paras1 = { "-maxad", "3", "-mins", "1", "-minis", "1",
 * 				"-bexr","<http://dbpedia.org/ontology/nationality>", "-htr", " <http://dbpedia.org/ontology/nationality>",
 * 				"-minhc", "0.1",
 * 				 "-minpca", "0.1",
 * 				 "-dpr",
 * 				"-optimfh",
 * 				 "-const",
 * 				"/home/wy/Desktop/Triples_extend.tsv" };
 *
 * 		String[] paras = { "-maxad", "3", "-mins", "1", "-minis", "1", "-htr",
 * 				"<http://dbpedia.org/ontology/nationality>", "-bexr", "<http://dbpedia.org/ontology/nationality>","-minhc", "0.01","-minpca", "0.01",
 * //	    				"-mins", "1", "-minis", "1", "-bexr",
 * //					"<http://dbpedia.org/ontology/nationality>", "-htr",
 * //					" <http://dbpedia.org/ontology/nationality>", "-minhc", "0.01", "-minpca", "0.01", "-dpr", "-optimfh", "-const",
 * 				"/home/wy/Desktop/Triples_extend.tsv" };
 *
 * 	   	String[] paras3 =
 *            { "-maxad", "3",
 *     			//	"-mins", "1", "-minis", "1",
 *     				"-bexr","<http://dbpedia.org/ontology/nationality>",
 * 			 "-htr",
 * 				" <http://dbpedia.org/ontology/nationality>",
 *    				//"-minhc", "0.01", "-minpca", "0.01",
 *     			//	"-optimfh", "-dpr",
 *     			//	"-const",
 * 				"/home/wy/Desktop/Triples_extend.tsv"};//,"-datalog"};
 *
 * 	   amie.mining.AMIE miner = amie.mining.AMIE.getInstance(paras1);
 *
 * 	//	amie.mining.AMIE miner = amie.mining.AMIE.getInstance(paras3);// new GILPSettings().parameters());
 * 		 long loadingTime = System.currentTimeMillis() - loadingStartTime;
 * 		Announce.doing("\n Starting the mining phase \n");
 *
 * 		List<amie.rules.Rule> rules = miner.mine();
 *
 *
 * 		Announce.done("\n finish the mining phase \n");
 * 		System.out.println(rules.size() + " rules mined.");
 *
 * 		 long time = System.currentTimeMillis();
 *
 *
 * 		 long miningTime = System.currentTimeMillis() - time;
 * 	        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));
 * 	        Announce.done("Total time " + NumberFormatter.formatMS(miningTime + loadingTime));
 * 	        System.out.println(rules.size() + " rules mined.");
 *
 *
 * 		Writer writer1 = new OutputStreamWriter(new FileOutputStream("/home/wy/Desktop/sample.txt"),
 * 				Charset.forName("UTF-8"));
 *
 * 		for (int index = 0; index < rules.size(); index++)
 *
 * 			writer1.write(rules.get(index) + "\n");
 *
 * 		writer1.clos    );
 * 	}
 */