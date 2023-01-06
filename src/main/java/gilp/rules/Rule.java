package gilp.rules;

import gilp.KBC.Path;
import gilp.comments.Comment;
import gilp.knowledgeClean.GILPSettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;


public class Rule {

    /*
     * Horn Clause
     */

    RDFPredicate _head = null;
    Clause _body = null;

    private double confidence;

    boolean _isExtended = false;

    double _quality = -1;

    private int hashcode = 0;
    private boolean hashcodeInitialized = false;


    public Rule() {
        super();
        this._head = null;
        this._body = new Clause();
    }

    public Rule(Clause body, RDFPredicate head) {
        this._body = body;
        this._head = head;
    }

    public Rule(Rule t) {
        this(t.get_body(), t.get_head());
    }

    @Override
    public Rule clone() {

        Rule r = new Rule();
        if (this._body != null)
            r.set_body(this._body.clone());
        else
            r.set_body(null);

        if (this._head != null)
            r.set_head(this._head.clone());
        else
            r.set_head(null);

        return r;
    }

    public RDFPredicate get_head() {
        return _head;
    }

    public void set_head(RDFPredicate head) {

        this._head = head.clone();

    }

    public Clause get_body() {
        return _body;
    }

    public void set_body(Clause _body) {
        this._body = _body.clone();

    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getConfidence() {
        return this.confidence;
    }


    public boolean isEmpty() {

        if (this._head == null) {
            System.out.print("Error! The head of a rule cannot be null.");
            return false;
        }
        if (this._body == null) {
            System.out.print("Error! The body of a rule cannot be null.");
            return false;
        }

        return this.get_body().getBodyLength() <= 0;
    }

    public static void removeDuplicated(ArrayList<Rule> listRules) {
        ArrayList<Rule> listRlts = new ArrayList<Rule>();
        for (Rule r : listRules) {
            boolean existed = false;
            for (Rule r1 : listRlts) {
                if (r1.equals(r)) {
                    existed = true;
                    break;
                }
            }
            if (!existed)
                listRlts.add(r);
        }
        listRules.clear();
        listRules.addAll(listRlts);
    }


    // suppose r: B -> H
    // this returns B AND H
    // The head H will be excluded if it is a special predicate, like isCorrect or
    // isIncorrect.
    public Clause getCorrespondingClause() {
        Clause cls = null;
        cls = this.get_body().clone();
        cls.addPredicate(this.get_head().clone());
        return cls;
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(this._body).append("->");

        sb.append(this._head);

        return sb.toString();
    }


    public String toAmieString() {
        StringBuilder sb = new StringBuilder();

        Iterator<RDFPredicate> iter = this._body.getIterator().iterator();

        if (iter.hasNext()) {
            RDFPredicate var = iter.next();
            sb.append(var._subject).append("\t");
            sb.append(var._predicate_name).append("\t");
            sb.append(var._object).append("\t");
        }
        while (iter.hasNext()) {
            RDFPredicate var = iter.next();
            sb.append(var._subject).append("\t");
            sb.append(var._predicate_name).append("\t");
            sb.append(var._object).append("\t");
        }

        sb.append("=>").append("\t");

        sb.append(this._head._subject).append("\t");
        sb.append(this._head._predicate_name).append("\t");
        sb.append(this._head._object);

        return sb.toString();
    }


    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean isExclusive() {
        if (this._head == null) {
            return false;
        }
        return this._head.isIncorrectPredicate();
    }

    public boolean isInclusive() {
        if (this._head == null) {
            return false;
        }
        return this._head.isCorrectPredicate();
    }

    //check whether the input predicate_name appears in this rule
    public boolean containPredicate(String pred_name) {
        if (this._head == null) {
            return false;
        }
        if (this.get_head().getPredicateName().equals(pred_name))
            return true;

        ArrayList<RDFPredicate> myIter = this.get_body().getIterator();

        for (RDFPredicate rdfPredicate : myIter) {
            if (rdfPredicate.getPredicateName().equals(pred_name))
                return true;
        }
        return false;
    }

    //check whether an atom has already in the body or head of this rule
    public boolean containAtom(RDFPredicate tp) {
        if (this._head == null) {

            return false;
        }
        if (this._head.equals(tp))
            return true;
        ArrayList<RDFPredicate> myIter = this._body.getIterator();

        for (RDFPredicate rdfPredicate : myIter) {
            if (rdfPredicate.equals(tp))
                return true;
        }
        return false;
    }

    //	//TODO if head cannot be null, how can a rule be empty?
//	public boolean isEmpty(){
//		if (this._head == null){
//			return false;
//		}
//		if (this._body == null){
//			return false;
//		}
//		if (this.get_body().getBodyLength()>0)
//			return false;
//		return true;
//	}
    //TODO need to rethink about this
    public boolean isTooGeneral() {
        return this.getLength() < GILPSettings.MAXIMUM_RULE_LENGTH;
    }

    //TODO need to rethink about this
    public boolean isQualified() {
        return this.getLength() >= GILPSettings.MAXIMUM_RULE_LENGTH;
    }

    public int getLength() {
        if (this._head == null) {
            return 0;
        } else {
            int len = 1;//head
            len += this.get_body().getBodyLength();
            return len;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!this.getClass().isInstance(o))
            return false;
        return
                this.toString().equals(o.toString());
    }


    protected static class PredicateComparator implements Comparator<RDFPredicate> {
        @Override
        public int compare(RDFPredicate p1, RDFPredicate p2) {
            return p1.getPredicateName().compareTo(p2.getPredicateName());
        }

    }

    //decide whether a comment is consistent with the rule
    boolean isConsistent(Comment cmt) {
        if (cmt.get_decision() == true && this.isInclusive())
            return true;
        if (cmt.get_decision() == false && this.isExclusive())
            return true;
        return false;
    }

    //-----kbc
    public boolean isXYRule() {
        if (this.get_head().isSubjectVariable() || this.get_head().isObjectVariable())
            return false;
        else
            return true;
    }

    public boolean isXRule() {
        if (this.isXYRule())
            return false;
        else {
            if (!this.get_head().isSubjectVariable())
                return true;
            else
                return false;
        }
    }

    public boolean isYRule() {
        if (this.isXYRule())
            return false;
        else {
            if (!this.get_head().isSubjectVariable())
                return true;
            else
                return false;
        }
    }

    public Rule(Path p) {
        this._body = new Clause();
        if (p.markers[0] == '+') {
            this._head = new RDFPredicate(p.nodes[0], p.nodes[1], p.nodes[2]);

        } else {
            this._head = new RDFPredicate(p.nodes[2], p.nodes[1], p.nodes[0]);
        }

        for (int i = 1; i < p.markers.length; i++) {
            if (p.markers[i] == '+') {
                // System.out.println("markers size = " + p.markers.length + " nodes size = " +
                // p.nodes.length + " i =" + i);
                this._body.addPredicate(new RDFPredicate(p.nodes[i * 2], p.nodes[i * 2 + 1], p.nodes[i * 2 + 2]));
            } else {
                this._body.addPredicate(new RDFPredicate(p.nodes[i * 2 + 2], p.nodes[i * 2 + 1], p.nodes[i * 2]));
            }
        }
    }


    public int bodysize() {
        return this._body.getBodyLength();
    }

    public boolean hasConstantInBody() {
        if (!(this._body.getIterator().get(this._body.getBodyLength() - 1).isSubjectVariable())
                || !(this._body.getIterator().get(this._body.getBodyLength() - 1).isObjectVariable()))
            return true;
        return false;
    }

}