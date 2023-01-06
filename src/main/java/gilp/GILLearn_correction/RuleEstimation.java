package gilp.GILLearn_correction;

import amie.mining.AMIE;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import javatools.administrative.Announce;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.*;

public class RuleEstimation {

    public void estimationRules(int iterationTimes) throws Exception {
        ArrayList<Rule> negativeRules = getTestRules(false,iterationTimes);
        ArrayList<Rule> positiveRules = getTestRules(true,iterationTimes);
        int label = 1;
        if (!negativeRules.isEmpty()) {
            for (Rule r : negativeRules) {
                correctionSingleRule( r,positiveRules,label);
                label++;
            }
        }
    }

    public void correctionSingleRule(Rule r, ArrayList<Rule> positiveRules, int label) throws IOException {
      //  oneProperty=  Property.samePropertyOfRelation; "http://dbpedia.org/ontology/country"
        String headObject = r.get_head().getObject();
        ArrayList<String> allQueries = correctRuleQueries(r, positiveRules);
        ArrayList<String> allRefineQueries = refineRuleQueries(allQueries);


        HashMap<String, HashMap<Integer, String>> resultsInAllQueries = new HashMap<>();
        if (headObject.startsWith("?")) {
//<<?a,?b>, <?b1,?b2,?b3>>
            HashMap<String[], String[]> allQueriesResults = realizeVariableQuery(r, allQueries, 3);
            HashMap<String[], String[]> allRefineResults = realizeVariableQuery(r, allRefineQueries, 4);
            estimation(r, headObject, allQueriesResults, label);
            estimation(r, headObject, allRefineResults, label * 1000);
        } else {
            //<?a, ?b1,?b2> + head-object || owl:sameAs results
            HashMap<String, String[]> allQueriesResults = realizeQuery(r, allQueries, 2);
            HashMap<String, String[]> allRefineResults = realizeQuery(r, allRefineQueries, 3);
            estimationConstants(r, headObject, allQueriesResults, label);
            estimationConstants(r, headObject, allRefineResults, label * 1000);
        }


    }

    public ArrayList<String> refineRuleQueries(ArrayList<String> allQueries) {
        //String oneProperty = dbo:country
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

            String newString = "{?" + var + " a <" + Property.RANGE + ">} union {?" + var + " <" + Property.samePropertyOfRelation + "> ?" + newVar + "}";
            sb1.insert(index + 1, newString);

            allRefineQueries.add(sb1.toString());
            System.out.println(sb1.toString());
        }


