package com.example.axolotltouch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
//General term Interface 
interface Term  extends Parcelable {
     <R> R accept(Visitor<R> visitor);
     String Print();
      HashMap<String,HashSet<Integer>> basicTerms();
     ArrayList<Term> subTerms();
     String getSym();
     Term replace(Const c, Term r);
     Term replaceLeft(Const c, Term r);
     boolean contains(Const c);
     Term Dup();
      boolean uniqueArity();
      interface Visitor<R> {
     	 R visitConst(String sym);
         R visitFunc(String sym, ArrayList<Term> args);
    }

    Parcelable.Creator<Term> CREATOR = new Parcelable.Creator<Term>() {
		public Term createFromParcel(Parcel in) {
			String tempSym = in.readString();
			ArrayList<Term> tempSub = new ArrayList<>();
			in.readTypedList(tempSub,Term.CREATOR);
			if(tempSub.isEmpty())	return new Const(tempSym);
		    else return new Func(tempSym,tempSub);
		}

		public Term[] newArray(int size) {
			return new Term[size];
		}
	};}
//Class defining Constant terms
final class Const  implements Term, Parcelable  {

	//Special constants used for substitution definition
	public final static Const HoleSelected = new Const("⚫");
	public final static Const Hole = new Const("⚪");
//Constant symbol
    private final String Sym;
	private  ArrayList<Term> Args;
    public Const(String sym) {
    	Sym = sym;
		Args = new ArrayList<Term>();
	}
	//Constants always have a unique arity
    public  boolean uniqueArity(){return true;}
//For visitor definition
    public <R> R accept(Visitor<R> visitor) {return visitor.visitConst(Sym);}
//Constants don't have direct subterms
    public ArrayList<Term> subTerms(){return Args;}
//Return term symbol
    public String getSym(){return this.Sym;}
//Prints the constant as a string
    public String Print(){return this.Sym;}
//Creates a duplicate term object
    public Term Dup(){return new Const(new String(this.Sym));}
//Replaces the constant by the term r
    public Term replace(Const c, Term r){
        if(c.Sym.matches(this.Sym)) return r.Dup();
        else return this;
    }
//Replaces the constant by the term r
    public Term replaceLeft(Const c, Term r) {return replace(c,r);}
//checks if a constant is equivalent to this constant
    public boolean contains(Const c){
        if(c.Sym.matches(this.Sym)) return true;
        else return false;
    }
//Finds all the term symbols in the term tree
    public  HashMap<String,HashSet<Integer>> basicTerms(){
    	HashMap<String,HashSet<Integer>> result = 	new HashMap<String,HashSet<Integer>>();
    	HashSet<Integer> arities = new HashSet<Integer>();
    	 arities.add(0);
    	 result.put(this.Sym,arities);
    	 return result;
    }
	@Override
	public int describeContents() {
		return 0;
	}
	// write your object's data to the passed-in Parcel
	@Override
	public   void writeToParcel(Parcel out, int flags) {
		out.writeString(this.getSym());
		out.writeTypedList(this.subTerms());
	}
}
//Class defining function terms
final class Func implements Term, Parcelable{
     final public String Sym;
     final public ArrayList<Term> Args;
    protected Func(String sym, ArrayList<Term> args) {
        this.Sym = sym;
        this.Args = args;
    }
	//Returns all proper direct subterms
    public ArrayList<Term> subTerms(){return this.Args;}
//Returns term symbol
    public String getSym(){return this.Sym;}
//Part of visitor definition
    public <R> R accept(Visitor<R> visitor) { return visitor.visitFunc(Sym, Args);}
//Duplicates term object 
    public Term Dup(){
    	ArrayList<Term> newArgs = new ArrayList<Term>();
    	for(int i = 0; i< this.Args.size(); i++) newArgs.add(this.Args.get(i).Dup());
    	return new Func(new String(this.Sym),newArgs);
    }
//Replaces every instance of the given constant c by the term r
    public Term replace(Const c, Term r){
    	if(this instanceof Func) {
    		ArrayList<Term> newArgs = new ArrayList<Term>();
    		for(int i = 0; i< this.Args.size(); i++) newArgs.add(this.Args.get(i).replace(c,r));
    		return new Func(new String(this.Sym),newArgs);
    	}
    	else{
    		if(this.getSym().compareTo(c.getSym())==0) return r.Dup();
    		else return this;
    	}
    }
//Replaces left most instance of the given constant c by the term r
    public Term replaceLeft(Const c, Term r) {
    	if(this instanceof Func) {
    		ArrayList<Term> newArgs = new ArrayList<Term>();
    		boolean diff = false;
    		for(int i = 0; i< this.Args.size(); i++) {
    			if(!diff) newArgs.add(this.Args.get(i).replaceLeft(c,r));
    			else newArgs.add(this.Args.get(i));
    			if(this.Args.get(i).Print().compareTo(newArgs.get(newArgs.size()-1).Print())!= 0)diff = true;
    			
    		}
    		return new Func(new String(this.Sym),newArgs);
    	}
    	else
    		if(this.getSym().compareTo(c.getSym())==0) return r.Dup();
    		else return this;
    }
//Checks if a term symbol is present in a given term tree
    public boolean contains(Const c){
    	boolean ret = false;
    	if(this instanceof Func) {
    		for(int i = 0; i< this.Args.size(); i++) 
    			if(!ret) ret |= this.Args.get(i).contains(c);
    		return ret;
    	}
    	else
    		if(this.getSym().compareTo(c.getSym())==0) return true;
    		else return false;
    }
//Prints term tree as a string
    public String Print(){
    	String s =Sym+"(";
    	int i=0;
    	for(;i<(this.Args.size()-1);i++) s+=this.Args.get(i).Print()+",";
    	Term t = this.Args.get(i);
    	String ss = t.Print();
    	if(this.Args.size()>0) s+=ss+")";
    	return s;
    }
//Finds all the term symbols in the term tree
    public  HashMap<String,HashSet<Integer>> basicTerms(){
    	HashMap<String,HashSet<Integer>> result = new HashMap<String,HashSet<Integer>>();
    	HashSet<Integer> temp3 = new HashSet<Integer>();
    	temp3.add(this.Args.size());
    	result.put(this.Sym,temp3 );
     for(Term t : this.Args){
    	 HashMap<String,HashSet<Integer>> temp = t.basicTerms();
    	 for(String s:temp.keySet()){
    		 if(result.containsKey(s)){
    			 HashSet<Integer> temp2 = result.get(s);
    			 temp2.addAll(temp.get(s));
    			 result.put(s, temp2);
    		 }
    		 else result.put(s, temp.get(s));
    	 }
     }
     return result;
    }
//Checks if a term tree does not contain the same function symbol with two different arities
    public  boolean uniqueArity(){
    	HashMap<String,HashSet<Integer>> store = this.basicTerms();
    	boolean result = true; 
    	for(HashSet<Integer> h :store.values()) result &= (h.size()==1)? true : false;
        return result;
    }
	@Override
	public int describeContents() {
		return 0;
	}
	// write your object's data to the passed-in Parcel
	@Override
	public   void writeToParcel(Parcel out, int flags) {
		out.writeString(this.getSym());
		out.writeTypedList(this.subTerms());
	}
}
