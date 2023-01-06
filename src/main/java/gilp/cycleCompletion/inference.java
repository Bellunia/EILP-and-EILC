package gilp.cycleCompletion;

import gilp.GILLearn_correction.Property;
import gilp.GILLearn_correction.RulesOthers;
import gilp.comments.AnnotatedTriple;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import org.apache.commons.cli.ParseException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static gilp.main.GILP_OWA_DBPedia.readAmieRule;

public class inference {

    public HashMap<String, String> inferencePositiveFactsFromRules() throws IOException {
        String path = "/home/wy/Desktop/test-22/positive-final-rules.txt";

        ArrayList<Rule> positiveRules = readAmieRule(path);
        ArrayList<String> allQueries = new ArrayList<>();
        HashMap<String, String> queryAndName = new HashMap<>();

        HashSet<String> countries = RuleLearnerHelper.readTypes("./data/conflict feedback/nationality/Country.txt");
        HashSet<String> countriesWikidata = RuleLearnerHelper.readTypes("./data/conflict feedback/nationality/wikidata-6256.txt");
        HashMap<Triple, Integer> measure = new HashMap<>();

        HashMap<String, Integer> verify = new HashMap<>();

        int i = 1;
        for (Rule r : positiveRules) {


            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("/home/wy/Desktop/test-22/positive-new/test" + i + ".tsv"),
                    StandardCharsets.UTF_8);
            String targetPredicate = r.get_head().getPredicateName();
            Clause clause = r.get_body();
            String sub = r.get_head().getSubject();
            String obj = r.get_head().getObject();

            StringBuffer sb = new StringBuffer();
            sb.append(" select ")
                    .append(sub).append(" ")
                    .append(obj)
                    .append(" where {");
            //    StringBuffer sb1 = optionBodyBufferIndbpedia(clause, sb, r.get_head());

            StringBuffer sb1 = buildBodyBufferIndbpedia(clause, sb, r.get_head());

            allQueries.add(sb1.toString());
            System.out.println(sb1);
            queryAndName.put(sb1.toString(), targetPredicate);


//            HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(sb1.toString(), 2);
//            for (String[] elements : getAll) {
//                Triple tri;
//                Triple triCorrection= new Triple(elements[0], targetPredicate, elements[1]);
//                        if(! verify.containsKey(elements[1])) {
//
//                            if (countriesWikidata.contains(elements[1])) {
//                                measure.put(triCorrection, 1);
//                                verify.put(elements[1],1);
//                            } else if (countries.contains(elements[1])) {
//                                measure.put(triCorrection, 0);
//                                verify.put(elements[1],0);
//
//                            } else {
//                                measure.put(triCorrection, -1);
//                                verify.put(elements[1],-1);
//                            }
//                        }
//                        else{
//                            measure.put(triCorrection, verify.get(elements[1]));
//                        }
//            }


//-------------------------online
            ArrayList<HashMap<String, String>> items = new Sparql().getResultsFromQuery(sb1.toString());
            HashSet<Triple> randomTriple = new HashSet<>();
            if (items != null) {
                for (HashMap<String, String> key : items) {
                    String item = key.get(sub.replace("?", ""));
                    String entity = key.get(obj.replace("?", ""));
                    Triple newTriple = new Triple(item, targetPredicate, entity);
                    randomTriple.add(newTriple);
                    if (!verify.containsKey(entity)) {

                        if (countriesWikidata.contains(entity)) {
                            //     measure.put(newTriple, 1);
                            verify.put(entity, 1);
                            writer.write(newTriple + "\t" + 1 + "\n");
                        } else if (countries.contains(entity)) {
                            //     measure.put(newTriple, 0);
                            verify.put(entity, 0);
                            writer.write(newTriple + "\t" + 0 + "\n");

                        } else {
                            //    measure.put(newTriple, -1);
                            verify.put(entity, -1);
                            writer.write(newTriple + "\t" + -1 + "\n");
                        }
                    } else {
                        //    measure.put(newTriple, verify.get(entity));
                        writer.write(newTriple + "\t" + verify.get(entity) + "\n");
                    }

                }
            }
            writer.close();
            System.out.println("next--" + i++);
        }

//        for(Triple t: measure.keySet()){
//            writer.write(t+"\t"+measure.get(t)+"\n");
//
//        }


