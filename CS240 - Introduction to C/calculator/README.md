# Postfix Calculator

This project was to write a postfix calculator in C. There are four files: 
- stack.c implements a stack, which is used to store operands and operators before and during processing. 
- getch.c reads a single character from a buffer, with the ability to return a character to the buffer if necessary.
- getop.c determines if the top of the stack is a number or an operator; if it is an operator, that operator is returned, otherwise it returns a flag indicating that the top of the stack is a number. 
- main.c uses the appropriate stack functions and getop.c functions for processing to implement logic for basic arithmetic and binary operations.