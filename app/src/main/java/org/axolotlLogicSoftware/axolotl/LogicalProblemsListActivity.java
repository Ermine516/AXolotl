package org.axolotlLogicSoftware.axolotl;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class extends AxolotlSupportingFunctionalityProblemList and provides a problem list activity for classical
 * logic problems.
 *
 * @author David M. Cerna
 */
public class LogicalProblemsListActivity extends AxolotlSupportingFunctionalityProblemList {
    /**
     * Location of the Axolotl files associated with classical logic hilbert style problems
     */
    public static final String CLASSICALPROBLEMS = "classical/";
    /**
     * Location of the Axolotl files associated with classical logic hilbert style problems
     */
    public static final String NONCLASSICALPROBLEMS = "modal/";
    /**
     * Location of the Axolotl files associated with classical logic hilbert style problems
     */
    public static final String HILBERTPROBLEMSLOCATION = "hilbert";
    /**
     * Location of the Axolotl files associated with classical logic sequent style problems
     */
    public static final String SEQUENTPROBLEMSLOCATION = "sequent";
    /**
     * Location of the Axolotl files associated with classical logic Natural Deduction style problems
     */
    public static final String NATURALPROBLEMSLOCATION = "natural";
    String directory = "";

    /**
     * This method overrides the onCreate method of AppCompatActivity.
     *
     * @param savedInstanceState The bundle sent along with the intent from the activity requesting
     *                           this activity to be created.
     * @author David M. Cerna
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_logical_problems_list_bar_layout);
        if (getIntent().hasExtra(AxolotlMessagingAndIO.PASSPROBLEMLIST)) {
            switch (getIntent().getIntExtra(AxolotlMessagingAndIO.PASSPROBLEMLIST, 0)) {
                case 0:
                    directory = CLASSICALPROBLEMS;
                    break;
                case 1:
                    directory = NONCLASSICALPROBLEMS;
                    break;
                default: {
                    Intent intent = new Intent(LogicalProblemsListActivity.this, MainActivity.class);
                    if (getIntent().hasExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE))
                        intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, getIntent().getParcelableExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE));
                    LogicalProblemsListActivity.this.startActivity(intent);
                    LogicalProblemsListActivity.this.finish();
                }
                break;
            }
        } else {
            Intent intent = new Intent(LogicalProblemsListActivity.this, MainActivity.class);
            if (getIntent().hasExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE))
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, getIntent().getParcelableExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE));
            LogicalProblemsListActivity.this.startActivity(intent);
            overridePendingTransition(0, 0);
            LogicalProblemsListActivity.this.finish();
        }
        PS = ConstructActivity(savedInstanceState);
        problems = new HashMap<>();
        String[] temp;
        int i, j = 0;
        try {
            temp = this.getAssets().list(directory + HILBERTPROBLEMSLOCATION);
            String[] cleaned = new String[temp.length - 1];
            i = 0;
            for (String s : temp)
                if (s.compareTo("problem_manifest.txt") != 0) {
                    cleaned[i] = temp[i + j];
                    i++;
                } else j = 1;
            problems.put(directory + HILBERTPROBLEMSLOCATION, cleaned);
            temp = this.getAssets().list(directory + SEQUENTPROBLEMSLOCATION);
            cleaned = new String[temp.length - 1];
            i = 0;
            j = 0;
            for (String s : temp)
                if (s.compareTo("problem_manifest.txt") != 0) {
                    cleaned[i] = temp[i + j];
                    i++;
                } else j = 1;

            problems.put(directory + SEQUENTPROBLEMSLOCATION, cleaned);
            temp = this.getAssets().list(directory + NATURALPROBLEMSLOCATION);
            cleaned = new String[temp.length - 1];
            i = 0;
            j = 0;
            for (String s : temp)
                if (s.compareTo("problem_manifest.txt") != 0) {
                    cleaned[i] = temp[i + j];
                    i++;
                } else j = 1;

            problems.put(directory + NATURALPROBLEMSLOCATION, cleaned);


        } catch (IOException e) {
            Toast.makeText(LogicalProblemsListActivity.this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            problems.put(directory + HILBERTPROBLEMSLOCATION, new String[]{});
            problems.put(directory + SEQUENTPROBLEMSLOCATION, new String[]{});
            problems.put(directory + NATURALPROBLEMSLOCATION, new String[]{});

        }
        parsedProblems = new ArrayList<>();
        ActivityDecorate();
    }

    /**
     * This method is an instantiation of the abstract method of AxolotlSupportingFunctionalityProblemList which decorates the main layout of
     * the activity.
     *
     * @author David M. Cerna
     */
    protected void ActivityDecorate() {
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.HilbertProblemList), directory + HILBERTPROBLEMSLOCATION);
        parsedProblems = new ArrayList<>();
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.SequentProblemList), directory + SEQUENTPROBLEMSLOCATION);
        parsedProblems = new ArrayList<>();
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.NaturalProblemList), directory + NATURALPROBLEMSLOCATION);
    }

    protected void switchDisplay() {
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

    protected void onInternalChange() {
    }
}
