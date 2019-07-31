package com.example.axolotltouch;

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
public class ClassicalProblemsListActivity extends AxolotlSupportingFunctionalityProblemList {
    /**
     * Location of the Axolotl files associated with classical logic hilbert style problems
     */
    public static final String HILBERTPROBLEMSLOCATION = "classical/hilbert";
    /**
     * Location of the Axolotl files associated with classical logic sequent style problems
     */
    public static final String SEQUENTPROBLEMSLOCATION = "classical/sequent";


    /**
     * This method overrides the onCreate method of AppCompatActivity.
     * @author David M. Cerna
     * @param savedInstanceState The bundle sent along with the intent from the activity requesting
     *                           this activity to be created.
     */
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

    /**
     * This method is an instantiation of the abstract method of AxolotlSupportingFunctionalityProblemList which decorates the main layout of
     * the activity.
     * @author David M. Cerna
     */
    protected void ActivityDecorate() {
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.HilbertProblemList), HILBERTPROBLEMSLOCATION);
        parsedProblems = new ArrayList<>();
        addProblemList(this.getAssets(), (LinearLayout) this.findViewById(R.id.SequentProblemList), SEQUENTPROBLEMSLOCATION);
    }


}
