package com.example.axolotltouch;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

  class AuxFunctionality {
      static final String PASSPROBLEMSTATE = "com.example.android.AXolotlTouch.extra.problemstate";
      static final int READ_REQUEST_CODE = 42;

     static void SideMenuItems(int id, Activity ctx, ProblemState PS) {
        Intent intent = null;
        if (id == R.id.Problembutton) {
            Toast.makeText(ctx, "Problem", Toast.LENGTH_SHORT).show();
            intent = new Intent(ctx, MainActivity.class);
        } else if (id == R.id.RuleSelectButton) {
            Toast.makeText(ctx, "Rule Select", Toast.LENGTH_SHORT).show();
            intent = new Intent(ctx, RuleSelectionActivity.class);
        } else if (id == R.id.TermConstructButton) {
            intent = new Intent(ctx, TermConstructActivity.class);
            Toast.makeText(ctx, "Term Construct", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.SubstitutionConstructButton) {
            intent = new Intent(ctx, SubstitutionConstructActivity.class);

            Toast.makeText(ctx, "Substitution Construct", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.SubstitutionSelectButton) {
            intent = new Intent(ctx, SubstitutionSelectionActivity.class);
            Toast.makeText(ctx, "Substitution Select", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.ViewProof) {
            intent = new Intent(ctx, ProofDisplayActivity.class);
            Toast.makeText(ctx, "View Proof", Toast.LENGTH_SHORT).show();
        }
        if (intent != null) {
            intent.putExtra(PASSPROBLEMSTATE, PS);
            ctx.startActivity(intent);
	    ctx.finish();
        }
    }

     private static void performFileSearch(Activity ctx) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        ctx.startActivityForResult(intent, READ_REQUEST_CODE);
    }

     static ProblemState loadFile(InputStream IS, String file, Activity ctx) {
        ProblemState newPS = new ProblemState();
        String line;
        int lineCount = 0;
         boolean foundProblemOrRule = false;
         try (BufferedReader br = new BufferedReader(new InputStreamReader(IS))) {
             while ((line = br.readLine()) != null) {
                 System.out.println(line);
                 if (line.matches("\\s+")) continue;
                 lineCount++;
                 String[] parts = line.split("\\s+");
//**********************************************Parsing functions from file***************************************************
                 if (parts[0].compareTo("Function:") == 0) {
                     if (foundProblemOrRule) throw new IOException();
                     StringBuilder name = new StringBuilder();
                     StringBuilder arity = new StringBuilder();
                     for (int i = 1; i < parts.length; i++)
                         if (parts[i].matches("[a-zA-Z]+")) name.append(parts[i]);
                     for (int i = 2; i < parts.length; i++)
                         if (parts[i].matches("[0-9]+")) arity.append(parts[i]);
                     if (name.toString().matches("[a-zA-Z]+")
                             && arity.toString().matches("[0-9]+")
                             && !newPS.Variables.contains(name.toString())
                             && !newPS.Constants.contains(name.toString())
                             && !newPS.Functions.containsKey(name.toString()))
                         if (Integer.parseInt(arity.toString()) == 0)
                             newPS.Constants.add(name.toString());
                         else
                             newPS.Functions.put(name.toString(), Integer.parseInt(arity.toString()));
                     else throw new TermHelper().new FormatException();
//**********************************************Parsing Rules from file***************************************************
                 } else if (parts[0].compareTo("Rule:") == 0) {
                     foundProblemOrRule = true;
                     boolean aTerm = false;
                     String posTermOne = parts[1];
                     int i = 2;
                     while (!aTerm && i < parts.length) {
                         try {
                             newPS.rSequent[0] = TermHelper.parse(posTermOne);
                         } catch (TermHelper.FormatException ex) {
                             posTermOne += parts[i];
                             i++;
                             continue;
                         }
                         aTerm = true;
                     }
                     if (i == parts.length) throw new TermHelper().new FormatException();
                     String posTermTwo = parts[i];
                     i++;
                     aTerm = false;
                     while (!aTerm) {
                         try {
                             newPS.rSequent[1] = TermHelper.parse(posTermTwo);
                         } catch (TermHelper.FormatException ex) {
                             if (i == parts.length) throw new TermHelper().new FormatException();
                             posTermTwo += parts[i];
                             i++;
                             continue;
                         }
                         aTerm = true;
                     }
                     if (i < parts.length) throw new TermHelper().new FormatException();
                     if (posTermOne.matches("\\s*") || !newPS.isIndexed(newPS.rSequent[0]))
                         throw new TermHelper().new FormatException();
                     if (posTermTwo.matches("\\s*") || !newPS.isIndexed(newPS.rSequent[1]))
                         throw new TermHelper().new FormatException();
                     ArrayList<Term> newRule = new ArrayList<>();
                     newRule.add(newPS.rSequent[0]);
                     newRule.add(newPS.rSequent[1]);
                     newPS.Rules.add(newRule);
//**********************************************Parsing Problems from file***************************************************
                 } else if (parts[0].compareTo("Problem:") == 0) {
                     foundProblemOrRule = true;
                     boolean aTerm = false;
                     String posTermOne = parts[1];
                     int i = 2;
                     while (!aTerm && i < parts.length) {
                         try {
                             newPS.sSequent[0] = TermHelper.parse(posTermOne);
                         } catch (TermHelper.FormatException ex) {
                             posTermOne += parts[i];
                             i++;
                             continue;
                         }
                         aTerm = true;
                     }
                     if (i == parts.length) throw new TermHelper().new FormatException();
                     String posTermTwo = parts[i];
                     i++;
                     aTerm = false;
                     while (!aTerm) {
                         try {
                             newPS.sSequent[1] = TermHelper.parse(posTermTwo);
                         } catch (TermHelper.FormatException ex) {
                             if (i == parts.length) throw new TermHelper().new FormatException();
                             posTermTwo += parts[i];
                             i++;
                             continue;
                         }
                         aTerm = true;
                     }
                     if (i < parts.length) throw new TermHelper().new FormatException();
                     if (parts[1].matches("\\s*") || !newPS.ProperProblemTerm(newPS.sSequent[0]) || !newPS.isIndexed(newPS.sSequent[0]))
                         throw new TermHelper().new FormatException();
                     if (parts[2].matches("\\s*") || !newPS.ProperProblemTerm(newPS.sSequent[1]) || !newPS.isIndexed(newPS.sSequent[1]))
                         throw new TermHelper().new FormatException();
                     if (newPS.sSequent[0].Print().compareTo(newPS.sSequent[1].Print()) != 0) {
                         ArrayList<Term> newProblem = new ArrayList<>();
                         newProblem.add(newPS.sSequent[0]);
                         newProblem.add(newPS.sSequent[1]);
                         newPS.OpenProblems.add(newProblem);
                         ArrayList<Term[]> newProblemHistory = new ArrayList<>();
                         newProblemHistory.add(new Term[]{newPS.sSequent[0], newPS.sSequent[1]});
                         newPS.History.add(newProblemHistory);
                     }
                 } else if (parts[0].compareTo("Variable:") == 0 && !foundProblemOrRule) {
                     StringBuilder name = new StringBuilder();
                     for (int i = 1; i < parts.length; i++)
                         if (parts[i].matches("[a-zA-Z]+")) name.append(parts[i]);
                     if (name.toString().matches("[a-zA-Z]+")
                             && !newPS.Variables.contains(name.toString())
                             && !newPS.Constants.contains(name.toString())
                             && !newPS.Functions.containsKey(name.toString()))
                         newPS.Variables.add(name.toString());
                     else throw new TermHelper().new FormatException();
                 } else throw new IOException();
             }
//**********************************************Parsing Variables from file***************************************************
         } catch (Exception ex) {
             Toast.makeText(ctx, "Syntax error on line " + lineCount + " of " + file + ".", Toast.LENGTH_SHORT).show();
             newPS = new ProblemState();
         }
        newPS.rSequent = new Term[]{Const.HoleSelected, Const.HoleSelected};
        newPS.sSequent = newPS.OpenProblems.get(0).toArray(new Term[]{Const.Hole, Const.Hole});

        return newPS;
    }


     static void OverflowMenuSelected(int id, Activity ctx) {
        if (id == R.id.load) {
            AuxFunctionality.performFileSearch(ctx);
        } else if (id == R.id.save) {
            Toast.makeText(ctx, "Save", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.help) {
            Intent intent = new Intent(ctx, HelpActivity.class);
            ctx.startActivity(intent);
            Toast.makeText(ctx, "Help", Toast.LENGTH_SHORT).show();
	    ctx.finish();
        } else if (id == R.id.about) {
            Intent intent = new Intent(ctx, AboutActivity.class);
            ctx.startActivity(intent);
            Toast.makeText(ctx, "About", Toast.LENGTH_SHORT).show();
            ctx.finish();
        }
    }

     static Term[] StringRuleToTerms(String rule) {
        String[] parts = rule.split(" ⇒ ");
        Term[] terms;
        if (parts.length != 2) return null;
        if (parts[0].contains("∀")) {
            int fp = parts[0].indexOf("(");
            parts[0] = parts[0].substring(fp + 1);
            parts[1] = parts[1].substring(0, parts[1].length() - 1);
        }
        try {
            terms = new Term[]{TermHelper.parse(parts[0]), TermHelper.parse(parts[1])};
        } catch (TermHelper.FormatException e) {
            return null;
        }
        return terms;
    }

     static String RuleTermstoString(ArrayList<Term> rule, ProblemState PS) {
        if(rule != null && rule.size()==2 && rule.get(0)!= null && rule.get(1) != null) {
            StringBuilder prefix = new StringBuilder();
            HashSet<String> vl = PS.VarList(rule.get(0));
            vl.addAll(PS.VarList(rule.get(1)));
            for (String t : vl) prefix.append("∀").append(t);
            return ((prefix.toString().compareTo("") != 0) ? prefix + "( " : "") + rule.get(0).Print() + " ⇒ " + rule.get(1).Print() + ((prefix.toString().compareTo("") != 0) ? " )" : "");
        }
        else return "";
    }

    static Term  ApplySubstitution(Term t,ArrayList<String> sub){

        if(sub.size()!=0){
            HashMap<String,String> parsedselections = new HashMap<>();
            for(int i = 0; i< sub.size();i++){
                String subsub =sub.get(i);
                String prunedSub = subsub.substring(1,subsub.length()-1);
                String[] subSplit = prunedSub.split(" ← ");
                if(!parsedselections.keySet().contains(subSplit[0]))
                    parsedselections.put(subSplit[0],subSplit[1]);
            }
            for(String var: parsedselections.keySet()) {
                String subterm = parsedselections.get(var);
                Term replacement;
                try {
                    replacement =    TermHelper.parse(subterm);
                }catch (TermHelper.FormatException e) { replacement = null; }
                if(replacement!= null){
                    t = t.replace(new Const(var),replacement);
                }
            }
        }
        return t;
    }
}
