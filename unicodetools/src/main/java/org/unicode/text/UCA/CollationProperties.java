package org.unicode.text.UCA;

import com.ibm.icu.impl.UnicodeMap;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.VersionInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.unicode.text.UCA.UCA.AppendToCe;
import org.unicode.text.UCA.UCA.UCAContents;
import org.unicode.text.UCA.UCA_Types.Alternate;
import org.unicode.text.utility.Utility;

public class CollationProperties {

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

    public static class FoldingType {
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

    public static Map<FoldingType, UnicodeMap<String>> getFoldings(VersionInfo version) {
        final UCA uca = UCA.buildDucetCollator(version);
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
                if (s.equals("\u2105"))
                    System.out.println(
                            "\u2105 "
                                    + type
                                    + " "
                                    + Arrays.stream(levelElements)
                                            .mapToObj(Utility::hex)
                                            .collect(Collectors.joining(",")));
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
                representatives.put(
                        elements, strings.stream().min(uca.thenComparing(String::compareTo)).get());
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
                                                        /* preceding= */ null);
                            }
                            if (maskedElements[0] != elements[i]
                                    || maskedElements[1] != elements[i + 1]) {
                                collationFolding.putAll(
                                        strings.cloneAsThawed()
                                                .remove(representatives.get(elements)),
                                        representatives.get(elements));
                                continue foldExpansions;
                            }
                            ++i;
                            folding.append(Character.toString(cp));
                        } else {
                            String representative = representatives.get(new long[] {elements[i]});
                            if (representative == null) {
                                collationFolding.putAll(
                                        strings.cloneAsThawed()
                                                .remove(representatives.get(elements)),
                                        representatives.get(elements));
                                continue foldExpansions;
                            }
                            folding.append(representative);
                        }
                    }
                    collationFolding.putAll(
                            strings.cloneAsThawed().remove(folding.toString()), folding.toString());
                } else {
                    collationFolding.putAll(
                            strings.cloneAsThawed().remove(representatives.get(elements)),
                            representatives.get(elements));
                }
                if (collationFolding.stringKeys() != null) {
                    collationFolding.removeAll(
                            new UnicodeSet().addAll(collationFolding.stringKeys()));
                }
            }
            System.err.println(
                    "%%%%%%%%%%%%%%% foldings for "
                            + type
                            + ": "
                            + (System.currentTimeMillis() - eqstart)
                            + "ms");
        }
        return collationFoldings;
    }

    public static Map<Alternate, UnicodeMap<String>> getNext(VersionInfo version) {
        final UCA uca = UCA.buildDucetCollator(version);
        final var nextStart = System.currentTimeMillis();
        final Map<Alternate, UnicodeMap<String>> next =
                Map.of(
                        Alternate.SHIFTED,
                        new UnicodeMap<>(),
                        Alternate.NON_IGNORABLE,
                        new UnicodeMap<>());
        System.err.println(
                Utility.hex(
                        uca.getSortKey(
                                Character.toString(0x249C),
                                Alternate.SHIFTED,
                                true,
                                AppendToCe.tieBreaker)));
        System.err.println(
                Utility.hex(
                        uca.getSortKey(
                                Character.toString(0x363),
                                Alternate.SHIFTED,
                                true,
                                AppendToCe.tieBreaker)));
        for (final var alternate : Alternate.values()) {
            final TreeMap<String, Integer> totalOrder = new TreeMap<>();
            for (int cp = 0; cp <= 0x10FFFF; ++cp) {
                totalOrder.put(
                        uca.getSortKey(
                                Character.toString(cp), alternate, true, AppendToCe.tieBreaker),
                        cp);
            }
            Integer preceding = null;
            for (final int cp : totalOrder.values()) {
                if (preceding != null && cp != preceding + 1) {
                    next.get(alternate).put(preceding, Character.toString(cp));
                }
                preceding = cp;
            }
        }
        System.err.println(
                "%%%%%%%%%%%%%%% next : " + (System.currentTimeMillis() - nextStart) + "ms");
        return next;
    }
}
