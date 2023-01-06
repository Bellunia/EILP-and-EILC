package gilp.GILLearn_correction;

import amie.data.KB;
import gilp.cycleCompletion.Exception_GILP;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.comments.AnnotatedTriple;
import gilp.comments.Comment;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import gilp.utils.KVPair;
import javatools.datatypes.ByteString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import amie.data.KB;

public class RulesOthers {

    public String getDatalogString(amie.rules.Rule RULE) {
        StringBuilder builder = new StringBuilder();
        builder.append(new amie.rules.Rule().toDatalog(RULE.getHead()));
        builder.append(" <=");
        Iterator var2 = RULE.getBody().iterator();

        while(var2.hasNext()) {
            int[] atom = (int[])var2.next();
            builder.append(" ");
            builder.append(new amie.rules.Rule().toDatalog(atom));
            builder.append(",");
        }

        if (builder.charAt(builder.length() - 1) == ',') {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    public Rule transformRule(amie.rules.Rule rule) {
        Rule rdfRule = new Rule();
        if (!rule.isEmpty()) {
            Clause clause = new Clause();
            for (int[] bs : rule.getBody()) {
                clause.addPredicate(extractComplexRDFPredicate(  bs ));
            }
            rdfRule.set_body(clause);
            RDFPredicate head = extractRDFPredicate(rule.getHead());
            rdfRule.set_head(head);
        }

        return rdfRule;
    }

    public String replaceVariable(String body){
        // String body="?a  <http://dbpedia.org/ontology/stateOfOrigin>  ?g  ?g  <http://www.w3.org/2000/01/rdf@@schema#seeAlso>  ?b";
        String[] split= body.split("  ");
        HashSet<String> splitLabels= new HashSet<>();
        for(String r : split){
            if(r.contains("?") ){
                if(!r.contains("?a") && !r.contains("?b"))
                    splitLabels.add(r);
            }
        }
        char relationVariable = 'b'+1;

        HashMap<String, String> newLabels= new HashMap<>();
        for(String key: splitLabels){

            newLabels.put(key, "?"+relationVariable );
            splitLabels.remove(key);
            relationVariable=(char) (relationVariable + 1);
        }

        for(String key : newLabels.keySet()) {
            body= body.replace(key, newLabels.get(key));
        }
//?a  <http://dbpedia.org/ontology/stateOfOrigin>  ?c  ?c  <http://www.w3.org/2000/01/rdf@@schema#seeAlso>  ?b
        return body;
    }

    public Rule transformAmieRule(String rule) {

        Rule rdfRule = new Rule();
        if (!rule.isEmpty()) {

            String[]  ruleItems= rule.split(" => ");
            String[] headItems= ruleItems[1].split("  ");
            String body= replaceVariable( ruleItems[0]);

            String[] bodyItems= body.split("  ");

            Clause clause = new Clause();
            for (int i=0;i<bodyItems.length/3;i++) {
                RDFPredicate rdfPredicate = new RDFPredicate();
                rdfPredicate.setSubject(simpleReplace(bodyItems[3*i]));
                rdfPredicate.setPredicateName(simpleReplace(bodyItems[3*i+1]));
                rdfPredicate.setObject(simpleReplace(bodyItems[3*i+2]));
                clause.addPredicate(rdfPredicate);
            }
            rdfRule.set_body(clause);

            rdfRule.set_head(extractPredicate(headItems));
        }

        return rdfRule;
    }

    public Rule transformAmieRule2(String rule) {

        Rule rdfRule = new Rule();
        if (!rule.isEmpty()) {

            String[]  ruleItems= rule.split("  ");
            Clause clause = new Clause();
            for (int i=1;i<ruleItems.length/3;i++) {
                RDFPredicate rdfPredicate = new RDFPredicate();
                rdfPredicate.setSubject(simpleReplace(ruleItems[3*i]));
                rdfPredicate.setPredicateName(simpleReplace(ruleItems[3*i+1]));
                rdfPredicate.setObject(simpleReplace(ruleItems[3*i+2]));
                clause.addPredicate(rdfPredicate);
            }
            rdfRule.set_body(clause);

            RDFPredicate rdfPredicate = new RDFPredicate();
            rdfPredicate.setPredicateName( RulesOthers.simpleReplace( ruleItems[1]));
            rdfPredicate.setSubject( RulesOthers.simpleReplace(ruleItems[0]));
            rdfPredicate.setObject( RulesOthers.simpleReplace(ruleItems[2]));

            rdfRule.set_head(rdfPredicate);
        }

        return rdfRule;
    }
    public RDFPredicate extractComplexPredicate(String[] bs) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName(simpleReplace(bs[1]));
        rdfPredicate.setSubject(simpleReplace(bs[0]));
        rdfPredicate.setObject(simpleReplace(bs[2]));
        return rdfPredicate;
    }

    private  RDFPredicate extractPredicate(String[] head) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName( RulesOthers.simpleReplace( head[1]));
        rdfPredicate.setSubject( RulesOthers.simpleReplace(head[0]));
        rdfPredicate.setObject( RulesOthers.simpleReplace(head[2]));
        return rdfPredicate;
    }

