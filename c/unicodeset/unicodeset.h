#include <stdbool.h>
#include <uchar.h>

typedef struct String String;
typedef struct UnicodeSet UnicodeSet;
typedef struct UnicodeString UnicodeString;

char32_t unicodeset_GetOneCodePoint(char** string);
void unicodeset_ClearString(UnicodeString**);
void unicodeset_AppendToString(UnicodeString* string, char32_t code_point);
UnicodeSet* unicodeset_Empty();
UnicodeSet* unicodeset_SingletonString(UnicodeString* string);
UnicodeSet* unicodeset_Range(char32_t first, char32_t last);
UnicodeSet* unicodeset_Union(UnicodeSet* left, UnicodeSet* right);
UnicodeSet* unicodeset_Intersection(UnicodeSet* left, UnicodeSet* right);
UnicodeSet* unicodeset_Difference(UnicodeSet* left, UnicodeSet* right);
void unicodeset_ListCharacters(UnicodeSet* set);