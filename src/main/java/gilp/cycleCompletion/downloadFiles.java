package gilp.cycleCompletion;

import gilp.knowledgeClean.RuleLearnerHelper;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class downloadFiles {
    /**
     * download 2021.9.1 dbpedia database from https://www.dbpedia.org/resources/ontology/
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {
        ArrayList<String> queries = allQuery();
        int i = 1;
        for (String query : queries) {
            getPath(query, i);
            HashSet<String> paths = RuleLearnerHelper.readTypes("./downFile/" + i + ".txt");
            for (String url : paths)
                downloadFromUrl(url, "./downFile/");
            i++;
        }
    }

    public static ArrayList<String> allQuery() {
        ArrayList<String> queries = new ArrayList<>();
        String instance_types = "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
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
        queries.add(instance_types);
        String mappingbased_objects = "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?file ?g WHERE {\n" +
                "\tGRAPH ?g {\n" +
                "\t\t?dataset dcat:distribution ?distribution .\n" +
                "\t\t?distribution dataid:file ?file .\n" +
                "\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/mappingbased-objects> .\n" +
                "\t\t{\n" +
                "\t\t\t?distribution dct:hasVersion ?version {\n" +
                "\t\t\t\tSELECT (?v as ?version) { \n" +
                "\t\t\t\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/mappingbased-objects> . \n" +
                "\t\t\t\t\t?dataset dct:hasVersion ?v . \n" +
                "\t\t\t\t} ORDER BY DESC (?version) LIMIT 1 \n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        queries.add(mappingbased_objects);
        String mappingbased_literals = "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?file ?g WHERE {\n" +
                "\tGRAPH ?g {\n" +
                "\t\t?dataset dcat:distribution ?distribution .\n" +
                "\t\t?distribution dataid:file ?file .\n" +
                "\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/mappingbased-literals> .\n" +
                "\t\t{\n" +
                "\t\t\t?distribution dct:hasVersion ?version {\n" +
                "\t\t\t\tSELECT (?v as ?version) { \n" +
                "\t\t\t\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/mappingbased-literals> . \n" +
                "\t\t\t\t\t?dataset dct:hasVersion ?v . \n" +
                "\t\t\t\t} ORDER BY DESC (?version) LIMIT 1 \n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}\n";
        queries.add(mappingbased_literals);
        String specific_mappingbased_properties = "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?file ?g WHERE {\n" +
                "\tGRAPH ?g {\n" +
                "\t\t?dataset dcat:distribution ?distribution .\n" +
                "\t\t?distribution dataid:file ?file .\n" +
                "\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/specific-mappingbased-properties> .\n" +
                "\t\t{\n" +
                "\t\t\t?distribution dct:hasVersion ?version {\n" +
                "\t\t\t\tSELECT (?v as ?version) { \n" +
                "\t\t\t\t\t?dataset dataid:artifact <https://databus.dbpedia.org/dbpedia/mappings/specific-mappingbased-properties> . \n" +
                "\t\t\t\t\t?dataset dct:hasVersion ?v . \n" +
                "\t\t\t\t} ORDER BY DESC (?version) LIMIT 1 \n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}\n";
        queries.add(specific_mappingbased_properties);
        return queries;
    }

    public static void getPath(String query1, int i) throws IOException {
        String query2 = "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
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
        Query query = QueryFactory.create(query1);
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
                    //     System.out.println(" var: " + var + "\n");
                    //    System.out.println(" val: " + val + "\n");
                    element.put(var, val);
                }

                queryResults.add(element);

            }
            qexec.close();

            System.out.println(" Counter Result: " + counter + "\n");


        } catch (Exception e) {
            e.printStackTrace();
        }

        Writer writer = new OutputStreamWriter(
                new FileOutputStream("./downFile/" + i + ".txt"),
                StandardCharsets.UTF_8);
        for (HashMap<String, String> qry : queryResults) {
            for (String key : qry.keySet()) {
                writer.write(qry.get(key) + "\n");
                break;
            }
        }
        writer.close();


    }
    public static String downloadFromUrl(String url, String dir) {

        try {
            URL httpurl = new URL(url);
            String fileName = getFileNameFromUrl(url);
            System.out.println(fileName);
            File f = new File(dir + fileName);
            FileUtils.copyURLToFile(httpurl, f);
        } catch (Exception e) {
            e.printStackTrace();
            return "Fault!";
        }
        return "Successful!";
    }

    public static String getFileNameFromUrl(String url) {
        String name = Long.toString(System.currentTimeMillis()) + ".X";
        int index = url.lastIndexOf("/");
        if (index > 0) {
            name = url.substring(index + 1);
            if (name.trim().length() > 0) {
                return name;
            }
        }
        return name;
    }
}

