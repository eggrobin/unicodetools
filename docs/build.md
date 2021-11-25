# Building Unicode Tools

This file provides instructions for building and running the UnicodeTools, which
can be used to:

*   build the Derived Unicode files in the UCD (Unicode Character Database),
*   build the transformed UCA (Unicode Collation Algorithm) files needed by
    Unicode.
*   run consistency checks on beta releases of the UCD and the UCA.
*   build 4 chart folders on the unicode site.
*   build files for ICU (collation, NFSkippable)

**WARNING!!**

*   This is NOT production level code, and should never be used in programs.
*   The API is subject to change without notice, and will not be maintained.
*   The source is uncommented, and has many warts; since it is not production
    code, it has not been worth the time to clean it up.
*   It will probably need some adjustments on Unix or Windows, such as changing
    the file separator.
*   Currently it uses hard-coded directory names.
*   The contents of multiple versions of the UCD must be copied to a local
    directory, as described below.
*   ***It will be useful to look at the history of the files in git to see the
    kinds of rule changes that are made!***
    *   Unfortunately, we lost some change history of about 1.5 years(?) leading up to April 2020.

## Instructions

### General Setup for Maven

1.  Configure Maven settings (including Github tokens) according to these instructions:
    <http://cldr.unicode.org/development/maven>
2.  Get your github account authorized for <https://github.com/unicode-org/unicodetools>,
    create a fork under your account, and create a local clone.

#### In-source / Out-of-source build setup

Some of the tasks within the Unicode Tools generate output files that can also be input files into other steps.
For this purpose, we create a folder named `Generated` to store these files.
This folder can be a subfolder inside the local working copy root (called an "In-source build" workspace layout), or this folder can be outside (ex: a sibling folder) the local working-copy root (called an "Out-of-source build" workspace layout). Both workspace styles are described below.

Out-of-source builds keep a separation between source files of the repository and their generated output files, which are not tracked in the repository. Out-of-source builds allow developers to maintain a clean view of changes to tracked source files, without mixing generated output files. (Out-of-source builds are also useful for C++ repositories in which multiple configurations can be invoked to generate independent sets of makefiles that result in corresponding different output compiled binary files.)

##### Setup for an out-of-source build workspace

1. Create directories for cloning the Unicode Tools and CLDR repositories:
```
mkdir -p unicodetools/mine/src
mkdir -p cldr/mine/src
```
2. Clone the repositories' contents into their respective source directories:
```
git clone https://github.com/unicode-org/unicodetools.git unicodetools/mine/src
git clone https://github.com/unicode-org/cldr.git cldr/mine/src
```
3. Create the `Generated` folder structure as a sibling to the local working copy root:
```
mkdir -p unicodetools/mine/Generated/BIN
```

##### Setup for an in-source build workspace

