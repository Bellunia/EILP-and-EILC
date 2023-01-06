package prediction_type.transC;

import gilp.utils.KVPair;

import java.io.*;
import java.util.*;

import static java.lang.Math.abs;
import static prediction_type.ConfE.Utils.*;
import static prediction_type.transC.Parameter.*;

public class test_classification_isA {

    private static void ParameterInit() {

        entity2id = new HashMap<>();
        concept2id = new HashMap<>();
        instance2id = new HashMap<>();

        id2relation = new HashMap<>();
        id2entity = new HashMap<>();
        id2instance = new HashMap<>();
        id2concept = new HashMap<>();

        ins_right = new HashMap<>();
        ins_wrong = new HashMap<>();
        sub_right = new HashMap<>();
        sub_wrong = new HashMap<>();


    }

    private Boolean checkSubClass(int concept1, int concept2) {
        double dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(concept_vec[concept1][i] - concept_vec[concept2][i]);
        }
        if (sqrt(dis) < abs(concept_r[concept1] - concept_r[concept2]) && concept_r[concept1] < concept_r[concept2]) {
            return true;
        }
        if (sqrt(dis) < concept_r[concept1] + concept_r[concept2]) {
            double tmp = (concept_r[concept1] + concept_r[concept2] - sqrt(dis)) / concept_r[concept1];
            if (tmp > delta_sub)
                return true;
        }
        return false;
    }

    private Boolean checkInstance(int instance, int concept) {
        double dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(entity_vec[instance][i] - concept_vec[concept][i]);
        }
        if (sqrt(dis) < concept_r[concept]) {
            return true;
        }
        double tmp = concept_r[concept] / sqrt(dis);
        return tmp > delta_ins;
    }




    private static void prepare() throws IOException {
        ParameterInit();

        int ins_negative_num = 0;
        int ins_positive_num = 0;

        if (valid) {
            if (mix) {
                Read_IntData("./type/ontology/yago/YAGO39K/M-Valid/instanceOf2id_negative.txt", ins_wrong, ins_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/M-Valid/instanceOf2id_positive.txt", ins_right, ins_positive_num);
            } else {
                Read_IntData("./type/ontology/yago/YAGO39K/Valid/instanceOf2id_negative.txt", ins_wrong, ins_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/Valid/instanceOf2id_positive.txt", ins_right, ins_positive_num);
            }
        } else {

            if (mix) {
                Read_IntData("./type/ontology/yago/YAGO39K/M-Test/instanceOf2id_negative.txt", ins_wrong, ins_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/M-Test/instanceOf2id_positive.txt", ins_right, ins_positive_num);
            } else {
                Read_IntData("./type/ontology/yago/YAGO39K/Test/instanceOf2id_negative.txt", ins_wrong, ins_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/Test/instanceOf2id_positive.txt", ins_right, ins_positive_num);
            }

        }

        ins_test_num = ins_negative_num;

        int sub_negative_num = 0;
        int sub_positive_num = 0;

        if (valid) {
            if (mix) {
                Read_IntData("./type/ontology/yago/YAGO39K/M-Valid/subClassOf2id_negative.txt", sub_wrong, sub_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/M-Valid/subClassOf2id_positive.txt", sub_right, sub_positive_num);
            } else {
                Read_IntData("./type/ontology/yago/YAGO39K/Valid/subClassOf2id_negative.txt", sub_wrong, sub_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/Valid/subClassOf2id_positive.txt", sub_right, sub_positive_num);
            }
        } else {

            if (mix) {
                Read_IntData("./type/ontology/yago/YAGO39K/M-Test/subClassOf2id_negative.txt", sub_wrong, sub_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/M-Test/subClassOf2id_positive.txt", sub_right, sub_positive_num);
            } else {
                Read_IntData("./type/ontology/yago/YAGO39K/Test/subClassOf2id_negative.txt", sub_wrong, sub_negative_num);
                Read_IntData("./type/ontology/yago/YAGO39K/Test/subClassOf2id_positive.txt", sub_right, sub_positive_num);
            }
        }

        sub_test_num = sub_negative_num;

        Read_Data("./type/ontology/yago/YAGO39K/Train/concept2id.txt", concept2id, concept_num);//--concept is the type in other kbs
        Read_Data("./type/ontology/yago/YAGO39K/Train/instance2id.txt", entity2id, entity_num);//--entity

        entity_vec = new double[entity_num][vector_len];
        concept_vec = new double[concept_num][concept_vector_len];

        Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../entity2vec.vec", entity_vec, vector_len);
        Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../concept2vec.vec", concept_vec, concept_vector_len);


    }




    private  KVPair<Double,Double> test() {
        double TP_ins = 0, TN_ins = 0, FP_ins = 0, FN_ins = 0;
        double TP_sub = 0, TN_sub = 0, FP_sub = 0, FN_sub = 0;
        Map<Integer, Double> TP_ins_map = new HashMap<>();
        Map<Integer, Double> TN_ins_map = new HashMap<>();
        Map<Integer, Double> FP_ins_map = new HashMap<>();
        Map<Integer, Double> FN_ins_map = new HashMap<>();

        Set<Integer> concept_set = new HashSet<>();
        for (Integer instance : ins_right.keySet()) {
            int concept = ins_right.get(instance);

            if (checkInstance(instance, concept)) {
                TP_ins++;
                if (TP_ins_map.containsKey(concept)) {
                    double value = TP_ins_map.get(concept);
                    value++;
                    TP_ins_map.put(concept, value);
                } else {
                    TP_ins_map.put(concept, 1.0);
                }
            } else {
                FN_ins++;
                if (FN_ins_map.containsKey(concept)) {
                    double value = FN_ins_map.get(concept);
                    value++;
                    FN_ins_map.put(concept, value);

                } else {
                    FN_ins_map.put(concept, 1.0);
                }
            }


            concept_set.add(concept);
        }

        for (Integer instance : ins_wrong.keySet()) {

            int concept = ins_wrong.get(instance);
            if (!checkInstance(instance, concept)) {
                TN_ins++;
                if (TN_ins_map.containsKey(concept)) {

                    double value = TN_ins_map.get(concept);
                    value++;
                    TN_ins_map.put(concept, value);
                } else {
                    TN_ins_map.put(concept, 1.0);
                }
            } else {
                FP_ins++;
                if (FP_ins_map.containsKey(concept)) {
                    double value = FP_ins_map.get(concept);
                    value++;
                    FP_ins_map.put(concept, value);

                } else {

                    FP_ins_map.put(concept, 1.0);
                }
            }
            concept_set.add(concept);
        }

        for (Integer concept : sub_right.keySet()) {
            int parent = sub_right.get(concept);
            if (checkSubClass(concept, parent))
                TP_sub++;
            else
                FN_sub++;

        }
        for (Integer concept : sub_wrong.keySet()) {
            int parent = sub_wrong.get(concept);
            if (checkSubClass(concept, parent))
                TN_sub++;
            else
                FP_sub++;

        }


        if (valid) {
            double ins_ans = (TP_ins + TN_ins) * 100 / (TP_ins + TN_ins + FN_ins + FP_ins);
            double sub_ins = (TP_sub + TN_sub) * 100 / (TP_sub + TN_sub + FN_sub + FP_sub);
            return new KVPair<>(ins_ans, sub_ins);
        } else {
            System.out.println("instanceOf triple classification:");
            System.out.println("accuracy: " + (TP_ins + TN_ins) * 100 / (TP_ins + TN_ins + FN_ins + FP_ins) + "%");
            System.out.println("precision: " + TP_ins * 100 / (TP_ins + FP_ins) + "%");
            System.out.println("recall: " + TP_ins * 100 / (TP_ins + FN_ins) + "%");
            double p = TP_ins * 100 / (TP_ins + FP_ins), r = TP_ins * 100 / (TP_ins + FN_ins);
            System.out.println("F1-score: " + 2 * p * r / (p + r) + "%");

            System.out.println("subClassOf triple classification:");
            System.out.println("accuracy: " + (TP_sub + TN_sub) * 100 / (TP_sub + TN_sub + FN_sub + FP_sub) + "%");
            System.out.println("precision: " + TP_sub * 100 / (TP_sub + FP_sub) + "%");
            System.out.println("recall: " + TP_sub * 100 / (TP_sub + FN_sub) + "%");
            p = TP_sub * 100 / (TP_sub + FP_sub);
            r = TP_sub * 100 / (TP_sub + FN_sub);
            System.out.println("F1-score: " + 2 * p * r / (p + r) + "%");

            for (int concept : concept_set) {

                TP_ins = TP_ins_map.get(concept);
                TN_ins = TN_ins_map.get(concept);
                FN_ins = FN_ins_map.get(concept);
                FP_ins = FP_ins_map.get(concept);
                double accuracy = (TP_ins + TN_ins) * 100 / (TP_ins + TN_ins + FN_ins + FP_ins);
                double precision = TP_ins * 100 / (TP_ins + FP_ins);
                double recall = TP_ins * 100 / (TP_ins + FN_ins);
                p = TP_ins * 100 / (TP_ins + FP_ins);
                r = TP_ins * 100 / (TP_ins + FN_ins);
                double f1 = 2 * p * r / (p + r);
            }
            return new KVPair<>(0.0, 0.0);
        }
    }


    private void runValid() throws IOException {
        double ins_best_answer = 0, ins_best_delta = 0;
        double sub_best_answer = 0, sub_best_delta = 0;
        for(int i = 0; i < 101; ++i){
            double f = i; f /= 100;
            delta_ins = f;
            delta_sub = f * 2;
            KVPair<Double,Double> ans = test();

            if(ans.getKey() > ins_best_answer){
                ins_best_answer = ans.getKey();
                ins_best_delta = f;
            }
            if(ans.getValue() > sub_best_answer){
                sub_best_answer = ans.getValue() ;
                sub_best_delta = f * 2;
            }
        }
        System.out.println( "delta_ins is " + ins_best_delta + ". The best ins accuracy on valid data is " + ins_best_answer +"%" );
        System.out.println( "delta_sub is " + sub_best_delta +". The best sub accuracy on valid data is " +sub_best_answer +"%");

        delta_ins = ins_best_delta;
        delta_sub = sub_best_delta;
        valid = false;
        prepare();
        test();
    }

    public void main (String[]args) throws IOException {
        if (mix)
            System.out.println( "mix = " + "True");
        else
            System.out.println( "mix = " + "False" );
        System.out.println( "dimension = " + vector_len);
        prepare();
        runValid();
    }



}
