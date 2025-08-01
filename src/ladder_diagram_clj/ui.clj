(ns ladder-diagram-clj.ui
  "cljfx-based user interface for ladder diagram editor"
  (:require [cljfx.api :as fx]
            [clojure.java.io :as io]
            [ladder-diagram-clj.elements :as elem]
            [ladder-diagram-clj.ladder :as ladder])
  (:import [javafx.scene.input KeyCode KeyEvent]
           [javafx.scene.control Alert Alert$AlertType ButtonType]
           [javafx.stage FileChooser FileChooser$ExtensionFilter]))

;; Application state
(def *state
  (atom {:ladder (ladder/ladder
                  (ladder/rung
                   (elem/contact "In1")
                   (elem/contact "In2")
                   (elem/coil "Out1")))
         :selected-element nil
         :view-mode :ascii
         :zoom 1.0}))

;; Event handling
(defmulti handle-event :event/type)

(defmethod handle-event :add-contact [_]
  (swap! *state update-in [:ladder :rungs]
         conj (ladder/rung (elem/contact "NewContact"))))

(defmethod handle-event :add-negated-contact [_]
  (swap! *state update-in [:ladder :rungs]
         conj (ladder/rung (elem/negated-contact "NewNegContact"))))

(defmethod handle-event :add-coil [_]
  (swap! *state update-in [:ladder :rungs]
         conj (ladder/rung (elem/coil "NewCoil"))))

(defmethod handle-event :add-negated-coil [_]
  (swap! *state update-in [:ladder :rungs]
         conj (ladder/rung (elem/negated-coil "NewNegCoil"))))

