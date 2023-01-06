package gilp.GILLearn_correction;

import amie.mining.AMIE;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import javatools.administrative.Announce;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.util.*;

import static gilp.GILLearn_correction.yagoCorrection.readTriples;

public class newSearchSpace {

    private BufferedWriter ruleWriter;

    /**
     * Note that a criterion is a predicate that assigns a True/False binary label
     * to a candidate triple.
     * <p>
     * four criteria to filter candidates
     * <p>
     * in the CHAI model, the given triples that has the binary label in the
     * train.txt and test.txt.
     * <p>
     * So, given one labeled triples, using the criteria to filtering candidates.
     * <p>
     * in the CHAI, the candidate as a triple(s,r,t) has a chance of representing
     * real -world knowledge, even if it does not exist in T(the set of triples).
     * <p>
     * if the triple is true, then the filtered candidate is also true.
     *
     * @author wy
     */


    public void filterCandidates(Triple triple) {
        String sub = triple.get_subject();
        String obj = triple.get_obj();

        //  "I: p(x,y) <= q(x,y)",p(x,y) is the source one.
        String query1 = "select distinct ?p where{ <" + sub + "> ?p <" + obj + ">.}";
        HashSet<String> distinctPredicates = new RDF3XEngine().getDistinctEntity(query1);


        try {
            if (!distinctPredicates.isEmpty()) {
                for (String key : distinctPredicates) {
                    Triple target = new Triple(sub, key, obj);
                    ruleWriter.write(target + "\n");
                    //    filterCandidates.add(target);
                }
            }

//  "II: p(x,y) <= q(y,x)",p(x,y) is the source one.
            String query2 = "select distinct ?p where{ <" + obj + "> ?p <" + sub + ">.}";
            HashSet<String> results2 = new RDF3XEngine().getDistinctEntity(query2);
            if (!results2.isEmpty()) {
                for (String key : results2) {
                    Triple target = new Triple(obj, key, sub);
                    ruleWriter.write(target + "\n");
                }
            }

            //  "III: p(x,y) <= q(x,z), r(z,y)",p(x,y) is the source one.
            String query3 = "select ?q ?r ?z where{ <" + sub + "> ?q ?z. ?z ?r <" + obj + ">.}";
            HashSet<String[]> getAll3 = new RDF3XEngine().getMultipleElements(query3, 3);
            if (!getAll3.isEmpty()) {
                for (String[] key : getAll3) {
                    Triple first = new Triple(sub, key[0], key[2]);
                    Triple second = new Triple(key[2], key[1], obj);

                    ruleWriter.write(first + "\n");
                    ruleWriter.write(second + "\n");
                    //   filterCandidates.add(first);
                    //   filterCandidates.add(second);
                }
            }


            //  "IV: p(x,y) <= q(x,z), r(y,z)",p(x,y) is the source one.
            String query4 = "select ?q ?r ?z where{ <" + sub + "> ?q ?z. <" + obj + "> ?r ?z .}";

            HashSet<String[]> getAll4 = new RDF3XEngine().getMultipleElements(query4, 3);
            if (!getAll4.isEmpty()) {
                for (String[] key : getAll4) {
                    Triple first = new Triple(sub, key[0], key[2]);
                    Triple second = new Triple(obj, key[1], key[2]);
                    ruleWriter.write(first + "\n");
                    ruleWriter.write(second + "\n");
                    // filterCandidates.add(first);
                    // filterCandidates.add(second);
                }
            }


//  "V: p(x,y) <= q(z,x), r(z,y)",p(x,y) is the source one.
            String query5 = "select ?q ?r ?z where{?z ?q <" + sub + ">. ?z ?r <" + obj + ">.}";
            HashSet<String[]> getAll5 = new RDF3XEngine().getMultipleElements(query5, 3);
            if (!getAll5.isEmpty()) {
                for (String[] key : getAll5) {
                    Triple first = new Triple(key[2], key[0], sub);
                    Triple second = new Triple(key[2], key[1], obj);
                    ruleWriter.write(first + "\n");
                    ruleWriter.write(second + "\n");
                    // filterCandidates.add(first);
                    //  filterCandidates.add(second);
                }
            }

            //   "VI: p(x,y) <= q(z,x), r(y,z)",p(x,y) is the source one.
            String query6 = "select ?q ?r ?z where{?z ?q  <" + sub + "> . <" + obj + "> ?r ?z .}";

            HashSet<String[]> getAll6 = new RDF3XEngine().getMultipleElements(query6, 3);
            if (!getAll6.isEmpty()) {
                for (String[] key : getAll6) {
                    Triple first = new Triple(key[2], key[0], sub);
                    Triple second = new Triple(obj, key[1], key[2]);
                    ruleWriter.write(first + "\n");
                    ruleWriter.write(second + "\n");
                    // filterCandidates.add(first);
                    //   filterCandidates.add(second);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashSet<Triple> candidatesType1(Triple triple) {
//  "I: p(x,y) <= q(x,y)",p(x,y) is the source one.
        HashSet<Triple> filterCandidates = new HashSet<>();
        String sub = triple.get_subject();
        String obj = triple.get_obj();
        String sparql = "select distinct ?p where{ <" + sub + "> ?p <" + obj + ">.}";

        HashSet<String> distinctPredicates = new RDF3XEngine().getDistinctEntity(sparql);


        if (!distinctPredicates.isEmpty()) {

            for (String key : distinctPredicates) {
                Triple target = new Triple(sub, key, obj);

                filterCandidates.add(target);
            }
        }


        return filterCandidates;
    }

    private HashSet<Triple> candidatesType2(Triple triple) {
//  "II: p(x,y) <= q(y,x)",p(x,y) is the source one.
        HashSet<Triple> filterCandidates = new HashSet<>();
        String sub = triple.get_subject();
        String obj = triple.get_obj();
        String sparql = "select distinct ?p where{ <" + obj + "> ?p <" + sub + ">.}";

        HashSet<String> distinctPredicates = new RDF3XEngine().getDistinctEntity(sparql);

        if (!distinctPredicates.isEmpty()) {

            for (String key : distinctPredicates) {
                Triple target = new Triple(obj, key, sub);
                filterCandidates.add(target);
            }
        }
        return filterCandidates;
    }

    private HashSet<Triple> candidatesType3(Triple triple) {
//  "III: p(x,y) <= q(x,z), r(z,y)",p(x,y) is the source one.
        HashSet<Triple> filterCandidates = new HashSet<>();
        String sub = triple.get_subject();
        String obj = triple.get_obj();
        String query = "select ?q ?r ?z where{ <" + sub + "> ?q ?z. ?z ?r <" + obj + ">.}";

        HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, 3);
        if (!getAll.isEmpty()) {
            for (String[] key : getAll) {
                Triple first = new Triple(sub, key[0], key[2]);
                Triple second = new Triple(key[2], key[1], obj);
                filterCandidates.add(first);
                filterCandidates.add(second);
            }
        }
        return filterCandidates;
    }

    private HashSet<Triple> candidatesType4(Triple triple) {
//  "IV: p(x,y) <= q(x,z), r(y,z)",p(x,y) is the source one.
        HashSet<Triple> filterCandidates = new HashSet<>();
        String sub = triple.get_subject();
        String obj = triple.get_obj();
        String query = "select ?q ?r ?z where{ <" + sub + "> ?q ?z. <" + obj + "> ?r ?z .}";

        HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, 3);
        if (!getAll.isEmpty()) {
            for (String[] key : getAll) {
                Triple first = new Triple(sub, key[0], key[2]);
                Triple second = new Triple(obj, key[1], key[2]);
                filterCandidates.add(first);
                filterCandidates.add(second);
            }
        }
        return filterCandidates;
    }

    private HashSet<Triple> candidatesType5(Triple triple) {
//  "V: p(x,y) <= q(z,x), r(z,y)",p(x,y) is the source one.
        HashSet<Triple> filterCandidates = new HashSet<>();
        String sub = triple.get_subject();
        String obj = triple.get_obj();
        String query = "select ?q ?r ?z where{?z ?q <" + sub + ">. ?z ?r <" + obj + ">.}";

        HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, 3);
        if (!getAll.isEmpty()) {
            for (String[] key : getAll) {
                Triple first = new Triple(key[2], key[0], sub);
                Triple second = new Triple(key[2], key[1], obj);
                filterCandidates.add(first);
                filterCandidates.add(second);
            }
        }
        return filterCandidates;
    }

