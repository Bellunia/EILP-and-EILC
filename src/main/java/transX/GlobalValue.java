package transX;

import java.util.Map;

public class GlobalValue {

    public static int EmbeddingAlgorithm = 1;
    //1-transE,2-PTransE;3-transH;4--transR

    public static boolean L1_flag = true; // distance is l1 or l2

    public static int vector_len = 100; // the embedding vector dimension --transE; PTransE
    // static int vector_dimension = 110; vector_dimension --transH
    // only the value is different
    public static int relation_dimension = 100;//---TransR; vector_len== relation_dimension
    public static int entity_dimension = 100;//---TransR
    //   relation_dimension== entity_dimension ,give the same value in relation_dimension and entity_dimension of TransR
//----------------------------------------------------------------

    public static double margin = 0.1;
    //transR_margin=0.2; transH_margin=0.2;
    // ---PTransE_margin = 0.1; transE_margin=0.1;

    public static double learning_rate = 0.02;

    public static int nepoch = 1200;  // 迭代次数  // the SGD iteration times
    public static int nbatches = 150;  // batch 大小   // the number of triples needed to train in each iteration


    public static int method = 1;  // method = 1 means bern version, else unif version
    public static String version = "bern";
   // int method=0; String version = "unif";



    public static int relation_num, entity_num;
    public static Map<String, Integer> relation2id, entity2id;
    public static Map<Integer, String> id2relation, id2entity;
    public static Map<Integer, Map<Integer, Integer>> left_entity, right_entity;
    public static Map<Integer, Double> left_num, right_num;

    public static double[][] relation_vec;
    public static double[][] entity_vec;

    public static double[][] Wr_vec_transH;//--TransH

    // public  static double[][][] Wr_vec;//----transR,TransH

    //----transR
    public static double[][] relation_copy;
    public static double[][] entity_copy;
    public static double[][][] Wr_vec_transR, Wr_transR_copy;
//----transR --special

    //PTransE---
    public static double margin_relation = 0.2;
    public static Map<Pair<String, Integer>, Double> path_confidence;
    //----PTransE---

}
