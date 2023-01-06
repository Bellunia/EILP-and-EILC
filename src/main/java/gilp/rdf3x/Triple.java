package gilp.rdf3x;

import gilp.rules.RDFPredicate;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Triple {
	String _subject;
	 String _predicate;
	 String _obj;
	private	String _tid;
	/** Hash code */
	 int hashCode;

	public Triple() {
	}
	public Triple(String id, String subject, String predicate, String obj) {
		this._tid = id;
		this._subject = subject;
		this._predicate = predicate;
		this._obj = obj;
		this.hashCode = subject.hashCode() ^ predicate.hashCode()
				^ obj.hashCode();
	}




	public Triple(String subject, String predicate, String obj) {

		//this(null, subject, predicate, obj);
		this._tid = null;
		this._subject = subject;
		this._predicate = predicate;
		this._obj = obj;
	}

	public String get_tid() {
		return _tid;
	}

	public void set_tid(String  tid) {
		this._tid = tid;
	}
	public String get_subject() {
		return _subject;
	}

	public void set_subject(String subject) {
		this._subject = subject;
	}

	public String get_predicate() {
		return removePointBrackets(_predicate);
	}

	public static String removePointBrackets(String str) {

			str = str.replace("<", "");
			str = str.replace(">", "");

		return str;
	}

	/** Creates a copy of the fact */
	public Triple(Triple copy) {
		this._subject = copy._subject;
		this._obj = copy._obj;
		this._predicate = copy._predicate;
		this._tid = copy.getId();
		this.hashCode = copy.hashCode;
	}
	/** returns the id */
	public String getId() {
		return _tid;
	}

	public void set_predicate(String predicate) {
		this._predicate = predicate;
	}

	public String get_obj() {
		return _obj;
	}

	public void set_obj(String obj) {
		this._obj = obj;
	}

	/** Gets argument 1 or 2 */
	public String getArg(int a) {
		return (a == 1 ? get_subject() : get_obj());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		//sb.append("<" +this.tid + "> ");
		sb.append(this._subject).append("\t");
		sb.append(this._predicate).append("\t");
		sb.append(this._obj);
		return sb.toString();
	}

	@Override
	public Triple clone() {
		Triple t = new Triple();
		t.set_obj(this._obj);
		t.set_subject(this._subject);
		t.set_predicate(this._predicate);
		t.set_tid(this._tid);
		return t;
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Triple))
			return (false);
		Triple f = (Triple) obj;
		return (equal(_tid, f._tid) && _subject.equals(f._subject)
				&& _predicate.equals(f._predicate) && _obj.equals(f._obj));
	}

	/** Returns true if two things are equal, including NULL */
	public static <E> boolean equal(E s1, E s2) {
		if (s1 == s2) return true;
		if (s1 == null || s2 == null) return false;
		return (s1.equals(s2));
	}


	//@Override
	public boolean equals1(Object o) {
		if (!this.getClass().isInstance(o)) {
			return false;
		}
		Triple t = (Triple) o;
		if (!this.get_subject().equals(t.get_subject()))
			return false;
		if (!this.get_obj().equals(t.get_obj()))
			return false;
		if (!this.get_predicate().equals(t.get_predicate()))
			return false;

		return true;
	}

	public static ArrayList<Triple> removeDuplicated(ArrayList<Triple> listTriples) {
		ArrayList<Triple> listRlts = new ArrayList<Triple>();
		for (Triple t : listTriples) {
			if (!listRlts.contains(t)) {
				listRlts.add(t);
			}
		}
		listTriples.clear();
		return listRlts;
	}


	@Override
	public int hashCode() {
		return (hashCode);
	}//--new


