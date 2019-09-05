package org.axolotlLogicSoftware.axolotl;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static org.axolotlLogicSoftware.axolotl.AxolotlMessagingAndIO.PASSPROBLEMSTATE;

public class MatchDisplayActivity extends AxolotlSupportingFunctionality {
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
        TextView placeInDisplay = this.findViewById(R.id.placein);
        placeInDisplay.setText("Variable " + (PS.subPos + 1) + " of " + PS.Substitutions.size());

        TextView varDisplay = this.findViewById(R.id.VarTextview);
        varDisplay.setTextSize(PS.textSize);
        TextView subDisplay = this.findViewById(R.id.SubTermTextView);
        subDisplay.setTextSize(PS.textSize);

        LinearLayout leftTerm = this.findViewById(R.id.LeftSideTermLayout);
        LinearLayout rightTerm = this.findViewById(R.id.RightSideTermLayout);
        leftTerm.removeAllViewsInLayout();
        rightTerm.removeAllViewsInLayout();
        String var = PS.Substitutions.get(PS.subPos).variable;
        Term succTerm = PS.getSelectedSuccTerm();
        succTerm.normalize(PS.Variables); // Don't forget that sequents are brittle terms
        leftTerm.addView(scrollTextSelectConstructString(PS.currentRule.argument.Print(new Const(var), PS.Variables.contains(var)), null, null, this, true));
        rightTerm.addView(scrollTextSelectConstructString(succTerm.Print(var, PS.currentRule.argument, PS.Substitutions.get(PS.subPos).replacement), null, null, this, true));
        varDisplay.setText(Html.fromHtml("<b>" + var + "</b>"));
        try {
            if (PS.Substitutions.get(PS.subPos).replacement.Print().compareTo("") != 0)
                subDisplay.setText(PS.Substitutions.get(PS.subPos).replacement.Print());
            else subDisplay.setText("ε");
        } catch (NullPointerException e) {
            Toast.makeText(MatchDisplayActivity.this, "Unable to Display State", Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean implementationOfSwipeLeft() {
        try {
            PS.subPos--;
            Intent intent;
            if (MatchDisplayActivity.this.PS.subPos == -1 || !PS.observe) {
                MatchDisplayActivity.this.PS.subPos = -1;
                PS.Substitutions = new Substitution();
                intent = new Intent(MatchDisplayActivity.this, MainActivity.class);
            } else if (PS.MatchorConstruct.get(PS.Substitutions.get(PS.subPos).variable)) {
                PS.Substitutions.alter(PS.subPos, PS.Substitutions.get(PS.subPos).variable, Const.HoleSelected.Dup());
                intent = new Intent(MatchDisplayActivity.this, TermConstructActivity.class);
            } else {
                intent = new Intent(MatchDisplayActivity.this, MatchDisplayActivity.class);
            }
            intent.putExtra(PASSPROBLEMSTATE, PS);
            MatchDisplayActivity.this.startActivity(intent);
            MatchDisplayActivity.this.finish();
            overridePendingTransition(0, 0);
            //overridePendingTransition(R.anim.animation_leave, R.anim.animation_enter);

        } catch (NullPointerException e) {
            Toast.makeText(MatchDisplayActivity.this, "Problems accessing Previous State", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    protected class SwipeListener extends OnSwipeTouchListener {
        SwipeListener(Context ctx) {
            super(ctx);
        }

        public boolean onSwipeRight() {
            ProblemState PS = MatchDisplayActivity.this.PS;
            Intent intent;
            try {
                if (PS.subPos != -1 && !PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected)) {
                    MatchDisplayActivity.this.PS.subPos++;
                    if (!PS.observe)
                        while (PS.Substitutions.isPosition(PS.subPos) && !PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                            PS.subPos++;
                    if (PS.Substitutions.isPosition(PS.subPos)) {
                        if (PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                            intent = new Intent(MatchDisplayActivity.this, TermConstructActivity.class);
                        else
                            intent = new Intent(MatchDisplayActivity.this, MatchDisplayActivity.class);
                    } else {
                        MatchDisplayActivity.this.swipeRightProblemStateUpdate();
                        intent = new Intent(MatchDisplayActivity.this, MainActivity.class);
                    }
                    intent.putExtra(PASSPROBLEMSTATE, PS);
                    MatchDisplayActivity.this.startActivity(intent);
                    overridePendingTransition(0, 0);
                    //overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
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

    protected void switchDisplay() {
    }

    protected void onInternalChange() {
    }
}
