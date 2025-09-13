%{

#include <stdio.h>

enum LexicalElement {
  LITERAL_ELEMENT,
  ESCAPED_ELEMENT,
  NAMED_ELEMENT,
  BRACKETED_ELEMENT,
  STRING_LITERAL,
  PROPERTY_QUERY,
};

%}

/* TODO(egg): non-ASCII… */
white-space     [\x09-\x0D\x20]+
literal-element [^\x09-\x0D\x20&\-\[\]\^\{\}$\\]

escaped-element (?x:{ee1}|{ee2}|{ee3}|{ee4}|{ee5})
ee1   \\x {up-to-two-hexadecimal-digits}
ee2   \\u {four-hexadecimal-digits}
ee3   \\U000 {five-hexadecimal-digits}
ee4   \\U0010 {four-hexadecimal-digits}
ee5   \\x\{ {hexadecimal-digits} \}
ee6   \\ {up-to-three-octal-digits}
ee7   \\ {escapable-character}
ee8   \\a | \\b | \\t | \\n | \\v | \\f | \\r
up-to-three-octal-digits (?x:{o1}|{o2}|{o3})
o1   {octal-digit}
o2   {octal-digit} {octal-digit}
o3   {octal-digit} {octal-digit} {octal-digit}
up-to-two-hexadecimal-digits (?x:{h1}|{h2})
h1 {hexadecimal-digit}
h2 {hexadecimal-digit} {hexadecimal-digit}
four-hexadecimal-digits {hexadecimal-digit}{hexadecimal-digit}{hexadecimal-digit}{hexadecimal-digit}
five-hexadecimal-digits {hexadecimal-digit}{hexadecimal-digit}{hexadecimal-digit}{hexadecimal-digit}{hexadecimal-digit}
hexadecimal-digits {hexadecimal-digit}+
hexadecimal-digit 0|1|2|3|4|5|6|7|8|9|A|B|C|D|E|F|a|b|c|d|e|f
octal-digit 0|1|2|3|4|5|6|7
/* TODO(egg): non-ASCII… */
escapable-character [^0-7uxUNabtnvfrpP]

named-element (?x:{ne1}|{ne2}|{ne3})
ne1 \\N\{ {ucd-identifier} \}
ne2 \\xN\{ {hexadecimal-digits} : {ucd-identifier} \}
ne3 \\xcN\{ {hexadecimal-digits} : {named-literal-element} : {ucd-identifier} \}
ucd-identifier {ucd-identifier-character}+

named-literal-element [^:}]
ucd-identifier-character [A-Za-z0-9\ _-]

bracketed-element \{{string-element}\}
string-literal \{\}|\{{string-elements}\}
string-element {bracketed-literal-element}|{escaped-element}|{named-element}
string-elements {string-element}{string-element}+
/* TODO(egg): non-ASCII… */
bracketed-literal-element [^\\}]

property-query {perl-start}{query-expression}{perl-end}|{posix-start}{query-expression}{posix-end}
perl-start  \\p\{|\\P\{
perl-end  \}
posix-start  \[:|\[:^
posix-end  :\]
query-expression {unary-query-expression}|{binary-query-expression}
unary-query-expression {optional-version-qualifier}{ucd-identifier}
binary-query-expression {optional-version-qualifier}{ucd-identifier}{query-operator}{property-predicate}
optional-version-qualifier {version-qualifier}?
version-qualifier (?x:{vq1}|{vq2}|{vq3})
vq1  U {version-number} :
vq2  U {version-suffix} :
vq3  U-1:
version-number (?x:{vn1}|{vn2}|{vn3})
vn1 {digits} {optional-suffix}
vn2 {digits} \. {digits} {optional-suffix}
vn3 {digits} \. {digits} \. {digits} {optional-suffix}
optional-suffix {version-suffix}?
version-suffix α|β|dev
digit 0|1|2|3|4|5|6|7|8|9
digits {digit}+
query-operator =|≠
property-predicate {property-value}|{regular-expression-match}|{property-comparison}
property-value {property-value-element}+
property-value-element {literal-value-element}|{escaped-element}|{named-element}
/* TODO(egg): Non-ASCII; exclude ≠. */
literal-value-element [^\\:{}=/@]
property-comparison @{unary-query-expression}@
regular-expression-match \/{regular-expression}\/
regular-expression {regular-expression-character}+
regular-expression-character {regex-unescaped}|\\{any}

/* TODO(egg): Non-ASCII */
regex-unescaped [^\\]
any .

%%

{white-space} {}

{literal-element} {
  puts("literal-element");
  return LITERAL_ELEMENT;
}

{escaped-element} {
  puts("escaped-element");
  return ESCAPED_ELEMENT;
}

{named-element} {
  puts("named-element");
  return NAMED_ELEMENT;
}

{bracketed-element} {
  puts("bracketed-element");
  return BRACKETED_ELEMENT;
}

{string-literal} {
  puts("string-literal");
  return STRING_LITERAL;
}

{property-query} {
  puts("property-query");
  return PROPERTY_QUERY;
}

%%

int yywrap() {
  return 0;
}

int main(int argc, char** argv) {
  yylex();
}