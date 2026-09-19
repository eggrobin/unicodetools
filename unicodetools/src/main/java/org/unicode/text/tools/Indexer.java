package org.unicode.text.tools;

import com.google.common.collect.Lists;
import com.ibm.icu.impl.UResource.Value;
import com.ibm.icu.segmenter.LocalizedSegmenter;
import com.ibm.icu.segmenter.LocalizedSegmenter.SegmentationType;
import com.ibm.icu.segmenter.Segment;
import com.ibm.icu.segmenter.Segmenter;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.Transliterator;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.VersionInfo;
import com.thaiopensource.relaxng.translate.test.Compare;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParsePosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.DeflaterOutputStream;
import org.unicode.props.IndexUnicodeProperties;
import org.unicode.props.UcdProperty;
import org.unicode.props.UcdPropertyValues;
import org.unicode.props.UcdPropertyValues.Block_Values;
import org.unicode.props.UcdPropertyValues.General_Category_Values;
import org.unicode.props.UnicodeProperty;
import org.unicode.text.UCD.Normalizer;
import org.unicode.text.utility.Settings;
import org.unicode.text.utility.Settings.ReleasePhase;
import org.unicode.text.utility.Utility;

public class Indexer {

    private static class VersionedIndexer {
        private static final char RECORD_SEPARATOR = 0x001E;

        private static Transliterator toHTML;
        private static String htmlRulesControls;

        private static final int BOOP = 0x10BE77;
        private static final int DOOD = 0x10D00D;

        private static final Normalizer NFKC = Normalizer.getNfkcInstance();

        private final VersionInfo version;
        private final ReleasePhase phase;
        private final String chartsRoot;
        private final String filename;
        private final String language;

        private final IndexUnicodeProperties IUP;
        private final UnicodeSet NEW_CHARACTERS;
        private final UnicodeProperty NAME;
        private final UnicodeProperty LOCALIZED_NAME;
        private final UnicodeProperty NAME_ALIAS;
        private final UnicodeProperty INFORMAL_ALIAS;
        private final UnicodeProperty BLOCK;
        private final UnicodeProperty PRETTY_BLOCK;
        private final UnicodeProperty SUBHEADER;
        private final UnicodeProperty SUBHEADER_NOTICE;
        private final UnicodeProperty COMMENT;
        private final UnicodeProperty K_RS_UNICODE;
        private final UnicodeProperty K_TGT_RS_UNICODE;
        private final UnicodeProperty K_JURC_RS_UNICODE;
        private final UnicodeProperty K_SEAL_RAD;
        private final UnicodeProperty CJK_RADICAL;
        private final UnicodeProperty GENERAL_CATEGORY;
        private final UnicodeSet NONCHARACTERS;

        private Map<Integer, String> representativeGlyphs;

        private final Map<String, UnicodeSet> blockSet = new HashMap<>();
        private final Map<String, String> prettifyBlock = new HashMap<>();

        VersionedIndexer(
                VersionInfo version,
                ReleasePhase phase,
                VersionInfo precedingVersion,
                String chartsRoot,
                String filename,
                String language) throws IOException {
            final String suffix = language == null ? "" : "_" + language;
            this.version = version;
            this.phase = phase;
            this.chartsRoot = chartsRoot;
            this.filename = filename;
            this.language = language;
            IUP = IndexUnicodeProperties.make(version);
            NEW_CHARACTERS =
                    IndexUnicodeProperties.make(precedingVersion)
                            .getProperty(UcdProperty.General_Category)
                            .getSet("Unassigned")
                            .removeAll(
                                    IUP.getProperty(UcdProperty.General_Category)
                                            .getSet("Unassigned"))
                            .freeze();
            NAME = IUP.getProperty(UcdProperty.Name);
            LOCALIZED_NAME =
                    language == null
                            ? NAME
                            : IUP.getProperty(UcdProperty.forString("Names_List_Name" + suffix));
            NAME_ALIAS =
                    language == null
                            ? IUP.getProperty(UcdProperty.Name_Alias)
                            : IUP.getProperty(
                                    UcdProperty.forString("Names_List_Formal_Alias" + suffix));
            INFORMAL_ALIAS = IUP.getProperty(UcdProperty.forString("Names_List_Alias" + suffix));
            BLOCK = IUP.getProperty(UcdProperty.Block);
            PRETTY_BLOCK =
                    IUP.getProperty(UcdProperty.forString("Names_List_Block_Header" + suffix));
            SUBHEADER = IUP.getProperty(UcdProperty.forString("Names_List_Subheader" + suffix));
            SUBHEADER_NOTICE =
                    IUP.getProperty(UcdProperty.forString("Names_List_Subheader_Notice" + suffix));
            COMMENT = IUP.getProperty(UcdProperty.forString("Names_List_Comment" + suffix));
            K_RS_UNICODE = IUP.getProperty(UcdProperty.kRSUnicode);
            K_TGT_RS_UNICODE = IUP.getProperty(UcdProperty.kTGT_RSUnicode);
            K_JURC_RS_UNICODE = IUP.getProperty(UcdProperty.kJURC_RSUnicode);
            K_SEAL_RAD = IUP.getProperty(UcdProperty.kSEAL_Rad);
            CJK_RADICAL = IUP.getProperty(UcdProperty.CJK_Radical);
            GENERAL_CATEGORY = IUP.getProperty(UcdProperty.General_Category);
            NONCHARACTERS = IUP.getProperty(UcdProperty.Noncharacter_Code_Point).getSet("Yes");
            for (String block : BLOCK.getAvailableValues()) {
                blockSet.put(block, BLOCK.getSet(block));
                if (Block_Values.forName(block) != Block_Values.No_Block
                        && !BLOCK.getSet(block).isEmpty()) {
                    // There are two cases where there are multiple names list block header values:
                    // C0 Controls and Basic Latin (Basic Latin)
                    // C1 Controls and Latin-1 Supplement (Latin-1 Supplement)
                    // In both cases, use the alternate name (in parentheses in NamesList.txt, the
                    // last name in the ordered values here), rather than the full name, as the
                    // index entry:
                    // Searching for C[01] controls should return the appropriate subheader, not the
                    // whole block.
                    // However, when referring to the block under the index entry (for instance, in
                    // the mention "In ..."), we use the first name, so a search for C0 finds
                    // C0 controls                         In C0 Controls and Basic Latin: 0000–001F
                    prettifyBlock.put(
                            block,
                            StreamSupport.stream(
                                            PRETTY_BLOCK
                                                    .getValues(
                                                            BLOCK.getSet(block)
                                                                    .removeAll(
                                                                            PRETTY_BLOCK.getSet(
                                                                                    UnicodeProperty
                                                                                            .NULL_MATCHER))
                                                                    .charAt(0))
                                                    .spliterator(),
                                            false)
                                    .reduce((first, second) -> second)
                                    .get());
                }
            }
        }

        private static final Segmenter SENTENCE_BREAK =
                LocalizedSegmenter.builder()
                        .setLocale(ULocale.ENGLISH)
                        .setSegmentationType(SegmentationType.SENTENCE)
                        .build();
        private static final Segmenter WORD_BREAK =
                LocalizedSegmenter.builder()
                        .setLocale(ULocale.ENGLISH)
                        .setSegmentationType(SegmentationType.WORD)
                        .build();

        private static int maxRSEntryCharacters = 0;

        private static class StringIndexer {
            public StringIndexer() {}

            public int getStringIndex(String s) {
                int result = stringIndices.getOrDefault(s, allTheStrings.length());
                if (result == allTheStrings.length()) {
                    allTheStrings.append(s).append(RECORD_SEPARATOR);
                    stringIndices.put(s, result);
                }
                return result;
            }

            @Override
            public String toString() {
                return allTheStrings.toString();
            }

            private final HashMap<String, Integer> stringIndices = new HashMap<>();
            private final StringBuilder allTheStrings = new StringBuilder();
        }

        static {
            String baseRules =
                    "'<' > '&lt;' ;"
                            + "'<' < '&'[lL][Tt]';' ;"
                            + "'&' > '&amp;' ;"
                            + "'&' < '&'[aA][mM][pP]';' ;"
                            + "'>' < '&'[gG][tT]';' ;"
                            + "'\"' < '&'[qQ][uU][oO][tT]';' ; "
                            + "'' < '&'[aA][pP][oO][sS]';' ; ";

            String contentRules = "'>' > '&gt;' ;";

            String htmlRules = baseRules + contentRules + "'\"' > '&quot;' ; '' > '&apos;' ;";

            htmlRulesControls =
                    htmlRules
                            + "[\\uD800-\\uDB7F] > '<span class=\"high-surrogate\"><span>'\uFFFD'</span></span>' ; "
                            + "[\\uDB80-\\uDBFF] > '<span class=\"private-surrogate\"><span>'\uFFFD'</span></span>' ; "
                            + "[\\uDC00-\\uDFFF] > '<span class=\"low-surrogate\"><span>'\uFFFD'</span></span>' ; "
                            + "([[:cn:][:co:][:cc:]-[:White_Space:]]) > '<span class=\"control\">'$1'</span>' ; "
                            + "([[:cc:]&[:White_Space:]]) > '<span class=\"control\">'\uFFFD'</span>' ; ";
            toHTML =
                    Transliterator.createFromRules(
                            "any-xml", htmlRulesControls, Transliterator.FORWARD);
        }

