import lark

with open("unicodeset.lark", encoding="utf-8") as f:
  early_parser = lark.Lark(f, start="unicode_set", lexer="basic")
with open("unicodeset-lalr(1).lark", encoding="utf-8") as f:
  # NOTE(egg): There appears to be a bug in the terminal overlap checking.
  # With strict=True, the lexer reports a conflict between BRACKETED_ELEMENT and
  # STRING_LITERAL on {\\33}, but this does not actually lex as a
  # BRACKETED_ELEMENT.  I suspect unescaping shenanigans.
  lalr_parser = lark.Lark(f, start="unicode_set", parser="lalr", lexer="basic")
for parser in (lalr_parser, early_parser):
  print(parser.parser.parser_conf.parser_type)
  print(parser.parse("[ a -   ]").pretty("|"))
  print(parser.parse("[ a - z ]").pretty("|"))
  print(parser.parse("[ [a - z] -       ]").pretty("|"))
  print(parser.parse("[ [a - z] - [c-x] ]").pretty("|"))