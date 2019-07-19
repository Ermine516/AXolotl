package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//Class defining Constant terms
public final class Const implements Term, Parcelable {

    //Special constants used for substitution definition
    final static Const HoleSelected = new Const("⚫");
    final static Const Hole = new Const("⚪");
    //Constant symbol
    private final String Sym;
    private ArrayList<Term> Args;

    public Const(String sym) {
        Sym = sym;
        Args = new ArrayList<>();
    }

    //For visitor definition
    //  public <R> R accept(Visitor<R> visitor) {return visitor.visitConst(Sym);}
//Constants don't have direct subterms
    public ArrayList<Term> subTerms() {
        return Args;
    }

    //Return term symbol
    public String getSym() {
        return this.Sym;
    }

    //Prints the constant as a string
    public String Print() {
        return this.Sym;
    }

    public String Print(Term t) {
        if (t.subTerms().size() == 0 && t.getSym().compareTo(this.getSym()) == 0)
            return "<font color=#ff0000>" + Print() + "</font>";
        else return Print();
    }

    public String Print(String var, Term compare, Term t) {
        if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && t.subTerms().size() == 0 && t.getSym().compareTo(this.getSym()) == 0)
            return "<font color=#ff0000>" + Print() + "</font>";
        else return Print();
    }

    public String toString() {
        return this.Sym;
    }

    //Creates a duplicate term object
    public Term Dup() {
        return new Const(this.Sym);
    }

    //Replaces the constant by the term r
    public Term replace(Const c, Term r) {
        if (c.Sym.matches(this.Sym)) return r.Dup();
        else return this;
    }

    //Replaces the constant by the term r
    public Term replaceLeft(Const c, Term r) {
        if (this.Sym.compareTo(c.getSym()) == 0) return r;
        else return this;
    }

    //checks if a constant is equivalent to this constant
    public boolean contains(Const c) {
        return c.Sym.matches(this.Sym);
    }

    //Finds all the term symbols in the term tree
    public HashMap<String, HashSet<Integer>> basicTerms() {
        HashMap<String, HashSet<Integer>> result = new HashMap<>();
        HashSet<Integer> arities = new HashSet<>();
        arities.add(0);
        result.put(this.Sym, arities);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.getSym());
        out.writeInt(0);
        out.writeTypedList(this.subTerms());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        else if ((o instanceof Const)) {
            return this.getSym().compareTo(((Const) o).getSym()) == 0;
        } else return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getSym().hashCode();
        return hash;
    }
}
