package gilp.GILLearn_correction;

import gilp.comments.AnnotatedTriple;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.sparql.GetSparql;
import gilp.sparql.Sparql;
import other_algorithms.OpenIE.ReVerbExample;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//1.extend the sample triples by the hierarchical property range:rdfs:subClassOf
//2. extend the triples by the Levels level=2
//3. extract the neighborhoods
public class getSearchSpace {
    public void filterSearchSpace(HashSet<Triple> positiveTriple, HashSet<Triple> negativeTriple, int iterationTimes) throws Exception {
        HashSet<Triple> expandedPositiveTriples = new HashSet<>();
        HashSet<Triple> expandedNegativeTriples = new HashSet<>();
        int numbers=positiveTriple.size()+negativeTriple.size();
        new ILPLearnSettings(). writeOutFeedbacks(positiveTriple,negativeTriple,numbers);
        // method1:level
        if (ILPLearnSettings.conditionInSearchSpace == 1) {
            extendTriplesByLevel(positiveTriple, ILPLearnSettings.DEFAULT_LEVEL, true, iterationTimes);
            extendTriplesByLevel(negativeTriple, ILPLearnSettings.DEFAULT_LEVEL, false, iterationTimes);
          //  expandedNegativeTriples.removeAll(expandedPositiveTriples);
        } else {
//method2:neighborhood
            HashSet<String> positiveTargets = new RulesOthers().extractAllEntities(positiveTriple);
            HashSet<String> negativeTargets = new RulesOthers().extractAllEntities(negativeTriple);
            expandedPositiveTriples = extendTriplesByNeighborhood(positiveTargets);
            expandedNegativeTriples = extendTriplesByNeighborhood(negativeTargets);
            expandedNegativeTriples.removeAll(expandedPositiveTriples);

        }
        new ILPLearnSettings().writeSearchSpace(iterationTimes);

      //  new ILPLearnSettings().writeOutSearchSpace(expandedPositiveTriples, true, iterationTimes);
      //  new ILPLearnSettings().writeOutSearchSpace(expandedNegativeTriples, false, iterationTimes);
        // return new KVPair<>(expandedPositiveTriples, expandedNegativeTriples);
    }