        return allRefineQueries;
    }
    private RDFPredicate replaceRDFVariables(RDFPredicate rdf, String oldVar, String newVar) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        String subject = rdf.getSubject();
        String predicate = rdf.getPredicateName();
        String object = rdf.getObject();

        if (subject.equals(oldVar))
            subject = newVar;
        else
            subject = subject + "1";

        if (object.equals(oldVar))
            object = newVar;
        else
            object = object + "1";

        subject =  RulesOthers.simpleReplace(subject);
        object =RulesOthers.simpleReplace(object);
        predicate = RulesOthers.simpleReplace(predicate);

        rdfPredicate.setPredicateName(predicate);
        rdfPredicate.setSubject(subject);
        rdfPredicate.setObject(object);
        return rdfPredicate;
    }

    public ArrayList<String> correctRuleQueries(Rule r, ArrayList<Rule> positiveRules) {
        //String oneProperty = dbo:country
        ArrayList<String> allQueries = new ArrayList<>();

        String subject = r.get_head().getSubject();
        String object = r.get_head().getObject();

        if (object.startsWith("?")) {

            for (Rule posRule : positiveRules) {

                Clause clause = r.getCorrespondingClause();

                String sub = posRule.get_head().getSubject();
                String obj = posRule.get_head().getObject();

                ArrayList<RDFPredicate> myIter1 = posRule.get_body().getIterator();
                //String newEntity = sparqlInExtendTriple(subject);
                for (RDFPredicate tp : myIter1) {
                    clause.addPredicate(replaceRDFVariables(tp, sub, subject));
                }

                StringBuffer sb = new StringBuffer();

                sb.append(" select distinct ")
                        .append(subject).append(" ").append(object).append(" ").append(obj).append("1")
                        .append(" where {");

                StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);

                allQueries.add(sb1.toString());
                System.out.println(sb1.toString());
            }
        } else {
            for (Rule posRule : positiveRules) {

                Clause clause = r.getCorrespondingClause();

                String sub = posRule.get_head().getSubject();
                String obj = posRule.get_head().getObject();

                ArrayList<RDFPredicate> myIter1 = posRule.get_body().getIterator();
                //String newEntity = sparqlInExtendTriple(subject);
                for (RDFPredicate tp : myIter1) {
                    clause.addPredicate(replaceRDFVariables(tp, sub, subject));
                }


                StringBuffer sb = new StringBuffer();
                sb.append(" select distinct ")
                        .append(subject).append(" ").append(obj).append("1")
                        .append(" where {");

                StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);

                allQueries.add(sb1.toString());
                System.out.println(sb1.toString());
            }

        }
        return allQueries;
    }


    public HashSet<ArrayList<String>> estimationConstants(Rule r, String headObject, HashMap<String, String[]> allQueriesResults, int label) throws IOException {
        HashSet<ArrayList<String>> correctionPairs = new HashSet<>();

     //   String correction = new CorrectionInDBPedia().correctErrors(headObject, range);
        for (String subject : allQueriesResults.keySet()) {
            ArrayList<String> pairs = new ArrayList<>();
            pairs.add(subject);
            pairs.add(headObject);
          //  pairs.add(correction);
            String[] values = allQueriesResults.get(subject);
            pairs.addAll(Arrays.asList(values));
            correctionPairs.add(pairs);
        }

        Writer writer1 = new OutputStreamWriter(new FileOutputStream("./data/RuleCorrections/experiments-result/rules-head-constants-" + label + "-.txt"),
                Charset.forName("UTF-8"));
        writer1.write(r.toString() + "\n");

        writer1.write("subject\terrors\t corrections\t var1\tvar2\tvar3\t var4\tvar5\t" + "\n");
        int id = 1;

        for (ArrayList<String> KEY : correctionPairs) {
            writer1.write(id + "\t");
            for (String ke : KEY)
                writer1.write(ke + "\t");

            id++;
            writer1.write("\n");
        }


        writer1.close();
        return correctionPairs;
    }


    public HashSet<ArrayList<String>> estimation(Rule r, String headObject, HashMap<String[], String[]> allQueriesResults,int label) throws IOException {
        HashSet<ArrayList<String>> correctionPairs = new HashSet<>();

        // object is variable
        String sparql = new GetSparql().ruleToSparqlIndbpedia(r, headObject);//object is variable.

        HashSet<String> objects = new RDF3XEngine().getDistinctEntity(sparql);
        HashMap<String, String> pairs = new HashMap<>();

//        if (objects != null) {
//            for (String obj : objects) {
//                String objCorrection = new CorrectionInDBPedia().correctErrors(obj, range);
//                pairs.put(obj, objCorrection);
//            }
//        }

        for (String[] subject : allQueriesResults.keySet()) {
            ArrayList<String> allResults = new ArrayList<>();
            allResults.add(subject[0]);
            allResults.add(subject[1]);

         //   String objCorrection =pairs.get(subject[1]);
          //  allResults.add(objCorrection);

            String[] values = allQueriesResults.get(subject);
            allResults.addAll(Arrays.asList(values));

            correctionPairs.add(allResults);
        }


        Writer writer1 = new OutputStreamWriter(new FileOutputStream("./data/RuleCorrections/experiments-result/rules-head-variable-" + label + "-"),
                Charset.forName("UTF-8"));
        int id = 1;
        writer1.write(r.toString() + "\n");

        writer1.write("subject\terrors\tcorrections\t var1\tvar2\tvar3\t var4\tvar5\t \t" + "\n");

        for (ArrayList<String> KEY : correctionPairs) {
            writer1.write(id + "\t");
            for (String ke : KEY)
                writer1.write(ke + "\t");
            id++;
            writer1.write("\n");
        }


        writer1.close();
        return correctionPairs;
    }

    public HashMap<String[], String[]> realizeVariableQuery(Rule r, ArrayList<String> allQueries, int number)  {
        //define the triple<subject,errorObject,correctObject>
        //count the variables , when the head's object is constants.
        //   HashSet<String> allSubjects = new RuleLearnerHelper().ruleToSubjects(r);
        HashMap<String[], String[]> resultsInAllQueries = new HashMap<>();

        Clause clause = r.getCorrespondingClause();

        StringBuffer sb = new StringBuffer();

        sb.append(" select distinct ")
                .append(r.get_head().getSubject()).append(" ").append(r.get_head().getObject())
                .append(" where {");

        String sparql = new GetSparql().buildStringBuffer(clause, sb).toString();

        HashSet<String[]> getOriginals = new RDF3XEngine().getMultipleElements(sparql, 2);
        int size = allQueries.size();
        String[] values = new String[size];
        for (String[] elements1 : getOriginals) {
            resultsInAllQueries.put(elements1, values);
        }

        int i = 0;
        for (String query : allQueries) {
            HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, number);
            if(!getAll.isEmpty()) {
                for (String[] elements : getAll) {
                    String[] pair = new String[2];
                    pair[0] = elements[0];
                    pair[1] = elements[1];
                    String objCorrect = elements[number - 1];
                    String[] single;
                    if (!resultsInAllQueries.containsKey(pair)) {
                        single = new String[size];


                    } else {
                        single = resultsInAllQueries.get(pair);
                    }
                    single[i] = objCorrect;
                    resultsInAllQueries.put(pair, single);

                }
            }
            i++;
        }

        return resultsInAllQueries;
    }


    public HashMap<String, String[]> realizeQuery(Rule r, ArrayList<String> allQueries, int number)  {
        //define the triple<subject,errorObject,correctObject>
        //count the variables , when the head's object is constants.
        //String[] the number is the allQueries.size
        HashSet<String> allSubjects = new RuleLearnerHelper().ruleToSubjects(r);
        int size = allQueries.size();
        HashMap<String, String[]> resultsInAllQueries = new HashMap<>();
        String[] values = new String[size];
        for (String key : allSubjects) {
            resultsInAllQueries.put(key, values);
        }
        int i = 0;
        for (String query : allQueries) {
            HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, number);
            if(getAll !=null ) {
                for (String[] elements : getAll) {
                    String sub = elements[0];
                    String objCorrect = elements[number - 1];
                    String[] single;
                    if (!resultsInAllQueries.containsKey(sub)) {
                        single = new String[size];

                    } else {
                        single = resultsInAllQueries.get(sub);
                    }
                    single[i] = objCorrect;
                    resultsInAllQueries.put(sub, single);

                }
            }
            i++;
        }

        return resultsInAllQueries;
    }



    public ArrayList<Rule> getTestRules(Boolean decision,int iterationTimes) throws Exception {

        ArrayList<Rule> gilpRules = new ArrayList<>();
        String[]   paras= new ILPLearnSettings().parameters(decision,iterationTimes);
        AMIE miner = AMIE.getInstance(paras);
        Announce.doing("\n Starting the mining phase \n");
        List<amie.rules.Rule> rules = miner.mine();

        Announce.done("\n finish the mining phase \n");

        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./data/RuleCorrections/experiments-result/rules-" + "-" + decision + ".tsv"),
                Charset.forName("UTF-8"));

        HashMap<amie.rules.Rule, Double> filterRulesSort = new HashMap<amie.rules.Rule, Double>();
        HashMap<amie.rules.Rule, Double> filterRules = new HashMap<amie.rules.Rule, Double>();
        List<String> finalHeaders = new ArrayList<>(headers);

        writer2.write(String.join("\t", finalHeaders) + "\n");
        if (!rules.isEmpty()) {
            for (amie.rules.Rule rule : rules) {

                int ruleLength = rule.getLength();
                if (ruleLength == 3) {
                    filterRules.put(rule, rule.getPcaConfidence());
                    //   writer2.write(rule.getFullRuleString().toString().replaceAll("@@", "-").replaceAll("##", "–") + "\n");
                }
            }
        }

        filterRulesSort = new RuleLearnerHelper().reverseOrderByValue(filterRules);
        ArrayList<amie.rules.Rule> finalRules = new ArrayList<>(filterRulesSort.keySet());
        int k = 0;
        for (amie.rules.Rule rule : finalRules) {
            Rule rdfRule = new RulesOthers().transformRule(rule);
            gilpRules.add(rdfRule);
            writer2.write(rule.getFullRuleString().toString().replaceAll("@@", "-").replaceAll("##", "–") + "\n");
        }
        writer2.close();

        return gilpRules;

    }

    public static final List<String> headers = Arrays.asList("Rule", "Head Coverage", "Std Confidence",
            "PCA Confidence", "Positive Examples", "Body size", "PCA Body size",
            "Functional variable", "Std. Lower Bound", "PCA Lower Bound", "PCA Conf estimation");





}
