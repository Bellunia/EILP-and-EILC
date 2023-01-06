package prediction_type.transC;

import prediction_type.ConfE.Pair;
import prediction_type.ConfE.Utils;

import javax.ws.rs.core.MultivaluedHashMap;
import java.io.*;
import java.util.*;


import static java.lang.Math.abs;
import static prediction_type.ConfE.Utils.*;
import static prediction_type.transC.Parameter.*;
import static transX.Gradient.calc_sum_transE;

public class trainTransC {

    private double res; // loss function value
    private double res2;
    static List<Integer> fb_h;
    static List<Integer> fb_l;
    static List<Integer> fb_r;


    static Map<Integer, Set<Integer>> head2triple;
    static Map<Integer, Set<Integer>> tail2triple;


    private static void ParameterInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        concept2id = new HashMap<>();
        instance2id = new HashMap<>();
        id2relation = new HashMap<>();
        id2entity = new HashMap<>();
        id2instance = new HashMap<>();
        id2concept= new HashMap<>();


        fb_h = new ArrayList<>();
        fb_l = new ArrayList<>();
        fb_r = new ArrayList<>();
        head2triple = new HashMap<>();
        tail2triple = new HashMap<>();


        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        left_num = new HashMap<>();
        right_num = new HashMap<>();
        head_relation2tail = new HashMap<>();
        instanceOf2id= new HashMap<>();
        subClassOf2id= new HashMap<>();
        concept_instance= new MultivaluedHashMap<>();
        up_sub_concept= new MultivaluedHashMap<>();

    }


    private Boolean checkSubClass(int concept1, int concept2){
        double dis = 0;
        for(int i = 0; i < vector_len; ++i){
            dis += sqr(concept_vec[concept1][i] - concept_vec[concept2][i]);
        }
        if(sqrt(dis) < abs(concept_r[concept1] - concept_r[concept2]) && concept_r[concept1] < concept_r[concept2]){
            return true;
        }
        if(sqrt(dis) < concept_r[concept1] + concept_r[concept2]){
            double tmp = (concept_r[concept1] + concept_r[concept2] - sqrt(dis)) / concept_r[concept1];
            return tmp > delta_sub;
        }
        return false;
    }

    private Boolean checkInstance(int instance, int concept){
        double dis = 0;
        for(int i = 0; i < vector_len; ++i){
            dis += sqr(entity_vec[instance][i] - concept_vec[concept][i]);
        }
        if(sqrt(dis) < concept_r[concept]){
            return true;
        }
        double tmp = concept_r[concept] / sqrt(dis);
        return tmp > delta_ins;
    }

    private static void prepare1() throws IOException {
        ParameterInit();


        Read_Data("./type/ontology/yago/YAGO39K/Train/relation2id.txt",
                relation2id,relation_num);
        Read_Data("./type/ontology/yago/YAGO39K/Train/concept2id.txt",
                concept2id,concept_num);//--concept is the type in other kbs
        Read_Data("./type/ontology/yago/YAGO39K/Train/instance2id.txt",
                entity2id,entity_num);//--entity
        Read_Triple("./type/ontology/yago/YAGO39K/Train/triple2id.txt",  head_relation2tail, triple_num);

        //----------------------

        Read_Data("./type/ontology/yago/YAGO39K/Train/instanceOf2id.txt",
                instanceOf2id);//[instance_id concept_id]
        Read_Data("./type/ontology/yago/YAGO39K/Train/subClassOf2id.txt",
                subClassOf2id);//[sub_concept_id concept_id]--<sub,parent>
        MultivaluedHashMap<String, Double> newTailTypeMap = new MultivaluedHashMap<>();

        for(int instance : instanceOf2id.keySet()){
            concept_instance.add(instanceOf2id.get(instance),instance);
        }

        for(int concept : subClassOf2id.keySet()){
            up_sub_concept.add(subClassOf2id.get(concept),concept);
        }


        System.out.printf("entity number = %s\n", instance_num);
        System.out.printf("relation number = %s\n", relation_num);
        System.out.printf("type number = %s\n", concept_num);
        System.out.printf("triple number = %s\n", triple_num);
    }



    private static void prepare() throws IOException {
        ParameterInit();
        entity_num = Read_idData("./type/ontology/yago/YAGO39K/M-Test/instanceOf2id_negative.txt", entity2id, id2entity);
        relation_num = Read_idData("./type/ontology/yago/YAGO39K/Train/relation2id.txt", relation2id, id2relation);
        concept_num = Read_idData("./type/ontology/yago/YAGO39K/Train/concept2id.txt", concept2id, id2concept);
        instance_num= Read_idData("./type/ontology/yago/YAGO39K/Train/instance2id.txt", instance2id, id2instance);

    }

    private void trainHLR(int i, int cut) {
        int j = 0;
        double pr = 500;
        if (bern) pr = 1000 * right_num.get(fb_r.get(i)) / (right_num.get(fb_r.get(i)) + left_num.get(fb_r.get(i)));
        if (rand() % 1000 < pr) {
            Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
            Set<Integer> values = head_relation2tail.get(key);
            do {
                if (instance_brother[fb_l.get(i)] != null) {
                    if (rand() % 10 < cut) {
                        j = rand_max(entity_num);
                    } else {
                        j = rand() % (int) instance_brother[fb_l.get(i)].length;
                        j = instance_brother[fb_l.get(i)][j];
                    }
                } else {
                    j = rand_max(entity_num);
                }

            } while (values.contains(j));
            doTrainHLR(fb_h.get(i), fb_l.get(i), fb_r.get(i), fb_h.get(i), j, fb_r.get(i));

            //-----
        } else {
            Pair<Integer, Integer> key = new Pair<>(j, fb_r.get(i));
            Set<Integer> values = head_relation2tail.get(key);
            do {
                if (instance_brother[fb_h.get(i)] != null) {
                    if (rand() % 10 < cut) {
                        j = rand_max(entity_num);
                    } else {
                        j = rand() % (int) instance_brother[fb_h.get(i)].length;
                        j = instance_brother[fb_h.get(i)][j];
                    }
                } else {
                    j = rand_max(entity_num);
                }

            }

            while (values.contains(fb_l.get(i)));// check the triple <j,fb_r.get(i),fb_l.get(i)> if exists.
            doTrainHLR(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));

        }
        norm(relation_vec[fb_r.get(i)], vector_len);
        norm(entity_vec[fb_h.get(i)], vector_len);
        norm(entity_vec[fb_l.get(i)], vector_len);
        norm(entity_vec[j], vector_len);
    }


    private void doTrainHLR(int e1_a, int e2_a, int rel_a, int e1_b, int e2_b, int rel_b) {
        double sum1 = calc_sum_transE(e1_a, e2_a, rel_a);
        double sum2 = calc_sum_transE(e1_b, e2_b, rel_b);
        if (sum1 + margin > sum2) {
            res += margin + sum1 - sum2;
            gradientHLR(e1_a, e2_a, rel_a, e1_b, e2_b, rel_b);

        }
    }

    private void doTrainInstanceOf(int e_a, int c_a, int e_b, int c_b) {
        double sum1 = calcSumInstanceOf(e_a, c_a);
        double sum2 = calcSumInstanceOf(e_b, c_b);
        if (sum1 + margin_instance > sum2) {
            res += (margin_instance + sum1 - sum2);
            gradientInstanceOf(e_a, c_a, e_b, c_b);
        }
    }

    private void doTrainSubClassOf(int c1_a, int c2_a, int c1_b, int c2_b) {
        double sum1 = calcSumSubClassOf(c1_a, c2_a);
        double sum2 = calcSumSubClassOf(c1_b, c2_b);
        if (sum1 + margin_subclass > sum2) {
            res += (margin_subclass + sum1 - sum2);
            gradientSubClassOf(c1_a, c2_a, c1_b, c2_b);
        }
    }



    double calcSumInstanceOf(int e, int c) {
        double dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(entity_vec[e][i] - concept_vec[c][i]);
        }
        if (dis < sqr(concept_r[c])) {
            return 0;
        }
        return dis - sqr(concept_r[c]);

    }

    double calcSumSubClassOf(int c1, int c2) {
        double dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(concept_vec[c1][i] - concept_vec[c2][i]);
        }
        if (sqrt(dis) < Utils.abs(concept_r[c1] - concept_r[c2])) {
            return 0;
        }
        return dis - sqr(concept_r[c2]) + sqr(concept_r[c1]);

    }

    void gradientHLR(int e1_a, int e2_a, int rel_a, int e1_b, int e2_b, int rel_b) {
        for (int i = 0; i < vector_len; i++) {
            double x = 2 * (entity_vec[e2_a][i] - entity_vec[e1_a][i] - relation_vec[rel_a][i]);
            if (L1Flag) {
                if (x > 0) {
                    x = 1;
                } else {
                    x = -1;
                }
            }
            relation_vec[rel_a][i] -= -1 * rate * x;
            entity_vec[e1_a][i] -= -1 * rate * x;
            entity_vec[e2_a][i] += -1 * rate * x;
            x = 2 * (entity_vec[e2_b][i] - entity_vec[e1_b][i] - relation_vec[rel_b][i]);
            if (L1Flag) {
                if (x > 0) {
                    x = 1;
                } else {
                    x = -1;
                }
            }
            relation_vec[rel_b][i] -= rate * x;
            entity_vec[e1_b][i] -= rate * x;
            entity_vec[e2_b][i] += rate * x;
        }
    }

    void gradientInstanceOf(int e_a, int c_a, int e_b, int c_b) {
        double dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(entity_vec[e_a][i] - concept_vec[c_a][i]);
        }
        if (dis > sqr(concept_r[c_a])) {
            for (int j = 0; j < vector_len; ++j) {
                double x = 2 * (entity_vec[e_a][j] - concept_vec[c_a][j]);
                entity_vec[e_a][j] -= x * rate;
                concept_vec[c_a][j] -= -1 * x * rate;
            }
            concept_r[c_a] -= -2 * concept_r[c_a] * rate;
        }

        dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(entity_vec[e_b][i] - concept_vec[c_b][i]);
        }
        if (dis > sqr(concept_r[c_b])) {
            for (int j = 0; j < vector_len; ++j) {
                double x = 2 * (entity_vec[e_b][j] - concept_vec[c_b][j]);
                entity_vec[e_b][j] += x * rate;
                concept_vec[c_b][j] += -1 * x * rate;
            }
            concept_r[c_b] += -2 * concept_r[c_b] * rate;
        }
    }

    void gradientSubClassOf(int c1_a, int c2_a, int c1_b, int c2_b) {
        double dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(concept_vec[c1_a][i] - concept_vec[c2_a][i]);
        }
        if (sqrt(dis) > Utils.abs(concept_r[c1_a] - concept_r[c2_a])) {
            for (int i = 0; i < vector_len; ++i) {
                double x = 2 * (concept_vec[c1_a][i] - concept_vec[c2_a][i]);
                concept_vec[c1_a][i] -= x * rate;
                concept_vec[c2_a][i] -= -x * rate;
            }
            concept_r[c1_a] -= 2 * concept_r[c1_a] * rate;
            concept_r[c2_a] -= -2 * concept_r[c2_a] * rate;
        }

        dis = 0;
        for (int i = 0; i < vector_len; ++i) {
            dis += sqr(concept_vec[c1_b][i] - concept_vec[c2_b][i]);
        }
        if (sqrt(dis) > Utils.abs(concept_r[c1_b] - concept_r[c2_b])) {
            for (int i = 0; i < vector_len; ++i) {
                double x = 2 * (concept_vec[c1_b][i] - concept_vec[c2_b][i]);
                concept_vec[c1_b][i] += x * rate;
                concept_vec[c2_b][i] += -x * rate;
            }
            concept_r[c1_b] += 2 * concept_r[c1_b] * rate;
            concept_r[c2_b] += -2 * concept_r[c2_b] * rate;
        }
    }



}
