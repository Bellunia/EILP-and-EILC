package gilp.KBC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Arrays;
import org.apache.commons.lang.ArrayUtils;

public class ReadFiles {

    public static void readFiles() throws IOException {

        File dir = new File("/home/wy/Downloads/test/");

        int j = 0;

        for (File file : dir.listFiles()) {

            HashMap<String, String[]> elements = new HashMap<String, String[]>();

            String relation = null;

            String fileName = file.getName();

            Scanner input = new Scanner(file);

            Writer writer = new OutputStreamWriter(new FileOutputStream("/home/wy/Downloads/test1/" + fileName),
                    Charset.forName("UTF-8"));

            relation = input.nextLine();

            while (input.hasNextLine()) {

                String line = input.nextLine();
                if (line != relation) {

                    String[] columns = line.split("\t");

                    String id = columns[2];

                    //columns = (String[]) ArrayUtils.remove(columns, 0);

                    elements.put(id, columns);
                }

            }

            String[] relations = relation.split("\t");

            int order=relations.length;

            for(String key:elements.keySet() ) {

                for (int i=0;i<order;i++) {
                    if(relations[i]!=null && elements.get(key)[i]!=null)

                        writer.write(key.replaceAll(" ", "_").replaceAll(",", "_")+"\t"+relations[i].replaceAll(" ", "_").replaceAll(",", "_")+"\t"+elements.get(key)[i].replaceAll(" ", "_").replaceAll(",", "_")+"\t"+"."+"\n");


                }
            }

            writer.write("       \n");

            input.close();
            writer.close();
            j++;
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

    public static Integer extractNumber(String s) {

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(s);

        return Integer.parseInt(m.group());
    }

    public static void main(String[] args) throws Exception {

        readFiles();
    }

}
