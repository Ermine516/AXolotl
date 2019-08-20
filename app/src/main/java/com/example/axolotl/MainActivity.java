package com.example.axolotl;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.HashSet;

import static com.example.axolotl.AxolotlMessagingAndIO.PASSPROBLEMSTATE;
import static com.example.axolotl.TermHelper.TermMatchWithVar;

public class MainActivity extends AxolotlSupportingFunctionality {
    AnimationDrawable animation;
    private int currentlayout;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    public void onConfigurationChanged(Configuration newconfig) {
        super.onConfigurationChanged(newconfig);
        currentlayout = this.getResources().getConfiguration().orientation;
        onInternalChange();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentlayout = this.getResources().getConfiguration().orientation;

        PS = constructProblemState(savedInstanceState, getIntent());
        int animate = -1;
        if (PS.mainActivityState == 3) {
            PS.mainActivityState = 0;
            animate = 0;
        } else if (PS.mainActivityState == 4) {
            PS.mainActivityState = 0;
            animate = 1;
        }
        if (PS.ActivityMode == 0) {
            if (PS.mainActivityState == -1) setContentView(R.layout.app_main_on_load_bar_layout);
            else if (PS.mainActivityState == 0) {
                setContentView(R.layout.app_main_bar_layout);
                if (animate == 0) {
                    if (currentlayout == Configuration.ORIENTATION_PORTRAIT) {
                        ImageView image = findViewById(R.id.AxolotlHorizontal);
                        animation = (AnimationDrawable) AppCompatResources.getDrawable(this, R.drawable.applied_rule_animation);
                        image.setImageDrawable(animation);
                        animation.start();
                    } else {
                        ImageView image = findViewById(R.id.AxolotlHorizontal);
                        animation = (AnimationDrawable) AppCompatResources.getDrawable(this, R.drawable.landscape_applied_rule_animation);
                        image.setImageDrawable(animation);
                        animation.start();
                    }
                } else if (animate == 1) {
                    if (currentlayout == Configuration.ORIENTATION_PORTRAIT) {
                        ImageView image = findViewById(R.id.AxolotlHorizontal);
                        animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.undo_animation);
                        image.setImageDrawable(animation);
                        animation.start();
                    } else {
                        ImageView image = findViewById(R.id.AxolotlHorizontal);
                        animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.landscape_undo_animation);
                        image.setImageDrawable(animation);
                        animation.start();
                    }
                }
                findViewById(R.id.OuterLayout).setOnTouchListener(new MainSwipeListener(this));
                findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            } else {
                setContentView(R.layout.app_main_on_completion_bar_layout);
                ImageView image = findViewById(R.id.Axolotlcompletion);
                animation = (AnimationDrawable) AppCompatResources.getDrawable(this, R.drawable.on_completion_animation);
                image.setImageDrawable(animation);
                animation.start();
            }
            ConstructActivity(savedInstanceState);
            if (PS.mainActivityState == 0) ActivityDecorate();
            if (ProblemState.sideContainsEmptySet(PS.problem) && PS.problem.size() > 1)
                PS.problemClean();
            if (PS.problem.size() == 0) PS.problem.add(Const.Empty.Dup());
            if (PS.problem.size() == 0 || PS.problem.iterator().next().getSym().compareTo(Const.Empty.getSym()) == 0) {
                boolean passobseve = PS.observe;
                ArrayList<State> ProofHistory = PS.History;
                ArrayList<Rule> rules = PS.Rules;
                PS = new ProblemState();
                PS.observe = passobseve;
                PS.History = ProofHistory;
                PS.Rules = rules;
            }
        } else switchDisplay();
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

    protected void ActivityDecorate() {
        if (PS.mainActivityState == 0) {
            UpdateProblemDisplay();
            RuleDisplayUpdate();
        }
    }

    private void UpdateProblemForRulesDisplay() {
        if (PS.mainActivityState == 0) {
            HashSet<Term> forDisplay = new HashSet<>();
            forDisplay.add(Const.Empty);
            forDisplay.addAll(PS.problem);
            updateProblemSideDisplay((LinearLayout) this.findViewById(R.id.RightSideTermLayout), forDisplay.toArray(AxolotlMessagingAndIO.HashSetTermArray));
        }
    }
    private void UpdateProblemDisplay() {
        if (PS.mainActivityState == 0)
            updateProblemSideDisplay((LinearLayout) this.findViewById(R.id.RightSideTermLayout), PS.problem.toArray(AxolotlMessagingAndIO.HashSetTermArray));
    }

    protected void switchDisplay() {
        if (PS.ActivityMode == 1) {
            setContentView(R.layout.app_rule_view_bar_layout);
            findViewById(R.id.backbutton).setOnClickListener(new BackButtonListener());
            UpdateProblemForRulesDisplay();
            drawRule();
        } else if (PS.ActivityMode == 2) {
            PS.ActivityMode = 0;
            setContentView(R.layout.app_main_bar_layout);
            findViewById(R.id.OuterLayout).setOnTouchListener(new MainSwipeListener(this));
            findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
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
    }

    protected boolean implementationOfSwipeLeft() {
        if (PS.History.size() != 0) {
            try {
                State laststep = PS.History.remove(PS.History.size() - 1);
                HashSet<Term> anteSideApply;
                Term succSideApply = laststep.substitution.apply(laststep.rule.argument.Dup());
                HashSet<Term> newSuccProblem = new HashSet<>();
                succSideApply = laststep.substitution.apply(succSideApply);
                newSuccProblem.add(succSideApply);
                anteSideApply = laststep.substitution.apply(laststep.rule.Conclusions);
                for (Term t : PS.problem) {
                    boolean wasselected = false;
                    for (Term s : anteSideApply)
                        if (t.Print().compareTo(s.Print()) == 0) wasselected = true;
                    if (!wasselected) newSuccProblem.add(t);
                }
                PS.problem = newSuccProblem;

            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "Problems accessing History", Toast.LENGTH_SHORT).show();
                return true;
            }
            PS.subPos = -1;
            PS.Substitutions = new Substitution();
            PS.mainActivityState = 4;
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra(PASSPROBLEMSTATE, PS);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
            overridePendingTransition(0, 0);
            //  overridePendingTransition(0,R.anim.animation_enter);


        } else {
            if (currentlayout == Configuration.ORIENTATION_PORTRAIT) {
                ImageView image = findViewById(R.id.AxolotlHorizontal);
                animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.no_undo_animation);
                image.setImageDrawable(animation);
                animation.start();
            } else {
                ImageView image = findViewById(R.id.AxolotlHorizontal);
                animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.landscape_no_undo_animation);
                image.setImageDrawable(animation);
                animation.start();
            }

        }

        return true;
    }

    protected class MainSwipeListener extends OnSwipeTouchListener {
        MainSwipeListener(Context ctx) {
            super(ctx);
        }

        public boolean onSwipeRight() {
            ProblemState PS = MainActivity.this.PS;
            Intent intent;
            try {
                if (PS.currentRule.argument.getSym().compareTo(Const.HoleSelected.getSym()) != 0) {
                    if (PS.selectedPosition.compareTo("") != 0) {
                        Term succTerm = ProblemState.getTermByString(PS.selectedPosition, PS.problem);
                        if (TermHelper.wellformedSequents(succTerm) && TermHelper.wellformedSequents(PS.currentRule.argument)) {
                            succTerm.normalize(PS.Variables); // Don't forget that sequents are brittle terms
                            PS.currentRule.argument.normalize(PS.Variables); // Don't forget that sequents are brittle terms
                        }
                        if (succTerm != null && TermMatchWithVar(succTerm, PS.currentRule.argument, PS.Variables)) {
                            PS.Substitutions = Substitution.substitutionConstruct(succTerm, PS.currentRule.argument, PS);
                            try {
                                PS.Substitutions = PS.Substitutions.clean();
                                HashSet<String> singleSide = new HashSet<>();
                                for (Term t : PS.currentRule.Conclusions) {
                                    HashSet<String> vars = PS.VarList(t);
                                    for (String s : vars)
                                        if (!PS.VarList(PS.currentRule.argument).contains(s))
                                            singleSide.add(s);
                                }
                                for (String s : singleSide)
                                    PS.Substitutions.varIsPartial(s);
                                PS.subPos = 0;
                                PS.MatchorConstruct = PS.Substitutions.partialOrNot();
                                if (!PS.observe)
                                    while (PS.Substitutions.isPosition(PS.subPos) && !PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                                        PS.subPos++;
                                if (PS.Substitutions.isPosition(PS.subPos) && PS.Substitutions.get(PS.subPos).replacement.contains(Const.HoleSelected))
                                    intent = new Intent(MainActivity.this, TermConstructActivity.class);
                                else if (PS.Substitutions.isPosition(PS.subPos))
                                    intent = new Intent(MainActivity.this, MatchDisplayActivity.class);
                                else {
                                    MainActivity.this.swipeRightProblemStateUpdate();
                                    intent = new Intent(MainActivity.this, MainActivity.class);
                                }
                                intent.putExtra(PASSPROBLEMSTATE, PS);
                                MainActivity.this.startActivity(intent);
                                overridePendingTransition(0, 0);
                                //overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                                MainActivity.this.finish();
                            } catch (Substitution.NotASubtitutionException e) {
                                if (currentlayout == Configuration.ORIENTATION_PORTRAIT) {
                                    ImageView image = findViewById(R.id.AxolotlHorizontal);
                                    animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.select_not_app_animation);
                                    image.setImageDrawable(animation);
                                    animation.start();
                                } else {
                                    ImageView image = findViewById(R.id.AxolotlHorizontal);
                                    animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.landscape_select_not_app_animation);
                                    image.setImageDrawable(animation);
                                    animation.start();
                                }
                            }
                        } else {
                            if (currentlayout == Configuration.ORIENTATION_PORTRAIT) {
                                ImageView image = findViewById(R.id.AxolotlHorizontal);
                                animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.select_not_app_animation);
                                image.setImageDrawable(animation);
                                animation.start();
                            } else {
                                ImageView image = findViewById(R.id.AxolotlHorizontal);
                                animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.landscape_select_not_app_animation);
                                image.setImageDrawable(animation);
                                animation.start();
                            }
                        }
                    } else {
                        if (currentlayout == Configuration.ORIENTATION_PORTRAIT) {
                            ImageView image = findViewById(R.id.AxolotlHorizontal);
                            animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.select_goal_animation);
                            image.setImageDrawable(animation);
                            animation.start();
                        } else {
                            ImageView image = findViewById(R.id.AxolotlHorizontal);
                            animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.landscape_select_goal_animation);
                            image.setImageDrawable(animation);
                            animation.start();
                        }
                    }
                } else {
                    if (currentlayout == Configuration.ORIENTATION_PORTRAIT) {
                        ImageView image = findViewById(R.id.AxolotlHorizontal);
                        animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.select_rule_animation);
                        image.setImageDrawable(animation);
                        animation.start();
                    } else {
                        ImageView image = findViewById(R.id.AxolotlHorizontal);
                        animation = (AnimationDrawable) AppCompatResources.getDrawable(MainActivity.this, R.drawable.landscape_select_rule_animation);
                        image.setImageDrawable(animation);
                        animation.start();
                    }
                }
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "Problems Substituting", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    protected void onInternalChange() {
        if (PS.ActivityMode == 0) {
            if (PS.mainActivityState == -1) setContentView(R.layout.app_main_on_load_bar_layout);
            else if (PS.mainActivityState == 0) {
                setContentView(R.layout.app_main_bar_layout);
                findViewById(R.id.OuterLayout).setOnTouchListener(new MainSwipeListener(this));
                findViewById(R.id.OuterLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            } else setContentView(R.layout.app_main_on_completion_bar_layout);
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
            if (PS.mainActivityState == 0) ActivityDecorate();
        } else switchDisplay();
    }


}
