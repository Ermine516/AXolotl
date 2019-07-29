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
            for (String problem : problems) {
                StringBuilder problemString = new StringBuilder();
                ProblemState newPS = AuxFunctionality.loadFile(manager.open("non_classical/" + problem), problem, this);
                Term[] anteProb = newPS.anteProblem.toArray(AuxFunctionality.HashSetTermArray);
                if (anteProb[0].Print().compareTo("∅") != 0) {
                    for (int j = 0; j < anteProb.length; j++)
                        if (j == anteProb.length - 1)
                            problemString.append(anteProb[j].Print()).append(" ");
                        else problemString.append(anteProb[j].Print()).append(" , ");
                }
                Term[] succProb = newPS.succProblem.toArray(AuxFunctionality.HashSetTermArray);
                for (int j = 0; j < succProb.length; j++)
                    if (j == succProb.length - 1) problemString.append(succProb[j].Print());
                    else problemString.append(succProb[j].Print()).append(" , ");
                parsedProblems.add(problemString.toString());
                TPLL.addView(scrollTextSelectConstruct(problemString.toString(), new ProblemSelectionListener(problems, parsedProblems, "non_classical/"), this, false));
            }
        } catch (IOException e) {
            Toast.makeText(NonClassicalProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            TPLL.removeAllViewsInLayout();
        }
    }
}
