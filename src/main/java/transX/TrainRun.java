package transX;

import transX.PCRA_Program.PCRA;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static transX.GlobalValue.*;
import static prediction_type.Preprocess.*;
public class TrainRun {
    private static Train train;

    private static void GlobalValueInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        id2relation = new HashMap<>();
        id2entity = new HashMap<>();
        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        left_num = new HashMap<>();
        right_num = new HashMap<>();

    }
    private void prepare() throws IOException {
        GlobalValueInit();
        entity_num = Read_Data("resource/data/entity2id.txt", entity2id, id2entity);
        relation_num = Read_Data("resource/data/relation2id.txt", relation2id, id2relation);

        File f = new File("resource/data/train.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
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

            train.add(entity2id.get(head), relation2id.get(relation), entity2id.get(tail));
        }
        for (int i = 0; i < relation_num; i++) {
            int count = 0;
            double sum = 0;
            Map<Integer, Integer> value = left_entity.get(i);
            for (int id : value.keySet()) {
                count++;
                sum += value.get(id);
            }
            left_num.put(i, sum / count); // 每个关系中的每个头实体平均出现的次数

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
    }
    private void prepare_PTransE() throws IOException {
        GlobalValueInit();
        //---------------
        path_confidence = new HashMap<>();//----PTransE

        entity_num = Read_Data("resource/data/entity2id.txt", entity2id, id2entity);
        relation_num = Read_Data("resource/data/relation2id.txt", relation2id, id2relation);

        File f = new File("resource/path_data/train_prob.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split(" ");
            int head_id = entity2id.get(split_data[0]);
            int tail_id = entity2id.get(split_data[1]);
            int relation_id = Integer.valueOf(split_data[2]);

            String[] path_info = reader.readLine().split(" ");
            List<Pair<List<Integer>, Double>> path2prob_list = new ArrayList<>();
            for (int i = 1; i < path_info.length; ) {
                int path_length = Integer.valueOf(path_info[i]);
                List<Integer> relation_id_list = new ArrayList<>();
                for (int j = 1; j <= path_length; j++) {
                    relation_id_list.add(Integer.valueOf(path_info[i + j]));
                }
                double prob = Double.valueOf(path_info[i + path_length + 1]);
                Pair<List<Integer>, Double> path2prob = new Pair<>(relation_id_list, prob);
                path2prob_list.add(path2prob);

                i += path_length + 2;
            }
            train.add(head_id, relation_id, tail_id, path2prob_list);
        }

        f = new File("resource/path_data/confident.txt");
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        while ((line = reader.readLine()) != null) {
            String[] line_split = line.split(" ");
            StringBuilder path = new StringBuilder();
            for (int i = 1; i < line_split.length; i++) {
                if (path.length() > 0) path.append(" ");
                path.append(line_split[i]);
            }

            String[] path_info = reader.readLine().split(" ");
            for (int i = 1; i < path_info.length; i += 2) {
                int relation_id = Integer.valueOf(path_info[i]);
                double prob = Double.valueOf(path_info[i + 1]);

                Pair<String, Integer> path2relation = new Pair<>(path.toString(), relation_id);
                path_confidence.put(path2relation, prob);
            }
        }

        System.out.printf("entity number = %s\n", entity_num);
        System.out.printf("relation number = %s\n", relation_num);
    }
    public void train_run() throws IOException {
        System.out.printf("iteration times = %s\n", nepoch);
        System.out.printf("nbatches = %s\n", nbatches);
        train = new Train();

        if (EmbeddingAlgorithm == 2){
            PCRA_run();
            prepare_PTransE();
        }
        else
            prepare();

        train.run();
    }

    private static void PCRA_run() throws IOException {
        File f = new File("resource/path_data/confident.txt");
        if (!f.exists()) {
            PCRA pcra = new PCRA();
            pcra.run();
        }
    }

}