    public HashSet<String> ruleToSubjects(Rule rule) {
        HashSet<String> subjects = new HashSet<String>();
        if (!rule.isEmpty()) {
            RDFPredicate newHead = rule.get_head();
            if (!newHead.isSubjectVariable()) {
                subjects.add(newHead.getSubject());
            } else {

                if (ILPLearnSettings.condition == 1) {// 1:rdf 2: dbpedia
                    String sparql = new GetSparql().ruleToSparqlRDF(rule);
                    HashSet<String> subjects1  = new RDF3XEngine().getDistinctEntity(sparql);
                    subjects.addAll(subjects1);
                } else {
                    String sparql = new GetSparql().ruleToSparqlIndbpedia(rule);
                    HashSet<String> subjects1 = new Sparql().getSingleVariable(sparql);
                    subjects.addAll(subjects1);
                }
            }
        }
        return subjects;
    }

    public HashSet<Triple> subjectsToTriples(HashSet<String> subjects) {
        HashSet<Triple> triples = new HashSet<>();
        for (String subject : subjects) {
            String sparql = "select ?o where{ <" + subject + "> <" + Property.PREDICATE_NAME + "> ?o.}";
            HashSet<String> singleObjects = new HashSet<>();
            if(ILPLearnSettings.condition == 1) {
                singleObjects = new RDF3XEngine().getDistinctEntity(sparql);
            }
            else
                singleObjects = new Sparql().getSingleVariable(sparql);

            for (String key : singleObjects) {
                Triple tri = new Triple(subject, Property.PREDICATE_NAME, key);
                triples.add(tri);
            }
        }
        return triples;
    }

    public KVPair<HashSet<Triple>,HashSet<Triple>> objectsToTriples
            (HashSet<String> positivePrediction,HashSet<String>negativePrediction) {

        HashSet<String> selectObjects = new HashSet<>(positivePrediction);
        selectObjects.addAll(negativePrediction);

        HashSet<Triple> pos = new HashSet<Triple>();
        HashSet<Triple> neg = new HashSet<Triple>();
        for (String obj : selectObjects) {
            if (judgeObject(obj)) {
                pos.addAll(objectToTriple(obj));
            } else {
                neg.addAll(objectToTriple(obj));
            }
        }

            return new KVPair<>(pos, neg);
    }

    public KVPair<HashSet<Triple>,HashSet<Triple>> subjectsToTriples(HashSet<String> positivePrediction,HashSet<String> negativePrediction) {
        HashSet<Triple> triplesPos =   subjectsToTriples(positivePrediction);
        HashSet<Triple> triplesNeg =   subjectsToTriples(negativePrediction);
        HashMap<String,Boolean> objectsCriterion =new HashMap<>();

        HashSet<Triple> selectTriples= new HashSet<>(triplesPos);
        selectTriples.addAll(triplesNeg);
        HashSet<Triple> pos= new HashSet<Triple>();
        HashSet<Triple> neg = new HashSet<Triple>();
        for (Triple key : selectTriples) {
            String obj = key.get_obj();
            if(!objectsCriterion.containsKey(obj)) {
                if (judgeObject(obj)) {
                    objectsCriterion.put(obj, true);
                    pos.add(key);
                }
                else {
                    objectsCriterion.put(obj, false);
                    neg.add(key);
                }
            }else{
                if(objectsCriterion.get(obj))
                    pos.add(key);
                else
                    neg.add(key);
            }
        }

        return new KVPair<>(pos,neg);
    }

