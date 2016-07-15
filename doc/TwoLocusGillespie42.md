# Gillespie's two-locus selected/neutral model 

A plot of the results of simulations based on Gillespie's version of a
model with linkage between a selected locus, *A*, and a neutral locus,
*B*.  This is currently the only thing implemented in this git repo.
See John Gillespie, *Population Genetics: A Concise Guide*, 2nd ed.,
Johns Hopkins University Press 2004, section 4.2.

## Simulation

The plot is the result of simulating the evolution of a population many
times, each time with a different recombination rate (probability) *r*,
from *r* = 0 to *r* = a maximum specified by, you, the user.  The values
of *r* vary by a small amount, say, 1/10000.  For each value of *r*, the
simulation runs for a several thousand generations, until the allele
frequencies stop changing significantly.  This is an "infinite
population" similation, which is to say that we assume that the
population is large enough that genetic drift can be ignored.  (This is
why we only need to run the simulation once for each recombination
rate.)

You also get to to specify the selection coefficient *s*, which is
the difference in fitness between the fitness, 1, of an organism that's
homozygous for allele *A*<sub>1</sub> and the fitness 1-*s* of an
organism that's homozygous for *A*<sub>2</sub>.    You can also
specify *h*, which specifies where in between the fitnesses of the two 
homozygotes the fitness of the heterozygote lies.  This model does
not have a way of specifying overdominance or underdominance: The
fitness of heterozygotes must be intermediate between the higher and
lower fitness homozygotes.

Finally, you can specify the initial haplotype frequencies
*x*<sub>1</sub>, *x*<sub>2</sub>, and *x*<sub>3</sub>:

* *x*<sub>1</sub> is the frequency of the *A*<sub>1</sub>*B*<sub>1</sub> haplotype.
* *x*<sub>2</sub> is the frequency of the *A*<sub>1</sub>*B*<sub>2</sub> haplotype.
* *x*<sub>3</sub> is the frequency of the *A*<sub>2</sub>*B*<sub>1</sub> haplotype.
* *x*<sub>4</sub>, the frequency of the *A*<sub>2</sub>*B*<sub>2</sub>
haplotype, will be calculated as 1 - (*x*<sub>1</sub> + *x*<sub>2</sub> +
*x*<sub>3</sub>).

Warning: You must specify *r*, *s* to be greater than 0 and less than or
equal to 1, i.e. in (0,1], and *h* must be strictly between 0 and 1,
i.e.  in (0,1); full dominance or recessivity isn't allowed, nor is
over- or underdominance.  Also, *x*<sub>1</sub>, *x*<sub>2</sub>, and
*x*<sub>3</sub> must each be greater than or equal to 0, and must sum to
less than or equal to 1.  Violation of these rules will produce
nonsensical results or throw the simulation into an infinite loop.

Since the simulation runs many times, for many generations, it can
take a few seconds to regenerate the plot, especially on a slow
computer. The number of simulations that get run depends on the values
of *s* and the maximum for *r*.  If *s* is small, you may have to wait
a bit.  Some browsers, such as Firefox, may pop up a warning message
about (e.g.) an "unresponsive script". Unless you've done something
forbidden with the parameters you specified, though, the simulations
are just taking a long time to run, and you can just tell Firefox to
let the script run.  If you don't like this message in Firefox, you
can use a different browser or [turn off the timeout
function](https://support.mozilla.org/en-US/kb/warning-unresponsive-script).


## More on what's in Gillespie's book

The goal of the simulation of the data is summarized in Problem 4.3.
Gillespie has Python implementation of the at the end of the chapter
(which has a mistake in one line, btw--perhaps intentionally?).  The
Python code only generates data, however.  Here I also generate a plot
of the simulation results, similar to the right half Gillespie's figure
4.4.  (The easiest way to generate the left half is simply to reflect
the right half around the zero *x* axis.  What seems to be represented
on the left is "negative" recombination rates or selection coefficients,
neither of which makes much sense.  However, Gillespie is using *r*/*s*
as a sort of abstract a measure of distance on a chromosome or chromsomes,
so he extends it in both directions.)

