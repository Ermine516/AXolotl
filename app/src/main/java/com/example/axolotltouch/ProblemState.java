package com.example.axolotltouch;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

//This contains all information concerning the problem rules and substitutions 
//as well as functions providing important features. 
public class ProblemState implements Parcelable {
    private static final String RULESYMBOL = "▶";
    int subPos;
    HashMap<String, Boolean> MatchorConstruct;
    boolean observe;
    int textSize;
    HashSet<Term> anteProblem;
    ArrayList<String> anteSelectedPositions;
    HashSet<Term> succProblem;
    String succSelectedPosition;
    ArrayList<Term> anteCurrentRule;
    Term succCurrentRule;

    HashSet<String> Variables;
    ArrayList<String> Constants;
    ArrayList<Pair<String, Pair<Integer, Boolean>>> Functions;
    ArrayList<Pair<String, Term>> Substitutions;
    ArrayList<Pair<String, Pair<ArrayList<Term>, Term>>> Rules;
    ArrayList<Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>>> History;
    HashMap<String, ArrayList<Term>> SubHistory;

    public ProblemState() {
        subPos = -1;
        observe = true;
        textSize = 25;
        MatchorConstruct = new HashMap<>();
        anteProblem = new HashSet<>();
        anteProblem.add(Const.Hole);
        anteSelectedPositions = new ArrayList<>();
        succProblem = new HashSet<>();
        succProblem.add(Const.Hole);
        succSelectedPosition = "";
        anteCurrentRule = new ArrayList<>();
        anteCurrentRule.add(Const.HoleSelected);
        succCurrentRule = Const.HoleSelected.Dup();

        SubHistory = new HashMap<>();
		Rules = new ArrayList<>();
        Substitutions = new ArrayList<>();
        Functions = new ArrayList<>();
        Functions.add(new Pair<>("cons", new Pair<>(2, false)));
        Functions.add(new Pair<>("⊢", new Pair<>(2, true)));
		Variables = new HashSet<>();
        Constants = new ArrayList<>();
        Constants.add("ε");
        History = new ArrayList<>();
	}
    ProblemState(Parcel in){
        subPos = in.readInt();
        observe = in.readInt() == 1;
        textSize = in.readInt();
        int MatchorConstructSize = in.readInt();
        MatchorConstruct = new HashMap<>();
        while (MatchorConstructSize > 0) {
            MatchorConstruct.put(in.readString(), in.readInt() == 1);
            MatchorConstructSize--;
        }
        int anteProblemsize = in.readInt();
        anteProblem = new HashSet<>();
        while (anteProblemsize > 0) {
            anteProblem.add(in.readTypedObject(Term.CREATOR));
            anteProblemsize--;
        }
        int anteSelectedPositionssize = in.readInt();
        anteSelectedPositions = new ArrayList<>();
        while (anteSelectedPositionssize > 0) {
            anteSelectedPositions.add(in.readString());
            anteSelectedPositionssize--;
        }
        int succProblemsize = in.readInt();
        succProblem = new HashSet<>();
        while (succProblemsize > 0) {
            succProblem.add(in.readTypedObject(Term.CREATOR));
            succProblemsize--;
        }
        succSelectedPosition = in.readString();
        int anteCurrentRuleSize = in.readInt();
        anteCurrentRule = new ArrayList<>();
        while (anteCurrentRuleSize > 0) {
            anteCurrentRule.add(in.readTypedObject(Term.CREATOR));
            anteCurrentRuleSize--;
        }
        succCurrentRule = in.readTypedObject(Term.CREATOR);
        String[] tempVar = new String[in.readInt()];
        in.readStringArray(tempVar);
        Variables = new HashSet<>();
        Variables.addAll(Arrays.asList(tempVar));
        String[] tempConst = new String[in.readInt()];
        in.readStringArray(tempConst);
        Constants = new ArrayList<>();
        Constants.addAll(Arrays.asList(tempConst));
        Functions = new ArrayList<>();
        int funcSize = in.readInt();
        for (int i = 0; i < funcSize; i++) {
            String key = in.readString();
            int arity = in.readInt();
            boolean infix = in.readInt() == 1;
            Functions.add(new Pair<>(key, new Pair<>(arity, infix)));
        }
        Substitutions = new ArrayList<>();
		int subsize = in.readInt();
		for(int i = 0; i< subsize; i++){
			String key  = in.readString();
            Term temp = in.readTypedObject(Term.CREATOR);
            Substitutions.add(new Pair<>(key, temp));
		}
        int rulesSize= in.readInt();
		Rules = new ArrayList<>();
        if (rulesSize > 0)
        	while( rulesSize>0){
                String label = in.readString();
                int ruleSize = in.readInt();
                ArrayList<Term> rule;
                if (ruleSize > 0) {
                    rule = new ArrayList<>();
                    for (int ri = 0; ri < ruleSize; ri++)
                        rule.add(in.readTypedObject(Term.CREATOR));
                } else rule = new ArrayList<>();
                Rules.add(new Pair<>(label, new Pair<>(rule, in.readTypedObject(Term.CREATOR))));
				rulesSize--;
			}

		int hisSize = in.readInt();
		History = new ArrayList<>();
		if(hisSize!= 0){
			while( hisSize>0){
                int side = in.readInt(); //either zero or one
                if (side == 0) {
                    int antesize = in.readInt();
                    ArrayList<String> anteselected = new ArrayList<>();
                    while (antesize > 0) {
                        anteselected.add(in.readString());
                        antesize--;
                    }

                    int subhissize = in.readInt();
                    ArrayList<Pair<String, Term>> hissubs = new ArrayList<>();
                    if (subhissize != 0) {
                        while (subhissize > 0) {
                            String hisvar = in.readString();
                            Term hissubterm = in.readTypedObject(Term.CREATOR);
                            hissubs.add(new Pair<>(hisvar, hissubterm));
                            subhissize--;
                        }
                    }
                    int hisruleleftsize = in.readInt();
                    ArrayList<Term> hisruleleft = new ArrayList<>();
                    while (hisruleleftsize > 0) {
                        hisruleleft.add(in.readTypedObject(Term.CREATOR));
                        hisruleleftsize--;
                    }
                    Term hisruleright = in.readTypedObject(Term.CREATOR);
                    History.add(new Pair<>(new Pair<>(anteselected, ""), new Pair<>(hissubs, new Pair<>(hisruleleft, hisruleright))));
                    hisSize--;
                } else {
                    String succside = in.readString();
                    int subhissize = in.readInt();
                    ArrayList<Pair<String, Term>> hissubs = new ArrayList<>();
                    if (subhissize != 0) {
                        while (subhissize > 0) {
                            String hisvar = in.readString();
                            Term hissubterm = in.readTypedObject(Term.CREATOR);
                            hissubs.add(new Pair<>(hisvar, hissubterm));
                            subhissize--;
                        }
                    }
                    int hisruleleftsize = in.readInt();
                    ArrayList<Term> hisruleleft = new ArrayList<>();
                    while (hisruleleftsize > 0) {
                        hisruleleft.add(in.readTypedObject(Term.CREATOR));
                        hisruleleftsize--;
                    }
                    Term hisruleright = in.readTypedObject(Term.CREATOR);
                    History.add(new Pair<>(new Pair<>(new ArrayList<String>(), succside), new Pair<>(hissubs, new Pair<>(hisruleleft, hisruleright))));
                    hisSize--;
                }
			}
		}
        SubHistory = new HashMap<>();
        int subhissize = in.readInt();
        while (subhissize > 0) {
            String key = in.readString();
            ArrayList<Term> values = new ArrayList<>();
            in.readTypedList(values, Term.CREATOR);
            SubHistory.put(key, values);
            subhissize--;
        }
    }

