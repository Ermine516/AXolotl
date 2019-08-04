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
    public static final String SEQUENT = "⊢";
    public static final String LIST = "cons";
    public static final String EMPTYLIST = "ε";
    public static final String[] RESERVEDFUNCTIONS = new String[]{SEQUENT, LIST};
    public static final String[] RESERVEDCONSTANTS = new String[]{EMPTYLIST};

    int subPos;
    HashMap<String, Boolean> MatchorConstruct;
    boolean observe;
    int textSize;
    int mainActivityState;
    HashSet<Term> anteProblem;
    ArrayList<String> anteSelectedPositions;
    HashSet<Term> succProblem;
    String succSelectedPosition;
    Rule currentRule;
    HashSet<String> Variables;
    ArrayList<String> Constants;
    ArrayList<Pair<String, Pair<Integer, Boolean>>> Functions;
    Substitution Substitutions;
    ArrayList<Rule> Rules;
    ArrayList<Pair<Pair<ArrayList<String>, String>, Pair<Substitution, Rule>>> History;
    HashMap<String, ArrayList<Term>> SubHistory;

    public ProblemState() {
        subPos = -1;
        observe = true;
        textSize = 25;
        mainActivityState = -1;
        MatchorConstruct = new HashMap<>();
        anteProblem = new HashSet<>();
        anteProblem.add(Const.Hole);
        anteSelectedPositions = new ArrayList<>();
        succProblem = new HashSet<>();
        succProblem.add(Const.Hole);
        succSelectedPosition = "";
        currentRule = new Rule("", new ArrayList<Term>(), Const.HoleSelected.Dup(), new HashSet<>());

        SubHistory = new HashMap<>();
		Rules = new ArrayList<>();
        Substitutions = new Substitution(new ArrayList<SingletonSubstitution>());
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
        mainActivityState = in.readInt();

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
        for (int i = 0; i < funcSize; i++) {
            String key = in.readString();
            int arity = in.readInt();
            boolean infix = in.readInt() == 1;
            Functions.add(new Pair<>(key, new Pair<>(arity, infix)));
        }
        Substitutions = in.readTypedObject(Substitution.CREATOR);
        int rulesSize= in.readInt();
		Rules = new ArrayList<>();
        for (int i = 0; i < rulesSize; i++) Rules.add(in.readTypedObject(Rule.CREATOR));


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
                    Substitution hissubs = in.readTypedObject(Substitution.CREATOR);
                    History.add(new Pair<>(new Pair<>(anteselected, ""), new Pair<>(hissubs, in.readTypedObject(Rule.CREATOR))));
                    hisSize--;
                } else {
                    String succside = in.readString();
                    Substitution hissubs = in.readTypedObject(Substitution.CREATOR);
                    History.add(new Pair<>(new Pair<>(new ArrayList<String>(), succside), new Pair<>(hissubs, in.readTypedObject(Rule.CREATOR))));
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



    @SuppressWarnings("ConstantConditions")
    boolean containsFunctionSymbol(String func) {
        boolean contained = true;
        for (Pair<String, Pair<Integer, Boolean>> p : Functions)
            if (func.compareTo(p.first) == 0) contained = false;
        return contained;
    }

    HashSet<Term> replaceSelectedSuccTerm(HashSet<Term> replacement) {
        HashSet<Term> succupdate = new HashSet<>();
        for (Term t : succProblem)
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

    boolean SequentProblem() {
        for (Term t : this.succProblem)
            if (TermHelper.wellformedSequents(t)) return true;
        return false;
    }

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
        out.writeInt(anteProblem.size());
        for (Term t : anteProblem) out.writeTypedObject(t, flags);
        out.writeInt(anteSelectedPositions.size());
        for (String t : anteSelectedPositions) out.writeString(t);
        out.writeInt(succProblem.size());
        for (Term t : succProblem) out.writeTypedObject(t, flags);
        out.writeString(succSelectedPosition);
        out.writeTypedObject(currentRule, flags);
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
        out.writeTypedObject(Substitutions, flags);
        out.writeInt(Rules.size());
        for (Rule rule : Rules) out.writeTypedObject(rule, flags);

		out.writeInt(History.size());
        for (Pair<Pair<ArrayList<String>, String>, Pair<Substitution, Rule>> his : History) {
            Pair<ArrayList<String>, String> selection = his.first;
            Substitution substitution = his.second.first;
            Rule rule = his.second.second;
            if (selection.first.size() != 0) {
                out.writeInt(0);
                out.writeInt(selection.first.size());
                for (String s : selection.first) out.writeString(s);
            } else {
                out.writeInt(1);
                out.writeString(selection.second);
            }
            out.writeTypedObject(substitution, flags);
            out.writeTypedObject(rule, flags);
        }
        out.writeInt(SubHistory.size());
        for (String key : SubHistory.keySet()) {
            out.writeString(key);
            out.writeTypedList(SubHistory.get(key));
        }
    }
}