        private class IndexEntry {
            IndexEntry(int snippetIndex, UnicodeProperty property) {
                this.snippetIndex = snippetIndex;
                this.property = property;
                characters = new UnicodeSet();
            }

            List<IndexSubEntry> subEntries() {
                try {
                    return VersionedIndexer.this.subEntries(
                            /* showBlock= */ property == SUBHEADER,
                            /* showSubheader= */ property == SUBHEADER_NOTICE,
                            /* showName= */ property != NAME,
                            characters);
                } catch (Exception e) {
                    System.err.println("In entry for " + property.getName() + ": " + snippetIndex);
                    throw e;
                }
            }

            int snippetIndex;
            UnicodeProperty property;
            UnicodeSet characters;
            Map<String, UnicodeSet> relatedCharacters = new TreeMap<>();

            public UnicodeSet coveredCharacters() {
                UnicodeSet result = characters.cloneAsThawed();
                for (UnicodeSet related : relatedCharacters.values()) {
                    result.addAll(related);
                }
                return result;
            }

            public String toHTML() {
                final var subEntries = subEntries();
                final String singleEntry =
                        subEntries.size() == 1 ? subEntries.get(0).toHTML("[RESULT TEXT]") : null;
                return "<tr class=entry>"
                        + (singleEntry != null
                                ? singleEntry + "</tr>"
                                : ("<td class=entry-text>[RESULT TEXT]</td></tr>"
                                        + "<tr class=subentry>"
                                        + subEntries().stream()
                                                .map(e -> e.toHTML(""))
                                                .collect(
                                                        Collectors.joining(
                                                                "</tr>" + "<tr class=subentry>"))
                                        + "</tr>"))
                        + relatedCharacters.entrySet().stream()
                                .map(
                                        entry ->
                                                "<tr class=related><td>"
                                                        + entry.getKey()
                                                        + "</td></tr>"
                                                        + "<tr class=subentry>"
                                                        + VersionedIndexer.this
                                                                .subEntries(
                                                                        /* showBlock= */ true,
                                                                        /* showSubheader= */ false,
                                                                        /* showName= */ true,
                                                                        entry.getValue())
                                                                .stream()
                                                                .map(e -> e.toHTML(""))
                                                                .collect(
                                                                        Collectors.joining(
                                                                                "</tr>"
                                                                                        + "<tr class=subentry>"))
                                                        + "</tr>")
                                .collect(Collectors.joining());
            }
        }

        // Keyed by radical character, not radical number.
        private Map<Integer, UnicodeSet> getRadicalSets() {
            final Map<String, UnicodeSet> fastCJKRadicals = new HashMap<>();
            for (final String r : CJK_RADICAL.getAvailableValues()) {
                fastCJKRadicals.put(r, CJK_RADICAL.getSet(r));
            }
            final Map<Integer, Integer> tangutComponents = new HashMap<>();
            for (int i = 1; i < 1000; ++i) {
                int cp = i <= 768 ? 0x18800 + i - 1 : 0x18D80 + i - 769;
                if (NAME.getValue(cp) == null) {
                    break;
                }
                if (!NAME.getValue(cp).equals(String.format("TANGUT COMPONENT-%03d", i))) {
                    throw new IllegalArgumentException(NAME.getValue(cp));
                }
                tangutComponents.put(i, cp);
            }
            final Map<Integer, Integer> jurchenRadicals = new HashMap<>();
            for (int i = 1; i < 100; ++i) {
                int cp = 0x191A0 + i - 1;
                if (NAME.getValue(cp) == null) {
                    break;
                }
                if (!NAME.getValue(cp).equals(String.format("JURCHEN RADICAL-%02d", i))) {
                    throw new IllegalArgumentException(NAME.getValue(cp));
                }
                jurchenRadicals.put(i, cp);
            }
            final Map<Integer, UnicodeSet> radicalSets = new HashMap<>();
            for (int cp = 0; cp <= 0x10FFFF; ++cp) {
                for (final String rs : K_RS_UNICODE.getValues(cp)) {
                    if (rs == null) {
                        continue;
                    }
                    final String radical = rs.split("\\.")[0];
                    for (String radicalCharacter : fastCJKRadicals.get(radical)) {
                        radicalSets
                                .computeIfAbsent(
                                        radicalCharacter.codePointAt(0), c -> new UnicodeSet())
                                .add(cp);
                    }
                }
                for (final String rs : K_TGT_RS_UNICODE.getValues(cp)) {
                    if (rs == null) {
                        continue;
                    }
                    final int component = Integer.parseInt(rs.split("\\.")[0]);
                    final int componentCharacter = tangutComponents.get(component);
                    radicalSets.computeIfAbsent(componentCharacter, c -> new UnicodeSet()).add(cp);
                }
                for (final String rs : K_JURC_RS_UNICODE.getValues(cp)) {
                    if (rs == null) {
                        continue;
                    }
                    final int radical = Integer.parseInt(rs.split("\\.")[0]);
                    final int radicalCharacter = jurchenRadicals.get(radical);
                    radicalSets.computeIfAbsent(radicalCharacter, c -> new UnicodeSet()).add(cp);
                }
                for (final String r : K_SEAL_RAD.getValues(cp)) {
                    if (r == null) {
                        continue;
                    }
                    final int radicalCharacter = Utility.codePointFromHex(r.split("\\.")[1]);
                    radicalSets.computeIfAbsent(radicalCharacter, c -> new UnicodeSet()).add(cp);
                }
            }
            return radicalSets;
        }

