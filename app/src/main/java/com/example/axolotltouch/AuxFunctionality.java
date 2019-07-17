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
    private static final String nameParseRegex = "[a-zA-Z&\\u2227\\u2228\\u00AC\\u21D2\\u21D4\\u2284\\u2285\\u22A4\\u25A1\\u25C7\\u25E6]+";

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
        } else if (id == R.id.misc01) {
            ProblemState newPS = loadFile(ctx.getResources().openRawResource(R.raw.misc1), "misc1.txt", ctx);
            intent = new Intent(ctx, MainActivity.class);
            PS = newPS;
        } else if (id == R.id.misc02) {
            ProblemState newPS = loadFile(ctx.getResources().openRawResource(R.raw.misc2), "misc2.txt", ctx);
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
                 else if (parts[0].compareTo("Problem:") == 0 && newPS.ssequent[0].getSym().compareTo(Const.Hole.getSym()) == 0)
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
         newPS.rsequent = new Term[]{Const.HoleSelected, Const.HoleSelected};
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
        if (parts.length != 3) throw new TermHelper().new FormatException();
        newPS.ssequent[0] = TermHelper.parse(parts[1], newPS);
        newPS.ssequent[1] = TermHelper.parse(parts[2], newPS);
        if (!newPS.isIndexed(newPS.ssequent[0]) || !newPS.isIndexed(newPS.ssequent[1]))
            throw new TermHelper().new FormatException();
        return true;
    }

    private static boolean parseRuleDefinition(ProblemState newPS, String[] parts) throws TermHelper.FormatException {
        if (parts.length != 3) throw new TermHelper().new FormatException();
        newPS.rsequent[0] = TermHelper.parse(parts[1], newPS);
        newPS.rsequent[1] = TermHelper.parse(parts[2], newPS);
        if (!newPS.isIndexed(newPS.rsequent[0]) || !newPS.isIndexed(newPS.rsequent[1]))
            throw new TermHelper().new FormatException();
        newPS.Rules.add(new Pair<>(newPS.rsequent[0], newPS.rsequent[1]));
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


    static String RuleTermstoString(Pair<Term, Term> rule, ProblemState PS) {
        if (rule != null && rule.first != null && rule.second != null) {
            StringBuilder prefix = new StringBuilder();
            HashSet<String> vl = new HashSet<>();
            vl.addAll(PS.VarList(rule.first));
            vl.addAll(PS.VarList(rule.second));
            for (String t : vl) prefix.append("∀").append(t);
            String retString = (prefix.toString().compareTo("") != 0) ? prefix + "(" : "";
            retString += "" + rule.first.Print() + " ⊢  ";

            return retString + rule.second.Print() + ((prefix.toString().compareTo("") != 0) ? " )" : "");
        }
        else return "";
    }

}
