package de.uks.se.gmde;

import de.uks.se.ArgumentNullException;
import org.junit.Assert;
import org.junit.Test;

public class GraphStatsTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullGraph()
	{
		GraphStats.createFrom((Graph[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyGraphArray()
	{
		GraphStats.createFrom();
	}

	@Test
	public void basicGraphStats()
	{
		Graph g = GraphUtils.createTestGraph();
		GraphStats statsSingleGraph = GraphStats.createFrom(g);
		Assert.assertNotNull(statsSingleGraph);
		System.out.println(statsSingleGraph.toString());

		GraphStats statsMultipleGraphs = GraphStats.createFrom(g, g, g);
		Assert.assertNotNull(statsMultipleGraphs);
		System.out.println(statsMultipleGraphs.toString());

		Assert.assertEquals(statsSingleGraph.toString(), statsMultipleGraphs.toString());
	}

	@Test
	public void multipleGraphStats()
	{
		Graph g0 = GraphUtils.createTestGraph();

		Node e0g0 = GraphUtils.getE0(g0);
		e0g0.removeEdge("has", e0g0.getEdges("has").get(0));
		GraphStats g0Stats = GraphStats.createFrom(g0);

		Assert.assertNotNull(g0Stats);

		System.out.println("Stats of g0:");
		System.out.println(g0Stats.toString());

		Graph g1 = GraphUtils.createTestGraph();
		Node e0g1 = GraphUtils.getE0(g1);
		e0g1.createEdge("has", g1.getNodes().get(0));
		GraphStats g1Stats = GraphStats.createFrom(g1);

		Assert.assertNotNull(g1Stats);

		System.out.println("Stats of g1:");
		System.out.println(g1Stats.toString());

		GraphStats stats = GraphStats.createFrom(g0, g1);

		Assert.assertNotNull(stats);

		System.out.println("Stats of g0 and g1:");
		System.out.println(stats.toString());
	}
}
