package prediction_type.ConfE;

import java.util.ArrayList;


public class Tri {
	public	int h;
	public	int r;
	public	int t;

	public Tri() {
	}

	public Tri(int subject, int predicate, int obj) {

		this.h = subject;
		this.r = predicate;
		this.t = obj;
	}

	public int head() {
		return h;
	}

	public void set_subject(int subject) {
		this.h = subject;
	}

	public int relation() {
		return r;
	}



	public void setR(int predicate) {
		this.r = predicate;
	}

	public int tail() {
		return t;
	}

	public void setT(int obj) {
		this.t = obj;
	}


	@Override
	public boolean equals(Object o) {
		if (!this.getClass().isInstance(o)) {
			return false;
		}
		Tri t = (Tri) o;
		if (this.head()!=(t.head()))
			return false;
		if (this.tail()!=(t.tail()))
			return false;
		return this.relation() == (t.relation());
	}

	public static ArrayList<Tri> removeDuplicated(ArrayList<Tri> listTriples) {
		ArrayList<Tri> listRlts = new ArrayList<>();
		for (Tri t : listTriples) {
			if (!listRlts.contains(t)) {
				listRlts.add(t);
			}
		}
		listTriples.clear();
		return listRlts;
	}


}