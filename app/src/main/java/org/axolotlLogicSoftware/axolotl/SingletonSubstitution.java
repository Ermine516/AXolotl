package org.axolotlLogicSoftware.axolotl;

import android.os.Parcel;
import android.os.Parcelable;

class SingletonSubstitution implements Parcelable {
    public static final Creator<SingletonSubstitution> CREATOR = new Creator<SingletonSubstitution>() {
        @Override
        public SingletonSubstitution createFromParcel(Parcel in) {
            return new SingletonSubstitution(in.readString(), in.readTypedObject(Term.CREATOR));
        }

        @Override
        public SingletonSubstitution[] newArray(int size) {
            return new SingletonSubstitution[size];
        }
    };
    String variable;
    Term replacement;

    SingletonSubstitution(String var, Term r) {
        variable = var;
        replacement = r;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(variable);
        dest.writeTypedObject(replacement, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "{" + variable + " <- " + replacement.toString() + "}";
    }
}