    static Term getTermByString(String succSelectedPosition, HashSet<Term> succProblem) {
        for (Term t : succProblem)
            if (t.Print().compareTo(succSelectedPosition) == 0) return t.Dup();
        return null;
    }

    static boolean sideContainsEmptySet(HashSet<Term> side) {
        for (Term t : side) if (t.getSym().compareTo(Const.Empty.getSym()) == 0) return true;
        return false;
    }

    Term getSelectedSuccTerm() {
        for (Term t : succProblem) {
            System.out.println(t.Print() + "  " + (t.Print().compareTo(succSelectedPosition) == 0));
            System.out.println(succSelectedPosition + "  " + (t.Print().compareTo(succSelectedPosition) == 0));
            if (t.Print().compareTo(succSelectedPosition) == 0) return t;
        }
        return null;
    }

    HashSet<Term> replaceSelectedSuccTerm(ArrayList<Term> replacement) {
        HashSet<Term> succupdate = new HashSet<>();
        for (Term t : succProblem)
            if (t.Print().compareTo(succSelectedPosition) != 0) succupdate.add(t);
            else succupdate.addAll(replacement);
        return succupdate;
    }

    HashSet<Term> replaceSelectedAnteTerms(Term replacement) {
        HashSet<Term> anteUpdate = new HashSet<>();
        boolean replaced = false;
        for (Term t : anteProblem)
            if (!anteSelectedPositions.contains(t.Print())) anteUpdate.add(t);
            else if (!replaced) {
                anteUpdate.add(replacement);
                replaced = true;
            }
        return anteUpdate;
    }

