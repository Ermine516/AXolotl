package com.example.axolotl;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AxolotlSupportingFunctionality {

    /**
     * Implementation of the onCreate method  which reads problem state and supports
     * activity decoration.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        ActivityDecorate();
    }

    /**
     * An unnecessary method in the current implementation. Will most likely be used in
     * future updates.
     */
    protected void ActivityDecorate() {
        ((TextView) findViewById(R.id.about1text)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.about2text)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.about3text)).setMovementMethod(LinkMovementMethod.getInstance());

    }

    /**
     * An unnecessary method in the current implementation. Will most likely be used in
     * future updates.
     */
    protected void switchDisplay() {
    }

    /**
     * Currently implements the standard back press function. Will most likely be used in
     * future updates.
     */
    @Override
    protected boolean implementationOfSwipeLeft() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, PS);
        startActivity(intent);
        finish();
        return true;
    }

    /**
     * An unnecessary method in the current implementation. Will most likely be used in
     * future updates.
     */
    protected void onInternalChange() {
    }

}