%{

#include <stdio.h>

#include "unicodeset.tab.hh"

%}

%option c++ noyywrap

u8_0080_07FF     [\xC2-\xDF][\x80-\xBF]
u8_0800_0FFF            \xE0[\xA0-\xBF][\x80-\xBF]
u8_1000_CFFF     [\xE1-\xEC][\x80-\xBF][\x80-\xBF]
u8_D000_D7FF            \xED[\x80-\x9F][\x80-\xBF]
u8_E000_FFFF     [\xEE-\xEF][\x80-\xBF][\x80-\xBF]
u8_10000_3FFFF          \xF0[\x90-\xBF][\x80-\xBF][\x80-\xBF]
u8_40000_FFFFF   [\xF1-\xF3][\x80-\xBF][\x80-\xBF][\x80-\xBF]
u8_100000_10FFFF        \xF4[\x80-\x8F][\x80-\xBF][\x80-\xBF]
non_ascii {u8_0080_07FF}|{u8_0800_0FFF}|{u8_1000_CFFF}|{u8_D000_D7FF}|{u8_E000_FFFF}|{u8_10000_3FFFF}|{u8_40000_FFFFF}|{u8_100000_10FFFF}

             /* U+2260 ≠ is E2 89 A0. */
u8_1000_2259 \xE1[\x80-\xBF][\x80-\xBF]|\xE2[\x80-\x88][\x80-\xBF]|\xE2\x89[\x80-\x9F]
u8_2261_CFFF \xE2\x89[\xA1-\xBF]|\xE2[\x90-\xBF][\x80-\xBF]|[\xE3-\xEC][\x80-\xBF][\x80-\xBF]
non_ascii_except_2260 {u8_0080_07FF}|{u8_0800_0FFF}|{u8_1000_2259}|{u8_2261_CFFF}|{u8_D000_D7FF}|{u8_E000_FFFF}|{u8_10000_3FFFF}|{u8_40000_FFFFF}|{u8_100000_10FFFF}

u8_1000_200D \xE1[\x80-\xBF][\x80-\xBF]|\xE2\x80[\x80-\x8D]
u8_2010_CFFF \xE2\x80[\x90-\xBF]|\xE2[\x81-\xBF][\x80-\xBF]|[\xE3-\xEC][\x80-\xBF][\x80-\xBF]
non_ascii_except_200E_200F {u8_0080_07FF}|{u8_0800_0FFF}|{u8_1000_200D}|{u8_2010_CFFF}|{u8_D000_D7FF}|{u8_E000_FFFF}|{u8_10000_3FFFF}|{u8_40000_FFFFF}|{u8_100000_10FFFF}

white_space              {horizontal_white_space}|{vertical_white_space}|{ignorable_format_control}
horizontal_white_space   [\x09\x20]
                             /* U+200E       U+200F */
ignorable_format_control  \xE2\x80\x8E|\xE2\x80\x8F
                                    /* U+0085       U+2028       U+2029 */
vertical_white_space     [\x0A-\x0D]|\xC2\x85|\xE2\x80\xA8|\xE2\x80\xA9

literal_element [^\x09-\x0D\x20&\-\[\]\^\{\}$\\\x80-\xFF]|{non_ascii}

escaped_element (?x:{ee1}|{ee2}|{ee3}|{ee4}|{ee5}|{ee6}|{ee7}|{ee8})
ee1   \\x {up_to_two_hexadecimal_digits}
ee2   \\u {four_hexadecimal_digits}
ee3   \\U000 {five_hexadecimal_digits}
ee4   \\U0010 {four_hexadecimal_digits}
ee5   \\x\{ {hexadecimal_digits} \}
ee6   \\ {up_to_three_octal_digits}
ee7   \\ {escapable_character}
ee8   \\a | \\b | \\t | \\n | \\v | \\f | \\r
up_to_three_octal_digits (?x:{uo1}|{uo2}|{uo3})
uo1   {octal_digit}
uo2   {octal_digit} {octal_digit}
uo3   {octal_digit} {octal_digit} {octal_digit}
up_to_two_hexadecimal_digits (?x:{uh1}|{uh2})
uh1 {hexadecimal_digit}
uh2 {hexadecimal_digit} {hexadecimal_digit}
four_hexadecimal_digits {hexadecimal_digit}{hexadecimal_digit}{hexadecimal_digit}{hexadecimal_digit}
five_hexadecimal_digits {hexadecimal_digit}{hexadecimal_digit}{hexadecimal_digit}{hexadecimal_digit}{hexadecimal_digit}
hexadecimal_digits {hexadecimal_digit}+
hexadecimal_digit 0|1|2|3|4|5|6|7|8|9|A|B|C|D|E|F|a|b|c|d|e|f
octal_digit 0|1|2|3|4|5|6|7

