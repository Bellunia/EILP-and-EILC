package transX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static transX.GlobalValue.*;
import static transX.Utils.*;
import static transX.Utils.norm;

public class Gradient {

    public  static double calc_sum(int e1, int e2, int rel) {
        // 计算头实体、关系与尾实体之间的向量距离
        double sum = 0;
        if(EmbeddingAlgorithm==1)
            sum=calc_sum_transE( e1,  e2,  rel);
        if(EmbeddingAlgorithm==2)
            sum=calc_sum_PTransE( e1,  e2,  rel);
        if(EmbeddingAlgorithm==3)
            sum=calc_sum_TransH( e1,  e2,  rel);
        if(EmbeddingAlgorithm==4)
            sum=calc_sum_TransR( e1,  e2,  rel);
        return sum;
    }


    double calc_sum1(int e1,int e2,int rel)
    {
        //calc sum
        double sum=0;
        if (L1_flag)
            for (int ii=0; ii<vector_len; ii++) {
                //        sum+=-abs(tail_entity_vec[e2][rel][ii]-head_entity_vec[e1][rel][ii]-relation_vec[rel][ii]);
            }
        else
            for (int ii=0; ii<vector_len; ii++) {
                //       sum+=-sqr(tail_entity_vec[e2][rel][ii]-head_entity_vec[e1][rel][ii]-relation_vec[rel][ii]);
            }
        return sum;
    }

    public static double calc_sum_transE(int e1, int e2, int rel) {
        // 计算头实体、关系与尾实体之间的向量距离
        double sum = 0;
        if (L1_flag) {//l1
            for (int i = 0; i < vector_len; i++) {
                sum += abs(entity_vec[e2][i] - entity_vec[e1][i] - relation_vec[rel][i]);
            }
        } else {//l2
            for (int i = 0; i < vector_len; i++) {
                sum += sqr(entity_vec[e2][i] - entity_vec[e1][i] - relation_vec[rel][i]);
            }
        }
        return sum;
    }
    static double calc_sum_PTransE(int e1, int e2, int rel) {
        double sum = 0;
        for (int i = 0; i < vector_len; i++) {
            sum += abs(entity_vec[e2][i] - entity_vec[e1][i] - relation_vec[rel][i]);
        }
        return sum;
    }
    static double calc_sum_TransH(int head, int tail, int relation) {
        double Wrh = 0;
        double Wrt = 0;
        for (int i = 0; i < vector_len; i++) {
            Wrh += Wr_vec_transH[relation][i] * entity_vec[head][i];
            Wrt += Wr_vec_transH[relation][i] * entity_vec[tail][i];
        }

        double sum = 0, tmp;
        for (int i = 0; i < vector_len; i++) {
            tmp = (entity_vec[tail][i] - Wrt * Wr_vec_transH[relation][i])
                    - relation_vec[relation][i]
                    - (entity_vec[head][i] - Wrh * Wr_vec_transH[relation][i]);
            sum += abs(tmp);
        }
        return sum;
    }
    static double calc_sum_TransR(int head, int tail, int relation) {
        double[] e1_vec = new double[relation_dimension];
        double[] e2_vec = new double[relation_dimension];
        for (int i = 0; i < entity_dimension; i++) {
            e1_vec[i] = 0;
            e2_vec[i] = 0;
            for (int j = 0; j < relation_dimension; j++) {
                e1_vec[i] += Wr_vec_transR[relation][i][j] * entity_vec[head][i];
                e2_vec[i] += Wr_vec_transR[relation][i][j] * entity_vec[tail][i];
            }
        }
        double sum = 0, tmp;
        for (int i = 0; i < entity_dimension; i++) {
            tmp = e2_vec[i] - e1_vec[i] - relation_vec[relation][i];
            sum += abs(tmp);
        }
        return sum;
    }

    public static double train_kb(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b) {
        // 极大似然估计的计算过程
        double sum1 = calc_sum(head_a, tail_a, relation_a);
        double sum2 = calc_sum(head_b, tail_b, relation_b);
       double res = 0;
        if (sum1 + margin > sum2) {
            res += margin + sum1 - sum2;
            if(EmbeddingAlgorithm==1) {
                gradient_transE(head_a, tail_a, relation_a, head_b, tail_b, relation_b,learning_rate);
            }
            if(EmbeddingAlgorithm==2) {
                gradient_PTransE(head_a, tail_a, relation_a, -1);
                gradient_PTransE(head_b, tail_b, relation_b, 1);
            }
            if(EmbeddingAlgorithm==3) {
                gradient_TransH(head_a, tail_a, relation_a, -1);
                gradient_TransH(head_b, tail_b, relation_b, 1);
            }
            if(EmbeddingAlgorithm==4) {
                gradient_TransR(head_a, tail_a, relation_a, -1);
                gradient_TransR(head_b, tail_b, relation_b, 1);
            }
        }
        return res;

    }

