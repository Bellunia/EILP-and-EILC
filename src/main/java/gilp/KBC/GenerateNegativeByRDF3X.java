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

import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.KBC.GenerateNegativeTriples;


/**
 * how to quickly generate the negative triples
 *
 * // positive triple: <s,r,t>
 *
 *the relation's domain and range are so large.
 *
 *
 *
 * // negative triple: <s',r,t>
 *
 *  // negative triple: <s,r,t'>
 *
 * @author wy
 *
 */
public class GenerateNegativeByRDF3X {

    public HashSet<String> entitySR = new HashSet<String>();// (entity of subjects and relations)
    public HashSet<String> entityRT = new HashSet<String>();// (entity of relations and objects)
    public HashSet<Triple> positiveTriples = new HashSet<Triple>();
    public HashSet<Triple> headNegTriples = new HashSet<Triple>();
    public HashSet<Triple> tailNegTriples = new HashSet<Triple>();
    public HashSet<Triple> negativeTriples = new HashSet<Triple>();
    // public HashMap< String,HashSet<String>> relationAndHead =new HashMap<
    // String,HashSet<String>>();

    Multimap<String, String> relationAndHead = ArrayListMultimap.create();

    Multimap<String, String> relationAndTail = ArrayListMultimap.create();

    public HashMap<String, HashSet<String>> relationAndtail = new HashMap<String, HashSet<String>>();

    public void convertData(String fn_train) throws Exception {

        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(fn_train), "UTF-8"));
        String line = "";

        while ((line = read.readLine()) != null) {
            String head = line.split("\t")[0].trim();
            String relation = line.split("\t")[1].trim();
            String tail = line.split("\t")[2].trim();

            relationAndHead.put(relation, head);
            relationAndTail.put(relation, tail);

            // entitySR.add(head);
            // entitySR.add(relation);

            // entityRT.add(relation);
            // entityRT.add(tail);

            positiveTriples.add(new Triple(head, relation, tail));

        }
        read.close();
    }

    public void generateNegativeTriples(String fn_train, String fn_negative) throws Exception {
        convertData(fn_train);

        while (positiveTriples.iterator().hasNext()) {

            Triple pos = positiveTriples.iterator().next();

            HashSet<String> allhead = new HashSet<String>(relationAndHead.get(pos.get_predicate()));

            for (String str : allhead) {
                if (!str.equals(pos.get_subject()))

                    headNegTriples.add(new Triple(str, pos.get_predicate(), pos.get_obj()));
            }

            HashSet<String> alltail = new HashSet<String>(relationAndTail.get(pos.get_predicate()));

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

    public HashSet<String> filterRandomString(int numbers, HashSet<String> filterComments) {
        // randomly choose @num comments
        int s = filterComments.size();

        int[] isChosen = new int[s];
        for (int i = 0; i < s; i++) {
            isChosen[i] = 0;
        }
        HashSet<String> comments = new HashSet<String>();

        while (comments.size() < Math.min(numbers, s)) {
            int idx = (int) Math.round(Math.random() * (s - 1));
            String cmt = filterComments.toArray()[idx].toString();

            if (isChosen[idx] == 0) {
                comments.add(cmt);

                isChosen[idx] = 1;
            }
        }
        return comments;
    }

    public HashSet<Triple> filterRandomTriple(int numbers, HashSet<Triple> filterComments) {
        // randomly choose @num comments
        int s = filterComments.size();

        int[] isChosen = new int[s];
        for (int i = 0; i < s; i++) {
            isChosen[i] = 0;
        }
        HashSet<Triple> comments = new HashSet<Triple>();

        while (comments.size() < Math.min(numbers, s)) {
            int idx = (int) Math.round(Math.random() * (s - 1));
            Triple cmt = (Triple) filterComments.toArray()[idx];

            if (isChosen[idx] == 0) {
                comments.add(cmt);

                isChosen[idx] = 1;
            }
        }
        return comments;
    }

    public void generateNegativeTriples(String fn_train, String fn_negative, int number) throws Exception {

        convertData(fn_train);

        HashSet<Triple> filterPositiveTriples = filterRandomTriple(number, positiveTriples);
        // negative triples
        // positive triple: <s,r,t>
        // negative triple: <s',r,t>
        // negative triple: <s,r,t'>

        for (Triple positiveTriple : filterPositiveTriples) {

            String relation = positiveTriple.get_predicate();
            String head = positiveTriple.get_subject();
            String tail = positiveTriple.get_obj();

            // //negative triple: <s',r,t>

            HashSet<String> allHeads = new HashSet<String>();

            HashSet<String> allTails = new HashSet<String>();

            HashSet<Triple> allTriplesByRelation = new RDF3XEngine().getTriplesByPredicate(relation);

            if (allTriplesByRelation != null) {

                for (Triple tri : allTriplesByRelation) {

                    allHeads.add(tri.get_subject());

                    allTails.add(tri.get_obj());

                }

                String sql = "select distinct ?a where{ ?a <" + relation + "> <" + tail + ">.}";

                HashSet<String> allSubjects = new RDF3XEngine().getDistinctEntity(sql);
                allHeads.removeAll(allSubjects);

                if (!allHeads.isEmpty()) {

                    HashSet<String> subheads = filterRandomString(number / 2, allHeads);

                    for (String subhead : subheads)
                        headNegTriples.add(new Triple(subhead, relation, tail));

                }

                String sql1 = "select distinct ?a where{ <" + head + "> <" + relation + "> ?a.}";

                HashSet<String> allObjects = new RDF3XEngine().getDistinctEntity(sql1);

                allTails.removeAll(allObjects);

                if (!allTails.isEmpty()) {
                    HashSet<String> subheads = filterRandomString(number / 2, allTails);
                    for (String subtail : subheads)
                        tailNegTriples.add(new Triple(head, relation, subtail));
                }
                // negative triple: <s,r,t'>

                negativeTriples.addAll(headNegTriples);
                negativeTriples.addAll(tailNegTriples);

            }

        }

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

        String fn_train = "/home/wy/Documents/datasets_knowledge_embedding-master/other/YAGO3-10/train.txt";// datasets\\yago37\\yago37_triples.train";
        String fn_negative = "/home/wy/Downloads/triples.negative";// datasets\\yago37\\yago37_triples.negative";

        // new GenerateNegativeTriples().generateNegativeTriples(fn_train, fn_negative);
        int number = 2;

        new GenerateNegativeByRDF3X().generateNegativeTriples(fn_train, fn_negative, number);
    }

}