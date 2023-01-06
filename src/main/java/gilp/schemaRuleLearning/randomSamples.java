package gilp.schemaRuleLearning;

import gilp.GILLearn_correction.FilterPediaTriples;
import gilp.GILLearn_correction.ILPLearnSettings;
import gilp.GILLearn_correction.Property;
import gilp.knowledgeClean.GILPSettings;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import gilp.utils.KVPair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class randomSamples {

    public void getSamples(String predicate, int number) {
//top-down ,like amie algorithm
        KVPair<HashSet<Triple>, HashSet<Triple>> initialTriples = new FilterPediaTriples().initialFeedback(number);
        HashSet<Triple> positiveTriple1 = initialTriples.getKey();
        HashSet<Triple> negativeTriple = initialTriples.getValue();
        for (Triple pos : positiveTriple1) {
            System.out.println(pos + "\n");
            String sparql = "select distinct ?p where{ <" + pos.get_subject() + "> ?p <" + pos.get_obj() + ">. ?p rdfs:range ?s. }";
            String filterPositiveTripleQuery = "select distinct ?subject ?object from <http://dbpedia.org> where{ ?subject <"
                    + Property.PREDICATE_NAME + "> ?object. ?subject rdf:type <" + Property.DOMAIN + ">."
                    //   + "?object rdf:type <" + Property.RANGE + ">."
                    + "?object rdf:type <" + Property.rangePropertyInwikiData + ">." // only for the nationality
                    + "} ORDER BY RAND() limit " + number;
//System.out.println("positive query:"+filterPositiveTripleQuery);

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

            HashSet<String> distinctPredicates = new RDF3XEngine().getDistinctEntity(sparql);
            distinctPredicates.remove(Property.PREDICATE_NAME);

        }


    }


    static void preprocessData() throws Exception {

        BufferedWriter writer1 = null;

        writer1 = new BufferedWriter(
                new FileWriter("/home/wy/Desktop/schemaRuleLearning/subClassOf-current-schema-simplify.txt"));

        File file = new File("/home/wy/Desktop/schemaRuleLearning/subClassOf-current-schema.txt");

        Scanner scan = new Scanner(file, "UTF-8");
        while (scan.hasNextLine()) {
            String old = scan.nextLine();
            System.out.println(old + "\n");
            String[] line = old.split("\t");

            //   if (line[1].contains("<http://www.w3.org/2000/01/rdf-schema#subClassOf>"))
            writer1.write(line[0].replace("<https://schema.org/", "").replace(">", "") + "\t"
                    + line[2].replace("<https://schema.org/", "").replace(">", "") + "\n");

            System.out.println("test" + "\n");
        }
        scan.close();

        writer1.close();
    }


    public static void main(String[] args) throws Exception {
        String predicate = "nationality";
        int number = 10;
        // new randomSamples().getSamples(predicate,number);

        //    preprocessData();

        String sparql = "select distinct ?subject ?object where{ ?subject owl:equivalentClass ?object. filter(regex(?subject, \"http://dbpedia.org/ontology/\"))}";

        ArrayList<HashMap<String, String>> positiveSubjectOBject = new Sparql()
                .getResultsFromQuery(sparql);

        BufferedWriter writer1 = new BufferedWriter(
                new FileWriter("/home/wy/Desktop/schemaRuleLearning/equivalentClass-dbpedia.txt"));

        for (HashMap<String, String> key : positiveSubjectOBject) {
            String subject = key.get("subject");
            String object = key.get("object");

            writer1.write(subject + "\t" + object + "\n");

        }

        writer1.close();
    }

}
