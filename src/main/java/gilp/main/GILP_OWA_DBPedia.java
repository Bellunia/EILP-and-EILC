package gilp.main;


import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.GILLearn_correction.*;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.rules.Rule;
import gilp.sparql.Sparql;
import gilp.utils.KVPair;
import javatools.parsers.NumberFormatter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;





public class GILP_OWA_DBPedia {

    public static KVPair<HashSet<String>, HashSet<String>> fileToTriples(int numbers) throws Exception {

        HashSet<String> positive = new HashSet<>();
        HashSet<String> negative = new HashSet<>();

        String path = "./data/searchSpace/feedback/feedback-" + numbers + ".tsv";

        Scanner scanner = new Scanner(path);
        File file = new File(scanner.nextLine());
        Scanner input = new Scanner(file);

        while (input.hasNextLine()) {
            String line = input.nextLine();

            StringTokenizer stringTokenizer = new StringTokenizer(line, "\t");
            String object = stringTokenizer.nextToken();// extract the object

            String value = stringTokenizer.nextToken();
            int i = Integer.parseInt(value);

            if (i > 0)
                positive.add(object);
            else
                negative.add(object);

        }
        input.close();
        scanner.close();
        return new KVPair<>(positive, negative);
    }

    public static void writeOutFeedbacks(HashSet<Triple> positiveTriples, Boolean decision) throws IOException {
        String path = "./data/searchSpace/localFalsePositiveFeedback-" + decision + ".txt";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));

        try {
            for (Triple t : positiveTriples)
                writer.write(t + "\n");

            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }

    public KVPair<HashSet<Triple>, HashSet<Triple>> initialLocalFeedback(int number) throws IOException {
//random feedback
        HashSet<Triple> positiveTriple =
                new FilterPediaTriples().filterPositiveTriples( number);

        String filterPositiveTripleQuery = "select distinct ?subject ?object  where{ ?subject <"
                + Property.PREDICATE_NAME + "> ?object. ?subject a <" + Property.DOMAIN + ">."
                + "?object a <" + Property.RANGE + ">.}  ";
        HashSet<Triple> positiveTriples = new HashSet<>();

        //  new RDF3XEngine().getDistinctEntity(query);
        HashSet<String[]> getAll = new RDF3XEngine().getMultipleElements(filterPositiveTripleQuery, 2);
        for (String[] elements : getAll) {
            Triple tri = new Triple(elements[0], Property.PREDICATE_NAME, elements[1]);
            positiveTriples.add(tri);
        }
        writeOutFeedbacks(positiveTriples, true);


        ArrayList<HashMap<String, String>> positiveSubjectOBject = new Sparql()
                .getResultsFromQuery(filterPositiveTripleQuery);

        HashSet<Triple> negativeTriple =
                new FilterPediaTriples().filterOtherNegativeTriples(number);
//---------------------conflict feedback
//        HashSet<AnnotatedTriple> feedbackSingleValue = new FilterPediaTriples().getRandomFeedbackSingleValue(predicate, number);
//        HashSet<AnnotatedTriple> feedbackDistinctValue= new FilterPediaTriples().getRandomFeedbackDistinctValue( predicate,number);
//        HashSet<Triple> positiveTriple = new RulesOthers().getTriplesByLabels(feedbackSingleValue, 1);
//        HashSet<Triple> negativeTriple = new RulesOthers().getTriplesByLabels(feedbackSingleValue, 0);
        //-----------------------------
        return new KVPair<>(positiveTriple, negativeTriple);


    }

    public static HashSet<Triple> fileToTriples(String pathToFBfile) throws Exception {

        HashSet<Triple> comments = new HashSet<Triple>();

        Scanner scanner = new Scanner(pathToFBfile);
        File file = new File(scanner.nextLine());
        Scanner input = new Scanner(file);

        while (input.hasNextLine()) {
            String line = input.nextLine();

            StringTokenizer stringTokenizer = new StringTokenizer(line, "\t");
            String subject, predicate, object;
            subject = stringTokenizer.nextToken();// extract the subject
            predicate = stringTokenizer.nextToken();// extract the predicate
            object = stringTokenizer.nextToken();// extract the object


            Triple triple = new Triple(subject, predicate, object);

            comments.add(triple);// generate a set of triples and comments
        }
        input.close();
        scanner.close();
        return comments;
    }

    private static <E> E getRandomElement(Set<? extends E> set) {

        /*
         * Generate a random number using nextInt
         * method of the Random class.
         */
        Random random = new Random();

        //this will generate a random number between 0 and HashSet.size - 1
        int randomNumber = random.nextInt(set.size());

        //get an iterator
        Iterator<? extends E> iterator = set.iterator();

        int currentIndex = 0;
        E randomElement = null;

        //iterate the HashSet
        while (iterator.hasNext()) {

            randomElement = iterator.next();

            //if current index is equal to random number
            if (currentIndex == randomNumber)
                return randomElement;

            //increase the current index
            currentIndex++;
        }

        return randomElement;
    }

    public static HashSet<Triple> filterRandomString(int numbers, HashSet<Triple> filterTriples) {
        int s = filterTriples.size();

        HashSet<Triple> comments = new HashSet<Triple>();

        while (comments.size() < Math.min(numbers, s)) {
            int idx = (int) Math.round(Math.random() * (s - 1));
            Triple cmt = getRandomElement(filterTriples);
            comments.add(cmt);
        }
        return comments;
    }


    public static ArrayList<Rule> readAmieRule(String test2) throws IOException {

        BufferedReader TSVReader = new BufferedReader(new FileReader(test2));
        String line = TSVReader.readLine();
        ArrayList<Rule> Data = new ArrayList<>();
        int i = 0;
        try {
            while (line != null) {

                String[] lineItems = line.split("\t");
                String rule = lineItems[0];
                // Rule rdfRule =  new RulesOthers().transformAmieRule( rule);
                Rule rdfRule = new RulesOthers().transformAmieRule2(rule);


                Data.add(rdfRule);

                //   System.out.println(i++ + "\t "+rdfRule+"\n");

                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }

    public static HashMap<String, Double> compareAmieRule(String test2) throws IOException {

        BufferedReader TSVReader = new BufferedReader(new FileReader(test2));
        String line = TSVReader.readLine();
        HashMap<String, Double> Data = new HashMap<String, Double>();
        int i = 0;
        try {
            while (line != null) {

                String[] lineItems = line.split("\t");
                String rule = lineItems[0];
                Data.put(rule, Double.parseDouble(lineItems[1]));

                //  System.out.println(i++ + "\t "+rdfRule+"\n");

                line = TSVReader.readLine();
            }
            TSVReader.close();
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }


    public void filterObjectsConditions(int numbers) throws Exception {

        /**
         * filter the different objects, here, the objects is true, the triple, the extracted relation is false, so we need to consider the subjects.
         */
        HashSet<String> truePositiveObjects = RuleLearnerHelper.readTypes("./data/dbpedia-type/tp-objects.txt");
        HashSet<String> trueNegativeObjects = RuleLearnerHelper.readTypes("./data/dbpedia-type/errors/all-errors-old.txt");
        HashSet<String> filterPositiveRandom = new RuleLearnerHelper().filterRandomString(numbers, truePositiveObjects);
        HashSet<String> filterNegativeRandom = new RuleLearnerHelper().filterRandomString(numbers, trueNegativeObjects);
        KVPair<HashSet<String>, HashSet<String>> initialTriples = new FilterPediaTriples().initialRandomObjects(numbers);

        KVPair<HashSet<String>, HashSet<String>> readFeedbacks = fileToTriples(numbers);
        HashSet<String> positiveObjects = readFeedbacks.getKey();
        HashSet<String> negativeObjects = readFeedbacks.getValue();
        System.out.println("positive:" + positiveObjects.size() + "   negative:" + negativeObjects.size());
        new getSearchSpace().filterObjectSearchSpace(positiveObjects, negativeObjects, numbers);
    }


    public void filterOnlineFeedbackConditions(int numbers) throws Exception {
        // in the dbpedia online, consider the wikipageRedirect objects, then to do the search space.
        KVPair<HashSet<Triple>, HashSet<Triple>> initialTriples = new FilterPediaTriples().initialFeedback(numbers);
        HashSet<Triple> positiveTriple = initialTriples.getKey();
        HashSet<Triple> negativeTriple = initialTriples.getValue();

        new getSearchSpace().filterSearchSpace(positiveTriple, negativeTriple, numbers);

    }

    public void filterLocalFeedbackConditions(int numbers) throws Exception {
        //the true positive: type:wikidata:Q6256
        //all positive:object type dbo:Country
        //negative: !dbo:Country->negative

        HashSet<Triple> allPositiveTriples = fileToTriples("./data/searchSpace/feedback/localFeedback-true.txt");

        HashSet<Triple> positiveTriple = filterRandomString(numbers, allPositiveTriples);

        HashSet<Triple> allNegativeTriples = fileToTriples("./data/searchSpace/feedback/localFeedback-false.txt");
        HashSet<Triple> negativeTriple = filterRandomString(numbers, allNegativeTriples);


        new getSearchSpace().filterSearchSpace(positiveTriple, negativeTriple, numbers);

    }

    public void redirect() throws Exception {
        //   HashSet<Triple> positiveTriples=fileToHashSet("/home/wy/gilp_learn/data/searchSpace/localFeedback-true.txt");
        HashSet<Triple> triples = fileToTriples("./data/searchSpace/feedback/localFalsePositiveFeedback-true.txt");
        //   triples.removeAll(positiveTriples);
        //  writeOutFeedbacks(triples,true);
        HashMap<String, String> matchEntity = new HashMap<>();
        //   HashSet<Triple> newtriples= new HashSet<>();
        for (Triple key : triples) {
            if (!matchEntity.containsKey(key.get_obj())) {
                String entity = new CorrectionInDBPedia().redictEntity(key.get_obj());
                matchEntity.put(key.get_obj(), entity);
            }
        }

        String path = "./data/searchSpace/feedback/localFalsePositiveFeedback-redirect.txt";
        Writer writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8"));

        try {
            for (String t : matchEntity.keySet())
                writer.write(t + "\t" + matchEntity.get(t) + "\n");

            writer.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }


    }


    public void filterFinalPositiveRules(int numbers) throws Exception {
        HashSet<Triple> allPositiveTriples = fileToTriples("./data/searchSpace/feedback/localFeedback-true.txt");
        HashSet<Triple> positiveTriple = filterRandomString(numbers, allPositiveTriples);
        HashSet<String> allPositiveSubjects = new HashSet<>();
        HashMap<String, Triple> compareTriples = new HashMap<>();
        for (Triple key : allPositiveTriples) {
            String sub = key.get_subject();
            allPositiveSubjects.add(sub);
            if (!compareTriples.containsKey(sub)) {
                compareTriples.put(sub, key);
            }
        }

        int iterationTimes = 0;

        while (true) {

            System.out.println("\n------- Iteration:" + iterationTimes++ + "-----------------\n");
            System.out.println("\n each iteration positiveTriple size :  " + positiveTriple.size());

            new getSearchSpace().
                    extendTriplesByLevel(positiveTriple, ILPLearnSettings.DEFAULT_LEVEL, true, iterationTimes);
            String pathPos ="./data/searchSpace/positiveSearchSpace" + iterationTimes + "-old.tsv";

            HashSet<Triple> positveExtendTriples = ILPLearnSettings.readTriples(pathPos);

            new ILPLearnSettings().writeOutSearchSpace(positveExtendTriples, true, iterationTimes);
            System.out.println("\n------- finish extending search space-----------------\n");
            ArrayList<Rule> positiveRules = new getRules().getAmieRules(true, iterationTimes);
//----------------------------------------
            HashSet<String> positivePrediction = filterSubjectsByType(positiveRules, allPositiveSubjects);

            System.out.println("\n------- finish filter rules-----------------\n");

            HashSet<Triple> newPositiveTriples = new HashSet<>();
            for (String key : positivePrediction) {
                Triple tri = compareTriples.get(key);
                if (!positiveTriple.contains(tri))
                    newPositiveTriples.add(tri);
            }

            if (newPositiveTriples.size() > 0) {
                positiveTriple.addAll(newPositiveTriples);
            } else {
                    try {
                        File file = new File("./data/gilpRules/positiveRules/finalPositiveRules.txt");
                        FileWriter fileReader = new FileWriter(file); // A stream that connects to the text file
                        BufferedWriter bufferedWriter = new BufferedWriter(fileReader); // Connect the FileWriter to the BufferedWriter
                        int j = 0;
                        for (Rule s : positiveRules) {
                            bufferedWriter.write(++j + "\t" + s  + "\n");
                        }
                        bufferedWriter.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                System.out.println("\n Last allPrediction: " + positiveTriple.size());
               break;
            }
        }

    }

    public void filterFinalNegativeRules(int numbers) throws Exception {
        HashSet<Triple> allPositiveTriples = fileToTriples("./data/searchSpace/feedback/localFeedback-false.txt");
        HashSet<Triple> positiveTriple = filterRandomString(numbers, allPositiveTriples);
        HashSet<String> allPositiveSubjects = new HashSet<>();
        HashMap<String, Triple> compareTriples = new HashMap<>();
        for (Triple key : allPositiveTriples) {
            String sub = key.get_subject();
            allPositiveSubjects.add(sub);
            if (!compareTriples.containsKey(sub)) {
                compareTriples.put(sub, key);
            }
        }

        int iterationTimes = 0;

        while (true) {

            System.out.println("\n------- Iteration:" + iterationTimes++ + "-----------------\n");
            System.out.println("\n each iteration positiveTriple size :  " + positiveTriple.size());

            new getSearchSpace().
                    extendTriplesByLevel(positiveTriple, ILPLearnSettings.DEFAULT_LEVEL, false, iterationTimes);
            String pathNeg = "./data/searchSpace/negativeSearchSpace" + iterationTimes + "-old.tsv";
            HashSet<Triple> positveExtendTriples = ILPLearnSettings.readTriples(pathNeg);

            new ILPLearnSettings().writeOutSearchSpace(positveExtendTriples, false, iterationTimes);
            System.out.println("\n------- finish extending search space-----------------\n");
            ArrayList<Rule> positiveRules = new getRules().getAmieRules(false, iterationTimes);
//----------------------------------------
            HashSet<String> positivePrediction = filterSubjectsByType(positiveRules, allPositiveSubjects);

            System.out.println("\n------- finish filter rules-----------------\n");

            HashSet<Triple> newPositiveTriples = new HashSet<>();
            for (String key : positivePrediction) {
                Triple tri = compareTriples.get(key);
                if (!positiveTriple.contains(tri))
                    newPositiveTriples.add(tri);
            }

            if (newPositiveTriples.size() > 0) {
                positiveTriple.addAll(newPositiveTriples);
            } else {
                try {
                    File file = new File("./data/gilpRules/negativeRules/finalNegativeRules.txt");
                    FileWriter fileReader = new FileWriter(file); // A stream that connects to the text file
                    BufferedWriter bufferedWriter = new BufferedWriter(fileReader); // Connect the FileWriter to the BufferedWriter
                    int j = 0;
                    for (Rule s : positiveRules) {
                        bufferedWriter.write(++j + "\t" + s  + "\n");
                    }
                    bufferedWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("\n Last allPrediction: " + positiveTriple.size());
                break;
            }
        }

    }

    private HashSet<String> filterSubjectsByType(ArrayList<Rule> positiveRules, HashSet<String> allPositiveSubjects)  {

        HashSet<String> ruleSubjectsAll = new HashSet<String>();

        for (Rule rule : positiveRules) {
            HashSet<String> predictions = new HashSet<>();
            if (Property.ruleLabel) {
                predictions = new RulesOthers().ruleToSubjects(rule);
                predictions.retainAll(allPositiveSubjects);
            } else {
                predictions = new RulesOthers().ruleToObjects(rule);
                //   predictions.retainAll(allPositiveObjects);
            }
            ruleSubjectsAll.addAll(predictions);
        }
        return ruleSubjectsAll;
    }


    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();
        int numbers=40;
        //   for(int i=numbers;i<101;) {
     //   new GILP_OWA_DBPedia().filterLocalFeedbackConditions(numbers);
    //   ArrayList<Rule> negativeRules = new getRules().getAmieRules(false, numbers);
     //   ArrayList<Rule> positiveRules = new getRules().getAmieRules(true, numbers);
     //   new RuleCorrection().correctTripleByRule(positiveRules, negativeRules);
        //  ArrayList<Rule> positiveRules1 = GILP_OWA_DBPedia.readAmieRule("./data/gilpRules/amieRules-" + i + "-true.tsv");
        //   ArrayList<Rule> negativeRules1 = GILP_OWA_DBPedia.readAmieRule("./data/gilpRules/amieRules-" + i + "-false.tsv");
        //     i=i+10;
        //  }

    //    int number=50;
    //    new GILP_OWA_DBPedia(). filterFinalNegativeRules(number);

   //     new getRules().filterIterativeRules(numbers);

        String query1= "select( count ( distinct ?b) as ?x ) where{   <http://dbpedia.org/resource/Wales>  <http://dbpedia.org/ontology/wikiPageWikiLink> ?b . " +
                "<http://dbpedia.org/resource/Welsh_people>  <http://dbpedia.org/ontology/wikiPageWikiLink> ?c .   filter(?b=?c) }";


String repair="dbr:Athens_County,_Ohio";
        String query2 = "select  (count ( distinct ?c) as ?x) where{   " + repair.replace("dbr:","<http://dbpedia.org/resource/")+">" + "  <http://dbpedia.org/ontology/wikiPageWikiLink> ?b . " +
                "<http://dbpedia.org/resource/Albanians>  <http://dbpedia.org/ontology/wikiPageWikiLink> ?c .   filter(?b=?c) }";
        System.out.println(query2);
     //   Double others = new Sparql().countNumbers(query1);

        String query = "select (count (distinct ?b) as ?x) where{  dbr:Pashtuns  <http://dbpedia.org/ontology/wikiPageWikiLink> ?b .} ";
        Double original = new Sparql().countNumbers(query);
        System.out.println("others  " + original);
        long miningTime = System.currentTimeMillis() - time;
        System.out.println("Mining done in " + NumberFormatter.formatMS(miningTime));


    }


}
