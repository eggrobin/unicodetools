package org.unicode.tools;

import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.VersionInfo;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import org.unicode.props.IndexUnicodeProperties;
import org.unicode.props.UcdProperty;
import org.unicode.text.utility.Settings;

public class MonkeyMaker {

    static final String[] MONKEYS = {"🙈", "🙉", "🙊", "🐵", "🐒"};

    public static void main(String[] args) throws IOException {
        String type = null;
        VersionInfo version = null;
        Boolean scalarsOnly = null;
        Boolean includeSA = null;
        Integer n = null;
        for (final String arg : args) {
            final String[] parts = arg.split("=", 2);
            switch (parts[0]) {
                case "--type":
                    if (type != null) {
                        throw new IllegalArgumentException("Duplicate " + arg);
                    }
                    type = parts[1];
                    break;
                case "--version":
                    if (version != null) {
                        throw new IllegalArgumentException("Duplicate " + arg);
                    }
                    version = VersionInfo.getInstance(parts[1]);
                    break;
                case "--scalarsOnly":
                    if (scalarsOnly != null) {
                        throw new IllegalArgumentException("Duplicate " + arg);
                    }
                    scalarsOnly = parts.length == 1 || Boolean.parseBoolean(parts[1]);
                    break;
                case "--includeSA":
                    if (includeSA != null) {
                        throw new IllegalArgumentException("Duplicate " + arg);
                    }
                    includeSA = parts.length == 1 || Boolean.parseBoolean(parts[1]);
                    break;
                case "--n":
                    if (n != null) {
                        throw new IllegalArgumentException("Duplicate " + arg);
                    }
                    n = Integer.parseInt(parts[1]);
                    break;
                default:
                    throw new IllegalArgumentException(arg);
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("--type= is required");
        }
        if (version == null) {
            version = Settings.LATEST_VERSION_INFO;
        }
        if (scalarsOnly == null) {
            scalarsOnly = false;
        }
        if (includeSA == null) {
            includeSA = true;
        }
        if (n == null) {
            n = 1;
        }
        final var segmenter = Segmenter.make(version, type).make();
        final var partition = segmenter.getPartitionDefinition();
        final var rng = new Random(1729);
        final UnicodeSet sa =
                IndexUnicodeProperties.make(version)
                        .getProperty(UcdProperty.Line_Break)
                        .getSet("SA");
        try (var file = new PrintStream(new File("Monkeys.txt"))) {
            for (int k = 0; k < n; ++k) {
                final var testString = new StringBuilder();
                for (int i = 0; i < 100; ++i) {
                    int cp;
                    do {
                        final UnicodeSet part =
                                partition.get(rng.nextInt(partition.size())).getSet();
                        cp = part.charAt(rng.nextInt(part.size()));
                    } while ((scalarsOnly && cp <= 0xFFFF && Character.isSurrogate((char) cp))
                            || (!includeSA && sa.contains(cp)));
                    testString.appendCodePoint(cp);
                }
                file.print(segmenter.breaksAt(testString, 0) ? "÷ " : "× ");
                for (int i = 0; i < testString.length(); ) {
                    final int cp = testString.codePointAt(i);
                    file.print(Utility.hex(cp));
                    if (cp > 0xFFFF) {
                        i += 2;
                    } else {
                        ++i;
                    }
                    file.print(segmenter.breaksAt(testString, i) ? " ÷ " : " × ");
                }
                final String monkey = MONKEYS[rng.nextInt(MONKEYS.length)];
                file.println("# " + monkey);
                System.out.print(monkey);
                if ((k + 1) % 100 == 0) {
                    System.out.println();
                    System.out.println((k + 1) + "...");
                }
            }
        }
    }
}
