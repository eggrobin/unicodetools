from collections import defaultdict

versions = [
  "2.0.0",
  "2.1.2",
  "3.0.0",
  "3.1.0",
  "3.1.1"
]

print('堊'.encode('cp932').decode('macroman'))

key_to_cp_to_version_to_value : dict[str, dict[str, dict[str, bytes]]] = defaultdict(lambda: defaultdict(dict))

B4_GARBAGE = "[B4 GARBAGE]"

key_to_version_to_encoding : dict[str, dict[str, str]] = {
  "kTang"       : {"2.0.0" : "macroman",
                   "3.0.0" : "latin-1",
                   "3.1.0" : B4_GARBAGE},
  "kMandarin"   : {"2.0.0" : "macroman",
                   "3.0.0" : "latin-1",
                   "3.1.0" : B4_GARBAGE},
  "kDefinition" : {
    "2.0.0" : "macroman",
    "3.0.0" : "latin-1",
#    "3.0.0" : "gb18030",
    "3.1.0" : "utf-8"
  }
}

KNOWN_ANOMALIES : dict[str, dict[str, dict[str, bytes]]] = {
  "kMandarin": {"U+4B3A" : { "3.1.0" : b"\x82\xb4" }},
}

for version in versions:
  with open(f"unicodetools/data/ucd/{version}-Update/Unihan.txt", 'rb') as f:
    data = f.read()
    lines = data.split(b"\r\n")
    non_ascii_lines = [line for line in lines if any(byte >= 0x80 for byte in line)]
    for line in non_ascii_lines:
      if not line.startswith(b"#"):
        cp, key, value = line.split(b"\t")
        key_to_cp_to_version_to_value[key.decode()][cp.decode()][version] = value

def clean_up(key: str, decoded_value:str) -> str:
  if key == "kDefinition":
    return decoded_value.rstrip().rstrip(";,")
  else:
    return decoded_value

for key, cp_to_version_to_value in key_to_cp_to_version_to_value.items():
  for cp, version_to_value in cp_to_version_to_value.items():
    if list(version_to_value) == ["3.1.1"]:
      continue
    anomaly = False
    value_3_1_1 = None
    if "3.1.1" in version_to_value:
      value_3_1_1 = version_to_value["3.1.1"].decode()
    for version, value in version_to_value.items():
      if version == "3.1.1":
        continue
      if key in key_to_version_to_encoding and version in key_to_version_to_encoding[key]:
        encoding = key_to_version_to_encoding[key][version]
        if (key in KNOWN_ANOMALIES and
            cp in KNOWN_ANOMALIES[key] and
            KNOWN_ANOMALIES[key][cp].get(version) == value):
          continue
        if encoding == B4_GARBAGE:
          if not all(byte == 0xB4 for byte in value if byte >= 0x80):
            raise ValueError("Not quite B4 garbage:", cp, key, value)
          continue
        try:
          cleaned_up_decoded_value = clean_up(key, value.decode(encoding))
          if cleaned_up_decoded_value != value_3_1_1:
            if version == "2.0.0":
              print(value.decode('cp932'))
            if version == "3.0.0" and cp == "U+8FD0":
              print(value.decode('latin-1').encode('macroman').decode('cp932'))
            print(version, repr(cleaned_up_decoded_value))
            anomaly = True
        except UnicodeDecodeError:
          print(cp, key, value, "# Version", version, "not in", encoding)
          anomaly = True
        continue
      print(version, value)
      anomaly = True
    if anomaly:
      if "3.1.1" in version_to_value:
        print("3.1.1", repr(value_3_1_1))
      print(cp, key)
      print(8 * "-")