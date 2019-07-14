package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

import static com.example.axolotltouch.AuxFunctionality.PASSPROBLEMSTATE;

public class MainActivity extends DisplayUpdateHelper {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_bar_layout);
        findViewById(R.id.OuterLayout).setOnTouchListener(new MainSwipeListener(this));
        findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        PS = ConstructActivity(savedInstanceState);
        if (PS.anteProblem[0].Print().compareTo(PS.succProblem[1].Print()) == 0) {
            boolean passobseve = PS.observe;
            PS = new ProblemState();
            PS.observe = passobseve;
        }
        findViewById(R.id.LeftSideProblem).setOnClickListener(new SideSelectionListener());
        findViewById(R.id.RightSideProblem).setOnClickListener(new SideSelectionListener());
        ActivityDecorate();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable("ProblemState", PS);
    }

    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        PS = in.getParcelable("ProblemState");
    }

    protected void ActivityDecorate() {
        UpdateProblemDisplay();
        RuleDisplayUpdate();
    }

    protected void UpdateProblemDisplay() {
        ((TextView) this.findViewById(R.id.LeftSideProblem)).setText(PS.anteProblem[0].Print());
        ((TextView) this.findViewById(R.id.RightSideProblem)).setText(PS.anteProblem[1].Print());
    }

    private void RuleDisplayUpdate() {
        LinearLayout RLVV = this.findViewById(R.id.RuleListVerticalLayout);
        RLVV.removeAllViewsInLayout();
        for (int i = 0; i < PS.Rules.size(); i++) {
            String Text = AuxFunctionality.RuleTermstoString(PS.Rules.get(i), PS);
            TextView ruleText = new TextView(this);
            ruleText.setTextSize(30);
            ruleText.setText(Text);
            ruleText.setFreezesText(true);
            ruleText.setLayoutParams(new TableRow.LayoutParams(((int) ruleText.getPaint().measureText(ruleText.getText().toString())) + 20, TableRow.LayoutParams.WRAP_CONTENT));
            ruleText.setOnClickListener(new RuleSelectionListener());
            LinearLayout ruleScrollLayout = new LinearLayout(this);
            ruleScrollLayout.setLayoutParams(new FrameLayout.LayoutParams(((int) ruleText.getPaint().measureText(ruleText.getText().toString())) + 20, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY));
            ruleScrollLayout.setOrientation(LinearLayout.VERTICAL);
            ruleScrollLayout.addView(ruleText);
            HorizontalScrollView ruleHScroll = new HorizontalScrollView(this);
            ruleHScroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
            ruleHScroll.addView(ruleScrollLayout);
            RLVV.addView(ruleHScroll);
        }
    }

    protected class MainSwipeListener extends OnSwipeTouchListener {
        MainSwipeListener(Context ctx) {
            super(ctx);
        }

        public boolean onSwipeLeft() {
            if (PS.History.size() != 0)
                try {
                    Pair<Integer, Pair<ArrayList<Pair<String, Term>>, Pair<Term, Term>>> laststep = PS.History.remove(PS.History.size() - 1);
                    Pair<Term, Term> rule = laststep.second.second;
                    Term ruleside = (laststep.first == 0) ? rule.first : rule.second;
                    for (Pair<String, Term> s : laststep.second.first)
                        ruleside = ruleside.replace(new Const(s.first), s.second);
                    PS.anteProblem[laststep.first] = ruleside;
                    PS.selectedSide = -1;
                    PS.subPos = -1;
                    PS.anteCurrentRule = new Term[]{Const.HoleSelected, Const.HoleSelected};
                    PS.Substitutions = new ArrayList<>();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra(PASSPROBLEMSTATE, PS);
                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();
                    Toast.makeText(MainActivity.this, "Rule Application Undone!", Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Toast.makeText(MainActivity.this, "Problems accessing History", Toast.LENGTH_SHORT).show();
                    return true;
                }
            return true;
        }

        public boolean onSwipeRight() {
            ProblemState PS = MainActivity.this.PS;
            Intent intent;
            try {
                if (PS.selectedSide > -1) {
                    if (PS.anteCurrentRule[PS.selectedSide].getSym().compareTo(Const.HoleSelected.getSym()) != 0) {
                        if (TermHelper.TermMatch(PS.anteProblem[PS.selectedSide], PS.anteCurrentRule[PS.selectedSide], PS)) {
                            PS.Substitutions = TermHelper.varTermMatch(PS.anteProblem[PS.selectedSide], PS.anteCurrentRule[PS.selectedSide], PS);
                            HashSet<String> occurences = new HashSet<>();
                            ArrayList<Pair<String, Term>> subCleaned = new ArrayList<>();
                            for (Pair<String, Term> p : PS.Substitutions)
                                if (!occurences.contains(p.first)) {
                                    subCleaned.add(p);
                                    occurences.add(p.first);
                                }
                            PS.Substitutions = subCleaned;
                            HashSet<String> vars = PS.VarList(PS.anteCurrentRule[PS.selectedSide]);
                            for (String s : PS.VarList(PS.anteCurrentRule[(PS.selectedSide == 1) ? 0 : 1]))
                                if (!vars.contains(s))
                                    PS.Substitutions.add(new Pair<>(s, Const.HoleSelected.Dup()));
                            PS.subPos = 0;
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
                        Toast.makeText(MainActivity.this, "Select a Rule", Toast.LENGTH_SHORT).show();

                } else
                    Toast.makeText(MainActivity.this, "Select a Side of the Problem ", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "Problems Substituting", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    private class RuleSelectionListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            LinearLayout rlvv = MainActivity.this.findViewById(R.id.RuleListVerticalLayout);
            for (int i = 0; i < rlvv.getChildCount(); i++) {
                TextView theText = ((TextView) ((LinearLayout) ((HorizontalScrollView) rlvv.getChildAt(i)).getChildAt(0)).getChildAt(0));
                if (theText.getText().toString().compareTo(((TextView) view).getText().toString()) == 0) {
                    MainActivity.this.PS.anteCurrentRule = MainActivity.this.PS.Rules.get(i).first;
                    MainActivity.this.PS.succCurrentRule = MainActivity.this.PS.Rules.get(i).second;
                    textViewSelected(((TextView) view));
                } else textViewUnselected(theText);
            }
        }
    }

    private class SideSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final int left = R.id.LeftSideProblem, right = R.id.RightSideProblem;
            if ((view.getId() == left && MainActivity.this.PS.selectedSide == 0) || (view.getId() == right && MainActivity.this.PS.selectedSide == 1)) {
                textViewUnselected((TextView) view);
                MainActivity.this.PS.selectedSide = -1;
            } else {
                textViewSelected((TextView) view);
                textViewUnselected((TextView) MainActivity.this.findViewById((view.getId() == left) ? right : left));
                MainActivity.this.PS.selectedSide = (view.getId() == left) ? 0 : 1;
            }
        }
    }

}
