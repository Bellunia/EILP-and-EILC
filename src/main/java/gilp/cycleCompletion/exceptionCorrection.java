package gilp.cycleCompletion;


import gilp.GILLearn_correction.ILPLearnSettings;

import gilp.GILLearn_correction.Property;
import gilp.GILLearn_correction.RulesOthers;
import gilp.rdf3x.RDF3XEngine;

import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import javatools.parsers.NumberFormatter;

import java.util.*;

public class exceptionCorrection {

    private static Map<Rule, Set<String>> rule2ExceptionSet;

    static {
        rule2ExceptionSet = new HashMap<>();
    }

    public HashSet<String> getTypeSetFromEntity(String entity) {//delete < and >
        //rdf:type
        HashSet<String> types = new HashSet<>();
        //in dbpedia type="http://www.w3.org/1999/02/22@@rdf@@syntax@@ns#type"  remember replacing  @@
        String query = "select distinct ?t where{<" + entity + "> a ?t.}";

        if (ILPLearnSettings.condition == 1) {
            types = new RDF3XEngine().getDistinctEntity(query);
        } else
            types = new Sparql().getSingleVariable(query);

        return types;
    }

    //-------------------------get abnormal triples in fixed predicate
    boolean isVariable(String val) {
        return val.startsWith("?");
    }

    public Set<String> getAllAbnormalExampleInHead(Rule r) {
        // given predicate, fixed predicate names
        // normalExamples.add(x + "\t" + z);
        String sub = r.get_head().getSubject();
        String obj = r.get_head().getObject();

        StringBuffer sb = new StringBuffer();
        Clause clauseBody = r.get_body();

        HashSet<String> abnormalAllExamples = new HashSet<>();
        if (isVariable(sub) && isVariable(obj)) {
            sb.append("select ?").append(sub).append(" ?").append(obj).append(" where{");

            StringBuffer sb2 = new GetSparql().buildStringBufferIndbpedia(clauseBody, sb);

            HashSet<String[]> getAll2 = new RDF3XEngine().getMultipleElements(sb2.toString(), 2);
            for (String[] element : getAll2)
                abnormalAllExamples.add(element[0] + "\t" + element[1]);

        } else if (isVariable(sub) && !isVariable(obj)) {
            sb.append("select ?").append(sub).append(" where{");//<?sub, constants>

            StringBuffer sb2 = new GetSparql().buildStringBufferIndbpedia(clauseBody, sb);

            HashSet<String> subs2 = new RDF3XEngine().getDistinctEntity(sb2.toString());

            for (String element : subs2) {
                String[] items = new String[2];
                items[0] = element;
                //   items[1] = obj;
                abnormalAllExamples.add(items[0] + "\t" + obj);
            }
        }

        return abnormalAllExamples;

    }

