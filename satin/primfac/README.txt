Splits a number N into its prime factors, by recursively splitting the
search space (initially the range 2..N) in two, until the search space
contains less than a certain threshold numbers. Then, each of these
numbers is tested: if it is a prime, it is decided if it is a factor of N,
and if so, how many times.

The program takes 1 parameter: N.
