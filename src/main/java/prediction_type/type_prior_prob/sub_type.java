package prediction_type.type_prior_prob;

//import org.apache.commons.collections4.MultiMap;
//import org.apache.commons.collections4.MultiValuedMap;

//import org.apache.commons.collections4.MultiValuedMap;


import com.opencsv.CSVWriter;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class sub_type {


    private static MultivaluedMap<String, ArrayList<String>> ReadSubType(String file_name) throws IOException {

//        business/company_type	15
//        organization/organization	16
//        tv/tv_program	17

        //  String path = "./type/sub-type/data/TKRL-all-input-v2/type2id.txt";
        File f = new File(file_name);
        Map<Integer, ArrayList<String>> subTypes = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        MultivaluedMap<String, ArrayList<String>> multiValueTypes = new MultivaluedHashMap<>();

        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            String[] subs = split_data[0].split("/");

            ArrayList<String> splitTypes = new ArrayList<>(Arrays.asList(subs));
            multiValueTypes.add(subs[0], splitTypes);
            //   BufferedWriter  writer = new BufferedWriter(new FileWriter("./type/types-weights.txt"));
            //   HashMap<String, Double> weights= singleHierarchicalStructures(splitTypes);
//            writer.write(split_data[0]+"\t");
//            for (String split_datum : subs) {
//                String str = String.format("%.4f\t", weights.get(split_datum));
//                writer.write(str);
//            }
//                writer.write("\n");


//tv=[[tv, tv_series_episode], [tv, tv_actor],..................
        }
        //  writer.close();

        return multiValueTypes;

    }

    private static HashMap<String, ArrayList<Double>> ReadSubTypeWeights(String file_name) throws IOException {
//   tv/tv_character=[0.2689, 0.7311], education/academic=[0.2689, 0.7311],....
        //  String path = "./type/sub-type/data/TKRL-all-input-v2/types-weights.txt";
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        HashMap<String, ArrayList<Double>> weights = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.trim().split("\t");
            ArrayList<Double> splitTypes = new ArrayList<>();
            for (int i = 1; i < split_data.length; i++)
                splitTypes.add(Double.valueOf(split_data[i]));
            weights.put(split_data[0], splitTypes);
        }
        //    System.out.println(weights);
        return weights;

    }

    private static HashMap<String, Double> singleHierarchicalStructures(ArrayList<String> subTypes) {
        // one type in freebase---
        /*
        H 1 = /person/actor, H 2 =/person/award winner, and H 3 = /person.
        w_e^{H_i}(person) = 0.27,i = {1, 2} and w_e^{H_3}(person)= 1.
         */
        int levels = subTypes.size();
        //  System.out.println(levels);
        ArrayList<String> subWeights = new ArrayList<>();
        HashMap<String, Double> typeWeights = new HashMap<>();

        // System.out.println(subTypes);

        double sum = 0.0;
        for (int j = 0; j < levels; j++) {
            sum = sum + Math.exp(j);
        }

        for (int i = 1; i < levels + 1; i++) {

            //   System.out.println(sum);
            double weight = Math.exp(i - 1) / sum;
            //  System.out.println(weight);
            BigDecimal b = new BigDecimal(weight);
            weight = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();//四舍五入
            typeWeights.put(subTypes.get(i - 1), weight);
        }
//System.out.println("singel types"+subTypes+":::"+typeWeights);
        return typeWeights;
    }

    private static MultivaluedHashMap<String, HashMap<String, Double>> singleEntityHierarchicalStructures(HashMap<String, ArrayList<String>> entity2type) {
        //< entity ,<subtype, minweights>>  one type in freebase---
        /*
        H 1 = /person/actor, H 2 =/person/award winner, and H 3 = /person.
        w_e^{H_i}(person) = 0.27,i = {1, 2} and w_e^{H_3}(person)= 1.
         */
        MultivaluedHashMap<String, HashMap<String, Double>> entity2typeWeight = new MultivaluedHashMap<>();
        for (String entity : entity2type.keySet()) {
            HashMap<String, Double> subTypeWeights = new HashMap<>();
            MultivaluedHashMap<String, Double> newEntityMap = new MultivaluedHashMap<>();
            ArrayList<String> types = entity2type.get(entity);
            for (String ty : types) {//   language/human_language	common/topic
                String[] subs = ty.split("/");

                ArrayList<String> splitTypes = new ArrayList<>(Arrays.asList(subs));
                HashMap<String, Double> weights = singleHierarchicalStructures(splitTypes);
                for (String weight : weights.keySet())
                    newEntityMap.add(weight, weights.get(weight));
            }

            for (Map.Entry<String, List<Double>> entr : newEntityMap.entrySet()) {
                String subtype = entr.getKey();
                List<Double> values = entr.getValue();
                // double max = values.stream().max(Comparator.naturalOrder()).get();
                double min = values.stream().min(Comparator.naturalOrder()).get();
                subTypeWeights.put(subtype, min);
            }
            entity2typeWeight.add(entity, subTypeWeights);
        }
        return entity2typeWeight;

    }

    private static void globalMinWeightedSubTypes(MultivaluedMap<String, ArrayList<String>> multiValueTypes) {
        MultivaluedHashMap<String, HashMap<String, Double>> minGlobalWeights = new MultivaluedHashMap<>();
        for (Map.Entry<String, List<ArrayList<String>>> entry : multiValueTypes.entrySet()) {
            HashMap<String, Double> minWeightType = new HashMap<>();
            MultivaluedHashMap<String, Double> newMap = new MultivaluedHashMap<>();
            String domain = entry.getKey();
            // System.out.println("domain: " + domain);
            for (int i = 0; i < entry.getValue().size(); i++) {
                ArrayList<String> subTypes = entry.getValue().get(i);//[tv, tv_series_episode]
                HashMap<String, Double> weights = singleHierarchicalStructures(subTypes);
                for (Map.Entry<String, Double> entri : weights.entrySet()) {
                    newMap.add(entri.getKey(), entri.getValue());
                }
            }

            //  System.out.println("test---newMap" + newMap);
            for (Map.Entry<String, List<Double>> entr : newMap.entrySet()) {
                String subtype = entr.getKey();
                List<Double> values = entr.getValue();
                // double max = values.stream().max(Comparator.naturalOrder()).get();
                //    System.out.println("test---" + subtype + ": " + values);
                double min = values.stream().min(Comparator.naturalOrder()).get();
                minWeightType.put(subtype, min);
            }
            //  System.out.println("test-222222222222222222222:\n");
            minGlobalWeights.add(domain, minWeightType);
        }
        //   System.out.println("test-weights:\n" + minGlobalWeights);

    }

    public static void relationTypeWeight() throws IOException {
        //"./type/sub-type/data/TKRL-all-input-v2/train.txt"
        MultivaluedHashMap<String, String> relationHead = new MultivaluedHashMap<>();
        MultivaluedHashMap<String, String> relationTail = new MultivaluedHashMap<>();
        File f2 = new File("./type/sub-type/data/TKRL-all-input-v2/train.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f2), "UTF-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            String relation = split_data[2];
            relationHead.add(relation, head);
            relationTail.add(relation, tail);

        }

        File f1 = new File("./type/sub-type/data/TKRL-all-input-v2/entity2type.txt");

        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), "UTF-8"));
        String line1; ///m/0h407	language/human_language	common/topic
        HashMap<String, ArrayList<String>> entity2type = new HashMap<>();
        while ((line1 = reader1.readLine()) != null) {
            String[] split_data = line1.split("\t");
            String entity = split_data[0];
            ArrayList<String> splitTypes = new ArrayList<>(Arrays.asList(split_data).subList(1, split_data.length));
            entity2type.put(entity, splitTypes);
        }

        MultivaluedHashMap<String, HashMap<String, Double>> entity2typeWeight =
                singleEntityHierarchicalStructures(entity2type);

        HashMap<String, HashMap<String, Double>> allHeadTypeWeightsSum = new HashMap<>();
        //<relation,<subtype, minWeightSum>>
        HashMap<String, HashMap<String, Double>> allTailTypeWeightsSum = new HashMap<>();

        HashMap<String, Double> allTypeSumOfHeadRelation = new HashMap<>();
        //<relation,<subtype, minWeightSum>>
        HashMap<String, Double> allTypeSumOfTailRelation = new HashMap<>();

        //<relation,<subtype, minWeightSum>>

        //------------------head-type weight sum


        HashMap<String, HashSet<String>> headTypeSets = new HashMap<>();
        HashMap<String, HashSet<String>> tailTypeSets = new HashMap<>();

        for (Map.Entry<String, List<String>> entr : relationHead.entrySet()) {
            String relation = entr.getKey();
            MultivaluedHashMap<String, Double> newHeadSubTypeMap = new MultivaluedHashMap<>();//<subtypes, weights>
            List<String> entitiesHead = entr.getValue();
            for (String entity : entitiesHead) {
                List<HashMap<String, Double>> subTypesWeight = entity2typeWeight.get(entity);
                // allHeadTypeMap.put(relation,subTypesWeight);

                for (HashMap<String, Double> types : subTypesWeight) {
                    for (String ty : types.keySet())
                        newHeadSubTypeMap.add(ty, types.get(ty));


                }

            }
            HashMap<String, Double> headRelationTypeWeightSum = new HashMap<>();
            HashSet<String> subTypes = new HashSet<>();
            double headTypeScores = 0.0;
            for (Map.Entry<String, List<Double>> subTypesWeights : newHeadSubTypeMap.entrySet()) {
                List<Double> weightsValues = subTypesWeights.getValue();
                double sumWeights = weightsValues.stream().mapToDouble(f -> f).sum();
                String subType = subTypesWeights.getKey();
                headRelationTypeWeightSum.put(subType, sumWeights);
                subTypes.add(subType);
                headTypeScores = headTypeScores + sumWeights;
            }
            headTypeSets.put(relation, subTypes);
            allHeadTypeWeightsSum.put(relation, headRelationTypeWeightSum);

            allTypeSumOfHeadRelation.put(relation, headTypeScores);
        }

