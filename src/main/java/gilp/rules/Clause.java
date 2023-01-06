package gilp.rules;

import java.util.ArrayList;
import java.util.Iterator;

public class Clause {

    private ArrayList<RDFPredicate> _predicates;

    public Clause() {
        _predicates = new ArrayList<>();
    }


    public void addPredicate(RDFPredicate p) {
        this._predicates.add(p);
    }


    public ArrayList<RDFPredicate> getIterator() {
        return this._predicates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<RDFPredicate> iter = this._predicates.iterator();

        if (iter.hasNext())
            sb.append(iter.next());
        while (iter.hasNext()) {
            sb.append(",").append(iter.next());
        }
        return sb.toString();
    }


    public int getBodyLength() {
        return this._predicates.size();
    }

    @Override
    public Clause clone() {
        Clause cls = new Clause();
        cls._predicates = new ArrayList<>();
        for (RDFPredicate tp : this._predicates) {
            cls._predicates.add(tp.clone());
        }
        return cls;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean removePredicate(RDFPredicate p) {
        RDFPredicate foundItem = null;
        for (RDFPredicate child: this._predicates){
            if (child.equals(p)){
                foundItem = child;
                break;
            }
        }
        if(foundItem == null)
            return false;
        else{
            this._predicates.remove(foundItem);
            return true;
        }
    }

    public boolean containPredicate(RDFPredicate p) {
        for (RDFPredicate child: this._predicates){
            if (child.equals(p)){
               return true;
            }
        }
            return false;

    }

}