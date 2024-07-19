import scalaproject.core.{DirectedGraph, Edge, Graph, UndirectedGraph}
import zio.json._

sealed trait GraphType
case object Directed extends GraphType
case object Undirected extends GraphType

case class AppState(
  graphType: Option[GraphType] = None,
  graph: Option[Graph[String]] = None,
  errorMessage: Option[String] = None
)

object AppState {
  implicit val graphTypeEncoder: JsonEncoder[GraphType] = DeriveJsonEncoder.gen[GraphType]
  implicit val graphTypeDecoder: JsonDecoder[GraphType] = DeriveJsonDecoder.gen[GraphType]
  implicit val appStateEncoder: JsonEncoder[AppState] = DeriveJsonEncoder.gen[AppState]
  implicit val appStateDecoder: JsonDecoder[AppState] = DeriveJsonDecoder.gen[AppState]

  def initializeGraph(state: AppState, graphType: GraphType, weighted: Boolean): AppState = {
    val graph = graphType match {
      case Directed   => DirectedGraph[String](Set.empty, Set.empty, weighted)
      case Undirected => UndirectedGraph[String](Set.empty, Set.empty, weighted)
    }
    state.copy(graphType = Some(graphType), graph = Some(graph))
  }

  def addVertex(state: AppState, vertex: String): AppState = {
    state.graph match {
      case Some(graph) =>
        val newGraph = graph match {
          case g: DirectedGraph[String]   => g.copy(initialVertices = g.vertices + vertex)
          case g: UndirectedGraph[String] => g.copy(initialVertices = g.vertices + vertex)
        }
        state.copy(graph = Some(newGraph))
      case None => state.copy(errorMessage = Some("Graph is not initialized."))
    }
  }

  def removeVertex(state: AppState, vertex: String): AppState = {
    state.graph match {
      case Some(graph) =>
        val newGraph = graph match {
          case g: DirectedGraph[String]   => g.copy(initialVertices = g.vertices - vertex)
          case g: UndirectedGraph[String] => g.copy(initialVertices = g.vertices - vertex)
        }
        state.copy(graph = Some(newGraph))
      case None => state.copy(errorMessage = Some("Graph is not initialized."))
    }
  }

  def addEdge(state: AppState, from: String, to: String, weight: Double = 1.0): AppState = {
    state.graph match {
      case Some(graph) =>
        val edge = Edge(from, to, weight)
        val newGraph = graph.addEdge(edge)
        state.copy(graph = Some(newGraph))
      case None => state.copy(errorMessage = Some("Graph is not initialized."))
    }
  }

  def removeEdge(state: AppState, from: String, to: String): AppState = {
    state.graph match {
      case Some(graph) =>
        val edge = Edge(from, to)
        val newGraph = graph.removeEdge(edge)
        state.copy(graph = Some(newGraph))
      case None => state.copy(errorMessage = Some("Graph is not initialized."))
    }
  }

  def dfs(state: AppState, start: String): Either[String, List[String]] = {
    state.graph match {
      case Some(graph) => Right(graph.dfs(start))
      case None => Left("Graph is not initialized.")
    }
  }

  def bfs(state: AppState, start: String): Either[String, List[String]] = {
    state.graph match {
      case Some(graph) => Right(graph.bfs(start))
      case None => Left("Graph is not initialized.")
    }
  }

  def dijkstra(state: AppState, start: String): Either[String, Map[String, Double]] = {
    state.graph match {
      case Some(graph) => graph.dijkstra(start)
      case None => Left("Graph is not initialized.")
    }
  }

  def floydWarshall(state: AppState): Either[String, Map[(String, String), Double]] = {
    state.graph match {
      case Some(graph) => graph.floydWarshall()
      case None => Left("Graph is not initialized.")
    }
  }

  def topologicalSort(state: AppState): Either[String, List[String]] = {
    state.graph match {
      case Some(graph: DirectedGraph[String]) => graph.topologicalSort
      case Some(_) => Left("Topological sort is only applicable to directed graphs.")
      case None => Left("Graph is not initialized.")
    }
  }

  def detectCycle(state: AppState): Either[String, List[String]] = {
    state.graph match {
      case Some(graph: DirectedGraph[String]) => graph.detectCycle
      case Some(_) => Left("Cycle detection is only applicable to directed graphs.")
      case None => Left("Graph is not initialized.")
    }
  }
}

