import zio.json.*
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.boundary.break

// Définir la classe abstraite de base pour les graphes
abstract class Graph[V, E] {
  def vertices: Set[V]
  def edges: Set[E]
  def neighbors(vertex: V): Set[V]
  def addEdge(edge: E): Graph[V, E]
  def removeEdge(edge: E): Graph[V, E]

  // Depth First Search (DFS)
  def dfs(start: V): List[V] = {
    def visit(vertex: V, visited: Set[V], result: List[V]): (Set[V], List[V]) = {
      if (visited.contains(vertex)) (visited, result)
      else {
        val newVisited = visited + vertex
        val neighborsList = neighbors(vertex).toList
        neighborsList.foldLeft((newVisited, vertex :: result)) {
          case ((visitedAcc, resultAcc), neighbor) =>
            visit(neighbor, visitedAcc, resultAcc)
        }
      }
    }

    visit(start, Set.empty, Nil)._2.reverse
  }

  // Breadth First Search (BFS)
  def bfs(start: V): List[V] = {
    val queue = mutable.Queue(start)
    val visited = mutable.Set(start)
    val result = mutable.ListBuffer(start)

    while (queue.nonEmpty) {
      val vertex = queue.dequeue()
      for (neighbor <- neighbors(vertex) if !visited.contains(neighbor)) {
        queue.enqueue(neighbor)
        visited.add(neighbor)
        result += neighbor
      }
    }

    result.toList
  }

  // Topological Sorting
  def topologicalSort: Either[String, List[V]] = this match {
    case dg: DirectedGraph[V] =>
      val visited = mutable.Set.empty[V]
      val tempMarked = mutable.Set.empty[V]
      val sortedList = mutable.ListBuffer.empty[V]

      def visit(node: V): Either[String, Unit] = {
        if (tempMarked.contains(node)) {
          Left("Graph has a cycle, topological sort not possible.")
        } else if (!visited.contains(node)) {
          tempMarked.add(node)
          val result = neighbors(node).foldLeft[Either[String, Unit]](Right(())) {
            (acc, neighbor) => acc.flatMap(_ => visit(neighbor))
          }
          result match {
            case Left(error) => Left(error)
            case Right(_) =>
              tempMarked.remove(node)
              visited.add(node)
              sortedList.prepend(node)
              Right(())
          }
        } else {
          Right(())
        }
      }

      val result = vertices.foldLeft[Either[String, Unit]](Right(())) {
        (acc, node) => acc.flatMap(_ => if (!visited.contains(node)) visit(node) else Right(()))
      }

      result.map(_ => sortedList.toList)
    case _ => Left("Topological sort is only applicable to directed graphs.")
  }

  // Cycle Detection
  def detectCycle: Either[String, List[V]] = this match {
    case dg: DirectedGraph[V] =>
      val visited = mutable.Set.empty[V]
      val stack = mutable.Set.empty[V]

      def visit(node: V, path: List[V]): Either[String, Unit] = {
        if (stack.contains(node)) {
          Left(s"Cycle detected: ${path.reverse.mkString(" -> ")} -> $node")
        } else if (!visited.contains(node)) {
          stack.add(node)
          visited.add(node)
          val result = neighbors(node).foldLeft[Either[String, Unit]](Right(())) {
            (acc, neighbor) => acc.flatMap(_ => visit(neighbor, node :: path))
          }
          result match {
            case Left(error) => Left(error)
            case Right(_) =>
              stack.remove(node)
              Right(())
          }
        } else {
          Right(())
        }
      }

      val result = vertices.foldLeft[Either[String, Unit]](Right(())) {
        (acc, node) => acc.flatMap(_ => if (!visited.contains(node)) visit(node, List(node)) else Right(()))
      }

      result.map(_ => Nil) // Right("No cycles detected")
    case _ => Left("Cycle detection is only applicable to directed graphs.")
  }
}

// Définir les cas de classes pour les arêtes
case class UndirectedEdge[V](v1: V, v2: V)
case class DirectedEdge[V](from: V, to: V)
case class WeightedEdge[V](from: V, to: V, weight: Double)

// Implémentation d'un graphe non directionnel
case class UndirectedGraph[V](vertices: Set[V], edges: Set[UndirectedEdge[V]]) extends Graph[V, UndirectedEdge[V]] {
  def neighbors(vertex: V): Set[V] = edges.collect {
    case UndirectedEdge(`vertex`, v) => v
    case UndirectedEdge(v, `vertex`) => v
  }

  def addEdge(edge: UndirectedEdge[V]): UndirectedGraph[V] = {
    copy(edges = edges + edge, vertices = vertices + edge.v1 + edge.v2)
  }

  def removeEdge(edge: UndirectedEdge[V]): UndirectedGraph[V] = {
    copy(edges = edges - edge)
  }
}

