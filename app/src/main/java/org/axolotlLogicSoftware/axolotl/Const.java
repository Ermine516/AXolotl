package org.axolotlLogicSoftware.axolotl;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This is a class which implements Term specifically for Logical Constants. This implies that
 * the Term do not take any arguments.
 *
 * @author David M. Cerna
 */
public final class Const implements Term, Parcelable {

    /**
     * A Const denoting a actively selected position in a term. Also used as a place holder in
     * many cases.
     */
    final static Const HoleSelected = new Const("⚫");
    /**
     * A Const denoting a inactive but possibly  active position in a term. Also used as a place holder in
     * many cases.
     */
    final static Const Hole = new Const("⚪");
    /**
     * An empty set of terms. Used for Problems which have been solved or for denoting the lack of
     * a selection from the problem state.
     */
    final static Const Empty = new Const("∅");
    /**
     * An empty list of terms. Specifically used in the presence of cons and sequents.
     */
    final static Const EmptyList = new Const("ε");


    /**
     * The symbol of the Const object.
     */
    private final String Sym;
    /**
     * The list of arguments. Should always be empty for a Const object.
     */
    private ArrayList<Term> Args;

    /**
     * Const Constructor.
     * @param sym The symbol of the constructed object.
     * @author David M. Cerna
     */
    public Const(String sym) {
        Sym = sym;
        Args = new ArrayList<>();
    }

    /**
     * An inherited method which returns an empty array.
     *
     * @return A empty array
     * @author David M. Cerna
     */
    public ArrayList<Term> subTerms() {
        return Args;
    }

    /**
     * Returns the symbol of the Const object.
     * @return The symbol of the given Const.

     * @author David M. Cerna
     */
    public String getSym() {
        return this.Sym;
    }

    /**
     * Checks if Const object is an instance the the EmptyList Const object.
     * @return True if this Const object is the empty list.
     * @author David M. Cerna
     */
    boolean isEmptyList() {
        return this.getSym().compareTo(EmptyList.getSym()) == 0;
    }

    /**
     * An inherited method which does effect Const object.
     */
    public void normalize(HashSet<String> var) {
    }

    /**
     * Creates a new Const object equivalent to the object the method is applied to.
     *
     * @return a copy of the given Const.
     * @author David M. Cerna
     */
    public Term Dup() {
        return new Const(this.Sym);
    }

    /**
     * Replaces the given Const object by a copy of the Term object r if the given Const object
     * is equivalent to the Const object c.
     * @param c the const object to be replaced
     * @param r the Term object to replace c
     * @return returns a Term which is either the given Const or a copy of Term r
     * @author David M. Cerna
     */
    public Term replace(Const c, Term r) {
        if (c.Sym.matches(this.Sym)) return r.Dup();
        else return this;
    }

    /**
     * This is an inherited method which is equivalent to replace for Const objects.
     *
     * @param c the const object to be replaced
     * @param r the Term object to replace c
     * @return returns a Term which is either the given Const or a copy of Term r
     * @author David M. Cerna
     */
    public Term replaceLeft(Const c, Term r) {
        return replace(c, r);
    }


    /**
     * This is an inherited method which is equivalent to equality check for Const objects.
     * @param c the const object to be replaced
     * @return whether the Const object c is equivalent to the given object
     * @author David M. Cerna
     */
    public boolean contains(Const c) {
        return this.equals(c);
    }

    /**
     * An inherited method which creates a HashMap of all the function and const symbols occuring
     * in a given term. The basecases of the method are the Const symbols occuring in the term tree.
     * At the base cases new HashMap containing only the given Const symbol and its arity of zero are
     * created
     * @return A HashMap of containing a single pair of string and HashSet<integer>
     * @author David M. Cerna
     */
    public HashMap<String, HashSet<Integer>> basicTerms() {
        HashMap<String, HashSet<Integer>> result = new HashMap<>();
        HashSet<Integer> arities = new HashSet<>();
        arities.add(0);
        result.put(this.Sym, arities);
        return result;
    }

    /**
     * An inherited method which is unused.
     * @return The default value.
     * @author David M. Cerna
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Equality for Const objects compares the symbol.
     * @param o The object which is to be compared to the given Const object.
     * @return The default value.
     * @author David M. Cerna
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        else if ((o instanceof Const)) {
            return this.getSym().compareTo(((Const) o).getSym()) == 0;
        } else return false;
    }

    /**
     * hashcode for Const objects hashes the symbol.
     * @return The hash code.
     * @author David M. Cerna
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getSym().hashCode();
        return hash;
    }

    /**
     * Provides the default printing style of a Const object.
     * @return Const object as a String.
     * @author David M. Cerna
     */
    public String Print() {
        if (this.isEmptyList()) return "";
        else return this.Sym;
    }