    private HashSet<Triple> candidatesType6(Triple triple) {
//   "VI: p(x,y) <= q(z,x), r(y,z)",p(x,y) is the source one.
        HashSet<Triple> filterCandidates = new HashSet<>();
        String sub = triple.get_subject();
        String obj = triple.get_obj();
        String query = "select ?q ?r ?z where{?z ?q  <" + sub + "> . <" + obj + "> ?r ?z .}";

        HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(query, 3);
        if (!getAll.isEmpty()) {
            for (String[] key : getAll) {
                Triple first = new Triple(key[2], key[0], sub);
                Triple second = new Triple(obj, key[1], key[2]);
                filterCandidates.add(first);
                filterCandidates.add(second);
            }
        }
        return filterCandidates;
    }

    public void filterSearchSpace(HashSet<Triple> triples, Boolean decision, int iterationTimes) throws FileNotFoundException {
        String path = null;

        if (decision)
            path = "./prediction/newSearchSpace/positiveSearchSpace" + iterationTimes + ".tsv";//-old
        else
            path = "./prediction/newSearchSpace/negativeSearchSpace" + iterationTimes + ".tsv";//-old
        try {
            ruleWriter = new BufferedWriter(new FileWriter(path));
            for (Triple triple : triples) {
                filterCandidates(triple);
            }
            ruleWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public double get_fitness(double recall, double rr) {

        return (2 * recall * rr) / (recall + rr);
    }

    public static void main(String[] args) throws Exception {
        int number = 100;
        String subVariable = "subject";
        String objVariable = "object";
        int filterCondition = 0;
        int iterationTimes = 1;

        new test_rules().selectTriples(subVariable, objVariable, number, filterCondition, true);
        String posPath = "./prediction/triples/positive.tsv";
        HashSet<Triple> positiveTriple = readTriples(posPath);

        new newSearchSpace().filterSearchSpace(positiveTriple, true, iterationTimes);

        new test_rules().selectTriples(subVariable, objVariable, number, filterCondition, false);
        String negPath = "./prediction/triples/negative.tsv";
        HashSet<Triple> negativeTriple = readTriples(negPath);

        new newSearchSpace().filterSearchSpace(negativeTriple, false, iterationTimes);

        System.out.println("\n------- finish pruned search space-----------------\n");

        new test_rules().pruneSearchSpace(iterationTimes);
        new newSearchSpace().amieRules(true, iterationTimes);
        new newSearchSpace().amieRules(false, iterationTimes);

    }


    public void amieRules(Boolean decision, int iterationTimes) {

        try {
            Announce.doing("\n Starting the mining phase \n");
            AMIE miner = AMIE.getInstance(new test_rules().parameters(decision, iterationTimes));
            List<amie.rules.Rule> amieRules = miner.mine();

            Announce.done("\n finish the mining phase \n");
            Announce.close();

            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("./prediction/triples/amieRules-" + iterationTimes + "-" + decision + ".tsv"),
                    StandardCharsets.UTF_8);
            if (!amieRules.isEmpty())
                for (amie.rules.Rule amierule : amieRules)
                    writer.write((amierule.toString()) + "\t" + amierule.getPcaConfidence() + "\n");

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
