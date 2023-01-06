package gilp.sparql;


//import com.sun.security.ntlm.Client;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import javax.crypto.CipherOutputStream;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
//import com.sun.security.ntlm.Client;

public class Test {
    static public void main(String... argv) throws IOException {

        String test="PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?file ?g WHERE {\n" +
                "\tGRAPH ?g {\n" +
                "\t\t?dataset dcat:distribution ?distribution .\n" +
                "\t\t?distribution dataid:file ?file .\n" +
                "\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/instance-types> .\n" +
                "\t\t{\n" +
                "\t\t\t?distribution dct:hasVersion ?version {\n" +
                "\t\t\t\tSELECT (?v as ?version) { \n" +
                "\t\t\t\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/instance-types> . \n" +
                "\t\t\t\t\t?dataset dct:hasVersion ?v . \n" +
                "\t\t\t\t} ORDER BY DESC (?version) LIMIT 1 \n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";



        Query query = QueryFactory.create(test);
        ArrayList<HashMap<String, String>> queryResults = new ArrayList<HashMap<String, String>>();
         String dbpediaWeb = "https://databus.dbpedia.org/repo/sparql";

        // Remote execution.
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaWeb, query)) {
            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "30000");

            int counter = 0;
            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {

                QuerySolution querySolution = resultSet.next();

                Iterator<String> vars = querySolution.varNames();

                counter++;

                HashMap<String, String> element = new HashMap<String, String>();

                // Visual results
                while (vars.hasNext()) {
                    String var = vars.next().toString();
                    String val = querySolution.get(var).toString();
                    System.out.println(" var: " + var + "\n");
                    System.out.println(" val: " + val + "\n");
                    element.put(var, val);
                }

                queryResults.add(element);

            }
            qexec.close();

             System.out.println(" Counter Result: " + counter + "\n");



        } catch (Exception e) {
            e.printStackTrace();
        }

     // new Test().filterType();
    //    new Test(). wikidata2type();

//        HashMap<String,String> id2Labels= readLabels("/home/wy/Downloads/8-7/id2Labels.txt");
//  String path2="/home/wy/Downloads/8-7/entity-type.txt";
//
//        File file = new File("/home/wy/Desktop/new-type1.txt");
//        FileWriter fileReader = new FileWriter(file);
//        BufferedWriter bufferedWriter = new BufferedWriter(fileReader);
//
//        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path2), StandardCharsets.UTF_8));
//
//        String line="";
//        while (true) {
//            try {
//                if ((line = read.readLine()) == null) break;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            String[] line1= line.split("\t");
//            bufferedWriter.write(line1[0]+"\t"+line1[1]+ "\t" +line1[2]);
//int length= line1.length;
//            if(length>3){
//                for(int i=0;i< length-4;i++){
//                    int j= length -3;
//                    bufferedWriter.write("\t"+j+"\t");
//
//
//                   String  one= line1[i+3].replace("http://www.wikidata.org/entity/", "");
//
//                   if(id2Labels.get(one)!=null) {
//                       bufferedWriter.write(id2Labels.get(one).replace(" ", "_"));
//
//                   }
//                   else {
//                       System.out.println(line1[i+3]);
//                       System.out.println(one);
//                   }
//                }
//            }
//            bufferedWriter.write("\n");
//        }
//
//            read.close();




    }



