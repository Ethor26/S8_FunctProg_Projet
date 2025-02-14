import zio.*
import zio.Console.*
import zio.json.*

import java.io.*
import java.nio.file.{Files, Paths}

object Main extends ZIOAppDefault {

  def run: URIO[ZIOAppArgs & Scope, ExitCode] = program.exitCode

  val program: ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Welcome to the ZIO Graph Application!")
    directedRef <- Ref.make(DirectedGraph(Set.empty[String], Set.empty, weighted = false))
    undirectedRef <- Ref.make(UndirectedGraph(Set.empty[String], Set.empty, weighted = false))
    _ <- selectMenu(AppState(directedRef, undirectedRef, GraphType.Directed))
  } yield ()

  def selectMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Select Graph Type:")
    _ <- printLine("1. Directed Graph")
    _ <- printLine("2. Undirected Graph")
    _ <- printLine("Q. Exit")

    choice <- readLine
    _ <- choice match {
      case "1" => mainMenu(appState.copy(graphType = GraphType.Directed))
      case "2" => mainMenu(appState.copy(graphType = GraphType.Undirected))
      case "Q" => printLine("Goodbye!") *> ZIO.succeed(())
      case _ => printLine("Invalid choice. Please try again.") *> selectMenu(appState)
    }
  } yield ()

  def mainMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Main Menu:")
    _ <- printLine("1. Add Edge")
    _ <- printLine("2. Remove Edge")
    _ <- printLine("3. Display Graph")
    _ <- printLine("4. Save Graph to JSON")
    _ <- printLine("5. Load Graph from JSON")
    _ <- printLine("6. Go to Algorithm Menu")
    _ <- printLine("Q. Exit")

    choice <- readLine
    _ <- choice match {
      case "1" => addEdgeMenu(appState)
      case "2" => removeEdgeMenu(appState)
      case "3" => displayGraph(appState)
      case "4" => saveGraphToJson(appState)
      case "5" => loadGraphFromJson(appState)
      case "6" => algorithmMenu(appState)
      case "Q" => printLine("Goodbye!") *> selectMenu(appState)
      case _ => printLine("Invalid choice. Please try again.") *> mainMenu(appState)
    }
  } yield ()

  def addEdgeMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Enter the source vertex:")
    source <- readLine
    _ <- printLine("Enter the destination vertex:")
    destination <- readLine
    _ <- appState.addEdge(Edge(source, destination))
    _ <- printLine(s"Edge ($source, $destination) added.")
    _ <- mainMenu(appState)
  } yield ()

  def removeEdgeMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Enter the source vertex:")
    source <- readLine
    _ <- printLine("Enter the destination vertex:")
    destination <- readLine
    _ <- appState.removeEdge(Edge(source, destination))
    _ <- printLine(s"Edge ($source, $destination) removed.")
    _ <- mainMenu(appState)
  } yield ()

  def displayGraph(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    graph <- appState.getGraph
    grviz = GraphVisualizer.toGraphViz(graph)
    _ <- printLine(grviz)
    _ <- mainMenu(appState)
  } yield ()

  def create_json_file(json: String): String = {
    val path = Paths.get("generated_graph.json")
    print(s"Writing JSON to file: $path...")
    Files.write(path, json.getBytes)
    json
  }

  def saveGraphToJson(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    graph <- appState.getGraph
    encoder = appState.getJsonEncoder
    json = create_json_file(graph.toJsonPretty(encoder))
    _ <- printLine(s"Graph JSON: $json")
    _ <- mainMenu(appState)
  } yield ()

  def loadGraphFromJson(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Enter the graph JSON:")
    json <- readLine
    _ <- appState.loadJson(json)
    _ <- printLine("Graph loaded from JSON.")
    _ <- mainMenu(appState)
  } yield ()

  def algorithmMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Algorithm Menu:")
    _ <- printLine("1. Depth-First Search (DFS)")
    _ <- printLine("2. Breadth-First Search (BFS)")
    _ <- printLine("3. Floyd-Warshall Algorithm")
    _ <- printLine("4. Djikstra's Algorithm")
    _ <- appState.graphType match {
      case GraphType.Directed =>
          printLine("5. Topological Sort") *>
          printLine("6. Cycle Detection") 
      case _ => printLine("Other algorithms not available for undirected graphs.")
    }
    _ <- printLine("Q. Back to Main Menu")

    choice <- readLine
    _ <- choice match {
      case "1" => dfsMenu(appState)
      case "2" => bfsMenu(appState)
      case "3" => floydWarshallMenu(appState)
      case "4" => djikstraMenu(appState)
      case "5" => appState.graphType match {
        case GraphType.Directed => topologicalSortMenu(appState)
        case _ => printLine("TopologicalSort is not available for this graph type.") *> algorithmMenu(appState)
      }
      case "6" => appState.graphType match {
        case GraphType.Directed => cycleDetectionMenu(appState)
        case _ => printLine("CycleDetection is not available for this graph type.") *> algorithmMenu(appState)
      }
      case "Q" => mainMenu(appState)
      case _ => printLine("Invalid choice. Please try again.") *> algorithmMenu(appState)
    }
  } yield ()

  // TODO: Fonctions suivantes à factoriser
  def floydWarshallMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Enter the starting vertex:")
    start <- readLine
    graph <- appState.getGraph
    result = graph.floydWarshall()
    _ <- printLine(s"DFS result: $result")
    _ <- algorithmMenu(appState)
  } yield ()

  def djikstraMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Enter the starting vertex:")
    start <- readLine
    graph <- appState.getGraph
    result = graph.dijkstra(start)
    _ <- printLine(s"DFS result: $result")
    _ <- algorithmMenu(appState)
  } yield ()

  def dfsMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Enter the starting vertex:")
    start <- readLine
    graph <- appState.getGraph
    result = graph.dfs(start)
    _ <- printLine(s"DFS result: $result")
    _ <- algorithmMenu(appState)
  } yield ()

  def bfsMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    _ <- printLine("Enter the starting vertex:")
    start <- readLine
    graph <- appState.getGraph
    result = graph.bfs(start)
    _ <- printLine(s"BFS result: $result")
    _ <- algorithmMenu(appState)
  } yield ()

  def topologicalSortMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    graph <- appState.getGraph
    result = graph match {
      case dg: DirectedGraph[String] => dg.topologicalSort
      case _ => Left("Topological sort is only applicable to directed graphs.")
    }
    _ <- result match {
      case Right(sorted) => printLine(s"Topological Sort result: $sorted")
      case Left(error) => printLine(s"Error: $error")
    }
    _ <- algorithmMenu(appState)
  } yield ()

  def cycleDetectionMenu(appState: AppState): ZIO[Any, String | IOException, Unit] = for {
    graph <- appState.getGraph
    result = graph match {
      case dg: DirectedGraph[String] => dg.detectCycle
      case _ => Left("Cycle detection is only applicable to directed graphs.")
    }
    _ <- result match {
      case Right(_) => printLine("No cycles detected.")
      case Left(error) => printLine(s"Error: $error")
    }
    _ <- algorithmMenu(appState)
  } yield ()
}

