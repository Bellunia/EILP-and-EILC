package other_algorithms.TextRanks;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class TextRank {
    public static void main(String args[]) {
    	String[] documents1 = {"I seem to have endless problems with one drive.  I wonder if anyone else does.......Often when I add files to one drive, on other computers those files appear during update and then emmedietely are moved into the trash bin. Now I notice that One Drive just does not update with ne files at all!",
    			"when I sign in to my Microsoft account it appears problem which contain: Microsoft account requires JavaScript to sign in. this web browser either does not support JavaScript, or scripts are being blocked> OT: Microsoft On Drive Dont work"};


		String[] documents = {"" +
				"" +
				"The English are a nation and an ethnic group native to England, who speak the English language. The English identity is of early medieval origin, when they were known in Old English as the Angelcynn (\"family of the Angles\"). Their ethnonym is derived from the Angles, one of the Germanic peoples who migrated to Great Britain around the 5th century AD. England is one of the countries of the United Kingdom. Historically, the English population is descended from several peoples — the earlier Britons (or Brythons) and the Germanic tribes that settled in Britain following the withdrawal of the Romans, including Angles, Saxons, Jutes and Frisians. Collectively known as the Anglo-Saxons, they founded what was to become England (from the Old English Englaland) along with the later Danes, Normans and other groups. In the Acts of Union 1707, the Kingdom of England was succeeded by the Kingdom of Great Britain. Over the years, English customs and identity have become fairly closely aligned with British customs and identity in general. Today many English people have recent forebears from other parts of the United Kingdom, while some are also descended from more recent immigrants from other European countries and from the Commonwealth. The English people are the source of the English language, the Westminster system, the common law system and numerous major sports such as cricket, football, rugby union, rugby league and tennis. These and other English cultural characteristics have spread worldwide, in part as a result of the former British Empire."

						};

    	StanfordLemmatizer slem = new StanfordLemmatizer();
    	List<ArrayList<String>> lemmatizedTickets = new ArrayList<ArrayList<String>>();
    	List<String> filteredWords = new ArrayList<String>();
    	
    	
    	// Stopwords removal
		Scanner sc = null;
		
		try {
			
			sc = new Scanner(new File("./data/SmartStoplist.txt"));
			sc.nextLine();
						
			Map<String, Integer> stopwords = new HashMap<String, Integer>();
			while(sc.hasNextLine()){
				stopwords.put(sc.nextLine(),1);
			} 
			
			for(String ticket:documents){
				ArrayList<String> lemmatizedTicket = slem.lemmatize(ticket);
	    		lemmatizedTickets.add(lemmatizedTicket);
				for(int i=0; i < lemmatizedTicket.size(); i++){
					if(stopwords.get(lemmatizedTicket.get(i)) == null){
						String word = lemmatizedTicket.get(i).toLowerCase();
						if(!filteredWords.contains(word))
							filteredWords.add(word);
						//System.out.print(stemmed_words[i]+" ");
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// view the filtered words,has only Nouns & Verbs
    	System.out.println(filteredWords);	
    	
        // constructs a directed graph with the specified vertices and edges
        DirectedGraph<String, DefaultEdge> directedGraph =
            new DefaultDirectedGraph<String, DefaultEdge>
            (DefaultEdge.class);
        
        // initialize vertices
        Map<String, Double> scores = new HashMap<String, Double>();
        for(String s: filteredWords){
        	directedGraph.addVertex(s);
        	scores.put(s, 1.0);
        }
        	
        // initialize edges
        for(int j=0; j< lemmatizedTickets.size(); j++){
        	ArrayList<String> lemmaT = lemmatizedTickets.get(j);
        	for(String word1 : lemmaT){
        		if(filteredWords.contains(word1)){
        			int index = lemmaT.indexOf(word1);
        			int startIndex = index- 4;
        			int endIndex = index + 4;
        			if(startIndex < 0)
        				startIndex = 0;
        			if(endIndex >= lemmaT.size())
        				endIndex = lemmaT.size()-1;
        			for(int i=startIndex; i<=endIndex; i++){
        				String word2 = lemmaT.get(i);
        				if(filteredWords.contains(word2) && !word1.equals(word2)){
        					directedGraph.addEdge(word1, word2);
        					directedGraph.addEdge(word2, word1);
        				}
        			}
        		}
        	}
        }
        
        //TextRank algorithm
        double dampingFactor = 0.85;
        Set<String> vertexSet = directedGraph.vertexSet();
        for(int i=0; i<20; i++){
        	for(String vertex : vertexSet){
        		Set<DefaultEdge> incomingEdges = directedGraph.incomingEdgesOf(vertex);
        		double score = 0.0;
        		for(Iterator<DefaultEdge> it = incomingEdges.iterator(); it.hasNext();){
        			String sourceVertex = it.next().toString().replaceAll("[\\p{Punct}]", " ").split(" ")[1];
//        			System.out.println(sourceVertex);
        			score += scores.get(sourceVertex)/directedGraph.outDegreeOf(sourceVertex);
        		}
        		score = score*dampingFactor + (1-dampingFactor);
        		scores.put(vertex, score);
        	}
        }
        
        List<Map.Entry<String, Double>> sortedScores = new LinkedList<Map.Entry<String, Double>>(scores.entrySet());
        sortedScores.sort(new Comparator<Map.Entry<String, Double>>(){

			@Override
			public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
        	
        });
        for(Map.Entry<String, Double> entry : sortedScores){
        	System.out.println(entry.getKey()+" "+entry.getValue());
        }
    }
}