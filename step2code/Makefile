

all: group compiler

group:
	@echo "Mahesh Babu Gorantla , Arjun Bery"

compiler: clean
	mkdir generated
	mkdir objects
	bison -d src/sample.ypp
	flex src/Scanner_Step2.l
	g++ -o micro sample.tab.cpp lex.yy.c	
	mv -f lex.yy.c sample.tab.cpp sample.tab.hpp generated/

clean:
	rm -rf generated > /dev/null
	rm -rf objects > /dev/null
	rm -f micro > /dev/null

.PHONY: all group compiler clean
