package com.example.axolotl;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class HelpActivity extends AxolotlSupportingFunctionality {
    private Integer helpPage = 1;
        @Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_help_bar_layout);
            findViewById(R.id.OuterLayout).setOnTouchListener(new HelpActivity.HelpSwipeListener(this));
            findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            PS = ConstructActivity(savedInstanceState);
            ActivityDecorate();
        }

        protected void ActivityDecorate() {
            int imageResource = (helpPage.toString().length() == 2) ? getResources().getIdentifier("drawable/screen0" + helpPage, null, getPackageName()) :
                    getResources().getIdentifier("drawable/screen00" + helpPage, null, getPackageName());
            int StringResource = (helpPage.toString().length() == 2) ? getResources().getIdentifier("string/help0" + helpPage, null, getPackageName()) :
                    getResources().getIdentifier("string/help00" + helpPage, null, getPackageName());
            Drawable image = getResources().getDrawable(imageResource, this.getTheme());
            ((ImageView) findViewById(R.id.HelpImage)).setImageDrawable(image);
            ((TextView) findViewById(R.id.HelpText)).setText(StringResource);
        }

    protected void switchDisplay() {
    }

    @Override
    protected boolean implementationOfSwipeLeft() {
        if (helpPage > 1) helpPage--;
        ActivityDecorate();
        return true;
    }

    protected class HelpSwipeListener extends OnSwipeTouchListener {
        HelpSwipeListener(Context ctx) {
            super(ctx);
        }

        public boolean onSwipeRight() {
            if (helpPage < 24) {
                helpPage++;
                ActivityDecorate();
            } else {
                Intent intent = new Intent(HelpActivity.this, AboutActivity.class);
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, HelpActivity.this.PS);
                HelpActivity.this.startActivity(intent);
                HelpActivity.this.finish();
            }
            return true;
        }
    }

    protected void onInternalChange() {
    }

}
