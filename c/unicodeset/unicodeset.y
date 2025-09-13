%{
%}

%token literal_element
%token escaped_element
%token named_element
%token bracketed_element
%token string_literal
%token property_query
// A '-' that precedes a ']'; making the lexer distinguish it from an ordinary
// '-' makes the LALR(2) grammar LALR(1).
%token unescaped_hyphen_minus_at_end_of_union

%%

UnicodeSet : Factor | NamedSingleton
Factor : '[' Union ']'
       | Complement
       | property_query
NamedSingleton : named_element
Complement : '[' '^' Union ']'
Union : Terms
      | UnescapedHyphenMinus Terms
      | Terms unescaped_hyphen_minus_at_end_of_union
      | UnescapedHyphenMinus Terms unescaped_hyphen_minus_at_end_of_union
UnescapedHyphenMinus : '-'
Terms :
      | Terms Term
Term : Elements
     | Restriction
Restriction : Factor
            | Intersection
            | Difference
Intersection : Restriction '&' Factor
Difference : Restriction '-' UnicodeSet
Elements : Element | Range
Range : RangeElement '-' RangeElement
RangeElement : literal_element
             | escaped_element
             | named_element
             | bracketed_element
Element : RangeElement | string_literal

%%