package com.example.axolotltouch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.axolotltouch.AuxFunctionality.PASSPROBLEMSTATE;

public abstract class DisplayUpdateHelper extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
     protected abstract void ActivityDecorate();
     abstract void setPS(ProblemState PS);
     abstract ProblemState getPS();

   protected ProblemState ConstructActivity(){
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
         if (intent.hasExtra(PASSPROBLEMSTATE)){
             return intent.getParcelableExtra(PASSPROBLEMSTATE);
         } else return new ProblemState();
     }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    protected  void UpdateProblemDisplay(){
        TextView lhs = this.findViewById(R.id.LeftSideProblem);
        TextView rhs = this.findViewById(R.id.RightSideProblem);
        lhs.setText(getPS().sSequent[0].Print());
        rhs.setText(getPS().sSequent[1].Print());
    }
    protected  void UpdateTermDisplay(){
        TextView td = this.findViewById(R.id.TermDisplay);
        td.setText(getPS().substitution.Print());
        td.setWidth(((int)td.getPaint().measureText(td.getText().toString()))+20);
    }
    protected void UpdateRuleDisplay(){
        TextView rd = this.findViewById(R.id.InstanceOfRuleDisplay);
        ArrayList<Term> temp = new ArrayList<>();
        temp.add(AuxFunctionality.ApplySubstitution(getPS().rSequent[0],getPS().selectedSubstitutions));
        temp.add(AuxFunctionality.ApplySubstitution(getPS().rSequent[1],getPS().selectedSubstitutions));
        rd.setText(AuxFunctionality.RuleTermstoString(temp,getPS()));
        rd.setWidth(((int)rd.getPaint().measureText(rd.getText().toString()))+20);
    }
    protected void UpdateSelectedSubstitutionDisplay(){
        LinearLayout RLVV = this.findViewById(R.id.SelectedSubstitutionLayout);
        RLVV.removeAllViewsInLayout();
        if(getPS().selectedSubstitutions.size()!=0){
            for(int i = 0; i< getPS().selectedSubstitutions.size();i++){
                String sub = getPS().selectedSubstitutions.get(i);
                String prunedSub = sub.substring(1,sub.length()-1);
                TextView SubText = new TextView(this);
                SubText.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
                SubText.setTextSize(32);
                SubText.setText(prunedSub);
                SubText.setPadding(40, 0, 40, 0);
                RLVV.addView(SubText);
                if(i!=getPS().selectedSubstitutions.size()-1) {
                    SubText = new TextView(this);
                    SubText.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
                    SubText.setTextSize(32);
                    SubText.setText(";");
                    SubText.setPadding(10, 0, 10, 0);
                    RLVV.addView(SubText);
                }
            }
        }

    }
    protected  void TermDisplayUpdate() {
           LinearLayout RLVV = this.findViewById(R.id.TermSelectionLayout);
           RLVV.removeAllViewsInLayout();
           for (String func : getPS().Functions.keySet()) {
               int arity = getPS().Functions.get(func);
               ArrayList<Term> args = new ArrayList<>();
               while (arity > 0) {
                   args.add(new Const(Const.Hole.getSym()));
                   arity--;
               }
               String funcText = new Func(func, args).Print();
               TextView functext = new TextView(this);
               functext.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
               functext.setTextSize(32);
               functext.setText(funcText);
               functext.setPadding(40, 0, 40, 0);
               functext.setOnClickListener(new SymbolSelectionListener());
               functext.setOnTouchListener(new OnTouchHapticListener());
               RLVV.addView(functext);
           }
           for (String cons : getPS().Constants) {
               String funcText = new Const(cons).Print();
               TextView functext = new TextView(this);
               functext.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
               functext.setTextSize(32);
               functext.setText(funcText);
               functext.setPadding(40, 0, 40, 0);
               functext.setOnClickListener(new SymbolSelectionListener());
               functext.setOnTouchListener(new OnTouchHapticListener());

               RLVV.addView(functext);
           }
   }

    protected  void SubstitutionDisplayUpdate() {
        LinearLayout RLVV = this.findViewById(R.id.SubstitutionListLayout);
        RLVV.removeAllViewsInLayout();
        for (String Var : getPS().Substitutions.keySet()) {
            ArrayList<Term> range = getPS().Substitutions.get(Var);
            for(Term t: range){
                String subText = "{"+Var+" ← "+t.Print()+"}";
                    TextView SubText = new TextView(this);
                    SubText.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
                    SubText.setTextSize(32);
                    SubText.setText(subText);
                if( getPS().selectedSubstitutions.contains(subText)) {
                    SubText.setBackgroundColor(Color.BLACK);
                    SubText.setTextColor(Color.WHITE);
                }else {
                    SubText.setBackgroundColor(Color.WHITE);
                    SubText.setTextColor(Color.BLACK);
                }
                SubText.setOnTouchListener(new OnTouchHapticListener());
                SubText.setPadding(40, 0, 40, 0);
                    SubText.setOnClickListener(new SubstitutionSelectionListener());
                RLVV.addView(SubText);

            }
        }
    }
    protected  void RuleDisplayUpdate() {
        LinearLayout RLVV = this.findViewById(R.id.RuleListVerticalLayout);
        RLVV.removeAllViewsInLayout();
        for (ArrayList<Term> rule : getPS().Rules) {
            String ruleText = AuxFunctionality.RuleTermstoString(rule, getPS());
            HorizontalScrollView ruleHScroll = new HorizontalScrollView(this);
            ruleHScroll.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
            LinearLayout ruleScrollLayout = new LinearLayout(this);
            ruleScrollLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY));
            ruleScrollLayout.setOrientation(LinearLayout.VERTICAL);
            TextView ruletext = new TextView(this);
            ruletext.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            ruletext.setTextSize(30);
            ruletext.setText(ruleText);
            ruletext.setOnTouchListener(new OnTouchHapticListener());
            ruletext.setOnClickListener(new RuleSelectionListener());
            ruleScrollLayout.addView(ruletext);
            ArrayList<Term> temp = new ArrayList<>();
            temp.add(this.getPS().rSequent[0]);
            temp.add(this.getPS().rSequent[1]);
            if(ruleText.compareTo(AuxFunctionality.RuleTermstoString(temp,getPS()))==0){
                ruletext.setBackgroundColor(Color.BLACK);
                ruletext.setTextColor(Color.WHITE);
            }
            ruleHScroll.addView(ruleScrollLayout);
            RLVV.addView(ruleHScroll);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ProblemState newPS = null;
        // Check which request we're responding to
        if (requestCode == AuxFunctionality.READ_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                    newPS = AuxFunctionality.loadFile(inputStream, new File(Objects.requireNonNull(data.getData().getPath())).getName(),this);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Unable to load file", Toast.LENGTH_SHORT).show();                }
            }
            if (newPS != null) {
                setPS(newPS);
                ActivityDecorate();

            } else Toast.makeText(this, "Unable to load file", Toast.LENGTH_SHORT).show();
        } else super.onActivityResult(requestCode,resultCode,data);

    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        AuxFunctionality.SideMenuItems(item.getItemId(), this,getPS());
        DrawerLayout drawer = findViewById(R.id.Drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @SuppressLint("ClickableViewAccessibility")
    protected void UpdateVariableDisplay(){
        LinearLayout RLVV = this.findViewById(R.id.VariableListLayout);
        RLVV.removeAllViewsInLayout();
        for (String var :getPS().Variables) {
            String funcText = new Const(var).Print();
            TextView functext = new TextView(this);
            functext.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY));
            functext.setTextSize(40);
            functext.setText(funcText);
            functext.setPadding(40,0,40,0);
            functext.setOnTouchListener(new OnTouchHapticListener());
            functext.setOnClickListener(new DisplayUpdateHelper.VariableSelectionListener());
            RLVV.addView(functext);
        }

    }

    private class VariableSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState PS = DisplayUpdateHelper.this.getPS();
            LinearLayout RLVV = DisplayUpdateHelper.this.findViewById(R.id.VariableListLayout);
            int count = RLVV.getChildCount();
            for (int i = 0; i < count; i++) {
                TextView thetext = (TextView) RLVV.getChildAt(i);
                thetext.setBackgroundColor(Color.WHITE);
                thetext.setTextColor(Color.BLACK);
            }
            view.setBackgroundColor(Color.BLACK);
            ((TextView) view).setTextColor(Color.WHITE);
            PS.selectedVariable = ((TextView) view).getText().toString();
            DisplayUpdateHelper.this.setPS(PS);
        }
    }

    private class SymbolSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState PS = DisplayUpdateHelper.this.getPS();
            Term replacement = null;
            try {
                replacement = TermHelper.parse(((TextView)view).getText().toString());
            } catch (TermHelper.FormatException e) {
                Toast.makeText(DisplayUpdateHelper.this, "Unable to add Symbol to Term", Toast.LENGTH_SHORT).show();
            }
            replacement = PS.substitution.replace(Const.HoleSelected,replacement).replaceLeft(Const.Hole,new Const(Const.HoleSelected.getSym()));
            PS.substitution= replacement;
            PS.SubHistory.add(replacement.Dup());
            DisplayUpdateHelper.this.setPS(PS);
            DisplayUpdateHelper.this.ActivityDecorate();
        }
    }
    private class SubstitutionSelectionListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState PS = DisplayUpdateHelper.this.getPS();
            int colorCode = ((ColorDrawable) view.getBackground()).getColor();
                if(colorCode == Color.BLACK){
                    view.setBackgroundColor(Color.WHITE);
                    ((TextView) view).setTextColor(Color.BLACK);
                    PS.selectedSubstitutions.remove(((TextView) view).getText().toString());
                } else{
                    view.setBackgroundColor(Color.BLACK);
                    ((TextView) view).setTextColor(Color.WHITE);
                    PS.selectedSubstitutions.add(((TextView) view).getText().toString());
                }
            DisplayUpdateHelper.this.setPS(PS);
            DisplayUpdateHelper.this.UpdateRuleDisplay();

        }
    }
    public class RuleSelectionListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            ProblemState PS = DisplayUpdateHelper.this.getPS();
            LinearLayout RLVV  = DisplayUpdateHelper.this.findViewById(R.id.RuleListVerticalLayout);
            int count=  RLVV.getChildCount();
            for(int i = 0; i<count; i++){
                HorizontalScrollView tempHS = (HorizontalScrollView) RLVV.getChildAt(i);
                TextView thetext =((TextView) ((LinearLayout) tempHS.getChildAt(0)).getChildAt(0));
                thetext.setBackgroundColor(Color.WHITE);
                thetext.setTextColor(Color.BLACK);
            }
            view.setBackgroundColor(Color.BLACK);
            ((TextView)view).setTextColor(Color.WHITE);
            String rule = ((TextView)view).getText().toString();
            PS.rSequent = AuxFunctionality.StringRuleToTerms(rule);
            DisplayUpdateHelper.this.setPS(PS);
            DisplayUpdateHelper.this.UpdateRuleDisplay();
        }

    }
    protected class applyruleListner implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ProblemState PS = DisplayUpdateHelper.this.getPS();
            TextView rule = DisplayUpdateHelper.this.findViewById(R.id.InstanceOfRuleDisplay);
            if (!rule.getText().toString().contains("∀")) {
                ArrayList<Term> temp = new ArrayList<>();
                temp.add(AuxFunctionality.ApplySubstitution(getPS().rSequent[0], getPS().selectedSubstitutions));
                temp.add(AuxFunctionality.ApplySubstitution(getPS().rSequent[1], getPS().selectedSubstitutions));
                if(temp.get(0).Print().compareTo(PS.sSequent[0].Print())== 0){
                    PS.sSequent[0] = temp.get(1);
                    PS.rSequent = new Term[]{Const.HoleSelected, Const.HoleSelected};
                    PS.selectedSubstitutions = new ArrayList<>();
                    if( PS.sSequent[0].Print().compareTo(PS.sSequent[1].Print())==0){
                        PS.sSequent = new Term[]{Const.Hole.Dup(),Const.Hole.Dup()};
                        Toast.makeText(DisplayUpdateHelper.this, "Problem Solved!", Toast.LENGTH_SHORT).show();
                    }
                    DisplayUpdateHelper.this.setPS(PS);
                    DisplayUpdateHelper.this.ActivityDecorate();
                } else if (temp.get(1).Print().compareTo(PS.sSequent[1].Print())== 0){
                    PS.sSequent[1] = temp.get(0);
                    PS.rSequent = new Term[]{Const.HoleSelected, Const.HoleSelected};
                    PS.selectedSubstitutions = new ArrayList<>();
                    if( PS.sSequent[0].Print().compareTo(PS.sSequent[1].Print())==0){
                        PS.sSequent = new Term[]{Const.Hole.Dup(),Const.Hole.Dup()};
                        Toast.makeText(DisplayUpdateHelper.this, "Problem Solved!", Toast.LENGTH_SHORT).show();
                    }
                    DisplayUpdateHelper.this.setPS(PS);
                    DisplayUpdateHelper.this.ActivityDecorate();
                } else  Toast.makeText(DisplayUpdateHelper.this, "Rule Cannot be Applied", Toast.LENGTH_SHORT).show();

            } else  Toast.makeText(DisplayUpdateHelper.this, "Substitute All Variables First!", Toast.LENGTH_SHORT).show();
        }

    }
    protected class OnTouchHapticListener implements View.OnTouchListener {
       @Override
        public boolean onTouch(View view, MotionEvent motionEvent){
           view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            return false;
       }
    }
    protected class UndoSubstitutionListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
           ProblemState PS = DisplayUpdateHelper.this.getPS();
            if(PS.SubHistory.isEmpty()) PS.substitution = Const.HoleSelected.Dup();
            else{
                PS.SubHistory.remove(PS.SubHistory.size()-1);
                if(PS.SubHistory.isEmpty()) PS.substitution = Const.HoleSelected.Dup();
                else PS.substitution = PS.SubHistory.get(PS.SubHistory.size()-1);
            }
            DisplayUpdateHelper.this.setPS(PS);
            DisplayUpdateHelper.this.ActivityDecorate();

        }
    }

}
