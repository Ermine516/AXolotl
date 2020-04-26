package org.axolotlLogicSoftware.axolotl;

class Adjustment {
    private String prefix;
    private String suffix;

    Adjustment(String pre, String suf) {
        prefix = pre;
        suffix = suf;
    }

    public String apply(String s) {
        return prefix + s + suffix;
    }
}

