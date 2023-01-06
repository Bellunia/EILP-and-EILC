package Exception;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.opencsv.CSVReader;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;


public class OwlParser {

    public HashSet<Triple> readOwl(String owl, String query1,String property, String originalSourceCSV,String outPath) {

        try {
            OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            File file = new File(owl);
            FileInputStream reader = new FileInputStream(file);
            model1.read(reader, null);

            Query query = QueryFactory.create(query1);// String query1= "SELECT distinct ?a WHERE { ?ind ?a ?b.}";
            QueryExecution exe = QueryExecutionFactory.create(query, model1);
            ResultSet resultSet = exe.execSelect();
            ResultSetFormatter.outputAsCSV(new PrintStream(new FileOutputStream(originalSourceCSV)), resultSet);

        //    ResultSetFormatter.out(System.out, resultSet, query);

            exe.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return readCSV( property, outPath,  originalSourceCSV);

    }

    public HashSet<Triple> readCSV(String property,String outPath, String originalSourceCSV) {

        HashSet<Triple> results= new HashSet<>();
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(outPath), StandardCharsets.UTF_8);
            CSVReader reader = new CSVReader(new FileReader(originalSourceCSV));
            String[] nextLine;
            int iteration = 0;
            while ((nextLine = reader.readNext()) != null) {
                if (iteration == 0) {
                    iteration++;
                    continue;
                }
                results.add(new Triple(nextLine[0],property,nextLine[1] ));

                writer.write("<" + nextLine[0] + ">\t<"+property+">\t<" + nextLine[1] + ">" + "\n");

                System.out.print("<" + nextLine[0] + ">\t<"+property+">\t<" + nextLine[1] + ">" + "\n");

            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

return results;
    }

    public static void testOwl(String owl, String query1) {
        // String query1= "SELECT distinct ?a WHERE { ?ind ?a ?b.}";
        try {
            OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            File file = new File(owl);
            FileInputStream reader = new FileInputStream(file);
            model1.read(reader, null);
            Query query = QueryFactory.create(query1);
            QueryExecution exe = QueryExecutionFactory.create(query, model1);
            ResultSet resultSet = exe.execSelect();
            ResultSetFormatter.out(System.out, resultSet, query);
            exe.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws FileNotFoundException {


        String query1 = "SELECT ?p (COUNT(?p) as ?pCount) WHERE { ?a ?p ?b.} group by ?p";
        String query2 = "SELECT distinct ?a ?c WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?c.}";

        String query_test = "select distinct ?b where{ ?b <http://www.w3.org/2000/01/rdf-schema#domain> <http://dbpedia.org/ontology/Place>." +
                "?b <http://www.w3.org/2000/01/rdf-schema#range> <http://dbpedia.org/ontology/Place>.}";
        String query_subclass = "select * where{ ?a <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?b.}";
        String subPropertyOf_query = "select * where{ ?a <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> ?b.}";
  //      String equal_query = "select * where{ ?a <http://www.w3.org/2002/07/owl#equivalentProperty> ?b.}";
        String query_equal2 = "select * where{ ?a <http://www.w3.org/2002/07/owl#equivalentClass> ?b.}";
        String query_sameAs = "select * where{ ?a <http://www.w3.org/2002/07/owl#sameAs> ?b.}";


//---------------------------------------------------------------------------------------
        String owl_new = "./type/owl/ontology_type=parsed.owl";
        String owl_old = "/home/wy/gilp_learn/type/owl/dbpedia_2016-10.owl";

//        String property="http://www.w3.org/2002/07/owl#equivalentProperty";
//        String originalSourceCSV ="./type/owl/equivalentProperty.csv";
//        String outPath="./type/owl/equivalentProperty1.txt";
//        String equal_query = "select * where{ ?a <http://www.w3.org/2002/07/owl#equivalentProperty> ?b.}";
//
//        HashSet<Triple> elements =
//                new OwlParser().readOwl(owl_new, equal_query,property,originalSourceCSV, outPath);

        String query0 = "SELECT * WHERE { ?a <http://www.w3.org/2002/07/owl#equivalentClass> ?b.} limit 100 ";
        //testOwl(owl_new,query0);
        String query="select distinct ?a where{<http://dbpedia.org/resource/Andijk> a ?a. filter(regex(?a, \"http://dbpedia.org/ontology\"))}";

        HashSet<String> positiveObjects= new Sparql().getSingleVariable(query);
        System.out.println(positiveObjects);
        for(String key : positiveObjects){

            String test_key1="select distinct ?a where{<"+key+">  <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?a. }";
            System.out.println(test_key1);

            testOwl(owl_old,test_key1);

            System.out.println("##################");

            String test_key="select distinct ?b ?c where{ {?b <http://www.w3.org/2002/07/owl#equivalentClass> <"+key+">." +
                    " ?b <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?c.}  " +
                    " union {<"+key+">  <http://www.w3.org/2002/07/owl#equivalentClass> ?b." +
                    " ?b <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?c. } }";
            System.out.println(test_key);
            testOwl(owl_old,test_key);
            System.out.println("********");


        }

    }

}