1.  Clone both the [Unicode Tools](https://github.com/unicode-org/unicodetools) and [CLDR](https://github.com/unicode-org/cldr) repositories:
```
git clone https://github.com/unicode-org/unicodetools.git
git clone https://github.com/unicode-org/cldr.git
```
2.  In the root folder of the `unicodetools` local working copy, create the `Generated/BIN` folder structure
    1. (Eclipse users can do this graphically by following the corresponding step in the Eclipse section below)
    1. At the command-line: `cd unicodetools; mkdir -p output/Generated/BIN`

##### Notes for both out-of-source and in-source build workspaces

Currently, some tests run on the generated output files of a tool (ex: in order to test the validity of the output files). However, after converting these tests into standard JUnit tests, these unit tests are then run in isolation by default. Until we amend the code for such tests, those tests will fail by default. These tests are picked up and run by `mvn test`, and thus, by any other subsequent Maven target in the Maven lifecycle (ex: `package`, `install`).

For people who need `mvn test` or other subsequent Maven targets to succeed, a temporary workaround would be the following (which points the generated files directory to find the test input files from the repository sources):

For out-of-source builds:
```
ln -s ../../src/unicodetools/data/security unicodetools/mine/Generated/security
```
For in-source builds:
```
cd <unicodetools-repo-root>; ln -s unicodetools/data/security output/Generated/security
```

This step to create a symbolic link on the file system is not necessary to run individual tools in Unicode Tools, nor is it intended to last long-term as we refactor code to establish stronger invariants and tests.

#### Java System properties used in Unicode Tools

(Note: The following example values for Java system properties are paths to local working copies that are organized using the out-of-source build workspace layout, as described above.)

| Property                | Example Value                                        |
|-------------------------|------------------------------------------------------|
| CLDR_DIR                | /usr/local/google/home/mscherer/cldr/mine/src        |
| IMAGES_REPO_DIR         | /usr/local/google/home/mscherer/images/mine/src      |
| UNICODETOOLS_REPO_DIR   | /usr/local/google/home/mscherer/unitools/mine/src    |
| UNICODETOOLS_OUTPUT_DIR | /usr/local/google/home/mscherer/unitools/mine        |
| UVERSION                | 14.0.0                                               |


### Eclipse-specific Additional Setup

1. Follow the Eclipse-specific settings at <http://cldr.unicode.org/development/maven>
    1.  Edit the cldr-code project’s Build Path:
        Under “Order and Export”, set the check mark next to “Maven Dependencies”
        so that CLDR makes its dependencies available to the Unicode Tools project.
3.  Import the unicodetools project into Eclipse. (Using Maven: General > Existing Projects into Workspace)
4.  Also create the project **and directory** Generated. Various results are
    deposited there. You need the directory, but the Eclipse project is optional.
    1.  New... -> Project... -> General/Project
    2.  Project Name=Generated
    3.  Uncheck "Use default location" (so that it's not inside your Eclipse workspace)
    4.  Browse or type a folder path like `Generated` that is a sibling to the top-level `unicodetools` directory
        1.  Create this folder
        2.  Create a subfolder BIN
5.  Project > Clean... > Clean all projects is your friend

### Running commands for Unicode Tools tasks

#### Setting system properties

For the tools to work, you need to set the JVM system properties according to your workspace layout.
Depending on which tool you are running, you may need some or all of the properties listed above in General Setup for Maven.

For command-line users:
- System properties are specified in this fashion for Maven (same as it is for the JVM CLI): `-Dvar1=path1 -Dvar2=path2 ...`

For Eclipse users:
- You can set these for each single tool in the Run > Debug Configurations... > (x)= Arguments tab > VM arguments
- Or you can set the common variables globally in Window > Preferences... > Java > Installed JREs > select the active JRE >  Edit... > Default VM arguments: `-Dvar1=path1 -Dvar2=path2 ...`.

#### Enabling assertions

Please also enable assertions when running commands so that failed assertions don’t just slip through.

Command-line users:
- Set the `MAVEN_OPTS` environment variable to include the `-ea` JVM option in its string value
  * Ex: `export MAVEN_OPTS="-ea"; mvn exec:java -Dexec.mainClass=...`
  * Ex: `MAVEN_OPTS="-ea" mvn exec:java -Dexec.mainClass=...`

Eclipse users:
- Use the VM argument `-ea` (enable assertions) in your Preferences or in your Run/Debug configurations

### Commands for Unicode Tools Tasks

All commands must be run in the root of the `unicodetools` repository local working copy directory.

#### Initialization command

The following command must be run first before all other commands. This command initializes ____ (?).

```
mvn -s .github/workflows/mvn-settings.xml -B compile
```

#### All other commands

Common tasks for Unicode Tools are listed below with example CLI commands with example argument values that they need:

- Make Unicode Files:
  * Out-of-source build: `mvn -s .github/workflows/mvn-settings.xml exec:java -Dexec.mainClass="org.unicode.text.UCD.Main"  -Dexec.args="version 14.0.0 build MakeUnicodeFiles"  -pl unicodetools  -DCLDR_DIR=$(cd ../../../cldr/mine/src ; pwd)  -DUNICODETOOLS_OUTPUT_DIR=$(cd .. ; pwd)  -DUNICODETOOLS_REPO_DIR=$(pwd)  -DUVERSION=14.0.0`
  * In-source build: `MAVEN_OPTS="-ea" mvn exec:java -Dexec.mainClass="org.unicode.text.UCD.Main"  -Dexec.args="version 14.0.0 build MakeUnicodeFiles"  -pl unicodetools  -DCLDR_DIR=$(cd ../cldr ; pwd)  -DUNICODETOOLS_OUTPUT_DIR=$(pwd)  -DUNICODETOOLS_REPO_DIR=$(pwd)  -DUVERSION=14.0.0`

- Build and Test:
  * Out-of-source build: `MAVEN_OPTS="-ea" mvn package -DCLDR_DIR=$(cd ../../../cldr/mine/src ; pwd)  -DUNICODETOOLS_OUTPUT_DIR=$(cd .. ; pwd)  -DUNICODETOOLS_REPO_DIR=$(pwd)  -DUVERSION=14.0.0`
  * In-source build: `MAVEN_OPTS="-ea" mvn package -DCLDR_DIR=$(cd ../cldr ; pwd)  -DUNICODETOOLS_OUTPUT_DIR=$(pwd)  -DUNICODETOOLS_REPO_DIR=$(pwd)  -DUVERSION=14.0.0`

### Updating CLDR and ICU versions

> :point_right: Note: This is a mess. See <https://unicode-org.atlassian.net/browse/ICU-21757>

See the top level `pom.xml` under `<properties>`.

#### ICU

- Every time an ICU release/prerelease (not tag) is created on GitHub, a new package is created.
- Go to <https://github.com/orgs/unicode-org/packages?repo_name=icu> to see what versions of ICU special packages are available.
- Update `icu.version` in the top level `pom.xml` to the version string, such as `70.0.1-SNAPSHOT-cldr-2021-09-15`
#### CLDR

- Every time a CLDR release/prerelease (not tag) is created on GitHub, a new package is created.
- Go to <https://github.com/orgs/unicode-org/packages?repo_name=cldr> to see what versions of CLDR packages are available.
- Update `cldr.version` in the top level `pom.xml` to this version string, which has 0.0.0 and a git hash in it, such as `0.0.0-SNAPSHOT-bfa39570be`

#### Using custom versions of CLDR

- Look at your version of CLDR's top level `pom.xml`
- It will have a version such as `40.0-SNAPSHOT`
- Change `cldr.version` to `40.0-SNAPSHOT` and this version will be used.
- If you are using Eclipse, make sure CLDR and UnicodeTools are in the same workspace,
  and Eclipse should do the right thing.
- I'm not sure how to do the same with ICU.

### Input data files

The input data files for the Unicode Tools are checked into the repo since
2012-dec-21:

*   <https://github.com/unicode-org/unicodetools/tree/main/unicodetools/data/ucd>
*   <https://github.com/unicode-org/unicodetools/tree/main/unicodetools/data/ucd>

This is inside the unicodetools file tree, and the Java code has been updated to
assume that. Any old Eclipse setup needs its path variables checked.

For details see [Input data setup](inputdata.md).

## Generating new data

To generate new data files, you can run the `org.unicode.text.UCD.Main` class
(yes, the `Main` class has a `main()` function)
with program arguments `build MakeUnicodeFiles`. You may optionally include e.g.
`version 14.0.0` if you wish to just generate the files for a single version.
Make sure you have the VM arguments set up as described above.

## Updating to a new Unicode version

### Unicode 15+ workflow

Starting with Unicode 15, we are developing most of the Unicode data files
in this Unicode Tools project, and publish them to the Public folder
only for alpha/beta/final releases.
That is, we are reversing the flow of files.
(See [issue #144](https://github.com/unicode-org/unicodetools/issues/144).)

We are also no longer generating and posting files with version suffixes.

Except: Some files, such as Unihan and ucdxml data files, are developed elsewhere,
and we continue to ingest them as before.

###

All of the following have `version 15.0.0` (or whatever the latest version is)
in the options given to Java.

Example changes for adding Unicode 15 version numbers:
See the second commit of https://github.com/unicode-org/unicodetools/pull/156

Example changes for adding properties:
<https://github.com/unicode-org/unicodetools/pull/40>. Throughout these steps we
will walk through updating unicodetools to support Unicode 15 or 14.

Starting with Unicode 15, we keep the latest versions of data files in
unversioned "dev" folders in this repo.

Unicode 14:

Firstly, fetch the latest data files for this version from
<https://www.unicode.org/Public/14.0.0/ucd/>, matching your new version number.
If this does not exist, request this be created from
[ucd-dev@googlegroups.com](mailto:ucd-dev@googlegroups.com). You may also need
to fetch the emoji files from <https://www.unicode.org/Public/emoji/13.1>, using
a previous version if a new one does not exist.

You may need to use the tools from [Input data setup](inputdata.md) to
desuffix the files (removing the -dN suffixes). Copy these into
`unicodetools/data/emoji/14.0` and `unicodetools/data/ucd/14.0.0-Update`.

to set up the inputs correctly. For some updates you may need to pull in other
(uca, security, idna, etc) files, see [Input data setup](inputdata.md) for more information.

Unicode 15:

We no longer generate files with version suffixes.
We now generate files into an output folder with the Unicode version number.

Unicode 14:

Now, update the following files:

`MakeUnicodeFiles.txt` (find in Eclipse via Navigate/Resource or Ctrl+Shift+R)

```
Generate: .*
CopyrightYear: 2021 (or whatever)
....
File: DerivedAge
..... add a value for the latest version at the bottom:
Value:  V14_0
```

Update `String[] LONG_AGE` and `String[] SHORT_AGE` in `UCD_Names.java`.

Update `latestVersion` and `lastVersion` in `org.unicode.text.utility.Settings.java` to fix:

```
public static final String latestVersion = "14.0.0";
public static final String lastVersion = "13.1.0"; // last released version
```

Update `LIMIT_AGE` and `AGE_VERSIONS` in `UCD_Types.java`.

Update `enum AGE_Values` in `UcdPropertyValues.java`.

Update `searchPath` in `org.unicode.text.utility.Utility.java`.

If there are new CJK characters
(if there are changes to entries in UnicodeData.txt that are for `<CJK Ideograph ..., First>` etc.),
`UCD.java` and `UCD_Types.java` need to be updated to handle these ranges.
See [PR #47](https://github.com/unicode-org/unicodetools/pull/47) for an example.

For CJK, you'll first need to compute the composite version, as `(major << 16) | (minor << 8) |` update.
E.g. Unicode 14 is 0xe0000.
Since the ranges change based on the version, the code here needs to be updated in a version-aware way.

If any range has changed its end point, say, CJK Extension C, update `CJK_C_LIMIT` in `UCD_Types.java`
(make sure to update the comment next to it with the latest Unicode version).

Then edit `mapToRepresentative()` in `UCD.java` to add the range. Make sure the
range is added *only* for the latest Unicode version, by using sections like
`if (ch <= 0x2B737 && rCompositeVersion >= 0xe0000)` .

If a new range has been introduced, add it to `UCD_Types.java` near `CJK_E_BASE`,
add it to `mapToRepresentative()`, update `hasComputableName` and `get()` in `UCD.java`
to add the first character.

Also search (case-insensitively) unicodetools for 2A700 (start of Extension C)
and add the new range accordingly.

When `CJK_LIMIT` moves, search for 9FCC and update near there as necessary.

If the main Tangut block has been extended, then in `UCD.java`
`mapToRepresentative()` add another per-version block for returning `TANGUT_BASE`.

You can now run the steps in “Generating new data” above to attempt to generate the files.
It will likely error due to missing enum values for new blocks and scripts.

### New blocks

Compare Blocks.txt to the old version (or check the errors from your attempt
to generate new files). For all the new ones:

*   Add to `ShortBlockNames.txt` (you need to know what the short name is, you can
    find it in `PropertyValueAliases.txt`)
*   Add long & short names to `UcdPropertyValues.java` `enum Block_Values`
    *   You may not have to do this for all of them, update `ShortBlockNames` and
        see if you still get errors.

### New scripts

*   Add long & short names to `UcdPropertyValues.java` `enum Script_Values`, in
    alphabetical order
*   Add the script code to `UCD_Types.java` below `SCRIPT_CODE`, in alphabetical
    order grouped by Unicode version. Update `LIMIT_SCRIPT` to use the name of the
    new last script
*   Update `SCRIPT` and `LONG_SCRIPT` in `UCD_Names.java`, in alphabetical order
    grouped by Unicode version. (Important: this must be in the same order as the
    previous one.)
*   After first run of UCD Main, take the `DerivedAge.txt` lines for the new
    version, copy them into the input `Scripts.txt` file, and change the new
    version number to the appropriate script (which can be new or old or `Common`
    etc.). Then run UCD Main again and check the generated `Scripts.txt`.

Make a pull request to incorporate these updates, and upload the generated files
in a way that can be shared with ucd-dev.

Unicode 15 TODO:
We plan to
- make a commit for changes in input data files
- copy the output files back into the input folders, review, and commit again
... instead of posting draft files elsewhere and re-ingesting them later.

Ideally, diff the files to check for any discrepancies. The script will do this
automatically, you can search the output for lines that say "Found difference in
`<filename>`", however note that it will only display the first line of the diff,
so if there are additional discrepancies you may miss them.

### New enum property values

***When you run, it will break if there are new enum property values.***

Note: For more information and newer code see the pages

*   [Changing UCD Properties & Printout](changing-ucd-properties.md)
*   [NewUnicodeProperties](newunicodeproperties.md)

To fix that:

Go into `org.unicode.text.UCD/`
*   `UCD_Names.java` and
*   `UCD_Types.java`

(These contain ugly items that should be enums nowadays.)

Find the property (easiest is to search for some other properties in the enum).
Add at end in `UCD_Types`. Be sure to update the limit, like

```
LIMIT_SCRIPT = Mandaic + 1;
```

Then in `UCD_Names`, change the corresponding name entry, both the full and abbreviated names.
Follow the format of the existing values.

For example:

In `UCDNames.java` in `BIDI_CLASS` add `"LRI", "RLI", "FSI", "PDI",`

In `UCDNames.java` in `LONG_BIDI_CLASS` add
`"LeftToRightIsolate", "RightToLeftIsolate", "FirstStrongIsolate", "PopDirectionalIsolate",`

In `UCD_Types.java` add & adjust

```
BIDI_LRI = 20,
BIDI_RLI = 21,
BIDI_FSI = 22,
BIDI_PDI = 23,
LIMIT_BIDI_CLASS = 24;
```

Some changes may cause collisions in the UnicodeMaps used for derived properties.
You'll find that out with an exception like:

> Exception in thread "main" java.lang.IllegalArgumentException: Attempt to
> reset value for 17B4 when that is disallowed. Old: Control; New: Extend at
> org.unicode.text.UCD.ToolUnicodePropertySource$28.\<init\>(ToolUnicodePropertySource.java:578)

### New scripts

Add new scripts like other new property values. In addition, make sure there are
ISO 15924 script codes, and collect CLDR script metadata. See

<http://cldr.unicode.org/development/updating-codes/updating-script-metadata>

<http://www.unicode.org/iso15924/codechanges.html>

### Break Rules

If there are new break rules (or changes), see
[Segmentation-Rules](changing-ucd-properties.md).

### Building Files

#### Setup

1.  In Eclipse, open the Package Explorer (Use Window>Show View if you don't
    see it)
2.  Open UnicodeTools
    *   package org.unicode.text.UCD
        *   MakeUnicodeFiles.txt

            This file drives the production of the derived Unicode files.
            The first three lines contain parameters that you may want to
            modify at some times:
            ```
            Generate: .*script.* // this is a regular expression. Use .* for all files
            CopyrightYear: 2010  // Pick the current year
            ```
3.  Open in Package Explorer
    *   package org.unicode.text.UCD
        *   Main.java
4.  Run>Run As...
    1.  Choose Java Application
        *   it will fail, don't worry; you need to set some parameters.
5.  Run>Run...
    *   Select the Arguments tab, and fill in the following
        *   Program arguments: `build MakeUnicodeFiles`
            *   For a specific version, prepend `version 6.3.0 ` or similar.
        *   VM arguments: CLDR_DIR etc., see the setup instructions near the top of this page;
            easiest to set them in the global Preferences.
            Otherwise copy them into each Run/Debug configuration.
    *   Close and Save

#### Run

1.  You'll see it build the 5.0 files, with something like the following
    results:
    ```
    Writing UCD_Data
    Data Size: 109,802
    Wrote Data 109802
    ```
    For each version, the tools build a set of binary data in BIN that contain the
    information for that release. This is done automatically, or you can
    manually do it with the Program Arguments
2.  As options, use: `version 5.0.0 build`

    This builds a compressed format of all the UCD data (except blocks and
    Unihan) into the BIN directory. Don't worry about the voluminous console
    messages, unless one says "FAIL".

    *You have to manually do this if you change any of the data files in
    that version! This ought to have build files, but I haven't worked
    around to it.*

    Note: if for any reason you modify the binary format of the BIN files,
    you also have to bump the value in that file:
    ```
    static final byte BINARY_FORMAT = 8; // bumped if binary format of UCD changes
    ```

#### Results in Generated

1.  The files will be in this directory.
2.  (Note: these don't get generated anymore!) There are also DIFF folders,
    that contain BAT files that you can run on Windows with CompareIt. (You
    can modify the code to build BATs with another Diff program if you
    want).
    1.  For any file with a significant difference, it will build two BAT
        files, such as the first two below.
        ```
        Diff_PropList-5.0.0d10.txt.bat
        OLDER-Diff_PropList-5.0.0d10.txt.bat

        UNCHANGED-Diff_PropertyValueAliases-5.0.0d10.txt.bat
        ```
3.  Any files without significant changes will have "UNCHANGED" as a prefix:
    ignore them. The OLDER prefix is the comparison to the last version of
    Unicode.
4.  On Windows you can run these BATs to compare files: TODO??

### Upload for Ken Whistler & editorial committee

Unicode 15 TODO: See above; commit new input data, run tools, review output, copy back to input, commit, pull request...

1.  Check diffs for problems
2.  First drop for a version: Upload **all** files
3.  Subsequent drop for a version: Upload *only modified* files

### Invariant Checking

***Note: Also build and run the [New Unicode Properties](newunicodeproperties.md) programs, since they have some additional checks.***

#### Setup
1.  Open in Package Explorer
    *   org.unicode.text.UCD
        *   TestUnicodeInvariants.java
2.  Run>Run As... Java Application\
    Will create the following file of results:
    ```
    {Generated}/UnicodeTestResults.txt
    ```

    Options:
    1.  -r Print the failures as a range list.
    2.  -fxxx Use a different input file, such as -fInvariantTest.txt

    The console output shows whether any problems are found. Thus in the
    following case there was one failure:
    ```
    ParseErrorCount=0
    TestFailureCount=1
    ```
3.  The header of the result file explains the syntax of the tests.
4.  Open that file and search for `**** START Test Failure`.
5.  Each such point provides a dump of comparison information.
    1.  Failures print a list of differences between two sets being
        compared. So if A and B are being compared, it prints all the items
        in A-B, then in B-A, then in A&B.
    2.  For example, here is a listing of a problem that must be corrected.
        Note that usually there is a comment that explains what the
        following line or lines are supposed to test. Then will come FALSE
        (indicating that the test failed), then the detailed error report.
        ```
        # Canonical decompositions (minus exclusions) must be identical across releases
        [$Decomposition_Type:Canonical - $Full_Composition_Exclusion] = [$ï¿½Decomposition_Type:Canonical - $ï¿½Full_Composition_Exclusion]

        FALSE
        **** START Error Info ****

        In [$ï¿½Decomposition_Type:Canonical - $ï¿½Full_Composition_Exclusion], but not in [$Decomposition_Type:Canonical - $Full_Composition_Exclusion] :

        # Total code points: 0

        Not in [$ï¿½Decomposition_Type:Canonical - $ï¿½Full_Composition_Exclusion], but in [$Decomposition_Type:Canonical - $Full_Composition_Exclusion] :
        1B06           # Lo       BALINESE LETTER AKARA TEDUNG
        1B08           # Lo       BALINESE LETTER IKARA TEDUNG
        1B0A           # Lo       BALINESE LETTER UKARA TEDUNG
        1B0C           # Lo       BALINESE LETTER RA REPA TEDUNG
        1B0E           # Lo       BALINESE LETTER LA LENGA TEDUNG
        1B12           # Lo       BALINESE LETTER OKARA TEDUNG
        1B3B           # Mc       BALINESE VOWEL SIGN RA REPA TEDUNG
        1B3D           # Mc       BALINESE VOWEL SIGN LA LENGA TEDUNG
        1B40..1B41     # Mc   [2] BALINESE VOWEL SIGN TALING TEDUNG..BALINESE VOWEL SIGN TALING REPA TEDUNG
        1B43           # Mc       BALINESE VOWEL SIGN PEPET TEDUNG

        # Total code points: 11

        In both [$ï¿½Decomposition_Type:Canonical - $ï¿½Full_Composition_Exclusion], and in [$Decomposition_Type:Canonical - $Full_Composition_Exclusion] :
        00C0..00C5     # L&   [6] LATIN CAPITAL LETTER A WITH GRAVE..LATIN CAPITAL LETTER A WITH RING ABOVE
        00C7..00CF     # L&   [9] LATIN CAPITAL LETTER C WITH CEDILLA..LATIN CAPITAL LETTER I WITH DIAERESIS
        00D1..00D6     # L&   [6] LATIN CAPITAL LETTER N WITH TILDE..LATIN CAPITAL LETTER O WITH DIAERESIS
        ...
        30F7..30FA     # Lo   [4] KATAKANA LETTER VA..KATAKANA LETTER VO
        30FE           # Lm       KATAKANA VOICED ITERATION MARK
        AC00..D7A3     # Lo [11172] HANGUL SYLLABLE GA..HANGUL SYLLABLE HIH

        # Total code points: 12089
        **** END Error Info ****
        ```
6.  The input file is [unicodetools/org/unicode/text/UCD/UnicodeInvariantTest.txt](https://github.com/unicode-org/unicodetools/blob/main/unicodetools/org/unicode/text/UCD/UnicodeInvariantTest.txt).
    1.  Some failures are expected for a new Unicode version, or new RTL blocks, etc. Adjust the input file as necessary.
    1.  For other failures, adjust the character properties.

### Options

1.  If you want to see files that are opened while processing, do the following:
    1.  Run>Run
    2.  Select the Arguments tab, and add the following
        1.  VM arguments: `-DSHOW_FILES`

## UCA

Instructions moved to [the uca tools main page](uca/index.md).

## Charts

To build all the charts, use org.unicode.text.UCA.Main, with the option:
```
charts
```

They will be built into

<http://unicode.org/draft/charts/>

**Once UCA is released, then copy those files up to the right spots in the
Unicode site:**

*   <http://www.unicode.org/charts/normalization/>
*   <http://www.unicode.org/charts/collation/>
*   <http://www.unicode.org/charts/case/>
*   <http://www.unicode.org/charts/collation/>
*   ...