

all: group compiler

group:
	@echo "Mahesh Babu Gorantla , Arjun Bery"

compiler: clean
	mkdir generated
	mkdir objects
	flex src/Scanner_Step2.l
	gcc lex.yy.c -lfl -o micro
	mv -f lex.yy.c generated/




clean:
	rm -rf generated 
	rm -rf objects
	rm -f micro

.PHONY: all group compiler clean
