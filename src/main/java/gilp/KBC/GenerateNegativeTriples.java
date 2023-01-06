package gilp.KBC;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.ArrayListMultimap;

import com.google.common.collect.Multimap;
import gilp.rdf3x.Triple;


/**
 * not finish , the qunatity is so large.
 * so follow the RUGE, using the number to represent the triples.
 * @author wy
 *
 */
public class GenerateNegativeTriples {

    public HashSet<String> entitySR = new HashSet<String>();// (entity of subjects and relations)
    public HashSet<String> entityRT = new HashSet<String>();// (entity of relations and objects)
    public HashSet<Triple> positiveTriples = new HashSet<Triple>();
    public HashSet<Triple> headNegTriples = new HashSet<Triple>();
    public HashSet<Triple> tailNegTriples = new HashSet<Triple>();
    public HashSet<Triple> negativeTriples = new HashSet<Triple>();
    //public HashMap< String,HashSet<String>> relationAndHead =new HashMap< String,HashSet<String>>();

    Multimap<String, String> relationAndHead = ArrayListMultimap.create();

    Multimap<String, String> relationAndTail = ArrayListMultimap.create();


    public HashMap< String,HashSet<String>> relationAndtail =new HashMap< String,HashSet<String>>();

    public void convertData(String fn_train) throws Exception {

        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(fn_train), "UTF-8"));
        String line = "";



        while ((line = read.readLine()) != null) {
            String head = line.split("\t")[0].trim();
            String relation = line.split("\t")[1].trim();
            String tail = line.split("\t")[2].trim();

            relationAndHead.put(relation, head);
            relationAndTail.put(relation, tail);

            //entitySR.add(head);
            //entitySR.add(relation);

            //entityRT.add(relation);
            //entityRT.add(tail);

            positiveTriples.add(new Triple(head, relation, tail));

        }
        read.close();
    }

    public void generateNegativeTriples(String fn_train, String fn_negative) throws Exception {
        convertData(fn_train);

        while (positiveTriples.iterator().hasNext()) {

            Triple pos = positiveTriples.iterator().next();

            HashSet<String> allhead = new HashSet<String>( relationAndHead.get( pos.get_predicate()));

            for (String str : allhead) {
                if (!str.equals(pos.get_subject()))

                    headNegTriples.add(new Triple(str, pos.get_predicate(), pos.get_obj()));
            }

            HashSet<String> alltail = new HashSet<String>( relationAndTail.get( pos.get_predicate()));

            for (String str : alltail) {
                if (!str.equals(pos.get_obj()))

                    tailNegTriples.add(new Triple(pos.get_subject(), pos.get_predicate(), str));
            }

        }

        headNegTriples.removeAll(positiveTriples);
        tailNegTriples.removeAll(positiveTriples);

        negativeTriples.addAll(headNegTriples);
        negativeTriples.addAll(positiveTriples);

        int ict = 0;
        BufferedWriter rel = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fn_negative), "UTF-8"));

        while (negativeTriples.iterator().hasNext()) {
            Triple tri = negativeTriples.iterator().next();
            rel.write(ict + "\t" + tri + "\n");

            ict++;
        }
        rel.close();

    }

    public static void main(String[] args) throws Exception {

        String fn_train = "/home/wy/Documents/datasets_knowledge_embedding-master/FB15k-237/train.txt";//datasets\\yago37\\yago37_triples.train";
        String fn_negative ="/home/wy/Downloads/triples.negative";//datasets\\yago37\\yago37_triples.negative";

        new GenerateNegativeTriples().generateNegativeTriples(fn_train, fn_negative);
    }

}