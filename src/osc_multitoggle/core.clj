(ns osc-multitoggle.core
  (:use [overtone.osc :only [osc-handle osc-rm-handler]]
        [clojure.string :only (split)]))

(def grid (atom {}))

(defn- path-params [path]
  (rest (split path #"/")))

(defn- get-layout-params [path]
  (first (path-params path)))

(defn- get-widget-params [path]
  (second (path-params path)))

(defn- get-row-params [path]
  (last (butlast (path-params path))))

(defn- get-column-params [path]
  (last (path-params path)))

(defn- path-from-params [layout widget row column]
  (str "/" layout "/" widget "/" row "/" column))

(defn- change-column-map [m layout widget row column value]
  (assoc-in (get m layout) [widget column row] value))

(defn- update-grid [msg]
  (let [layout (get-layout-params (get msg :path))
        row (get-row-params (get msg :path))
        column (get-column-params (get msg :path))
        widget (get-widget-params (get msg :path))
        value (first (:args msg))]
    (swap! grid assoc layout (change-column-map @grid layout widget row column value))))

(defn get-active-in-row [layout widget column]
  (let [rows (get-in @grid [layout widget column])]
    (keys (select-keys rows (for [[k v] rows :when (= v 1.0)] k)))))

(defn add-multitoggle-handler [server layout widget rows columns]
  (doseq [column (range 1 (+ columns 1))]
    (doseq [row (range 1 (+ rows 1))]
      (osc-handle server (path-from-params layout widget row column) update-grid))))

(defn remove-multitoggle-handler [server layout widget rows columns]
  (doseq [column (range 1 (+ columns 1))]
    (doseq [row (range 1 (+ rows 1))]
      (osc-rm-handler server (path-from-params layout widget row column))))
  (swap! grid assoc (str layout) (dissoc (get @grid (str layout)) widget)))