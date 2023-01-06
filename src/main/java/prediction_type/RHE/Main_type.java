package prediction_type.RHE;

import prediction_type.WHE.TestWHE;
import prediction_type.WHE.TrainWHE;

import java.io.IOException;
import java.util.Scanner;

public class Main_type {

    public static void main(String[] args) throws IOException {
        System.out.println("Which task do you want to conduct? " +
                "Please reply in N (for Recursive Hierarchy Encoder) or P (for Weighted Hierarchy Encoder)");
        Scanner sc = new Scanner(System.in);
        boolean task_flag = sc.next().equals("N");
        if (task_flag) {
            System.out.println("select Recursive Hierarchy Encoder to do type prediction");
            TrainRHE.train_run();
            TestRHE.test_run();
        } else {
            System.out.println("select Weighted Hierarchy Encoder to do type prediction");
            TrainWHE.train_run();
            TestWHE.first_run();
        }
    }

}
