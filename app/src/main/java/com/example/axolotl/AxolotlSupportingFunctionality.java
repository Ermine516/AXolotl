package com.example.axolotl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.drawerlayout.widget.DrawerLayout;

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static com.example.axolotl.AxolotlMessagingAndIO.PASSPROBLEMSTATE;

/**
 * This abstract class extends the abstract class AxolotlSupportingListenersAndMethods with methods essential for
 * the main activities of Axolotl.
 *
 * @author David M. Cerna
 */
public abstract class AxolotlSupportingFunctionality extends AxolotlSupportingListenersAndMethods {

    /**
     * This is the switch located in the navigation menu which turns observation mode on and off
     */
    androidx.appcompat.widget.SwitchCompat switcher;

    /**
     * This is the seekerbar located in the navigation menu which changes font size
     */
    androidx.appcompat.widget.AppCompatSeekBar seeker;


    /**
     * This is an abstract method used to decorate the layout of the activities implementing this
     * abstract class.
     * @author David M. Cerna
     */
    protected abstract void ActivityDecorate();

    /**
     * This is an abstract method is used to decorate the layout of the activities when an internal
     * change has occurred, i.e. a layout change.
     *
     * @author David M. Cerna
     */
    protected abstract void onInternalChange();

    /**
     * Each activity implementing AxolotlSupportingFunctionality must construct a particular toolbar and
     * navigation menu as well as read the problem state and state the switch in the appropriate
     * state.
     * @author David M. Cerna
     * @param in The Bundle containing the intent from the activity which requested the creation of
     *           current activity
     * @return The problem state after the necessary changes.
     */
    protected ProblemState ConstructActivity(Bundle in) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.Drawer);
        addMenulisteners();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Intent intent = getIntent();
        PS = (PS == null) ? constructProblemState(in, intent) : PS;
        switcher = findViewById(R.id.observeswitchformenu);
        switcher.setChecked(PS.observe);
        switcher.setOnCheckedChangeListener(new ObservationListener());
        seeker = findViewById(R.id.Adjusttextseeker);
        seeker.setProgress(PS.textSize);
        return PS;
    }

    /**
     * If the activity implementing AxolotlSupportingFunctionality has a variety of layouts depending on the
     * problem state it may be necessary to extract the problem state from the Bundle prior to
     * constructing the activity.
     * @param in The Bundle containing the intent from the activity which requested the creation of
     *           current activity
     * @param tent The intent sent along with the Bundle.
     * @return The problem state after the necessary changes.
     */
    protected ProblemState constructProblemState(Bundle in, Intent tent) {
        if (tent.hasExtra(PASSPROBLEMSTATE)) PS = tent.getParcelableExtra(PASSPROBLEMSTATE);
        else if (in != null && in.containsKey("ProblemState"))
            PS = in.getParcelable("ProblemState");
        else PS = new ProblemState();
        return PS;
    }

    /**
     * Being that the Navigation menu includes non-standard components we avoid using the standard
     * listeners associated with the navigation menu and instead add the following
     * MenuOnClickListener to each menu item.
     * @author David M. Cerna
     */
    protected void addMenulisteners() {
        LinearLayout ll = findViewById(R.id.nonclassicbuttonlayout);
        ll.setOnClickListener(new MenuOnClickListener());
        ll = findViewById(R.id.classicbuttonlayout);
        ll.setOnClickListener(new MenuOnClickListener());
        ll = findViewById(R.id.TermMatchingbuttonlayout);
        ll.setOnClickListener(new MenuOnClickListener());
        ll = findViewById(R.id.Proofbuttonlayout);
        ll.setOnClickListener(new MenuOnClickListener());
        ll = findViewById(R.id.problembuttonlayout);
        ll.setOnClickListener(new MenuOnClickListener());
        SeekBar seek = findViewById(R.id.Adjusttextseeker);
        seek.setOnSeekBarChangeListener(new TextSizeChangeListener());
    }

    /**
     * Swiping right results in a few standard changes to the problem state which are handled by the
     * following function. It is important that the changes occur prior to creating the new activity
     * which will handle the changes to the problem state.
     * @author David M. Cerna
     */
    protected void swipeRightProblemStateUpdate() {
        if (PS.currentRule.argument.getSym().compareTo(new Rule().argument.getSym()) != 0) {
            PS.History.add(new State(PS.selectedPosition, PS.Substitutions, PS.currentRule));
        }
        HashSet<Term> newProblemsucc = new HashSet<>();
        for (Term t : PS.problem)
            if (t.Print().compareTo(PS.selectedPosition) != 0) newProblemsucc.add(t);
            else newProblemsucc.addAll(PS.Substitutions.apply(PS.currentRule.Conclusions));
        PS.problem = newProblemsucc;
        for (Term t : PS.problem)
            if (TermHelper.wellformedSequents(t))
                t.normalize(PS.Variables);
        PS.selectedPosition = "";
        PS.subPos = -1;
        PS.currentRule = new Rule();
        PS.Substitutions = new Substitution();
        PS.SubHistory = new HashMap<>();
        if (PS.problem.isEmpty()) PS.problem.add(Const.Empty.Dup());
        if ((PS.problem.size() == 0 || PS.problem.iterator().next().getSym().compareTo("∅") == 0)) {
            PS.mainActivityState = 2;
        } else {
            PS.mainActivityState = 3;
        }
    }

    /**
     * When loading a file from the system or from the internal problem library this function captures
     * the result of the request and handles it.
     * @author David M. Cerna
     * @param requestCode The request sent to a system level activity.
     * @param resultCode The result of the request.
     * @param data What was retrieved from the request.
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AxolotlMessagingAndIO.READ_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                    PS = AxolotlMessagingAndIO.loadFile(inputStream, new File(Objects.requireNonNull(data.getData().getPath())).getName(), this);
                    PS.mainActivityState = 0;
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "Unable to load file", Toast.LENGTH_SHORT).show();                }
            }
            if (PS != null) onInternalChange();
            else {
                PS = new ProblemState();
                Toast.makeText(this, "Unable to load file", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode,resultCode,data);
        }

    }

    /**
     * This method construct scrollable text views in a uniform way allowing a consistent look across
     * all activities.
     * @author David M. Cerna
     * @param text The text to be displayed by the text view. (only when a term)
     * @param lis The listener associated with clicking on the view.
     * @param ctx The activity associated with the text view.
     * @param gravity whether or not gravity should be centered within the text view and its scrolling
     *                elements.
     * @return The scroll view containing the text view.
     */
    public HorizontalScrollView scrollTextSelectConstruct(Term text, View.OnClickListener lis, View.OnLongClickListener longLis, Context ctx, boolean gravity) {
        TextView TermText = new TextView(ctx);
        TermText.setTextSize(PS.textSize);
        TermText.setText(Html.fromHtml(text.Print()));
        if (gravity) TermText.setGravity(Gravity.CENTER);
        TermText.setFreezesText(true);
        if (text.Print().compareTo(Const.Empty.getSym()) == 0 && PS.selectedPosition.compareTo(Const.Empty.getSym()) == 0) {
            TermText.setTextColor(Color.WHITE);
            TermText.setBackgroundColor(Color.BLACK);
        } else if (PS.selectedPosition.compareTo("") != 0 && (text.Print().compareTo(PS.selectedPosition) == 0)) {
            TermText.setTextColor(Color.WHITE);
            TermText.setBackgroundColor(Color.BLACK);
        } else if (PS.currentRule.argument.getSym().compareTo(Const.HoleSelected.getSym()) != 0 && (text.Print().compareTo(PS.selectedPosition) == 0)) {
            TermText.setTextColor(Color.WHITE);
            TermText.setBackgroundColor(Color.BLACK);
        } else {
            TermText.setTextColor(Color.BLACK);
            TermText.setBackgroundColor(Color.WHITE);
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(((int) TermText.getPaint().measureText(TermText.getText().toString()) + 20), FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 48);
        TermText.setLayoutParams(lp);
        if (lis != null) TermText.setOnClickListener(lis);
        if (longLis != null) {
            TermText.setLongClickable(true);
            TermText.setOnLongClickListener(longLis);
        }
        LinearLayout scrollLayout = new LinearLayout(this);
        scrollLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY));
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

    /**
     * This method construct scrollable text views in a uniform way allowing a consistent look across
     * all activities.
     *
     * @param text    The text to be displayed by the text view. When no additional checks are needed
     * @param lis     The listener associated with clicking on the view.
     * @param ctx     The activity associated with the text view.
     * @param gravity whether or not gravity should be centered within the text view and its scrolling
     *                elements.
     * @return The scroll view containing the text view.
     * @author David M. Cerna
     */
    public HorizontalScrollView scrollTextSelectConstructString(String text, View.OnClickListener lis, View.OnLongClickListener longLis, Context ctx, boolean gravity) {
        TextView TermText = new TextView(ctx);
        TermText.setTextSize(PS.textSize);
        TermText.setText(Html.fromHtml(text));
        if (gravity) TermText.setGravity(Gravity.CENTER);
        TermText.setFreezesText(true);
        if (PS.currentRule.argument.getSym().compareTo(Const.HoleSelected.getSym()) != 0 && (text.compareTo(Rule.RuleTermsToString(PS.currentRule)) == 0)) {
            TermText.setTextColor(Color.WHITE);
            TermText.setBackgroundColor(Color.BLACK);
        } else {
            TermText.setTextColor(Color.BLACK);
            TermText.setBackgroundColor(Color.WHITE);
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(((int) TermText.getPaint().measureText(TermText.getText().toString()) + 20), FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 48);
        TermText.setLayoutParams(lp);
        if (lis != null) TermText.setOnClickListener(lis);
        if (longLis != null) {
            TermText.setLongClickable(true);
            TermText.setOnLongClickListener(longLis);
        }
        LinearLayout scrollLayout = new LinearLayout(this);
        scrollLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY));
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

    /**
     * The problem is a sequence of terms which are displayed within a vertically oriented linear layout.
     * We add this sequence of terms to the problem display using the uniform text view created by the
     * scrollTextSelectConstruct() method.
     * @author David M. Cerna
     * @param sl The linear layout concerning the problem as text views
     * @param t The sequence of terms representing the problem
     */
    protected void updateProblemSideDisplay(LinearLayout sl, Term[] t) {
        sl.removeAllViewsInLayout();
        for (Term term : t) {
            sl.addView(scrollTextSelectConstruct(term, new SideSelectionListener(), null, this, true));
        }
    }

    /**
     * The future problem state is a sequence of terms which are displayed within a vertically oriented linear layout.
     * We add this sequence of terms to the future problem state display using the uniform text view created by the
     * scrollTextSelectConstruct() method.
     * @author David M. Cerna
     * @param sl The linear layout concerning the future problem state as text views
     * @param t The sequence of terms representing the future problem state
     */
    protected void updatefutureProblemSideDisplay(LinearLayout sl, Term[] t) {
        sl.removeAllViewsInLayout();
        for (Term term : t) {
            term.normalize(PS.Variables);
            sl.addView(scrollTextSelectConstruct(term, null, null, this, false));
        }
    }

    /**
     * Rules are a sequence of term pairs which are displayed within a vertically oriented linear layout.
     * We add this sequence of term pairs to the rule display using the uniform text view created by the
     * scrollTextSelectConstruct() method.
     * @author David M. Cerna
     */
    protected void RuleDisplayUpdate() {
        LinearLayout RLVV = this.findViewById(R.id.RuleListVerticalLayout);
        RLVV.removeAllViewsInLayout();
        for (int i = 0; i < PS.Rules.size(); i++)
            RLVV.addView(scrollTextSelectConstructString(Rule.RuleTermsToString(PS.Rules.get(i)),
                    new AxolotlSupportingFunctionality.RuleSelectionListener(),
                    new AxolotlSupportingFunctionality.RuleViewListener(),
                    this, false));
    }


    /**
     * Draws the bitmap representation of a proof.
     * @param  bm The Bitmap of a proof
     * @author Rafael Kiesl
     */
    protected void drawBitmap(Bitmap bm) {
        Bitmap bm1 = Bitmap.createBitmap(bm.getWidth() + 500, bm.getHeight() + 500, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bm, 250, 250, null);
        TouchImageView myImage = findViewById(R.id.proofViz);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        TileBitmapDrawable.attachTileBitmapDrawable(myImage, is, null, null);
    }

    /**
     * Constructs a bitmap of a rule (uninstantiated) and draws the corresponding object.
     * @author David M. Cerna
     */
    protected void drawRule() {
        if (PS.selectedPosition.compareTo(Const.Empty.getSym()) == 0) {
            ArrayList<Proof> args = new ArrayList<>();
            for (Term t : PS.currentRule.Conclusions) {
                Proof p = new Proof(t.Print(), "");
                p.drawLine = false;
                p.finished = true;
                args.add(p);
            }
            Proof p = new Proof(PS.currentRule.argument.Print(), PS.currentRule.Label);
            p.finished = true;
            p.antecedents = args;
            Pair<Bitmap, Pair<Float, Float>> bm = p.draw();
            drawBitmap(bm.first);
        } else drawRuleFromSelection();
    }

    /**
     * Constructs a bitmap of a rule instance and draws the corresponding object.
     * @author David M. Cerna
     */
    protected void drawRuleFromSelection() {
        Term succTerm = ProblemState.getTermByString(PS.selectedPosition, PS.problem);
        Substitution sub = Substitution.substitutionConstruct(succTerm, PS.currentRule.argument, PS);
        try {
            sub = sub.clean();
            HashSet<String> singleSide = new HashSet<>();
            for (Term t : PS.currentRule.Conclusions) {
                HashSet<String> vars = PS.VarList(t);
                for (String s : vars)
                    if (!PS.VarList(PS.currentRule.argument).contains(s))
                        singleSide.add(s);
            }
            for (String s : singleSide)
                sub.varIsPartial(s);
        } catch (Substitution.NotASubtitutionException e) {
            sub = null;
            String temp = PS.selectedPosition;
            PS.selectedPosition = Const.Empty.getSym();
            drawRule();
            PS.selectedPosition = temp;
        }
        if (sub != null) {
            ArrayList<Term> temp = new ArrayList<>(sub.losslessApply(PS.currentRule.Conclusions));
            Rule rule = new Rule(PS.currentRule.Label, temp, sub.apply(PS.currentRule.argument), PS.Variables);
            ArrayList<Proof> args = new ArrayList<>();
            for (Term t : rule.Conclusions) {
                Proof p = new Proof(t.Print(), "");
                p.drawLine = false;
                p.finished = true;
                args.add(p);
            }
            Proof p = new Proof(rule.argument.Print(), rule.Label);
            p.finished = true;
            p.antecedents = args;
            Pair<Bitmap, Pair<Float, Float>> bm = p.draw();
            drawBitmap(bm.first);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // request was granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        saveProof();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Storage permissions are needed to save.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    protected void saveProof() throws IOException {
        Bitmap proofPic = Proof.extractProof(PS).draw().first;
        Bitmap bm1 = Bitmap.createBitmap(proofPic.getWidth() + 500, proofPic.getHeight() + 500, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        canvas.drawBitmap(proofPic, 250, 250, null);
        String location = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "AXolotl";
        System.out.println("here  " + location);
        File myDir = new File(location);
        if (!myDir.exists()) myDir.mkdirs();
        if (!myDir.exists()) throw new IOException();
        long n = System.currentTimeMillis();
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        boolean gone = true;
        if (file.exists()) gone = file.delete();
        if (!gone) throw new IOException();
        FileOutputStream out = new FileOutputStream(file);
        bm1 = Proof.drawProblemSolution(PS);
        bm1.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
        Toast.makeText(this, "Saved Proof to Gallery", Toast.LENGTH_SHORT).show();
    }

    protected void copyLatexToClipboard() {
        // Nothing selected
        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        StringBuilder sb = new StringBuilder();
        sb.append("\\documentclass{article}\n" +
                "\\usepackage[a2paper]{geometry}\n" +
                "\\geometry{landscape}\n" +
                "\\usepackage{amsmath,amsthm,amssymb,amsfonts}\n" +
                "\\usepackage{bussproofs}\n" +
                "\n" +
                "\\begin{document}\n" +
                "\\begin{prooftree}\n");
        sb.append(Proof.extractProof(PS).printLatex());
        sb.append("\\end{prooftree}\n" +
                "\\end{document}");
        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("simple text", sb.toString());
        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip);
    }
}
