(ns bunnymark.core.desktop-launcher
  (:require [bunnymark.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. bunnymark-game "bunnymark" 800 600)
  (Keyboard/enableRepeatEvents true))
