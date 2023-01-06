package gilp.utils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Csv2Rdf {
    private static String FILE_PATH, FILE_PATH2;
    private ArrayList<String> cropList;
    private ArrayList<String> readList;

    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        FILE_PATH = sc.nextLine();
        FILE_PATH2 = sc.nextLine();
        Csv2Rdf cr = new Csv2Rdf();
        cr.Read();//subjects
        cr.Read2();//objects
        cr.Converting();

    }

    private void Read()
    {
        int i = 0;
        cropList = new ArrayList<String>();
        ArrayList<String> varietyList = new ArrayList<String>();
        File file = new File(FILE_PATH);
        if(file.exists()) {
            try {
                FileReader filereader = new FileReader(file);
                BufferedReader br = new BufferedReader(filereader);
                String read = "";
                int j =0;
                while((read = br.readLine())!=null) {
                    if(j ==0 ) {
                        j++;
                    }
                    else {
                        System.out.println(read);
                        String temp[] = read.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                        cropList.add(read.trim().replaceAll(" ", "_"));
                        //varietyList.add(temp[2].trim().replaceAll(" ", "_"));
                    }
                }
                System.out.println(cropList);
            }
            catch(IOException io) {
                io.printStackTrace();
            }
        }

        else {
            System.out.println("File is missing");
        }
    }
    private void Read2()
    {
        int i = 0;
        readList = new ArrayList<String>();
        File file = new File(FILE_PATH2);
        if(file.exists()) {
            try {
                FileReader filereader = new FileReader(file);
                BufferedReader br = new BufferedReader(filereader);
                String read = "";
                int j =0;
                while((read = br.readLine())!=null) {
                    if(j ==0 ) {
                        j++;
                    }
                    else {
                        System.out.println(read);
                        readList.add(read.trim().replaceAll(" ", "_"));
                    }
                }
                System.out.println(readList);
            }
            catch(IOException io) {
                io.printStackTrace();
            }
        }

        else {
            System.out.println("File is missing");
        }
    }
    //http://www.farmingonto.bodige#crop
    //http://www.farmingonto.bodige#type_of_seed
    //Type = http://www.w3.org/1999/02/22-rdf-syntax-ns#type
    private void Converting()
    {
        Model mymodel = ModelFactory.createDefaultModel();
        Property rdfType = mymodel.createProperty("http://www.farmingonto.bodige#cost_of_cultivation"); //Movie
        for(int i = 1;i<cropList.size();i++) {
            Resource actor = mymodel.createResource("http://www.farmingonto.bodige#"+cropList.get(i));
            //Resource variety = mymodel.createResource("http://www.farmingonto.bodige#"+readList.get(i));
            actor.addProperty(rdfType, readList.get(i));
        }
        mymodel.write(System.out, "TURTLE");


        try {
            Writer wr = new FileWriter("./Mydata17.ttl");
            mymodel.write(wr,"TURTLE");}
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}

