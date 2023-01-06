package prediction_type.RHE;


import prediction_type.ConfE.AnnotatedTri;
import prediction_type.ConfE.Pair;
import prediction_type.ConfE.Tri;


import java.io.IOException;

import java.util.*;

import static prediction_type.ConfE.Utils.*;

import static prediction_type.RHE.GlobalValue.*;
import static prediction_type.ConfE.Gradient.train_kb;
import static prediction_type.ConfE.Gradient.train_kbt;


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
		Write_Vec2FileArray("./type/resource/relation2vec." + version, relation_vec, relation_num, vector_len);
		Write_Vec2FileArray("./type/resource/entity2vec." + version, entity_vec, entity_num, vector_len);
		Write_Vec2FileArray("./type/resource/type2vec." + version, type_vec, type_num, vector_len);
		Write_Vec2FileArray("./type/resource/A." + version, A, type_vector_len, vector_len);
		Write_Vec2FileArray("./type/resource/rateconf." + version, rateconf, fb_ht.size());
		Write_Vec2FileArray("./type/resource/connectconf." + version, connectconf, fb_ht.size());
	}


	private void bfgs(int nepoch) throws IOException {
		for (int epoch = 0; epoch < nepoch; epoch++) {
			res = 0; // means the total loss in each epoch
			res2 = 0;
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
			}
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
						res = train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i), lr1);
						res2 = train_kbt(fb_ht.get(ii), fb_lt.get(ii), kk, fb_lt.get(ii), res2, lr2, ii);
					}
					
			}
			System.out.printf("epoch: %s %s\n", epoch, res);
			System.out.printf("epoch: %s %s\n", epoch, res2);
		}
		Write_Vec2FileArray("./type/resource/data/entity2vec." + version, entity_vec,
				entity_num, vector_len);
		Write_Vec2FileArray("./type/resource/data/type2vec." + version, type_vec,
				type_num, type_vector_len);
		Write_Vec2FileArray("./type/resource/data/A." + version, A, type_vector_len, vector_len);
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
		for (int ii = 0; ii < fb_ht.size(); ii++) {
			int curentity = (int) (fb_ht.get(ii));
			if (tail2triple.containsKey(curentity)) {
				List<Integer> list = new ArrayList<Integer>(tail2triple.get(curentity));

				if (!et2tail2trt.containsKey(curentity)) {
					et2tail2trt.put(curentity, new HashSet<>());
				}
				Set<Integer> type_set = et2tail2trt.get(curentity);

				for (int listi = 0; listi < list.size(); listi++) {
					int curtriple = (int) (list.get(listi));
					int curhead = (int) (fb_h.get(curtriple));

					if (et2type.get(curhead) == null) {
						continue;
					}
					List<Integer> ltypelist = new ArrayList<Integer>(et2type.get(curhead));
					for (int typelisti = 0; typelisti < ltypelist.size(); typelisti++) {
						int curtype = (int) (ltypelist.get(typelisti));
						type_set.add(curtype);
					}
				}
			}
			if (head2triple.containsKey(curentity)) {
				List<Integer> list = new ArrayList<Integer>(head2triple.get(curentity));

				if (!et2head2trt.containsKey(curentity)) {
					et2head2trt.put(curentity, new HashSet<>());
				}
				Set<Integer> type_set = et2head2trt.get(curentity);

				for (int listi = 0; listi < list.size(); listi++) {
					int curtriple = (int) (list.get(listi));
					int curtail = (int) (fb_l.get(curtriple));

					if (et2type.get(curtail) == null) {
						continue;
					}
					List<Integer> ltypelist = new ArrayList<Integer>(et2type.get(curtail));
					for (int typelisti = 0; typelisti < ltypelist.size(); typelisti++) {
						int curtype = (int) (ltypelist.get(typelisti));
						type_set.add(curtype);
					}
				}
			}
		}
		return;
	}

	public void run(int nepoch) throws IOException {
		//build relation-specific information for type

		relation_vec = new double[relation_num][vector_len]; //x_vec都是（对应去重对象数x向量长度（解释维度））尺寸的矩阵
		type_relation_vec = new double[relation_num][type_vector_len];
		entity_vec = new double[entity_num][vector_len];
		type_vec = new double[type_num][type_vector_len];

		domain_vec = new double[type_num][type_vector_len];//@8.29

		A = new double[type_vector_len][vector_len];
		connectconf = new double[fb_ht.size()];
		rateconf = new double[fb_ht.size()];
		for (int i = 0; i < fb_ht.size(); i++) {
			connectconf[i] = 1;
			rateconf[i] = 1;
		}
		for (int i = 0; i < relation_num; i++) {
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
			for (int j = 0; j < type_vector_len; j++) {
				type_vec[i][j] = uniform(-6 / sqrt(type_vector_len), 6 / sqrt(type_vector_len));
			}
		}
		for (int i = 0; i < type_vector_len; i++) {
			for (int j = 0; j < vector_len; j++) {
				A[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
			}
		}

		for (int i = 0; i < type_num; i++) {
			for (int j = 0; j < type_vector_len; j++) {
				domain_vec[i][j] = uniform(-6 / sqrt(type_vector_len), 6 / sqrt(type_vector_len));
			}
		}

		domain_mat = new double[domain_num][vector_len][vector_len];
		for(int i=0; i<domain_num; i++) {
			for(int ii=0; ii<vector_len; ii++) {
				for(int iii=0; iii<vector_len; iii++) {
					if(ii==iii)  domain_mat[i][ii][iii] = 1;
					else  domain_mat[i][ii][iii] = 0;
				}
			}
		}

		type_mat=new double[type_num][vector_len][vector_len];
		for(int i=0; i<type_num; i++) {
			for(int ii=0; ii<vector_len; ii++) {
				for(int iii=0; iii<vector_len; iii++) {
					if(ii==iii)  type_mat[i][ii][iii] = 1;
					else  type_mat[i][ii][iii] = 0;
				}
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


	//------------------------@8.29
	//--------------------@8.29
	List<AnnotatedTri> ok;
	void add(int x,int y,int z)
	{
		fb_h.add(x);
		fb_r.add(z);
		fb_l.add(y);
		 ok.add( new AnnotatedTri(new Tri(x,z,y), 1)); //positive mark ---ok[new Pair(x,z)][y]=1;
	}
	public int exist(int head,int relation,int tail, List<AnnotatedTri> tri){
		Tri t = new Tri(head,relation,tail);
		for(AnnotatedTri key: tri){

			if (!key.get_tri().equals(t)) {
				continue;
			}
			return 1;
		}

		return 0;
	}
	public int existBody(int head,int relation,List<AnnotatedTri> tri){
		for(AnnotatedTri key: tri){

			if (!key.get_body().equals(new Pair(head, relation))) {
				continue;
			}
			return 1;
		}

		return 0;
	}

	public int existTail(List<AnnotatedTri> tri, int tail){
		for(AnnotatedTri key: tri){

			if (key.get_tail()!= tail) {
				continue;
			}
			return 1;
		}

		return 0;
	}

	public void rand_sel(int nbatches,int tid)	{	//multi-thread train
		int batchsize = fb_h.size() / nbatches;
		for (int k=0; k<batchsize; k++)
		{
			int i=rand_max(fb_h.size());		//positive mark
			int j=rand_max(entity_num);		//negative entity
			double pr = 1000*right_num.get(fb_r.get(i))/(right_num.get(fb_r.get(i))+left_num.get(fb_r.get(i)));
			if (method ==0)
				pr = 500;

			//negative sampling
			int flag_num = rand_max(1000);
			int temp_head_type = head_type_vec.get(fb_r.get(i));
			int temp_tail_type = tail_type_vec.get(fb_r.get(i));
			if (flag_num<pr)
			{
				Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));   //构造了反例，其中头部实体是错的
				Set<Integer> values = head_relation2tail.get(key);
				if(values!=null) {

					if (rand_max(entity_num + 10 * type_entity_num.get(temp_tail_type)) > entity_num)        //Soft type constraint, parameter could be changed
					{
						int jj = rand_max(type_entity_num.get(temp_tail_type));
						j = type_entity_list[temp_tail_type][jj];
					}

					//while (ok.count(make_pair(fb_h[i],fb_r[i]))>0&&ok[make_pair(fb_h[i],fb_r[i])].count(j)>0)
					//--	while (existBody(fb_h.get(i),fb_r.get(i),ok)>0&&existTail(ok,j) >0)
					//--		j=rand_max(entity_num);
					while (values.contains(j)) // judge the ok contain the <i,i,j>
						j = rand_max(entity_num);

					train_triple_mul(fb_h.get(i), fb_l.get(i), fb_r.get(i), fb_h.get(i), j, fb_r.get(i), tid);
				}
			}
			else
			{
				if(rand_max(entity_num+10*type_entity_num.get(temp_head_type)) > entity_num)		//Soft type constraint, parameter could be changed
				{
					int jj = rand_max(type_entity_num.get(temp_head_type));
					j = type_entity_list[temp_head_type][jj];
				}
				while(exist(j,fb_r.get(i),fb_l.get(i),ok)>0)
				//while (ok.count(make_pair(j,fb_r[i]))>0&&ok[make_pair(j,fb_r[i])].count(fb_l[i])>0)
					j=rand_max(entity_num);
				train_triple_mul(fb_h.get(i),fb_l.get(i),fb_r.get(i),j,fb_l.get(i),fb_r.get(i),tid);
			}

			int rel_neg = rand_max(relation_num);		//negative relation

			Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), rel_neg);   //构造了反例，其中头部实体是错的
			Set<Integer> values = head_relation2tail.get(key);

			while(values.contains(fb_l.get(i)))
		//	while (ok.count(make_pair(fb_h[i], rel_neg))>0&& ok[make_pair(fb_h[i], rel_neg)].count(fb_l[i]) > 0)
				rel_neg = rand_max(relation_num);
			train_triple_mul(fb_h.get(i),fb_l.get(i),fb_r.get(i),fb_h.get(i),fb_l.get(i),rel_neg,tid);

			//normalization
			norm(relation_vec [fb_r.get(i)],vector_len);
			norm(relation_vec[rel_neg],vector_len);
			norm(entity_vec[fb_h.get(i)],vector_len);
			norm(entity_vec[fb_l.get(i)],vector_len);
			norm(entity_vec[j],vector_len);


			norm_2(entity_vec[fb_h.get(i)], domain_mat[head_domain_vec.get(fb_r.get(i))], type_mat[head_type_vec.get(fb_r.get(i))], tid);
			norm_2(entity_vec[fb_l.get(i)], domain_mat[tail_domain_vec.get(fb_r.get(i))], type_mat[tail_type_vec.get(fb_r.get(i))], tid);
			if(flag_num<pr)
				norm_2(entity_vec[j], domain_mat[tail_domain_vec.get(fb_r.get(i))], type_mat[tail_type_vec.get(fb_r.get(i))], tid);
			else
				norm_2(entity_vec[j], domain_mat[head_domain_vec.get(fb_r.get(i))], type_mat[head_type_vec.get(fb_r.get(i))], tid);
			norm_2(entity_vec[fb_h.get(i)], domain_mat[head_domain_vec.get(rel_neg)], type_mat[head_type_vec.get(rel_neg)], tid);
			norm_2(entity_vec[fb_l.get(i)], domain_mat[tail_domain_vec.get(rel_neg)], type_mat[tail_type_vec.get(rel_neg)], tid);

		}
	}

	static void norm_2(double[] a, double[][] D, double[][] T, int tid){		//normalization

		double x=0;

		double der_1 = 0;
		//calc

		for(int i = 0; i< prediction_type.RHE.GlobalValue.vector_len; i++)
		{
			mid_norm_vec[tid][i] = 0;// [THREADS_NUM][vector_len]
			for(int ii = 0; ii< prediction_type.RHE.GlobalValue.vector_len; ii++) {
				mid_norm_vec[tid][i] += D[i][ii] * a[ii];
			}
		}
		for(int i = 0; i< prediction_type.RHE.GlobalValue.vector_len; i++)
		{
			double tmp = 0;
			for(int ii = 0; ii< prediction_type.RHE.GlobalValue.vector_len; ii++)
			{
				tmp += T[i][ii] * mid_norm_vec[tid][ii];
			}
			x += sqr(tmp);
		}

		//gradient
		if(x>1)
		{
			for(int i = 0; i< prediction_type.RHE.GlobalValue.vector_len; i++)
				mid_norm_grad[tid][i] = 0;
			double lamda=1;
			for(int i = 0; i< prediction_type.RHE.GlobalValue.vector_len; i++)
			{
				double tmp = 0;
				for(int ii = 0; ii< prediction_type.RHE.GlobalValue.vector_len; ii++)
					tmp += T[i][ii] * mid_norm_vec[tid][ii];
				tmp *= 2;
				der_1 = -rate*lamda*tmp;
				for(int ii = 0; ii< prediction_type.RHE.GlobalValue.vector_len; ii++)
				{
					T[i][ii] += der_1 * mid_norm_vec[tid][ii];
					mid_norm_grad[tid][ii] += der_1 * T[i][ii];
				}
			}
			for(int i = 0; i< prediction_type.RHE.GlobalValue.vector_len; i++)
			{
				for(int ii = 0; ii< prediction_type.RHE.GlobalValue.vector_len; ii++)
				{
					D[i][ii] += mid_norm_grad[tid][i] * a[ii];
					a[ii] += mid_norm_grad[tid][i] * D[i][ii];
				}
			}
		}
	}




	public void gradient_triple(int e1_a,int e2_a,int rel_a,int e1_b,int e2_b,int rel_b,int tid)		//SGD update
	{
		int tempDomain = -1;
		int tempType = -1;
		double der_1 = -1;
		//positive triple
		//relation
		for(int i=0; i<vector_len; i++)
		{
			relation_vec[rel_a][i] += rate*posErrorVec[tid][i];
		}
		//head
		tempDomain = head_domain_vec.get(rel_a);
		tempType = head_type_vec.get(rel_a);
		for(int i=0; i<vector_len; i++)		//init
			mid_grad[tid][i] = 0;
		for(int i=0; i<vector_len; i++)
		{
			der_1 = rate*posErrorVec[tid][i];
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * mid_head_vec[tid][1][ii];
				mid_grad[tid][ii] += der_1 * type_mat[tempType][i][ii];
			}
		}
		for(int i=0; i<vector_len; i++)
		{
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += mid_grad[tid][i] * entity_vec[e1_a][ii];
				entity_vec[e1_a][ii] += mid_grad[tid][i] * domain_mat[tempDomain][i][ii];
			}
		}
		//tail
		tempDomain = tail_domain_vec.get(rel_a);
		tempType = tail_type_vec.get(rel_a);
		for(int i=0; i<vector_len; i++)		//init
			mid_grad[tid][i] = 0;
		for(int i=0; i<vector_len; i++)
		{
			der_1 = -rate*posErrorVec[tid][i];
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * mid_tail_vec[tid][1][ii];
				mid_grad[tid][ii] += der_1 * type_mat[tempType][i][ii];
			}
		}
		for(int i=0; i<vector_len; i++)
		{
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += mid_grad[tid][i] * entity_vec[e2_a][ii];
				entity_vec[e2_a][ii] += mid_grad[tid][i] * domain_mat[tempDomain][i][ii];
			}
		}
		//negative triple
		for (int i = 0;i<vector_len;i++)
		{
			relation_vec[rel_b][i] -= rate*negErrorVec[tid][i];
		}
		//head
		tempDomain = head_domain_vec.get(rel_b);
		tempType = head_type_vec.get(rel_b);
		for(int i=0; i<vector_len; i++)		//init
			mid_grad[tid][i] = 0;
		for(int i=0; i<vector_len; i++)
		{
			der_1 = -rate*negErrorVec[tid][i];
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * mid_head_vec[tid][0][ii];
				mid_grad[tid][ii] += der_1 * type_mat[tempType][i][ii];
			}
		}
		for(int i=0; i<vector_len; i++)
		{
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += mid_grad[tid][i] * entity_vec[e1_b][ii];
				entity_vec[e1_b][ii] += mid_grad[tid][i] * domain_mat[tempDomain][i][ii];
			}
		}
		//tail
		tempDomain = tail_domain_vec.get(rel_b);
		tempType = tail_type_vec.get(rel_b);
		for(int i=0; i<vector_len; i++)		//init
			mid_grad[tid][i] = 0;
		for(int i=0; i<vector_len; i++)
		{
			der_1 = rate*negErrorVec[tid][i];
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * mid_tail_vec[tid][0][ii];
				mid_grad[tid][ii] += der_1 * type_mat[tempType][i][ii];
			}
		}
		for(int i=0; i<vector_len; i++)
		{
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += mid_grad[tid][i] * entity_vec[e2_b][ii];
				entity_vec[e2_b][ii] += mid_grad[tid][i] * domain_mat[tempDomain][i][ii];
			}
		}
	}
	void train_triple_mul(int e1_a,int e2_a,int rel_a,int e1_b,int e2_b,int rel_b,int tid)		//margin-based score function
	{
		double sum1 = calc_sum_triple(e1_a,e2_a,rel_a,1,tid);		//positive score
		double sum2 = calc_sum_triple(e1_b,e2_b,rel_b,0,tid);		//negative score
		if (sum1+margin>sum2)
		{
			//res_thread_triple[tid]+=margin+sum1-sum2;
			res+=margin+sum1-sum2;
			gradient_triple( e1_a, e2_a, rel_a, e1_b, e2_b, rel_b, tid);
		}
	}
	//--------------------------------@8.29----  train_triple_mul ?????????????
	//calc entity representation
	public void calc_entity_vec(int head, int tail, int rel, int flag, int tid)	{	//use Recursive Hierarchy Encoder

		int tempHeadType = head_type_vec.get(rel);
		int tempTailType = tail_type_vec.get(rel);
		int tempHeadDomain = head_domain_vec.get(rel);
		int tempTailDomain = tail_domain_vec.get(rel);
		//build head_final_vec
		for(int i=0; i<vector_len; i++)
		{
			mid_head_vec[tid][flag][i] = 0;
			for(int ii=0; ii<vector_len; ii++)
			{
				mid_head_vec[tid][flag][i] += domain_mat[tempHeadDomain][i][ii] * entity_vec[head][ii];
			}
		}
		for(int i=0; i<vector_len; i++)
		{
			head_final_vec[tid][i] = 0;
			for(int ii=0; ii<vector_len; ii++)
			{
				head_final_vec[tid][i] += type_mat[tempHeadType][i][ii] * mid_head_vec[tid][flag][ii];
			}
		}
		//build tail_final_vec
		for(int i=0; i<vector_len; i++)
		{
			mid_tail_vec[tid][flag][i] = 0;
			for(int ii=0; ii<vector_len; ii++)
			{
				mid_tail_vec[tid][flag][i] += domain_mat[tempTailDomain][i][ii] * entity_vec[tail][ii];
			}
		}
		for(int i=0; i<vector_len; i++)
		{
			tail_final_vec[tid][i] = 0;
			for(int ii=0; ii<vector_len; ii++)
			{
				tail_final_vec[tid][i] += type_mat[tempTailType][i][ii] * mid_tail_vec[tid][flag][ii];
			}
		}
	}

	public double calc_sum_triple(int e1,int e2,int rel, int flag, int tid)		//similarity
	{
		double sum=0;
		calc_entity_vec(e1, e2, rel, flag, tid);//-RHE
	//	calc_entity_vec(e1, e2, rel, tid);//-WHE

		if(flag == 1)		//positive_sign
		{
			if (L1_flag)		//L1
			{
				for (int ii=0; ii<vector_len; ii++)
				{
					double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
					sum+=Math.abs(tempSum);//returns the absolute value
					if(tempSum > 0)
						posErrorVec[tid][ii] = 1;
					else
						posErrorVec[tid][ii] = -1;
				}
			}
			else		//L2
			{
				for (int ii=0; ii<vector_len; ii++)
				{
					double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
					sum+=sqr(tempSum);
					posErrorVec[tid][ii] = 2*tempSum;
				}
			}
			return sum;
		}
		else		//negative_sign
		{
			if (L1_flag)		//L1
			{
				for (int ii=0; ii<vector_len; ii++)
				{
					double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
					sum+=Math.abs(tempSum);
					if(tempSum > 0)
						negErrorVec[tid][ii] = 1;
					else
						negErrorVec[tid][ii] = -1;
				}
			}
			else		//L2
			{
				for (int ii=0; ii<vector_len; ii++)
				{
					double tempSum = tail_final_vec[tid][ii]-head_final_vec[tid][ii]-relation_vec[rel][ii];
					sum+=sqr(tempSum);
					negErrorVec[tid][ii] = 2*tempSum;
				}
			}
			return sum;
		}
	}

