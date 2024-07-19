import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GraphSpec extends AnyFlatSpec with Matchers {

  "An UndirectedGraph" should "add and retrieve edges correctly" in {
    var graph = UndirectedGraph[Int](Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3)), weighted = false)
    graph.neighbors(1) should contain (2)
    graph = graph.addEdge(Edge(1, 3)).asInstanceOf[UndirectedGraph[Int]]
    graph.neighbors(1) should contain (3)
  }

  it should "perform DFS correctly" in {
    val graph = UndirectedGraph[Int](Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3)), weighted = false)
    graph.dfs(1) should equal (List(1, 2, 3))
  }

  it should "perform BFS correctly" in {
    val graph = UndirectedGraph[Int](Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3)), weighted = false)
    graph.bfs(1) should equal (List(1, 2, 3))
  }

  it should "perform Dijkstra's algorithm correctly on weighted graph" in {
    val graph = UndirectedGraph(Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3, 2.0)), weighted = true)
    graph.dijkstra(1) should equal (Right(Map(1 -> 0.0, 2 -> 1.0, 3 -> 3.0)))
  }

  it should "perform Floyd-Warshall algorithm correctly on weighted graph" in {
    val graph = UndirectedGraph(Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3, 2.0)), weighted = true)
    graph.floydWarshall() should equal (Right(Map(
      (1, 1) -> 0.0, (1, 2) -> 1.0, (1, 3) -> 3.0,
      (2, 1) -> Double.PositiveInfinity, (2, 2) -> 0.0, (2, 3) -> 2.0,
      (3, 1) -> Double.PositiveInfinity, (3, 2) -> Double.PositiveInfinity, (3, 3) -> 0.0
    )))
  }

  "A DirectedGraph" should "perform topological sorting correctly" in {
    val graph = DirectedGraph(Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3)), weighted = false)
    graph.topologicalSort should equal (Right(List(1, 2, 3)))
  }

  it should "detect cycles correctly" in {
    val graph = DirectedGraph(Set(1, 2, 3), Set(Edge(1, 2), Edge(2, 3), Edge(3, 1)), weighted = false)
    graph.detectCycle should be (Left("Cycle detected: 1 -> 2 -> 3 -> 1"))
  }
}
