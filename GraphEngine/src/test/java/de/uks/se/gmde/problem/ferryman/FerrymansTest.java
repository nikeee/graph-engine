package de.uks.se.gmde.problem.ferryman;

import com.google.common.collect.ImmutableSet;
import de.uks.se.Tuple;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.ReachabilityGraph;
import de.uks.se.gmde.RulePriorityMap;
import de.uks.se.gmde.rendering.GraphDumper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

public class FerrymansTest
{
	@Test
	public void buildLts() throws IOException
	{
		Graph start = createHost();
		RulePriorityMap rules = RulePriorityMap.ofArray(
			ImmutableSet.of(new FinalStateRule()),
			ImmutableSet.of(new NomNomRule()),
			ImmutableSet.of(new TransferEmptyRule(), new TransferRule())
		);

		Set<Graph> starts = ImmutableSet.of(start);
		ReachabilityGraph lts = ReachabilityGraph.computeLTS(
			starts,
			rules
		);

		Assert.assertNotNull(lts);

		GraphDumper.dumpGraph(lts.getLTSGraph(), "dumps/ferryman-lts.svg", starts);

		Assert.assertEquals(35, lts.getLTSGraph().getNodes().size());
		GraphDumper.dumpLts(lts, "dumps/ferrymans-lts-dump");
	}


	static Tuple<ReachabilityGraph, Graph> createLtsHost()
	{
		Graph start = createHost();
		RulePriorityMap rules = RulePriorityMap.ofArray(
			ImmutableSet.of(new FinalStateRule()),
			ImmutableSet.of(new NomNomRule()),
			ImmutableSet.of(new TransferEmptyRule(), new TransferRule())
		);

		Set<Graph> starts = ImmutableSet.of(start);
		ReachabilityGraph lts = ReachabilityGraph.computeLTS(
			starts,
			rules
		);

		Assert.assertNotNull(lts);
		return new Tuple<>(lts, start);
	}

	private static Graph createHost()
	{
		final Graph res = new Graph();

		Node left = res.createNode()
			.withLabel("side")
			.withAttr("location", "left");
		Node right = res.createNode()
			.withLabel("side")
			.withAttr("location", "right");
		Node boat = res.createNode().withLabel("boat");

		left.createEdge("opposite", right);
		right.createEdge("opposite", left);

		Node cabbage = res.createNode()
			.withLabel("cargo")
			.withAttr("kind", "cabbage");
		Node goat = res.createNode()
			.withLabel("cargo")
			.withAttr("kind", "goat");
		Node wolf = res.createNode()
			.withLabel("cargo")
			.withAttr("kind", "wolf");

		wolf.createEdge("eats", goat);
		goat.createEdge("eats", cabbage);

		cabbage.createEdge("at", left);
		goat.createEdge("at", left);
		wolf.createEdge("at", left);
		boat.createEdge("at", left);

		return res;
	}
}
