import codecs
from collections import defaultdict

ENCODINGS = ['ascii',
 'big5',
 'big5hkscs',
 'cp037',
 'cp273',
 'cp424',
 'cp437',
 'cp500',
 'cp720',
 'cp737',
 'cp775',
 'cp850',
 'cp852',
 'cp855',
 'cp856',
 'cp857',
 'cp858',
 'cp860',
 'cp861',
 'cp862',
 'cp863',
 'cp864',
 'cp865',
 'cp866',
 'cp869',
 'cp874',
 'cp875',
 'cp932',
 'cp949',
 'cp950',
 'cp1006',
 'cp1026',
 'cp1125',
 'cp1140',
 'cp1250',
 'cp1251',
 'cp1252',
 'cp1253',
 'cp1254',
 'cp1255',
 'cp1256',
 'cp1257',
 'cp1258',
 'euc_jp',
 'euc_jis_2004',
 'euc_jisx0213',
 'euc_kr',
 'gb2312',
 'gbk',
 'gb18030',
 'hz',
 'iso2022_jp',
 'iso2022_jp_1',
 'iso2022_jp_2',
 'iso2022_jp_2004',
 'iso2022_jp_3',
 'iso2022_jp_ext',
 'iso2022_kr',
 'latin_1',
 'iso8859_2',
 'iso8859_3',
 'iso8859_4',
 'iso8859_5',
 'iso8859_6',
 'iso8859_7',
 'iso8859_8',
 'iso8859_9',
 'iso8859_10',
 'iso8859_11',
 'iso8859_13',
 'iso8859_14',
 'iso8859_15',
 'iso8859_16',
 'johab',
 'koi8_r',
 'koi8_t',
 'koi8_u',
 'kz1048',
 'mac_cyrillic',
 'mac_greek',
 'mac_iceland',
 'mac_latin2',
 'mac_roman',
 'mac_turkish',
 'ptcp154',
 'shift_jis',
 'shift_jis_2004',
 'shift_jisx0213',
 'utf_32',
 'utf_32_be',
 'utf_32_le',
 'utf_16',
 'utf_16_be',
 'utf_16_le',
 'utf_7',
 'utf_8',
 'utf_8_sig'
]

print("F6 to 96:")
for decoding in ENCODINGS:
    for encoding in ENCODINGS:
        try:
            if b'\xF6'.decode(decoding).encode(encoding) == b"\x96":
                print(decoding, encoding)
        except (UnicodeDecodeError, UnicodeEncodeError):
            pass

print("E2 to 92:")
for decoding in ENCODINGS:
    for encoding in ENCODINGS:
        try:
            if b'\xE2'.decode(decoding).encode(encoding) == b"\x92":
                print(decoding, encoding)
        except (UnicodeDecodeError, UnicodeEncodeError):
            pass

print("Ü to D0:")
for decoding in ENCODINGS:
    for encoding in ENCODINGS:
        try:
            if 'Ü'.encode(encoding).decode(decoding).encode(encoding) == b"\xD0":
                print(decoding, encoding)
        except (UnicodeDecodeError, UnicodeEncodeError):
            pass

print("ä to 94:")
for decoding in ENCODINGS:
    for encoding in ENCODINGS:
        try:
            if 'ä'.encode(encoding).decode(decoding).encode(encoding) == b"\x94":
                print(decoding, encoding)
        except (UnicodeDecodeError, UnicodeEncodeError):
            pass

print("84 to 94:")
for decoding in ENCODINGS:
    for encoding in ENCODINGS:
        try:
            if b'\x84'.decode(decoding).encode(encoding) == b"\x94":
                print(decoding, encoding)
        except (UnicodeDecodeError, UnicodeEncodeError):
            pass

print("E4 to 94:")
for decoding in ENCODINGS:
    for encoding in ENCODINGS:
        try:
            if b'\xE4'.decode(decoding).encode(encoding) == b"\x94":
                print(decoding, encoding)
        except (UnicodeDecodeError, UnicodeEncodeError):
            pass
        
versions = [
  "2.0.0",
  "2.1.2",
  "3.0.0",
  "3.1.0",
  "3.1.1"
]

print('堊'.encode('cp932').decode('macroman'))

def double_encode_8_bit(data: bytes, decoding: str, encoding: str):
  result = b""
  for b in data:
    try:
      result += bytes([b]).decode(decoding).encode(encoding)
    except (UnicodeDecodeError, UnicodeEncodeError):
      result += bytes([b])
  return result

