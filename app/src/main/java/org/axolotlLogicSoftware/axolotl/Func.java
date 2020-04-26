package org.axolotlLogicSoftware.axolotl;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This is a class which implements functions with a particular arity for the construction Logical
 * Terms.
 * @author David M. Cerna
 */
public final class Func implements Term, Parcelable {

    /**
     * The symbol of the function. Inherited from Term
     */
    private final String Sym;
    /**
     * The list of arguments, i.e. direct sub-terms. The length of this list is precisely the arity
     * of the function. Furthermore, this list may never have size zero as that would conflict with
     * the definition of Const.
     */
    private final ArrayList<Term> Args;
    /**
     * This field is for display purposes only. Some function symbols are better precieved as infix
     * rather than prefixed. Note that internally function symbols are always prefixed.
     */
    private final boolean infix;

    /**
     * The standard constructor for functions.
     *
     * @param sym  The symbol of the function
     * @param args The argument list. List should be at least size 1.
     * @param fix  Whether them symbol should be printed infix or prefix.
     * @author David M. Cerna
     */
    Func(String sym, ArrayList<Term> args, boolean fix) {
        this.Sym = sym;
        this.Args = args;
        this.infix = fix;
    }

    /**
     * If t is a list, i.e. the othermost function symbol is cons, then we can extract all non-list
     * terms. Note that this is under the assumption that list cannot be nested and only occur as
     * top level terms or as the arguments to sequents. This function is primarily used for the
     * normalization procedure.
     *
     * @param t A list term.
     * @return the list of terms contained in the list term t.
     * @author David M. Cerna
     */
    private static ArrayList<Term> extractTerms(Term t) {
        ArrayList<Term> listTerms = new ArrayList<>();
        if (t.getSym().compareTo("cons") == 0) {
            listTerms.addAll(extractTerms(t.subTerms().get(0)));
            listTerms.addAll(extractTerms(t.subTerms().get(1)));
        } else if (t.getSym().compareTo("ε") != 0) listTerms.add(t);

        return listTerms;
    }

    /**
     * Returns the list of arguments, i.e. the direct subterms.
     *
     * @author David M. Cerna
     */
    public ArrayList<Term> subTerms() {
        return this.Args;
    }

    /**
     * Returns the function symbol
     * @author David M. Cerna
     */
    public String getSym() {
        return this.Sym;
    }

    /**
     * Creates a new Term object equivalent to the given Term object.
     * @author David M. Cerna
     */
    public Term Dup() {
        ArrayList<Term> newArgs = new ArrayList<>();
        for (int i = 0; i < this.Args.size(); i++) newArgs.add(this.Args.get(i).Dup());
        return new Func(this.Sym, newArgs, this.infix);
    }

    /**
     * Replaces all instances of the given Const object  in the given term by a copy of the
     * Term object r.
     * @param c the const object to be replaced.
     * @param r the Term object to replace c.
     * @return returns a Term which no longer constains the object c and instead contains instances
     * of r.
     * @author David M. Cerna
     */
    public Term replace(Const c, Term r) {
        ArrayList<Term> newArgs = new ArrayList<>();
        for (int i = 0; i < this.Args.size(); i++) newArgs.add(this.Args.get(i).replace(c, r));
        return new Func(this.Sym, newArgs, this.infix);
    }

    /**
     * Replaces the left most instance of the given Const object in the given term by a copy of the
     * Term object r.
     * @param c the const object to be replaced.
     * @param r the Term object to replace c.
     * @return returns a Term whose left most instance of the object c is replace by an instance of
     * r.
     * @author David M. Cerna
     */
    public Term replaceLeft(Const c, Term r) {
        ArrayList<Term> newArgs = new ArrayList<>();
        boolean diff = false;
        for (int i = 0; i < this.Args.size(); i++) {
            if (!diff) newArgs.add(this.Args.get(i).replaceLeft(c, r));
            else newArgs.add(this.Args.get(i));
            if (this.Args.get(i).Print(new ArrayList<Term>(), TxtAdj.Std).compareTo(newArgs.get(newArgs.size() - 1).Print(new ArrayList<Term>(), TxtAdj.Std)) != 0)
                diff = true;

        }
        return new Func(this.Sym, newArgs, this.infix);
    }

