package prediction_type.transC;

import prediction_type.ConfE.Pair;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.Map;
import java.util.Set;

public class Parameter {
    //static int dim = 100; // the embedding vector dimension
    static int vector_len = 100;
    static int concept_vector_len=100;
    static int sub_test_num = 0, ins_test_num = 0;
    static double delta_ins = 0, delta_sub = 0;

    static Boolean valid = true;
    static Boolean mix = false;
    static Boolean getMinMax = false;
    static  Boolean L1Flag = true;
    static  Boolean bern = false;
    static double ins_cut = 8.0;
    static double sub_cut = 8.0;

    static  double rate = 0.001, margin = 1, margin_instance = 0.4, margin_subclass = 0.3;

    static Map<Integer, Integer>  ins_right, ins_wrong, sub_right, sub_wrong;// vector<pair<int, int> >

    static double[][] concept_vec;
    static double[][] entity_vec;//vector<vector<double> >
    static double[][] relation_vec;
    static Map<String, Integer> relation2id, entity2id;
    static Map<Integer, String> id2relation, id2entity, id2concept,id2instance;
    static int relation_num, entity_num, concept_num,triple_num;
    static int instance_num;
    static Map<Pair<Integer, Integer>, Set<Integer>> head_relation2tail; // to save the (h, r, ti)
    static Map<String, Integer> concept2id,instance2id;
    static Map<Integer, Integer>   instanceOf2id,subClassOf2id;
    /*

    instance2id.txt, concept2id.txt, relation2id.txt: the id of every instance, concept and relation in this dataset.
    triple2id.txt: normal triples represented as ids with format [head_instance_id tail_instance_id relation_id].
    instanceOf2id.txt: instanceOf triples represented as ids with format [instance_id concept_id].
    subClassOf2id.txt: subClassOf triples represented as ids with format [sub_concept_id concept_id].

     */
    static MultivaluedHashMap<Integer, Integer> concept_instance;
    static MultivaluedHashMap<Integer, Integer> up_sub_concept;
   // static int[][] concept_instance;//1-N
 //   static int[][] instance_concept;//instanceOf2id
    static int[][] instance_brother;
   // static int[][] sub_up_concept;//subClassOf2id


    //static int[][] up_sub_concept;//1-N
    static int[][] concept_brother;
    static Map<Integer,Map<Integer,Integer> > left_entity, right_entity;
    static  Map<Integer, Double> left_num, right_num;
    static double[] concept_r;


}
