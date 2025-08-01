(ns ladder-diagram-clj.elements
  "Ladder diagram elements - contacts, coils, etc.")

;; Protocols for polymorphic behavior
(defprotocol Renderable
  "Protocol for rendering ladder elements"
  (render-ascii [element] "Render element as ASCII art")
  (render-name [element] "Render element name")
  (element-width [element] "Calculate element width"))

(defprotocol Measurable
  "Protocol for measuring elements"
  (element-depth [element] "Calculate element depth in rows"))

;; Justification options
(def justification
  {:left "<"
   :right ">"
   :center "^"})

;; Base element record
(defrecord LadderElement [name ascii-art justify]
  Renderable
  (render-ascii [element]
    (:ascii-art element))

  (render-name [element]
    (:name element ""))

  (element-width [element]
    (max (count (:ascii-art element))
         (count (:name element ""))))

  Measurable
  (element-depth [element] 2))

;; Helper function to create justified string
(defn justify-string [s width justify-type]
  (let [pad-total (- width (count s))
        pad-char "─"]
    (case justify-type
      :left (str s (apply str (repeat pad-total pad-char)))
      :right (str (apply str (repeat pad-total pad-char)) s)
      :center (let [pad-left (int (/ pad-total 2))
                    pad-right (- pad-total pad-left)]
                (str (apply str (repeat pad-left pad-char))
                     s
                     (apply str (repeat pad-right pad-char)))))))

;; Element creation functions
(defn contact
  "Create a contact element"
  ([name] (->LadderElement name "─┤ ├─" :center))
  ([] (contact "")))

(defn negated-contact
  "Create a negated contact element"
  ([name] (->LadderElement name "─┤/├─" :center))
  ([] (negated-contact "")))

(defn rising-edge-contact
  "Create a rising edge contact element"
  ([name] (->LadderElement name "─┤↑├─" :center))
  ([] (rising-edge-contact "")))

(defn falling-edge-contact
  "Create a falling edge contact element"
  ([name] (->LadderElement name "─┤↓├─" :center))
  ([] (falling-edge-contact "")))

(defn edge-contact
  "Create an edge contact element"
  ([name] (->LadderElement name "─┤↕├─" :center))
  ([] (edge-contact "")))

(defn coil
  "Create a coil element"
  ([name] (->LadderElement name "─( )" :right))
  ([] (coil "")))

(defn negated-coil
  "Create a negated coil element"
  ([name] (->LadderElement name "─(/)" :right))
  ([] (negated-coil "")))

(defn set-coil
  "Create a set coil element"
  ([name] (->LadderElement name "─(S)" :right))
  ([] (set-coil "")))

(defn reset-coil
  "Create a reset coil element"
  ([name] (->LadderElement name "─(R)" :right))
  ([] (reset-coil "")))

(defn toggle-coil
  "Create a toggle coil element"
  ([name] (->LadderElement name "─(T)" :right))
  ([] (toggle-coil "")))

;; Element rendering functions
(defn render-element-ascii
  "Render a single element as ASCII with proper justification"
  [element]
  (let [width (element-width element)
        name-line (render-name element)
        ascii-line (render-ascii element)
        justify-type (:justify element :center)]
    [(cond
       (empty? name-line) (apply str (repeat width " "))
       :else (format (str "%-" width "s") name-line))
     (justify-string ascii-line width justify-type)]))

;; Element type predicates
(defn contact? [element]
  (and (instance? LadderElement element)
       (= (:ascii-art element) "─┤ ├─")))

(defn coil? [element]
  (and (instance? LadderElement element)
       (.contains (:ascii-art element) "(")))

(defn negated? [element]
  (and (instance? LadderElement element)
       (.contains (:ascii-art element) "/")))

;; Utility functions
(defn update-element-name
  "Update the name of an element"
  [element new-name]
  (assoc element :name new-name))

(defn element-type-name
  "Get the type name of an element"
  [element]
  (cond
    (= (:ascii-art element) "─┤ ├─") "Contact"
    (= (:ascii-art element) "─┤/├─") "Negated Contact"
    (= (:ascii-art element) "─┤↑├─") "Rising Edge Contact"
    (= (:ascii-art element) "─┤↓├─") "Falling Edge Contact"
    (= (:ascii-art element) "─┤↕├─") "Edge Contact"
    (= (:ascii-art element) "─( )") "Coil"
    (= (:ascii-art element) "─(/)") "Negated Coil"
    (= (:ascii-art element) "─(S)") "Set Coil"
    (= (:ascii-art element) "─(R)") "Reset Coil"
    (= (:ascii-art element) "─(T)") "Toggle Coil"
    :else "Unknown"))