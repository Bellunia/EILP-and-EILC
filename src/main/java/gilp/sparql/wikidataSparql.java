package gilp.sparql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class wikidataSparql {
    private static final String PREFIX_OWL = " PREFIX owl: <http://www.w3.org/2002/07/owl#>";

    private String prefix() {
        String header = "PREFIX wd: <http://www.wikidata.org/entity/>";
        header += " PREFIX wds: <http://www.wikidata.org/entity/statement/>";
        header += " PREFIX wdv: <http://www.wikidata.org/value/>";
        header += " PREFIX wdt: <http://www.wikidata.org/prop/direct/>";
        header += " PREFIX wikibase: <http://wikiba.se/ontology#>";
        header += " PREFIX p: <http://www.wikidata.org/prop/>";
        header += " PREFIX ps: <http://www.wikidata.org/prop/statement/>";
        header += " PREFIX pq: <http://www.wikidata.org/prop/qualifier/>";
        header += " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
        header += " PREFIX bd: <http://www.bigdata.com/rdf#>";
/**

 header += " PREFIX wdref: <http://www.wikidata.org/reference/>";
 header += " PREFIX psv: <http://www.wikidata.org/prop/statement/value/>";
 header += " PREFIX psn: <http://www.wikidata.org/prop/statement/value-normalized/>";
 header += " PREFIX pqv: <http://www.wikidata.org/prop/qualifier/value/>";
 header += " PREFIX pqn: <http://www.wikidata.org/prop/qualifier/value-normalized/>";
 header += " PREFIX pr: <http://www.wikidata.org/prop/reference/>";
 header += " PREFIX prv: <http://www.wikidata.org/prop/reference/value/>";
 header +=" PREFIX prn: <http://www.wikidata.org/prop/reference/value-normalized/>";
 header +=" PREFIX wdno: <http://www.wikidata.org/prop/novalue/>";
 header +=" PREFIX wdata: <http://www.wikidata.org/wiki/Special:EntityData/>";

 header +=" PREFIX prov: <http://www.w3.org/ns/prov#>";
 header +=" PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";
 header +=" PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";
 header +=" PREFIX owl: <http://www.w3.org/2002/07/owl#>";
 header +=" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
 header +=" PREFIX schema: <http://schema.org/>";
 header +=" PREFIX bds: <http://www.bigdata.com/rdf/search#>";
 header +=" PREFIX gas: <http://www.bigdata.com/rdf/gas#>";
 header +=" PREFIX hint: <http://www.bigdata.com/queryHints#>";
 **/
        return header;
    }

    public String replaceQueryHeader(String header) {
        header = header.replace("http://www.wikidata.org/entity/", "wd:");
        header = header.replace("http://www.wikidata.org/entity/statement/", "wds:");
        header = header.replace("http://www.wikidata.org/value/", "wdv:");

        header = header.replace("http://www.wikidata.org/prop/direct/", "wdt:");
        header = header.replace("http://wikiba.se/ontology#", "wikibase:");
        header = header.replace("http://www.wikidata.org/prop/", "p:");

        header = header.replace("http://www.wikidata.org/prop/statement/", "ps:");
        header = header.replace("http://www.wikidata.org/prop/qualifier/", "pq");
        header = header.replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
        header = header.replace("http://www.bigdata.com/rdf#", "bd:");

        return header;
    }

    public static String execCurl(String[] cmds) {
        ProcessBuilder process = new ProcessBuilder(cmds);
        Process p;
        String builds = null;
        InputStream is = null;
        try {
            p = process.start();
            is = p.getInputStream();
            builds = IOUtils.toString(is, "UTF-8");

//            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            StringBuilder builder = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                System.out.println("test3");
//                    builder.append(line);
//                    builder.append(System.getProperty("line.separator"));
//            }

        } catch (IOException e) {
            System.out.print("error");
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return builds;// builder.toString();
    }

    public ArrayList<String[]> getCommands(String query) {

        //    String[] cmds_test = {"curl", "-H", "Host: www.chineseconverter.com",
        //    "-H", "Cache-Control: max-age=0", "--compressed",
        //    "https://www.chineseconverter.com/zh-cn/convert/chinese-stroke-order-tool"};

        String queryUrls = null;
        try {
            queryUrls = URLEncoder.encode(prefix() + query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        queryUrls = "https://query.wikidata.org/sparql?query=" + queryUrls;

        String[] finalCommands = {"curl", queryUrls,
                // "-x", "socks5://127.0.0.1:1080",
                "-H", "Host: query.wikidata.org",
                "-H", "Cache-Control: max-age=0",
                "-H", "Accept: application/sparql-results+json",
                "-H", "Accept-Language:en-US,en;q=0.5",
                "--compressed",
                "-H", "X-Requested-With: XMLHttpRequest", "-H", "Connection: keep-alive",
                "-H", "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:83.0) Gecko/20100101 Firefox/83.0",
                "-H", "Referer: https://query.wikidata.org/",
                //  "-H","Cookie: WMF-Last-Access-Global=23-Nov-2020; WMF-Last-Access=23-Nov-2020;",// GeoIP=US:CA:Los_Angeles:34.05:-118.25:v4",
                "-H", "TE:Trailers"
        };

        return analysisJasonResults(execCurl(finalCommands));
    }

    public ArrayList<String[]> analysisJasonResults(String jsonData) {
        ArrayList<String[]> resultArray = new ArrayList<String[]>();
        ArrayList<String> varsArray = new ArrayList<String>();

        JSONArray varsEntity = JSON.parseObject(jsonData).getJSONObject("head").getJSONArray("vars");

        int size = varsEntity.size();
        String[] varsStrings = new String[size];
        int j = 0;
        if (!varsEntity.isEmpty()) {
            for (Object jsonObject : varsEntity) {

                varsStrings[j] = (String) jsonObject;
                varsArray.add((String) jsonObject);
                j++;
            }
        }

        resultArray.add(varsStrings);

        JSONArray bindingsEntity = JSON.parseObject(jsonData).getJSONObject("results").getJSONArray("bindings");
        if (!bindingsEntity.isEmpty()) {
            for (Object jsonObject : bindingsEntity) {
                String[] resultStrings = new String[size];
                for (int i = 0; i < size; i++) {
                    JSONObject jsonObject1 = ((JSONObject) jsonObject).getJSONObject(varsArray.get(i));
                    resultStrings[i] = (String) jsonObject1.get("value");
                }
                resultArray.add(resultStrings);

            }
        }
        return resultArray;
    }


    public static void main(String[] args) throws IOException, Exception {

        String predicate1 = "dbo:nationality";
        String sql = "select distinct ?range where{ dbr:Kanami_Tashiro " + predicate1 + " ?range. } ";
        System.out.println(sql);
        ArrayList<String> range = new Sparql().getSingleResultsFromQuery(sql);
        System.out.println(range);
//        String errorQuery = " select ?a where {<"+ range.get(0) + ">  owl:sameAs ?a." +
//                "filter(regex(?a,\"wikidata.org\"))}";
//        System.out.println(errorQuery);

        String test = "select distinct ?a where{<http://www.wikidata.org/entity/Q42884> ?b ?a.}";

        String correctionQuery = "select distinct ?value where{<http://www.wikidata.org/entity/Q42884> p:P31 ?A.\n" +
                // "  ?A ps:P31 wd:Q231002. " +
                "?A pq:P642 ?value. }";

        ArrayList<String[]> results = new wikidataSparql().getCommands(correctionQuery);
        System.out.println("test");
        for (String[] ke : results) {
            for (String k : ke) {
                System.out.println(k);
            }
        }
        System.out.println("test---finally");


    }


}
