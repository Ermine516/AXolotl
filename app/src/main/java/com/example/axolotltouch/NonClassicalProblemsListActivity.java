package com.example.axolotltouch;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class NonClassicalProblemsListActivity extends DisplayUpdateHelper {
    private String[] problems;
    private ArrayList<String> parsedProblems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_non_classical_problems_list_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        try {
            problems = this.getAssets().list("non_classical");
        } catch (IOException e) {
            Toast.makeText(NonClassicalProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            problems = new String[0];
        }
        parsedProblems = new ArrayList<>();
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        AssetManager manager = this.getAssets();
        LinearLayout TPLL = this.findViewById(R.id.NonclassicalProblemList);
        TPLL.removeAllViewsInLayout();
        try {
            new ArrayList<>();
            for (int i = 0; i < problems.length; i++) {
                String problemString = "";
                ProblemState newPS = AuxFunctionality.loadFile(manager.open("non_classical/" + problems[i]), problems[i], this);
                Term[] anteProb = newPS.anteProblem.toArray(AuxFunctionality.HashSetTermArray);
                if (anteProb[0].Print().compareTo("∅") != 0) {
                    for (int j = 0; j < anteProb.length; j++)
                        if (j == anteProb.length - 1) problemString += anteProb[j].Print() + " ";
                        else problemString += anteProb[j].Print() + " , ";
                }
                Term[] succProb = newPS.succProblem.toArray(AuxFunctionality.HashSetTermArray);
                for (int j = 0; j < succProb.length; j++)
                    if (j == succProb.length - 1) problemString += succProb[j].Print();
                    else problemString += succProb[j].Print() + " , ";
                parsedProblems.add(problemString);
                TPLL.addView(scrollTextSelectConstruct(problemString, new DisplayListenerHelper.ProblemSelectionListener(problems, parsedProblems, "non_classical/"), this, false));
            }
        } catch (IOException e) {
            Toast.makeText(NonClassicalProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            TPLL.removeAllViewsInLayout();
        }
    }
}
