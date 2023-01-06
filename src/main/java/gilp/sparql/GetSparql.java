package gilp.sparql;

import gilp.rules.Clause;
import gilp.rules.RDFPredicate;
import gilp.rules.Rule;

import java.util.ArrayList;
import java.util.Iterator;




public class GetSparql {

	public StringBuffer buildStringBuffer(Clause cls, StringBuffer sb) {
		ArrayList<RDFPredicate> myIter = cls.getIterator();

		for (RDFPredicate tp : myIter) {

			// in our current RDF3x data set, each constant is enclosed by <>
			if (!tp.isSubjectVariable())
				sb.append("<");
			sb.append(tp.getSubject());
			if (!tp.isSubjectVariable())
				sb.append("> ");
			else
				sb.append(" ");

			if (!tp.getPredicateName().startsWith("?"))
				sb.append("<");
			sb.append(tp.getPredicateName());
			if (!tp.getPredicateName().startsWith("?"))
				sb.append("> ");
			else
				sb.append(" ");

			if (!tp.isObjectVariable())
				sb.append("<");
			sb.append(tp.getObject());
			if (!tp.isObjectVariable())
				sb.append(">. ");
			else
				sb.append(". ");
		}

		sb.append("}");
		return sb;
	}

	public StringBuffer buildStringBufferIndbpedia(Clause cls, StringBuffer sb) {
		ArrayList<RDFPredicate> myIter = cls.getIterator();
		//String newEntity = sparqlInExtendTriple(subject);

		for (RDFPredicate tp : myIter) {

			// in our current RDF3x data set, each constant is enclosed by <>
			if (!tp.isSubjectVariable()) {
				//sb.append("<");
				sb.append(sparqlInExtendTriple(tp.getSubject()));
			} else {
				sb.append(tp.getSubject());
			}
//			if (!tp.isSubjectVariable())
//				sb.append("> ");
//			else
			sb.append(" ");

			if (!tp.getPredicateName().startsWith("?"))
				sb.append("<");
			sb.append(tp.getPredicateName());

			if (!tp.getPredicateName().startsWith("?"))
				sb.append("> ");
			else
				sb.append(" ");

			if (!tp.isObjectVariable()) {
				//sb.append("<");
				sb.append(sparqlInExtendTriple(tp.getObject()));
			} else {
				sb.append(tp.getObject());
			}
//			if (!tp.isObjectVariable())
//				sb.append(">. ");
//			else
			sb.append(". ");
		}

		sb.append("}");
		return sb;
	}

	public String ruleToSparqlIndbpedia(Rule rule) {//in dbpedia

		String sparql = null;
		if (!rule.isEmpty()) {
			Clause clause = rule.getCorrespondingClause();
			//sparql = new GetSparql().buildSingleSPARQL(clause);
			StringBuffer sb = new StringBuffer();

			sb.append(" select distinct ?a where {");

			StringBuffer sb1 = buildStringBufferIndbpedia(clause, sb);

			sparql=sb1.toString();
		}
		return sparql;
	}

	public  String ruleToSparqlIndbpedia(Rule rule,String variable) {//in dbpedia

		String sparql = null;
		if (!rule.isEmpty()) {
			Clause clause = rule.getCorrespondingClause();
			//sparql = new GetSparql().buildSingleSPARQL(clause);
			StringBuffer sb = new StringBuffer();

			sb.append(" select distinct "+variable +" where {");

			StringBuffer sb1 = buildStringBufferIndbpedia(clause, sb);

			sparql=sb1.toString();
		}
		return sparql;
	}



	public String ruleToSparql(Rule rule) {//in yago
		String sparql = null;
		if (!rule.isEmpty()) {
			Clause clause = rule.getCorrespondingClause();
			sparql = new GetSparql().buildSingleSPARQL(clause);
		}
		return sparql;
	}

	public String buildSingleSPARQL(Clause cls) {
		// sparql example: select * where {?s1 <hasGivenName> <Yao>. ?s1 ?y ?o1 .}

		StringBuffer sb = new StringBuffer();

		sb.append(" select distinct ?a where {");

		StringBuffer sb1 = buildStringBuffer(cls, sb);
		return sb1.toString();
	}