        return queryAndName;
    }


    public HashMap<String, String> inferenceNegativeFactsFromRules() throws IOException {
        String path = "./prediction/negative-final-rules.txt";

        ArrayList<Rule> negativeRules = readAmieRule(path);
        ArrayList<String> allQueries = new ArrayList<>();
        HashMap<String, String> queryAndName = new HashMap<>();

        HashSet<String> countries = RuleLearnerHelper.readTypes("./data/conflict feedback/nationality/Country.txt");
        HashSet<String> countriesWikidata = RuleLearnerHelper.readTypes("./data/conflict feedback/nationality/wikidata-6256.txt");
        HashMap<Triple, Integer> measure = new HashMap<>();

        HashMap<String, Integer> verify = new HashMap<>();

        int i = 1;
        for (Rule r : negativeRules) {

            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("./prediction/negative-new/test--" + i + ".tsv"),
                    StandardCharsets.UTF_8);
            String targetPredicate = r.get_head().getPredicateName();
            Clause clause = r.get_body();
            String sub = r.get_head().getSubject();
            String obj = r.get_head().getObject();

            StringBuffer sb = new StringBuffer();
            sb.append(" select ")
                    .append(sub).append(" ")
                    .append(obj)
                    .append(" where {");
            StringBuffer sb1 = optionBodyBufferIndbpedia(clause, sb, r.get_head());

            //     StringBuffer sb1 = buildBodyBufferIndbpedia(clause, sb, r.get_head());

            allQueries.add(sb1.toString());
            System.out.println(sb1);
            queryAndName.put(sb1.toString(), targetPredicate);


//            HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(sb1.toString(), 2);
//            for (String[] elements : getAll) {
//                Triple tri;
//                Triple triCorrection= new Triple(elements[0], targetPredicate, elements[1]);
//                        if(! verify.containsKey(elements[1])) {
//
//                            if (countriesWikidata.contains(elements[1])) {
//                                measure.put(triCorrection, 1);
//                                verify.put(elements[1],1);
//                            } else if (countries.contains(elements[1])) {
//                                measure.put(triCorrection, 0);
//                                verify.put(elements[1],0);
//
//                            } else {
//                                measure.put(triCorrection, -1);
//                                verify.put(elements[1],-1);
//                            }
//                        }
//                        else{
//                            measure.put(triCorrection, verify.get(elements[1]));
//                        }
//            }


//-------------------------online
            ArrayList<HashMap<String, String>> items = new Sparql().getResultsFromQuery(sb1.toString());
            HashSet<Triple> randomTriple = new HashSet<>();
            if (items != null) {
                for (HashMap<String, String> key : items) {
                    String item = key.get(sub.replace("?", ""));
                    String entity = key.get(obj.replace("?", ""));
                    Triple newTriple = new Triple(item, targetPredicate, entity);
                    randomTriple.add(newTriple);
                    if (!verify.containsKey(entity)) {

                        if (countriesWikidata.contains(entity)) {
                            //     measure.put(newTriple, 1);
                            verify.put(entity, 1);
                            writer.write(newTriple + "\t" + 1 + "\n");
                        } else if (countries.contains(entity)) {
                            //     measure.put(newTriple, 0);
                            verify.put(entity, 0);
                            writer.write(newTriple + "\t" + 0 + "\n");

                        } else {
                            //    measure.put(newTriple, -1);
                            verify.put(entity, -1);
                            writer.write(newTriple + "\t" + -1 + "\n");
                        }
                    } else {
                        //    measure.put(newTriple, verify.get(entity));
                        writer.write(newTriple + "\t" + verify.get(entity) + "\n");
                    }

                }
            }
            writer.close();
            System.out.println("next--" + i++);
        }

