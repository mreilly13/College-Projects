/*
 * stack.c
 *
 * Stack routines
 */

#include <stdio.h>
#include <stdlib.h>
#include "calc.h"

#define MAXVAL 1024  /* maximum depth of val stack */

static int val[MAXVAL];    /* value stack */
static unsigned stackTop = 0; /* next free stack position */

/* push: push i onto value stack */
void push(int i) {
    /* If the stack has free position, store the value */
    if (stackTop < MAXVAL) {
        val[stackTop++] = i;
    }
    else {
        fprintf(stderr, "error: stack full, can't push %d\n", i);
        exit(1);
    }
}

/* pop: pop and return top value from stack */
int pop(void) {
    /* If the stack contains values, return the last one */
    if (stackTop > 0) {
        return val[--stackTop];
    }
    else {
        fprintf(stderr, "error: stack empty\n");
        exit(1);
    }
}