//------------------tail-type weight sum

        for (Map.Entry<String, List<String>> entr : relationTail.entrySet()) {

            String relation = entr.getKey();
            MultivaluedHashMap<String, Double> newTailTypeMap = new MultivaluedHashMap<>();
            List<String> entitiesTail = entr.getValue();
            for (String entity : entitiesTail) {
                List<HashMap<String, Double>> subTypesWeight = entity2typeWeight.get(entity);
                for (HashMap<String, Double> types : subTypesWeight) {
                    for (String ty : types.keySet())
                        newTailTypeMap.add(ty, types.get(ty));
                }
            }
            HashSet<String> subTypes = new HashSet<>();
            HashMap<String, Double> tailRelationTypeWeightSum = new HashMap<>();
            double tailTypeScores = 0.0;
            for (Map.Entry<String, List<Double>> subTypesWeights : newTailTypeMap.entrySet()) {
                List<Double> weightsValues = subTypesWeights.getValue();
                double sumWeights = weightsValues.stream().mapToDouble(f -> f).sum();
                String subType = subTypesWeights.getKey();
                tailRelationTypeWeightSum.put(subType, sumWeights);
                subTypes.add(subType);
                tailTypeScores = tailTypeScores + sumWeights;
            }
            tailTypeSets.put(relation, subTypes);
            allTailTypeWeightsSum.put(relation, tailRelationTypeWeightSum);
            allTypeSumOfTailRelation.put(relation, tailTypeScores);
        }
        // System.out.println("headTypeSets"+headTypeSets );
        //  System.out.println("tailTypeSets"+tailTypeSets );
        //   writeOut( allTypeSumOfHeadRelation,"./type/allTypeSumOfHeadRelation.txt");
        //   writeOut( allTypeSumOfTailRelation,"./type/allTypeSumOfTailRelation.txt");

