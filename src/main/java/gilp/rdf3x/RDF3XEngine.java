package gilp.rdf3x;

import de.mpii.rdf3x.Driver;
import gilp.GILLearn_correction.ILPLearnSettings;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.sparql.GetSparql;

import java.sql.SQLException;
import java.util.*;

public class RDF3XEngine {
    static java.sql.Connection _con = null;
    String RDF3X_DBFILE = null;

    private static final boolean KB_WITH_PREFIX = false;

    private static final String YAGO_PREFIX_RDF = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    private static final String YAGO_PREFIX_RDFS = "<http://www.w3.org/2000/01/rdf-schema#>";
    private static final String YAGO_PREFIX_X = "<http://www.w3.org/2001/XMLSchema#>";
    private static final String YAGO_PREFIX_Y = "<http://mpii.de/yago/resource/>";
    private static final String YAGO_PREFIX_BASE = "<http://mpii.de/yago/resource/>";

    //to generate a query header when KB has prefix
    private String getQueryHeader(){
        String header = "PREFIX rdf:" + RDF3XEngine.YAGO_PREFIX_RDF;
        header += " PREFIX  rdfs:" +  RDF3XEngine.YAGO_PREFIX_RDFS;
        header += " PREFIX x:" + RDF3XEngine.YAGO_PREFIX_X;
        header += " PREFIX y:" + RDF3XEngine.YAGO_PREFIX_Y;
        return header;
    }

