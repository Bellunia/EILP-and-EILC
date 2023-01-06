package horn_markov_logic_rules;

import gilp.GILLearn_correction.Property;
import gilp.comments.AnnotatedTriple;
import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import org.apache.commons.cli.ParseException;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HornRules {
    /**
     * Horn Concerto - Mining Horn clauses in RDF datasets using SPARQL queries.
     * paper: Beyond Markov Logic: Efficient Mining of Prediction Rules in Large Graphs
     * <p>
     * <p>
     * Use test endpoint (DBpedia)
     */

    public Double MIN_CONFIDENCE = 0.001;
    public Integer N_PROPERTIES = 100;//100
    public Integer N_TRIANGLES = 10;

    public HashMap<String, Double> simple_rules(String q) {
        String SIMPLE_RULES = "SELECT ?p (COUNT(*) AS ?c) WHERE { ?x ?p ?y . ?x <" + q + "> ?y . " +
                "FILTER(?p != <" + q + "> && regex(?p, \"http://dbpedia.org/ontology/\")) ) }" +
                " GROUP BY ?p ORDER BY DESC(?c)";
        System.out.println("query_SIMPLE_RULES:  " + SIMPLE_RULES);
        HashMap<String, Double> others = new Sparql().countResults(SIMPLE_RULES);
        System.out.println("others  " + others.size());

        return others;

    }

    public HashMap<String, Double> type_two_rules(String q) {
        String TYPE_2_RULES = "SELECT ?p (COUNT(*) AS ?c) WHERE { ?y ?p ?x . ?x <" + q + "> ?y. " +
                "FILTER(regex(?p, \\\"http://dbpedia.org/ontology/\\\")) } GROUP BY ?p ORDER BY DESC(?c)";
        System.out.println("query_SIMPLE_RULES:  " + TYPE_2_RULES);
        HashMap<String, Double> others = new Sparql().countResults(TYPE_2_RULES);
        System.out.println("others  " + others.size());

        return others;
    }

    public HashMap<String, Double> top_properties() {
        String TOP_PROPERTIES = "SELECT ?q (COUNT(*) AS ?c) WHERE { [] ?q [] " +
                "filter(regex(?q, \"http://dbpedia.org/ontology/\"))} GROUP BY ?q ORDER BY DESC(?c) LIMIT " + N_PROPERTIES;
        System.out.println("query_SIMPLE_RULES:  " + TOP_PROPERTIES);
        HashMap<String, Double> others = new Sparql().countResults(TOP_PROPERTIES);
        System.out.println("others  " + others.size());


        //---backup all original predicates
        String file_name = "./knowledgeClean-data/horn_rule/top_properties-predicates.tsv";
        try {
            File f = new File(file_name);
            if (!f.exists()) {    // if file does not exist, then create it
                File dir = new File(f.getParent());
                dir.mkdirs();
                f.createNewFile();
            }
            FileWriter writer = new FileWriter(file_name, Charset.forName("UTF-8"), true);

            for (String r_key : others.keySet()) {
                double r_val = others.get(r_key);

                System.out.println("*** new predicate mining FOUND! ***");
                writer.write(r_key + "\t" +r_val + "\n");
           //     writer.write("----\t"+System.currentTimeMillis()+"\n");

            }



            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


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

        String TRIANGLES = "SELECT ?q ?r (COUNT(*) AS ?c) WHERE { " + tri[t] + "?x <" + p + "> ?y ." +
                "filter(regex(?q, \"http://dbpedia.org/ontology/\") || regex(?r, \"http://dbpedia.org/ontology/\"))" +
                "} " +
                "GROUP BY ?q ?r ORDER BY DESC(?c) LIMIT " + N_TRIANGLES;
        String query_test = "SELECT ?q ?r (COUNT(*) AS ?c) WHERE { ?x ?q ?z . ?z ?r ?y . ?x dbo:nationality ?y .}  GROUP BY ?q ?r ORDER BY DESC(?c) LIMIT 10";

        System.out.println("TRANGLES-QUERY:"+TRIANGLES+"\n");
        // rules[(str(result["q"]["value"]), str(result["r"]["value"]))] = int(result["c"]["value"])

        ArrayList<HashMap<String, String>> items = new Sparql().getResultsFromQuery(TRIANGLES);
        System.out.println("items-QUERY:"+items+"\n");
        HashMap<String[], Double> triangles = new HashMap<>();

        if (items != null) {
            for (HashMap<String, String> key : items) {
                String q = key.get("q");
                String r = key.get("r");
                String c= key.get("c");
                System.out.println("c:"+c);
                double number = getNumberFromString( c);
                String[] pairs = new String[2];
                pairs[0] = q;
                pairs[1] = r;
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
        String ADJACENCIES = "SELECT (COUNT(*) AS ?c) WHERE { ?" + getChar(nodes[t], 0) + " <" + k[0] + "> ?" + getChar(nodes[t], 1)
                + ". ?" + getChar(nodes[t], 2) + " <" + k[1] + "> ?" + getChar(nodes[t], 3) + ". }";
        System.out.println("Querying:" + ADJACENCIES + "\n");

        HashMap<String, Double> others = new Sparql().countResults(ADJACENCIES);
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
            if (c > MIN_CONFIDENCE) {
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
            if (c > MIN_CONFIDENCE) {
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

            Writer writer = null;
            String path = "./knowledgeClean-data/horn_rule/rules-" + files[t] + ".tsv";
            try {
                writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));
                if (t < 2) {
                    writer.write("weight\tp\t(?,?)\tq\t(?,?)\n");//unicode in python2
                } else {

                    writer.write("weight\tp\t(?,?)\tq\t(?,?)\tr\t(?,?)\n");
                }

                writer.close();

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
                "Min_Confidence: \n" + MIN_CONFIDENCE + "\n" +
                "N_Properties: \n" + N_PROPERTIES + "\n" +
                "N_Triangles: \n" + N_TRIANGLES + "\n" +
                "Output_Folder:" + "./knowledgeClean-data/horn_rule/\n");
        String Output_Folder = "./knowledgeClean-data/horn_rule";

     //   write_titles();--只有第一次运行可用，提供文件title，其余时候不用

        HashMap<String, Double> tp = top_properties();

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
        HashMap<String, Double> tp = top_properties();
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

       new HornRules().algorithm();
    }


}
