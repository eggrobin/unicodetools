package org.unicode.jsp;

import com.ibm.icu.dev.util.UnicodeMap;
import com.ibm.icu.text.UnicodeSet;

public class XIDModifications {
  static UnicodeSet allowed = new UnicodeSet("[:XID_Continue:]");
  static UnicodeMap<String> reasons = new UnicodeMap<String>();

  static class MyReader extends FileUtilities.SemiFileReader {
    
    @Override
    protected boolean handleLine(int start, int end, String[] items) {
      String type = items[1];
      if (type.equalsIgnoreCase("allowed")) {
        allowed.add(start, end);
        reasons.putAll(start, end, items[2]);
      } else if (type.equalsIgnoreCase("restricted")) {
        allowed.remove(start, end);
        reasons.putAll(start, end, items[2]);
      } else {
        throw new IllegalArgumentException(type);
      }
      return true;
    }
  }
  static {
    reasons.putAll(0,0x10FFFF,"ok");
    reasons.putAll(new UnicodeSet("[[:gc=cn:][:gc=co:][:gc=cs:][:gc=cc:]-[:whitespace:]]"),"not-char");
    new MyReader().process(XIDModifications.class, "xidmodifications.txt");
    allowed.freeze();
    reasons.freeze();
  }
  public static UnicodeSet getAllowed() {
    return allowed;
  }
  public static UnicodeMap<String> getReasons() {
    return reasons;
  }
}
