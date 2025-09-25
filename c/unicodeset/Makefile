lex.yy.c : unicodeset.l
	flex $^

y.tab.c y.tab.h : unicodeset.y
	yacc -d $^

bin/lex.yy.o : lex.yy.c | y.tab.h unicodeset.h
	mkdir -p bin
	clang-20 -c -g $^ -o $@

bin/y.tab.o : y.tab.c | y.tab.h unicodeset.h
	mkdir -p bin
	clang-20 -c -g $^ -o $@

bin/unicodeset.o : unicodeset.cpp | unicodeset.h
	mkdir -p bin
	clang++-20 -c -g --std=c++23 --stdlib=libc++ $^ -o $@

bin/unicodeset_test.o : unicodeset_test.cpp
	mkdir -p bin
	clang++-20 -c -g --std=c++23 --stdlib=libc++ $^ -o $@

bin/unicodeset_test : bin/y.tab.o bin/lex.yy.o bin/unicodeset.o bin/unicodeset_test.o
	mkdir -p bin
	clang++-20 $^ --std=c++23 --stdlib=libc++ -o $@

test : bin/unicodeset_test
	$^

clean:
	rm lex.yy.c
	rm y.tab.*
	rm bin/*