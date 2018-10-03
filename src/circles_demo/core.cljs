(ns circles-demo.core
  (:require
    [quil.core :as q]
    [quil.middleware :as m]
    [reagent.core :as r]))

(defn draw [{:keys [circles]}]
  (q/background 255)
  (doseq [{[x y] :pos [r g b] :color} circles]
    (q/fill r g b)
    (q/ellipse x y 10 10)))

(defn draw2 [{:keys [circles]}]
  (q/background 0)
  (doseq [{[x y] :pos [r g b] :color} circles]
    (q/fill r g b)
    (q/ellipse x y 10 10)))

(defn update-state [{:keys [width height] :as state}]
  (update state :circles conj {:pos   [(+ 20 (rand-int (- width 40)))
                                       (+ 20 (rand-int (- height 40)))]
                               :color (repeatedly 3 #(rand-int 250))}))

(defn init [width height]
  (fn []
    {:width   width
     :height  height
     :circles []}))

(defn sketch [& sketch-args]
  (let [active-sketch (r/atom nil)
        refs (r/atom {})
        reset-sketch! (fn [args]
                        (->> (into [:host (:canvas @refs)] args)
                             (apply q/sketch)
                             (reset! active-sketch)))]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (reset-sketch! sketch-args))
      :component-did-update
      (fn [component _]
        (when-let [s @active-sketch]
          (q/with-sketch s (q/exit)))
        (-> (r/argv component) rest reset-sketch!))
      :reagent-render
      (fn []
        [:canvas {:ref #(swap! refs assoc :canvas %)}])})))


(defn home-page []
  (r/with-let [running? (r/atom false)
               draw-fn (r/atom draw)
               width (/ (.-innerWidth js/window) 2)
               height (/ (.-innerHeight js/window) 2)]
    [:div
     [:h3 "circles demo"]
     [:div>button
      {:on-click #(swap! running? not)}
      (if @running? "stop" "start")]
     [:div>button
      {:on-click #(reset! draw-fn draw)}
      "draw 1"]
     [:div>button
      {:on-click #(reset! draw-fn draw2)}
      "draw 2"]
     (when @running?
       [sketch
        :draw @draw-fn
        :setup (init width height)
        :update update-state
        :size [width height]
        :middleware [m/fun-mode]])]))


(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