        public void generateIndex(VersionedIndexer linkedVersion) throws IOException {
            representativeGlyphs = loadRepresentativeGlyphs();
            class PropertyComparator implements Comparator<UnicodeProperty> {
                @Override
                public int compare(UnicodeProperty left, UnicodeProperty right) {
                    return left.getName().compareTo(right.getName());
                }
            }
            final var allTheStrings = new StringIndexer();
            // Property to snippet based on property value (as an index in allTheStrings) to index
            // entry.
            Map<UnicodeProperty, Map<Integer, IndexEntry>> indexEntries =
                    new TreeMap<>(new PropertyComparator());
            // Lemma to snippet (as an index in allTheStrings) to position of the word in the
            // snippet.
            Map<String, Map<Integer, Integer>> wordIndex = new TreeMap<>();
            final var properties =
                    List.of(
                            BLOCK,
                            SUBHEADER,
                            LOCALIZED_NAME,
                            NAME_ALIAS,
                            INFORMAL_ALIAS,
                            SUBHEADER_NOTICE,
                            COMMENT,
                            K_RS_UNICODE,
                            K_TGT_RS_UNICODE,
                            K_JURC_RS_UNICODE,
                            K_SEAL_RAD);
            for (int cp = 0; cp <= 0x10FFFF; ++cp) {
                for (var prop : properties) {
                    final var propertyIndex =
                            indexEntries.computeIfAbsent(prop, k -> new TreeMap<>());
                    for (String snippet : getSnippets(prop, cp)) {
                        if (snippet == null) {
                            continue;
                        }
                        if (prop == BLOCK) {
                            if (Block_Values.forName(snippet) == Block_Values.No_Block) {
                                continue;
                            }
                            snippet = prettifyBlock.get(snippet);
                        } else if (prop == NAME) {
                            snippet = snippet.replace(Utility.hex(cp), "#");
                        }
                        final int snippetIndex = allTheStrings.getStringIndex(snippet);
                        propertyIndex
                                .computeIfAbsent(snippetIndex, k -> new IndexEntry(k, prop))
                                .characters
                                .add(cp);
                        // Override word breaking of ' and - in appropriate contexts so that
                        // radical/stroke indices are atomic.
                        // With ICU4J we could do that with custom segmentation rules, but we need
                        // to have the same segmentation in the JavaScript where we do not have that
                        // luxury, so poor man’s tailoring by segmenting a mangled string it is.
                        final String mangledForWordBreak =
                                snippet.replaceAll("\\.-", ".0")
                                        .replaceAll("(?<=[0-9]'*)'(?='*\\.[0-9])", "0");
                        final Iterable<Segment> segments =
                                WORD_BREAK
                                                .segment(mangledForWordBreak)
                                                .segments()
                                                .filter(
                                                        s ->
                                                                s.ruleStatus
                                                                        >= BreakIterator
                                                                                .WORD_NUMBER)
                                        ::iterator;
                        for (final var segment : segments) {
                            String word =
                                    snippet.substring(segment.start, segment.limit)
                                            .toLowerCase(Locale.ROOT);
                            String lemma = lemmatize(word);
                            wordIndex
                                    .computeIfAbsent(fold(word), k -> new TreeMap<>())
                                    .putIfAbsent(snippetIndex, segment.start);
                            if (!lemma.equals(fold(word))) {
                                wordIndex
                                        .computeIfAbsent(lemma, k -> new TreeMap<>())
                                        .putIfAbsent(snippetIndex, segment.start);
                            }
                        }
                    }
                }
                if (cp % 0x10000 == 0xFFFF) {
                    System.out.println("Indexed plane " + cp / 0x10000);
                }
            }
            final int bettyIndex = allTheStrings.getStringIndex("Betty");
            final int theIndex = allTheStrings.getStringIndex("the");
            indexEntries
                    .get(BLOCK)
                    .computeIfAbsent(bettyIndex, k -> new IndexEntry(k, BLOCK))
                    .characters
                    .add(BOOP);
            indexEntries
                    .get(BLOCK)
                    .computeIfAbsent(theIndex, k -> new IndexEntry(k, BLOCK))
                    .characters
                    .add(DOOD);
            wordIndex.computeIfAbsent("betty", k -> new TreeMap<>()).putIfAbsent(bettyIndex, 0);
            wordIndex.computeIfAbsent("the", k -> new TreeMap<>()).putIfAbsent(theIndex, 0);

            System.out.println("Radicals…");
            final var radicalSets = getRadicalSets();
            for (final var propertyIndex : indexEntries.entrySet()) {
                if (propertyIndex.getKey() == K_RS_UNICODE) {
                    continue;
                }
                for (final var indexEntry : propertyIndex.getValue().values()) {
                    if (indexEntry.characters.size() == 1
                            && radicalSets.containsKey(indexEntry.characters.charAt(0))) {
                        final int radical = indexEntry.characters.charAt(0);
                        final String kind =
                                NAME.getValue(radical).contains("COMPONENT")
                                        ? "component"
                                        : "radical";
                        String cjkParenthetical = CJK_RADICAL.getValue(radical);
                        cjkParenthetical =
                                cjkParenthetical == null ? "" : " (" + cjkParenthetical + ")";
                        indexEntry.relatedCharacters.put(
                                "Characters with this " + kind + cjkParenthetical + ":",
                                radicalSets.get(radical));
                    }
                }
            }

            System.out.println("Writing " + filename + "...");
            final String resources =
                    Settings.UnicodeTools.UNICODETOOLS_RSRC_DIR + "org/unicode/text/tools/";
            if (language != null) {
                new File(Settings.Output.GEN_DIR + "/" + language).mkdir();
            }
            var file = new PrintStream(new File(Settings.Output.GEN_DIR + filename));
            final var htmlTemplate =
                    new BufferedReader(
                            new FileReader(new File(resources + "charindex_template.html")));
            for (String htmlLine = htmlTemplate.readLine();
                    htmlLine != null;
                    htmlLine = htmlTemplate.readLine()) {
                if (htmlLine.contains("CSS HERE")) {
                    final var css =
                            new BufferedReader(
                                    new FileReader(new File(resources + "charindex.css")));
                    for (String cssLine = css.readLine();
                            cssLine != null;
                            cssLine = css.readLine()) {
                        file.println(cssLine);
                    }
                    css.close();
                } else if (htmlLine.contains("JS HERE")) {
                    // No pretty-printing in the loops that print these two maps; each space or
                    // newline here enlarges charindex.html by hundreds of kilobytes.  These are not
                    // suitable  for human consumption anyway, since anything readable is turned
                    // into indices in allTheStrings.
                    file.print("let wordIndex = new Map([");
                    System.out.println("wordIndex...");
                    {
                        int i = 0;
                        for (var wordAndSnippets : wordIndex.entrySet()) {
                            if (++i % 1000 == 0) {
                                System.out.println(i + "/" + wordIndex.size() + "...");
                            }
                            file.print(
                                    "['"
                                            + wordAndSnippets.getKey().replace("'", "\\'")
                                            + "',new Map([");
                            // Stream and collect for the innermost map to avoid trailing commas,
                            // for size.
                            file.print(
                                    wordAndSnippets.getValue().entrySet().stream()
                                            .map(
                                                    snippetAndPosition ->
                                                            "["
                                                                    + snippetAndPosition.getKey()
                                                                    + ","
                                                                    + snippetAndPosition.getValue()
                                                                    + "]")
                                            .collect(Collectors.joining(",")));
                            file.print("])],");
                        }
                    }
                    file.println("]);");
                    System.out.println("indexEntries...");
                    file.print("let indexEntries = new Map([");
                    for (var property : properties) {
                        System.out.println(property.getName() + "...");
                        final var propertyIndex = indexEntries.get(property);
                        file.print("['" + property.getName() + "',new Map([");
                        int i = 0;
                        for (var indexEntry : propertyIndex.values()) {
                            if (++i % 1000 == 0) {
                                System.out.println(i + "/" + propertyIndex.size() + "...");
                            }
                            final int htmlIndex = allTheStrings.getStringIndex(indexEntry.toHTML());
                            file.print("[" + indexEntry.snippetIndex + ",{");
                            file.print("html:" + htmlIndex + ",");
                            file.print("characters:[");
                            // Stream and collect for the innermost array to avoid trailing commas,
                            // for size.
                            file.print(
                                    indexEntry
                                            .coveredCharacters()
                                            .rangeStream()
                                            .map(
                                                    range ->
                                                            // Code points in decimal without
                                                            // zero-padding for size.
                                                            "["
                                                                    + range.codepoint
                                                                    + (range.codepointEnd
                                                                                    != range.codepoint
                                                                            ? ","
                                                                                    + range.codepointEnd
                                                                            : "")
                                                                    + "]")
                                            .collect(Collectors.joining(",")));
                            file.print("]}],");
                        }
                        file.print("])],");
                    }
                    file.println("]);");
                    file.println("let bettyIndex = " + bettyIndex + ";");
                    file.println("let theIndex = " + theIndex + ";");
                    final var compressed = new ByteArrayOutputStream();
                    final var compressor = new DeflaterOutputStream(compressed);
                    final var uncompressed = allTheStrings.toString().getBytes("UTF-8");
                    compressor.write(uncompressed);
                    compressor.close();
                    final var compressedBytes = compressed.toByteArray();
                    System.out.println(
                            "Strings compressed from "
                                    + (uncompressed.length >> 20)
                                    + " MiB to "
                                    + (compressedBytes.length >> 10)
                                    + " kiB ("
                                    + 100 * compressedBytes.length / uncompressed.length
                                    + "%)");
                    System.out.println(
                            "Compressed payload is "
                                    + compressedBytes.length
                                    + " bytes, first byte is "
                                    + Byte.toUnsignedInt(compressedBytes[0]));
                    file.println(
                            "let allTheStringsCompressed = '"
                                    + Base64.getEncoder().encodeToString(compressedBytes)
                                    + "'");
                    final var js =
                            new BufferedReader(
                                    new FileReader(new File(resources + "charindex.js")));
                    for (String jsLine = js.readLine(); jsLine != null; jsLine = js.readLine()) {
                        if (jsLine.contains("GENERATED LINE")) {
                            continue;
                        }
                        file.println(jsLine);
                    }
                    js.close();
                } else {
                    file.println(
                            htmlLine.replace(
                                            "<!--VERSION HERE-->",
                                            version.getVersionString(2, 2) + phase)
                                    .replace(
                                            "<!--FULL-VERSION-HERE-->",
                                            version.getVersionString(3, 3) + phase)
                                    .replace("CHARTS-ROOT-HERE", chartsRoot)
                                    .replace(
                                            "LANDING-PAGE-HERE",
                                            phase == ReleasePhase.ALPHA
                                                    ? "https://www.unicode.org/versions/alpha-"
                                                            + version.getVersionString(3, 3)
                                                            + ".html"
                                                    : phase == ReleasePhase.BETA
                                                            ? "https://www.unicode.org/versions/beta-"
                                                                    + version.getVersionString(3, 3)
                                                                    + ".html"
                                                            : "https://www.unicode.org/versions/Unicode"
                                                                    + version.getVersionString(
                                                                            3, 3))
                                    .replace(
                                            "<!--DRAFT LINK HERE-->",
                                            linkedVersion == null
                                                    ? ""
                                                    : "<p class=body-width id=see-version>(See also <a href=\""
                                                            + linkedVersion.filename
                                                            + "\">Unicode "
                                                            + linkedVersion.version
                                                                    .getVersionString(2, 2)
                                                            + linkedVersion.phase
                                                            + "</a>)</p>"));
                }
            }
            htmlTemplate.close();
            file.close();

            System.out.println(wordIndex.size() + " words");
            System.out.println(
                    indexEntries.values().stream().collect(Collectors.summingInt(Map::size))
                            + " index entries");
            System.out.println("Max characters in RS entries: " + maxRSEntryCharacters);
        }

