package transX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import static transX.GlobalValue.*;
import static transX.GlobalValue.entity_vec;
import static transX.Gradient.train_kb;
import static transX.Gradient.train_path;
import static transX.Utils.*;
import static prediction_type.Preprocess.*;
public class Train {

    // region private members
    private double res; //loss function value
    private List<Integer> fb_h;
    private List<Integer> fb_l;
    private List<Integer> fb_r;
    private Map<Pair<Integer, Integer>, Set<Integer>> head_relation2tail; // to save the (h, r, t)
    // endregion

    private List<List<Pair<List<Integer>, Double>>> fb_path2prob;//---PTransE

    public Train() {
        fb_h = new ArrayList<>();
        fb_l = new ArrayList<>();
        fb_r = new ArrayList<>();
        head_relation2tail = new HashMap<>();
        fb_path2prob = new ArrayList<>();//---PTransE
    }


    //----------------------PTransE

    private int random_tail(int pos, int neg_pos) {
        // 随机替换尾实体
        Pair<Integer, Integer> key = new Pair<>(fb_h.get(pos), fb_r.get(pos));
        Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
        while (values.contains(neg_pos)) {
            neg_pos = rand_max(entity_num);
        }
        return neg_pos;
    }

    private int random_head(int pos, int neg_pos) {
        // 随机替换头实体
        Pair<Integer, Integer> key = new Pair<>(neg_pos, fb_r.get(pos));
        Set<Integer> values = head_relation2tail.get(key);
        if (values != null) {
            while (values.contains(fb_l.get(pos))) {
                neg_pos = rand_max(entity_num);
                key = new Pair<>(neg_pos, fb_r.get(pos));
                values = head_relation2tail.get(key);
                if (values == null) break;
            }
        }
        return neg_pos;
    }

    private int random_relation(int pos, int neg_pos) {
        // 随机替换关系
        Pair<Integer, Integer> key = new Pair<>(fb_h.get(pos), neg_pos);
        Set<Integer> values = head_relation2tail.get(key);
        if (values != null) {
            while (values.contains(fb_l.get(pos))) {
                neg_pos = rand_max(relation_num);
                key = new Pair<>(fb_h.get(pos), neg_pos);
                values = head_relation2tail.get(key);
                if (values == null) break;
            }
        }
        return neg_pos;
    }

    private void bfgs_PTransE() throws IOException {
        int batchsize = fb_h.size() / nbatches;
        System.out.printf("Batch size = %s\n", batchsize);
        for (int epoch = 0; epoch < nepoch; epoch++) {
            // region private members
            //loss function value
            res = 0;
            for (int batch = 0; batch < nbatches; batch++) {
                for (int k = 0; k < batchsize; k++) {

                    int pos = rand_max(fb_h.size());  // 随机选取一行三元组, 行号
                    int tmp_rand = rand() % 100;
                    if (tmp_rand < 25) {
                        int tail_neg = rand_max(entity_num);
                        tail_neg = random_tail(pos, tail_neg);
                        res = train_kb(fb_h.get(pos), fb_l.get(pos), fb_r.get(pos), fb_h.get(pos), tail_neg, fb_r.get(pos));

                        norm(entity_vec[tail_neg]);
                    } else if (tmp_rand < 50) {
                        int head_neg = rand_max(entity_num);
                        head_neg = random_head(pos, head_neg);
                        res = train_kb(fb_h.get(pos), fb_l.get(pos), fb_r.get(pos), head_neg, fb_l.get(pos), fb_r.get(pos));

                        norm(entity_vec[head_neg]);
                    } else {
                        int relation_neg = rand_max(relation_num);
                        relation_neg = random_relation(pos, relation_neg); // 若某一对实体之间存在所有的关系，则陷入死循环
                        res = train_kb(fb_h.get(pos), fb_l.get(pos), fb_r.get(pos), fb_h.get(pos), fb_l.get(pos), relation_neg);

                        norm(relation_vec[relation_neg]);
                    }
                    update_relation(pos);
                    norm(relation_vec[fb_r.get(pos)]);
                    norm(entity_vec[fb_h.get(pos)]);
                    norm(entity_vec[fb_l.get(pos)]);
                }
            }
            System.out.printf("epoch: %s %s\n", epoch, res);
        }
        Write_Vec2File("resource/result/relation2vec.txt", relation_vec, relation_num);
        Write_Vec2File("resource/result/entity2vec.txt", entity_vec, entity_num);
    }

