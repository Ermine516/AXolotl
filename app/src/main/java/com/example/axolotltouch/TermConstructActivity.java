package com.example.axolotltouch;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class TermConstructActivity extends DisplayUpdateHelper  {
    private ProblemState PS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_term_bar_layout);
        FloatingActionButton fab = findViewById(R.id.UndoButtonTerm);
        fab.setOnTouchListener(new OnTouchHapticListener());
        fab.setOnClickListener(new UndoSubstitutionListener());
        PS = ConstructActivity();
        ActivityDecorate();
    }

    void setPS(ProblemState ps){ PS = ps; }
    ProblemState getPS(){ return PS;}
    protected void ActivityDecorate() {
            UpdateProblemDisplay();
            UpdateTermDisplay();
            TermDisplayUpdate();
    }


}
