package com.example.axolotltouch;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//This contains all information concerning the problem rules and substitutions 
//as well as functions providing important features. 
public class ProblemState implements Parcelable {
    int subPos;
    boolean observe;
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
    ArrayList<Pair<ArrayList<Term>, Term>> Rules;
    ArrayList<Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>>> History;
    HashMap<String, ArrayList<Term>> SubHistory;

    public ProblemState() {
        subPos = -1;
        observe = true;
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
		Variables = new HashSet<>();
        Constants = new ArrayList<>();
		History= new ArrayList<>();
	}
    ProblemState(Parcel in){
        subPos = in.readInt();
        observe = in.readInt() == 1;
        int anteProblemsize = in.readInt();
        anteProblem = new HashSet<>();
        while (anteProblemsize > 0) {
            anteProblem.add(in.readTypedObject(Term.CREATOR));
            anteProblemsize--;
        }
        int anteSelectedPositionssize = in.readInt();
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
        for (String t: tempVar) Variables.add(t);
        String[] tempConst = new String[in.readInt()];
        in.readStringArray(tempConst);
        Constants = new ArrayList<>();
        for (String t: tempConst) Constants.add(t);
        Functions = new ArrayList<>();
        int funcSize = in.readInt();
        for (int i = 0; i < funcSize; i++) {
            String key = in.readString();
            int arity = in.readInt();
            boolean infix = (in.readInt() == 1) ? true : false;
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
                int ruleSize = in.readInt();
                ArrayList<Term> rule;
                if (ruleSize > 0) {
                    rule = new ArrayList<>();
                    for (int ri = 0; ri < ruleSize; ri++)
                        rule.add(in.readTypedObject(Term.CREATOR));
                } else rule = new ArrayList<>();
                Rules.add(new Pair<>(rule, in.readTypedObject(Term.CREATOR)));
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
                    while (antesize > 0) anteselected.add(in.readString());

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

    public void setProblem(HashSet<Term> ante, HashSet<Term> succ) {
        anteProblem = new HashSet<>();
        for (Term t : ante) anteProblem.add(t.Dup());
        succProblem = new HashSet<>();
        for (Term t : succ) succProblem.add(t.Dup());

    }
//This method checks if the given problem Term is properly constructed
//i.e. variable free and functions and constants are used correctly
boolean ProperProblemTerm(Term prob) {
		boolean ret = true;
		for (String t: this.Constants) if(t.compareTo(prob.getSym())==0 && prob.Print().contains("(")) ret =false;
		if(ret) for (String t: this.Variables) if(t.compareTo(prob.getSym())==0) ret =false;
    if (ret) for (Pair<String, Pair<Integer, Boolean>> t : this.Functions)
        if (t.first.compareTo(prob.getSym()) == 0 && prob.Print().contains("(") && prob.subTerms().size() != t.second.first)
            ret = false;
		if(prob instanceof Func)
            for (Term t : prob.subTerms()) ret &= ProperProblemTerm(t);
		return ret;
	}

//Checks if every symbol within a term is indexed
boolean isIndexed(Term ti) {
		boolean result = true;
		if(ti instanceof Func) {
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
		}
		else return Constants.contains(ti.getSym()) || Variables.contains(ti.getSym());
	}

    public boolean containsFunctionsymbol(String func) {
        boolean contained = false;
        for (Pair<String, Pair<Integer, Boolean>> p : Functions)
            if (func.compareTo(p.first) == 0) contained = true;
        return contained;
    }
//Finds all variables within a term
HashSet<String> VarList(Term ti) {
    HashSet<String> vl = new HashSet<>();
    if (ti instanceof Func) for (Term t : ti.subTerms()) vl.addAll(VarList(t));
		else for (String t: this.Variables) if(t.compareTo(ti.getSym())==0) vl.add(ti.getSym());
	    return vl;

	}

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(subPos);
        out.writeInt((observe) ? 1 : 0);
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
        for (Pair<ArrayList<Term>, Term> rule : Rules) {
            out.writeInt(rule.first.size());
            for (int i = 0; i < rule.first.size(); i++)
                out.writeTypedObject(rule.first.get(i), flags);
            out.writeTypedObject(rule.second, flags);
        }
		out.writeInt(History.size());
        for (Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>> his : History) {
            if (his.first.first.size() != 0) {
                out.writeInt(0);
                out.writeInt(his.first.first.size());
                for (String s : his.first.first) out.writeString(s);
            } else if (his.first.second.compareTo("") != 0) {
                out.writeInt(1);
                out.writeString(succSelectedPosition);
            }
            out.writeInt(his.second.first.size());
            for (int i = 0; i < his.second.first.size(); i++) {
                out.writeString(his.second.first.get(i).first);
                out.writeTypedObject(his.second.first.get(i).second, flags);
            }
            out.writeInt(his.second.second.first.size());
            for (Term t : his.second.second.first) out.writeTypedObject(t, flags);
            out.writeTypedObject(his.second.second.second, flags);
        }
        out.writeInt(SubHistory.size());
        for (String key : SubHistory.keySet()) {
            out.writeString(key);
            out.writeTypedList(SubHistory.get(key));
        }
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<ProblemState> CREATOR = new Parcelable.Creator<ProblemState>() {
        public ProblemState createFromParcel(Parcel in) {
            return new ProblemState(in);
        }

        public ProblemState[] newArray(int size) {
            return new ProblemState[size];
        }
    };
}
