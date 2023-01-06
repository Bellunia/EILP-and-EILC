package prediction_type.ConfE;

public class KVPair<K, V> {
	
	private K _key;
	private V _value;

	public KVPair() {
	}

	public KVPair(K key, V val){
		this._key = key;
		this._value = val;
	}
	
	public K getKey() {
		return _key;
	}
	public void set_key(K key) {
		this._key = key;
	}
	public V getValue() {
		return _value;
	}
	public void set_value(V value) {
		this._value = value;
	}

	public boolean isEmpty() {
		return this._key == null && this._value == null;
	}

    public void put(K key, V val) {
		this._key = key;
		this._value = val;
    }

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( "(");
		sb.append(this._key).append(",");
		sb.append(this._value).append(")");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		KVPair<?, ?> pair = (KVPair<?, ?>) o;

		if (_key != null ? !_key.equals(pair._key) : pair._key != null) return false;
		return _value != null ? _value.equals(pair._value) : pair._value == null;
	}

	@Override
	public int hashCode() {
		int result = _key != null ? _key.hashCode() : 0;
		result = 31 * result + (_value != null ? _value.hashCode() : 0);
		return result;
	}
}
