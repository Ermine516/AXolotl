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
    Term[] anteProblem;
    Term[] succProblem;
    Term[] anteCurrentRule;
    Term succCurrentRule;
    int selectedSide;
    int subPos;
    boolean observe;
    HashSet<String> Variables;
    ArrayList<String> Constants;
    ArrayList<Pair<String, Pair<Integer, Boolean>>> Functions;
    ArrayList<Pair<String, Term>> Substitutions;
    ArrayList<Pair<Term[], Term>> Rules;
    ArrayList<Pair<Integer, Pair<ArrayList<Pair<String, Term>>, Pair<Term, Term>>>> History;
    HashMap<String, ArrayList<Term>> SubHistory;

    public ProblemState() {
        anteCurrentRule = new Term[]{Const.HoleSelected};
        succCurrentRule = Const.HoleSelected.Dup();
        anteProblem = new Term[]{Const.Hole};
        succProblem = new Term[]{Const.Hole};
        selectedSide = -1;
        subPos = -1;
        observe = true;
        SubHistory = new HashMap<>();
		Rules = new ArrayList<>();
        Substitutions = new ArrayList<>();
        Functions = new ArrayList<>();
		Variables = new HashSet<>();
        Constants = new ArrayList<>();
		History= new ArrayList<>();
	}
    ProblemState(Parcel in){
        observe = in.readInt() == 1;
        selectedSide = in.readInt();
        subPos = in.readInt();
        anteProblem = new Term[in.readInt()];
        in.readTypedArray(anteProblem, Term.CREATOR);
        succProblem = new Term[in.readInt()];
        in.readTypedArray(succProblem, Term.CREATOR);
        anteCurrentRule = new Term[in.readInt()];
        in.readTypedArray(anteCurrentRule, Term.CREATOR);
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
                Term[] rule;
                if (ruleSize > 0) {
                    rule = new Term[ruleSize];
                    for (int ri = 0; ri < ruleSize; ri++)
                        rule[ri] = in.readTypedObject(Term.CREATOR);
                } else rule = new Term[0];
                Rules.add(new Pair<>(rule, in.readTypedObject(Term.CREATOR)));
				rulesSize--;
			}

		int hisSize = in.readInt();
		History = new ArrayList<>();
		if(hisSize!= 0){
			while( hisSize>0){
                int side = in.readInt();
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
                Term hisruleleft = in.readTypedObject(Term.CREATOR);
                Term hisruleright = in.readTypedObject(Term.CREATOR);
                History.add(new Pair<>(side, new Pair<>(hissubs, new Pair<>(hisruleleft, hisruleright))));
				hisSize--;
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

    public void setProblem(Term[] problem) {
        if (problem.length == 2) anteProblem = new Term[]{problem[0].Dup(), problem[1].Dup()};
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
        out.writeInt((observe) ? 1 : 0);
        out.writeInt(selectedSide);
        out.writeInt(subPos);
        out.writeInt(anteProblem.length);
        out.writeTypedArray(anteProblem, flags);
        out.writeInt(succProblem.length);
        out.writeTypedArray(succProblem, flags);
        out.writeInt(anteCurrentRule.length);
        out.writeTypedArray(anteCurrentRule, flags);
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
        for (Pair<Term[], Term> rule : Rules) {
            out.writeInt(rule.first.length);
            for (int i = 0; i < rule.first.length; i++)
                out.writeTypedObject(rule.first[i], flags);
            out.writeTypedObject(rule.second, flags);
        }
		out.writeInt(History.size());
        for (Pair<Integer, Pair<ArrayList<Pair<String, Term>>, Pair<Term, Term>>> his : History) {
            out.writeInt(his.first);
            out.writeInt(his.second.first.size());
            for (int i = 0; i < his.second.first.size(); i++) {
                out.writeString(his.second.first.get(i).first);
                out.writeTypedObject(his.second.first.get(i).second, flags);
            }
            out.writeTypedObject(his.second.second.first, flags);
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
