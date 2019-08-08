package com.example.axolotltouch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import static com.example.axolotltouch.TermHelper.TermMatchWithVar;

public abstract class AxolotlSupportingListenersAndMethods extends AppCompatActivity {

    /**
     * This is the top most class in the hierarchy and thus contains this essential field which may
     * be found in every activity.
     */
    ProblemState PS;

    /**
     * This method is meant to aid the switching of layouts which occur at the whim of the user.
     *
     * @author David M. Cerna
     */
    abstract protected void switchDisplay();

    /**
     * This method is meant to aid the drawing of a rule chosen by the user.
     * @author David M. Cerna
     */
    protected abstract void drawRule();

    /**
     * This method is meant to aid the drawing of a rule post instantiation as chosen by the user.
     * @author David M. Cerna
     */
    protected abstract void drawRuleFromSelection();

    /**
     * This method is meant to implement the resulting changes of a left swipe gesture. The method
     * is used by the backpress button.
     * @author David M. Cerna
     */
    protected abstract boolean implementationOfSwipeLeft();

    /**
     * This method is meant to draw proofs.
     *
     * @author Rafael Kiesl
     */
    protected abstract void drawBitmap(Bitmap first);


    /**
     * Allows one to output Latex code.
     *
     * @author Rafael Kiesl
     */
    protected abstract void copyLatexToClipboard();


