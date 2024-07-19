package scalaproject.core

import zio.json.*
// import java.nio.file.{Files, Paths}
// import java.nio.charset.StandardCharsets
// import scala.annotation.tailrec
import scala.collection.mutable

// Définir la classe pour les arêtes
case class Edge[V](from: V, to: V, weight: Double = Double.NaN)

// Définir l'interface pour les graphes avec type paramétré G
abstract class Graph_manager[V, G <: Graph_manager[V, G]] {
  def vertices: Set[V]
  def edges: Set[Edge[V]]
  def weighted: Boolean

  def neighbors(vertex: V): Set[V]
  protected def copyWith(edges: Set[Edge[V]], vertices: Set[V]): G

  // Valider les arêtes
  def validateEdges(edges: Set[Edge[V]]): Set[Edge[V]] = {
    edges.map { edge =>
      if (!weighted && !edge.weight.isNaN && edge.weight != 1.0)
        throw new IllegalArgumentException(s"Invalid edge weight for a non-weighted graph: $edge")
      else if (weighted && edge.weight.isNaN) {
        println(s"Warning: Edge (${edge.from}, ${edge.to}) is missing weight. Assigning default weight 1.")
        edge.copy(weight = 1.0)
      } else
        edge
    }
  }

  def addEdge(edge: Edge[V]): G = {
    val validatedEdges = validateEdges(Set(edge))
    copyWith(edges ++ validatedEdges, vertices ++ Set(edge.from, edge.to))
  }

  def addEdges(newEdges: Set[Edge[V]]): G = {
    val validatedEdges = validateEdges(newEdges)
    val newVertices = validatedEdges.flatMap(e => Set(e.from, e.to))
    copyWith(edges ++ validatedEdges, vertices ++ newVertices)
  }

  def removeEdge(edge: Edge[V]): G = {
    copyWith(edges - edge, vertices -- Set(edge.from, edge.to).filter(v => edges.exists(e => e.from == v || e.to == v)))
  }

  def removeEdges(oldEdges: Set[Edge[V]]): G = {
    val oldVertices = oldEdges.flatMap(e => Set(e.from, e.to))
    copyWith(edges -- oldEdges, vertices -- oldVertices.filter(v => edges.exists(e => e.from == v || e.to == v)))
  }

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

  // Floyd's Algorithm
  def floydWarshall(): Either[String, Map[(V, V), Double]] = {
    if (!weighted) return Left("Floyd-Warshall algorithm is only applicable to weighted graphs.")

    val dist = mutable.Map[(V, V), Double]().withDefaultValue(Double.PositiveInfinity)
    for (v <- vertices) {
      dist((v, v)) = 0.0
    }
    for (Edge(from, to, weight) <- edges) {
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
    Right(dist.toMap)
  }

  // Dijkstra's Algorithm
  def dijkstra(start: V): Either[String, Map[V, Double]] = {
    if (!weighted) return Left("Dijkstra's algorithm is only applicable to weighted graphs.")

    val dist = mutable.Map(start -> 0.0).withDefaultValue(Double.PositiveInfinity)
    val pq = mutable.PriorityQueue((start, 0.0))(Ordering.by(-_._2))
    val visited = mutable.Set.empty[V]

    while (pq.nonEmpty) {
      val (current, currentDist) = pq.dequeue()
      if (!visited.contains(current)) {
        visited.add(current)
        for (Edge(`current`, neighbor, weight) <- edges) {
          val newDist = currentDist + weight
          if (newDist < dist(neighbor)) {
            dist(neighbor) = newDist
            pq.enqueue((neighbor, newDist))
          }
        }
      }
    }

    Right(dist.toMap)
  }
}

// Implémentation d'un graphe non directionnel
case class UndirectedG[V](vertices: Set[V], private val initialEdges: Set[Edge[V]], weighted: Boolean) extends Graph_manager[V, UndirectedG[V]] {
  override val edges: Set[Edge[V]] = validateEdges(initialEdges)

  override protected def copyWith(edges: Set[Edge[V]], vertices: Set[V]): UndirectedG[V] = {
    UndirectedG(vertices, edges, weighted)
  }

  def neighbors(vertex: V): Set[V] = edges.collect {
    case Edge(`vertex`, v, _) => v
    case Edge(v, `vertex`, _) => v
  }
}