        private Iterable<String> getSnippets(UnicodeProperty prop, int cp) {
            if (prop == SUBHEADER_NOTICE || prop == COMMENT) {
                return StreamSupport.stream(prop.getValues(cp).spliterator(), false)
                                .filter(Objects::nonNull)
                                .flatMap(s -> SENTENCE_BREAK.segment(s).segments())
                                .map(Segment::getSubSequence)
                                .map(CharSequence::toString)
                                .map(String::strip)
                        ::iterator;
            } else if (prop == INFORMAL_ALIAS) {
                return StreamSupport.stream(prop.getValues(cp).spliterator(), false)
                                .filter(Objects::nonNull)
                                .flatMap(s -> Arrays.stream(s.split("[,;]")))
                                .map(String::strip)
                        ::iterator;
            } else if (prop == K_RS_UNICODE
                    || prop == K_TGT_RS_UNICODE
                    || prop == K_JURC_RS_UNICODE) {
                final String rsKind =
                        (prop == K_RS_UNICODE
                                        ? "CJK "
                                        : prop == K_TGT_RS_UNICODE ? "Tangut " : "Jurchen ")
                                + " radical-stroke ";
                return StreamSupport.stream(prop.getValues(cp).spliterator(), false)
                                .filter(Objects::nonNull)
                                .map(s -> rsKind + s)
                        ::iterator;
            } else if (prop == K_SEAL_RAD) {
                // The kSealRad property maps Seal characters to numbered radicals, which are
                // seal characters; the property values are <radical number>.<code point>.
                // If a seal character is a radical, it is mapped to itself; if we find such a
                // self-mapping, add an index entry "Seal radical <number>".
                for (final String value : prop.getValues(cp)) {
                    if (value == null) {
                        continue;
                    }
                    final String[] parts = value.split("\\.");
                    if (Utility.codePointFromHex(parts[1]) != cp) {
                        continue;
                    }
                    return List.of("Seal radical " + parts[0]);
                }
                return List.of();
            } else {
                return prop.getValues(cp);
            }
        }

        private static String fold(String word) {
            // TODO(egg): collation folding.
            // Maybe some of it before segmentation.
            String folding = NFKC.normalize(word).toLowerCase(Locale.ROOT);
            return folding.replace("š", "sh");
        }

        private static String lemmatize(String word) {
            // TODO(egg): proper lemmatization.
            String lemma = fold(word);
            if (lemma.endsWith("ses") && lemma.length() > 4) {
                lemma = lemma.substring(0, lemma.length() - 2);
            } else if (lemma.endsWith("s") && !lemma.endsWith("ss") && lemma.length() > 2) {
                lemma = lemma.substring(0, lemma.length() - 1);
            }
            if (lemma.matches("^[0-9]*[1-9][0-9]*$")) {
                lemma = lemma.replaceAll("^0+", "");
            }
            return lemma;
        }

        private static class IndexSubEntry {
            String block;
            String subheader;
            String chartLink;
            String ranges;
            String characters;
            boolean rsEntry = false;

            @Override
            public String toString() {
                StringBuilder result = new StringBuilder("        ");
                if (subheader != null) {
                    result.append(subheader + ". ");
                }
                if (block != null) {
                    result.append("In " + block + ": ");
                }
                result.append(ranges);
                return result.toString();
            }

            public String toHTML(String entryText) {
                StringBuilder result = new StringBuilder();
                result.append("<td class=entry-text>");
                result.append(entryText);
                result.append("<span class=location>");
                if (subheader != null) {
                    result.append(toHTML.transform(subheader) + ". ");
                }
                if (block != null) {
                    result.append("In " + block + ": ");
                }
                result.append("</span>");
                result.append("</td><td class='ranges");
                if (rsEntry) {
                    result.append(" rs-entry");
                }
                result.append("'><a href='" + chartLink + "'>");
                result.append(toHTML.transform(ranges));
                result.append("</a>");
                result.append("</td><td class='characters");
                if (rsEntry) {
                    result.append(" rs-entry");
                }
                result.append("'>");
                if (characters != null) {
                    result.append(characters);
                }
                result.append("</td>");
                return result.toString();
            }
        }

        private List<IndexSubEntry> subEntries(
                boolean showBlock, boolean showSubheader, boolean showName, UnicodeSet characters) {
            List<IndexSubEntry> result = new ArrayList<>();
            final boolean showBlocks =
                    showBlock
                            || showSubheader
                            || !blockSet.get(BLOCK.getValue(characters.charAt(0)))
                                    .containsAll(characters);
            if (!showBlocks) {
                result.add(new IndexSubEntry());
            }
            IndexSubEntry previousSubEntryWithLocation = null;
            for (var range : characters.ranges()) {
                if (range.codepointEnd == range.codepoint) {
                    if (showBlocks) {
                        result.add(new IndexSubEntry());
                        result.get(result.size() - 1).block =
                                PRETTY_BLOCK.getValues(range.codepoint).iterator().next();
                    }
                    final var currentSubEntry = result.get(result.size() - 1);
                    if (showSubheader) {
                        currentSubEntry.subheader = SUBHEADER.getValue(range.codepoint);
                    }
                    currentSubEntry.chartLink =
                            getChartLink(new UnicodeSet(range.codepoint, range.codepoint));
                    currentSubEntry.ranges = Utility.hex(range.codepoint);
                    currentSubEntry.characters = representativeGlyphs.get(range.codepoint);
                    if (range.codepoint == BOOP || range.codepoint == DOOD) {
                        currentSubEntry.chartLink = "https://unicode.org/charts/PDF/UBOOP.pdf";
                        currentSubEntry.ranges = range.codepoint == BOOP ? "BOOP" : "DOOD";
                    }
                    if (previousSubEntryWithLocation != null
                            && Objects.equals(
                                    currentSubEntry.subheader,
                                    previousSubEntryWithLocation.subheader)
                            && Objects.equals(
                                    currentSubEntry.block, previousSubEntryWithLocation.block)) {
                        currentSubEntry.subheader = null;
                        currentSubEntry.block = null;
                    } else {
                        previousSubEntryWithLocation = currentSubEntry;
                    }
                } else {
                    UnicodeSet remainder = new UnicodeSet(range.codepoint, range.codepointEnd);
                    while (!remainder.isEmpty()) {
                        final String blockValue = BLOCK.getValue(remainder.charAt(0));
                        final var currentBlock = blockSet.get(blockValue);
                        if (showBlocks) {
                            result.add(new IndexSubEntry());
                            result.get(result.size() - 1).block =
                                    PRETTY_BLOCK.getValues(remainder.charAt(0)).iterator().next();
                        }
                        final var subrange = remainder.cloneAsThawed().retainAll(currentBlock);
                        remainder.removeAll(currentBlock);
                        final var currentSubEntry = result.get(result.size() - 1);
                        if (showSubheader) {
                            currentSubEntry.subheader = SUBHEADER.getValue(range.codepoint);
                        }
                        currentSubEntry.chartLink = getChartLink(subrange);
                        currentSubEntry.ranges =
                                Utility.hex(subrange.getRangeStart(0))
                                        + "–"
                                        + Utility.hex(subrange.getRangeEnd(0));
                        rsProperties:
                        for (var rsProperty :
                                new UnicodeProperty[] {
                                    K_RS_UNICODE, K_TGT_RS_UNICODE, K_JURC_RS_UNICODE
                                }) {
                            Set<String> commonValues = new HashSet<>();
                            commonValues.addAll(
                                    Lists.newArrayList(
                                            rsProperty
                                                    .getValues(subrange.getRangeStart(0))
                                                    .iterator()));
                            commonValues.remove(null);
                            for (int cp : subrange.codePoints()) {
                                commonValues.retainAll(
                                        Lists.newArrayList(rsProperty.getValues(cp).iterator()));
                                if (commonValues.isEmpty()) {
                                    continue rsProperties;
                                }
                            }
                            currentSubEntry.characters =
                                    subrange.stream().map(s -> s.codePointAt(0)).map(representativeGlyphs::get).collect(Collectors.joining());
                            currentSubEntry.rsEntry = true;
                            maxRSEntryCharacters = Math.max(maxRSEntryCharacters, subrange.size());
                        }
                        final String firstGC = GENERAL_CATEGORY.getValue(subrange.getRangeStart(0));
                        if (currentSubEntry.characters == null
                                && GENERAL_CATEGORY.getSet(firstGC).containsAll(subrange)) {
                            currentSubEntry.characters =
                                    representativeGlyphs.get(subrange.getRangeStart(0))
                                            + "–"
                                            + representativeGlyphs.get(subrange.getRangeEnd(0));
                        }
                        if (previousSubEntryWithLocation != null
                                && Objects.equals(
                                        currentSubEntry.subheader,
                                        previousSubEntryWithLocation.subheader)
                                && Objects.equals(
                                        currentSubEntry.block,
                                        previousSubEntryWithLocation.block)) {
                            currentSubEntry.subheader = null;
                            currentSubEntry.block = null;
                        } else {
                            previousSubEntryWithLocation = currentSubEntry;
                        }
                    }
                }
            }
            return result;
        }

