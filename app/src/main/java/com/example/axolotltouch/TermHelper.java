package com.example.axolotltouch;


import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;


class TermHelper {
	class FormatException extends Exception {
		private static final long serialVersionUID = 1L;}

    static Term parse(String s, ProblemState PS) throws FormatException {
        Term result = pI(clean(s), PS).get(0).get(0);
        System.out.println(clean(s) + " " + result.toString() + " " + result.Print());
        if (clean(s).compareTo(result.toString()) != 0)
            throw (new TermHelper()).new FormatException();
		else return result;
	}

    private static ArrayList<ArrayList<Term>> pI(String s, ProblemState PS) {
//Finds first left peren
        String[] pL = s.split("(\\s*[(]{1}\\s*)+?", 2),
//Finds first comma peren
				 pC = s.split("(\\s*[,]{1}\\s*)+?",2), 
//Finds first right peren
                pR = s.split("(\\s*[)]{1}\\s*)+?", 2);
//if a comma or a left peren is found then continue, otherwise we have reached the end of the term		
        ArrayList<ArrayList<Term>> res = (pL[0].contains(",")) ? pI(pC[1], PS) : (pL.length == 2) ? pI(pL[1], PS) : new ArrayList<ArrayList<Term>>();
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
                boolean infixity = false;
                for (Pair<String, Pair<Integer, Boolean>> p : PS.Functions)
                    if (pL[0].compareTo(p.first) == 0) infixity = p.second.second;

                Func f = new Func(pL[0], res.remove(0), infixity);
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

    private static String clean(String s) throws FormatException {
		String[] spaces = s.split("\\s*");
        StringBuilder ret = new StringBuilder();
		if(spaces.length==0) throw (new TermHelper()).new FormatException();
		for(String ss:spaces)
            if (!ss.contains("\\s*")) ret.append(ss);
        return ret.toString();
    }

    static boolean TermMatch(Term left, Term right, ProblemState PS) {
        if (left instanceof Const && right instanceof Const && left.getSym().compareTo(right.getSym()) == 0)
            return true;
        else if (left instanceof Func && right instanceof Func && left.getSym().compareTo(right.getSym()) == 0) {
            boolean ret = true;
            for (int i = 0; i < left.subTerms().size(); i++)
                ret &= TermMatch(left.subTerms().get(i), right.subTerms().get(i), PS);
            return ret;
        } else
            return (right instanceof Const && PS.Variables.contains(right.getSym())) || (left instanceof Const && PS.Variables.contains(left.getSym()));


    }

    static boolean TermMatch(Term left, Term right) {
        if (left instanceof Const && right instanceof Const && left.getSym().compareTo(right.getSym()) == 0)
            return true;
        else if (left instanceof Func && right instanceof Func && left.getSym().compareTo(right.getSym()) == 0) {
            boolean ret = true;
            for (int i = 0; i < left.subTerms().size(); i++)
                ret &= TermMatch(left.subTerms().get(i), right.subTerms().get(i));
            return ret;
        } else return false;


    }

    static ArrayList<Pair<String, Term>> varTermMatch(Term left, Term right, ProblemState PS) {
        ArrayList<Pair<String, Term>> ret = new ArrayList<>();
        if (PS.VarList(left).size() == 0)
            if (PS.Variables.contains(right.getSym())) ret.add(new Pair<>(right.getSym(), left));
            else if (left.subTerms().size() == right.subTerms().size() && left.subTerms().size() != 0)
                for (int i = 0; i < left.subTerms().size(); i++)
                    ret.addAll(varTermMatch(left.subTerms().get(i), right.subTerms().get(i), PS));
        return ret;
    }

    static HashMap<String, Term> varTermMatchMap(Term left, Term right, ProblemState PS) {
        HashMap<String, Term> ret = new HashMap<>();
        if (PS.VarList(left).size() == 0)
            if (PS.Variables.contains(right.getSym())) ret.put(right.getSym(), left);
            else if (left.subTerms().size() == right.subTerms().size() && left.subTerms().size() != 0)
                for (int i = 0; i < left.subTerms().size(); i++)
                    ret.putAll(varTermMatchMap(left.subTerms().get(i), right.subTerms().get(i), PS));
        return ret;
    }
}
