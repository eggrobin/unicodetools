#include <cstdio>
#include <fstream>
#include <print>
#include <string>
#include <string_view>

#include "unicodeset.h"

extern "C" void unicodeset_Free();
extern "C" int yylex();
extern "C" int yyparse();
extern "C" std::FILE* yyin;

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
      std::ofstream out("unicodeset_temp.txt", std::ios_base::binary);
      out.write(expression.data(), expression.size());
      out.close();
      yyin = fopen("unicodeset_temp.txt", "r");
      const int error = yyparse();
      if (error) {
        while (yylex()) {}
      }
      std::println("{} Parsed {}: {}", error == 0 ? "---" : "***", expression, error);
    }
  }
}
