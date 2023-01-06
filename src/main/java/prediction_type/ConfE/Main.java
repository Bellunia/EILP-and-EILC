package prediction_type.ConfE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static prediction_type.ConfE.GlobalValue.*;
import static prediction_type.ConfE.GlobalValue.type_num;
import static prediction_type.ConfE.Gradient.calc_sum2;
import static prediction_type.Preprocess.Read_Vec_File;

public class Main {

    public static void main(String[] args) throws IOException {
      // do the test of yago+concepts

        System.out.println("Which task do you want to conduct? Please reply in N (for noise detection) or P (for type prediction)");
        Scanner sc = new Scanner(System.in);
        boolean task_flag = sc.next().equals("N");
        if (task_flag) {
            System.out.println("Begin noise detection");
            TrainRun.train_run();
            TestRun.test_run();
        } else {
        	System.out.println("Begin type perdiction");
            TrainRun.train_run();
            FirstRun.first_run();
        }
    }


    public static void trustE_Main(String[] args) throws IOException {
        System.out.println("Train or test? y/n");
        Scanner sc = new Scanner(System.in);
        boolean train_flag;

        train_flag = sc.next().equals("y");
        if (train_flag) {
            System.out.println("Begin train");
            TrainRun.train_run();
            //TestRun.test_run();
        } else {
            System.out.println("Begin test");
            TestRun.test_run();

        }
    }

    public void run() throws IOException {
       double[][] entity_vec = new double[14951][50];
        double[][] type_vec = new double[3851][30];
        double[][]  A = new double[30][50];

        Read_Vec_File("./type/resource/data/entity2vec.bern", entity_vec, 50);
     //   Read_Vec_File("./type/resource/data/type2vec.bern", type_vec, 30);
     //   Read_Vec_File("./type/resource/data/A.bern", A, 50);

        int raw_rank = 1, filter_rank = 1, raw_hit10 = 0, filter_hit10 = 0;
        double raw_mrr = 0, filter_mrr = 0;


        System.out.printf("Total double iterations\n");

         }

}
