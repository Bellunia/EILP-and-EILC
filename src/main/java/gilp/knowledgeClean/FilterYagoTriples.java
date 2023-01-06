package gilp.knowledgeClean;

import gilp.comments.AnnotatedTriple;
import gilp.comments.Comment;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.RDFSubGraphSet;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import javatools.parsers.NumberFormatter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/*
 *  In the closed world assumption, the search space.
 */
public class FilterYagoTriples {

    public HashSet<Comment> generateRandomComments(String initialName) throws IOException {
        long time = System.currentTimeMillis();

        HashSet<Triple> firstTriples = new HashSet<>();

        HashSet<String> allName = GILPSettings.getInformationEntropyNames();

        HashMap<Triple, Triple> selectAllTriples = new HashMap<>();

        // --- get the first Instances for the first feedback
        for (String secondName : allName) {
            LinkedHashMap<Triple, Triple> selectTriples = selectLinkedSingleTriples(initialName, secondName);
            selectAllTriples.putAll(selectTriples);
            firstTriples.addAll(selectAllTriples.keySet());
        }

        // do the first feedback based on the Chinese surnames
        HashSet<Comment> selectedComments = new HashSet<>(new RuleLearnerHelper().tripleToComment(firstTriples));

        // the second feedback-- start -------------------
        HashSet<Triple> negativeTriples = new HashSet<>();
        for (Comment comment : selectedComments) {
            if (!comment.get_decision())// negative
                negativeTriples.add(comment.get_triple());
        }

        HashSet<Triple> negativeLinkedTriple = new HashSet<>();
        for (Triple triple : selectAllTriples.keySet()) {
            if (negativeTriples.contains(triple))
                negativeLinkedTriple.add(selectAllTriples.get(triple));
        }


        HashSet<String> subjectSelectInitial = new HashSet<>();

        for (Triple triple : negativeLinkedTriple) {
            String predicateName = triple.get_predicate();
            String obj = triple.get_obj();

            String sql = "select distinct ?a where {?a <" + initialName + "> ?b. ?a <" + predicateName + "> <" + obj
                    + ">.} ";

            HashSet<String> subjectDoubleSql = new RDF3XEngine().getDistinctEntity(sql); // all conditionNumber subjects

            int subjectsSizes = subjectDoubleSql.size();
            int maxObjectGroups = selectAllTriples.size();

            for (int i = Math.min(maxObjectGroups, subjectsSizes); i > 0; i--) {
                String sub = null;
                if (subjectsSizes == 1) {
                    sub = (String) subjectDoubleSql.toArray()[0];// .get(0);
                } else {
                    sub = (String) subjectDoubleSql.toArray()[new Random().nextInt(subjectsSizes)];
                }
                subjectSelectInitial.add(sub);
            }
        }

        HashSet<Triple> selectSecondTriples = subjectToTriple(subjectSelectInitial, initialName);
        // do the second feedback based on the Chinese surnames
        selectedComments.addAll(new RuleLearnerHelper().tripleToComment(selectSecondTriples));

        System.out.print("Found " + selectedComments.size() + " feedbacks! \n");

        long selectTime3 = System.currentTimeMillis() - time;
        float selectTime3_sec = selectTime3 / 1000F;
        System.out.println("Time:" + selectTime3_sec + "seconds\n");

        int positive = 0;
        int negative = 0;

        for (Comment comment : selectedComments) {
            if (comment.get_decision())// positive
                positive++;
            else
                negative++;
        }

        System.out.print("Finally. Found " + positive + "positive feedbacks! \n");
        System.out.print("Finally. Found " + negative + "negative feedbacks! \n");

        BufferedWriter writer = new BufferedWriter(new FileWriter(GILPSettings.getRootPath() + "/data/feedback/GILP_feedback.txt"));


        for (Comment key : selectedComments) {
            writer.write(key.get_triple() + " " + key.get_decision() + "\n");
        }

        writer.close();

        return selectedComments;
    }

    public HashSet<Triple> subjectToTriple(HashSet<String> subjects, String initialName) {
        HashSet<Triple> triples = new HashSet<>();

        for (String subject : subjects) {
            Clause clause = new Clause();
            clause.addPredicate(new RDFPredicate(subject, initialName, "?o"));

            RDFSubGraphSet rdfSubGraphSet = new RDF3XEngine().getTriplesByCNF(clause);
            HashSet<Triple> subjectTriples = rdfSubGraphSet.getAllTriples();

            triples.addAll(subjectTriples);
        }
        return triples;
    }

    public LinkedHashMap<HashSet<String>, Double> selectObjectGroup(String initialName, String secondName) {
        LinkedHashMap<HashSet<String>, Double> doubleObjectGroup = new LinkedHashMap<>();
        HashMap<String, Double> objects = new HashMap<>();

        String sparql = "select count ?c where {?a <" + initialName + "> ?b. ?a <" + secondName + "> ?c.} order by ?c";

        HashMap<String, Double>   objectsWithNumbers = new RDF3XEngine().getCountSingleEntity(sparql);

        HashMap<String, Double>  doubleObjectSort =  new RuleLearnerHelper().reverseOrderByValue(objectsWithNumbers);

        HashSet<Double> differentValues = new HashSet<>();

        for (String key : doubleObjectSort.keySet()) {
            double value = doubleObjectSort.get(key);
            objects.put(key, value);

            differentValues.add(value);
        }


        for (double value : differentValues) {
            HashSet<String> sqlHashMapKey = new HashSet<>();
            HashSet<String>   sqlHashMap = new RuleLearnerHelper().getAllKeysForValue(objects, value);

            for (String str : sqlHashMap) {
                sqlHashMapKey.add(str.replace("[", "").replace("]", ""));
            }
            doubleObjectGroup.put(sqlHashMapKey, value);
        }

        LinkedHashMap<HashSet<String>, Double> selectObjectGroup = new LinkedHashMap<>();

        for (HashSet<String> key : doubleObjectGroup.keySet()) {
            double value = doubleObjectGroup.get(key);
            selectObjectGroup.put(key, value);

        }
        return selectObjectGroup;
    }