    void buildConn() {
        try {
            if(ILPLearnSettings.Database==1)
            RDF3X_DBFILE = "dbpedia";
            else
                RDF3X_DBFILE = "yago_type";

            Properties prop = new Properties();
            prop.put("rdf3xembedded", ILPLearnSettings.RDF3X_PATH + "rdf3xembedded");
            Driver myDriver = new Driver();
            RDF3XEngine._con = myDriver.connect("rdf3x://" + ILPLearnSettings.RDF3X_PATH + RDF3X_DBFILE, prop);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }

    void reset() {
        try {
            RDF3XEngine._con.close();
            this.buildConn();
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }

    public HashSet<Triple> getTriplesByPredicate(String predicate) {
        RDFPredicate tp = new RDFPredicate();
        tp.setSubject(new String("?x"));
        tp.setObject(new String("?y"));
        tp.setPredicateName(predicate);
        return getTriples(tp);
    }

    public HashSet<Triple> getTriplesBySubject(String subject) {
        RDFPredicate tp = new RDFPredicate();
        tp.setSubject(new String(subject));
        tp.setObject(new String("?y"));
        tp.setPredicateName("?p");
        return getTriples(tp);
    }

    public HashSet<Triple> getTriplesByObject(String object) {
        RDFPredicate tp = new RDFPredicate();
        tp.setSubject(new String("?x"));
        tp.setObject(new String(object));
        tp.setPredicateName("?p");
        return getTriples(tp);
    }

    public HashSet<Triple> getTriples(RDFPredicate tp) {

        Clause cls = new Clause();
        cls.addPredicate(tp);
        String query = new GetSparql().buildSPARQL(cls);

        // execute the SPARQL
        if (RDF3XEngine._con == null) {
            this.buildConn();
        }

//		if (query.contains("\"")) {
//			query = query.replaceAll("\"", "'");
//
//		}
// 		System.out.println(query+"\n");
        HashSet<Triple> triples = new HashSet<Triple>();
        try {
            java.sql.Statement stat = _con.createStatement();

            java.sql.ResultSet rlt = stat.executeQuery(query);

            RDFSubGraphSet sg_set = mountSGSet(rlt, cls);
            for (RDFSubGraph sg : sg_set.getSubGraphs()) {

                Triple t = sg.getTriples().get(0);

                triples.add(t);
            }

            return triples;
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return null;
        }
    }

    public HashSet<Triple> getTriplesInSubject(String newEntity) {
        String sparql = "select distinct ?a ?o where{ " + newEntity + " ?a ?o.}";
     //  System.out.println(sparql);
        HashSet<Triple> getTriples = new HashSet<Triple>();

        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = _con.createStatement();
            java.sql.ResultSet rlt = stat.executeQuery(sparql);
            while (rlt.next()) {
                String count_rlt = removePointBrackets(rlt.getString(1));
                String count_rlt2 = removePointBrackets(rlt.getString(2));
                Triple tri = new Triple(removePointBrackets(newEntity), count_rlt, count_rlt2);
                getTriples.add(tri);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
        return getTriples;
    }

    public HashSet<Triple> getTriplesInObject(String newEntity) {

        String sparql = "select distinct ?a ?o where{  ?a ?o " + newEntity + ".}";

        HashSet<Triple> getTriples = new HashSet<Triple>();

        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = _con.createStatement();

            java.sql.ResultSet rlt = stat.executeQuery(sparql);

            while (rlt.next()) {

                String count_rlt = removePointBrackets(rlt.getString(1));
                String count_rlt2 = removePointBrackets(rlt.getString(2));

                Triple tri = new Triple(count_rlt, count_rlt2, removePointBrackets(newEntity));
                // triples' object put the newEntity or the original obj???

                getTriples.add(tri);
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
        return getTriples;
    }

    public HashSet<Triple> getTriplesInObject(String newEntity, String predicate) {

        String sparql = "select distinct ?a where{  ?a <" + predicate + "> " + newEntity + ".}";

        HashSet<Triple> getTriples = new HashSet<Triple>();

        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = _con.createStatement();

            java.sql.ResultSet rlt = stat.executeQuery(sparql);

            while (rlt.next()) {

                String count_rlt = removePointBrackets(rlt.getString(1));
                //String count_rlt2 = removePointBrackets(rlt.getString(2));

                Triple tri = new Triple(count_rlt, predicate, removePointBrackets(newEntity));

                getTriples.add(tri);
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
        return getTriples;
    }

    public HashSet<String[]> getMultipleElements(String sparql, int number) {

System.out.println("sparql :"+sparql);
        HashSet<String[]> getAll = new HashSet<>();

        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = _con.createStatement();

            java.sql.ResultSet rlt = stat.executeQuery(sparql);

            while (rlt.next()) {
                String[] elements = new String[number];
                //ArrayList<String> elements =new ArrayList<>();
                for (int i = 0; i < number; i++) {
                String test=rlt.getString(1 + i);
                    if (test!=null) {

                        String element = removePointBrackets(test);
                        elements[i] = element;
                    } else {
                        elements[i] = null;
                    }
                }
                getAll.add(elements);

            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
        return getAll;
    }

    public HashMap<String, Double> getCountSingleEntity(String sparql) {
        // select count ?d where{ ?a <hasGivenName> ?c.?a <wasBornIn> ?d.}
        // for example: sub_query_num ={[ <Uttar_Pradesh>]=1,.....}={d,num} 2 results.

        HashMap<String, Double> CountObject = new HashMap<String, Double>();

        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = _con.createStatement();

            java.sql.ResultSet rlt = stat.executeQuery(sparql);

            while (rlt.next()) {


                String count_rlt = removePointBrackets(rlt.getString(1));

                String strVal = removePointBrackets(rlt.getString(2));
                double num = Integer.parseInt(strVal);
                CountObject.put(count_rlt, num);
            }

        } catch (Exception e) {
            //System.out.println(sparql);
            e.printStackTrace(System.out);
            return null;
        }
        return CountObject;
    }

    public HashMap<ArrayList<String>, Double> getCountDoubleEntity(String sparql) {
        // select count ?d  ?a where{ ?a <hasGivenName> ?c.?a <wasBornIn> ?d.}
        // for example: sub_query_num ={[ <Uttar_Pradesh>]=1,.....}={d,num} 2 results.

        HashMap<ArrayList<String>, Double> CountObject = new HashMap<ArrayList<String>, Double>();

        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = _con.createStatement();

            java.sql.ResultSet rlt = stat.executeQuery(sparql);

            while (rlt.next()) {

                ArrayList<String> str = new ArrayList<String>();
                String count_rlt = removePointBrackets(rlt.getString(1));
                String count_rlt2 = removePointBrackets(rlt.getString(2));

                String strVal = removePointBrackets(rlt.getString(3));
                double num = Integer.parseInt(strVal);

                str.add(count_rlt);
                str.add(count_rlt2);
                CountObject.put(str, num);
            }

        } catch (Exception e) {
            //System.out.println(sparql);
            e.printStackTrace(System.out);
            return null;
        }
        return CountObject;
    }

    public HashSet<String> getDistinctEntity(String sparql) {
        // select distinct ?a where {?a <hasGivenName> ?b. ?a <isCitizenOf> <Argentina>.}
//		if (sparql.contains("\"")) {
//			sparql = sparql.replaceAll("\"", "'");
//		}
        HashSet<String> sqlSubject = new HashSet<String>();
        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = _con.createStatement();
            java.sql.ResultSet rlt = stat.executeQuery(sparql);
            // get the result
            while (rlt.next()) {
                sqlSubject.add(removePointBrackets(rlt.getString(1)));
            }
        } catch (Exception e) {

            return null;
        }
        return sqlSubject;
    }

    // get all sub-graphs
    private RDFSubGraphSet doQuery(Clause cls, String query) {
        return doQuery(cls, query, -1);
    }

    // get all sub-graphs
    public RDFSubGraphSet doYagoQuery(Clause cls, String query) {
        return doQuery(cls, query, -1);
    }

    // get at most @num sub-graphs
    private RDFSubGraphSet doQuery(Clause cls, String query, int num) {
        // execute the SPARQL
        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        if (query.contains("\"")) {
            query = query.replaceAll("\"", "'");
        }
        try {
            java.sql.Statement stat = _con.createStatement();
            de.mpii.rdf3x.Statement rdf3x_stat = (de.mpii.rdf3x.Statement) stat;
            java.sql.ResultSet rlt = rdf3x_stat.executeQuery(query);
            // java.sql.ResultSet rlt = rdf3x_stat.executeQuery(query, num);
            // java.sql.ResultSet rlt = stat.executeQuery(query);
            return mountSGSet(rlt, cls, num);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return null;
        }
    }

    private RDFSubGraphSet mountSGSet(java.sql.ResultSet rlt, Clause cls) {
        return mountSGSet(rlt, cls, -1);
    }

    private RDFSubGraphSet mountSGSet(java.sql.ResultSet rlt, Clause cls, int num) {
        // In results of RDF3x, the columns are sorted in lexical order of the variable
        // names
        // if the query is like select * .
        // First, we need to compute the column index of each variable.
        PriorityQueue<String> pqVars = new PriorityQueue<String>();
        ArrayList<RDFPredicate> myIter = cls.getIterator();
        HashSet<RDFPredicate> preds = new HashSet<RDFPredicate>();

        for (RDFPredicate tp : myIter) {
            preds.add(tp);
            if (tp.isSubjectVariable()) {
                if (!pqVars.contains(tp.getSubject().toString())) {
                    pqVars.add(tp.getSubject().toString());
                }
            }
            if (tp.isObjectVariable()) {
                if (!pqVars.contains(tp.getObject().toString())) {
                    pqVars.add(tp.getObject().toString());
                }
            }
            if (tp.isPredicateVariable()) {
                if (!pqVars.contains(tp.getPredicateName())) {
                    pqVars.add(tp.getPredicateName());
                }
            }
        }
        // initialize the graph set
        RDFSubGraphSet sg_set = new RDFSubGraphSet();
        sg_set.setPredicates(preds);

        String[] var_names = pqVars.toArray(new String[0]);
        Arrays.sort(var_names);
        // the order generated by PriorityQueue is not good, thus we have to sort the
        // var names again.
        HashMap<String, Integer> hmapVars = new HashMap<String, Integer>();
        for (int i = 0; i < var_names.length; i++) {
            hmapVars.put(var_names[i], i + 1);
            // store i+1 instead of i, because the column index of java.sql.resultset starts
            // from 1 while array starts from 0.
        }
        try {// for each result tuple, we mount a sub-graph
            int count = 0;
            while (rlt.next()) {
                RDFSubGraph sg = new RDFSubGraph();
                boolean meetError = false;
                for (int i = 0; i < preds.size(); i++) {
                    RDFPredicate tp = (RDFPredicate) preds.toArray()[i];// .get(i);
                    Triple t = new Triple();
                    if (tp.isPredicateVariable()) {
                        int idx = hmapVars.get(tp.getPredicateName());
                        String strVal = rlt.getString(idx);

                        t.set_predicate(removePointBrackets(strVal));
                    } else
                        t.set_predicate(removePointBrackets(tp.getPredicateName()));

                    if (tp.isSubjectVariable()) {
                        int idx = hmapVars.get(tp.getSubject().toString());
                        String strVal = rlt.getString(idx);

                        if (strVal != null)
                            t.set_subject(removePointBrackets(strVal));
                        else {
                            meetError = true;
                            break;
                        }
                    } else
                        t.set_subject(tp.getSubject().toString());

                    if (tp.isObjectVariable()) {
                        int idx = hmapVars.get(tp.getObject().toString());
                        String strVal = rlt.getString(idx);

                        if (strVal != null)
                            t.set_obj(removePointBrackets(strVal));
                        else {
                            meetError = true;
                            break;
                        }
                    } else
                        t.set_obj(tp.getObject().toString());

                    sg.addTriple(t);

                    if (num > 0 && ++count >= num) {
                        break;
                    }
                }
                if (!meetError)
                    sg_set.addSubGraph(sg);
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return null;
        }
        return sg_set;
    }

    public static String removePointBrackets(String str) {
        if (!str.isEmpty()) {
            if (str.contains("<<") || str.contains(">>")) {
                str = str = str.substring(1, str.length() - 1);
            } else {
                str = str.replace("<", "");
                str = str.replace(">", "");
            }
        }
        return str;

    }

    public RDFSubGraphSet getTriplesByCNF(Clause cls) {
        String query = new GetSparql().buildSPARQL(cls);
        RDFSubGraphSet rlt = doQuery(cls, query);
        this.reset();
        return rlt;
    }

    //--------------@2021.11.12

    public RDFSubGraphSet getTriplesByCNF(Clause cls, ArrayList<RDFFilter> listFilters, int num){
        String query = buildSPARQL(cls, listFilters);
        RDFSubGraphSet rlt = doQuery(cls, query, num);
        this.reset();
        return rlt;
    }

    public RDFSubGraphSet getTriplesByCNF(Clause cls, ArrayList<RDFFilter> listFilters){
        String query = buildSPARQL(cls, listFilters);
        //System.out.println(query);
        return doQuery(cls, query);
    }

    //execute a sparql query which must contain "count" in the select clause
    //only return the count value of the first result tuple
    public int getCount(String sparql){
        if (RDF3XEngine._con == null) {
            this.buildConn();
        }
        try {
            java.sql.Statement stat = ((de.mpii.rdf3x.Connection) _con).createStatement();

            java.sql.ResultSet rlt = stat.executeQuery(sparql);

            if(rlt.next()){
                String strVal = rlt.getString(2);
                int num = Integer.parseInt(strVal);
                rlt.close();
                return num;
            }
            else{
                rlt.close();
                return 0;
            }

        } catch (Exception e) {
            System.out.println(sparql);
            e.printStackTrace(System.out);
            return -1;
        }
    }

    //transform a Clause into a SPARQL
    private String buildSPARQL(Clause cls, ArrayList<RDFFilter> listFilters ) {
        // sparql example: select * where {?s1 <hasGivenName> <Yao>. ?s1 ?y ?o1
        // .}

        StringBuffer sb = new StringBuffer();
        if (RDF3XEngine.KB_WITH_PREFIX){
            sb.append(this.getQueryHeader()+ " ");
        }
        sb.append(" select * where {");

        ArrayList<RDFPredicate> myIter = cls.getIterator();
        for (RDFPredicate tp : myIter) {
            if (RDF3XEngine.KB_WITH_PREFIX) {
                if (tp.isSubjectVariable())
                    sb.append(tp.getSubject() + " ");
                else
                    sb.append(RDF3XEngine.YAGO_PREFIX_BASE.replace(">", "")).append(tp.getSubject()).append("> ");

                if (tp.getPredicateName().startsWith("?"))
                    sb.append(tp.getPredicateName() + " ");
                else
                    sb.append("y:").append(tp.getPredicateName() + " ");
                //TODO need to handle different prefixes of the predicates, like y: and rdf:

                if (tp.isObjectVariable())
                    sb.append(tp.getObject() + ". ");
                else
                    sb.append("\"").append(tp.getObject()).append("\". ");
            } else {
                //in our current RDF3x data set, each constant is enclosed by <>
                if (!tp.isSubjectVariable())
                    sb.append("<");
                sb.append(tp.getSubject());
                if (!tp.isSubjectVariable())
                    sb.append("> ");
                else
                    sb.append(" ");

                if (!tp.getPredicateName().startsWith("?"))
                    sb.append("<");
                sb.append(tp.getPredicateName());
                if (!tp.getPredicateName().startsWith("?"))
                    sb.append("> ");
                else
                    sb.append(" ");

                if (!tp.isObjectVariable())
                    sb.append("<");
                sb.append(tp.getObject());
                if (!tp.isObjectVariable())
                    sb.append(">. ");
                else
                    sb.append(". ");
            }
        }
        if (listFilters!=null){
            //sb.append(" filter(?x = <Yao_Ming>)");
            for (RDFFilter filter: listFilters){
                if (RDF3XEngine.KB_WITH_PREFIX){
                    sb.append(" filter(" + filter.get_variable() + filter.get_opt() + RDF3XEngine.YAGO_PREFIX_BASE.replace(">", "") + filter.get_value() + ">) ");
                }else{
                    sb.append(" filter(" + filter.get_variable() + filter.get_opt() + "<" + filter.get_value() + ">) ");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
}