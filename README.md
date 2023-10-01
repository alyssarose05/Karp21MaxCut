### HOW TO COMPILE AND RUN:

COMPILE: Type this command: `javac Karp21MaxCut.java`

RUN: Type this command: `java Karp21MaxCut -inputFile [input file] -outputFile [output file] -size [size]`

- Sample input files: Small_Input.txt, Medium_Input.txt, Large_Input.txt (input.txt by default)
- The output file is output.txt by default.
- Available sizes: small (at least 10), medium (at least 15), large (at least 20)


ADDITIONAL FLAGS:
- `-bruteForce`: Solve using brute force.
- `-mathRandom`: Solve using `Math.random()`.
- `-bbs`: Solve using the Blum-Blum-Shub (BBS) algorithm.

- `-max_gen <number>`: The maximum number of generations you want to stop at (100 by default)
- `-max_sol <number>`: The maximum number of correct solutions you want to stop at (10000 by default)
- `-max_fitness <number>`: The maximum fitness you want to stop at (1 by default)

Use `--h` or `--help` for more help on flags.
