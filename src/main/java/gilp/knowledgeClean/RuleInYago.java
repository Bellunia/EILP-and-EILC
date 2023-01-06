package gilp.knowledgeClean;

import gilp.GILLearn_correction.ILPLearnSettings;
import gilp.GILLearn_correction.yagoCorrection;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.utils.KVPair;
import javatools.administrative.Announce;
import javatools.parsers.NumberFormatter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RuleInYago {

    public HashSet<Triple> filterRandomTriples(int numbers) throws Exception {

        HashSet<Triple> allTriples=  yagoCorrection.readTriples("./data/yago_correction/isCitizenOf/isCitizenOf-allTriples.txt");

        int s = allTriples.size();

        int[] isChosen = new int[s];
        for (int i = 0; i < s; i++) {
            isChosen[i] = 0;
        }
        HashSet<Triple> triples = new HashSet<>();

        HashSet<String> object = new HashSet<>();

        while (triples.size() < Math.min(numbers, s)) {
            int idx = (int) Math.round(Math.random() * (s - 1));
            Triple cmt = (Triple) allTriples.toArray()[idx];

            if (isChosen[idx] == 0 && !object.contains(cmt.get_obj())) {
                triples.add(cmt);
                object.add(cmt.get_obj());
                isChosen[idx] = 1;
            }
        }
        return triples;
    }

    public KVPair<HashSet<Triple>,HashSet<Triple>> generateFeedback(HashSet<Triple> triples){
        HashSet<String>  truePositive_country = RuleLearnerHelper.readTypes("./data/yago_correction/isCitizenOf/truePositive-country.txt");
        HashSet<Triple> positive = new HashSet<>();
        HashSet<Triple> negative = new HashSet<>();
        for(Triple tri: triples){
            if(truePositive_country.contains(tri.get_obj()))
                positive.add(tri);
            else
                negative.add(tri);
        }
        return new KVPair<>(positive, negative);
    }


    public HashSet<String> getAllRules(int initialNumbers, HashSet<Rule> posQualifiedRules,
                                       HashSet<Rule> negQualifiedRules, int selectNumber) throws Exception {
        // full the condition: T(i)=T(i+1) ---------------------------
        HashSet<String> allPrediction = new HashSet<String>();
        HashSet<Triple> randomTriples= filterRandomTriples(initialNumbers);

        KVPair<HashSet<Triple>,HashSet<Triple>> feedback= generateFeedback(randomTriples);
        HashSet<Triple> positive = feedback.getKey();
        HashSet<Triple> negative = feedback.getValue();

        int iterationTimes = 0;

        HashSet<String> predictionsEachLoop = new HashSet<String>();
        while (true) {

            iterationTimes++;
            long time = System.currentTimeMillis();
            System.out.println("\n------- Iteration:" + iterationTimes + "-----------------\n");
           filterSearchSpace(positive, negative, iterationTimes);

            HashSet<String> positivePrediction = filterAmieRules(positive,negative, true, iterationTimes,
                    posQualifiedRules, selectNumber);
            HashSet<String> negativePrediction = filterAmieRules(positive,negative, false, iterationTimes,
                    negQualifiedRules, selectNumber);

            predictionsEachLoop.addAll(positivePrediction);
            predictionsEachLoop.addAll(negativePrediction);

            HashSet<String> subjectsPos = triplesToSub(positive);
            HashSet<String> subjectsNeg= triplesToSub(negative);

            positivePrediction.removeAll(subjectsPos);
            negativePrediction.removeAll(subjectsNeg);


                if (positivePrediction.isEmpty() || negativePrediction.isEmpty()) {
                    return null;
                }

            HashSet<Triple> newPositive = filterTriplesByType(new RuleLearnerHelper().subjectsToTriples(positivePrediction),true);

            HashSet<Triple> newNegative = filterTriplesByType(new RuleLearnerHelper().subjectsToTriples(negativePrediction),false);


            if (posQualifiedRules.isEmpty() || negQualifiedRules.isEmpty()) {
                return null;
            }

            if (!(predictionsEachLoop.containsAll(allPrediction) && allPrediction.containsAll(predictionsEachLoop))) {

                positive.addAll(newPositive);
                negative.addAll(newNegative);

                allPrediction.clear();
                allPrediction.addAll(predictionsEachLoop);
                predictionsEachLoop.clear();

            } else {
                System.out.println("\n Last allPrediction: " + allPrediction.size());
                return allPrediction;
            }

            long miningTime = System.currentTimeMillis() - time;
            System.out.println("finish " + iterationTimes + " iteration time in " + NumberFormatter.formatMS(miningTime));
        }
    }

    private HashSet<Triple> filterTriplesByType(HashSet<Triple> selectTriples, Boolean decision) {
        HashSet<String>  truePositive_country = RuleLearnerHelper.readTypes("./data/yago_correction/isCitizenOf/truePositive-country.txt");
        HashSet<Triple> selectedTriples = new HashSet<Triple>();
        HashSet<Triple> selectedNegTriples = new HashSet<Triple>();

        HashMap<String, Boolean> objectType= new HashMap<>();

        for (Triple key : selectTriples) {
            String obj = key.get_obj();
            if(!objectType.containsKey(obj)) {

                if (truePositive_country.contains(obj)) {
                    selectedTriples.add(key);
                    objectType.put(obj,true);
                } else {
                    selectedNegTriples.add(key);
                    objectType.put(obj,false);
                }

            }
            else{
                Boolean deci= objectType.get(obj);
                if(deci)
                    selectedTriples.add(key);
                else
                    selectedNegTriples.add(key);
            }
        }
        if(decision)
        return selectedTriples;
        else
            return selectedNegTriples;
    }
    private HashSet<String> triplesToSub(HashSet<Triple> triples) {
        HashSet<String> positiveSet = new HashSet<String>();
        for (Triple tri : triples)
                positiveSet.add(tri.get_subject());
        return positiveSet;
    }

    public HashSet<String> filterAmieRules(HashSet<Triple> positive,HashSet<Triple> negative, boolean decision, Integer iterationTimes,
                                           HashSet<Rule> qualifiedRules, int numbers) throws Exception {

        long time = System.currentTimeMillis();


        HashSet<Rule> amieRules =new HashSet<>();
        if(decision)
            amieRules = amieRules(iterationTimes, true);
        else
            amieRules = amieRules(iterationTimes, false);

        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/yago_correction/rule/filterRule_" + iterationTimes + "_" + decision + ".tsv"),
                Charset.forName("UTF-8"));

        writer.write("positive :\t" + positive.size() + "\n");
        writer.write("negative :\t" + negative.size() + "\n");
        writer.write("rule.size :\t" + amieRules.size() + "\n");

        int i = 1;

        HashSet<String> ruleSubjectsAll = new HashSet<String>();

        qualifiedRules.addAll(amieRules);

        for (Rule rule : amieRules) {
            HashSet<String> ruleSubjects = new RuleLearnerHelper().ruleToSubjects(rule);
            if (ruleSubjects != null) {
                ruleSubjectsAll.addAll(ruleSubjects);
                writer.write(i++ + "\t" + rule + "\t" + ruleSubjects.size() + "\n");
            }
        }

        ArrayList<Double> selectMeasure = new Evaluation().evaluation
                ( ruleSubjectsAll, positive, negative, decision, numbers);

        double precisionInFeedback =  selectMeasure.get(0);
        double lowerBoundInAllFeedback =selectMeasure.get(4);
        double upperBoundInAllFeedback =selectMeasure.get(5);
        double interval = upperBoundInAllFeedback - lowerBoundInAllFeedback;

        if (interval > GILPSettings.intervalConfidence || precisionInFeedback < 0.9) {
            numbers = numbers + 100;
        }

        writer.write("precision:\t" + precisionInFeedback + "\n");
        writer.write("lowerBoundInAllFeedback:\t" + lowerBoundInAllFeedback + "\n");
        writer.write("upperBoundInAllFeedback:\t" + upperBoundInAllFeedback + "\n");
        writer.write("interval:\t" + interval + "\n");
        writer.write("ruleSubjectsAll.size:\t" + ruleSubjectsAll.size() + "\n");

        writer.close();

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("\n " + iterationTimes + " iteration--finish  " + decision + " analysis of rules in "
                + NumberFormatter.formatMS(miningTime));

        return ruleSubjectsAll;

    }

    public HashSet<String> filterAmieRulesInType(HashSet<Triple> positive,HashSet<Triple> negative, boolean decision, Integer iterationTimes,
                                           HashSet<Rule> qualifiedRules, int numbers) throws Exception {

        long time = System.currentTimeMillis();


        HashSet<Rule> amieRules =new HashSet<>();
        if(decision)
            amieRules = amieRules(iterationTimes, true);
        else
            amieRules = amieRules(iterationTimes, false);

        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/yago_correction/rule/filterRule_" + iterationTimes + "_" + decision + ".tsv"),
                Charset.forName("UTF-8"));

        writer.write("positive :\t" + positive.size() + "\n");
        writer.write("negative :\t" + negative.size() + "\n");
        writer.write("rule.size :\t" + amieRules.size() + "\n");

        int i = 1;

        HashSet<String> ruleSubjectsAll = new HashSet<String>();

        qualifiedRules.addAll(amieRules);

        for (Rule rule : amieRules) {
            HashSet<String> ruleSubjects = new RuleLearnerHelper().ruleToSubjects(rule);


            if (ruleSubjects != null) {
                ruleSubjectsAll.addAll(ruleSubjects);
                writer.write(i++ + "\t" + rule + "\t" + ruleSubjects.size() + "\n");
            }
        }
        HashSet<Triple> newPositive = filterTriplesByType(new RuleLearnerHelper().subjectsToTriples(ruleSubjectsAll),decision);
        ArrayList<Double> selectMeasure = new Evaluation().evaluation
                ( ruleSubjectsAll, positive, negative, decision, numbers);

        double precisionInFeedback =  selectMeasure.get(0);
        double lowerBoundInAllFeedback =selectMeasure.get(4);
        double upperBoundInAllFeedback =selectMeasure.get(5);
        double interval = upperBoundInAllFeedback - lowerBoundInAllFeedback;

        if (interval > GILPSettings.intervalConfidence || precisionInFeedback < 0.9) {
            numbers = numbers + 100;
        }

        writer.write("precision:\t" + precisionInFeedback + "\n");
        writer.write("lowerBoundInAllFeedback:\t" + lowerBoundInAllFeedback + "\n");
        writer.write("upperBoundInAllFeedback:\t" + upperBoundInAllFeedback + "\n");
        writer.write("interval:\t" + interval + "\n");
        writer.write("ruleSubjectsAll.size:\t" + ruleSubjectsAll.size() + "\n");

        writer.close();

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("\n " + iterationTimes + " iteration--finish  " + decision + " analysis of rules in "
                + NumberFormatter.formatMS(miningTime));

        return ruleSubjectsAll;

    }


    public void filterSearchSpace(HashSet<Triple> positiveTriple, HashSet<Triple> negativeTriple, int iterationTimes) throws Exception {

        int numbers=positiveTriple.size()+negativeTriple.size();
         writeOutFeedbacks(positiveTriple,negativeTriple,numbers);

        new YagoSearchSpace().extendTriples(positiveTriple, GILPSettings.DEFAULT_LEVEL, true, iterationTimes);
       new YagoSearchSpace().extendTriples(positiveTriple, GILPSettings.DEFAULT_LEVEL, false, iterationTimes);
       //---------------------

        String pathPos = "./data/yago_correction/searchSpace/positiveSearchSpace" + iterationTimes + "-old.tsv";
        String pathNeg = "./data/yago_correction/searchSpace/negativeSearchSpace" + iterationTimes + "-old.tsv";

        HashSet<Triple> positiveTriples =  ILPLearnSettings.readTriples(pathPos);
        HashSet<Triple> negativeTriples = ILPLearnSettings.readTriples(pathNeg);
        //negativeTriples.removeAll(positiveTriples);
        writeOutSearchSpace(positiveTriples, true, iterationTimes);
        writeOutSearchSpace(negativeTriples, false, iterationTimes);
    }

    private void writeOutSearchSpace(HashSet<Triple> expandedTriples, Boolean decision, int iterationTimes) throws IOException {
        String path = null;
        if (decision)
            path = "./data/yago_correction/searchSpace/positiveSearchSpace" + iterationTimes + ".tsv";
        else path = "./data/yago_correction/searchSpace/negativeSearchSpace" + iterationTimes + ".tsv";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));

        try {
            for (Triple t : expandedTriples) {
                writer.write(("<" + replaceSpecialSymbol(t.get_subject()) + ">\t"));
                writer.write(("<" + replaceSpecialSymbol(t.get_predicate()) + ">\t"));
                writer.write(("<" + replaceSpecialSymbol(t.get_obj()) + ">\n"));

            }
            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }

    private void writeOutFeedbacks(HashSet<Triple> positiveTriples,HashSet<Triple> negativeTriples,  int numbers) throws IOException {
        String path = "./data/yago_correction/searchSpace/feedback-" + numbers + ".txt";
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

    private String[] parameters(Boolean decision, int iterationTimes) {
        if (decision)
            return new String[]{"-maxad", "3",
                    "-mins", String.valueOf(ILPLearnSettings.initialNumber2), "-minis", String.valueOf(ILPLearnSettings.initialNumber2),
                    "-bexr", "<" + GILPSettings.DEFAULT_CONDITIONAL_NAME + ">", "-htr", " <" + GILPSettings.DEFAULT_CONDITIONAL_NAME + ">",
                    "-minhc", "0.01", "-minpca", "0.01",
                    "-dpr", "-optimfh",
                    // "-const",
                    "./data/yago_correction/searchSpace/positiveSearchSpace" + iterationTimes + ".tsv"};
        else
            return new String[]{"-maxad", "3",
                    "-mins", String.valueOf(ILPLearnSettings.initialNumberNeg), "-minis", String.valueOf(ILPLearnSettings.initialNumberNeg),
                    "-bexr", "<" + GILPSettings.DEFAULT_CONDITIONAL_NAME + ">", "-htr", " <" + GILPSettings.DEFAULT_CONDITIONAL_NAME + ">",
                    "-minhc", "0.01", "-minpca", "0.01",
                    "-dpr", "-optimfh",
                    "-const",
                    "./data/yago_correction/searchSpace/negativeSearchSpace" + iterationTimes + ".tsv"};

    }

    public HashSet<Rule> amieRules(int iterationTimes, Boolean decision) throws Exception {

        HashSet<Rule> gilpRules = new HashSet<>();

        amie.mining.AMIE miner = amie.mining.AMIE.getInstance(parameters(decision,iterationTimes));

        Announce.doing("\n Starting the mining phase \n");

        List<amie.rules.Rule> rules = miner.mine();

        Announce.done("\n finish the mining phase \n");
        Announce.close();

        HashMap<amie.rules.Rule, Double> filterRulesSort = new HashMap<amie.rules.Rule, Double>();
        HashMap<amie.rules.Rule, Double> filterRules = new HashMap<amie.rules.Rule, Double>();
        Writer writer1 = new OutputStreamWriter(
                new FileOutputStream("./data/yago_correction/amieRules-" + iterationTimes + "-" + decision + ".tsv"),
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
            Rule rdfRule = transformRule(rule);
            gilpRules.add(rdfRule);
            //  writer1.write(rule+"\t"+ filterRules.get(rule) + "\n");
            writer1.write(rdfRule + "\t" + filterRules.get(rule) + "\n");
        }


        writer1.close();
        return gilpRules;
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


    private String replaceSpecialSymbol(String element){
        return "<" +element.replace("-", "@").replace("–", "#")+">";
    }

    private RDFPredicate extractRDFPredicate(int[] byteStrings) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        String subject = Integer.toString(byteStrings[0]).replaceAll("<", "").replaceAll(">", "");
        String predicate = Integer.toString(byteStrings[1]).replaceAll("<", "").replaceAll(">", "");
        String object =Integer.toString( byteStrings[2]).replaceAll("<", "").replaceAll(">", "");

        rdfPredicate.setPredicateName(predicate);
        rdfPredicate.setSubject(subject);
        rdfPredicate.setObject(object);
        return rdfPredicate;
    }

    private RDFPredicate extractComplexRDFPredicate(int[] bs) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        String subject = Integer.toString(bs[0]).replaceAll("<", "").replaceAll(">", "").replaceAll("@", "-").replaceAll("#", "–");
        String predicate = Integer.toString(bs[1]).replaceAll("<", "").replaceAll(">", "").replaceAll("@", "-").replaceAll("#", "–");
        String object =Integer.toString( bs[2]).replaceAll("<", "").replaceAll(">", "").replaceAll("@", "-").replaceAll("#", "–");
        //String object = bs[2].toString().replaceAll("<", "").replaceAll(">", "").replaceAll("@", "-").replaceAll("#", "–");;

        rdfPredicate.setPredicateName(predicate);
        rdfPredicate.setSubject(subject);
        rdfPredicate.setObject(object);
        return rdfPredicate;
    }




}
