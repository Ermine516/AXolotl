package com.example.axolotltouch;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.core.util.Pair;

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;


public class ProofDisplayActivity extends AxolotlSupportingFunctionality {
    private ProblemState PS;

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
        drawFlat();
    }

    private void draw2D() {
        Proof proof = Proof.extractProof(PS);
        Pair<Bitmap, Pair<Float, Float>> bm = proof.draw();
        drawBitmap(bm.first);
    }

    @SuppressWarnings("ConstantConditions")
    private void drawFlat() {
        ArrayList<Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>>> history = PS.History;
        ArrayList<Pair<ArrayList<String>, ArrayList<String>>> proof = new ArrayList<>();

        HashSet<Term> curAnteProblem = PS.anteProblem;
        HashSet<Term> curSuccProblem = PS.succProblem;
        ArrayList<String> anteStrings = new ArrayList<>();
        for(Term t : curAnteProblem) {
            anteStrings.add(t.Print());
        }
        ArrayList<String> succStrings = new ArrayList<>();
        for(Term t : curSuccProblem) {
            succStrings.add(t.Print());
        }
        proof.add(Pair.create(anteStrings, succStrings));

        for(int ind = history.size() - 1; ind >= 0; ind--) {
            Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>> laststep = history.get(ind);
            Pair<ArrayList<Term>, Term> rule = laststep.second.second;
            ArrayList<Term> anteSideApply = new ArrayList<>();
            Term succSideApply = rule.second.Dup();
            for (Pair<String, Term> s : laststep.second.first) {
                succSideApply = succSideApply.replace(new Const(s.first), s.second);
            }
            if (laststep.first.first.size() != 0) {
                if (rule.first.size() > 0) {
                    anteSideApply.addAll(rule.first);
                    for (int i = 0; i < anteSideApply.size(); i++)
                        for (Pair<String, Term> s : laststep.second.first)
                            anteSideApply.set(i, anteSideApply.get(i).replace(new Const(s.first), s.second));
                }
                HashSet<Term> newAnteProblem = new HashSet<>(anteSideApply);
                for (Term t : curAnteProblem)
                    if (t.Print().compareTo(succSideApply.Print()) != 0)
                        newAnteProblem.add(t);
                curAnteProblem = newAnteProblem;
            } else {
                HashSet<Term> newSuccProblem = new HashSet<>();
                for (Pair<String, Term> s : laststep.second.first)
                    succSideApply = succSideApply.replace(new Const(s.first), s.second);
                newSuccProblem.add(succSideApply);
                for (Term t : rule.first) {
                    Term temp = t.Dup();
                    for (Pair<String, Term> s : laststep.second.first)
                        temp = temp.replace(new Const(s.first), s.second);
                    anteSideApply.add(temp);
                }
                for (Term t : curSuccProblem) {
                    boolean wasselected = false;
                    for (Term s : anteSideApply)
                        if (t.Print().compareTo(s.Print()) == 0) wasselected = true;
                    if (!wasselected) newSuccProblem.add(t);
                }
                curSuccProblem = newSuccProblem;
            }
            anteStrings = new ArrayList<>();
            for(Term t : curAnteProblem) {
                anteStrings.add(t.Print());
            }
            succStrings = new ArrayList<>();
            for(Term t : curSuccProblem) {
                succStrings.add(t.Print());
            }
            proof.add(Pair.create(anteStrings, succStrings));
        }
        StringBuilder seq = new StringBuilder();
        Pair<ArrayList<String>,ArrayList<String>> cur = proof.get(0);
        for(int i = 0; i < cur.first.size(); i++) {
            seq.append(cur.first.get(i));
        }
        seq.append("\u22A2");
        for(int i = 0; i < cur.second.size(); i++) {
            seq.append(cur.second.get(i));
        }
        Pair<Bitmap, Pair<Float, Float>> bm = Proof.drawAxiom(seq.toString());
        for(int j = 1; j < proof.size(); j++){
            seq = new StringBuilder();
            cur = proof.get(j);
            for(int i = 0; i < cur.first.size(); i++) {
                seq.append(cur.first.get(i));
                if(i < cur.first.size() - 1) {
                    seq.append(",");
                }
            }
            seq.append("\u22A2");
            for(int i = 0; i < cur.second.size(); i++) {
                seq.append(cur.second.get(i));
                if(i < cur.second.size() - 1) {
                    seq.append(",");
                }
            }
            bm = Proof.drawUnaryInference(bm, seq.toString());
        }
        drawBitmap(bm.first);
    }

    private void drawBitmap(Bitmap bm) {
        TouchImageView myImage = findViewById(R.id.proofViz);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        TileBitmapDrawable.attachTileBitmapDrawable(myImage, is, null, null);
    }


}

