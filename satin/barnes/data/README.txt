This directory contains a couple of data sets to try a visualizer on.
Each dataset D? has a corresponding args? file containing the parameters
to be used. The amount of computation can be made higher by increasing theta,
or reducing the timestep (in which case the number of iterations should be
increased). Similarly, the amount of computation can be made lower by reducing
theta or increasing the timestep. This will, however, make the computation less
precise and may result in strange behavior.

D1 is a 2500-body dataset consisting of a Plummer and a single, high-mass
body. Should be run with a timestep not larger than 0.03125 and theta at
least 2. Run for thousands of iterations.
D2 is a 4096-body dataset, similar in concept to D1.
D3 is a 4096-body dataset consisting of two Plummers colliding. Eventually
results in a merge of the two systems. Should be run with a timestep not
larger than 0.03125 and theta at least 2. Run for about 1500 iterations.
D4 is a 4096-body dataset consisting of a disk and a Plummer colliding.
Visualization is a bit disappointing.
D5 exercises the code on a highly irregular mass distribution. Over time the
fractal structure initially present will be erased, eventually leaving a 
relaxed, "monolithic" system.
D6 is like D3, but a 8192-body data-set.
D7 is like D3, but a 12288-body data-set.
D8 is sort of like D4, but a 8193-body data-set and a nicer disk.
D9, D10 and D11 are various collision-type data-sets, mostly Plummers and
disks.