    //    public HashSet<String> getAbnormalExampleInHead(Rule r) {
//
//        HashSet<String> allAbnormal= getAllAbnormalExampleInHead(r);
//        Set<String> normalExamples = getNormalExampleInHead(r);
//
//        HashSet<String> abnormalExamples= new HashSet<>(allAbnormal);
//
//        abnormalExamples.removeAll(normalExamples);
//
//        return abnormalExamples;
//    }
//    public HashSet<Triple> getSameNormalPredicateInHead(Rule r) {
//        //for example: nationality<x,y> and birthplace<x,y> ,and pair of <x,y> are same.
//        HashSet<Triple> sameTriplesInHead= new HashSet<>();
//        Set<String> normalExamples = getNormalExampleInHead(r);
//
//        for(String pair: normalExamples){
//
//            String[] elements = pair.split("\t");
//            String sub=elements[0];
//            String obj= elements[1];
//            HashSet<String> predicateSets= getPSetFromXY(sub, obj);
//            predicateSets.remove(Property.PREDICATE_NAME);
//            for(String pre : predicateSets) {
//                Triple t = new Triple(sub, pre, obj);
//                sameTriplesInHead.add(t);
//            }
//        }
//
//        return sameTriplesInHead;
//    }
    public Set<String> getNormalExampleInHead(Rule r) {
        // given predicate, fixed predicate names
        // normalExamples.add(x + "\t" + z);
        Set<String> normalExamples = new HashSet<>();

        String sub = r.get_head().getSubject();
        String obj = r.get_head().getObject();

        Clause clause = r.getCorrespondingClause();

        StringBuffer sb = new StringBuffer();

        if (isVariable(sub) && isVariable(obj)) {
            sb.append("select ?").append(sub).append(" ?").append(obj).append(" where{");
            StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);
            String query = sb1.toString();
            HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, 2);
            for (String[] element : getAll) {

                normalExamples.add(element[0] + "\t" + element[1]);
            }

        } else if (isVariable(sub) && !isVariable(obj)) {
            sb.append("select ?").append(sub).append(" where{");//<?sub, constants>
            StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);
            String query = sb1.toString();

            HashSet<String> subs = new RDF3XEngine().getDistinctEntity(query);

            for (String element : subs) {
                String[] items = new String[2];
                items[0] = element;
                //  items[1] = obj;
                //  normalExamples.add(items);
                normalExamples.add(items[0] + "\t" + obj);
            }
        }
        return normalExamples;

    }

    //------------------"Done with normal and abnormal sets"
    public HashSet<String> getPSetFromXY(String subject, String object) {
        //rdf:type
        HashSet<String> types = new HashSet<>();
        //in dbpedia type="http://www.w3.org/1999/02/22@@rdf@@syntax@@ns#type"  remember replacing  @@
        String query = "select distinct ?p where{<" + subject + "> ?p <" + object + "> .}";

        if (ILPLearnSettings.condition == 1) {
            types = new RDF3XEngine().getDistinctEntity(query);
        } else
            types = new Sparql().getSingleVariable(query);

        return types;
    }

    public Set<String> getAbnormalPredicatesInHead(Rule r) {
        //EWS---predicate_type
        //        type = "0"; //exception_type(?sub)
        //               type = "1";//exception_type（？obj)
        //                type = "2";//exception_predicate(?sub,?obj)

        Set<String> abnormalExamples = getAllAbnormalExampleInHead(r);

        Set<String> normalExamples = getNormalExampleInHead(r);

        Set<String> exceptionSet = new HashSet<>();
        for (int i = 0; i < 3; ++i) {
//            String type = null;
//            if (i == 0) {
//                type = "0"; //exception_type(?sub)
//            } else if (i == 1) {
//                type = "1";//exception_type（？obj)
//            } else {
//                type = "2";//exception_predicate(?sub,?obj)
//            }
            Set<String> textExceptionSet = new HashSet<>();
            if (abnormalExamples != null) {
                for (String pair : abnormalExamples) {
                    String[] parts = pair.split("\t");
                    Set<String> positiveExceptionSet = null;
                    if (i < 2) {//HashSet<String> getTypeSetFromEntity(String entity)
                        positiveExceptionSet = getTypeSetFromEntity(parts[i]);
                    } else {
                        positiveExceptionSet = getPSetFromXY(parts[0], parts[1]);
                    }
                    if (positiveExceptionSet == null) {
                        continue;
                    }
                    textExceptionSet.addAll(positiveExceptionSet);
                }
            }
            if (normalExamples != null) {
                for (String pair : normalExamples) {
                    String[] parts = pair.split("\t");
                    Set<String> negativeExceptionSet = null;
                    if (i < 2) {
                        negativeExceptionSet = getTypeSetFromEntity(parts[i]);
                    } else {
                        negativeExceptionSet = getPSetFromXY(parts[0], parts[1]);
                    }
                    if (negativeExceptionSet == null) {
                        continue;
                    }
                    textExceptionSet.removeAll(negativeExceptionSet);
                }
            }
            for (String textException : textExceptionSet) {
                exceptionSet.add(textException + "\t" + i);
            }
        }
        rule2ExceptionSet.put(r, exceptionSet);

        // HashSet<Triple> sameTriplesInHead= getSameNormalPredicateInHead( r);

        //  abnormalTriplesInHead.addAll(sameTriplesInHead);

        return exceptionSet;
    }
