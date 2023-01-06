package transX;

import prediction_type.Preprocess;

import java.io.*;
import java.util.*;

import static transX.GlobalValue.*;
import static transX.Gradient.calc_sum;
import static prediction_type.Preprocess.Read_Vec_File;
import static prediction_type.Preprocess.Read_Wr_File;


public class Test {

    private List<Integer> fb_h;
    private List<Integer> fb_l;
    private List<Integer> fb_r;
    private Map<Pair<Integer, Integer>, Set<Integer>> head_relation2tail; // to save the (h, r, t)

    public Test() {
        fb_h = new ArrayList<>();
        fb_l = new ArrayList<>();
        fb_r = new ArrayList<>();
        head_relation2tail = new HashMap<>();
    }

    public void add(int head, int relation, int tail, boolean flag) {
        /**
         * head_relation2tail用于存放 正确的三元组
         * flag=true 表示该三元组关系正确
         */
        if (flag) {
            Pair<Integer, Integer> key = new Pair<>(head, relation);
            if (!head_relation2tail.containsKey(key)) {
                head_relation2tail.put(key, new HashSet<>());
            }
            Set<Integer> tail_set = head_relation2tail.get(key);
            tail_set.add(tail);
        } else {
            fb_h.add(head);
            fb_r.add(relation);
            fb_l.add(tail);
        }
    }

    private boolean hrt_isvalid(int head, int relation, int tail) {
        /**
         * 如果实体之间已经存在正确关系，则不需要计算距离
         * 如果头实体与尾实体一致，也排除该关系的距离计算
         */
        if (head == tail) {
            return true;
        }
        Pair<Integer, Integer> key = new Pair<>(head, relation);
        Set<Integer> values = head_relation2tail.get(key);
        if (values == null || !values.contains(tail)) {
            return false;
        } else {
            return true;
        }
    }

