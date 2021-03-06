This directory contains a collection of Ibis GMI example programs, organized
into a number of sub-directories (some of which may not be present in your
Ibis installation). See the README files in each sub-directory for more
details on the application.

Some low-level benchmarks and tests:
Generic
    Benchmark measuring group invocation with reply combining.
broadcast
    Measures multicast latency.
Remote
    Tests group invocation with reply discard.
invocation-gather
    Invocation combining benchmark, for throughput as well as latency.

Some applications:
QR
    A parallel implementation of QR factorization.
acp
    ACP (the Arc Consistency program) can be used as a first step in solving
    Constraint Satisfaction problems. It eliminates impossible values from
    domains of variables, by repeatedly applying constraints defined on pairs
    of variables.
asp
    All-pairs Shortest Path (ASP). This program computes the shortest path
    between ant two nodes of a given graph.
fft
    FFT is a complex 1D Fast Fourier Transform based on code from the
    SPLASH-2 suite.
leq
    A linear equation solver. Each iteration refines a candidate solution
    vector into a better solution. This is repeated until the difference
    becomes smaller than a specific bound.
tsp
    Tsp (travelling salesperson problem) computes the shortest path (roundtrip)
    for a salesperson to visit all cities in a given set exactly once,
    starting in one specific city, and ending up in the same city.

Other files in this directory are:

build.xml
    Ant build file for building Ibis GMI applications.
    "ant build" (or simply: "ant") will build all applications that
    are present in this directory. "ant clean" will remove
    what "ant build" made.