    /**
     * Saves proof as image.
     *
     * @author Rafael Kiesl
     */
    protected abstract void saveProof() throws IOException;

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
        if (AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RulePrettyDisplay) != null) {
            AxolotlSupportingListenersAndMethods.this.PS.ActivityMode = 2;
            switchDisplay();
        } else if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
        } else implementationOfSwipeLeft();
    }

    /**
     * This class implements View.OnClickListener specifically for the selection of terms
     * wwithin the problem statement
     *
     * @author David M. Cerna
     */
    protected class SideSelectionListener implements View.OnClickListener {
        /**
         * When a text view containing a term from the problem state is clicked on
         * the contrast between the term and the background is changed with respect to the text
         * color.
         * @author David M. Cerna
         * @param view The text view containing the term of the problem state
         */
        @Override
        public void onClick(View view) {
            if (view instanceof TextView && isMemberOf((TextView) view, (LinearLayout) AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RightSideTermLayout))) {
                cleanSlection((LinearLayout) AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RightSideTermLayout));
                textViewSelected((TextView) view);
                AxolotlSupportingListenersAndMethods.this.PS.selectedPosition = ((TextView) view).getText().toString();
            }
            if (AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RulePrettyDisplay) != null) {
                Term succTerm = ProblemState.getTermByString(PS.selectedPosition, PS.problem);
                if (TermHelper.wellformedSequents(succTerm) && TermHelper.wellformedSequents(PS.currentRule.argument)) {
                    succTerm.normalize(PS.Variables);
                    PS.currentRule.argument.normalize(PS.Variables);
                }
                if (succTerm != null && TermMatchWithVar(succTerm, PS.currentRule.argument, PS.Variables)) {
                    drawRuleFromSelection();
                } else if (PS.selectedPosition.compareTo(Const.Empty.getSym()) == 0) {
                    drawRule();
                } else
                    Toast.makeText(AxolotlSupportingListenersAndMethods.this, "Rule not applicable", Toast.LENGTH_SHORT).show();

            }
        }


        /**
         * This method returns all text views within a given layout to the default contrast settings.
         * @author David M. Cerna
         * @param side The layout which will be reset to default contrast settings.
         */
        private void cleanSlection(LinearLayout side) {
            int size = side.getChildCount();
            for (int i = 0; i < size; i++)
                textViewUnselected(((TextView) ((LinearLayout) ((HorizontalScrollView) side.getChildAt(i)).getChildAt(0)).getChildAt(0)));
            AxolotlSupportingListenersAndMethods.this.PS.selectedPosition = "";
        }

        /**
         * Checks is a given text view is a child of the given layout
         * @author David M. Cerna
         * @param view The view selected by the user.
         * @param side The layout which will be reset to default contrast settings.
         */
        private boolean isMemberOf(TextView view, LinearLayout side) {
            int size = side.getChildCount();
            for (int i = 0; i < size; i++) {
                String value = ((TextView) ((LinearLayout) ((HorizontalScrollView) side.getChildAt(i)).getChildAt(0)).getChildAt(0)).getText().toString();
                if (value.compareTo(view.getText().toString()) == 0) return true;
            }
            return false;

        }
    }

    /**
     * Many of the activities of Axolotl require implementation of a left and right swipe listener. This class
     * implements View.OnTouchListener and defines the swipe gesture to be used by the other activities.
     * @author David M. Cerna
     */
    protected abstract class OnSwipeTouchListener implements View.OnTouchListener {
        /**
         * Required for capturing the swipe gesture.
         */
        private final GestureDetector gestureDetector;

        /**
         * Constructor for the OnSwipeTouchListener class instantiating the gesture detector.
         * @author David M. Cerna
         * @param context the activity associated with this listener.
         */
        OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }


        boolean onSwipeLeft() {
            return implementationOfSwipeLeft();
        }

        public abstract boolean onSwipeRight();

        /**
         * When an event activites the listener we state a touch event to be handled by our
         * gestureDetector.
         * @author David M. Cerna
         * @param v The view which resulted in the event
         * @param event The motion event captured by the listener
         * @return Whether the listener completely captured all motion
         */
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        /**
         * This listener is written specifically to capture the fling motion which activates either
         * the swipe left or swipe right motion required by many of the activities of Axolotl.
         * @author David M. Cerna
         */
        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final int SWIPE_DISTANCE_THRESHOLD = 75;
            private static final int SWIPE_VELOCITY_THRESHOLD = 1000;

            /**
             * Captures a flinging motion based on a set distance and velocity.
             * @author David M. Cerna
             * @param e1 The start of fling motion event.
             * @param e2 The end of fling motion event.
             * @param velocityX The starting velocity
             * @param velocityY The ending velocity
             * @return returns left or right swipe depending on the fling motion or returns false
             *         implying that the motion event was not a fling.
             */
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

    /**
     * The AXolotl navigation menu contains a switch which controls which activities are activated when
     * solving a problem. This listener updates the state of the switch.
     * @author David M. Cerna
     */
    protected class ObservationListener implements CompoundButton.OnCheckedChangeListener {
        /**
         * Updates the state of the observe boolean contain within the activity's problem state.
         * @author David M. Cerna
         * @param buttonView The view requesting the change.
         * @param isChecked The value of the change.
         */
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            AxolotlSupportingListenersAndMethods.this.PS.observe = isChecked;
        }
    }

    /**
     * This class implements View.OnClickListener specifically for the selection of pairs of terms
     * within the rule list.
     * @author David M. Cerna
     */
    protected class RuleSelectionListener implements View.OnClickListener {
        /**
         * When a text view containing the printed version of a pair of terms from the rule list is clicked on
         * the contrast between the printed pair of terms  and the background is changed with respect to the text
         * color.
         * @author David M. Cerna
         * @param view The text view containing the term of the problem state
         */
        @Override
        public void onClick(View view) {
            LinearLayout rlvv = AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RuleListVerticalLayout);
            for (int i = 0; i < rlvv.getChildCount(); i++) {
                TextView theText = ((TextView) ((LinearLayout) ((HorizontalScrollView) rlvv.getChildAt(i)).getChildAt(0)).getChildAt(0));
                if (theText.getText().toString().compareTo(((TextView) view).getText().toString()) == 0) {
                    AxolotlSupportingListenersAndMethods.this.PS.currentRule = new Rule(AxolotlSupportingListenersAndMethods.this.PS.Rules.get(i));
                    textViewSelected(((TextView) view));
                } else textViewUnselected(theText);
            }
        }
    }

    /**
     * This class implements View.OnClickListener and is designed to allow selection of problems from the list
     * of problems stored in the assets. Note that each listener is associated with a particular set of problems
     * from the assets and store the problems in string form along with the directory where they may be found.
     * @author David M. Cerna
     */
    class ProblemSelectionListener implements View.OnClickListener {
        private String[] problems;
        private ArrayList<String> parsedProblems;
        private String dir;
        private int textsize;
        private boolean observe;

        /**
         * The constructor for this listener requires additional information in order to properly apply
         * problem selection
         * @author David M. Cerna
         * @param prob The list of problems.
         * @param parProb The list of problems as printed terms.
         * @param direct the directory from the assets where the problems may be found.
         */
        ProblemSelectionListener(String[] prob, ArrayList<String> parProb, String direct, int t, boolean o) {
            super();
            problems = prob;
            parsedProblems = parProb;
            dir = direct;
            textsize = t;
            observe = o;
        }

        /**
         * Once of problem has been selected a new activity is generated and the problem state
         * associated with the problem is added to the intent.
         * @author David M. Cerna
         * @param view The view displaying the printed version of the problem
         */
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
                    newPS.textSize = textsize;
                    newPS.observe = observe;
                    intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, newPS);
                    AxolotlSupportingListenersAndMethods.this.startActivity(intent);
                    AxolotlSupportingListenersAndMethods.this.finish();
                    break;
                }
            }
        }
    }

    /**
     * In order to add non-standard components to our navigation menu we built out own
     * drawer menu which uses the following onclicklistener.
     * @author David M. Cerna
     */
    protected class MenuOnClickListener implements View.OnClickListener {
        /**
         * The onclick function for our menu listener
         * @author David M. Cerna
         * @param view The view representing the menuitem
         */
        @Override
        public void onClick(View view) {
            int id = view.getId();
            Intent intent = null;
            if (id == R.id.problembuttonlayout) {
                Toast.makeText(AxolotlSupportingListenersAndMethods.this, "Problem", Toast.LENGTH_SHORT).show();
                intent = new Intent(AxolotlSupportingListenersAndMethods.this, MainActivity.class);
            } else if (id == R.id.classicbuttonlayout) {
                intent = new Intent(AxolotlSupportingListenersAndMethods.this, LogicalProblemsListActivity.class);
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMLIST, 0);
            } else if (id == R.id.TermMatchingbuttonlayout) {
                intent = new Intent(AxolotlSupportingListenersAndMethods.this, TermMatchingProblemsListActivity.class);
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMLIST, 0);
            } else if (id == R.id.nonclassicbuttonlayout) {
                intent = new Intent(AxolotlSupportingListenersAndMethods.this, LogicalProblemsListActivity.class);
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMLIST, 1);
            } else if (id == R.id.Proofbuttonlayout) {
                intent = new Intent(AxolotlSupportingListenersAndMethods.this, ProofDisplayActivity.class);
                Toast.makeText(AxolotlSupportingListenersAndMethods.this, "View Proof", Toast.LENGTH_SHORT).show();
            }
            if (intent != null) {
                intent.putExtra(AxolotlMessagingAndIO.PASSPROBLEMSTATE, PS);
                AxolotlSupportingListenersAndMethods.this.startActivity(intent);
                AxolotlSupportingListenersAndMethods.this.finish();
            }
        }
    }

    /**
     * Used for the seekbar which sets the size of the text within the app.
     * @author David M. Cerna
     */
    class TextSizeChangeListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * progress is not allowed to go below 10 being that text of the size <=10 is not readable
         * @author David M. Cerna
         * @param seekBar The text size seekbar.
         * @param progress The current position of the seekbar.
         * @param fromUser Whether the change was user activated.
         */
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PS.textSize = (progress >= 10) ? progress : 10;
        }

        /**
         * Not required for our listener.
         *
         * @param seekBar The textsize seekbar
         * @author David M. Cerna
         */
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        /**
         * What the progress is fixed we call activity decorate to change the text size
         * @author David M. Cerna
         * @param seekBar The textsize seekbar
         */
        public void onStopTrackingTouch(SeekBar seekBar) {
            ((AxolotlSupportingFunctionality) AxolotlSupportingListenersAndMethods.this).ActivityDecorate();
        }

    }

    /**
     * Used on a view to activate the pretty print layout of the activity.
     * @author David M. Cerna
     */
    protected class RuleViewListener implements View.OnLongClickListener {

        /**
         * Switches the layout after a long click input from the user
         * @param view The view which has been long clicked
         * @author David M. Cerna
         */
        @Override
        public boolean onLongClick(View view) {
            LinearLayout rlvv = AxolotlSupportingListenersAndMethods.this.findViewById(R.id.RuleListVerticalLayout);
            for (int i = 0; i < rlvv.getChildCount(); i++) {
                TextView theText = ((TextView) ((LinearLayout) ((HorizontalScrollView) rlvv.getChildAt(i)).getChildAt(0)).getChildAt(0));
                if (theText.getText().toString().compareTo(((TextView) view).getText().toString()) == 0) {
                    AxolotlSupportingListenersAndMethods.this.PS.currentRule = new Rule(AxolotlSupportingListenersAndMethods.this.PS.Rules.get(i));
                    textViewSelected(((TextView) view));
                } else textViewUnselected(theText);
            }
            AxolotlSupportingListenersAndMethods.this.PS.ActivityMode = 1;
            PS.selectedPosition = Const.Empty.getSym();
            switchDisplay();
            return false;
        }
    }

    /**
     * Returns to activity after activating the pretty print layout.
     * @author David M. Cerna
     */
    protected class BackButtonListener implements View.OnClickListener {
        /**
         * Changes the mode of the activity after the back button is pressed.
         * @param view The back button.
         * @author David M. Cerna
         */
        @Override
        public void onClick(View view) {
            AxolotlSupportingListenersAndMethods.this.PS.ActivityMode = 2;
            switchDisplay();
        }
    }


}