        private String getChartLink(UnicodeSet set) {
            int chartStart;
            var blockValue = UcdPropertyValues.Block_Values.forName(BLOCK.getValue(set.charAt(0)));
            boolean blockHasNewCharacters = false;
            if (NONCHARACTERS.containsAll(set)
                    && (blockValue == Block_Values.No_Block
                            || blockValue == Block_Values.Supplementary_Private_Use_Area_A
                            || blockValue == Block_Values.Supplementary_Private_Use_Area_B)) {
                chartStart = set.charAt(0) & 0xFF_FF80;
            } else {
                if (blockValue == Block_Values.No_Block) {
                    throw new IllegalArgumentException(
                            "Getting chart for No_Block characters " + set);
                }
                if (blockValue == Block_Values.High_Private_Use_Surrogates) {
                    blockValue = Block_Values.High_Surrogates;
                }
                final var currentBlock = blockSet.get(blockValue.toString());
                chartStart = currentBlock.getRangeStart(0);
                blockHasNewCharacters = currentBlock.containsSome(NEW_CHARACTERS);
            }
            switch (phase) {
                // TODO(egg): Figure out where those live in 19.0α and β.
                case ALPHA:
                    if (blockHasNewCharacters) {
                        return "https://www.unicode.org/charts/PDF/Unicode-"
                                + version.getVersionString(2, 2)
                                + "/U"
                                + version.getVersionString(2, 2).replace(".", "")
                                + "-"
                                + Utility.hex(chartStart)
                                + ".pdf";
                    } else {
                        return "https://unicode.org/charts/PDF/U"
                                + Utility.hex(chartStart)
                                + ".pdf";
                    }
                case BETA:
                    return "https://www.unicode.org/Public/draft/charts/PDF/U"
                            + Utility.hex(chartStart)
                            + ".pdf";
                default:
                    return "https://unicode.org/charts/PDF/U" + Utility.hex(chartStart) + ".pdf";
            }
        }
    }

    public static final void main(String[] args) throws IOException {
        /*System.err.println(signedArea(
            new Cubic(new Point(21, -12), 
            new Displacement(2.3263950580876793, 3.4895925871315194), 
            new Displacement(34.6573492121799, 5.3426507878201),
            new Displacement(41, -1))));
        final var q = new Quadratic(new Point(2, 3), 
            new Displacement(-4-2,-5-3),
            new Displacement(6-2,7-3));
        System.err.println(q.evaluate(0));
        System.err.println(q.evaluate(.5));
        System.err.println(q.evaluate(1));
        System.err.println(signedArea(
            q));
        final var piecewise = new PiecewiseFunction(
            new Quadratic(new Point(21, -12),
            new Displacement(6, 9),
            new Displacement(18, 9)));
        piecewise.pieces.add(
            new Quadratic(new Point(39.0,-3.0), 
            new Displacement(13.0,0.0), 
        new Displacement(23.0,-10.0)));
        System.err.println(piecewise.evaluate(0));
        System.err.println(piecewise.evaluate(.25));
        System.err.println(piecewise.evaluate(.5));
        System.err.println(piecewise.evaluate(.75));
        System.err.println(piecewise.evaluate(1));
        System.err.println(signedArea(piecewise));
        System.exit(2);*/
        final var main =
                new VersionedIndexer(
                        Settings.LAST_VERSION_INFO,
                        ReleasePhase.GAMMA,
                        Settings.LAST2_VERSION_INFO,
                        "https://www.unicode.org/charts",
                        "charindex.html",
                        /* language= */ null);
        final var draft =
                new VersionedIndexer(
                        Settings.LATEST_VERSION_INFO,
                        Settings.latestVersionPhase,
                        Settings.LAST_VERSION_INFO,
                        Settings.latestVersionPhase == ReleasePhase.GAMMA
                                ? "https://www.unicode.org/charts"
                                : "https://www.unicode.org/Public/draft/charts",
                        "charindex-draft.html",
                        /* language= */ null);
        // Link to the draft if it is at least in α (i.e., do not link to a pre-α dev version), but
        // not if it is γ (i.e., do not link to a file that looks release-final but isn’t).
        if(false)main.generateIndex(
                /* linkedVersion= */ Settings.latestVersionPhase.compareTo(ReleasePhase.ALPHA) >= 0
                                && Settings.latestVersionPhase.compareTo(ReleasePhase.GAMMA) < 0
                        ? draft
                        : null);
        // Link from the draft to the earlier version unless the draft is release-final (γ).
        draft.generateIndex(
                /* linkedVersion= */ Settings.latestVersionPhase.compareTo(ReleasePhase.GAMMA) < 0
                        ? main
                        : null);
        final var fr =
                new VersionedIndexer(
                        Settings.LAST_VERSION_INFO,
                        ReleasePhase.GAMMA,
                        Settings.LAST2_VERSION_INFO,
                        "https://www.unicode.org/charts/fr",
                        "fr/charindex.html",
                        "fr");
        if(false)fr.generateIndex(/* linkedVersion= */ null);
    }

    private static Map<Integer, String> loadRepresentativeGlyphs() throws IOException {
        System.out.println("Loading representative glyphs…");
        final Map<Integer, String> result = new HashMap<>();
        final var id = Pattern.compile("id=\"([0-9a-f]{4,})\"");
        final var rows = new File(Settings.UnicodeTools.UNICODETOOLS_REPO_DIR + "/../representative-glyphs/ranges").listFiles();
        Arrays.sort(rows, Comparator.<File, Integer>comparing(f -> f.getName().length()).thenComparing(f -> f.getName()));
        for (final var row : rows) {
            System.err.println("Glyphs for row " + row.getName() +"...");
            try (final var rowGlyphs =
                    new BufferedReader(
                            new FileReader(row))) {
                for (var line = rowGlyphs.readLine(); line != null; line = rowGlyphs.readLine()) {
                    final var matcher = id.matcher(line);
                    if (matcher.find()) {
                        final int cp = Utility.codePointFromHex(matcher.group(1));
                        final int start = line.indexOf("<svg");
                        if (start == -1) {
                            continue;
                        }
                        final int end = line.indexOf("</svg>");
                        String svg = line.substring(start, end + 6);
                        //System.err.println(Utility.hex(cp));
                        if (false&&cp > 0x1F00) {
                            return result;
                        }
                        current_cp = cp;
                        svg = mangleSVG(svg);
                        svg = "<span class=character>" + svg + "<span class=literal>" + VersionedIndexer.toHTML.transform(Character.toString(cp)) + "</span></span>";
                        result.put(cp, svg);
                    }
                }
            }
        }
        return result;
    }

    private static int current_cp;

