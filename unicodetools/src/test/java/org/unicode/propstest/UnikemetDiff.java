package org.unicode.propstest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.unicode.props.DerivedPropertyStatus;
import org.unicode.props.IndexUnicodeProperties;
import org.unicode.props.UcdProperty;
import org.unicode.text.utility.Settings;

import com.ibm.icu.impl.Utility;

public class UnikemetDiff {

  public static final IndexUnicodeProperties oldProperties = IndexUnicodeProperties.make(Settings.LAST_VERSION_INFO);
  public static final IndexUnicodeProperties newProperties = IndexUnicodeProperties.make();

  @Test
  void testAllDiffs() throws FileNotFoundException {
    final var out = new PrintStream(new File("unikemet_diff.py"));
    out.println("UNIKEMET_DIFF : dict[str,dict[str,tuple[str|None,str|None]]] = {");
    for (final var property : UcdProperty.values()) {
      if (!property.name().startsWith("kEH")) {
        continue;
      }
      out.println("'" +  property + (property.getDerivedStatus() == DerivedPropertyStatus.Provisional ? " (Provisional)" : "") +"': {");
      final var oldProperty = oldProperties.getProperty(property);
      final var newProperty = newProperties.getProperty(property);
      for (int cp = 0; cp <= 0x10FFFF; ++cp) {
        final String oldValue = oldProperty.getValue(cp);
        final String newValue = newProperty.getValue(cp);
        if (!Objects.equals(oldValue, newValue)) {
          out.println("'U+" + Utility.hex(cp) + "':(" + (oldValue == null ? "None" : "'" + oldValue.replace("'", "\\'") + "'") + "," + (newValue == null ? "None" : "'" + newValue.replace("'", "\\'") + "'")  + "),");
        }
      }
      out.println("},");
    }
    out.println("}");
    out.close();
  }
}
