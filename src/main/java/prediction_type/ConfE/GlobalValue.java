package prediction_type.ConfE;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.Map;

public class GlobalValue {
    // some train parameters
    static boolean L1_flag = false; // distance is l1 or l2
    static int vector_len = 50; // the embedding vector dimension---dim = 100;

    static int type_vector_len = 30;

    public static int nepoch = 800;  // 迭代次数  // the SGD iteration times
    public static int nbatches = 150;  // batch 大小   // the number of triples needed to train in each iteration


    static double learning_rate1 = 0.01;
    static double learning_rate2 = 0.01;
    static double margin1 = 7;
    static double margin2 = -5;
    static double margin3 = 1;
    static int method = 1;  // method = 1 means bern version, else unif version
    static double lameda1 = 0.7;
    static double lameda2 = 0.3;

    static String version = "bern";
    static int relation_num, entity_num, type_num;
    static Map<String, Integer> relation2id, entity2id, type2id;
    static Map<Integer, String> id2relation, id2entity, id2type;
    static Map<Integer, Map<Integer, Integer>> left_entity, right_entity;
    static Map<Integer, Double> left_num, right_num;

    static double[][] type_relation_vec;
    static double[][] relation_vec;
    static double[][] entity_vec;
    static double[][] type_vec;
    static double[][] A;
    static double[] rateconf;
    static double[] connectconf;


    // some train parameters---in transE
//    static boolean L1_flag = true; // distance is l1 or l2
//    static int vector_len = 50; // the embedding vector dimension
//    static int m = 30;
//    static double learning_rate1 = 0.01;
//    static double learning_rate2 = 0.01;
//    static double margin1 = 3;
//    static double margin2 = 1;
//    static double lameda1 = 0.8;
//    static double lameda2 = 0.2;

    //-------new add
    static Map<Integer, Integer>   instanceOf2id;
    static Map<Integer, Integer>  subClassOf2id;
    static MultivaluedHashMap<Integer, Integer> concept_instance;
    static MultivaluedHashMap<Integer, Integer> up_sub_concept;
}