    private void update_relation(int pos) {
        int relation_neg = rand_max(relation_num);
        relation_neg = random_relation(pos, relation_neg);

        List<Pair<List<Integer>, Double>> path2prob_list = fb_path2prob.get(pos);
        for (Pair<List<Integer>, Double> path2prob: path2prob_list) {
            List<Integer> path = path2prob.a;
            double prob = path2prob.b;

            StringBuilder str = new StringBuilder();
            for (int path_id: path) {
                if (str.length() > 0) str.append(" ");
                str.append(path_id);
            }

            Pair<String, Integer> tmp_path2rel = new Pair<>(str.toString(), fb_r.get(pos));
            double tmp_confidence = 0;
            if (path_confidence.containsKey(tmp_path2rel)) {
                tmp_confidence = path_confidence.get(tmp_path2rel);
            }
            tmp_confidence = (0.99 * tmp_confidence + 0.01) * prob;
            train_path(fb_r.get(pos), relation_neg, path, tmp_confidence, res);
        }
    }
    public void add(int head, int relation, int tail, List<Pair<List<Integer>, Double>> path2prob_list) {
        fb_h.add(head);
        fb_r.add(relation);
        fb_l.add(tail);
        fb_path2prob.add(path2prob_list);

        Pair<Integer, Integer> key = new Pair<>(head, relation);
        if (!head_relation2tail.containsKey(key)) {
            head_relation2tail.put(key, new HashSet<>());
        }
        Set<Integer> tail_set = head_relation2tail.get(key);
        tail_set.add(tail);
    }


    //----------------------PTransE