    public void run() throws IOException {
        relation_vec = new double[relation_num][vector_len];
        entity_vec = new double[entity_num][vector_len];

        Read_Vec_File("TransE/resource/result/relation2vec.bern", relation_vec);
        Read_Vec_File("TransE/resource/result/entity2vec.bern", entity_vec);

        if (EmbeddingAlgorithm == 3) {//TransH
            Wr_vec_transH = new double[relation_num][vector_len];
            Read_Vec_File("resource/result/Wr_vec.bern", Wr_vec_transH);
        }
        if (EmbeddingAlgorithm == 4) {//TransR
            Wr_vec_transR = new double[relation_num][entity_dimension][relation_dimension];
            Read_Wr_File("resource/result/Wr_vec.bern", Wr_vec_transR);
        }

        int head_meanRank_raw = 0, tail_meanRank_raw = 0, head_meanRank_filter = 0, tail_meanRank_filter = 0;  // 在正确三元组之前的匹配距离之和
        int head_hits10 = 0, tail_hits10 = 0, head_hits10_filter = 0, tail_hits10_filter = 0;  // 在正确三元组之前的匹配个数之和
        int relation_meanRank_raw = 0, relation_meanRank_filter = 0;
        int relation_hits10 = 0, relation_hits10_filter = 0;

        // ------------------------ evaluation link predict ----------------------------------------
        System.out.printf("Total test triple = %s\n", fb_l.size());
        System.out.printf("The evaluation of link predict\n");
        for (int id = 0; id < fb_l.size(); id++) {
            int head = fb_h.get(id);
            int tail = fb_l.get(id);
            int relation = fb_r.get(id);
            List<Pair<Integer, Double>> head_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {

                double sum = calc_sum(i, tail, relation);
                head_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(head_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            int filter = 0;  // 统计匹配过程已有的正确三元组个数
            for (int i = 0; i < head_dist.size(); i++) {
                int cur_head = head_dist.get(i).a;

                if (hrt_isvalid(cur_head, relation, tail)) {  // 如果当前三元组是正确三元组，则记录到filter中
                    filter += 1;
                }

                if (cur_head == head) {
                    head_meanRank_raw += i; // 统计小于<h, l, r>距离的数量
                    head_meanRank_filter += i - filter;
                    if (i <= 10) {
                        head_hits10++;
                    }
                    if (i - filter <= 10) {
                        head_hits10_filter++;
                    }
                    break;
                }
            }

            filter = 0;
            List<Pair<Integer, Double>> tail_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {
                double sum = calc_sum(head, i, relation);
                tail_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(tail_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            for (int i = 0; i < tail_dist.size(); i++) {
                int cur_tail = tail_dist.get(i).a;
                if (hrt_isvalid(head, relation, cur_tail)) {
                    filter++;
                }
                if (cur_tail == tail) {
                    tail_meanRank_raw += i;
                    tail_meanRank_filter += i - filter;
                    if (i <= 10) {
                        tail_hits10++;
                    }
                    if (i - filter <= 10) {
                        tail_hits10_filter++;
                    }
                    break;
                }
            }
        }
        System.out.printf("-----head prediction------\n");
        System.out.printf("Raw MeanRank: %.3f,  Filter MeanRank: %.3f\n",
                (head_meanRank_raw * 1.0) / fb_l.size(), (head_meanRank_filter * 1.0) / fb_l.size());
        System.out.printf("Raw Hits@10: %.3f,  Filter Hits@10: %.3f\n",
                (head_hits10 * 1.0) / fb_l.size(), (head_hits10_filter * 1.0) / fb_l.size());

        System.out.printf("-----tail prediction------\n");
        System.out.printf("Raw MeanRank: %.3f,  Filter MeanRank: %.3f\n",
                (tail_meanRank_raw * 1.0) / fb_l.size(), (tail_meanRank_filter * 1.0) / fb_l.size());
        System.out.printf("Raw Hits@10: %.3f,  Filter Hits@10: %.3f\n",
                (tail_hits10 * 1.0) / fb_l.size(), (tail_hits10_filter * 1.0) / fb_l.size());

        // ------------------------ evaluation relation-linked predict ----------------------------------------
        int relation_hits = 5;  // 选取hits@5为评价指标
        for (int id = 0; id < fb_l.size(); id++) {
            int head = fb_h.get(id);
            int tail = fb_l.get(id);
            int relation = fb_r.get(id);
            List<Pair<Integer, Double>> relation_dist = new ArrayList<>();
            for (int i = 0; i < relation_num; i++) {
                double sum = calc_sum(head, tail, i);
                relation_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(relation_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            int filter = 0;  // 统计匹配过程已有的正确三元组个数
            for (int i = 0; i < relation_dist.size(); i++) {
                int cur_relation = relation_dist.get(i).a;
                if (hrt_isvalid(head, cur_relation, tail)) {  // 如果当前三元组是正确三元组，则记录到filter中
                    filter += 1;
                }
                if (cur_relation == relation) {
                    relation_meanRank_raw += i; // 统计小于<h, l, r>距离的数量
                    relation_meanRank_filter += i - filter;
                    if (i <= 5) {
                        relation_hits10++;
                    }
                    if (i - filter <= 5) {
                        relation_hits10_filter++;
                    }
                    break;
                }
            }
        }
        System.out.printf("-----relation prediction------\n");
        System.out.printf("Raw MeanRank: %.3f,  Filter MeanRank: %.3f\n",
                (relation_meanRank_raw * 1.0) / fb_r.size(), (relation_meanRank_filter * 1.0) / fb_r.size());
        System.out.printf("Raw Hits@%d: %.3f,  Filter Hits@%d: %.3f\n",
                relation_hits, (relation_hits10 * 1.0) / fb_r.size(),
                relation_hits, (relation_hits10_filter * 1.0) / fb_r.size());
    }

    public void run_PTransE() throws IOException {
        relation_vec = new double[relation_num][vector_len];
        entity_vec = new double[entity_num][vector_len];

        Read_Vec_File("resource/result/relation2vec.txt", relation_vec);
        Read_Vec_File("resource/result/entity2vec.txt", entity_vec);

        int lsum = 0, rsum = 0;
        int lp_n = 0, rp_n = 0;
        Map<Integer, Integer> lsum_r = new HashMap<>();
        Map<Integer, Integer> rsum_r = new HashMap<>();
        Map<Integer, Integer> lp_n_r = new HashMap<>();
        Map<Integer, Integer> rp_n_r = new HashMap<>();
        Map<Integer, Integer> rel_num = new HashMap<>();

        File out_file = new File("resource/result/output_detail.txt");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(out_file), "UTF-8");

        System.out.printf("Total iterations = %s\n", fb_l.size());
        for (int id = 0; id < fb_l.size(); id++) {
            System.out.println(id);
            int head = fb_h.get(id);
            int tail = fb_l.get(id);
            int relation = fb_r.get(id);
           new Preprocess().relation_add(rel_num, relation);
            List<Pair<Integer, Double>> head_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {
                if (hrt_isvalid(i, relation, tail)) {//// 如果当前三元组是正确三元组，
                    continue;
                }
                double sum = calc_sum(i, tail, relation);
                head_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(head_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            for (int i = 0; i < head_dist.size(); i++) {
                int cur_head = head_dist.get(i).a;

                if (cur_head == head) {
                    lsum += i; // 统计小于<h, l, r>距离的数量---head_meanRank_raw
                    new Preprocess().map_add_value(lsum_r, relation, i);
                    if (i <= 10) {
                        lp_n++;
                        new Preprocess().map_add_value(lp_n_r, relation, 1);
                    }
                    String str = String.format("%s  %s  %s, dist=%f, %d\n\n", id2entity.get(head), id2relation.get(relation),
                            id2entity.get(tail), calc_sum(head, tail, relation), i);
                    writer.write(str);
                    writer.flush();
                    break;
                } else {
                    String temp_str = String.format("%s  %s  %s, dist=%f, %d\n", id2entity.get(cur_head), id2relation.get(relation),
                            id2entity.get(tail), calc_sum(cur_head, tail, relation), i);
                    writer.write(temp_str);
                    writer.flush();
                }
            }

            List<Pair<Integer, Double>> tail_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {
                if (hrt_isvalid(head, relation, i)) {
                    continue;
                }
                double sum = calc_sum(head, i, relation);
                tail_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(tail_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            for (int i = 0; i < tail_dist.size(); i++) {
                int cur_tail = tail_dist.get(i).a;
                if (cur_tail == tail) {
                    rsum += i;
                    new Preprocess().map_add_value(rsum_r, relation, i);
                    if (i <= 10) {
                        rp_n++;
                        new Preprocess().map_add_value(rp_n_r, relation, 1);
                    }
                    break;
                }
            }
        }
        System.out.printf("lsum = %s, tail number = %s\n", lsum, fb_l.size());
        System.out.printf("left: %s\t%s\n", (lsum * 1.0) / fb_l.size(), (lp_n * 1.0) / fb_l.size());
        System.out.printf("right: %s\t%s\n", (rsum * 1.0) / fb_h.size(), (rp_n * 1.0) / fb_h.size());
    }

}