//        Map<String, Integer> relation2id = new HashMap<>();
//        Read_id("./type/sub-type/data/TKRL-all-input-v2/relation2id.txt", relation2id);
//
//        Map<String, Integer> subtype2id = new HashMap<>();
//        Read_id("./type/sub-type/data/type-completion/complete/subtype2id.txt", subtype2id);


//        BufferedWriter  writer =
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/relationHeadGlobalScoresOfType.txt"));//allTypeSumOfHeadRelation
//        for (String rel : allTypeSumOfHeadRelation.keySet()) {
//            writer.write(rel+"\t"+allTypeSumOfHeadRelation.get(rel)+"\n");
//        }
//        writer.close();
//        BufferedWriter  writer1 =
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/relationTailGlobalScoresOfType.txt"));
//        for (String rel : allTypeSumOfTailRelation.keySet()) {
//            writer1.write(rel+"\t"+allTypeSumOfTailRelation.get(rel)+"\n");
//        }
//        writer1.close();

//        BufferedWriter  writer2 =
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/relationHeadIdGlobalScoresOfType.txt"));
//        for (String rel : allTypeSumOfHeadRelation.keySet()) {
//            writer2.write(relation2id.get(rel)+"\t"+allTypeSumOfHeadRelation.get(rel)+"\n");
//        }
//        writer2.close();
//
//        BufferedWriter  writer3 =
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/relationTailIdGlobalScoresOfType.txt"));
//        for (String rel : allTypeSumOfTailRelation.keySet()) {
//            writer3.write(relation2id.get(rel)+"\t"+allTypeSumOfTailRelation.get(rel)+"\n");
//        }
//        writer3.close();


