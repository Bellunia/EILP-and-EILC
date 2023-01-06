package other_algorithms.OpenIE;

    /* For representing a sentence that is annotated with pos tags and np chunks.*/

import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import gilp.comments.AnnotatedTriple;
import gilp.rdf3x.Triple;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;


public class ReVerbExample {
    public HashSet<AnnotatedTriple> reverbSentence(String sentence) throws IOException {
        // Looks on the classpath for the default model files.
     //   sentence = sentence.replace("(", "").replace(")", "");
     //   System.out.println("sentence=" + sentence);

        OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();
        ChunkedSentence sent = chunker.chunkSentence(sentence);//sentStr
        HashSet<AnnotatedTriple> extractAnnotatedTriples=new HashSet<AnnotatedTriple>();

        // Prints out extractions from the sentence.
        ReVerbExtractor reverb = new ReVerbExtractor();
        ConfidenceFunction confFunc = new ReVerbOpenNlpConfFunction();
        for (ChunkedBinaryExtraction extr : reverb.extract(sent)) {
            double conf = confFunc.getConf(extr);
            String subject=extr.getArgument1().toString().replace(" ","_");
            String predicate=extr.getRelation().toString().replace(" ","_");
            String object= extr.getArgument2().toString().replace(" ","_");

            Triple element = new Triple(subject,predicate,object);

            AnnotatedTriple triple= new AnnotatedTriple(element,conf);
            extractAnnotatedTriples.add(triple);
          //  System.out.println("Arg1=" + extr.getArgument1());
           // System.out.println("Rel=" + extr.getRelation());
          //  System.out.println("Arg2=" + extr.getArgument2());
         //   System.out.println("Conf=" + conf);

            System.out.println("element=" + triple);
        }

        return extractAnnotatedTriples;
    }


    public static HashSet<String> readStopWords(String pathToFBfile) {

        HashSet<String> types = new HashSet<String>();
        BufferedReader read = null;
        try {
            read = new BufferedReader(new InputStreamReader(
                    new FileInputStream(pathToFBfile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line="";
        while (true) {
            try {
                assert read != null;
                if ((line = read.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            types.add(line);
        }
        try {
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return types;
    }

    public static void main(String[] args) throws Exception {

        String path="./data/stopwords/english";
        HashSet<String>  stopwords= readStopWords(path);

            String sentStr = "U.S. president Barack Obama gave his inaugural address on January 20, 2013.";//Michael McGinn is the mayor of Seattle.
String test1="Italy (Italian: Italia [iˈtaːlja] ()), officially the Italian Republic (Italian: Repubblica Italiana [reˈpubblika itaˈljaːna]), is a country consisting of a peninsula delimited by the Alps and surrounded by several islands. Italy is located in the centre of Southern Europe, and it is also considered a part of western Europe. A unitary parliamentary republic with its capital in Rome, the country covers a total area of 301,340 km2 (116,350 sq mi) and shares land borders with France, Switzerland, Austria, Slovenia, and the enclaved microstates of Vatican City and San Marino. Italy has a territorial exclave in Switzerland (Campione) and a maritime exclave in Tunisian waters (Lampedusa). With around 60 million inhabitants, Italy is the third-most populous member state of the European Union.";
//   for(String word: stopwords){
//       test1= test1.replace(word,"");
//   }
//   System.out.println(test1);

        new ReVerbExample().reverbSentence(test1);


        }
    }
    /*
    Output Columns:
    1. filename
    2. sentence number
    3. arg1
    4. rel
    5. arg2
    6. arg1 start
    7. arg1 end
    8. rel start
    9. rel end
    10. arg2 start
    11. arg2 end
    12. conf
    13. sentence words
    14. sentence pos tags
    15. sentence chunk tags
    16. arg1 normalized
    17. rel normalized
    18. arg2 normalized

     */
/***
 * original codes
 * //            // Looks on the classpath for the default model files.
 * //            OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();
 * //            ChunkedSentence sent = chunker.chunkSentence(test);//sentStr
 * //
 * //            // Prints out the (token, tag, chunk-tag) for the sentence
 * //          //  System.out.println(sentStr);
 * //            for (int i = 0; i < sent.getLength(); i++) {
 * //                String token = sent.getToken(i);
 * //                String posTag = sent.getPosTag(i);
 * //                String chunkTag = sent.getChunkTag(i);
 * //              //  System.out.println(token + " " + posTag + " " + chunkTag);
 * //            }
 * //
 * //            // Prints out extractions from the sentence.
 * //            ReVerbExtractor reverb = new ReVerbExtractor();
 * //            ConfidenceFunction confFunc = new ReVerbOpenNlpConfFunction();
 * //            for (ChunkedBinaryExtraction extr : reverb.extract(sent)) {
 * //                double conf = confFunc.getConf(extr);
 * //                System.out.println("Arg1=" + extr.getArgument1());
 * //                System.out.println("Rel=" + extr.getRelation());
 * //                System.out.println("Arg2=" + extr.getArgument2());
 * //                System.out.println("Conf=" + conf);
 * //            }
 */

