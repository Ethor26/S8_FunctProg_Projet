import zio.json.*
// import java.nio.file.{Files, Paths}
// import java.nio.charset.StandardCharsets
// import scala.annotation.tailrec
import scala.collection.mutable

// Définir la classe abstraite de base pour les graphes
abstract class Graph[V] {
  def vertices: Set[V]
  def edges: Set[Edge[V]]
  def weighted: Boolean

  def neighbors(vertex: V): Set[V]
  protected def copyWith(edges: Set[Edge[V]], vertices: Set[V]): Graph[V]

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

  def addEdge(edge: Edge[V]): Graph[V] = {
    val validatedEdges = validateEdges(Set(edge))
    copyWith(edges ++ validatedEdges, vertices ++ Set(edge.from, edge.to))
  }

  def addEdges(newEdges: Set[Edge[V]]): Graph[V] = {
    val validatedEdges = validateEdges(newEdges)
    val newVertices = validatedEdges.flatMap(e => Set(e.from, e.to))
    copyWith(edges ++ validatedEdges, vertices ++ newVertices)
  }

  def removeEdge(edge: Edge[V]): Graph[V] = {
    copyWith(edges - edge, vertices -- Set(edge.from, edge.to).filter(v => edges.exists(e => e.from == v || e.to == v)))
  }

  def removeEdges(oldEdges: Set[Edge[V]]): Graph[V] = {
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

// Définir la classe pour les arêtes
case class Edge[V](from: V, to: V, weight: Double = Double.NaN)

// Implémentation d'un graphe non directionnel
case class UndirectedGraph[V](vertices: Set[V], private val initialEdges: Set[Edge[V]], weighted: Boolean) extends Graph[V] {
  override val edges: Set[Edge[V]] = validateEdges(initialEdges)

  override protected def copyWith(edges: Set[Edge[V]], vertices: Set[V]): UndirectedGraph[V] = {
    UndirectedGraph(vertices, edges, weighted)
  }
  def neighbors(vertex: V): Set[V] = edges.collect {
    case Edge(`vertex`, v, _) => v
    case Edge(v, `vertex`, _) => v
  }
}

// Implémentation d'un graphe directionnel
case class DirectedGraph[V](vertices: Set[V], private val initialEdges: Set[Edge[V]], weighted: Boolean) extends Graph[V] {
  override val edges: Set[Edge[V]] = validateEdges(initialEdges)

  override protected def copyWith(edges: Set[Edge[V]], vertices: Set[V]): DirectedGraph[V] = {
    DirectedGraph(vertices, edges, weighted)
  }
  def neighbors(vertex: V): Set[V] = edges.collect {
    case Edge(`vertex`, to, _) => to
  }

  // Topological Sorting
  def topologicalSort: Either[String, List[V]] = {
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
  }

  // Cycle Detection
  def detectCycle: Either[String, List[V]] = {
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
  }
}

@main
def main(): Unit = {
  println("Hello world!")
  var undirected_unweighted_graph = UndirectedGraph[Int](Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3)), weighted = false)
  println("--- undirected_unweighted_graph.neighbors(1): " + undirected_unweighted_graph.neighbors(1))
  undirected_unweighted_graph = undirected_unweighted_graph.addEdge(Edge(1, 3)).asInstanceOf[UndirectedGraph[Int]]
  println("--- undirected_unweighted_graph.neighbors(1) + Edge(1, 3): " + undirected_unweighted_graph.neighbors(1))
  val weightedGraph = UndirectedGraph(Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3, 2.0)), weighted = true)
  println("--- weightedGraph.dijkstra(1): " + weightedGraph.dijkstra(1))

  // Tests des algorithmes
  println("--- undirected_unweighted_graph.dfs(1): " + undirected_unweighted_graph.dfs(1))
  println("--- undirected_unweighted_graph.bfs(1): " + undirected_unweighted_graph.bfs(1))
  val directedGraph = DirectedGraph(Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3)), weighted = false)
  println("--- directedGraph.topologicalSort: " + directedGraph.topologicalSort)
  println("--- directedGraph.detectCycle: " + directedGraph.detectCycle)
  println("--- weightedGraph.floydWarshall(): " + weightedGraph.floydWarshall())
}
