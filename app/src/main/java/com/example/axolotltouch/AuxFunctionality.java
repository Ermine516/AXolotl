package com.example.axolotltouch;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.widget.Toast;

import androidx.core.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

class AuxFunctionality {
    static final Term[] HashSetTermArray = new Term[]{Const.Hole};
    static final String PASSPROBLEMSTATE = "com.example.android.AXolotlTouch.extra.problemstate";
    static final int READ_REQUEST_CODE = 42;
    private static final String nameParseRegex = "[a-zA-Z&\\u2194\\u25E6\\u2227\\u2228\\u00AC\\u21D2\\u21D4\\u2284\\u2285\\u22A4\\u25A1\\u25C7\\u22A2\\u03B5]+";

     static void SideMenuItems(int id, Activity ctx, ProblemState PS) {
         AssetManager manager = ctx.getAssets();
         Intent intent = null;
        if (id == R.id.Problembutton) {
            Toast.makeText(ctx, "Problem", Toast.LENGTH_SHORT).show();
            intent = new Intent(ctx, MainActivity.class);
        } else if (id == R.id.PropositionalProblems) {
            intent = new Intent(ctx, PropositionalProblemsListActivity.class);
        } else if (id == R.id.TermMatchingProblems) {
            intent = new Intent(ctx, TermMatchingProblemsListActivity.class);
        } else if (id == R.id.nonclassical) {
            intent = new Intent(ctx, NonClassicalProblemsListActivity.class);
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
                 else if (parts[0].compareTo("Problem:") == 0 && newPS.anteProblem.size() == 1 && newPS.anteProblem.toArray(HashSetTermArray)[0].getSym().compareTo(Const.Hole.getSym()) == 0)
                     foundProblemOrRule = parseProblemDefinition(newPS, parts);
                 else if (parts[0].compareTo("Variable:") == 0 && !foundProblemOrRule)
                     parsevariableSymbol(newPS, parts);
                 else throw new IOException();
             }
         } catch (Exception ex) {
             Toast.makeText(ctx, "Syntax error on line " + lineCount + " of " + file + ".", Toast.LENGTH_SHORT).show();
             newPS = new ProblemState();
             newPS.observe = ((DisplayUpdateHelper) ctx).PS.observe;
         }
        if (!newPS.SequentProblem()) {
            ArrayList<Pair<String, Pair<Integer, Boolean>>> cleanedFunctions = new ArrayList<>();
            for (Pair<String, Pair<Integer, Boolean>> p : newPS.Functions)
                if (p.first.compareTo("cons") != 0)
                    if (p.first.compareTo("⊢") != 0)
                        cleanedFunctions.add(p);
            ArrayList<String> cleanedConstants = new ArrayList<>();
            for (String s : newPS.Constants)
                if (s.compareTo("ε") != 0)
                    cleanedConstants.add(s);
            newPS.Functions = cleanedFunctions;
            newPS.Constants = cleanedConstants;
        }
         newPS.anteCurrentRule = new ArrayList<>();
         newPS.anteCurrentRule.add(Const.HoleSelected);
         return newPS;
     }

    private static void parsevariableSymbol(ProblemState newPS, String[] parts) throws TermHelper.FormatException {
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < parts.length; i++)
            if (parts[i].matches("[a-zA-Z]+")) name.append(parts[i]);
        if (name.toString().matches("[a-zA-Z]+")
                && !newPS.Variables.contains(name.toString())
                && !newPS.Constants.contains(name.toString())
                && newPS.containsFunctionSymbol(name.toString()))
            newPS.Variables.add(name.toString());
        else throw new TermHelper().new FormatException();
    }

    private static boolean parseProblemDefinition(ProblemState newPS, String[] parts) throws TermHelper.FormatException {
        if (parts.length < 3) throw new TermHelper().new FormatException();
        int anteSize = Integer.valueOf(parts[1]);
        int succSize = Integer.valueOf(parts[2]);
        if (parts.length != anteSize + succSize + 3) throw new TermHelper().new FormatException();
        newPS.anteProblem = new HashSet<>();
        newPS.succProblem = new HashSet<>();
        for (int i = 3; i < anteSize + 3; i++) {
            Term temp = TermHelper.parse(parts[i], newPS);
            if (!newPS.isIndexed(temp) && !TermHelper.containsNestedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (temp.Print().contains("⊢") && !TermHelper.wellformedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (!temp.Print().contains("⊢") && !TermHelper.freeOfCons(temp))
                throw new TermHelper().new FormatException();
            else newPS.anteProblem.add(temp);

        }
        if (newPS.anteProblem.size() == 0) newPS.anteProblem.add(Const.Empty.Dup());
        for (int i = anteSize + 3; i < parts.length; i++) {
            Term temp = TermHelper.parse(parts[i], newPS);
            if (!newPS.isIndexed(temp) && !TermHelper.containsNestedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (temp.Print().contains("⊢") && !TermHelper.wellformedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (!temp.Print().contains("⊢") && !TermHelper.freeOfCons(temp))
                throw new TermHelper().new FormatException();
            else newPS.succProblem.add(temp);
        }
        return true;
    }

    private static boolean parseRuleDefinition(ProblemState newPS, String[] parts) throws TermHelper.FormatException {
        String ruleRegex = "\\[(.)*\\]";
        if (parts.length < 2) throw new TermHelper().new FormatException();
        int anteSize = Integer.valueOf(parts[1]);
        int partsAjustedSize = parts.length;
        String ruleLabel = "";
        if (parts.length != anteSize + 3) {
            if (parts.length != anteSize + 4) throw new TermHelper().new FormatException();
            partsAjustedSize--;
            if (parts[partsAjustedSize].matches(ruleRegex)) ruleLabel = parts[partsAjustedSize];
            else throw new TermHelper().new FormatException();
        }
        ArrayList<Term> anteRule = new ArrayList<>();
        Term succRule = Const.HoleSelected;
        for (int i = 2; i < partsAjustedSize; i++) {
            succRule = TermHelper.parse(parts[i], newPS);
            if (!newPS.isIndexed(succRule) && !TermHelper.containsNestedSequents(succRule))
                throw new TermHelper().new FormatException();
            else if (succRule.Print().contains("⊢") && !TermHelper.wellformedSequents(succRule))
                throw new TermHelper().new FormatException();
            else if (!succRule.Print().contains("⊢") && !TermHelper.freeOfCons(succRule))
                throw new TermHelper().new FormatException();
            else if (i != partsAjustedSize - 1) anteRule.add(succRule);
        }
        newPS.Rules.add(new Pair<>(ruleLabel, new Pair<>(anteRule, succRule)));
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
        if (name.toString().compareTo("cons") == 0 ||
                name.toString().compareTo("⊢") == 0 ||
                name.toString().compareTo("ε") == 0) throw new TermHelper().new FormatException();
        if (name.toString().matches(nameParseRegex)
                && arity.toString().matches("[0-9]+")
                && !PS.Variables.contains(name.toString())
                && !PS.Constants.contains(name.toString())
                && PS.containsFunctionSymbol(name.toString())) {
            if (Integer.parseInt(arity.toString()) == 0) PS.Constants.add(name.toString());
            else
                PS.Functions.add(new Pair<>(name.toString(), new Pair<>(Integer.parseInt(arity.toString()), infix)));
        } else throw new TermHelper().new FormatException();
    }


}