    private static double parseNumber(String source, ParsePosition pos) {
        final var NUMBER = Pattern.compile("\\s*-?[\\d.]+");
        final var matcher = NUMBER.matcher(source);
        matcher.region(pos.getIndex(), source.length());
        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException(source.substring(matcher.regionStart()));
        }
        pos.setIndex(matcher.end());
        return Double.parseDouble(matcher.group());
    }

    private static class Point {
        static Point parse(String source, ParsePosition pos) {
            double x = parseNumber(source, pos);
            double y = parseNumber(source, pos);
            return new Point(x, y);
        }
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        Displacement minus(Point q) {
            return new Displacement(x - q.x, y - q.y);
        }
        Point plus(Displacement d) {
            return new Point(x + d.x, y + d.y);
        }
        Point minus(Displacement d) {
            return new Point(x - d.x, y - d.y);
        }
        Point round() {
            return new Point((double)Math.round(x), (double)Math.round(y));
        }
        @Override 
        public boolean equals(Object other) {
            if (!(other instanceof Point)) {
                return false;
            }
            final var otherPoint = (Point)other;
            return otherPoint.x == x && otherPoint.y == y;
        }
        @Override 
        public String toString() {
            return "(" + x + "," + y + ")";
        }
        final double x;
        final double y;
    }

    private static class Displacement {
        Displacement(double x, double y) {
            this.x = x;
            this.y = y;
        }
        Displacement plus(Displacement d) {
            return new Displacement(x + d.x, y + d.y);
        }
        Displacement minus(Displacement d) {
            return new Displacement(x - d.x, y - d.y);
        }
        Displacement times(double λ) {
            return new Displacement(λ * x, λ * y);
        }
        double dot(Displacement v) {
            return x * v.x + y * v.y;
        }
        double squareNorm() {
            return this.dot(this);
        }
        double norm() {
            return Math.sqrt(squareNorm());
        }
        Displacement round() {
            return new Displacement((double)Math.round(x), (double)Math.round(y));
        }
        @Override 
        public String toString() {
            return "(" + x + "," + y + ")";
        }
        final double x;
        final double y;
    }

    private static class Transform {
        double translation_x;
        double translation_y;
        double scale_x;
        double scale_y;
        Point apply(Point q) {
            return new Point(scale_x * q.x + translation_x, scale_y * q.y + translation_y);
        }
    }

    private static String mangleSVG(String svg) {
        final var result = new StringBuilder();
        final var SVG = Pattern.compile("<svg viewBox=\"(?<minx>-?\\d+) (?<miny>-?\\d+) (?<width>\\d+) (?<height>\\d+)\" data-bounds=\"[\\d -]+\">");
        final var PATH = Pattern.compile("<path transform=\"(?<transform>[^\"]+)\" d=\"(?<commands>[^\\\"]+)\" />");
        var matcher = SVG.matcher(svg);
        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException(svg);
        }
        final double minx = Double.parseDouble(matcher.group("minx"));
        final double miny = Double.parseDouble(matcher.group("miny"));
        final double height = Double.parseDouble(matcher.group("height"));
        final double width = Double.parseDouble(matcher.group("width"));
        if (height != 22528) {
            throw new IllegalArgumentException("Unexpected height " + height);
        }
        final double scale = 100 / height;
        result.append("<svg viewbox=\"" + Math.round(minx * scale) + " " + Math.round(miny * scale) + " " + Math.round(width * scale) + " 100\">");
        result.append("<path d=\"");
        for (;;) {
            matcher = PATH.matcher(svg).region(matcher.end(), svg.length());
            if (!matcher.lookingAt()) {
                break;
            }
            final var transform = parseTransform(matcher.group("transform"));
            transform.translation_x *= scale;
            transform.translation_y *= scale;
            transform.scale_x *= scale;
            transform.scale_y *= scale;
            result.append(transformCommands(matcher.group("commands"), transform));
        }
        if (!svg.substring(matcher.regionStart()).equals("</svg>")) {
            throw new IllegalArgumentException(svg.substring(matcher.regionStart()));
        }
        result.append("\"/></svg>");
        return result.toString();
    }

    private static Transform parseTransform(String transform) {
        final var SCALE = Pattern.compile("(?:translate\\((?<tx>-?[\\d.]+)(?: (?<ty>-?[\\d.]+))?\\) )?scale\\((?<x>-?[\\d.]+) (?<y>-?[\\d.]+)\\)");
        var matcher = SCALE.matcher(transform);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(transform);
        }
        final var result = new Transform();
        result.scale_x = Double.parseDouble(matcher.group("x"));
        result.scale_y = Double.parseDouble(matcher.group("y"));
        result.translation_x = matcher.group("tx") == null ? 0 : Double.parseDouble(matcher.group("tx"));
        result.translation_y = matcher.group("ty") == null ? 0 : Double.parseDouble(matcher.group("ty"));
        return result;
    }

    private static class PathBuilder {
        void appendInteger(long i) {
            if(!result.isEmpty() && Character.isDigit(result.charAt(result.length() - 1)) && i >= 0) {
                result.append(" ");
            }
            result.append(i);
        }
        void appendIntegerPoint(Point q) {
            appendInteger(Math.round(q.x));
            appendInteger(Math.round(q.y));
        }
        void appendIntegerDisplacement(Displacement d) {
            appendInteger(Math.round(d.x));
            appendInteger(Math.round(d.y));
        }
        void append(char c) {
            flushCurrent();
            result.append(c);
        }
        void append(Curve γ) {
            if (current == null) {
                current = new PiecewiseFunction(γ);
            } else {
                current.pieces.add(γ);
                if (errorMetric(current.quadraticInterpolant(), current) > TOLERANCE
                    //&&errorMetric(current.cubicInterpolant(), current) > TOLERANCE
                    ) {
                    current.pieces.removeLast();
                    flushCurrent();
                    current = new PiecewiseFunction(γ);
                }
        }
        }
        void flushCurrent() {
            if (current == null) {
                return;
            }
            Line linearInterpolant = current.linearInterpolant();
            if (errorMetric(linearInterpolant, current) > TOLERANCE) {
                final var quadraticInterpolant = current.quadraticInterpolant();
                if (false&&errorMetric(quadraticInterpolant, current) > TOLERANCE) {
                    final var cubicInterpolant = current.cubicInterpolant();
                    result.append('c');
                    appendIntegerDisplacement(cubicInterpolant.control1);
                    appendIntegerDisplacement(cubicInterpolant.control2);
                    appendIntegerDisplacement(cubicInterpolant.end);
                } else {
                    result.append('q');
                    appendIntegerDisplacement(quadraticInterpolant.control);
                    appendIntegerDisplacement(quadraticInterpolant.end);
                }
            } else {
                result.append('l');
                appendIntegerDisplacement(linearInterpolant.end);
            }
            current = null;
        }
        @Override public String toString() {
            if (current != null) {
                throw new IllegalArgumentException(current.toString());
            }
            if (current_cp == 0x954 && result.toString().startsWith("m22-55")) {
                //throw new IllegalArgumentException(result.toString());
            }
            return result.toString();
        }
        StringBuilder result = new StringBuilder();
        PiecewiseFunction current;
    }

    private static interface Curve {
        Point evaluate(double t);
        Displacement initialDerivative();
        Displacement finalDerivative();
    }

    private static class Line implements Curve {
        Line(Point start, Displacement end) {
            this.start = start;
            this.end = end;
        }
        public Point evaluate(double t) {
            return start.plus(end.times(t));
        }
        public Displacement initialDerivative() {
            return end;
        }
        public Displacement finalDerivative() {
            return end;
        }
        @Override 
        public String toString() {
            return "M" + start + " l " + end;
        }
        Point start;
        Displacement end;
    }

    private static class Quadratic implements Curve {
        Quadratic(Point start, Displacement control, Displacement end) {
            this.start = start;
            this.control = control;
            this.end = end;
        }
        public Point evaluate(double t) {
            return start.plus(control.times(2*(1-t)).plus(end.times(t)).times(t));
        }
        public Displacement initialDerivative() {
            return control;
        }
        public Displacement finalDerivative() {
            return end.minus(control);
        }
        @Override 
        public String toString() {
            return "M" + start + " q " + control + " " + end;
        }
        Point start;
        Displacement control;
        Displacement end;
    }

    private static class Cubic implements Curve {
        Cubic(Point start, Displacement control1, Displacement control2, Displacement end) {
            this.start = start;
            this.control1 = control1;
            this.control2 = control2;
            this.end = end;
        }
        public Displacement initialDerivative() {
            return control1;
        }
        public Displacement finalDerivative() {
            return end.minus(control2);
        }
        public Point evaluate(double t) {
            return start.plus(
                control1.times(3*(1-t)*(1-t))
                .plus(control2.times(3*(1-t)).plus(end.times(t)).times(t))
                    .times(t));
        }
        @Override 
        public String toString() {
            return "M" + start + " c " + control1 + " " + control2 + " " + end;
        }
        Point start;
        Displacement control1;
        Displacement control2;
        Displacement end;
    }

    private static class PiecewiseFunction implements Curve {
        PiecewiseFunction(Curve γ) {
            pieces = new ArrayList<>();
            pieces.add(γ);
        }
        List<Curve> pieces;
        public Displacement initialDerivative() {
            return pieces.get(0).initialDerivative();
        }
        public Displacement finalDerivative() {
            return pieces.get(pieces.size() - 1).finalDerivative();
        }
        public Point evaluate(double t) {
            int piece = (int)(t * pieces.size());
            if (t == 1) {
                --piece;
            }
            return pieces.get(piece).evaluate(pieces.size() * t - piece);
        }
        Line linearInterpolant() {
            final var start = pieces.get(0).evaluate(0);
            final var end = pieces.get(pieces.size() - 1).evaluate(1);
            return new Line(start, end.minus(start));
        }

        Quadratic quadraticInterpolant() {
            final var start = pieces.get(0).evaluate(0);
            final var end = pieces.get(pieces.size() - 1).evaluate(1);
            final var initialDerivative = initialDerivative();
            final var finalDerivative = finalDerivative();
            final double denominator = initialDerivative.y * finalDerivative.x - initialDerivative.x * finalDerivative.y;
            final Point controlPoint;
            if (denominator == 0) {
                controlPoint = (start.plus(end.minus(start).times(0.5))).round();
            } else {
            final double a = ((end.y - start.y) * finalDerivative.x + (start.x - end.x) * finalDerivative.y) / denominator;
            final double b = ((end.y - start.y) * initialDerivative.x + (start.x - end.x) * initialDerivative.y) / denominator;
             controlPoint = (start.plus(initialDerivative.times(a))).round();
            if (false&& !controlPoint.equals(end.plus(finalDerivative.times(b)).round())
            ||denominator != 0&&current_cp==0x954&&controlPoint.minus(start).x < -43) {
                throw new IllegalArgumentException(toString()+"-->start="+start+",end="+end+",initialDerivative="+initialDerivative
                    +",finalDerivative="+finalDerivative+",a="+a+",b="+b+",controlPoint="+controlPoint+",end.plus(finalDerivative.times(b)).round()="+end.plus(finalDerivative.times(b)).round()
                    +"unrounded:" + start.plus(initialDerivative.times(a))+ ","+end.plus(finalDerivative.times(b))
                );
            }
        }
            if (current_cp==0x954) System.err.println(denominator + " " + (controlPoint.minus(start).x < -43) + " " +controlPoint.minus(start));
            if (current_cp==0x954) System.err.println(new Quadratic(start, controlPoint.minus(start), end.minus(start)));
            return new Quadratic(start, controlPoint.minus(start), end.minus(start));
        }

        Cubic cubicInterpolant() {
            final var start = pieces.get(0).evaluate(0);
            final var end = pieces.get(pieces.size() - 1).evaluate(1);
            final var d = end.minus(start);
            final var initialDerivative = initialDerivative();
            final var finalDerivative = finalDerivative();
            final double a = signedArea(this);
            if (current_cp=='C'&&start.x==21) System.err.println("C target a="+a);
            final double αMax = Math.min((10 * a)/(3 * (d.y * initialDerivative.x - d.x * initialDerivative.y)),100);
            if (αMax <= 0 || Double.isNaN(αMax)) {
                return new Cubic(start, initialDerivative.round(), end.minus(finalDerivative).minus(start).round(), end.minus(start));
            }
            final double αOptimal =
            Brent((α) -> {
                double β = (20 * a - 6 *d.y * α * initialDerivative.x + 6 * d.x * α  * initialDerivative.y)/(
                6 * d.y * finalDerivative.x - 3 * α * initialDerivative.y  * finalDerivative.x - 
                6 * d.x  * finalDerivative.y + 3 * α * initialDerivative.x * finalDerivative.y);
                if (Double.isNaN(β)) {
                    β = -1;  // TODO(egg): This should not happen; factor out the d∧γ′(0)
                }
                if (current_cp=='C'&&start.x==21)System.err.println("C interpolant:"+new Cubic(start, initialDerivative.times(α), end.plus(finalDerivative.times(β)).minus(start) , end.minus(start)));
                if (current_cp=='C'&&start.x==21)System.err.println("C interpolant β="+β);
                if (current_cp=='C'&&start.x==21)System.err.println("C interpolant a="+signedArea(new Cubic(start, initialDerivative.times(α), end.plus(finalDerivative.times(β)).minus(start) , end.minus(start))));
                return errorMetric(this, new Cubic(start, initialDerivative.times(α), end.plus(finalDerivative.times(β)).minus(start) , end.minus(start)));
            }, 0, αMax, Comparator.naturalOrder());
            double β = (20 * a - 6 *d.y * αOptimal * initialDerivative.x + 6 * d.x * αOptimal  * initialDerivative.y)/(
                        6 * d.y * finalDerivative.x - 3 * αOptimal * initialDerivative.y  * finalDerivative.x - 
                        6 * d.x  * finalDerivative.y + 3 * αOptimal * initialDerivative.x * finalDerivative.y);
            if (current_cp=='C'&&start.x==21) System.err.println("C αOptimal="+αOptimal);
            return new Cubic(start, initialDerivative.times(αOptimal).round(), end.plus(finalDerivative.times(β)).minus(start).round(), end.minus(start));
        }
        @Override 
        public String toString() {
            return pieces.stream().map(Curve::toString).collect(Collectors.joining(" "));
        }
    }

    private static double errorMetric(Curve γ1, Curve γ2) {
        final int steps = 100;
        var q1_previous = γ1.evaluate(0);
        var q2_previous = γ2.evaluate(0);
        double result = 0;
        double arcLength1 = 0;
        double arcLength2 = 0;
        for (int i = 0; i < steps; ++i) {
            final double t1 = (i + 1.0) / steps;
            final var γ1_t1 = γ1.evaluate(t1);
            final Function<Double, Double> distance = t2 -> (γ1_t1.minus(γ2.evaluate(t2))).norm();
            result = Math.max(result, distance.apply(Brent(distance, 0, 1, Comparator.naturalOrder(), 0.01)));
        }
        if (current_cp=='C') {
            System.err.println("C error between "+γ1 +" and "+γ2 +":\n"+result);
        }
        return result;
    }

    private static double signedArea(Curve γ) {
        final int steps = 100;
        final Point γ0 = γ.evaluate(0);
        var r_previous = new Displacement(0, 0);
        double result = 0;
        for (int i = 0; i < steps; ++i) {
            double t = (i + 1.0) / steps;
            final var r = γ.evaluate(t).minus(γ0);
            final var dr = r.minus(r_previous);
            result += (r.x * dr.y - r.y * dr.x) / 2;
            r_previous = r;
        }
        return result;
    }

    private final static double TOLERANCE = 2;

    private static String transformCommands(String commands, Transform transform) {
        final var result = new PathBuilder();
        char implicitCommand = 0;
        var lastPosition = new Point(0, 0);
        var pathStart = new Point(0, 0);
        for (ParsePosition pos = new ParsePosition(0); pos.getIndex() < commands.length();) {
            char command = commands.charAt(pos.getIndex());
            if (command == ' ') {
                command = implicitCommand;
            }
            pos.setIndex(pos.getIndex() + 1);
            switch (command) {
                case 'M': {
                    result.append(Character.toLowerCase(command));
                    final var to = transform.apply(Point.parse(commands, pos)).round();
                    result.appendIntegerDisplacement(to.minus(lastPosition));
                    lastPosition = to;
                    pathStart = lastPosition;
                    break;
                }
                case 'L': {
                    final var to = transform.apply(Point.parse(commands, pos)).round();
                    result.append(new Line(lastPosition, to.minus(lastPosition)));
                    lastPosition = to;
                    break;
                }
                case 'Q': {
                    final var control = transform.apply(Point.parse(commands, pos)).round();
                    final var to = transform.apply(Point.parse(commands, pos)).round();
                    final var c = control.minus(lastPosition);
                    final var d = to.minus(lastPosition);
                    result.append(new Quadratic(lastPosition, c, d));
                    lastPosition = to;
                    break;
                }
                case 'C': {
                    final var control1 = transform.apply(Point.parse(commands, pos)).round();
                    final var control2 = transform.apply(Point.parse(commands, pos)).round();
                    final var to = transform.apply(Point.parse(commands, pos)).round();
                    result.append(new Cubic(lastPosition, control1.minus(lastPosition), control2.minus(lastPosition), to.minus(lastPosition)));
                    lastPosition = to;
                    break;
                }
                case 'V': {
                    result.append(Character.toLowerCase(command));
                    double y = parseNumber(commands, pos);
                    final var to = new Point(lastPosition.x, transform.translation_y + transform.scale_y * y).round();
                    result.appendInteger(Math.round(to.y - lastPosition.y));
                    lastPosition = to;
                    break;
                }
                case 'H': {
                    result.append(Character.toLowerCase(command));
                    double x = parseNumber(commands, pos);
                    final var to = new Point(transform.translation_x + transform.scale_x * x, lastPosition.y).round();
                    result.appendInteger(Math.round(to.x - lastPosition.x));
                    lastPosition = to;
                    break;
                }
                case 'Z':
                    result.append(command);
                    lastPosition = pathStart;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected command " + command);
            }
            implicitCommand = command;
            if (implicitCommand == 'M') {
                implicitCommand = 'L';
            }
        }
        return result.toString();
    }

    private static double sign(double x) {
        return Math.copySign(1, x);
    }

    private static double Brent(Function<Double, Double> f, double lowerBound, double upperBound) {
                System.err.println("Brent zero");
        final double f_upper = f.apply(upperBound);
        final double f_lower = f.apply(lowerBound);
        if (f_upper == 0) {
        return upperBound;
        }
        if (f_lower == 0) {
        return lowerBound;
        }
         if(sign(f_lower) == sign(f_upper))
            throw new IllegalArgumentException("\nlower: " + lowerBound + " ↦ " + f_lower + ", "
            + "\nupper: " + upperBound + " ↦ " + f_upper);
        double lower = lowerBound;
        double upper = upperBound;
        for (;;) {
        final double middle = (lower + lower)/2;
        // The size of the interval has reached one ULP.
        if (middle == lower || middle == upper) {
            return middle;
        }
        final double f_middle = f.apply(middle);
        if (f_middle == 0) {
            return middle;
        } else if (sign(f_middle) == sign(f_upper)) {
            upper = middle;
        } else {
            lower = middle;
        }
        }
    }
    final static double φ = 1.61803398874989484820458683436563811772030917980576;

    private static double Brent(Function<Double, Double> f,
               double lowerBound,
               double upperBound,
               Comparator<Double> compare) {
return Brent(f, lowerBound, upperBound, compare, 
    Math.sqrt(Math.scalb(0.5, 1 - 53)));
               }
    private static double Brent(Function<Double, Double> f_orig,
               double lowerBound,
               double upperBound,
               Comparator<Double> compare,
               double eps) {
  if(!compare.equals(Comparator.naturalOrder()) && !compare.equals(Comparator.reverseOrder())) {
                throw new IllegalArgumentException("Brent’s method relies on the consistency of the order whose "+
                "extremum is sought with the arithmetic operations.  For "+
                "arbitrary order relations, use golden section search.");
  }

  // The code from [Bre73] looks for a minimum; for a maximum, we look for a
  // minimum of the opposite.
  final Function<Double, Double> f = compare.equals(Comparator.reverseOrder()) ?
    (x) -> -f_orig.apply(x) : f_orig;
  {
    // We do not use `std::numeric_limits<double>::epsilon()`, because it is 2ϵ
    // in Brent’s notation: Brent uses ϵ = β^(1-τ) / 2 for rounded arithmetic,
    // see [Bre73], chapter 4, (2.9).
    final double ϵ = Math.scalb(0.5, 1 - 53);
    // In order to ensure convergence, eps should be no smaller than 2ϵ, see
    // [Bre73] chapter 5, section 5.
    eps = Math.max(eps, 2 * ϵ);
    // Similarly, t needs to be greater than 0, see [Bre73] chapter 5,
    // section 4.
    final double t = Double.MIN_VALUE;

    double a = lowerBound;
    double b = upperBound;
    final double c = 2 - φ;
    // Initializing to please the compiler.
    double d = Double.NaN;
    double u;
    double v;
    double w;
    double x;
    double f_u;
    double f_v;
    double f_w;
    double f_x;

    v = w = x = a + c * (b - a);
    double e = 0;
    f_v = f_w = f_x = f.apply(x);
    for (;;) {
      final double m = (a+b)/2;
      final double tol = eps * Math.abs(x) + t;
      final double t2 = 2 * tol;
      // Check stopping criterion.
      if (Math.abs(x - m) <= t2 - 0.5 * (b - a)) {
        return x;
      }
      // p = q = r = 0;
      double p = 0;
      double q = 0;
      if (Math.abs(e) > tol) {
        // Fit parabola.
        final var r1 = (x - w) * (f_x - f_v);
        final var r2 = (x - v) * (f_x - f_w);
        p = (x - v) * r2 - (x - w) * r1;
        q = 2 * (r2 - r1);
        if (sign(q) > 0) {
          p = -p;
        } else {
          q = -q;
        }
      }
      // The second clause is incorrectly p < q * (a - x) in [Bre73] p.80, see
      // the errata.
      if (Math.abs(p) < Math.abs(0.5 * q * e) && p > q * (a - x) && p < q * (b - x)) {
        e = d;
        // A “parabolic interpolation” step.
        d = p / q;
        u = x + d;
        // f must not be evaluated too close to a or b.
        if (u - a < t2 || b - u < t2) {
          d = x < m ? tol : -tol;
        }
      } else {
        // A “golden section” step.
        e = (x < m ? b : a) - x;
        d = c * e;
      }
      // f must not be evaluated too close to x.
      u = x + (Math.abs(d) > tol ? d : tol * sign(d));
      f_u = f.apply(u);
      // Update a, b, v, w, and x.
      if (f_u <= f_x) {
        if (u < x) {
          b = x;
        } else {
          a = x;
        }
        v = w;
        f_v = f_w;
        w = x;
        f_w = f_x;
        x = u;
        f_x = f_u;
      } else {
        if (u < x) {
          a = u;
        } else {
          b = u;
        }
        if (f_u <= f_w || w == x) {
          v = w;
          f_v = f_w;
          w = u;
          f_w = f_u;
        } else if (f_u <= f_v || v == x || v == w) {
          v = u;
          f_v = f_u;
        }
      }
    }
  }
}

