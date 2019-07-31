package com.example.axolotltouch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

public class Proof {

    ArrayList<Proof> antecedents;
    String formula;
    boolean finished;
    boolean drawLine = true;

    public Proof(String formula) {
        antecedents = new ArrayList<>();
        this.formula = formula;
        finished = false;
    }

    static Proof incomplete() {
        Proof result = new Proof("?");
        result.setFinished(true);
        result.drawLine = false;
        return result;
    }

    public boolean isAxiom() {
        return antecedents.isEmpty();
    }

    public ArrayList<Proof> getAntecedents() {
        return antecedents;
    }

    public void addAntecedent(Proof antecedent) {
        antecedents.add(antecedent);
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Pair<Bitmap, Pair<Float, Float>> draw() {
        Pair<Bitmap, Pair<Float, Float>> result;
        if(!finished) {
            addAntecedent(incomplete());
        }
        if(isAxiom()) {
            if(drawLine) {
                result = drawAxiom(formula);
            } else {
                result = drawText(formula);
            }
        } else if(antecedents.size() == 1) {
            result =  drawUnaryInference(antecedents.get(0).draw(), formula);
        } else if(antecedents.size() == 2) {
            result =  drawBinaryInference(antecedents.get(0).draw(), antecedents.get(1).draw(), formula);
        } else {
            ArrayList<Pair<Bitmap, Pair<Float, Float>>> prev = new ArrayList<>();
            for(Proof antecedent : antecedents) {
                prev.add(antecedent.draw());
            }
            result = drawNaryInference(prev, formula);
        }
        if(!finished) {
            antecedents.remove(antecedents.size() - 1);
        }
        return result;
    }

    static Proof extractProof(ProblemState PS) {
        ArrayList<Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>>> history = PS.History;
        ArrayList<Pair<ArrayList<String>, ArrayList<String>>> proof = new ArrayList<>();

        HashSet<Term> curSuccProblem = PS.succProblem;
        if(history.size() == 0) {
            return new Proof(curSuccProblem.iterator().next().Print());
        }

        ArrayList<Proof> cur = new ArrayList<>();
        for(int ind = history.size() - 1; ind >= 0; ind--) {
            Pair<Pair<ArrayList<String>, String>, Pair<ArrayList<Pair<String, Term>>, Pair<ArrayList<Term>, Term>>> laststep = history.get(ind);
            Pair<ArrayList<Term>, Term> rule = laststep.second.second;
            ArrayList<Term> anteSideApply = new ArrayList<>();
            Term succSideApply = rule.second.Dup();
            for (Pair<String, Term> s : laststep.second.first) {
                succSideApply = succSideApply.replace(new Const(s.first), s.second);
            }
            HashSet<Term> newSuccProblem = new HashSet<>();
            for (Pair<String, Term> s : laststep.second.first)
                succSideApply = succSideApply.replace(new Const(s.first), s.second);
            newSuccProblem.add(succSideApply);

            //make a new proof with the formula that we just derived
            Proof der = new Proof(succSideApply.Print());
            der.setFinished(true);

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

            //find all the antecedents that were used in our derivation
            for (Term s : anteSideApply){
                boolean availabe = false;
                Proof instance = null;
                for (Proof t : cur) {
                    if (t.formula.compareTo(s.Print()) == 0) {
                        availabe = true;
                        der.addAntecedent(t);
                        instance = t;
                    }
                }
                if(!availabe) {
                    Proof underived = new Proof(s.Print());
                    underived.setFinished(false);
                    der.addAntecedent(underived);
                } else {
                    cur.remove(instance);
                }
            }
            cur.add(der);
            curSuccProblem = newSuccProblem;
        }
        return cur.get(0);
    }

    static Pair<Bitmap, Pair<Float, Float>> drawText(String ax) {
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTextSize(48);
        paint.getTextBounds(ax, 0, ax.length(), bounds);
        Rect bounds1 = new Rect();
        String test = "`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?";
        paint.getTextBounds(test, 0, test.length(), bounds1);

        Bitmap bm = Bitmap.createBitmap(bounds.left + bounds.width(), bounds1.bottom + bounds1.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(ax, 0, bounds1.height(), paint);
        return Pair.create(bm, Pair.create(0f, (float) (bounds.width() + bounds.left)));
    }

    static Pair<Bitmap, Pair<Float, Float>> drawAxiom(String ax) {
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTextSize(48);
        paint.getTextBounds(ax, 0, ax.length(), bounds);
        Rect bounds1 = new Rect();
        String test = "`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?";
        paint.getTextBounds(test, 0, test.length(), bounds1);

        Bitmap bm = Bitmap.createBitmap(bounds.left + bounds.width(), bounds1.bottom + bounds1.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        canvas.drawText(ax, 0, bounds1.height(), paint);
        Pair<Bitmap, Pair<Float, Float>> empty = Pair.create(bm, Pair.create(0f, (float) (bounds.width() + bounds.left)));
        return drawUnaryInference(empty, ax);
    }

    @SuppressWarnings("ConstantConditions")
    static Pair<Bitmap, Pair<Float, Float>> drawUnaryInference(Pair<Bitmap, Pair<Float, Float>> proof, String derived) {
        Bitmap der = drawText(derived).first;

        Bitmap bmOld = proof.first;

        //get the size of the new proof
        float middle = proof.second.first + (proof.second.second - proof.second.first) / 2f;
        float offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
        float offsetRight = Math.abs(Math.min(0, bmOld.getWidth() - (middle + der.getWidth() / 2f)));

        //create Bitmap which fits everything
        int width = Math.round(offsetLeft) + bmOld.getWidth() + Math.round(offsetRight);
        int height = bmOld.getHeight() + der.getHeight();
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //paint it white
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        //add the two previous proofs
        canvas.drawBitmap(bmOld, offsetLeft, 0, null);

        float startLine;
        float endLine;
        if (offsetLeft > 0) {
            startLine = 0;
            endLine = der.getWidth();
        } else {
            startLine = Math.min(proof.second.first, middle - der.getWidth() / 2f);
            endLine = Math.max(proof.second.second, middle + der.getWidth() / 2f);
        }

        //the consequence should be centered with respect to the line
        float startCons = startLine + (endLine - startLine) / 2f - der.getWidth() / 2f;
        float endCons = startCons + der.getWidth();


        //add the consequence
        int heightCons = bmOld.getHeight();
        canvas.drawBitmap(der, startCons, heightCons, null);

        //draw the black line
        paint.setColor(Color.BLACK);
        canvas.drawLine(startLine, heightCons, endLine, heightCons, paint);

        return Pair.create(bm, Pair.create(startCons, endCons));
    }

    @SuppressWarnings("ConstantConditions")
    static Pair<Bitmap, Pair<Float, Float>> drawBinaryInference(Pair<Bitmap, Pair<Float, Float>> proofLeft, Pair<Bitmap, Pair<Float, Float>> proofRight, String derived) {
        Bitmap der = drawText(derived).first;

        Bitmap bmLeft = proofLeft.first;
        Bitmap bmRight = proofRight.first;

        //get the size of the new proof
        float middle = proofLeft.second.first + (bmLeft.getWidth() + 50 + proofRight.second.second - proofLeft.second.first) / 2f;
        float offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
        float offsetRight = Math.abs(Math.min(0, bmLeft.getWidth() + 50 + bmRight.getWidth() - (middle + der.getWidth() / 2f)));

        //create Bitmap which fits everything
        int width = Math.round(offsetLeft) + bmLeft.getWidth() + 50 + bmRight.getWidth() + Math.round(offsetRight);
        int height = Math.max(bmLeft.getHeight(), bmRight.getHeight()) + der.getHeight();
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //paint it white
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        //add the two previous proofs
        int topLeft = Math.max(bmLeft.getHeight(), bmRight.getHeight()) - bmLeft.getHeight();
        int topRight = Math.max(bmRight.getHeight(), bmLeft.getHeight()) - bmRight.getHeight();
        canvas.drawBitmap(bmLeft, offsetLeft, topLeft, null);
        canvas.drawBitmap(bmRight, offsetLeft + bmLeft.getWidth() + 50, topRight, null);

        float startLine;
        float endLine;
        if (offsetLeft > 0) {
            startLine = 0;
            endLine = der.getWidth();
        } else {
            startLine = Math.min(proofLeft.second.first, middle - der.getWidth() / 2f);
            endLine = Math.max(bmLeft.getWidth() + 50 + proofRight.second.second, middle + der.getWidth() / 2f);
        }

        //the consequence should be centered with respect to the line
        float startCons = startLine + (endLine - startLine) / 2f - der.getWidth() / 2f;
        float endCons = startCons + der.getWidth();


        //add the consequence
        int heightCons = Math.max(bmLeft.getHeight(), bmRight.getHeight());
        canvas.drawBitmap(der, startCons, heightCons, null);

        //draw the black line
        paint.setColor(Color.BLACK);
        canvas.drawLine(startLine, heightCons, endLine, heightCons, paint);

        return Pair.create(bm, Pair.create(startCons, endCons));
    }

    @SuppressWarnings("ConstantConditions")
    static Pair<Bitmap, Pair<Float, Float>> drawNaryInference(ArrayList<Pair<Bitmap, Pair<Float, Float>>> proofs, String derived) {
        assert(proofs.size() > 2);
        Bitmap der = drawText(derived).first;


        //get the size of the new proof
        float left = proofs.get(0).second.first;
        int lineLength = 0;
        int height = proofs.get(proofs.size() - 1).first.getHeight();
        for(int i = 0; i < proofs.size() - 1; i++) {
            lineLength += proofs.get(i).first.getWidth();
            height = Math.max(height,proofs.get(i).first.getHeight());
        }
        lineLength += (proofs.size() - 1) * 50;
        float right = lineLength + proofs.get(proofs.size() - 1).second.second;
        float middle = left + (right - left)/2f;
        float offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
        int totalSize = lineLength + proofs.get(proofs.size() - 1).first.getWidth();
        float offsetRight = Math.abs(Math.min(0, totalSize - (middle + der.getWidth() / 2f)));

        //create Bitmap which fits everything
        int width = Math.round(offsetLeft) + totalSize + Math.round(offsetRight);
        Bitmap bm = Bitmap.createBitmap(width, height + der.getHeight(), Bitmap.Config.ARGB_8888);
        //paint it white
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        //add the previous proofs
        float currentOffset = offsetLeft;
        for(int i = 0; i < proofs.size(); i++) {
            Bitmap toDraw = proofs.get(i).first;
            int top = height - toDraw.getHeight();
            canvas.drawBitmap(toDraw, currentOffset, top, null);
            currentOffset += toDraw.getWidth() + 50;
        }

        float startLine;
        float endLine;
        if (offsetLeft > 0) {
            startLine = 0;
            endLine = der.getWidth();
        } else {
            startLine = Math.min(left, middle - der.getWidth() / 2f);
            endLine = Math.max(right, middle + der.getWidth() / 2f);
        }

        //the consequence should be centered with respect to the line
        float startCons = startLine + (endLine - startLine) / 2f - der.getWidth() / 2f;
        float endCons = startCons + der.getWidth();


        //add the consequence
        int heightCons = height;
        canvas.drawBitmap(der, startCons, heightCons, null);

        //draw the black line
        paint.setColor(Color.BLACK);
        canvas.drawLine(startLine, heightCons, endLine, heightCons, paint);

        return Pair.create(bm, Pair.create(startCons, endCons));
    }
}