//	@Override
//	public int hashCode() {---old
//		return toString().hashCode();
//	}

	//-----@2021.11.12
	// get the corresponding correct_prefixed predicate
	// e.g. hasGivenName(X,Y) -----> correct_hasGivenName(X,Y)
	public Triple mapToCorrectTriple() {
		Triple t = this.clone();
		String pred_name = t.get_predicate();
		pred_name = RDFPredicate.CORRECT_PREDICATE + "_" + pred_name;
		t.set_predicate(pred_name);
		return t;
	}

	public Triple mapToIncorrectTriple() {
		Triple t = this.clone();
		String pred_name = t.get_predicate();
		pred_name = RDFPredicate.INCORRECT_PREDICATE + "_" + pred_name;
		t.set_predicate(pred_name);
		return t;
	}

	// e.g. correct_hasGivenName(X,Y) -----> hasGivenName(X,Y)
	public Triple mapToOriginalTriple() {
		Triple t = this.clone();
		String pred_name = t.get_predicate();
		pred_name = pred_name.substring(pred_name.indexOf("_") + 1);
		t.set_predicate(pred_name);
		return t;
	}

	public static ArrayList<Triple> getCommonTriples (ArrayList<Triple> list1, ArrayList<Triple> list2){
		ArrayList<Triple> listRlts = new ArrayList<Triple>();
		for (Triple t1: list1){
			for (Triple t2: list2){
				if (t1.equals(t2)){
					if (!listRlts.contains(t1))
						listRlts.add(t1);
				}
			}
		}
		return listRlts;
	}

	//-----@2021.12.7
	/** TRUE for literals */
	public static boolean isLiteral(String entity) {
		return (entity.startsWith("\""));
	}
	/** returns a TSV line */
	public String toTsvLine(boolean withValue) {
		if (withValue && isLiteral(_obj)) {
			String val = getValue();
			if (val == null)
				val = "";
			return ((_tid == null ? "" : _tid) + "\t" + getArg(1) + "\t"
					+ get_predicate() + "\t" + getArg(2) + "\t" + val + "\n");
		} else {
			return ((_tid == null ? "" : _tid) + "\t" + getArg(1) + "\t"
					+ get_predicate() + "\t" + getArg(2) + (withValue ? "\t\n"
					: "\n"));
		}
	}
	/** removes quotes before and after a string */
	public static String stripQuotes(String s) {
		if (s == null)
			return null;
		if (s.startsWith("\""))
			s = s.substring(1);
		if (s.endsWith("\""))
			s = s.substring(0, s.length() - 1);
		return s;
	}



	/** A unit as a captuing regex */
	private static final String UNIT = "([/a-zA-Z\\%]++(?:\\^\\d)?)";
	/** A number as a capturing RegEx */
	public static final String FLOAT = "([\\-\\+]?\\d++(?:\\.[0-9]++)?(?:[Ee]\\-?[0-9]++)?)";
	/** The number pattern */
	public static final Pattern NUMBERPATTERN = Pattern.compile(newNumber(FLOAT + "(?:", UNIT + ")?"));

	/** Creates a normalized number from a number and a type */
	public static final String newNumber(String n, String type) {
		return (n + '#' + type);
	}


	/**
	 * Extracts the pure number from a String containing a normalized number,
	 * else null
	 */
	public static String getNumber(CharSequence d) {
		if(d==null) return(null);
		Matcher m = NUMBERPATTERN.matcher(d);
		if (m.find()) return (m.group(1));
		return (null);
	}
	public String getValue() {
		String val = null;
		if (isLiteral(_obj)) {
			String datatype = getDatatype(_obj);
			if (datatype != null && datatype.equals("xsd:date")) {
				String[] split =getDate(_obj, new int[2]);
				if (split != null && split.length == 3) {
					for (int i = 0; i < 3; i++) {
						split[i] = split[i].replace('#', '0');
						while (split[i].length() < 2)
							split[i] = "0" + split[i];
					}
					val = split[0] + "." + split[1] + split[2];
				}
			} else if (datatype != null) {
				val = getNumber(stripQuotes(_obj));
			}
		}
		return val;
	}


	/**
	 * Returns the components of the date (year, month, day) in a normalized
	 * date string (or null) and writes the start and end position in pos[0] and
	 * pos[1]---new int[2]
	 */
	public static String[] getDate(CharSequence d, int[] pos) {
		if (d == null) return (null);

		String DATE = "(-?[0-9#X]++)" + "-" + "([0-9#X]{1,2})" + "-" + "([0-9#X]{1,2})";
		/** Creates a date-string of the form "year-month-day" */

		Matcher m = Pattern.compile(DATE).matcher(d);
		if (!m.find()) {
			m = Pattern.compile("\\b(\\d{3,4})\\b").matcher(d.toString());
			if (!m.find()) return (null);
			pos[0] = m.start();
			pos[1] = m.end();
			return (new String[] { m.group(1), "##", "##" });
		}
		pos[0] = m.start();
		pos[1] = m.end();
		String[] result = new String[] { m.group(1), m.group(2), m.group(3) };
		return (result);
	}



	/** returns the datatype part of a literal */
	public static String getDatatype(String stringLiteral) {
		String[] split = literalAndDatatypeAndLanguage(stringLiteral);
		if (split == null)
			return (null);
		return (split[1]);
	}

	/**
	 * Splits a literal into literal (with quotes) and datatype, followed by the
	 * language. Non-existent components are NULL
	 */
	public static String[] literalAndDatatypeAndLanguage(String s) {
		if (s == null || !s.startsWith("\""))
			return (null);

		// Get the language tag
		int at = s.lastIndexOf('@');
		if (at > 0 && s.indexOf('\"', at) == -1) {
			String language = s.substring(at + 1);
			String string = s.substring(0, at);
			return (new String[] { string, null, language });
		}

		// Get the data type
		int dta = s.lastIndexOf("\"^^");
		if (dta > 0 && s.indexOf('\"', dta + 1) == -1) {
			String datatype = s.substring(dta + 3);
			String string = s.substring(0, dta + 1);
			return (new String[] { string, datatype, null });
		}

		// Otherwise, return just the string
		return (new String[] { s, null, null });
	}


	/** returns a TSV line */
	public String toTsvLine() {
		return toTsvLine(false);
	}


	public static void main(String[] args){
		Triple t = new Triple("Yao_Ming","hasGivenName","Yao");
		Triple t1 = t.clone();
		System.out.println(t.equals(t1));
		System.out.println(t == t1);
		t = t.mapToCorrectTriple();
		System.out.println(t);
		t = t.mapToOriginalTriple();
		System.out.println(t);
		t = t.mapToIncorrectTriple();
		System.out.println(t);
	}


}