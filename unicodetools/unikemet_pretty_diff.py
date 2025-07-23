import difflib
import re

from unikemet_diff import UNIKEMET_DIFF

with open("unikemet_diff.html", "w", encoding="utf-8") as f:
  print("""
  <!DOCTYPE html>
  <html>
  <head>
    <style>
    .changed	 	{ background-color: #FFFF00; border-style: dotted; border-width: 1px; }
    .removed	 	{ text-decoration: line-through; background-color: #FFFF00; border-style: dotted; border-width: 1px; }
    </style>
    <title>Changes to Unikemet properties between 17.0β and 17.0</title>
  </head>
  <body>
    <p style=text-align:right>L2/25-178</p>
    <p>From: Robin Leroy</p>
    <p>Date: 2025-07-22</p>
  """, file=f)
  print(f"<h1>Changes to Unikemet properties between 17.0β and 17.0</h1>", file=f)
  for provisional in False, True:
    if provisional:
      print(f"<h2>Changes to Provisional properties</h2>", file=f)
    else:
      print(f"<h2>Changes to Normative or Informative properties</h2>", file=f)
    for property, changes in UNIKEMET_DIFF.items():
      if ("Provisional" in property) != provisional:
        continue
      print(f"<h3>Changes for {property}</h3>", file=f)
      if not changes:
        print("(None)", file=f)
        continue
      print("<table>", file=f)
      for codepoint, (old, new) in changes.items():
        if (old and ("<" in old or "&" in old)) or (new and ("<" in new or "&" in new)):
          raise ValueError(property, codepoint, old, new)
        old_words = re.split(r"\b", old or "")
        new_words = re.split(r"\b", new or "")
        diff = difflib.SequenceMatcher(
          None,
          old_words,
          new_words).get_opcodes()
        html_diff = ""
        for operation, old_begin, old_end, new_begin, new_end in diff:
          if operation == 'replace':
            old_letters = "".join(old_words[old_begin:old_end])
            new_letters = "".join(new_words[new_begin:new_end])
            intraword_diff = difflib.SequenceMatcher(
              None,
              old_letters,
              new_letters).get_opcodes()
            intraword_ops = set(op for op, _, _, _, _ in intraword_diff)
            if 'replace' not in intraword_ops and not ('insert' in intraword_ops and 'delete' in intraword_ops):
              for op, old_lbegin, old_lend, new_lbegin, new_lend in intraword_diff:
                if op == 'delete':
                  html_diff += "<span class='removed'>" + old_letters[old_lbegin:old_lend] + "</span>"
                elif op == 'insert':
                  html_diff += "<span class='changed'>" + new_letters[new_lbegin:new_lend] + "</span>"
                else:
                  html_diff += new_letters[new_lbegin:new_lend]
            else:
              html_diff += "<span class='removed'>" + "".join(old_words[old_begin:old_end]) + "</span>"
              html_diff += "<span class='changed'>" + "".join(new_words[new_begin:new_end]) + "</span>"
          elif operation == 'delete':
            html_diff += "<span class='removed'>" + "".join(old_words[old_begin:old_end]) + "</span>"
          elif operation == 'insert':
            html_diff += "<span class='changed'>" + "".join(new_words[new_begin:new_end]) + "</span>"
          else:
            html_diff += "".join(new_words[new_begin:new_end])
        print("<tr><th>", codepoint, "</th><td>", html_diff, "</td></tr>", file=f)
      print("</table>", file=f)
  print("</body></html>", file=f)