	// transform a Clause into a SPARQL
	public String buildSPARQL(Clause cls) {
		// sparql example: select * where {?s1 <hasGivenName> <Yao>. ?s1 ?y ?o1 .}

		StringBuffer sb = new StringBuffer();

		sb.append(" select * where {");
		//System.out.println(cls+"\n");

		StringBuffer sb1 = buildStringBuffer(cls, sb);
		return sb1.toString();
	}

//------------------------------
public String ruleToSparqlRDF(Rule rule) {//in dbpedia

	String sparql = null;
	if (!rule.isEmpty()) {
		Clause clause = rule.getCorrespondingClause();
		//sparql = new GetSparql().buildSingleSPARQL(clause);
		StringBuffer sb = new StringBuffer();

		sb.append(" select distinct ?a where {");

		StringBuffer sb1 = buildStringBufferInRdf(clause, sb);

		sparql = sb1.toString();
	}
	return sparql;
}
	public String ruleToObjectSparql(Clause clause, RDFPredicate head) {//in dbpedia

		StringBuffer sb = new StringBuffer();
		sb.append(" select count ").append(head.getObject()).append(" where {");
		StringBuffer sb1 = buildStringBufferInRdf(clause, sb);

		return sb1.toString();
	}
	public String ruleToObjectSparqlOnLine(Clause clause, RDFPredicate head) {//in dbpedia

		String sparql = null;

		//   if( head.isObjectVariable()){
		StringBuffer sb = new StringBuffer();

		sb.append(" select ").append(head.getObject()).append(" (count(").append(head.getObject()).append(") AS ?triples) where {");

		StringBuffer sb1 = buildStringBufferInRdf(clause, sb);

		sparql = sb1.toString();
		//   }

		return sparql;
	}
	public String ruleToSubjectSparql(Clause clause, RDFPredicate head) {//in dbpedia

		String sparql = null;

		//  if(head.isSubjectVariable() ){
		StringBuffer sb = new StringBuffer();

		sb.append(" select count ").append(head.getSubject()).append(" where {");

		StringBuffer sb1 = buildStringBufferInRdf(clause, sb);

		sparql = sb1.toString();

		//    }

		return sparql;
	}
	public StringBuffer buildStringBufferInRdf(Clause cls, StringBuffer sb) {
		ArrayList<RDFPredicate> myIter = cls.getIterator();
		//String newEntity = sparqlInExtendTriple(subject);

		for (RDFPredicate tp : myIter) {

			// in our current RDF3x data set, each constant is enclosed by <>
			if (!tp.isSubjectVariable()) {
				//sb.append("<");
				sb.append(sparqlInExtendTriple(tp.getSubject()));
			} else {
				sb.append(tp.getSubject());
			}
//			if (!tp.isSubjectVariable())
//				sb.append("> ");
//			else
			sb.append(" ");

			if (!tp.getPredicateName().startsWith("?"))
				sb.append("<");
			sb.append(tp.getPredicateName());

			if (!tp.getPredicateName().startsWith("?"))
				sb.append("> ");
			else
				sb.append(" ");

			if (!tp.isObjectVariable()) {
				//sb.append("<");
				sb.append(sparqlInExtendTriple(tp.getObject()));
			} else {
				sb.append(tp.getObject());
			}
//			if (!tp.isObjectVariable())
//				sb.append(">. ");
//			else
			sb.append(". ");
		}

		sb.append("}");
		return sb;
	}

	public String sparqlInExtendTriple(String subject) {
		String changedThing = null;
		if (subject.contains("http") && (!subject.contains("^^")) && (!subject.contains("@"))) {
			changedThing = "<" + subject + ">";
		} else if (subject.contains("@")) {
			//  String[] result = subject.split("@");
			// System.out.print("\n @:"+result + "\n");
			// changedThing = "\"" + result[0] + "\"@" + result[1];
			changedThing = subject.replace(" \"", " \\\"").replace("\" ", "\\\" ");

		} else if (subject.contains("^^")) {
			String[] result = subject.split("\\^\\^");
			// System.out.print("\n ^^: "+result.length + "\n");

			// System.out.print("\n ^^: "+result[0] + "\n");
			//  changedThing = "\"" + result[0] + "\"^^<" + result[1] + ">";
			changedThing = result[0] + "^^<" + result[1] + ">";

		} else if (isNumeric(subject)) {

			changedThing = subject;
		} else {
			// changedThing = "\"" + subject + "\"";
			changedThing = subject;

		}

		return changedThing;
	}
	private static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0; ) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
