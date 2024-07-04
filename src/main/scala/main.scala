import zio.json.*

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.boundary.break

// Noeud du graphe
case class Node(id: Int)
object Node {
  implicit val nodeEncoder: JsonEncoder[Node] = DeriveJsonEncoder.gen[Node]
  implicit val nodeDecoder: JsonDecoder[Node] = DeriveJsonDecoder.gen[Node]
}

// Types de ponts
sealed trait Edge
object Edge {
  implicit val edgeEncoder: JsonEncoder[Edge] = DeriveJsonEncoder.gen[Edge]
  implicit val edgeDecoder: JsonDecoder[Edge] = DeriveJsonDecoder.gen[Edge]
}

case class BidirectionalEdge(node1: Node, node2: Node) extends Edge
object BidirectionalEdge {
  implicit val bidirectionalEdgeEncoder: JsonEncoder[BidirectionalEdge] = DeriveJsonEncoder.gen[BidirectionalEdge]
  implicit val bidirectionalEdgeDecoder: JsonDecoder[BidirectionalEdge] = DeriveJsonDecoder.gen[BidirectionalEdge]
}

case class DirectedEdge(from: Node, to: Node) extends Edge
object DirectedEdge {
  implicit val directedEdgeEncoder: JsonEncoder[DirectedEdge] = DeriveJsonEncoder.gen[DirectedEdge]
  implicit val directedEdgeDecoder: JsonDecoder[DirectedEdge] = DeriveJsonDecoder.gen[DirectedEdge]
}

case class WeightedEdge(node1: Node, node2: Node, weight: Double) extends Edge
object WeightedEdge {
  implicit val weightedEdgeEncoder: JsonEncoder[WeightedEdge] = DeriveJsonEncoder.gen[WeightedEdge]
  implicit val weightedEdgeDecoder: JsonDecoder[WeightedEdge] = DeriveJsonDecoder.gen[WeightedEdge]
}

// Graphe générique
sealed trait Graph
object Graph {
  implicit val graphEncoder: JsonEncoder[Graph] = DeriveJsonEncoder.gen[Graph]
  implicit val graphDecoder: JsonDecoder[Graph] = DeriveJsonDecoder.gen[Graph]
}

case class BidirectionalGraph(edges: Set[BidirectionalEdge]) extends Graph
object BidirectionalGraph {
  implicit val bidirectionalGraphEncoder: JsonEncoder[BidirectionalGraph] = DeriveJsonEncoder.gen[BidirectionalGraph]
  implicit val bidirectionalGraphDecoder: JsonDecoder[BidirectionalGraph] = DeriveJsonDecoder.gen[BidirectionalGraph]
}

case class DirectedGraph(edges: Set[DirectedEdge]) extends Graph
object DirectedGraph {
  implicit val directedGraphEncoder: JsonEncoder[DirectedGraph] = DeriveJsonEncoder.gen[DirectedGraph]
  implicit val directedGraphDecoder: JsonDecoder[DirectedGraph] = DeriveJsonDecoder.gen[DirectedGraph]
}

case class WeightedGraph(edges: Set[WeightedEdge]) extends Graph
object WeightedGraph {
  implicit val weightedGraphEncoder: JsonEncoder[WeightedGraph] = DeriveJsonEncoder.gen[WeightedGraph]
  implicit val weightedGraphDecoder: JsonDecoder[WeightedGraph] = DeriveJsonDecoder.gen[WeightedGraph]
}

// Ajouter un pont bidirectionnel
def addBidirectionalEdge(graph: BidirectionalGraph, edge: BidirectionalEdge): BidirectionalGraph = {
  BidirectionalGraph(graph.edges + edge)
}

// Ajouter un pont dirigé
def addDirectedEdge(graph: DirectedGraph, edge: DirectedEdge): DirectedGraph = {
  DirectedGraph(graph.edges + edge)
}

// Ajouter un pont pondéré
def addWeightedEdge(graph: WeightedGraph, edge: WeightedEdge): WeightedGraph = {
  WeightedGraph(graph.edges + edge)
}

// Calculer le poids total d'un parcours dans un graphe pondéré
def calculatePathWeight(graph: WeightedGraph, path: List[Node]): Option[Double] = {
  path.sliding(2).foldLeft(Option(0.0)) { (acc, pair) =>
    pair match {
      case List(from, to) =>
        graph.edges.find(edge => (edge.node1 == from && edge.node2 == to) || (edge.node1 == to && edge.node2 == from)) match {
          case Some(edge) => acc.map(_ + edge.weight)
          case None => None
        }
      case _ => acc
    }
  }
}

def saveGraphToJson(graph: Graph, filename: String): Unit = {
  val jsonString = graph.toJson
  Files.write(Paths.get(filename), jsonString.getBytes(StandardCharsets.UTF_8))
}

def loadGraphFromJson(filename: String): Either[String, Graph] = {
  val jsonString = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8)
  jsonString.fromJson[Graph]
}

// Fonction pour obtenir les voisins d'un noeud dans différents types de graphes
def neighbors(graph: Graph, node: Node): List[Node] = graph match {
  case BidirectionalGraph(edges) =>
    edges.collect {
      case BidirectionalEdge(`node`, neighbor) => neighbor
      case BidirectionalEdge(neighbor, `node`) => neighbor
    }.toList

  case DirectedGraph(edges) =>
    edges.collect {
      case DirectedEdge(`node`, neighbor) => neighbor
    }.toList

  case WeightedGraph(edges) =>
    edges.collect {
      case WeightedEdge(`node`, neighbor, _) => neighbor
      case WeightedEdge(neighbor, `node`, _) => neighbor
    }.toList
}

