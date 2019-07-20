package com.example.axolotltouch;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public abstract class DisplayListenerHelper extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ProblemState PS;

    public void textViewSelected(TextView v) {
        v.setBackgroundColor(Color.BLACK);
        v.setTextColor(Color.WHITE);
    }

    public void textViewUnselected(TextView v) {
        v.setBackgroundColor(Color.WHITE);
        v.setTextColor(Color.BLACK);
    }

    public boolean isNotSelected(TextView v) {
        return ((ColorDrawable) v.getBackground()).getColor() != Color.BLACK;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflowmenu, menu);
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AuxFunctionality.OverflowMenuSelected(item.getItemId(), this);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.Drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();

    }

    protected class SideSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view instanceof TextView && isMemberOf((TextView) view, (LinearLayout) DisplayListenerHelper.this.findViewById(R.id.RightSideTermLayout))) {
                Cleanslection((LinearLayout) DisplayListenerHelper.this.findViewById(R.id.LeftSideTermLayout), false);
                Cleanslection((LinearLayout) DisplayListenerHelper.this.findViewById(R.id.RightSideTermLayout), true);
                textViewSelected((TextView) view);
                DisplayListenerHelper.this.PS.succSelectedPosition = ((TextView) view).getText().toString();
            } else if (view instanceof TextView && isMemberOf((TextView) view, (LinearLayout) DisplayListenerHelper.this.findViewById(R.id.LeftSideTermLayout))) {
                Cleanslection((LinearLayout) DisplayListenerHelper.this.findViewById(R.id.RightSideTermLayout), true);
                if (isNotSelected((TextView) view)) {
                    textViewSelected((TextView) view);
                    DisplayListenerHelper.this.PS.anteSelectedPositions.add(((TextView) view).getText().toString());
                } else {
                    textViewUnselected((TextView) view);
                    DisplayListenerHelper.this.PS.anteSelectedPositions.remove(((TextView) view).getText().toString());
                }
            }
        }

        private void Cleanslection(LinearLayout side, boolean leftorright) {
            int size = side.getChildCount();
            for (int i = 0; i < size; i++)
                textViewUnselected(((TextView) ((LinearLayout) ((HorizontalScrollView) side.getChildAt(i)).getChildAt(0)).getChildAt(0)));
            DisplayListenerHelper.this.PS.succSelectedPosition = "";
            if (!leftorright)
                DisplayListenerHelper.this.PS.anteSelectedPositions = new ArrayList<>();
        }

        private boolean isMemberOf(TextView view, LinearLayout side) {
            int size = side.getChildCount();
            for (int i = 0; i < size; i++) {
                String value = ((TextView) ((LinearLayout) ((HorizontalScrollView) side.getChildAt(i)).getChildAt(0)).getChildAt(0)).getText().toString();
                if (value.compareTo(view.getText().toString()) == 0) return true;
            }
            return false;

        }
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
            DisplayListenerHelper.this.PS.observe = isChecked;
        }
    }

    protected class RuleSelectionListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            LinearLayout rlvv = DisplayListenerHelper.this.findViewById(R.id.RuleListVerticalLayout);
            for (int i = 0; i < rlvv.getChildCount(); i++) {
                TextView theText = ((TextView) ((LinearLayout) ((HorizontalScrollView) rlvv.getChildAt(i)).getChildAt(0)).getChildAt(0));
                if (theText.getText().toString().compareTo(((TextView) view).getText().toString()) == 0) {
                    DisplayListenerHelper.this.PS.anteCurrentRule = DisplayListenerHelper.this.PS.Rules.get(i).first;
                    DisplayListenerHelper.this.PS.succCurrentRule = DisplayListenerHelper.this.PS.Rules.get(i).second;
                    textViewSelected(((TextView) view));
                } else textViewUnselected(theText);
            }
        }
    }

}
