package com.example.axolotltouch;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

//This contains all information concerning the problem rules and substitutions 
//as well as functions providing important features. 
public class ProblemState implements Parcelable {
    private static final String SEQUENT = "⊢";
    private static final String LIST = "cons";
    private static final String[] RESERVEDFUNCTIONS = new String[]{SEQUENT, LIST};
    private static final String EMPTYLIST = "ε";
    private static final String HOLESELECTEDSYM = Const.HoleSelected.getSym();
    private static final String HOLESYM = Const.Hole.getSym();
    private static final String EMPTYSYM = Const.Empty.getSym();
    private static final String[] RESERVEDCONSTANTS = new String[]{EMPTYLIST, HOLESELECTEDSYM, HOLESYM, EMPTYSYM};

    int subPos;
    HashMap<String, Boolean> MatchorConstruct;
    boolean observe;
    int textSize;
    int mainActivityState;
    HashSet<Term> problem;
    String succSelectedPosition;
    Rule currentRule;
    HashSet<String> Variables;
    ArrayList<String> Constants;
    ArrayList<FunctionDefinition> Functions;
    Substitution Substitutions;
    ArrayList<Rule> Rules;
    ArrayList<State> History;
    HashMap<String, ArrayList<Term>> SubHistory;

    public ProblemState() {
        subPos = -1;
        observe = true;
        textSize = 25;
        mainActivityState = -1;
        MatchorConstruct = new HashMap<>();
        problem = new HashSet<>();
        problem.add(Const.Hole);
        succSelectedPosition = "";
        currentRule = new Rule("", new ArrayList<Term>(), Const.HoleSelected.Dup(), new HashSet<String>());
        SubHistory = new HashMap<>();
		Rules = new ArrayList<>();
        Substitutions = new Substitution(new ArrayList<SingletonSubstitution>());
        Functions = new ArrayList<>();
        Functions.add(new FunctionDefinition("cons", 2, false));
        Functions.add(new FunctionDefinition("⊢", 2, true));
		Variables = new HashSet<>();
        Constants = new ArrayList<>();
        Constants.add("ε");
        History = new ArrayList<>();
	}
    ProblemState(Parcel in){
        subPos = in.readInt();
        observe = in.readInt() == 1;
        textSize = in.readInt();
        mainActivityState = in.readInt();
        int MatchorConstructSize = in.readInt();
        MatchorConstruct = new HashMap<>();
        while (MatchorConstructSize > 0) {
            MatchorConstruct.put(in.readString(), in.readInt() == 1);
            MatchorConstructSize--;
        }
        int problemsize = in.readInt();
        problem = new HashSet<>();
        while (problemsize > 0) {
            problem.add(in.readTypedObject(Term.CREATOR));
            problemsize--;
        }
        succSelectedPosition = in.readString();
        currentRule = in.readTypedObject(Rule.CREATOR);

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
        for (int i = 0; i < funcSize; i++)
            Functions.add(in.readTypedObject(FunctionDefinition.CREATOR));
        Substitutions = in.readTypedObject(Substitution.CREATOR);
        int rulesSize= in.readInt();
		Rules = new ArrayList<>();
        for (int i = 0; i < rulesSize; i++) Rules.add(in.readTypedObject(Rule.CREATOR));


		int hisSize = in.readInt();
		History = new ArrayList<>();
        for (int i = 0; i < hisSize; i++)
            History.add(in.readTypedObject(State.CREATOR));

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
        for (Term t : problem) {
            if (t.Print().compareTo(succSelectedPosition) == 0) return t;
        }
        return null;
    }

    static boolean isReserved(String func) {
        for (String s : RESERVEDFUNCTIONS) {
            if (s.compareTo(func) == 0) return true;
        }
        for (String s : RESERVEDCONSTANTS) {
            if (s.compareTo(func) == 0) return true;
        }
        return false;
    }

    boolean containsFunctionSymbol(String func) {
        boolean contained = true;
        for (FunctionDefinition p : Functions)
            if (func.compareTo(p.name) == 0) contained = false;
        return contained;
    }

    HashSet<Term> replaceSelectedSuccTerm(HashSet<Term> replacement) {
        HashSet<Term> succupdate = new HashSet<>();
        for (Term t : problem)
            if (t.Print().compareTo(succSelectedPosition) != 0) succupdate.add(t);
            else succupdate.addAll(replacement);
        return succupdate;
    }

    //Finds all variables within a term
    HashSet<String> VarList(Term ti) {
        HashSet<String> vl = new HashSet<>();
        if (ti instanceof Func) for (Term t : ti.subTerms()) vl.addAll(VarList(t));
        else for (String t : this.Variables) if (t.compareTo(ti.getSym()) == 0) vl.add(ti.getSym());
        return vl;
    }

    void problemClean() {
        HashSet<Term> newSucc = new HashSet<>();
        for (Term t : problem)
            if (t.getSym().compareTo(Const.Empty.getSym()) != 0) newSucc.add(t);
        problem = newSucc;
    }

    //Checks if every symbol within a term is indexed
    boolean isIndexed(Term ti) {
        boolean result = true;
        if (ti instanceof Func) {
            for (Term t : ti.subTerms()) result &= isIndexed(t);
            boolean contained = false;
            boolean sameArity = false;
            for (FunctionDefinition p : Functions) {
                if (ti.getSym().compareTo(p.name) == 0) {
                    contained = true;
                    if (p.arity == ti.subTerms().size()) sameArity = true;
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


    // write your object's data to the passed-in Parcel
    @Override
    @SuppressWarnings("ConstantConditions")
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(subPos);
        out.writeInt((observe) ? 1 : 0);
        out.writeInt(textSize);
        out.writeInt(mainActivityState);
        out.writeInt(MatchorConstruct.size());
        for (String key : MatchorConstruct.keySet()) {
            out.writeString(key);
            out.writeInt((MatchorConstruct.get(key)) ? 1 : 0);
        }
        out.writeInt(problem.size());
        for (Term t : problem) out.writeTypedObject(t, flags);
        out.writeString(succSelectedPosition);
        out.writeTypedObject(currentRule, flags);
		out.writeInt(Variables.size());
		out.writeStringArray(Variables.toArray(new String[0]));
    	out.writeInt(Constants.size());
		out.writeStringArray(Constants.toArray(new String[0]));
        out.writeInt(Functions.size());
        for (FunctionDefinition f : Functions)
            out.writeTypedObject(f, flags);
        out.writeTypedObject(Substitutions, flags);
        out.writeInt(Rules.size());
        for (Rule rule : Rules) out.writeTypedObject(rule, flags);

		out.writeInt(History.size());
        for (State his : History)
            out.writeTypedObject(his, flags);
        out.writeInt(SubHistory.size());
        for (String key : SubHistory.keySet()) {
            out.writeString(key);
            out.writeTypedList(SubHistory.get(key));
        }
    }
}
