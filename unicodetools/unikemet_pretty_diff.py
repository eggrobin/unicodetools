import difflib

from unikemet_diff import UNIKEMET_DIFF

print("""
<head>
  <style>
  .changed	 	{ background-color: #FFFF00; border-style: dotted; border-width: 1px; }
  .removed	 	{ text-decoration: line-through; background-color: #FFFF00; border-style: dotted; border-width: 1px; }
  </style>
</head>
<body>
""")
for provisional in False, True:
  if provisional:
    print(f"<h1>Changes to Provisional properties</h1>")
  else:
    print(f"<h1>Changes to Normative or Informative properties</h1>")
  for property, changes in UNIKEMET_DIFF.items():
    if ("Provisional" in property) != provisional:
      continue
    if property == "kEH_AltSeq (Provisional)":
      continue
    print(f"<h2>Changes for {property}</h2>")
    print("<table>")
    for codepoint, (old, new) in changes.items():
      if (old and ("<" in old or "&" in old)) or (new and ("<" in new or "&" in new)):
        raise ValueError(property, codepoint, old, new)
      old_words = list(old or "")
      new_words = list(new or "")
      diff = difflib.SequenceMatcher(
        None,
        old_words,
        new_words).get_opcodes()
      html_diff = ""
      for operation, old_begin, old_end, new_begin, new_end in diff:
        if operation == 'replace':
          html_diff += "<span class='removed'>" + "".join(old_words[old_begin:old_end]) + "</span>"
          html_diff += "<span class='changed'>" + "".join(new_words[new_begin:new_end]) + "</span>"
        elif operation == 'delete':
          html_diff += "<span class='removed'>" + "".join(old_words[old_begin:old_end]) + "</span>"
        elif operation == 'insert':
          html_diff += "<span class='changed'>" + "".join(new_words[new_begin:new_end]) + "</span>"
        else:
          html_diff += "".join(new_words[new_begin:new_end])
      print("<tr><th>", codepoint, "</th><td>", html_diff, "</td></tr>")
    print("</table>")
  print("</body>")