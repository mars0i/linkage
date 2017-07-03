# linkage

Experiments with linkage disequilibrium using Clojurescript,
clojure.spec (cljs.spec), NVD3, d3.js, Reagent (react.js), and figwheel.

## Overview

At present, there is only one such experiment, a Clojurescript
implementation of a simulation model of two-locus selection, with an
nvd3 plot of the results, following the material in John Gillespie's
*Population Genetics: A Concise Guide*, 2nd ed., Johns Hopkins
University Press 2004, section 4.2, as described in Problem 4.3.  Also
note Gillespie's Python implementation at the end of the chapter
(which has a mistake in one line, btw--perhaps intentionally?).
[This page](http://members.logical.net/~marshall/linkage/TwoLocusInfo.html) has more information.

## License

This software is copyright 2016 by [Marshall
Abrams](http://members.logical.net/~marshall/), and is distributed
under the [Gnu General Public License version
3.0](http://www.gnu.org/copyleft/gpl.html) as specified in the file
LICENSE, except where noted, or where code has been included that was
released under a different license.  (For example, some of the source
files were created automatically by running 'lein new reagent ...', and
may be under a different license if I haven't modified them much.)

## General remarks on code

The experiments are written in Clojurescript, a dialect of Clojure.
Clojurescript code is compiled into Javascript code by the Clojurescript
compiler, and then possibly further compiled into more efficient
Javascript by the Google Clojure compiler.  So what is actually running
in your browser is Javascript code, but the source code here looks like
Clojure--because it is.

I also use the Reagent Clojurescript library (a wrapper around the
Javascript library React.js), to generate and update HTML
programmatically and cause immediate updating of what's displayed in
response to user input.

In addition, I use NVD3, a Javascript charting library built on the
Javascript library D3.js.  (It's easy to call Javascript functions from
Clojurescript code.)  The charts here could be generated directly
using D3, but it's easier with NVD3, and I end up with fancier charts
than I'd write from scratch.  (Try mousing over the plot.)

To compile the source code, you'll need working installations of Java,
Clojure, Clojurescript, and Leiningen.  The project is set up to use
Figwheel for iterative compilation during the development process.

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
