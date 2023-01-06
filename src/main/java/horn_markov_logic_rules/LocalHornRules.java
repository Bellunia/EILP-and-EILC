package horn_markov_logic_rules;

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class LocalHornRules {

    /**
     * path-based algorithm to mine horn rules in 6 types
     * - Mining Horn clauses in RDF datasets using RDF3Xengine-dbpedia.
     */


    public HashMap<String, Double> simple_rules(String q) {
        String SIMPLE_RULES = "select count ?p where { ?x ?p ?y . ?x <" + q + "> ?y . " +
                "filter(?p != <" + q + ">)  }";
        System.out.println("query_SIMPLE_RULES:  " + SIMPLE_RULES);

        HashMap<String, Double> countRules = new RDF3XEngine().getCountSingleEntity(SIMPLE_RULES);

        System.out.println("others  " + countRules.size());

        return countRules;

    }

    public HashMap<String, Double> type_two_rules(String q) {
        String TYPE_2_RULES = "select count ?p where { ?y ?p ?x . ?x <" + q + "> ?y.} ";
        //   +"filter(regex(?p, \\\"http://dbpedia.org/ontology/\\\")) } GROUP BY ?p ORDER BY DESC(?c)";
        System.out.println("query_SIMPLE_RULES:  " + TYPE_2_RULES);
        HashMap<String, Double> others = new RDF3XEngine().getCountSingleEntity(TYPE_2_RULES);
        System.out.println("others  " + others.size());

        return others;
    }


    public Double getNumberFromString(String subject) {
        String changedThing = null;
        double number = 0.0;
        if (isNumeric(subject))
            number = Double.parseDouble(subject);

        if (subject.contains("\\^\\^")) {
            String z = subject.substring(1).split("\\^\\^")[0];
            number = Double.parseDouble(z);
        }
        return number;
    }


    private static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    public HashMap<String[], Double> triangles(int t, String p) {

        String[] tri = new String[4];
        tri[0] = "?x ?q ?z . ?z ?r ?y . ";
        tri[1] = "?x ?q ?z . ?y ?r ?z . ";
        tri[2] = "?z ?q ?x . ?z ?r ?y . ";
        tri[3] = "?z ?q ?x . ?y ?r ?z . ";

        //   String countSql = "select count ?b ?p where{?a <rdftype> ?b. ?a ?p ?c. filter(?p != <rdftype> && ?p != <wikiPageDisambiguates>)} ";
        //   HashMap<ArrayList<String>, Double> allResources = new RDF3XEngine().getCountDoubleEntity(countSql);

        String TRIANGLES = "SELECT count ?q ?r  WHERE { " + tri[t] + "?x <" + p + "> ?y ." +
                //    "filter(regex(?q, \"http://dbpedia.org/ontology/\") || regex(?r, \"http://dbpedia.org/ontology/\"))" +
                "} ";

//        String TRIANGLES = "SELECT ?q ?r (COUNT(*) AS ?c) WHERE { " + tri[t] + "?x <" + p + "> ?y ." +
//                "filter(regex(?q, \"http://dbpedia.org/ontology/\") || regex(?r, \"http://dbpedia.org/ontology/\"))" +
//                "} " +
//                "GROUP BY ?q ?r ORDER BY DESC(?c) LIMIT " + N_TRIANGLES;

        String query_test = "SELECT ?q ?r (COUNT(*) AS ?c) WHERE { ?x ?q ?z . ?z ?r ?y . ?x dbo:nationality ?y .} " +
                " GROUP BY ?q ?r ORDER BY DESC(?c) LIMIT 10";

        System.out.println("TRANGLES-QUERY:" + TRIANGLES + "\n");
        // rules[(str(result["q"]["value"]), str(result["r"]["value"]))] = int(result["c"]["value"])

        HashMap<ArrayList<String>, Double> items = new RDF3XEngine().getCountDoubleEntity(TRIANGLES);

        System.out.println("items-QUERY:" + items.size() + "\n");
        HashMap<String[], Double> triangles = new HashMap<>();

        if (!items.isEmpty()) {
            for (ArrayList<String> key : items.keySet()) {

                double number = items.get(key);
                String[] pairs = key.toArray(new String[0]);
                triangles.put(pairs, number);

            }
            System.out.println("triangles:" + triangles + "\n");
        }
        return triangles;
    }

    public static char getChar(String str, int index) {
        return str.charAt(index);
    }

    public Double adjacencies(int t, String[] k) {
        String[] nodes = {"xzzy", "xzyz", "zxzy", "zxyz"};
        char ch = getChar(nodes[t], 0);
        String ADJACENCIES = "select count ?" + getChar(nodes[t], 0) +
                " WHERE { ?" + getChar(nodes[t], 0) + " <" + k[0] + "> ?" + getChar(nodes[t], 1)
                + ". ?" + getChar(nodes[t], 2) + " <" + k[1] + "> ?" + getChar(nodes[t], 3) + ". }";
        System.out.println("Querying:" + ADJACENCIES + "\n");

        HashMap<String, Double> others = new RDF3XEngine().getCountSingleEntity(ADJACENCIES);
        System.out.println("others  " + others.size());
        if (!others.isEmpty()) {

            Map.Entry<String, Double> entry = others.entrySet().iterator().next();

            return entry.getValue();
        } else
            return 0.0;

    }

    public Boolean write_rule(int t, double c, String p, String q) {//python-fopen(a), add new rules in files
        String[] files = {"pxy-qxy", "pxy-qyx"};
        String[] args = {"(x,y)", "(y,x)"};
        boolean worth = false;

        String file_name = "./knowledgeClean-data/horn_rule/rules-" + files[t] + ".tsv";
        try {
            File f = new File(file_name);
            if (!f.exists()) {    // if file does not exist, then create it
                File dir = new File(f.getParent());
                dir.mkdirs();
                f.createNewFile();
            }
            FileWriter writer = new FileWriter(file_name, Charset.forName("UTF-8"), true);
            if (c > preprocessing.MIN_CONFIDENCE) {
                writer.write(c + "\t" + p + "\t(x,y)\t" + q + "\t" + args[t] + "\n");
                worth = true;
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return worth;
    }


    public Boolean write_rule_3(int t, double c, String p, String q, String r) {


        String[] files = {"pxy-qxz-rzy", "pxy-qxz-ryz", "pxy-qzx-rzy", "pxy-qzx-ryz"};
        String[][] args = {{"(x,z)", "(z,y)"}, {"(x,z)", "(y,z)"}, {"(z,x)", "(z,y)"}, {"(z,x)", "(y,z)"}};
        boolean worth = false;

        String file_name = "./knowledgeClean-data/horn_rule/rules-" + files[t] + ".tsv";

        try {
            File f = new File(file_name);
            if (!f.exists()) {    // if file does not exist, then create it
                File dir = new File(f.getParent());
                dir.mkdirs();
                f.createNewFile();
            }
            FileWriter writer = new FileWriter(file_name, Charset.forName("UTF-8"), true);
            if (c > preprocessing.MIN_CONFIDENCE) {
                writer.write(c + "\t" + p + "\t(x,y)\t" + q + "\t" + args[t][0] + "\t" + r + "\t" + args[t][1] + "\n");
                worth = true;
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return worth;
    }

    public static void write_titles() {
        String[] files = {"pxy-qxy", "pxy-qyx", "pxy-qxz-rzy", "pxy-qxz-ryz", "pxy-qzx-rzy", "pxy-qzx-ryz"};
        int len = files.length;
        for (int t = 0; t < len; t++) {



            String file_name = "./knowledgeClean-data/horn_rule/rules-" + files[t] + ".tsv";

            try {

                File f = new File(file_name);
                if (!f.exists()) {    // if file does not exist, then create it
                    File dir = new File(f.getParent());
                    dir.mkdirs();
                    f.createNewFile();

                    Writer  writer = new OutputStreamWriter(new FileOutputStream(file_name), Charset.forName("UTF-8"));
                    if (t < 2) {
                        writer.write("weight\tp\t(?,?)\tq\t(?,?)\n");//unicode in python2
                    } else {

                        writer.write("weight\tp\t(?,?)\tq\t(?,?)\tr\t(?,?)\n");
                    }

                    writer.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //############################### ALGORITHM ################################
    public String VERSION = "0.0.2";

    public void algorithm() {
        System.out.println("Horn Concerto v" + VERSION + ".");
        System.out.println("Endpoint: " + "http://dbpedia.org/sparql" + "\n" +
                "Graph: " + "http://dbpedia.org" + "\n" +
                "Min_Confidence: \n" + preprocessing.MIN_CONFIDENCE + "\n" +
                "N_Properties: \n" + preprocessing.N_PROPERTIES + "\n" +
                "N_Triangles: \n" + preprocessing.N_TRIANGLES + "\n" +
                "Output_Folder:" + "./knowledgeClean-data/horn_rule/\n");
        String Output_Folder = "./knowledgeClean-data/horn_rule";

        write_titles();//--只有第一次运行可用，提供文件title，其余时候不用

        LinkedHashMap<String, Double> tp = preprocessing.filterTopProperty();// filter     int limit = N_PROPERTIES; top property.

        String[] types = {
                "I: p(x,y) <= q(x,y)",
                "II: p(x,y) <= q(y,x)",
                "III: p(x,y) <= q(x,z), r(z,y)",
                "IV: p(x,y) <= q(x,z), r(y,z)",
                "V: p(x,y) <= q(z,x), r(z,y)",
                "VI: p(x,y) <= q(z,x), r(y,z)"};

        String[] body1 = {"(x,y)", "(y,x)"};

        String[][] body2 = {
                {"(x,z)", " (z,y)"},
                {"(x,z)", "  (y,z)"},
                {"(z,x)", "  (z,y)"},
                {"(z,x)", "  (y,z)"}};

//outer loop
        for (int i = 0; i < types.length; i++) {
            //   for i in range(len(types)) {
            System.out.println("Rules of type" + types[i]);
            //there might exist p_1, p_2 such that:p_i(x, y) <= q( ?,?),r( ?,?)
            // # shared dictionary
            HashMap<String[], Double> adj_dict = new HashMap<>();

            HashMap<String, Double> reverseResults = new RuleLearnerHelper().reverseOrderByValue(tp);
            //inner loop

            for (String tp_key : reverseResults.keySet()) {//for tp_key, tp_val in sort_by_value_desc (tp):
                double tp_val = reverseResults.get(tp_key);
                System.out.println("Processing:" + tp_key + "\t" + tp_val);//System.out.println( "Processing:", tp_key, tp_val);
                HashMap<String, Double> r = new HashMap<>();
                if (i < 2) { //p - q rules
                    if (i == 0) //p(x, y) <= q(x, y)
                        r = simple_rules(tp_key);
                    else { //p(x, y) <= q(y, x)
                        r = type_two_rules(tp_key);
                    }
                    HashMap<String, Double> reverseRules = new RuleLearnerHelper().reverseOrderByValue(r);
                    for (String r_key : reverseRules.keySet()) {    //for r_key, r_val in sort_by_value_desc (r):
                        double r_val = reverseRules.get(r_key);

                        System.out.println(r_key + "\t" + r_val);
                        System.out.println("*** RULE FOUND! ***");

                        double c = (r_val) / (tp_val);
                        System.out.println("c = " + c + "\t" + r_key + " (x,y) <= " + tp_key + " " + body1[i] + "");
                        Boolean worth = write_rule(i, c, r_key, tp_key);
                        if (!worth)
                            break;
                    }
                } else {//p - q - r rules
                    int j = i - 2; //p - q - r rule index
                    HashMap<String[], Double> triang = triangles(j, tp_key);
                    HashMap<String[], Double> reverseTriang = new RuleLearnerHelper().reverseOrderByValue(triang);
                    for (String[] k : reverseTriang.keySet()) {//  for k, v in sort_by_value_desc (triang):
                        double v = reverseTriang.get(k);

                        System.out.println(k + "\t" + v);
                        Double adj = 0.0;
                        if (adj_dict.containsKey(k)) { //  if k in adj_dict:
                            System.out.println("Value found in dictionary:" + k);
                            adj = adj_dict.get(k);
                        } else {
                            adj = adjacencies(j, k);
                        }

                        if (adj == 0.0)
                            continue;

                        double c = v / adj;
                        System.out.println("*** RULE FOUND! ***");
                        System.out.println("c = " + c + "\t" + tp_key + " (x,y) <= " + k[0] + " " + body2[j][0] + " ^ " + k[1] + " " + body2[j][1] + "");
                        Boolean worth = write_rule_3(j, c, tp_key, k[0], k[1]);
                        if (!worth)
                            break;
                        adj_dict.put(k, adj);

                    }
                }
            }
            System.out.println("Done.");
            System.out.println("\nRules saved in files " + Output_Folder + "/rules-*.tsv");


        }
    }

    //   # outer loop
//#for i in range(len(types)):
    //           # outer loop Parallel
    public void rangeTypes(int i) {//---val
        //   # print "Rules of type", types[i]
        //    # there might exist p_1,p_2 such that: p_i(x,y) <= q(?,?), r(?,?)
        //     # shared dictionary
        HashMap<String[], Double> adj_dict = new HashMap<>();
        //  # inner loop
        HashMap<String, Double> tp = preprocessing.filterTopProperty();
        HashMap<String, Double> reverseResults = new RuleLearnerHelper().reverseOrderByValue(tp);

        for (String tp_key : reverseResults.keySet()) {//for tp_key, tp_val in sort_by_value_desc (tp):
            double tp_val = reverseResults.get(tp_key);
            System.out.println("Processing:" + tp_key + "\t" + tp_val);//System.out.println( "Processing:", tp_key, tp_val);
            HashMap<String, Double> r = new HashMap<>();
            if (i < 2) { //p - q rules
                if (i == 0) //p(x, y) <= q(x, y)
                    r = simple_rules(tp_key);
                else { //p(x, y) <= q(y, x)
                    r = type_two_rules(tp_key);
                }
                HashMap<String, Double> reverseRules = new RuleLearnerHelper().reverseOrderByValue(r);
                for (String r_key : reverseRules.keySet()) {    //for r_key, r_val in sort_by_value_desc (r):
                    double r_val = reverseRules.get(r_key);

                    System.out.println(r_key + "\t" + r_val);
                    System.out.println("*** RULE FOUND! ***");

                    double c = (r_val) / (tp_val);
                    //   System.out.println("c = " + c + "\t" + r_key + " (x,y) <= " + tp_key + " " + body1[i] + "");
                    Boolean worth = write_rule(i, c, r_key, tp_key);
                    if (!worth)
                        break;
                }
            } else {//p - q - r rules
                int j = i - 2; //p - q - r rule index
                HashMap<String[], Double> triang = triangles(j, tp_key);
                HashMap<String[], Double> reverseTriang = new RuleLearnerHelper().reverseOrderByValue(triang);
                for (String[] k : reverseTriang.keySet()) {//  for k, v in sort_by_value_desc (triang):
                    double v = reverseTriang.get(k);

                    System.out.println(k + "\t" + v);
                    Double adj = 0.0;
                    if (adj_dict.containsKey(k)) { //  if k in adj_dict:
                        System.out.println("Value found in dictionary:" + k);
                        adj = adj_dict.get(k);
                    } else {
                        adj = adjacencies(j, k);
                    }

                    if (adj == 0.0)
                        continue;

                    double c = v / adj;
                    //   System.out.println("*** RULE FOUND! ***");
                    //   System.out.println("c = " + c + "\t" + tp_key + " (x,y) <= " + k[0] + " " + body2[j][0] + " ^ " + k[1] + " " + body2[j][1] + "");
                    Boolean worth = write_rule_3(j, c, tp_key, k[0], k[1]);
                    if (!worth)
                        break;
                    adj_dict.put(k, adj);

                }
            }
        }
    }

    public static void main(String[] args) throws ParseException, IOException {
        String[][] body2 = {
                {"(x,z)", " (z,y)"},
                {"(x,z)", "  (y,z)"},
                {"(z,x)", "  (z,y)"},
                {"(z,x)", "  (y,z)"}};
        //  write_titles();

        //  new LocalHornRules().algorithm();
        //  new LocalHornRules().top_properties();

        String str = "http://dbpedia.org/ontology/bpnId";
        System.out.println(str.contains("ontology"));

        /* this will print false as the contains() method is
         * case sensitive. Here we have mentioned letter "l"
         * in upper case and in the actual string we have this
         * letter in the lower case.http://www.w3.org/1999/02/22-rdf-syntax-ns#type
         */
        System.out.println(str.contains("Like"));
        System.out.println(str.contains("Game"));
        System.out.println(str.contains("Game of"));

        write_titles();

    }


}