    ArrayList<Term> getSelectedAnteTerm() {
        ArrayList<Term> termlist = new ArrayList<>();
        for (Term t : anteProblem)
            for (String s : anteSelectedPositions)
                if (t.Print().compareTo(s) == 0) termlist.add(t);
        return termlist;


    }

    String RuleTermsToString(Pair<ArrayList<Term>, Term> rule) {
        if (rule != null && rule.first != null && rule.second != null) {
            StringBuilder retString = new StringBuilder("Δ ");
            ArrayList<Term> varAsTerms = new ArrayList<>();
            for (String var : Variables) varAsTerms.add(new Const(var));
            if (rule.first.size() > 0)
                for (int i = 0; i < rule.first.size(); i++)
                    if (i == 0 && i != rule.first.size() - 1)
                        retString.append(", ").append(rule.first.get(i).PrintBold(varAsTerms)).append(" , ");
                    else if (0 == rule.first.size() - 1)
                        retString.append(", ").append(rule.first.get(i).PrintBold(varAsTerms)).append(" " + RULESYMBOL).append(" Δ , ");
                    else if (i == rule.first.size() - 1)
                        retString.append(rule.first.get(i).PrintBold(varAsTerms)).append(" " + RULESYMBOL).append(" Δ , ");
                    else retString.append(rule.first.get(i).PrintBold(varAsTerms)).append(" , ");
            else retString.append(RULESYMBOL).append(" Δ , ");
            System.out.println(retString + rule.second.PrintBold(varAsTerms));
            return retString + rule.second.PrintBold(varAsTerms);
        } else return "";
    }

    @SuppressWarnings("ConstantConditions")
    boolean containsFunctionSymbol(String func) {
        boolean contained = true;
        for (Pair<String, Pair<Integer, Boolean>> p : Functions)
            if (func.compareTo(p.first) == 0) contained = false;
        return contained;
    }