escapable_character [^0-7uxUNabtnvfrpP\x80-\xFF]|{non_ascii_except_200E_200F}

named_element (?x:{ne1}|{ne2}|{ne3})
ne1 \\N\{ {ucd_identifier} \}
ne2 \\xN\{ {hexadecimal_digits} : {ucd_identifier} \}
ne3 \\xcN\{ {hexadecimal_digits} : {named_literal_element} : {ucd_identifier} \}
ucd_identifier {ucd_identifier_character}+

named_literal_element [^:}]
ucd_identifier_character [A-Za-z0-9\ _-]

bracketed_element \{{string_element}\}
string_literal \{\}|\{{string_elements}\}
string_element {bracketed_literal_element}|{escaped_element}|{named_element}
string_elements {string_element}{string_element}+

bracketed_literal_element [^\\}\x80-\xFF]|{non_ascii}

property_query {perl_start}{query_expression}{perl_end}|{posix_start}{query_expression}{posix_end}
perl_start  \\p\{|\\P\{
perl_end  \}
posix_start  \[:|\[:^
posix_end  :\]
query_expression {unary_query_expression}|{binary_query_expression}
unary_query_expression {optional_version_qualifier}{ucd_identifier}
binary_query_expression {optional_version_qualifier}{ucd_identifier}{query_operator}{property_predicate}
optional_version_qualifier {version_qualifier}?
version_qualifier (?x:{vq1}|{vq2}|{vq3})
vq1  U {version_number} :
vq2  U {version_suffix} :
vq3  U_1:
version_number (?x:{vn1}|{vn2}|{vn3})
vn1 {digits} {optional_suffix}
vn2 {digits} \. {digits} {optional_suffix}
vn3 {digits} \. {digits} \. {digits} {optional_suffix}
optional_suffix {version_suffix}?
version_suffix α|β|dev
digit 0|1|2|3|4|5|6|7|8|9
digits {digit}+
query_operator =|≠
property_predicate {property_value}|{regular_expression_match}|{property_comparison}
property_value {property_value_element}+
property_value_element {literal_value_element}|{escaped_element}|{named_element}
literal_value_element [^\\:{}=/@\x80-\xFF]|{non_ascii_except_2260}
property_comparison @{unary_query_expression}@
regular_expression_match \/{regular_expression}\/
regular_expression {regular_expression_character}+
regular_expression_character {regex_unescaped}|\\{any}

regex_unescaped [^\\\x80-\xFF]|{non_ascii}
any [^\x80-\xFF]|{non_ascii}

%%

\x0D\x0A|{vertical_white_space} {}

{horizontal_white_space}|{ignorable_format_control} {}

[&\-\[\]\^] {
  // set-operator.
  return *yytext;
}

{literal_element} {
  return unicodeset::parser::token::literal_element;
}

{escaped_element} {
  return unicodeset::parser::token::escaped_element;
}

{named_element} {
  return unicodeset::parser::token::named_element;
}

{bracketed_element} {
  return unicodeset::parser::token::bracketed_element;
}

{string_literal} {
  return unicodeset::parser::token::string_literal;
}

{property_query} {
  return unicodeset::parser::token::property_query;
}

-/{white_space}?\] {
  return unicodeset::parser::token::unescaped_hyphen_minus_at_end_of_union;
}

\\{ignorable_format_control} {
  LexerError("ignorable-format-control is not an escapable-character");
}

\[: {
  LexerError("Ill-formed property-query");
}

\[{ignorable_format_control}+:                                        |
\\x{hexadecimal_digit}{ignorable_format_control}+{hexadecimal_digit}  |
\\x{octal_digit}{ignorable_format_control}+{octal_digit}              |
\\x{octal_digit}{octal_digit}{ignorable_format_control}+{octal_digit} {
  LexerError("ignorable-format-control within token");
}

[\x00-\x7F]|{non_ascii} {
  LexerError("unrecognized token");
}

. {
  LexerError("Ill-formed UTF-8");
}

<<EOF>> {
  return 0;
}

%%

int yywrap(void) {
  return 1;
}