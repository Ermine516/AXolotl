package com.example.axolotltouch;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConstTermTest {
    @Test
    public void assertIsEmptyListTrue() {
        assertTrue(Const.EmptyList.isEmptyList());
    }

    @Test
    public void assertIsEmptyListFalse() {
        assertFalse(Const.HoleSelected.isEmptyList());
    }

    @Test
    public void assertSubtermsIsEmpty() {
        assertEquals(0, new Const("sjdslkjd").subTerms().size());
    }

    @Test
    public void assertSymbolIsCorrect() {
        assertEquals(0, new Const("sjdslkjd").getSym().compareTo("sjdslkjd"));
    }

    @Test
    public void assertPrintEmptyList() {
        assertNotEquals(0, Const.EmptyList.Print().compareTo(Const.EmptyList.getSym()));
        assertEquals(0, Const.EmptyList.Print().compareTo(""));

    }

    @Test
    public void assertReplace() {
        Const one = new Const("a");
        Const two = new Const("b");
        assertEquals((one.replace(one, two)), two);
        ArrayList<Term> three = new ArrayList<>();
        three.add(one);
        three.add(two);
        Func four = new Func("f", three, false);
        assertEquals((one.replace(one, four)), four);
        assertNotEquals((one.replace(two, four)), four);
    }

    @Test
    public void assertReplaceLeft() {
        Const one = new Const("a");
        Const two = new Const("b");
        assertEquals((one.replaceLeft(one, two)), two);
        ArrayList<Term> three = new ArrayList<>();
        three.add(one);
        three.add(two);
        Func four = new Func("f", three, false);
        assertEquals((one.replaceLeft(one, four)), four);
        assertNotEquals((one.replaceLeft(two, four)), four);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void assertBasicSymbols() {
        Const one = new Const("a");
        assertEquals(one.basicTerms().size(), 1);
        assertNotNull(one.basicTerms().get(one.getSym()));
        assertEquals(one.basicTerms().get(one.getSym()).size(), 1);
    }

    @Test
    public void assertPrint() {
        Const one = new Const("a");
        assertEquals(one.Print().compareTo("a"), 0);
        assertEquals(one.Print().compareTo("b"), -1);
        assertEquals(Const.EmptyList.Print().compareTo(""), 0);
        //printing a variable
        assertTrue(one.Print(one, false).contains("<font color=#ff0000>"));
        assertTrue(one.Print(one, false).contains("</font>"));
        assertFalse(one.Print(one, false).contains("<b>"));
        assertFalse(one.Print(one, false).contains("</b>"));
        assertTrue(one.Print(one, true).contains("<font color=#ff0000>"));
        assertTrue(one.Print(one, true).contains("</font>"));
        assertTrue(one.Print(one, true).contains("<b>"));
        assertTrue(one.Print(one, true).contains("</b>"));
        assertFalse(Const.EmptyList.Print(Const.EmptyList, false).contains("<font color=#ff0000>"));
        assertFalse(Const.EmptyList.Print(Const.EmptyList, false).contains("</font>"));
        assertFalse(Const.EmptyList.Print(Const.EmptyList, true).contains("<b>"));
        assertFalse(Const.EmptyList.Print(Const.EmptyList, true).contains("</b>"));
        assertFalse(Const.EmptyList.Print(one, false).contains("<font color=#ff0000>"));
        assertFalse(Const.EmptyList.Print(one, false).contains("</font>"));
        assertFalse(Const.EmptyList.Print(one, true).contains("<b>"));
        assertFalse(Const.EmptyList.Print(one, true).contains("</b>"));
        //printing a term Match

    }

    @Test
    public void assertPrintCons() {
        //standard print
        Const one = new Const("a");
        assertEquals(one.PrintCons().compareTo("a"), 0);
        assertEquals(one.PrintCons().compareTo("b"), -1);
        assertEquals(Const.EmptyList.PrintCons().compareTo(""), 0);

        //printing a variable
        assertTrue(one.PrintCons(one, false).contains("<font color=#ff0000>"));
        assertTrue(one.PrintCons(one, false).contains("</font>"));
        assertFalse(one.PrintCons(one, false).contains("<b>"));
        assertFalse(one.PrintCons(one, false).contains("</b>"));
        assertTrue(one.PrintCons(one, true).contains("<font color=#ff0000>"));
        assertTrue(one.PrintCons(one, true).contains("</font>"));
        assertTrue(one.PrintCons(one, true).contains("<b>"));
        assertTrue(one.PrintCons(one, true).contains("</b>"));
        assertFalse(Const.EmptyList.PrintCons(Const.EmptyList, false).contains("<font color=#ff0000>"));
        assertFalse(Const.EmptyList.PrintCons(Const.EmptyList, false).contains("</font>"));
        assertFalse(Const.EmptyList.PrintCons(Const.EmptyList, true).contains("<b>"));
        assertFalse(Const.EmptyList.PrintCons(Const.EmptyList, true).contains("</b>"));
        assertFalse(Const.EmptyList.PrintCons(one, false).contains("<font color=#ff0000>"));
        assertFalse(Const.EmptyList.PrintCons(one, false).contains("</font>"));
        assertFalse(Const.EmptyList.PrintCons(one, true).contains("<b>"));
        assertFalse(Const.EmptyList.PrintCons(one, true).contains("</b>"));

    }


}
