  public void filterAllConditionSimilarity() throws IOException {
        String multipleRepairs = "/home/wy/Desktop/test-4-12-searchSpace/feedback/multipleValueSimple.tsv";//
        ArrayList<String[]> manyRepairs = tsvr(multipleRepairs);

        BufferedWriter out = new BufferedWriter(new FileWriter(
                "/home/wy/Desktop/test-4-12-searchSpace/feedback/multipleValueSimple-AllSimilarity4.tsv"));//

        BufferedWriter writer = new BufferedWriter(new FileWriter(
                "/home/wy/Desktop/test-4-12-searchSpace/feedback/multipleValueSimple-filterCorrection4.tsv"));//
        
        for (String[] ke : manyRepairs) {
            String error = ke[0];
            List<String> list = Arrays.asList(ke);
            System.out.println(list);
            ArrayList<String> listCopy = new ArrayList<>(list);
            listCopy.remove(error);
            HashMap<String, double[]> correctionAllWeight = new HashMap<>();
            for (String item : listCopy) {
                HashMap<String, double[]> correctionWeight =
                        new SimilarityAlgorithms().externalSimilarity(error, item);
                correctionAllWeight.putAll(correctionWeight);
            }
            out.write(ke[0] + "\t");
            for (String key : correctionAllWeight.keySet()) {
                out.write(key + "\t");
                double[] rates = correctionAllWeight.get(key);
                for (double ratio : rates)
                    out.write(ratio + "\t");
            }
            out.write("\n");
            System.out.println("test:" + correctionAllWeight + "\n");
            String[] fiterAllCorrection = new validation().compareSimilarity(correctionAllWeight);
            writer.write(ke[0] + "\t");
            for (String corre : fiterAllCorrection)
                writer.write(corre + "\t");

            writer.write("\n");
        }
        writer.close();
        out.close();
    }


    public LinkedHashMap<String, double[]> compareAllWordSimilarityMeasure(String repair, String errorEntity) {
        //single correction compare errors to find the suitable measure
        LinkedHashMap<String, double[]> targets = new LinkedHashMap<>();
        HashMap<String, Double> orders = new HashMap<>();

        String[] id2 = errorEntity.split("/");
        String base = id2[id2.length - 1];
        //    for (String ke : results) {
        String[] kSplits = repair.split("/");
        String compare = kSplits[kSplits.length - 1];
        double[] similarityRatio = new SimilarityAlgorithms().getWordSimilarityRatio(base, compare);
        for (double rate : similarityRatio)
            orders.put(repair, rate);
        targets.put(repair, similarityRatio);
        return targets;

    }

        //  String correctPropertyQuery = "select distinct ?a where{<" + error + "> " + Property.relationPropertyInWikidata + " ?a.}";//wdt:P17
            //   System.out.println("wikidata--correctQuery1:" + correctPropertyQuery);
            //  ArrayList<String> finalResults = new ArrayList<>();
            //  ArrayList<String[]> propertyResults = new wikidataSparql().getCommands(correctPropertyQuery);
//            for (int i = 1; i < propertyResults.size(); i++) {
//                String[] ke = propertyResults.get(i);
//                finalResults.addAll(Arrays.asList(ke));
//            }
//            String instanceQuery = "select distinct ?a where{<" + error + "> p:P31 ?A. ?A pq:P642 ?a. }";
//            //    "  ?A ps:P31 wd:Q231002. " +// property:nationality of
//            System.out.println("wikidata--correctQuery2:" + instanceQuery);
//            ArrayList<String[]> resultsOfInstance = new wikidataSparql().getCommands(instanceQuery);
//            for (int i = 1; i < resultsOfInstance.size(); i++) {
//                String[] ke = resultsOfInstance.get(i);
//                Collections.addAll(finalResults, ke);
//            }
//            if (!finalResults.isEmpty()) {
//                if (finalResults.size() > 1)
//                    correction = new CorrectionInDBPedia().compareSimilarity2(finalResults, errorEntity);
//                else
//                    correction = finalResults.get(0);
//                String corresondQuery = "select ?a where {?a owl:sameAs " + new wikidataSparql().replaceQueryHeader(oneResult) + " }";
//                System.out.println("reverse-query-dbpedia:" + corresondQuery + "\n");
//                HashSet<String> corresondCorrection = new Sparql().getSingleVariable(corresondQuery);
//              //  ArrayList<String> corresondCorrection = new Sparql().getSingleResultsFromQuery(corresondQuery);
//                if (corresondCorrection != null) {
//                    for(String key: corresondCorrection) {
//                        correction = key;
//                        break;
//                    }
//                }
            //           }
