package prediction_type.ConfE;

import javax.ws.rs.core.MultivaluedHashMap;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static prediction_type.ConfE.GlobalValue.*;



public class TrainRun {

    private static Train train;

    private static int Read_Data(String file_name, Map<String, Integer> data2id) throws IOException {
        int count = 0;
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            data2id.put(split_data[0], Integer.valueOf(split_data[1]));
            count++;
        }
        return count;
    }


    private static void Read_Data2(String file_name, Map<Integer, Integer> data2id) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            data2id.put(Integer.valueOf(split_data[0]), Integer.valueOf(split_data[1]));
        }
    }

    private static void GlobalValueInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        type2id = new HashMap<>();
        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        left_num = new HashMap<>();
        right_num = new HashMap<>();
        instanceOf2id= new HashMap<>();
      subClassOf2id = new HashMap<>();
        concept_instance= new MultivaluedHashMap<>();
        up_sub_concept= new MultivaluedHashMap<>();
    }

    private static void prepare() throws IOException {
        GlobalValueInit();

//        entity_num = Read_Data("./type/resource/data/FB15k/entity2id.txt",
//				entity2id);
//		relation_num = Read_Data("./type/resource/data/FB15k/relation2id.txt",
//				relation2id);
//		type_num = Read_Data("./type/resource/data/FB15k/type2id.txt", type2id);
//
//		File f = new File("./type/resource/data/FB15k/train.txt");
//		File f1 = new File("./type/resource/data/FB15k_Entity_Types/FB15k_Entity_Type_train10.txt");


        entity_num = Read_Data("./type/resource/data/YAGO/YAGO43k/ent2id.txt",
				entity2id);
		relation_num = Read_Data("./type/resource/data/YAGO/YAGO43k/rel2id.txt",
				relation2id);
		type_num = Read_Data("./type/resource/data/YAGO/YAGO43k/type2id.txt", type2id);

		File f = new File("./type/resource/data/YAGO/YAGO43k/train.txt");
		File f1 = new File("./type/resource/data/YAGO/YAGO43k_Entity_Types/YAGO43k_Entity_Type_train10.txt");

        Read_Data2("./type/ontology/yago/YAGO39K/Train/instanceOf2id.txt",
                instanceOf2id);//[instance_id concept_id]

        Read_Data2("./type/ontology/yago/YAGO39K/Train/subClassOf2id.txt",
                subClassOf2id);//[sub_concept_id concept_id]--<sub,parent>

        for(int instance : instanceOf2id.keySet()){
            concept_instance.add(instanceOf2id.get(instance),instance);
        }

        for(int concept : subClassOf2id.keySet()){
            up_sub_concept.add(subClassOf2id.get(concept),concept);
        }
		
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
    }

    public static void train_run() throws IOException {
        int nepoch = 800; //循环次数
        if (method == 0) {
            version = "unif";
        }
        System.out.printf("iteration times = %s\n", nepoch);
        train = new Train();
        prepare();
        train.run(nepoch);
    }


    //------------------------------
    private static void prepare_trustE() throws IOException {
        GlobalValueInit();
        entity_num = Read_Data("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k\\entity2id.txt", entity2id);
        relation_num = Read_Data("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k\\relation2id.txt", relation2id);
        type_num = Read_Data("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k\\type2id.txt", type2id);
//        entity_num = Read_Data("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k\\entity2id.txt", entity2id, id2entity);
//        relation_num = Read_Data("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k\\relation2id.txt", relation2id, id2relation);
//        type_num = Read_Data("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k\\type2id.txt", type2id, id2type);
        File f = new File("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k\\train.txt");
        File f1 = new File("D:\\eclipse-workspace\\TrustE-master\\TrustE-master\\resource\\data\\FB15k_Entity_Types\\FB15k_Entity_Type_train20.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), "UTF-8"));
        String line;
        int k = 0;


        while ((line = reader.readLine()) != null) {   //逐行读取训练集
//            System.out.println(line);

            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            String relation = split_data[2];
//            System.out.println(relation);
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss head entity: %s\n", head);
                continue;
            }
            if (!entity2id.containsKey(tail)) {
                System.out.printf("miss tail entity: %s\n", tail);
                continue;
            }
            if (!relation2id.containsKey(relation)) {
                relation2id.put(relation, relation_num);
                relation_num++;
            }

            if (!left_entity.containsKey(relation2id.get(relation))) {
                left_entity.put(relation2id.get(relation), new HashMap<>());
            }

            Map<Integer, Integer> value = left_entity.get(relation2id.get(relation));   //返回左实体中的relation对应的头部实体
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
            train.add(entity2id.get(head), relation2id.get(relation), entity2id.get(tail), k++);    //把triple添加到各种东西中去
        }
        while ((line = reader1.readLine()) != null) {   //开始读noisy的训练集tuple部分
//            System.out.println(line);
            String[] split_data = line.split("\t");
            String heade = split_data[0];
            String tailet = split_data[1];
            if (!entity2id.containsKey(heade)) {
                System.out.printf("miss entity: %s\n", heade);
                continue;
            }
            if (!type2id.containsKey(tailet)) {
                System.out.printf("miss type: %s\n", tailet);
                continue;
            }
            train.add2(entity2id.get(heade), type2id.get(tailet));   //该往哪加往哪加
        }
        for (int i = 0; i < relation_num; i++) {
            int count = 0;
            double sum = 0;
            Map<Integer, Integer> value = left_entity.get(i);
            for (int id : value.keySet()) {
                count++;
                sum += value.get(id);
            }
            left_num.put(i, sum / count);  // 每个关系中的每个头实体平均出现的次数
            count = 0;
            sum = 0;
            value = right_entity.get(i);
            for (int id : value.keySet()) {
                count++;
                sum += value.get(id);
            }
            right_num.put(i, sum / count);  // 每个关系中的每个尾实体平均出现的次数
        }
        System.out.printf("entity number = %s\n", entity_num);   //疯狂打印
        System.out.printf("relation number = %s\n", relation_num);
        System.out.printf("type number = %s\n", type_num);
    }

    public static void train_run_trustE() throws IOException {
        int nepoch = 800;   //循环次数
        int nbatches = 400;   //他这个和别人不一样，别人的batch是“在更新各种向量矩阵之前要学习的样本数”，他是想要把总体训练集划分成的组数
        if (method == 0) {
            version = "unif";   //
        }
        System.out.printf("iteration times = %s\n", nepoch);
        System.out.printf("nbatches = %s\n", nbatches);
        train = new Train();
        prepare_trustE();
        train.run(nepoch, nbatches);
    }
//----------------------------------


}
