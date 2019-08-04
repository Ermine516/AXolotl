package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
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
            ArrayList<State> ProofHistory = PS.History;
            PS = new ProblemState();
            PS.observe = passobseve;
            PS.History = ProofHistory;
        }
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

        public boolean onSwipeLeft() {
            if (PS.History.size() != 0) {
                try {
                    State laststep = PS.History.remove(PS.History.size() - 1);
                    HashSet<Term> anteSideApply;
                    Term succSideApply = laststep.substitution.apply(laststep.rule.argument.Dup());
                    HashSet<Term> newSuccProblem = new HashSet<>();
                    succSideApply = laststep.substitution.apply(succSideApply);
                    newSuccProblem.add(succSideApply);
                    anteSideApply = laststep.substitution.apply(laststep.rule.Conclusions);
                    for (Term t : PS.succProblem) {
                        boolean wasselected = false;
                        for (Term s : anteSideApply)
                            if (t.Print().compareTo(s.Print()) == 0) wasselected = true;
                        if (!wasselected) newSuccProblem.add(t);
                    }
                    PS.succProblem = newSuccProblem;

                } catch (NullPointerException e) {
                    Toast.makeText(MainActivity.this, "Problems accessing History", Toast.LENGTH_SHORT).show();
                    return true;
                }
                PS.anteSelectedPositions = new ArrayList<>();
                PS.succSelectedPosition = "";
                PS.subPos = -1;
                PS.currentRule = new Rule();
                PS.Substitutions = new Substitution();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra(PASSPROBLEMSTATE, PS);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
                Toast.makeText(MainActivity.this, "Rule Application Undone!", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(MainActivity.this, "No Rule Application to Undo!", Toast.LENGTH_SHORT).show();

            return true;
        }

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
                            PS.Substitutions = Substitution.substitutionConstruct(succTerm, PS.currentRule.argument, PS);
                            try {
                                PS.Substitutions = PS.Substitutions.clean();
                                HashSet<String> singleSide = new HashSet<>();
                                for (Term t : PS.currentRule.Conclusions) {
                                    HashSet<String> vars = PS.VarList(t);
                                    for (String s : vars)
                                        if (!PS.VarList(PS.currentRule.argument).contains(s))
                                            singleSide.add(s);
                                }
                                for (String s : singleSide)
                                    PS.Substitutions.varIsPartial(s);
                                PS.subPos = 0;
                                PS.MatchorConstruct = PS.Substitutions.partialOrNot();
                                if (!PS.observe)
                                    while (PS.Substitutions.isPosition(PS.subPos) && !PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                                        PS.subPos++;
                                if (PS.Substitutions.isPosition(PS.subPos) && PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                                    intent = new Intent(MainActivity.this, TermConstructActivity.class);
                                else if (PS.Substitutions.isPosition(PS.subPos))
                                    intent = new Intent(MainActivity.this, MatchDisplayActivity.class);
                                else {
                                    MainActivity.this.swipeRightProblemStateUpdate();
                                    intent = new Intent(MainActivity.this, MainActivity.class);
                                }
                                if (PS.subPos != -1)
                                    Toast.makeText(MainActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).variable, Toast.LENGTH_SHORT).show();
                                intent.putExtra(PASSPROBLEMSTATE, PS);
                                MainActivity.this.startActivity(intent);
                                MainActivity.this.finish();
                            } catch (Substitution.NotASubtitutionException e) {
                                Toast.makeText(MainActivity.this, "Rule not applicable", Toast.LENGTH_SHORT).show();
                            }
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