    /**
     * Checks if the given term contains the Const object c.
     * @param c the const object to check containment of .
     * @return true if the given term contains the Const object c
     * @author David M. Cerna
     */
    public boolean contains(Const c) {
        boolean ret = false;
        for (int i = 0; i < this.Args.size(); i++)
            if (!ret) ret = this.Args.get(i).contains(c);
        return ret;
    }

    /**
     * Under the assumption that the given term is a wellformed sequent we can normalize the list
     * representation of the antecedent and succedent enforcing left-association of the non-list
     * subterms. This is essential for the unification and matching problems associated with sequent
     * terms  Note that non-list terms which are also variables are treated special. For non-list
     * terms which are not variables, singleton list ought to end with an empty list implying that two
     * variables can capture the list. That is one captures the term and the other captures an empty
     * list. However, in the case of variables, the variable itself ought to capture as much as possible
     * including the empty list, thus a variable should be considered both a list and not a list until
     * instantiation.
     *
     * @param var A set of variables contained within the given term.
     * @author David M. Cerna
     */
    public void normalize(HashSet<String> var) {
        if (TermHelper.wellformedSequents(this)) {
            this.subTerms().set(0, leftAssociate(this.subTerms().get(0), var));
            this.subTerms().set(1, leftAssociate(this.subTerms().get(1), var));
        }
    }

    /**
     * Given a list term with has unordered pairing operators we enforce a left associative ordering.
     *
     * @param t   A list term.
     * @param var A set of variables occurring within the list term.
     * @return A left associated list term
     */
    private Term leftAssociate(Term t, HashSet<String> var) {
        ArrayList<Term> listTerms = (t.getSym().compareTo("cons") == 0) ? extractTerms(t.subTerms().get(0)) : new ArrayList<>(Collections.singletonList(t));
        if (t.getSym().compareTo("cons") == 0)
            listTerms.addAll((extractTerms(t.subTerms().get(1))));
        if (listTerms.size() > 1) {
            ArrayList<Term> argList = new ArrayList<>();
            int startvalue = listTerms.size() - 3;
            if (!var.contains(listTerms.get(listTerms.size() - 1).getSym())) {
                argList.add(listTerms.get(listTerms.size() - 1));
                argList.add(new Const("ε"));
                startvalue++;
            } else {
                argList.add(listTerms.get(listTerms.size() - 2));
                argList.add(listTerms.get(listTerms.size() - 1));
            }

            Term ret = new Func("cons", argList, false);
            for (int i = startvalue; i >= 0; i--) {
                argList = new ArrayList<>();
                argList.add(listTerms.get(i));
                argList.add(ret);
                ret = new Func("cons", argList, false);
            }
            return ret;
        } else if (listTerms.size() == 1) {
            if (var.contains(listTerms.get(0).getSym()) || listTerms.get(0).getSym().compareTo("ε") == 0)
                return listTerms.get(0);
            else {
                ArrayList<Term> argList = new ArrayList<>();
                argList.add(listTerms.get(0));
                argList.add(new Const("ε"));
                return new Func("cons", argList, false);
            }
        } else return new Const("ε");
    }

