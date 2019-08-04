package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

public class State implements Parcelable {
    public static final Creator<State> CREATOR = new Creator<State>() {
        @Override
        public State createFromParcel(Parcel in) {
            return new State(in.readString(), in.readTypedObject(Substitution.CREATOR), in.readTypedObject(Rule.CREATOR));
        }

        @Override
        public State[] newArray(int size) {
            return new State[size];
        }
    };
    String selection;
    Substitution substitution;
    Rule rule;

    State(String s, Substitution sub, Rule r) {
        selection = s;
        substitution = sub;
        rule = r;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(selection);
        parcel.writeTypedObject(substitution, i);
        parcel.writeTypedObject(rule, i);
    }
}
