(ns fractals.fractal)
(set! *warn-on-reflection* true)

(import '(org.apfloat Apfloat ApfloatMath))

(defn ^Long get-precision [] 50)

(defn ^Apfloat v [^Long number] (Apfloat. number (get-precision)))
(defn ^Apfloat str->v [^String number] (Apfloat. number (get-precision)))


(defn ^Apfloat square [^Apfloat number]
  (.multiply number number))

(defn ^Apfloat update-x [^Apfloat x0 ^Apfloat x ^Apfloat y]
  (.add (.subtract (.multiply x x) (.multiply y y)) x0))

(defn ^Apfloat update-y [^Apfloat y0 ^Apfloat x ^Apfloat y]
  (.add (.multiply (.multiply x y) (v 2)) y0))


(defn is-constrained 
  ([[^Apfloat x0 ^Apfloat y0] iterations]
    (is-constrained x0 y0 iterations))
  ([^Apfloat x0 ^Apfloat y0 iterations]
    (let [c (v 2)]
      (loop [^Apfloat x (Apfloat. 0 (get-precision))
             ^Apfloat y (Apfloat. 0 (get-precision))
             i 0]
        (if (or (> (.add x y) 4)
                (>= i iterations))
          i
          (recur (update-x x0 x y)
                 (update-y y0 x y)
                 (inc i)))))))

;r is between 0 and 1
(defn ^Apfloat scale-pos-in-fractal [^Apfloat r ^Apfloat start  ^Apfloat length]
  (.add start (.multiply length r)))

(def all-pixels '())
(defn pixels [width height]
  (if (empty? all-pixels)
    (def all-pixels 
      (doall 
        (for [x (range width)
              y (range height)]
          [x y]))))
  all-pixels)



(defn ^Apfloat scale-fn [source-width ^Apfloat dest-start ^Apfloat dest-width]
  (let [step-size (.divide dest-width (v source-width))]
    (fn [number]
      (.add dest-start (.multiply (v number) step-size)))))
    


(defn window-coords->fractal-coords [window-width window-height 
                                     ^Apfloat fractal-width ^Apfloat fractal-height
                                     ^Apfloat fractal-left  ^Apfloat fractal-top]
  (let [x-step-size (.divide fractal-width (v window-width))
        y-step-size (.divide fractal-height (v window-height))]
    (fn [x y]
      [(.add fractal-left (.multiply x-step-size (v x)))
       (.add fractal-top (.multiply y-step-size (v y)))])))

(defn point-in-fractal [x y x-scale-fn y-scale-fn iterations]
  (let [x-in-fractal (x-scale-fn x)
        y-in-fractal (y-scale-fn y)]
        (merge {:x x :y y} (is-constrained x-in-fractal y-in-fractal iterations))))


(defn print-fractal-coord 
  ([[^Apfloat x ^Apfloat y]] (print-fractal-coord x y))
  ([^Apfloat x ^Apfloat y]
   (println (str "(" (.toString x) ", " (.toString y) ")"))))

(def coords->fractal-coords nil)

(defn ^Apfloat scale
  [^Apfloat number n] (.multiply number (v n)))

(defn ^Apfloat scale
  [^Apfloat number ^String n] (.multiply number (str->v n)))



(defstruct fractal-pixel :x :y :iterations)

(defn get-fractal-pixel [[x y] iterations]
  (struct-map fractal-pixel 
    :x x :y y
    :iterations (is-constrained (coords->fractal-coords x y) iterations)))


(defn fractal [^Apfloat left-pos ^Apfloat top-pos ^Apfloat width ^Apfloat height] 
  (fn [window-width window-height iterations]
    (def coords->fractal-coords (window-coords->fractal-coords 
                                 window-width window-height
                                 width height
                                 left-pos top-pos)) 
    (pmap
      (fn[[x y]] (struct-map fractal-pixel :x x :y y
                   :iterations (is-constrained (coords->fractal-coords x y) iterations)))
      (shuffle (pixels window-width window-height)))))

                     
(defn fractal-at [^Apfloat x ^Apfloat y ^Apfloat width ^Apfloat height]
  (let [left-pos (.subtract x (scale width "0.5"))
        top-pos  (.subtract y (scale height "0.5"))]
    (fractal left-pos top-pos width height)))



