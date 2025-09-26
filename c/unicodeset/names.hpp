#include <fstream>
#include <map>
#include <optional>
#include <string>
#include <string_view>

#include "../uca/sifter/ucd.hpp"

namespace unicode {

std::string UAX44LM2Skeleton(std::string_view name) {
  std::string result;
  for (int i = 0; i < name.size(); ++i) {
    char c = name[i];
    c = std::toupper(c);
    if (c == '-') {
      const bool medial = i > 0 && std::isalnum(name[i - 1]) && i < name.length() - 1 && std::isalnum(name[i + 1]);
      if (!medial || (result == "HANGULJUNGSEONGO" && i < name.length() - 1 && name[i + 1] == 'E')) {
        result += c;
      }
    } else if (c != ' ' && c != '_') {
      result += c;
    }
  }
  return result;
}

std::optional<char32_t> lookupByName(std::string_view name) {
  static const std::map<std::string, char32_t>& names = []() -> std::map<std::string, char32_t>& {
    auto& names = *new std::map<std::string, char32_t>;
    std::ifstream derived_name("../../unicodetools/data/ucd/dev/extracted/DerivedName.txt");
    for (std::string line; std::getline(derived_name, line);) {
      if (!line.empty() && line.back() == '\r') {
        line.pop_back();
      }
      const auto record = stripComment(line);
      if (record.empty()) {
        continue;
      }
      const auto [hexRange, name_pattern] = fields<2>(record);
      const auto range = parseHexCodePointRange(hexRange);
      for (char32_t cp : range) {
        std::string name(name_pattern);
        for (int i = 0; i < name.length(); ++i) {
          if (name[i] == '#') {
            name.replace(i, i + 1, std::format("{:04X}", static_cast<std::uint32_t>(cp)));
          }
        }
        names[UAX44LM2Skeleton(name)] = cp;
      }
    }
    return names;
  }();
  auto it = names.find(UAX44LM2Skeleton(name));
  if (it == names.end()) {
    return std::nullopt;
  }
  return it->second;
}

}  // namespace unicode