    public LinkedHashMap<Triple, Triple> selectLinkedSingleTriples(String initialName, String secondName) {
        LinkedHashMap<HashSet<String>, Double> objectsWithNumbers = selectObjectGroup(initialName, secondName);
        LinkedHashMap<Triple, Triple> linkedTriples = new LinkedHashMap<>();
        HashSet<String> selectInitialSubject = new HashSet<>();

        for (HashSet<String> key : objectsWithNumbers.keySet()) {
            String randomKey = null;
            String newKey = null;

            if (key.size() > 1) {
                int randomNumber1 = new Random().nextInt(key.size());
                randomKey = (String) key.toArray()[randomNumber1];// .get(randomNumber1);
                newKey = randomKey.toString().replace("[", "").replace("]", "");

            } else if (key.size() == 1) {
                newKey = key.toString().replace("[", "").replace("]", "");
            }
            String sql = "select ?a where {?a <" + initialName + "> ?b. ?a <" + secondName + "> <" + newKey + "> .}";

            HashSet<String> allSubjects = new RDF3XEngine().getDistinctEntity(sql); // all subjects

            int subjectsSizes = allSubjects.size();
            if (subjectsSizes == 0) {
                continue;
            }
            int randomNumber = subjectsSizes == 1 ? 0 : new Random().nextInt(allSubjects.size());
            String subjectRandom = (String) allSubjects.toArray()[randomNumber];// .get(randomNumber);

            selectInitialSubject.add(subjectRandom);
        }

        for (String sub : selectInitialSubject) {
            Triple subTripl = singleSubjectToTriple(sub, initialName);
            Triple subTripl1 = singleSubjectToTriple(sub, secondName);
            if (subTripl == null || subTripl1 == null) {
                continue;
            }
            linkedTriples.put(subTripl, subTripl1);
        }
        return linkedTriples;
    }

    public Triple singleSubjectToTriple(String sub, String initialName) {
        String str = sub.replace("<", "").replace(">", "");
        Clause clause = new Clause();
        clause.addPredicate(new RDFPredicate(str, initialName, "?o"));

        RDFSubGraphSet rdfSubGraphSet = new RDF3XEngine().getTriplesByCNF(clause);
        HashSet<Triple> subTriples = rdfSubGraphSet.getAllTriples();
        Triple triple = null;
        if (subTriples != null) {

            triple = (Triple) subTriples.toArray()[0];// .get(0);
        }
        return triple;
    }

    public HashSet<Comment> generateAllFalseChineseComments(String initialName) throws IOException {

        HashSet<Comment> selectedComments = new HashSet<>();

        HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();

        HashSet<Triple> TriplesFromChineseSurnames = new HashSet<>();
        for (String str : chineseSurnames) {

            RDFPredicate tp = new RDFPredicate();
            tp.setSubject("?x");
            tp.setObject(str);
            tp.setPredicateName(GILPSettings.DEFAULT_PREDICATE_NAME);

            HashSet<Triple> triplesByObject = new RDF3XEngine().getTriples(tp);

            if (triplesByObject != null)

                TriplesFromChineseSurnames.addAll(triplesByObject);

        }

        for (Triple tri : TriplesFromChineseSurnames) {
            Comment cmt = new Comment(tri.clone(), false);
            if (!selectedComments.contains(cmt))
                selectedComments.add(cmt);

        }
        HashSet<String> selectedComments1 = new HashSet<>();
        BufferedWriter writer1 = null;
        writer1 = new BufferedWriter(new FileWriter(GILPSettings.getRootPath() + "/data/feedback/allFalseTriples.txt"));

        for (Comment key : selectedComments) {
            if (!selectedComments1.contains(key.get_triple().get_subject())) {
                writer1.write(key.get_triple() + "\n");
                selectedComments1.add(key.get_triple().get_subject());

            }
        }

        writer1.close();

        System.out.print("Finally. Found " + TriplesFromChineseSurnames.size() + " negative feedbacks! \n");

        return selectedComments;
    }

    public HashSet<Comment> generateAllFalseComments(String initialName) throws IOException {

        HashSet<Comment> negativeChineseComments = generateAllFalseChineseComments(initialName);

        HashSet<Comment> selectedComments = new HashSet<Comment>();
        selectedComments.addAll(negativeChineseComments);

        HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();
        HashSet<String> asianSurnames = GILPSettings.getAsianSurnameList();
        HashSet<String> specialPlaces = GILPSettings.getSpecialRegionsList();

        asianSurnames.removeAll(chineseSurnames);

        // -------------------------------------------

        HashSet<Triple> TriplesFromChineseSurnames = new HashSet<Triple>();
        for (String str : asianSurnames) {

            RDFPredicate tp = new RDFPredicate();
            tp.setSubject("?x");
            tp.setObject(str);
            tp.setPredicateName(GILPSettings.DEFAULT_PREDICATE_NAME);

            HashSet<Triple> triplesByObject = new RDF3XEngine().getTriples(tp);

            TriplesFromChineseSurnames.addAll(triplesByObject);

        }

        for (Triple key : TriplesFromChineseSurnames) {
            String obj = key.get_obj();

            if (chineseSurnames.contains(obj)) {
                selectedComments.add(new Comment(key.clone(), false));
            } else if (asianSurnames.contains(obj)) {

                String subject = key.get_subject();

                String sql = "select distinct ?a where {<" + subject + ">  <" + GILPSettings.DEFAULT_CONDITIONAL_NAME
                        + "> ?a.} ";

                HashSet<String> placeSpecial = new RDF3XEngine().getDistinctEntity(sql); // all special place

                if (placeSpecial != null && specialPlaces.containsAll(placeSpecial)) {
                    selectedComments.add(new Comment(key.clone(), false));
                } else {

                    continue;
                }

            } else {
                selectedComments.add(new Comment(key.clone(), true));
            }
        }

        return selectedComments;
    }

