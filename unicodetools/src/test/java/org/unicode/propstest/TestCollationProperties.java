package org.unicode.propstest;

import com.ibm.icu.text.UnicodeSet;
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
                iup.getProperty(UcdProperty.UCA_Fold_1_Shifted).getSet("\u4E00"),
                new UnicodeSet("[㈠ ⼀㊀㆒🈩一]"));
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
                "Shifted primary collation folding of CARE OF",
                "c/o",
                iup.getProperty(UcdProperty.UCA_Fold_1_Non_Ignorable).getValue("℅"));
        assertEquals(
                "Shifted secondary collation folding of CARE OF",
                "c/o",
                iup.getProperty(UcdProperty.UCA_Fold_2_Non_Ignorable).getValue("℅"));
        assertEquals(
                "Shifted tertiary collation folding of CARE OF",
                "℅",
                iup.getProperty(UcdProperty.UCA_Fold_3_Non_Ignorable).getValue("℅"));
    }
}
