(ns ladder-diagram-clj.core
  "Main entry point for the ladder diagram application"
  (:require [ladder-diagram-clj.ui :as ui]
            [ladder-diagram-clj.ladder :as ladder]
            [ladder-diagram-clj.elements :as elem])
  (:gen-class))

(defn create-simple-example
  "Create a simple two-rung ladder example"
  []
  (ladder/ladder
   (ladder/rung
    (elem/contact "In1")
    (elem/contact "In2")
    (elem/coil "Out1"))
   (ladder/rung
    (elem/contact "In1")
    (elem/negated-contact "In2")
    (elem/coil "Out2"))))

(defn create-branch-example
  "Create a ladder with branches"
  []
  (ladder/ladder
   (ladder/branch
    (ladder/rung
     (elem/contact "In1")
     (elem/contact "In2")
     (elem/coil "Out1"))
    (ladder/rung
     (elem/contact "In1")
     (elem/negated-contact "In2")
     (elem/coil "Out2")))))

(defn create-complex-example
  "Create a more complex ladder example"
  []
  (ladder/ladder
   (ladder/rung
    (ladder/branch
     (ladder/rung
      (elem/contact "In1")
      (elem/contact "In2"))
     (ladder/rung
      (elem/contact "In1")
      (elem/negated-contact "In2")))
    (elem/coil "Out3"))
   (ladder/rung
    (elem/rising-edge-contact "Start")
    (elem/set-coil "Motor"))
   (ladder/rung
    (elem/falling-edge-contact "Stop")
    (elem/reset-coil "Motor"))))

(defn create-three-wire-example
  "Create a three-wire control example"
  []
  (ladder/ladder
   (ladder/rung
    (elem/contact "Stop")
    (ladder/branch
     (ladder/rung
      (elem/contact "Start"))
     (ladder/rung
      (elem/contact "Motor")))
    (elem/coil "Motor"))))

(defn demo-ascii-rendering
  "Demonstrate ASCII rendering capabilities"
  []
  (println "=== Simple Ladder ===")
  (println (elem/render-ascii (create-simple-example)))
  (println)

  (println "=== Branch Example ===")
  (println (elem/render-ascii (create-branch-example)))
  (println)

  (println "=== Complex Example ===")
  (println (elem/render-ascii (create-complex-example)))
  (println)

  (println "=== Three-Wire Example ===")
  (println (elem/render-ascii (create-three-wire-example)))
  (println))



(defn -main
  "Main entry point - can run in GUI or CLI mode"
  [& args]
  (if (some #{"--cli" "-c"} args)
    ;; CLI mode - just show ASCII examples
    (do
      (println "Ladder Diagram Library - CLI Mode")
      (println "==================================")
      (demo-ascii-rendering)
      (System/exit 0))

    ;; GUI mode - start the JavaFX application
    (do
      (println "Starting Ladder Diagram Editor...")
      (ui/start-app!))))

;; Additional utility functions for REPL usage
(defn quick-ladder
  "Quick function to create and display a ladder from elements"
  [& elements]
  (let [ladder (ladder/ladder (apply ladder/rung elements))]
    (println (elem/render-ascii ladder))
    ladder))

(defn show-elements
  "Show all available element types"
  []
  (println "Available Elements:")
  (println "==================")
  (doseq [[name constructor] [["Contact" elem/contact]
                              ["Negated Contact" elem/negated-contact]
                              ["Rising Edge Contact" elem/rising-edge-contact]
                              ["Falling Edge Contact" elem/falling-edge-contact]
                              ["Edge Contact" elem/edge-contact]
                              ["Coil" elem/coil]
                              ["Negated Coil" elem/negated-coil]
                              ["Set Coil" elem/set-coil]
                              ["Reset Coil" elem/reset-coil]
                              ["Toggle Coil" elem/toggle-coil]]]
    (let [element (constructor "Example")]
      (printf "%-20s: %s\n" name (:ascii-art element))))
  (println))

(comment
  ;; REPL usage examples

  ;; Start the GUI
  (ui/start-app!)

  ;; Create a simple ladder
  (quick-ladder (elem/contact "Start") (elem/coil "Motor"))

  ;; Show available elements
  (show-elements)


  ;; Create and print a complex example
  (println (elem/render-ascii (create-complex-example)))

  ;; Create and print a three-wire example
  (println (elem/render-ascii (create-three-wire-example)))

  ;; Stop the GUI
  (ui/stop-app!)
  (demo-ascii-rendering))