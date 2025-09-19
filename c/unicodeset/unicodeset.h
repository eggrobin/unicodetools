#include <uchar.h>

typedef struct String String;
typedef struct UnicodeSet UnicodeSet;

UnicodeSet* unicodeset_Empty();
char32_t unicodeset_GetOneCodePoint(char** string);
UnicodeSet* unicodeset_SingletonString(char* string);
UnicodeSet* unicodeset_Range(char32_t first, char32_t last);
UnicodeSet* unicodeset_Union(UnicodeSet* left, UnicodeSet* right);
UnicodeSet* unicodeset_Intersection(UnicodeSet* left, UnicodeSet* right);
UnicodeSet* unicodeset_Difference(UnicodeSet* left, UnicodeSet* right);
void unicodeset_ListCharacters(UnicodeSet* set);