package org.axolotlLogicSoftware.axolotl;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
//General term Interface 
public interface Term extends Parcelable {
    Parcelable.Creator<Term> CREATOR = new Parcelable.Creator<Term>() {
		public Term createFromParcel(Parcel in) {
			String tempSym = in.readString();
            int infix = in.readInt();
			ArrayList<Term> tempSub = new ArrayList<>();
			in.readTypedList(tempSub,Term.CREATOR);
			if(tempSub.isEmpty())	return new Const(tempSym);
            else return new Func(tempSym, tempSub, infix != 0);
		}
		public Term[] newArray(int size) {
			return new Term[size];
		}
    };


    String Print(ArrayList<Term> terms, Adjustment adj);

    String Print(String var, Term compare, Term t, Adjustment adj);

    String PrintCons(ArrayList<Term> terms, Adjustment adj);

    String PrintCons(String var, Term compare, Term t, Adjustment adj);

    void normalize(HashSet<String> var);

    HashMap<String, HashSet<Integer>> basicTerms();

    ArrayList<Term> subTerms();

    String getSym();


    Term replace(Const c, Term r);

    Term replaceLeft(Const c, Term r);

    boolean contains(Const c);

    Term Dup();

    String toString();

    boolean equals(Object o);

    int hashCode();

}

