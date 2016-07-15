;; Marshall Abrams
;;
;; This software is copyright 2016 by Marshall Abrams, and is distributed
;; under the Gnu General Public License version 3.0 as specified in the
;; the file LICENSE.

;; Clojure implementation of model of two-locus selection
;; in John Gillespie's _Population Genetics: A Concise Guide_, 2nd ed.,
;; Johns Hopkins University Press 2004, section 4.2, 
;; as described in Problem 4.3.  cf. Gillespie's Python solution at
;; the end of the chapter.

(ns linkage.two-locus)

(defn round2
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Gillespie's example of a model with selection with linkage.  A is
;;  the locus on which selection takes place.  The B locus is neutral.

;; Plotting example (reproduces right half of figure 4.4).
;; Note left half can't be calculated directly; r/s is never negative.
;; The left half is just the mirror image of the right half.
;;
;; (use '[incanter core charts svg]) ; core for view, charts for xy-plot
;; (let [ys (range 0.0001 0.04001 0.0001)
;;       xs (map #(B-het-ratio % 0.1 0.5) ys)
;;       ch (xy-plot ys xs)] ; make chart object
;;   (view ch) ; display it in a window
;;   (save-svg ch "het.svg") ; save it to a file
;;   xs)

(defn next-gen-hap-freqs
  "Given recombination probability r; selection parameters h and s with
  w(A1, A1)=1, w(A1, A2)=1-hs, w(A2, A2)=1-s; and previous-generation 
  frequencies of x1 through x3 for haplotypes A1B1, A1B2, A2B1, with 
  the frequency x4 of A2B2 being calculated from the others;
  returns a (lazy) sequence of first three haplotype frequencies for 
  the next generation, in the same order as in the parameter list.
  The fourth haplotype x4 can be calculated as (- 1 x1 x2 x3)."
  [r s h [x1 x2 x3]]
  (let [x4 (- 1 x1 x2 x3)
        p1 (+ x1 x2)  ; freq of A1
        q1 (- 1 p1)   ; freq of A2
        w-A1 (- 1 (* q1 h s))          ; mean fitness of A1 or x1 or x2 (wBar1)
        w-A2 (- 1 (* p1 h s) (* q1 s)) ; mean fitness of A2 or x3 or x4 (wBar3)
        w-bar (- 1 (* 2 p1 q1 h s) (* q1 q1 s)) ; overall mean fitness
        ;w-bar (- 1 (* q1 s)) ; overall mean fitness: book code's erroneous def
        D  (- (* x1 x4) (* x2 x3))]    ; linkage disequilibrium
    (map (fn [+- hap-freq hap-w]  ; cf. pp. 107,117
           (/ (+- (* hap-freq hap-w) 
                  (* r D (- 1 (* h s))))
              w-bar))
         [-    +    +]        ; and: -  [no need to calc x4 = (- 1 x1 x2 x3)]
         [x1   x2   x3]       ; and: x4
         [w-A1 w-A1 w-A2])))  ; and: w-A2

(defn hap-freqs
  "Iterates next-gen-hap-freqs to generate a lazy sequence of triplets of 
  haplotype frequencies x1, x2, x3: Given recombination probability r; 
  selection parameters h and s with w(A1, A1)=1, w(A1, A2)=1-hs, 
  w(A2, A2)=1-s; and previous-generation frequencies of x1 through x3 
  for haplotypes A1B1, A1B2, A2B1, with the frequency x4 of A2B2 being 
  calculated from the others; returns a lazy sequence of triplets 
  (sequences) of x1, x2, x3.  The fourth haplotype x4 can be calculated 
  as (- 1 x1 x2 x3)."
  [r s h x1 x2 x3]
  (iterate (partial next-gen-hap-freqs r s h)
           [x1 x2 x3]))

(defn freqs-at-p1-threshold
  "Given a sequence of haplotype frequency triples, returns the first
  triple for which the value of p1 = x1 + x2 is > threshold."
  [threshold freqs]
  (first 
    (drop-while (fn [[x1 x2 _]] (<= (+ x1 x2) threshold))
                freqs)))

(defn B-het
  "Given a sequence of haplotypes x1, x2, x3, returns the heterozygosity
  at the B locus."
  [[x1 _ x3]]
  (let [p2 (+ x1 x3)]    ; freq of B1
    (* 2 p2 (- 1 p2))))

(defn B-het-ratio-at-p1-threshold
  [threshold freqs]
  (let [initial-freqs (first freqs)
        final-freqs (freqs-at-p1-threshold threshold freqs)]
    ;(println initial-freqs final-freqs)                 ; DEBUG
    ;(println (B-het final-freqs) (B-het initial-freqs)) ; DEBUG
    (/ (B-het final-freqs)
       (B-het initial-freqs))))

(defn B-het-ratio
  "Given only a value for r, returns the B locus heterozygoisty of the first 
  element with p1 > 0.9999 from a lazy sequence of triplets of haplotype 
  frequencies of x1 through x3 for haplotypes A1B1, A1B2, A2B1, using the 
  parameters specified by Gillespie's problem 4.3:
  h = 1/2, s = 0.1, x1 = 1/10000, x2 = 0, x3 = 1 - x1.
  If additional parameters are provided, they'll replace these values."
  ([r]
   (B-het-ratio r 0.1 0.5))
  ([r s h]
   (B-het-ratio r s h 0.0001 0 0.4999))
  ([r s h x1 x2 x3]
   (let [threshold 0.9999] ; make into a parameter?
     (B-het-ratio-at-p1-threshold threshold
                                  (hap-freqs r s h x1 x2 x3)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; old version of last expression in next-gen-hap-freqs:
;        r-w14-D (* r w14 D)]       ; a repeated product below
;      (map #(/ % w-bar)               ; cf. pp. 107,117
;           [(- (* x1 w-bar-1) r-w14-D)
;            (+ (* x2 w-bar-1) r-w14-D) 
;            (+ (* x3 w-bar-3) r-w14-D) 
;            (- (* x4 w-bar-3) r-w14-D)])))
