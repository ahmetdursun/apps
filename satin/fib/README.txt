This program calculates the Fibonacci numbers:

f(n) = (n < 2) ?
	    n :
	    f(n-1) + f(n-2)

In this Satin version, the recursion is replaced by spawns.

Program parameters are: <n>  [ <threshold> ]
where <n> stands for the "n" above, and <threshold> is a boundary
below which no spawns are done but recursion is used instead.