// Implémentation d'un graphe directionnel
case class DirectedGraph[V](vertices: Set[V], edges: Set[DirectedEdge[V]]) extends Graph[V, DirectedEdge[V]] {
  def neighbors(vertex: V): Set[V] = edges.collect {
    case DirectedEdge(`vertex`, to) => to
  }

  def addEdge(edge: DirectedEdge[V]): DirectedGraph[V] = {
    copy(edges = edges + edge, vertices = vertices + edge.from + edge.to)
  }

  def removeEdge(edge: DirectedEdge[V]): DirectedGraph[V] = {
    copy(edges = edges - edge)
  }
}

// Implémentation d'un graphe pondéré
case class WeightedGraph[V](vertices: Set[V], edges: Set[WeightedEdge[V]]) extends Graph[V, WeightedEdge[V]] {
  def neighbors(vertex: V): Set[V] = edges.collect {
    case WeightedEdge(`vertex`, to, _) => to
  }

  def addEdge(edge: WeightedEdge[V]): WeightedGraph[V] = {
    copy(edges = edges + edge, vertices = vertices + edge.from + edge.to)
  }

  def removeEdge(edge: WeightedEdge[V]): WeightedGraph[V] = {
    copy(edges = edges - edge)
  }

  // Méthode pour calculer le poids d'un chemin entre deux sommets
  def pathWeight(path: List[V]): Double = {
    @annotation.tailrec
    def helper(path: List[V], acc: Double): Double = path match {
      case from :: to :: rest =>
        edges.find(e => e.from == from && e.to == to) match {
          case Some(WeightedEdge(_, _, weight)) => helper(to :: rest, acc + weight)
          case None => Double.PositiveInfinity // Si une arête n'existe pas, chemin invalide
        }
      case _ => acc // Fin du chemin
    }

    if (path.isEmpty) Double.PositiveInfinity else helper(path, 0.0)
  }

  // Floyd's Algorithm
  def floydWarshall(): Map[(V, V), Double] = {
    val dist = mutable.Map[(V, V), Double]().withDefaultValue(Double.PositiveInfinity)
    for (v <- vertices) {
      dist((v, v)) = 0.0
    }
    for (WeightedEdge(from, to, weight) <- edges) {
      dist((from, to)) = weight
    }
    for {
      k <- vertices
      i <- vertices
      j <- vertices
    } {
      if (dist((i, k)) + dist((k, j)) < dist((i, j))) {
        dist((i, j)) = dist((i, k)) + dist((k, j))
      }
    }
    dist.toMap
  }

  // Dijkstra's Algorithm
  def dijkstra(start: V): Map[V, Double] = {
    val dist = mutable.Map(start -> 0.0).withDefaultValue(Double.PositiveInfinity)
    val pq = mutable.PriorityQueue((start, 0.0))(Ordering.by(-_._2))
    val visited = mutable.Set.empty[V]

    while (pq.nonEmpty) {
      val (current, currentDist) = pq.dequeue()
      if (!visited.contains(current)) {
        visited.add(current)
        for (WeightedEdge(`current`, neighbor, weight) <- edges) {
          val newDist = currentDist + weight
          if (newDist < dist(neighbor)) {
            dist(neighbor) = newDist
            pq.enqueue((neighbor, newDist))
          }
        }
      }
    }

    dist.toMap
  }
}

@main
def main(): Unit = {
  println("Hello world!")
  var undirected_graph = UndirectedGraph(Set(1, 2, 3), Set(UndirectedEdge(1, 2), UndirectedEdge(2, 3)))
  println("--- undirected_graph.neighbors(1): " + undirected_graph.neighbors(1))
  undirected_graph = undirected_graph.addEdge(UndirectedEdge(1, 3))
  println("--- undirected_graph.neighbors(1) + UndirectedEdge(1, 3): " + undirected_graph.neighbors(1))
  val weightedGraph = WeightedGraph(Set(1, 2, 3), Set(WeightedEdge(1, 2, 1.0), WeightedEdge(2, 3, 2.0)))
  println("--- weightedGraph.pathWeight(List(1, 2, 3)): " + weightedGraph.pathWeight(List(1, 2, 3)))

  // Tests des algorithmes
  println("--- undirected_graph.dfs(1): " + undirected_graph.dfs(1))
  println("--- undirected_graph.bfs(1): " + undirected_graph.bfs(1))
  println("--- undirected_graph.dijkstra(1): " + weightedGraph.dijkstra(1))
  println("--- undirected_graph.floydWarshall(): " + weightedGraph.floydWarshall())
}