//        for(Triple t: measure.keySet()){
//            writer.write(t+"\t"+measure.get(t)+"\n");
//
//        }


        return queryAndName;
    }


    public void analysisNewFacts(HashMap<String, String> queryAndName) {
        String path = "";

        for (String query : queryAndName.keySet()) {
            String name = queryAndName.get(query);


        }


    }

    public StringBuffer buildBodyBufferIndbpedia(Clause cls, StringBuffer sb, RDFPredicate head) {
        ArrayList<RDFPredicate> myIter = cls.getIterator();
        //String newEntity = sparqlInExtendTriple(subject);

        for (RDFPredicate tp : myIter) {

            // in our current RDF3x data set, each constant is enclosed by <>
            if (!tp.isSubjectVariable()) {
                //sb.append("<");
                sb.append(new GetSparql().sparqlInExtendTriple(tp.getSubject()));
            } else {
                sb.append(tp.getSubject());
            }
//			if (!tp.isSubjectVariable())
//				sb.append("> ");
//			else
            sb.append(" ");

            if (!tp.getPredicateName().startsWith("?"))
                sb.append("<");
            sb.append(tp.getPredicateName());

            if (!tp.getPredicateName().startsWith("?"))
                sb.append("> ");
            else
                sb.append(" ");

            if (!tp.isObjectVariable()) {
                //sb.append("<");
                sb.append(new GetSparql().sparqlInExtendTriple(tp.getObject()));
            } else {
                sb.append(tp.getObject());
            }
//			if (!tp.isObjectVariable())
//				sb.append(">. ");
//			else
            sb.append(". ");
        }


        sb.append("filter not exists{ ");

        // in our current RDF3x data set, each constant is enclosed by <>
        if (!head.isSubjectVariable()) {
            //sb.append("<");
            sb.append(new GetSparql().sparqlInExtendTriple(head.getSubject()));
        } else {
            sb.append(head.getSubject());
        }
//			if (!tp.isSubjectVariable())
//				sb.append("> ");
//			else

        sb.append(" ");
        if (!head.getPredicateName().startsWith("?")) {
            sb.append("<");
            sb.append(head.getPredicateName());
            sb.append("> ");
        } else
            sb.append(" ");

        if (!head.isObjectVariable()) {
            //sb.append("<");
            sb.append(new GetSparql().sparqlInExtendTriple(head.getObject()));
        } else {
            sb.append(" ");
            sb.append(head.getObject());
        }
//			if (!tp.isObjectVariable())
//				sb.append(">. ");
//			else
        sb.append(".} ");

        sb.append("}");
        return sb;
    }

    public StringBuffer optionBodyBufferIndbpedia(Clause cls, StringBuffer sb, RDFPredicate head) {
        ArrayList<RDFPredicate> myIter = cls.getIterator();
        //String newEntity = sparqlInExtendTriple(subject);

        for (RDFPredicate tp : myIter) {

            // in our current RDF3x data set, each constant is enclosed by <>
            if (!tp.isSubjectVariable()) {
                //sb.append("<");
                sb.append(new GetSparql().sparqlInExtendTriple(tp.getSubject()));
            } else {
                sb.append(tp.getSubject());
            }
//			if (!tp.isSubjectVariable())
//				sb.append("> ");
//			else
            sb.append(" ");

            if (!tp.getPredicateName().startsWith("?"))
                sb.append("<");
            sb.append(tp.getPredicateName());

            if (!tp.getPredicateName().startsWith("?"))
                sb.append("> ");
            else
                sb.append(" ");

            if (!tp.isObjectVariable()) {
                //sb.append("<");
                sb.append(new GetSparql().sparqlInExtendTriple(tp.getObject()));
            } else {
                sb.append(tp.getObject());
            }
//			if (!tp.isObjectVariable())
//				sb.append(">. ");
//			else
            sb.append(". ");
        }


        sb.append(" OPTIONAL { ");

        // in our current RDF3x data set, each constant is enclosed by <>
        if (!head.isSubjectVariable()) {
            //sb.append("<");
            sb.append(new GetSparql().sparqlInExtendTriple(head.getSubject()));
        } else {
            sb.append(head.getSubject());
        }
//			if (!tp.isSubjectVariable())
//				sb.append("> ");
//			else

        sb.append(" ");
        if (!head.getPredicateName().startsWith("?")) {
            sb.append("<");
            sb.append(head.getPredicateName());
            sb.append("> ");
        } else
            sb.append(" ");

        if (!head.isObjectVariable()) {
            //sb.append("<");
            sb.append(new GetSparql().sparqlInExtendTriple(head.getObject()));
        } else {
            sb.append(" ");
            sb.append(head.getObject()).append("1");
        }
//			if (!tp.isObjectVariable())
//				sb.append(">. ");
//			else
        sb.append(".} FILTER (!bound(");
        sb.append(head.getObject()).append("1");

        sb.append(")) }");
        return sb;
    }


    public static void main(String[] args) throws IOException {
        new inference().inferenceNegativeFactsFromRules();
        String test_query = " select ?a ?b where {?a <http://dbpedia.org/ontology/primeMinister> ?f. ?f <http://dbpedia.org/ontology/deathPlace> ?b. \n" +
                "  OPTIONAL { ?a <http://dbpedia.org/ontology/nationality> ?d. } .\n" +
                "         FILTER (!bound(?d))}";
    }
}
