package prediction_type.transC;

import prediction_type.ConfE.KVPair;
import prediction_type.ConfE.Tri;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static prediction_type.ConfE.Utils.*;
import static prediction_type.transC.Parameter.*;

public class test_classification_normal {


    int valid_num = 0;
    int test_num = 0;

    static double[] delta_relation;
    KVPair<Double, Double>[] max_min_relation;

    int[][] right_triple, wrong_triple;


    private void prepare(Boolean final_test ) throws IOException {

        List<Tri> wrong_triple = new ArrayList<>();
        List<Tri> right_triple = new ArrayList<>();
        List<Tri> validList = new ArrayList<>();
        List<Tri> tripleList = new ArrayList<>();

        if (valid) {

            Read_Triple("./type/ontology/yago/YAGO39K/Valid/triple2id_negative.txt", wrong_triple, valid_num);
            Read_Triple("./type/ontology/yago/YAGO39K/Valid/triple2id_positive.txt", right_triple, valid_num);


        } else {

            Read_Triple("./type/ontology/yago/YAGO39K/Test/triple2id_negative.txt", wrong_triple, test_num);
            Read_Triple("./type/ontology/yago/YAGO39K/Test/triple2id_positive.txt", right_triple, test_num);

        }
        Read_Data("./type/ontology/yago/YAGO39K/Train/relation2id.txt", relation2id, relation_num);//--concept is the type in other kbs
        Read_Data("./type/ontology/yago/YAGO39K/Train/instance2id.txt", entity2id, entity_num);//--entity


if(!final_test)
            delta_relation= new double[relation_num];

        for (int i = 0; i < relation_num; ++i) {


            max_min_relation[i]= new KVPair<>((double)-1,(double)1000000);
        }


        Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../entity2vec.vec", entity_vec, vector_len);
        //     Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../concept2vec.vec", concept_vec, concept_vector_len);

        Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../relation2vec.vec", relation_vec, vector_len);


    }

    private boolean check(int h, int t, int r){
        double[] tmp=new double[vector_len];
        for(int i = 0; i < vector_len; ++i){
            tmp[i] = entity_vec[h][i] + relation_vec[r][i];
        }
        double dis = 0;
        for(int i = 0; i < vector_len; ++i){
            dis += abs(tmp[i] - entity_vec[t][i]);
        }

        if(getMinMax){
            if(dis > -1)
                max_min_relation[r].set_key( dis);
            if(dis < max_min_relation[r].getValue())
                max_min_relation[r].set_value( dis);
        }
        return dis < delta_relation[r];
    }

    private double[] test(){
        double TP = 0, TN = 0, FP = 0, FN = 0;
         double[][] ans = new double[relation_num][4];
         
        for(int i = 0; i < relation_num; ++i) {
            ans[i][0] = 0; ans[i][1] = 0; ans[i][2] = 0; ans[i][3] = 0;
        }
        int inputSize = valid ? valid_num : test_num;
        for(int i = 0; i < inputSize; ++i){
            if(check(right_triple[i][0], right_triple[i][1], right_triple[i][2])) {
                TP++;
                ans[right_triple[i][2]][0]++;
            }
            else{
                FN++;
                ans[right_triple[i][2]][1]++;
            }
            if(!check(wrong_triple[i][0], wrong_triple[i][1], wrong_triple[i][2])) {
                TN++;
                ans[wrong_triple[i][2]][2]++;
            }
            else {
                FP++;
                ans[wrong_triple[i][2]][3]++;
            }
        }
        if(valid){
            double[] returnAns= new double[relation_num];
          //  returnAns.resize(relation_num);
            for(int i = 0; i < relation_num; ++i){
                returnAns[i] = (ans[i][0] + ans[i][2]) * 100 / (ans[i][0] + ans[i][1] + ans[i][2] + ans[i][3]);
            }
            return returnAns;
        }else{
            System.out.println( "Triple classification:");
            System.out.println( "accuracy: " + (TP + TN) * 100 / (TP + TN + FP + FN) + "%" );
            System.out.println( "precision: " + TP * 100 /(TP + FP) + "%" );
            System.out.println( "recall: " + TP * 100 / (TP + FN) + "%" );
            double p = TP * 100 /(TP + FP), r = TP * 100 / (TP + FN);
            System.out.println( "F1-score: " + 2 * p * r / (p + r) + "%" );

            double[] tmp = new double[0];
            return tmp;
        }
    }

    private void runValid() throws IOException {
        getMinMax = true;
        test();
        getMinMax = false;

        double[] best_delta_relation= new double[relation_num];
        double[] best_ans_relation= new double[relation_num];

        for(int i = 0; i < relation_num; ++i)
            best_ans_relation[i] = 0;

        for(int i = 0; i < 100; ++i){
            for(int j = 0; j < relation_num; ++j){
                delta_relation[j] = max_min_relation[j].getValue() + (max_min_relation[j].getKey() - max_min_relation[j].getValue()) * i / 100;
            }
            double[] ans = test();
            for(int k = 0; k < relation_num; ++k){
                if(ans[k] > best_ans_relation[k]){
                    best_ans_relation[k] = ans[k];
                    best_delta_relation[k] = delta_relation[k];
                }
            }
        }
        for(int i = 0; i < relation_num; ++i){
            delta_relation[i] = best_delta_relation[i];
        }
        valid = false;
        prepare(true);
        test();
    }

}
