package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static com.example.axolotltouch.AuxFunctionality.PASSPROBLEMSTATE;

public abstract class DisplayUpdateHelper extends DisplayListenerHelper {
    androidx.appcompat.widget.SwitchCompat switcher;

    protected abstract void ActivityDecorate();

    protected ProblemState ConstructActivity(Bundle in) {
         Toolbar toolbar = findViewById(R.id.toolbar);
         setSupportActionBar(toolbar);
         DrawerLayout drawer = findViewById(R.id.Drawer);
         NavigationView navigationView = findViewById(R.id.nv);
         ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                 this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
         drawer.addDrawerListener(toggle);
         toggle.syncState();
         navigationView.setNavigationItemSelectedListener(this);
         Intent intent = getIntent();
        if (intent.hasExtra(PASSPROBLEMSTATE)) PS = intent.getParcelableExtra(PASSPROBLEMSTATE);
        else if (in != null && in.containsKey("ProblemState"))
            PS = in.getParcelable("ProblemState");
        else PS = new ProblemState();
        Menu menu = navigationView.getMenu();
        switcher = findViewById(R.id.observeswitchformenu);
        switcher.setChecked(PS.observe);
        switcher.setOnCheckedChangeListener(new ObservationListener());
        return PS;
     }

    protected void swipeRightProblemStateUpdate() {
        if (PS.succCurrentRule.getSym().compareTo(Const.Hole.getSym()) != 0)
            PS.History.add(new Pair<>(new Pair<>(PS.anteSelectedPositions, PS.succSelectedPosition), new Pair<>(PS.Substitutions, new Pair<>(PS.anteCurrentRule, PS.succCurrentRule.Dup()))));
        if ((PS.anteSelectedPositions.size() != 0)) {
            Term temp = PS.succCurrentRule.Dup();
            for (Pair<String, Term> s : PS.Substitutions)
                temp = temp.replace(new Const(s.first), s.second);
            HashSet<Term> newProblemAnte = new HashSet<>();
            for (Term t : PS.anteProblem) {
                boolean selectedTerm = false;
                for (String s : PS.anteSelectedPositions)
                    if (t.Print().compareTo(s) == 0) selectedTerm = true;
                if (!selectedTerm || PS.anteCurrentRule.size() == 0) newProblemAnte.add(t.Dup());
            }
            newProblemAnte.add(temp);
            PS.anteProblem = newProblemAnte;
        } else {
            ArrayList<Term> temp = new ArrayList<>();
            for (Term t : PS.anteCurrentRule) temp.add(t.Dup());
            for (Pair<String, Term> s : PS.Substitutions)
                for (int i = 0; i < temp.size(); i++)
                    temp.set(i, temp.get(i).replace(new Const(s.first), s.second));
            HashSet<Term> newProblemsucc;
            newProblemsucc = new HashSet<>();
            for (Term t : PS.succProblem)
                if (t.Print().compareTo(PS.succSelectedPosition) != 0) newProblemsucc.add(t);
            newProblemsucc.addAll(temp);
            PS.succProblem = newProblemsucc;
        }
        PS.anteSelectedPositions = new ArrayList<>();
        PS.succSelectedPosition = "";
        PS.subPos = -1;
        PS.anteCurrentRule = new ArrayList<>();
        PS.anteCurrentRule.add(Const.HoleSelected);
        PS.succCurrentRule = Const.HoleSelected;
        PS.Substitutions = new ArrayList<>();
        PS.SubHistory = new HashMap<>();
        if (PS.succProblem.isEmpty()) PS.succProblem.add(Const.Empty.Dup());
        if (PS.anteProblem.isEmpty()) PS.anteProblem.add(Const.Empty.Dup());
        if ((PS.anteProblem.containsAll(PS.succProblem) && PS.succProblem.containsAll(PS.anteProblem)))
            Toast.makeText(DisplayUpdateHelper.this, "Congratulations! Problem Solved! ", Toast.LENGTH_SHORT).show();
        else Toast.makeText(DisplayUpdateHelper.this, "Rule Applied", Toast.LENGTH_SHORT).show();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == AuxFunctionality.READ_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                    PS = AuxFunctionality.loadFile(inputStream, new File(Objects.requireNonNull(data.getData().getPath())).getName(), this);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Unable to load file", Toast.LENGTH_SHORT).show();                }
            }
            if (PS != null) ActivityDecorate();
            else {
                PS = new ProblemState();
                Toast.makeText(this, "Unable to load file", Toast.LENGTH_SHORT).show();
            }
        } else super.onActivityResult(requestCode,resultCode,data);

    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        AuxFunctionality.SideMenuItems(item.getItemId(), this, PS);
        DrawerLayout drawer = findViewById(R.id.Drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public HorizontalScrollView scrollTextSelectConstruct(String text, View.OnClickListener lis, Context ctx, boolean gravity) {
        TextView TermText = new TextView(ctx);
        TermText.setTextSize(40);
        TermText.setText(Html.fromHtml(text));
        if (gravity) TermText.setGravity(Gravity.CENTER);
        TermText.setFreezesText(true);
        TermText.setTextColor(Color.BLACK);
        TermText.setBackgroundColor(Color.WHITE);
        TermText.setLayoutParams(new FrameLayout.LayoutParams(((int) TermText.getPaint().measureText(TermText.getText().toString()) + 20), FrameLayout.LayoutParams.WRAP_CONTENT));
        if (lis != null) TermText.setOnClickListener(lis);
        LinearLayout scrollLayout = new LinearLayout(this);
        scrollLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY));//(gravity) ? Gravity.CENTER : Gravity.NO_GRAVITY));
        scrollLayout.setOrientation(LinearLayout.VERTICAL);
        scrollLayout.addView(TermText);
        HorizontalScrollView HScroll = new HorizontalScrollView(this);
        HScroll.setScrollbarFadingEnabled(false);
        HScroll.setScrollBarDefaultDelayBeforeFade(0);
        HScroll.setHorizontalScrollBarEnabled(true);
        HScroll.addView(scrollLayout);
        HScroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, (gravity) ? Gravity.CENTER : Gravity.NO_GRAVITY));

        return HScroll;
    }

    protected void updateProblemSideDisplay(LinearLayout sl, Term[] t) {
        sl.removeAllViewsInLayout();
        for (int i = 0; i < t.length; i++)
            sl.addView(scrollTextSelectConstruct(t[i].Print(), new DisplayUpdateHelper.SideSelectionListener(), this, true));
    }

    protected void updatefutureProblemSideDisplay(LinearLayout sl, Term[] t) {
        sl.removeAllViewsInLayout();
        for (int i = 0; i < t.length; i++) {
            t[i].normalize(PS.Variables);
            sl.addView(scrollTextSelectConstruct(t[i].Print(), null, this, false));
        }
    }

    protected void RuleDisplayUpdate() {
        LinearLayout RLVV = this.findViewById(R.id.RuleListVerticalLayout);
        RLVV.removeAllViewsInLayout();
        for (int i = 0; i < PS.Rules.size(); i++)
            RLVV.addView(scrollTextSelectConstruct(PS.RuleTermsToString(PS.Rules.get(i).second), new DisplayUpdateHelper.RuleSelectionListener(), this, false));
    }
}
