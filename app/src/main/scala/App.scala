import zio._
import zio.Console._
import zio.Console.{printLine, readLine}
import java.io.IOException

object Main extends ZIOAppDefault {

  def run: URIO[ZIOAppArgs & Scope, ExitCode] = program.exitCode

  val program: ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Welcome to the ZIO Graph Application!")
    _ <- selectMenu()
  } yield ()

  def selectMenu(): ZIO[Any, IOException, Unit] = for {
    _ <- printLine("Select Graph Type:")
    _ <- printLine("1. Directed Graph")
    _ <- printLine("2. Undirected Graph")
    _ <- printLine("3. Weighted Graph")
    _ <- printLine("Q. Exit") 

    choice <- readLine
    _ <- choice match {
      case "1" => mainMenu()
      case "2" => mainMenu()
      case "3" => mainMenu()
      case "Q" => printLine("Goodbye!") *> ZIO.succeed(())
      case _ => printLine("Invalid choice. Please try again.") *> selectMenu()
    }
  } yield ()

  def mainMenu(): ZIO[Any, IOException, Unit] = {
    for {
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
        case "1" => addEdgeMenu()
        case "2" => removeEdgeMenu()
        case "3" => displayGraph()
        case "4" => saveGraphToJson()
        case "5" => loadGraphFromJson()
        case "6" => algorithmMenu()
        case "Q" => printLine("Goodbye!") *> selectMenu()
        case _ => printLine("Invalid choice. Please try again.") *> mainMenu()
      }
    } yield ()
  }

  // Dummy implementations for other methods
  def addEdgeMenu(): ZIO[Any, IOException, Unit] = {
    for {
      _ <- printLine("this is to test the addEdgeMenu")
    } yield ()
  }
  def removeEdgeMenu(): ZIO[Any, IOException, Unit] = ???
  def displayGraph(): ZIO[Any, IOException, Unit] = ???
  def saveGraphToJson(): ZIO[Any, IOException, Unit] = ???
  def loadGraphFromJson(): ZIO[Any, IOException, Unit] = ???
  def algorithmMenu(): ZIO[Any, IOException, Unit] = ???
}
