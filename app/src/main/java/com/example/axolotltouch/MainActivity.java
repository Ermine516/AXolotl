package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.example.axolotltouch.AxolotlMessagingAndIO.PASSPROBLEMSTATE;

public class MainActivity extends AxolotlSupportingFunctionality {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PS = constructProblemState(savedInstanceState, getIntent());
        if (PS.mainActivityState == -1) setContentView(R.layout.app_main_on_load_bar_layout);
        else if (PS.mainActivityState == 0) {
            setContentView(R.layout.app_main_bar_layout);
            findViewById(R.id.OuterLayout).setOnTouchListener(new MainSwipeListener(this));
            findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

        } else setContentView(R.layout.app_main_on_completion_bar_layout);
        ConstructActivity(savedInstanceState);
        if (PS.mainActivityState == 0) ActivityDecorate();
        if (ProblemState.sideContainsEmptySet(PS.anteProblem) && PS.anteProblem.size() > 1 ||
                ProblemState.sideContainsEmptySet(PS.succProblem) && PS.succProblem.size() > 1)
            PS.problemClean();
        if (PS.anteProblem.size() == 0) PS.anteProblem.add(Const.Empty.Dup());
        if (PS.succProblem.size() == 0) PS.succProblem.add(Const.Empty.Dup());
        if (PS.anteProblem.containsAll(PS.succProblem) && PS.succProblem.containsAll(PS.anteProblem)) {
            boolean passobseve = PS.observe;
            ArrayList<Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Rule>>> ProofHistory = PS.History;
            PS = new ProblemState();
            PS.observe = passobseve;
            PS.History = ProofHistory;
        }
        if (PS.History.size() > 0)
            System.out.println(PS.RuleTermsToString(PS.History.get(PS.History.size() - 1).second.second));
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable("ProblemState", PS);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        PS = in.getParcelable("ProblemState");
    }

    protected void ActivityDecorate() {
        UpdateProblemDisplay();
        RuleDisplayUpdate();
    }

    private void UpdateProblemDisplay() {
        updateProblemSideDisplay((LinearLayout) this.findViewById(R.id.RightSideTermLayout), PS.succProblem.toArray(AxolotlMessagingAndIO.HashSetTermArray));
    }


    protected class MainSwipeListener extends OnSwipeTouchListener {
        MainSwipeListener(Context ctx) {
            super(ctx);
        }

        @SuppressWarnings("ConstantConditions")
        public boolean onSwipeLeft() {
            if (PS.History.size() != 0) {
                try {
                    Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Rule>> laststep = PS.History.remove(PS.History.size() - 1);
                    Rule rule = laststep.second.second;
                    ArrayList<Term> anteSideApply = new ArrayList<>();
                    Term succSideApply = rule.argument.Dup();
                    for (Pair<String, Term> s : laststep.second.first)
                        succSideApply = succSideApply.replace(new Const(s.first), s.second);
                    if (laststep.first.first.size() != 0) {
                        if (rule.Conclusions.size() > 0) {
                            anteSideApply.addAll(rule.Conclusions);
                            for (int i = 0; i < anteSideApply.size(); i++)
                                for (Pair<String, Term> s : laststep.second.first)
                                    anteSideApply.set(i, anteSideApply.get(i).replace(new Const(s.first), s.second));
                        }
                        HashSet<Term> newAnteProblem = new HashSet<>(anteSideApply);
                        for (Term t : PS.anteProblem)
                            if (t.Print().compareTo(succSideApply.Print()) != 0)
                                newAnteProblem.add(t);
                        PS.anteProblem = newAnteProblem;
                    } else {

                        HashSet<Term> newSuccProblem = new HashSet<>();
                        for (Pair<String, Term> s : laststep.second.first)
                            succSideApply = succSideApply.replace(new Const(s.first), s.second);
                        newSuccProblem.add(succSideApply);
                        for (Term t : rule.Conclusions) {
                            Term temp = t.Dup();
                            for (Pair<String, Term> s : laststep.second.first)
                                temp = temp.replace(new Const(s.first), s.second);
                            anteSideApply.add(temp);
                        }
                        for (Term t : PS.succProblem) {
                            boolean wasselected = false;
                            for (Term s : anteSideApply)
                                if (t.Print().compareTo(s.Print()) == 0) wasselected = true;
                            if (!wasselected) newSuccProblem.add(t);
                        }
                        PS.succProblem = newSuccProblem;
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(MainActivity.this, "Problems accessing History", Toast.LENGTH_SHORT).show();
                    return true;
                }
                PS.anteSelectedPositions = new ArrayList<>();
                PS.succSelectedPosition = "";
                PS.subPos = -1;
                PS.currentRule = new Rule();
                PS.Substitutions = new ArrayList<>();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra(PASSPROBLEMSTATE, PS);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
                Toast.makeText(MainActivity.this, "Rule Application Undone!", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(MainActivity.this, "No Rule Application to Undo!", Toast.LENGTH_SHORT).show();

            return true;
        }

        @SuppressWarnings("ConstantConditions")
        public boolean onSwipeRight() {
            ProblemState PS = MainActivity.this.PS;
            Intent intent;
            try {
                if (PS.currentRule.argument.getSym().compareTo(Const.HoleSelected.getSym()) != 0) {
                    if (PS.succSelectedPosition.compareTo("") != 0) {
                        Term succTerm = ProblemState.getTermByString(PS.succSelectedPosition, PS.succProblem);

                        if (TermHelper.wellformedSequents(succTerm) && TermHelper.wellformedSequents(PS.currentRule.argument)) {
                            succTerm.normalize(PS.Variables);
                            PS.currentRule.argument.normalize(PS.Variables);
                        }
                        if (succTerm != null && TermHelper.TermMatchWithVar(succTerm, PS.currentRule.argument, PS.Variables)) {
                            PS.Substitutions = TermHelper.varTermMatch(succTerm, PS.currentRule.argument, PS);
                            HashSet<String> occurences = new HashSet<>();
                            HashMap<String, Term> subCleaned = new HashMap<>();
                            for (Pair<String, Term> p : PS.Substitutions) {
                                if (!occurences.contains(p.first)) {
                                    subCleaned.put(p.first, p.second);
                                    occurences.add(p.first);
                                } else if (p.second.toString().compareTo(subCleaned.get(p.first).toString()) != 0) {
                                    subCleaned = null;
                                    break;
                                }
                            }
                            if (subCleaned != null) {
                                PS.Substitutions = new ArrayList<>();
                                for (String s : subCleaned.keySet())
                                    PS.Substitutions.add(new Pair<>(s, subCleaned.get(s)));

                                HashSet<String> singleSide = new HashSet<>();
                                for (Term t : PS.currentRule.Conclusions) {
                                    HashSet<String> vars = PS.VarList(t);
                                    for (String s : vars)
                                        if (!PS.VarList(PS.currentRule.argument).contains(s))
                                            singleSide.add(s);
                                }
                                for (String s : singleSide)
                                    PS.Substitutions.add(new Pair<>(s, Const.HoleSelected.Dup()));
                                PS.subPos = 0;
                                PS.MatchorConstruct = new HashMap<>();
                                for (Pair<String, Term> p : PS.Substitutions)
                                    PS.MatchorConstruct.put(p.first, p.second.getSym().compareTo(Const.HoleSelected.getSym()) == 0);

                                if (!PS.observe)
                                    while (PS.subPos < PS.Substitutions.size() && !PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected))
                                        PS.subPos++;
                                if (PS.subPos < PS.Substitutions.size() && PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected))
                                    intent = new Intent(MainActivity.this, TermConstructActivity.class);
                                else if (PS.subPos < PS.Substitutions.size())
                                    intent = new Intent(MainActivity.this, MatchDisplayActivity.class);
                                else {
                                    MainActivity.this.swipeRightProblemStateUpdate();
                                    intent = new Intent(MainActivity.this, MainActivity.class);
                                }
                                if (PS.subPos != -1)
                                    Toast.makeText(MainActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).first, Toast.LENGTH_SHORT).show();
                                intent.putExtra(PASSPROBLEMSTATE, PS);
                                MainActivity.this.startActivity(intent);
                                MainActivity.this.finish();
                            } else
                                Toast.makeText(MainActivity.this, "Rule not applicable", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Rule not applicable", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MainActivity.this, "Select a Side of the Problem ", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "Select a Rule", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "Problems Substituting", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

    }




}
