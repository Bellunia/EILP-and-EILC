package gilp.KBC;
import gilp.rdf3x.Triple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TripleSet {

    ArrayList<Triple> triples;

    HashMap<String, ArrayList<Triple>> headToList;
    HashMap<String, ArrayList<Triple>> tailToList;
    HashMap<String, ArrayList<Triple>> relationToList;

    HashMap<String, HashMap<String, HashSet<String>>> headRelation2Tail;
    HashMap<String, HashMap<String, HashSet<String>>> headTail2Relation;
    HashMap<String, HashMap<String, HashSet<String>>> tailRelation2Head;


    HashSet<String> frequentRelations = new HashSet<String>();

    public TripleSet(String filepath) {
        this();
        this.readTriples(filepath);
        this.indexTriples();
    }

    public TripleSet() {
        this.triples = new ArrayList<Triple>();
        this.headToList = new HashMap<String, ArrayList<Triple>>();
        this.tailToList = new HashMap<String, ArrayList<Triple>>();
        this.relationToList = new HashMap<String, ArrayList<Triple>>();

        this.headRelation2Tail = new HashMap<String, HashMap<String, HashSet<String>>>();
        this.headTail2Relation = new HashMap<String, HashMap<String, HashSet<String>>>();
        this.tailRelation2Head = new HashMap<String, HashMap<String, HashSet<String>>>();
    }

    public void addTripleSet(TripleSet ts) {
        for (Triple t : ts.getTriples()) {
            this.addTriple(t);
        }
    }

    public void addTriple(Triple t) {
        this.triples.add(t);
        this.addTripleToIndex(t);
    }

    private void indexTriples() {
        long tCounter = 0;
        for (Triple t : triples) {
            tCounter++;
            if (tCounter % 100000 == 0) {
                System.out.println("* indexed " + tCounter + " triples");
            }
            addTripleToIndex(t);
        }
        System.out.println("* set up index for " + this.relationToList.keySet().size() + " relations, " + this.headToList.keySet().size() + " head entities, and " + this.tailToList.keySet().size() + " tail entities" );
    }

    private void addTripleToIndex(Triple t) {
        String head = t.get_subject();
        String tail = t.get_obj();
        String relation = t.get_predicate();
        // index head
        if (!this.headToList.containsKey(head)) {
            this.headToList.put(head, new ArrayList<Triple>());
        }
        this.headToList.get(head).add(t);
        // index tail
        if (!this.tailToList.containsKey(tail)) {
            this.tailToList.put(tail, new ArrayList<Triple>());
        }
        this.tailToList.get(tail).add(t);
        // index relation
        if (!this.relationToList.containsKey(relation)) {
            this.relationToList.put(relation, new ArrayList<Triple>());
        }
        this.relationToList.get(relation).add(t);
        // index head-relation => tail
        if(!this.headRelation2Tail.containsKey(head)) {
            this.headRelation2Tail.put(head, new HashMap<String, HashSet<String>>());
        }
        if (!this.headRelation2Tail.get(head).containsKey(relation)) {
            this.headRelation2Tail.get(head).put(relation, new HashSet<String>());
        }
        this.headRelation2Tail.get(head).get(relation).add(tail);
        // index tail-relation => head
        if(!this.tailRelation2Head.containsKey(tail)) {
            this.tailRelation2Head.put(tail, new HashMap<String, HashSet<String>>());
        }
        if (!this.tailRelation2Head.get(tail).containsKey(relation)) {
            this.tailRelation2Head.get(tail).put(relation, new HashSet<String>());
        }
        this.tailRelation2Head.get(tail).get(relation).add(head);
        // index headTail => relation
        if(!this.headTail2Relation.containsKey(head)) {
            this.headTail2Relation.put(head, new HashMap<String, HashSet<String>>());
        }
        if (!this.headTail2Relation.get(head).containsKey(tail)) {
            this.headTail2Relation.get(head).put(tail, new HashSet<String>());
        }
        this.headTail2Relation.get(head).get(tail).add(relation);
    }


    private void readTriples(String filepath) {
        Path file = (new File(filepath)).toPath();
        // Charset charset = Charset.forName("US-ASCII");
        Charset charset = Charset.forName("UTF8");
        String line = null;
        long lineCounter = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            while ((line = reader.readLine()) != null) {
                lineCounter++;
                if (lineCounter % 1000000 == 0) {
                    System.out.println(">>> parsed " + lineCounter + " lines");
                }

                if (line.length() <= 2) continue;

                String[] token = line.split("\t");

                if (token.length < 3) token = line.split(" ");

                Triple t = null;
                if (token.length == 3) t = new Triple(token[0], token[1], token[2]);

                if (token.length == 4) {
                    if (token[3].equals(".")) {
                        t = new Triple(token[0], token[1], token[2]);
                    }
                    else {

                        try {
                            t = new AnnotatedTriple(token[0], token[1], token[2]);
                            ((AnnotatedTriple)t).setConfidence(Double.parseDouble(token[3]));
                        }
                        catch (NumberFormatException nfe) {
                            System.err.println("could not parse line " + line);
                            t = null;
                        }

                    }
                }

                if (t == null) {
                    // System.err.println("problem in parsing line " + lineCounter + ": " + line);
                    // System.exit(1);
                }
                else {
                    this.triples.add(t);
                }
                // System.out.println("line= " + line);
                // System.out.println("triple= " + t);


            }

        }
        catch (IOException x) {
            System.err.format("IOException: %s%n", x);
            System.err.format("Error occured for line: " + line + " LINE END");
        }
        // Collections.shuffle(this.triples);
        System.out.println("* read " + this.triples.size() + " triples");
    }

    public ArrayList<Triple> getTriples() {
        return this.triples;
    }


    public ArrayList<Triple> getTriplesByHead(String head) {
        if (this.headToList.containsKey(head)) {
            return this.headToList.get(head);
        }
        else {
            return new ArrayList<Triple>();
        }
    }

    public ArrayList<Triple> getNTriplesByHead(String head, int n) {

        if (this.headToList.containsKey(head)) {
            if (this.headToList.get(head).size() <= n) return this.headToList.get(head);
            else {
                Random rand = new Random();
                ArrayList<Triple> chosen = new ArrayList<Triple>();
                for (int i = 0; i < n; i++) {
                    int index = rand.nextInt(this.headToList.get(head).size());
                    chosen.add(this.headToList.get(head).get(index));
                }
                return chosen;
            }
        }
        else return new ArrayList<Triple>();
    }



    public ArrayList<Triple> getTriplesByTail(String tail) {
        if (this.tailToList.containsKey(tail)) {
            return this.tailToList.get(tail);
        }
        else {
            return new ArrayList<Triple>();
        }
    }

    public ArrayList<Triple> getNTriplesByTail(String tail, int n) {

        if (this.tailToList.containsKey(tail)) {
            if (this.tailToList.get(tail).size() <= n) return this.tailToList.get(tail);
            else {
                Random rand = new Random();
                ArrayList<Triple> chosen = new ArrayList<Triple>();
                for (int i = 0; i < n; i++) {
                    int index = rand.nextInt(this.tailToList.get(tail).size());
                    chosen.add(this.tailToList.get(tail).get(index));
                }
                return chosen;
            }
        }
        else return new ArrayList<Triple>();
    }


    public ArrayList<Triple> getTriplesByRelation(String relation) {
        if (this.relationToList.containsKey(relation)) {
            return this.relationToList.get(relation);
        }
        else {
            return new ArrayList<Triple>();
        }
    }


    public Set<String> getRelations() {
        return this.relationToList.keySet();
    }

    public Set<String> getHeadEntities(String relation, String tail) {
        if (tailRelation2Head.get(tail) != null) {
            if (tailRelation2Head.get(tail).get(relation) != null) {
                return tailRelation2Head.get(tail).get(relation);
            }
        }
        return new HashSet<String>();
    }

    public Set<String> getTailEntities(String relation, String head) {
        if (headRelation2Tail.get(head) != null) {
            if (headRelation2Tail.get(head).get(relation) != null) {
                return headRelation2Tail.get(head).get(relation);
            }
        }
        return new HashSet<String>();
    }

    /**
     * Returns those values for which the relation holds for a given value. If the headNotTail is
     * set to true, the value is interpreted as head value and the corresponding tails are returned.
     * Otherwise, the corresponding heads are returned.
     *
     * @param relation The specified relation.
     * @param value The value interpreted as given head or tail.
     * @param headNotTail Whether to interpret the value as head and not as tail (false interprets as tail).
     * @return The resulting values.
     */
    public Set<String> getEntities(String relation, String value, boolean headNotTail) {
        if (headNotTail) return this.getTailEntities(relation, value);
        else return this.getHeadEntities(relation, value);

    }

    public Set<String> getRelations(String head, String tail) {
        if (headTail2Relation.get(head) != null) {
            if (headTail2Relation.get(head).get(tail) != null) {
                return headTail2Relation.get(head).get(tail);
            }
        }
        return new HashSet<String>();
    }

    public boolean isTrue(String head, String relation, String tail) {
        if (tailRelation2Head.get(tail) != null) {
            if (tailRelation2Head.get(tail).get(relation) != null) {
                return tailRelation2Head.get(tail).get(relation).contains(head);
            }
        }
        return false;
    }

    public boolean isTrue(Triple triple) {
        return this.isTrue(triple.get_subject(), triple.get_predicate(), triple.get_obj());
    }

    public void compareTo(TripleSet that, String thisId, String thatId) {
        System.out.println("* Comparing two triple sets");
        int counter = 0;
        for (Triple t : triples) {
            if (that.isTrue(t)) {
                counter++;
            }
        }

        System.out.println("* size of " + thisId + ": " +  this.triples.size());
        System.out.println("* size of " + thatId + ": " +  that.triples.size());
        System.out.println("* size of intersection: " + counter);

    }

    public TripleSet getIntersectionWith(TripleSet that) {// the same triples
        TripleSet ts = new TripleSet();
        for (Triple t : triples) {
            if (that.isTrue(t)) {
                ts.addTriple(t);
            }
        }
        return ts;
    }

    public TripleSet minus(TripleSet that) {// triples differentfrom that
        TripleSet ts = new TripleSet();
        for (Triple t : triples) {
            if (!that.isTrue(t)) {
                ts.addTriple(t);
            }
        }
        return ts;
    }

    public int getNumOfEntities() {
        Set<String> intersection = new HashSet<String>(headToList.keySet());
        intersection.retainAll(tailToList.keySet());
        //return headToList.keySet().size() + tailToList.keySet().size();// the old codes
        return headToList.keySet().size() + tailToList.keySet().size()-intersection.size();
    }

    public HashSet<String> getAllEntities() {
        HashSet <String> allEntities = new HashSet<String>(headToList.keySet());
        allEntities.addAll(tailToList.keySet());
        return allEntities;
    }


    public void determineFrequentRelations(double coverage) {
        HashMap<String, Integer> relationCounter = new HashMap<String, Integer>();
        int allCounter = 0;
        for (Triple t : this.triples) {
            allCounter++;
            String r = t.get_predicate();//.getRelation();
            if (relationCounter.containsKey(r)) {
                int count = relationCounter.get(r);
                relationCounter.put(r, count + 1);
            }
            else {
                relationCounter.put(r, 1);
            }
        }

        ArrayList<Integer> counts = new ArrayList<Integer>();
        counts.addAll(relationCounter.values());
        Collections.sort(counts);
        int countUp = 0;
        int border = 0;
        for (Integer c : counts) {
            countUp = countUp + c;
            //System.out.println("countUp: " + countUp);
            //System.out.println("c: " + c);
            if (((double)(allCounter - countUp) / (double)allCounter) < coverage) {
                border = c;
                break;
            }
        }

        //System.out.println("Number of all relations: " + relationCounter.size());
        //System.out.println("Relations covering " + coverage + " of all triples");
        for (String r : relationCounter.keySet()) {

            if (relationCounter.get(r) > border) {
                frequentRelations.add(r);
                //System.out.println(r + " (used in " + relationCounter.get(r) + " triples)");
            }
        }
        //System.out.println("Number of frequent (covering " + coverage+ " of all) relations: " + frequentRelations.size());
    }

    public boolean isFrequentRelation(String relation) {
        return this.frequentRelations.contains(relation);
    }

    public boolean existsPath(String x, String y, int pathLength) {
        if (pathLength == 1) {
            if (this.getRelations(x, y).size() > 0) {
                return true;
            }
            if (this.getRelations(y, x).size() > 0) {
                return true;
            }
            return false;
        }
        if (pathLength == 2) {
            Set<String> hop1x = new HashSet<String>();
            for (Triple hx : this.getTriplesByHead(x)) { hop1x.add(hx.get_obj()); }
            for (Triple tx : this.getTriplesByTail(x)) { hop1x.add(tx.get_subject()); }

            for (Triple hy : this.getTriplesByHead(y)) {
                if (hop1x.contains(hy.get_obj())) return true;
            }
            for (Triple ty : this.getTriplesByTail(y)) {
                if (hop1x.contains(ty.get_subject()))  return true;
            }
            return false;
        }
        if (pathLength > 2 ) {
            System.err.println("checking the existence of a path longer than 2 is so far not supported");
            System.exit(-1);

        }
        return false;

    }

    public Set<String> getEntities() {
        HashSet<String> entities = new HashSet<String>();
        entities.addAll(headToList.keySet());
        entities.addAll(tailToList.keySet());
        return entities;
    }

    public void write(String filepath) throws FileNotFoundException {
        PrintWriter  pw = new PrintWriter(filepath);

        for (Triple t : triples) {
            pw.println(t);
        }

        pw.flush();
        pw.close();

    }






}