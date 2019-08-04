package com.example.axolotltouch;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
        draw2D();
    }

    private void draw2D() {
        Proof proof = Proof.extractProof(PS);
        Pair<Bitmap, Pair<Float, Float>> bm = proof.draw();
        drawBitmap(bm.first);
    }

    @SuppressWarnings("ConstantConditions")
    private void drawFlat() {
        ArrayList<State> history = PS.History;
        ArrayList<Pair<ArrayList<String>, ArrayList<String>>> proof = new ArrayList<>();

        HashSet<Term> curSuccProblem = PS.problem;
        ArrayList<String> anteStrings = new ArrayList<>();
        ArrayList<String> succStrings = new ArrayList<>();
        for (Term t : curSuccProblem) {
            succStrings.add(t.Print());
        }
        proof.add(Pair.create(anteStrings, succStrings));

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
            proof.add(Pair.create(anteStrings, succStrings));
        }
        StringBuilder seq = new StringBuilder();
        Pair<ArrayList<String>, ArrayList<String>> cur = proof.get(0);

        for (int i = 0; i < cur.second.size(); i++) {
            seq.append(cur.second.get(i));
            if (i < cur.second.size() - 1) {
                seq.append(",");
            }
        }
        Pair<Bitmap, Pair<Float, Float>> bm = Proof.drawAxiom(seq.toString(), Proof.FORMULA_SIZE);
        for (int j = 1; j < proof.size(); j++) {
            seq = new StringBuilder();
            cur = proof.get(j);
            for (int i = 0; i < cur.second.size(); i++) {
                seq.append(cur.second.get(i));
                if (i < cur.second.size() - 1) {
                    seq.append(",");
                }
            }
            bm = Proof.drawUnaryInference(bm, seq.toString());
        }
        drawBitmap(bm.first);
    }

    private void drawBitmap(Bitmap bm) {
        Bitmap bm1 = Bitmap.createBitmap(bm.getWidth() + 500, bm.getHeight() + 500, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bm, 250, 250, null);
        TouchImageView myImage = findViewById(R.id.proofViz);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        TileBitmapDrawable.attachTileBitmapDrawable(myImage, is, null, null);
    }


}