(defmethod handle-event :toggle-view [_]
  (swap! *state update :view-mode
         #(if (= % :ascii) :graphical :ascii)))

(defmethod handle-event :zoom-in [_]
  (swap! *state update :zoom #(min 3.0 (* % 1.2))))

(defmethod handle-event :zoom-out [_]
  (swap! *state update :zoom #(max 0.5 (/ % 1.2))))

(defmethod handle-event :new-ladder [_]
  (swap! *state assoc :ladder (ladder/ladder)))

(defmethod handle-event :example-ladder [_]
  (swap! *state assoc :ladder
         (ladder/ladder
          (ladder/rung
           (elem/contact "In1")
           (elem/contact "In2")
           (elem/coil "Out1"))
          (ladder/rung
           (elem/contact "In1")
           (elem/negated-contact "In2")
           (elem/coil "Out2")))))

(defmethod handle-event :branch-example [_]
  (swap! *state assoc :ladder
         (ladder/ladder
          (ladder/branch
           (ladder/rung
            (elem/contact "In1")
            (elem/contact "In2")
            (elem/coil "Out1"))
           (ladder/rung
            (elem/contact "In1")
            (elem/negated-contact "In2")
            (elem/coil "Out2"))))))

(defmethod handle-event :add-rung [_]
  (swap! *state update-in [:ladder :rungs]
         conj (ladder/rung (elem/contact "NewInput") (elem/coil "NewOutput"))))

(defmethod handle-event :add-branch [_]
  (swap! *state update-in [:ladder :rungs]
         conj (ladder/branch
               (ladder/rung (elem/contact "BranchInput1") (elem/coil "BranchOutput1"))
               (ladder/rung (elem/contact "BranchInput2") (elem/coil "BranchOutput2")))))

;; UI Components

(defn menu-bar [{:keys []}]
  {:fx/type :menu-bar
   :menus [{:fx/type :menu
            :text "File"
            :items [{:fx/type :menu-item
                     :text "New"
                     :on-action {:event/type :new-ladder}}
                    {:fx/type :separator-menu-item}
                    {:fx/type :menu-item
                     :text "Exit"
                     :on-action (fn [_] (System/exit 0))}]}
           {:fx/type :menu
            :text "Examples"
            :items [{:fx/type :menu-item
                     :text "Simple Ladder"
                     :on-action {:event/type :example-ladder}}
                    {:fx/type :menu-item
                     :text "Branch Example"
                     :on-action {:event/type :branch-example}}]}
           {:fx/type :menu
            :text "View"
            :items [{:fx/type :menu-item
                     :text "Toggle ASCII/Graphical"
                     :on-action {:event/type :toggle-view}}
                    {:fx/type :separator-menu-item}
                    {:fx/type :menu-item
                     :text "Zoom In"
                     :on-action {:event/type :zoom-in}}
                    {:fx/type :menu-item
                     :text "Zoom Out"
                     :on-action {:event/type :zoom-out}}]}]})

(defn tool-bar [{:keys []}]
  {:fx/type :tool-bar
   :items [{:fx/type :button
            :text "Rung"
            :tooltip {:fx/type :tooltip :text "Add New Rung with Contact and Coil"}
            :on-action {:event/type :add-rung}}
           {:fx/type :separator}
           {:fx/type :button
            :text "Branch"
            :tooltip {:fx/type :tooltip :text "Add New Branch"}
            :on-action {:event/type :add-branch}}
           {:fx/type :separator}
           {:fx/type :button
            :text "Contact"
            :tooltip {:fx/type :tooltip :text "Add Contact"}
            :on-action {:event/type :add-contact}}
           {:fx/type :separator}
           {:fx/type :button
            :text "NC Contact"
            :tooltip {:fx/type :tooltip :text "Add Negated Contact"}
            :on-action {:event/type :add-negated-contact}}
           {:fx/type :separator}
           {:fx/type :button
            :text "Coil"
            :tooltip {:fx/type :tooltip :text "Add Coil"}
            :on-action {:event/type :add-coil}}
           {:fx/type :separator}
           {:fx/type :button
            :text "NC Coil"
            :tooltip {:fx/type :tooltip :text "Add Negated Coil"}
            :on-action {:event/type :add-negated-coil}}
           {:fx/type :separator}
           {:fx/type :button
            :text "Toggle View"
            :tooltip {:fx/type :tooltip :text "Switch between ASCII and Graphical view"}
            :on-action {:event/type :toggle-view}}
           {:fx/type :separator}
           {:fx/type :button
            :text "Exit"
            :tooltip {:fx/type :tooltip :text "Exit Application"}
            :on-action (fn [_] (System/exit 0))}]})

(defn ascii-display [{:keys [ladder zoom]}]
  {:fx/type :scroll-pane
   :fit-to-width true
   :fit-to-height true
   :content {:fx/type :text-area
             :text (elem/render-ascii ladder)
             :editable false
             :style {:-fx-font-family "monospace"
                     :-fx-font-size (* 12 zoom)}
             :wrap-text false}})

(defn graphical-display [{:keys [ladder zoom]}]
  {:fx/type :scroll-pane
   :fit-to-width true
   :fit-to-height true
   :content {:fx/type :canvas
             :width (* 800 zoom)
             :height (* 600 zoom)
             :on-mouse-clicked (fn [event]
                                 (println "Canvas clicked at"
                                          (.getX event) (.getY event)))
             :draw (fn [canvas]
                     (let [ctx (.getGraphicsContext2D canvas)
                           width (* 800 zoom)
                           height (* 600 zoom)]
                       ;; Clear canvas with white background
                       (.setFill ctx javafx.scene.paint.Color/WHITE)
                       (.fillRect ctx 0 0 width height)

                       ;; Set drawing properties
                       (.setStroke ctx javafx.scene.paint.Color/BLACK)
                       (.setLineWidth ctx (* 2 zoom))
                       (.setFont ctx (javafx.scene.text.Font/font (* 12 zoom)))
                       (.setFill ctx javafx.scene.paint.Color/BLACK)

                       ;; Draw power rails (vertical lines)
                       (let [rail-left (* 20 zoom)
                             rail-right (* 750 zoom)
                             rail-top (* 50 zoom)
                             rail-bottom (* 550 zoom)]
                         (.strokeLine ctx rail-left rail-top rail-left rail-bottom)
                         (.strokeLine ctx rail-right rail-top rail-right rail-bottom))

                       ;; Draw ladder rungs graphically
                       (let [rungs (:rungs ladder)
                             rung-height (* 80 zoom)
                             start-y (* 100 zoom)
                             x-start (* 20 zoom)
                             x-end (* 750 zoom)]
                         (doseq [[idx rung] (map-indexed vector rungs)]
                           (let [y (+ start-y (* idx rung-height))]
                             ;; Draw horizontal rung line
                             (.strokeLine ctx x-start y x-end y)

                             ;; Draw elements if it's a rung (not a branch)
                             (when (instance? ladder_diagram_clj.ladder.Rung rung)
                               (let [elements (:elements rung)
                                     available-width (- x-end x-start (* 40 zoom))
                                     element-width (if (pos? (count elements))
                                                     (/ available-width (count elements))
                                                     0)]
                                 (doseq [[elem-idx element] (map-indexed vector elements)]
                                   (let [elem-x (+ x-start (* 40 zoom) (* elem-idx element-width))
                                         element-type (elem/element-type-name element)
                                         element-name (:name element "")]
                                     ;; Draw element symbol (rectangle)
                                     (let [rect-size (* 30 zoom)
                                           rect-x (- elem-x (/ rect-size 2))
                                           rect-y (- y (/ rect-size 2))]
                                       (.strokeRect ctx rect-x rect-y rect-size rect-size))

                                     ;; Draw element type below the symbol
                                     (.fillText ctx element-type (- elem-x (* 15 zoom)) (+ y (* 25 zoom)))

                                     ;; Draw element name above the symbol
                                     (.fillText ctx element-name (- elem-x (* 15 zoom)) (- y (* 20 zoom))))))))))))}})

(defn ladder-tree [{:keys [ladder]}]
  {:fx/type :tree-view
   :root {:fx/type :tree-item
          :value "Ladder Diagram"
          :expanded true
          :children (map-indexed
                     (fn [idx rung]
                       {:fx/type :tree-item
                        :value (str "Rung " (inc idx))
                        :expanded true
                        :children (when (instance? ladder_diagram_clj.ladder.Rung rung)
                                    (map-indexed
                                     (fn [elem-idx element]
                                       {:fx/type :tree-item
                                        :value (str (elem/element-type-name element)
                                                    " - " (:name element ""))})
                                     (:elements rung)))})
                     (:rungs ladder))}})

(defn properties-panel [{:keys [selected-element]}]
  {:fx/type :v-box
   :spacing 10
   :padding 10
   :children [{:fx/type :label
               :text "Properties"
               :style {:-fx-font-weight "bold"}}
              (if selected-element
                {:fx/type :v-box
                 :spacing 5
                 :children [{:fx/type :label :text "Name:"}
                            {:fx/type :text-field
                             :text (:name selected-element "")
                             :prompt-text "Enter element name"}
                            {:fx/type :label :text "Type:"}
                            {:fx/type :label
                             :text (elem/element-type-name selected-element)}]}
                {:fx/type :label
                 :text "No element selected"
                 :style {:-fx-text-fill "gray"}})]})

(defn status-bar [{:keys [ladder view-mode zoom]}]
  (let [stats (ladder/ladder-statistics ladder)]
    {:fx/type :h-box
     :spacing 20
     :padding 5
     :children [{:fx/type :label
                 :text (format "Rungs: %d | Elements: %d | Branches: %d"
                               (:total-rungs stats)
                               (:total-elements stats)
                               (:total-branches stats))}
                {:fx/type :region
                 :h-box/hgrow :always}
                {:fx/type :label
                 :text (format "View: %s | Zoom: %.0f%%"
                               (name view-mode)
                               (* zoom 100))}]}))

(defn main-content [{:keys [ladder view-mode zoom selected-element]}]
  {:fx/type :split-pane
   :orientation :horizontal
   :divider-positions [0.5]
   :items [{:fx/type :split-pane
            :orientation :vertical
            :items [(ladder-tree {:ladder ladder})
                    (properties-panel {:selected-element selected-element})]}
           {:fx/type :v-box
            :children [(case view-mode
                         :ascii (ascii-display {:ladder ladder :zoom zoom})
                         :graphical (graphical-display {:ladder ladder :zoom zoom}))]}]})

(defn root-view [{:keys [ladder view-mode zoom selected-element]}]
  {:fx/type :stage
   :showing true
   :title "Ladder Diagram Editor"
   :width 1200
   :height 800
   :on-close-request (fn [_] (System/exit 0))
   :scene {:fx/type :scene
           :root {:fx/type :border-pane
                  :top (tool-bar {})
                  :center (main-content {:ladder ladder
                                         :view-mode view-mode
                                         :zoom zoom
                                         :selected-element selected-element})
                  :bottom (status-bar {:ladder ladder
                                       :view-mode view-mode
                                       :zoom zoom})}}})

;; Renderer and app setup
(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc (fn [state]
                                   (root-view state)))
   :opts {:fx.opt/map-event-handler handle-event}))

(defn start-app! []
  (fx/mount-renderer *state renderer))

(defn stop-app! []
  (fx/unmount-renderer *state renderer))