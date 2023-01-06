package horn_markov_logic_rules;

import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;

import org.apache.commons.collections.map.MultiValueMap;


import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Stream;

import static horn_markov_logic_rules.preprocessing.fun;

public class hornInference {

    public static String[] files = {"pxy-qxy", "pxy-qyx", "pxy-qxz-rzy", "pxy-qxz-ryz", "pxy-qzx-rzy", "pxy-qzx-ryz"};



    public static double opposite_product(List<Double> a) {
        double value=1;
        for(double i :a){
            value=value*(1-i);
        }
        return 1-value;//1 - np.prod(np.ones(len(a)) - a);
    }

    public static String rules = "./knowledgeClean-data/horn_rule";
    public static String output_folder = "./knowledgeClean-data/horn_rule";

    public static ArrayList<String[]> tsvr(File test2) {
        ArrayList<String[]> Data = new ArrayList<>(); //initializing a new ArrayList out of String[]'s
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(test2))) {
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


    public MultiValueMap retrieve(int t) {

      //  HashMap<Triple, Double> preds = new HashMap<>();// preds = dict()

        MultiValueMap preds = new MultiValueMap();



      //  Collection<String> coll = map.get(key);


        File file = new File(rules + "/rules-" + files[t] + ".tsv");
        ArrayList<String[]> allBodies = tsvr(file);

        for (String[] line : allBodies) {

            double weight = Double.parseDouble(line[0]);
            String head = line[1];
            int len1 = line.length - 3;
            String[] body = new String[3];
            for (int i = 0; i < len1 / 2; i++) {
                body[0] = line[3 + i * 2];
                body[1] = String.valueOf(HornRules.getChar(line[4 + i * 2], 1));//line[4+i*2][1]; //char ch = getChar(nodes[t], 0);
                body[2] = String.valueOf(HornRules.getChar(line[4 + i * 2], 3));//line[4+i*2][3];
            } //print head, body
            String bodies = "?" + body[1] + " <" + body[0] + "> ?" + body[2] + " . ";//    bodies += "?{} <{}> ?{} . ".format(b[1], b[0], b[2])

            int offset = 0;
            while (true) {
              String  query = "SELECT DISTINCT(?x) ?y WHERE { " + bodies + "MINUS { ?x <" + head + "> ?y } } LIMIT 10000 OFFSET " + offset;
                System.out.println( query);
                ArrayList<HashMap<String, String>> results = new Sparql()
                        .getResultsFromQuery(query);
                int result_len= results.size();
                System.out.println("result_len:"+ result_len);
                System.out.println("\t"+ results);

                for (HashMap<String, String> key : results) {
                    String x = key.get("x");
                    String y = key.get("y");
                    Triple triple= new Triple(x,head,y);
                   // print weight, triple

                    if(!preds.containsKey(triple)){

                        preds.put(triple,weight);
                    }
                    // print predictions[triple]
                }
                if (result_len == 10000)
                offset += 10000;
                else
                break;

            }
        }


        return preds;
    }


    public void inference(){


        int t=0;
     //   MultiValueMap predictions = new MultiValueMap();
        MultiValueMap preds=retrieve(t);
//        for(Object triple : preds.keySet()){
//            if(!predictions.containsKey(triple)){
//                predictions.put(triple, preds.get(triple));
//            }
//
//        }

        try {
            Writer writer =
                    new OutputStreamWriter(
                            new FileOutputStream(output_folder+"/predictions.txt"), Charset.forName("UTF-8"));
           for(Object p : preds.keySet())
                writer.write(p+"\t"+preds.get(p)+"\n");

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println( "Computing inference values...");




        HashMap<Triple, Double>  predictions_fun = new HashMap<>();

                for(Object triple :preds.keySet()){
                    List<Double> coll = (List<Double>) preds.get(triple);
                    int size= coll.size();
                    double mean= 0.0;

                        if ( fun.equals(InferType.A) ) {//'A' (average),

                         for(Object i: coll){
                             mean+=(double)i;
                         }
                            mean=mean/size;
                            predictions_fun.put((Triple) triple,  mean);
                       //     predictions_fun.put(triple,  np.mean(predictions.get(triple)));
                        }
                        double j=0.0;
                        if ( fun.equals(InferType.M))  {//

                            for(double i: coll) {if (i > j)  j = i; } //filter max values

                            predictions_fun.put((Triple) triple,  j);

                      //      predictions_fun.put(triple, np.max(predictions.get(triple)));
                        }
                        if ( fun.equals(InferType.P)) {//  'P' (opp.product)

                            predictions_fun.put((Triple) triple,  opposite_product(coll));
                     //       predictions_fun.put(triple, opposite_product(predictions.get(triple)));

                }

                    System.out.println("Number of predicted triples:"+ (predictions_fun).size());
                    System.out.println( "Saving predictions to file...");
                    try {
                        Writer writer =
                                new OutputStreamWriter(
                                        new FileOutputStream(output_folder+"/inferred_triples_"+fun+".txt"), Charset.forName("UTF-8"));

                            writer.write("\n");

                 //       for key, value in sorted(predictions_fun.iteritems(), key=lambda (k,v): (v,k), reverse=True):
                // print "%.3f\t%s" % (value, key)
                //        fout.write("%.3f\t%s\n" % (value, key))


                        writer.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }




    }
    }


}
