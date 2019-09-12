package org.axolotlLogicSoftware.axolotl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TermMatchingProblemsListActivity extends AxolotlSupportingFunctionalityProblemList {

    /**
     * Location of the Axolotl files associated with Term Matching problems
     */
    public static final String TERMPROBLEMS = "termMatchingProblems";
    /**
     * Location of the Axolotl files associated with Resolution problems
     */
    public static final String RESOLUTIONPROBLEMS = "resolution";
    String directory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_term_matching_problems_list_bar_layout);
        if (getIntent().hasExtra(AxolotlMessagingAndIO.PASSPROBLEMLIST)) {
            switch (getIntent().getIntExtra(AxolotlMessagingAndIO.PASSPROBLEMLIST, 0)) {
                case 0:
                    directory = TERMPROBLEMS;
                    break;
                case 1:
                    directory = RESOLUTIONPROBLEMS;
                    break;
                default: {
                    Intent intent = new Intent(TermMatchingProblemsListActivity.this, MainActivity.class);
                    if (getIntent().hasExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE))
                        intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, getIntent().getParcelableExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE));
                    TermMatchingProblemsListActivity.this.startActivity(intent);
                    TermMatchingProblemsListActivity.this.finish();
                }
                break;
            }
        } else {
            Intent intent = new Intent(TermMatchingProblemsListActivity.this, MainActivity.class);
            if (getIntent().hasExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE))
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, getIntent().getParcelableExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE));
            TermMatchingProblemsListActivity.this.startActivity(intent);
            overridePendingTransition(0, 0);
            TermMatchingProblemsListActivity.this.finish();
        }
        PS = ConstructActivity(savedInstanceState);
        problems = new HashMap<>();
        String[] temp;
        int i, j = 0;
        try {
            temp = this.getAssets().list(directory);
            String[] cleaned = new String[temp.length - 1];
            i = 0;
            for (String s : temp)
                if (s.compareTo("problem_manifest.txt") != 0) {
                    cleaned[i] = temp[i + j];
                    i++;
                } else j = 1;
            problems.put(directory, cleaned);

        } catch (IOException e) {
            Toast.makeText(TermMatchingProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            problems.put(directory, new String[]{});

        }
        parsedProblems = new ArrayList<>();
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.TermMatchingProblemList), directory);
    }

    protected void switchDisplay() {
    }

    protected void onInternalChange() {
    }
    @Override
    protected boolean implementationOfSwipeLeft() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, PS);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        return true;
    }
}