private void wikidata2type() throws IOException {
    HashMap<String,String> id2Labels= readLabels("/home/wy/Downloads/8-7/wikidata/wikidata-type-labels.txt");


    File file = new File("/home/wy/Downloads/8-7/wikidata/wiki64k-entity-wikidata-type.txt");
    FileWriter fileReader = new FileWriter(file);
    BufferedWriter bufferedWriter = new BufferedWriter(fileReader);
    String path2="/home/wy/Downloads/8-7/entity-type-wikidata.txt";
    BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path2), StandardCharsets.UTF_8));

    String line="";
    while (true) {
        try {
            if ((line = read.readLine()) == null) break;
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] line1= line.split("\t");

        int length= line1.length;
        if(length>3){
            int j= length -3;
            bufferedWriter.write(line1[0]+"\t"+line1[1]+ "\t" +line1[2]+"\t"+j+"\t");
            int k=0;
            for(int i=0;i< length-3;i++){
                k++;
                String  one= line1[i+3];

                if(id2Labels.get(one)!=null) {
                    bufferedWriter.write(id2Labels.get(one));
                }
                else {
                    System.out.println(line1[i+3]);
                    System.out.println(one);
                }
                if(k!=length-3)
                    bufferedWriter.write(",");
            }
        }else{
            System.out.println(line);
            bufferedWriter.write(line1[0]+"\t"+line1[1]+ "\t" +line1[2]+"\t"+0);
        }
        bufferedWriter.write("\n");
    }

    read.close();

}



    private HashSet<String> getWikiEnLabels(String type){

            String instanceQuery = "select distinct ?a where{ <" +type + "> rdfs:label ?a. filter (lang(?a) = \"en\")}";
            ArrayList<String[]> resultsOfInstance = new wikidataSparql().getCommands(instanceQuery);
            HashSet<String> finalResults = new HashSet<>();
            for (int i = 1; i < resultsOfInstance.size(); i++) {
                String[] ke = resultsOfInstance.get(i);
                Collections.addAll(finalResults, ke);
            }

          return finalResults;
    }

private void filterType()throws IOException{

    String path2="/home/wy/Downloads/8-7/entity-type-wikidata.txt";
    BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path2), StandardCharsets.UTF_8));
HashSet<String> types= new HashSet<>();
    String line="";
    while (true) {
        try {
            if ((line = read.readLine()) == null) break;
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] line1 = line.split("\t");
        int length = line1.length;
        if (length > 3) {
            types.addAll(Arrays.asList(line1).subList(3, length));
        }
    }
    read.close();
    //-----------------------------------------------
    File file = new File("/home/wy/Downloads/8-7/entity-type-wikidata-id.txt");
    FileWriter fileReader = new FileWriter(file);
    BufferedWriter bufferedWriter = new BufferedWriter(fileReader);

    for(String type :types) {
        bufferedWriter.write( type+"\t");

        HashSet<String>  labels = getWikiEnLabels( type);
        int k=0;
        for(String label :labels) {
            k=k+1;
            bufferedWriter.write(label);
            if(k!= labels.size())
                bufferedWriter.write( "\t");
        }
        bufferedWriter.write(  "\n");
    }
    bufferedWriter.close();
}


