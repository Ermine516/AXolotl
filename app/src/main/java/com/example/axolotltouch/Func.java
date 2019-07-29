package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

//Class defining function terms
public final class Func implements Term, Parcelable {
    private final String Sym;
    private final ArrayList<Term> Args;
    private final boolean infix;

    Func(String sym, ArrayList<Term> args, boolean fix) {
        this.Sym = sym;
        this.Args = args;
        this.infix = fix;
    }

    //Returns all proper direct subterms
    public ArrayList<Term> subTerms() {
        return this.Args;
    }

    //Returns term symbol
    public String getSym() {
        return this.Sym;
    }

    //Part of visitor definition
    //  public <R> R accept(Visitor<R> visitor) { return visitor.visitFunc(Sym, Args);}
//Duplicates term object
    public Term Dup() {
        ArrayList<Term> newArgs = new ArrayList<>();
        for (int i = 0; i < this.Args.size(); i++) newArgs.add(this.Args.get(i).Dup());
        return new Func(this.Sym, newArgs, this.infix);
    }

    //Replaces every instance of the given constant c by the term r
    public Term replace(Const c, Term r) {
        ArrayList<Term> newArgs = new ArrayList<>();
        for (int i = 0; i < this.Args.size(); i++) newArgs.add(this.Args.get(i).replace(c, r));
        return new Func(this.Sym, newArgs, this.infix);
    }

    //Replaces left most instance of the given constant c by the term r
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

    //Checks if a term symbol is present in a given term tree
    public boolean contains(Const c) {
        boolean ret = false;
        for (int i = 0; i < this.Args.size(); i++)
            if (!ret) ret = this.Args.get(i).contains(c);
        return ret;
    }

    //Prints term tree as a string
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

    public String PrintCons() {
        if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintCons();
            return this.Args.get(0).PrintCons() + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else return this.Print();

    }

    public String PrintConsBold(ArrayList<Term> terms) {
        if (this.getSym().compareTo("cons") == 0) {
            String s = this.Args.get(1).PrintConsBold(terms);
            return this.Args.get(0).PrintConsBold(terms) + ((s.compareTo("") != 0) ? " , " : "") + s;
        } else return this.PrintBold(terms);

    }

    public String Print(Term t) {
        if (TermHelper.TermMatch(this, t)) return "<font color=#ff0000>" + this.Print() + "</font>";
        else {
            if (this.getSym().compareTo("⊢") == 0)
                return "(" + this.Args.get(0).Print(t) + " " + this.getSym() + " " + this.Args.get(1).Print(t) + ")";
            else if (this.getSym().compareTo("cons") == 0) {
                String s = this.Args.get(1).PrintCons(t);
                return this.Args.get(0).PrintCons(t) + ((s.compareTo("") != 0) ? " , " : "") + s;

            } else if (this.Args.size() == 2 && infix) {
                String s = "(";
                s += this.Args.get(0).Print(t) + " " + this.getSym() + " " + this.Args.get(1).Print(t);
                return s + ")";
            } else {
                StringBuilder s = new StringBuilder(Sym + "(");
                int i = 0;
                for (; i < (this.Args.size() - 1); i++)
                    s.append(this.Args.get(i).Print(t)).append(",");
                Term tt = this.Args.get(i);
                String ss = tt.Print(t);
                if (this.Args.size() > 0) s.append(ss).append(")");
                return s.toString();
            }
        }
    }

    public String PrintCons(Term t) {
        if (this.getSym().compareTo("cons") == 0)
            if (TermHelper.TermMatch(this, t))
                return "<font color=#ff0000>" + this.PrintCons() + "</font>";
            else {
                String s = this.Args.get(1).PrintCons(t);
                return this.Args.get(0).PrintCons(t) + ((s.compareTo("") != 0) ? " , " : "") + s;
            }
        else if (TermHelper.TermMatch(this, t))
            return "<font color=#ff0000>" + this.Print() + "</font>";
        else return this.Print(t);
    }

    public String PrintCons(String var, Term compare, Term t) {
        if (this.getSym().compareTo("cons") == 0)
            if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && TermHelper.TermMatch(this, t))
                return "<font color=#ff0000>" + this.PrintCons() + "</font>";
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
            return "<font color=#ff0000>" + this.Print() + "</font>";
        else return this.Print(var, compare, t);
    }
    public String Print(String var, Term compare, Term t) {
        if (compare.subTerms().size() == 0 && compare.getSym().compareTo(var) == 0 && TermHelper.TermMatch(this, t))
            return "<font color=#ff0000>" + this.Print() + "</font>";
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

    //need to think about the empty sequent
    public void normalize(HashSet<String> var) {
        if (TermHelper.wellformedSequents(this)) {
            this.subTerms().set(0, leftAssociate(this.subTerms().get(0), var));
            this.subTerms().set(1, leftAssociate(this.subTerms().get(1), var));
        }
    }

    private Term leftAssociate(Term t, HashSet<String> var) {
        ArrayList<Term> listTerms = (t.getSym().compareTo("cons") == 0) ? extractterms(t.subTerms().get(0)) : new ArrayList<>(Arrays.asList(AuxFunctionality.HashSetTermArray));
        if (t.getSym().compareTo("cons") == 0)
            listTerms.addAll((extractterms(t.subTerms().get(1))));
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

    private ArrayList<Term> extractterms(Term t) {
        ArrayList<Term> listTerms = new ArrayList<>();
        if (t.getSym().compareTo("cons") == 0) {
            listTerms.addAll(extractterms(t.subTerms().get(0)));
            listTerms.addAll(extractterms(t.subTerms().get(1)));
        } else if (t.getSym().compareTo("ε") != 0) listTerms.add(t);

        return listTerms;
    }
    //Prints term tree as a string
    public String toString() {
        StringBuilder s = new StringBuilder(Sym + "(");
        int i = 0;
        for (; i < (this.Args.size() - 1); i++) s.append(this.Args.get(i).toString()).append(",");
        Term t = this.Args.get(i);
        String ss = t.toString();
        if (this.Args.size() > 0) s.append(ss).append(")");
        return s.toString();
    }

    //Finds all the term symbols in the term tree
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

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.getSym());
        out.writeInt((this.infix) ? 1 : 0);
        out.writeTypedList(this.subTerms());
    }

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
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getSym().hashCode();
        for (Term t : subTerms())
            hash = 31 * hash + t.hashCode();
        return hash;
    }
}
