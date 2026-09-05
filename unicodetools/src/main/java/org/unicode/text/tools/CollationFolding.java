package org.unicode.text.tools;

import com.ibm.icu.impl.UnicodeMap;
import com.ibm.icu.text.UnicodeSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.unicode.cldr.util.Tabber;
import org.unicode.props.IndexUnicodeProperties;
import org.unicode.text.UCA.CEList;
import org.unicode.text.UCA.UCA;
import org.unicode.text.UCA.UCA.UCAContents;
import org.unicode.text.UCA.UCA_Types.Alternate;
import org.unicode.text.utility.DiffingPrintWriter;
import org.unicode.text.utility.Settings;
import org.unicode.text.utility.Utility;

public class CollationFolding {

    private static long addQuaternary(
            UCA uca, Alternate alternate, int collationElement, Integer preceding) {
        if (alternate == Alternate.NON_IGNORABLE) {
            return (long) collationElement << 16;
        }
        int l1 = CEList.getPrimary(collationElement);
        int l3 = CEList.getTertiary(collationElement);
        if (collationElement == 0) {
            return 0;
        } else if (l1 == 0 && l3 != 0 && preceding != null && uca.isVariable(preceding)) {
            return 0;
        } else if (l1 != 0 && uca.isVariable(collationElement)) {
            return l1;
        } else if (l1 == 0 && l3 != 0 && (preceding == null || !uca.isVariable(preceding))) {
            return ((long) collationElement << 16) | 0xFFFF;
        } else if (l1 != 0 && !uca.isVariable(collationElement)) {
            return ((long) collationElement << 16) | 0xFFFF;
        } else {
            throw new IllegalArgumentException(
                    (preceding == null ? "null" : new CEList(new int[] {preceding}).toString())
                            + new CEList(new int[] {collationElement}).toString());
        }
    }

    private static int removeQuaternary(long collationElement) {
        return (int) (collationElement >> 16);
    }

    static class FoldingType {
        FoldingType(int level, Alternate alternate) {
            if (level < 1 || level > 4) {
                throw new IllegalArgumentException("Bad level " + level);
            }
            if (level == 4 && alternate == Alternate.NON_IGNORABLE) {
                throw new IllegalArgumentException("Bad level " + level + " for non-ignorable");
            }
            this.level = level;
            this.alternate = alternate;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof FoldingType
                    && ((FoldingType) other).level == level
                    && ((FoldingType) other).alternate == alternate;
        }

        @Override
        public int hashCode() {
            return Objects.hash(level, alternate);
        }

        @Override
        public String toString() {
            return Integer.toString(level) + "_" + alternate;
        }

        int level;
        Alternate alternate;
    }

    static final FoldingType[] FOLDING_TYPES =
            new FoldingType[] {
                new FoldingType(1, Alternate.SHIFTED),
                new FoldingType(2, Alternate.SHIFTED),
                new FoldingType(3, Alternate.SHIFTED),
                new FoldingType(4, Alternate.SHIFTED),
                new FoldingType(1, Alternate.NON_IGNORABLE),
                new FoldingType(2, Alternate.NON_IGNORABLE),
                new FoldingType(3, Alternate.NON_IGNORABLE),
            };

