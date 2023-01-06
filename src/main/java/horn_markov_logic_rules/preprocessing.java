package horn_markov_logic_rules;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class preprocessing {

    public static LinkedHashMap<String, Double> topProperties = null;

    public static Double MIN_CONFIDENCE = 0.001;
    public static Integer N_PROPERTIES = 100;//100
    public static Integer N_TRIANGLES = 10;

    public static  InferType fun= InferType.M;//(maximum)--function
    // 'A' (average), 'M' (maximum), 'P' (opp.product)


    public LinkedHashMap<String, Double> top_properties() {
        String TOP_PROPERTIES = "select count ?q  where { ?x ?q ?y. filter(regex(?q,\"ontology\")) } ";
        System.out.println("query_SIMPLE_RULES:  " + TOP_PROPERTIES);
        HashMap<String, Double> others = new RDF3XEngine().getCountSingleEntity(TOP_PROPERTIES);
        System.out.println("others  " + others.size());

        LinkedHashMap<String, Double> reverseResults = (LinkedHashMap<String, Double>) new RuleLearnerHelper().reverseOrderByValue(others);

        //---backup all original predicates
        String file_name = "./knowledgeClean-data/horn_rule/top_properties-ontology.tsv";
        try {
            File f = new File(file_name);
            if (!f.exists()) {    // if file does not exist, then create it
                File dir = new File(f.getParent());
                dir.mkdirs();
                f.createNewFile();
            }
            FileWriter writer = new FileWriter(file_name, Charset.forName("UTF-8"), true);

            for (String r_key : reverseResults.keySet()) {
                double r_val = reverseResults.get(r_key);
                //    if(r_key.contains("ontology")) {//str.contains("ontology")

                System.out.println("*** new predicate mining FOUND! ***");
                writer.write(r_key + "\t" + r_val + "\n");
                //     writer.write("----\t"+System.currentTimeMillis()+"\n");
                System.out.println(r_key + "\t" + r_val + "\n");
                //  }

            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return reverseResults;

    }

    public static LinkedHashMap<String, Double> filterTopProperty(){

        LinkedHashMap<String, Double> tp = preprocessing.readTopProperties();
        LinkedHashMap<String, Double> filterResults = new LinkedHashMap<>();

        int i = 0;
        for (String key : tp.keySet()) {
            if (i < N_PROPERTIES) {
                filterResults.put(key, tp.get(key));
                i++;
            } else
                break;
        }

        filterResults.remove("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        filterResults.remove("http://xmlns.com/foaf/0.1/primaryTopic");
        filterResults.remove("http://purl.org/dc/elements/1.1/language");
        filterResults.remove("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");

        return filterResults;

    }

    public static LinkedHashMap<String, Double> readTopProperties() {
        String path = "./knowledgeClean-data/horn_rule/dbpedia-2016-04/top_properties-ontology.tsv";
        if (topProperties == null) {

            try (BufferedReader TSVReader = new BufferedReader(new FileReader(path))) {
                String line = null;
                while ((line = TSVReader.readLine()) != null) {
                    String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
                    topProperties.put(lineItems[0], Double.parseDouble(lineItems[1]));
                }
            } catch (Exception e) {
                System.out.println("Something went wrong");
            }
        }
        return topProperties;
    }
}
