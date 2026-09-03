package org.unicode.text.tools;

import com.ibm.icu.impl.UnicodeMap;
import com.ibm.icu.text.UnicodeSet;
import java.io.IOException;
import java.util.Arrays;
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
import org.unicode.text.utility.DiffingPrintWriter;
import org.unicode.text.utility.Settings;
import org.unicode.text.utility.Utility;

public class CollationFolding {
    public static final void main(String[] args) throws IOException {
        final UCA uca = UCA.buildDucetCollator();
        final UCAContents ucaContents = uca.getContents(null);
        final List<UnicodeMap<CEList>> stringToElementsByLevel =
                List.of(new UnicodeMap<>(), new UnicodeMap<>(), new UnicodeMap<>());
        final List<TreeMap<CEList, UnicodeSet>> elementsToStringsByLevel =
                List.of(new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
        final int[] masks = {0xFFFF_0000, 0xFFFF_FF80, 0xFFFF_FFFF};
        final long start = System.currentTimeMillis();
        for (String s = ucaContents.next(); s != null; s = ucaContents.next()) {
            CEList collationElements = ucaContents.getCEs();
            for (int level = 0; level < 3; ++level) {
                int[] maskedElements = new int[collationElements.length()];
                for (int i = 0; i < collationElements.length(); ++i) {
                    maskedElements[i] = masks[level] & collationElements.at(i);
                }
                CEList levelElements =
                        new CEList(Arrays.stream(maskedElements).filter(i -> i != 0).toArray());
                stringToElementsByLevel.get(level).put(s, levelElements);
                elementsToStringsByLevel
                        .get(level)
                        .computeIfAbsent(levelElements, k -> new UnicodeSet())
                        .add(s);
            }
        }
        System.err.println(
                "%%%%%%%%%%%%%%% iteration : " + (System.currentTimeMillis() - start) + "ms");

        final List<UnicodeMap<String>> collationFoldings =
                List.of(new UnicodeMap<>(), new UnicodeMap<>(), new UnicodeMap<>());
        for (int level = 0; level < 3; ++level) {
            final long eqstart = System.currentTimeMillis();
            final Map<CEList, String> representatives = new TreeMap<>();
            for (final var entry : elementsToStringsByLevel.get(level).entrySet()) {
                final CEList elements = entry.getKey();
                final UnicodeSet strings = entry.getValue();
                representatives.put(elements, strings.stream().min(uca).get());
            }
            final UnicodeMap<String> collationFolding = collationFoldings.get(level);
            foldExpansions:
            for (final var entry : elementsToStringsByLevel.get(level).entrySet()) {
                final CEList elements = entry.getKey();
                final UnicodeSet strings = entry.getValue();
                if (elements.length() > 1) {
                    final var folding = new StringBuilder();
                    for (int i = 0; i < elements.length(); ++i) {
                        if (UCA.isImplicitLeadCE(elements.at(i))) {
                            final int cp =
                                    uca.implicit.codePointForPrimaryPair(
                                            CEList.getPrimary(elements.at(i)),
                                            CEList.getPrimary(elements.at(i + 1)));
                            final CEList cpElements = uca.getCEListForImplicit(cp);
                            int[] maskedElements = new int[cpElements.length()];
                            for (int j = 0; j < cpElements.length(); ++j) {
                                maskedElements[j] = masks[level] & cpElements.at(j);
                            }
                            if (maskedElements[0] != elements.at(i)
                                    || maskedElements[1] != elements.at(i + 1)) {
                                collationFolding.putAll(strings, representatives.get(elements));
                                continue foldExpansions;
                            }
                            ++i;
                            folding.append(Character.toString(cp));
                        } else {
                            String representative =
                                    representatives.get(new CEList(new int[] {elements.at(i)}));
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
            CEList previousElements = null;
            final UnicodeMap<String> nextCodePoint = new UnicodeMap<>();
            final UnicodeMap<String> previousCodePoint = new UnicodeMap<>();
            for (CEList elements : elementsToStringsByLevel.get(level).keySet()) {
                final UnicodeSet equivalenceClass =
                        stringToElementsByLevel.get(level).keySet(elements);
                if (equivalenceClass.contains("é")) {
                    System.out.println("uca level " + (level + 1) + " equivalence class of é:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("é")));
                }
                if (equivalenceClass.contains("\u4E00")) {
                    System.out.println(
                            "uca level " + (level + 1) + " equivalence class of \u4E00:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("\u4E00")));
                }
                if (equivalenceClass.contains("\u3226")) {
                    System.out.println(
                            "uca level " + (level + 1) + " equivalence class of \u3226:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("\u3226")));
                }
                if (equivalenceClass.contains("\u0439")) {
                    System.out.println(
                            "uca level " + (level + 1) + " equivalence class of \u0439:");
                    System.out.println(equivalenceClass);
                    System.out.println(Utility.hex(collationFolding.get("\u0439")));
                }
                if (previousElements != null) {
                    nextCodePoint.putAll(equivalenceClass, representatives.get(previousElements));
                    previousCodePoint.putAll(
                            stringToElementsByLevel.get(level).keySet(previousElements),
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
                    "%%%%%%%%%%%%%%% equivalence classes level "
                            + level
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
            tabber.add(17, Tabber.LEFT); // level 1
            tabber.add(17, Tabber.LEFT); // level 2
            tabber.add(17, Tabber.LEFT); // level 3
            int rangeStart = -1;
            List<String> rangeFoldings = null;
            for (int cp = 0; cp <= 0x10FFFF + 1; ++cp) {
                // For use in λs.
                final var codePoint = cp;
                final var string = cp == 0x110000 ? null : Character.toString(cp);
                final var foldings =
                        cp == 0x110000
                                ? null
                                : collationFoldings.stream()
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
