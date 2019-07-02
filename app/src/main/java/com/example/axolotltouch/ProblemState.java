package com.example.axolotltouch;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//This contains all information concerning the problem rules and substitutions 
//as well as functions providing important features. 
public class ProblemState implements Parcelable {
	class IndexingException extends Exception { private static final long serialVersionUID = 1L;}
	public Term[] sSequent;
	public Term[] rSequent;
	public String selectedVariable;
	public ArrayList<String> selectedSubstitutions;
	public Term substitution;
	public HashSet<String> Variables;
	public HashSet<String> Constants;
	public HashMap<String,Integer> Functions;
	public HashMap<String,ArrayList<Term>> Substitutions;
	public ArrayList<ArrayList<Term>> Rules;
	public ArrayList<ArrayList<Term>> OpenProblems;
	public ArrayList<ArrayList<Term[]>> History;
	public ArrayList<Term> SubHistory;

	ProblemState(){
		rSequent = new Term[]{Const.HoleSelected,Const.HoleSelected};
		sSequent = new Term[]{Const.Hole,Const.Hole};
		selectedVariable = "";
		selectedSubstitutions = new ArrayList<>();
		substitution =  Const.HoleSelected;
		SubHistory = new ArrayList<Term>();
		SubHistory.add(substitution.Dup());
		Rules = new ArrayList<>();
		OpenProblems = new ArrayList<>();
		Substitutions = new HashMap<>();
		Functions = new HashMap<>();
		Variables = new HashSet<>();
		Constants = new HashSet<>();
		History= new ArrayList<>();
	}
    ProblemState(Parcel in){
		selectedVariable = in.readString();
		selectedSubstitutions = new ArrayList<>();
		in.readStringList(selectedSubstitutions);
		substitution  = (Term) in.readTypedObject(Term.CREATOR);
		sSequent = new Term[in.readInt()];
		in.readTypedArray(sSequent,Term.CREATOR);
		rSequent = new Term[in.readInt()];
		in.readTypedArray(rSequent,Term.CREATOR);
		String[] tempVar = new String[in.readInt()];
        in.readStringArray(tempVar);
        Variables = new HashSet<String>();
        for (String t: tempVar) Variables.add(t);
		String[] tempConst = new String[in.readInt()];;
        in.readStringArray(tempConst);
        Constants = new HashSet<String>();
        for (String t: tempConst) Constants.add(t);
        Functions =  new HashMap<>();
        Functions = in.readHashMap(Integer.class.getClassLoader());
        Substitutions = new HashMap<>();
		int subsize = in.readInt();
		for(int i = 0; i< subsize; i++){
			String key  = in.readString();
			ArrayList<Term> temp = new ArrayList<>();
			in.readTypedList(temp,Term.CREATOR);
			Substitutions.put(key,temp);
		}
        int rulesSize= in.readInt();
		Rules = new ArrayList<>();
        if(rulesSize!= 0){
        	while( rulesSize>0){
        		ArrayList<Term> temp = new ArrayList<>();
				in.readTypedList(temp,Term.CREATOR);
				Rules.add(temp);
				rulesSize--;
			}

		}
		int probSize= in.readInt();
		OpenProblems = new ArrayList<>();
		if(probSize!= 0){
			while( probSize>0){
				ArrayList<Term> temp = new ArrayList<>();
				in.readTypedList(temp,Term.CREATOR);
				OpenProblems.add(temp);
				probSize--;
			}

		}
		int hisSize = in.readInt();
		History = new ArrayList<>();
		if(hisSize!= 0){
			while( hisSize>0){
				int histSize = in.readInt();
				ArrayList<Term[]> temphis = new ArrayList<>();
				if(hisSize!= 0){
					Term[] temp = new Term[in.readInt()];
					in.readTypedArray(temp, Term.CREATOR);
					temphis.add(temp);
					histSize--;
				}
				History.add(temphis);
				hisSize--;
			}
		}
        SubHistory = new ArrayList<Term>();
        in.readTypedList(SubHistory,Term.CREATOR);
    }
//This method checks if the given problem Term is properly constructed
//i.e. variable free and functions and constants are used correctly
	public boolean ProperProblemTerm(Term prob){
		boolean ret = true;
		for (String t: this.Constants) if(t.compareTo(prob.getSym())==0 && prob.Print().contains("(")) ret =false;
		if(ret) for (String t: this.Variables) if(t.compareTo(prob.getSym())==0) ret =false;
		if(ret) for (String t: this.Functions.keySet()) if(t.compareTo(prob.getSym())==0 && prob.Print().contains("(") && ((Func)prob).Args.size() != Functions.get(t)) ret = false;
		if(prob instanceof Func) 
			for(Term t:((Func) prob).Args) ret &= ProperProblemTerm(t);
		return ret;
	}
//This method checks if the given Rule Term is properly constructed
	public boolean ProperRuleTerm(Term prob){
		boolean ret = true;
		for (String t: this.Constants) if(t.compareTo(prob.getSym())==0 && prob.Print().contains("(")) ret =false;
		if(ret) for (String t: this.Variables) if(t.compareTo(prob.getSym())==0 && prob.Print().contains("(")) ret =false;
		if(ret) for (String t: this.Functions.keySet()) if(t.compareTo(prob.getSym())==0 && !prob.Print().contains("(") && ((Func)prob).Args.size() != Functions.get(t)) ret = false;
		if(ret && prob instanceof Func)  for(Term t:((Func) prob).Args) ret &= ProperRuleTerm(t);
		return ret;
	}
//This method checks if the given substitution Term is properly constructed
//that is variable free
	public boolean ProperSubTerm(Term prob){
		boolean ret = true;
		for (String t: this.Constants) if(t.compareTo(prob.getSym())==0 && prob.Print().contains("(")) ret =false;
		if(ret)for (String t: this.Variables)if(t.compareTo(prob.getSym())==0 && prob.Print().contains("(")) ret =false;
		if(ret) for (String t: this.Variables) if(t.compareTo(prob.getSym())==0) ret = false;
		if(ret) for (String t: this.Functions.keySet()) if(t.compareTo(prob.getSym())==0 && !prob.Print().contains("(") && ((Func)prob).Args.size() != Functions.get(t)) ret = false;
		if(ret && prob instanceof Func) for(Term t:((Func) prob).Args) if(!this.ProperSubTerm(t)) ret=false;
		return ret;
	}
//This function adds all constants and functions within a term to the term index
//Written for Problem terms only 
	public void TermIndex(Term ti) throws IndexingException{
		if(ti instanceof Func) {
			for(Term t:((Func) ti).Args) TermIndex(t);
			Functions.put(ti.getSym(), ((Func) ti).Args.size());
		}else {
			boolean ret = true;
			for (String t: this.Variables) if(t.compareTo(ti.getSym())==0) ret =false;
		    if(ret) Constants.add(ti.getSym());
		    else throw new IndexingException();
		}
	}
//This function adds all constants and functions within a term to the term index
//Written for Rule terms only 
	public void TermIndexR(Term ti){
		if(ti instanceof Func) {
			for(Term t:((Func) ti).Args) TermIndexR(t);
			Functions.put(ti.getSym(), ((Func) ti).Args.size());
		}else {
			boolean ret = true;
			for (String t: this.Variables)if(t.compareTo(ti.getSym())==0) ret =false;
		    if(ret) Constants.add(ti.getSym());
		}
	}
//Checks if every symbol within a term is indexed
	public boolean isIndexed(Term ti){
		boolean result = true;
		if(ti instanceof Func) {
			for(Term t:((Func) ti).Args) result &= isIndexed(t);
			return Functions.keySet().contains(ti.getSym()) && Functions.get(ti.getSym())== ti.subTerms().size() && result;
		}
		else return Constants.contains(ti.getSym()) || Variables.contains(ti.getSym());
	}
//Finds all variables within a term
	public HashSet<String> VarList(Term ti){
		HashSet<String> vl = new HashSet<String>();
		if(ti instanceof Func) for(Term t:((Func) ti).Args) vl.addAll(VarList(t));
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
		out.writeString(selectedVariable);
		out.writeStringList(selectedSubstitutions);
		out.writeTypedObject(substitution,CONTENTS_FILE_DESCRIPTOR);
		out.writeInt(2);
		out.writeTypedArray(sSequent,CONTENTS_FILE_DESCRIPTOR);
		out.writeInt(2);
    	out.writeTypedArray(rSequent,CONTENTS_FILE_DESCRIPTOR);
		out.writeInt(Variables.size());
		out.writeStringArray(Variables.toArray(new String[0]));
    	out.writeInt(Constants.size());
		out.writeStringArray(Constants.toArray(new String[0]));
        out.writeMap(Functions);
        out.writeInt(Substitutions.keySet().size());
        for(String s : Substitutions.keySet()){
        	out.writeString(s);
        	out.writeTypedList(Substitutions.get(s));
		}
		out.writeInt(Rules.size());
		for(ArrayList<Term> rule: Rules) out.writeTypedList(rule);
		out.writeInt(OpenProblems.size());
		for(ArrayList<Term> prob: OpenProblems) out.writeTypedList(prob);
		out.writeInt(History.size());
		for(ArrayList<Term[]> his: History){
			out.writeInt(his.size());
			for(Term[] hist : his){
				out.writeInt(hist.length);
				out.writeTypedArray(hist,CONTENTS_FILE_DESCRIPTOR);
			}
		}
        out.writeTypedList(SubHistory);
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