//        BufferedWriter writer4 =
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/relationTailScoresOfSubType.txt"));
//        for (String rel : allTailTypeWeightsSum.keySet()) {
//            writer4.write(relation2id.get(rel) + "\t");
//            HashMap<String, Double> values = allTailTypeWeightsSum.get(rel);
//            for (String ke : values.keySet()) {
//                writer4.write(subtype2id.get(ke)+ '\t');
//              //  double weight = getValue(values.get(ke),4);
//                writer4.write(String.valueOf(values.get(ke))+ '\t');
//            }
//            writer4.write("\n");
//        }
//        writer4.close();


//            File f = new File("./type/sub-type/data/type-completion/relationTailScoresOfSubType.txt");
//           OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
//
//        for (String rel : allTailTypeWeightsSum.keySet()) {
//            writer.write(relation2id.get(rel) + "\t");
//            HashMap<String, Double> values = allTailTypeWeightsSum.get(rel);
//            int i=0;
//            for (String ke : values.keySet()) {
//                writer.write(subtype2id.get(ke)+"\t"+getValue(values.get(ke),4));
//                if (i < values.size() - 1)
//                    writer.write("\t");
//                i++;
//            }
//            writer.write("\n");
//            writer.flush();
//        }
//
//        writer.close();
//
//        BufferedWriter writer5 =
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/relationHeadScoresOfSubType.txt"));
//        for (String rel : allHeadTypeWeightsSum.keySet()) {
//            writer5.write(relation2id.get(rel) + "\t");
//            HashMap<String, Double> values = allHeadTypeWeightsSum.get(rel);
//            int i=0;
//            for (String ke : values.keySet()) {
//                writer5.write(subtype2id.get(ke)+"\t"+getValue(values.get(ke),4));
//                if (i < values.size() - 1)
//                    writer5.write("\t");
//                i++;
//            }
//            writer5.write("\n");
//            writer5.flush();
//        }
//        writer5.close();


//        BufferedWriter  writer6=
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/headTypeSetsId.txt"));
//        for (String rel : headTypeSets.keySet()) {
//            writer6.write(relation2id.get(rel)+"\t");
//            HashSet<String> values = headTypeSets.get(rel);
//            int i = 0;
//            for (String ke : values) {
//                writer6.write(subtype2id.get(ke)+"");
//                if (i < values.size() - 1)
//                    writer6.write("\t");
//                i++;
//            }
//            writer6.write("\n");
//        }
//        writer6.close();

