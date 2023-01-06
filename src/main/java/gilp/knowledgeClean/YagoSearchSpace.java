package gilp.knowledgeClean;
/*
 * In the closed world assumption, the search space.
 */

import gilp.comments.Comment;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;


//1.extend the sample triples by the hierarchical property range:rdfs:subClassOf
//2. extend the triples by the Levels level=2
//3. extract the neighborhoods


public class YagoSearchSpace {

    public HashSet<Triple> extendTriplesByComments(HashSet<Comment> filterComments, int level) {

        HashSet<Triple> firstLevelTriples = new HashSet<>();

        HashSet<Triple> triplesOriginal = new HashSet<>();

        for (Comment comment : filterComments) {

            Triple triple = comment.get_triple();

            triplesOriginal.add(triple);

            String subject = triple.get_subject();
            HashSet<Triple> getTriplesBySubject = new RDF3XEngine().getTriplesBySubject(subject);

            HashSet<Triple> changedTriples = new HashSet<>();
            for (Triple triple1 : getTriplesBySubject) {
                String predicate = triple1.get_predicate();
                Triple tr = new Triple(triple1.get_subject(), predicate, triple1.get_obj());
                changedTriples.add(tr);
            }
            changedTriples.remove(comment.get_triple());

            firstLevelTriples.addAll(changedTriples);
        }

        HashSet<Triple> otherLevelTriples = new HashSet<>(firstLevelTriples);

        HashSet<Triple> triplesQualified = new HashSet<>(otherLevelTriples);

        for (int i = 1; i <= level - 1; i++) {

            HashSet<Triple> getTriplesBySubject1 = new HashSet<>();

            HashSet<String> allObjects = new HashSet<>();

            for (Triple tri : otherLevelTriples) {
                String obj = tri.get_obj();
                if (!allObjects.contains(obj)) {
                    allObjects.add(obj);

                    if (obj.startsWith("?")) {
                        obj = "<" + obj + ">";
                    }

                    HashSet<Triple> getTriplesBySubject = new RDF3XEngine().getTriplesBySubject(obj);

                    if (getTriplesBySubject != null) {

                        for (Triple tri1 : getTriplesBySubject) {
                            String predicate = tri1.get_predicate();
                            Triple tr = new Triple(tri1.get_subject(), predicate, tri1.get_obj());
                            getTriplesBySubject1.add(tr);
                        }
                    }
                }
            }

            otherLevelTriples.clear();
            otherLevelTriples.addAll(getTriplesBySubject1);

            getTriplesBySubject1.removeAll(triplesQualified);
            triplesQualified.addAll(getTriplesBySubject1);

            if (otherLevelTriples.isEmpty() || getTriplesBySubject1.isEmpty()) {
                break;
            }
        }
        HashSet<Triple> anotherTriples = new HashSet<>();

        anotherTriples.addAll(triplesQualified);
        anotherTriples.addAll(triplesOriginal);

        return anotherTriples;
    }

    public void extendTriples(HashSet<Triple> filterTriples, int level, Boolean decision, int iterationTimes) throws IOException {
        String path = null;
        if (decision)
            path = "./data/yago_correction/searchSpace/positiveSearchSpace" + iterationTimes + "-old.tsv";
        else
            path = "./data/yago_correction/searchSpace/negativeSearchSpace" + iterationTimes + "-old.tsv";
        BufferedWriter  writer = new BufferedWriter(new FileWriter(path));
      //  HashSet<Triple> anotherTriples = new HashSet<>();
        HashSet<Triple> firstLevelTriples = new HashSet<>();

        HashSet<String> extractSubject = new HashSet<>();
        for (Triple triple : filterTriples) {
            extractSubject.add(triple.get_subject());
        }

        for (String subject  : extractSubject) {

            HashSet<Triple> getTriplesBySubject = new RDF3XEngine().getTriplesBySubject(subject);
            for(Triple key: getTriplesBySubject){
                writer.write(key+"\n");
            }
            HashSet<Triple> changedTriples = new HashSet<>(getTriplesBySubject);

            firstLevelTriples.addAll(changedTriples);
        }

        HashSet<Triple> otherLevelTriples = new HashSet<>(firstLevelTriples);

        for (int i = 1; i <= level - 1; i++) {

            HashSet<Triple> eachLevelTriples = new HashSet<>();
            HashSet<String> allObjects = new HashSet<>();

            for (Triple tri : otherLevelTriples) {

                String obj = tri.get_obj();

                if (obj.startsWith("?"))
                    obj = "<" + obj + ">";

                allObjects.add(obj);
            }

            for (String obj : allObjects) {

                if (!allObjects.contains(obj)) {
                    allObjects.add(obj);

                    if (obj.startsWith("?")) {
                        obj = "<" + obj + ">";
                    }

                    HashSet<Triple> newLevelTriples = new RDF3XEngine().getTriplesBySubject(obj);

                    if (newLevelTriples != null) {
                        eachLevelTriples.addAll(newLevelTriples);
                        for(Triple key : newLevelTriples)
                            writer.write(key+"\n");

                    }
                }
            }

            if (eachLevelTriples.isEmpty())
                break;
            otherLevelTriples.clear();
            otherLevelTriples.addAll(eachLevelTriples);
           // anotherTriples.addAll(eachLevelTriples);
        }
     //   anotherTriples.addAll(firstLevelTriples);
        writer.close();
      //  return anotherTriples;
    }

    }




