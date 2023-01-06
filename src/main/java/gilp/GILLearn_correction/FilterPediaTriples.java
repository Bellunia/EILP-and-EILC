package gilp.GILLearn_correction;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.comments.AnnotatedTriple;
import gilp.comments.Comment;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import gilp.sparql.wikidataSparql;
import gilp.utils.KVPair;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class FilterPediaTriples {

    public Boolean generateFeedback(String errorEntity, String property) {

        String errorQuery = " select ?a where {<" + redictEntity(errorEntity) + "> owl:sameAs ?a." +
                "filter(regex(?a,\"wikidata.org\"))}";
        System.out.println("errorQuery  " + errorQuery);
        ArrayList<String> matchError = new Sparql().getSingleResultsFromQuery(errorQuery);

        String correctPropertyQuery = "select distinct ?a where{ BIND(wdt:P31 AS ?instanceOf)." +
                "<" + matchError.get(0) + "> " + "?instanceOf ?a." +
                //   "filter(?a in( wd:" +property + ")) }";
                "filter(?a in( <" + property + ">)) }";

        System.out.println("wikidata--correctQuery:" + correctPropertyQuery);

        ArrayList<String[]> propertyResults2 = new wikidataSparql().getCommands(correctPropertyQuery);
        System.out.println("propertyResults2.size()  " + propertyResults2.size());

        String query2 = "select ?a where{ <" + redictEntity(errorEntity) + "> wdt:P17 ?a.}";
        ArrayList<String[]> propertyResults3 = new wikidataSparql().getCommands(query2);
        System.out.println("propertyResults3.size()  " + propertyResults3.size());
        return propertyResults2.size() == 2 || propertyResults3.size() == 2;
    }

    public Boolean generateFeedback2(String errorEntity) {

        String errorQuery = " select ?a where {<" + redictEntity(errorEntity) + "> a ?a." +
                "filter(regex(?a,\"Country\") || regex(?a,\"country\"))}";
        System.out.println("errorQuery  " + errorQuery);
        ArrayList<String> matchError = new Sparql().getSingleResultsFromQuery(errorQuery);

        return !matchError.isEmpty();
    }

    public Boolean generateFeedbackInTbox(String errorEntity, String range) {

        String errorQuery = " select ?a where {<" + redictEntity(errorEntity) + "> a ?a." +
                "filter(?a in( <" + range + ">))}";
        System.out.println("errorQuery  " + errorQuery);
        ArrayList<String> matchError = new Sparql().getSingleResultsFromQuery(errorQuery);
        return !matchError.isEmpty();
    }

    public String redictEntity(String entity) {
        String redictQuery = " select ?a where {<" + entity + "> dbo:wikiPageRedirects ?a. }";

        ArrayList<String> redict = new Sparql().getSingleResultsFromQuery(redictQuery);
        if (redict.isEmpty())
            return entity;
        else
            return redict.get(0);
    }

    public KVPair<HashSet<Triple>, HashSet<Triple>>initialFeedback(int number){
//random feedback
        HashSet<Triple> positiveTriple =
                new FilterPediaTriples().filterPositiveTriples(number);

        HashSet<Triple> negativeTriple =
                new FilterPediaTriples().filterOtherNegativeTriples(number);
//---------------------conflict feedback
//        HashSet<AnnotatedTriple> feedbackSingleValue = new FilterPediaTriples().getRandomFeedbackSingleValue(predicate, number);
//        HashSet<AnnotatedTriple> feedbackDistinctValue= new FilterPediaTriples().getRandomFeedbackDistinctValue( predicate,number);
//        HashSet<Triple> positiveTriple = new RulesOthers().getTriplesByLabels(feedbackSingleValue, 1);
//        HashSet<Triple> negativeTriple = new RulesOthers().getTriplesByLabels(feedbackSingleValue, 0);
        //-----------------------------
        return new KVPair<>(positiveTriple, negativeTriple);


    }

    public   KVPair<HashSet<String>,HashSet<String>> initialDifferentObjects (int number){
        String positiveQuery= "select distinct ?object  where {" +
                " ?subject dbo:nationality ?object. " +
                "?subject rdf:type dbo:Person." +
                " ?object rdf:type wikidata:Q6256. " +
                "} ORDER BY RAND() limit"+number;
        HashSet<String> positiveObjects= new Sparql().getSingleVariable(positiveQuery);

        String negativeQuery="select distinct ?object  where { " +
                "?subject dbo:nationality ?object." +
                " ?subject rdf:type dbo:Person. " +
                " filter not exists{ ?object rdf:type wikidata:Q6256.}" +
                "  } ORDER BY RAND() limit"+number;
        HashSet<String> negativeObjects= new Sparql().getSingleVariable(negativeQuery);

//        String positiveRedirectQuery= "select distinct ?object  where {" +
//                " ?subject dbo:nationality ?object. " +
//                "?subject rdf:type dbo:Person." +
//                " {?object rdf:type wikidata:Q6256.} union {?object dbo:wikiPageRedirects ?c. ?c rdf:type wikidata:Q6256.} " +
//                "} ORDER BY RAND() limit"+number;
//        HashSet<String> positiveRedirectObjects= new Sparql().getSingleVariable(positiveRedirectQuery);
//
//        String negativeRedirectQuery="select distinct ?object  where { " +
//                "?subject dbo:nationality ?object." +
//                " ?subject rdf:type dbo:Person. " +
//                " filter not exists{ {?object rdf:type wikidata:Q6256.} union " +
//                " {?object dbo:wikiPageRedirects ?c. ?c rdf:type wikidata:Q6256.}} } ORDER BY RAND() limit"+number;
//        HashSet<String> negativeRedirectObjects= new Sparql().getSingleVariable(negativeRedirectQuery);
return new KVPair<>(positiveObjects,negativeObjects);
    }

    public   KVPair<HashSet<String>,HashSet<String>> initialRandomObjects (int number) throws IOException {
        String randomQuery= "select distinct ?object  where {" +
                "{ ?subject dbo:nationality ?object1. } union" +
                "{?subject dbo:nationality ?object .?object dbo:wikiPageRedirects ?object1. }" +
                "?subject rdf:type dbo:Person." +
                "} ORDER BY RAND() limit "+number;
        String query="select distinct   ?object1  where {?subject dbo:nationality ?object." +
                "?object dbo:wikiPageRedirects ?object1. ?subject rdf:type dbo:Person.} ORDER BY RAND() limit "+number;
        System.out.println("randomQuery:"+query);
        HashSet<String> randomObjects= new Sparql().getSingleVariable(query);

        HashSet<String> truePositiveObjects=  RuleLearnerHelper.readTypes("./data/dbpedia-type/tp-objects.txt");
    //   HashSet<String> errorObjects=  RuleLearnerHelper.readTypes("./data/dbpedia-type/errors/all-errors-old.txt");

       HashSet<String> positiveObjects= new HashSet<>(randomObjects);
        positiveObjects.retainAll(truePositiveObjects);
        HashSet<String> negativeObjects= new HashSet<>(randomObjects);
        negativeObjects.removeAll(positiveObjects);

        new ILPLearnSettings().writeOutRandomObjects(positiveObjects,negativeObjects,number);

        return new KVPair<>(positiveObjects,negativeObjects);
    }

    public HashSet<Comment> initialComment(int number){

        KVPair<HashSet<Triple>, HashSet<Triple>> initialTriples = initialFeedback(number);
        HashSet<Triple> positiveTriple = initialTriples.getKey();
        HashSet<Triple> negativeTriple = initialTriples.getValue();

        HashSet<Comment> comments = new HashSet<Comment>();
        comments.addAll(new RulesOthers().tripleLabelled(positiveTriple, true));
        comments.addAll(new RulesOthers().tripleLabelled(negativeTriple, false));
        return comments;


    }
    public HashSet<AnnotatedTriple> getRandomFeedbackDistinctValue(String predicate, int number) {
        HashSet<String> countries =  RuleLearnerHelper.readTypes("./data/conflict feedback/nationality/range.txt");

        String randomQuery = "select distinct ?subject ?object where {?subject dbo:nationality ?object.  " +
                "?subject dbo:nationality ?c. filter(?object!=?c)" +
                "BIND(RAND() AS ?rand) } ORDER BY ?rand limit " + number;

        ArrayList<HashMap<String, String>> items = new Sparql().getResultsFromQuery(randomQuery);

        HashSet<AnnotatedTriple> randomAnnotatedTriple = new HashSet<>();

        if (items != null) {
            for (HashMap<String, String> key : items) {
                String item = key.get("subject");
                String entity = key.get("object");
                Triple negativeElement = new Triple(item, predicate, entity);

                AnnotatedTriple newTriple;
                if (countries.contains(entity)) {
                    newTriple = new AnnotatedTriple(negativeElement, 1);
                } else {
                    newTriple = new AnnotatedTriple(negativeElement, 0);
                }
                randomAnnotatedTriple.add(newTriple);

                System.out.println("test:" + newTriple + "\n");

            }
        }
        return randomAnnotatedTriple;
    }

    public HashSet<AnnotatedTriple> getRandomFeedbackSingleValue(String predicate, int number) {

        HashSet<String> countries =  RuleLearnerHelper.readTypes("./data/conflict feedback/nationality/range.txt");

        String randomQuery = "select distinct ?subject ?object where {?subject <"+Property.PREDICATE_NAME+"> ?object.  " +
                "?subject <"+Property.PREDICATE_NAME+"> ?c. filter(?object=?c)" +
                "BIND(RAND() AS ?rand) } ORDER BY ?rand limit " + number;

        ArrayList<HashMap<String, String>> items = new Sparql().getResultsFromQuery(randomQuery);

        HashSet<AnnotatedTriple> randomAnnotatedTriple = new HashSet<>();

        if (items != null) {
            for (HashMap<String, String> key : items) {
                String item = key.get("subject");
                String entity = key.get("object");
                Triple negativeElement = new Triple(item, predicate, entity);

                AnnotatedTriple newTriple;
                if (countries.contains(entity)) {
                    newTriple = new AnnotatedTriple(negativeElement, 1);
                } else {
                    newTriple = new AnnotatedTriple(negativeElement, 0);
                }
                randomAnnotatedTriple.add(newTriple);

                System.out.println("test:" + newTriple + "\n");

            }
        }
        return randomAnnotatedTriple;
    }

    public HashSet<Comment> doComment( KVPair<HashSet<Triple>, HashSet<Triple>> initialTriples){

        HashSet<Triple> positiveTriple = initialTriples.getKey();
        HashSet<Triple> negativeTriple = initialTriples.getValue();

        HashSet<Comment> comments = new HashSet<Comment>();
        comments.addAll(new RulesOthers().tripleLabelled(positiveTriple, true));
        comments.addAll(new RulesOthers().tripleLabelled(negativeTriple, false));
        return comments;


    }

    public void generateWrongFacts() {
        /*
         * "Generates error detection data in knowledge graphs by randomly corrupting triples in order to generate wrong facts. "
         * "These wrong facts can be of three kinds:" " 1 - Randomly corrupted triple"
         * " 2 - Same type as the original entity"
         */

    }

    public HashSet<Triple> filterRudikNegativeSample( int number) {

        String sql = " select distinct ?subject ?object from <http://dbpedia.org> where { "
                + "?subject rdf:type <" + Property.DOMAIN + ">. " + "?object rdf:type <" + Property.RANGE + ">. "
                + "{{?subject ?relTarget ?realObject.} UNION {?realSubject ?relTarget ?object.}} "
                + "?subject ?relation ?object. FILTER (?relTarget = <" + Property.PREDICATE_NAME + ">)" + " FILTER (?relation != <"
                + Property.PREDICATE_NAME + ">) "
                + "FILTER NOT EXISTS {?subject <" + Property.PREDICATE_NAME + "> ?object.} }  limit " + number;//ORDER BY RAND()
        System.out.println(sql);

        return filterNegativeTriples(sql);
    }

    public KVPair<HashSet<Triple>, HashSet<Triple>> filterSampleByDomainAndRange(int number) {

        HashSet<Triple> positiveTriple = filterPositiveTriples( number);
        HashSet<Triple> negativeTriple = filterRudikNegativeSample(number);

        System.out.println("\n ******************** \n");

        HashSet<Triple> negativeTriple1 = filterOtherNegativeTriples( number);

        HashSet<HashMap<Triple, Triple>> filterFeedbacks = filterConflictFeedbackBasedOnNegativeTriple(negativeTriple);

        System.out.println("\n ********************size: " + filterFeedbacks.size());

        KVPair<HashSet<Triple>, HashSet<Triple>> filterSamples = new KVPair<HashSet<Triple>, HashSet<Triple>>(
                positiveTriple, negativeTriple);

        BufferedWriter writer1 = null;

        try {
            writer1 = new BufferedWriter(new FileWriter("/home/wy/Downloads/ConflictingTriplesSamples.tsv"));

            for (HashMap<Triple, Triple> sample : filterFeedbacks) {

                for (Triple conflict : sample.keySet()) {
                    writer1.write(conflict.toString() + "\t" + sample.get(conflict) + "\n");
                }
            }

//			for (Triple key : positiveTriple)
//				writer1.write(key.toString());
//
//			writer1.write("****************");
//
//			for (Triple key : negativeTriple)
//				writer1.write(key.toString());

            writer1.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return filterSamples;
    }


    public KVPair<HashSet<Triple>, HashSet<Triple>> filterSampleByType( int number) {

        String objectTypeQuery = "SELECT distinct ?x WHERE {"
                + "{ select distinct ?x  (count(?x) as ?xcount) where{?a <" + Property.PREDICATE_NAME + "> ?b.?b a ?x."
                + " FILTER(regex(str(?x),\"http://dbpedia.org/ontology/\"))} GROUP BY ?x ORDER BY DESC(?xcount) limit 1 } }";

        String subjectTypeQuery = "SELECT distinct ?x WHERE {{"
                + "select distinct ?x  (count(?x) as ?xcount) where{?a <" + Property.PREDICATE_NAME + "> ?b. ?a a ?x."
                + " FILTER(regex(str(?x),\"http://dbpedia.org/ontology/\"))} GROUP BY ?x ORDER BY DESC(?xcount) limit 1 }}";

//		ArrayList<String> objectTypeResults = new Sparql().getSingleResultsFromQuery(objectTypeQuery);
//		String maxObjectType = objectTypeResults.get(0);
//		System.out.println(maxObjectType);
//
//		ArrayList<String> subjectTypeResults = new Sparql().getSingleResultsFromQuery(subjectTypeQuery);
//		String maxSubjectType = subjectTypeResults.get(0);
//		System.out.println(maxSubjectType);

        // for test, using the type we get.

        String maxSubjectType = "http://dbpedia.org/ontology/Person";
        String maxObjectType = "http://dbpedia.org/ontology/Place";

        String filterPositiveTripleQuery = "select distinct ?subject ?object from <http://dbpedia.org> where{ ?subject <"
                + Property.PREDICATE_NAME + "> ?object. ?subject rdf:type <" + maxSubjectType + ">." + "?object rdf:type <"
                + maxObjectType + ">.} ORDER BY RAND() limit 100";
//		String test = "select distinct ?subject ?object from <http://dbpedia.org> where { ?subject rdf:type <http://dbpedia.org/ontology/Person>. "
//				+ "?object rdf:type <http://dbpedia.org/ontology/Person>. ?subject <http://dbpedia.org/ontology/birthPlace> ?object. } limit 10";

        String filterNegativeTripleQuery = " select distinct ?subject ?object from <http://dbpedia.org> where { "
                + "?subject rdf:type <" + maxSubjectType + ">. " + "?object rdf:type <" + maxObjectType + ">. "
                + "{{?subject ?relTarget ?realObject.} UNION {?realSubject ?relTarget ?object.}} "
                + "?subject ?relation ?object. FILTER (?relTarget = <" + Property.PREDICATE_NAME + ">)" + " FILTER (?relation != <"
                + Property.PREDICATE_NAME + ">) " + "FILTER NOT EXISTS {?subject <" + Property.PREDICATE_NAME + "> ?object.} } ORDER BY RAND() limit 10";

        HashSet<Triple> positiveTriple = filterPositiveTriples(number);

        System.out.println("\n ******************** \n");

        HashSet<Triple> negativeTriple = filterOtherNegativeTriples( number);

        HashSet<HashMap<Triple, Triple>> filterFeedbacks = filterConflictFeedbackBasedOnNegativeTriple(negativeTriple);

        System.out.println("\n ********************size: " + filterFeedbacks.size());

        KVPair<HashSet<Triple>, HashSet<Triple>> filterSamples = new KVPair<HashSet<Triple>, HashSet<Triple>>(
                positiveTriple, negativeTriple);

        BufferedWriter writer1 = null;

        try {
            writer1 = new BufferedWriter(new FileWriter("/home/wy/Downloads/ConflictingTriplesSamples.tsv"));

            for (HashMap<Triple, Triple> sample : filterFeedbacks) {

                for (Triple conflict : sample.keySet()) {
                    writer1.write(conflict.toString() + "\t" + sample.get(conflict) + "\n");
                }
            }

//			for (Triple key : positiveTriple)
//				writer1.write(key.toString());
//
//			writer1.write("****************");
//
//			for (Triple key : negativeTriple)
//				writer1.write(key.toString());

            writer1.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return filterSamples;
    }

    public HashSet<Triple> filterPositiveTriples(int number) {

        String filterPositiveTripleQuery = "select distinct ?subject ?object from <http://dbpedia.org> where{ ?subject <"
                +Property.PREDICATE_NAME+ "> ?object. ?subject rdf:type <" + Property.DOMAIN + ">."
             //   + "?object rdf:type <" + Property.RANGE + ">."
               + "?object rdf:type <" + Property.rangePropertyInwikiData + ">." // only for the nationality
                + "} ORDER BY RAND() limit " + number;
System.out.println("positive query:"+filterPositiveTripleQuery);

        ArrayList<HashMap<String, String>> positiveSubjectOBject = new Sparql()
                .getResultsFromQuery(filterPositiveTripleQuery);
        HashSet<Triple> positiveTriple = new HashSet<Triple>();

        HashSet<String> samekey = new HashSet<>();

        for (HashMap<String, String> key : positiveSubjectOBject) {
            String subject = key.get("subject");
            String object = key.get("object");

            if (!samekey.contains(subject)) {
                samekey.add(subject);
                Triple positiveElement = new Triple(subject, Property.PREDICATE_NAME, object);
                positiveTriple.add(positiveElement);
            }
        }
        System.out.println("positive query:"+positiveTriple.size());
        return positiveTriple;
    }

    public HashSet<HashMap<Triple, Triple>> filterConflictFeedbackBasedOnNegativeTriple(HashSet<Triple> negativeTriple) {
        // <positive,negative>
        HashSet<HashMap<Triple, Triple>> filterFeedbacks = new HashSet<HashMap<Triple, Triple>>();

        for (Triple negative : negativeTriple) {
            String subject = negative.get_subject();
            String object = negative.get_obj();

            String filterPositiveTripleQuery = "select distinct ?object from <http://dbpedia.org> where{ ?subject <"
                    + Property.PREDICATE_NAME + "> ?object." + "?object rdf:type <" + Property.RANGE + ">. filter(?subject=<" + subject
                    + "> && ?object !=<" + object + "> )} ORDER BY RAND()";
            ArrayList<String> objects = new Sparql().getSingleResultsFromQuery(filterPositiveTripleQuery);
            if (!objects.isEmpty()) {

                String randomObject = objects.get(0);
                Triple conflictingFeedback = new Triple(subject, Property.PREDICATE_NAME, randomObject);

                HashMap<Triple, Triple> conflictingSamples = new HashMap<Triple, Triple>();

                conflictingSamples.put(conflictingFeedback, negative);

                filterFeedbacks.add(conflictingSamples);
            }

        }

        return filterFeedbacks;
    }

    public HashSet<HashMap<Triple, Triple>> filterConfilctingFeedbackBasedOnPositiveTriple(
            String predicate, String maxSubjectType, String maxObjectType, HashSet<Triple> positiveTriple) {
        // <positive,negative>
        HashSet<HashMap<Triple, Triple>> filterFeedbacks = new HashSet<HashMap<Triple, Triple>>();

        for (Triple positive : positiveTriple) {
            String subject = positive.get_subject();
            String object = positive.get_obj();

            String filterPositiveTripleQuery = "select distinct ?object from <http://dbpedia.org> where{ <" + subject
                    + ">  <" + predicate + "> ?object." + "?object rdf:type ?wrongObjectType. filter(?object !=<"
                    + object + "> )" + "" + "FILTER NOT EXISTS {" + "{?wrongObjectType rdfs:subClassOf  <"
                    + maxObjectType + ">.} union" +

                    "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + maxObjectType + ">. } union" +

                    "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + maxObjectType
                    + ">.} union " +

                    "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C. ?C rdfs:subClassOf <"
                    + maxObjectType + ">.} union " +

                    "{?wrongObjectType owl:equivalentClass " + " <" + maxObjectType + ">.} union " +

                    "{ <" + maxObjectType + "> owl:equivalentClass ?wrongObjectType.} union "
                    + "{ ?wrongObjectType owl:equivalentClass <" + maxObjectType + ">.}}"

                    + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") )"

                    + "FILTER NOT EXISTS {?object a <http://dbpedia.org/ontology/Place>.} }" + "" + "} ORDER BY RAND()";

            ArrayList<String> objects = new Sparql().getSingleResultsFromQuery(filterPositiveTripleQuery);
            if (!objects.isEmpty()) {

                String randomObject = objects.get(0);
                Triple conflictingFeedback = new Triple(subject, predicate, randomObject);

                HashMap<Triple, Triple> conflictingSamples = new HashMap<Triple, Triple>();

                conflictingSamples.put(positive, conflictingFeedback);

                filterFeedbacks.add(conflictingSamples);
            }

        }

        return filterFeedbacks;
    }

    public String positiveQuery(int number) {

        String query = "select distinct ?subject ?object from <http://dbpedia.org> where{ ?subject <"
                + Property.PREDICATE_NAME + "> ?object. ?subject rdf:type <" + Property.DOMAIN + ">." + "?object rdf:type <"
                + Property.RANGE + ">.} ORDER BY RAND() limit " + number;

        return query;
    }

    public String negativeQuery(String subject,String object) {// int number

        String filterNegativeTripleQuery = "select distinct ?object from <http://dbpedia.org> where{ <" + subject
                + ">  <" + Property.PREDICATE_NAME + "> ?object." + "?object rdf:type ?wrongObjectType. filter(?object !=<"
                + object + "> )" + "" + "FILTER NOT EXISTS {" + "{?wrongObjectType rdfs:subClassOf  <"
                + Property.RANGE + ">.} union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + Property.RANGE + ">. } union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + Property.RANGE
                + ">.} union " +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C." +
                " ?C rdfs:subClassOf <"
                + Property.RANGE + ">.} union " +

                "{?wrongObjectType owl:equivalentClass " + " <" + Property.RANGE + ">.} union " +

                "{ <" + Property.RANGE + "> owl:equivalentClass ?wrongObjectType.} union "
                + "{ ?wrongObjectType owl:equivalentClass <" + Property.RANGE + ">.}}"

                + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") )"

                + "FILTER NOT EXISTS {?object a <" + Property.RANGE + ">.} }" + "" + "} ORDER BY RAND()";
        System.out.println(filterNegativeTripleQuery);

        return filterNegativeTripleQuery;
    }

    public HashSet<HashMap<Triple, Triple>> filterConflictFeedback(int number) {
        // <positive,negative>
        HashSet<HashMap<Triple, Triple>> filterFeedbacks = new HashSet<HashMap<Triple, Triple>>();

        String positiveQuery = positiveQuery(number);

        ArrayList<HashMap<String, String>> positiveSubjectOBject = new Sparql()
                .getResultsFromQuery(positiveQuery);
        HashSet<Triple> positiveTriple = new HashSet<Triple>();

        HashSet<String> samekey = new HashSet<String>();

        for (HashMap<String, String> key : positiveSubjectOBject) {
            String subject = key.get("subject");
            String object = key.get("object");

            if (!samekey.contains(subject)) {
                samekey.add(subject);

                Triple positiveElement = new Triple(subject, Property.PREDICATE_NAME, object);

                String filterNegativeTripleQuery =  negativeQuery(subject, object);

                ArrayList<String> objects = new Sparql().getSingleResultsFromQuery(filterNegativeTripleQuery);
                if (!objects.isEmpty()) {

                    String randomObject = objects.get(0);
                    Triple conflictingFeedback = new Triple(subject, Property.PREDICATE_NAME, randomObject);

                    HashMap<Triple, Triple> conflictingSamples = new HashMap<Triple, Triple>();

                    conflictingSamples.put(positiveElement, conflictingFeedback);
                    System.out.println(positiveElement+"\t"+conflictingFeedback+"\n");

                    filterFeedbacks.add(conflictingSamples);
                }

            }

        }

        return filterFeedbacks;
    }

    public HashSet<Triple> filterOtherNegativeTriples(int number) {
        // the queryNegative1 is not strictly to find the negative examples, some
        // triples' object type also not fit the sparql.

        String queryRough = "select distinct ?subject ?object  ?wrongObjectType from <http://dbpedia.org> where {"
                + " ?subject <" + Property.PREDICATE_NAME + "> ?object."
                + "				 ?subject rdf:type <" + Property.DOMAIN + ">. "
                + "				 ?object rdf:type ?wrongObjectType."
                + "			 FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\"))"
                + "				FILTER NOT EXISTS {?object a <" + Property.RANGE + ">.} }";

        String queryNegative1 = "select distinct * from <http://dbpedia.org> where {" + " ?subject <" + Property.PREDICATE_NAME
                + "> ?object." + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                + "	 ?object rdf:type ?wrongObjectType. " + "FILTER NOT EXISTS {"
                + "{?wrongObjectType rdfs:subClassOf  <" + Property.RANGE + ">.} union"
                + "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + Property.RANGE + ">. } union"
                + "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + Property.RANGE
                + ">.} union "
                + "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C. " +
                "?C rdfs:subClassOf <" + Property.RANGE + ">.} union "
                + "{?wrongObjectType owl:equivalentClass <" + Property.RANGE
                + ">.} union " + "{ <" + Property.RANGE + "> owl:equivalentClass ?wrongObjectType.} union "
                + "{ ?wrongObjectType owl:equivalentClass <" + Property.RANGE + ">.}}"
                + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") && ?wrongObjectType != <"
                + Property.RANGE + ">) } ORDER BY RAND() limit " + number;

        String queryNegative = "select distinct ?subject ?object from <http://dbpedia.org> where {" + " ?subject <" + Property.PREDICATE_NAME
                + "> ?object." + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                + "	 ?object rdf:type ?wrongObjectType. " + "FILTER NOT EXISTS {"
                + "{?wrongObjectType rdfs:subClassOf  <" + Property.RANGE + ">.} union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + Property.RANGE + ">. } union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + Property.RANGE
                + ">.} union " +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C. ?C rdfs:subClassOf <"
                + Property.RANGE + ">.} union " +

                "{?wrongObjectType owl:equivalentClass " + " <" + Property.RANGE + ">.} union " +

                "{ <" + Property.RANGE + "> owl:equivalentClass ?wrongObjectType.} union "
                + "{ ?wrongObjectType owl:equivalentClass <" + Property.RANGE + ">.}}"

                + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") )"

                + "FILTER NOT EXISTS {?object a <" + Property.RANGE + ">.} }ORDER BY RAND() limit " + number;//http://dbpedia.org/ontology/Place

          System.out.println("\n queryNegative:" + queryNegative + "\n");


        return filterNegativeTriples(queryNegative);

    }

    public HashSet<Triple> filterNegativeTriples(String sql) {
        // the queryNegative1 is not strictly to find the negative examples, some
        // triples' object type also not fit the sparql.

        ArrayList<HashMap<String, String>> negativeSubjectOBject = new Sparql().getResultsFromQuery(sql);
        HashSet<Triple> negativeTriple = new HashSet<Triple>();
        //  System.out.println("\n negative---samples" + "\n");

        HashSet<String> samekey = new HashSet<String>();
        for (HashMap<String, String> key : negativeSubjectOBject) {
            String subject = key.get("subject");
            String object = key.get("object");
            //     String objectType = key.get("wrongObjectType");

            if (!samekey.contains(subject)) {
                samekey.add(subject);

                Triple negativeElement = new Triple(subject, Property.PREDICATE_NAME, object);

                negativeTriple.add(negativeElement);

              //   System.out.println(negativeElement  + "\n");
            }

        }
        return negativeTriple;

    }




    public HashSet<Triple> filterNegativeTriples
            (String maxSubjectType, String maxObjectType, String predicate, int number) {
        // the queryNegative1 is not strictly to find the negative examples, some
        // triples' object type also not fit the sparql.
        String queryNegative = "select distinct ?subject ?object from <http://dbpedia.org> where {"
                + " ?subject <" + predicate + "> ?object." + " ?subject rdf:type <" + maxSubjectType + ">. "
                + "	 ?object rdf:type ?wrongObjectType. "
                + "FILTER NOT EXISTS {"
                + "{?wrongObjectType rdfs:subClassOf  <" + maxObjectType + ">.} union" +
                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + maxObjectType + ">. } union" +
                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + maxObjectType
                + ">.} union " +
                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C. ?C rdfs:subClassOf <"
                + maxObjectType + ">.} union " +
                "{?wrongObjectType owl:equivalentClass " + " <" + maxObjectType + ">.} union " +
                "{ <" + maxObjectType + "> owl:equivalentClass ?wrongObjectType.} union "
                + "{ ?wrongObjectType owl:equivalentClass <" + maxObjectType + ">.}}"
                + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") )"
                + "FILTER NOT EXISTS {?object a <" + maxObjectType + ">.} }ORDER BY RAND() limit " + number;

        System.out.println("\n queryNegative:" + queryNegative + "\n");

        ArrayList<HashMap<String, String>> negativeSubjectOBject = new Sparql().getResultsFromQuery(queryNegative);
        HashSet<Triple> negativeTriple = new HashSet<Triple>();
        //  System.out.println("\n negative---samples" + "\n");


        HashSet<String> samekey = new HashSet<String>();
        for (HashMap<String, String> key : negativeSubjectOBject) {
            String subject = key.get("subject");
            String object = key.get("object");
            //     String objectType = key.get("wrongObjectType");

            if (!samekey.contains(object)) {
                samekey.add(object);

                Triple negativeElement = new Triple(subject, predicate, object);

                negativeTriple.add(negativeElement);

                //   System.out.println(negativeElement  + "\n");
            }

        }
        return negativeTriple;

    }


    public static void main(String[] args) throws IOException {




        // new DbpediaSearchSpace().filterPositiveSample(predicate);
//		 KVPair<HashSet<Triple>, HashSet<Triple>> filterSamples = new
//		 DbpediaSearchSpace().filterSampleByType(predicate);

        final String queryDBPediaSpouseUnion = " select distinct ?subject ?object from <http://dbpedia.org> where { "
                + "?subject rdf:type <http://dbpedia.org/ontology/Person>. "
                + "?object rdf:type <http://dbpedia.org/ontology/Person>. "
                + "{{?subject ?relTarget ?realObject.} UNION {?realSubject ?relTarget ?object.}} "
                + "?subject ?relation ?object. FILTER (?relTarget = <http://dbpedia.org/ontology/spouse>)"
                + " FILTER (?relation != <http://dbpedia.org/ontology/spouse>) "
                + "FILTER NOT EXISTS {?subject <http://dbpedia.org/ontology/spouse> ?object.} }";

        String negativeSameSUbjects = " select distinct ?subject ?object from <http://dbpedia.org> where { "
                + "?subject rdf:type <http://dbpedia.org/ontology/Person>. " + "?object rdf:type ?wrongObjectType. "
                + "{{?subject ?relTarget ?realObject.} UNION {?realSubject ?relTarget ?object.}} "
                + "?subject ?relation ?object. FILTER (?relTarget = <http://dbpedia.org/ontology/birthPlace>)"
                + " FILTER (?relation != <http://dbpedia.org/ontology/birthPlace>) "
                + "Filter(?wrongObjectType != <http://dbpedia.org/ontology/Place>)"
                + "FILTER NOT EXISTS {?subject <http://dbpedia.org/ontology/birthPlace> ?object.} }";

	//	String predicate = "http://dbpedia.org/ontology/birthPlace";
//		String maxSubjectType = "http://dbpedia.org/ontology/Person";
//		String maxObjectType = "http://dbpedia.org/ontology/Place";


       String predicate = "http://dbpedia.org/ontology/nationality";
        String maxSubjectType = "http://dbpedia.org/ontology/Person";
        String maxObjectType = "http://dbpedia.org/ontology/Country";
        int number = 100;

//        HashSet<Triple> positiveTriple = new FilterTriples().filterPositiveTriples(predicate, maxSubjectType,
//                maxObjectType, number);
//
////
 //       new FilterPediaTriples().filterRudikNegativeSample(predicate,number);
 //       HashSet<Triple> negativeTriple = new FilterPediaTriples().filterOtherNegativeTriples(number);
    //    new FilterPediaTriples(). filterConfilctFeedback(predicate,number);


        String queryNegative = "select distinct ?subject ?object from <http://dbpedia.org> where {" + " ?subject <" + Property.PREDICATE_NAME
                + "> ?object." + " ?subject rdf:type <" + Property.DOMAIN + ">. "
                + "	 ?object rdf:type ?wrongObjectType. " + "FILTER NOT EXISTS {"
                + "{?wrongObjectType rdfs:subClassOf  <" + Property.RANGE + ">.} union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf <" + Property.RANGE + ">. } union" +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf <" + Property.RANGE
                + ">.} union " +

                "{?wrongObjectType rdfs:subClassOf  ?A. ?A rdfs:subClassOf ?B. ?B rdfs:subClassOf ?C. ?C rdfs:subClassOf <"
                + Property.RANGE + ">.} union " +

                "{?wrongObjectType owl:equivalentClass " + " <" + Property.RANGE + ">.} union " +

                "{ <" + Property.RANGE + "> owl:equivalentClass ?wrongObjectType.} union "
                + "{ ?wrongObjectType owl:equivalentClass <" + Property.RANGE + ">.}}"

                + " FILTER(regex(str(?wrongObjectType),\"http://dbpedia.org/ontology\") )"

                + "FILTER NOT EXISTS {?object a <" + Property.RANGE + ">.} }ORDER BY RAND() limit " + number;
        System.out.println(queryNegative);



        HashSet<Triple> negativeTriple = new FilterPediaTriples().filterNegativeTriples(queryNegative);

        String path = "/home/wy/Desktop/test/negativeTriple.txt";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));

        try {
            for (Triple t : negativeTriple)
                writer.write(t + "\t"+"1"+"\n");


            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


    }

}
