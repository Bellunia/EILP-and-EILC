package gilp.measure;

import gilp.GILLearn_correction.ILPLearnSettings;
import gilp.GILLearn_correction.Property;
import gilp.GILLearn_correction.RulesOthers;
import gilp.knowledgeClean.GILPSettings;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import gilp.utils.KVPair;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Evaluation {

    public ArrayList<Double> evaluation
            (HashSet<String> ruleSubjects, HashSet<Triple> positiveTriple,HashSet<Triple> negativeTriple, boolean decision,
                                        int numbers) {

        HashSet<String> randomPositiveString = new RuleLearnerHelper().filterRandomString(numbers, ruleSubjects);
        return oneRuleMeasureInFeedback(randomPositiveString,
                positiveTriple,negativeTriple, decision);

    }

    public ArrayList<Double> evaluation(HashSet<Triple> rulesToTriples,
                                        HashSet<Triple> positiveTriples, HashSet<Triple> negativeTriples, boolean decision
                                        ) {

       // HashSet<String> randomPositiveString = new RuleLearnerHelper().filterRandomString(numbers, ruleSubjects);

       // HashSet<Triple> rulesToTriples= new RuleInDbpedia().subjectsToTriples(ruleSubjects, predicate);

        ArrayList<Double> oneRuleMeasureInFeedback = oneRuleMeasureInFeedback1(rulesToTriples,
                positiveTriples,negativeTriples, decision);

        return new ArrayList<Double>(oneRuleMeasureInFeedback);

    }

    public ArrayList<Double> oneRuleMeasureInFeedback1(HashSet<Triple> rulesToTriples,
                                                      HashSet<Triple> positiveTriples, HashSet<Triple> negativeTriples,
                                                      boolean decision) {

        // -----------from the select comments,compare the subjects
        ConfusionMatrix confusionMatrixInFeedbck = getConfusionMatrixInFeedback1(positiveTriples,negativeTriples, rulesToTriples, decision);

        double precisionInFeedback = evaluatePrecision(confusionMatrixInFeedbck);
        double yieldInFeedback = confusionMatrixInFeedbck.getNumberOfTruePositives();

        double accuracyInFeedback = Precision.round(evaluateAccuracy(confusionMatrixInFeedbck), 4);
        double recallInFeedback = Precision.round(evaluateRecall(confusionMatrixInFeedbck), 4);

        ConfidenceInterval intervalInFeedback = interval(yieldInFeedback,
                confusionMatrixInFeedbck.getNumberOfPredictedPositive());
        double lowerBoundInFeedback = Precision.round(intervalInFeedback.getLowerBound(), 4);
        double upperBoundInFeedback = Precision.round(intervalInFeedback.getUpperBound(), 4);

        ArrayList<Double> measure = new ArrayList<Double>();
        measure.add(precisionInFeedback);
        measure.add(yieldInFeedback);
        measure.add(accuracyInFeedback);
        measure.add(recallInFeedback);

        measure.add(lowerBoundInFeedback);
        measure.add(upperBoundInFeedback);

        return measure;
    }


    public ConfusionMatrix getConfusionMatrixInFeedback1(HashSet<Triple> positiveTriples, HashSet<Triple> negativeTriples,
                                                         HashSet<Triple> rulesToTriples,
                                                         boolean decision) {

        ConfusionMatrix confusionMatrixInFeedback = new ConfusionMatrix();

//        KVPair<HashSet<String>, HashSet<String>> subjectsFromComments = new RuleLearnerHelper()
//                .commentsToSubjects(comments);
      //  HashSet<String> positiveInComments = subjectsFromComments.getKey();
     //   HashSet<String> negativeInComments = subjectsFromComments.getValue();

        if (!rulesToTriples.isEmpty()) {

            HashSet<Triple> truePositive = new HashSet<Triple>(rulesToTriples);
            if (decision) {
                truePositive.retainAll(positiveTriples);
            } else {
                truePositive.retainAll(negativeTriples);
            }
            double numberOfTruePositives = truePositive.size();
            // --------------------------------------
            HashSet<Triple> falseNegative = new HashSet<Triple>();
            if (decision) {
                falseNegative.addAll(positiveTriples);
            } else {
                falseNegative.addAll(negativeTriples);
            }

            falseNegative.removeAll(truePositive);

            double numberOfFalseNegatives = falseNegative.size();
            // -------------------------------------------

            HashSet<Triple> falsePositive = new HashSet<Triple>(rulesToTriples);
            if (decision) {
                falsePositive.retainAll(negativeTriples);
            } else {
                falsePositive.retainAll(positiveTriples);
            }

            double numberOfFalsePositives = falsePositive.size();
            // ------------------------------------------------------------------
            HashSet<Triple> trueNegative = new HashSet<Triple>();
            if (decision) {
                trueNegative.addAll(negativeTriples);
            } else {
                trueNegative.addAll(positiveTriples);
            }

            trueNegative.removeAll(falsePositive);

            double numberOfTrueNegatives = trueNegative.size();
            // ------------------------------------

            confusionMatrixInFeedback.setNumberOfTruePositives(numberOfTruePositives);
            confusionMatrixInFeedback.setNumberOfFalseNegatives(numberOfFalseNegatives);
            confusionMatrixInFeedback.setNumberOfFalsePositives(numberOfFalsePositives);
            confusionMatrixInFeedback.setNumberOfTrueNegatives(numberOfTrueNegatives);

        }

        return confusionMatrixInFeedback;
    }



    public ArrayList<Double> oneRuleMeasureInFeedback(HashSet<String> ruleSubjects,HashSet<Triple> positiveTriple,HashSet<Triple> negativeTriple,
                                                      boolean decision) {

        // -----------from the select comments,compare the subjects
        ConfusionMatrix confusionMatrixInFeedbck = getConfusionMatrixInFeedback(positiveTriple,negativeTriple, ruleSubjects, decision);

        double precisionInFeedback = evaluatePrecision(confusionMatrixInFeedbck);
        double yieldInFeedback = confusionMatrixInFeedbck.getNumberOfTruePositives();

        double accuracyInFeedback = Precision.round(evaluateAccuracy(confusionMatrixInFeedbck), 4);
        double recallInFeedback = Precision.round(evaluateRecall(confusionMatrixInFeedbck), 4);

        ConfidenceInterval intervalInFeedback = interval(yieldInFeedback,
                confusionMatrixInFeedbck.getNumberOfPredictedPositive());
        double lowerBoundInFeedback = Precision.round(intervalInFeedback.getLowerBound(), 4);
        double upperBoundInFeedback = Precision.round(intervalInFeedback.getUpperBound(), 4);

        ArrayList<Double> measure = new ArrayList<Double>();
        measure.add(precisionInFeedback);
        measure.add(yieldInFeedback);
        measure.add(accuracyInFeedback);
        measure.add(recallInFeedback);

        measure.add(lowerBoundInFeedback);
        measure.add(upperBoundInFeedback);

        return measure;
    }

    public ConfusionMatrix getConfusionMatrixInFeedback(HashSet<Triple> positiveTriple, HashSet<Triple> negativeTriple, HashSet<String> ruleSubjects,
                                                        boolean decision) {

        ConfusionMatrix confusionMatrixInFeedback = new ConfusionMatrix();
        HashSet<String> subjectsPos = new RulesOthers().triplesToSub(positiveTriple);
        HashSet<String> subjectsNeg = new RulesOthers().triplesToSub(negativeTriple);

        if (ruleSubjects != null) {

            HashSet<String> truePositive = new HashSet<String>();
            truePositive.addAll(ruleSubjects);
            if (decision) {
                truePositive.retainAll(subjectsPos);
            } else {
                truePositive.retainAll(subjectsNeg);
            }
            double numberOfTruePositives = truePositive.size();
            // --------------------------------------
            HashSet<String> falseNegative = new HashSet<String>();
            if (decision) {
                falseNegative.addAll(subjectsPos);
            } else {
                falseNegative.addAll(subjectsNeg);
            }

            falseNegative.removeAll(truePositive);

            double numberOfFalseNegatives = falseNegative.size();
            // -------------------------------------------

            HashSet<String> falsePositive = new HashSet<String>();
            falsePositive.addAll(ruleSubjects);
            if (decision) {
                falsePositive.retainAll(subjectsNeg);
            } else {
                falsePositive.retainAll(subjectsPos);
            }

            double numberOfFalsePositives = falsePositive.size();
            // ------------------------------------------------------------------
            HashSet<String> trueNegative = new HashSet<String>();
            if (decision) {
                trueNegative.addAll(subjectsNeg);
            } else {
                trueNegative.addAll(subjectsPos);
            }

            trueNegative.removeAll(falsePositive);

            double numberOfTrueNegatives = trueNegative.size();
            // ------------------------------------

            confusionMatrixInFeedback.setNumberOfTruePositives(numberOfTruePositives);
            confusionMatrixInFeedback.setNumberOfFalseNegatives(numberOfFalseNegatives);
            confusionMatrixInFeedback.setNumberOfFalsePositives(numberOfFalsePositives);
            confusionMatrixInFeedback.setNumberOfTrueNegatives(numberOfTrueNegatives);

        }

        return confusionMatrixInFeedback;
    }

    public double evaluatePrecision(final ConfusionMatrix confusionMatrix) {
        double predictedPositive = confusionMatrix.getNumberOfPredictedPositive();
        return predictedPositive != 0 ? confusionMatrix.getNumberOfTruePositives() / predictedPositive : 0;
    }

    public double evaluateAccuracy(final ConfusionMatrix confusionMatrix) {
        double examples = confusionMatrix.getNumberOfExamples();
        return examples != 0 ? confusionMatrix.getNumberOfCorrectlyClassified() / examples : 0;
    }

    public double evaluateRecall(final ConfusionMatrix confusionMatrix) {
        double truePositives = confusionMatrix.getNumberOfTruePositives();
        double falseNegatives = confusionMatrix.getNumberOfFalseNegatives();
        double denominator = truePositives + falseNegatives;
        return denominator != 0 ? truePositives / denominator : 0;
    }

    public ConfidenceInterval interval(double numberOfSuccesses, double numberOfTrials) {
        // precision interval
        double confidenceLevel = GILPSettings.CONFIDENCE_LEVEL;

        final double alpha = (1.0 - confidenceLevel) / 2;
        final NormalDistribution normalDistribution = new NormalDistribution();
        final double z = normalDistribution.inverseCumulativeProbability(1 - alpha);
        final double zSquared = FastMath.pow(z, 2);
        final double mean = (double) numberOfSuccesses / (double) numberOfTrials;

        final double factor = 1.0 / (1 + (1.0 / numberOfTrials) * zSquared);
        final double modifiedSuccessRatio = mean + (1.0 / (2 * numberOfTrials)) * zSquared;
        final double difference = z * FastMath.sqrt(
                1.0 / numberOfTrials * mean * (1 - mean) + (1.0 / (4 * FastMath.pow(numberOfTrials, 2)) * zSquared));

        final double lowerBound = factor * (modifiedSuccessRatio - difference);
        final double upperBound = factor * (modifiedSuccessRatio + difference);
        return new ConfidenceInterval(lowerBound, upperBound, confidenceLevel);
    }

    public KVPair<HashSet<String>, Values> heuristicEvaluation(HashMap<String, Double> objects, Boolean decision) throws IOException {
        KVPair<HashSet<String>, Values> filterObjects = new KVPair<HashSet<String>, Values>();
        HashSet<String>  sameTypes = RuleLearnerHelper.readTypes("./data/dbpedia-type/country-type/sameTypes.txt");
        HashSet<String>  webTypes = RuleLearnerHelper.readTypes("./data/dbpedia-type/country-type/webDifferTypes.txt");
        HashSet<String>  rdfTypes = RuleLearnerHelper.readTypes("./data/dbpedia-type/country-type/rdfDifferTypes.txt");
//        Writer writer1 = new OutputStreamWriter(new FileOutputStream("/home/wy/Desktop/10-18/test.txt"),
//                Charset.forName("UTF-8"));
        double trueSize = 0;
        double fpSize = 0;
        double fnSize = 0;

        double allNumber = 0;
        HashSet<String> allObjects = new HashSet<>();
        for (String k : objects.keySet()) {
            allNumber = allNumber + objects.get(k);
            String[] bits = k.split("http://dbpedia.org/resource/");
            String lastOne = bits[bits.length - 1];
            allObjects.add(lastOne);
            //    writer1.write(k+"\t"+lastOne+"\n");
        }
        System.out.println("allnumber: " + allNumber + "\n");
        //  writer1.close();
        //  HashSet<String> objectsEntities = new HashSet<>(allObjects);

        HashSet<String> tp = new HashSet<>(sameTypes);
        tp.retainAll(allObjects);
        if (!tp.isEmpty()) {
            for (String k : tp) {
                k = "http://dbpedia.org/resource/" + k;
                trueSize = (double) (trueSize + objects.get(k));
            }
        }
        //   System.out.println("trueSize: "+trueSize+"\n");

        HashSet<String> fp = new HashSet<>(rdfTypes);
        fp.retainAll(allObjects);
        if (!fp.isEmpty()) {
            for (String k : fp) {
                k = "http://dbpedia.org/resource/" + k;
                fpSize = (double) (fpSize + objects.get(k));
            }
        }
        //   System.out.println("fpSize: "+fpSize+"\n");
        HashSet<String> fn = new HashSet<>(webTypes);
        fn.retainAll(allObjects);
        if (!fn.isEmpty()) {
            for (String k : fn) {
                k = "http://dbpedia.org/resource/" + k;
                fnSize = (double) (fnSize + objects.get(k));
            }
        }
        //   System.out.println("fnSize: "+fnSize+"\n");
        double tnNumber = (double) allNumber - trueSize - fpSize - fnSize;

        Values measure;
        if (decision) {
            measure = new Values(trueSize, fpSize, tnNumber, fnSize);

        } else {
            measure = new Values(tnNumber, fnSize, trueSize, fpSize);
        }
        filterObjects.put(tp, measure);


        //double coverage=

//        for(String obj: objects.keySet()){
//
//      //      String sql = "select ?type where {<" + obj + "> a ?type. filter( ?type=<" + maxObjectType + ">)}";
//      //      System.out.println("sql:"+sql+"\n");
//          //  HashSet<String> types = new RDF3XEngine().getDistinctEntity(sql);
//        //    HashSet<String> types = new Sparql().getSingleVariable(sql);
//            if(!objectTypes.contains(obj)){
//         //   if (types.isEmpty()) {
//             //   objTypes.put(obj,false);
//                negative=negative+objects.get(obj);
//                objects_false.add(obj);
//            } else {
//             //   objTypes.put(obj,true);
//                correction=correction+objects.get(obj);
//                objects_true.add(obj);
//            }
//
//        }
//       double  sum=correction+negative;
//


        return filterObjects;
    }

    public Double heuristicSubjectEvaluation(HashMap<String, Double> subjects, Boolean decision) {
        double precision = 0.0;
        double correction = 0.0;
        double negative = 0.0;

        for (String sub : subjects.keySet()) {
            HashSet<String> types =new HashSet<>();
            String sql = "select ?type where {<" + sub + "> <" + Property.PREDICATE_NAME + ">  ?obj. ?obj a ?type.filter( ?type=<" + Property.DOMAIN + ">)}";
            if(ILPLearnSettings.condition==1){
                types = new RDF3XEngine().getDistinctEntity(sql);
            }else
                types = new Sparql().getSingleVariable(sql);
            if (types.isEmpty()) {
                //   objTypes.put(obj,false);
                negative = negative + subjects.get(sub);
            } else {
                //   objTypes.put(obj,true);
                correction = correction + subjects.get(sub);
            }
        }
        double sum = correction + negative;

        if (decision) {
            precision = correction / sum;
        } else {
            precision = negative / sum;
        }
        return precision;
    }


}
