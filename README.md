# Ladder Diagram Editor (Clojure)

A Clojure implementation of a ladder logic diagram editor with both ASCII rendering and graphical user interface using cljfx.

This is a port of the original Python `py-ladder-diagram` library, enhanced with a modern JavaFX-based GUI.

## Features

- **ASCII Art Rendering**: Generate clean ASCII representations of ladder diagrams
- **Graphical User Interface**: Modern JavaFX-based editor using cljfx
- **Multiple Element Types**: Contacts, coils, and various specialized elements
- **Branch Support**: Complex branching logic with proper ASCII rendering
- **Interactive Editing**: Add, remove, and modify ladder elements
- **Dual View Modes**: Switch between ASCII text and graphical representations
- **REPL-Friendly**: Easy to use from the Clojure REPL for programmatic creation

## Installation

### Prerequisites

- Java 11 or later
- Clojure CLI tools

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd ladder-diagram-clj

# Run the GUI application
clj -M:run

# Or run in CLI mode (ASCII examples only)
clj -M:run --cli
```

### Running Tests

```bash
clj -M:test
```

## Usage

### GUI Mode

Launch the application with:

```bash
clj -M:run
```

The GUI provides:
- **Menu Bar**: File operations and view controls
- **Tool Bar**: Quick access to element creation
- **Tree View**: Hierarchical view of ladder structure
- **Main Canvas**: ASCII or graphical diagram display
- **Properties Panel**: Edit selected element properties
- **Status Bar**: Ladder statistics and view information

### Programmatic Usage (REPL)

```clojure
(require '[ladder-diagram.core :as core]
         '[ladder-diagram.elements :as elem]
         '[ladder-diagram.ladder :as ladder])

;; Create a simple ladder
(def simple-ladder
  (ladder/ladder
    (ladder/rung 
      (elem/contact "In1")
      (elem/contact "In2")
      (elem/coil "Out1"))))

;; Render as ASCII
(println (elem/render-ascii simple-ladder))
;; Output:
;; █
;; █     In1    In2   Out1
;; █─────┤ ├────┤ ├────( )
;; █

;; Create a ladder with branches
(def branch-ladder
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

(println (elem/render-ascii branch-ladder))
;; Output:
;; █
;; █       In1    In2   Out1
;; █───┬───┤ ├────┤ ├────( )
;; █   │                    
;; █   │   In1    In2   Out2
;; █   └───┤ ├────┤/├────( )
;; █                        
;; █

;; Quick ladder creation helper
(core/quick-ladder (elem/contact "Start") (elem/coil "Motor"))

;; Show all available elements
(core/show-elements)
```

### Available Elements

The library supports all standard ladder logic elements:

**Contacts:**
- `contact` - Normal contact: `─┤ ├─`
- `negated-contact` - Negated contact: `─┤/├─`
- `rising-edge-contact` - Rising edge: `─┤↑├─`
- `falling-edge-contact` - Falling edge: `─┤↓├─`
- `edge-contact` - Either edge: `─┤↕├─`

**Coils:**
- `coil` - Normal coil: `─( )`
- `negated-coil` - Negated coil: `─(/)`
- `set-coil` - Set coil: `─(S)`
- `reset-coil` - Reset coil: `─(R)`
- `toggle-coil` - Toggle coil: `─(T)`

### Complex Example

```clojure
(def complex-ladder
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

(println (elem/render-ascii complex-ladder))
;; Output:
;; █
;; █         In1    In2      Out3
;; █─────┬───┤ ├────┤ ├──┬────( )
;; █     │               │       
;; █     │   In1    In2  │       
;; █     └───┤ ├────┤/├──┘       
;; █                             
;; █
;; █     Start         Motor
;; █─────┤↑├──────────(S)
;; █
;; █     Stop          Motor
;; █─────┤↓├──────────(R)
;; █
```

## Architecture

The Clojure implementation uses a functional approach with the following key components:

### Data Structures

- **Records**: `LadderElement`, `Rung`, `Branch`, `Ladder` for structured data
- **Protocols**: `Renderable` and `Measurable` for polymorphic behavior
- **Immutable**: All operations return new data structures

### Namespaces

- `ladder-diagram.elements` - Element definitions and creation functions
- `ladder-diagram.ladder` - Rung, branch, and ladder logic
- `ladder-diagram.ui` - cljfx-based user interface
- `ladder-diagram.core` - Main entry point and utilities

### Key Differences from Python Version

1. **Functional Design**: No mutable state, all operations pure functions
2. **Protocol-Based**: Uses Clojure protocols instead of class inheritance
3. **GUI Added**: Modern JavaFX interface using cljfx
4. **REPL-Friendly**: Easy interactive development and testing
5. **Better Separation**: Clear separation between data, logic, and presentation

## GUI Features

### Main Interface

- **Dual View Modes**: Toggle between ASCII text and graphical rendering
- **Interactive Tree**: Navigate ladder structure hierarchically
- **Property Editing**: Modify element names and properties
- **Zoom Controls**: Zoom in/out for better visibility
- **Status Information**: Real-time ladder statistics

### Keyboard Shortcuts

- `Ctrl+N` - New ladder
- `Ctrl+T` - Toggle view mode
- `Ctrl++` - Zoom in
- `Ctrl+-` - Zoom out

### Menu Options

**File Menu:**
- New - Create empty ladder
- Exit - Close application

**Examples Menu:**
- Simple Ladder - Two-rung example
- Branch Example - Demonstrates branching

**View Menu:**
- Toggle ASCII/Graphical view
- Zoom controls

## Development

### Project Structure

```
ladder-diagram-clj/
├── deps.edn                 # Dependencies and build config
├── README.md               # This file
├── src/
│   └── ladder_diagram/
│       ├── core.clj        # Main entry point
│       ├── elements.clj    # Element definitions
│       ├── ladder.clj      # Ladder logic
│       └── ui.clj          # cljfx interface
└── test/
    └── ladder_diagram/
        ├── elements_test.clj
        └── ladder_test.clj
```

### Running from Source

```bash
# Start REPL
clj

# In REPL:
(require '[ladder-diagram.core :as core])
(core/-main)  ; Start GUI
```

### Building Uberjar

```bash
clj -M:uberjar
java -jar target/ladder-diagram.jar
```

## Testing

The test suite verifies compatibility with the original Python implementation:

```bash
# Run all tests
clj -M:test

# Run specific test namespace
clj -M:test ladder-diagram.elements-test
```

Tests cover:
- Element creation and properties
- ASCII rendering accuracy
- Ladder construction and manipulation
- Branch rendering logic
- Compatibility with Python examples

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

MIT License - same as the original Python version.

## Acknowledgments

- Original Python implementation: [py-ladder-diagram](https://github.com/engineerjoe440/pyld)
- cljfx library for JavaFX integration
- Clojure community for excellent functional programming tools