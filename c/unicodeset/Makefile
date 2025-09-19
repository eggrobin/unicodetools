lex.yy.c : unicodeset.l
	flex $^

y.tab.c y.tab.h : unicodeset.y
	yacc -d $^

bin/lex.yy.o : lex.yy.c | y.tab.h unicodeset.h
	clang-20 -c -g $^ -o $@

bin/y.tab.o : y.tab.c | y.tab.h unicodeset.h
	clang-20 -c -g $^ -o $@

bin/unicodeset.o : unicodeset.cpp | unicodeset.h
	clang++-20 -c -g --std=c++23 --stdlib=libc++ $^ -o $@

bin/unicodeset_parser : bin/y.tab.o bin/lex.yy.o bin/unicodeset.o
	clang++-20 $^ --std=c++23 --stdlib=libc++ -o $@

clean:
	rm lex.yy.c
	rm y.tab.*
	rm bin/*