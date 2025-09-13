%{

#include <stdio.h>

#include "y.tab.h"

%}

/* TODO(egg): non_ASCII… */
white_space     [\x09-\x0D\x20]+
literal_element [^\x09-\x0D\x20&\-\[\]\^\{\}$\\]

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
/* TODO(egg): non_ASCII… */
escapable_character [^0-7uxUNabtnvfrpP]

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
/* TODO(egg): non_ASCII… */
bracketed_literal_element [^\\}]

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
/* TODO(egg): Non_ASCII; exclude ≠. */
literal_value_element [^\\:{}=/@]
property_comparison @{unary_query_expression}@
regular_expression_match \/{regular_expression}\/
regular_expression {regular_expression_character}+
regular_expression_character {regex_unescaped}|\\{any}

/* TODO(egg): Non_ASCII */
regex_unescaped [^\\]
any .

%%

{white_space} {}

{literal_element} {
  puts("literal_element");
  return literal_element;
}

{escaped_element} {
  puts("escaped_element");
  return escaped_element;
}

{named_element} {
  puts("named_element");
  return named_element;
}

{bracketed_element} {
  puts("bracketed_element");
  return bracketed_element;
}

{string_literal} {
  puts("string_literal");
  return string_literal;
}

{property_query} {
  puts("property_query");
  return property_query;
}

_/{white_space}?\] {
  return unescaped_hyphen_minus_at_end_of_union;
}

%%

int yywrap() {
  return 0;
}

int main(int argc, char** argv) {
  yylex();
}