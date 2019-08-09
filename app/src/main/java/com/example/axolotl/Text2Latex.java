package com.example.axolotl;

public class Text2Latex {
    static String[] sourceArray = new String[]{
            " ", "#", "$", "%", "&", "*", "<", ">", "?", "@", "[", "\"", "\\", "]", "^", "_", "{", "|", "}", "~", "¡", "¢", "£", "¤", "§", "¨", "©", "ª", "¬", "®", "¯", "°", "±", "²", "³", "µ", "¶", "·", "¹", "º", "¼", "½", "¾", "¿", "À", "Á", "Â", "Ã", "Ä", "Å", "Æ", "Ç", "È", "É", "Ê", "Ë", "Ì", "Í", "Î", "Ï", "Ð", "Ñ", "Ò", "Ó", "Ô", "Õ", "Ö", "×", "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "Þ", "ß", "à", "á", "â", "ã", "ä", "å", "æ", "ç", "è", "é", "ê", "ë", "ì", "í", "î", "ï", "ð", "ñ", "ò", "ó", "ô", "õ", "ö", "÷", "ø", "ù", "ú", "û", "ü", "ý", "þ", "ÿ", "Š", "š", "Ÿ", "ˆ", "˜", "Α", "Β", "Γ", "Δ", "Ε", "Ζ", "Η", "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ", "Σ", "Τ", "Υ", "Φ", "Χ", "Ψ", "Ω", "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "ς", "σ", "τ", "υ", "φ", "χ", "ψ", "ω", "ϑ", "ϖ", "–", "—", "‘", "’", "‚", "“", "”", "†", "‡", "…", "€", "™", "←", "↑", "→", "↓", "↔", "⇐", "⇑", "⇒", "⇓", "⇔", "∀", "∃", "∈", "∉", "∏", "∑", "∞", "∧", "∨", "∩", "∪", "≈", "≠", "≤", "≥"
    };

    static String[] targetArray = new String[]{
            " ", "\\#", "\\$", "\\%", "\\&", "\\textasteriskcentered", "\\textless", "\\textgreater", "\\?", "\\@", "[", "\"", "\\textbackslash", "]", "\\textasciicircum", "\\_", "\\{", "\\textbar", "\\}", "\\textasciitilde", "\\textexclamdown", "\\cent", "\\pounds", "\\currency", "{\\S}", "\\textasciidieresis", "{\\copyright}", "\\textordfeminine", "\\textlnot", "{\\textregistered}", "\\textasciimacron", "\\textdegree", "\\textpm", "\\texttwosuperior", "\\textthreesuperior", "\\textmu", "\\P", "\\textperiodcentered", "\\textonesuperior", "\\textordmasculine", "\\textonequarter", "\\textonehalf", "\\textthreequarters", "\\textquestiondown", "\\`{A}", "\\'{A}", "\\^{A}", "\\~{A}", "\\\"{A}", "\\AA", "\\AE", "\\c{C}", "\\`{E}", "\\'{E}", "\\^{E}", "\\\"{E}", "\\`{I}", "\\'{I}", "\\^{I}", "\\\"{I}", "\\DH", "\\~{N}", "\\`{O}", "\\'{O}", "\\^{O}", "\\~{O}", "\\\"{O}", "\\texttimes", "\\O", "\\`{U}", "\\'{U}", "\\^{U}", "\\\"{U}", "\\'{Y}", "\\TH", "{\\ss}", "\\'{a}", "\\^{a}", "\\`{a}", "\\~{a}", "\\\"{a}", "\\aa", "\\ae", "\\c{c}", "\\`{e}", "\\'{e}", "\\^{e}", "\\\"{e}", "\\`{i}", "\\'{i}", "\\^{i}", "\\\"{i}", "\\dh", "\\~{n}", "\\`{o}", "\\'{o}", "\\^{o}", "\\~{o}", "\\\"{o}", "\\textdiv", "\\o", "\\`{u}", "\\'{u}", "\\^{u}", "\\\"{u}", "\\'{y}", "\\th", "\\\"{u}", "\\v{S}", "\\v{s}", "\\\"{Y}", "\\texcuub", "	extasciitilde", "A", "B", "\\Gamma", "\\Delta", "E", "Z", "H", "\\Theta", "I", "K", "\\Lambda", "M", "N", "\\Xi", "O", "\\Pi", "P", "\\Sigma", "T", "\\Upsilon", "\\Phi", "X", "\\Psi", "\\Omega", "\\alpha", "\\beta", "\\gamma", "\\delta", "\\epsilon", "\\zeta", "\\eta", "\\theta", "\\iota", "\\kappa", "\\lambda", "\\mu", "\\nu", "\\xi", "o", "\\pi", "\\rho", "\\varsigma", "\\sigma", "\\tau", "\\upsilon", "\\phi", "\\chi", "\\psi", "\\omega", "\\vartheta", "\\varpi", "--", "---", "`", "'", ",", "``", "''", "\\dag", "\\ddag", "{\\dots}", "{\\euro}", "{\\texttrademark}", "\\(\\leftarrow{}\\)", "\\(\\uparrow\\)", "\\(\\rightarrow{}\\)", "\\(\\downarrow{}\\)", "\\(\\leftrightarrow{}\\)", "\\(\\Leftarrow{}\\)", "\\(\\Uparrow\\)", "\\(\\Rightarrow{}\\)", "\\(\\Downarrow{}\\)", "\\(\\Leftrightarrow{}\\)", "$\\forall$", "$\\exists$", "$\\in$", "$\\notin$", "$\\prod$", "$\\sum$", "$infty$", "$\\wedge$", "$\\vee$", "$\\cap$", "$\\cup$", "$\\approx{}$", "$\\neq$", "$\\leq$", "$\\geq$"
    };

    static String translate(String toTranslate) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < toTranslate.length(); i++) {
            String subst = toTranslate.substring(i, i+1);
            int j;
            for(j = 0; j < sourceArray.length; j++) {
                if(sourceArray[j].equals(subst)) {
                    break;
                }
            }
            if(j < sourceArray.length) {
                sb.append(targetArray[j]);
            } else {
                sb.append(subst);
            }
        }
        return sb.toString();
    }
}
