package gilp.utils;

import gilp.rdf3x.Triple;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AuxiliaryParameter {

    public HashSet<Triple> readTriples(String path) {
        HashSet<Triple> triples = new HashSet<>();
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(path))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t");
                Triple triple = new Triple(lineItems[0], lineItems[1], lineItems[2]);
                triples.add(triple);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return triples;
    }

    public static HashSet<String> singleLineTsv(String test2) {
        HashSet<String> Data = new HashSet<>(); //initializing a new ArrayList out of String[]'s
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(test2))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
                Data.add(lineItems[0]); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }

    public static ArrayList<String[]> tsvr(String test2) {
        ArrayList<String[]> Data = new ArrayList<>(); //initializing a new ArrayList out of String[]'s
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(new File(test2)))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
                Data.add(lineItems); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }

    public static HashMap<String, Integer> readCount(String pathToFBfile) {

        HashMap<String, Integer> types = new HashMap<>();
        BufferedReader read = null;
        try {
            read = new BufferedReader(new InputStreamReader(
                    new FileInputStream(pathToFBfile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = "";
        while (true) {
            try {
                assert read != null;
                if ((line = read.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!line.equals("\t")) {
                String[] lineSplits = line.split("\t");
                types.put(lineSplits[0], Integer.parseInt(lineSplits[1]));
            }
        }
        try {
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return types;
    }

    public static HashMap<String, String> readLabels(String pathToFBfile) {

        HashMap<String, String> types = new HashMap<>();
        BufferedReader read = null;
        try {
            read = new BufferedReader(new InputStreamReader(
                    new FileInputStream(pathToFBfile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = "";
        while (true) {
            try {
                assert read != null;
                if ((line = read.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!line.equals("\t")) {
                String[] lineSplits = line.split("\t");
                String string = lineSplits[1];

                types.put(lineSplits[0], string);
            }
        }
        try {
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return types;
    }

    public static HashMap<String, Integer> importNumbers(String path) {

        HashMap<String, Integer> counts = new HashMap<>();
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(path))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t");
                counts.put(lineItems[0], Integer.valueOf(lineItems[1]));
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return counts;
    }
}
