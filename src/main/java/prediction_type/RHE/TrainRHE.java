package prediction_type.RHE;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static prediction_type.ConfE.Utils.*;
import static prediction_type.RHE.GlobalValue.*;


public class TrainRHE {

    private static Train train;

    private static void GlobalValueInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        type2id = new HashMap<>();
        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        left_num = new HashMap<>();
        right_num = new HashMap<>();
        //----------------------@wy
        domain2id = new HashMap<>();
        typeEntity = new HashMap<>();
        relationDomain = new HashSet<>();
        relationType = new HashSet<>();

        id2relation = new HashMap<>();
        id2entity = new HashMap<>();
        id2type = new HashMap<>();
        id2domain= new HashMap<>();

        head_type_vec = new HashMap<>();
        tail_type_vec = new HashMap<>();
        head_domain_vec = new HashMap<>();
        tail_domain_vec = new HashMap<>();

    }



    private static void prepare() throws IOException {
        GlobalValueInit();

       // entity_num = Read_Data("./type/sub-type/data/TKRL-all-input-v2/entity2id.txt", entity2id);
      //  relation_num = Read_Data("./type/sub-type/data/TKRL-all-input-v2/relation2id.txt", relation2id);
      //  type_num = Read_Data("./type/sub-type/data/TKRL-all-input-v2/type2id.txt", type2id);
      //  domain_num = Read_Data("./type/sub-type/data/TKRL-all-input-v2/domain2id.txt", domain2id);
        entity_num =Read_idData("./type/sub-type/data/TKRL-all-input-v2/entity2id.txt", entity2id, id2entity);
        relation_num =Read_idData("./type/sub-type/data/TKRL-all-input-v2/relation2id.txt", relation2id, id2relation);
        type_num =  Read_idData("./type/sub-type/data/TKRL-all-input-v2/type2id.txt", type2id, id2type);
        domain_num = Read_idData("./type/sub-type/data/TKRL-all-input-v2/domain2id.txt", domain2id,id2domain);

        Read_Data2("./type/sub-type/data/TKRL-all-input-v2/typeEntity.txt", typeEntity);

        Read_Data3("./type/sub-type/data/TKRL-all-input-v2/relationDomain.txt", relationDomain);
        Read_Data3("./type/sub-type/data/TKRL-all-input-v2/relationType.txt", relationType);

        for(String[] key: relationType){
//build relation-specific information for type
            head_type_vec.put(relation2id.get(key[0]), type2id.get(key[1]));
            tail_type_vec.put(relation2id.get(key[0]), type2id.get(key[2]));
        }

        for(String[] key: relationDomain){
//build relation-specific information for domain
            head_domain_vec.put(relation2id.get(key[0]), domain2id.get(key[1]));
            tail_domain_vec.put(relation2id.get(key[0]), domain2id.get(key[2]));
        }



        File f = new File("./type/sub-type/data/TKRL-all-input-v2/train.txt");
        File f1 = new File("./type/resource/data/YAGO/YAGO43k_Entity_Types/YAGO43k_Entity_Type_train10.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), "UTF-8"));


        String line;
        int k = 0;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            String relation = split_data[2];
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss entity: %s\n", head);
                continue;
            }
            if (!entity2id.containsKey(tail)) {
                System.out.printf("miss entity: %s\n", tail);
                continue;
            }
            if (!relation2id.containsKey(relation)) {
                relation2id.put(relation, relation_num);
                relation_num++;
            }

            if (!left_entity.containsKey(relation2id.get(relation))) {
                left_entity.put(relation2id.get(relation), new HashMap<>());
            }
            Map<Integer, Integer> value = left_entity.get(relation2id.get(relation));
            if (!value.containsKey(entity2id.get(head))) {
                value.put(entity2id.get(head), 0);
            }
            value.put(entity2id.get(head), value.get(entity2id.get(head)) + 1);

            if (!right_entity.containsKey(relation2id.get(relation))) {
                right_entity.put(relation2id.get(relation), new HashMap<>());
            }
            value = right_entity.get(relation2id.get(relation));
            if (!value.containsKey(entity2id.get(tail))) {
                value.put(entity2id.get(tail), 0);
            }
            value.put(entity2id.get(tail), value.get(entity2id.get(tail)) + 1);

            train.add(entity2id.get(head), relation2id.get(relation), entity2id.get(tail), k++);
        }

        while ((line = reader1.readLine()) != null) {
            String[] split_data = line.split("\t");
            String heade = split_data[0];
            String tailet = split_data[1];
            train.add2(entity2id.get(heade), type2id.get(tailet));
        }

        for (int i = 0; i < relation_num; i++) {
            int count = 0;
            double sum = 0;
            Map<Integer, Integer> value = left_entity.get(i);
            for (int id : value.keySet()) {
                count++;
                sum += value.get(id);
            }
            left_num.put(i, sum / count);

            count = 0;
            sum = 0;
            value = right_entity.get(i);
            for (int id : value.keySet()) {
                count++;
                sum += value.get(id);
            }
            right_num.put(i, sum / count);
        }

        System.out.printf("entity number = %s\n", entity_num);
        System.out.printf("relation number = %s\n", relation_num);
        System.out.printf("type number = %s\n", type_num);
        System.out.printf("domain number = %s\n", domain_num);
    }

    public static void train_run() throws IOException {
        int nepoch = 800; //循环次数 int nepoch = 1000;
        if (method == 0) {
            version = "unif";
        }
        System.out.printf("iteration times = %s\n", nepoch);
        train = new Train();
        prepare();
        train.run(nepoch);
    }


    //calc entity representation
    void calc_entity_vec(int head, int tail, int rel, int tid)		//use Weighted Hierarchy Encoder
    {
        int tempHeadType = head_type_vec.get(rel);
        int tempTailType = tail_type_vec.get(rel);
        int tempHeadDomain = head_domain_vec.get(rel);
        int tempTailDomain = tail_domain_vec.get(rel);


        //build head_final_vec
        for(int i=0; i<vector_len; i++)
        {
            double type_score = 0, domain_score = 0;
            for(int ii=0; ii<vector_len; ii++)
            {
                type_score += type_mat[tempHeadType][i][ii] * entity_vec[head][ii];
            }
            for(int ii=0; ii<vector_len; ii++)
            {
                domain_score += domain_mat[tempHeadDomain][i][ii] * entity_vec[head][ii];
            }
            head_final_vec[tid][i] = type_score * type_weight + domain_score * (1-type_weight);
        }
        //build tail_final_vec
        for(int i=0; i<vector_len; i++)
        {
            double type_score = 0, domain_score = 0;
            for(int ii=0; ii<vector_len; ii++)
            {
                type_score += type_mat[tempTailType][i][ii] * entity_vec[tail][ii];
            }
            for(int ii=0; ii<vector_len; ii++)
            {
                domain_score += domain_mat[tempTailDomain][i][ii] * entity_vec[tail][ii];
            }
            tail_final_vec[tid][i] = type_score * type_weight + domain_score * (1-type_weight);
        }
    }

    double calc_sum_triple(int e1,int e2,int rel, int flag, int tid)		//similarity
    {
        double sum=0;
        calc_entity_vec(e1, e2, rel, tid);
        if(flag == 1)		//positive_sign
        {
            if (L1_flag)		//L1
            {
                for (int ii=0; ii<vector_len; ii++)
                {
                    double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
                    sum+=abs(tempSum);
                    if(tempSum > 0)
                        posErrorVec[tid][ii] = 1;
                    else
                        posErrorVec[tid][ii] = -1;
                }
            }
            else		//L2
            {
                for (int ii=0; ii<vector_len; ii++)
                {
                    double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
                    sum+=sqr(tempSum);
                    posErrorVec[tid][ii] = 2*tempSum;
                }
            }
            return sum;
        }
        else		//negative_sign
        {
            if (L1_flag)		//L1
            {
                for (int ii=0; ii<vector_len; ii++)
                {
                    double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
                    sum+=abs(tempSum);
                    if(tempSum > 0)
                        negErrorVec[tid][ii] = 1;
                    else
                        negErrorVec[tid][ii] = -1;
                }
            }
            else		//L2
            {
                for (int ii=0; ii<vector_len; ii++)
                {
                    double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
                    sum+=sqr(tempSum);
                    negErrorVec[tid][ii] = 2*tempSum;
                }
            }
            return sum;
        }
    }

}
