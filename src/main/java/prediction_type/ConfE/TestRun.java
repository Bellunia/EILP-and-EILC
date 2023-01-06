package prediction_type.ConfE;

import java.io.*;
import java.util.HashMap;

import static prediction_type.Preprocess.*;

import static prediction_type.ConfE.GlobalValue.*;

public class TestRun {

    private static Test test;


    private static void GlobalValueInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        type2id = new HashMap<>();
        id2relation = new HashMap<>();
        id2entity = new HashMap<>();
        id2type = new HashMap<>();
        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        left_num = new HashMap<>();
        right_num = new HashMap<>();
    }

    private static void prepare() throws IOException {
        GlobalValueInit();
   //     entity_num = Read_Data("./type/resource/data/YAGO/YAGO43k/ent2id.txt",
 //               entity2id);
//        relation_num = Read_Data("./type/resource/data/YAGO/YAGO43k/rel2id.txt",
//                relation2id);
    //    type_num = Read_Data("./type/resource/data/YAGO/YAGO43k/type2id.txt", type2id);

    //    File f1 = new File("./type/resource/data/YAGO/YAGO43k_Entity_Types/YAGO43k_Entity_Type_test.txt");

       entity_num = Read_Data("./type/resource/data/FB15k/entity2id.txt",
               entity2id);
        type_num = Read_Data("./type/resource/data/FB15k/type2id.txt", type2id);

        File f1 = new File("./type/resource/data/FB15k_Entity_Types/FB15k_Entity_Type_test.txt");
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), "UTF-8"));
        String line;

        while ((line = reader1.readLine()) != null) {
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss entity: %s\n", head);
            }
            if (!type2id.containsKey(tail)) {
                System.out.printf("miss type: %s\n", tail);
            }
            test.add1(entity2id.get(head), type2id.get(tail), false);
            test.add1(entity2id.get(head), type2id.get(tail), true);
        }

        File f2 = new File("./type/resource/data/FB15k_Entity_Types/FB15k_Entity_Type_train10.txt");
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), "UTF-8"));

        while ((line = reader2.readLine()) != null) {
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss entity: %s\n", head);
            }
            if (!type2id.containsKey(tail)) {
                System.out.printf("miss type: %s\n", tail);
            }
            test.add1(entity2id.get(head), type2id.get(tail), true);
        }

        File f3 = new File("./type/resource/data/FB15k_Entity_Types/FB15k_Entity_Type_valid.txt");
        BufferedReader reader3 = new BufferedReader(new InputStreamReader(new FileInputStream(f1), "UTF-8"));

        while ((line = reader3.readLine()) != null) {
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss entity: %s\n", head);
            }
            if (!type2id.containsKey(tail)) {
                System.out.printf("miss type: %s\n", tail);
            }
            test.add1(entity2id.get(head), type2id.get(tail), true);
        }

        System.out.printf("entity number = %s\n", entity_num);
        System.out.printf("type number = %s\n", type_num);

    }

    public static void test_run() throws IOException {
        test = new Test();
        prepare();
        test.run();
    }

}
