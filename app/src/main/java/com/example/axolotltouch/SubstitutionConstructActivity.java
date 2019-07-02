package com.example.axolotltouch;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class SubstitutionConstructActivity extends DisplayUpdateHelper {
    private ProblemState PS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_sub_con_bar_layout);
        FloatingActionButton fab = findViewById(R.id.fabsub);
        fab.setOnClickListener(new SubstitutionCreateListener());
        fab.setOnTouchListener(new OnTouchHapticListener());

        PS = ConstructActivity();
        ActivityDecorate();
    }
    void setPS(ProblemState ps){ PS = ps; }

    ProblemState getPS(){ return PS;}

    protected void ActivityDecorate() {
        UpdateProblemDisplay();
        UpdateTermDisplay();
        UpdateVariableDisplay();
    }

    private class SubstitutionCreateListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            ProblemState PS = SubstitutionConstructActivity.this.getPS();
            TextView RLVV = SubstitutionConstructActivity.this.findViewById(R.id.TermDisplay);
            String subTerm = RLVV.getText().toString();

            if(subTerm.contains(Const.Hole.getSym()) || subTerm.contains(Const.HoleSelected.getSym()) ){
                Toast.makeText(SubstitutionConstructActivity.this, "Term Cot Complete", Toast.LENGTH_SHORT).show();
                return;
            }else if(PS.selectedVariable.compareTo("")== 0){
                Toast.makeText(SubstitutionConstructActivity.this, "Select a Variable", Toast.LENGTH_SHORT).show();
                return;
            }else {
                if (!PS.Substitutions.keySet().contains(PS.selectedVariable)) {
                    ArrayList<Term> temp = new ArrayList<>();
                    temp.add(PS.substitution);
                    PS.Substitutions.put(PS.selectedVariable, temp);
                } else {
                    ArrayList<Term> Subs = PS.Substitutions.get(PS.selectedVariable);
                    for (Term t : Subs)
                        if (t.Print().compareTo(subTerm) == 0) {
                            Toast.makeText(SubstitutionConstructActivity.this, "Substitution Already Exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    Subs.add(PS.substitution);
                    PS.Substitutions.put(PS.selectedVariable, Subs);

                }
                PS.substitution = Const.HoleSelected.Dup();
                PS.selectedVariable = "";
                SubstitutionConstructActivity.this.setPS(PS);
                SubstitutionConstructActivity.this.ActivityDecorate();
            }
        }

    }
}