    /**
     * equates terms based on the direct subterms and there symbol
     *
     * @param o The object to compared with the given term.
     * @return true if o is an object and the given term has the same symbol
     * and direct subterms as o.
     * @author David M. Cerna
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        else if ((o instanceof Func)) {
            if (this.getSym().compareTo(((Func) o).getSym()) == 0) {
                boolean areequal = true;
                for (int i = 0; i < this.subTerms().size(); i++)
                    areequal &= this.subTerms().get(i).equals(((Term) o).subTerms().get(i));
                return areequal;
            } else return false;
        } else if ((o instanceof Const)) {
            if (this.subTerms().size() == 0)
                return this.getSym().compareTo(((Const) o).getSym()) == 0;
            else return false;
        } else return false;

    }
    /**
     * Provides a default pretty printing of terms.
     * @return a term object as a pretty printed String.
     * @author David M. Cerna
     */
    public String Print(ArrayList<Term> terms, Adjustment adj) {
        if (terms.contains(this)) return adj.apply(this.Print(new ArrayList<Term>(), TxtAdj.Std));
        else if (this.getSym().compareTo("⊢") == 0)
            return "(" + this.Args.get(0).Print(terms, adj) + " " + this.getSym() + " " + this.Args.get(1).Print(terms, adj) + ")";
        else if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintCons(terms, adj);
            return this.Args.get(0).PrintCons(terms, adj) + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else if (this.Args.size() == 2 && infix) {
            String s = "(";
            s += this.Args.get(0).Print(terms, adj) + " " + this.getSym() + " " + this.Args.get(1).Print(terms, adj);
            return s + ")";
        } else {
            StringBuilder s = new StringBuilder(Sym + "(");
            int i = 0;
            for (; i < (this.Args.size() - 1); i++)
                s.append(this.Args.get(i).Print(terms, adj)).append(",");
            Term t = this.Args.get(i);
            String ss = t.Print(terms, adj);
            if (this.Args.size() > 0) s.append(ss).append(")");
            return s.toString();
        }
    }
    /**
     * When pretty printing list terms the symbols should be dropped. This is a variant of the
     * print function which drops the cons symbol when printing list terms.
     * @return a pretty printed list term.
     * @author David M. Cerna
     */
    public String PrintCons(ArrayList<Term> terms, Adjustment adj) {
        if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintCons(terms, adj);
            return this.Args.get(0).PrintCons(terms, adj) + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else return this.Print(terms, adj);

    }
    /**
     * Prints the term using the FONTCOLOR markup if the symbol of term compare is var and the Term
     * t is contained in the given term. Note that the empty list is printed using print() as an
     * empty string.
     *
     * @param var     The variable possibly contained in  the compare object.
     * @param compare The Term to be compared to the given term object.
     * @param t       If compare is a variable then the given term ought to contain t
     * @return Const object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String Print(String var, Term compare, Term t, Adjustment adj) {
        if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && TermHelper.TermMatch(this, t))
            return adj.apply(this.Print(new ArrayList<Term>(), TxtAdj.Std));
        else {
            if (this.getSym().compareTo("⊢") == 0)
                return "(" + this.Args.get(0).Print(var, compare.subTerms().get(0), t, adj) + " " + this.getSym() + " " + this.Args.get(1).Print(var, compare.subTerms().get(1), t, adj) + ")";
            else if (this.getSym().compareTo("cons") == 0) {
                if (compare.subTerms().size() != 0) {
                    String s = this.Args.get(1).PrintCons(var, compare.subTerms().get(1), t, adj);
                    return this.Args.get(0).PrintCons(var, compare.subTerms().get(0), t, adj) + ((s.compareTo("") != 0) ? " , " : "") + s;
                } else {
                    String s = this.Args.get(1).PrintCons(var, compare, t, adj);
                    return this.Args.get(0).PrintCons(var, compare, t, adj) + ((s.compareTo("") != 0) ? " , " : "") + s;
                }

            } else if (this.Args.size() == 2 && infix) {
                if (compare.subTerms().size() != 0) {
                    String s = "(";
                    s += this.Args.get(0).Print(var, compare.subTerms().get(0), t, adj) + " " + this.getSym() + " " + this.Args.get(1).Print(var, compare.subTerms().get(1), t, adj);
                    return s + ")";
                } else {
                    String s = "(";
                    s += this.Args.get(0).Print(var, compare, t, adj) + " " + this.getSym() + " " + this.Args.get(1).Print(var, compare, t, adj);
                    return s + ")";
                }
            } else {
                if (compare.subTerms().size() != 0) {
                    StringBuilder s = new StringBuilder(Sym + "(");
                    int i = 0;
                    for (; i < (this.Args.size() - 1); i++)
                        s.append(this.Args.get(i).Print(var, compare.subTerms().get(i), t, adj)).append(",");
                    Term tt = this.Args.get(i);
                    String ss = tt.Print(var, compare.subTerms().get(i), t, adj);
                    if (this.Args.size() > 0) s.append(ss).append(")");
                    return s.toString();
                } else {
                    StringBuilder s = new StringBuilder(Sym + "(");
                    int i = 0;
                    for (; i < (this.Args.size() - 1); i++)
                        s.append(this.Args.get(i).Print(var, compare, t, adj)).append(",");
                    Term tt = this.Args.get(i);
                    String ss = tt.Print(var, compare, t, adj);
                    if (this.Args.size() > 0) s.append(ss).append(")");
                    return s.toString();
                }

            }
        }
    }
    /**
     * Prints the term list using the FONTCOLOR markup if the symbol of term compare is var and the Term
     * t is contained in the given term. Note that the empty list is printed using print() as an
     * empty string.
     * @param var The variable possibly contained in  the compare object.
     * @param compare The Term to be compared to the given term object.
     * @param t If compare is a variable then the given term ought to contain t
     * @return Const object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String PrintCons(String var, Term compare, Term t, Adjustment adj) {
        if (this.getSym().compareTo("cons") == 0)
            if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && TermHelper.TermMatch(this, t))
                return adj.apply(this.PrintCons(new ArrayList<Term>(), TxtAdj.Std));
            else {
                if (compare.subTerms().size() != 0) {
                    String s = this.Args.get(1).PrintCons(var, compare.subTerms().get(1), t, adj);
                    return this.Args.get(0).PrintCons(var, compare.subTerms().get(0), t, adj) + ((s.compareTo("") != 0) ? " , " : "") + s;
                } else {
                    String s = this.Args.get(1).PrintCons(var, compare, t, adj);
                    return this.Args.get(0).PrintCons(var, compare, t, adj) + ((s.compareTo("") != 0) ? " , " : "") + s;
                }
            }
        else return this.Print(var, compare, t, adj);
    }
    /**
     * Finds all the symbols and their associated arities within the given term.
     * @return A Hashmap of all the symbols occurring in the given term paired with the arities
     * associated with those symbols.
     * @author David M. Cerna
     */
    @SuppressWarnings("ConstantConditions")
    public HashMap<String, HashSet<Integer>> basicTerms() {
        HashMap<String, HashSet<Integer>> result = new HashMap<>();
        HashSet<Integer> temp3 = new HashSet<>();
        temp3.add(this.Args.size());
        result.put(this.Sym, temp3);
        for (Term t : this.Args) {
            HashMap<String, HashSet<Integer>> temp = t.basicTerms();
            for (String s : temp.keySet()) {
                if (result.containsKey(s)) {
                    HashSet<Integer> temp2 = result.get(s);
                    if (temp.get(s) != null) temp2.addAll(temp.get(s));
                    result.put(s, temp2);
                } else result.put(s, temp.get(s));
            }
        }
        return result;
    }

    /**
     * prints term object as a string. Mainly used internally.
     *
     * @return term object as a String
     * @author David M. Cerna
     */
    public String toString() {
        StringBuilder s = new StringBuilder(Sym + "(");
        int i = 0;
        for (; i < (this.Args.size() - 1); i++) s.append(this.Args.get(i).toString()).append(",");
        Term t = this.Args.get(i);
        String ss = t.toString();
        if (this.Args.size() > 0) s.append(ss).append(")");
        return s.toString();
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
     * Write the term object into the given parcel out using the given flags.
     * @param out a parcel.
     * @param flags flags for appropriate construction of the parcel.
     * @author David M. Cerna
     */    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.getSym());
        out.writeInt((this.infix) ? 1 : 0);
        out.writeTypedList(this.subTerms());
    }

    /**
     * computes the hashcode of terms based on the direct subterms and there symbol
     *
     * @return A hashcode of the given term
     * @author David M. Cerna
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getSym().hashCode();
        for (Term t : subTerms())
            hash = 31 * hash + t.hashCode();
        return hash;
    }


}
