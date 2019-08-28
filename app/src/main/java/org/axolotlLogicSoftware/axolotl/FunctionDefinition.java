package org.axolotlLogicSoftware.axolotl;

import android.os.Parcel;
import android.os.Parcelable;

public class FunctionDefinition implements Parcelable {
    public static final Creator<FunctionDefinition> CREATOR = new Creator<FunctionDefinition>() {
        @Override
        public FunctionDefinition createFromParcel(Parcel in) {
            return new FunctionDefinition(in.readString(), in.readInt(), ((in.readInt() == 1) ? true : false));
        }

        @Override
        public FunctionDefinition[] newArray(int size) {
            return new FunctionDefinition[size];
        }
    };
    String name;
    Integer arity;
    Boolean fixity;

    public FunctionDefinition(String s, int a, boolean f) {
        name = s;
        arity = a;
        fixity = f;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(arity);
        parcel.writeInt((fixity) ? 1 : 0);
    }
}