//        BufferedWriter  writer7=
//                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/tailTypeSetsId.txt"));
//        for (String rel : tailTypeSets.keySet()) {
//            //  System.out.println(rel+"\t"+relation2id.get(rel));
//            writer7.write(relation2id.get(rel) + "\t");
//            HashSet<String> values = tailTypeSets.get(rel);
//            int i = 0;
//            for (String ke : values) {
//                writer7.write(subtype2id.get(ke)+"");
//                if (i < values.size() - 1)
//                    writer7.write("\t");
//                i++;
//            }
//            writer7.write("\n");
//        }
//
//        writer7.close();

        }

        private static double getValue ( double weight, int keepNumber){

            BigDecimal b = new BigDecimal(weight);
            weight = b.setScale(keepNumber, RoundingMode.HALF_UP).doubleValue();
            return weight;
        }

        private static void Read_id (String file_name, Map < String, Integer > data2id) throws IOException {

            File f = new File(file_name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split_data = line.split("\t");
                data2id.put(split_data[0], Integer.valueOf(split_data[1]));
            }
        }

        private static void writeOut (HashMap < String, Double > scores, String path) throws IOException {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));

            for (String sco : scores.keySet()) {
                writer.write(sco + "\t");
                String str = String.format("%.4f\t", scores.get(sco));
                writer.write(str + "\n");
            }

            writer.close();
        }



        private static void getSubType2id (String
        file_name, Map < String, Integer > data2id, Map < String, Integer > subType2id) throws IOException {

            File f = new File(file_name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split_data = line.split("\t");
                data2id.put(split_data[0], Integer.valueOf(split_data[1]));

                String[] subs = split_data[0].split("/");

                for (String sub : subs) {
                    if (!subType2id.containsKey(sub))
                        subType2id.put(sub, 0);
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/subtype2id.txt"));
            int j = 0;
            for (String sco : subType2id.keySet()) {
                writer.write(sco + "\t" + j++);
                writer.write("\n");
            }
            writer.close();

        }

//        private void getentity2subTypes(){
//            Map<String, Integer> entity2id = new HashMap<>();
//            Read_id("./type/sub-type/data/TKRL-all-input-v2/entity2id.txt", entity2id);
//            Map<String, Integer> subtype2id = new HashMap<>();
//            Read_id("./type/sub-type/data/type-completion/complete/subtype2id.txt", subtype2id);
//            BufferedWriter  writer=
//                    new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/entity2subtype.txt"));
//            File f = new File("./type/entity2type.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                HashSet<String> subTypes = new HashSet<>();
//                String[] split_data = line.split("\t");
//                writer.write(entity2id.get(split_data[0])+"\t");
//                for(int i=1;i<split_data.length;i++ ){
//                    subTypes.addAll(Arrays.asList(split_data[i].split("/")));
//                }
//                int j=0;
//                for(String sub :subTypes) {
//                    writer.write(subtype2id.get(sub)+"");
//                    if (j < subTypes.size() - 1)
//                        writer.write("\t");
//                    j++;
//                }
//                writer.write("\n");
//            }
//            writer.close();
//        }
    private static void seperateType() throws IOException {
        Map < String, Integer > type2id= new HashMap<>();
        Map < String, Integer > subtype2id= new HashMap<>();
        Read_id ("/home/wy/gilp_learn/type/sub-type/data/type2id.txt", type2id);//user/narphorium/people/wealthy_person	14
        Read_id ("/home/wy/gilp_learn/type/sub-type/data/subtype2id.txt", subtype2id);//radio_format	38
        MultivaluedHashMap<Integer, Integer> subClassOf2id_fb = new MultivaluedHashMap<>();
      //  Map < Integer, Integer > subClassOf2id_fb= new HashMap<>();//<subtype,subclassof,subtype>
        for(String type :type2id.keySet()){

            String[] split_data=type.split("/");
            for(int i=0;i<split_data.length-1;i++){
                int right=subtype2id.get(split_data[i]);
                int left=subtype2id.get(split_data[i+1]);
                if(left!=right)
                subClassOf2id_fb.add(left,right);

            }

        }

        BufferedWriter  writer =
                new BufferedWriter(new FileWriter("./type/sub-type/data/type-completion/subClassOf2id_fb2.txt"));//allTypeSumOfHeadRelation


        for (Map.Entry<Integer, List<Integer>> subClassOf2id : subClassOf2id_fb.entrySet()) {
            List<Integer> weightsValues = subClassOf2id.getValue();


            Set<Integer> set = new HashSet<>(weightsValues);
            weightsValues.clear();
            weightsValues.addAll(set);

            int subType = subClassOf2id.getKey();

            writer.write(subType+"");
            for(int id: weightsValues)
                writer.write("\t"+id);

            writer.write("\n");
        }

        writer.close();

    }


    public static void main (String[]args) throws IOException {

        String path = "./type/sub-type/data/TKRL-all-input-v2/type2id.txt";

        // ReadSubType(path);

        // globalMinWeightedSubTypes(ReadSubType(path));
        String path2 = "/home/wy/gilp_learn/type/types-weights.txt";
        // ReadSubTypeWeights(path2);
        String path3 = "./type/sub-type/data/TKRL-all-input-v2/train.txt";
        //  relationTypeWeight(path3);
        Map<String, Integer> type2id = new HashMap<>();
        Map<String, Integer> subType2id = new HashMap<>();
        ;
        // getSubType2id("./type/sub-type/data/TKRL-all-input-v2/type2id.txt", type2id, subType2id);

        // relationTypeWeight();
        seperateType();

    }

}
