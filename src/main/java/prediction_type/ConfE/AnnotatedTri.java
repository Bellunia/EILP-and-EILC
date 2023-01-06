package prediction_type.ConfE;


public class AnnotatedTri {
	public Tri _triple;
	double _sign;
	// _sign: 0.5 rough triples
	//_sign: 1 true triples
	//_sign: 0 false triples

	public AnnotatedTri() {
	}

	public AnnotatedTri(Tri t, double sign) {
		_triple = t;
		_sign = sign;
	}

	public Tri get_tri() {
		return _triple;
	}

	public Pair get_body() {
		return new Pair(_triple.head() ,_triple.relation());
	}
	public int get_tail() {
		return _triple.tail();
	}

	public void set_tri(Tri triple) {
		this._triple = triple;
	}

	public double get_sign() {
		return _sign;
	}

	public void set_sign(int sign) {
		this._sign = sign;
	}



}
