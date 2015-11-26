(ns bunnymark.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]))

(declare bunnymark-game main-screen ui-screen)

(def gravity -0.7)
(def speed-max 23)
(def speed-min 1)

(defn create-bunny []
  (assoc (texture "wabbit_alpha.png") :x 0 :y 600
         :speed-x (+ 3 (rand 12))
         :speed-y (- (rand 10))
         :angle (- (rand 90) 45)))

(defn regulate [s]
  (let [ps (if (< s 0) (- s) s)]
    (cond
      (< ps speed-min) (+ speed-min (rand 5))
      (> ps speed-max) (- speed-max (rand 5))
      :else (+ ps (rand 5)))))

(defn animate [entity]
  (let [new-x (+ (:x entity) (:speed-x entity))
        new-y (+ (:y entity) (:speed-y entity))
        new-speed-y (+ (:speed-y entity) gravity)
        new-speed-x (if (or (< new-x 0) (> new-x (game :width))) (- (:speed-x entity)) (:speed-x entity))
        new-speed-y (if (< new-y 0) (regulate new-speed-y) new-speed-y)]
    (assoc entity :x new-x :y new-y :speed-x new-speed-x :speed-y new-speed-y)))

(defscreen main-screen
   :on-show
   (fn [screen entities]
     (update! screen :renderer (stage) :camera (orthographic))
     [(create-bunny) (create-bunny)])

   :on-render
   (fn [screen entities]
     (clear! 1 1 1 1)
     (some->> (map (fn [entity]
                     (->> entity
                          (animate)))
                   entities)
              (render! screen)))

   :on-resize
   (fn [screen entities]
     (height! screen 600))

   :on-touch-down
   (fn [screen entities]
     (screen! ui-screen :on-add-bunnies :bunnies 1000)
     (conj entities (repeatedly 1000 #(create-bunny))))

   :on-key-down
   (fn [screen entities]
     (if (key-pressed? :R)
       (set-screen! bunnymark-game main-screen ui-screen))))

(defscreen ui-screen
           :on-show
           (fn [screen entities]
             (update! screen :camera (orthographic) :renderer (stage) :bunnies 2)
             [(shape :filled :set-color (color :black)
                     :rect 5 5 100 39)
              (assoc (label "0" (color :green))
               :id :fps
               :x 10 :y 5)
              (assoc (label "0" (color :green))
                :id :bunnies
                :bunnies 2
                :x 10 :y 25)])

           :on-render
           (fn [screen entities]
             (->> (for [entity entities]
                    (case (:id entity)
                      :fps (doto entity (label! :set-text (str "FPS "(game :fps))))
                      :bunnies (doto entity (label! :set-text (str "Bunnies "(:bunnies entity))))
                      entity))
                  (render! screen)))

           :on-add-bunnies
           (fn [screen entities]
             (for [entity entities]
               (case (:id entity)
                 :bunnies (update entity :bunnies + (:bunnies screen))
                 entity)))

           :on-resize
           (fn [screen entities]
             (height! screen 600)))

(defscreen blank-screen
           :on-render
           (fn [screen entities]
             (clear!))

           :on-key-down
           (fn [screen entities]
             (if (key-pressed? :R)
               (set-screen! bunnymark-game main-screen ui-screen))))

(set-screen-wrapper! (fn [screen screen-fn]
                       (try (screen-fn)
                            (catch Exception e
                              (.printStackTrace e)
                              (set-screen! bunnymark-game blank-screen)))))

(defgame bunnymark-game
  :on-create
  (fn [this]
    (set-screen! this main-screen ui-screen)))
