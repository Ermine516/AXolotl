package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Rule implements Parcelable {
    static Parcelable.Creator<Rule> CREATOR = new Parcelable.Creator<Rule>() {
        public Rule createFromParcel(Parcel in) {
            String label = in.readString();
            int ruleSize = in.readInt();
            ArrayList<Term> rule;
            if (ruleSize > 0) {
                rule = new ArrayList<>();
                for (int ri = 0; ri < ruleSize; ri++)
                    rule.add(in.readTypedObject(Term.CREATOR));
            } else rule = new ArrayList<>();
            return new Rule(label, rule, in.readTypedObject(Term.CREATOR));

        }

        public Rule[] newArray(int size) {
            return new Rule[size];
        }
    };
    String Label;
    ArrayList<Term> Conclusions;
    Term argument;

    Rule(String l, ArrayList<Term> con, Term a) {
        Label = l;
        Conclusions = con;
        argument = a;
    }

    Rule() {
        Label = "";
        Conclusions = new ArrayList<>();
        argument = Const.HoleSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(Label);
        parcel.writeInt(Conclusions.size());
        for (int i = 0; i < Conclusions.size(); i++)
            parcel.writeTypedObject(Conclusions.get(i), flags);
        parcel.writeTypedObject(argument, flags);
    }
}