    static void gradient_transE(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b, double learning_rate) {
        for (int i = 0; i < vector_len; i++) {
            double delta1 = entity_vec[tail_a][i] - entity_vec[head_a][i] - relation_vec[relation_a][i];
            double delta2 = entity_vec[tail_b][i] - entity_vec[head_b][i] - relation_vec[relation_b][i];
            double x;
            if (L1_flag) {
                if (delta1 > 0) {
                    x = 1;
                } else {
                    x = -1;
                }
                relation_vec[relation_a][i] += x * learning_rate;
                entity_vec[head_a][i] += x * learning_rate;
                entity_vec[tail_a][i] -= x * learning_rate;

                if (delta2 > 0) {
                    x = 1;
                } else {
                    x = -1;
                }
                relation_vec[relation_b][i] -= x * learning_rate;
                entity_vec[head_b][i] -= x * learning_rate;
                entity_vec[tail_b][i] += x * learning_rate;
            } else {
                delta1 = abs(delta1);//--ConfE---delete the abs
                delta2 = abs(delta2);//---ConfE---delete the abs
                relation_vec[relation_a][i] += learning_rate * 2 * delta1;
                entity_vec[head_a][i] += learning_rate * 2 * delta1;
                entity_vec[tail_a][i] -= learning_rate * 2 * delta1;

                relation_vec[relation_b][i] -= learning_rate * 2 * delta2;
                entity_vec[head_b][i] -= learning_rate * 2 * delta2;
                entity_vec[tail_b][i] += learning_rate * 2 * delta2;
            }
        }
    }

   //-------------------PTransE
    private static void gradient_PTransE(int head, int tail, int relation, int beta) {
        for (int i = 0; i < vector_len; i++) {
            double delta = entity_vec[tail][i] - entity_vec[head][i] - relation_vec[relation][i];
            double x = (delta > 0) ? 1 : -1;
            relation_vec[relation][i] -= x * learning_rate * beta;
            entity_vec[head][i] -= x * learning_rate * beta;
            entity_vec[tail][i] += x * learning_rate * beta;
        }
    }
    public static double train_path(int relation, int neg_relation, List<Integer> path, double alpha, double loss) {
        double sum1 = calc_path(relation, path);
        double sum2 = calc_path(neg_relation, path);
        if (sum1 + margin_relation > sum2) {
            loss += alpha * (sum1 + margin_relation - sum2);
            gradient_path(relation, path, -1 * alpha);
            gradient_path(neg_relation, path, alpha);
        }
        return loss;
    }
    static private void gradient_path(int relation, List<Integer> path, double beta) {
        /**
         * 相关联的路径和关系之间的空间位置相近，反之疏远
         */
        for (int i = 0; i < vector_len; i++) {
            double x = relation_vec[relation][i];
            for (int path_id: path) {
                x -= relation_vec[path_id][i];
            }
            int flag = (x > 0) ? 1 : -1;
            relation_vec[relation][i] += beta * learning_rate * flag;
            for (int path_id : path) {
                relation_vec[path_id][i] -= beta * learning_rate * flag;
            }
        }
    }
    static private double calc_path(int relation, List<Integer> path) {
        double sum = 0;
        for (int i = 0; i < vector_len; i++) {
            double x = relation_vec[relation][i];
            for (int path_id : path) {
                x -= relation_vec[path_id][i];
            }
            sum += abs(x);
        }
        return sum;
    }
    //-------------------PTransE

    private static void gradient_TransH(int head, int tail, int relation, double beta) {
        double Wrh = 0;
        double Wrt = 0;
        for (int i = 0; i < vector_len; i++) {
            Wrh += Wr_vec_transH[relation][i] * entity_vec[head][i];
            Wrt += Wr_vec_transH[relation][i] * entity_vec[tail][i];
        }

        double sum = 0;
        for (int i = 0; i < vector_len; i++) {
            double delta = (entity_vec[tail][i] - Wrt * Wr_vec_transH[relation][i])
                    - relation_vec[relation][i]
                    - (entity_vec[head][i] - Wrh * Wr_vec_transH[relation][i]);
            double x = (delta > 0) ? 1 : -1;
            sum += x * Wr_vec_transH[relation][i];
            relation_vec[relation][i] -= beta * learning_rate * x;
            entity_vec[head][i] -= beta * learning_rate * x;
            entity_vec[tail][i] += beta * learning_rate * x;
            Wr_vec_transH[relation][i] += beta * x * learning_rate  * (Wrh - Wrt);
        }
        for (int i = 0; i < vector_len; i++) {
            Wr_vec_transH[relation][i] += beta * learning_rate * sum * (entity_vec[head][i] - entity_vec[tail][i]);
        }
        norm(relation_vec[relation]);
        norm(entity_vec[head]);
        norm(entity_vec[tail]);

        norm2one(Wr_vec_transH[relation]);
        norm(relation_vec[relation], Wr_vec_transH[relation]);
    }
    private static void gradient_TransR(int head, int tail, int relation, double beta) {
        for (int i = 0; i < entity_dimension; i++) {
            double Wrh = 0;
            double Wrt = 0;
            for (int j = 0; j < relation_dimension; j++) {
                Wrh += Wr_vec_transR[relation][i][j] * entity_vec[head][j];
                Wrt += Wr_vec_transR[relation][i][j] * entity_vec[tail][j];
            }
            double x = 2 * (Wrt - Wrh - relation_vec[relation][i]);
            for (int j = 0; j < relation_dimension; j++) {
                Wr_transR_copy[relation][i][j] -= beta * learning_rate * (entity_vec[head][j] - entity_vec[tail][j]);
                entity_copy[head][j] -= beta * learning_rate * Wr_vec_transR[relation][i][j];
                entity_copy[tail][j] += beta * learning_rate * Wr_vec_transR[relation][i][j];
            }
            relation_copy[relation][i] -= beta * learning_rate * x;
        }
    }
}
