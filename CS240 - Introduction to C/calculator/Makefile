# Makefile for project 1
# format of entries;

# <target list>: <dependency list>
# 	<command list>

# make will look at the last-write dates of each file, and if the
# target file hasn't been updated since the source files were last
# modified, it will update the target file by executing the shell
# command.  Note that the character before <command list> MUST be a
# tab, not spaces.

calc: main.o getop.o stack.o getch.o
	gcc -Wall -o calc main.o getop.o stack.o getch.o

# note that if calc.h changes, main.c and getop.c must be recompiled

main.o: main.c calc.h
	gcc -Wall -c main.c 

getop.o: getop.c calc.h
	gcc -Wall -c getop.c 

stack.o: stack.c 
	gcc -Wall -c stack.c

getch.o: getch.c 
	gcc -Wall -c getch.c

# This tells make what to make if you just type 'make'; if this isn't
# there, the first rule in the file will be the default.

# .DEFAULT_GOAL := calc

# Here is a rule to clean up the directory when calc is final and you
# want to reduce the clutter in the listing.  Note that clean doesn't
# need to depend on any file modification time, so the <dependency
# list> position is just left empty.

clean:
	rm main.o getop.o stack.o getch.o

