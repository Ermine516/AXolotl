package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.IOException;
import java.util.ArrayList;

public abstract class AxolotlSupportingListenersAndMethods extends AppCompatActivity {
    /**
     * This is the top most class in the hierarchy and thus contains this essential field which may
     * be found in every activity.
     */
    ProblemState PS;

    /**
     * When a text view is selected, the colors ought to change in a high contrast way.
     *
     * @param v The associated text view.
     * @author David M. Cerna
     */
    public void textViewSelected(TextView v) {
        v.setBackgroundColor(Color.BLACK);
        v.setTextColor(Color.WHITE);
    }

    /**
     * This method undoes the high contrast color change.
     * @author David M. Cerna
     * @param v The associated text view.
     */
    public void textViewUnselected(TextView v) {
        v.setBackgroundColor(Color.WHITE);
        v.setTextColor(Color.BLACK);
    }

    /**
     * Checks if the text view has not been selected
     * @author David M. Cerna
     * @param v The associated text view.
     */
    public boolean isNotSelected(TextView v) {
        return ((ColorDrawable) v.getBackground()).getColor() != Color.BLACK;
    }

    /**
     * Inflates the option menu.
     * @author David M. Cerna
     * @param menu The menu to be inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflowmenu, menu);
        return true;
    }

    /**
     * Handles the selection of items within the option menu.
     * @author David M. Cerna
     * @param item The selected item.
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AxolotlMessagingAndIO.OverflowMenuSelected(item.getItemId(), this);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles the closing of the navigation draw.
     * @author David M. Cerna
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.Drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();

    }

    protected class SideSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view instanceof TextView && isMemberOf((TextView) view, (LinearLayout) AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RightSideTermLayout))) {
                Cleanslection((LinearLayout) AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RightSideTermLayout));
                textViewSelected((TextView) view);
                AxolotlSupportingListenersAndMethods.this.PS.succSelectedPosition = ((TextView) view).getText().toString();
            } else if (view instanceof TextView && isMemberOf((TextView) view, (LinearLayout) AxolotlSupportingListenersAndMethods.this.findViewById(R.id.LeftSideTermLayout))) {
                Cleanslection((LinearLayout) AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RightSideTermLayout));
                if (isNotSelected((TextView) view)) {
                    textViewSelected((TextView) view);
                    AxolotlSupportingListenersAndMethods.this.PS.anteSelectedPositions.add(((TextView) view).getText().toString());
                } else {
                    textViewUnselected((TextView) view);
                    AxolotlSupportingListenersAndMethods.this.PS.anteSelectedPositions.remove(((TextView) view).getText().toString());
                }
            }
        }

        private void Cleanslection(LinearLayout side) {
            int size = side.getChildCount();
            for (int i = 0; i < size; i++)
                textViewUnselected(((TextView) ((LinearLayout) ((HorizontalScrollView) side.getChildAt(i)).getChildAt(0)).getChildAt(0)));
            AxolotlSupportingListenersAndMethods.this.PS.succSelectedPosition = "";
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
            AxolotlSupportingListenersAndMethods.this.PS.observe = isChecked;
        }
    }

    protected class RuleSelectionListener implements View.OnClickListener {

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onClick(View view) {
            LinearLayout rlvv = AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RuleListVerticalLayout);
            for (int i = 0; i < rlvv.getChildCount(); i++) {
                TextView theText = ((TextView) ((LinearLayout) ((HorizontalScrollView) rlvv.getChildAt(i)).getChildAt(0)).getChildAt(0));
                if (theText.getText().toString().compareTo(((TextView) view).getText().toString()) == 0) {
                    AxolotlSupportingListenersAndMethods.this.PS.anteCurrentRule = AxolotlSupportingListenersAndMethods.this.PS.Rules.get(i).second.first;
                    AxolotlSupportingListenersAndMethods.this.PS.succCurrentRule = AxolotlSupportingListenersAndMethods.this.PS.Rules.get(i).second.second;
                    textViewSelected(((TextView) view));
                } else textViewUnselected(theText);
            }
        }
    }

    protected class ProblemSelectionListener implements View.OnClickListener {
        private String[] problems;
        private ArrayList<String> parsedProblems;
        private String dir;

        ProblemSelectionListener(String[] prob, ArrayList<String> parProb, String direct) {
            super();
            problems = prob;
            parsedProblems = parProb;
            dir = direct;
        }

        @Override
        public void onClick(View view) {
            ProblemState newPS;
            TextView text = ((TextView) view);
            for (int i = 0; i < parsedProblems.size(); i++) {
                if (text.getText().toString().compareTo(parsedProblems.get(i)) == 0) {
                    try {
                        newPS = AxolotlMessagingAndIO.loadFile(AxolotlSupportingListenersAndMethods.this.getAssets().open(dir + "/" + problems[i]), problems[i], AxolotlSupportingListenersAndMethods.this);
                    } catch (IOException e) {
                        break;
                    }
                    Intent intent = new Intent(AxolotlSupportingListenersAndMethods.this, MainActivity.class);
                    newPS.mainActivityState = 0;
                    intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, newPS);
                    AxolotlSupportingListenersAndMethods.this.startActivity(intent);
                    AxolotlSupportingListenersAndMethods.this.finish();
                    break;
                }
            }
        }
    }

    protected class MenuOnClickListener implements View.OnClickListener {
        Context OwningActivity;

        MenuOnClickListener(Context oA) {
            super();
            OwningActivity = oA;
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            Intent intent = null;
            if (id == R.id.problembuttonlayout) {
                Toast.makeText(OwningActivity, "Problem", Toast.LENGTH_SHORT).show();
                intent = new Intent(OwningActivity, MainActivity.class);
            } else if (id == R.id.classicbuttonlayout) {
                intent = new Intent(OwningActivity, ClassicalProblemsListActivity.class);
            } else if (id == R.id.TermMatchingbuttonlayout) {
                intent = new Intent(OwningActivity, TermMatchingProblemsListActivity.class);
            } else if (id == R.id.nonclassicbuttonlayout) {
                intent = new Intent(OwningActivity, NonClassicalProblemsListActivity.class);
            } else if (id == R.id.Proofbuttonlayout) {
                intent = new Intent(OwningActivity, ProofDisplayActivity.class);
                Toast.makeText(OwningActivity, "View Proof", Toast.LENGTH_SHORT).show();
            }
            if (intent != null) {
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, PS);
                OwningActivity.startActivity(intent);
                ((AxolotlSupportingListenersAndMethods) OwningActivity).finish();
            }
        }
    }

    class textsizechangeListener implements SeekBar.OnSeekBarChangeListener {
        Context OwningActivity;

        textsizechangeListener(Context oA) {
            super();
            OwningActivity = oA;
        }

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            PS.textSize = (progress >= 10) ? progress : 10;
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            ((AxolotlSupportingFunctionality) OwningActivity).ActivityDecorate();
        }

    }
}
