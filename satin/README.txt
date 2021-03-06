This directory contains a collection of Satin example programs, organized
into a number of sub-directories (some of which may not be present in your
Ibis installation). See the README files in each sub-directory for more
details on the application.

Some low-level benchmarks and tests:

hello
    Very simple spawn test.
spawntest
    Test of Satin spawn.
two_out_three
    Simple test of abort and inlet mechanism.

Some applications:

adapint
    Adaptive numerical integration. The basic idea is that f(x) is replaced
    by a straight line from (a, f(a)) to (b, f(b)) and the integral is
    approximated by computing the area bounded by the resulting trapezoid.
    This process is recursively continued for two subintervals as long as the
    area differs significantly from the sum of the areas of the subintervals'
    trapezoids.
barnes
    The Barnes-Hut algorithm simulates the evolution of a large set of bodies
    under the influence of forces. It can be applied to various domains of
    scientific computing, including but not limited to astrophysics, fluid
    dynamics, electrostatics and even computer graphics. In our implementation,
    the bodies represent planets and stars in the universe. The forces are
    calculated using Newton's gravitational theory.
barnes_inline
    A version of barnes with many small objects "inlined".
checkers
    This program plays rudimentary checkers, without double jumps. Both
    colors are played by the computer.
cover
    The Set Covering Problem. The task is to find a minimal set of subsets
    of a set S which covers the properties of all elements of the set S.
fib
    This program calculates the Fibonacci numbers. In this Satin version,
    the recursion is replaced by spawns.
fft
    This FFT implementation was taken from the Princeton introduction to
    Computer Science, see also http://www.cs.princeton.edu/introcs/97data,
    and was adapted for Satin.
grammy
    This directory contains a grammar-based text compressor. Given a text,
    it constructs a set of grammar rules that produce one sentence: the
    input text.
ida
    Iterative Deepening A* (IDA) is a combinatorial seach algorithm, based on
    repeated depth-first searches. IDA* tries to find all shortest solution
    paths to a given problem by doing a depth-first search up to a certain
    maximum depth. If the search fails, it is repeated with a higher search
    depth, until a solution is found.
knapsack
    The goal is to find a set of items, each with a weight w and a value v such
    that the total value is maximal, while not exceeding a fixed weight limit.
    The problem for n items is recursively divided into two subproblems for
    n-1 items, one with the missimg item placed into the knapsack, and one
    without it.
mmult
    Matrix multiplication.  This Satin application multiplies two matrices A
    and B, storing the result in a matrix C, by recursively dividing A and B
    into four quadrants each, and multiplying these smaller matrices.
mtdf
    This Awari implementation uses the MTDf algorithm to do a forward search
    through the possible game states. A transposition table is used to avoid
    searching identical positions multiple times. The parallel version
    speculatively searches multiple states in parallel, and replicates
    (a part of) the transposition table to avoid search overhead.
    Message combining is used to aggregate multiple transposition table
    updates into a singe network message, to avoid excessive communication.
noverk
    This Satin application computes the binomial coefficient, often also
    referred to as "n over k".
nqueens
    The N-Queens benchmark solves the combinatorially hard problem of placing
    N queens on a chess board such that no queen attacks another. Recursive
    search is used to find a solution.
paraffins
    Salishan Paraffins Problem: Given an integer n, output the chemical
    structure of all paraffin molecules for i<=n, without repetition and in 
    order of increasing size. Include all isomers, but no dupicates. The
    chemical formula for paraffin molecules is C(i)H(2i+2). Any representation
    for the molecules could be chosen, as long as it clearly distinguishes
    among isomers.
primfac
    Splits a number N into its prime factors, by recursively splitting the
    search space (initially the range 2..N) in two, until the search space
    contains less than a certain threshold numbers. Then, each of these
    numbers is tested: if it is a prime, it is decided if it is a factor of N,
    and if so, how many times.
raytracer
    The raytracer application computes a picture using an abstract description
    of a scene. The application is parallelized by recursively dividing the
    picture up to the pixel level. Next, the correct color of that pixel is
    calculated and the image is reassembled.
sat
    This directory contains several implementations of a solver for the
    Satisfiability (SAT) problem.
textcompress
    This directory contains a simple text compressor that uses backreferences
    to previous text as compression method. It looks ahead for a number of
    compression steps to select the best sequence of backreferences.
textindex
    This directory contains a small program to produce an index of a set
    of files. That is, as output it produces a list of files it has examined,
    and a list of all the words that occur in these files, plus for each word
    a list of files that the word occurs in.
tsp
    The Traveling Salesperson Problem (TSP) computes the shortest path for a
    salesperson to visit all cities in a given set exactly once, starting in
    one specific city.
tsp_tuple
    Like tsp, but uses the Satin tuple space for the distance table, and an
    active tuple for the minimum.

Other files in this directory are:

build.xml
    Ant build file for building Satin applications.
    "ant build" (or simply: "ant") will build all applications that
    are present in this directory. "ant clean" will remove
    what "ant build" made.
