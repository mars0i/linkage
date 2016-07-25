

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

;; This works, but seems ... wrong:
;(s/def ::x1+x2+x3 (gt-le 0.0 1.0))
;(s/def ::freqs (fn [{:keys [x1 x2 x3]}] (s/valid? ::x1+x2+x3 (+ x1 x2 x3))))

;; These don't work:

;(defn applied-interval-spec
;  [inf inf-fn sup sup-fn arg]
;  (s/and #(inf-fn arg inf)
;         #(sup-fn arg sup)))

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