ENCODINGS_TO_TRY = [e for e in ENCODINGS if e.startswith("cp") or e.startswith("iso8859") or e.startswith("latin") or e.startswith("mac")]

bytes_2_0_0 = "Ü".encode("mac-roman")
e1, e2, e3, e4 = "mac_roman", "mac_iceland", "latin_1", "mac_iceland"
print(double_encode_8_bit(bytes_2_0_0, e1, e2))
print(double_encode_8_bit(double_encode_8_bit(bytes_2_0_0, e1, e2), e3, e4))

for e1 in ("mac-iceland",):
  for i, e2 in enumerate(ENCODINGS_TO_TRY):
    print(i, "/", len(ENCODINGS_TO_TRY), "...")
    for e3 in ENCODINGS_TO_TRY:
      for e4 in ENCODINGS_TO_TRY:
        for e5 in ENCODINGS_TO_TRY:
          for e6 in ENCODINGS_TO_TRY:
            good = 0
            results = []
            for bytes_2_0_0, expected_2_1_2 in (
              #("Ü".encode("mac-roman"), b"\xD0"),
              #("æ".encode("mac-roman"), b"\xCA"),
              #("ä".encode("mac-roman"), b"\x94"),
              ("堊".encode("shift-jis"), b"\x96\xAF"),
              ("ﾚﾟ".encode("shift-jis"), b"\xE9\x9C")):
              result = double_encode_8_bit(double_encode_8_bit(double_encode_8_bit(bytes_2_0_0, e1, e2), e3, e4), e5, e6)
              if result == expected_2_1_2:
                good += 1
              results.append((bytes_2_0_0.decode("mac-roman"), bytes_2_0_0, e1, e2, e3, e4, e5, e6,
                              result, "==" if result == expected_2_1_2 else "!=", expected_2_1_2))
            if good > 1:
              print(good, "/", 4)
              for line in results:
                  print(*line)
exit(0)

for c in ("堊", "運", "ﾚﾟ", ):
  print(c, c.encode("shift-jis")
        .decode("mac_iceland").encode("latin-1", "keep_mac_iceland").decode("mac_iceland")
        .encode("latin-1", "keep_mac_iceland").decode("iso8859_8", "decode_as_latin_1").encode("cp862", "keep_iso8859_8"))
for c in ("ä", "Ü"):
  print(c, c.encode("latin-1").decode("mac_iceland")
        .encode("latin-1", "keep_mac_iceland")
        .decode("iso8859_8", "decode_as_latin_1").encode("cp862", "keep_iso8859_8"))
#exit(0)


key_to_cp_to_version_to_value : dict[str, dict[str, dict[str, bytes]]] = defaultdict(lambda: defaultdict(dict))

B4_GARBAGE = "[B4 GARBAGE]"
VERSION_2_1_DOUBLE_ENCODING = "[2.1 salad]"

key_to_version_to_encoding : dict[str, dict[str, str]] = {
  "kTang"       : {"2.0.0" : "macroman",
                   "2.1.2" : VERSION_2_1_DOUBLE_ENCODING,
                   "3.0.0" : "latin-1",
                   "3.1.0" : B4_GARBAGE},
  "kMandarin"   : {"2.0.0" : "macroman",
                   "2.1.2" : VERSION_2_1_DOUBLE_ENCODING,
                   "3.0.0" : "latin-1",
                   "3.1.0" : B4_GARBAGE},
  "kJapaneseKun" : {"2.0.0" : "macroman",
                    "2.1.2" : VERSION_2_1_DOUBLE_ENCODING,
                    "3.0.0" : "latin-1",
                    "3.1.0" : B4_GARBAGE},
  "kDefinition" : {
    "2.0.0" : "macroman",
    "2.1.2" : VERSION_2_1_DOUBLE_ENCODING,
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
          if encoding == VERSION_2_1_DOUBLE_ENCODING:
            cleaned_up_decoded_value = clean_up(key, value.decode("cp1252").encode("mac_iceland").decode("cp1252"))
          else:
            cleaned_up_decoded_value = clean_up(key, value.decode(encoding))
          if cleaned_up_decoded_value != value_3_1_1:
            if version == "2.0.0":
              print(value.decode('cp932'), value)
            if version == "3.0.0" and cp == "U+8FD0":
              print(value.decode('latin-1').encode('mac_iceland').decode('cp932'), value)
            print(version, repr(cleaned_up_decoded_value), value)
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