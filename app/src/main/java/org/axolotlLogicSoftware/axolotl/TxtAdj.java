package org.axolotlLogicSoftware.axolotl;

class TxtAdj {
    /**
     * When highlighting subterms we use the same color as end-gradiant for the action bar.
     */
    public static final String FONTCOLOR = "<font color=#EF4665>";
    public static final Adjustment Std = new Adjustment("", "");
    public static final Adjustment Bold = new Adjustment("<b>", "</b>");
    public static final Adjustment Color = new Adjustment(FONTCOLOR, "</font>");
    public static final Adjustment BoldColor = new Adjustment(FONTCOLOR + "<b>", "</b>" + "</font>");


}


