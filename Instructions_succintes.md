### Functional Programming in Scala 3 - Functional Graphs Project

**Project Deadline: Friday, 19 July**

### Graph Data Structure
1. **Testing**
    - Implement unit tests using ScalaTest FlatSpec to ensure the correctness of all graph structures and operations.

2. **Data Structure Design**
    - Design an interface for graphs, parameterized by type.
    - Implement methods to get all vertices, edges, neighbors of a vertex, add and remove edges.

3. **Implementations**
    - Implement Directed Graph, Undirected Graph, and Weighted Graph using the designed interface.

4. **JSON Encoding/Decoding**
    - Implement JSON encoding and decoding for graphs using zio-json.

5. **GraphViz Representation**
    - Implement GraphViz representation generation using the DOT language.

### Graph Operations
1. **Depth First Search (DFS)**
    - Implement and test DFS for graph traversal.

2. **Breadth First Search (BFS)**
    - Implement and test BFS for graph traversal.

3. **Topological Sorting**
    - Implement and test topological sorting using DFS.

4. **Cycle Detection**
    - Implement and test cycle detection using DFS.

5. **Floyd's Algorithm**
    - Implement and test Floyd's algorithm for shortest paths in weighted graphs.

6. **Dijkstra's Algorithm**
    - Implement and test Dijkstra's algorithm for shortest paths from a single source vertex.

### ZIO 2 Application Integration
1. **Application Design**
    - Integrate the graph library and operations into a ZIO 2 application.
    - Optionally, build a terminal-based interactive application or an API-based application.

2. **State Management**
    - Choose and implement a state management method for the application.
    - Document state management decisions and options in the README.

### Optional Feature
1. **Logging**
    - Add functional logging support to the implemented operations.
    - Discuss the logging implementation in the README.

### Project Organization
1. **Use sbt Subprojects**
    - Separate the core library from the ZIO application using sbt subprojects.

2. **Scala Worksheets**
    - Use Scala worksheets to experiment and validate implementations.

3. **Version Control**
    - Use Git for version control with regular commits and meaningful messages.

### Documentation
1. **README Requirements**
    - Include a project overview, instructions for building, testing, and running the application.
    - Explain design decisions, state management, logging (if implemented), usage examples, and test coverage.

### Grading Criteria
1. **Implementation Behavior**
    - Ensure correct functionality of all implemented features.

2. **Functional Programming Principles**
    - Adhere to functional programming paradigms.

3. **Test Coverage**
    - Provide comprehensive unit tests covering all features.

4. **Project Organization**
    - Maintain a clear and logical project structure.

5. **README Quality**
    - Ensure the README is comprehensive and clear.