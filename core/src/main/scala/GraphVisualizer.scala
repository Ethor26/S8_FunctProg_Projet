// package scalaproject.core

import scala.sys.process.*
import java.io.*

object GraphVisualizer {

  def toGraphViz[V](graph: Graph[V]): String = { // [G, V <: Graph[V]]
    val edges = graph.edges.map {
      case Edge(from, to, weight) =>
        s""""$from" -> "$to" [label="$weight"];"""
    }.mkString("\n")
    s"""
      digraph G {
        $edges
      }
    """
  }

  def visualize[V](graph: Graph[V], fileName: String = "graph"): Unit = { // [G, V <: Graph[V]]
    val dotFileContent = toGraphViz(graph)
    val dotFile = new File(System.getProperty("user.dir") + s"/$fileName.dot")
    val writer = new PrintWriter(dotFile)
    writer.write(dotFileContent)
    writer.close()

    s"dot -Tpng $fileName.dot -o $fileName.png".!
    println(s"Graph saved as $fileName.png")
  }

  def main(args: Array[String]): Unit = {
    val graph = new DirectedGraph[Int](Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3)), weighted = false)
    graph.addEdge(Edge(1, 2))
    graph.addEdge(Edge(2, 3))
    graph.addEdge(Edge(3, 1))
    visualize[Int](graph)
  }
}
