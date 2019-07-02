package com.example.axolotltouch;


import android.os.Bundle;


public class SubstitutionSelectionActivity extends DisplayUpdateHelper {
    private ProblemState PS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_sub_select_bar_layout);
        PS = ConstructActivity();
        ActivityDecorate();
    }

    void setPS(ProblemState ps){ PS = ps; }
    ProblemState getPS(){ return PS;}
    protected void ActivityDecorate() {
        UpdateProblemDisplay();
        UpdateRuleDisplay();
        SubstitutionDisplayUpdate();
    }

}