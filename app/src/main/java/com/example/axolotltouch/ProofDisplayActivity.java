package com.example.axolotltouch;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Pair;

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class ProofDisplayActivity extends DisplayUpdateHelper{
    private ProblemState PS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_proof_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        ActivityDecorate();
    }

    void setPS(ProblemState ps){ PS = ps; }
    ProblemState getPS(){ return PS;}
    protected void ActivityDecorate() {
        Pair<Bitmap, Pair<Float, Float>> bm = drawAxiom("yyy");
        Pair<Bitmap, Pair<Float, Float>> bm1 = drawBinaryInference(bm, bm, "yHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");
        bm1 = drawBinaryInference(bm1, bm, "HHHHH");


        TouchImageView myImage = findViewById(R.id.proofViz);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm1.first.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        TileBitmapDrawable.attachTileBitmapDrawable(myImage, is, null, null);
    }

    private Pair<Bitmap, Pair<Float, Float>> drawAxiom(String ax) {
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

    private Pair<Bitmap, Pair<Float, Float>> drawBinaryInference(Pair<Bitmap, Pair<Float, Float>> proofLeft, Pair<Bitmap, Pair<Float,Float>> proofRight, String derived) {
        Bitmap der = drawAxiom(derived).first;

        Bitmap bmLeft = proofLeft.first;
        Bitmap bmRight = proofRight.first;

        //create Bitmap which fits everything
        int width = Math.max(bmLeft.getWidth() + bmRight.getWidth() + 50, der.getWidth());
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
        int topRight = Math.max(bmLeft.getHeight(), bmRight.getHeight()) - bmRight.getHeight();
        canvas.drawBitmap(bmLeft, 0, topLeft, null);
        canvas.drawBitmap(bmRight, bmLeft.getWidth() + 50, topRight, null);


        float startLine = 0;
        float endLine = width;
        float startCons = 0;
        float endCons = width;
        int heightCons = Math.max(bmLeft.getHeight(), bmRight.getHeight());
        if(width != der.getWidth()) {
            startLine = proofLeft.second.first;
            //fit some free space, the left proof, some free space and then go to the end of the consequence of the right proof
            endLine = bmLeft.getWidth() + 50 + proofRight.second.second;

            //the consequence should be centered with respect to the line
            startCons = startLine + (endLine - startLine)/2f - der.getWidth()/2f;
            endCons = startCons + der.getWidth();
        }

        //add the consequence
        canvas.drawBitmap(der, startCons, heightCons, null);

        //draw the black line
        paint.setColor(Color.BLACK);
        canvas.drawLine(startLine, heightCons, endLine, heightCons, paint);

        return  Pair.create(bm, Pair.create(startCons, endCons));
    }
}

