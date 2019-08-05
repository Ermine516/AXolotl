package com.example.axolotltouch;

import android.os.Bundle;

public class AboutActivity extends AxolotlSupportingFunctionality {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_bar_layout);
        PS = ConstructActivity(savedInstanceState);
        ActivityDecorate();
    }

    protected void ActivityDecorate() {}

    protected void switchDisplay() {
    }

}