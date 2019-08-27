package com.example.axolotl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class Proof {

    static final int FORMULA_SIZE = 72;
    static final int LABEL_SIZE = 48;
    static final android.graphics.Bitmap.Config COLOR_CODE = Bitmap.Config.RGB_565;

    ArrayList<Proof> antecedents;
    String formula;
    String label;
    boolean finished;
    boolean drawLine = true;

    public Proof(String formula, String l) {
        antecedents = new ArrayList<>();
        this.formula = formula;
        finished = false;
        label = l;
    }

    static Proof incomplete() {
        Proof result = new Proof("?", "");
        result.setFinished(true);
        result.drawLine = false;
        return result;
    }

    void setTo(Proof proof) {
        this.antecedents = proof.antecedents;
        this.formula = proof.formula;
        this.finished = proof.finished;
        this.label = proof.label;
        this.drawLine = proof.drawLine;
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

    static Bitmap drawProblemSolution(ProblemState PS) {
        //proof
        float downscale = 1;
        Bitmap proofPic = Proof.extractProof(PS).draw().first;
        Bitmap bm1 = Bitmap.createBitmap(proofPic.getWidth() + (int) (500 * downscale), proofPic.getHeight() + (int) (500 * downscale), COLOR_CODE);
        System.gc();
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        canvas.drawBitmap(proofPic, (int)(250*downscale), (int)(250*downscale), null);

        //rules
        Bitmap[] rules = new Bitmap[PS.Rules.size()];
        for(int i = 0; i < PS.Rules.size(); i++) {
            Rule rule = PS.Rules.get(i);
            ArrayList<Proof> args = new ArrayList<>();
            for (Term t : rule.Conclusions) {
                if (TermHelper.wellformedSequents(t))
                    t.normalize(PS.Variables); // Don't forget that sequents are brittle terms
                Proof p = new Proof(t.Print(), "");
                p.drawLine = false;
                p.finished = true;
                args.add(p);
            }
            Proof p = new Proof(rule.argument.Print(), rule.Label);
            p.finished = true;
            p.antecedents = args;
            Pair<Bitmap, Pair<Float, Float>> bm = p.draw();
            rules[i]= bm.first;
        }
        Arrays.sort(rules, new Comparator<Bitmap>() {
            @Override
            public int compare(Bitmap o1, Bitmap o2) {
                return o2.getWidth() - o1.getWidth();
            }
        });
        int sideLength = (int)(Math.ceil(Math.sqrt(rules.length)));
        int[] startingPosVert = new int[sideLength];
        int[] startingPosHor = new int[sideLength];
        int curVert = (int)(250*downscale);
        int curHor = (int)(250*downscale);
        for(int i = 0; i < sideLength; i++) {
            startingPosVert[i] = curVert;
            startingPosHor[i] = curHor;
            if ((i * sideLength) < rules.length) {
                curVert += rules[i * sideLength].getWidth() + (int)(50*downscale);
            }
            int next = 0;
            for(int j = 0; j < sideLength && sideLength*j + i < rules.length; j++) {
                next = Math.max(next, rules[j*sideLength +i].getHeight());
            }
            curHor += next + (int)(50*downscale);
        }


        Bitmap result = Bitmap.createBitmap(Math.max(curVert + (int) (250 * downscale), bm1.getWidth()), curHor + bm1.getHeight(), COLOR_CODE);
        System.gc();

        canvas = new Canvas(result);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        int startProof = Math.max(curVert + (int)(250*downscale), bm1.getWidth())/2 - bm1.getWidth()/2;
        canvas.drawBitmap(bm1, startProof, curHor, null);
        int startRules = Math.max(curVert + (int)(250*downscale), bm1.getWidth())/2 - (curVert + (int)(250*downscale))/2;
        for(int i = 0; i < sideLength; i++) {
            for(int j = 0; j < sideLength && i*sideLength + j < rules.length; j++) {
                canvas.drawBitmap(rules[i*sideLength + j], startingPosVert[i] + startRules, startingPosHor[j], null);
            }
        }
        Bitmap ruleHeader = drawText("Rules", FORMULA_SIZE*2).first;
        canvas.drawBitmap(ruleHeader, (int)(50*downscale), (int)(50*downscale), null);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(FORMULA_SIZE/10);
        canvas.drawLine(0, (int)(50*downscale) + ruleHeader.getHeight(), result.getWidth(), (int)(50*downscale) + ruleHeader.getHeight(), paint);
        Bitmap proofHeader = drawText("Proof", FORMULA_SIZE*2).first;
        canvas.drawBitmap(proofHeader, (int)(50*downscale), (int)(50*downscale) + curHor, null);
        canvas.drawLine(0, (int)(50*downscale) + curHor + proofHeader.getHeight(), result.getWidth(), (int)(50*downscale) + curHor + proofHeader.getHeight(), paint);
        System.gc();

        return result;
    }

    static Proof extractProof(ProblemState PS) {
        ArrayList<State> history = PS.History;
        ArrayList<Pair<ArrayList<String>, ArrayList<String>>> proof = new ArrayList<>();

        HashSet<Term> curSuccProblem = PS.problem;
        if(history.size() == 0) {
            Term onlyOne = curSuccProblem.iterator().next();
            if (TermHelper.wellformedSequents(onlyOne))
                onlyOne.normalize(PS.Variables); // Don't forget that sequents are brittle terms
            return new Proof(onlyOne.Print(), "");
        }

        ArrayList<Proof> cur = new ArrayList<>();
        ArrayList<Proof> underivedProofs = new ArrayList<>();
        HashMap<String, Proof> map = new HashMap<>();
        for(int ind = history.size() - 1; ind >= 0; ind--) {
            State laststep = history.get(ind);
            ArrayList<Term> anteSideApply = new ArrayList<>();
            Term succSideApply = laststep.substitution.apply(laststep.rule.argument);
            HashSet<Term> newSuccProblem = new HashSet<>();
            newSuccProblem.add(succSideApply);

            //make a new proof with the formula that we just derived
            if (TermHelper.wellformedSequents(succSideApply))
                succSideApply.normalize(PS.Variables);// Don't forget that sequents are brittle terms
            Proof der = new Proof(succSideApply.Print(), laststep.rule.Label);
            der.setFinished(true);

            for (Term t : laststep.rule.Conclusions)
                anteSideApply.add(laststep.substitution.apply(t));

            for (Term t : curSuccProblem) {
                if (TermHelper.wellformedSequents(t))
                    t.normalize(PS.Variables); // Don't forget that sequents are brittle terms
                boolean wasselected = false;
                for (Term s : anteSideApply) {
                    if (TermHelper.wellformedSequents(s))
                        s.normalize(PS.Variables); // Don't forget that sequents are brittle terms
                    if (t.Print().compareTo(s.Print()) == 0) wasselected = true;
                }
                if (!wasselected) newSuccProblem.add(t);
            }

            //find all the antecedents that were used in our derivation
            ArrayList<Proof> toRemove = new ArrayList<>();
            for (Term s : anteSideApply){
                if (TermHelper.wellformedSequents(s))
                    s.normalize(PS.Variables); // Don't forget that sequents are brittle terms
                boolean availabe = false;
                Proof instance = null;
                for (Proof t : cur) {
                    if (t.formula.compareTo(s.Print()) == 0) {
                        availabe = true;
                        der.addAntecedent(t);
                        instance = t;
                        map.put(s.Print(), t);
                    }
                }
                if(!availabe) {
                    Proof underived = new Proof(s.Print(), laststep.rule.Label);
                    underived.setFinished(false);
                    underivedProofs.add(underived);
                    der.addAntecedent(underived);
                } else {
                    toRemove.add(instance);
                }
            }
            cur.removeAll(toRemove);
            cur.add(der);
            curSuccProblem = newSuccProblem;
        }
        for(Proof und: underivedProofs) {
            if(map.containsKey(und.formula)) {
                und.setTo(map.get(und.formula));
            }
        }
        return cur.get(0);
    }

    static Pair<Bitmap, Pair<Float, Float>> drawText(String ax, int size) {
        if (ax == null || ax.compareTo("") == 0) {
            return Pair.create(null, Pair.create(0f, 0f));
        }
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTextSize(size);
        paint.getTextBounds(ax, 0, ax.length(), bounds);
        Rect bounds1 = new Rect();
        String test = "`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?";
        paint.getTextBounds(test, 0, test.length(), bounds1);
        System.gc();
        Bitmap bm = Bitmap.createBitmap(bounds.left + bounds.width(), bounds1.bottom + bounds1.height(), COLOR_CODE);
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(ax, 0, bounds1.height(), paint);
        System.gc();
        return Pair.create(bm, Pair.create(0f, (float) (bounds.width() + bounds.left)));
    }

    static Pair<Bitmap, Pair<Float, Float>> drawAxiom(String ax, String label, int size) {
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTextSize(size);
        paint.getTextBounds(ax, 0, ax.length(), bounds);
        Rect bounds1 = new Rect();
        String test = "`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?";
        paint.getTextBounds(test, 0, test.length(), bounds1);
        System.gc();
        Bitmap bm = Bitmap.createBitmap(bounds.left + bounds.width(), bounds1.bottom + bounds1.height(), COLOR_CODE);
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        canvas.drawText(ax, 0, bounds1.height(), paint);
        Pair<Bitmap, Pair<Float, Float>> empty = Pair.create(bm, Pair.create(0f, (float) (bounds.width() + bounds.left)));
        System.gc();
        return drawUnaryInference(empty, label, ax);
    }

    static Pair<Bitmap, Pair<Float, Float>> drawUnaryInference(Pair<Bitmap, Pair<Float, Float>> proof, String label, String derived) {
        return drawUnaryInference(proof, derived, label, false);
    }

    static Pair<Bitmap, Pair<Float, Float>> drawBinaryInference(Pair<Bitmap, Pair<Float, Float>> proofLeft, Pair<Bitmap, Pair<Float, Float>> proofRight, String label, String derived) {
        return drawBinaryInference(proofLeft, proofRight, derived, label, false);
    }

    static Pair<Bitmap, Pair<Float, Float>> drawNaryInference(ArrayList<Pair<Bitmap, Pair<Float, Float>>> proofs, String label, String derived) {
        return drawNaryInference(proofs, derived, label, false);
    }

    @SuppressWarnings("ConstantConditions")
    static Pair<Bitmap, Pair<Float, Float>> drawUnaryInference(Pair<Bitmap, Pair<Float, Float>> proof, String derived, String label, boolean labelSide) {
        Bitmap der = drawText(derived, FORMULA_SIZE).first;
        Bitmap lab = drawText(label, LABEL_SIZE).first;

        Bitmap bmOld = proof.first;

        //get the size of the new proof
        float middle = proof.second.first + (proof.second.second - proof.second.first) / 2f;
        float offsetLeft;
        float offsetRight;
        if (label == null || label.compareTo("") == 0) {
            offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
            offsetRight = Math.abs(Math.min(0, bmOld.getWidth() - (middle + der.getWidth() / 2f)));
        } else if(labelSide) {
            offsetLeft = Math.abs(Math.min(Math.min(0, proof.second.first - lab.getWidth() - 20), middle - der.getWidth() / 2f - lab.getWidth() - 20));
            offsetRight = Math.abs(Math.min(0, bmOld.getWidth() - (middle + der.getWidth() / 2f)));
        } else {
            offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
            offsetRight = Math.abs(Math.min(Math.min(0, bmOld.getWidth() - (proof.second.second + lab.getWidth() + 20)), bmOld.getWidth() - (middle + der.getWidth() / 2f) - lab.getWidth() - 20));
        }

        //create Bitmap which fits everything
        int width = Math.round(offsetLeft) + bmOld.getWidth() + Math.round(offsetRight);
        int height = bmOld.getHeight() + der.getHeight();
        System.gc();
        Bitmap bm = Bitmap.createBitmap(width, height, COLOR_CODE);
        //paint it white
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        //add the previous proof
        canvas.drawBitmap(bmOld, offsetLeft, 0, null);


        float startLine = Math.min(proof.second.first, middle - der.getWidth() / 2f) + offsetLeft;
        float endLine = Math.max(proof.second.second, middle + der.getWidth() / 2f) + offsetLeft;

        //the consequence should be centered with respect to the line
        float startCons = startLine + (endLine - startLine) / 2f - der.getWidth() / 2f;
        float endCons = startCons + der.getWidth();


        //add the consequence
        int heightCons = bmOld.getHeight();
        canvas.drawBitmap(der, startCons, heightCons, null);

        //draw the black line
        paint.setColor(Color.BLACK);
        canvas.drawLine(startLine, heightCons, endLine, heightCons, paint);

        //draw the label
        if (label != null && label.compareTo("") != 0) {
            canvas.drawBitmap(lab, (labelSide?startLine-lab.getWidth()-10:endLine+10), heightCons - lab.getHeight()/2f, null);
        }
        System.gc();
        return Pair.create(bm, Pair.create(startCons, endCons));
    }

    @SuppressWarnings("ConstantConditions")
    static Pair<Bitmap, Pair<Float, Float>> drawBinaryInference(Pair<Bitmap, Pair<Float, Float>> proofLeft, Pair<Bitmap, Pair<Float, Float>> proofRight, String derived, String label, boolean labelSide) {
        Bitmap der = drawText(derived, FORMULA_SIZE).first;
        Bitmap lab = drawText(label, LABEL_SIZE).first;

        Bitmap bmLeft = proofLeft.first;
        Bitmap bmRight = proofRight.first;

        //get the size of the new proof
        float middle = proofLeft.second.first + (bmLeft.getWidth() + 50 + proofRight.second.second - proofLeft.second.first) / 2f;
        float offsetLeft;
        float offsetRight;
        if (label == null || label.compareTo("") == 0) {
            offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
            offsetRight = Math.abs(Math.min(0, bmLeft.getWidth() + 50 + bmRight.getWidth() - (middle + der.getWidth() / 2f)));
        } else if(labelSide) {
            offsetLeft = Math.abs(Math.min(Math.min(0, proofLeft.second.first - lab.getWidth() - 20), middle - der.getWidth() / 2f - lab.getWidth() - 20));
            offsetRight = Math.abs(Math.min(0,  bmLeft.getWidth() + 50 + bmRight.getWidth() - (middle + der.getWidth() / 2f)));
        } else {
            offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
            offsetRight = Math.abs(Math.min(Math.min(0, bmLeft.getWidth() + 50 + bmRight.getWidth() - (bmLeft.getWidth() + 50 + proofRight.second.second + lab.getWidth() + 20)),  bmLeft.getWidth() + 50 + bmRight.getWidth() - (middle + der.getWidth() / 2f) - lab.getWidth() - 20));
        }

        //create Bitmap which fits everything
        int width = Math.round(offsetLeft) + bmLeft.getWidth() + 50 + bmRight.getWidth() + Math.round(offsetRight);
        int height = Math.max(bmLeft.getHeight(), bmRight.getHeight()) + der.getHeight();
        System.gc();
        Bitmap bm = Bitmap.createBitmap(width, height, COLOR_CODE);
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

        float startLine = Math.min(proofLeft.second.first, middle - der.getWidth() / 2f) + offsetLeft;
        float endLine = Math.max(bmLeft.getWidth() + 50 + proofRight.second.second, middle + der.getWidth() / 2f) + offsetLeft;


        //the consequence should be centered with respect to the line
        float startCons = startLine + (endLine - startLine) / 2f - der.getWidth() / 2f;
        float endCons = startCons + der.getWidth();


        //add the consequence
        int heightCons = Math.max(bmLeft.getHeight(), bmRight.getHeight());
        canvas.drawBitmap(der, startCons, heightCons, null);

        //draw the black line
        paint.setColor(Color.BLACK);
        canvas.drawLine(startLine, heightCons, endLine, heightCons, paint);

        //draw the label
        if (label != null && label.compareTo("") != 0) {
            canvas.drawBitmap(lab, (labelSide?startLine-lab.getWidth()-10:endLine+10), heightCons - lab.getHeight()/2f, null);
        }
        System.gc();
        return Pair.create(bm, Pair.create(startCons, endCons));
    }

    public Pair<Bitmap, Pair<Float, Float>> draw() {
        Pair<Bitmap, Pair<Float, Float>> result;
        String internalLabel = label;
        if (!finished) {
            addAntecedent(incomplete());
            internalLabel = "";
        }
        if (isAxiom()) {
            if (drawLine) {
                result = drawAxiom(formula, internalLabel, FORMULA_SIZE);
            } else {
                result = drawText(formula, FORMULA_SIZE);
            }
        } else if (antecedents.size() == 1) {
            result = drawUnaryInference(antecedents.get(0).draw(), internalLabel, formula);
        } else if (antecedents.size() == 2) {
            result = drawBinaryInference(antecedents.get(0).draw(), antecedents.get(1).draw(), internalLabel, formula);
        } else {
            ArrayList<Pair<Bitmap, Pair<Float, Float>>> prev = new ArrayList<>();
            for (Proof antecedent : antecedents) {
                prev.add(antecedent.draw());
            }
            result = drawNaryInference(prev, internalLabel, formula);
        }
        if (!finished) {
            antecedents.remove(antecedents.size() - 1);
        }
        return result;
    }

    public String printLatex() {
        StringBuilder result = new StringBuilder();
        String internalLabel = label;
        if (!finished) {
            addAntecedent(incomplete());
            internalLabel = "";
        }
        if (isAxiom()) {
            if (drawLine) {
                result.append("\\AxiomC{ }\n");
            }
            result.append("\\RightLabel{" + Text2Latex.translate(internalLabel) + "}\n");
            result.append("\\UnaryInfC{" + Text2Latex.translate(formula) + "}\n");
        } else if (antecedents.size() == 1) {
            result.append(antecedents.get(0).printLatex());
            result.append("\\RightLabel{" + Text2Latex.translate(internalLabel) + "}\n");
            result.append("\\UnaryInfC{" + Text2Latex.translate(formula) + "}\n");
        } else if (antecedents.size() == 2) {
            result.append(antecedents.get(0).printLatex());
            result.append(antecedents.get(1).printLatex());
            result.append("\\RightLabel{" + Text2Latex.translate(internalLabel) + "}\n");
            result.append("\\BinaryInfC{" + Text2Latex.translate(formula) + "}\n");
        } else {
            for (Proof antecedent : antecedents) {
                result.append(antecedent.printLatex());
            }
            if (isAxiom() && drawLine) {
                result.append("\\AxiomC{ }\n");
            }
            result.append("\\RightLabel{" + Text2Latex.translate(internalLabel) + "}\n");
            switch (antecedents.size()) {
                case 0:
                    result.append("\\UnaryInfC{" + Text2Latex.translate(formula) + "}\n");
                    break;
                case 1:
                    result.append("\\UnaryInfC{" + Text2Latex.translate(formula) + "}\n");
                    break;
                case 2:
                    result.append("\\BinaryInfC{" + Text2Latex.translate(formula) + "}\n");
                    break;
                case 3:
                    result.append("\\TrinaryInfC{" + Text2Latex.translate(formula) + "}\n");
                    break;
                case 4:
                    result.append("\\QuaternaryInfC{" + Text2Latex.translate(formula) + "}\n");
                    break;
                case 5:
                    result.append("\\QuinaryInfC{" + Text2Latex.translate(formula) + "}\n");
                    break;
            }
        }
        if (!finished) {
            antecedents.remove(antecedents.size() - 1);
        }
        return result.toString();
    }

    @SuppressWarnings("ConstantConditions")
    static Pair<Bitmap, Pair<Float, Float>> drawNaryInference(ArrayList<Pair<Bitmap, Pair<Float, Float>>> proofs, String derived, String label, boolean labelSide) {
        assert(proofs.size() > 2);
        Bitmap der = drawText(derived, FORMULA_SIZE).first;
        Bitmap lab = drawText(label, LABEL_SIZE).first;


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
        int totalSize = lineLength + proofs.get(proofs.size() - 1).first.getWidth();


        float offsetLeft;
        float offsetRight;
        if(label == null) {
            offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
            offsetRight = Math.abs(Math.min(0, totalSize - (middle + der.getWidth() / 2f)));
        } else if(labelSide) {
            offsetLeft = Math.abs(Math.min(Math.min(0, left - lab.getWidth() - 20), middle - der.getWidth() / 2f - lab.getWidth() - 20));
            offsetRight = Math.abs(Math.min(0,  totalSize - (middle + der.getWidth() / 2f)));
        } else {
            offsetLeft = Math.abs(Math.min(0, middle - der.getWidth() / 2f));
            offsetRight = Math.abs(Math.min(Math.min(0, totalSize - (right + lab.getWidth() + 20)),  totalSize - (middle + der.getWidth() / 2f) - lab.getWidth() - 20));
        }
        //create Bitmap which fits everything
        int width = Math.round(offsetLeft) + totalSize + Math.round(offsetRight);
        System.gc();
        Bitmap bm = Bitmap.createBitmap(width, height + der.getHeight(), COLOR_CODE);
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


        float startLine = Math.min(left, middle - der.getWidth() / 2f) + offsetLeft;
        float endLine = Math.max(right, middle + der.getWidth() / 2f) + offsetLeft;

        //the consequence should be centered with respect to the line
        float startCons = startLine + (endLine - startLine) / 2f - der.getWidth() / 2f;
        float endCons = startCons + der.getWidth();


        //add the consequence
        int heightCons = height;
        canvas.drawBitmap(der, startCons, heightCons, null);

        //draw the black line
        paint.setColor(Color.BLACK);
        canvas.drawLine(startLine, heightCons, endLine, heightCons, paint);

        //draw the label
        if(label != null) {
            canvas.drawBitmap(lab, (labelSide?startLine-lab.getWidth()-10:endLine+10), heightCons - lab.getHeight()/2f, null);
        }
        System.gc();
        return Pair.create(bm, Pair.create(startCons, endCons));
    }
}