//-------------------@ 8.30
//calc entity representation
void calc_entity_vec(int head, int tail, int rel, int tid)		//use Weighted Hierarchy Encoder
{
	int tempHeadType = head_type_vec.get(rel);
	int tempTailType = tail_type_vec.get(rel);
	int tempHeadDomain = head_domain_vec.get(rel);
	int tempTailDomain = tail_domain_vec.get(rel);
	//build head_final_vec
	for(int i=0; i<vector_len; i++)
	{
		double type_score = 0, domain_score = 0;
		for(int ii=0; ii<vector_len; ii++)
		{
			type_score += type_mat[tempHeadType][i][ii] * entity_vec[head][ii];
		}
		for(int ii=0; ii<vector_len; ii++)
		{
			domain_score += domain_mat[tempHeadDomain][i][ii] * entity_vec[head][ii];
		}
		head_final_vec[tid][i] = type_score * type_weight + domain_score * (1-type_weight);
	}
	//build tail_final_vec
	for(int i=0; i<vector_len; i++)
	{
		double type_score = 0, domain_score = 0;
		for(int ii=0; ii<vector_len; ii++)
		{
			type_score += type_mat[tempTailType][i][ii] * entity_vec[tail][ii];
		}
		for(int ii=0; ii<vector_len; ii++)
		{
			domain_score += domain_mat[tempTailDomain][i][ii] * entity_vec[tail][ii];
		}
		tail_final_vec[tid][i] = type_score * type_weight + domain_score * (1-type_weight);
	}
}

}
