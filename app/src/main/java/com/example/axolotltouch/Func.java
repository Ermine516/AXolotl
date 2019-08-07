package com.example.axolotltouch;

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
     * When highlighting subterms we use the same color as end-gradiant for the action bar.
     */
    private static final String FONTCOLOR = "<font color=#EF4665>";
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
            if (this.Args.get(i).Print().compareTo(newArgs.get(newArgs.size() - 1).Print()) != 0)
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
            if (var.contains(listTerms.get(0).getSym())) return listTerms.get(0);
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

    /**
     * Provides a default pretty printing of terms.
     * @return a term object as a pretty printed String.
     * @author David M. Cerna
     */
    public String Print() {
        if (this.getSym().compareTo("⊢") == 0)
            return "(" + this.Args.get(0).Print() + " " + this.getSym() + " " + this.Args.get(1).Print() + ")";
        else if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintCons();
            return this.Args.get(0).PrintCons() + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else if (this.Args.size() == 2 && infix) {
            String s = "(";
            s += this.Args.get(0).Print() + " " + this.getSym() + " " + this.Args.get(1).Print();
            return s + ")";
        } else {
            StringBuilder s = new StringBuilder(Sym + "(");
            int i = 0;
            for (; i < (this.Args.size() - 1); i++) s.append(this.Args.get(i).Print()).append(",");
            Term t = this.Args.get(i);
            String ss = t.Print();
            if (this.Args.size() > 0) s.append(ss).append(")");
            return s.toString();
        }

    }

    /**
     * Provides a pretty printing of a term object which displays the subterms contained in terms
     * bold using the html markup.
     * @param terms a list of terms to print bold.
     * @return a term object as a pretty printed String possibly containing HTML.
     * @author David M. Cerna
     */
    public String PrintBold(ArrayList<Term> terms) {
        if (this.getSym().compareTo("⊢") == 0)
            return "(" + this.Args.get(0).PrintBold(terms) + " " + this.getSym() + " " + this.Args.get(1).PrintBold(terms) + ")";
        else if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintConsBold(terms);
            return this.Args.get(0).PrintConsBold(terms) + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else if (this.Args.size() == 2 && infix) {
            String s = "(";
            s += this.Args.get(0).PrintBold(terms) + " " + this.getSym() + " " + this.Args.get(1).PrintBold(terms);
            return s + ")";
        } else {
            StringBuilder s = new StringBuilder(Sym + "(");
            int i = 0;
            for (; i < (this.Args.size() - 1); i++)
                s.append(this.Args.get(i).PrintBold(terms)).append(",");
            Term t = this.Args.get(i);
            String ss = t.PrintBold(terms);
            if (this.Args.size() > 0) s.append(ss).append(")");
            for (Term tt : terms)
                if (tt.PrintBold(terms).compareTo(s.toString()) == 0)
                    return "<b>" + s.toString() + "</b>";
            return s.toString();
        }

    }

    /**
     * When pretty printing list terms the symbols should be dropped. This is a variant of the
     * print function which drops the cons symbol when printing list terms.
     * @return a pretty printed list term.
     * @author David M. Cerna
     */
    public String PrintCons() {
        if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintCons();
            return this.Args.get(0).PrintCons() + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else return this.Print();

    }

    /**
     * Similar to printbold() except this version correctly prints list terms.
     * @param terms a list of terms to print bold.
     * @return a term object as a pretty printed String possibly containing HTML.
     * @author David M. Cerna
     */
    public String PrintConsBold(ArrayList<Term> terms) {
        if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintConsBold(terms);
            return this.Args.get(0).PrintConsBold(terms) + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else return this.PrintBold(terms);

    }

    /**
     * Prints the term using the FONTCOLOR markup if the term compare is equivalent to the given term.
     * Furthermore, if compare is also labeled as a variable, i.e. isvar is true, then the Const is
     * printed in bold using the FONTCOLOR markup as well as the bold markup. Note that the empty
     * list is print() as an empty string.
     *
     * @param compare The Term to be compared to the given Const object.
     * @param isvar   If compare is a variable then isvar is true.
     * @return term object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String Print(Term compare, boolean isvar) {
        if (TermHelper.TermMatch(this, compare)) return FONTCOLOR + this.Print() + "</font>";
        else {
            if (this.getSym().compareTo("⊢") == 0)
                return "(" + this.Args.get(0).Print(compare, isvar) + " " + this.getSym() + " " + this.Args.get(1).Print(compare, isvar) + ")";
            else if (this.getSym().compareTo("cons") == 0) {
                String s = this.Args.get(1).PrintCons(compare, isvar);
                return this.Args.get(0).PrintCons(compare, isvar) + ((s.compareTo("") != 0) ? " , " : "") + s;

            } else if (this.Args.size() == 2 && infix) {
                String s = "(";
                s += this.Args.get(0).Print(compare, isvar) + " " + this.getSym() + " " + this.Args.get(1).Print(compare, isvar);
                return s + ")";
            } else {
                StringBuilder s = new StringBuilder(Sym + "(");
                int i = 0;
                for (; i < (this.Args.size() - 1); i++)
                    s.append(this.Args.get(i).Print(compare, isvar)).append(",");
                Term tt = this.Args.get(i);
                String ss = tt.Print(compare, isvar);
                if (this.Args.size() > 0) s.append(ss).append(")");
                return s.toString();
            }
        }
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
    public String Print(String var, Term compare, Term t) {
        if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && TermHelper.TermMatch(this, t))
            return FONTCOLOR + this.Print() + "</font>";
        else {
            if (this.getSym().compareTo("⊢") == 0)
                return "(" + this.Args.get(0).Print(var, compare.subTerms().get(0), t) + " " + this.getSym() + " " + this.Args.get(1).Print(var, compare.subTerms().get(1), t) + ")";
            else if (this.getSym().compareTo("cons") == 0) {
                if (compare.subTerms().size() != 0) {
                    String s = this.Args.get(1).PrintCons(var, compare.subTerms().get(1), t);
                    return this.Args.get(0).PrintCons(var, compare.subTerms().get(0), t) + ((s.compareTo("") != 0) ? " , " : "") + s;
                } else {
                    String s = this.Args.get(1).PrintCons(var, compare, t);
                    return this.Args.get(0).PrintCons(var, compare, t) + ((s.compareTo("") != 0) ? " , " : "") + s;
                }

            } else if (this.Args.size() == 2 && infix) {
                if (compare.subTerms().size() != 0) {
                    String s = "(";
                    s += this.Args.get(0).Print(var, compare.subTerms().get(0), t) + " " + this.getSym() + " " + this.Args.get(1).Print(var, compare.subTerms().get(1), t);
                    return s + ")";
                } else {
                    String s = "(";
                    s += this.Args.get(0).Print(var, compare, t) + " " + this.getSym() + " " + this.Args.get(1).Print(var, compare, t);
                    return s + ")";
                }
            } else {
                if (compare.subTerms().size() != 0) {
                    StringBuilder s = new StringBuilder(Sym + "(");
                    int i = 0;
                    for (; i < (this.Args.size() - 1); i++)
                        s.append(this.Args.get(i).Print(var, compare.subTerms().get(i), t)).append(",");
                    Term tt = this.Args.get(i);
                    String ss = tt.Print(var, compare.subTerms().get(i), t);
                    if (this.Args.size() > 0) s.append(ss).append(")");
                    return s.toString();
                } else {
                    StringBuilder s = new StringBuilder(Sym + "(");
                    int i = 0;
                    for (; i < (this.Args.size() - 1); i++)
                        s.append(this.Args.get(i).Print(var, compare, t)).append(",");
                    Term tt = this.Args.get(i);
                    String ss = tt.Print(var, compare, t);
                    if (this.Args.size() > 0) s.append(ss).append(")");
                    return s.toString();
                }

            }
        }
    }

    /**
     * Prints the term using the FONTCOLOR markup if the term compare is equivalent to the given term.
     * Furthermore, if compare is also labeled as a variable, i.e. isvar is true, then the Const is
     * printed in bold using the FONTCOLOR markup as well as the bold markup. Note that the empty
     * list is print() as an empty string.
     *
     * @param compare The Term to be compared to the given Const object.
     * @param isvar   If compare is a variable then isvar is true.
     * @return term object as a String which contains HTML code.
     * @author David M. Cerna
     */
    public String PrintCons(Term compare, boolean isvar) {
        if (this.getSym().compareTo("cons") == 0)
            if (TermHelper.TermMatch(this, compare))
                return FONTCOLOR + this.PrintCons() + "</font>";
            else {
                String s = this.Args.get(1).PrintCons(compare, isvar);
                return this.Args.get(0).PrintCons(compare, isvar) + ((s.compareTo("") != 0) ? " , " : "") + s;
            }
        else if (TermHelper.TermMatch(this, compare))
            return FONTCOLOR + this.Print() + "</font>";
        else return this.Print(compare, isvar);
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
    public String PrintCons(String var, Term compare, Term t) {
        if (this.getSym().compareTo("cons") == 0)
            if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && TermHelper.TermMatch(this, t))
                return FONTCOLOR + this.PrintCons() + "</font>";
            else {
                if (compare.subTerms().size() != 0) {
                    String s = this.Args.get(1).PrintCons(var, compare.subTerms().get(1), t);
                    return this.Args.get(0).PrintCons(var, compare.subTerms().get(0), t) + ((s.compareTo("") != 0) ? " , " : "") + s;
                } else {
                    String s = this.Args.get(1).PrintCons(var, compare, t);
                    return this.Args.get(0).PrintCons(var, compare, t) + ((s.compareTo("") != 0) ? " , " : "") + s;
                }
            }
        else if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && TermHelper.TermMatch(this, t))
            return FONTCOLOR + this.Print() + "</font>";
        else return this.Print(var, compare, t);
    }


    /**
     * prints term object as a string. Mainly used internally.
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



}
