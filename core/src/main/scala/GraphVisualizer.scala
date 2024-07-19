package scalaproject.core

import scala.sys.process._
import java.io._

object GraphVisualizer {

  def toGraphViz[V, G <: Graph_manager[V, G]](graph: Graph_manager[V, G]): String = {
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

  def visualize[V, G <: Graph_manager[V, G]](graph: Graph_manager[V, G], fileName: String = "graph"): Unit = {
    val dotFileContent = toGraphViz(graph)
    val dotFile = new File(s"$fileName.dot")
    val writer = new PrintWriter(dotFile)
    writer.write(dotFileContent)
    writer.close()

    s"dot -Tpng $fileName.dot -o $fileName.png".!
    println(s"Graph saved as $fileName.png")
  }
}
