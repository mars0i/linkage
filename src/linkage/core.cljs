;; Marshall Abrams
;;
;; This software is copyright 2016 by Marshall Abrams, and is distributed
;; under the Gnu General Public License version 3.0 as specified in the
;; the file LICENSE.

(ns linkage.core
  ;(:refer-clojure :exclude [boolean?])
  (:require [cljs.pprint :as pp]
            [cljs.spec :as s]
            [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [goog.string]
            [cljsjs.d3]
            [cljsjs.nvd3]
            [linkage.two-locus :as two]))

;; NOTE to get this to look like the NVD3 examples on the web, it was
;; crucial to use nv.d3.css instead of or from
;; resources/public/css/site.css.

;; Note: I name atoms with a terminal $ .

;; -------------------------
;; spec

(defn explain-data-problem-keys
  "Given the result of a call to spec/explain-data, returns the keys of 
  the tests that failed."
  [data]
  (map first 
       (keys  ; will this always work? should I get the :in val?
         (:cljs.spec/problems data))))


(defn ge-le [inf sup] (s/and #(>= % inf) #(<= % sup)))
(defn ge-lt [inf sup] (s/and #(>= % inf) #(<  % sup)))
(defn gt-le [inf sup] (s/and #(>  % inf) #(<= % sup)))
(defn gt-lt [inf sup] (s/and #(>  % inf) #(<  % sup)))

;; These expect to get numbers passed to them:
(s/def ::max-r (gt-le 0.0 0.5))
(s/def ::s     (gt-le 0.0 1.0))
(s/def ::h     (gt-lt 0.0 1.0))
;; setting freqs to 1 causes problems:
(s/def ::x1    (gt-lt 0.0 1.0)) ; 0 seems to cause problems
(s/def ::x2    (ge-lt 0.0 1.0))
(s/def ::x3    (ge-lt 0.0 1.0))
(s/def ::x1+x2+x3 (gt-le 0.0 1.0))

(s/def ::indiv-chart-params (s/keys :req-un [::max-r ::s ::h ::x1 ::x2 ::x3]))

(defn applied-interval-spec
  [inf inf-fn sup sup-fn arg]
  (s/and #(inf-fn arg inf)
         #(sup-fn arg sup)))

;(s/def ::freqs #(let [{:keys [x1 x2 x3]} %]
;                      (s/valid? (interval-spec 0.0 > 1.0 <=) (+ x1 x2 x3))))
;; Seems to work, but is it this the right way to do this?

;; This works, but seems ... wrong:
(s/def ::freqs (fn [{:keys [x1 x2 x3]}] (s/valid? ::x1+x2+x3 (+ x1 x2 x3))))

;; These don't work:

;(s/def ::freqs (fn [xs]
;                     (let [{:keys [x1 x2 x3]} xs
;                           sum (+ x1 x2 x3)]
;                       (applied-interval-spec 0.0 > 1.0 <= sum))))

;(s/def ::freqs (fn [params]
;                     (let [{:keys [x1 x2 x3]} params
;                           sum (+ x1 x2 x3)]
;                       (s/and #(> sum 0.0)
;                              #(<= sum 1.0)))))

;(s/def ::freqs (fn [params]
;                     (let [{:keys [x1 x2 x3]} params
;                           sum (+ x1 x2 x3)]
;                       #(and (> sum 0.0)
;                             (<= sum 1.0)))))

(s/def ::chart-params (s/and ::indiv-chart-params ::freqs))

;(defn conform-if-spec
;  "If spec is truthy, apply conform spec to second argument.
;  Otherwise return argument unchanged."
;  [spec x]
;  (if spec
;    (s/conform spec x)
;    x))
;
;(defn valid-if-spec?
;  "If spec is truthy, apply valid? spec to second argument.
;  Otherwise return true."
;  [spec x]
;  (if spec
;    (s/valid? spec x)
;    true))

;; -------------------------
;; app code

;; How many simulations to run--i.e. how many recombination rate r values?
(def num-sims 100)
(def svg-height 400)
(def svg-width 600)
(def chart-svg-id "chart-svg")
(def default-input-color "#000000")
(def error-input-color   "#FF0000")

(def copyright-sym (goog.string/unescapeEntities "&copy;")) 
(def nbsp (goog.string/unescapeEntities "&nbsp;")) 

;; Default simulation parameters
(defonce chart-params$ (r/atom {:max-r 0.02 :s 0.1 :h 0.5
                                :x1 0.0001 :x2 0 :x3 0.4999})) ; x4 = 0.5

(defonce default-chart-param-colors (zipmap (keys @chart-params$) 
                                            (repeat default-input-color)))

(defonce chart-param-colors$ (r/atom default-chart-param-colors))

(defn het-ratio-coords
  "Generate heterozygosity final/initial ratio for recombination rates r
  from 0 to max-r, using selection coefficient s and heterozygote factor h."
  [max-r s h x1 x2 x3]
  (let [rs (range 0.000 (+ max-r 0.00001) (/ max-r num-sims))
        het-ratios (map #(two/B-het-ratio % s h x1 x2 x3) rs)]
    (vec (map #(hash-map :x %1 :y %2)
              (map #(/ % s) rs) ; we calculated the data wrt vals of r,
              het-ratios))))      ; but we want to display it using r/s

(defn make-chart-config [chart-params$]
  "Make NVD3 chart configuration data object."
  (let [{:keys [max-r s h x1 x2 x3]} @chart-params$]
    (clj->js
      [{:values (het-ratio-coords max-r s h x1 x2 x3)
        :key "het-ratio" 
        :color "#0000ff" 
        ;:strokeWidth 1 
        :area false
        :fillOpacity -1}])))

(defn make-chart [svg-id chart-params$]
  "Create an NVD3 line chart with configuration parameters in @chart-params$
  and attach it to SVG object with id svg-id."
  (let [s (:s @chart-params$)
        chart (.lineChart js/nv.models)]
    ;; configure nvd3 chart:
    (-> chart
        (.height svg-height)
        (.width svg-width)
        ;(.margin {:left 100}) ; what does this do?
        (.useInteractiveGuideline true)
        (.duration 200) ; how long is gradual transition from old to new plot
        (.pointSize 1)
        (.showLegend false) ; true is useful for multiple lines on same plot
        (.showXAxis true)
        (.showYAxis true)
        (.forceY (clj->js [0,1]))) ; force y-axis to go to 1 even if data doesn't
    (-> chart.xAxis
        (.axisLabel "r/s")
        (.tickFormat (fn [d] (pp/cl-format nil "~,3f" d))))
    (-> chart.yAxis
        (.axisLabel "final/init heteterozygosity at the linked neutral locus")
        (.tickFormat (fn [d] (pp/cl-format nil "~,3f" d))))
    ;; add chart to dom using d3:
    (.. js/d3
        (select svg-id)
        (datum (make-chart-config chart-params$))
        (call chart))
    chart)) 

(defn spaces 
  "Returns a text element containing n nbsp;'s."
  [n]
  (into [:text] (repeat n nbsp)))

;; a "form-2" component function (https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components)
(defn chart-button
  [svg-id label1 label2]
  (let [label$ (r/atom label1)] ; runs only once
    (fn [svg-id label1 label2]  ; called repeatedly
      [:button {:type "button" 
                :id "chart-button"
                :on-click (fn []
                            (if (s/valid? ::chart-params @chart-params$)
                              (do 
                                (reset! label$ label2) ; button label should show it's running
                                (reset! chart-param-colors$ default-chart-param-colors) ; make sure input text is all the default
                                (js/setTimeout (fn [] ; allow DOM update b4 make-chart runs
                                                 (make-chart svg-id chart-params$)
                                                 (reset! label$ label1))
                                               100))
                              (do 
                                (reset! label$ "Uh-oh!")
                                (doseq [k (explain-data-problem-keys 
                                            (s/explain-data ::chart-params @chart-params$))]
                                  (swap! chart-param-colors$ assoc k error-input-color)))))}
       @label$])))

;; Note: for comparison, in lescent, I used d3 to set the onchange of 
;; dropdowns to a function that set a single global var for each.
(defn float-input 
  "Create a text input that accepts numbers.  k is keyword to be used to extract
  a default value from params$, and to be passed to swap! assoc.  It will also 
  be converted to a string an set as the id and name properties of the input 
  element.  This string will also be used as the name of the variable in the label,
  unless var-label is present, in which it will be used for that purpose."
  ([k params$ size label] (float-input k params$ size label [:em (name k)]))
  ([k params$ size label & var-label]
   (let [id (name k)
         old-val (k @params$)]
     [:span {:id (str id "-span")}
      (vec (concat [:text label " "] var-label [" : "]))
      [:input {:id id
               :name id
               :type "text"
               :style {:color (k @chart-param-colors$)} ;; PASS AS ARG?
               :size size
               :defaultValue old-val
               :on-change #(swap! params$ assoc k (js/parseFloat (-> % .-target .-value)))}]
      [spaces 4]])))

(defn float-text
  "Display a number with a label so that size is similar to float inputs."
  [n & label]
  (vec (concat [:text] label [": "]
               (list [:span {:style {:font-size "12px"}} 
                      (pp/cl-format nil "~,4f" n)]))))

(defn plot-params-form
  "Create form to allow changing model parameters and creating a new chart."
  [svg-id params$]
  (let [float-width 6
        {:keys [x1 x2 x3]} @params$]  ; seems ok: entire form re-rendered(?)
    [:form 
     [float-input :s params$ float-width "selection coeff"]
     [float-input :h params$ float-width "heterozygote coeff"]
     [float-input :max-r params$ float-width "max recomb prob" [:em "r"]]
     [spaces 4]
     [chart-button svg-id "re-run" "running..."]
     [:br]
     [float-input :x1 params$ float-width "" [:em "x"] [:sub 1]]
     [float-input :x2 params$ float-width "" [:em "x"] [:sub 2]]
     [float-input :x3 params$ float-width "" [:em "x"] [:sub 3]]
     [spaces 3]
     [float-text (- 1 x1 x2 x3) [:em "x"] [:sub 4]] ; display x4
     [spaces 13]
     [float-text (two/B-het [x1 x2 x3]) "initial neutral heterozygosity"]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:script {:type "text/javascript" :src "js/compiled/linkage.js"}]])

(defn home-render []
  "Set up main chart page (except for chart)."
  (head)
  [:div
   [:h3 "Simulations: effect of selection on a linked neutral locus"]
   [:h2 "See Gillespie's " [:em "Population Genetics: A Concise Guide"] 
    " 2nd ed., section 4.2., and the file TwoLocusGillespie42.md ."]
   [:text "Marshall Abrams (" copyright-sym " 2016, GPL v.3)"]
   [:div {:id "chart-div"}
    [:svg {:id chart-svg-id :height (str svg-height "px")}]
    [plot-params-form (str "#" chart-svg-id) chart-params$]]])

(defn home-did-mount [this]
  "Add initial chart to main page."
  (make-chart (str "#" chart-svg-id) chart-params$))

(defn home-page []
  (r/create-class {:reagent-render home-render
                   :component-did-mount home-did-mount}))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))

(init!)

;; ----------------------------

;; From simple figwheel template:
;; optionally touch your app-state to force rerendering depending on
;; your application
;; (swap! app-state update-in [:__figwheel_counter] inc)
(defn on-js-reload [])
