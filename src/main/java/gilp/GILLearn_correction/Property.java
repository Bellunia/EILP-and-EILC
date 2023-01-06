package gilp.GILLearn_correction;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import gilp.utils.KVPair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Property {
    public static final String PREDICATE_NAME = "http://dbpedia.org/ontology/nationality";//birthPlace
    public static final String DOMAIN ="http://dbpedia.org/ontology/Person";
    public static final String RANGE = "http://dbpedia.org/ontology/Country";//Place
    public static final String same_NAME = "http://dbpedia.org/ontology/citizenship";
    /**
     * http://dbpedia.org/ontology/birthPlace subject-type:
     * http://dbpedia.org/resource/Person object-type:
     * http://dbpedia.org/resource/place Choose the birthPlace to do the test
     *
     *
     */

    public static final String simpleLabel ="Country";

    public static final Boolean ruleLabel =true;
    //true: choose the subject, one to one relation
    // false :choose the object, one to many relation

    public static final String samePropertyOfRelation ="http://dbpedia.org/ontology/country"; //String oneProperty = dbo:country
    public static final String rangePropertyInwikiData = "http://www.wikidata.org/entity/Q6256";
    public static String rangeNegativePropertyInwikiData = "http://www.wikidata.org/entity/Q41710";
    public static final String relationPropertyInWikidata="wdt:P17";
    public static String rangeEquivalentClassProperty() {
        //String rangePropertyInwikiData
        String propertyQuery = "select distinct ?d where { <" + RANGE + "> owl:equivalentClass ?d. " +
                "filter(regex(?d,\"wikidata.org\"))}";
        ArrayList<String> propertyResults = new Sparql().getSingleResultsFromQuery(propertyQuery);
        return propertyResults.get(0);

    }
    private static String sameProperty(){

        String sameProperty = " select distinct ?a  where {<" + Property.PREDICATE_NAME + "> rdfs:range ?e." +
                " ?a rdfs:range ?e. ?a rdfs:label ?c. ?e rdfs:label ?d. filter(?c=?d)}";
        ArrayList<String> oneProperties = new Sparql().getSingleResultsFromQuery(sameProperty);
        if(oneProperties!=null)
            return oneProperties.get(0);//dbo:country --equivalentProperty of range
        else
            return null;

    }
    public String simpleLabels(){
        String[] rangeSplit = Property.RANGE.split("/");
        return rangeSplit[rangeSplit.length - 1];
    }

    private KVPair<String, String> getDomainAndRange(String predicate) {
        //  String predicate = "http://dbpedia.org/ontology/nationality";// "http://dbpedia.org/ontology/nationality";
        //  String maxSubjectType = "http://dbpedia.org/ontology/Person";// "http://dbpedia.org/ontology/Person";
        //  String maxObjectType = "http://dbpedia.org/ontology/Country";// "http://dbpedia.org/ontology/Country";

        String sql = "select distinct  ?domain ?range where{ <" + predicate + "> rdfs:domain ?domain. <" +
                predicate + "> rdfs:range ?range. } ";
        ArrayList<HashMap<String, String>> getDomainAndRange = new Sparql()
                .getResultsFromQuery(sql);

        String domain = null;
        String range = null;
        for (HashMap<String, String> key : getDomainAndRange) {
            domain = key.get("domain");
            range = key.get("range");
        }

        String maxSubjectType = null;
        String maxObjectType = null;
        if (getDomainAndRange.isEmpty()) {
            ArrayList<String> getBestTypes = getBestTypes();
            maxSubjectType = getBestTypes.get(0);
            maxObjectType = getBestTypes.get(1);
            return new KVPair<>(maxSubjectType, maxObjectType);
        } else {
            return new KVPair<>(domain, range);
        }

    }

    private ArrayList<String> getBestTypes() {
        // for test, using the type we get.
        //	String maxSubjectType = "http://dbpedia.org/ontology/Person";
        //	String maxObjectType = "http://dbpedia.org/ontology/Place";
        String objectTypeQuery = "SELECT distinct ?x WHERE {"
                + "{ select distinct ?x  (count(?x) as ?xcount) where{?a <" + Property.PREDICATE_NAME + "> ?b.?b a ?x."
                + " FILTER(regex(str(?x),\"http://dbpedia.org/ontology/\"))} GROUP BY ?x ORDER BY DESC(?xcount) limit 1 } }";

        String subjectTypeQuery = "SELECT distinct ?x WHERE {{"
                + "select distinct ?x  (count(?x) as ?xcount) where{?a <" + Property.PREDICATE_NAME + "> ?b. ?a a ?x."
                + " FILTER(regex(str(?x),\"http://dbpedia.org/ontology/\"))} GROUP BY ?x ORDER BY DESC(?xcount) limit 1 }}";

        ArrayList<String> objectTypeResults = new Sparql().getSingleResultsFromQuery(objectTypeQuery);
        String maxObjectType = objectTypeResults.get(0);
        //System.out.println(maxObjectType);

        ArrayList<String> subjectTypeResults = new Sparql().getSingleResultsFromQuery(subjectTypeQuery);
        String maxSubjectType = subjectTypeResults.get(0);
        //	System.out.println(maxSubjectType);

        ArrayList<String> types = new ArrayList<>();
        types.add(maxSubjectType);
        types.add(maxObjectType);

        return types;
    }


    public static String relationPropertyInWikidata() {
        String propertyQuery = "select distinct ?b where {?a rdfs:range <" + RANGE + ">. " +
                "?a rdfs:label ?c. <" + RANGE + "> rdfs:label ?d. " +
                " ?a owl:equivalentProperty ?b. filter(?c=?d)} ";
        // http://www.wikidata.org/entity/P17---get the property P17
        ArrayList<String> propertyQueryResult = new Sparql().getSingleResultsFromQuery(propertyQuery);
        String[] property = propertyQueryResult.get(0).split("/");
        String pro = "wdt:"+property[property.length - 1];
        System.out.println(pro);
        return pro;
    }


    public static ArrayList<String> samePropertyInType() throws IOException {
        String propertyQuery = "select distinct ?c where {?a dbo:nationality ?b. ?b a ?c. filter(regex(?c ,\"wikidata\")) } ";
        // http://www.wikidata.org/entity/P17---get the property P17
        ArrayList<String> propertyQueryResult = new Sparql().getSingleResultsFromQuery(propertyQuery);

        Writer writer1 = new OutputStreamWriter(
                new FileOutputStream("./prediction/nationality_type1.txt"),
                StandardCharsets.UTF_8);

        for(String property : propertyQueryResult)

            writer1.write(property+"\n");
        writer1.close();

        return propertyQueryResult;
    }

    public static final List<String> headers = Arrays.asList("Rule", "Head Coverage", "Std Confidence",
            "PCA Confidence", "Positive Examples", "Body size", "PCA Body size",
            "Functional variable", "Std. Lower Bound", "PCA Lower Bound", "PCA Conf estimation");

    public static void main(String[] args) throws Exception {
        // KVPair<String, String> domainAndRange = new Property().getDomainAndRange(Property.PREDICATE_NAME);

    //   ArrayList<String> samePropertyOfRelation=  samePropertyInType();
// pre-compute the domain and range; sameProperty
  System.out.println(headers);

    }

}
