package scalaproject.app

import zio.{ ZIO, ZIOAppDefault, Console }
import zio.Console._
import scalaproject.core._
import scalaproject.core.Edge

object App extends ZIOAppDefault {

//   def run = {
//     val program = for {
//       _ <- Console.printLine("Hello, ZIO!")  // Use printLine for console output
//     } yield ()

//     program
//       .catchAll(err => Console.printLine(s"Error: ${err.getMessage}"))
//       .provide(Console.live)  // Use Console.live for providing the Console environment
//   }

// Define your ZIO program
  val program: ZIO[Console, IOException, Unit] = for {
    _ <- Console.printLine("Hello, ZIO!")
  } yield ()

  // Run the program with default environment provided by ZIOAppDefault
  override def run: ZIO[Environment with Console, Any, Any] = program

  def mainMenu(): ZIO[Console, Throwable, Unit] = {
    for {
      _ <- printLine("Main Menu")
      _ <- printLine("1. Create Graph")
      _ <- printLine("2. Add Edge")
      _ <- printLine("3. Remove Edge")
      _ <- printLine("4. Display Graph")
      _ <- printLine("5. Perform DFS")
      _ <- printLine("6. Perform BFS")
      _ <- printLine("7. Exit")
      choice <- readLine
      _ <- choice match {
        case "1" => createGraph()
        case "2" => addEdge()
        case "3" => removeEdge()
        case "4" => displayGraph()
        case "5" => performDFS()
        case "6" => performBFS()
        case "7" => ZIO.succeed(())
        case _   => printLine("Invalid choice, please try again.") *> mainMenu()
      }
    } yield ()
  }

  def createGraph(): ZIO[Console, Throwable, Unit] = {
    for {
      _ <- printLine("Enter graph type (directed/undirected):")
      graphType <- readLine
      _ <- graphType match {
        case "directed" => printLine("Directed graph created.")
        case "undirected" => printLine("Undirected graph created.")
        case _ => printLine("Invalid graph type.") *> createGraph()
      }
      _ <- mainMenu()
    } yield ()
  }

  def addEdge(): ZIO[Console, Throwable, Unit] = {
    for {
      _ <- printLine("Adding an edge to the graph.")
      _ <- mainMenu()
    } yield ()
  }

  def removeEdge(): ZIO[Console, Throwable, Unit] = {
    for {
      _ <- printLine("Removing an edge from the graph.")
      _ <- mainMenu()
    } yield ()
  }

  def displayGraph(): ZIO[Console, Throwable, Unit] = {
    for {
      _ <- printLine("Displaying the graph.")
      _ <- mainMenu()
    } yield ()
  }

  def performDFS(): ZIO[Console, Throwable, Unit] = {
    for {
      _ <- printLine("Performing DFS on the graph.")
      _ <- mainMenu()
    } yield ()
  }

  def performBFS(): ZIO[Console, Throwable, Unit] = {
    for {
      _ <- printLine("Performing BFS on the graph.")
      _ <- mainMenu()
    } yield ()
  }
}
