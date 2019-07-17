package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
//General term Interface 
public interface Term extends Parcelable {
     String Print();

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
    String Print(Term t);

    String Print(String var, Term compare, Term t);

    HashMap<String, HashSet<Integer>> basicTerms();

    ArrayList<Term> subTerms();

    String getSym();

    Term replace(Const c, Term r);

    Term replaceLeft(Const c, Term r);

    boolean contains(Const c);

    Term Dup();

    String toString();
}

