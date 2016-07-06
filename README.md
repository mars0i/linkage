# re_linkage

Experiments with linkage disequilibrium using Clojurescript, NVD3,
d3.js, Reagent (react.js), and figwheel.

## Overview

Includes Clojurescript implementation of model of two-locus selection in
John Gillespie's _Population Genetics: A Concise Guide_, 2nd ed., Johns
Hopkins University Press 2004, section 4.2, as described in Problem 4.3.
cf. Gillespie's Python solution at the end of the chapter.

## Setup

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

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

This software is copyright 2016 by [Marshall
Abrams](http://members.logical.net/~marshall/), and is distributed
under the [Gnu General Public License version
3.0](http://www.gnu.org/copyleft/gpl.html) as specified in the file
LICENSE, except where noted, or where code has been included that was
released under a different license.  (For example, some of the source
files were created automatically by running 'lein new reagent ...', and
may be under a different license if I haven't modified them much.)

