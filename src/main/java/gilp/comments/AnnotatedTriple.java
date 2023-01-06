package gilp.comments;

import gilp.rdf3x.Triple;

public class AnnotatedTriple {
	public Triple _triple;
	double _sign;
	// _sign: 0.5 rough triples
	//_sign: 1 true triples
	//_sign: 0 false triples

	public AnnotatedTriple() {

	}

	public AnnotatedTriple(Triple t, double sign) {
		_triple = t;
		_sign = sign;
	}

	public Triple get_triple() {
		return _triple;
	}

	public void set_triple(Triple triple) {
		this._triple = triple;
	}

	
	public double get_sign() {
		return _sign;
	}


	public void set_sign(int sign) {
		this._sign = sign;
	}

	@Override
	public AnnotatedTriple clone() {
		AnnotatedTriple cmt = new AnnotatedTriple(this._triple.clone(), this._sign);
		return cmt;
	}

	@Override
	public String toString() {
		return this._triple.toString()+"\t" + this._sign;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
