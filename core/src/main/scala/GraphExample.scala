import zio.json.*

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.boundary.break

/*
// Définir le trait de base pour les graphes
trait Graph[V, E] {
  def vertices: Set[V]
  def edges: Set[E]
  def neighbors(vertex: V): Set[V]
  def addEdge(edge: E): Graph[V, E]
  def removeEdge(edge: E): Graph[V, E]
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
}

@main
def main(): Unit = {
  println("Hello world!")
  val graph = UndirectedGraph(Set(1, 2, 3), Set(UndirectedEdge(1, 2), UndirectedEdge(2, 3)))
  println(graph.neighbors(1))
  val graph2 = graph.addEdge(UndirectedEdge(1, 3))
  println(graph2.neighbors(1))
  val weightedGraph = WeightedGraph(Set(1, 2, 3), Set(WeightedEdge(1, 2, 1.0), WeightedEdge(2, 3, 2.0)))
  println(weightedGraph.pathWeight(List(1, 2, 3)))

}
*/