    public Boolean judgeObject(String obj){
        String sql = "select ?type where {<" + obj + "> a ?type.filter( ?type=<" + Property.RANGE + ">)}";
        HashSet<String> types = new HashSet<>();
        if (ILPLearnSettings.condition == 1)
            types = new RDF3XEngine().getDistinctEntity(sql);
            else
            types = new Sparql().getSingleVariable(sql);
        return types != null;

    }

    public HashSet<String> ruleToObjects(Rule rule) {
        //consider the head's subject is constant.
        HashSet<String> objects = new HashSet<String>();

        if (!rule.isEmpty()) {
            RDFPredicate newHead =  rule.get_head();
            if (!newHead.isObjectVariable()) {
                objects.add(newHead.getObject());
            } else {

                String sparql = null;
                if (!rule.isEmpty()) {
                    Clause clause = rule.getCorrespondingClause();
                    //sparql = new GetSparql().buildSingleSPARQL(clause);
                    StringBuffer sb = new StringBuffer();
                    sb.append(" select distinct ").append(newHead.getObject()).append(" where {");

                    StringBuffer sb1 = new GetSparql().buildStringBufferIndbpedia(clause, sb);

                    sparql = sb1.toString();
                }
                assert sparql != null;
                String  sparql1 = sparql.replace("\"\"","\"");;
                System.out.println("\n" + sparql1);
                HashSet<String> objects1 = new Sparql().getSingleVariable(sparql1);
                objects.addAll(objects1);
            }
        }
        return objects;
    }
    public HashSet<Triple> objectsToTriple(HashSet<String> objects) {

        HashSet<Triple> triples = new HashSet<Triple>();

        for (String obj : objects) {
            String query = "select distinct ?a where{ ?a <" + Property.PREDICATE_NAME + "> <" + obj + ">.}  ORDER BY RAND() LIMIT 100";
            ArrayList<String> targets = new Sparql().getSingleResultsFromQuery(query);
            if (!targets.isEmpty()) {
                Triple tri = new Triple(targets.get(0), Property.PREDICATE_NAME, obj);
                triples.add(tri);
            }
        }
        return triples;
    }

    public HashSet<Triple> objectToTriple(String obj) {

        HashSet<Triple> triples = new HashSet<Triple>();
        String sparql = "select distinct ?a where{ ?a <" + Property.PREDICATE_NAME + "> <" + obj + ">.} ";

        HashSet<String> singleSubjects = new HashSet<>();
        if(ILPLearnSettings.condition == 1)
                singleSubjects = new RDF3XEngine().getDistinctEntity(sparql);
        else
            singleSubjects = new Sparql().getSingleVariable(sparql);

        for (String key : singleSubjects) {
                Triple tri = new Triple(key, Property.PREDICATE_NAME, obj);
                triples.add(tri);
            }
        return triples;
    }
    public static String simpleOtherReplace(String str){
        str= str.replaceAll("<", "").replaceAll(">", "");
        return str;
    }
    public static String simpleReplace(String str){
        str= str.replaceAll("<", "").replaceAll(">", "")
                .replaceAll("@@", "-").replaceAll("##", "–");
        return str;
    }


    public RDFPredicate replaceRDFVariables(RDFPredicate rdf, String oldVar, String newVar) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        String subject = rdf.getSubject();
        String predicate = rdf.getPredicateName();
        String object = rdf.getObject();
        if(subject.contains("?")) {
            if (subject.equals(oldVar))
                subject = newVar;
            else
                subject = subject + "1";
        }