    public HashSet<Comment> generateComments(String initialName) throws IOException {

        long time = System.currentTimeMillis();

        HashSet<Comment> negativeComments = generateAllFalseChineseComments(initialName);

        HashSet<Comment> positiveComments = new RuleLearnerHelper().filterComments(generateRandomComments(initialName),
                true);

        negativeComments.addAll(positiveComments);

        HashSet<Comment> comments = new HashSet<>();
        HashSet<String> selectedComments = new HashSet<>();

        BufferedWriter writer1 = new BufferedWriter(
                new FileWriter(GILPSettings.getRootPath() + "/data/feedback/GILP_feedback_subjects_test.txt"));

        for (Comment key : negativeComments) {
            if (!selectedComments.contains(key.get_triple().get_subject())) {
                writer1.write(key.get_triple() + "	" + key.get_decision() + "\n");
                selectedComments.add(key.get_triple().get_subject());
                comments.add(key);
            }
        }

        writer1.close();

        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));

        return comments;
    }

    //------------------------------------------------------------------------------------------------------------------------
    public HashSet<Comment> getAlmostAllOfComments(String initialName) throws IOException {

        HashSet<Triple> firstTriples = new HashSet<>();

        HashSet<String> allName = GILPSettings.getInformationEntropyNames();

        HashMap<Triple, Triple> selectAllTriples = new HashMap<Triple, Triple>();

        for (String secondName : allName) {
            LinkedHashMap<Triple, Triple> selectTriples = new LinkedHashMap<Triple, Triple>();
            selectTriples = selectLinkedSingleTriples(initialName, secondName);
            selectAllTriples.putAll(selectTriples);
            firstTriples.addAll(selectAllTriples.keySet());
        }

        HashSet<Comment> selectedPositiveComments = new RuleLearnerHelper()
                .filterComments(tripleDeleteRoughComment(firstTriples), true);

        HashSet<Comment> positiveExpandComments = expandPositiveCommentsByObjects(selectedPositiveComments);

        System.out.print("Finally. Found " + positiveExpandComments.size() + "positive feedbacks! \n");

        HashSet<Comment> negativeExpandComments = generateAllFalseComments(initialName);
        System.out.print("Finally. Found " + negativeExpandComments.size() + "negative feedbacks! \n");

        BufferedWriter writer = null;
        writer = new BufferedWriter(
                new FileWriter(GILPSettings.getRootPath() + "/data/feedback/GILP_feedback_all.txt"));


        HashSet<Comment> commentsSet = new HashSet<Comment>(negativeExpandComments);
        commentsSet.addAll(positiveExpandComments);

        for (Comment key : commentsSet) {
            writer.write(key.get_triple() + "\t" + key.get_decision() + "\n");
        }

        writer.close();

        System.out.print("Finally. Found " + commentsSet.size() + "all feedbacks! \n");


        return commentsSet;
    }

    public HashSet<Comment> expandPositiveCommentsByObjects(HashSet<Comment> comments) throws IOException {

        HashSet<String> selectedObjects = new HashSet<String>();

        HashSet<Comment> selectedComments = new HashSet<Comment>();
        for (Comment key : comments) {

            if (!selectedObjects.contains(key.get_triple().get_obj())) {

                selectedObjects.add(key.get_triple().get_obj());

            }

        }

        HashSet<String> asianSurnames = GILPSettings.getAsianSurnameList();
        selectedObjects.removeAll(asianSurnames);

        HashSet<Triple> TriplesFromDifferentObjects = new HashSet<Triple>();
        for (String str : selectedObjects) {

            RDFPredicate tp = new RDFPredicate();
            tp.setSubject(new String("?x"));
            tp.setObject(new String(str));
            tp.setPredicateName(GILPSettings.DEFAULT_PREDICATE_NAME);

            HashSet<Triple> triplesByObject = new RDF3XEngine().getTriples(tp);

            TriplesFromDifferentObjects.addAll(triplesByObject);

        }

        for (Triple tri : TriplesFromDifferentObjects) {
            Comment cmt = new Comment(tri.clone(), true);
            if (!selectedComments.contains(cmt))
                selectedComments.add(cmt);

        }

        return selectedComments;
    }

    public HashSet<Comment> tripleDeleteRoughComment(HashSet<Triple> selectTriples) {

        HashSet<Comment> selectedComments = new HashSet<Comment>();

        HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();
        HashSet<String> asianSurnames = GILPSettings.getAsianSurnameList();
        HashSet<String> specialPlaces = GILPSettings.getSpecialRegionsList();

        asianSurnames.removeAll(chineseSurnames);

        for (Triple key : selectTriples) {
            String obj = key.get_obj();

            if (chineseSurnames.contains(obj)) {
                selectedComments.add(new Comment(key.clone(), false));
            } else if (asianSurnames.contains(obj)) {

                String subject = key.get_subject();

                String sql = "select distinct ?a where {<" + subject + ">  <" + GILPSettings.DEFAULT_CONDITIONAL_NAME
                        + "> ?a.} ";

                HashSet<String> placeSpecial = new RDF3XEngine().getDistinctEntity(sql);

                if (placeSpecial != null && specialPlaces.containsAll(placeSpecial)) {
                    selectedComments.add(new Comment(key.clone(), false));
                } else {

                    continue;
                }

            } else {
                selectedComments.add(new Comment(key.clone(), true));
            }
        }

        return selectedComments;
    }

    //----------------------------------------------------------------------

