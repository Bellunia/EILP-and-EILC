package prediction_type.ConfE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static prediction_type.ConfE.Gradient.calc_sum2;

public class First {
    private List<Pair<Integer, Double>> score_list;
    private int count_true;
    
    First() {
        score_list = new ArrayList<>();
    }

    public void add1(int head, int tail, boolean flag) {
    	double sum = calc_sum2(head, tail);
		/* 
		 * if the type instance is positive,  we set flag=true
		 */
        if (flag) {
            score_list.add(new Pair<>(1, sum));
        } else {
            score_list.add(new Pair<>(0, sum));
        }
    }

    public void count_pass(int i, int j) {
    	count_true = i;
    }

    public void run() throws IOException {

        Collections.sort(score_list, (o1, o2) -> {
        	return o2.b.compareTo(o1.b);
		});
        
        for(Double rec = 0.0; rec < 0.9; rec += 0.05) {
        	int p_count = 0;
        	Double tp_count = 0.0;
        	Double recall = 0.0;
        	Double precision = 0.0;
        	for(int id = 0; id < score_list.size(); id ++) {
        		p_count += 1;
        		tp_count += score_list.get(id).a;
            	recall = tp_count / count_true;
            	if((rec - recall) < 0.00001) {
            		precision = tp_count / p_count;
                    System.out.printf("Recall:\t%s\tPrecision:\t%s\n", rec, precision);
            		break;
            	}
        	}
        }
    }

}
