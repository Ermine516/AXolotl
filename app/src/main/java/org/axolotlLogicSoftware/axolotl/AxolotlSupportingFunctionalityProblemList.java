package org.axolotlLogicSoftware.axolotl;

import android.content.res.AssetManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


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
     * associate these strings with a particular logic or proof calculus being that the  parsed problems are only
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
            ArrayList<String> probs = new ArrayList<>();
            InputStream is = manager.open(directory + "/" + "problem_manifest.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) probs.add(line);
            br.close();
            for (int i = 0; i < probs.size(); i++) {
                String problemstatement = probs.get(i).split(":")[1];
                parsedProblems.add(problemstatement);
                ll.addView(scrollTextSelectConstructString(problemstatement,
                        new AxolotlSupportingListenersAndMethods.ProblemSelectionListener(problems.get(directory),
                                parsedProblems, directory, PS.textSize, PS.observe), null, this, false));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            ll.removeAllViewsInLayout();
        }
    }

}
