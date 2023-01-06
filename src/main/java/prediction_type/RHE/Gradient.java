package prediction_type.RHE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static prediction_type.ConfE.Utils.*;
import static prediction_type.RHE.GlobalValue.*;



public class






















































Gradient {

	//-----------------@8.30
	double calc_sum3(int e1,int e2,int rel) {
		//calc sum
		double sum=0;
		if (L1_flag)
			for (int ii=0; ii<vector_len; ii++)
				sum+=-abs(tail_entity_vec[e2][rel][ii]-head_entity_vec[e1][rel][ii]-relation_vec[rel][ii]);
		else
			for (int ii=0; ii<vector_len; ii++)
				sum+=-sqr(tail_entity_vec[e2][rel][ii]-head_entity_vec[e1][rel][ii]-relation_vec[rel][ii]);
		return sum;
	}

	void gradient_triple(int e1_a,int e2_a,int rel_a,int e1_b,int e2_b,int rel_b,int tid)		//SGD update
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
		for(int i=0; i<vector_len; i++)
		{
			der_1 = rate*posErrorVec[tid][i]*(1-type_weight);
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += der_1 * entity_vec[e1_a][ii];
				entity_vec[e1_a][ii] += der_1 * domain_mat[tempDomain][i][ii];
			}
			der_1 = rate*posErrorVec[tid][i]*type_weight;
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * entity_vec[e1_a][ii];
				entity_vec[e1_a][ii] += der_1 * type_mat[tempType][i][ii];
			}
		}
		//tail
		tempDomain = tail_domain_vec.get(rel_a);
		tempType = tail_type_vec.get(rel_a);
		for(int i=0; i<vector_len; i++)
		{
			der_1 = -rate*posErrorVec[tid][i]*(1-type_weight);
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += der_1 * entity_vec[e2_a][ii];
				entity_vec[e2_a][ii] += der_1 * domain_mat[tempDomain][i][ii];
			}
			der_1 = -rate*posErrorVec[tid][i]*type_weight;
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * entity_vec[e2_a][ii];
				entity_vec[e2_a][ii] += der_1 * type_mat[tempType][i][ii];
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
		for(int i=0; i<vector_len; i++)
		{
			der_1 = -rate*negErrorVec[tid][i]*(1-type_weight);
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += der_1 * entity_vec[e1_b][ii];
				entity_vec[e1_b][ii] += der_1 * domain_mat[tempDomain][i][ii];
			}
			der_1 = -rate*negErrorVec[tid][i]*type_weight;
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * entity_vec[e1_b][ii];
				entity_vec[e1_b][ii] += der_1 * type_mat[tempType][i][ii];
			}
		}
		//tail
		tempDomain = tail_domain_vec.get(rel_b);
		tempType = tail_type_vec.get(rel_b);
		for(int i=0; i<vector_len; i++)
		{
			der_1 = rate*negErrorVec[tid][i]*(1-type_weight);
			for(int ii=0; ii<vector_len; ii++)
			{
				domain_mat[tempDomain][i][ii] += der_1 * entity_vec[e2_b][ii];
				entity_vec[e2_b][ii] += der_1 * domain_mat[tempDomain][i][ii];
			}
			der_1 = rate*negErrorVec[tid][i]*type_weight;
			for(int ii=0; ii<vector_len; ii++)
			{
				type_mat[tempType][i][ii] += der_1 * entity_vec[e2_b][ii];
				entity_vec[e2_b][ii] += der_1 * type_mat[tempType][i][ii];
			}
		}
	}
}