object Main extends App {
  var state = AppState()

  def displayState(): Unit = {
    println(state.toJsonPretty)
  }

  def runApp(): Unit = {
    var continue = true
    while (continue) {
      println("\nMenu:")
      println("1. Initialize Graph")
      println("2. Add Vertex")
      println("3. Remove Vertex")
      println("4. Add Edge")
      println("5. Remove Edge")
      println("6. DFS Traversal")
      println("7. BFS Traversal")
      println("8. Dijkstra's Algorithm")
      println("9. Floyd-Warshall Algorithm")
      println("10. Topological Sort")
      println("11. Detect Cycle")
      println("12. Display State")
      println("13. Exit")

      print("\nEnter your choice: ")
      val choice = scala.io.StdIn.readInt()

      choice match {
        case 1 =>
          print("Enter Graph Type (1 for Directed, 2 for Undirected): ")
          val gType = scala.io.StdIn.readInt() match {
            case 1 => Directed
            case 2 => Undirected
          }
          print("Is the graph weighted? (yes/no): ")
          val weighted = scala.io.StdIn.readLine().trim.toLowerCase == "yes"
          state = AppState.initializeGraph(state, gType, weighted)

        case 2 =>
          print("Enter Vertex: ")
          val vertex = scala.io.StdIn.readLine()
          state = AppState.addVertex(state, vertex)

        case 3 =>
          print("Enter Vertex: ")
          val vertex = scala.io.StdIn.readLine()
          state = AppState.removeVertex(state, vertex)

        case 4 =>
          print("Enter From Vertex: ")
          val from = scala.io.StdIn.readLine()
          print("Enter To Vertex: ")
          val to = scala.io.StdIn.readLine()
          if (state.graph.exists(_.weighted)) {
            print("Enter Weight: ")
            val weight = scala.io.StdIn.readDouble()
            state = AppState.addEdge(state, from, to, weight)
          } else {
            state = AppState.addEdge(state, from, to)
          }

        case 5 =>
          print("Enter From Vertex: ")
          val from = scala.io.StdIn.readLine()
          print("Enter To Vertex: ")
          val to = scala.io.StdIn.readLine()
          state = AppState.removeEdge(state, from, to)

        case 6 =>
          print("Enter Start Vertex: ")
          val start = scala.io.StdIn.readLine()
          AppState.dfs(state, start) match {
            case Right(path) => println(s"DFS Path: ${path.mkString(" -> ")}")
            case Left(error) => println(s"Error: $error")
          }

        case 7 =>
          print("Enter Start Vertex: ")
          val start = scala.io.StdIn.readLine()
          AppState.bfs(state, start) match {
            case Right(path) => println(s"BFS Path: ${path.mkString(" -> ")}")
            case Left(error) => println(s"Error: $error")
          }

        case 8 =>
          print("Enter Start Vertex: ")
          val start = scala.io.StdIn.readLine()
          AppState.dijkstra(state, start) match {
            case Right(distances) =>
              println("Dijkstra's Algorithm Result:")
              distances.foreach { case (vertex, distance) =>
                println(s"Vertex: $vertex, Distance: $distance")
              }
            case Left(error) => println(s"Error: $error")
          }

        case 9 =>
          AppState.floydWarshall(state) match {
            case Right(distances) =>
              println("Floyd-Warshall Algorithm Result:")
              distances.foreach { case ((from, to), distance) =>
                println(s"From: $from, To: $to, Distance: $distance")
              }
            case Left(error) => println(s"Error: $error")
          }

        case 10 =>
          AppState.topologicalSort(state) match {
            case Right(sorted) => println(s"Topologically Sorted Order: ${sorted.mkString(" -> ")}")
            case Left(error) => println(s"Error: $error")
          }

        case 11 =>
          AppState.detectCycle(state) match {
            case Right(cycle) =>
              if (cycle.isEmpty) println("No cycle detected.")
              else println(s"Detected Cycle: ${cycle.mkString(" -> ")}")
            case Left(error) => println(s"Error: $error")
          }

        case 12 =>
          displayState()

        case 13 =>
          println("Exiting...")
          continue = false

        case _ =>
          println("Invalid choice, please try again.")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    runApp()
  }
}