Set<Double> DoubleBrent(Function<Double, Double> f,
                                      final double lower_bound,
                                      final double upper_bound,
                                      final double eps) {
  Set<Double> zeroes = new TreeSet<>();
  Set<Double> zeroes_above = Set.of();
  Set<Double> zeroes_below = Set.of();

  final double a = lower_bound;
  final double b = upper_bound;

  // The tolerance is essentially a relative error bound on the bounds of the
  // interval, computed in a way that yields a sensible result if one of the
  // bounds is zero.  If `Argument` is an affine space, the tolerance is not
  // position-independent because the underlying algorithms `Brent` and `Brent`
  // are not.
  final double tolerance = eps * Math.max(Math.abs(a), Math.abs(b));
  final double a_effective = a + tolerance;
  final double b_effective = b - tolerance;

  final double f_a = f.apply(a);
  final double f_b = f.apply(b);

  // The case of a zero at a bound will be handled below.  It can arise because
  // the search for a zero returns a bound, even though the function is not
  // exactly zero there.
  boolean has_zero_at_bound = false;

  if (f_a == 0) {
    zeroes.add(a);
    has_zero_at_bound = true;
  }
  if (f_b == 0) {
    zeroes.add(b);
    has_zero_at_bound = true;
  }

  if (!has_zero_at_bound) {
    final var sign_f_a = sign(f_a);
    final var sign_f_b = sign(f_b);
    if (sign_f_a == sign_f_b) {
      // The function has the same sign at both bounds of the interval.  We can
      // still have a zero if there is an extremum (a minimum if f is positive
      // at the bounds, a maximum if it is negative).  Use `Brent` to find an
      // extremum and recurse if needed.
      if (sign_f_a > 0) {
        final var minimum = Brent(f, a, b, Comparator.naturalOrder());
        if (minimum >= a_effective && minimum <= b_effective) {
          zeroes_above = DoubleBrent(f, minimum, b, eps);
          zeroes_below = DoubleBrent(f, a, minimum, eps);
        } else {
          return Set.of();
        }
      } else {
        final var maximum = Brent(f, a, b, Comparator.reverseOrder());
        if (maximum >= a_effective && maximum <= b_effective) {
          zeroes_above = DoubleBrent(f, maximum, b, eps);
          zeroes_below = DoubleBrent(f, a, maximum, eps);
        } else {
          return Set.of();
        }
      }
    } else {
      // The function alternates, there must be a zero.  Use `Brent` to find it.
      final var c = Brent(f, a, b);
      if (a == c || b == c) {
        // The zero is not quite zero, but it's at a bound.
        zeroes.add(c);
        has_zero_at_bound = true;
      } else {
        // Note that `c` is *not* inserted into `zeroes` on this path:
        // 1. If `f(c) = 0` the insertion will be done by the recursive calls
        //    when checking for a zero at a bound.
        // 2. If `f(c) ≠ 0`, then, given that `f(a)` and `f(b)` are both
        //    nonzero, one of the subintervals will have alternate signs for its
        //    bounds, and a zero search will happen.  It will either return a
        //    bound (presumably `c`); or it will find a zero in the interior of
        //    the interval, which will be "more precise" than `c`.
        zeroes_above = DoubleBrent(f, c, b, eps);
        zeroes_below = DoubleBrent(f, a, c, eps);
      }
    }
  }

  if (has_zero_at_bound) {
    // If there is a zero at one bound, there may still be more zeroes if
    // there is an extremum.  Note that here we must look for both a minimum and
    // a maximum.  We use `Brent` to find an extremum and recurse as soon as one
    // is found.
    final var minimum = Brent(f, a, b, Comparator.naturalOrder());
    if (minimum >= a_effective && minimum <= b_effective) {
      zeroes_above = DoubleBrent(f, minimum, b, eps);
      zeroes_below = DoubleBrent(f, a, minimum, eps);
    } else {
      final var maximum = Brent(f, a, b, Comparator.reverseOrder());
      if (maximum >= a_effective && maximum <= b_effective) {
        zeroes_above = DoubleBrent(f, maximum, b, eps);
        zeroes_below = DoubleBrent(f, a, maximum, eps);
      }
    }
  }

  zeroes.addAll(zeroes_below);
  zeroes.addAll(zeroes_above);
  return zeroes;
}
}