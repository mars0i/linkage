;; Marshall Abrams
;;
;; This software is copyright 2016 by Marshall Abrams, and is distributed
;; under the Gnu General Public License version 3.0 as specified in the
;; the file LICENSE.

(ns linkage.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljs.pprint :as pp]
              [goog.string :as gstr]
              [cljsjs.d3]
              [cljsjs.nvd3]
              [linkage.two-locus :as two]))

;; NOTE to get this to look like the NVD3 examples on the web, it was
;; crucial to use nv.d3.css instead of or from
;; resources/public/css/site.css.

;; -------------------------
;; app code

;; How many simulations to run--i.e. how many recombination rate r values?
(def num-sims 100)
(def svg-height 400)
(def svg-width 600)
(def button-color "white")
(def chart-svg-id "chart-svg")

(def copyright-sym (gstr/unescapeEntities "&copy;")) 

(defonce is-running-text$ (r/atom ""))

;; Default simulation parameters
;; note: I name atoms with a terminal $ .
(defonce chart-params$ (atom {:max-r 0.02 :s 0.1 :h 0.5}))
;; not currently using ratom capabilities, so use a regular Clojure atom

(defn het-ratio-coords [max-r s h]
  "Generate heterozygosity final/initial ratio for recombination rates r
  from 0 to max-r, using selection coefficient s and heterozygote factor h."
  (let [rs (range 0.000 (+ max-r 0.00001) (/ max-r num-sims))
        het-ratios (map #(two/B-het-ratio % s h) rs)]
    (vec (map #(hash-map :x %1 :y %2)
              (map #(/ % s) rs) ; we calculated the data wrt vals of r,
              het-ratios))))      ; but we want to display it using r/s

(defn update-params! [params$ k v]
  "Update params$ with value v for key k."
  (swap! params$ assoc k v))

(defn make-chart-config [chart-params$]
  "Make NVD3 chart configuration data object."
  (let [{:keys [max-r s h]} @chart-params$]
    (clj->js
      [{:values (het-ratio-coords max-r s h)
        :key "het-ratios" 
        :color "#0000ff" 
        ;:strokeWidth 1 
        :area false
        :fillOpacity -1}])))

(defn make-chart [svg-id chart-params$]
  "Create an NVD3 line chart with configuration parameters in @chart-params$
  and attach it to SVG object with id svg-id."
  ;(reset! is-running-text$ "running...")
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
        (.tickFormat (fn [d] (pp/cl-format nil "~,2f" d))))
    ;; add chart to dom using d3:
    (.. js/d3
        (select svg-id)
        (datum (make-chart-config chart-params$))
        (call chart))
    ;(reset! is-running-text$ "")
    chart)) 
     ;; in nvd3 examples, we return also chart, but not needed here

;; Note: for comparison, in lescent, I used d3 to set the onchange of 
;; dropdowns to a function that set a single global var for each.
(defn float-input 
  "Create a text input that accepts numbers.  k is keyword to be used to extract
  a default value from params$, and to be passed to update-params!.  It will also 
  be converted to a string an set as the id and name properties of the input 
  element."
  ([k params$ size label] (float-input k params$ size label (name k)))
  ([k params$ size label var-label]
   (let [id (name k)]
     [:span {:id (str id "-div")}
      [:text " " label " " [:em var-label] ": "]
      [:input {:id id
               :name id
               :type "text"
               :size size
               :defaultValue (k @params$)
               :on-change #(update-params! params$ k (js/parseFloat (-> % .-target .-value)))}]
      ])))

(defn chart-button
  [svg-id label1 label2 color1 color2]
  (let [label$ (r/atom label1)
        color$ (r/atom color1)]
    [:button {:type "button" 
              ;:style {:background-color @color$}
              ;:on-mouse-down #(reset! color$ color2)
              ;:on-mouse-up   #(reset! color$ color1)
              ;:on-mouse-down #(reset! label$ label2)
              ;:on-mouse-up #(reset! label$ label1)
              :on-click (fn []
                          ;(reset! color$ color2)
                          ;(reset! label$ label2)
                          ;(reset! color$ color1)
                          (println "about to reset")(flush)
                          (reset! is-running-text$ "running...")
                          ;(r/force-update-all)
                          (println "about to make chart")(flush)
                          (make-chart svg-id chart-params$)
                          (println "done making chart")(flush)
                          ;(reset! is-running-text$ "")
                          ;(println "just re-reset")(flush)
                          ;(reset! color$ color1)
                          ;(reset! button-label$ label1)
                          )
              }
     @label$]))

(defn plot-params-form
  "Create form to allow changing model parameters and creating a new chart."
  [svg-id chart-params$]
  [:form 
   [float-input :s chart-params$ 5 "selection coeff"]
   [float-input :h chart-params$ 5 "heterozygote coeff"]
   [float-input :max-r chart-params$ 5 "max recomb prob" [:em "r"]]
   [:text "  "] ; add space before button
   [chart-button svg-id "re-run" "running..." "#F0C0C0" "#F0A0A0"] ; doesn't really work
   [:text " yow! " @is-running-text$]]) ; also doesn't work.

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:script {:type "text/javascript" :src "js/compiled/linkage.js"}]])

(defn home-render []
  "Set up main page (except for chart, which is made elsewhere)."
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
