(ns fractals.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [fractals.fractal :as f]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)



(def default-mandelbrot {:left (f/v -2.0)
                         :top (f/v 1)
                         :width (f/v 3)
                         :height (f/v -2)})

(def mandelbrot-point    {:x (f/str->v "0")
                          :y (f/str->v "0")
                          :width (f/v 3)
                          :height (f/v -2)})


(defn width []
  (long (q/width)))

(defn height []
  (long (q/height)))
  

(defn n-color-gradient [colors]
  (let [interval-size (/ 1 (dec (count colors)))]
    (fn [percent]
      (let [normalized-percent (rem percent interval-size)
            color-index (quot percent interval-size)]
        (q/lerp-color 
          (nth colors color-index) 
          (nth colors (inc color-index))
          normalized-percent)))))
   
;will be set below in setup-color-gradient   
(def percent-to-color nil)
    
(defn setup-color-gradient []
  (def percent-to-color 
    (n-color-gradient 
      (list  (q/color 0 0 0)
             (q/color 0 0 255)
             (q/color 255 0 0)))))




(defn setup []
  (q/frame-rate 1)
  (q/color-mode :rgb)
  (setup-color-gradient)
  {:pause -1
   :fractal mandelbrot-point
   :pixels (map (fn[v]{:x v :y v :iterations 50}) (range 100))
   :iteration-depth 50})


(defn get-fractal-fn [state]
  (let [fractal (:fractal state)]
    (f/fractal-at (:x fractal)
                  (:y fractal)
                  (:width fractal)
                  (:height fractal))))

(defn get-iteration-depth [state]
  (:iteration-depth state))

(defn on-fractal [state f]
  (update-in state [:fractal] f))


(defn get-pixels [state]
  (let [fractal-fn (get-fractal-fn state)
        iteration-depth (get-iteration-depth state)]
    (fractal-fn (width) (height) iteration-depth)))



(defn update-state [state] 
  (-> state
    (update :pause inc)
    (assoc :pixels (get-pixels state))))



(defn normalize-fn [n max-n]
  (/ n max-n))

;accepts range between -6 to 6
(defn logistic-curve [x]  
  (/ 1 (+ 1 (Math/pow (Math/E) (- x)))))

(defn normalize-fn [n max-n]
  (let [x (- (* 12 (/ n max-n)) 6)]
    (logistic-curve x)))

(defn normalize-fn [n max-n]
  (/ n max-n))
  
(defn draw-point 
  ([frac max-iteration]
   (draw-point (:x frac) (:y frac) max-iteration (:iterations frac)))
  ([x y max-iteration iteration-reached]
    (q/set-pixel x y 
                 (if (= iteration-reached max-iteration)
                   (q/color 0 0 0)
                   (percent-to-color 
                     (normalize-fn iteration-reached max-iteration))))))


(defn print-fractal-info [fractal]
  (print "Point: ")
  (f/print-fractal-coord (:x fractal) (:y fractal))
  (println (str "Width: " (:width fractal)))
  (println (str "Height: " (:height fractal))))



(defn draw-state [state]
  (when (zero? (:pause state))
    (println "Calculating...")
    (print-fractal-info (:fractal state))
    (println (str "Count of Pixels: " (count (:pixels state))))
    (let [pixels (:pixels state)
          iteration-depth (get-iteration-depth state)]
      (loop [remaining pixels]
        (when (not (empty? remaining))
          (draw-point (first remaining) iteration-depth)
          (recur (rest remaining)))))          
    (println "Done.")))

(defn zoom-level [] "0.2") ;smaller means zoom more


(defn zoom-in [fractal]
  (fn [x y]
    (let [[fx fy] (f/coords->fractal-coords (long x) (long y))
          new-width (f/scale (:width fractal) (zoom-level))
          new-height (f/scale (:height fractal) (zoom-level))]
      {:x fx :y fy 
       :width new-width
       :height new-height})))


(defn on-left-mouse-click [state event]
  (let [zoom (zoom-in (:fractal state))]  
    (update state :fractal (fn[_] (zoom (:x event) (:y event))))))
    
  
(defn on-right-mouse-click [state event]
  (update state :iteration-depth #(int (* % 1.5))))
      

(defn on-center-mouse-click [state event]
  (-> state
      (assoc :fractal mandelbrot-point)
      (assoc :iteration-depth 50)))


;set center to mouse click
;zoom-in by 30 percent
(defn mouse-clicked [state event]
  (println event)
  ( -> 
   (cond 
    (= (:button event) :left)  (on-left-mouse-click state event)
    (= (:button event) :right) (on-right-mouse-click state event)
    (= (:button event) :center) (on-center-mouse-click state event))
   (assoc :pause -1)))  
    ;(update-in state [:fractal] f))


(defn start []
  (q/defsketch fractals
    :title "You spin my circle right round"
    :size [600 400]
    ; setup function called only once, during sketch initialization.
    :setup setup
    ; update-state is called on each iteration before draw-state.
    :update update-state
    :draw draw-state
    :features [:keep-on-top]
    :mouse-clicked mouse-clicked
    ; This sketch uses functional-mode middleware.
    ; Check quil wiki for more info about middlewares and particularly
    ; fun-mode.
    :middleware [m/fun-mode]))

(defn -main
  "Run the sketch."
  [& args]
  (println "Behold, the Mandelbrot!")
  (start))
