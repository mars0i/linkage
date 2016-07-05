(ns re-linkage.core
    (:require [reagent.core :as r :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljs.pprint :as pp]
              [re-linkage.two-locus :as two]
              [cljsjs.d3]
              [cljsjs.nvd3]))

;; -------------------------
;; app code

(def het-coord-seqs
  (let [xs (range 0.000 0.03001 0.0001)  ; different from in two-locus src
        ys (map #(two/B-het-ratio %, 0.1, 0.5) xs)]
    [xs ys]))

(defn het-rat-coords [[xs ys]]
    (vec (map #(hash-map :x %1 :y %2) xs ys)))

(defn make-chart-config [coords]
  (clj->js
    [{:values coords 
      :key "het-rat" 
      :color "#0000ff" 
      ;:strokeWidth 1 
      :area false
      :fillOpacity -1
      }]))

;; name atoms with terminal $
(def chart-config$
  (r/atom (make-chart-config (het-rat-coords het-coord-seqs))))

(defn setup-chart [svg-id chart-config]
  (let [chart (.lineChart js/nv.models)]
    (-> chart
        (.height 400)
        (.width 600)
        ;(.margin {:left 100})
        (.useInteractiveGuideline true)
        (.duration 300)
        (.pointSize 1)
        (.showLegend false) ; turn on for multiple lines on same plot
        (.showYAxis true)
        (.showXAxis true))
    (-> chart.xAxis
        (.axisLabel "r/s")
        (.tickFormat (fn [d] (pp/cl-format nil "~,3f" d))))
    (-> chart.yAxis
        (.axisLabel "final/init heteterozygosity at B locus")
        (.tickFormat (fn [d] (pp/cl-format nil "~,2f" d))))
    (.. js/d3
        (select svg-id)
        (datum chart-config)
        (call chart))
    chart))

;; original code for next lines: nv.utils.windowResize(chart.update);
;; or: nv.utils.windowResize(function() {chart.update()});
;(.windowResize js/nv.utils (.-update chart))
;(.windowResize js/nv.utils chart.update)
;(.. js/nv -utils (windowResize (fn [] (.update chart))))
;(.windowResize js/nv.utils #(chart.update))
;(.utils.windowResize js/nv (.update chart))
;(.windowResize (.utils js/nv) (.update chart))

;(def chart (setup-chart))
;(.addGraph js/nv chart)
;; https://github.com/nvd3-community/nvd3/blob/gh-pages/examples/lineChart.html
;; says:
;; "Wrapping in nv.addGraph allows for '0 timeout render', stores
;; rendered charts in nv.graphs, and may do more in the future... it's
;; NOT required"

;; -------------------------
;; Views

;(defn home-render []
;  [:div {:style {:min-width "310px" :max-width "800px" 
;                 :height "400px" :margin "0 auto"}}])

;; NOTE to get this to look like the NVD3 examples on the web, it was
;; crucial to use nv.d3.css instead of or from
;; resources/public/css/site.css.

(defn home-render []
  [:div [:h3 "Effect of selection on a linked neutral locus (Gillespie 2ed sect 4.2)"]
   [:div {:id "chart-div"}
    [:svg {:id "chart-svg" :height "400px"}]] ; height will be overridden by NVD3, but we need it here so Reagent knows where to put the next div
    ;[:svg {:id "chart-svg" :height "400px" :width "500px"}]]
   [:div {:id "data-div"} 
    [:text {:style {:font-size "12px"}}
     (pp/cl-format nil "~{ ~,3f~}" (second het-coord-seqs))]]])

(defn home-did-mount [this]
  (setup-chart "#chart-svg" @chart-config$))

(defn home-page []
  (r/create-class {:reagent-render home-render
                   :component-did-mount home-did-mount}))

;(defn home-page []
;  [:div [:h2 "Welcome to re_linkage"]
;   [:div [:a {:href "/about"} "go to about page"]]])

;(defn about-page []
;  [:div [:h2 "About re_linkage"]
;   [:div [:a {:href "/"} "go to the home page"]]
;   [:div {:id "chart-div"}
;    [:svg {:id "chart-svg" :width "400px" :height "300px"}]]
;   [:div {:id "data-div"} 
;    [:text (pp/cl-format nil "~{ ~,3f~}" (second het-coord-seqs))]]])

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
