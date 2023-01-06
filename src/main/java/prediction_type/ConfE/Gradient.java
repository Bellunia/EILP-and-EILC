package prediction_type.ConfE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static prediction_type.ConfE.GlobalValue.*;
import static prediction_type.ConfE.Train.*;
import static prediction_type.ConfE.Utils.*;
import static prediction_type.ConfE.GlobalValue.L1_flag;
import static prediction_type.ConfE.GlobalValue.entity_vec;
import static prediction_type.ConfE.GlobalValue.relation_vec;
import static prediction_type.ConfE.GlobalValue.vector_len;

import static prediction_type.ConfE.Utils.abs;
import static prediction_type.ConfE.Utils.sqr;

public class Gradient {


	private static double calc_sum(int e1, int e2, int rel) {
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

	public static double calc_sum1(int t1, int t2, int rel) {
		double sum = 0;
		if (L1_flag) {
			for (int i = 0; i < type_vector_len; i++) {
				sum += abs(type_vec[t2][i] - type_vec[t1][i] - type_relation_vec[rel][i]);
			}
		} else {
			for (int i = 0; i < type_vector_len; i++) {
				sum += sqr(type_vec[t2][i] - type_vec[t1][i] - type_relation_vec[rel][i]);
			}
		}
		return sum;
	}

	public static double calc_sum2(int e, int t) {
		double[] e1_vec = new double[vector_len];
		double sum = 0;
		for (int ii = 0; ii < vector_len; ii++) {
			for (int jj = 0; jj < type_vector_len; jj++) {
				e1_vec[ii] += type_vec[t][jj] * A[jj][ii];
				//用A的第i列给type_vec的被选中的的类型（第e2行对应的行向量）依次加权，累加作为映射后的第i个值
				//含义是对行向量里的所有值，以每次完全不同 的权重进行加权
			}
			sum += entity_vec[e][ii] * e1_vec[ii];
		}
		return sum;
	}

	public static double train_kb(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b, double lr) {
		// 极大似然估计的计算过程
		//get_relvec(relation_a);
		double sum1 = calc_sum(head_a, tail_a, relation_a);
		double sum2 = calc_sum(head_b, tail_b, relation_b);
		double res = 0;
		if (sum1 + margin1 > sum2) {
			res += margin1 + sum1 - sum2;
			gradient(head_a, tail_a, relation_a, head_b, tail_b, relation_b, lr);
		}
		return res;
	}

	public static double train_kbt(int head_a, int tail_a, int head_b, int tail_b, double res, double lr, int i) {
		
		
		double sum1 = calc_sum2(head_a, tail_a);
		double sum2 = calc_sum2(head_b, tail_b);

		if (sum1 + margin2 < sum2) {
			res += sum2 - margin2 - sum1;
			//因去掉GT故删除double conf = lameda1 * rateconf[i] + lameda2 * connectconf[i];
			gradient1(head_a, tail_a, head_b, tail_b, lr, rateconf[i]);//因去掉GT故把conf改成rateconf[i]
			rateconf[i] *= 0.98;
		} else {
			rateconf[i] += 0.001;
			if (rateconf[i] > 1.0) {
				rateconf[i] = 1.0;
			}
		}
		//因去掉GT故删除gradient2(head_a, tail_a, i, lr);

		return res;
	}

	static void gradient(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b, double lr) {
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
				relation_vec[relation_a][i] += x * lr;
				entity_vec[head_a][i] += x * lr;
				entity_vec[tail_a][i] -= x * lr;

				if (delta2 > 0) {
					x = 1;
				} else {
					x = -1;
				}
				relation_vec[relation_b][i] -= x * lr;
				entity_vec[head_b][i] -= x * lr;
				entity_vec[tail_b][i] += x * lr;
			} else {

				relation_vec[relation_a][i] += lr * 2 * delta1;
				entity_vec[head_a][i] += lr * 2 * delta1;
				entity_vec[tail_a][i] -= lr * 2 * delta1;

				relation_vec[relation_b][i] -= lr * 2 * delta2;
				entity_vec[head_b][i] -= lr * 2 * delta2;
				entity_vec[tail_b][i] += lr * 2 * delta2;
			}
		}
	}

	static void gradient1(int head_a, int tail_a, int head_b, int tail_b, double lr, double conf) {
		for (int ii = 0; ii < vector_len; ii++) {
			for (int jj = 0; jj < type_vector_len; jj++) {
				A[jj][ii] += lr * entity_vec[head_a][ii] * type_vec[tail_a][jj] * conf;
				A[jj][ii] -= lr * entity_vec[head_b][ii] * type_vec[tail_b][jj] * conf;
				type_vec[tail_a][jj] += lr * entity_vec[head_a][ii] * A[jj][ii] * conf;
				type_vec[tail_b][jj] -= lr * entity_vec[head_b][ii] * A[jj][ii] * conf;
				}
			}
	}

	static void gradient2(int head_a, int tail_a, int i, double lr) {
		int ii = rand_max(type_num);
		boolean unchange_sign = true;

		if (tail2triple.containsKey(head_a)) {

			List<Integer> result = checktailrand(head_a, ii);

			int relindex = (int) (result.get(0));
			int typeindex = (int) (result.get(1));
			ii = (int) (result.get(2));

			if (typeindex < type_num) {
				double s1 = calc_sum1(typeindex, tail_a, relindex);
				double s2 = calc_sum1(typeindex, ii, relindex);
				
				if (s1 + margin3 > s2) {
					gradient3(typeindex, tail_a, relindex, typeindex, ii, relindex, lr);
					connectconf[i] *= 0.98;
				} else {
					connectconf[i] += 0.002;
					if (connectconf[i] > 1.0) {
						connectconf[i] = 1.0;
					}
				}
				unchange_sign = false;
			}
		}

		if (unchange_sign) {
			if (head2triple.containsKey(head_a)) {

				List<Integer> result = checkheadrand(head_a, ii);

				int relindex = (int) (result.get(0));
				int typeindex = (int) (result.get(1));
				ii = (int) (result.get(2));

				if (typeindex < type_num) {
					//get_typerelvec(relindex);
					double s1 = calc_sum1(tail_a, typeindex, relindex);
					double s2 = calc_sum1(ii, typeindex, relindex);
					if (s1 + margin3 > s2) {
						gradient3(tail_a, typeindex, relindex, ii, typeindex, relindex, lr);
						connectconf[i] *= 0.98;
					} else {
						connectconf[i] += 0.002;
						if (connectconf[i] > 1.0) {
							connectconf[i] = 1.0;
						}
					}
				}
			}
		}

	}

	static void gradient3(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b, double lr) {
		for (int i = 0; i < type_vector_len; i++) {
			
			double delta1 = type_vec[tail_a][i] - type_vec[head_a][i] - type_relation_vec[relation_a][i];
			double delta2 = type_vec[tail_b][i] - type_vec[head_b][i] - type_relation_vec[relation_b][i];
			double x;
			if (L1_flag) {
				if (delta1 > 0) {
					x = 1;
				} else {
					x = -1;
				}
				type_relation_vec[relation_a][i] += x * lr;
				type_vec[head_a][i] += x * lr;
				type_vec[tail_a][i] -= x * lr;

				if (delta2 > 0) {
					x = 1;
				} else {
					x = -1;
				}
				type_relation_vec[relation_b][i] -= x * lr;
				type_vec[head_b][i] -= x * lr;
				type_vec[tail_b][i] += x * lr;
			} else {

				type_relation_vec[relation_a][i] += lr * 2 * delta1;
				type_vec[head_a][i] += lr * 2 * delta1;
				type_vec[tail_a][i] -= lr * 2 * delta1;

				type_relation_vec[relation_b][i] -= lr * 2 * delta2;
				type_vec[head_b][i] -= lr * 2 * delta2;
				type_vec[tail_b][i] += lr * 2 * delta2;
			}
		}
	}
	
	static void get_relvec(int relation_a){
		for(int j  = 0; j < vector_len; j ++) {
			relation_vec[relation_a][j] = 0;
			for(int k  = 0; k < type_vector_len; k ++) {
				relation_vec[relation_a][j] += type_relation_vec[relation_a][k] * A[k][j];
			}	
		}
	}
	
	static void get_typerelvec(int relation_a){
		for(int j  = 0; j < type_vector_len; j ++) {
			type_relation_vec[relation_a][j] = 0;
			for(int k  = 0; k < vector_len; k ++) {
				type_relation_vec[relation_a][j] += relation_vec[relation_a][k] * A[j][k];		
			}	
		}
	}
	
	static List<Integer> checktailrand(int head_a, int ii) {
		
		Random random = new Random();
		List<Integer> result = new ArrayList<Integer>();
		List<Integer> list = new ArrayList<Integer>(tail2triple.get(head_a));
		if(list.isEmpty()) {
			result.add(4000);
			result.add(5000);
		}else {
			int rn = random.nextInt(list.size());
			int index = list.get(rn);
			result.add(fb_r.get(index));
			
			if (et2type.get(fb_h.get(index)) == null) {
				result.add(4000);
			}else {
				List<Integer> typelist = new ArrayList<Integer>(et2type.get(fb_h.get(index)));
				int typern = random.nextInt(typelist.size());
				int typeindex = typelist.get(typern);
				result.add(typeindex);
			}
		}
		
		List<Integer> totaltype = new ArrayList<Integer>(et2tail2trt.get(head_a));
		while (totaltype.contains(ii)) {
			ii = rand_max(type_num);
		}
		result.add(ii);

		return result;
	}

	static List<Integer> checkheadrand(int head_a, int ii) {

		Random random = new Random();
		List<Integer> result = new ArrayList<Integer>();
		List<Integer> list = new ArrayList<Integer>(head2triple.get(head_a));
		if(list.isEmpty()) {
			result.add(4000);
			result.add(5000);
		}else {
			int rn = random.nextInt(list.size());
			int index = list.get(rn);
			result.add(fb_r.get(index));
			
			if (et2type.get(fb_l.get(index)) == null) {
				result.add(4000);
			}else {
				List<Integer> typelist = new ArrayList<Integer>(et2type.get(fb_l.get(index)));
				int typern = random.nextInt(typelist.size());
				int typeindex = typelist.get(typern);
				result.add(typeindex);
			}
		}
		
		List<Integer> totaltype = new ArrayList<Integer>(et2head2trt.get(head_a));
		while (totaltype.contains(ii)) {
			ii = rand_max(type_num);
		}
		result.add(ii);
		
		return result;
	}
}