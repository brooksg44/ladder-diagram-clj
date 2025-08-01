(ns ladder-diagram-clj.render
  "Enhanced rendering capabilities for ladder diagrams"
  (:require [ladder-diagram.elements :as elem]
            [ladder-diagram.ladder :as ladder]
            [clojure.string :as str]))

;; Enhanced ASCII rendering with better spacing and alignment
(defn render-element-with-padding
  "Render an element with proper padding and alignment"
  [element total-width]
  (let [elem-width (elem/element-width element)
        [name-line ascii-line] (elem/render-element-ascii element)
        padding (max 0 (- total-width elem-width))
        left-pad (int (/ padding 2))
        right-pad (- padding left-pad)]
    [(str (apply str (repeat left-pad " "))
          name-line
          (apply str (repeat right-pad " ")))
     (str (apply str (repeat left-pad "─"))
          ascii-line
          (apply str (repeat right-pad "─")))]))

(defn calculate-rung-layout
  "Calculate optimal layout for a rung"
  [rung]
  (let [elements (:elements rung)]
    (if (empty? elements)
      {:total-width 0 :element-widths []}
      (let [element-widths (mapv elem/element-width elements)
            min-spacing 2
            total-content-width (apply + element-widths)
            spacing-width (* (dec (count elements)) min-spacing)
            total-width (+ total-content-width spacing-width)]
        {:total-width total-width
         :element-widths element-widths
         :spacing min-spacing}))))

(defn render-rung-optimized
  "Render a rung with optimized spacing"
  [rung]
  (let [elements (:elements rung)
        layout (calculate-rung-layout rung)]
    (if (empty? elements)
      [""]
      (let [depth (elem/element-depth rung)
            rows (vec (repeat depth ""))]
        (loop [remaining-elements elements
               current-position 0
               result-rows rows]
          (if (empty? remaining-elements)
            result-rows
            (let [element (first remaining-elements)
                  elem-lines (elem/render-element-ascii element)
                  elem-width (elem/element-width element)]
              (recur (rest remaining-elements)
                     (+ current-position elem-width (:spacing layout 2))
                     (mapv #(str %1 (if (< %2 (count elem-lines))
                                      (get elem-lines %2)
                                      (apply str (repeat elem-width " ")))
                                 (if (seq (rest remaining-elements))
                                   (apply str (repeat (:spacing layout 2)
                                                      (if (zero? %2) "─" " ")))
                                   ""))
                           result-rows
                           (range depth))))))))))

(defn render-with-grid
  "Render ladder with grid background"
  [ladder]
  (let [base-render (elem/render-ascii ladder)
        lines (str/split-lines base-render)]
    (str/join "\n"
              (map-indexed
               (fn [idx line]
                 (if (and (> idx 0) (< idx (dec (count lines))))
                   (str line " │ " (mod idx 5))  ; Add grid markers
                   line))
               lines))))

(defn render-with-comments
  "Render ladder with comment annotations"
  [ladder comments]
  (let [base-render (elem/render-ascii ladder)
        lines (str/split-lines base-render)]
    (str/join "\n"
              (map-indexed
               (fn [idx line]
                 (let [comment (get comments idx)]
                   (if comment
                     (str line "  // " comment)
                     line)))
               lines))))

(defn export-to-svg
  "Export ladder diagram to SVG format"
  [ladder {:keys [width height scale] :or {width 800 height 600 scale 1.0}}]
  (let [rungs (:rungs ladder)
        svg-header (format "<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">"
                           width height)
        svg-footer "</svg>"
        rung-height (* 80 scale)
        start-y (* 50 scale)]
    (str svg-header
         "\n<g stroke=\"black\" stroke-width=\"2\" fill=\"none\">"
         ;; Power rails
         (format "\n<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"/>"
                 (* 20 scale) start-y (* 20 scale) (- height start-y))
         (format "\n<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"/>"
                 (- width (* 20 scale)) start-y (- width (* 20 scale)) (- height start-y))
         ;; Rungs
         (str/join ""
                   (map-indexed
                    (fn [idx rung]
                      (let [y (+ start-y (* idx rung-height))
                            x-start (* 20 scale)
                            x-end (- width (* 20 scale))]
                        (format "\n<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"/>"
                                x-start y x-end y)))
                    rungs))
         "\n</g>"
         svg-footer)))

(defn render-compact
  "Render ladder in compact format (minimal spacing)"
  [ladder]
  (let [base-render (elem/render-ascii ladder)
        lines (str/split-lines base-render)
        non-empty-lines (filter #(not (str/blank? %)) lines)]
    (str/join "\n" non-empty-lines)))

(defn render-with-line-numbers
  "Render ladder with line numbers"
  [ladder]
  (let [base-render (elem/render-ascii ladder)
        lines (str/split-lines base-render)]
    (str/join "\n"
              (map-indexed
               (fn [idx line]
                 (format "%3d: %s" (inc idx) line))
               lines))))

(defn analyze-ladder-complexity
  "Analyze and report ladder complexity metrics"
  [ladder]
  (let [stats (ladder/ladder-statistics ladder)
        rungs (:rungs ladder)
        max-elements (if (empty? rungs)
                       0
                       (apply max (map #(if (instance? ladder_diagram.ladder.Rung %)
                                          (count (:elements %))
                                          0) rungs)))
        avg-elements (if (zero? (:total-rungs stats))
                       0
                       (/ (:total-elements stats) (:total-rungs stats)))
        ascii-lines (count (str/split-lines (elem/render-ascii ladder)))]
    {:basic-stats stats
     :max-elements-per-rung max-elements
     :avg-elements-per-rung (float avg-elements)
     :total-ascii-lines ascii-lines
     :complexity-score (+ (:total-rungs stats)
                          (* 2 (:total-branches stats))
                          (* 0.5 (:total-elements stats)))}))

(defn render-debug
  "Render ladder with debug information"
  [ladder]
  (let [complexity (analyze-ladder-complexity ladder)
        base-render (elem/render-ascii ladder)]
    (str base-render
         "\n\n=== DEBUG INFO ==="
         "\nComplexity Analysis:"
         (format "\n  Total Rungs: %d" (get-in complexity [:basic-stats :total-rungs]))
         (format "\n  Total Elements: %d" (get-in complexity [:basic-stats :total-elements]))
         (format "\n  Total Branches: %d" (get-in complexity [:basic-stats :total-branches]))
         (format "\n  Max Elements/Rung: %d" (:max-elements-per-rung complexity))
         (format "\n  Avg Elements/Rung: %.1f" (:avg-elements-per-rung complexity))
         (format "\n  ASCII Lines: %d" (:total-ascii-lines complexity))
         (format "\n  Complexity Score: %.1f" (:complexity-score complexity)))))

;; Export functions
(defn export-ladder
  "Export ladder to various formats"
  [ladder format & options]
  (case format
    :ascii (elem/render-ascii ladder)
    :compact (render-compact ladder)
    :grid (render-with-grid ladder)
    :debug (render-debug ladder)
    :line-numbers (render-with-line-numbers ladder)
    :svg (apply export-to-svg ladder options)
    :comments (apply render-with-comments ladder options)
    (elem/render-ascii ladder)))

(defn save-to-file
  "Save ladder diagram to file"
  [ladder filename format]
  (let [content (export-ladder ladder format)]
    (spit filename content)
    (println (str "Ladder saved to " filename " in " (name format) " format"))))