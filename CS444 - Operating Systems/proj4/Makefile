CC=cc
CFLAGS=-Wall -O2

all: bsh

%: %.c
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm -f bsh