// DFS générique pour n'importe quel type de graphe
def dfs(graph: Graph, start: Node): Set[Node] = {
  @tailrec
  def dfsRecursive(stack: List[Node], visited: Set[Node]): Set[Node] = stack match {
    case Nil => visited
    case node :: rest =>
      if (visited.contains(node)) {
        dfsRecursive(rest, visited)
      } else {
        val neighborsList = neighbors(graph, node)
        dfsRecursive(neighborsList ++ rest, visited + node)
      }
  }
  dfsRecursive(List(start), Set.empty[Node])
}

// BFS générique pour n'importe quel type de graphe
def bfs(graph: Graph, start: Node): Set[Node] = {
  @tailrec
  def bfsRecursive(queue: List[Node], visited: Set[Node]): Set[Node] = queue match {
    case Nil => visited
    case node :: rest =>
      if (visited.contains(node)) {
        bfsRecursive(rest, visited)
      } else {
        val neighborsList = neighbors(graph, node)
        bfsRecursive(rest ++ neighborsList, visited + node)
      }
  }
  bfsRecursive(List(start), Set.empty[Node])
}

// Tri topologique pour un graphe dirigé
def topologicalSort(graph: DirectedGraph): Either[String, List[Node]] = {
  val visited = mutable.Set.empty[Node]
  val tempMarked = mutable.Set.empty[Node]
  val sortedList = mutable.ListBuffer.empty[Node]
  var has_error_loop = false

  def visit(node: Node): Either[String, Unit] = {
    var has_error_loop = false
    if (tempMarked.contains(node)) {
      Left("Graph has a cycle, topological sort not possible.")
    } else if (!visited.contains(node)) {
      tempMarked.add(node)
      for (neighbor <- neighbors(graph, node)) {
        visit(neighbor) match {
          case Left(error) =>  has_error_loop = true // return Left(error)
          case Right(_) => // continue
        }
      }
      if (has_error_loop) {
        return Left("Error in visiting neighbors: function 'topological sort.visit'")
      }
      tempMarked.remove(node)
      visited.add(node)
      sortedList.prepend(node)
      Right(())
    } else {
      Right(())
    }
  }

  for (node <- graph.edges.flatMap(edge => List(edge.from, edge.to)).toSet) {
    if (!visited.contains(node)) {
      visit(node) match {
        case Left(error) => has_error_loop = true // return Left(error)
        case Right(_) => // continue
      }
    }
  }
  if (has_error_loop) {
    return Left("Error in visiting neighbors: function 'topological sort'")
  }

  Right(sortedList.toList)
}

// Fonction récursive de détection de cycles pour un graphe dirigé
def detectCycle(graph: DirectedGraph): Either[String, List[Node]] = {
  val visited = mutable.Set.empty[Node]
  val stack = mutable.Set.empty[Node]
  var has_error_loop = false

  def visit(node: Node, path: List[Node]): Either[String, Unit] = {
    var has_error_loop = false
    if (stack.contains(node)) {
      Left(s"Cycle detected: ${path.reverse.mkString(" -> ")} -> $node")
    } else if (!visited.contains(node)) {
      stack.add(node)
      visited.add(node)
      for (neighbor <- neighbors(graph, node)) {
        visit(neighbor, node :: path) match {
          case Left(error) => has_error_loop = true// return Left(error)
          case Right(_) => // continue
        }
      }
      if (has_error_loop) {
        return Left("Error in visiting neighbors: function 'detect_cycle.visit'")
      }
      stack.remove(node)
      Right(())
    } else {
      Right(())
    }
  }

  for (node <- graph.edges.flatMap(edge => List(edge.from, edge.to)).toSet) {
    if (!visited.contains(node)) {
      visit(node, List(node)) match {
        case Left(error) => has_error_loop = true // return Left(error)
        case Right(_) => // continue
      }
    }
  }
  if (has_error_loop) {
    return Left("Error in visiting neighbors: function 'detect cycle'")
  }
  Right(Nil) //   Right("No cycles detected")

}

object GraphExample extends App {
  val node1 = Node(1)
  val node2 = Node(2)
  val node3 = Node(3)

  // Créer un graphe bidirectionnel
  var bidirectionalGraph = BidirectionalGraph(Set())
  bidirectionalGraph = addBidirectionalEdge(bidirectionalGraph, BidirectionalEdge(node1, node2))
  bidirectionalGraph = addBidirectionalEdge(bidirectionalGraph, BidirectionalEdge(node2, node3))

  // Créer un graphe dirigé
  var directedGraph = DirectedGraph(Set())
  directedGraph = addDirectedEdge(directedGraph, DirectedEdge(node1, node2))
  directedGraph = addDirectedEdge(directedGraph, DirectedEdge(node2, node3))

  // Créer un graphe pondéré
  var weightedGraph = WeightedGraph(Set())
  weightedGraph = addWeightedEdge(weightedGraph, WeightedEdge(node1, node2, 1.5))
  weightedGraph = addWeightedEdge(weightedGraph, WeightedEdge(node2, node3, 2.5))

  // Calculer le poids d'un parcours
  val pathWeight = calculatePathWeight(weightedGraph, List(node1, node2, node3))
  println(s"Weight of the path from node1 to node3: $pathWeight")
}


@main
def main(): Unit = {
  println("Hello world!")
}