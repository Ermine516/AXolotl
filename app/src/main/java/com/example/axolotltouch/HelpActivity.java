package com.example.axolotltouch;


import android.os.Bundle;


public class HelpActivity extends AxolotlSupportingFunctionality {
        @Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_help_bar_layout);
            PS = ConstructActivity(savedInstanceState);
        ActivityDecorate();
        }

        protected void ActivityDecorate() {
        }

    protected void switchDisplay() {
    }

}
