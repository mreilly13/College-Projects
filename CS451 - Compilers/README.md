#j-- Compiler

This entire course was about one large project: a compiler for java, written in java, called j--. Most of the bones of the compiler were written already; The provided files were included with the textbook for this course, [Introduction to Compiler Construction in a Java World](https://www.cs.umb.edu/j--/). However, many fundamental features of java were not implemented. In order to make j-- a usable programming language, over the course of the semester each part of the compiler was modified to implement the following core features:
1. The Long and Double types
2. Operators:
    1. negation (!)
    2. unary plus (+)
    3. post-increment (i++)
    4. pre-decrement (--i)
    5. logical or
    6. not equal
    7. greater than or equal
    8. less than
    9. subtraction
    10. multiplication
    11. division
    12. modulo
    13. plus assign
    14. minus assign
    15. times assign
    16. divide assign
    17. modulo assign
    18. or assign
    19. and assign
    20. xor assign
    21. arithmetic left shift assign
    22. arithmetic right shift assign
    23. logical right shift assign
    24. ternary
3. switch statements
4. do while loop
5. for loop
6. break statement
7. continue statement
8. exception handling
9. interfaces

Implementing all of these operators and features required modifying the existing EBNF-compatible grammar, scanner, parser, type checker, and bytecode generating code for each.