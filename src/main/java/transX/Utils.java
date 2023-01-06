package transX;

import java.util.Random;

import static transX.GlobalValue.*;

public class Utils {
    static Random random = new Random();
    static double PI = Math.PI;

    public static double sqrt(double x) {
        return Math.sqrt(x);
    }

    public  static double sqr(double x) {
        return x * x;
    }

    public  static double abs(double x) {
        return Math.abs(x);
    }

    public  static double exp(double x) {
        return Math.exp(x);
    }

    public static double normal(double x) {
        // Standard Gaussian distribution
        return exp(-0.5 * sqr(x)) / sqrt(2 * PI);
    }

    public  static int rand() {
        return random.nextInt(32767);
    }

    public static double uniform(double min, double max) {
        // generate a float number which is in [min, max), refer to the Python uniform
        return min + (max - min) * Math.random();
    }

    public  static int rand_max(int x) {
        // get a random number between (0, x)
        int res = (rand() * rand()) % x;
        while (res < 0) {
            res += x;
        }
        return res;
    }

    //----transE;PTransE
    /**
     * a-L2 norm
     * L2范数是指向量a各元素的平方和然后求平方根
     */
    public  static double vec_len(double[] a) { //
        // calculate the length of the vector
        double res = 0;
        for (int i = 0; i < vector_len; i++) {
            //vector_dimension--TransH    //relation_dimension---TransR
            res += sqr(a[i]);
        }
        return sqrt(res);
    }

    //------------TransH
    public static void norm(double[] a) {//正则化
        // limit the element a under 1
        double x = vec_len(a);
        if (x > 1) {
            for (int i = 0; i < vector_len; i++) {
                a[i] /= x;
            }
        }
    }
    public  static void norm2one(double[] a) {
        // limit the element equal 1
        double x = vec_len(a);
        for (int i = 0; i < vector_len; i++) {//    //vector_dimension---TransH
            a[i] /= x;
        }
    }
    public  static void norm(double[] a, double[] Wr) {
        double sum = 0;
        while (true) {
            for (int i = 0; i < vector_len; i++) {
                sum += sqr(Wr[i]);
            }
            sum = sqrt(sum);
            for (int i = 0; i < vector_len; i++) {
                Wr[i] /= sum;
            }

            double x = 0;
            for (int i = 0; i < vector_len; i++) {
                x += Wr[i] * a[i];
            }
            if (x > 0.1) {
                for (int i = 0; i < vector_len; i++) {
                    double tmp = a[i];
                    a[i] -= learning_rate * Wr[i];
                    Wr[i] -= learning_rate * tmp;
                }
            } else {
                break;
            }
        }
        norm2one(Wr);
    }
    //------------TransH


    //-----TransR
    static double vec_len_TransR(double[] a) {
        // calculate the length of the vector
        double res = 0;
        for (int i = 0; i < relation_dimension; i++) {
            res += sqr(a[i]);
        }
        return sqrt(res);
    }
    public  static void norm_TransR(double[] a) {
        // limit the element a under 1
        double x = vec_len_TransR(a);
        if (x > 1) {
            for (int i = 0; i < relation_dimension; i++) {
                a[i] /= x;
            }
        }
    }
    public static void norm_TransR(double[] a, double[][] Wr) {
        while (true) {
            double sum = 0;
            for (int i = 0; i < entity_dimension; i++) {
                double temp = 0;
                for (int j = 0; j < relation_dimension; j++) {
                    temp += Wr[i][j] * a[j];
                }
                sum += sqr(temp);
            }
            if (sum > 1) {
                for (int i = 0; i < entity_dimension; i++) {
                    double temp = 0;
                    for (int j = 0; j < relation_dimension; j++) {
                        temp += Wr[i][j] * a[j];
                    }
                    temp *= 2;
                    for (int j = 0; j < relation_dimension; j++) {
//                        double copy = Wr[j][i];
                        Wr[i][j] -= learning_rate * temp * a[j];
                        a[j] -= learning_rate * temp * Wr[i][j];
                    }
                }
            } else {
                break;
            }
        }
    }
    //-----TransR
}
