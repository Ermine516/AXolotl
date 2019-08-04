package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Substitution implements Parcelable {
    public static final Creator<Substitution> CREATOR = new Creator<Substitution>() {
        @Override
        public Substitution createFromParcel(Parcel in) {
            int size = in.readInt();
            ArrayList<SingletonSubstitution> r = new ArrayList<>();
            for (int i = 0; i < size; i++)
                r.add(in.readTypedObject(SingletonSubstitution.CREATOR));
            return new Substitution(r);
        }

        @Override
        public Substitution[] newArray(int size) {
            return new Substitution[size];
        }
    };
    private ArrayList<SingletonSubstitution> replacements;

    Substitution() {
        replacements = new ArrayList<>();
    }

    Substitution(ArrayList<SingletonSubstitution> r) {
        replacements = r;
    }

    private Substitution(HashMap<String, Term> pairs) {
        replacements = new ArrayList<>();
        for (String s : pairs.keySet())
            replacements.add(new SingletonSubstitution(s, pairs.get(s)));

    }

    static Substitution substitutionConstruct(Term left, Term right, ProblemState PS) {
        Substitution ret = new Substitution();
        if (PS.VarList(left).size() == 0)
            if (PS.Variables.contains(right.getSym())) {
                if (left.getSym().compareTo("cons") == 0) {
                    if (left.subTerms().get(1).getSym().compareTo("ε") == 0)
                        ret.union(new SingletonSubstitution(right.getSym(), left.subTerms().get(0)));
                    else ret.union(new SingletonSubstitution(right.getSym(), left));
                } else ret.union(new SingletonSubstitution(right.getSym(), left));
            } else if (left.subTerms().size() == right.subTerms().size() && left.subTerms().size() != 0)
                for (int i = 0; i < left.subTerms().size(); i++)
                    ret.union(substitutionConstruct(left.subTerms().get(i), right.subTerms().get(i), PS));
        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public SingletonSubstitution get(int i) {
        return replacements.get(i);
    }

    void alter(int pos, String var, Term val) {
        if (replacements.get(pos).variable.compareTo(var) == 0) {
            replacements.set(pos, new SingletonSubstitution(var, val));
        }
    }

    boolean isPosition(int pos) {
        return pos < replacements.size();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(replacements.size());
        for (SingletonSubstitution s : replacements) out.writeTypedObject(s, flags);
    }

    public HashSet<Term> apply(ArrayList<Term> terms) {
        HashSet<Term> newterms = new HashSet<>();
        for (Term t : terms)
            newterms.add(apply(t));
        return newterms;
    }

    public Term apply(Term t) {
        Term temp = t.Dup();
        for (SingletonSubstitution s : replacements)
            temp = temp.replace(new Const(s.variable), s.replacement);
        return temp;
    }

    Substitution simplifyWithRespectTo(String var) {
        ArrayList<SingletonSubstitution> ret = new ArrayList<>();
        for (SingletonSubstitution p : replacements) {
            if (p.variable.compareTo(var) != 0 && p.replacement.getSym().compareTo(Const.HoleSelected.getSym()) == 0)
                ret.add(new SingletonSubstitution(p.variable, new Const(p.variable)));
            else ret.add(p);
        }
        return new Substitution(ret);

    }

    Substitution clean() throws NotASubtitutionException {
        HashSet<String> occurences = new HashSet<>();
        HashMap<String, Term> subCleaned = new HashMap<>();
        for (SingletonSubstitution p : this.replacements)
            if (!occurences.contains(p.variable)) {
                subCleaned.put(p.variable, p.replacement);
                occurences.add(p.variable);
            } else if (p.replacement.toString().compareTo(subCleaned.get(p.variable).toString()) != 0)
                throw new NotASubtitutionException();
        return new Substitution(subCleaned);
    }

    private void union(SingletonSubstitution sub) {
        this.replacements.add(sub);
    }

    private void union(Substitution sub) {
        this.replacements.addAll(sub.replacements);
    }

    HashMap<String, Boolean> partialOrNot() {
        HashMap<String, Boolean> ret = new HashMap<>();
        for (SingletonSubstitution p : replacements)
            ret.put(p.variable, p.replacement.getSym().compareTo(Const.HoleSelected.getSym()) == 0);
        return ret;
    }

    void varIsPartial(String s) {
        replacements.add(new SingletonSubstitution(s, Const.HoleSelected.Dup()));
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (SingletonSubstitution s : replacements)
            ret.append(s.toString()).append("  ");
        return ret.toString();
    }

    class NotASubtitutionException extends Exception {
    }
}
