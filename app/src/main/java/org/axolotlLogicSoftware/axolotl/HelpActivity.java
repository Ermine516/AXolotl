package org.axolotlLogicSoftware.axolotl;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;


public class HelpActivity extends AxolotlSupportingFunctionality {
    private Integer helpPage = 1;

    @Override
    public void onConfigurationChanged(Configuration newconfig) {
        super.onConfigurationChanged(newconfig);
        onInternalChange();
    }
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
            findViewById(R.id.ScrollHelpText).setOnTouchListener(new HelpActivity.HelpSwipeListener(this));
            findViewById(R.id.ScrollHelpText).setOnClickListener(new View.OnClickListener() {
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
            String message = getApplicationContext().getString(R.string.helpCounterString, helpPage.toString());
            ((TextView) findViewById(R.id.countertext)).setText(message);
            findViewById(R.id.ScrollHelpText).setScrollY(0);

        }

    protected void switchDisplay() {
    }

    @Override
    protected boolean implementationOfSwipeLeft() {
        if (helpPage > 1) {
            helpPage--;
            ActivityDecorate();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, PS);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
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
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                HelpActivity.this.finish();
            }
            return true;
        }
    }

    protected void onInternalChange() {
        setContentView(R.layout.app_help_bar_layout);
        findViewById(R.id.OuterLayout).setOnTouchListener(new HelpActivity.HelpSwipeListener(this));
        findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        findViewById(R.id.ScrollHelpText).setOnTouchListener(new HelpActivity.HelpSwipeListener(this));
        findViewById(R.id.ScrollHelpText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.Drawer);
        addMenulisteners();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        switcher = findViewById(R.id.observeswitchformenu);
        switcher.setChecked(PS.observe);
        switcher.setOnCheckedChangeListener(new ObservationListener());
        seeker = findViewById(R.id.Adjusttextseeker);
        seeker.setProgress(PS.textSize);
        ActivityDecorate();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable("ProblemState", PS);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        PS = in.getParcelable("ProblemState");
    }
}
