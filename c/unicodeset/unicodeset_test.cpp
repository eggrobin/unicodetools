#include <cstdio>
#include <fstream>
#include <print>
#include <set>
#include <string>
#include <string_view>

#include "../uca/sifter/ucd.hpp"

using UnicodeSet = std::set<std::u32string>;

extern "C" UnicodeSet* const unicodeset_parse_result;
extern "C" void unicodeset_Free();
extern "C" int yylex();
extern "C" int yyparse();
extern "C" std::FILE* yyin;

namespace {

void PrintSet(const UnicodeSet& set) {
  std::println();
  std::print("[");
  for (const std::u32string& s : set) {
    if (s.size() != 1) {
      std::print("{{");
    }
    for (const char32_t c : s) {
      std::print(R"(\x{{{:04x}}})", static_cast<std::uint32_t>(c));
    }
    if (s.size() != 1) {
      std::print("}}");
    }
  }
  std::println("]");
}

}  // namespace

int main(int argc, char** argv) {
  std::ifstream in("unicodeset_test.txt", std::ios_base::binary);
  std::string buffer;
  bool in_expression = false;
  while (in.good()) {
    buffer.push_back(in.get());
    if (!in_expression && buffer.ends_with("<<<<<")) {
      in_expression = true;
      buffer.clear();
    }
    if (in_expression && buffer.ends_with(">>>>>")) {
      in_expression = false;
      std::string_view expression(buffer.data(), buffer.size() - 5);
      std::string line;
      std::getline(in, line);
      auto [_, expected_error, _] = unicode::fields<3>(line);
      std::ofstream out("unicodeset_temp.txt", std::ios_base::binary);
      out.write(expression.data(), expression.size());
      out.close();
      yyin = fopen("unicodeset_temp.txt", "r");
      const bool error = yyparse();
      if (error) {
        while (yylex()) {}
      }
      std::println("{} Parsed {}: {}", (error == !expected_error.empty()) ? "---" : "***", expression, error);
      if (!error) {
        //std::println("--- {} elements:", unicodeset_parse_result->size());
        //PrintSet(*unicodeset_parse_result);
      }
    }
  }
}