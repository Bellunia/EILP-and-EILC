package gilp.knowledgeClean;

import gilp.comments.AnnotatedTriple;
import gilp.comments.Comment;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.RDFSubGraphSet;
import gilp.rdf3x.Triple;
import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;
import gilp.sparql.GetSparql;
import gilp.utils.KVPair;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RuleLearnerHelper {
	public HashSet<String> ruleToSubjects(Rule rule) {
		HashSet<String> subjects = new HashSet<>();
		if (!rule.isEmpty()) {
			RDFPredicate newHead = rule.get_head();
			if (!newHead.isSubjectVariable()) {
				subjects.add(newHead.getSubject());
			} else {
				String sparql = new GetSparql().ruleToSparql(rule);
				HashSet<String> subjects1 = new RDF3XEngine().getDistinctEntity(sparql);
				if(subjects1!=null)
				subjects.addAll(subjects1);
			}
		}

		return subjects;
	}
	public HashSet<Triple> subjectsToTriples(HashSet<String> subjects) {
		HashSet<Triple> triples = new HashSet<>();
		for (String subject : subjects) {
			Clause clause = new Clause();
			RDFPredicate rdfPredicate = new RDFPredicate();
			rdfPredicate.setSubject(subject);
			rdfPredicate.setPredicateName(GILPSettings.DEFAULT_PREDICATE_NAME);
			rdfPredicate.setObject("?o");

			clause.addPredicate(rdfPredicate);
			RDFSubGraphSet rdfSubGraphSet = new RDF3XEngine().getTriplesByCNF(clause);
			HashSet<Triple> subTriples = rdfSubGraphSet.getAllTriples();

			triples.addAll(subTriples);
		}
		return triples;
	}
	public HashSet<Comment> tripleToCommentByChineseSurnames(HashSet<Triple> selectTriples) {
		// do the feedback based on the Chinese surnames
		HashSet<Comment> selectedComments = new HashSet<Comment>();
		HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();
		for (Triple key : selectTriples) {
			String obj = key.get_obj();
			if (chineseSurnames.contains(obj)) {
				selectedComments.add(new Comment(key.clone(), false));
			} else {
				selectedComments.add(new Comment(key.clone(), true));
			}
		}
		return selectedComments;
	}
	public HashSet<AnnotatedTriple> tripleToAnntatedByChineseSurnames(HashSet<Triple> selectTriples) {
		// do the feedback based on the Chinese surnames
		HashSet<AnnotatedTriple> selectedComments = new HashSet<AnnotatedTriple>();
		HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();
		for (Triple key : selectTriples) {
			String obj = key.get_obj();
			if (chineseSurnames.contains(obj)) {
				selectedComments.add(new AnnotatedTriple(key.clone(), -1));
			} else {
				selectedComments.add(new AnnotatedTriple(key.clone(), 1));
			}
		}
		return selectedComments;
	}
	public HashSet<AnnotatedTriple> tripleToAnnotated(HashSet<Triple> selectTriples) {
		// do the feedback based on the Chinese surnames
		HashSet<AnnotatedTriple> selectedComments = new HashSet<AnnotatedTriple>();
		HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();
		HashSet<String> asianSurnames = GILPSettings.getAsianSurnameList();
		HashSet<String> specialPlaces = GILPSettings.getSpecialRegionsList();
		asianSurnames.removeAll(chineseSurnames);
		for (Triple key : selectTriples) {
			String obj = key.get_obj();
			if (chineseSurnames.contains(obj)) {
				selectedComments.add(new AnnotatedTriple(key.clone(), -1));
			} else if (asianSurnames.contains(obj)) {
				String subject = key.get_subject();

				String sql = "select distinct ?a where {<" + subject + ">  <" + GILPSettings.DEFAULT_CONDITIONAL_NAME
						+ "> ?a.} ";
				HashSet<String> placeSpecial = new RDF3XEngine().getDistinctEntity(sql); // all special place
				int negative = 0;
				int positive = 0;
				if (placeSpecial != null) {
					for (String str : placeSpecial) {
						String place = str.replace("<", "").replace(">", "");
						if (specialPlaces.contains(place))
							negative++;
						else
							positive++;
					}
				}
				if (placeSpecial.size() == negative) {
					selectedComments.add(new AnnotatedTriple(key.clone(), -1));
				} else if (positive == placeSpecial.size()) {
					selectedComments.add(new AnnotatedTriple(key.clone(), 1));

				} else {
					selectedComments.add(new AnnotatedTriple(key.clone(), 0));
				}

//				if (negative > positive) {
//					selectedComments.add(new AnnotatedTriple(key.clone(), -1));
//				}

			} else {
				selectedComments.add(new AnnotatedTriple(key.clone(), 1));
			}
		}

		return selectedComments;
	}
	public HashSet<Comment> anntatedTripleToComments(HashSet<AnnotatedTriple> selectedAnnotatedTriple) {
		HashSet<Comment> selectedComments = new HashSet<Comment>();
		for (AnnotatedTriple tri : selectedAnnotatedTriple) {
			if (tri.get_sign() == 1) {
				selectedComments.add(new Comment(tri.get_triple(), true));
			} else if (tri.get_sign() == -1) {
				selectedComments.add(new Comment(tri.get_triple(), false));
			}
		}
		return selectedComments;
	}
	public HashSet<Comment> tripleToComment(HashSet<Triple> selectTriples) {
		// do the feedback based on the Chinese surnames
		HashSet<Comment> selectedComments = new HashSet<Comment>();
		HashSet<String> chineseSurnames = GILPSettings.getChineseSurnameList();
		HashSet<String> asianSurnames = GILPSettings.getAsianSurnameList();
		HashSet<String> specialPlaces = GILPSettings.getSpecialRegionsList();
		asianSurnames.removeAll(chineseSurnames);
		for (Triple key : selectTriples) {
			String obj = key.get_obj();
			if (chineseSurnames.contains(obj)) {
				selectedComments.add(new Comment(key.clone(), false));
			} else if (asianSurnames.contains(obj)) {
				String subject = key.get_subject();
				String sql = "select distinct ?a where {<" + subject + ">  <" + GILPSettings.DEFAULT_CONDITIONAL_NAME
						+ "> ?a.} ";
				HashSet<String> placeSpecial = new RDF3XEngine().getDistinctEntity(sql); // all special place
				int negative = 0;
				int positive = 0;
				if (placeSpecial != null) {
					for (String str : placeSpecial) {
						String place = str.replace("<", "").replace(">", "");
						if (specialPlaces.contains(place))
							negative++;
						else
							positive++;
					}
				}
				if (negative > positive) {
					selectedComments.add(new Comment(key.clone(), false));
				}
			} else {
				selectedComments.add(new Comment(key.clone(), true));
			}
		}

		return selectedComments;
	}
	public HashSet<Comment> getRandomComments(int numbers, HashSet<Comment> listComments) throws IOException {
		HashSet<Comment> randomPositiveComments = filterRandomCommentsByObject(numbers,
				filterComments(listComments, true));
		HashSet<Comment> randomNegativeComments = filterRandomCommentsByObject(numbers,
				filterComments(listComments, false));
		HashSet<Comment> allRandomComments = new HashSet<Comment>();
		allRandomComments.addAll(randomNegativeComments);
		allRandomComments.addAll(randomPositiveComments);
		Writer writer = new OutputStreamWriter(
				new FileOutputStream(GILPSettings.getRootPath() + "/data/gilpRules/selectedComments.txt"),
				Charset.forName("UTF-8"));
		for (Comment cmt : allRandomComments)
			writer.write(cmt.get_triple() + " " + cmt.get_decision() + "\n");
		writer.close();
		return allRandomComments;
	}
	public HashSet<Comment> filterRandomCommentsByObject(int numbers, HashSet<Comment> filterComments) {
		// randomly choose @num comments
		int s = filterComments.size();
		int[] isChosen = new int[s];
		for (int i = 0; i < s; i++) {
			isChosen[i] = 0;
		}
		HashSet<Comment> comments = new HashSet<Comment>();

		HashSet<String> object = new HashSet<String>();

		while (comments.size() < Math.min(numbers, s)) {
			int idx = (int) Math.round(Math.random() * (s - 1));
			Comment cmt = (Comment) filterComments.toArray()[idx];

			if (isChosen[idx] == 0 && !object.contains(cmt.get_triple().get_obj())) {
				comments.add(cmt);
				object.add(cmt.get_triple().get_obj());
				isChosen[idx] = 1;
			}
		}
		return comments;
	}
	public HashSet<String> filterRandomString(int numbers, HashSet<String> filterComments) {
		int s = filterComments.size();
		int[] isChosen = new int[s];
		for (int i = 0; i < s; i++) {
			isChosen[i] = 0;
		}
		HashSet<String> comments = new HashSet<String>();

		while (comments.size() < Math.min(numbers, s)) {
			int idx = (int) Math.round(Math.random() * (s - 1));
			String cmt = filterComments.toArray()[idx].toString();

			if (isChosen[idx] == 0) {
				comments.add(cmt);

				isChosen[idx] = 1;
			}
		}
		return comments;
	}
	public HashSet<Comment> filterComments(HashSet<Comment> listComments, Boolean decision) {
		HashSet<Comment> negativeComments = new HashSet<Comment>();
		HashSet<Comment> positiveComments = new HashSet<Comment>();
		for (Comment comment : listComments) {
			if (comment.get_decision()) {
				positiveComments.add(comment);
			} else if (!comment.get_decision()) {
				negativeComments.add(comment);
			}
		}

		if (decision) {
			return positiveComments;
		} else {
			return negativeComments;
		}
	}
	public KVPair<HashSet<String>, HashSet<String>> commentsToSubjects(HashSet<Comment> comments) {
		// <K,V> :(positive_subjects ,negative_subjects)
		HashSet<String> positiveSet = new HashSet<String>();
		HashSet<String> negativeSet = new HashSet<String>();

		for (Comment comment : comments) {
			if (comment.get_decision()) {
				positiveSet.add(comment.get_triple().get_subject());
			} else if (!comment.get_decision()) {
				negativeSet.add(comment.get_triple().get_subject());
			}
		}

		return new KVPair<HashSet<String>, HashSet<String>>(
				positiveSet, negativeSet);
	}
	public HashSet<String> commentsToSub(HashSet<Comment> comments) {
		// <K,V> :(positive_subjects ,negative_subjects)
		HashSet<String> positiveSet = new HashSet<String>();
		HashSet<String> negativeSet = new HashSet<String>();

		HashSet<String> commentSubjects = new HashSet<String>();

		for (Comment comment : comments) {
			if (comment.get_decision()) {
				positiveSet.add(comment.get_triple().get_subject());
			} else if (!comment.get_decision()) {
				negativeSet.add(comment.get_triple().get_subject());
			}
		}
		commentSubjects.addAll(negativeSet);
		commentSubjects.addAll(positiveSet);

		return commentSubjects;
	}
	public KVPair<HashSet<String>,HashSet<String>> commentsToObj(HashSet<Comment> comments) {
		// <K,V> :(positive_subjects ,negative_subjects)
		HashSet<String> positiveSet = new HashSet<String>();
		HashSet<String> negativeSet = new HashSet<String>();

		KVPair<HashSet<String>,HashSet<String>> commentObjects = new KVPair<HashSet<String>,HashSet<String>>();

		for (Comment comment : comments) {
			if (comment.get_decision()) {
				positiveSet.add(comment.get_triple().get_obj());
			} else if (!comment.get_decision()) {
				negativeSet.add(comment.get_triple().get_obj());
			}
		}
		commentObjects.put(positiveSet,negativeSet);

		return commentObjects;
	}
	public static HashSet<Comment> fileToHashSet(String pathToFBfile) throws Exception {

		HashSet<Comment> comments = new HashSet<Comment>();

		Scanner scanner = new Scanner(pathToFBfile);
		File file = new File(scanner.nextLine());
		Scanner input = new Scanner(file);

		while (input.hasNextLine()) {
			String line = removePointBrackets(input.nextLine());

			StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
			String subject, predicate, object;
			subject = stringTokenizer.nextToken();// extract the subject
			predicate = stringTokenizer.nextToken();// extract the predicate
			object = stringTokenizer.nextToken();// extract the object

			String value = stringTokenizer.nextToken();
			boolean decision = Boolean.parseBoolean(value);// parse"true"
			Triple triple = new Triple(subject, predicate, object);

			Comment comment = new Comment(triple, decision);
			comments.add(comment);// generate a set of triples and comments
		}
		input.close();
		scanner.close();
		return comments;
	}
	public static HashSet<String> readTypes(String pathToFBfile) {

		HashSet<String> types = new HashSet<>();
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
	public static String removePointBrackets(String str) {
		str = str.replace("<", "");
		str = str.replace(">", "");
		return str;
	}
	public <K, V> HashSet<K> getAllKeysForValue(Map<K, V> mapOfWords, V value) {
		HashSet<K> listOfKeys = new HashSet<>();
		if (mapOfWords.containsValue(value)) {
			// listOfKeys = new HashSet<>();
			for (Map.Entry<K, V> entry : mapOfWords.entrySet()) {
				if (entry.getValue().equals(value)) {
					listOfKeys.add(entry.getKey());
				}
			}
		}
		return listOfKeys;
	}
	public <K, V> HashMap<K, Double> reverseOrderByValue(final HashMap<K, Double> wordCounts) {
		// the reverse order

		return wordCounts.entrySet()

				.stream()

				.sorted((Map.Entry.<K, Double>comparingByValue().reversed()))

				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

	}

}