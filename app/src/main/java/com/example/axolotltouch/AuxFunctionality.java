package com.example.axolotltouch;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

class AuxFunctionality {
      static final String PASSPROBLEMSTATE = "com.example.android.AXolotlTouch.extra.problemstate";
    static final int READ_REQUEST_CODE = 42;
    private static final String nameParseRegex = "[a-zA-Z&\\u2227\\u2228\\u00AC\\u21D2\\u21D4\\u2284\\u2285\\u22A4\\u25A1\\u25C7]+";

     static void SideMenuItems(int id, Activity ctx, ProblemState PS) {
        Intent intent = null;
        if (id == R.id.Problembutton) {
            Toast.makeText(ctx, "Problem", Toast.LENGTH_SHORT).show();
            intent = new Intent(ctx, MainActivity.class);
        } else if (id == R.id.propositional01) {
            ProblemState newPS = loadFile(ctx.getResources().openRawResource(R.raw.prop1), "prop1.txt", ctx);
            intent = new Intent(ctx, MainActivity.class);
            PS = newPS;
        } else if (id == R.id.propositional02) {
            ProblemState newPS = loadFile(ctx.getResources().openRawResource(R.raw.prop2), "prop2.txt", ctx);
            intent = new Intent(ctx, MainActivity.class);
            PS = newPS;
        } else if (id == R.id.propositional03) {
            ProblemState newPS = loadFile(ctx.getResources().openRawResource(R.raw.prop3), "prop3.txt", ctx);
            intent = new Intent(ctx, MainActivity.class);
            PS = newPS;
        } else if (id == R.id.modalProblem01) {
            ProblemState newPS = loadFile(ctx.getResources().openRawResource(R.raw.modal1), "modal1.txt", ctx);
            intent = new Intent(ctx, MainActivity.class);
            PS = newPS;
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

     private static void performFileSearch(Activity ctx) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        ctx.startActivityForResult(intent, READ_REQUEST_CODE);
    }

     static ProblemState loadFile(InputStream IS, String file, Activity ctx) {
         ProblemState newPS = new ProblemState();
         newPS.observe = ((DisplayUpdateHelper) ctx).PS.observe;
         String line;
         int lineCount = 0;
         boolean foundProblemOrRule = false;
         try (BufferedReader br = new BufferedReader(new InputStreamReader(IS))) {
             while ((line = br.readLine()) != null) {
                 if (line.matches("\\s+")) continue;
                 lineCount++;
                 String[] parts = line.split("\\s+");
                 if (parts[0].compareTo("Function:") == 0 && !foundProblemOrRule)
                     parseFunctionSymbol(newPS, parts);
                 else if (parts[0].compareTo("Rule:") == 0)
                     foundProblemOrRule = parseRuleDefinition(newPS, parts);
                 else if (parts[0].compareTo("Problem:") == 0 && newPS.anteProblem[0].getSym().compareTo(Const.Hole.getSym()) == 0)
                     foundProblemOrRule = parseProblemDefinition(newPS, parts);
                 else if (parts[0].compareTo("Variable:") == 0 && !foundProblemOrRule)
                     parsevariableSymbol(newPS, parts);
                 else throw new IOException();
             }
//**********************************************Parsing Variables from file***************************************************
         } catch (Exception ex) {
             Toast.makeText(ctx, "Syntax error on line " + lineCount + " of " + file + ".", Toast.LENGTH_SHORT).show();
             newPS = new ProblemState();
             newPS.observe = ((DisplayUpdateHelper) ctx).PS.observe;
         }
         newPS.anteCurrentRule = new Term[]{Const.HoleSelected, Const.HoleSelected};
         return newPS;
     }

    private static void parsevariableSymbol(ProblemState newPS, String[] parts) throws TermHelper.FormatException {
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < parts.length; i++)
            if (parts[i].matches("[a-zA-Z]+")) name.append(parts[i]);
        if (name.toString().matches("[a-zA-Z]+")
                && !newPS.Variables.contains(name.toString())
                && !newPS.Constants.contains(name.toString())
                && !newPS.containsFunctionsymbol(name.toString()))
            newPS.Variables.add(name.toString());
        else throw new TermHelper().new FormatException();
    }