//****************************************************************************************************
    //---------------------------------------------------------------------
    public HashSet<Comment> getRandomComments(int numbers, HashSet<Comment> listComments) throws IOException {

        HashSet<Comment> randomPositiveComments = filterRandomCommentsByObject(numbers,
                filterComments(listComments, true));

        HashSet<Comment> randomNegativeComments = filterRandomCommentsByObject(numbers,
                filterComments(listComments, false));

        HashSet<Comment> allRandomComments = new HashSet<Comment>();

        allRandomComments.addAll(randomNegativeComments);

        allRandomComments.addAll(randomPositiveComments);

        Writer writer = new OutputStreamWriter(
                new FileOutputStream(GILPSettings.getRootPath() + "/data/gilpRules/selectedComments.txt"),
                Charset.forName("UTF-8"));

        for (Comment cmt : allRandomComments)

            writer.write(cmt.get_triple() + " " + cmt.get_decision() + "\n");

        writer.close();

        return allRandomComments;
    }
    public HashSet<Comment> filterRandomCommentsByObject(int numbers, HashSet<Comment> filterComments) {// @2019.11.27
        // randomly choose @num comments
        int s = filterComments.size();

        int[] isChosen = new int[s];
        for (int i = 0; i < s; i++) {
            isChosen[i] = 0;
        }
        HashSet<Comment> comments = new HashSet<>();

        HashSet<String> object = new HashSet<>();

        while (comments.size() < Math.min(numbers, s)) {
            int idx = (int) Math.round(Math.random() * (s - 1));
            Comment cmt = (Comment) filterComments.toArray()[idx];

            if (isChosen[idx] == 0 && !object.contains(cmt.get_triple().get_obj())) {
                comments.add(cmt);
                object.add(cmt.get_triple().get_obj());
                isChosen[idx] = 1;
            }
        }
        return comments;
    }

    public HashSet<Comment> filterComments(HashSet<Comment> listComments, Boolean decision) {
        HashSet<Comment> negativeComments = new HashSet<Comment>();
        HashSet<Comment> positiveComments = new HashSet<Comment>();

        for (Comment comment : listComments) {
            if (comment.get_decision()) {
                positiveComments.add(comment);
            } else if (!comment.get_decision()) {
                negativeComments.add(comment);
            }
        }

        if (decision) {
            return positiveComments;
        } else {
            return negativeComments;
        }
    }

    LinkedHashMap<ArrayList<String>, Double> DoubleObjectGroup(String intialName, String secondName) {

        HashMap<String, Double> DoubleObject = new HashMap<String, Double>();
        HashMap<String, Double> DoubleObjectSort = new HashMap<String, Double>();
        LinkedHashMap<ArrayList<String>, Double> DoubleObjectGroup = new LinkedHashMap<ArrayList<String>, Double>();
        HashMap<String, Double> Objects = new HashMap<String, Double>();

        String sparql = "select count ?c where {?a <" + intialName + "> ?b. ?a <" + secondName + "> ?c.} order by ?c";
        DoubleObject = new RDF3XEngine().getCountSingleEntity(sparql);

        DoubleObjectSort = new RuleLearnerHelper().reverseOrderByValue(DoubleObject);

        ArrayList<Double> differentValue = new ArrayList<Double>();
        for (String key : DoubleObjectSort.keySet()) {

            double value = DoubleObjectSort.get(key);
            Objects.put(key, value);

            if (!differentValue.contains(value)) {
                differentValue.add(value);
            }
        }

        HashSet<String> sqlHashMap = new HashSet<String>();

        for (double value : differentValue) {
            ArrayList<String> sqlHashMapkey1 = new ArrayList<String>();
            sqlHashMap = new RuleLearnerHelper().getAllKeysForValue(Objects, value);
            for(String key:sqlHashMap) {
                sqlHashMapkey1.add(key.replace("[", "").replace("]", ""));
            }
            DoubleObjectGroup.put(sqlHashMapkey1, value);

        }

        return DoubleObjectGroup;
    }

    public LinkedHashMap<ArrayList<String>, Double> DoubleObjectGroup_select(String intialName, String secondName) {
        // only select the objects that the number is more than mean.

        HashMap<String, Double> DoubleObject = new HashMap<String, Double>();
        HashMap<String, Double> DoubleObjectSort = new HashMap<String, Double>();
        LinkedHashMap<ArrayList<String>, Double> DoubleObjectGroup = new LinkedHashMap<ArrayList<String>, Double>();
        HashMap<String, Double> Objects = new HashMap<String, Double>();

        String sparql = "select count ?c where {?a <" + intialName + "> ?b. ?a <" + secondName + "> ?c.} order by ?c";
        DoubleObject = new RDF3XEngine().getCountSingleEntity(sparql);

        DoubleObjectSort = new RuleLearnerHelper().reverseOrderByValue(DoubleObject);

        ArrayList<Double> differentValue = new ArrayList<Double>();
        for (String key : DoubleObjectSort.keySet()) {

            double value = DoubleObjectSort.get(key);
            Objects.put(key, value);

            if (!differentValue.contains(value)) {
                differentValue.add(value);
            }
        }

        HashSet<String> sqlHashMap = new HashSet<String>();
        double sum = 0;

        for (double value : differentValue) {
            ArrayList<String> sqlHashMapkey1 = new ArrayList<String>();
            sqlHashMap = new RuleLearnerHelper().getAllKeysForValue(Objects, value);

            for(String key: sqlHashMap){
                String mapKey = key.replace("[", "").replace("]", "");
                sqlHashMapkey1.add(mapKey);
            }

            DoubleObjectGroup.put(sqlHashMapkey1, value);
            sum = sum + value;
        }

        Double mean = 0.0;

        mean = (double) (sum / (DoubleObjectGroup.size()));

        LinkedHashMap<ArrayList<String>, Double> DoubleObjectGroup_select = new LinkedHashMap<ArrayList<String>, Double>();

        for (ArrayList<String> key : DoubleObjectGroup.keySet()) {
            Double value = DoubleObjectGroup.get(key);
            if (value > mean) {
                DoubleObjectGroup_select.put(key, value);
            }
        }

        return DoubleObjectGroup_select;
    }

    public LinkedHashMap<Triple, HashSet<Triple>> selectLinkedTriples(String intialName, String secondName) {

        LinkedHashMap<ArrayList<String>, Double> objByOtherName = DoubleObjectGroup(intialName, secondName);
        HashSet<Triple> selectTriples = new HashSet<Triple>();
        LinkedHashMap<Triple, HashSet<Triple>> selectTriplesMap = new LinkedHashMap<Triple, HashSet<Triple>>();
        ArrayList<String> subjectSelectInitial = new ArrayList<String>();
        ArrayList<String> sqlName = new ArrayList<String>();
        ArrayList<String> sqlSecondName = new ArrayList<String>();

        HashMap<String, String> subjectChooseOneInstance = new HashMap<String, String>();

        sqlName.add(intialName);
        sqlName.add(secondName);
        sqlSecondName.add(secondName);

        for (ArrayList<String> key : objByOtherName.keySet()) {
            String randomkey = null;
            String key1 = null;
            if (key.size() > 1) {
                int randomNumber1 = new Random().nextInt(key.size());
                randomkey = key.get(randomNumber1);
                key1 = randomkey.toString().replace("[", "").replace("]", "");
            } else if (key.size() == 1) {
                key1 = key.toString().replace("[", "").replace("]", "");
            }
            String sql = "select distinct ?a where {?a <" + intialName + "> ?b. ?a <" + secondName + "> <" + key1
                    + ">.}";
            // System.out.print(sql + "\n***********************");
            HashSet<String> subjectDoublesql = new RDF3XEngine().getDistinctEntity(sql); // all subjects
            int randomNumber = new Random().nextInt(subjectDoublesql.size());
            String subjectRandom = (String) subjectDoublesql.toArray()[randomNumber];// .get(randomNumber);
            subjectSelectInitial.add(subjectRandom);
            subjectChooseOneInstance.put(key1, subjectRandom); // put( object, one random subject)
        }

        for (String sub : subjectSelectInitial) {
            Clause cls = new Clause();
            cls.addPredicate(new RDFPredicate(sub, intialName, "?o"));
            RDFSubGraphSet sg_set = new RDF3XEngine().getTriplesByCNF(cls);
            HashSet<Triple> subTripl = sg_set.getAllTriples();

            selectTriples.add((Triple) subTripl.toArray()[0]);// .get(0));

            Clause cls1 = new Clause();
            cls1.addPredicate(new RDFPredicate(sub, secondName, "?q"));
            RDFSubGraphSet sg_set1 = new RDF3XEngine().getTriplesByCNF(cls1);
            HashSet<Triple> subTripl1 = sg_set1.getAllTriples();

            selectTriplesMap.put((Triple) subTripl.toArray()[0], subTripl1);
        }
        return selectTriplesMap;
    }

    public ArrayList<String> InformationEntropyName() {
        String pred = "/home/wy/gilp_learn/data/yago/entropy.txt";
        ArrayList<String> p1 = new ArrayList<String>();
        RandomAccessFile file_data = null;
        try {
            file_data = new RandomAccessFile(pred, "r");
            String line = "";
            while ((line = file_data.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, "\n ");
                String s;
                s = st.nextToken();
                Collections.addAll(p1, s.split("\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p1;
    }

    public LinkedHashMap<Triple, HashSet<Triple>> selectLinkedTriples_select(String intialName, String secondName) {

        LinkedHashMap<ArrayList<String>, Double> objByOtherName = DoubleObjectGroup_select(intialName, secondName);
        ArrayList<Triple> selectTriples = new ArrayList<Triple>();
        LinkedHashMap<Triple, HashSet<Triple>> selectTriplesMap = new LinkedHashMap<Triple, HashSet<Triple>>();
        ArrayList<String> subjectSelectInitial = new ArrayList<String>();
        ArrayList<String> sqlName = new ArrayList<String>();
        ArrayList<String> sqlSecondName = new ArrayList<String>();

        HashMap<String, String> subjectChooseOneInstance = new HashMap<String, String>();

        sqlName.add(intialName);
        sqlName.add(secondName);
        sqlSecondName.add(secondName);

        for (ArrayList<String> key : objByOtherName.keySet()) {
            String randomkey = null;
            String key1 = null;
            if (key.size() > 1) {
                int randomNumber1 = new Random().nextInt(key.size());
                randomkey = key.get(randomNumber1);
                key1 = randomkey.toString().replace("[", "").replace("]", "");
            } else if (key.size() == 1) {
                key1 = key.toString().replace("[", "").replace("]", "");
            }
            String sql = "select ?a where {?a <" + intialName + "> ?b. ?a <" + secondName + "> <" + key1 + ">.}";
            // System.out.print(sql + "\n***********************");
            HashSet<String> subjectDoublesql = new RDF3XEngine().getDistinctEntity(sql); // all subjects
            // System.out.print(subjectDoublesql + "\n%%%%%%%%%%%%%%%%%%%%");

            int randomNumber = new Random().nextInt(subjectDoublesql.size());
            String subjectRandom = (String) subjectDoublesql.toArray()[randomNumber];// .get(randomNumber)
            subjectSelectInitial.add(subjectRandom);
            subjectChooseOneInstance.put(key1, subjectRandom); // put( object, one random subject)
        }

        for (String sub : subjectSelectInitial) {
            Clause cls = new Clause();
            cls.addPredicate(new RDFPredicate(sub, intialName, "?o"));
            RDFSubGraphSet sg_set = new RDF3XEngine().getTriplesByCNF(cls);
            HashSet<Triple> subTripl = sg_set.getAllTriples();

            selectTriples.add((Triple) subTripl.toArray()[0]);// .get(0));

            Clause cls1 = new Clause();
            cls1.addPredicate(new RDFPredicate(sub, secondName, "?q"));
            RDFSubGraphSet sg_set1 = new RDF3XEngine().getTriplesByCNF(cls1);
            HashSet<Triple> subTripl1 = sg_set1.getAllTriples();

            selectTriplesMap.put((Triple) subTripl.toArray()[0], subTripl1);
        }
        return selectTriplesMap;
    }

    public HashSet<Comment> Comments(String intialName, Integer maxObjectGroups) throws IOException {

        ArrayList<String> allName = InformationEntropyName();
        HashMap<Triple, HashSet<Triple>> selectTriplesAll = new HashMap<Triple, HashSet<Triple>>();

        // -------------get the first Instances for the first
        // feedback-----------------------------------------
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter("/home/wy/Desktop/InstancesFirst.txt"));

        for (String secondName : allName) {
            LinkedHashMap<Triple, HashSet<Triple>> selectTriples = selectLinkedTriples(intialName, secondName);

            int i = 0;
            for (Triple key : selectTriples.keySet()) {

                selectTriplesAll.put(key, selectTriples.get(key));

                writer.write(key + " 1" + "\n");// the default ,all triple is true

                if (i == maxObjectGroups) {// the condition to next name
                    break;
                }
                i++;
            }
        }
        writer.close();

        // -----------finish get the first
        // feedback---------------------------------------

        ArrayList<Triple> listTriple = new ArrayList<Triple>();
        ArrayList<Comment> listComments = new ArrayList<Comment>();

        System.out.print("Enter the path of file contaning the first feedback: ");

        Scanner input = new Scanner(System.in);// home/wy/Documents/GILP/feedback1/comments.txt
        File file = new File(input.nextLine());
        input = new Scanner(file);

        while (input.hasNextLine()) {
            String line = input.nextLine().replace("<", "").replace(">", "");// replace the symbol "<, >" , "".
            StringTokenizer st = new StringTokenizer(line, " ");
            String s, p, o;
            s = st.nextToken();// extract the subject
            p = st.nextToken();// extract the predicate
            o = st.nextToken();// extract the object
            int d = Integer.parseInt(st.nextToken());// get the feedback, 1 stand the positive,0 stand for the negative
            Triple t = new Triple(s, p, o);
            Comment cmt = new Comment(t, (d > 0));// d>0,positive rules,(d>0)stands for the true.when (d<0)stands for
            // false.
            listComments.add(cmt);// generate a set of triples and comments
            listTriple.add(t);
        }
        if (file.exists()) {
            System.out.print("Read the feedback file. Found " + listTriple.size() + " feedbacks!\n");
        }
        input.close();

        HashSet<Comment> listLinkedComments = new HashSet<Comment>(listComments);

        // the second feedback-- start -------------------
        ArrayList<Triple> NegativeTriple = new ArrayList<Triple>();
        for (Comment cmt1 : listComments) {
            if (!cmt1.get_decision())// negative
                NegativeTriple.add(cmt1.get_triple());
        }

        ArrayList<Triple> NegativeLinkedTriple = new ArrayList<Triple>();
        for (Triple tri : selectTriplesAll.keySet()) {
            if (NegativeTriple.contains(tri))
                NegativeLinkedTriple.addAll(selectTriplesAll.get(tri));
        }

        HashSet<String> subjectDoublesql = new HashSet<String>();
        HashSet<String> subjectSelectInitial = new HashSet<String>();
        for (Triple tri : NegativeLinkedTriple) {
            String predName = tri.get_predicate().replace("<", "").replace(">", "");
            String obj = tri.get_obj();

            String sql = "select distinct ?a where {?a <" + intialName + "> ?b. ?a <" + predName + "> <" + obj + ">.} ";
            // System.out.print(sql + "\n");
            subjectDoublesql = new RDF3XEngine().getDistinctEntity(sql); // all conditionNumber subjects

            int j = 0;
            for (String str : subjectDoublesql) {

                subjectSelectInitial.add(str);
                if (j == maxObjectGroups) {// choose the maximum number of selectTriples//??????
                    break;
                }
                j++;
            }
        }

        // List<String> subsecondInitialDistinct =
        // subjectSelectInitial.stream().distinct().collect(Collectors.toList());
        // get the different subject for the second feedback
        HashSet<Triple> selectSecondTriples = new HashSet<Triple>();
        for (String str : subjectSelectInitial) {
            Clause cls = new Clause();
            cls.addPredicate(new RDFPredicate(str, intialName, "?o"));
            RDFSubGraphSet sg_set = new RDF3XEngine().getTriplesByCNF(cls);
            HashSet<Triple> subTripl = sg_set.getAllTriples();
            selectSecondTriples.add((Triple) subTripl.toArray()[0]);
            ;// .get(0));
        }

        BufferedWriter writer1 = null;
        writer1 = new BufferedWriter(new FileWriter("/home/wy/Desktop/InstancesSecond.txt"));
        for (Triple key : selectSecondTriples) {
            writer1.write(key + " 0" + "\n");// the second default ,all instacne is false,here, the negative more.
        }
        writer1.close();
        // go to get the negative feedback and the positive feedback

        // second feedback--- finish-------------------------------

        HashSet<Triple> listTriple1 = new HashSet<Triple>();
        HashSet<Comment> listComments1 = new HashSet<Comment>();

        System.out.print("Enter the path of file contaning the second feedback: ");

        Scanner input1 = new Scanner(System.in);// home/wy/Documents/GILP/feedback1/Secondcomments.txt
        File file1 = new File(input1.nextLine());
        input1 = new Scanner(file1);
        while (input1.hasNextLine()) {
            String line = input1.nextLine().replace("<", "").replace(">", "");// replace the symbol "<, >" , "".
            StringTokenizer st = new StringTokenizer(line, " ");
            String s, p, o;
            s = st.nextToken();// extract the subject
            p = st.nextToken();// extract the predicate
            o = st.nextToken();// extract the object
            int d = Integer.parseInt(st.nextToken());// get the feedback, 1 stand the positive,0 stand for the negative
            Triple t1 = new Triple(s, p, o);
            Comment cmt1 = new Comment(t1, (d > 0));// d>0,positive rules,(d>0)stands for the true.when (d<0)stands for
            // false.
            listComments1.add(cmt1);// generate a set of triples and comments
            listTriple1.add(t1);
        }
        if (file1.exists()) {
            System.out.print("Read the second feedback file. Found " + listTriple1.size() + " feedbacks! \n");
        }
        input1.close();

        listLinkedComments.addAll(listComments1);
        // List<Comment> listLinkedCommentsDistinct =
        // listLinkedComments.stream().distinct().collect(Collectors.toList());
        // get the different comments
        System.out.print("Finally. Found " + listLinkedComments.size() + " feedbacks! \n");
        int pos = 0;
        int neg = 0;

        for (Comment cmt1 : listLinkedComments) {
            if (cmt1.get_decision())// positive
                pos++;
            else
                neg++;
        }
        System.out.print("Finally. Found " + pos + "positive feedbacks! \n");
        System.out.print("Finally. Found " + neg + "negative feedbacks! \n");
        // -------------export the lastComments
        BufferedWriter writer2 = null;
        writer2 = new BufferedWriter(new FileWriter("/home/wy/Desktop/FinallyComments.txt"));
        for (Comment key : listLinkedComments) {
            writer2.write(key + "\n");
        }
        writer2.close();
        // ------------finish getting the comments.

        return listLinkedComments;
    }

    public HashSet<Comment> Comments_select(String intialName) throws IOException {

        HashSet<Comment> listLinkedComments = new HashSet<Comment>();

        ArrayList<String> allName = InformationEntropyName();
        HashMap<Triple, HashSet<Triple>> selectTriplesAll = new HashMap<Triple, HashSet<Triple>>();

        // -------------get the first Instances for the first
        // feedback-----------------------------------------
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter("/home/wy/Desktop/InstancesFirst.txt"));
        HashSet<String> objects = new HashSet<String>();

        for (String secondName : allName) {
            LinkedHashMap<Triple, HashSet<Triple>> selectTriples = new LinkedHashMap<Triple, HashSet<Triple>>();
            selectTriples = selectLinkedTriples_select(intialName, secondName);

            for (Triple key : selectTriples.keySet()) {
                if (!objects.contains(key.get_obj())) {

                    selectTriplesAll.put(key, selectTriples.get(key));

                    writer.write(key + "\t" + "1" + "\n");// the default ,all triple is true

                    objects.add(key.get_obj());
                }

            }
        }
        writer.close();

        // -----------finish get the first
        // feedback---------------------------------------

        HashSet<Triple> listTriple = new HashSet<Triple>();
        HashSet<Comment> listComments = new HashSet<Comment>();

        System.out.print("Enter the path of file contaning the first feedback: ");

        Scanner input = new Scanner(System.in);// home/wy/Documents/GILP/feedback1/comments.txt
        File file = new File(input.nextLine());
        input = new Scanner(file);

        while (input.hasNextLine()) {
            String line = input.nextLine();
            StringTokenizer st = new StringTokenizer(line, "\t");
            String s, p, o;
            s = st.nextToken();// extract the subject
            p = st.nextToken();// extract the predicate
            o = st.nextToken();// extract the object
            int d = Integer.parseInt(st.nextToken());// get the feedback, 1 stand the positive,0 stand for the negative
            Triple t = new Triple(s, p, o);
            Comment cmt = new Comment(t, (d > 0));// d>0,positive rules,(d>0)stands for the true.when (d<0)stands for
            // false.
            listComments.add(cmt);// generate a set of triples and comments
            listTriple.add(t);
        }
        if (file.exists()) {
            System.out.print("Read the feedback file. Found " + listTriple.size() + " feedbacks!\n");
        }
        input.close();

        listLinkedComments.addAll(listComments);

        // the second feedback-- start -------------------
        HashSet<Triple> NegativeTriple = new HashSet<Triple>();
        for (Comment cmt1 : listComments) {
            if (!cmt1.get_decision())// negative
                NegativeTriple.add(cmt1.get_triple());
        }

        HashSet<Triple> NegativeLinkedTriple = new HashSet<Triple>();
        for (Triple tri : selectTriplesAll.keySet()) {
            if (NegativeTriple.contains(tri))
                NegativeLinkedTriple.addAll(selectTriplesAll.get(tri));
        }

        HashSet<String> subjectDoublesql = new HashSet<String>();
        HashSet<String> subjectSelectInitial = new HashSet<String>();

        int maxObjectGroups = (selectTriplesAll.size() / 2);

        for (Triple tri : NegativeLinkedTriple) {
            String predName = tri.get_predicate();// .replace("<", "").replace(">", "");
            String obj = tri.get_obj();

            String sql = "select distinct ?a where {?a <" + intialName + "> ?b. ?a <" + predName + "> <" + obj + ">.} ";
            // System.out.print(sql + "\n");
            subjectDoublesql = new RDF3XEngine().getDistinctEntity(sql); // all conditionNumber subjects

            int j = 0;
            for (String str : subjectDoublesql) {
                String sub = str;// .replace("<", "").replace(">", "");
                subjectSelectInitial.add(sub);
                if (j == maxObjectGroups) {// choose the half number of selectTriplesAll to do the second feedback
                    break;
                }
                j++;
            }
        }

        List<String> subsecondInitialDistinct = subjectSelectInitial.stream().distinct().collect(Collectors.toList());
        // get the different subject for the second feedback
        ArrayList<Triple> selectSecondTriples = new ArrayList<Triple>();
        for (String str : subsecondInitialDistinct) {
            Clause cls = new Clause();
            cls.addPredicate(new RDFPredicate(str, intialName, "?o"));
            RDFSubGraphSet sg_set = new RDF3XEngine().getTriplesByCNF(cls);
            HashSet<Triple> subTripl = sg_set.getAllTriples();
            selectSecondTriples.add((Triple) subTripl.toArray()[0]);
        }

        BufferedWriter writer1 = null;
        writer1 = new BufferedWriter(new FileWriter("/home/wy/Desktop/InstancesSecond.txt"));
        for (Triple key : selectSecondTriples) {
            writer1.write(key + "\t" + "0" + "\n");// the second default ,all instacne is false,here, the negative more.
        }
        writer1.close();
        // go to get the negative feedback and the positive feedback

        // second feedback--- finish-------------------------------

        HashSet<Triple> listTriple1 = new HashSet<Triple>();
        HashSet<Comment> listComments1 = new HashSet<Comment>();

        System.out.print("Enter the path of file contaning the second feedback: ");

        Scanner input1 = new Scanner(System.in);// home/wy/Documents/GILP/feedback1/Secondcomments.txt
        File file1 = new File(input1.nextLine());
        input1 = new Scanner(file1);
        while (input1.hasNextLine()) {
            String line = input1.nextLine().replace("<", "").replace(">", "");// replace the symbol "<, >" , "".
            StringTokenizer st = new StringTokenizer(line, "\t");
            String s, p, o;
            s = st.nextToken();// extract the subject
            p = st.nextToken();// extract the predicate
            o = st.nextToken();// extract the object
            int d = Integer.parseInt(st.nextToken());// get the feedback, 1 stand the positive,0 stand for the negative
            Triple t1 = new Triple(s, p, o);
            Comment cmt1 = new Comment(t1, (d > 0));// d>0,positive rules,(d>0)stands for the true.when (d<0)stands for
            // false.
            listComments1.add(cmt1);// generate a set of triples and comments
            listTriple1.add(t1);
        }
        if (file1.exists()) {
            System.out.print("Read the second feedback file. Found " + listTriple1.size() + " feedbacks! \n");
        }
        input1.close();

        listLinkedComments.addAll(listComments1);
        // List<Comment> listLinkedCommentsDistinct =
        // listLinkedComments.stream().distinct().collect(Collectors.toList());
        // get the different comments
        System.out.print("Finally. Found " + listLinkedComments.size() + " feedbacks! \n");
        int pos = 0;
        int neg = 0;

        for (Comment cmt1 : listLinkedComments) {
            if (cmt1.get_decision())// positive
                pos++;
            else
                neg++;
        }
        System.out.print("Finally. Found " + pos + "positive feedbacks! \n");
        System.out.print("Finally. Found " + neg + "negative feedbacks! \n");
        // -------------export the lastComments
        BufferedWriter writer2 = null;
        writer2 = new BufferedWriter(new FileWriter("/home/wy/Desktop/FinallyComments.txt"));
        for (Comment key : listLinkedComments) {
            writer2.write(key + "\n");
        }
        writer2.close();
        // ------------finish getting the comments.

        return listLinkedComments;
    }

    public HashSet<Comment> tripleToAnnotated(HashSet<Triple> selectTriples,int num) throws IOException {
        // do the feedback based on the Chinese surnames
        HashSet<Comment> selectedComments = new HashSet<Comment>();

        HashSet<AnnotatedTriple> selectedAnnotatedTriple = new HashSet<AnnotatedTriple>();

        HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();
        HashSet<String> asianSurnames = GILPSettings.getAsianSurnameList();
        HashSet<String> specialPlaces = GILPSettings.getSpecialRegionsList();

        asianSurnames.removeAll(chineseSurnames);

        for (Triple key : selectTriples) {
            String obj = key.get_obj();

            if (chineseSurnames.contains(obj)) {
                selectedComments.add(new Comment(key.clone(), false));
            } else if (asianSurnames.contains(obj)) {

                String subject = key.get_subject();

                String sql = "select distinct ?a where {<" + subject + ">  <" + GILPSettings.DEFAULT_CONDITIONAL_NAME
                        + "> ?a.} ";

                HashSet<String> placeSpecial = new RDF3XEngine().getDistinctEntity(sql); // all special place
                int negative = 0;
                int positive = 0;

                if (placeSpecial != null) {
                    for (String str : placeSpecial) {
                        // String place = str.replace("<", "").replace(">", "");
                        if (specialPlaces.contains(str))
                            negative++;
                        else
                            positive++;
                    }
                }

                if (placeSpecial.size() == negative && negative > 0) {
                    selectedComments.add(new Comment(key.clone(), false));
                } else if (positive == placeSpecial.size() && positive > 0) {
                    selectedComments.add(new Comment(key.clone(), true));

                } else {
                    selectedAnnotatedTriple.add(new AnnotatedTriple(key.clone(), 0));
                }

//				if (negative > positive) {
//					selectedComments.add(new AnnotatedTriple(key.clone(), -1));
//				}

            } else {
                selectedComments.add(new Comment(key.clone(), true));
            }
        }

        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter("/home/wy/Desktop/InstancesFirst"+num+".txt"));
        for (Comment cmt : selectedComments) {

            writer.write(cmt + "\n");
        }

//		writer.write("-----------\n");

//		for(AnnotatedTriple cmt:selectedAnnotatedTriple) {
//
//			writer.write(cmt+"\n");
//		}

        writer.close();

        return selectedComments;
    }

    public HashSet<Comment> getMoreNegativeTriples(HashSet<Triple> negativeTriple) throws IOException{
        HashSet<Triple> newTriple = new HashSet<Triple>();

        for(Triple tri: negativeTriple) {

            String sub =tri.get_subject();
            String relation =tri.get_predicate();
            String obj= tri.get_obj();

            String sparql = "select distinct ?a where{?a <" + relation + "> <"+obj+">.}";//?b. ?a <" + firstName + "> ?c. }";
            HashSet<String> differentSubjects = new RDF3XEngine().getDistinctEntity(sparql);// .getCountSingleEntity(sparql);
            differentSubjects.remove(sub);
            for(String str: differentSubjects)
                newTriple.add(new Triple(str,relation,obj));


            String sparql1 = "select distinct ?a where{<"+sub+"> <" + relation + "> ?a.}";//?b. ?a <" + firstName + "> ?c. }";
            HashSet<String> differentObjects = new RDF3XEngine().getDistinctEntity(sparql1);// .getCountSingleEntity(sparql);
            differentObjects.remove(obj);

            for(String str: differentObjects)
                newTriple.add(new Triple(sub,relation,str));




        }

        HashSet<Comment> selectedComments = tripleToAnnotated(newTriple,-1);

        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter("/home/wy/Desktop/InstancesSecond1.txt"));
        for (Comment cmt : selectedComments) {

            writer.write(cmt + "\n");
        }

//		writer.write("-----------\n");

//		for(AnnotatedTriple cmt:selectedAnnotatedTriple) {
//
//			writer.write(cmt+"\n");
//		}

        writer.close();

        return selectedComments;
    }

    public HashSet<Comment> CommentsByEntropyName(String intialName) throws IOException {

        HashSet<Comment> listLinkedComments = new HashSet<Comment>();

        ArrayList<String> allName = InformationEntropyName();
        String firstName = allName.get(0);

        String sparql = "select distinct ?c where{?a <" + intialName + "> ?b. ?a <" + firstName + "> ?c. }";
        HashSet<String> countObject = new RDF3XEngine().getDistinctEntity(sparql);// .getCountSingleEntity(sparql);

        int maxObjectGroups = countObject.size();

        HashMap<Triple, HashSet<Triple>> selectTriplesAll = new HashMap<Triple, HashSet<Triple>>();

        // -------------get the first Instances for the first
        // feedback-----------------------------------------
        HashSet<Triple> selectFirstTriples = new HashSet<Triple>();
        for (String secondName : allName) {
            LinkedHashMap<Triple, HashSet<Triple>> selectTriples = new LinkedHashMap<Triple, HashSet<Triple>>();
            selectTriples = selectLinkedTriples(intialName, secondName);
            HashSet<String> differentObjects = new HashSet<String>();
            int i = 0;
            for (Triple key : selectTriples.keySet()) {
                if (!differentObjects.contains(key.get_obj())) {
                    selectTriplesAll.put(key, selectTriples.get(key));
                    selectFirstTriples.add(key);
                    if (i == maxObjectGroups) {// the condition to next name
                        break;
                    }
                    i++;
                }
            }
        }

        HashSet<Comment> selectedFirstComments = tripleToAnnotated(selectFirstTriples,2);
        // -finish get the first feedback---------------------------------------

        listLinkedComments.addAll(selectedFirstComments);

//method1:--- select second triples for comments

        // the second feedback-- start -------------------
        HashSet<Triple> NegativeTriple = new HashSet<Triple>();
        for (Comment cmt1 : selectedFirstComments) {
            if (!cmt1.get_decision())// negative
                NegativeTriple.add(cmt1.get_triple());
        }

        HashSet<Comment> getMoreNegativeTriples= getMoreNegativeTriples(NegativeTriple);

        listLinkedComments.addAll(getMoreNegativeTriples);

        HashSet<Triple> NegativeLinkedTriple = new HashSet<Triple>();
        for (Triple tri : selectTriplesAll.keySet()) {
            if (NegativeTriple.contains(tri))
                NegativeLinkedTriple.addAll(selectTriplesAll.get(tri));
        }

        HashSet<String> subjectDoublesql = new HashSet<String>();
        HashSet<String> subjectSelectInitial = new HashSet<String>();
        for (Triple tri : NegativeLinkedTriple) {
            String predName = tri.get_predicate();
            String obj = tri.get_obj();

            String sql = "select distinct ?a where {?a <" + intialName + "> ?b. ?a <" + predName + "> <" + obj + ">.} ";
            // System.out.print(sql + "\n");
            subjectDoublesql = new RDF3XEngine().getDistinctEntity(sql); // all conditionNumber subjects

            int j = 0;
            for (String str : subjectDoublesql) {

                subjectSelectInitial.add(str);
                if (j == maxObjectGroups) {// choose the maximum number of selectTriples//??????
                    break;
                }
                j++;
            }
        }

        // get the different subject for the second feedback
        HashSet<Triple> selectSecondTriples = new HashSet<Triple>();
        for (String str : subjectSelectInitial) {
            Clause cls = new Clause();
            cls.addPredicate(new RDFPredicate(str, intialName, "?o"));
            RDFSubGraphSet sg_set = new RDF3XEngine().getTriplesByCNF(cls);
            HashSet<Triple> subTripl = sg_set.getAllTriples();
            selectSecondTriples.add((Triple) subTripl.toArray()[0]);// .get(0));
        }
        HashSet<Comment> selectedSecondComments = tripleToAnnotated(selectSecondTriples,2);

//		BufferedWriter writer1 = null;
//		writer1 = new BufferedWriter(new FileWriter("/home/wy/Desktop/InstancesSecond.txt"));
//		for (Triple key : selectSecondTriples) {
//			writer1.write(key + "\t0" + "\n");// the second default ,all instacne is false,here, the negative more.
//		}
//		writer1.close();
        // go to get the negative feedback and the positive feedback

        // second feedback--- finish-------------------------------

//		HashSet<Triple> listTriple1 = new HashSet<Triple>();
//		HashSet<Comment> listComments1 = new HashSet<Comment>();
//
//		System.out.print("Enter the path of file contaning the second feedback: ");
//
//		Scanner input1 = new Scanner(System.in);// home/wy/Documents/GILP/feedback1/Secondcomments.txt
//		File file1 = new File(input1.nextLine());
//		input1 = new Scanner(file1);
//		while (input1.hasNextLine()) {
//			String line = input1.nextLine();// .replace("<", "").replace(">", "");// replace the symbol "<, >" , "".
//			StringTokenizer st = new StringTokenizer(line, " ");
//			String s, p, o;
//			s = st.nextToken();// extract the subject
//			p = st.nextToken();// extract the predicate
//			o = st.nextToken();// extract the object
//			int d = Integer.parseInt(st.nextToken());// get the feedback, 1 stand the positive,0 stand for the negative
//			Triple t1 = new Triple(s, p, o);
//			Comment cmt1 = new Comment(t1, (d > 0));// d>0,positive rules,(d>0)stands for the true.when (d<0)stands for
//													// false.
//			listComments1.add(cmt1);// generate a set of triples and comments
//			listTriple1.add(t1);
//		}
//		if (file1.exists()) {
//			System.out.print("Read the second feedback file. Found " + listTriple1.size() + " feedbacks! \n");
//		}
//		input1.close();

        listLinkedComments.addAll(selectedSecondComments);

        System.out.print("Finally. Found " + listLinkedComments.size() + " feedbacks! \n");
        int pos = 0;
        int neg = 0;

        for (Comment cmt1 : listLinkedComments) {
            if (cmt1.get_decision())// positive
                pos++;
            else
                neg++;
        }
        System.out.print("Finally. Found " + pos + "positive feedbacks! \n");
        System.out.print("Finally. Found " + neg + "negative feedbacks! \n");
        // -------------export the lastComments
        BufferedWriter writer2 = null;
        writer2 = new BufferedWriter(new FileWriter("/home/wy/Desktop/FinallyComments.txt"));
        for (Comment key : listLinkedComments) {
            writer2.write(key + "\n");
        }
        writer2.close();
        // ------------finish getting the comments.

        return listLinkedComments;
    }
}
