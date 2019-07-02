package com.example.axolotltouch;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;



public class MainActivity extends DisplayUpdateHelper {
    private ProblemState PS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_bar_layout);
        FloatingActionButton fab = findViewById(R.id.UndoButtonTerm);
        //fab.setOnClickListener(new applyruleListner());
        fab = findViewById(R.id.ApplyButton);
        fab.setOnClickListener(new applyruleListner());
        fab.setOnTouchListener(new OnTouchHapticListener());

        PS = ConstructActivity();
        ActivityDecorate();
    }

    void setPS(ProblemState ps){ PS = ps; }
    ProblemState getPS(){ return PS;}
    protected void ActivityDecorate() {
        UpdateProblemDisplay();
        UpdateRuleDisplay();
        UpdateSelectedSubstitutionDisplay();
    }


}
