;; Marshall Abrams
;;
;; This software is copyright 2016 by Marshall Abrams, and is distributed
;; under the Gnu General Public License version 3.0 as specified in the
;; the file LICENSE.

(ns re-linkage.core
    (:require [reagent.core :as r :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljs.pprint :as pp]
              [re-linkage.two-locus :as two]
              [cljsjs.d3]
              [cljsjs.nvd3]))

;; NOTE to get this to look like the NVD3 examples on the web, it was
;; crucial to use nv.d3.css instead of or from
;; resources/public/css/site.css.

;; -------------------------
;; app code

(defn het-rat-coords [max-r s h]
  (let [xs (range 0.000 (+ max-r 0.00001) 0.0001)  ; different from in two-locus src
        ys (map #(two/B-het-ratio % s h) xs)]
    (vec (map #(hash-map :x %1 :y %2) xs ys))))

;; name atoms with terminal $
(def chart-params$ (r/atom {:max-r 0.02 :s 0.1 :h 0.5}))

(defn make-chart-config [chart-params$]
  (let [{:keys [max-r s h]} @chart-params$
        coords (het-rat-coords max-r s h)]
    (clj->js
      [{:values coords 
        :key "het-rat" 
        :color "#0000ff" 
        ;:strokeWidth 1 
        :area false
        :fillOpacity -1}])))

;(def chart-config (make-chart-config @chart-params$))

(defn setup-chart [svg-id chart-params$]
  (let [chart (.lineChart js/nv.models)]
    ;; configure nvd3 chart:
    (-> chart
        (.height 400)
        (.width 600)
        ;(.margin {:left 100}) ; what does this do?
        (.useInteractiveGuideline true)
        (.duration 300)
        (.pointSize 1)
        (.showLegend false) ; true is useful if for multiple lines on same plot
        (.showYAxis true)
        (.showXAxis true))
    (-> chart.xAxis
        (.axisLabel "r/s")
        (.tickFormat (fn [d] (pp/cl-format nil "~,3f" d))))
    (-> chart.yAxis
        (.axisLabel "final/init heteterozygosity at B locus")
        (.tickFormat (fn [d] (pp/cl-format nil "~,2f" d))))
    ;; add chart to dom using d3:
    (.. js/d3
        (select svg-id)
        (datum (make-chart-config chart-params$))
        (call chart)))) 
     ;; in nvd3 examples, we return also chart, but not needed here

(defn plot-params-form
  [chart-params$]
  (let [{:keys [max-r s h]} @chart-params$
        max-r$ (atom max-r) ; regular Clojurescript atoms--
        s$ (atom s)         ; it's better not to force update
        h$ (atom h)]        ; every time a field changes
    [:form 
     {:on-submit #(swap! chart-params$ assoc :s @s$)}
     [:text "s:"]
     [:input {:id "s-text-input"
              :name "s"
              :type "text"
              :required ""
              :defaultValue @s$
              :on-change #(reset! s$ (-> % .-target .-value))
              }]
     [:input {:type "submit"
              :value "re-plot"}]]))


(defn home-render []
  [:div [:h3 "Effect of selection on a linked neutral locus (Gillespie 2ed sect 4.2)"]
   [:div {:id "chart-div"}
    [:svg {:id "chart-svg" :height "400px"}] ; height will be overridden by NVD3, but we need it here so Reagent knows where to put the next div
    [plot-params-form chart-params$]]])

(defn home-did-mount [this]
  (setup-chart "#chart-svg" chart-params$))

(defn home-page []
  (r/create-class {:reagent-render home-render
                   :component-did-mount home-did-mount}))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

;; THIS IS WHAT MAKES CALLS THE PAGE FUNCTIONS TO CREATE THE PAGES (?)
;; NOTE THERE IS CORRESPONDING BUT DIFFERENT CODE IN handler.js

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;(secretary/defroute "/about" []
;  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [current-page] (.getElementById js/document "app")))

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