    // write your object's data to the passed-in Parcel
    @Override
    @SuppressWarnings("ConstantConditions")
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(subPos);
        out.writeInt((observe) ? 1 : 0);
        out.writeInt(textSize);
        out.writeInt(MatchorConstruct.size());
        for (String key : MatchorConstruct.keySet()) {
            out.writeString(key);
            out.writeInt((MatchorConstruct.get(key)) ? 1 : 0);
        }
        out.writeInt(anteProblem.size());
        for (Term t : anteProblem) out.writeTypedObject(t, flags);
        out.writeInt(anteSelectedPositions.size());
        for (String t : anteSelectedPositions) out.writeString(t);
        out.writeInt(succProblem.size());
        for (Term t : succProblem) out.writeTypedObject(t, flags);
        out.writeString(succSelectedPosition);
        out.writeInt(anteCurrentRule.size());
        for (Term t : anteCurrentRule) out.writeTypedObject(t, flags);
        out.writeTypedObject(succCurrentRule, flags);
		out.writeInt(Variables.size());
		out.writeStringArray(Variables.toArray(new String[0]));
    	out.writeInt(Constants.size());
		out.writeStringArray(Constants.toArray(new String[0]));
        out.writeInt(Functions.size());
        for (int i = 0; i < Functions.size(); i++) {
            out.writeString(Functions.get(i).first);
            out.writeInt(Functions.get(i).second.first);
            out.writeInt((Functions.get(i).second.second) ? 1 : 0);
        }
        out.writeInt(Substitutions.size());
        for (int i = 0; i < Substitutions.size(); i++) {
            out.writeString(Substitutions.get(i).first);
            out.writeTypedObject(Substitutions.get(i).second, flags);
		}
		out.writeInt(Rules.size());
        for (Pair<String, Pair<ArrayList<Term>, Term>> rule : Rules) {
            out.writeString(rule.first);
            out.writeInt(rule.second.first.size());
            for (int i = 0; i < rule.second.first.size(); i++)
                out.writeTypedObject(rule.second.first.get(i), flags);
            out.writeTypedObject(rule.second.second, flags);
        }
		out.writeInt(History.size());
        for (Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>> his : History) {
            Pair<ArrayList<String>, String> selection = his.first;
            ArrayList<Pair<String, Term>> substitution = his.second.first;
            Pair<ArrayList<Term>, Term> rule = his.second.second;
            if (selection.first.size() != 0) {
                out.writeInt(0);
                out.writeInt(selection.first.size());
                for (String s : selection.first) out.writeString(s);
            } else {
                out.writeInt(1);
                out.writeString(selection.second);
            }
            out.writeInt(substitution.size());
            for (int i = 0; i < substitution.size(); i++) {
                out.writeString(substitution.get(i).first);
                out.writeTypedObject(substitution.get(i).second, flags);
            }
            out.writeInt(rule.first.size());
            for (Term t : rule.first) out.writeTypedObject(t, flags);
            out.writeTypedObject(rule.second, flags);
        }
        out.writeInt(SubHistory.size());
        for (String key : SubHistory.keySet()) {
            out.writeString(key);
            out.writeTypedList(SubHistory.get(key));
        }
    }

    //Finds all variables within a term
    HashSet<String> VarList(Term ti) {
        HashSet<String> vl = new HashSet<>();
        if (ti instanceof Func) for (Term t : ti.subTerms()) vl.addAll(VarList(t));
        else for (String t : this.Variables) if (t.compareTo(ti.getSym()) == 0) vl.add(ti.getSym());
        return vl;
    }

    void problemClean() {
        HashSet<Term> newAnte = new HashSet<>();
        HashSet<Term> newSucc = new HashSet<>();
        for (Term t : anteProblem)
            if (t.getSym().compareTo(Const.Empty.getSym()) != 0) newAnte.add(t);
        for (Term t : succProblem)
            if (t.getSym().compareTo(Const.Empty.getSym()) != 0) newSucc.add(t);
        anteProblem = newAnte;
        succProblem = newSucc;
    }

    //Checks if every symbol within a term is indexed
    @SuppressWarnings("ConstantConditions")
    boolean isIndexed(Term ti) {
        boolean result = true;
        if (ti instanceof Func) {
            for (Term t : ti.subTerms()) result &= isIndexed(t);
            boolean contained = false;
            boolean sameArity = false;
            for (Pair<String, Pair<Integer, Boolean>> p : Functions) {
                if (ti.getSym().compareTo(p.first) == 0) {
                    contained = true;
                    if (p.second.first == ti.subTerms().size()) sameArity = true;
                }

            }
            return contained && sameArity && result;
        } else return Constants.contains(ti.getSym()) || Variables.contains(ti.getSym());
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<ProblemState> CREATOR = new Parcelable.Creator<ProblemState>() {
        public ProblemState createFromParcel(Parcel in) {
            return new ProblemState(in);
        }

        public ProblemState[] newArray(int size) {
            return new ProblemState[size];
        }
    };

    public boolean SequentProblem() {
        for (Term t : this.succProblem)
            if (TermHelper.wellformedSequents(t)) return true;
        return false;
    }
}
