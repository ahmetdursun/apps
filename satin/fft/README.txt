This FFT implementation was taken from the Princeton introduction to
Computer Science, see also http://www.cs.princeton.edu/introcs/97data.

This readme is also based on the text presented there.

The discovery of efficient algorithms for many problems has direct
impact on our everyday lives. The discrete Fourier transform is a
method for decomposing a waveform of N samples (e.g., sound) into
periodic components. The brute force solution takes time proportional
to N2. At the age of 27, Freidrich Gauss proposed a method that takes
roughly N log N steps and used it to analyze the periodic motion of
the asteroid Ceres. This method was later rediscovered and popularized
by Cooley and Tukey in 1965 after they described how to efficiently
implement it on a digital computer. Their motivation was detecting
nuclear tests in the Soviet Union and tracking Soviet submarines. The
FFT has become a cornerstone of signal processing, and is a crucial
component of devices like DVD players, cell phones, and disk
drives. It is also forms the foundation of many popular data formats
including JPEG, MP3, and DivX. Also speech analysis, music synthesis,
image processing. Doctors routinely use the FFT for medical imaging,
including Magnetic Resonance Imaging (MRI), Magnetic Resonance
Spectroscopy (MRS), Computer Assisted Tomography (CAT scans). Another
important application is fast solutions to partial differential
equations with periodic boundary conditions, most notably Poisson's
equation and the nonlinear Schroedinger equation. Without a fast way
to solve to compute the DFT none of this would be possible. Charles
van Loan writes "The FFT is one of the truly great computational
developments of this [20th] century. It has changed the face of
science and engineering so much that it is not an exaggeration to say
that life as we know it would be very different without the FFT."

Fourier analysis is a methodology for approximating a function
(signal) by a sum of sinusoidals (complex exponentials), each at a
diffferent frequency. When using computers, we also assume that the
the continuous function is approximated by a finite number points,
sampled over a regular interval. Sinusoids play a crucial role in
physics for describing oscillating systems, including simple harmonic
motion. The human ear is a Fourier analyzer for sound. Roughly
speaking, human hearing works by splitting a sound wave into
sinusoidal components. Each frequency resonates at a different
position in the basilar membrane, and these signals are delivered to
the brain along the auditory nerve. One of the main applications of
the DFT is to identify periodicities in data and their relative
strengths, e.g., filtering out high frequency noise in acoustic data,
isolating diurnal and annual cycles in weather, analyzing astronomical
data, performing atmospheric imaging, and identifying seasonal trends
in economic data.

Because the FFTs great importance, there
is a rich literature of efficient FFT algorithms, and there are many
high optimized library implementations available (e.g., Matlab). Our
implementation is a bare bones version that captures the most salients
ideas, but it can be improved in a number of ways. For example, it
works for any N, not just powers of 2. If the input is real (instead
of complex), it exploits additional symmetry and runs faster. Our FFT
implementation has a much higher memory footprint than required. With
great care, it is even possible to do the FFT in-place, i.e., with no
extra arrays other than x. Commericial FFT implementations also use
iterative algorithms instead of recursion. This can make the code more
efficient, but harder to understand. High performance computing
machines have specialized vector processors, which can perform vector
operations faster than an equivalent sequence of scalar
operations. Although computational scientists often measure
performance in terms of the number of flops (floating point
operations), with the FFT, the number of mems (memory accesses) is
also critical. Commercial FFT algorithms pay special attention to the
costs associated with moving data around in memory.



Program parameters are: <n> 
Where N is the size of the array to do FFT on.
The value of n must be a power of two.
