package gilp.KBC;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SampledPairedResultSet {

    private int valueCounter = 0;

    private HashMap<String, HashSet<String>> values;
    private boolean sampling;

    private String currentKey = "";

    public SampledPairedResultSet() {
        this.values = new HashMap<String, HashSet<String>>();
        this.sampling = false;
    }

    public void addKey(String key) {
        this.currentKey = key;
        if (this.values.containsKey(key)) return;
        else this.values.put(key, new HashSet<String>());
    }

    public HashMap<String, HashSet<String>> getValues() {
        return this.values;
    }

    public boolean usedSampling() {
        return this.sampling;
    }

    public void addValue(String value) {
        this.values.get(currentKey).add(value);
        this.valueCounter++;
    }

    public int size() {
        return this.valueCounter;
    }

}