//        String sparql="select count ?p where{ ?a ?p ?c . filter(regex(?p,\"http://dbpedia.org/ontology/\"))  } ";
//        HashMap<String, Double> results= new RDF3XEngine().getCountSingleEntity(sparql);
//
//        try {
//            File file = new File("/home/wy/Desktop/relations-counts.txt");
//            FileWriter fileReader = new FileWriter(file); // A stream that connects to the text file
//            BufferedWriter bufferedWriter = new BufferedWriter(fileReader); // Connect the FileWriter to the BufferedWriter
//
//            for (String s : results.keySet()) {
//                bufferedWriter.write(s +"\t"+results.get(s)+ "\n");
//            }
//            bufferedWriter.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        String queryStr = "SELECT ?prop ?place WHERE { <http://dbpedia.org/resource/%C3%84lvdalen> ?prop ?place .}";
//
//        String query = "SELECT ?abs WHERE{<http://dbpedia.org/ontology/Country> dbo:abstract ?abs."
//                + " FILTER (langmatches(lang(?abs), 'en'))}";
//        String dbpediaWeb = "https://dbpedia.org/sparql";
//
//        final String queryDirectorRestrict =
//                "SELECT DISTINCT ?subject ?object WHERE { ?subject rdf:type <http://schema.org/Movie>. ?object rdf:type <http://dbpedia.org/ontology/Person>. ?subject ?relTarget ?realObject. ?realSubject ?relTarget ?object. ?subject ?relation ?object. FILTER (?relTarget = <http://dbpedia.org/ontology/director>) FILTER (?relation != <http://dbpedia.org/ontology/director>) FILTER NOT EXISTS {?subject <http://dbpedia.org/ontology/director> ?object.} }";
//        String instance = "07_Vestur";
//        String sparqlDbpedia = "select distinct ?p where{ ?a ?p <http://dbpedia.org/resource/" + instance + ">.}";
//
//        //	ArrayList<HashMap<String, String>> getResultsFromQuery = new Sparql().getResultsFromQuery(sparqlDbpedia, dbpediaWeb);
//
//        //	HashSet<String> getResultsFromQuery = new Sparql().getSingleResultsFromQuery(sparqlDbpedia, dbpediaWeb);
//
////		String sparql = "select distinct ?p where{ ?a ?p <"+instance+">.}";
////
////		HashSet<String> predicateRDF3x = new RDF3XEngine().getDistinctEntity(sparql);
////
////		System.out.println(predicateRDF3x);
//        String sql = " select distinct ?a where {?b <http://dbpedia.org/ontology/related> " +
//                "<http://dbpedia.org/resource/Slavs>. ?a <http://dbpedia.org/ontology/birthYear>" +
//                " \"1945\"^^<http://www.w3.org/2001/XMLSchema#gYear>. ?a <http://dbpedia.org/ontology/nationality> ?b. }";
//        String sparqlType = "	select distinct ?p where{ ?a a ?p filter(?a= <http://dbpedia.org/resource/Gucci>).}";
//       HashSet<String> getResultsFromQuery = new Sparql().getSingleResultsFromQuery(sql, dbpediaWeb);
//
//       System.out.println(getResultsFromQuery);
//String query_test="SELECT DISTINCT ?item WHERE" +
//        " { <http://www.wikidata.org/entity/Q1196645> <http://www.wikidata.org/prop/direct/P17> ?item  }";
//       new Test().runSparqlQuery(query_test);



    public static HashSet<String> readTypes(String pathToFBfile) {

        HashSet<String> types = new HashSet<>();
        BufferedReader read = null;
        try {
            read = new BufferedReader(new InputStreamReader(
                    new FileInputStream(pathToFBfile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line="";
        while (true) {
            try {
                assert read != null;
                if ((line = read.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            types.add(line);
        }
        try {
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return types;
    }

    public static HashMap<String,String> readLabels(String pathToFBfile) {

        HashMap<String,String> types = new HashMap<>();
        BufferedReader read = null;
        try {
            read = new BufferedReader(new InputStreamReader(
                    new FileInputStream(pathToFBfile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line="";
        while (true) {
            try {
                assert read != null;
                if ((line = read.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(line);
            String[] line1= line.split("\t");
            types.put(line1[0].trim(),line1[1].trim());
        }
        try {
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return types;
    }



    /**
     * Executes a given SPARQL query and returns a stream with the result in JSON
     * format.
     *
     * @param query
     * @return
     * @throws IOException
     */
    public void runSparqlQuery(String query) throws IOException {//InputStream
        try {
            final String banner = "#TOOL:SQID-helper, https:/tools.wmflabs.org/sqid/\n";
            String queryString = "query=" + URLEncoder.encode(banner + query, "UTF-8");
            URL url = new URL("http://query.wikidata.org/sparql?" + queryString);
            System.out.println("Running SPARQL query: `" + url + "'.");
          //  HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//url="https://query.wikidata.org/#SELECT%20DISTINCT%20%3Fitem%20WHERE%20%7B%20%3Chttp%3A%2F%2Fwww.wikidata.org%2Fentity%2FQ1196645%3E%20%3Chttp%3A%2F%2Fwww.wikidata.org%2Fprop%2Fdirect%2FP17%3E%20%3Fitem.%20%20%7D";
         //   URL oracle = new URL("http://www.oracle.com/");
           URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
           // connection.setRequestMethod("GET");
         //   connection.addRequestProperty("Accept", "text/csv"); // JSON leads to timeouts
         //  connection.setRequestProperty("User-Agent", getUserAgent());
            System.out.println("\n ##########" +
                    ":/n" +connection.getInputStream().toString()+ "'.");
         //   return connection.getInputStream();
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    /**
     * Builds a User-Agent string complying with the WDQS User-Agent policy.
     */
    public static String getUserAgent() {
     //   String version = Client.class.getPackage().getImplementationVersion();
        return null;// "wdtk-client/" + version + " (https://github.com/Wikidata/Wikidata-Toolkit)";
    }
}
