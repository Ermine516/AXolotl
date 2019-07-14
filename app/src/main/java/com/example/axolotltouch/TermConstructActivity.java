package com.example.axolotltouch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Pair;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.example.axolotltouch.AuxFunctionality.PASSPROBLEMSTATE;


public class TermConstructActivity extends DisplayUpdateHelper  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_term_bar_layout);
        FloatingActionButton fab = findViewById(R.id.UndoButtonTerm);
        findViewById(R.id.OuterLayout).setOnTouchListener(new TermConstructActivity.SwipeListener(this));
        findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        fab.setOnTouchListener(new OnTouchHapticListener());
        fab.setOnClickListener(new UndoSubstitutionListener());
        PS = ConstructActivity(savedInstanceState);
        if (!PS.SubHistory.keySet().contains(PS.Substitutions.get(PS.subPos).first))
            PS.SubHistory.put(PS.Substitutions.get(PS.subPos).first, new ArrayList<Term>());
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        FurtureProblemDisplay();
        UpdateTermDisplay();
        TermDisplayUpdate();
    }

    private void FurtureProblemDisplay() {
        TextView lhs = this.findViewById(R.id.LeftSideProblem);
        TextView rhs = this.findViewById(R.id.RightSideProblem);
        if (PS.selectedSide == 1) {
            lhs.setText(PS.ssequent[0].Print());
            Term temp = PS.rsequent[0].Dup();
            for (Pair<String, Term> s : PS.Substitutions)
                temp = temp.replace(new Const(s.first), s.second);
            rhs.setText(Html.fromHtml(temp.Print(PS.Substitutions.get(PS.subPos).second)));
        }

    }

    private void UpdateTermDisplay() {
        TextView td = this.findViewById(R.id.TermDisplay);
        LinearLayout ltd = this.findViewById(R.id.TermInstancceLayout);
        td.setText(PS.Substitutions.get(PS.subPos).second.Print());
        int width = ((int) td.getPaint().measureText(td.getText().toString())) + 20;
        td.setWidth((width > 75) ? width : 75);
        ltd.setMinimumWidth((width > 75) ? width : 75);
    }

    private void TermDisplayUpdate() {
        LinearLayout RLVV = this.findViewById(R.id.TermSelectionLayout);
        RLVV.removeAllViewsInLayout();
        for (Pair<String, Pair<Integer, Boolean>> p : PS.Functions) {
            int arity = p.second.first;
            boolean infix = p.second.second;
            ArrayList<Term> args = new ArrayList<>();
            while (arity > 0) {
                args.add(new Const(Const.Hole.getSym()));
                arity--;
            }
            String funcText = new Func(p.first, args, infix).Print();
            TextView functext = new TextView(this);
            functext.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
            functext.setTextSize(32);
            functext.setText(funcText);
            functext.setPadding(40, 0, 40, 0);
            functext.setOnClickListener(new SymbolSelectionListener());
            functext.setOnTouchListener(new OnTouchHapticListener());
            RLVV.addView(functext);
        }
        for (String cons : PS.Constants) {
            String funcText = new Const(cons).Print();
            TextView functext = new TextView(this);
            functext.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
            functext.setTextSize(32);
            functext.setText(funcText);
            functext.setPadding(40, 0, 40, 0);
            functext.setOnClickListener(new SymbolSelectionListener());
            functext.setOnTouchListener(new OnTouchHapticListener());

            RLVV.addView(functext);
        }
    }

    protected class SwipeListener extends OnSwipeTouchListener {
        SwipeListener(Context ctx) {
            super(ctx);
        }

        public boolean onSwipeLeft() {
            PS.subPos--;
            Intent intent;
            try {
                if (TermConstructActivity.this.PS.subPos == -1 || !PS.observe) {
                    TermConstructActivity.this.PS.subPos = -1;
                    PS.selectedSide = -1;
                    PS.Substitutions = new ArrayList<>();
                    intent = new Intent(TermConstructActivity.this, MainActivity.class);
                    Toast.makeText(TermConstructActivity.this, "Select Rule and Problem Side", Toast.LENGTH_SHORT).show();
                } else {
                    if (PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected))
                        intent = new Intent(TermConstructActivity.this, TermConstructActivity.class);
                    else
                        intent = new Intent(TermConstructActivity.this, MatchDisplayActivity.class);
                    Toast.makeText(TermConstructActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).first, Toast.LENGTH_SHORT).show();
                }
                intent.putExtra(PASSPROBLEMSTATE, PS);
                TermConstructActivity.this.startActivity(intent);
                TermConstructActivity.this.finish();
            } catch (NullPointerException e) {
                Toast.makeText(TermConstructActivity.this, "Problems accessing Previous State", Toast.LENGTH_SHORT).show();

            }
            return true;
        }

        public boolean onSwipeRight() {
            ProblemState PS = TermConstructActivity.this.PS;
            Intent intent;
            try {
                if (!PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected)) {
                    TermConstructActivity.this.PS.subPos++;
                    if (!PS.observe)
                        while (PS.subPos < PS.Substitutions.size() && !PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected))
                            PS.subPos++;
                    if (TermConstructActivity.this.PS.subPos < TermConstructActivity.this.PS.Substitutions.size()) {
                        if (PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected))
                            intent = new Intent(TermConstructActivity.this, TermConstructActivity.class);
                        else
                            intent = new Intent(TermConstructActivity.this, MatchDisplayActivity.class);
                        Toast.makeText(TermConstructActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).first, Toast.LENGTH_SHORT).show();
                    } else {
                        TermConstructActivity.this.swipeRightProblemStateUpdate();
                        intent = new Intent(TermConstructActivity.this, MainActivity.class);
                    }
                    intent.putExtra(PASSPROBLEMSTATE, PS);
                    TermConstructActivity.this.startActivity(intent);
                    TermConstructActivity.this.finish();
                } else
                    Toast.makeText(TermConstructActivity.this, "Incomplete Substitution", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Toast.makeText(TermConstructActivity.this, "Problems Substituting", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    private class SymbolSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState pschange = TermConstructActivity.this.PS;
            LinearLayout tl = TermConstructActivity.this.findViewById(R.id.TermSelectionLayout);
            int position = -1;
            for (int i = 0; i < tl.getChildCount(); i++)
                if (((TextView) tl.getChildAt(i)).getText().toString().compareTo(((TextView) view).getText().toString()) == 0)
                    position = i;
            Term replacement;
            if (position < PS.Functions.size()) {
                Pair<String, Pair<Integer, Boolean>> thereplace = PS.Functions.get(position);
                ArrayList<Term> args = new ArrayList<>();
                for (int i = 0; i < thereplace.second.first; i++) args.add(Const.Hole.Dup());
                replacement = new Func(thereplace.first, args, thereplace.second.second);
            } else replacement = new Const(PS.Constants.get(position - PS.Functions.size()));
            replacement = pschange.Substitutions.get(pschange.subPos).second.replace(Const.HoleSelected, replacement).replaceLeft(Const.Hole, Const.HoleSelected.Dup());
            pschange.Substitutions.set(pschange.subPos, new Pair<>(pschange.Substitutions.get(pschange.subPos).first, replacement));
            ArrayList<Term> history = pschange.SubHistory.get(PS.Substitutions.get(PS.subPos).first);
            history.add(replacement.Dup());
            TermConstructActivity.this.ActivityDecorate();
        }
    }

    private class UndoSubstitutionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState PS = TermConstructActivity.this.PS;
            if (PS.SubHistory.get(PS.Substitutions.get(PS.subPos).first).isEmpty())
                PS.Substitutions.set(PS.subPos, new Pair<>(PS.Substitutions.get(PS.subPos).first, Const.HoleSelected.Dup()));
            else {
                ArrayList<Term> temp = PS.SubHistory.get(PS.Substitutions.get(PS.subPos).first);
                temp.remove(PS.SubHistory.get(PS.Substitutions.get(PS.subPos).first).size() - 1);
                PS.SubHistory.put(PS.Substitutions.get(PS.subPos).first, temp);
                if (PS.SubHistory.get(PS.Substitutions.get(PS.subPos).first).isEmpty())
                    PS.Substitutions.set(PS.subPos, new Pair<>(PS.Substitutions.get(PS.subPos).first, Const.HoleSelected.Dup()));
                else
                    PS.Substitutions.set(PS.subPos, new Pair<>(PS.Substitutions.get(PS.subPos).first, PS.SubHistory.get(PS.Substitutions.get(PS.subPos).first).get(PS.SubHistory.get(PS.Substitutions.get(PS.subPos).first).size() - 1)));
            }
            TermConstructActivity.this.ActivityDecorate();
        }
    }

    private class OnTouchHapticListener implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            return false;
        }
    }

}