    private void bfgs() throws IOException {  // 随机梯度下降---transE
        int batchsize = fb_h.size() / nbatches;
        System.out.printf("Batch size = %s\n", batchsize);
        for (int epoch = 0; epoch < nepoch; epoch++) {
            res = 0;  // means the total loss in each epoch
            for (int batch = 0; batch < nbatches; batch++) {
                for (int k = 0; k < batchsize; k++) {
                    int i = rand_max(fb_h.size());
                    int j = rand_max(entity_num);
                    int relation_id = fb_r.get(i);
                    double pr = 1000 * right_num.get(relation_id) / (right_num.get(relation_id) + left_num.get(relation_id));
                    if (method == 0) {
                        pr = 500;
                    }
                    if (rand() % 1000 < pr) {// 替换头实体
                        Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
                        while (values.contains(j)) {
                            j = rand_max(entity_num);
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    } else {
                        Pair<Integer, Integer> key = new Pair<>(j, fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);
                        if (values != null) {
                            while (values.contains(fb_l.get(i))) {
                                j = rand_max(entity_num);
                                key = new Pair<>(j, fb_r.get(i));
                                values = head_relation2tail.get(key);
                                if (values == null) break;
                            }
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    }

                    norm(entity_vec[fb_h.get(i)]);
                    norm(entity_vec[fb_l.get(i)]);
                    norm(entity_vec[j]);

                    norm(relation_vec[fb_r.get(i)]);//TRANSE
                }
            }
            System.out.printf("epoch: %s %s\n", epoch, res);
        }
        Write_Vec2File("resource/result/relation2vec." + version, relation_vec, relation_num);
        Write_Vec2File("resource/result/entity2vec." + version, entity_vec, entity_num);
    }

    private void bfgs(int algorithm) throws IOException {  // 随机梯度下降---transE
        int batchsize = fb_h.size() / nbatches;
        System.out.printf("Batch size = %s\n", batchsize);
        for (int epoch = 0; epoch < nepoch; epoch++) {
            res = 0;  // means the total loss in each epoch
            for (int batch = 0; batch < nbatches; batch++) {
                for (int k = 0; k < batchsize; k++) {
                    int i = rand_max(fb_h.size());
                    int j = rand_max(entity_num);
                    int relation_id = fb_r.get(i);
                    double pr = 1000 * right_num.get(relation_id) / (right_num.get(relation_id) + left_num.get(relation_id));
                    if (method == 0) {
                        pr = 500;
                    }
                    if (rand() % 1000 < pr) {// 替换头实体
                        Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
                        while (values.contains(j)) {
                            j = rand_max(entity_num);
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    } else {
                        Pair<Integer, Integer> key = new Pair<>(j, fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);
                        if (values != null) {
                            while (values.contains(fb_l.get(i))) {
                                j = rand_max(entity_num);
                                key = new Pair<>(j, fb_r.get(i));
                                values = head_relation2tail.get(key);
                                if (values == null) break;
                            }
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    }
                    norm(entity_vec[fb_h.get(i)]);
                    norm(entity_vec[fb_l.get(i)]);
                    norm(entity_vec[j]);


                    if (algorithm == 1)//transE
                    {
                        norm(relation_vec[fb_r.get(i)]);//TRANSE

                    } else//transH
                    {
                        //   norm(entity_vec[fb_h.get(i)]);
                        //   norm(entity_vec[fb_l.get(i)]);
                        //   norm(entity_vec[j]);

                        norm(entity_vec[fb_h.get(i)], Wr_vec_transH[fb_r.get(i)]);
                        norm(entity_vec[fb_l.get(i)], Wr_vec_transH[fb_r.get(i)]);
                        norm(entity_vec[j], Wr_vec_transH[fb_r.get(i)]);
                    }
                }
            }
            System.out.printf("epoch: %s %s\n", epoch, res);
        }

        Write_Vec2File("resource/result/relation2vec." + version, relation_vec, relation_num);
        Write_Vec2File("resource/result/entity2vec." + version, entity_vec, entity_num);

        if (algorithm == 3)  {//transH
            Write_Vec2File("resource/result/Wr_vec." + version, Wr_vec_transH, relation_num);
        }
    }


    // region public members & methods

    public void add(int head, int relation, int tail) {
        fb_h.add(head);
        fb_r.add(relation);
        fb_l.add(tail);
        Pair<Integer, Integer> key = new Pair<>(head, relation);
        if (!head_relation2tail.containsKey(key)) {
            head_relation2tail.put(key, new HashSet<>());
        }
        Set<Integer> tail_set = head_relation2tail.get(key);
        tail_set.add(tail);
    }

    public void run() throws IOException {//1200,150
        relation_vec = new double[relation_num][vector_len];//43,100
        entity_vec = new double[entity_num][vector_len];//89,100

//----TransR
        relation_copy = new double[relation_num][vector_len];
        entity_copy = new double[entity_num][vector_len];

        //----TransR

        for (int i = 0; i < relation_num; i++) {//43
            for (int j = 0; j < vector_len; j++) {//100
                relation_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));//transE

                if(EmbeddingAlgorithm==3 || EmbeddingAlgorithm==4) {
                    relation_vec[i][j] = uniform(-1, 1);
                    relation_copy[i][j] = relation_vec[i][j];
                }
            }
        }
        for (int i = 0; i < entity_num; i++) {
            for (int j = 0; j < vector_len; j++) {
                entity_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
                if(EmbeddingAlgorithm==3 || EmbeddingAlgorithm==4) {
                    entity_vec[i][j] = uniform(-1, 1);
                    entity_copy[i][j] = entity_vec[i][j];
                }
            }
            norm(entity_vec[i]);
        }

        if(EmbeddingAlgorithm==1) {
         //   bfgs(nepoch, nbatches);//transE
            bfgs( 1);
        }
        if(EmbeddingAlgorithm==2) {
            bfgs_PTransE();//PTransE
        }
        if(EmbeddingAlgorithm==3) {//TransH
            Wr_vec_transH = new double[relation_num][vector_len];
            for (int i = 0; i < relation_num; i++) {
                for (int j = 0; j < vector_len; j++) {
                    Wr_vec_transH[i][j] = uniform(-1, 1);
                }
                norm2one(Wr_vec_transH[i]);
            }
            bfgs( 3);

        }
        if(EmbeddingAlgorithm==4) {//TransR--//relation_dimension= vector_len

            Wr_vec_transR = new double[relation_num][entity_dimension][vector_len];
            Wr_transR_copy = new double[relation_num][entity_dimension][vector_len];
            for (int i = 0; i < relation_num; i++) {
                for (int j = 0; j < entity_dimension; j++) {
                    for (int k = 0; k < vector_len; k++) {
                        Wr_vec_transR[i][j][k] = (j == k) ? 1 : 0;
                        Wr_transR_copy[i][j][k] = (j == k) ? 1 : 0;
                    }
                }
            }

            bfgs_TransR();
        }


    }
    // endregion


    private void Write_Vec(String file_name, double[][] vec, int number) throws IOException {
        File f = new File(file_name);
        if (!f.exists()) {	// if file does not exist, then create it
            File dir = new File(f.getParent());
            dir.mkdirs();
            f.createNewFile();
        }
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < relation_dimension; j++) {
                String str = String.format("%.6f\t", vec[i][j]);
                writer.write(str);
            }
            writer.write("\n");
            writer.flush();
        }
    }

