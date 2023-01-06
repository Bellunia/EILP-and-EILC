package other_algorithms.similarityMeasure;

/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import gilp.knowledgeClean.RuleLearnerHelper;
import gilp.sparql.Sparql;
import info.debatty.java.stringsimilarity.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Thibault Debatty
 */
public class SimilarityAlgorithms {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String path = "./data/estimation/error-old/roughCorrections.tsv";
        // new Examples().readRoughCorrection(path);
        //  readNumbers();
       System.out.println(similarityHeads());
    }

    public static HashMap<String, Integer> importNumbers(String path) {
        String path2 = "/home/wy/Desktop/counts-positive-numbers.txt";
        String path3 = "/home/wy/Desktop/negative-objects-numbers.txt";
        HashMap<String, Integer> counts = new HashMap<>();
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(path))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t");
                counts.put(lineItems[0], Integer.valueOf(lineItems[1]));
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return counts;
    }


    public static void readNumbers() throws IOException {
        String path = "/home/wy/Desktop/counts-positive-numbers.txt";
        String path3 = "/home/wy/Desktop/negative-objects-numbers.txt";
        String path2 = "/home/wy/Desktop/test-original-entity.txt";
        ArrayList<String> Data = new ArrayList<>();
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(path2))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                Data.add("http://dbpedia.org/resource/" + line);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        HashMap<String, Integer> counts = importNumbers(path3);

        HashMap<String, Integer> counts1 = importNumbers(path);
        counts.putAll(counts1);

        BufferedWriter out = new BufferedWriter(new FileWriter("./data/estimation/error-old/rough-all-similarity-cosine-numbers.tsv"));
        for (String items : Data)
            out.write(items + "\t" + counts.get(items) + "\n");
        out.close();
    }


    public void readRoughCorrection(String path) throws IOException {
        //HashSet<ArrayList<String>>
        HashSet<ArrayList<String>> Data = new HashSet<>();
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(path))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t");
                ArrayList<String> items = new ArrayList<>();
                for (String it : lineItems) {
                    if (it != null)
                        items.add(it);
                }
                Data.add(items);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        BufferedWriter out = new BufferedWriter(new FileWriter("./data/estimation/error-old/rough-all-similarity-cosine.tsv"));

        for (ArrayList<String> items : Data) {
            String original = items.get(0);
            items.remove(original);
            out.write(original + "\t");
            HashMap<String, double[]> correction = compareSimilarity(items, original);
            //  String firstKey = correction.keySet().iterator().next();
            //  double value = correction.get(firstKey);
            for (String key : correction.keySet()) {
                out.write(key + "\t");
                for (double prob : correction.get(key))
                    out.write(prob + ",");
                out.write("\t");
            }
            out.write("\n");

        }
        out.close();
    }

    public LinkedHashMap<String, double[]> compareSimilarity(ArrayList<String> results, String errorEntity) {
        LinkedHashMap<String, double[]> targets = new LinkedHashMap<>();
        HashMap<String, Double> orders = new HashMap<>();

        String[] id2 = errorEntity.split("/");
        String base = id2[id2.length - 1];
        for (String ke : results) {
            String[] kSplits = ke.split("/");
            String compare = kSplits[kSplits.length - 1];
            double[] similarityRatio = getSimilarityRatio(base, compare);
            orders.put(ke, similarityRatio[4]);//0 is edit distance 4: Cosine similarity between two chars
            targets.put(ke, similarityRatio);
        }
        //        String firstKey = reverseResults.keySet().iterator().next();
//        double value = reverseResults.get(firstKey);
        return newOrder(targets, new RuleLearnerHelper().reverseOrderByValue(orders));

    }

    public LinkedHashMap<String, double[]> compareHarmonicRatios(ArrayList<String> results, String errorEntity) {
        LinkedHashMap<String, double[]> targets = new LinkedHashMap<>();
        HashMap<String, Double> orders = new HashMap<>();

        String[] error = errorEntity.split("/");
        String base = error[error.length - 1];
        for (String ke : results) {
            String[] kSplits = ke.split("/");
            String compare = kSplits[kSplits.length - 1];
            double[] similarityRatio = getCompareRatio(base, compare);
            orders.put(ke, similarityRatio[4]);//0 is Cosine 4: edit distance
            targets.put(ke, similarityRatio);
        }


        return newOrder(targets, new RuleLearnerHelper().reverseOrderByValue(orders));

    }

    private HashSet<String> checkWikiLink(String entity) {
        String path = "./data/RuleCorrections/dbpedia-2020-wikiLink/" + entity + ".txt";
        Path file = new File(path).toPath();
        HashSet<String> allLinks = new HashSet<>();
        boolean exists = Files.exists(file);
        if (exists) {
            allLinks = RuleLearnerHelper.readTypes(path);
        } else {

            String newEntity = "select distinct ?b where{  <" +
                    entity.replace("dbr:", "http://dbpedia.org/resource/") +
                    "> <http://dbpedia.org/ontology/wikiPageWikiLink> ?b .}";
            System.out.println(newEntity);
            allLinks = new Sparql().getSingleVariable(newEntity);


            try {
                Writer writer1 = new OutputStreamWriter(
                        new FileOutputStream(path), Charset.forName("UTF-8"));
                for (String instance : allLinks)
                    writer1.write(instance + "\n");

                writer1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return allLinks;

    }

    public HashMap<String, double[]> crossNewSimilarity(String entity, HashMap<String, Integer> roughCorrection) {
//@2021.3.31
        HashSet<String> errorLinks = checkWikiLink(entity);

        int original = errorLinks.size();

        HashMap<String, Double> outSim = new HashMap<>();

        String[] error = entity.split("/");
        String base = error[error.length - 1];

        HashMap<String, double[]> correctionWeight = new HashMap<>();

        for (String repair : roughCorrection.keySet()) {
            int num = roughCorrection.get(repair);
            if (!outSim.containsKey(repair)) {
                HashSet<String> newRepair = checkWikiLink(repair);
                newRepair.retainAll(errorLinks);
                int others = newRepair.size();
                double precision = (double) others / original;
                outSim.put(repair, precision);
            }

            String[] kSplits = repair.split("/");
            String compare = kSplits[kSplits.length - 1];
            double simOut = outSim.get(repair);

            double[] similarityRatio = getCompareRatio(base, compare);
            int len = similarityRatio.length;
            double[] newSimilarity = new double[4 * len];
            int i = 0;
            for (double similarity : similarityRatio) {//11 types
                double accuracy = similarity * 2 * simOut / (similarity + simOut);
                double accuracy1 = similarity + simOut;
                double accuracy2 = similarity * num + simOut;
                double accuracy3 = similarity + simOut * num;

                newSimilarity[i] = accuracy;
                newSimilarity[i + len] = accuracy1;
                newSimilarity[i + len * 2] = accuracy2;
                newSimilarity[i + len * 3] = accuracy3;
                i++;
            }
            correctionWeight.put(repair, newSimilarity);
        }
        writeSingleEntityAnalysis(entity,correctionWeight);
        return correctionWeight;
    }

    private void writeSingleEntityAnalysis(String entity, HashMap<String, double[]> correctionWeight)  {

        try {
            Writer   writer2 = new OutputStreamWriter(
                    new FileOutputStream("./data/RuleCorrections/dbpedia-2020/singleEntityAnalysis/" + entity + ".txt"),
                    Charset.forName("UTF-8"));

        writer2.write(similarityHeads());
            writer2.write(entity + "\n");
        for (String correction : correctionWeight.keySet()) {

            writer2.write(correction + "\t");
            double[] values = correctionWeight.get(correction);
            int i = 0;
            for (double val : values) {
                writer2.write(val + "\t");
                i++;
                if (i % 11 == 0)
                    writer2.write("\t\t\t");
            }
            writer2.write("\n");
        }
        writer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String similarityHeads() {
        return "Cosine(2)\tJaccard(2)\tJaroWinkler\t SorensenDice(2)\t" +
                "Levenshtein\t Normalized Levenshtein\t" + "Damerau\tOptimalStringAlignment\t" +
                "LongestCommonSubsequence\t	NGram(2)\t" + "QGram(2)\n";
        //Levenshtein ( the same resluts: OptimalStringAlignment)
    }

    private double[] getCompareRatio(String base, String compare) {
        //algorithm:11--@2021.3.31

        int max = Math.max(base.length(), compare.length());

        double[] similarity = new double[11];

        Cosine cos = new Cosine(2);  // Cosine
        similarity[0] = cos.similarity(base, compare);
        Jaccard j2 = new Jaccard(2);
        similarity[1] = j2.similarity(base, compare);
        JaroWinkler jw = new JaroWinkler(); // Jaro-Winkler
        similarity[2] = jw.similarity(base, compare);
       SorensenDice sd = new SorensenDice(2);    // Sorensen-Dice
        similarity[3] = sd.similarity(base, compare);
        //----------------distance-------
        Levenshtein levenshtein = new Levenshtein();// Levenshtein
        double editDistance = levenshtein.distance(base, compare);
        similarity[4] = 1 - editDistance / max;
        NormalizedLevenshtein l = new NormalizedLevenshtein();   // Normalized Levenshtein
        similarity[5] = 1 - l.distance(base, compare) / max;
        Damerau damerau = new Damerau();  // Damerau
        similarity[6] = 1 - damerau.distance(base, compare) / max;
       OptimalStringAlignment osa = new OptimalStringAlignment();       // Optimal String Alignment
        similarity[7] = 1 - osa.distance(base, compare) / max;
        LongestCommonSubsequence lcs = new LongestCommonSubsequence(); // Longest Common Subsequence
        similarity[8] = 1 - lcs.distance(base, compare) / max;
        NGram twogram = new NGram(2);   // NGram
        similarity[9] = 1 - twogram.distance(base, compare) / max;
        QGram dig = new QGram(2);      // QGram
        similarity[10] = 1 - dig.distance(base, compare) / max;

        return similarity;
    }


    public HashMap<String, double[]> filterCorrection(String error, HashMap<String, Integer> repairs) {
        //@--2021.3.31
        HashMap<String, HashMap<String, Double>> filterCorrection = new HashMap<>();

        ArrayList<String> elements = new ArrayList<>(repairs.keySet());
        HashMap<String, double[]> correctionWeight = crossNewSimilarity(error, repairs);//--new

        // crossSimilarity(error, elements);--old
//        HashMap<String, Double> firstFilter=judgeSimilarity( correctionWeight, 4);
//        filterCorrection.put(error, firstFilter);
        return correctionWeight;
    }

    public HashMap<String, double[]> judgeSimilarity(HashMap<String, double[]> correctionWeight, int algorithmType) {

        HashMap<String, Double> filterPrecision = new HashMap<>();

        HashMap<String, double[]> firstFilter = new HashMap<>();

        for (String key : correctionWeight.keySet()) {
            double[] prob = correctionWeight.get(key);
            filterPrecision.put(key, prob[algorithmType]);
        }

        final Map<String, Double> correctionWeightOrder = new RuleLearnerHelper().reverseOrderByValue(filterPrecision);
        String firstKey = correctionWeightOrder.keySet().iterator().next();
        //  double value = correctionWeightOrder.get(firstKey);
        firstFilter.put(firstKey, correctionWeight.get(firstKey));
        return firstFilter;
    }

    public HashMap<String, double[]> crossSimilarity(String entity, ArrayList<String> roughCorrection) {

        HashMap<String, Integer> hm = countFrequencies(roughCorrection);
        ArrayList<String> keys = new ArrayList<>(hm.keySet());
        HashMap<String, double[]> correction = new SimilarityAlgorithms().compareSimilarity(keys, entity);
        HashMap<String, double[]> correctionWeight = new HashMap<String, double[]>();
        for (String sim : correction.keySet()) {
            double[] similarity = correction.get(sim);
            double[] newSimlarity = new double[similarity.length];
            for (int i = 0; i < similarity.length; i++) {
                newSimlarity[i] = similarity[i] * hm.get(sim);

            }

            correctionWeight.put(sim, newSimlarity);
        }

        return correctionWeight;
    }




    public HashMap<String, double[]> crossNewSimilarity2(String entity, HashMap<String, Integer> roughCorrection) {
//@2021.3.31
        HashSet<String> errorLinks= checkWikiLink( entity);

        int original = errorLinks.size();

        HashMap<String, Double> outSim = new HashMap<>();

        for (String repair : roughCorrection.keySet()) {

            if (!outSim.containsKey(repair)) {
                HashSet<String> newRepair= checkWikiLink(repair);
                newRepair.retainAll(errorLinks);
                int others = newRepair.size();
                double precision =(double) others / original;
                outSim.put(repair, precision);
            }
        }

        ArrayList<String> keys = new ArrayList<>(roughCorrection.keySet());
        // HashMap<String, Double> correction = new SimilarityAlgorithms().compareInnerSimilarity(keys, entity);
        HashMap<String, double[]> correction = new SimilarityAlgorithms().compareSimilarity(keys, entity);

        HashMap<String, double[]> correctionWeight = new HashMap<>();
        for (String sim : correction.keySet()) {
            double simOut = outSim.get(sim);
            double similarity = correction.get(sim)[4];
            double accuracy = similarity * 2 * simOut / (similarity + simOut);
            double accuracy1 = similarity + simOut;
            double accuracy2 = similarity * roughCorrection.get(sim) + simOut;
            double accuracy3 = similarity + simOut * roughCorrection.get(sim);
            double[] newSimilarity = new double[4];

            newSimilarity[0] = accuracy;
            newSimilarity[1] = accuracy1;
            newSimilarity[2] = accuracy2;
            newSimilarity[3] = accuracy3;
            System.out.println("repair\t" + sim +
                    "\taccuracy\t " + accuracy +
                    "\t accuracy1:  \t" + accuracy1
                    + "\t accuracy2:  \t" + accuracy2
                    + "\t accuracy3 \t" + accuracy3);
            correctionWeight.put(sim, newSimilarity);
        }

        return correctionWeight;
    }

    public HashMap<String, double[]> crossNewSimilarity_old(String entity, HashMap<String, Integer> roughCorrection) {
//@2021.3.23--in local database using the property:<http://dbpedia.org/ontology/wikiPageExternalLink>

        String newEntity = "select (count (distinct ?b) as ?x) where{  <" +
                entity.replace("dbr:", "http://dbpedia.org/resource/") +
                "> <http://dbpedia.org/ontology/wikiPageWikiLink> ?b .}";
        Double original = new Sparql().countNumbers(newEntity);
        HashMap<String, Double> outSim = new HashMap<>();
        int i = 0;
        for (String repair : roughCorrection.keySet()) {
            i++;
            if (!outSim.containsKey(repair)) {
                StringBuilder newRepair = new StringBuilder();
                newRepair.append("select  (count ( distinct ?c) as ?x) where{   <")
                        .append(repair.replace("dbr:", "http://dbpedia.org/resource/"))
                        .append("> <http://dbpedia.org/ontology/wikiPageWikiLink> ?b . <")
                        .append(entity.replace("dbr:", "http://dbpedia.org/resource/"))
                        .append(">  <http://dbpedia.org/ontology/wikiPageWikiLink> ?c .   filter(?b=?c) }");

                Double others = new Sparql().countNumbers(newRepair.toString());
                double precision = others / original;
                outSim.put(repair, precision);
            }
        }

        //  HashMap<String, Integer> hm = countFrequencies(roughCorrection);
        ArrayList<String> keys = new ArrayList<>(roughCorrection.keySet());
        // HashMap<String, Double> correction = new SimilarityAlgorithms().compareInnerSimilarity(keys, entity);
        HashMap<String, double[]> correction = new SimilarityAlgorithms().compareSimilarity(keys, entity);

        HashMap<String, double[]> correctionWeight = new HashMap<>();
        for (String sim : correction.keySet()) {
            double simOut = outSim.get(sim);
            double similarity = correction.get(sim)[4];
            double accuracy = similarity * 2 * simOut / (similarity + simOut);
            double accuracy1 = similarity + simOut;
            double accuracy2 = similarity * roughCorrection.get(sim) + simOut;
            double accuracy3 = similarity + simOut * roughCorrection.get(sim);
            double[] newSimilarity = new double[4];

            newSimilarity[0] = accuracy;
            newSimilarity[1] = accuracy1;
            newSimilarity[2] = accuracy2;
            newSimilarity[3] = accuracy3;
            System.out.println("repair\t" + sim +
                    "\taccuracy\t " + accuracy +
                    "\t accuracy1:  \t" + accuracy1
                    + "\t accuracy2:  \t" + accuracy2
                    + "\t accuracy3 \t" + accuracy3);
            correctionWeight.put(sim, newSimilarity);
        }

        return correctionWeight;
    }

    public HashMap<String, Integer> countFrequencies(ArrayList<String> list) {
        HashMap<String, Integer> hm = new HashMap<>();
        for (String i : list) {
            Integer j = hm.get(i);
            hm.put(i.replace("\"", ""), (j == null) ? 1 : j + 1);
        }
        return hm;
    }

    private LinkedHashMap<String, double[]> newOrder(HashMap<String, double[]> targets, HashMap<String, Double> orders) {
        LinkedHashMap<String, double[]> newTargets = new LinkedHashMap<>();
        for (String key : orders.keySet())
            newTargets.put(key, targets.get(key));
        return newTargets;
    }


    private double[] getSimilarityRatio(String base, String compare) {
        String head = "Levenshtein	Jaccard(2)	JaroWinkler	Cosine(3)	Cosine(2)	Damerau	OptimalStringAlignment	" +
                "LongestCommonSubsequence	NGram(2)	NGram(4)	" +
                "NormalizedLevenshtein	QGram(2)	SorensenDice(2)	WeightedLevenshtein";
        double[] similarity = new double[14];
        // Levenshtein
        Levenshtein levenshtein = new Levenshtein();

        double editDistance = levenshtein.distance(base, compare);
        similarity[0] = 1 - editDistance / Math.max(base.length(), compare.length());

        // Jaccard index
        Jaccard j2 = new Jaccard(2);
        // AB BC CD DE DF
        // 1  1  1  1  0
        // 1  1  1  0  1
        // => 3 / 5 = 0.6
        //  System.out.println(j2.similarity("ABCDE", "ABCDF"));
        similarity[1] = j2.similarity(base, compare);

        // Jaro-Winkler
        JaroWinkler jw = new JaroWinkler();
        similarity[2] = jw.similarity(base, compare);

        // Cosine
        Cosine cos = new Cosine(3);
        // ABC BCE
        // 1  0
        // 1  1
        // angle = 45°
        // => similarity = .71 //   System.out.println(cos.similarity("ABC", "ABCE"));
        similarity[3] = cos.similarity(base, compare);

        cos = new Cosine(2);
        // AB BA
        // 2  1
        // 1  1
        // similarity = .95//  System.out.println(cos.similarity("ABAB", "BAB"));
        similarity[4] = cos.similarity(base, compare);

        // Damerau
        Damerau damerau = new Damerau();
        similarity[5] = damerau.distance(base, compare);

        // Optimal String Alignment
        OptimalStringAlignment osa = new OptimalStringAlignment();
        similarity[6] = osa.distance(base, compare);

        // Longest Common Subsequence
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        similarity[7] = lcs.distance(base, compare);

        // NGram
        NGram twogram = new NGram(2);
        similarity[8] = twogram.distance(base, compare);

        NGram ngram = new NGram(4);
        similarity[9] = ngram.distance(base, compare);

        // Normalized Levenshtein
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        similarity[10] = l.distance(base, compare);

        // QGram
        QGram dig = new QGram(2);
        // AB BC CD CE
        // 1  1  1  0
        // 1  1  0  1
        // Total: 2
        similarity[11] = dig.distance(base, compare);

        // Sorensen-Dice
        SorensenDice sd = new SorensenDice(2);
        // AB BC CD DE DF FG
        // 1  1  1  1  0  0
        // 1  1  1  0  1  1
        // => 2 x 3 / (4 + 5) = 6/9 = 0.6666
        similarity[12] = sd.similarity(base, compare);

        // Weighted Levenshtein
        WeightedLevenshtein wl = new WeightedLevenshtein(
                new CharacterSubstitutionInterface() {
                    public double cost(char c1, char c2) {

                        // The cost for substituting 't' and 'r' is considered
                        // smaller as these 2 are located next to each other
                        // on a keyboard
                        if (c1 == 't' && c2 == 'r') {
                            return 0.5;
                        }

                        // For most cases, the cost of substituting 2 characters
                        // is 1.0
                        return 1.0;
                    }
                });
        similarity[13] = wl.distance(base, compare);


        // K-Shingling
//        s1 = "my string,  \n  my song";
//        s2 = "another string, from a song";
        //       Cosine cosine = new Cosine(4);
//        System.out.println(cosine.getProfile(s1));
//        System.out.println(cosine.getProfile(s2));
//
//        cosine = new Cosine(2);
//        System.out.println(cosine.getProfile("ABCAB"));


        return similarity;
    }


}

