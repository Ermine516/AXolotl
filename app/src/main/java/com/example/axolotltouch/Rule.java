package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;

public class Rule implements Parcelable {
    private static final String RULESYMBOL = "<font color=#ff0000>\u2b05</font>";
    String Label;
    ArrayList<Term> Conclusions;
    Term argument;
    private HashSet<String> variables;

    static Parcelable.Creator<Rule> CREATOR = new Parcelable.Creator<Rule>() {
        public Rule createFromParcel(Parcel in) {
            String label = in.readString();
            int varSize = in.readInt();
            HashSet<String> variables = new HashSet<>();
            if (varSize > 0)
                for (int i = 0; i < varSize; i++)
                    variables.add(in.readString());

            int ruleSize = in.readInt();
            ArrayList<Term> con = new ArrayList<>();
            if (ruleSize > 0)
                for (int i = 0; i < ruleSize; i++)
                    con.add(in.readTypedObject(Term.CREATOR));
            return new Rule(label, con, in.readTypedObject(Term.CREATOR), variables);

        }

        public Rule[] newArray(int size) {
            return new Rule[size];
        }
    };


    Rule(String l, ArrayList<Term> con, Term a, HashSet<String> vars) {
        Label = l;
        Conclusions = con;
        argument = a;
        variables = vars;
    }

    Rule(Rule r) {
        Label = r.Label;
        Conclusions = r.Conclusions;
        argument = r.argument;
        variables = r.variables;
    }

    Rule() {
        Label = "";
        Conclusions = new ArrayList<>();
        argument = Const.HoleSelected;
        variables = new HashSet<>();
    }

    static String RuleTermsToString(Rule rule) {
        if (rule != null && rule.Conclusions != null && rule.argument != null) {
            StringBuilder retString = new StringBuilder("Δ ");
            ArrayList<Term> varAsTerms = new ArrayList<>();
            for (String var : rule.variables) varAsTerms.add(new Const(var));
            if (rule.Conclusions.size() > 0)
                for (int i = 0; i < rule.Conclusions.size(); i++)
                    if (i == 0 && i != rule.Conclusions.size() - 1)
                        retString.append(", ").append(rule.Conclusions.get(i).PrintBold(varAsTerms)).append(" , ");
                    else if (0 == rule.Conclusions.size() - 1)
                        retString.append(", ").append(rule.Conclusions.get(i).PrintBold(varAsTerms)).append(" " + RULESYMBOL).append(" Δ , ");
                    else if (i == rule.Conclusions.size() - 1)
                        retString.append(rule.Conclusions.get(i).PrintBold(varAsTerms)).append(" " + RULESYMBOL).append(" Δ , ");
                    else
                        retString.append(rule.Conclusions.get(i).PrintBold(varAsTerms)).append(" , ");
            else retString.append(RULESYMBOL).append(" Δ , ");
            return retString + rule.argument.PrintBold(varAsTerms);
        } else return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(Label);
        parcel.writeInt(variables.size());
        for (String s : variables) parcel.writeString(s);
        parcel.writeInt(Conclusions.size());
        for (Term t : Conclusions)
            parcel.writeTypedObject(t, flags);
        parcel.writeTypedObject(argument, flags);
    }
}
