(ns osc-multitoggle.core
  (:use [overtone.osc :only [osc-handle]]
        [clojure.string :only (split)]))

(def beat (atom {}))

(defn- path-params [path]
  (rest (split path #"/")))

(defn- get-layout-params [path]
  (first (path-params path)))

(defn- get-row-params [path]
  (last (butlast (path-params path))))

(defn- get-column-params [path]
  (last (path-params path)))

(defn- path-from-params [layout widget row column]
  (str "/" layout "/" widget "/" row "/" column))

(defn- change-column-map [m layout row column value]
  (assoc (get m layout) column (assoc (get (get m layout) column) row value)))

(defn- update-beat [msg]
  (let [layout (get-layout-params (get msg :path))
        row (get-row-params (get msg :path))
        column (get-column-params (get msg :path))
        value (first (:args msg))]
    (swap! beat assoc layout (change-column-map @beat layout row column value))))

(defn multitoggle-handler [server layout widget rows columns]
  (doseq [column (range 1 (+ columns 1))]
    (doseq [row (range 1 (+ rows 1))]
      (osc-handle server (path-from-params layout widget row column) update-beat))))