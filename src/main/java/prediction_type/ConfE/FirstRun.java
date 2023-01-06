package prediction_type.ConfE;

import java.io.*;
import java.util.HashMap;
import static prediction_type.Preprocess.*;

import static prediction_type.ConfE.GlobalValue.*;

public class FirstRun {

    private static prediction_type.ConfE.First First;

    private static void GlobalValueInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        type2id = new HashMap<>();
    }

    private static void prepare() throws IOException {
        GlobalValueInit();
        entity_num = Read_Data("ConfE-master\\resource\\data\\FB15k\\entity2id.txt",
				entity2id);
		type_num = Read_Data("ConfE-master\\resource\\data\\FB15k\\type2id.txt", type2id);

        entity_vec = new double[entity_num][vector_len];
        type_vec = new double[type_num][type_vector_len];
        A = new double[type_vector_len][vector_len];

        Read_Vec_File("ConfE-master\\resource\\data\\entity2vec.bern", entity_vec, vector_len);
        Read_Vec_File("ConfE-master\\resource\\data\\type2vec.bern", type_vec, type_vector_len);
        Read_Vec_File("ConfE-master\\resource\\data\\A.bern", A, vector_len);

		File f1 = new File("ConfE-master\\resource\\data\\FB15k_Entity_Types\\FB15k_Entity_Type_train.txt");
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1),"UTF-8"));
        String line;
        int count_true = 0;

        while ((line = reader1.readLine()) != null) {
        	count_true += 1;
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss entity: %s\n", head);
            }
            if (!type2id.containsKey(tail)) {
                System.out.printf("miss type: %s\n", tail);
            }
            First.add1(entity2id.get(head), type2id.get(tail), true);
        }
        
        File f2 = new File("ConfE-master\\resource\\data\\FB15k_Entity_Types\\FB15k_Entity_Type_train10.txt");
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f2),"UTF-8"));
        int count_noise = 0;

        while ((line = reader2.readLine()) != null) {
        	count_noise += 1;
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss entity: %s\n", head);
            }
            if (!type2id.containsKey(tail)) {
                System.out.printf("miss type: %s\n", tail);
            }
            if(count_noise > count_true) {
            	First.add1(entity2id.get(head), type2id.get(tail), false);
            }
        }
        First.count_pass(count_true, count_noise);
    }

    public static void first_run() throws IOException {
        First = new First();
        prepare();
        First.run();
    }

}