//--------------------finish get abnormal triples in fixed predicate

    public HashSet<Rule> getExceptionRule(Rule r) {
        HashSet<Rule> exceptionRules= new HashSet<>();
        Set<String> exceptionPredicates = getAbnormalPredicatesInHead(r);

        for (String pair : exceptionPredicates) {

            String[] parts = pair.split("\t");
            String exception = parts[0];
            int type = Integer.parseInt(parts[1]);
            Clause body = r.get_body();

            if (type < 2) {
                //   type = "0"; //exception_type(?sub)
                //  type = "1";//exception_type（？obj)
                body.addPredicate(new RDFPredicate(r.get_head().getSubject(), "not_" + "type", exception));
                body.addPredicate(new RDFPredicate(r.get_head().getObject(), "not_" + "type", exception));
            } else {
                //   type = "2";//exception_predicate(?sub,?obj)
                body.addPredicate(new RDFPredicate(r.get_head().getSubject(), "not_" + exception, r.get_head().getObject()));
            }
            Rule newRule = new Rule();
            newRule.set_head(r.get_head());
            newRule.set_body(body);
            exceptionRules.add(newRule);
        }
        return exceptionRules;
    }

    public  HashSet<Rule> getExceptionRulesFromFeedback(Rule r,Boolean decision) {
// type:wikidata:Q6256
        HashSet<Rule> exceptionRules= new HashSet<>();
        Clause body = r.get_body();
        Clause body1 = r.get_body();

        if(decision){//positive prediction
                body.addPredicate(new RDFPredicate(r.get_head().getObject(),  "rdf:type", Property.rangePropertyInwikiData ));
                body1.addPredicate(new RDFPredicate(r.get_head().getObject(),  "not_" + "rdf:type", Property.rangeNegativePropertyInwikiData ));
        }else{//false
            body.addPredicate(new RDFPredicate(r.get_head().getObject(),  "not_" +"rdf:type", Property.rangePropertyInwikiData ));
            body1.addPredicate(new RDFPredicate(r.get_head().getObject(),   "rdf:type", Property.rangeNegativePropertyInwikiData ));
        }

        Rule newRule = new Rule();
        newRule.set_head(r.get_head());
        newRule.set_body(body);

        Rule newRule1 = new Rule();
        newRule1.set_head(r.get_head());
        newRule1.set_body(body1);
        exceptionRules.add(newRule);
        exceptionRules.add(newRule1);

        return exceptionRules;
    }

    public  HashSet<Rule> getExceptionRulesFromRule(Rule positiveRule,Rule negativeRule) {
        Rule rdfRule = new Rule();

        Clause bodyPos = positiveRule.get_body();

        Clause bodyNeg = negativeRule.get_body();

        Clause clause = positiveRule.getCorrespondingClause();
        String sub = positiveRule.get_head().getSubject();
        String obj = positiveRule.get_head().getObject();

        ArrayList<RDFPredicate> myIter = negativeRule.get_body().getIterator();
        //String newEntity = sparqlInExtendTriple(subject);

        Rule newRule1 = new Rule();
        newRule1.set_head(positiveRule.get_head());

        if(!bodyPos.equals(bodyNeg)){




            //????????
        }


        for (RDFPredicate tp : myIter) {
            if(bodyPos.containPredicate(tp)){
                newRule1.set_body(bodyPos);
            }

        }


        return null;
    }

    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();


        int number = 100;
        //   new Exception_GILP().filterAllRules(number);
        int iterationTimes = 1;
        //  new Exception_GILP().analysisAllRules();
        //  new Exception_GILP().validateGILPRules();

        //  new Exception_GILP().  simpleAllRules();


        String originalPath = "./prediction/amie-exception/roughPositiveRules-1.tsv";
        String amiePath = "./prediction/newSearchSpace/rules/positive/amieRules-1-true.tsv";
        String finalPath = "./prediction/amie-exception/exception/rough-positive-exception-1.tsv";


        //     new Exception_GILP().  analysisRoughRules();
//        String writeOutPath="./prediction/newSearchSpace/analysis/rough-negative-1.tsv";
//        String readRulePath="./prediction/newSearchSpace/analysis/sameRules.tsv";
//        new Exception_GILP(). analysisSingleRules(true, 1, writeOutPath, readRulePath);

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

    }

    public static Set<String> getExceptionCandidateSet(Rule rule) {
        return rule2ExceptionSet.get(rule);
    }
}
