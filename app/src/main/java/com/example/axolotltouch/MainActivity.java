package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
        if (PS.anteProblem.containsAll(PS.succProblem) && PS.succProblem.containsAll(PS.succProblem)) {
            boolean passobseve = PS.observe;
            PS = new ProblemState();
            PS.observe = passobseve;
        }
        findViewById(R.id.LeftSideTerm).setOnClickListener(new SideSelectionListener());
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

    private void UpdateProblemDisplay() {
        updateProblemSideDisplay((LinearLayout) this.findViewById(R.id.LeftSideTermLayout), PS.anteProblem.toArray(new Term[1]));
        updateProblemSideDisplay((LinearLayout) this.findViewById(R.id.RightSideTermLayout), PS.succProblem.toArray(new Term[1]));

    }

    private void updateProblemSideDisplay(LinearLayout sl, Term[] t) {
        for (int i = 0; i < t.length; i++) {
            String Text = t[i].Print();
            TextView anteTermText = new TextView(this);
            anteTermText.setTextSize(40);
            anteTermText.setText(Text);
            anteTermText.setFreezesText(true);
            anteTermText.setTextColor(Color.BLACK);
            anteTermText.setLayoutParams(new TableRow.LayoutParams(((int) anteTermText.getPaint().measureText(anteTermText.getText().toString())) + 20, TableRow.LayoutParams.WRAP_CONTENT));
            LinearLayout problemScrollLayout = new LinearLayout(this);
            problemScrollLayout.setLayoutParams(new FrameLayout.LayoutParams(((int) anteTermText.getPaint().measureText(anteTermText.getText().toString())) + 20, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY));
            problemScrollLayout.setOrientation(LinearLayout.VERTICAL);
            problemScrollLayout.addView(anteTermText);
            HorizontalScrollView ruleHScroll = new HorizontalScrollView(this);
            ruleHScroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
            ruleHScroll.addView(problemScrollLayout);
            sl.addView(ruleHScroll);
        }

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
                    PS.anteProblem.toArray(new Term[1])[laststep.first] = ruleside;
                    PS.anteSelectedPositions = new ArrayList<>();
                    PS.succSelectedPosition = "";
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
                if (PS.succCurrentRule.getSym().compareTo(Const.HoleSelected.getSym()) != 0) {
                    if (PS.succSelectedPosition.compareTo("") != 0) {
                        Term succTerm = getTermByString(PS.succSelectedPosition, PS.succProblem);
                        if (succTerm != null) {
                            PS.Substitutions = TermHelper.varTermMatch(succTerm, PS.succCurrentRule, PS);
                            HashSet<String> occurences = new HashSet<>();
                            ArrayList<Pair<String, Term>> subCleaned = new ArrayList<>();
                            for (Pair<String, Term> p : PS.Substitutions)
                                if (!occurences.contains(p.first)) {
                                    subCleaned.add(p);
                                    occurences.add(p.first);
                                }
                            PS.Substitutions = subCleaned;
                            for (Term t : PS.anteCurrentRule) {
                                HashSet<String> vars = PS.VarList(t);
                                for (String s : PS.VarList(PS.succCurrentRule))
                                    if (!vars.contains(s))
                                        PS.Substitutions.add(new Pair<>(s, Const.HoleSelected.Dup()));
                            }

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
                    } else if (PS.anteSelectedPositions.size() != 0) {
                        ArrayList<Term> anteterm = new ArrayList<>();
                        for (String s : PS.anteSelectedPositions) {
                            Term temp = getTermByString(s, PS.anteProblem);
                            if (temp != null) anteterm.add(temp);
                        }
                        if (anteterm.size() == PS.anteSelectedPositions.size()) {
                            ArrayList<Pair<Term, Term>> matchings = matchAnteProblemRule(anteterm, PS.anteCurrentRule);
                            PS.Substitutions = TermHelper.varTermMatch(succTerm, PS.succCurrentRule, PS);
                            HashSet<String> occurences = new HashSet<>();
                            ArrayList<Pair<String, Term>> subCleaned = new ArrayList<>();
                            for (Pair<String, Term> p : PS.Substitutions)
                                if (!occurences.contains(p.first)) {
                                    subCleaned.add(p);
                                    occurences.add(p.first);
                                }
                            PS.Substitutions = subCleaned;
                            for (Term t : PS.anteCurrentRule) {
                                HashSet<String> vars = PS.VarList(t);
                                for (String s : PS.VarList(PS.succCurrentRule))
                                    if (!vars.contains(s))
                                        PS.Substitutions.add(new Pair<>(s, Const.HoleSelected.Dup()));
                            }

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
                        Toast.makeText(MainActivity.this, "Select a Side of the Problem ", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "Select a Rule", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "Problems Substituting", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        private ArrayList<Pair<Term, Term>> matchAnteProblemRule(ArrayList<Term> problem, Term[] rule) {
            for (Term t : rule) {
                for (Term s : problem) {
                    ArrayList<Pair<String, Term>> varMatching = TermHelper.varTermMatch(s, t, PS);
                }
            }
        }

        private Term getTermByString(String succSelectedPosition, HashSet<Term> succProblem) {
            for (Term t : succProblem)
                if (t.Print().compareTo(succSelectedPosition) == 0) return t.Dup();
            return null;
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
            if (view instanceof TextView && isMemberOf((TextView) view, (LinearLayout) MainActivity.this.findViewById(R.id.RightSideTermLayout))) {
                Cleanslection((LinearLayout) MainActivity.this.findViewById(R.id.LeftSideTermLayout));
                Cleanslection((LinearLayout) MainActivity.this.findViewById(R.id.RightSideTermLayout));
                textViewSelected((TextView) view);
                MainActivity.this.PS.succSelectedPosition = ((TextView) view).getText().toString();
            } else if (view instanceof TextView && isMemberOf((TextView) view, (LinearLayout) MainActivity.this.findViewById(R.id.LeftSideTermLayout))) {
                Cleanslection((LinearLayout) MainActivity.this.findViewById(R.id.RightSideTermLayout));
                if (isNotSelected((TextView) view)) {
                    textViewSelected((TextView) view);
                    MainActivity.this.PS.anteSelectedPositions.add(((TextView) view).getText().toString());
                } else {
                    textViewUnselected((TextView) view);
                    MainActivity.this.PS.anteSelectedPositions.remove(((TextView) view).getText().toString());
                }
            }
        }

        private void Cleanslection(LinearLayout side) {
            int size = side.getChildCount();
            for (int i = 0; i < size; i++)
                textViewUnselected(((TextView) ((LinearLayout) ((HorizontalScrollView) side.getChildAt(i)).getChildAt(0)).getChildAt(0)));
            MainActivity.this.PS.succSelectedPosition = "";
            MainActivity.this.PS.anteSelectedPositions = new ArrayList<>();
        }

        private boolean isMemberOf(TextView view, LinearLayout side) {
            int size = side.getChildCount();
            for (int i = 0; i < size; i++) {
                String value = ((TextView) ((LinearLayout) ((HorizontalScrollView) side.getChildAt(i)).getChildAt(0)).getChildAt(0)).getText().toString();
                if (value.compareTo(view.getText().toString()) == 0) return true;
            }
            return false;

        }
    }

}
