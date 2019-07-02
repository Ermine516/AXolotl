package com.example.axolotltouch;


import java.util.ArrayList;


public class TermHelper {
	class FormatException extends Exception {
		private static final long serialVersionUID = 1L;}
	public static Term parse(String s) throws FormatException {
		Term result = pI(clean(s)).get(0).get(0);
		if(clean(s).compareTo(result.Print())!=0)throw (new TermHelper()).new FormatException();
		else return result;
	}
	public static ArrayList<ArrayList<Term>> pI(String s) throws FormatException{
//Finds first left peren
		String[] pL = s.split("(\\s*[\\(]{1}\\s*)+?",2) , 
//Finds first comma peren
				 pC = s.split("(\\s*[,]{1}\\s*)+?",2), 
//Finds first right peren
				 pR = s.split("(\\s*[\\)]{1}\\s*)+?",2);
//if a comma or a left peren is found then continue, otherwise we have reached the end of the term		
		 ArrayList<ArrayList<Term>> res = (pL[0].contains(","))? pI(pC[1]) : (pL.length == 2)? pI(pL[1]) : new ArrayList<ArrayList<Term>>();
//When a comma is found in 	pL[0] we check if there is also right peren indicating a nested term
//Right Perens are replaced by empty arrays indicating unknown function nesting
			if(pL[0].contains(",")){ 
				if(pC[0].contains(")")) 
					for(int i = 0; i<(pC[0].length() - pC[0].replace(")", "").length());i++) 
						res.add(0,new ArrayList<Term>());
			}
//pL of length 2 indicates that a function symbol occurred and we need to introduce a node using the previous 
//computed terms
			else if(pL.length == 2){
				Func f = new Func(pL[0],res.remove(0));
				if(res.size()==0) res.add(0,new ArrayList<Term>()); 
				res.get(0).add(0,f);
			}
//Otherwise we have reached a constant
		    else res.add(new ArrayList<Term>());
//There may be more than one constant separated by a comma
			if(pL.length != 2 || pL[0].contains(",")) 
				res.get(0).add(0,new Const((pC[0].contains(")")|| (!pL[0].contains(",") && pL.length != 2))? pR[0]:pC[0]));
		   return res;
	}
	public static String clean(String s) throws FormatException{
		String[] spaces = s.split("\\s*");
		String ret = "";
		if(spaces.length==0) throw (new TermHelper()).new FormatException();
		for(String ss:spaces)
			if(!ss.contains("\\s*")) ret+= ss;
		return ret;
	}
}
