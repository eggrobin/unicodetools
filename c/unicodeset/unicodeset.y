%{
#include <stdio.h>

int yylex(void);
void yyerror(char* s);

#include "unicodeset.h"

%}

%union {
  char32_t code_point;
  UnicodeSet* set;
}

%token <code_point> literal_element
%token <code_point> escaped_element
%token <code_point> named_element
%token <code_point> bracketed_element
%token <set> string_literal
%token <set> property_query
%token '&' '-' '[' ']' '^'

%token unescaped_hyphen_minus_at_end_of_union
%token lexical_error

%type <set> UnicodeSet
%type <set> Factor
%type <set> NamedSingleton
%type <set> Complement
%type <set> Union
%type <set> UnescapedHyphenMinus
%type <set> Terms
%type <set> Term
%type <set> Restriction
%type <set> Intersection
%type <set> Difference
%type <set> Elements
%type <set> Range
%type <code_point> RangeElement
%type <set> Element

%%

UnicodeSet : Factor         { unicodeset_ListCharacters($$); }
           | NamedSingleton { unicodeset_ListCharacters($$); }
           ;
Factor : '[' Union ']' { $$ = $2; }
       | Complement
       | property_query
       ;
NamedSingleton : named_element {
  $$ = unicodeset_Range($1, $1);
};
Complement : '[' '^' Union ']' {
  $$ = unicodeset_Difference(unicodeset_Range(0, 0x10FFFF), $3);
};
Union : Terms
      | UnescapedHyphenMinus Terms {
        $$ = unicodeset_Union($1, $2);
      }
      | Terms unescaped_hyphen_minus_at_end_of_union {
        $$ = unicodeset_Union($1, unicodeset_Range(U'-', U'-'));
      }
      | UnescapedHyphenMinus Terms unescaped_hyphen_minus_at_end_of_union {
        $$ = unicodeset_Union($1, $2);
      }
      ;
UnescapedHyphenMinus : '-' { $$ = unicodeset_Range(U'-', U'-'); }
Terms :  { $$ = unicodeset_Empty(); }
      | Terms Term { $$ = unicodeset_Union($1, $2); }
      ;
Term : Elements
     | Restriction
     ;
Restriction : Factor
            | Intersection
            | Difference
            ;
Intersection : Restriction '&' Factor   {
  $$ = unicodeset_Intersection($1, $3);
};
Difference : Restriction '-' UnicodeSet {
  $$ = unicodeset_Difference($1, $3);
};
Elements : Element | Range ;
Range : RangeElement '-' RangeElement { $$ = unicodeset_Range($1, $3); };
RangeElement : literal_element
             | escaped_element
             | named_element
             | bracketed_element
             ;
Element : RangeElement   { $$ = unicodeset_Range($1, $1); }
        | string_literal
        ;

%%

void yyerror(char* s) {
  puts(s);
}

int main(int argc, char** argv) {
  return yyparse();
}