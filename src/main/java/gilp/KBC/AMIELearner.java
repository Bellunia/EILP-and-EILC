package gilp.KBC;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import amie.data.KB;
import amie.mining.AMIE;


import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import javatools.administrative.Announce;
import javatools.datatypes.ByteString;


public class AMIELearner  {
    static boolean _DEBUG = false;
    //whether or not running in debug mode

    String _temp_data_file = "chinese.txt";
    //the name of the file to store the temp data


    public ArrayList<Rule> learn_rule(ArrayList<Triple> triples) throws Exception {
        //first of all, create a temporal file storing the fb's content


        ArrayList<Rule> gilpRules = new ArrayList<Rule>();

        Writer writer = new OutputStreamWriter(new FileOutputStream(_temp_data_file),
                Charset.forName("UTF-8"));

        try {
            for (Triple t : triples) {
                writer.write(("<" + t.get_subject().replace("-", "@").replace("–", "#") + ">\t"));
                writer.write(("<" + t.get_predicate().replace("-", "@").replace("–", "#") + ">\t"));
                writer.write(("<" + t.get_obj().replace("-", "@").replace("–", "#") + ">\n"));
            }
            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        String[] paras= {"-mins", "1", "-minis", "1", "-const", _temp_data_file};

        amie.mining.AMIE miner = amie.mining.AMIE.getInstance(paras);

        Announce.doing("\n Starting the mining phase \n");

        List<amie.rules.Rule> rules = miner.mine();

        Announce.done("\n finish the mining phase \n");

        // transform rules_amie to rules_gilp
        if (!rules.isEmpty()) {
            for (amie.rules.Rule rule : rules) {

                double stdConfidence=rule.getStdConfidence();
                //double pcaConfidence=rule.getPcaConfidence();
                //double headCoverage=rule.getHeadCoverage();

                Clause clause = new Clause();

                for (int[] bs : rule.getBody()) {
                    clause.addPredicate(extractComplexRDFPredicate(bs));
                }

                Rule rdfRule = new Rule();
                rdfRule.set_body(clause);

                RDFPredicate head = extractRDFPredicate(rule.getHead());

                rdfRule.set_head(head);
                rdfRule.setConfidence(stdConfidence);

                gilpRules.add(rdfRule);





            }
        }
        return gilpRules;
    }

    public RDFPredicate extractRDFPredicate(int[] byteStrings) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName(simpleOtherReplace( KB.unmap(byteStrings[1]).replace("<", "").replace(">", "")));
        rdfPredicate.setSubject(simpleOtherReplace(KB.unmap(byteStrings[0])));
        rdfPredicate.setObject(simpleOtherReplace(KB.unmap(byteStrings[2])));
        return rdfPredicate;
    }
    public static String simpleOtherReplace(String str){
        str= str.replaceAll("<", "").replaceAll(">", "");
        return str;
    }

    private RDFPredicate extractRDFPredicate(ByteString[] byteStrings) {
        RDFPredicate rdfPredicate = new RDFPredicate();
        String subject = byteStrings[0].toString();
        String predicate = byteStrings[1].toString();
        String object = byteStrings[2].toString();

        subject = subject.replaceAll("<", "").replaceAll(">", "");
        object = object.replaceAll("<", "").replaceAll(">", "");
        predicate = predicate.replaceAll("<", "").replaceAll(">", "");

        rdfPredicate.setPredicateName(predicate);
        rdfPredicate.setSubject(new String(subject));
        rdfPredicate.setObject(new String(object));
        return rdfPredicate;
    }

    public static RDFPredicate extractComplexRDFPredicate(int[] bs) {
//KB.unmap(atom[1]).replace("<", "").replace(">", "") + "(" + atom[0] + ", " + atom[2] + ")
        //  new amie.rules.Rule().toDatalog(RULE.getHead())
        RDFPredicate rdfPredicate = new RDFPredicate();
        rdfPredicate.setPredicateName(simpleReplace(KB.unmap(bs[1]).replace("<", "").replace(">", "")));
        rdfPredicate.setSubject(simpleReplace(KB.unmap(bs[0])));
        rdfPredicate.setObject(simpleReplace(KB.unmap(bs[2])));
        return rdfPredicate;
    }
    public static String simpleReplace(String str){
        str= str.replaceAll("<", "").replaceAll(">", "")
                .replaceAll("@@", "-").replaceAll("##", "–");
        return str;
    }

    //unit test
    public static void main(String[] args) throws Exception{

        _DEBUG = true;

        //1. generate a set of triples and comments
        ArrayList<Triple> listComments = new ArrayList<Triple>();

        Triple t;

        RandomAccessFile file_data = null;

        try {
            file_data = new RandomAccessFile("/home/wy/Documents/datasets_knowledge_embedding-master/FB15k-237/test.txt","r");
            String line = "";
            while((line=file_data.readLine())!=null){
                StringTokenizer st = new StringTokenizer(line,"\t");
                String s, p, o;
                System.out.println(line+"\n");
                s = st.nextToken();
                p = st.nextToken();
                o = st.nextToken();

                t= new Triple(s, p, o);

                listComments.add(t);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        AMIELearner learner = new AMIELearner();
        ArrayList<Rule> rules = learner.learn_rule(listComments);
        for (int i=0;i<rules.size();i++){
            System.out.println(rules.get(i));
        }
    }

}