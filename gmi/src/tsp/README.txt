Tsp (travelling salesperson problem) computes the shortest path (roundtrip) for a salesperson
to visit all cities in a given set exactly once, starting in one specific city, and ending up
in the same city.
This program uses a branch-and-bound algorithm, which prunes a large part of the search space
by  ignoring routes that are already longer than the current best solution.
The program has the following arguments:

    <file>	filename of an input-file (table-15.1 is included as an example)
    -bound <num>
		sets the program's idea of the current shortest path to <num>. This
		is useful of you already know the length of the shortest path, to make
		the program run completely deterministically. The shortest path of
		table-15.1 is 3162.
