package com.example.axolotltouch;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TermMatchingProblemsListActivity extends AxolotlSupportingFunctionalityProblemList {
    public static final String TERMPROBLEMSLOCATION = "termMatchingProblems";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_term_matching_problems_list_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        problems = new HashMap<>();
        try {
            problems.put(TERMPROBLEMSLOCATION, this.getAssets().list(TERMPROBLEMSLOCATION));
        } catch (IOException e) {
            Toast.makeText(TermMatchingProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            problems.put(TERMPROBLEMSLOCATION, new String[0]);
        }
        parsedProblems = new ArrayList<>();
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.TermMatchingProblemList), TERMPROBLEMSLOCATION);
    }

}