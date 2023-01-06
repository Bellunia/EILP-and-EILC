package prediction_type.RHE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GlobalValue {

    //---------------------------WHE+stc
    static double type_weight = 0.9;		//proportional-declined weighting belta


//-----------------------------------------------------------------------------@wy
    static double  rate = 0.0025;		//learning rate
    static double  rate_begin = 0.0025;		//begin
    static double  rate_end = 0.0001;		//end
    static double margin = 1;		//loss margin
    static int method = 1;  // method = 1 means bern version, else unif version
    static int THREADS_NUM =8;



 //---------------------------------------------------------------------------------------
    // some train parameters
    static boolean L1_flag = false; // distance is l1(true) or l2(false)
    static int vector_len = 50; // the embedding vector dimension
    static int type_vector_len = 30;

    static double learning_rate1 = 0.01;
    static double learning_rate2 = 0.01;
    static double margin1 = 7;
    static double margin2 = -5;
    static double margin3 = 1;

    static double lameda1 = 0.7;
    static double lameda2 = 0.3;

    static String version = "bern";//string transE_version = "unif";		//unif/bern
    static int relation_num, entity_num, type_num, domain_num;
    static Map<String, Integer> relation2id, entity2id, type2id, domain2id;
    static Map<Integer, String> id2relation, id2entity, id2type, id2domain;

    static Map<Integer, Map<Integer, Integer>> left_entity, right_entity;
    static Map<Integer, Double> left_num, right_num;

    static Map<Integer, Integer> type2domain;//typeid2domainid

    static HashSet<String[]> relationDomain;//relation head-type tail-type
    //--/people/person/nationality	people	location   ----*****
    static Map<Integer, Integer> head_domain_vec; //<relationid,head-domain-id>
    static Map<Integer, Integer> tail_domain_vec;


    static HashSet<String[]> relationType;//relation head-type tail-type
    //--/people/person/nationality	people/person	location/country  ----*****
    static Map<Integer, Integer> head_type_vec;//<relationid,head-type-id>
    static Map<Integer, Integer> tail_type_vec;

    static double[][] domain_vec;

    static double[][] type_relation_vec;
    static double[][] relation_vec;
    static double[][] entity_vec;
    static double[][] type_vec;
    static double[][] A;
    static double[] rateconf;
    static double[] connectconf;
     List<Integer> fb_h;
      List<Integer> fb_l;
     List<Integer> fb_r;
    //relation-specific information: indicate the corresponding type head/tail should be in specific relation

    static Map<Integer, HashSet<Integer>> typeEntity;//typeid, entities_id
//???
    static int[][] type_entity_list;//record all entities a certain type has, for Soft type constraint
    static List<Integer> type_entity_num;

    //Freebase general hierarchical type structure: domain/type/topic
   // vector<vector<vector<double> > > domain_mat;		//sub-type matrices
  //  vector<vector<vector<double> > > type_mat;
    static  double[][][]  domain_mat;//<domain,type,topic>
    static  double[][][]  type_mat;


    static double[][] posErrorVec= new double[THREADS_NUM][vector_len];
    static double[][] negErrorVec= new double[THREADS_NUM][vector_len];
    static double[][] head_final_vec= new double[THREADS_NUM][vector_len];
    static double[][] tail_final_vec= new double[THREADS_NUM][vector_len];
    static  double[][][] mid_head_vec= new double[THREADS_NUM][2][vector_len];
    static double[][][] mid_tail_vec= new double[THREADS_NUM][2][vector_len];
    static double[][] mid_grad= new double[THREADS_NUM][vector_len];
    static double[][] mid_norm_vec= new double[THREADS_NUM][vector_len];
    static double[][] mid_norm_grad= new double[THREADS_NUM][vector_len];


    static  double[][][] tail_entity_vec= new double[entity_num][relation_num][vector_len];
    static double[][][] head_entity_vec= new double[entity_num][relation_num][vector_len];


}
