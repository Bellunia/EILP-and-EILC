package prediction_type.transC;

import prediction_type.ConfE.Tri;

import java.io.*;
import java.util.*;

import static prediction_type.ConfE.Utils.*;
import static prediction_type.transC.Parameter.*;
import static prediction_type.transC.Parameter.relation_num;

public class test_link_prediction {

    float[] entityVec = new float[entity_num * vector_len];
    float[] relationVec = new float[relation_num * vector_len];
    float[] entityRelVec = new float[entity_num * relation_num * vector_len];
    float[] matrix = new float[relation_num * vector_len * vector_len];

    int testTotal = 0;
    int tripleTotal = 0;
    int trainTotal = 0;
    int validTotal = 0;
    int threads = 8;

    List<Tri> testList = new ArrayList<>();
    List<Tri> trainList = new ArrayList<>();
    List<Tri> validList = new ArrayList<>();
    List<Tri> tripleList = new ArrayList<>();


    public void main(String[] args) throws IOException {
        init();
        prepare();
          test();
    }



    private void init() throws IOException {

        Read_Data("./type/ontology/yago/YAGO39K/Train/relation2id.txt", relation2id, relation_num);
        Read_Data("./type/ontology/yago/YAGO39K/Train/instance2id.txt", instance2id, instance_num);//--entity


        Read_Triple("./type/ontology/yago/YAGO39K/Test/triple2id_positive.txt", testList, testTotal);
        Read_Triple("./type/ontology/yago/YAGO39K/Train/triple2id.txt", trainList, trainTotal);
        Read_Triple("./type/ontology/yago/YAGO39K/Valid/triple2id_positive.txt", validList, validTotal);

        tripleTotal = testTotal + trainTotal + validTotal;
        tripleList.addAll(testList);
        tripleList.addAll(trainList);
        tripleList.addAll(validList);

        tripleList.sort(new Comparator<Tri>() {
            @Override
            public int compare(Tri a, Tri b) {
                if ((a.h < b.h) || (a.h == b.h && a.r < b.r) || (a.h == b.h && a.r == b.r && a.t < b.t))
                    return 1;
                else
                    return -1;
            }
        });

    }

    private void prepare() throws IOException {
        entity_vec = new double[entity_num][vector_len];
        concept_vec = new double[concept_num][concept_vector_len];
        //relationTotal=relation_num
        //entityTotal=instance_num

        Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../entity2vec.vec", entity_vec, vector_len);
        Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../concept2vec.vec", concept_vec, concept_vector_len);

        Read_Vec_File("./type/ontology/yago/YAGO39K/vector/.../relation2vec.vec", relation_vec, vector_len);


        for (int i = 0; i < entity_num; i++) {
            int last = i * vector_len;
            for (int j = 0; j < vector_len; j++)
                entityVec[last + j] = (float) entity_vec[i][j];
        }

        for (int i = 0; i < relation_num; i++) {
            int last = i * vector_len;
            for (int j = 0; j < vector_len; j++)
                relationVec[last + j] = (float) relation_vec[i][j];
        }


        for (int i = 0; i < entity_num; i++)
            for (int j = 0; j < relation_num; j++) {
                int last = i * vector_len * relation_num + j * vector_len;
                for (int k = 0; k < vector_len; k++)
                    entityRelVec[last + k] = entityVec[i * vector_len + k];
            }
    }

    float calcSum(int e1, int e2, int rel) {
        float res = 0;
        int last1 = e1 * relation_num * vector_len + rel * vector_len;
        int last2 = e2 * relation_num * vector_len + rel * vector_len;
        int lastr = rel * vector_len;
        for (int i = 0; i < vector_len; i++)
            res += abs(entityRelVec[last1 + i] + relationVec[lastr + i] - entityRelVec[last2 + i]);
        return res;
    }

    private Boolean find(int h, int t, int r) {
        int lef = 0;
        int rig = tripleTotal - 1;
        int mid = 0;
        while (lef + 1 < rig) {
            mid = ((lef + rig) >> 1);
            if ((tripleList.get(mid).h < h)
                    || (tripleList.get(mid).h == h && tripleList.get(mid).r < r)
                    || (tripleList.get(mid).h == h && tripleList.get(mid).r == r && tripleList.get(mid).t < t))
                lef = mid;
            else
                rig = mid;
        }
        if (tripleList.get(lef).h == h && tripleList.get(lef).r == r && tripleList.get(lef).t == t) return true;
        if (tripleList.get(rig).h == h && tripleList.get(rig).r == r && tripleList.get(rig).t == t) return true;
        return false;
    }

