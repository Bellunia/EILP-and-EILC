package gilp.sparql;

import javatools.parsers.NumberFormatter;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import java.util.HashSet;
import java.util.Iterator;

public class yagoSarql {
    //   https://linkeddata1.calcul.u-psud.fr/sparql

    //补充一个实验，自动的GILP实验

    //----http://yago-knowledge.org/resource/...
    /*
    yago-negative feedback type:<http://yago-knowledge.org/resource/wikicat_Chinese_people>

    predicate- 自定义

     */

    public HashSet<String> getYagoVariable(String queryStr) {


        Query query = QueryFactory.create(queryStr);
        HashSet<String> queryResults = new HashSet<String>();
        String dbpediaWeb = "https://linkeddata1.calcul.u-psud.fr/sparql";

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

    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();

      String query="SELECT distinct ?a WHERE {  ?a  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\t<http://yago-knowledge.org/resource/wikicat_Chinese_people>. ?a <http://yago-knowledge.org/resource/hasGivenName> ?c.}";

        String query1="SELECT distinct ?a WHERE {  ?a  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\t<http://yago-knowledge.org/resource/wikicat_Chinese_people>. }";

String query2="SELECT distinct ?p WHERE {  ?a  ?p ?c. filter(regex(?p,\"http://schema.org/\")) } limit 10";
        HashSet<String> entities= new yagoSarql().getYagoVariable(query2);

        //http://yago-knowledge.org/resource/Zeng_Jiongzhi


        for(String tri: entities){
            System.out.println(tri+"\n");
        }

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

    }

}
