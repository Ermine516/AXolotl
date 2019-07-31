package com.example.axolotltouch;

import android.content.res.AssetManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


/**
 * This is an abstract subclass AxolotlSupportingFunctionality which focuses on providing the appropriate
 * methods for constructing the activities displaying problems from the problem database contained within
 * the asset directory of Axolotl.
 *
 * @author David M. Cerna
 */
public abstract class AxolotlSupportingFunctionalityProblemList extends AxolotlSupportingFunctionality {
    /**
     * Each list of problems is associate with a certain logic or proof calculus style. This hashmap
     * captures this separation of ensures proper display of the problem
     */
    HashMap<String, String[]> problems;
    /**
     * This field contains the parsed (as in pretty printed term) associated with a given problem. We do not
     * associate these strings with a particular logic or proof calculus being the  parsed problems are only
     * used for display reasons.
     */
    ArrayList<String> parsedProblems;

    /**
     * This method adds a list of problems to the layout of the given activity.
     * @author David M. Cerna
     * @param manager The object managing the assets of the Axolotl.
     * @param ll The layout which will inlcude the list of problems.
     * @param directory The asset directory containing the problem files.
     */
    protected void addProblemList(AssetManager manager, LinearLayout ll, String directory) {
        ll.removeAllViewsInLayout();
        try {
            for (int i = 0; i < Objects.requireNonNull(problems.get(directory)).length; i++) {
                StringBuilder problemString = new StringBuilder();
                ProblemState newPS = AxolotlMessagingAndIO.loadFile(manager.open(directory + "/" + Objects.requireNonNull(problems.get(directory))[i]), Objects.requireNonNull(problems.get(directory))[i], this);
                Term[] anteProb = newPS.anteProblem.toArray(AxolotlMessagingAndIO.HashSetTermArray);
                if (anteProb[0].Print().compareTo("∅") != 0) {
                    for (int j = 0; j < anteProb.length; j++)
                        if (j == anteProb.length - 1)
                            problemString.append(anteProb[j].Print()).append(" ");
                        else problemString.append(anteProb[j].Print()).append(" , ");
                }
                Term[] succProb = newPS.succProblem.toArray(AxolotlMessagingAndIO.HashSetTermArray);
                for (int j = 0; j < succProb.length; j++)
                    if (j == succProb.length - 1) problemString.append(succProb[j].Print());
                    else problemString.append(succProb[j].Print()).append(" , ");
                parsedProblems.add(problemString.toString());
                ll.addView(scrollTextSelectConstruct(problemString.toString(), new AxolotlSupportingListenersAndMethods.ProblemSelectionListener(problems.get(directory), parsedProblems, directory), this, false));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            ll.removeAllViewsInLayout();
        }
    }

}
