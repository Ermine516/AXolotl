package com.example.axolotltouch;

import android.os.Bundle;

public class AboutActivity extends DisplayUpdateHelper{
    private ProblemState PS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        ActivityDecorate();
    }
    void setPS(ProblemState ps){ PS = ps; }
    ProblemState getPS(){ return PS;}
    protected void ActivityDecorate() {}

}