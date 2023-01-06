package prediction_type.ConfE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import static prediction_type.ConfE.GlobalValue.*;
import static prediction_type.ConfE.Gradient.train_kb;
import static prediction_type.ConfE.Gradient.train_kbt;
import static prediction_type.ConfE.Utils.*;
import static prediction_type.ConfE.GlobalValue.entity_vec;
import static prediction_type.ConfE.GlobalValue.relation_vec;


public class Train {

	private double res; // loss function value
	private double res2;
	static List<Integer> fb_h;
	static List<Integer> fb_l;
	static List<Integer> fb_r;
	private List<Integer> fb_ht;
	private List<Integer> fb_lt;
	private Map<Pair<Integer, Integer>, Set<Integer>> head_relation2tail; // to save the (h, r, t)

	static Map<Integer, Set<Integer>> head2triple;
	static Map<Integer, Set<Integer>> tail2triple;
	static Map<Integer, Set<Integer>> et2type;
	static Map<Integer, Set<Integer>> et2head2trt;
	static Map<Integer, Set<Integer>> et2tail2trt;

	Train() {
		fb_h = new ArrayList<>();
		fb_l = new ArrayList<>();
		fb_r = new ArrayList<>();
		fb_ht = new ArrayList<>();
		fb_lt = new ArrayList<>();
		et2type = new HashMap<>();
		head2triple = new HashMap<>();
		tail2triple = new HashMap<>();
		et2head2trt = new HashMap<>();
		et2tail2trt = new HashMap<>();
		head_relation2tail = new HashMap<>();
	}
	public static void Write_Vec2File(String file_name, double[][] vec, int number, int veclen) throws IOException {
		File f = new File(file_name);
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
		for (int i = 0; i < number; i++) {
			for (int j = 0; j < veclen; j++) {
				String str = String.format("%.6f\t", vec[i][j]);
				writer.write(str);
			}
			writer.write("\n");
			writer.flush();
		}
	}

	private void Write_Vec2File(String file_name, double[] vec, int num) throws IOException {
		File f = new File(file_name);
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
		for (int i = 0; i < num; i++) {
			for (int j = 0; j < 1; j++) {
				String str = String.format("%.6f", vec[i]);
				writer.write(str);
			}
			writer.write("\n");
			writer.flush();
		}
	}

