%{
#include <stdio.h>

int yylex(void);
void yyerror(char* s);

%}

%token literal_element
%token escaped_element
%token named_element
%token bracketed_element
%token string_literal
%token property_query
%token '&' '-' '[' ']' '^'

%token unescaped_hyphen_minus_at_end_of_union
%token lexical_error

%%

UnicodeSet : Factor
           | NamedSingleton
           ;
Factor : '[' Union ']'
       | Complement
       | property_query
       ;
NamedSingleton : named_element
Complement : '[' '^' Union ']' { puts("Complement"); }
Union : Terms
      | UnescapedHyphenMinus Terms
      | Terms unescaped_hyphen_minus_at_end_of_union
      | UnescapedHyphenMinus Terms unescaped_hyphen_minus_at_end_of_union
      ;
UnescapedHyphenMinus : '-'
Terms :
      | Terms Term
      ;
Term : Elements
     | Restriction
     ;
Restriction : Factor
            | Intersection
            | Difference
            ;
Intersection : Restriction '&' Factor   { puts("Intersection"); } ;
Difference : Restriction '-' UnicodeSet { puts("Difference"); } ;
Elements : Element | Range ;
Range : RangeElement '-' RangeElement { puts("Range"); } ;
RangeElement : literal_element
             | escaped_element
             | named_element
             | bracketed_element
             ;
Element : RangeElement | string_literal ;

%%

void yyerror(char* s) {
  puts(s);
}

int main(int argc, char** argv) {
  return yyparse();
}