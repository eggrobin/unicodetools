import os
import html
import html.parser
import re
import sys
from typing import Optional

CODE_POINT_OR_RANGE = re.compile(r"U\+([0-9A-F]{4,6})(?:\u2013U\+([0-9A-F]{4,6}))")

class SubHeadParser(html.parser.HTMLParser):
    def __init__(self, *, convert_charrefs: bool = True) -> None:
        super().__init__(convert_charrefs=convert_charrefs)
        self.block_to_id: dict[str, str] = {}
        self.range_to_id: dict[tuple[int, int], str] = {}

    def handle_starttag(self, tag: str, attrs: list[tuple[str, Optional[str]]]):
        if tag == "subheading":
            attrs: dict[str, Optional[str]] = dict(attrs)
            title = attrs["title"]
            id = attrs["id"]
            if not title or not id:
                raise ValueError(title, id)
            if "block" in attrs:
                self.block_to_id[title] = id
            elif ":" in title:
                tail = title.split(":", maxsplit=1)[1]
                for match in CODE_POINT_OR_RANGE.finditer(tail):
                    self.range_to_id[
                        (int(match.group(1), 16),
                         int(match.group(2) or match.group(1), 16))] = id

def main():
    parser = SubHeadParser()
    block_to_range: dict[str, tuple[int, int]] = {}
    with open(sys.argv[2] + "/Blocks.txt", encoding="utf-8") as f:
        for line in f.readlines():
            line = line.split("#", maxsplit=1)[0].strip()
            if not line:
                continue
            hex_range, block = line.split(";")
            block = block.strip()
            first, last = hex_range.split("..")
            block_to_range[block] = (int(first, 16), int(last, 16))
    for root, _, files in os.walk(sys.argv[1]):
        for file in files:
            if file.endswith(".svelte"):
                with open(root + "/" + file, encoding="utf-8") as f:
                    parser.feed(f.read())
    data = {block_to_range[block]: id for block, id in parser.block_to_id.items()}
    for (first, last), id in parser.range_to_id.items():
        for cp in range(first, last + 1):
            for (f, l), block_based_id in data.items():
                if f <= cp and cp <= l:
                    break
            else:
                continue
            del data[f, l]
            if cp > f:
                data[f, cp - 1] = block_based_id
            if cp < l:
                data[cp + 1, l] = block_based_id
        data[first, last] = id

    for (first, last), id in sorted(data.items()):
        print("%04X..%04X" % (first, last), ";", id)

if __name__ == "__main__":
  main()