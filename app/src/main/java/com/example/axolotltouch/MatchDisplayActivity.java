package com.example.axolotltouch;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.axolotltouch.AuxFunctionality.PASSPROBLEMSTATE;

public class MatchDisplayActivity extends DisplayUpdateHelper {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_match_bar_layout);
        findViewById(R.id.OuterLayout).setOnTouchListener(new SwipeListener(this));
        findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        PS = ConstructActivity(savedInstanceState);
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        System.out.println(PS.Substitutions.get(PS.subPos).first + "  " + PS.Substitutions.get(PS.subPos).second.Print());

        TextView lhs = this.findViewById(R.id.LeftSideProblem);
        TextView rhs = this.findViewById(R.id.RightSideProblem);
        TextView varDisplay = this.findViewById(R.id.VarTextview);
        TextView subDisplay = this.findViewById(R.id.SubTermTextView);
        String var = PS.Substitutions.get(PS.subPos).first;
        if (MatchDisplayActivity.this.PS.anteSelectedPositions.size() == 0) {
            Term succTerm = PS.getSelectedSuccTerm();
            lhs.setText(Html.fromHtml(succTerm.Print(new Const(var))));
            rhs.setText(Html.fromHtml(PS.succCurrentRule.Print(PS.Substitutions.get(PS.subPos).second)));
        } else {
            ArrayList<Term> anteTerm = PS.getSelectedAnteTerm();
        }

        varDisplay.setText(var);
        try {
            subDisplay.setText(PS.Substitutions.get(PS.subPos).second.Print());
        } catch (NullPointerException e) {
            Toast.makeText(MatchDisplayActivity.this, "Unable to Display State", Toast.LENGTH_SHORT).show();
        }
    }

    protected class SwipeListener extends OnSwipeTouchListener {
        SwipeListener(Context ctx) {
            super(ctx);
        }

        public boolean onSwipeLeft() {
            try {
                PS.subPos--;
                Intent intent;
                if (MatchDisplayActivity.this.PS.subPos == -1 || !PS.observe) {
                    MatchDisplayActivity.this.PS.subPos = -1;
                    PS.anteSelectedPositions = new ArrayList<>();
                    PS.succSelectedPosition = "";
                    PS.Substitutions = new ArrayList<>();
                    intent = new Intent(MatchDisplayActivity.this, MainActivity.class);
                    Toast.makeText(MatchDisplayActivity.this, "Select Rule and Problem Side", Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(MatchDisplayActivity.this, MatchDisplayActivity.class);
                    Toast.makeText(MatchDisplayActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).first, Toast.LENGTH_SHORT).show();
                }
                intent.putExtra(PASSPROBLEMSTATE, PS);
                MatchDisplayActivity.this.startActivity(intent);
                MatchDisplayActivity.this.finish();
            } catch (NullPointerException e) {
                Toast.makeText(MatchDisplayActivity.this, "Problems accessing Previous State", Toast.LENGTH_SHORT).show();

            }
            return true;
        }

        public boolean onSwipeRight() {
            ProblemState PS = MatchDisplayActivity.this.PS;
            Intent intent;
            try {
                if (!PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected)) {
                    MatchDisplayActivity.this.PS.subPos++;
                    if (!PS.observe)
                        while (PS.subPos < PS.Substitutions.size() && !PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected))
                            PS.subPos++;
                    if (MatchDisplayActivity.this.PS.subPos < MatchDisplayActivity.this.PS.Substitutions.size()) {
                        if (PS.Substitutions.get(PS.subPos).second.contains(Const.HoleSelected))
                            intent = new Intent(MatchDisplayActivity.this, TermConstructActivity.class);
                        else
                            intent = new Intent(MatchDisplayActivity.this, MatchDisplayActivity.class);
                        Toast.makeText(MatchDisplayActivity.this, "Substitution for " + PS.Substitutions.get(PS.subPos).first, Toast.LENGTH_SHORT).show();
                    } else {
                        MatchDisplayActivity.this.swipeRightProblemStateUpdate();
                        intent = new Intent(MatchDisplayActivity.this, MainActivity.class);
                    }
                    intent.putExtra(PASSPROBLEMSTATE, PS);
                    MatchDisplayActivity.this.startActivity(intent);
                    MatchDisplayActivity.this.finish();
                } else {
                    MatchDisplayActivity.this.PS.subPos--;
                    Toast.makeText(MatchDisplayActivity.this, "Incomplete Substitution", Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException e) {
                Toast.makeText(MatchDisplayActivity.this, "Problems Substituting", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }
}
