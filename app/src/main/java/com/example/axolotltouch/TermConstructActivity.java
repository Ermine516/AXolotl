package com.example.axolotltouch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.example.axolotltouch.AxolotlMessagingAndIO.PASSPROBLEMSTATE;


public class TermConstructActivity extends AxolotlSupportingFunctionality {
    @SuppressLint("ClickableViewAccessibility")
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
        if (!PS.SubHistory.keySet().contains(PS.Substitutions.get(PS.subPos).variable))
            PS.SubHistory.put(PS.Substitutions.get(PS.subPos).variable, new ArrayList<Term>());
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        FurtureProblemDisplay();
        UpdateTermDisplay();
        TermDisplayUpdate();
    }

    private void FurtureProblemDisplay() {
        ProblemState PS = TermConstructActivity.this.PS;
        Substitution localSubstitution = PS.Substitutions.simplifyWithRespectTo(PS.Substitutions.get(PS.subPos).variable);
            updatefutureProblemSideDisplay((LinearLayout) this.findViewById(R.id.RightSideTermLayout), PS.replaceSelectedSuccTerm(localSubstitution.apply(PS.currentRule.Conclusions)).toArray(AxolotlMessagingAndIO.HashSetTermArray));
    }

    private void UpdateTermDisplay() {
        TextView td = this.findViewById(R.id.TermDisplay);
        LinearLayout ltd = this.findViewById(R.id.TermInstancceLayout);
        td.setText(PS.Substitutions.get(PS.subPos).replacement.Print());
        int width = ((int) td.getPaint().measureText(td.getText().toString())) + 20;
        td.setWidth((width > 75) ? width : 75);
        ltd.setMinimumWidth((width > 75) ? width : 75);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void TermDisplayUpdate() {
        LinearLayout RLVV = this.findViewById(R.id.TermSelectionLayout);
        RLVV.removeAllViewsInLayout();
        for (FunctionDefinition p : PS.Functions) {
            if (ProblemState.isNotReserved(p.name)) {
                int arity = p.arity;
                boolean infix = p.fixity;
                ArrayList<Term> args = new ArrayList<>();
                while (arity > 0) {
                    args.add(new Const(Const.Hole.getSym()));
                    arity--;
                }
                String funcText = new Func(p.name, args, infix).Print();
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
        for (String cons : PS.Constants) {
            if (ProblemState.isNotReserved(cons)) {
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
    }

    protected class SwipeListener extends OnSwipeTouchListener {
        SwipeListener(Context ctx) {
            super(ctx);
        }

        @SuppressWarnings("ConstantConditions")
        public boolean onSwipeLeft() {
            PS.subPos--;
            Intent intent;
            try {
                if (TermConstructActivity.this.PS.subPos == -1 || !PS.observe) {
                    TermConstructActivity.this.PS.subPos = -1;
                    PS.selectedPosition = "";
                    PS.Substitutions = new Substitution();
                    intent = new Intent(TermConstructActivity.this, MainActivity.class);
                    Toast.makeText(TermConstructActivity.this, "Select Rule and Problem Side", Toast.LENGTH_SHORT).show();
                } else {
                    if (PS.MatchorConstruct.get(PS.Substitutions.get(PS.subPos).variable)) {
                        PS.Substitutions.alter(PS.subPos, PS.Substitutions.get(PS.subPos).variable, Const.HoleSelected.Dup());
                        intent = new Intent(TermConstructActivity.this, TermConstructActivity.class);
                    } else
                        intent = new Intent(TermConstructActivity.this, MatchDisplayActivity.class);
                    Toast.makeText(TermConstructActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).variable, Toast.LENGTH_SHORT).show();
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
                if (!PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected)) {
                    TermConstructActivity.this.PS.subPos++;
                    if (!PS.observe)
                        while (PS.Substitutions.isPosition(PS.subPos) && !PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                            PS.subPos++;
                    if (PS.Substitutions.isPosition(PS.subPos)) {
                        if (PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                            intent = new Intent(TermConstructActivity.this, TermConstructActivity.class);
                        else
                            intent = new Intent(TermConstructActivity.this, MatchDisplayActivity.class);
                        Toast.makeText(TermConstructActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).variable, Toast.LENGTH_SHORT).show();
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

    @SuppressWarnings("ConstantConditions")
    private class SymbolSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState pschange = TermConstructActivity.this.PS;
            LinearLayout tl = TermConstructActivity.this.findViewById(R.id.TermSelectionLayout);
            int position = -1;
            for (int i = 0; i < tl.getChildCount(); i++)
                if (((TextView) tl.getChildAt(i)).getText().toString().compareTo(((TextView) view).getText().toString()) == 0)
                    position = i += 2;
            Term replacement;
            if (position < PS.Functions.size()) {
                FunctionDefinition thereplace = PS.Functions.get(position);
                ArrayList<Term> args = new ArrayList<>();
                for (int i = 0; i < thereplace.arity; i++) args.add(Const.Hole.Dup());
                replacement = new Func(thereplace.name, args, thereplace.fixity);
            } else replacement = new Const(PS.Constants.get(position + 1 - PS.Functions.size()));
            replacement = pschange.Substitutions.get(pschange.subPos).replacement.replace(Const.HoleSelected, replacement).replaceLeft(Const.Hole, Const.HoleSelected.Dup());
            pschange.Substitutions.alter(pschange.subPos, pschange.Substitutions.get(pschange.subPos).variable, replacement);
            ArrayList<Term> history = pschange.SubHistory.get(PS.Substitutions.get(PS.subPos).variable);
            history.add(replacement.Dup());
            TermConstructActivity.this.ActivityDecorate();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private class UndoSubstitutionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState PS = TermConstructActivity.this.PS;
            if (PS.SubHistory.get(PS.Substitutions.get(PS.subPos).variable).isEmpty())
                PS.Substitutions.alter(PS.subPos, PS.Substitutions.get(PS.subPos).variable, Const.HoleSelected.Dup());
            else {
                ArrayList<Term> temp = PS.SubHistory.get(PS.Substitutions.get(PS.subPos).variable);
                temp.remove(PS.SubHistory.get(PS.Substitutions.get(PS.subPos).variable).size() - 1);
                PS.SubHistory.put(PS.Substitutions.get(PS.subPos).variable, temp);
                if (PS.SubHistory.get(PS.Substitutions.get(PS.subPos).variable).isEmpty())
                    PS.Substitutions.alter(PS.subPos, PS.Substitutions.get(PS.subPos).variable, Const.HoleSelected.Dup());
                else
                    PS.Substitutions.alter(PS.subPos, PS.Substitutions.get(PS.subPos).variable, PS.SubHistory.get(PS.Substitutions.get(PS.subPos).variable).get(PS.SubHistory.get(PS.Substitutions.get(PS.subPos).variable).size() - 1));
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

    protected void switchDisplay() {
    }

}
