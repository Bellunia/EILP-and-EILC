package transX;


import java.util.Scanner;


public class Main {

  //  public static int EmbeddingAlgorithm = 1;
    //1-transE,2-PTransE;3-transH;4--transR
    public static void main(String[] args) throws Exception {

        System.out.println("Train or test? y/n");
        Scanner sc = new Scanner(System.in);
        boolean train_flag;
        train_flag = sc.next().equals("y");

        System.out.println("Which Embedding methods?");

        if (train_flag) {
            TrainRun trainRun = new TrainRun();
            trainRun.train_run();
        } else {
            TestRun testRun = new TestRun();
            testRun.test_run();
        }
        }

    }