package prediction_type.ConfE;


import java.io.*;
import java.util.*;

import static prediction_type.ConfE.GlobalValue.type_vector_len;
import static prediction_type.ConfE.GlobalValue.vector_len;


public class Utils {

    static Random random = new Random();
    static double PI = Math.PI;

    public static double sqrt(double x) {
        return Math.sqrt(x);
    }

    public static double sqr(double x) {
        return x * x;
    }

    public static double abs(double x) {
        return Math.abs(x);
    }

    public static double exp(double x) {
        return Math.exp(x);
    }

    public static double sigmod(double x) {
        return 1.0 / (1 + exp(-x));
    }

    public static double normal(double x) {
        // Standard Gaussian distribution--sigma =1.
        return exp(-0.5 * sqr(x)) / sqrt(2 * PI);
    }

    public static double normal(double x, double miu, double sigma) {
        return 1.0 / Math.sqrt(2 * PI) / sigma * Math.exp(-1 * (x - miu) * (x - miu) / (2 * sigma * sigma));
    }

    public static int rand() {
        return random.nextInt(32767);
    }

    public static double uniform(double min, double max) { //random number in the interval of [min,max]
        // generate a float number which is in [min, max), refer to the Python uniform
        return min + (max - min) * Math.random();
    }

    public static double vec_len(double[] a, int vec_size) {
        // calculate the length of the vector
        double res = 0;
        for (int i = 0; i < vec_size; i++) {
            res += sqr(a[i]);
        }
        return sqrt(res);
    }

    public static void norm(double[] a, int vec_size) {
        // limit the element a under 1
        double x = vec_len(a, vec_size);
        if (x > 1) {
            for (int i = 0; i < vec_size; i++) {
                a[i] /= x;
            }
        }
    }

    public static void norm(double[] a, double[][] A, double lr) {
        // limit the element a under 1
        while (true) {
            double x = 0;
            for (int ii = 0; ii < vector_len; ii++) {
                double tmp = 0;
                for (int jj = 0; jj < type_vector_len; jj++)
                    tmp += A[jj][ii] * a[jj];
                x += sqr(tmp);
            }
            if (x > 1) {
                double lambda = 1;
                for (int ii = 0; ii < vector_len; ii++) {
                    double tmp = 0;
                    for (int jj = 0; jj < type_vector_len; jj++)
                        tmp += A[jj][ii] * a[jj];
                    tmp *= 2;
                    for (int jj = 0; jj < type_vector_len; jj++) {
                        A[jj][ii] -= lr * lambda * tmp * a[jj];
                        a[jj] -= lr * lambda * tmp * A[jj][ii];
                    }
                }
            } else
                break;
        }
    }

    public static int rand_max(int x) {
        // get a random number between (0, x)
        int res = (rand() * rand()) % x;
        while (res < 0) {
            res += x;
        }
        return res;
    }
    //----------------------
    public boolean cmp(Mix a, Mix b) {
        return a.number > b.number;
    }

    boolean my_cmp(Pair<Double, Integer> a, Pair<Double, Integer> b)
    {

        return a.a>b.a;
    }

    boolean cmp(Pair<Double, Integer> a, Pair<Double, Integer> b)
    {
        return a.b<b.b;
    }

    //-----------------------------------------


    public static void Read_Data(String file_name, Map<String, Integer> data2id, Integer data_num) throws IOException {

        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        data_num = Integer.valueOf(reader.readLine());
        while ((line = reader.readLine()) != null) {

            String[] split_data = line.split("\t");

            data2id.put(split_data[0], Integer.valueOf(split_data[1]));

        }

    }
    public static void Read_Data(String file_name, Map<Integer, Integer> data2id) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        //   data_num=Integer.valueOf(reader.readLine());
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            data2id.put(Integer.valueOf(split_data[0]), Integer.valueOf(split_data[1]));
        }
    }

    public static void Read_Data2(String file_name, Map<Integer, HashSet<Integer>> typeEntity) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            int distance = split_data.length;
            HashSet<Integer> entities = new HashSet<>();
            for (int i = 1; i < distance; i++)
                entities.add(Integer.valueOf(split_data[i]));
            typeEntity.put(Integer.valueOf(split_data[0]), entities);
        }
    }

    public static void Read_Data3(String file_name, HashSet<String[]> relationDomain) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            relationDomain.add(split_data);
        }
    }



    public static void Read_IntData(String file_name, Map<Integer, Integer> data2id, Integer data_num) throws IOException {


        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        data_num = Integer.parseInt(reader.readLine());
        while ((line = reader.readLine()) != null) {

            String[] split_data = line.split("\t");

            data2id.put(Integer.valueOf(split_data[0]), Integer.valueOf(split_data[1]));

        }

    }


    public static int Read_idData(String file_name, Map<String, Integer> data2id, Map<Integer, String> id2data) throws IOException {
        int count = 0;
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            data2id.put(split_data[0], Integer.valueOf(split_data[1]));
            id2data.put(Integer.valueOf(split_data[1]), split_data[0]);
            count++;
        }

        return count;
    }


    public static void Read_Vec_File(String file_name, double[][] vec, int veclen) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        for (int i = 0; (line = reader.readLine()) != null; i++) {
            String[] line_split = line.split("\t");
            for (int j = 0; j < veclen; j++) {
                vec[i][j] = Double.valueOf(line_split[j]);
            }
        }
    }



    public static void Read_Triple(String file_name, Map<Pair<Integer, Integer>, Set<Integer>> triples, Integer triple_num) throws IOException {
        // triple2id.txt: [head_instance_id tail_instance_id relation_id].
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        triple_num=Integer.valueOf(reader.readLine());
        while ((line = reader.readLine()) != null) {

            String[] split_data = line.split("\t");
            int head=Integer.parseInt(split_data[0]);
            int relation=Integer.parseInt(split_data[1]);
            int tail= Integer.parseInt(split_data[2]);

            Pair<Integer, Integer> key = new Pair<>(head, relation);
            if (!triples.containsKey(key)) {
                triples.put(key, new HashSet<>());//这个head_relation组合都有和哪些tail中组合过
            }
            Set<Integer> tail_set = triples.get(key);
            tail_set.add(tail);  //获得了对应的tail在tail2id中的位置


        }

    }

    public static void Read_Triple(String file_name, List<Tri> testList, Integer testTotal) throws IOException {
        // triple2id.txt: [head_instance_id tail_instance_id relation_id].
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        testTotal = Integer.parseInt(reader.readLine());
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            int head = Integer.parseInt(split_data[0]);
            int relation = Integer.parseInt(split_data[1]);
            int tail = Integer.parseInt(split_data[2]);
            Tri triple = new Tri(head, relation, tail);

            if (!testList.contains(triple)) {
                testList.add(triple);
            }
        }

    }

    public static void Write_Vec2FileArray(String file_name, double[][] vec, int number, int veclen) throws IOException {
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

    public static void Write_Vec2FileArray(String file_name, double[] vec, int num) throws IOException {
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
}