    /**
     * Prints the Const in red if the term compare is equivalent to the given term. Furthermore, if
     * compare is also labeled as a variable, i.e. isvar is true, then the Const is printed in bold
     * red. Note that the empty list is print() as an empty string.
     *
     * @param compare The Term to be compared to the given Const object.
     * @param isvar   If compare is a variable then isvar is true.
     * @return Const object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String Print(Term compare, boolean isvar) {
        if (this.isEmptyList()) return "";
        else if (compare.subTerms().size() == 0 && compare.getSym().compareTo(this.getSym()) == 0)
            return FONTCOLOR + ((isvar) ? "<b>" : "") + Print() + ((isvar) ? "</b>" : "") + "</font>";
        else return Print();
    }

    /**
     * Prints the Const in red if the symbol of term compare is var and the Term t is equivalent to the given term.
     * Note that the empty list is print() as an empty string.
     * @param var The Term to be compared to the given Const object.
     * @param compare The Term to be compared to the given Const object.
     * @param t If compare is a variable then isvar is true.
     * @return Const object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String Print(String var, Term compare, Term t) {
        if (this.isEmptyList()) return "";
        if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && t.subTerms().size() == 0 && t.getSym().compareTo(this.getSym()) == 0)
            return FONTCOLOR + Print() + "</font>";
        else return Print();
    }

    /**
     * A special version of the print function used when printing terms within a list structure.
     * @return Const object as a String.
     * @author David M. Cerna
     */
    public String PrintCons() {
        if (this.isEmptyList()) return "";
        else return this.Sym;
    }

    /**
     * Prints the Const in red if the term compare is equivalent to the given term. Furthermore, if
     * compare is also labeled as a variable, i.e. isvar is true, then the Const is printed in bold
     * red. Note that the empty list is print() as an empty string. A special version of the print
     * function used when printing terms within a list structure.
     *
     * @param compare The Term to be compared to the given Const object.
     * @param isvar   If compare is a variable then isvar is true.
     * @return Const object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String PrintCons(Term compare, boolean isvar) {
        if (this.isEmptyList()) return "";
        else if (compare.subTerms().size() == 0 && compare.getSym().compareTo(this.getSym()) == 0)
            return FONTCOLOR + ((isvar) ? "<b>" : "") + Print() + ((isvar) ? "</b>" : "") + "</font>";
        else return Print();
    }

    /**
     * Prints the Const in red if the symbol of term compare is var and the Term t is equivalent to the given term.
     * Note that the empty list is print() as an empty string.  A special version of the print
     * function used when printing terms within a list structure.
     * @param var The Term to be compared to the given Const object.
     * @param compare The Term to be compared to the given Const object.
     * @param t If compare is a variable then isvar is true.
     * @return Const object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String PrintCons(String var, Term compare, Term t) {
        if (this.isEmptyList()) return "";
        else if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && t.subTerms().size() == 0 && t.getSym().compareTo(this.getSym()) == 0)
            return FONTCOLOR + Print() + "</font>";
        else return Print();
    }


    /**
     * Prints the Const object bold if  it is contained in the list terms.
     * @param terms  A list of terms
     * @return Const object as a String which may contains HTML code if the object is contained
     * within the list terms
     * @author David M. Cerna
     */
    public String PrintBold(ArrayList<Term> terms) {
        for (Term t : terms)
            if (t.getSym().compareTo(this.getSym()) == 0)
                return "<b>" + this.Print() + "</b>";
        return this.Print();
    }

    /**
     * Prints the Const object bold if  it is contained in the list terms. A special version of the print
     * function used when printing terms within a list structure.
     * @param terms  A list of terms
     * @return Const object as a String which may contains HTML code if the object is contained
     * within the list terms
     * @author David M. Cerna
     */
    public String PrintConsBold(ArrayList<Term> terms) {
        if (this.isEmptyList()) return "";
        else return this.PrintBold(terms);
    }

    /**
     * Prints the Const object as a string. Used mainly for internal use rather than for Graphical
     * display.
     * @return Const object as a String
     * @author David M. Cerna
     */
    @Override
    public String toString() {
        return this.Sym;
    }

    /**
     * Write the Const object into the given parcel out using the given flags.
     * @param out a parcel.
     * @param flags flags for appropriate construction of the parcel.
     * @author David M. Cerna
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.getSym());
        out.writeInt(0);
        out.writeTypedList(this.subTerms());
    }
}
