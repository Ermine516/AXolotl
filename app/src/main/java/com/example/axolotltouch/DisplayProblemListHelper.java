package com.example.axolotltouch;

import android.content.res.AssetManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public abstract class DisplayProblemListHelper extends DisplayUpdateHelper {
    HashMap<String, String[]> problems;
    ArrayList<String> parsedProblems;

    protected void addProblemList(AssetManager manager, LinearLayout ll, String directory) {
        ll.removeAllViewsInLayout();
        try {
            for (int i = 0; i < Objects.requireNonNull(problems.get(directory)).length; i++) {
                StringBuilder problemString = new StringBuilder();
                ProblemState newPS = AuxFunctionality.loadFile(manager.open(directory + "/" + Objects.requireNonNull(problems.get(directory))[i]), Objects.requireNonNull(problems.get(directory))[i], this);
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
                ll.addView(scrollTextSelectConstruct(problemString.toString(), new DisplayListenerHelper.ProblemSelectionListener(problems.get(directory), parsedProblems, directory), this, false));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Issues accessing Problem Database.", Toast.LENGTH_SHORT).show();
            ll.removeAllViewsInLayout();
        }
    }

}
