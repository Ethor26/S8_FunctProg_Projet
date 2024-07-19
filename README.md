# Scala Project: graphes algorithmes
For course "Functionnal programming" at EFREI Paris (S8)

### Contributors
Maxime GOUJON, Ethan SUISSA, Timothée FOUGERON, Arthur BUISSON

## Description
The project is an ZIO 2 application : a graph data structure library with various operations.

## Structure
The project is divided in 2 parts:
- The core library, in the `core` module
- The application, in the `app` module

The core library has the files:
- `Graph_manager.scala` : the graph data structure

The application has the files:
- `App.scala` : the main application

## Installation
To run the application, you need to have `sbt` installed on your machine.
1) Clone the repository
2) Go to the root of the project
3) Run `sbt run` to run the application
4) Run `sbt test` to run the tests
5) Go to the `app` module and launch `Main.scala` to run the application

## Design Decisions

We have chosen to create several classes to represent our graphs.

### The Edge Class

**Why**: To represent the edges between the vertices of the graph

**How**:
- **Attributes**:
    - `from`: `Int` representing the vertex where the edge comes from.
    - `to`: `Int` representing the vertex where the edge goes
    - `weight`: `Double` representing the weight of the edge, default value is `Double.NaN`
- **Methods**:
    - No methods
- **Note**:
  - You certainly noticed vertices are represented by Int, so there are no classes for them here. By using a class for edges, we did not need a class for vertices. But, if respecting the SOLID principles was a concern, it would certainly better to have this Vertices class to give him the responsibilities over itself such as the neighbours method which you will see below in the Methods section of the next Class.

### The Graph Class (Abstract)

**Why**: To implement the common behaviors of the concrete graph classes. We have chosen to make it abstract because the main concern of the project was about directed, undirected, and weighted graphs, so instanciate a Graph object would not be usefull.  

**How**:
- **Attributes**:
    - `vertices`: `Set[Int]` to store the vertices of the graph.
    - `edges`: `Set[Edge]` to store the edges of the graph
    - `weighted`: `Boolean` to indicate if the graph uses weights or not
- **Methods**:
    - `addEdge`: `Graph[V]`: Add an edge to the graph
    - `addEdges`: `Graph[V]`: Add multiple edges to the graph
    - `neighbors (abstract)`: `Set[V]`: Get the neighbors of a vertex
    - `copyWith (abstract)`: `Set[V]`: Get the deep copy of the object
    - `removeEdge`: `Graph[V]`: Remove an edge from the graph
    - `removeEdges`: `Graph[V]`: Remove multiple edges from the graph
    - `dfs`: `List[V]`: Perform a Depth-First Search on the graph
    - `bfs`: `List[V]`: Perform a Breadth-First Search on the graph
    - `floydWarshall`: `Either[String, Map[(V, V), Double]]`: Execute the Floyd-Warshall algorithm on the graph
    - `dijkstra`: `Either[String, Map[V, Double]]`: Execute Dijkstra's algorithm on the graph
    - `validateEdges`: `Set[Edge[V]]`: Check if an edge is valid or not

### The UndirectedGraph Class (Extends Graph)

**Why**: To represent an undirected graph

**How**:
- **Attributes**:
    - Same as the `Graph`, but overrides edges to ensure they are valid for an undirected graph
- **Methods**:
    - Implements `neighbors` and `copyWith` from `Graph`

### The DirectedGraph Class (Extends Graph)

**Why**: To represent a directed graph

**How**:
- **Attributes**:
    - Same as the `Graph`, but ensures edges are valid for a directed graph
- **Methods**:
    - Implements `neighbors` and `copyWith` from `Graph`
    - `topologicalSort`: `Either[String, List[V]]`: Compute a topological sorting of the graph
    - `detectCycle`: `Either[String, List[V]]`: Detect if there are cycles in the graph


## TESTING

### How to Run the Tests
1. Go to the root of the project.
2. open a Terminal
3. Set "sbt test"

### Tests Overview

#### UndirectedGraph

- **Adding and Retrieving Edges**
  - **Goal**: Check that edges can be added correctly and that a node's neighbors are updated as expected.
  - **Detail**: Create a graph with nodes `{1, 2, 3}` and edges `{(1, 2), (2, 3)}`. The test checks that the neighbor of node `1` is `2`. Then, a new edge `(1, 3)` is added, and the test checks that `3` is now a neighbor of `1`.

- **DFS (Depth-First Search)**
  - **Goal**: Check that the DFS algorithm correctly traverses the graph.
  - **Detail**: Using the same graph as before, the test verifies that the DFS from node `1` returns the list of nodes `[1, 2, 3]`.

- **BFS (Breadth-First Search)**
  - **Goal**: Check that the BFS algorithm correctly traverses the graph.
  - **Detail**: Using the same graph as before, the test verifies that the BFS from node `1` returns the list of nodes `[1, 2, 3]`.

- **Dijkstra (on a weighted graph)**
  - **Goal**: Check that Dijkstra's algorithm works correctly for weighted graphs.
  - **Detail**: The graph is weighted, with the edge `(2, 3)` having a weight of `2.0`. The test verifies that Dijkstra from node `1` correctly calculates the shortest distances to all nodes.

- **Floyd-Warshall (on a weighted graph)**
  - **Goal**: Check that the Floyd-Warshall algorithm correctly calculates the distances between all nodes in a weighted graph.
  - **Detail**: Using the same weighted graph, the test checks that Floyd-Warshall correctly calculates distances between all nodes, including infinite distances where there is no direct path.

#### DirectedGraph

- **Topological Sorting**
  - **Goal**: Check that topological sorting works correctly for acyclic directed graphs.
  - **Detail**: The directed graph has edges `{(1, 2), (2, 3)}`. The test checks that the topological sort returns the list `[1, 2, 3]`, which is a valid order for the nodes.

- **Cycle Detection**
  - **Goal**: Check that cycle detection works correctly for directed graphs with cycles.
  - **Detail**: The graph contains a cycle with the edges `{(1, 2), (2, 3), (3, 1)}`. The test checks that the cycle is detected and that the message correctly indicates the cycle found.

## Usage Examples
In the application, you can do sereval actions such as:

- ### create a new graph, then:
  - Add an edge
  - Remove an edge
  - Display the graph
  - Save graph to JSON
  - Load Graph from JSON
  - Go To Algorithm Menu
- ### Exit the graph
