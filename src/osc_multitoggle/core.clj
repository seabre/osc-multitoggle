(ns osc-multitoggle.core
  (:use [overtone.osc :only [osc-handle]]
        [clojure.string :only (split)]))

(def grid (atom {}))

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

(defn- update-grid [msg]
  (let [layout (get-layout-params (get msg :path))
        row (get-row-params (get msg :path))
        column (get-column-params (get msg :path))
        value (first (:args msg))]
    (swap! grid assoc layout (change-column-map @grid layout row column value))))

(defn get-active-in-row [layout column]
  (let [rows (get (get @grid layout) column)]
    (keys (select-keys rows (for [[k v] rows :when (= v 1.0)] k)))))

(defn add-multitoggle-handler [server layout widget rows columns]
  (doseq [column (range 1 (+ columns 1))]
    (doseq [row (range 1 (+ rows 1))]
      (osc-handle server (path-from-params layout widget row column) update-grid))))
