package scalaproject.app

import scalaproject.core._
import zio._
import zio.Console._
import zio.json._
import java.io._

object Main extends ZIOAppDefault {

  def run: URIO[ZIOAppArgs & Scope, ExitCode] = program.exitCode

  val program: ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Welcome to the ZIO Graph Application!")
    directedRef <- Ref.make(DirectedGraph(Set.empty[String], Set.empty, weighted = false))
    undirectedRef <- Ref.make(UndirectedGraph(Set.empty[String], Set.empty, weighted = false))
    _ <- selectMenu(AppState(directedRef, undirectedRef, GraphType.Directed))
  } yield ()

  def selectMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
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

  def mainMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
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

  def addEdgeMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Enter the source vertex:")
    source <- readLine
    _ <- printLine("Enter the destination vertex:")
    destination <- readLine
    _ <- appState.addEdge(source, destination)
    _ <- printLine(s"Edge ($source, $destination) added.")
    _ <- mainMenu(appState)
  } yield ()

  def removeEdgeMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Enter the source vertex:")
    source <- readLine
    _ <- printLine("Enter the destination vertex:")
    destination <- readLine
    _ <- appState.removeEdge(source, destination)
    _ <- printLine(s"Edge ($source, $destination) removed.")
    _ <- mainMenu(appState)
  } yield ()

  def displayGraph(appState: AppState): ZIO[Any, IOException, Unit] = for {
    graph <- appState.graph // Extract graph from effect
    grviz = GraphVisualizer.toGraphViz(graph)
    _ <- printLine(grviz)
    _ <- mainMenu(appState)
  } yield ()

  def saveGraphToJson(appState: AppState): ZIO[Any, IOException, Unit] = for {
    graph <- appState.graph // Extract graph from effect
    json = graph.toJson
    _ <- printLine(s"Graph JSON: $json")
    _ <- mainMenu(appState)
  } yield ()

  def loadGraphFromJson(appState: AppState): ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Enter the graph JSON:")
    json <- readLine
    _ <- appState.loadJson(json)
    _ <- printLine("Graph loaded from JSON.")
    _ <- mainMenu(appState)
  } yield ()

  def algorithmMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Algorithm Menu:")
    _ <- appState.graphType match {
      case GraphType.Directed =>
        printLine("1. Depth-First Search (DFS)") *>
        printLine("2. Breadth-First Search (BFS)") *>
        printLine("3. Topological Sort") *>
        printLine("4. Cycle Detection")
      case _ =>
        printLine("Graph algorithms are not available for this graph type.")
    }
    _ <- printLine("Q. Back to Main Menu")

    choice <- readLine
    _ <- choice match {
      case "1" => appState.graphType match {
        case GraphType.Directed => dfsMenu(appState)
        case _ => printLine("DFS is not available for this graph type.") *> algorithmMenu(appState)
      }
      case "2" => appState.graphType match {
        case GraphType.Directed => bfsMenu(appState)
        case _ => printLine("BFS is not available for this graph type.") *> algorithmMenu(appState)
      }
      case "3" => topologicalSortMenu(appState)
      case "4" => cycleDetectionMenu(appState)
      case "Q" => mainMenu(appState)
      case _ => printLine("Invalid choice. Please try again.") *> algorithmMenu(appState)
    }
  } yield ()

  def dfsMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Enter the starting vertex:")
    start <- readLine
    graph <- appState.graph
    result = graph.dfs(start)
    _ <- printLine(s"DFS result: $result")
    _ <- algorithmMenu(appState)
  } yield ()

  def bfsMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Enter the starting vertex:")
    start <- readLine
    graph <- appState.graph
    result = graph.bfs(start)
    _ <- printLine(s"BFS result: $result")
    _ <- algorithmMenu(appState)
  } yield ()

  def topologicalSortMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
    graph <- appState.graph
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

  def cycleDetectionMenu(appState: AppState): ZIO[Any, IOException, Unit] = for {
    graph <- appState.graph
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

case class AppState(graphType: GraphType) {
  private val directedGraph = Ref.make(DirectedGraph[String](Set.empty, Set.empty, weighted = false))
  private val undirectedGraph = Ref.make(UndirectedGraph[String](Set.empty, Set.empty, weighted = false))

  def saveGraphToJson: IO[String, String] = {
    for {
      graph <- graphType match {
        case GraphType.Directed => directedGraph.get
        case GraphType.Undirected => undirectedGraph.get
      }
    } yield graph match {
      case g: DirectedGraph[String] => g.toJson
      case g: UndirectedGraph[String] => g.toJson
    }
  }

  def loadJson(json: String): IO[String, Unit] = {
    graphType match {
      case GraphType.Directed =>
        ZIO.fromEither(json.fromJson[DirectedGraph[String]]).flatMap(directedGraph.set).mapError(_.getMessage)
      case GraphType.Undirected =>
        ZIO.fromEither(json.fromJson[UndirectedGraph[String]]).flatMap(undirectedGraph.set).mapError(_.getMessage)
    }
  }
}
