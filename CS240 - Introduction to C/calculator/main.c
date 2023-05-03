/*
 * main.c
 *
 * Project: Reverse polish calculator
 * Author: Michael Reilly
 * Email: michael.reilly002@umb.edu
 * Date: 11/12/20
 * Notes:
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "calc.h" /* header file */

#define MAXOP 128

int main(int argc, char *argv[]) {
    int a, b, c, type;
    double op2;
    char s[MAXOP];
    
    while ((type = getop(s)) != EOF) {
        switch(type) {
            case NUMBER:
                push(atoi(s));
                break;
            case '+':
                push(pop() + pop());
                break;
            case '-':
                op2 = pop();
                push(pop() - op2);
                break;
            case '*':
                push(pop() * pop());
                break;
            case '/':
                op2 = pop();
                if (op2 != 0.0)
                    push(pop() / op2);
                else {
                    printf("error : zero divisor\n");
                    exit(1);
                }
                break;
            case '&':
                push(pop() & pop());
                break;
            case '|':
                push(pop() | pop());
                break;
            case '^':
                push(pop() ^ pop());
                break;
            case '~':
                push(~pop());
                break;
            case '>':
                push(pop() < pop());
                break;
            case '=':
                push(pop() == pop());
                break;
            case '<':
                push(pop() > pop());
                break;
            case '!':
                push(!pop());
                break;
            case '?':
                a = pop(), b = pop(), c = pop();
                push(a ? b : c);
                break;
            case '\n':
                printf("The answer is %d.\n", pop());
                break;
            default:
                printf("error: unknown command %s\n", s);
                exit(1);
        }
    }
}

