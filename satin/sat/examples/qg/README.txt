This directory contains several quasigroup (or Latin square) instances.

A Latin square of order v is specified by:

   (x * u = y, x * w = y) ==> u = w 
   (u * x = y, w * x = y) ==> u = w 
   (x * y = u, x * y = w) ==> u = w 
   (x * y = 0) | (x * y = 1) | ... |  (x * y = (v-1))

Some additional constraints may be added.
The constraints used here are named as follows:

Name  Constraint              
qg1   (x * y = u, z * w = u, v * y = x, v * w = z) ==> x = z 
qg2   (x * y = u, z * w = u, y * v = x, w * v = z) ==> x = z
qg3   (x * y) * (y * x) = x
qg4   (x * y) * (y * x) = y
qg5   ((x * y) * x) * x = y
qg6   (x * y) * y = x * (x * y)
qg7   ((x * y) * x) * y = x

A Latin square of order v is encoded by v^3 propositional variables.
The number of propositional clauses are listed below:

 qg1-07.in          68083
 qg1-08.in         148957
 qg2-07.in          68083
 qg2-08.in         148957
 qg3-08.in          10469
 qg3-09.in          16732
 qg4-08.in           9685
 qg4-09.in          15580
 qg5-09.in          28540
 qg5-10.in          43636
 qg5-11.in          64054
 qg5-12.in          90919
 qg5-13.in         125464
 qg6-09.in          21844
 qg6-10.in          33466
 qg6-11.in          49204
 qg6-12.in          69931
 qg7-09.in          22060
 qg7-10.in          33736
 qg7-11.in          49534
 qg7-12.in          70327
 qg7-13.in          97072

The file qg1-07.in contains the propositional clauses in DIMACS format
for Latin square of order 7 with constraint qg1, etc.
These instances are used in the following paper, where the performances
of a dozen of different implementations of the Davis-Putnam
method were compared.

Zhang, H., Stickel, M.: (2000) 
Implementing the Davis-Putnam method.
Special issue on Propositional Satisfiability, 
J. of Automated Reasoning, to appear.

(available at http://www.cs.uiowa.edu/~hzhang/sato/papers)