        if(object.contains("?")) {
            if (object.equals(oldVar))
                object = newVar;
            else
                object = object + "1";
        }

        rdfPredicate.setPredicateName(simpleReplace(predicate));
        rdfPredicate.setSubject(simpleReplace(subject));
        rdfPredicate.setObject(simpleReplace(object));
        return rdfPredicate;
    }
    public RDFPredicate replaceVariables(ByteString[] bs, String oldVar, String newVar) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        String subject = bs[0].toString();
        String predicate = bs[1].toString();
        String object = bs[2].toString();

        if (subject.equals(oldVar))
            subject = newVar;
        else
            subject = subject + "1";

        if (object.equals(oldVar))
            object = newVar;
        else
            object = object + "1";

        rdfPredicate.setPredicateName(simpleReplace(predicate));
        rdfPredicate.setSubject(simpleReplace(subject));
        rdfPredicate.setObject(simpleReplace(object));

        return rdfPredicate;
    }

    public RDFPredicate extractRDFPredicate(int[] byteStrings) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName(
                simpleOtherReplace( KB.unmap(byteStrings[1]).replace("<", "").replace(">", "")));
        rdfPredicate.setSubject(simpleOtherReplace(KB.unmap(byteStrings[0])));
        rdfPredicate.setObject(simpleOtherReplace(KB.unmap(byteStrings[2])));
        return rdfPredicate;
    }
    public static RDFPredicate extractComplexRDFPredicate(int[] bs) {
//KB.unmap(atom[1]).replace("<", "").replace(">", "") + "(" + atom[0] + ", " + atom[2] + ")
        //  new amie.rules.Rule().toDatalog(RULE.getHead())
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName(simpleReplace(KB.unmap(bs[1]).replace("<", "").replace(">", "")));
        rdfPredicate.setSubject(simpleReplace(KB.unmap(bs[0])));
        rdfPredicate.setObject(simpleReplace(KB.unmap(bs[2])));
        return rdfPredicate;
    }



    public HashSet<Comment> objectToComments(HashSet<String> objects, Boolean decision) {
        HashSet<Comment> negativeComments = new HashSet<Comment>();
        HashSet<Comment> positiveComments = new HashSet<Comment>();

        for (String obj : objects) {
            String sparql = "select distinct ?a where{ ?a <" + Property.PREDICATE_NAME + "> <" + obj + ">.";
            HashSet<String> getSubjects = new RDF3XEngine().getDistinctEntity(sparql);
            for (String sub : getSubjects) {

                Triple element = new Triple(sub, Property.PREDICATE_NAME, obj);
                if (decision) {
                    positiveComments.add(new Comment(element, true));
                } else {
                    positiveComments.add(new Comment(element, false));
                }
            }
        }
        if (decision) {
            return positiveComments;
        } else {
            return negativeComments;
        }
    }
    public HashSet<Comment> tripleToComment(HashSet<Triple> selectTriples) {
        HashSet<Comment> selectedComments = new HashSet<Comment>();
        for (Triple key : selectTriples) {
            String obj = key.get_obj();
            String sql = "select ?type where {<" + obj + "> a ?type.filter( ?type=<" + Property.RANGE + ">)}";
            HashSet<String> types = new HashSet<>();
            if(ILPLearnSettings.condition ==1){
               types = new RDF3XEngine().getDistinctEntity(sql);
            }else
            types = new Sparql().getSingleVariable(sql);

            if (types.isEmpty()) {
                selectedComments.add(new Comment(key.clone(), false));
            } else {
                selectedComments.add(new Comment(key.clone(), true));
            }
        }
        return selectedComments;
    }

    public HashSet<Comment> tripleLabelled(HashSet<Triple> selectTriples, Boolean decision) {

        HashSet<Comment> selectedComments = new HashSet<>();
        for (Triple key : selectTriples) {
            selectedComments.add(new Comment(key.clone(), decision));
        }
        return selectedComments;
    }
    public HashSet<Triple> filterTripleByType(HashSet<Triple> selectTriples, Boolean decision) {
        HashSet<Triple> selectedPositiveTriples = new HashSet<>();
        HashSet<Triple> selectedNegativeTriples = new HashSet<>();

        for (Triple key : selectTriples) {
            String obj = key.get_obj();
            String sql = "select ?type where {<" + obj + "> a ?type. filter( ?type=<" + Property.RANGE + ">)}";
            HashSet<String> types = new HashSet<>();
            if(ILPLearnSettings.condition ==1){
                types = new RDF3XEngine().getDistinctEntity(sql);
            }else
                types = new Sparql().getSingleVariable(sql);

            if (types.isEmpty()) {
                selectedNegativeTriples.add(key.clone());
            } else {
                selectedPositiveTriples.add(key.clone());
            }
        }
        if (decision)
            return selectedPositiveTriples;
        else
            return selectedNegativeTriples;
    }

    public HashSet<Triple> filterTripleByRangeResults(HashSet<Triple> selectTriples, Boolean decision) {
        HashSet<String> countries =  RuleLearnerHelper.readTypes("./data/conflict feedback/nationality/range.txt");

        HashSet<Triple> selectedPositiveTriples = new HashSet<Triple>();
        HashSet<Triple> selectedNegativeTriples = new HashSet<Triple>();
        for (Triple key : selectTriples) {
            String obj = key.get_obj();
            if (!countries.contains(obj))
                selectedNegativeTriples.add(key.clone());
            else
                selectedPositiveTriples.add(key.clone());
        }
        if (decision)
            return selectedPositiveTriples;
        else
            return selectedNegativeTriples;
    }


    public HashSet<Triple> getTriplesByLabels(HashSet<AnnotatedTriple> annotatedTriple, double number) {
        //number=0;--negative  number=1;--positive
        HashSet<Triple> allPrediction = new HashSet<Triple>();
        for (AnnotatedTriple key : annotatedTriple) {
            if (key.get_sign() == number) {
                allPrediction.add(key.get_triple());
            }
        }
        return allPrediction;
    }
    public HashSet<Triple> filterTriples(HashSet<Comment> listComments, Boolean decision) {
        HashSet<Triple> negativeTriples = new HashSet<Triple>();
        HashSet<Triple> positiveTriples = new HashSet<Triple>();

        for (Comment comment : listComments) {
            if (comment.get_decision()) {
                positiveTriples.add(comment.get_triple());
            } else if (!comment.get_decision()) {
                negativeTriples.add(comment.get_triple());
            }
        }

        if (decision) {
            return positiveTriples;
        } else {
            return negativeTriples;
        }
    }

    public HashSet<Triple> filterComments(HashSet<AnnotatedTriple> listComments, Boolean decision) {
        HashSet<Triple> negativeComments = new HashSet<Triple>();
        HashSet<Triple> positiveComments = new HashSet<Triple>();

        for (AnnotatedTriple comment : listComments) {
            if (comment.get_sign() == 1 || comment.get_sign() == -0.5) {
                positiveComments.add(comment.get_triple());
            } else {
                negativeComments.add(comment.get_triple());
            }
        }

        if (decision) {
            return positiveComments;
        } else {
            return negativeComments;
        }
    }

    public HashSet<String> extractAllEntities(HashSet<Triple> triples){
        HashSet<String> extractTargets = new HashSet<>();
        for (Triple triple : triples) {
            extractTargets.add(triple.get_subject());
            extractTargets.add(triple.get_obj());
        }
        return extractTargets;
    }

    public HashSet<String> triplesToSub(HashSet<Triple> triples) {
        HashSet<String> triplesSubjects = new HashSet<String>();
        for (Triple tri : triples)
            triplesSubjects.add(tri.get_subject());

        return triplesSubjects;
    }
    public HashSet<String> triplesToObj(HashSet<Triple> triples) {
        HashSet<String> triplesObjects = new HashSet<String>();
        for (Triple tri : triples)
            triplesObjects.add(tri.get_obj());
        return triplesObjects;
    }

}
