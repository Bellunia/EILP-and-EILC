package gilp.KBC;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import gilp.rules.RDFPredicate;
import gilp.rules.Rule;



public class RuleFilter {


    static int countOneElement = 0;
    static int countMoreElemet = 0;
    static int countConst = 0;
    static int countVar = 0;

    public static ArrayList<Rule> filter(List<Rule> ruleList, PrintWriter log) throws IOException {
        System.out.println(">>> removing redundant rules " );
        HashMap<RDFPredicate, ArrayList<Rule>> allRules = new HashMap<>();

        for (Rule r1 : ruleList) {
            if (r1.isXRule() || r1.isYRule()) {
                ArrayList<Rule> b = new ArrayList<>();
                allRules.put(r1.get_head(), b);
            }
        }

        for (Rule r1 : ruleList) {
            if (allRules.containsKey(r1.get_head())) {
                allRules.get(r1.get_head()).add(r1);
            }
        }
        HashMap<RDFPredicate, HashSet<Rule>> rulesFiltered = removeConst(allRules);
        ArrayList<Rule> filteredRules = new ArrayList<Rule>();
        for (HashSet<Rule> ruleSet : rulesFiltered.values()) {
            filteredRules.addAll(ruleSet);
        }
        for (Rule r : ruleList) {
            if (r.isXYRule()) {
                filteredRules.add(r);
            }
        }
        System.out.println("* before=" + ruleList.size() + "   after=" + filteredRules.size());
        log.println("filtering: before=" + ruleList.size() + "   after=" + filteredRules.size());
        log.flush();


        return filteredRules;
    }

    public static HashMap<RDFPredicate, HashSet<Rule>> removeConst(HashMap<RDFPredicate, ArrayList<Rule>> allRules) {
        HashMap<RDFPredicate, HashSet<Rule>> newRules = new HashMap<>();


        for (RDFPredicate h : allRules.keySet()) {
            // ergOld += allRules.get(h).size();
            HashSet<Rule> rulesN = new HashSet<>();

            for (int i = 0; i < allRules.get(h).size(); i++) {
                boolean inside = false;
                for (int j = 0; j < allRules.get(h).size(); j++) {
                    if (i != j) {
                        if (special(allRules.get(h).get(j), allRules.get(h).get(i))) {
                            if (allRules.get(h).get(i).bodysize() > 1 && !inside) {
                                countMoreElemet++;
                            }
                            if (allRules.get(h).get(i).bodysize() == 1 && !inside) {
                                countOneElement++;
                            }
                            if (allRules.get(h).get(i).hasConstantInBody()) {
                                countConst++;
                            } else {
                                countVar++;
                            }
                            inside = true;
                            break;
                        }
                    }
                }
                if (!inside) {
                    rulesN.add(allRules.get(h).get(i));
                }
            }
            newRules.put(h, rulesN);
        }

        return newRules;
    }

    // r1 soll allgemeiner sein als r2

    // r1 is more special than r2
    // z.B. r2: ... <= r(X,A)
    //      r1: ... <= r(X,c)
    public static boolean special(Rule r1, Rule r2) {
        if (r1.bodysize() > r2.bodysize()) {
            return false;
        }

        if (r1.hasConstantInBody()) {
            return false;
        }

//		if (r1.getAppliedConfidence() < r2.getAppliedConfidence()) {
//			return false;
//		}

        if (r1.bodysize() == r2.bodysize()) {
            for (int i = 0; i < r2.bodysize() - 1; i++) {
                if (!r1.get_body().getIterator().get(i).equals(r2.get_body().getIterator().get(i))) {
                    // unterschiedliche Relationen in Body - Letztes Body-Element wird spaeter
                    // geprueft
                    return false;
                }
            }

            if (r1.get_body().getIterator().get(r1.bodysize() - 1).getPredicateName().equals(r2.get_body().getIterator().get(r2.bodysize() - 1).getPredicateName())) {
                if ((!r1.get_body().getIterator().get(r1.bodysize() - 1).getSubject().equals(r2.get_body().getIterator().get(r2.bodysize() - 1).getSubject())
                        && !r1.get_body().getIterator().get(r1.bodysize() - 1).getObject()
                        .equals(r2.get_body().getIterator().get(r2.bodysize() - 1).getObject()))) {
                    // falsche Signatur in letztem Body
                    return false;
                }
            } else if (!r1.get_body().getIterator().get(r1.bodysize() - 1).getPredicateName().equals(r2.get_body().getIterator().get(r2.bodysize() - 1).getPredicateName())) {
                // unterschiedliche Relationen in letztem Body
                return false;
            }
        } else if (r1.bodysize() < r2.bodysize()) {
            for (int i = 0; i < r1.bodysize(); i++) {
                if (!r1.get_body().getIterator().get(i).equals(r2.get_body().getIterator().get(i))) {
                    // unterschiedliche Relationen in Body
                    return false;
                }
            }
        }
        // if (r1.isXYRule() || r2.isXYRule()) {
        //	System.out.println("r2 is more special than r1");
        //	System.out.println("r1: " + r1);
        // 	System.out.println("r2: " + r2);
        // }
        return true;
    }

    public static int sameRel(HashSet<Rule> regel) {
        HashSet<String> s1 = new HashSet<>();
        for (Rule r : regel) {
            for (RDFPredicate a : r.get_body().getIterator()) {
                s1.add(a.getPredicateName());
            }
        }
        return s1.size();
    }

    public static int sameRel(ArrayList<Rule> regel) {
        HashSet<String> s1 = new HashSet<>();
        for (int i = 0; i < regel.size(); i++) {
            for (RDFPredicate a : regel.get(i).get_body().getIterator()) {
                s1.add(a.getPredicateName());
            }
        }
        return s1.size();
    }

    public static boolean zweierRegel(ArrayList<Rule> regel) {
        for (int i = 0; i < regel.size(); i++) {
            if (regel.get(i).bodysize() > 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean zweierRegel(HashSet<Rule> regel) {
        for (Rule r : regel) {
            if (r.bodysize() > 1) {
                return true;
            }
        }
        return false;
    }
}