	private void bfgs(int nepoch, int nbatches) throws IOException {
		int batchsize = fb_h.size() / nbatches;
		//将训练集样本总数按照400组均匀划分，计算每组有多少个样本，感觉他这个算法并没有从功能上区分batch和batchsize，都是对每一个样本算一个权重？？？？
		System.out.printf("Batch size = %s\n", batchsize);
		for (int epoch = 0; epoch < nepoch; epoch++) {  //按照设定的学习次数800次开始迭代咯
			res = 0;    // means the total loss in each epoch
			res2 = 0;   //res是triple的损失函数，res2是tuple的损失函数
			double lr1,lr2 = 0;   //把lr和设定的参数绑定起来
			if(epoch<50){
				lr1 = learning_rate1;
				lr2 = learning_rate2;
			}
			else if (epoch<800){
				lr1 = 50*learning_rate1/epoch;
				lr2 = 50*learning_rate2/epoch;
			}
			else {
				lr1 = 0.0001;
				lr2 = 0.0001;
			}   //梯度设置好啦
			for (int batch = 0; batch < nbatches; batch++) {   //对于每一组样本构造正反例，算损失函数
				for (int k = 0; k < batchsize; k++) {   //对于400组中每一组样本里的每一个triple进行迭代，构造正反例，算损失函数
					int i = rand_max(fb_h.size());   //从0到“训练集样本总数”中抽一个数字
					int j = rand_max(entity_num);   //从0到“去重实体个数”中抽一个数字
					int relation_id = fb_r.get(i);   //从训练集样本总数中找到第i个triple对应的关系
					int ii = rand_max(fb_ht.size());   //从0到“训练集类型样本总数”中抽一个数字
					int jj = rand_max(type_num);   //从0到“去重类型个数”中抽一个数字
					int kk = rand_max(entity_num);   //从0到“去重实体个数”中再抽一个数字
					double pr = 1000 * right_num.get(relation_id) / (right_num.get(relation_id) + left_num.get(relation_id));
					//relation_id是“训练集样本总数”中第i个triple对应的关系在“去重关系总数”中对应的位置
					//right_num.get（）是有了在“去重关系总数”中对应的位置之后，查找尾部实体在这个关系下平均出现次数
					//所以pr的含义是什么呢？根据下面的代码来说，是以某种几率替换头部或者尾部实体，那么为什么要拿rand() % 1000和这个出现率比较呢？？？
					if (method == 0) {
						pr = 500;
						//如果method为0，那么就是头部实体和尾部实体在每一个关系中都均匀分布，此时尾部实体出现率为50%，问题是已经设定了是伯努利分布
						//按照我的理解，pr应该就等于尾部实体出现的概率，也就是这个if循环根本不用管
					}
					if (rand() % 1000 < pr) {   //如果余数低于这一关系下尾部实体出现率，则构造反例时替换尾部实体
						Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
						//别管结构是怎么回事，目的是把训练集总体中第i个triple的头部实体抽出来，后面跟拟用作反例的尾部实体比较，看看需不需要重新抽取
						Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
						while (values.contains(j)) {   //如果这对头部和关系中已经包括了拟作为反例的尾部实体，相当于加上这个就头尾相等了
							j = rand_max(entity_num);   //就重抽一个作为拟用作反例的尾部实体
						}
						while(et2type.get(fb_ht.get(ii)).contains(jj)){   //如果这对实体和类型中已经包括了拟作为反例的实体，相当于抽重复了
							jj = rand_max(type_num);   //就重抽一个作为拟作为反例的实体
						}
						res = train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), fb_h.get(i), j, fb_r.get(i), lr1);
						//把第i个triple里的尾部实体换掉了，更新了行向量里的可信度
						res2 = train_kbt(fb_ht.get(ii), fb_lt.get(ii), fb_ht.get(ii), jj, res2, lr2, ii);
						//把第ii个tuple里的类型换掉了，更新了GT和LT
					} else {   //如果余数高于这一关系下尾部实体出现率，则构造反例时替换头部实体
						Pair<Integer, Integer> key = new Pair<>(j, fb_r.get(i));   //构造了反例，其中头部实体是错的
						Set<Integer> values = head_relation2tail.get(key);
						//下面的目的是检查这个反例是否包含在训练集样本中，如果包含，就重新抽一个头部实体再次检测，直到这样的组合没在训练集中出现过为止
						if (values != null) {
							while (values.contains(fb_l.get(i))) {
								j = rand_max(entity_num);
								key = new Pair<>(j, fb_r.get(i));
								values = head_relation2tail.get(key);
								if (values == null) break;
							}
						}
						//以上循环是为了构造一个triple中的反例，下面使用同样的方法构造tuple中的反例
						//因为突破了中本来就有反例（我们在excel中浓度），所以这里的“反”其实是指没有在这个noisy的tuple训练集中出现过的
						while(!et2type.containsKey(kk)||et2type.get(kk).contains(fb_lt.get(ii))){
							kk = rand_max(entity_num);
						}
						res = train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i), lr1);
						//把第i个triple里的头部实体换掉了，更新了行向量里的可信度
						res2 = train_kbt(fb_ht.get(ii), fb_lt.get(ii), kk, fb_lt.get(ii),  res2, lr2, ii);
						//把第ii个tuple里的实体换掉了，更新了GT和LT
					}
					norm(relation_vec[fb_r.get(i)], vector_len);   //标准化
					norm(entity_vec[fb_h.get(i)], vector_len);
					norm(entity_vec[fb_l.get(i)], vector_len);
					norm(entity_vec[j], vector_len);
					norm(type_vec[fb_lt.get(ii)], vector_len);
					norm(type_vec[jj], vector_len);
					norm(type_vec[fb_lt.get(ii)], A,lr1);
					norm(type_vec[jj], A,lr1);
					norm(entity_vec[kk],vector_len);
					norm(entity_vec[fb_ht.get(ii)],vector_len);
				}
			}
			System.out.println(fb_h.size());   //打印出来总体样本个数
			System.out.printf("epoch: %s %s\n", epoch, res);   //打印出来这是第几次循环，输出未经映射的Qglobal
			System.out.printf("epoch: %s %s\n", epoch, res2);   //打印出来这是第几次循环，输出未经映射的Qlocal
		}
		Write_Vec2File("./type/resource/relation2vec." + version, relation_vec, relation_num, vector_len);
		Write_Vec2File("./type/resource/entity2vec." + version, entity_vec, entity_num, vector_len);
		Write_Vec2File("./type/resource/type2vec." + version, type_vec, type_num, vector_len);
		Write_Vec2File("./type/resource/A." + version, A, type_vector_len, vector_len);
		Write_Vec2File("./type/resource/rateconf." + version, rateconf, fb_ht.size());
		Write_Vec2File("./type/resource/connectconf." + version, connectconf, fb_ht.size());
	}


	private void bfgs(int nepoch) throws IOException {

		int batchsize = fb_h.size() / nbatches;
		//将训练集样本总数按照400组均匀划分，计算每组有多少个样本，感觉他这个算法并没有从功能上区分batch和batchsize，都是对每一个样本算一个权重？？？？
		System.out.printf("Batch size = %s\n", batchsize);

		for (int epoch = 0; epoch < nepoch; epoch++) {  //按照设定的学习次数800次开始迭代咯
			res = 0; // means the total loss in each epoch
			res2 = 0;   //res是triple的损失函数，res2是tuple的损失函数
			double lr1, lr2 = 0;
			if (epoch < 50) {
				lr1 = learning_rate1;
				lr2 = learning_rate2;
			} else if (epoch < 800) {
				lr1 = 50 * learning_rate1 / epoch;
				lr2 = 50 * learning_rate2 / epoch;
			} else {
				lr1 = 0.0001;
				lr2 = 0.0001;
			}  //梯度设置好啦

			for(int i  = 0; i < fb_h.size(); i++) {
					int j = rand_max(entity_num);
					int relation_id = fb_r.get(i);

					int ii = rand_max(fb_ht.size());
					int jj = rand_max(type_num);

					int kk = rand_max(entity_num);
					double pr = 1000 * right_num.get(relation_id)
							/ (right_num.get(relation_id) + left_num.get(relation_id));
					if (method == 0) {
						pr = 500;
					}
					if (rand() % 1000 < pr) {
						Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
						Set<Integer> values = head_relation2tail.get(key);
						while (values.contains(j)) {
							j = rand_max(entity_num);
						}
						while (et2type.get(fb_ht.get(ii)).contains(jj)) {
							jj = rand_max(type_num);
						}
						System.out.println(fb_h.get(i)+"\t"+ fb_l.get(i)+"\t"+ fb_r.get(i)+"\t"+fb_h.get(i)+"\t"+ j+"\t"+ fb_r.get(i));
						res = train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), fb_h.get(i), j, fb_r.get(i), lr1);
						res2 = train_kbt(fb_ht.get(ii), fb_lt.get(ii), fb_ht.get(ii), jj, res2, lr2, ii);
					} else {
						Pair<Integer, Integer> key = new Pair<>(j, fb_r.get(i));
						Set<Integer> values = head_relation2tail.get(key);
						if (values != null) {
							while (values.contains(fb_l.get(i))) {
								j = rand_max(entity_num);
								key = new Pair<>(j, fb_r.get(i));
								values = head_relation2tail.get(key);
								if (values == null)
									break;
							}
						}
						while (!et2type.containsKey(kk) || et2type.get(kk).contains(fb_lt.get(ii))) {
							kk = rand_max(entity_num);
						}
						System.out.println(fb_h.get(i)+"\t"+ fb_l.get(i)+"\t"+ fb_r.get(i)+"\t"+j+"\t"+ fb_l.get(i)+"\t"+ fb_r.get(i)+"\t"+lr1);
						res = train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i), lr1);
						res2 = train_kbt(fb_ht.get(ii), fb_lt.get(ii), kk, fb_lt.get(ii), res2, lr2, ii);
					}

			}
			System.out.printf("epoch: %s %s\n", epoch, res);
			System.out.printf("epoch: %s %s\n", epoch, res2);
		}
		Write_Vec2File("./type/resource/data/entity2vec." + version, entity_vec,
				entity_num, vector_len);
		Write_Vec2File("./type/resource/data/type2vec." + version, type_vec,
				type_num, type_vector_len);
		Write_Vec2File("./type/resource/data/A." + version, A, type_vector_len, vector_len);
	}

	// region public members & methods

	public void add(int head, int relation, int tail, int i) {
		fb_h.add(head); //把训练集里的这个triple的位置和在x_id中的位置对应起来
		fb_r.add(relation);
		fb_l.add(tail);

		Pair<Integer, Integer> key = new Pair<>(head, relation);
		if (!head_relation2tail.containsKey(key)) {
			head_relation2tail.put(key, new HashSet<>());//这个head_relation组合都有和哪些tail中组合过
		}
		Set<Integer> tail_set = head_relation2tail.get(key);
		tail_set.add(tail);  //获得了对应的tail在tail2id中的位置

		if (!head2triple.containsKey(head)) { //这个head都有在哪些triple中出现过？加入总体字典中
			head2triple.put(head, new HashSet<>());
		}
		if (!tail2triple.containsKey(tail)) { //这个tail都有在哪些triple中出现过？加入总体字典中
			tail2triple.put(tail, new HashSet<>());
		}
		Set<Integer> h_triple_set = head2triple.get(head);  //这个head都有在哪些triple中出现过？
		Set<Integer> t_triple_set = tail2triple.get(tail); //这个tail都有在哪些triple中出现过？
		h_triple_set.add(i);  //把这些triple按顺序组合起来，但是h_triple_set的作用是什么呢？？？
		t_triple_set.add(i); //把这些triple按顺序组合起来
	}

	public void add2(int head, int tail) {
		fb_ht.add(head);
		fb_lt.add(tail);
		if (!et2type.containsKey(head)) {
			et2type.put(head, new HashSet<>()); //这个head（实体）都有在哪些tuple中出现过？加入总体字典中
		}
		Set<Integer> type_set = et2type.get(head);
		type_set.add(tail);
	}

	public void preparetrt() {
		for (Integer curEntity : fb_ht) {

			if (tail2triple.containsKey(curEntity)) {
				List<Integer> list = new ArrayList<>(tail2triple.get(curEntity));

				if (!et2tail2trt.containsKey(curEntity)) {
					et2tail2trt.put(curEntity, new HashSet<>());
				}
				Set<Integer> type_set = et2tail2trt.get(curEntity);

				for (Integer curTriple : list) {
					int curHead =  fb_h.get(curTriple);

					if (et2type.get(curHead) != null)
					type_set.addAll(et2type.get(curHead));
				}
			}
			if (head2triple.containsKey(curEntity)) {
				List<Integer> list = new ArrayList<>(head2triple.get(curEntity));

				if (!et2head2trt.containsKey(curEntity)) {
					et2head2trt.put(curEntity, new HashSet<>());
				}
				Set<Integer> type_set = et2head2trt.get(curEntity);

				for (Integer curtriple : list) {
					int curtail = fb_l.get(curtriple);
					if (et2type.get(curtail) != null)
						type_set.addAll(et2type.get(curtail));
				}
			}
		}
	}

	public void run(int nepoch) throws IOException {
		relation_vec = new double[relation_num][vector_len]; //x_vec都是（对应去重对象数x向量长度（解释维度））尺寸的矩阵
		type_relation_vec = new double[relation_num][type_vector_len];
		entity_vec = new double[entity_num][vector_len];
		type_vec = new double[type_num][type_vector_len];//type_vec = new double[type_num][vector_len];
		A = new double[type_vector_len][vector_len];
		connectconf = new double[fb_ht.size()];   //对于总体类型训练集中的每一个tuple，初始化一个GT
		rateconf = new double[fb_ht.size()];     //对于总体类型训练集中的每一个tuple，初始化一个LT

		for (int i = 0; i < fb_ht.size(); i++) { //对于总体类型训练集中的每一个tuple进行循环赋值
			connectconf[i] = 1; //把每一个GT初始化为1
			rateconf[i] = 1;  //把每一个LT初始化为1
		}
		for (int i = 0; i < relation_num; i++) { //下面依次对x_vec和A进行标准化or whatever化
			for (int j = 0; j < type_vector_len; j++) {
				type_relation_vec[i][j] = uniform(-6 / sqrt(type_vector_len), 6 / sqrt(type_vector_len));
			}
		}
		for (int i = 0; i < relation_num; i++) {
			for (int j = 0; j < vector_len; j++) {
				relation_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
		}
		for (int i = 0; i < entity_num; i++) {
			for (int j = 0; j < vector_len; j++) {
				entity_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
			norm(entity_vec[i], vector_len);
		}
		for (int i = 0; i < type_num; i++) {
			for (int j = 0; j < type_vector_len; j++) { //vector_len
				type_vec[i][j] = uniform(-6 / sqrt(type_vector_len), 6 / sqrt(type_vector_len));
			}
		}
		for (int i = 0; i < type_vector_len; i++) {
			for (int j = 0; j < vector_len; j++) {
				A[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
		}

		preparetrt();
		bfgs(nepoch);
	}

	public void run(int nepoch, int nbatches) throws IOException {
		relation_vec = new double[relation_num][vector_len];   //x_vec都是（对应去重对象数x向量长度（解释维度））尺寸的矩阵
		entity_vec = new double[entity_num][vector_len];
		type_vec = new double[type_num][vector_len];
		A = new double[type_vector_len][vector_len];//type_vector_len replace the m
		//初始化一个（mx向量长度（解释维度））的矩阵，但是m为什么要小于x_vec的列数（vector_len）呢？这样不就只能给前m=40列加权了吗？？？？
		connectconf = new double[fb_ht.size()];   //对于总体类型训练集中的每一个tuple，初始化一个GT
		rateconf = new double[fb_ht.size()];   //对于总体类型训练集中的每一个tuple，初始化一个LT
		for (int i = 0; i < fb_ht.size(); i++) {   //对于总体类型训练集中的每一个tuple进行循环赋值
			connectconf[i] = 1;   //把每一个GT初始化为1
			rateconf[i] = 1;   //把每一个LT初始化为1
		}
		for (int i = 0; i < relation_num; i++) {   //下面依次对x_vec和A进行标准化or whatever化
			for (int j = 0; j < vector_len; j++) {
				relation_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
		}
		for (int i = 0; i < entity_num; i++) {
			for (int j = 0; j < vector_len; j++) {
				entity_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
			norm(entity_vec[i], vector_len);
		}
		for (int i = 0; i < type_num; i++) {
			for (int j = 0; j < vector_len; j++) {
				type_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
		}
		for (int i = 0; i < type_vector_len; i++) {
			for (int j = 0; j < vector_len; j++) {
				A[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
		}
		bfgs(nepoch, nbatches);
	}
	// endregion
}
