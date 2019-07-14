package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.example.axolotltouch.AuxFunctionality.PASSPROBLEMSTATE;

public abstract class DisplayUpdateHelper extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    ProblemState PS;
    Switch switcher;
     protected abstract void ActivityDecorate();

    public void textViewSelected(TextView v) {
        v.setBackgroundColor(Color.BLACK);
        v.setTextColor(Color.WHITE);
    }

    public void textViewUnselected(TextView v) {
        v.setBackgroundColor(Color.WHITE);
        v.setTextColor(Color.BLACK);
    }

    public boolean isNotSelected(TextView v) {
        if (((ColorDrawable) v.getBackground()).getColor() == Color.BLACK) return false;
        else return true;
    }


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
        switcher = MenuItemCompat.getActionView(menu.findItem(R.id.observeswitch)).findViewById(R.id.observeswitchformenu);
        switcher.setChecked(PS.observe);
        switcher.setOnCheckedChangeListener(new ObservationListener());
        return PS;
     }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflowmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AuxFunctionality.OverflowMenuSelected(item.getItemId(),this);
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.Drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();

    }

    protected void swipeRightProblemStateUpdate() {
        PS.History.add(new Pair<>(PS.selectedSide, new Pair<>(PS.Substitutions, new Pair<>(PS.anteCurrentRule[0].Dup(), PS.anteCurrentRule[1].Dup()))));
        int anti = (PS.selectedSide == 1) ? 0 : 1;
        Term temp = PS.anteCurrentRule[anti].Dup();
        for (Pair<String, Term> s : PS.Substitutions)
            temp = temp.replace(new Const(s.first), s.second);
        PS.anteProblem[PS.selectedSide] = temp;
        PS.anteSelectedPositions = new ArrayList<>();
        PS.succSelectedPosition = "";
        PS.subPos = -1;
        PS.anteCurrentRule = new Term[]{Const.HoleSelected, Const.HoleSelected};
        PS.Substitutions = new ArrayList<>();
        PS.SubHistory = new HashMap<>();
        if (PS.anteProblem[0].Print().compareTo(PS.anteProblem[1].Print()) == 0)
            Toast.makeText(DisplayUpdateHelper.this, "Congratulations! Problem Solved! ", Toast.LENGTH_SHORT).show();
        else Toast.makeText(DisplayUpdateHelper.this, "Rule Applied", Toast.LENGTH_SHORT).show();
    }

    @Override
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
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        AuxFunctionality.SideMenuItems(item.getItemId(), this, PS);
        DrawerLayout drawer = findViewById(R.id.Drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected abstract class OnSwipeTouchListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector;

        OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public abstract boolean onSwipeLeft();

        public abstract boolean onSwipeRight();

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final int SWIPE_DISTANCE_THRESHOLD = 75;
            private static final int SWIPE_VELOCITY_THRESHOLD = 1000;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX(), distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
                    if (distanceX > 0) return onSwipeRight();
                    else return onSwipeLeft();
                else return false;
            }
        }
    }

    protected class ObservationListener implements CompoundButton.OnCheckedChangeListener {
        ObservationListener() {
            super();
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DisplayUpdateHelper.this.PS.observe = isChecked;
        }
    }

}
