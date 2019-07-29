package com.example.axolotltouch;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ClassicalProblemsListActivity extends DisplayProblemListHelper {
    public static final String HILBERTPROBLEMSLOCATION = "classical/hilbert";
    public static final String SEQUENTPROBLEMSLOCATION = "classical/sequent";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_propositional_problems_list_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        problems = new HashMap<>();
        try {
            problems.put(HILBERTPROBLEMSLOCATION, this.getAssets().list(HILBERTPROBLEMSLOCATION));
            problems.put(SEQUENTPROBLEMSLOCATION, this.getAssets().list(SEQUENTPROBLEMSLOCATION));

        } catch (IOException e) {
            Toast.makeText(ClassicalProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            problems.put(HILBERTPROBLEMSLOCATION, new String[]{});
            problems.put(SEQUENTPROBLEMSLOCATION, new String[]{});
        }
        parsedProblems = new ArrayList<>();
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.HilbertProblemList), HILBERTPROBLEMSLOCATION);
        parsedProblems = new ArrayList<>();
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.SequentProblemList), SEQUENTPROBLEMSLOCATION);
    }


}