    private static boolean parseProblemDefinition(ProblemState newPS, String[] parts) throws TermHelper.FormatException {
        if (parts.length < 3) throw new TermHelper().new FormatException();
        int anteSize = new Integer(parts[1]);
        int succSize = new Integer(parts[1]);
        if (parts.length != anteSize + succSize + 3) throw new TermHelper().new FormatException();
        newPS.anteProblem = new Term[anteSize];
        newPS.succProblem = new Term[succSize];
        for (int i = 3; i < anteSize + 3; i++) {
            newPS.anteProblem[i - 3] = TermHelper.parse(parts[i], newPS);
            if (!newPS.isIndexed(newPS.anteProblem[i - 3]))
                throw new TermHelper().new FormatException();
        }
        for (int i = anteSize + 3; i < parts.length; i++) {
            newPS.succProblem[i - (anteSize + 3)] = TermHelper.parse(parts[i], newPS);
            if (!newPS.isIndexed(newPS.succProblem[i - (anteSize + 3)]))
                throw new TermHelper().new FormatException();
        }
        return true;
    }

    private static boolean parseRuleDefinition(ProblemState newPS, String[] parts) throws TermHelper.FormatException {
        if (parts.length < 2) throw new TermHelper().new FormatException();
        int anteSize = new Integer(parts[1]);
        if (parts.length != anteSize + 3) throw new TermHelper().new FormatException();
        Term[] anteRule = new Term[anteSize];
        Term succRule = Const.HoleSelected;
        for (int i = 2; i < parts.length; i++) {
            succRule = TermHelper.parse(parts[i], newPS);
            if (!newPS.isIndexed(succRule)) throw new TermHelper().new FormatException();
            else if (i != parts.length - 1) anteRule[i - 2] = TermHelper.parse(parts[i], newPS);
        }
        newPS.Rules.add(new Pair<>(anteRule, succRule));
        return true;
    }

    private static void parseFunctionSymbol(ProblemState PS, String[] parts) throws TermHelper.FormatException {
        StringBuilder name = new StringBuilder();
        StringBuilder arity = new StringBuilder();
        boolean infix = false;
        for (int i = 1; i < parts.length; i++)
            if (parts[i].matches(nameParseRegex) && !parts[i].matches("infix"))
                name.append(parts[i]);
        for (int i = 2; i < parts.length; i++)
            if (parts[i].matches("[0-9]+")) arity.append(parts[i]);
        for (int i = 2; i < parts.length; i++)
            if (parts[i].matches("infix") && Integer.decode(arity.toString()) == 2) infix = true;
            else if (parts[i].matches("infix") && Integer.decode(arity.toString()) != 2)
                throw new TermHelper().new FormatException();
        if (name.toString().matches(nameParseRegex)
                && arity.toString().matches("[0-9]+")
                && !PS.Variables.contains(name.toString())
                && !PS.Constants.contains(name.toString())
                && !PS.containsFunctionsymbol(name.toString())) {
            if (Integer.parseInt(arity.toString()) == 0) PS.Constants.add(name.toString());
            else
                PS.Functions.add(new Pair<>(name.toString(), new Pair<>(Integer.parseInt(arity.toString()), infix)));
        } else throw new TermHelper().new FormatException();
    }


    static String RuleTermstoString(Pair<Term[], Term> rule, ProblemState PS) {
        if (rule != null && rule.first != null && rule.second != null) {
            StringBuilder prefix = new StringBuilder();
            HashSet<String> vl = new HashSet<>();
            for (Term t : rule.first) vl.addAll(PS.VarList(t));
            vl.addAll(PS.VarList(rule.second));
            for (String t : vl) prefix.append("∀").append(t);
            String retString = (prefix.toString().compareTo("") != 0) ? prefix + "(Δ " : "Δ ";
            if (rule.first.length > 0)
                for (int i = 0; i < rule.first.length; i++)
                    if (i == 0 && i != rule.first.length - 1)
                        retString += ", " + rule.first[i].Print() + " , ";
                    else if (0 == rule.first.length - 1)
                        retString += ", " + rule.first[i].Print() + " ⊢ Δ , ";
                    else if (i == rule.first.length - 1)
                        retString += rule.first[i].Print() + " ⊢ Δ , ";
                    else retString += rule.first[i].Print() + " , ";
            else retString += "⊢ Δ , ";
            return retString + rule.second.Print() + ((prefix.toString().compareTo("") != 0) ? " )" : "");
        }
        else return "";
    }

}
