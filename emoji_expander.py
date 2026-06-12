import re

rules = {}

for file in ("emoji_grammar.txt", "emoji_grammar_roozbeh.txt"):
  with open(file) as f:
    for line in f.readlines():
      nonterminal, expansion = re.split(r"\s*:=\s*", line)
      rules[nonterminal] = expansion

  regex_from_hell = "emoji_sequence"

  previous = ""
  while previous != regex_from_hell:
    previous = regex_from_hell
    regex_from_hell = " ".join(
      " ( " + rules[word] + " ) " if word in rules else word
      for word in regex_from_hell.split())

  print(file, regex_from_hell)
  with open("C:/Users/robin/Projects/Unicode/icu/" + file.replace("_grammar", ""), "w") as rbbi:
    print("!!quoted_literals_only;", file=rbbi)
    print(
        re.sub(r" ZWJ ", r"[\\N{ZERO WIDTH JOINER}]",
        re.sub(r"\\p\{([^}]*)\}", r"[:\1:]",
        re.sub(r" \\x\{([0-9A-F]+) ([0-9A-F]+)\} ", r" ([\\x{\1}] [\\x{\2}]) ",
        re.sub(r" \\x\{([0-9A-F]+)\}( |$)", r" [\\x{\1}]\2", regex_from_hell)))) + ";",
        file=rbbi)