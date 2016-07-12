# linkage

Experiments with linkage disequilibrium using Clojurescript, NVD3,
d3.js, Reagent (react.js), and figwheel.

## Overview

At present, there is only one such experiment, a Clojurescript
implementation of a simulation model of two-locus selection, with an
nvd3 plot of the results, following the material in John Gillespie's
*Population Genetics: A Concise Guide*, 2nd ed., Johns Hopkins
University Press 2004, section 4.2, as described in Problem 4.3.  Also
note Gillespie's Python implementation at the end of the chapter
(which has a mistake in one line, btw--perhaps intentionally?).

## License

This software is copyright 2016 by [Marshall
Abrams](http://members.logical.net/~marshall/), and is distributed
under the [Gnu General Public License version
3.0](http://www.gnu.org/copyleft/gpl.html) as specified in the file
LICENSE, except where noted, or where code has been included that was
released under a different license.  (For example, some of the source
files were created automatically by running 'lein new reagent ...', and
may be under a different license if I haven't modified them much.)


## Setup

Notes from the original figwheel README.md:

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`.
You will not get live reloading, nor a REPL.
