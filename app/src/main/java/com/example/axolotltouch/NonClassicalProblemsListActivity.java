package com.example.axolotltouch;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class NonClassicalProblemsListActivity extends DisplayProblemListHelper {
    public static final String NONCLASSICALPROBLEMSLOCATION = "non_classical";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_non_classical_problems_list_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        problems = new HashMap<>();
        try {
            problems.put(NONCLASSICALPROBLEMSLOCATION, this.getAssets().list(NONCLASSICALPROBLEMSLOCATION));
        } catch (IOException e) {
            Toast.makeText(NonClassicalProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            problems.put(NONCLASSICALPROBLEMSLOCATION, new String[0]);
        }
        parsedProblems = new ArrayList<>();
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.NonclassicalProblemList), NONCLASSICALPROBLEMSLOCATION);

    }
}