    public static final void main(String[] args) throws IOException {
        final UCA uca = UCA.buildDucetCollator();
        final UCAContents ucaContents = uca.getContents(null);
        final Map<FoldingType, UnicodeMap<long[]>> stringToElementsByType = new HashMap<>();
        final Map<FoldingType, TreeMap<long[], UnicodeSet>> elementsToStringsByType =
                new HashMap<>();
        final long[] masks = {
            0, 0xFFFF_0000_0000L, 0xFFFF_FF80_0000L, 0xFFFF_FFFF_0000L, 0xFFFF_FFFF_FFFFL
        };
        final long start = System.currentTimeMillis();
        for (String s = ucaContents.next(); s != null; s = ucaContents.next()) {
            CEList collationElements = ucaContents.getCEs();
            for (final var type : FOLDING_TYPES) {
                long[] maskedElements = new long[collationElements.length()];
                for (int i = 0; i < collationElements.length(); ++i) {
                    maskedElements[i] =
                            masks[type.level]
                                    & addQuaternary(
                                            uca,
                                            type.alternate,
                                            collationElements.at(i),
                                            i == 0 ? null : collationElements.at(i - 1));
                }
                long[] levelElements = Arrays.stream(maskedElements).filter(i -> i != 0).toArray();
                if (s.equals("\u2105")) System.out.println("\u2105 " + type + " " + Arrays.stream(levelElements).mapToObj(Utility::hex).collect(Collectors.joining(",")));
                stringToElementsByType
                        .computeIfAbsent(type, k -> new UnicodeMap<>())
                        .put(s, levelElements);
                elementsToStringsByType
                        .computeIfAbsent(type, k -> new TreeMap<>(Arrays::compare))
                        .computeIfAbsent(levelElements, k -> new UnicodeSet())
                        .add(s);
            }
        }
        System.err.println(
                "%%%%%%%%%%%%%%% iteration : " + (System.currentTimeMillis() - start) + "ms");

        final Map<FoldingType, UnicodeMap<String>> collationFoldings = new HashMap<>();
        for (final var type : FOLDING_TYPES) {
            final long eqstart = System.currentTimeMillis();
            final Map<long[], String> representatives = new TreeMap<>(Arrays::compare);
            for (final var entry : elementsToStringsByType.get(type).entrySet()) {
                final long[] elements = entry.getKey();
                final UnicodeSet strings = entry.getValue();
                representatives.put(elements, strings.stream().min(uca.thenComparing(String::compareTo)).get());
            }
            final UnicodeMap<String> collationFolding =
                    collationFoldings.computeIfAbsent(type, k -> new UnicodeMap<>());
            foldExpansions:
            for (final var entry : elementsToStringsByType.get(type).entrySet()) {
                final long[] elements = entry.getKey();
                final UnicodeSet strings = entry.getValue();
                if (elements.length > 1) {
                    final var folding = new StringBuilder();
                    for (int i = 0; i < elements.length; ++i) {
                        if (UCA.isImplicitLeadCE(removeQuaternary(elements[i]))) {
                            final int cp =
                                    uca.implicit.codePointForPrimaryPair(
                                            CEList.getPrimary(removeQuaternary(elements[i])),
                                            CEList.getPrimary(removeQuaternary(elements[i + 1])));
                            final CEList cpElements = uca.getCEListForImplicit(cp);
                            long[] maskedElements = new long[cpElements.length()];
                            for (int j = 0; j < cpElements.length(); ++j) {
                                maskedElements[j] =
                                        masks[type.level]
                                                & addQuaternary(
                                                        uca,
                                                        type.alternate,
                                                        cpElements.at(j),
                                                        /*preceding=*/null);
                            }
                            if (maskedElements[0] != elements[i]
                                    || maskedElements[1] != elements[i + 1]) {
                                collationFolding.putAll(strings, representatives.get(elements));
                                continue foldExpansions;
                            }
                            ++i;
                            folding.append(Character.toString(cp));
                        } else {
                            String representative = representatives.get(new long[] {elements[i]});
                            if (representative == null) {
                                collationFolding.putAll(strings, representatives.get(elements));
                                continue foldExpansions;
                            }
                            folding.append(representative);
                        }
                    }
                    collationFolding.putAll(strings, folding.toString());
                } else {
                    collationFolding.putAll(strings, representatives.get(elements));
                }
            }
            long[] previousElements = null;
            final UnicodeMap<String> nextCodePoint = new UnicodeMap<>();
            final UnicodeMap<String> previousCodePoint = new UnicodeMap<>();
            for (long[] elements : elementsToStringsByType.get(type).keySet()) {
                final UnicodeSet equivalenceClass =
                        stringToElementsByType.get(type).keySet(elements);
                if (equivalenceClass.contains("é")) {
                    System.out.println("uca level " + type + " equivalence class of é:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("é")));
                }
                if (equivalenceClass.contains("\u4E00")) {
                    System.out.println("uca level " + type + " equivalence class of \u4E00:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("\u4E00")));
                }
                if (equivalenceClass.contains("\u3226")) {
                    System.out.println("uca level " + type + " equivalence class of \u3226:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("\u3226")));
                }
                if (equivalenceClass.contains("\u0439")) {
                    System.out.println("uca level " + type + " equivalence class of \u0439:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("\u0439")));
                }
                if (previousElements != null) {
                    nextCodePoint.putAll(equivalenceClass, representatives.get(previousElements));
                    previousCodePoint.putAll(
                            stringToElementsByType.get(type).keySet(previousElements),
                            representatives.get(elements));
                }
                previousElements = elements;
            }
            /*
            String prefix = "uca_" + (level + 1);
            add(
                    new UnicodeProperty.UnicodeMapProperty()
                            .set(nextCodePoint)
                            .setMain(
                                    prefix + "_previous",
                                    prefix + "_previous",
                                    UnicodeProperty.STRING,
                                    "1.1"));
            add(
                    new UnicodeProperty.UnicodeMapProperty()
                            .set(previousCodePoint)
                            .setMain(
                                    prefix + "_next",
                                    prefix + "_next",
                                    UnicodeProperty.STRING,
                                    "1.1"));
            add(
                    new UnicodeProperty.UnicodeMapProperty()
                            .set(collationFolding)
                            .setMain(
                                    prefix + "_fold",
                                    prefix + "_fold",
                                    UnicodeProperty.STRING,
                                    "1.1"));*/
            System.err.println(
                    "%%%%%%%%%%%%%%% equivalence classes for "
                            + type
                            + ": "
                            + (System.currentTimeMillis() - eqstart)
                            + "ms");
        }
        try (final var writer =
                new DiffingPrintWriter(
                        Settings.UnicodeTools.UNICODETOOLS_REPO_DIR
                                + "/unicodetools/data/uca/unpublished/",
                        "CollationFolding.txt")) {
            final var iup = IndexUnicodeProperties.make();
            final var tabber = new Tabber.MonoTabber();
            tabber.add(12, Tabber.LEFT);
            for (final var type : FOLDING_TYPES) {
                tabber.add(17, Tabber.LEFT);
            }
            int rangeStart = -1;
            List<String> rangeFoldings = null;
            for (int cp = 0; cp <= 0x10FFFF + 1; ++cp) {
                // For use in λs.
                final var codePoint = cp;
                final var string = cp == 0x110000 ? null : Character.toString(cp);
                final var foldings =
                        cp == 0x110000
                                ? null
                                : Arrays.stream(FOLDING_TYPES)
                                        .map(collationFoldings::get)
                                        .map(
                                                f ->
                                                        Objects.requireNonNullElse(
                                                                f.get(codePoint), string))
                                        .toList();
                if (!Objects.equals(foldings, rangeFoldings)) {
                    if (rangeFoldings != null) {
                        writer.println(
                                tabber.process(getLine(rangeStart, cp - 1, rangeFoldings, iup)));
                    }
                    if (foldings != null
                            && foldings.stream().allMatch(folding -> folding.equals(string))) {
                        rangeFoldings = null;
                        continue;
                    }
                    rangeStart = cp;
                    rangeFoldings = foldings;
                }
            }
            /*
            for (final String s : uca.getContractions()) {
                final var foldings =
                        collationFoldings.stream()
                                        .map(
                                                f ->
                                                        Objects.requireNonNullElse(
                                                                f.get(s), s))
                                        .toList();
                        writer.println(
                                tabber.process(getLine(s, foldings, iup)));
            }
            */
        }
    }

    private static String getLine(
            int rangeFirst, int rangeLast, List<String> foldings, IndexUnicodeProperties iup) {
        return Utility.hex(rangeFirst)
                + (rangeFirst != rangeLast ? ".." + Utility.hex(rangeLast) : "")
                + "\t; "
                + foldings.stream().map(Utility::hex).collect(Collectors.joining("\t; "))
                + "\t# "
                + iup.getName(rangeFirst)
                + (rangeFirst != rangeLast ? ".." + iup.getName(rangeLast) : "");
    }

    private static String getLine(
            String string, List<String> foldings, IndexUnicodeProperties iup) {
        return Utility.hex(string)
                + "\t; "
                + foldings.stream().map(Utility::hex).collect(Collectors.joining("\t; "))
                + "\t# "
                + string.codePoints().mapToObj(iup::getName).collect(Collectors.joining(", "));
    }
}