    private void test() {


        float[] l_filter_tot = new float[threads];
        float[] r_filter_tot = new float[threads];
        float[] l_filter_tot1 = new float[threads];
        float[] r_filter_tot1 = new float[threads];
        float[] l_filter_tot3 = new float[threads];
        float[] r_filter_tot3 = new float[threads];
        float[] l_filter_tot5 = new float[threads];
        float[] r_filter_tot5 = new float[threads];

        float[] l_tot = new float[threads];
        float[] r_tot = new float[threads];
        float[] l_tot1 = new float[threads];
        float[] r_tot1 = new float[threads];
        float[] l_tot3 = new float[threads];
        float[] r_tot3 = new float[threads];
        float[] l_tot5 = new float[threads];
        float[] r_tot5 = new float[threads];

        float[] l_filter_rank = new float[threads];
        float[] r_filter_rank = new float[threads];
        float[] l_filter_rank_dao = new float[threads];
        float[] r_filter_rank_dao = new float[threads];

        float[] l_rank = new float[threads];
        float[] r_rank = new float[threads];
        float[] l_rank_dao = new float[threads];
        float[] r_rank_dao = new float[threads];

        float[][] lft = new float[10][1500];
        float[][] rft = new float[10][1500];


        for (int a = 1; a < threads; a++) {
            l_filter_tot[a] += l_filter_tot[a - 1];
            r_filter_tot[a] += r_filter_tot[a - 1];
            l_filter_tot1[a] += l_filter_tot1[a - 1];
            r_filter_tot1[a] += r_filter_tot1[a - 1];
            l_filter_tot3[a] += l_filter_tot3[a - 1];
            r_filter_tot3[a] += r_filter_tot3[a - 1];
            l_filter_tot5[a] += l_filter_tot5[a - 1];
            r_filter_tot5[a] += r_filter_tot5[a - 1];

            for (int j = 0; j < relation_num; ++j) {
                lft[a][j] += lft[a - 1][j];
                rft[a][j] += rft[a - 1][j];
            }
            l_tot[a] += l_tot[a - 1];
            r_tot[a] += r_tot[a - 1];
            l_tot1[a] += l_tot1[a - 1];
            r_tot1[a] += r_tot1[a - 1];
            l_tot3[a] += l_tot3[a - 1];
            r_tot3[a] += r_tot3[a - 1];
            l_tot5[a] += l_tot5[a - 1];
            r_tot5[a] += r_tot5[a - 1];

            l_filter_rank[a] += l_filter_rank[a - 1];
            r_filter_rank[a] += r_filter_rank[a - 1];
            l_rank[a] += l_rank[a - 1];
            r_rank[a] += r_rank[a - 1];

            l_filter_rank_dao[a] += l_filter_rank_dao[a - 1];
            r_filter_rank_dao[a] += r_filter_rank_dao[a - 1];
            l_rank_dao[a] += l_rank_dao[a - 1];
            r_rank_dao[a] += r_rank_dao[a - 1];
        }

        System.out.println("metric:\t\t\t MRR \t\t MR \t\t hit@10 \t hit@5  \t hit@3  \t hit@1 \n");
        System.out.println("averaged(raw):\t\t %.3f \t %.1f \t %.3f \t %.3f \t %.3f \t %.3f \n" +
                (l_rank_dao[threads - 1] / testTotal + r_rank_dao[threads - 1] / testTotal) / 2 + "\t" +
                (l_rank[threads - 1] / testTotal + r_rank[threads - 1] / testTotal) / 2 + "\t" +
                (l_tot[threads - 1] / testTotal + r_tot[threads - 1] / testTotal) / 2 + "\t" +
                (l_tot5[threads - 1] / testTotal + r_tot5[threads - 1] / testTotal) / 2 + "\t" +
                (l_tot3[threads - 1] / testTotal + r_tot3[threads - 1] / testTotal) / 2 + "\t" +
                (l_tot1[threads - 1] / testTotal + r_tot1[threads - 1] / testTotal) / 2);
        System.out.println("averaged(filter):\t %.3f \t %.1f \t %.3f \t %.3f \t %.3f \t %.3f \n" + "\t" +
                (l_filter_rank_dao[threads - 1] / testTotal + r_filter_rank_dao[threads - 1] / testTotal) / 2 + "\t" +
                (l_filter_rank[threads - 1] / testTotal + r_filter_rank[threads - 1] / testTotal) / 2 + "\t" +
                (l_filter_tot[threads - 1] / testTotal + r_filter_tot[threads - 1] / testTotal) / 2 + "\t" +
                (l_filter_tot5[threads - 1] / testTotal + r_filter_tot5[threads - 1] / testTotal) / 2 + "\t" +
                (l_filter_tot3[threads - 1] / testTotal + r_filter_tot3[threads - 1] / testTotal) / 2 + "\t" +
                (l_filter_tot1[threads - 1] / testTotal + r_filter_tot1[threads - 1] / testTotal) / 2);
    }

}
