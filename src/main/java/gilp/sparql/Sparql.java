package gilp.sparql;

import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * in the Sparql.java, we can search the results by the sparql in the dbpedia online.
 *
 * @author wy
 */

public class Sparql {

    private static final String YAGO_PREFIX_RDF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    private static final String YAGO_PREFIX_RDFS = " PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
//	private static final String YAGO_PREFIX_X = " PREFIX x: <http://www.w3.org/2001/XMLSchema#>";
//	private static final String YAGO_PREFIX_Y = " PREFIX y: <http://mpii.de/yago/resource/>";
//	private static final String YAGO_PREFIX_BASE = " PREFIX base: <http://mpii.de/yago/resource/>";

    private static final String PREFIX_OWL = " PREFIX owl: <http://www.w3.org/2002/07/owl#>";
    private static final String PREFIX_XSD = " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ";

    //	private static final String PREFIX_FOAF = " PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
//	private static final String PREFIX_DC = " PREFIX dc: <http://purl.org/dc/elements/1.1/>";
    private static final String PREFIX_RES = " PREFIX dbr: <http://dbpedia.org/resource/>";
    private static final String PREFIX_DBP = " PREFIX dbp: <http://dbpedia.org/property/>";
    private static final String PREFIX_DBO = " PREFIX dbo: <http://dbpedia.org/ontology/>";
//	private static final String PREFIX_dbpedia = " PREFIX dbpedia: <http://dbpedia.org/>";
//	private static final String PREFIX_skos = " PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";

	private static final String PREFIX_wdt = " prefix wdt: <http://www.wikidata.org/prop/direct/>";
	private static final String PREFIX_wd = " prefix wd: <http://www.wikidata.org/entity/>";

    // to generate a query header when KB has prefix
    private String getQueryHeader() {
        //select the general prefix
        String header = Sparql.YAGO_PREFIX_RDF;
        header += Sparql.YAGO_PREFIX_RDFS;
        //	header += Sparql.YAGO_PREFIX_X;
        //	header += Sparql.YAGO_PREFIX_Y;
        //	header += Sparql.YAGO_PREFIX_BASE;
        header += Sparql.PREFIX_OWL;
        header += Sparql.PREFIX_XSD;
        //	header += Sparql.PREFIX_FOAF;
        //	header += Sparql.PREFIX_DC;
        header += Sparql.PREFIX_RES;
        header += Sparql.PREFIX_DBP;
        header += Sparql.PREFIX_DBO;
        //	header += Sparql.PREFIX_dbpedia;
        //	header += Sparql.PREFIX_skos;
		header += Sparql.PREFIX_wdt;
		header += Sparql.PREFIX_wd;

        return header;
    }

    public String replaceQueryHeader(String header) {
        header = header.replace("http://dbpedia.org/ontology/", "dbo:");
        header = header.replace("http://dbpedia.org/property/", "dbp:");
        header = header.replace("http://dbpedia.org/resource/", "dbr:");
        //	header = header.replace("http://purl.org/dc/elements/1.1/","dc:");
        header = header.replace("http://xmlns.com/foaf/0.1/", "foaf:");
        header = header.replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
        header = header.replace("http://www.w3.org/2002/07/owl#", "owl:");
        //	header = header.replace("http://mpii.de/yago/resource/","base:");
        //	header = header.replace("http://mpii.de/yago/resource/","y:");
        //	header = header.replace("http://www.w3.org/2001/XMLSchema#","x:");
        header = header.replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
        header = header.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
		header = header.replace("http://www.wikidata.org/prop/direct/", "wdt:");
		header = header.replace("http://www.wikidata.org/entity/", "wd:");
        //	header = header.replace("http://www.w3.org/2004/02/skos/core#","skos:");
        //	header = header.replace("http://www.wikidata.org/prop/direct/","wdt:");
        //	header = header.replace("http://www.wikidata.org/entity/","wd:");

        return header;
    }

