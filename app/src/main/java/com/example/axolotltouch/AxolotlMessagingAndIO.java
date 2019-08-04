package com.example.axolotltouch;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class deals with all messaging and IO related functionality of Axolotl
 *
 * @author David M. Cerna
 */
class AxolotlMessagingAndIO {
    /**
     * An array which may be used for list-array translation.
     */
    static final Term[] HashSetTermArray = new Term[]{Const.Hole};
    /**
     * Code for passing the problem state within an intent
     */
    static final String PASSPROBLEMSTATE = "com.example.android.AXolotlTouch.extra.problemstate";
    /**
     * Code for passing the correct problem list within an intent
     */
    static final String PASSPROBLEMLIST = "com.example.android.AXolotlTouch.extra.ProblemList";
    /**
     * Code required for the open file activity
     */
    static final int READ_REQUEST_CODE = 42;
    /**
     * a list of allowed symbols for AXolotl files. May be extended
     */
    private static final String nameParseRegex = "[a-zA-Z&\\u2194\\u25E6\\u2227\\u2228\\u00AC\\u21D2\\u21D4\\u2284\\u2285\\u22A4\\u22A5\\u25A1\\u25C7\\u22A2\\u03B5]+";

    /**
     * @author David M. Cerna
     * @param id The id of the gui element of the overflow menu which was selected
     * @param ctx The activity from which  the request was made
     */
    static void OverflowMenuSelected(int id, Activity ctx) {
        if (id == R.id.load) {
            AxolotlMessagingAndIO.performFileSearch(ctx);
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

    /**
     * Starts a new file search activity.
     * @author David M. Cerna
     * @param ctx The activity which made the request
     */
    private static void performFileSearch(Activity ctx) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        ctx.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * Reads problems from AXolotl files
     * @author David M. Cerna
     * @param IS The stream reading from the selected file.
     * @param file The name of the file which IS is reading from.
     * @param ctx The activity which requested the file to be read.
     * @return A ProblemState containing the Information stored in the file.
     */
    static ProblemState loadFile(InputStream IS, String file, Activity ctx) {
        ProblemState newPS = new ProblemState();
        newPS.observe = ((AxolotlSupportingFunctionality) ctx).PS.observe;
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
            newPS.observe = ((AxolotlSupportingFunctionality) ctx).PS.observe;
        }
        newPS.currentRule = new Rule();
        return newPS;
    }

    /**
     * This method parses variable symbols from AXolotl files
     *
     * @param PS    A partially constructed problem state
     * @param parts A sequence of strings resulting from a line of an AXolotl file
     * @throws TermHelper.FormatException When the input does not fit the constraints of the AXolotl file format this exception is thrown
     * @author David M. Cerna
     */
    private static void parsevariableSymbol(ProblemState PS, String[] parts) throws TermHelper.FormatException {
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < parts.length; i++)
            if (parts[i].matches("[a-zA-Z]+")) name.append(parts[i]);
        if (name.toString().matches("[a-zA-Z]+")
                && !PS.Variables.contains(name.toString())
                && !PS.Constants.contains(name.toString())
                && PS.containsFunctionSymbol(name.toString()))
            PS.Variables.add(name.toString());
        else throw new TermHelper().new FormatException();
    }

    /**
     * This method parses problem statements from AXolotl files
     *
     * @param PS    A partially constructed problem state
     * @param parts A sequence of strings resulting from a line of an AXolotl file
     * @throws TermHelper.FormatException When the input does not fit the constraints of the AXolotl file format this exception is thrown
     * @author David M. Cerna
     */
    private static boolean parseProblemDefinition(ProblemState PS, String[] parts) throws TermHelper.FormatException {
        if (parts.length < 3) throw new TermHelper().new FormatException();
        int anteSize = Integer.valueOf(parts[1]);
        int succSize = Integer.valueOf(parts[2]);
        if (parts.length != anteSize + succSize + 3) throw new TermHelper().new FormatException();
        PS.anteProblem = new HashSet<>();
        PS.succProblem = new HashSet<>();
        for (int i = 3; i < anteSize + 3; i++) {
            Term temp = TermHelper.parse(parts[i], PS);
            if (!PS.isIndexed(temp) && !TermHelper.containsNestedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (temp.Print().contains("⊢") && !TermHelper.wellformedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (!temp.Print().contains("⊢") && !TermHelper.freeOfCons(temp))
                throw new TermHelper().new FormatException();
            else PS.anteProblem.add(temp);

        }
        if (PS.anteProblem.size() == 0) PS.anteProblem.add(Const.Empty.Dup());
        for (int i = anteSize + 3; i < parts.length; i++) {
            Term temp = TermHelper.parse(parts[i], PS);
            if (!PS.isIndexed(temp) && !TermHelper.containsNestedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (temp.Print().contains("⊢") && !TermHelper.wellformedSequents(temp))
                throw new TermHelper().new FormatException();
            else if (!temp.Print().contains("⊢") && !TermHelper.freeOfCons(temp))
                throw new TermHelper().new FormatException();
            else PS.succProblem.add(temp);
        }
        return true;
    }

    /**
     * This method parses rule statements from AXolotl files
     *
     * @param PS    A partially constructed problem state
     * @param parts A sequence of strings resulting from a line of an AXolotl file
     * @throws TermHelper.FormatException When the input does not fit the constraints of the AXolotl file format this exception is thrown
     * @author David M. Cerna
     */
    private static boolean parseRuleDefinition(ProblemState PS, String[] parts) throws TermHelper.FormatException {
        String ruleRegex = "\\[(.)*]";
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
            succRule = TermHelper.parse(parts[i], PS);
            if (!PS.isIndexed(succRule) && !TermHelper.containsNestedSequents(succRule))
                throw new TermHelper().new FormatException();
            else if (succRule.Print().contains("⊢") && !TermHelper.wellformedSequents(succRule))
                throw new TermHelper().new FormatException();
            else if (!succRule.Print().contains("⊢") && !TermHelper.freeOfCons(succRule))
                throw new TermHelper().new FormatException();
            else if (i != partsAjustedSize - 1) anteRule.add(succRule);
        }
        HashSet<String> ruleVar = new HashSet<>(PS.Variables);
        PS.Rules.add(new Rule(ruleLabel, anteRule, succRule, ruleVar));
        return true;
    }

    /** This method parses function symbols from AXolotl files
     * @author David M. Cerna
     * @param PS A partially constructed problem state
     * @param parts A sequence of strings resulting from a line of an AXolotl file
     * @throws TermHelper.FormatException When the input does not fit the constraints of the AXolotl file format this exception is thrown
     */
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
                PS.Functions.add(new FunctionDefinition(name.toString(), Integer.parseInt(arity.toString()), infix));
        } else throw new TermHelper().new FormatException();
    }


}
