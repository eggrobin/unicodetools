#include <algorithm>
#include <cstdint>
#include <functional>
#include <print>
#include <iterator>
#include <set>
#include <string>

using UnicodeSet = std::set<std::u32string>;
using UnicodeString = std::u32string;

namespace {

static std::vector<std::function<void()>>& destructors = *new std::vector<std::function<void()>>;

template<typename T, typename...Args>
T* make(Args&&... args) {
  T* result = new T(std::forward<Args>(args)...);
  destructors.push_back([result]() { delete result; });
  return result;
}

}  // namespace

extern "C" void unicodeset_Free() {
  for (auto destructor : destructors) {
    destructor();
  }
  destructors.clear();
}

extern "C" void unicodeset_ClearString(UnicodeString** s) {
  if (*s == nullptr) {
    *s = make<UnicodeString>();
  } else {
    (*s)->clear();
  }
}

extern "C" void unicodeset_AppendToString(UnicodeString* string, char32_t code_point) {
  string->push_back(code_point);
}

extern "C" UnicodeSet* unicodeset_Empty() {
  return make<UnicodeSet>();
}

extern "C" char32_t unicodeset_GetOneCodePoint(const char** const string) {
  const std::uint8_t* s = reinterpret_cast<const std::uint8_t*>(*string);
  char32_t result;
  while (*s != 0) {
    if (s[0] < 0b01111111) {
      result = s[0];
      ++s;
    } else if(s[0] < 0b11100000) {
      result = ((s[0] & 0b00011111) << 6) |
               (s[1] & 0b00111111);
      s += 2;
    } else if(s[0] < 0b11110000) {
      result = ((s[0] & 0b00001111) << 12) |
               ((s[1] & 0b00111111) << 6) |
                (s[2] & 0b00111111);
      s += 3;
    } else {
      result = ((s[0] & 0b00000111) << 18) |
               ((s[1] & 0b00111111) << 12) |
               ((s[2] & 0b00111111) << 6)  |
                (s[3] & 0b00111111);
      s += 4;
    }
  }
  *string = reinterpret_cast<const char*>(s);
  return result;
}

extern "C" UnicodeSet* unicodeset_SingletonString(UnicodeString* string) {
  return make<UnicodeSet>(std::initializer_list{*string});
}

extern "C" UnicodeSet* unicodeset_Range(char32_t first, char32_t last) {
  auto* result = make<UnicodeSet>();
  if (last >= first) {
    for (char32_t cp = first; cp <= last; ++cp) {
      result->insert({cp});
    }
  } else {
    std::abort();
  }
  return result;
}

extern "C" UnicodeSet* unicodeset_Union(UnicodeSet* left, UnicodeSet* right) {
  left->merge(*right);
  return left;
}

extern "C" UnicodeSet* unicodeset_Intersection(UnicodeSet* left,
                                               UnicodeSet* right) {
  auto* result = make<UnicodeSet>();
  std::ranges::set_intersection(*left, *right,
                                std::inserter(*result, result->end()));
  return result;
}

extern "C" UnicodeSet* unicodeset_Difference(UnicodeSet* left,
                                             UnicodeSet* right) {
  auto* result = make<UnicodeSet>();
  std::ranges::set_difference(*left, *right,
                              std::inserter(*result, result->end()));
  return result;
}

extern "C" void unicodeset_ListCharacters(UnicodeSet* set) {
  std::println();
  std::print("[");
  for (const std::u32string& s : *set) {
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