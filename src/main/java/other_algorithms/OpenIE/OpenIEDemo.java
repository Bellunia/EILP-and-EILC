package other_algorithms.OpenIE;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.OpenIE;
import edu.stanford.nlp.naturalli.SentenceFragment;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * A demo illustrating how to call the OpenIE system programmatically.
 * You can call this code with:
 *
 * <pre>
 *   java -mx1g -cp stanford-openie.jar:stanford-openie-models.jar edu.stanford.nlp.naturalli.OpenIEDemo
 * </pre>
 *
 */
public class OpenIEDemo {

    private OpenIEDemo() {} // static main

    public static void main(String[] args) throws Exception {
        // Create the Stanford CoreNLP pipeline
        Properties props = PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie"
        );
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Annotate an example document.
        String text;
        if (args.length > 0) {
            text = IOUtils.slurpFile(args[0]);
        } else {
//            text =// "George Dixon (July 29, 1870 – January 6, 1908) was a Black Canadian professional boxer. He was the first black world boxing champion in any weight class, while also being the first ever Canadian-born boxing champion. Ring Magazine founder Nat Fleischer ranked Dixon as the #1 Featherweight of all-time. Dixon was inducted posthumously into Canada's Sports Hall of Fame in 1955. He was also inducted into the Ring Magazine Hall of Fame in 1956 and into the International Boxing Hall of Fame as a first-class inductee in 1990.";
//                    "Obama was born in Hawaii. He is our president.";
            text=
                   // "The English are a nation and an ethnic group native to England, who speak the English language. The English identity is of early medieval origin, when they were known in Old English as the Angelcynn (\"family of the Angles\"). Their ethnonym is derived from the Angles, one of the Germanic peoples who migrated to Great Britain around the 5th century AD. England is one of the countries of the United Kingdom. Historically, the English population is descended from several peoples — the earlier Britons (or Brythons) and the Germanic tribes that settled in Britain following the withdrawal of the Romans, including Angles, Saxons, Jutes and Frisians. Collectively known as the Anglo-Saxons, they founded what was to become England (from the Old English Englaland) along with the later Danes, Normans and other groups. In the Acts of Union 1707, the Kingdom of England was succeeded by the Kingdom of Great Britain. Over the years, English customs and identity have become fairly closely aligned with British customs and identity in general. Today many English people have recent forebears from other parts of the United Kingdom, while some are also descended from more recent immigrants from other European countries and from the Commonwealth. The English people are the source of the English language, the Westminster system, the common law system and numerous major sports such as cricket, football, rugby union, rugby league and tennis. These and other English cultural characteristics have spread worldwide, in part as a result of the former British Empire. "
            //
                    "Italian (italiano, [itaˈljaːno] () or lingua italiana, [ˈliŋɡwa itaˈljaːna]) is a Romance language of the Indo-European language family. Italian descended from the Vulgar Latin of the Roman Empire and, together with Sardinian, is by most measures the Romance language closest to it. Italian is an official language in Italy, Switzerland (where it is the main language of Ticino and the Graubünden valleys of Calanca, Mesolcina, Bregaglia and val Poschiavo), San Marino and Vatican City. It has an official minority status in western Istria (Croatia and Slovenia). It formerly had official status in Albania, Malta, Monaco, Montenegro (Kotor) and Greece (Ionian Islands and Dodecanese) and is generally understood in Corsica (due to its close relation with the Tuscan-influenced local language) and Sa"
            ;


        }
        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);

        // Loop over sentences in the document
        int sentNo = 0;
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("Sentence #" + ++sentNo + ": " + sentence.get(CoreAnnotations.TextAnnotation.class));

            // Print SemanticGraph
            System.out.println(sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));

            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);

            // Print the triples
            for (RelationTriple triple : triples) {
                System.out.println(triple.confidence + "\t" +
                        triple.subjectGloss() + "\t" +
                        triple.relationGloss() + "\t" +
                        triple.objectGloss());
            }

            // Alternately, to only run e.g., the clause splitter:
            List<SentenceFragment> clauses = new OpenIE(props).clausesInSentence(sentence);
            for (SentenceFragment clause : clauses) {
                System.out.println(clause.parseTree.toString(SemanticGraph.OutputFormat.LIST));
            }
            System.out.println();
        }
    }

}
