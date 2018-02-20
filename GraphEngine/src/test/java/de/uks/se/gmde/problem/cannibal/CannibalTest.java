package de.uks.se.gmde.problem.cannibal;

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

public class CannibalTest
{
	@Test
	public void buildLts() throws IOException
	{
		Graph start = createHost();
		RulePriorityMap rules = RulePriorityMap.ofArray(
			ImmutableSet.of(new FinalStateRule()),
			ImmutableSet.of(new NomNom0Rule(), new NomNom1Rule()),
			ImmutableSet.of(new TransferRule())
		);

//		RulePriorityMap rules = RulePriorityMap.ofSinglePriority(
//			new FinalStateRule(), new NomNom0Rule(), new NomNom1Rule(), new TransferRule()
//		);

		Set<Graph> starts = ImmutableSet.of(start);
		ReachabilityGraph lts = ReachabilityGraph.computeLTS(
			starts,
			rules
		);

		Assert.assertNotNull(lts);

		GraphDumper.dumpGraph(lts.getLTSGraph(), "dumps/cannibal-lts.svg", starts);

		Assert.assertEquals(84, lts.getLTSGraph().getNodes().size());
	}

	@Test
	public void dumpLts() throws IOException
	{
		Graph start = createHost();
		RulePriorityMap rules = RulePriorityMap.ofArray(
			ImmutableSet.of(new FinalStateRule()),
			ImmutableSet.of(new NomNom0Rule(), new NomNom1Rule()),
			ImmutableSet.of(new TransferRule())
		);

		ReachabilityGraph lts = ReachabilityGraph.computeLTS(
			ImmutableSet.of(start),
			rules
		);

		Assert.assertNotNull(lts);
		GraphDumper.dumpLts(lts, "dumps/cannibal-lts-dump");
	}

	static Tuple<ReachabilityGraph, Graph> createLtsHost()
	{
		Graph start = createHost();
		RulePriorityMap rules = RulePriorityMap.ofArray(
			ImmutableSet.of(new FinalStateRule()),
			ImmutableSet.of(new NomNom0Rule(), new NomNom1Rule()),
			ImmutableSet.of(new TransferRule())
		);

		ReachabilityGraph lts = ReachabilityGraph.computeLTS(
			ImmutableSet.of(start),
			rules
		);

		Assert.assertNotNull(lts);
		return new Tuple<>(lts, start);
	}

	private static Graph createHost()
	{
		final Graph res = new Graph();

		Node left = res.createNode()
			.withLabel("bank")
			.withAttr("location", "left");
		Node right = res.createNode()
			.withLabel("bank")
			.withAttr("location", "right");
		Node boat = res.createNode()
			.withLabel("boat");

		left.createEdge("not-equal", right);
		right.createEdge("not-equal", left);

		Node cannibal0 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "cannibal")
			.withAttr("can-drive", true);
		Node cannibal1 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "cannibal")
			.withAttr("can-drive", false);
		Node cannibal2 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "cannibal")
			.withAttr("can-drive", false);

		cannibal0.createEdge("not-equal", cannibal1);
		cannibal0.createEdge("not-equal", cannibal2);

		cannibal1.createEdge("not-equal", cannibal0);
		cannibal1.createEdge("not-equal", cannibal2);

		cannibal2.createEdge("not-equal", cannibal0);
		cannibal2.createEdge("not-equal", cannibal1);

		cannibal0.createEdge("at", right);
		cannibal1.createEdge("at", right);
		cannibal2.createEdge("at", right);

		Node missionary0 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "missionary")
			.withAttr("can-drive", true);

		Node missionary1 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "missionary")
			.withAttr("can-drive", true);

		Node missionary2 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "missionary")
			.withAttr("can-drive", true);

		missionary0.createEdge("not-equal", missionary1);
		missionary0.createEdge("not-equal", missionary2);

		missionary1.createEdge("not-equal", missionary0);
		missionary1.createEdge("not-equal", missionary2);

		missionary2.createEdge("not-equal", missionary0);
		missionary2.createEdge("not-equal", missionary1);

		missionary0.createEdge("at", right);
		missionary1.createEdge("at", right);
		missionary2.createEdge("at", right);

		boat.createEdge("at", right);

		return res;
	}

	public static Graph createClassModelHost()
	{
		Graph res = createHost();

		Node left = res.getNodes().get(0);
		Node right = res.getNodes().get(1);

		// Some test values for type widening
		left.withAttr("sunset", 10); // int
		right.withAttr("sunset", 15.5); // double -> sunrise should be double
		left.withAttr("sunrise", "noon"); // String
		right.withAttr("sunrise", 15.5); // double -> sunrise should be Object
		left.withAttr("midnight", 0); // int
		left.withAttr("midnight", 0L); // long -> midnight should be long

		// Add some stuff for cardinality inference
		Node bogusClass0Node0 = res.createNode().withLabel("bogus-class-0");
		Node bogusClass0Node1 = res.createNode().withLabel("bogus-class-0");
		Node bogusClass1Node0 = res.createNode().withLabel("bogus-class-1");
		Node bogusClass1Node1 = res.createNode().withLabel("bogus-class-1");

		bogusClass0Node0.createEdge("should-have-card-1", bogusClass1Node0);
		bogusClass0Node0.createEdge("should-have-card-2", bogusClass1Node0);
		bogusClass0Node1.createEdge("should-have-card-2", bogusClass1Node0);
		bogusClass0Node1.createEdge("should-have-card-2", bogusClass1Node1);

		return res;
	}
}
