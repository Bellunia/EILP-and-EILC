package prediction_type;

import java.io.*;
import java.util.List;
import java.util.Map;

import static transX.GlobalValue.*;

public class Preprocess {
    public static int Read_Data(String file_name, Map<String, Integer> data2id, Map<Integer, String> id2data) throws IOException {
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

    public static int Read_Data(String file_name, Map<String, Integer> data2id) throws IOException {
        int count = 0;
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            data2id.put(split_data[0], Integer.valueOf(split_data[1]));
            count++;
        }
        return count;
    }

    public static void Read_Wr_File(String file_name, double[][][] wr_vec) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        for (int i = 0; i < relation_num; i++) {
            System.out.println(i);
            for (int j = 0; j < entity_dimension; j++) {
                line = reader.readLine();
                String[] line_split = line.split("\t");
                for (int k = 0; k < relation_dimension; k++) {
                    wr_vec[i][j][k] = Double.valueOf(line_split[j]);
                }
            }
        }
    }

    public static void Read_Vec_File(String file_name, double[][] vec) throws IOException {  //  读取向量文件
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;

        for (int i = 0; (line = reader.readLine()) != null; i++) {
            String[] line_split = line.split("\t");
            for (int j = 0; j < vector_len; j++) {
                vec[i][j] = Double.parseDouble(line_split[j]);
            }
        }
    }

    public static void Read_Vec_File(String file_name, double[][] vec, int veclen) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;

        for (int i = 0; (line = reader.readLine()) != null ; i++) {
            String[] line_split = line.split("\t");

            for (int j = 0; j < veclen; j++) {
                vec[i][j] = Double.parseDouble(line_split[j]);
            }

        }
    }

    //-----------------transH;transR;PTransE
    public void relation_add(Map<Integer, Integer> relation_num, int relation) {
        if (!relation_num.containsKey(relation)) {
            relation_num.put(relation, 0);
        }
        int count = relation_num.get(relation);
        relation_num.put(relation, count + 1);
    }

    public void map_add_value(Map<Integer, Integer> tmp_map, int id, int value) {
        if (!tmp_map.containsKey(id)) {
            tmp_map.put(id, 0);
        }
        int tmp_value = tmp_map.get(id);
        tmp_map.put(id, tmp_value + value);
    }
    //---------------transH;transR;PTransE

    public static void Write_Vec2File(String file_name, double[][] vec, int number) throws IOException {  // 将向量写出到相关文件中
        File f = new File(file_name);
        if (!f.exists()) {    // if file does not exist, then create it
            File dir = new File(f.getParent());
            dir.mkdirs();
            f.createNewFile();
        }
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < vector_len; j++) {//100
                String str = String.format("%.6f\t", vec[i][j]);
                writer.write(str);
            }
            writer.write("\n");
            writer.flush();
        }
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

    public static void Write_Vec2File(String file_name, List<String> fb_ht, List<String> fb_lt) throws IOException {
        File f = new File(file_name);
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        for (int i = 0; i < fb_ht.size(); i++) {
            String str = fb_ht.get(i) + "\t" + fb_lt.get(i) + "\n";
            writer.write(str);
            writer.flush();
        }
    }

}
