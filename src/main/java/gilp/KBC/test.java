package gilp.KBC;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.util.FileUtils;

public class test {

    public static void readFiles() throws IOException {

        HashMap<Integer, String> negative = new HashMap<Integer, String>();

        HashMap<Integer, String> positive = new HashMap<Integer, String>();

        HashMap<Integer, ArrayList<String>> negativePrediction = new HashMap<Integer, ArrayList<String>>();

        HashMap<Integer, ArrayList<String>> positivePrediction = new HashMap<Integer, ArrayList<String>>();

        File dir = new File("/home/wy/Downloads/final-results/40comments-3min,1s/");

        for (File file : dir.listFiles()) {

            String fileName = file.getName();

            List<Integer> order = extractNumbers(fileName);
            System.out.print(order + "\n");

            int order1 = order.get(0).intValue();

            Scanner input = new Scanner(file);

            if (fileName.contains("false")) {

                // String line3 = (String) FileUtils.readLines(file).get(3);
                //
                ArrayList<String> test = new ArrayList<String>();
                ArrayList<String> test1 = new ArrayList<String>();

                while (input.hasNextLine()) {

                    String line = input.nextLine();

                    test.add(line);

                }

                String ruleSize = test.get(3).split("\t")[1];
                String precision = test.get(test.size() - 5).split("\t")[1];
                String lower = test.get(test.size() - 4).split("\t")[1];
                String upper = test.get(test.size() - 3).split("\t")[1];

                String predictions = test.get(test.size() - 1).split("\t")[1];
                test1.add(precision);
                test1.add(lower);
                test1.add(upper);
                test1.add(predictions);

                negative.put(order1, ruleSize);

                negativePrediction.put(order1, test1);

            }
            if (fileName.contains("true")) {

                ArrayList<String> test = new ArrayList<String>();
                ArrayList<String> test1 = new ArrayList<String>();

                while (input.hasNextLine()) {

                    String line = input.nextLine();

                    test.add(line);

                }

                String ruleSize = test.get(3).split("\t")[1];

                String predictions = test.get(test.size() - 1).split("\t")[1];
                //String ruleSize = test.get(3).split("\t")[1];
                String precision = test.get(test.size() - 5).split("\t")[1];
                String lower = test.get(test.size() - 4).split("\t")[1];
                String upper = test.get(test.size() - 3).split("\t")[1];

                //String predictions = test.get(test.size() - 1).split("\t")[1];
                test1.add(precision);
                test1.add(lower);
                test1.add(upper);
                test1.add(predictions);

                positive.put(order1, ruleSize);

                positivePrediction.put(order1, test1);

                input.close();
            }

        }

        Writer writer = new OutputStreamWriter(new FileOutputStream("/home/wy/Downloads/t1.txt"),
                Charset.forName("UTF-8"));
        writer.write(" Negative-rule\tprecision\tlower\tupper\tNegative-predictions\tPositive-rule\tprecision\tlower\tupper\tPositive-prediction\n");
        for (int j = 0; j < Math.min(negative.size(), positive.size()); j++) {

            writer.write(negative.get(j + 1) + "\t" + negativePrediction.get(j + 1).get(0) + "\t"
                    + negativePrediction.get(j + 1).get(1) + "\t"
                    + negativePrediction.get(j + 1).get(2) + "\t"
                    + negativePrediction.get(j + 1).get(3) + "\t");
            writer.write(positive.get(j + 1) + "\t" + positivePrediction.get(j + 1).get(0)
                    + "\t" + positivePrediction.get(j + 1).get(1)
                    + "\t" + positivePrediction.get(j + 1).get(2)
                    + "\t" + positivePrediction.get(j + 1).get(3)
                    + "\n");

        }

        writer.close();

    }

    public static void readFiles1() throws IOException {

        File dir = new File("/home/wy/Downloads/final-results/");
///home/wy/Downloads/final-results/10comments-4 min, 12 s/GILP_hasGivenName_1_false.tsv

        for (File file : dir.listFiles()) {// final-results/

            String fileName = file.getName();

            HashMap<Integer, String> negative = new HashMap<Integer, String>();

            HashMap<Integer, String> positive = new HashMap<Integer, String>();

            HashMap<Integer, String> negativePrediction = new HashMap<Integer, String>();

            HashMap<Integer, String> positivePrediction = new HashMap<Integer, String>();

            if (file.isDirectory()) {
                File[] listOfFiles = file.listFiles();


                for (File subfile : listOfFiles) {// 10comments-4 min, 12 s/
                    String subfileName = subfile.getName();

                    List<Integer> order = extractNumbers(subfileName);
                    //System.out.print(order+"\n");

                    int order1 = order.get(0).intValue();

                    Scanner input = new Scanner(subfile);

                    if (subfileName.contains("false")) {



                        ArrayList<String> test = new ArrayList<String>();

                        while (input.hasNextLine()) {

                            String line = input.nextLine();

                            test.add(line);

                        }

                        String ruleSize = test.get(3).split("\t")[1];

                        String predictions = test.get(test.size() - 1).split("\t")[1];

                        negative.put(order1, ruleSize);

                        negativePrediction.put(order1, predictions);

                    }
                    if (subfileName.contains("true")) {

                        ArrayList<String> test = new ArrayList<String>();

                        while (input.hasNextLine()) {

                            String line = input.nextLine();

                            test.add(line);

                        }

                        String ruleSize = test.get(3).split("\t")[1];

                        String predictions = test.get(test.size() - 1).split("\t")[1];

                        positive.put(order1, ruleSize);

                        positivePrediction.put(order1, predictions);

                        input.close();
                    }

                }
                Writer writer = new OutputStreamWriter(
                        new FileOutputStream("/home/wy/Downloads/test-results/" + fileName + ".tsv"),
                        Charset.forName("UTF-8"));

                writer.write(" Negative-rule\tNegative-predictions\tPositive-rule\tPositive-prediction\n");
                for (int j = 0; j < Math.min(negative.size(), positive.size()); j++) {

                    writer.write(negative.get(j + 1) + "\t" + negativePrediction.get(j + 1) + "\t");
                    writer.write(positive.get(j + 1) + "\t" + positivePrediction.get(j + 1) + "\n");

                }

                writer.close();

            }
        }

    }

    public static List<Integer> extractNumbers(String s) {
        List<Integer> numbers = new ArrayList<Integer>();

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(s);

        while (m.find()) {
            numbers.add(Integer.parseInt(m.group()));
        }
        return numbers;
    }

    public static void main(String[] args) throws Exception {

        readFiles();
    }



}