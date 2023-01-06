package gilp.utils;

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

	public boolean containsKey(K key) {
		return this._key.equals(key);
	}
}