    private void Write_Wr(String file_name, double[][][] vec, int number) throws IOException {
        File f = new File(file_name);
        if (!f.exists()) {	// if file does not exist, then create it
            File dir = new File(f.getParent());
            dir.mkdirs();
            f.createNewFile();
        }
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < entity_dimension; j++) {
                for (int k = 0; k < relation_dimension; k++) {
                    String str = String.format("%.6f\t", vec[i][j][k]);
                    writer.write(str);
                }
                writer.write("\n");
                writer.flush();
            }
        }
    }


    private void bfgs_TransR() throws IOException {
        int batchsize = fb_h.size() / nbatches;
        System.out.printf("Batch size = %s\n", batchsize);
        for (int epoch = 0; epoch < nepoch; epoch++) {
            res = 0;  // means the total loss in each epoch
            for (int batch = 0; batch < nbatches; batch++) {
                for (int k = 0; k < batchsize; k++) {
                    int i = rand_max(fb_h.size());
                    int j = rand_max(entity_num);
                    int relation_id = fb_r.get(i);
                    double pr = 1000 * right_num.get(relation_id) / (right_num.get(relation_id) + left_num.get(relation_id));
                    if (method == 0) {
                        pr = 500;
                    }
                    if (rand() % 1000 < pr) {  // 替换头实体
                        Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
                        while (values.contains(j)) {
                            j = rand_max(entity_num);
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    } else {  // 替换尾实体
                        Pair<Integer, Integer> key = new Pair<>(j, fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);
                        if (values != null) {
                            while (values.contains(fb_l.get(i))) {
                                j = rand_max(entity_num);
                                key = new Pair<>(j, fb_r.get(i));
                                values = head_relation2tail.get(key);
                                if (values == null) break;
                            }
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    }
                    norm_TransR(relation_copy[fb_r.get(i)]);
                    norm_TransR(entity_copy[fb_h.get(i)]);
                    norm_TransR(entity_copy[fb_l.get(i)]);
                    norm_TransR(entity_copy[j]);

                    norm_TransR(entity_copy[fb_h.get(i)], Wr_vec_transR[fb_r.get(i)]);
                    norm_TransR(entity_copy[fb_l.get(i)], Wr_vec_transR[fb_r.get(i)]);
                    norm_TransR(entity_copy[j], Wr_vec_transR[fb_r.get(i)]);
                }
                relation_vec = relation_copy;
                entity_vec = entity_copy;
                Wr_vec_transR = Wr_transR_copy;
            }
            System.out.printf("epoch: %s %s\n", epoch, res);
        }
        Write_Vec("resource/result/relation2vec." + version, relation_vec, relation_num);
        Write_Vec("resource/result/entity2vec." + version, entity_vec, entity_num);
        Write_Wr("resource/result/Wr_vec." + version, Wr_vec_transR, relation_num);
    }

}
