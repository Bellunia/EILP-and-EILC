package prediction_type.ConfE;

import java.io.*;
import java.util.*;

import static prediction_type.ConfE.GlobalValue.*;
import static prediction_type.ConfE.Gradient.calc_sum2;
import static prediction_type.Preprocess.Read_Vec_File;

public class Test {
    // region private members
    private List<Integer> fb_ht;
    private List<Integer> fb_lt;
    private Map<Integer, Set<Integer>> et2type;
    // end region

    Test() {
        fb_ht = new ArrayList<>();
        fb_lt = new ArrayList<>();
        et2type = new HashMap<>();
    }


    public void add1(int head, int tail, boolean flag) {
        /**
         * if type instance is true, we set flag=true
         * if type instance is candidate, we set flag=false
         */
        if (flag) {
            if (!et2type.containsKey(head)) {
                et2type.put(head, new HashSet<>());
            }
            Set<Integer> tail_set = et2type.get(head);
            tail_set.add(tail);
        } else {
            fb_ht.add(head);
            fb_lt.add(tail);
        }
    }



    public void run() throws IOException {
        entity_vec = new double[entity_num][vector_len];
        type_vec = new double[type_num][type_vector_len];
        A = new double[type_vector_len][vector_len];

        Read_Vec_File("./type/resource/data/entity2vec.bern", entity_vec, vector_len);
        Read_Vec_File("./type/resource/data/type2vec.bern", type_vec, type_vector_len);
        Read_Vec_File("./type/resource/data/A.bern", A, vector_len);

        int raw_rank = 1, filter_rank = 1, raw_hit10 = 0, filter_hit10 = 0;
        double raw_mrr = 0, filter_mrr = 0;


        System.out.printf("Total double iterations = %s\n", fb_ht.size());

        for (int id = 0; id < fb_ht.size(); id++) {

            int head = fb_ht.get(id);
            int tail = fb_lt.get(id);
            List<Pair<Integer, Double>> type_dist = new ArrayList<>();
            for (int i = 0; i < type_num; i++) {
                
                double sum = calc_sum2(head, i);
                type_dist.add(new Pair<>(i, sum));
            }
			
            Collections.sort(type_dist, (o1, o2) -> {
            	return o2.b.compareTo(o1.b);
			});
            for (int i = 0; i < type_dist.size(); i++) {
                int cur_type = type_dist.get(i).a;
				
				if (et2type.get(head) != null && et2type.get(head).contains(cur_type)) {
					filter_rank += i;
					filter_mrr += 1 / (i + 1);
					if (i <= 10) {
						filter_hit10++;
					}
					break;
				}

				if (cur_type == tail) {
					raw_rank += i;
					raw_mrr += 1 / (i + 1);
					if (i <= 10) {
						raw_hit10++;
					}
					break;
				}
					 
            }

        }

        System.out.printf("RAW MODE:\tHits@10:\t%s\tMRR:\t%s\n", (raw_hit10 * 1.0) / fb_ht.size(), raw_mrr/fb_ht.size());
        System.out.printf("FILTER MODE:\tHits@10:\t%s\tMRR:\t%s\n", (filter_hit10 * 1.0) / fb_ht.size(), filter_mrr/fb_ht.size());
    }

}
