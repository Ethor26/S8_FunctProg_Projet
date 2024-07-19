package scalaproject.core

import java.io._
import scala.sys.process._

object GraphVisualizer {

  def toGraphViz[V](graph: Graph[V]): String = {
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

  def visualize[V](graph: Graph[V], fileName: String = "graph"): Unit = {
    val dotFileContent = toGraphViz(graph)
    val dotFile = new File(s"$fileName.dot")
    val writer = new PrintWriter(dotFile)
    writer.write(dotFileContent)
    writer.close()

    s"dot -Tpng $fileName.dot -o $fileName.png".!
    println(s"Graph saved as $fileName.png")
  }
}
