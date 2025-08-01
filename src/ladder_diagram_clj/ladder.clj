(ns ladder-diagram-clj.ladder
  "Core ladder diagram logic - rungs, branches, and ladders"
  (:require [ladder-diagram-clj.elements :as elem]))

;; Forward declarations
(declare render-rung-ascii render-branch-ascii render-ladder-ascii)

;; Rung structure
(defrecord Rung [elements]
  elem/Renderable
  (render-ascii [rung]
    (render-rung-ascii rung))
  (render-name [rung]
    "")
  (element-width [rung]
    (if (empty? (:elements rung))
      0
      (reduce + (map elem/element-width (:elements rung)))))

  elem/Measurable
  (element-depth [rung]
    (if (empty? (:elements rung))
      2
      (apply max (map elem/element-depth (:elements rung))))))

;; Branch structure
(defrecord Branch [rungs]
  elem/Renderable
  (render-ascii [branch]
    (render-branch-ascii branch))
  (render-name [branch]
    "")
  (element-width [branch]
    (if (empty? (:rungs branch))
      0
      (apply max (map elem/element-width (:rungs branch)))))

  elem/Measurable
  (element-depth [branch]
    (+ (apply + (map elem/element-depth (:rungs branch)))
       (count (:rungs branch)))))

;; Ladder structure
(defrecord Ladder [rungs]
  elem/Renderable
  (render-ascii [ladder]
    (render-ladder-ascii ladder))
  (render-name [ladder]
    "")
  (element-width [ladder]
    (if (empty? (:rungs ladder))
      0
      (apply max (map elem/element-width (:rungs ladder))))))

;; Constructor functions
(defn rung
  "Create a new rung with the given elements"
  [& elements]
  (->Rung (vec elements)))

(defn branch
  "Create a new branch with the given rungs"
  [& rungs]
  (->Branch (vec rungs)))

(defn ladder
  "Create a new ladder with the given rungs"
  [& rungs]
  (->Ladder (vec rungs)))

;; Rendering functions
(defn render-element-for-rung
  "Render an element appropriately for use in a rung"
  [element]
  (cond
    ;; If it's a LadderElement, use the standard rendering
    (instance? ladder_diagram_clj.elements.LadderElement element)
    (elem/render-element-ascii element)

    ;; If it's a Branch, render it as ASCII and format appropriately
    (instance? Branch element)
    (let [branch-lines (elem/render-ascii element)
          width (elem/element-width element)]
      (if (seq branch-lines)
        (vec branch-lines)
        ["" ""]))

    ;; For anything else that implements Renderable, render as lines
    (satisfies? elem/Renderable element)
    (let [ascii-result (elem/render-ascii element)]
      (if (string? ascii-result)
        [ascii-result]  ; Convert string to vector of lines
        (vec ascii-result)))

    ;; Fallback
    :else
    ["" ""]))

