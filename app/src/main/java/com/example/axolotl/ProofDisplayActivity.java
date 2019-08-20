package com.example.axolotl;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;


public class ProofDisplayActivity extends AxolotlSupportingFunctionality {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_proof_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        ActivityDecorate();
    }

    protected void ActivityDecorate() {
//        Pair<Bitmap, Pair<Float, Float>> bm = drawAxiom("yyy");
//        Pair<Bitmap, Pair<Float, Float>> bm1 = drawBinaryInference(bm, bm, "yHH");
//        bm1 = drawUnaryInference(bm1, "HHHHH");
//        bm1 = drawBinaryInference(bm1, bm, "a");
//        drawBitmap(bm1.first);
        draw2D();
    }

    private void draw2D() {
//        Proof proof = Proof.extractProof(PS);
//        Pair<Bitmap, Pair<Float, Float>> bm = proof.draw();
        drawBitmap(Proof.drawProblemSolution(PS));
    }

    @SuppressWarnings("ConstantConditions")
    private void drawFlat() {
        ArrayList<State> history = PS.History;
        ArrayList<Pair<String, Pair<ArrayList<String>, ArrayList<String>>>> proof = new ArrayList<>();

        HashSet<Term> curSuccProblem = PS.problem;
        ArrayList<String> anteStrings = new ArrayList<>();
        ArrayList<String> succStrings = new ArrayList<>();
        for (Term t : curSuccProblem) {
            succStrings.add(t.Print());
        }
        proof.add(Pair.create("", Pair.create(anteStrings, succStrings)));

        for (int ind = history.size() - 1; ind >= 0; ind--) {
            State laststep = history.get(ind);
            Term succSideApply = laststep.substitution.apply(laststep.rule.argument.Dup());
            HashSet<Term> newSuccProblem = new HashSet<>();
            newSuccProblem.add(succSideApply);
            HashSet<Term> anteSideApply = laststep.substitution.apply(laststep.rule.Conclusions);
            for (Term t : curSuccProblem) {
                boolean wasselected = false;
                for (Term s : anteSideApply)
                    if (t.Print().compareTo(s.Print()) == 0) wasselected = true;
                if (!wasselected) newSuccProblem.add(t);
            }
            curSuccProblem = newSuccProblem;
            succStrings = new ArrayList<>();
            for (Term t : curSuccProblem) {
                succStrings.add(t.Print());
            }
            proof.add(Pair.create(laststep.rule.Label, Pair.create(anteStrings, succStrings)));
        }
        StringBuilder seq = new StringBuilder();
        Pair<String, Pair<ArrayList<String>, ArrayList<String>>> cur = proof.get(0);

        for (int i = 0; i < cur.second.second.size(); i++) {
            seq.append(cur.second.second.get(i));
            if (i < cur.second.second.size() - 1) {
                seq.append(",");
            }
        }
        Pair<Bitmap, Pair<Float, Float>> bm = Proof.drawAxiom(seq.toString(), cur.first, Proof.FORMULA_SIZE);
        for (int j = 1; j < proof.size(); j++) {
            seq = new StringBuilder();
            cur = proof.get(j);
            for (int i = 0; i < cur.second.second.size(); i++) {
                seq.append(cur.second.second.get(i));
                if (i < cur.second.second.size() - 1) {
                    seq.append(",");
                }
            }
            bm = Proof.drawUnaryInference(bm, cur.first, seq.toString());
        }
        drawBitmap(bm.first);
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