sealed trait GraphType
object GraphType {
  case object Directed extends GraphType
  case object Undirected extends GraphType
}

case class AppState(
                     directedGraph: Ref[DirectedGraph[String]],
                     undirectedGraph: Ref[UndirectedGraph[String]], 
                     graphType: GraphType
                   ) {

  def getJsonEncoder: JsonEncoder[Graph[String]] = graphType match {
    case GraphType.Directed => implicitly[JsonEncoder[DirectedGraph[String]]].asInstanceOf[JsonEncoder[Graph[String]]]
    case GraphType.Undirected => implicitly[JsonEncoder[UndirectedGraph[String]]].asInstanceOf[JsonEncoder[Graph[String]]]
  }

  def getGraph: UIO[Graph[String]] = graphType match {
    case GraphType.Directed => directedGraph.get
    case GraphType.Undirected => undirectedGraph.get
  }

  def addEdge(edge: Edge[String]): ZIO[Any, String, Unit] = {
    graphType match {
      case GraphType.Directed =>
        directedGraph.update(g => g.addEdge(edge).asInstanceOf).unit
      case GraphType.Undirected =>
        undirectedGraph.update(_.addEdge(edge).asInstanceOf).unit
    }
  }

  def removeEdge(edge: Edge[String]): ZIO[Any, String, Unit] = {
    graphType match {
      case GraphType.Directed =>
        directedGraph.update(_.removeEdge(edge).asInstanceOf).unit
      case GraphType.Undirected =>
        undirectedGraph.update(_.removeEdge(edge).asInstanceOf).unit
    }
  }

  def toJson: UIO[String] = { // ZIO[Any, String, String] = {
    graphType match {
      case GraphType.Directed =>
        directedGraph.get.map(_.toJsonPretty) // graph => graph.toJsonPretty
      case GraphType.Undirected =>
        undirectedGraph.get.map(_.toJsonPretty)
    }
  }

  def fromJson(json: String): ZIO[Any, String, Unit] = {
    graphType match {
      case GraphType.Directed =>
        ZIO
          .fromEither(json.fromJson[DirectedGraph[String]])
          .flatMap(graph => directedGraph.set(graph))
      case GraphType.Undirected =>
        ZIO
          .fromEither(json.fromJson[UndirectedGraph[String]])
          .flatMap(graph => undirectedGraph.set(graph))
    }
  }

  def saveGraphToJson: UIO[String] = toJson // getGraph.flatMap(graph => ZIO.succeed(graph.toJsonPretty)) // getGraph.map(_.toJsonPretty)

  def loadJson(json: String): IO[String, Unit] = graphType match {
    case GraphType.Directed =>
      ZIO.fromEither(json.fromJson[DirectedGraph[String]]).flatMap(directedGraph.set).mapError(_.toString)
    case GraphType.Undirected =>
      ZIO.fromEither(json.fromJson[UndirectedGraph[String]]).flatMap(undirectedGraph.set).mapError(_.toString)
    case _ => ZIO.fail("Invalid graph type.")
  }
}