(defn render-rung-ascii
  "Render a rung as ASCII art"
  [rung]
  (let [elements (:elements rung)]
    (if (empty? elements)
      ["" ""]
      (let [depth (elem/element-depth rung)
            rendered-elements (map render-element-for-rung elements)
            rows (into {} (for [i (range depth)] [i ""]))

            ;; Build each row by concatenating element content
            result-rows
            (reduce
             (fn [rows [elem rendered]]
               (let [elem-depth (elem/element-depth elem)
                     elem-width (elem/element-width elem)]
                 (reduce
                  (fn [rows i]
                    (let [padding (if (and (< i elem-depth)
                                           (seq (get rendered i)))
                                    (if (.startsWith (get rendered i) "─")
                                      "──"
                                      "  ")
                                    "  ")
                          content (if (< i elem-depth)
                                    (get rendered i "")
                                    (apply str (repeat elem-width " ")))]
                      (update rows i str padding content)))
                  rows
                  (range depth))))
             rows
             (map vector elements rendered-elements))]
        (mapv #(get result-rows %) (range depth))))))

(defn render-branch-ascii
  "Render a branch as ASCII art"
  [branch]
  (let [rungs (:rungs branch)
        rung-count (count rungs)]
    (if (empty? rungs)
      []
      (loop [result []
             rung-idx 0
             branching? false]
        (if (>= rung-idx rung-count)
          result
          (let [current-rung (nth rungs rung-idx)
                is-first? (zero? rung-idx)
                is-last? (= rung-idx (dec rung-count))
                rung-lines (elem/render-ascii current-rung)
                joining? (some #(.endsWith % "─") rung-lines)

                processed-lines
                (map-indexed
                 (fn [line-idx line]
                   (let [starts-with-wire? (.startsWith line "─")]
                     (cond
                       starts-with-wire?
                       (cond
                         (and is-first? (not branching?))
                         (str "─┬" line (if joining? "─┬─" ""))

                         (and is-last? branching?)
                         (str " └" line (if joining? "─┘ " ""))

                         branching?
                         (str " ├" line (if joining? "─┤ " ""))

                         :else
                         (str "  " line (if joining? "   " "")))

                       branching?
                       (str " │" line (if joining? " │ " ""))

                       :else
                       (str "  " line (if joining? "   " "")))))
                 rung-lines)

                ;; Add separator line if branching and not last
                separator (when (and branching? (not is-last?))
                            (let [line-length (count (first processed-lines))]
                              (str " │" (apply str (repeat (- line-length 2) " "))
                                   (if joining? " │ " ""))))

                new-branching? (or branching? is-first?)]

            (recur (concat result
                           processed-lines
                           (when separator [separator]))
                   (inc rung-idx)
                   new-branching?)))))))

(defn render-ladder-ascii
  "Render a complete ladder as ASCII art"
  [ladder]
  (let [rungs (:rungs ladder)]
    (if (empty? rungs)
      "█\n█"
      (let [rendered-rungs (map elem/render-ascii rungs)
            ladder-lines (mapcat identity rendered-rungs)]
        (str "█\n"
             (clojure.string/join "\n"
                                  (map #(str "█"
                                             (if (.startsWith % "─") "──" "  ")
                                             %)
                                       ladder-lines))
             "\n█")))))

;; Utility functions
(defn add-element-to-rung
  "Add an element to the end of a rung"
  [rung element]
  (update rung :elements conj element))

(defn remove-element-from-rung
  "Remove an element at the given index from a rung"
  [rung index]
  (update rung :elements
          (fn [elements]
            (vec (concat (take index elements)
                         (drop (inc index) elements))))))

(defn add-rung-to-ladder
  "Add a rung to the end of a ladder"
  [ladder rung]
  (update ladder :rungs conj rung))

(defn add-rung-to-branch
  "Add a rung to a branch"
  [branch rung]
  (update branch :rungs conj rung))

(defn get-element-at
  "Get element at specific rung and element indices"
  [ladder rung-idx elem-idx]
  (when-let [target-rung (get-in ladder [:rungs rung-idx])]
    (if (instance? Rung target-rung)
      (get-in target-rung [:elements elem-idx])
      nil)))

(defn update-element-at
  "Update element at specific rung and element indices"
  [ladder rung-idx elem-idx new-element]
  (assoc-in ladder [:rungs rung-idx :elements elem-idx] new-element))

(defn ladder-statistics
  "Get statistics about a ladder"
  [ladder]
  (let [rungs (:rungs ladder)
        total-rungs (count rungs)
        total-elements (reduce + (map #(if (instance? Rung %)
                                         (count (:elements %))
                                         0) rungs))
        total-branches (count (filter #(instance? Branch %) rungs))]
    {:total-rungs total-rungs
     :total-elements total-elements
     :total-branches total-branches}))

;; Validation functions
(defn valid-rung?
  "Check if a rung is valid"
  [rung]
  (and (instance? Rung rung)
       (vector? (:elements rung))
       (every? #(satisfies? elem/Renderable %) (:elements rung))))

(defn valid-ladder?
  "Check if a ladder is valid"
  [ladder]
  (and (instance? Ladder ladder)
       (vector? (:rungs ladder))
       (every? #(or (valid-rung? %)
                    (instance? Branch %)) (:rungs ladder))))
