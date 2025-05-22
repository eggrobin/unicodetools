package org.unicode.propstest;

import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.VersionInfo;
import org.junit.jupiter.api.Test;
import org.unicode.text.UCD.VersionedSymbolTable;
import org.unicode.text.utility.Utility;

public class Historian {
    @Test
    void testGeneralCategoryHistory() {
        UnicodeSet.setDefaultXSymbolTable(VersionedSymbolTable.forDevelopment());
        for (final var version : Utility.UNICODE_VERSIONS) {
            if (version.getMajor() == 13 && version.getMinor() == 1) {
                continue;
            }
            if (version.equals(VersionInfo.UNICODE_3_1_0)) {
                break;
            }
            String u = Utility.getVersionPreceding(version).toString();
            String v = version.toString();
            final var diff =
                    new UnicodeSet("[\\P{U" + u + ":gc=@U" + v + ":gc@}-\\p{U" + u + ":Cn}]");
            if (!diff.isEmpty()) {
                System.err.println(v + "\t" + diff.complement().complement());
            }
        }
    }
}
