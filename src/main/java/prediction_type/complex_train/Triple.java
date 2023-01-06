package prediction_type.complex_train;

public class Triple {
	private int head;// iHeadEntity
	private int tail;//iTailEntity
	private int rel;// iRelation
	
	public Triple() {
	}
	
	public Triple(int i, int j, int k) {//<head, tail,relation>
		head = i;
		tail = j;
		rel = k;
	}
	
	public int head() {
		return head;
	}
	
	public int tail() {
		return tail;
	}
	
	public int relation() {
		return rel;
	}
}
