package gilp.GILLearn_correction;

import gilp.knowledgeClean.RuleInYago;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Rule;
import javatools.parsers.NumberFormatter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Scanner;

public class yagoCorrection {
    /*
    wordnet in the type property, in the yago, we consider the isCitizenOf to inference the country.
    isCitizenOf Paris  => nationality France
    Compare the wikidata:Q6256, we find the false positive cities, true positive is the country name.
     */

    public void getYagoFeedback() throws Exception {
        String query1= "select distinct ?b where{ ?a <isCitizenOf> ?b. ?b <rdf:type> ?c. filter(regex(?c,\"Countries\"))}";
        HashSet<String> Countries = new RDF3XEngine().getDistinctEntity(query1);
        writeCorrection(Countries,"Countries");

        String query= "select distinct ?b where{ ?a <isCitizenOf> ?b. ?b <rdf:type> ?c. filter(regex(?c,\"countries\"))}";
        HashSet<String> countries = new RDF3XEngine().getDistinctEntity(query);
           writeCorrection(countries,"countries");

       String query2= "select distinct ?b where{ ?a <isCitizenOf> ?b. ?b <rdf:type> <wikicat_Countries>.}";
        HashSet<String> wikicat_Countries = new RDF3XEngine().getDistinctEntity(query2);
        writeCorrection(wikicat_Countries,"wikicat_Countries");

        String query3= "select distinct ?b where{ ?a <isCitizenOf> ?b. ?b <rdf:type> <wordnet_country_108544813>.}";
        HashSet<String> wordnet_country_108544813 = new RDF3XEngine().getDistinctEntity(query3);
       writeCorrection(wordnet_country_108544813,"wordnet_country_108544813");

        String query4= "select distinct ?b where{ ?a <isCitizenOf> ?b. ?b <rdf:type> ?c. filter(regex(?c,\"country\"))}";
        HashSet<String> country = new RDF3XEngine().getDistinctEntity(query4);
        writeCorrection(country,"country");

     //   String query5= "select distinct ?b where{ ?a <isCitizenOf> ?b. ?b <rdf:type> ?c. filter(regex(?c,\"Country\"))}";--0
        String query6= "select distinct ?b where{ ?a <isCitizenOf> ?b. }";
        HashSet<String> AllCountry = new RDF3XEngine().getDistinctEntity(query6);
        writeCorrection(AllCountry,"AllCountry");

    }

    public void writeCorrection(HashSet<String> values,String name) throws IOException {
        Writer writer2 = new OutputStreamWriter(
                new FileOutputStream("./data/yago_correction/"+name+".txt"),
                Charset.forName("UTF-8"));
        for (String val : values)
            writer2.write(val  + "\n");
        writer2.close();
    }

    public void filterYagoFeedback(){
        HashSet<String>  AllCountry = RuleLearnerHelper.readTypes("./data/yago_correction/isCitizenOf/AllCountry.txt");// all entity
        HashSet<String>  wordnet_country = RuleLearnerHelper.readTypes("./data/yago_correction/isCitizenOf/wordnet_country_108544813.txt");//positive
        HashSet<String>  negative_country = RuleLearnerHelper.readTypes("./data/yago_correction/isCitizenOf/negative-country.txt");//negative
        HashSet<String>  falsePositive_country = RuleLearnerHelper.readTypes("./data/yago_correction/isCitizenOf/false-positive-country.txt");// false
        HashSet<String>  truePositive_country = RuleLearnerHelper.readTypes("./data/yago_correction/isCitizenOf/truePositive-country.txt");

    }

    public static HashSet<Triple> readTriples(String path) throws Exception {

        HashSet<Triple> triples = new HashSet<>();

        Scanner scanner = new Scanner(path);
        File file = new File(scanner.nextLine());
        Scanner input = new Scanner(file);

        while (input.hasNextLine()) {
            String line1 = input.nextLine();

            String[] line = line1.split("\t");
            if(line.length==3) {

                String subject, predicate, object;
                subject = line[0];
                predicate = line[1];
                object = line[2];

                Triple triple = new Triple(subject, predicate, object);

                triples.add(triple);
            }else
                System.out.println(line1);

        }
        input.close();
        scanner.close();

        return triples;
    }

    public void filterYagoTriples(String predicate) throws IOException {
       // String query="select distinct ?a ?b where{ ?a <isCitizenOf> ?b.}";
        HashSet<Triple> allTriples= new RDF3XEngine().getTriplesByPredicate(predicate);
        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./data/yago_correction/isCitizenOf-allTriples.txt"),
                Charset.forName("UTF-8"));
        for (Triple val : allTriples)
            writer.write(val  + "\n");
        writer.close();

    }




    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();
        HashSet<Rule> posQualifiedRules = new HashSet<Rule>();
        HashSet<Rule> negQualifiedRules = new HashSet<Rule>();
        int initialNumbers=100;
        int selectNumber=100;
      // new yagoCorrection().getYagoFeedback();

        HashSet<String> finalAllPredictions = new RuleInYago().getAllRules(initialNumbers,posQualifiedRules,
						negQualifiedRules, selectNumber);

        String predicate="isCitizenOf";
      //  new yagoCorrection().filterYagoTriples(predicate);
        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));


    }

}