    public void filterObjectSearchSpace(HashSet<String> positiveTargets, HashSet<String> negativeTargets, int iterationTimes) throws Exception {
        HashSet<Triple> expandedPositiveTriples = new HashSet<>();
        HashSet<Triple> expandedNegativeTriples = new HashSet<>();
        // method1:level
        if (ILPLearnSettings.conditionInSearchSpace == 1) {
            extendObjectsByLevel(positiveTargets, ILPLearnSettings.DEFAULT_LEVEL, true, iterationTimes);
            extendObjectsByLevel(negativeTargets, ILPLearnSettings.DEFAULT_LEVEL, false, iterationTimes);
            extendObjectsByReverseLevel(positiveTargets, ILPLearnSettings.DEFAULT_LEVEL, true, iterationTimes);
            extendObjectsByReverseLevel(negativeTargets, ILPLearnSettings.DEFAULT_LEVEL, false, iterationTimes);

        } else {
//method2:neighborhood

            expandedPositiveTriples = extendTriplesByNeighborhood(positiveTargets);
            expandedNegativeTriples = extendTriplesByNeighborhood(negativeTargets);
            expandedNegativeTriples.removeAll(expandedPositiveTriples);

        }
        new ILPLearnSettings().combineTwofiles("./data/searchSpace/positiveSearchSpace"+iterationTimes+"-old1.tsv","./data/searchSpace/positiveSearchSpace"+iterationTimes+"-old2.tsv",
                "./data/searchSpace/positiveSearchSpace"+iterationTimes+"-old.tsv");

        new ILPLearnSettings().combineTwofiles("./data/searchSpace/negativeSearchSpace"+iterationTimes+"-old1.tsv","./data/searchSpace/negativeSearchSpace"+iterationTimes+"-old2.tsv",
                "./data/searchSpace/negativeSearchSpace"+iterationTimes+"-old.tsv");

        new ILPLearnSettings().writeSearchSpace(iterationTimes);

        //  new ILPLearnSettings().writeOutSearchSpace(expandedPositiveTriples, true, iterationTimes);
        //  new ILPLearnSettings().writeOutSearchSpace(expandedNegativeTriples, false, iterationTimes);
        // return new KVPair<>(expandedPositiveTriples, expandedNegativeTriples);
    }
    public void extendObjectsByLevel
            (HashSet<String> positiveTargets,  int level, Boolean decision, int iterationTimes) throws IOException {
//right order
        String path = null;
        if (decision)
            path = "./data/searchSpace/positiveSearchSpace" + iterationTimes + "-old1.tsv";
        else
            path = "./data/searchSpace/negativeSearchSpace" + iterationTimes + "-old1.tsv";
        BufferedWriter  writer = new BufferedWriter(new FileWriter(path));

        HashSet<Triple> firstLevelTriples = new HashSet<>();
       // HashSet<String> extractSubject = new HashSet<>(positiveTargets);

        for (String subject : positiveTargets) {
            String newEntity = new GetSparql().sparqlInExtendTriple(subject);

            HashSet<Triple> getTriplesBySubject = new HashSet<>();

            if (ILPLearnSettings.condition == 1)
                getTriplesBySubject = new RDF3XEngine().getTriplesInSubject(newEntity);
            else
                getTriplesBySubject = extendAllTriplesBySubject(subject);

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
                allObjects.add(tri.get_obj());
            }
            for (String obj : allObjects) {
                String newEntity = new GetSparql().sparqlInExtendTriple(obj);
                HashSet<Triple> newLevelTriples = new HashSet<>();

                if (ILPLearnSettings.condition == 1)
                    newLevelTriples = new RDF3XEngine().getTriplesInSubject(newEntity);
                else
                    newLevelTriples = extendAllTriplesBySubject(obj);

                if (newLevelTriples != null) {
                    for(Triple key : newLevelTriples)
                        writer.write(key+"\n");
                    eachLevelTriples.addAll(newLevelTriples);
                }
            }
            if (eachLevelTriples.isEmpty())
                break;
            otherLevelTriples.clear();
            otherLevelTriples.addAll(eachLevelTriples);
        }
        writer.close();
    }

    public void extendObjectsByReverseLevel
            (HashSet<String> positiveTargets,  int level, Boolean decision, int iterationTimes) throws IOException {
//right order
        String path = null;
        if (decision)
            path = "./data/searchSpace/positiveSearchSpace" + iterationTimes + "-old2.tsv";
        else
            path = "./data/searchSpace/negativeSearchSpace" + iterationTimes + "-old2.tsv";
        BufferedWriter  writer = new BufferedWriter(new FileWriter(path));

        HashSet<Triple> firstLevelTriples = new HashSet<>();

        for (String subject : positiveTargets) {//as the object
            String newEntity = new GetSparql().sparqlInExtendTriple(subject);

            HashSet<Triple> getTriplesBySubject = new HashSet<>();

            if (ILPLearnSettings.condition == 1)
                getTriplesBySubject = new RDF3XEngine().getTriplesInObject(newEntity);
            else
                getTriplesBySubject = getTriplesByObject(subject);

            for(Triple key: getTriplesBySubject){
                writer.write(key+"\n");
            }

            HashSet<Triple> changedTriples = new HashSet<>(getTriplesBySubject);
            firstLevelTriples.addAll(changedTriples);
        }

        HashSet<Triple> otherLevelTriples = new HashSet<>(firstLevelTriples);

        for (int i = 1; i <= level - 1; i++) {

            HashSet<Triple> eachLevelTriples = new HashSet<>();
            HashSet<String> allSubjects = new HashSet<>();

            for (Triple tri : otherLevelTriples) {
                allSubjects.add(tri.get_subject());
            }
            for (String sub : allSubjects) {
                String newEntity = new GetSparql().sparqlInExtendTriple(sub);
                HashSet<Triple> newLevelTriples = new HashSet<>();
                System.out.println(sub+"  "+newEntity);

                if (ILPLearnSettings.condition == 1)
                    newLevelTriples = new RDF3XEngine().getTriplesInObject(newEntity);
                else
                    newLevelTriples = getTriplesByObject(sub);

                if (newLevelTriples != null) {
                    for(Triple key : newLevelTriples)
                        writer.write(key+"\n");
                    eachLevelTriples.addAll(newLevelTriples);
                }
            }
            if (eachLevelTriples.isEmpty())
                break;
            otherLevelTriples.clear();
            otherLevelTriples.addAll(eachLevelTriples);
        }
        writer.close();
    }


    public HashSet<Triple> extendTriplesByAbstract(HashSet<Triple> triples) throws IOException {
        // <s,p,o>-> <s,abstract.?> <o,abstract,?>
        //abstract--extract the triples

        HashSet<Triple> TripleInNeighborhood = new HashSet<Triple>();

        HashSet<Triple> TripleInSubjectAbstract = new HashSet<Triple>();

        HashSet<Triple> TripleInObjectAbstract = new HashSet<Triple>();
        for (Triple t : triples) {

            String subject = t.get_subject();
            String object = t.get_obj();

            String subjectSparql = "select ?object from <http://dbpedia.org> where{  <" + subject
                    + "> <http://dbpedia.org/ontology/abstract> ?object. filter( langMatches(lang(?object),\"en\"))} ";

            ArrayList<String> subjectNeighbor = new Sparql().getSingleResultsFromQuery(subjectSparql);


            for (String abs : subjectNeighbor) {

                String abstr = abs.replace("\"", "").replace("@en", "");
                HashSet<AnnotatedTriple> extractTriples = new ReVerbExample().reverbSentence(abstr);

                for (AnnotatedTriple ele : extractTriples) {

                    if (ele.get_sign() > 0.8) {


                        TripleInSubjectAbstract.add(ele.get_triple());
                    }
                }


            }

            //------------------------

            String objectSparql = "select ?object from <http://dbpedia.org> where{  <" + object
                    + "> <http://dbpedia.org/ontology/abstract> ?object. filter( langMatches(lang(?object),\"en\"))} ";


            ArrayList<String> objectNeighbor = new Sparql().getSingleResultsFromQuery(objectSparql);


            for (String abs : objectNeighbor) {

                String abstr = abs.replace("\"", "").replace("@en", "");
                HashSet<AnnotatedTriple> extractTriples = new ReVerbExample().reverbSentence(abstr);

                for (AnnotatedTriple ele : extractTriples) {

                    if (ele.get_sign() > 0.8) {


                        TripleInObjectAbstract.add(ele.get_triple());
                    }
                }

            }

            TripleInNeighborhood.addAll(TripleInSubjectAbstract);

            TripleInNeighborhood.addAll(TripleInObjectAbstract);

        }
        return TripleInNeighborhood;

    }


    public void extendTriplesByLevel
            (HashSet<Triple> filterTriples, int level, Boolean decision, int iterationTimes) throws IOException {

        String path = null;
        if (decision)
            path = "./data/searchSpace/positiveSearchSpace" + iterationTimes + "-old.tsv";
        else
            path = "./data/searchSpace/negativeSearchSpace" + iterationTimes + "-old.tsv";
        BufferedWriter  writer = new BufferedWriter(new FileWriter(path));

            HashSet<Triple> firstLevelTriples = new HashSet<>();
            HashSet<String> extractSubject = new HashSet<>();
            for (Triple triple : filterTriples) {
                extractSubject.add(triple.get_subject());
            }
            for (String subject : extractSubject) {
                String newEntity = new GetSparql().sparqlInExtendTriple(subject);

                HashSet<Triple> getTriplesBySubject = new HashSet<>();

                if (ILPLearnSettings.condition == 1)
                    getTriplesBySubject = new RDF3XEngine().getTriplesInSubject(newEntity);
                else
                    getTriplesBySubject = extendAllTriplesBySubject(subject);

                for(Triple key: getTriplesBySubject){
                    writer.write(key+"\n");
                }

                HashSet<Triple> changedTriples = new HashSet<>(getTriplesBySubject);
                firstLevelTriples.addAll(changedTriples);
            }

            HashSet<Triple> otherLevelTriples = new HashSet<>(firstLevelTriples);

        //    HashSet<Triple> triplesQualified = new HashSet<>(firstLevelTriples);
            //  writer.write("************************************\n");
            for (int i = 1; i <= level - 1; i++) {

                HashSet<Triple> eachLevelTriples = new HashSet<>();
                HashSet<String> allObjects = new HashSet<>();

                for (Triple tri : otherLevelTriples) {
                    allObjects.add(tri.get_obj());
                }
                for (String obj : allObjects) {
                    String newEntity = new GetSparql().sparqlInExtendTriple(obj);
                    HashSet<Triple> newLevelTriples = new HashSet<>();

                    if (ILPLearnSettings.condition == 1)
                        newLevelTriples = new RDF3XEngine().getTriplesInSubject(newEntity);
                    else
                        newLevelTriples = extendAllTriplesBySubject(obj);

                    if (newLevelTriples != null) {
                        for(Triple key : newLevelTriples)
                            writer.write(key+"\n");
                        eachLevelTriples.addAll(newLevelTriples);
                    }
                }

                if (eachLevelTriples.isEmpty())
                    break;

                otherLevelTriples.clear();
                otherLevelTriples.addAll(eachLevelTriples);


                //  eachLevelTriples.removeAll(triplesQualified);
              //  triplesQualified.addAll(eachLevelTriples);

//                if (otherLevelTriples.isEmpty() || eachLevelTriples.isEmpty())
//                    break;

            }

            writer.close();
         //   return triplesQualified;

    }

    public HashSet<Triple> extendTriplesByRDFNeighborhood(HashSet<Triple> filterTriples, int level, Boolean decision) {

        HashSet<Triple> triplesQualified = new HashSet<>();

        HashSet<String> oldTargets = new RulesOthers().extractAllEntities(filterTriples);

        for (int i = 1; i <= level - 1; i++) {
            HashSet<Triple> eachLevelTriples = new getSearchSpace().extendTriplesByNeighborhood(oldTargets);
            triplesQualified.addAll(eachLevelTriples);
            HashSet<String> extractTargets = new RulesOthers().extractAllEntities(eachLevelTriples);
            extractTargets.removeAll(oldTargets);
            oldTargets.clear();
            oldTargets.addAll(extractTargets);

            if (extractTargets.isEmpty() || oldTargets.isEmpty()) {
                break;
            }
        }

        return triplesQualified;
    }

    //---------------------------------------
    private HashSet<Triple> extendAllTriplesBySubject(String subject) {
        // not only consider the contents in the dbpedia--from <http://dbpedia.org>
        // how to limit the predicate and target to do the search space.
        //here, there are 4 conditions.

        String newEntity = new GetSparql().sparqlInExtendTriple(subject);

// limit the target
        String query1 = "select * from <http://dbpedia.org> " + "where{ " + newEntity
                + " ?predicate ?target. " + "FILTER("
                + "regex(str(?target ),\"http://dbpedia.org/resource\") "

                + "&& !regex(str(?predicate),\"wiki\") "
                + "&& regex(str(?predicate ),\"http://dbpedia.org/ontology\") "
                + "&& ?predicate NOT IN (<http://dbpedia.org/ontology/abstract>, <http://dbpedia.org/ontology/deathDate>,"
                + "<http://dbpedia.org/ontology/birthDate> ,<http://dbpedia.org/ontology/wikiPageExternalLink>, "
                + "<http://dbpedia.org/ontology/wikiPageWikiLink>)"
                + ")}";
        System.out.println(query1);

        ArrayList<HashMap<String, String>> extendNegativeEntities = new Sparql().getResultsFromQuery(query1);
        HashSet<String> samekey = new HashSet<String>();
        HashSet<Triple> triplesExtends = new HashSet<Triple>();
        if (extendNegativeEntities != null) {
            for (HashMap<String, String> key : extendNegativeEntities) {
                String predicate = key.get("predicate");
                String object = key.get("target");

                if (!samekey.contains(object)) {
                    samekey.add(object);

                    Triple negativeElement = new Triple(subject, predicate, object);

                    triplesExtends.add(negativeElement);

                }

            }
        }
        return triplesExtends;
    }

    private HashSet<Triple> extendAllTriplesByObjects(String object) {
        // not only consider the contents in the dbpedia--from <http://dbpedia.org>
        // how to limit the predicate and target to do the search space.
        //here, there are 4 conditions.

        String newEntity = new GetSparql().sparqlInExtendTriple(object);

        //   System.out.print("\n newEntity---:"+newEntity + "\n");
// limit the target
        String query1 = "select * from <http://dbpedia.org>" + "where{ ?target ?predicate " + newEntity
                + ". " + "FILTER("
                + "regex(str(?target ),\"http://dbpedia.org/resource\") "
                + "&&"
                + "regex(str(?predicate ),\"http://dbpedia.org/ontology\") "
                + ")}";

        ArrayList<HashMap<String, String>> extendNegativeEntities = new Sparql().getResultsFromQuery(query1);
        HashSet<String> samekey = new HashSet<>();
        HashSet<Triple> triplesExtends = new HashSet<>();
        if (extendNegativeEntities != null) {
            for (HashMap<String, String> key : extendNegativeEntities) {
                String predicate = key.get("predicate");
                String subject = key.get("target");

                if (!samekey.contains(subject)) {
                    samekey.add(subject);

                    Triple negativeElement = new Triple(subject, predicate, object);

                    triplesExtends.add(negativeElement);

                }

            }
        }
        return triplesExtends;
    }

    private HashSet<Triple> extendTriplesBySubject(String subject) {

        String newEntity = new GetSparql().sparqlInExtendTriple(subject);

        // System.out.print("\n newEntity---:"+newEntity + "\n");

        String extendNegativeQuery = "select * from <http://dbpedia.org> " + "where{ " + newEntity
                + " ?predicate ?entity. " + "FILTER(regex(str(?predicate ),\"http://dbpedia.org/ontology\") &&"
                + "?predicate NOT IN (<http://dbpedia.org/ontology/abstract>, <http://dbpedia.org/ontology/deathDate>,<http://dbpedia.org/ontology/wikiPageWikiLink>,"
                + "<http://dbpedia.org/ontology/birthDate>))" + "}";// ,<http://dbpedia.org/ontology/wikiPageExternalLink>,<http://dbpedia.org/property/tartan>

        String extendNegativeQuery2 = "select * from <http://dbpedia.org> " + "where{ " + subject
                + " ?predicate ?entity.} ";
        // System.out.print(extendNegativeQuery + "\n");
        ArrayList<HashMap<String, String>> extendNegativeEntitys = new Sparql()
                .getResultsFromQuery(extendNegativeQuery);
        // HashSet<Triple> positiveEntity = new HashSet<Triple>();

        HashSet<String> samekey = new HashSet<String>();

        HashSet<Triple> triplesExtends = new HashSet<Triple>();
        if (extendNegativeEntitys != null) {
            for (HashMap<String, String> key : extendNegativeEntitys) {
                String predicate = key.get("predicate");
                String object = key.get("entity");

                if (!samekey.contains(object)) {
                    samekey.add(object);

                    Triple negativeElement = new Triple(subject, predicate, object);

                    triplesExtends.add(negativeElement);

                    // System.out.println(negativeElement + "\t" + positiveExtends + "\n");
                }

            }
        }
        return triplesExtends;
    }

    //----------------------------------------
    public HashSet<Triple> extendTriplesByNeighborhood(HashSet<String> targets) {//HashSet<Triple> filterTriples
        // <s,p,o>-> <s,?.?> <?,?,s> <o,?.?> <?,?,o>
        //  HashSet<String> targets =  new RulesOthers().extractAllEntities(filterTriples);

        HashSet<Triple> TripleInNeighborhood = new HashSet<Triple>();
        for (String t : targets) {
            String newEntity = new GetSparql().sparqlInExtendTriple(t);
            HashSet<Triple> subjectTriple = new HashSet<>();
            if (ILPLearnSettings.condition == 1)
                subjectTriple = new RDF3XEngine().getTriplesInSubject(newEntity);
            else
                subjectTriple = extendAllTriplesBySubject(newEntity);

            TripleInNeighborhood.addAll(subjectTriple);
            HashSet<Triple> objectTriple = new HashSet<Triple>();
            if (ILPLearnSettings.condition == 1)
                objectTriple = new RDF3XEngine().getTriplesInObject(newEntity);
            else
                objectTriple = getTriplesByObject(t);

            TripleInNeighborhood.addAll(objectTriple);
        }
        return TripleInNeighborhood;

    }

    private HashSet<Triple> getTriplesByObject(String object) {
        String newEntity = new GetSparql().sparqlInExtendTriple(object);
        String objectSparql = "select distinct ?subject ?predicate from <http://dbpedia.org> where{ ?subject ?predicate "
                + newEntity + ". }";
        System.out.println(objectSparql);
        ArrayList<HashMap<String, String>> objectNeighbor = new Sparql().getResultsFromQuery(objectSparql);
        HashSet<Triple> objectTriple = new HashSet<Triple>();

        for (HashMap<String, String> key : objectNeighbor) {
            String pred = key.get("predicate");
            String sub = key.get("subject");
            Triple objectElement = new Triple(sub, pred, object);
            objectTriple.add(objectElement);
        }
        return objectTriple;

    }

    public HashSet<Triple> extendDBpediaTriplesByNeighborhood(HashSet<String> targets) {

        // <s,p,o>-> <s,?.?> <?,?,s> <o,?.?> <?,?,o>
        HashSet<Triple> TripleInNeighborhood = new HashSet<Triple>();
        for (String t : targets) {
            String newEntity = new GetSparql().sparqlInExtendTriple(t);
            String subjectSparql = "select distinct ?predicate ?object from <http://dbpedia.org> where{  " + newEntity
                    + " ?predicate ?object. } ";
            System.out.println(subjectSparql);
            ArrayList<HashMap<String, String>> subjectNeighbor = new Sparql().getResultsFromQuery(subjectSparql);
            HashSet<Triple> subjectTriple = new HashSet<Triple>();

            for (HashMap<String, String> key : subjectNeighbor) {
                String pred = key.get("predicate");
                String obj = key.get("object");
                Triple subjectElement = new Triple(newEntity, pred, obj);
                subjectTriple.add(subjectElement);
            }

            TripleInNeighborhood.addAll(subjectTriple);

            //---------------------------------------------------------------------------
            String objectSparql = "select distinct ?subject ?predicate from <http://dbpedia.org> where{ ?subject ?predicate "
                    + newEntity + ". }";
            System.out.println(objectSparql);
            ArrayList<HashMap<String, String>> objectNeighbor = new Sparql().getResultsFromQuery(objectSparql);
            HashSet<Triple> objectTriple = new HashSet<Triple>();

            for (HashMap<String, String> key : objectNeighbor) {
                String pred = key.get("predicate");
                String sub = key.get("subject");
                Triple objectElement = new Triple(sub, pred, newEntity);
                objectTriple.add(objectElement);
            }
            TripleInNeighborhood.addAll(objectTriple);
        }
        return TripleInNeighborhood;
    }

    public HashSet<Triple> extendTriplesPeerBasedTypes(HashSet<Triple> triples) {
        // occupation, nationality, or field of work
        return null;
    }

    public HashSet<Triple> extendTriplesByObjectNeighborhood(HashSet<Triple> triples) {
        // <s,p,o>-> <o,?.?> <?,?,o>  --- level=2
        HashSet<Triple> TripleInNeighborhood = new HashSet<Triple>();
        HashSet<String> allobject = new HashSet<>();
        for (Triple t : triples) {
            allobject.add(t.get_obj());
        }
        for (String object : allobject) {
            String subjectSparql = "select distinct ?predicate ?object where{  <" + object
                    + "> ?predicate ?object. } ";
            HashSet<Triple> subjectTriple = new RDF3XEngine().getTriplesBySubject(object);
            TripleInNeighborhood.addAll(subjectTriple);
//---------------------------------------------------------------------------
            String objectSparql = "select distinct ?subject ?predicate where{ ?subject ?predicate <"
                    + object + ">. }";
            HashSet<Triple> objectTriple = new RDF3XEngine().getTriplesByObject(object);
            TripleInNeighborhood.addAll(objectTriple);
        }
        return TripleInNeighborhood;

    }

    public static void getEntityByTypes(String maxObjectType) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream("./data/dbpedia-type/object_type_online.txt"),
                Charset.forName("UTF-8"));
        String sql = "select distinct ?a from <http://dbpedia.org> where {?a a ?type. filter( ?type=<" + maxObjectType + ">)}";//
        System.out.println("sql:" + sql + "\n");
        HashSet<String> typeOne = new HashSet<>();
        HashSet<String> types = new Sparql().getSingleVariable(sql);
        System.out.println("size:" + types.size() + "\n");
        for (String type : types) {
            String[] bits = type.split("http://dbpedia.org/resource/");
            String lastOne = bits[bits.length - 1];
            writer.write(lastOne + "\n");
            typeOne.add(lastOne);
        }
        writer.close();
        Writer writer1 = new OutputStreamWriter(new FileOutputStream("./data/dbpedia-type/object_type_rdf.txt"),
                Charset.forName("UTF-8"));
        String sql1 = "select distinct ?a where {?a a ?type. filter( ?type=<" + maxObjectType + ">)}";
        System.out.println("sql:" + sql1 + "\n");
        HashSet<String> types1 = new RDF3XEngine().getDistinctEntity(sql1);

        System.out.println("size-types1:" + types1.size() + "\n");
        HashSet<String> typeTwo = new HashSet<>();
        for (String type : types1) {
            String[] bits = type.split("http://dbpedia.org/resource/");
            String lastOne = bits[bits.length - 1];
            writer1.write(lastOne + "\n");
            typeTwo.add(lastOne);
        }
        writer1.close();
        HashSet<String> sameTypes = new HashSet<>(typeOne);
        sameTypes.retainAll(typeTwo);
        Writer writer2 = new OutputStreamWriter(new FileOutputStream("./data/dbpedia-type/sameTypes.txt"),
                Charset.forName("UTF-8"));
        for (String key : sameTypes)
            writer2.write(key + "\n");
        writer2.close();

        System.out.println("sameSize:" + sameTypes.size() + "\n");
        HashSet<String> oneDifferTypes = new HashSet<>(typeOne);
        oneDifferTypes.removeAll(typeTwo);
        Writer writer3 = new OutputStreamWriter(new FileOutputStream("./data/dbpedia-type/webDifferTypes.txt"),
                Charset.forName("UTF-8"));
        for (String key : oneDifferTypes)
            writer3.write(key + "\n");
        writer3.close();
        System.out.println("oneDifferTypes:" + oneDifferTypes.size() + "\n");
        HashSet<String> twoDifferTypes = new HashSet<>(typeTwo);
        twoDifferTypes.removeAll(typeOne);

        Writer writer4 = new OutputStreamWriter(new FileOutputStream("./data/dbpedia-type/rdfDifferTypes.txt"),
                Charset.forName("UTF-8"));
        for (String key : twoDifferTypes)
            writer4.write(key + "\n");
        writer4.close();
        System.out.println("twoDifferTypes:" + twoDifferTypes.size() + "\n");
    }

}
