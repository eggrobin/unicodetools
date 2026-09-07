package org.unicode.propstest;

import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.VersionInfo;
import org.junit.jupiter.api.Test;
import org.unicode.props.IndexUnicodeProperties;
import org.unicode.props.UcdProperty;
import org.unicode.unittest.TestFmwkMinusMinus;

public class TestCollationProperties extends TestFmwkMinusMinus {
    @Test
    public void TestCollationFolding() {
        final var iup = IndexUnicodeProperties.make();
        assertEquals(
                "Shifted primary equivalents of ideograph one",
                new UnicodeSet("[㈠ ⼀㊀㆒🈩一]"),
                iup.getProperty(UcdProperty.UCA_Fold_1_Shifted).getSet("\u4E00"));
        assertEquals(
                "Shifted primary collation folding of CARE OF",
                "co",
                iup.getProperty(UcdProperty.UCA_Fold_1_Shifted).getValue("℅"));
        assertEquals(
                "Shifted secondary collation folding of CARE OF",
                "co",
                iup.getProperty(UcdProperty.UCA_Fold_2_Shifted).getValue("℅"));
        assertEquals(
                "Shifted tertiary collation folding of CARE OF",
                "⒞⒪",
                iup.getProperty(UcdProperty.UCA_Fold_3_Shifted).getValue("℅"));
        assertEquals(
                "Shifted quaternary collation folding of CARE OF",
                "\u0368/\u0366",
                iup.getProperty(UcdProperty.UCA_Fold_4_Shifted).getValue("℅"));
        assertEquals(
                "Non-ignorable primary collation folding of CARE OF",
                "c/o",
                iup.getProperty(UcdProperty.UCA_Fold_1_Non_Ignorable).getValue("℅"));
        assertEquals(
                "Non-ignorable secondary collation folding of CARE OF",
                "c/o",
                iup.getProperty(UcdProperty.UCA_Fold_2_Non_Ignorable).getValue("℅"));
        assertEquals(
                "Non-ignorable tertiary collation folding of CARE OF",
                "℅",
                iup.getProperty(UcdProperty.UCA_Fold_3_Non_Ignorable).getValue("℅"));
        final var iup150 = IndexUnicodeProperties.make(VersionInfo.UNICODE_15_0);
        final var iup151 = IndexUnicodeProperties.make(VersionInfo.UNICODE_15_1);
        assertEquals(
                "15.0 non-ignorable primary equivalents of APOSTROPHE",
                new UnicodeSet("['＇]"),
                iup150.getProperty(UcdProperty.UCA_Fold_1_Non_Ignorable).getSet("'"));
        assertEquals(
                "15.1 non-ignorable primary equivalents of APOSTROPHE",
                new UnicodeSet("['׳‘-‛＇]"),
                iup151.getProperty(UcdProperty.UCA_Fold_1_Non_Ignorable).getSet("'"));
    }

    @Test
    public void TestNextCodePoint() {
        final var iup = IndexUnicodeProperties.make();
        assertEquals(
                "code point following ideograph one in shifted order",
                "㈠",
                iup.getProperty(UcdProperty.UCA_Next_Shifted).getValue("\u4E00"));
        assertEquals(
                "code point following ideograph one in non-ignorable order",
                "\u2F00",
                iup.getProperty(UcdProperty.UCA_Next_Non_Ignorable).getValue("\u4E00"));
        final var iup150 = IndexUnicodeProperties.make(VersionInfo.UNICODE_15_0);
        final var iup151 = IndexUnicodeProperties.make(VersionInfo.UNICODE_15_1);
        assertEquals(
                "15.0 code point following APOSTROPHE in shifted order",
                "＇",
                iup150.getProperty(UcdProperty.UCA_Next_Shifted).getValue("'"));
        assertEquals(
                "15.1 code point following APOSTROPHE in shifted order",
                "׳",
                iup151.getProperty(UcdProperty.UCA_Next_Shifted).getValue("'"));
        assertEquals(
                "15.0 code point following RIGHT SINGLE QUOTATION MARK in shifted order",
                "‚",
                iup150.getProperty(UcdProperty.UCA_Next_Shifted).getValue("’"));
        assertEquals(
                "15.1 code point following RIGHT SINGLE QUOTATION MARK in shifted order",
                "‚",
                iup151.getProperty(UcdProperty.UCA_Next_Shifted).getValue("’"));
    }
}