    public ArrayList<HashMap<String, String>> getResultsFromQuery(String queryStr, String dbpediaWeb) {
        // String queryStr = "SELECT ?prop ?place WHERE {
        // <http://dbpedia.org/resource/%C3%84lvdalen> ?prop ?place .}";
        queryStr = getQueryHeader() + queryStr;
        // System.out.println(queryStr);

        Query query = QueryFactory.create(queryStr);
        ArrayList<HashMap<String, String>> queryResults = new ArrayList<HashMap<String, String>>();
        // String dbpediaWeb = "https://dbpedia.org/sparql";

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

                    element.put(var, val);
                }

                queryResults.add(element);

            }
            qexec.close();

            // System.out.println(" Counter Result: " + counter + "\n");

//            ResultSetFormatter.out(System.out, rs, query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResults;
    }



    public ArrayList<HashMap<String, String>> getResultsFromQuery(String queryStr) {
        // String queryStr = "SELECT ?prop ?place WHERE {
        // <http://dbpedia.org/resource/%C3%84lvdalen> ?prop ?place .}";
        queryStr = getQueryHeader() + queryStr;
        // System.out.println(queryStr);

        Query query = QueryFactory.create(queryStr);
        ArrayList<HashMap<String, String>> queryResults = new ArrayList<HashMap<String, String>>();
        String dbpediaWeb = "https://dbpedia.org/sparql";

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

                    element.put(var, val);
                }

                queryResults.add(element);

            }
            try {
                qexec.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // System.out.println(" Counter Result: " + counter + "\n");

//            ResultSetFormatter.out(System.out, rs, query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResults;
    }

    public String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * * select distinct ?x (count(?x) as ?xcount) where{?a
     * <http://dbpedia.org/ontology/birthPlace> ?b.?b a ?x." + "
     * FILTER(regex(str(?x),\"http://dbpedia.org/ontology/\"))} GROUP BY ?x ORDER BY
     * DESC(?xcount) limit 10
     * <p>
     * [{http://dbpedia.org/ontology/Place=134396.0},
     * {http://dbpedia.org/ontology/Location=134084.0},.....
     *
     * @param queryStr
     * @return
     */
    public ArrayList<HashMap<String, Double>> countResultsFromQuery(String queryStr) {

        queryStr = getQueryHeader() + queryStr;

        Query query = QueryFactory.create(queryStr);
        ArrayList<HashMap<String, Double>> queryResults = new ArrayList<HashMap<String, Double>>();
        String dbpediaWeb = "https://dbpedia.org/sparql";

        // Remote execution.
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaWeb, query)) {
            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "30000");// 30000
//((QueryEngineHTTP) qexec).addParam("timeout", "10000"); // the old codes

            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {

                HashMap<String, Double> element = new HashMap<String, Double>();

                QuerySolution querySolution = resultSet.next();

                Iterator<String> vars = querySolution.varNames();

                String value = null;
                Double number = 0.0;

                // Visual results
                while (vars.hasNext()) {
                    String var = vars.next().toString();

                    String val = querySolution.get(var).toString();

                    // System.out.println(" val Result: " + val + "\n");

                    String numberString = getNumbers(val);

                    if (!numberString.isEmpty()) {
                        number = Double.parseDouble(numberString);

                    } else {
                        value = val;
                    }

                }
                element.put(value, number);

                queryResults.add(element);

            }
            qexec.close();

//            ResultSetFormatter.out(System.out, rs, query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResults;
    }

    public HashMap<String, Double> countResults(String queryStr) {

        queryStr = getQueryHeader() + queryStr;

        Query query = QueryFactory.create(queryStr);
        HashMap<String, Double> queryResults = new HashMap<String, Double>();
        String dbpediaWeb = "https://dbpedia.org/sparql";

        // Remote execution.
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaWeb, query)) {
            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "30000");// 30000
//((QueryEngineHTTP) qexec).addParam("timeout", "10000"); // the old codes

            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {

                HashMap<String, Double> element = new HashMap<String, Double>();

                QuerySolution querySolution = resultSet.next();

                Iterator<String> vars = querySolution.varNames();

                String value = null;
                double number = 0.0;

                // Visual results
                while (vars.hasNext()) {
                    String var = vars.next().toString();

                    String val = querySolution.get(var).toString();

                    // System.out.println(" val Result: " + val + "\n");

                    String numberString = getNumbers(val);

                    if (!numberString.isEmpty()) {
                        number = Double.parseDouble(numberString);

                    } else {
                        value = val;
                    }

                }
                element.put(value, number);

                queryResults.putAll(element);

            }


            qexec.close();

//            ResultSetFormatter.out(System.out, rs, query);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return queryResults;
    }


    public  Double countNumbers(String queryStr) {

        queryStr = getQueryHeader() + queryStr;
        Query query = QueryFactory.create(queryStr);
        String dbpediaWeb = "https://dbpedia.org/sparql";

        double number = 0.0;

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaWeb, query)) {
            ((QueryEngineHTTP) qexec).addParam("timeout", "30000");

            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {

                QuerySolution querySolution = resultSet.next();

                Iterator<String> vars = querySolution.varNames();

                while (vars.hasNext()) {
                    String var = vars.next().toString();

                    String val = querySolution.get(var).toString();

                    String numberString = getNumbers(val);

                    if (!numberString.isEmpty()) {
                        number = Double.parseDouble(numberString);
                    }
                }
            }
            qexec.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return number;
    }


    public HashSet<String> getSingleResultsFromQuery(String queryStr, String dbpediaWeb) {
        // String queryStr = "SELECT ?prop ?place WHERE {
        // <http://dbpedia.org/resource/%C3%84lvdalen> ?prop ?place .}";
        queryStr = getQueryHeader() + queryStr;
        // System.out.println(queryStr);

        Query query = QueryFactory.create(queryStr);
        HashSet<String> queryResults = new HashSet<String>();
        // String dbpediaWeb = "http://dbpedia.org/sparql";

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

                // HashMap<String, String> element = new HashMap<String, String>();

                // Visual results
                while (vars.hasNext()) {
                    String var = vars.next().toString();
                    String val = querySolution.get(var).toString();// values
                    queryResults.add(val);

                    // element.put(var, val);
                }

                // queryResults.add(element);

            }
            qexec.close();
            // System.out.println(" Counter Result: " + counter + "\n");

//            ResultSetFormatter.out(System.out, rs, query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResults;

        // String test="select * where{<http://dbpedia.org/resource/Andy_Dornan>
        // <http://dbpedia.org/ontology/birthPlace> ?b.}";
        // HashSet <String> getSingleResultsFromQuery =new
        // Sparql().getSingleResultsFromQuery( test, "https://dbpedia.org/sparql");
        // System.out.println(" Counter Result: " + getSingleResultsFromQuery + "\n");
// Counter Result: [http://dbpedia.org/resource/Scotland, http://dbpedia.org/resource/Aberdeen_F.C.]
    }

    public HashSet<String> getSingleVariable(String queryStr) {

        queryStr = getQueryHeader() + queryStr;

        Query query = QueryFactory.create(queryStr);
        HashSet<String> queryResults = new HashSet<String>();
        String dbpediaWeb = "https://dbpedia.org/sparql";

        // Remote execution.
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaWeb, query)) {
            // Set the DBpedia specific timeout.
            ((QueryEngineHTTP) qexec).addParam("timeout", "30000");


            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {

                QuerySolution querySolution = resultSet.next();

                Iterator<String> vars = querySolution.varNames();


                while (vars.hasNext()) {
                    String var = vars.next().toString();
                    String val = querySolution.get(var).toString();// values
                    queryResults.add(val);

                }

            }
            qexec.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResults;

    }

    public ArrayList<String> getSingleResultsFromQuery(String queryStr) {
// one results use get(0)
        queryStr = getQueryHeader() + queryStr;

        String dbpediaWeb = "https://dbpedia.org/sparql";

        Query query = QueryFactory.create(queryStr);
        ArrayList<String> queryResults = new ArrayList<String>();

        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaWeb, query)) {

            ((QueryEngineHTTP) qexec).addParam("timeout", "30000");

            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {

                QuerySolution querySolution = resultSet.next();

                Iterator<String> vars = querySolution.varNames();

                while (vars.hasNext()) {
                    String var = vars.next().toString();

                    String val = querySolution.get(var).toString();// values

                    queryResults.add(val);

                }

            }
            qexec.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResults;

    }



}
