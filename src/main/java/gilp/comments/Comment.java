package gilp.comments;

import gilp.rdf3x.Triple;

public class Comment {
	public Triple _triple;
	boolean _decision;

	public Comment() {

	}

	public Comment(Triple t, boolean decision) {
		_triple = t;
		_decision = decision;
	}

	public Triple get_triple() {
		return _triple;
	}

	public void set_triple(Triple triple) {
		this._triple = triple;
	}

	public boolean get_decision() {
		return _decision;
	}

	public void set_decision(boolean decision) {
		this._decision = decision;
	}

	@Override
	public Comment clone() {
		return new Comment(this._triple.clone(), this._decision);
	}

	@Override
	public String toString() {
		return this._triple.toString() + "\t"+this._decision;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
