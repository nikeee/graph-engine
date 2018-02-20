package de.uks.se.gmde.query;

import com.google.common.collect.ImmutableSet;
import de.uks.se.gmde.*;

import java.util.List;
import java.util.stream.Collectors;

public class QueryTestUtil
{
	static boolean leftSideIsEmpty(final Graph state)
	{
		List<Node> leftNodes = state.getNodes().stream()
			.filter(n -> "left".equals(n.getAttribute("location").orElse(null)))
			.collect(Collectors.toList());

		assert leftNodes.size() == 1;
		return leftNodes.get(0).getEdges("has-cargo").size() == 0;
	}

	static boolean goatAtLeftSide(final Graph state)
	{
		List<Node> leftNodes = state.getNodes().stream()
			.filter(n -> "left".equals(n.getAttribute("location").orElse(null)))
			.collect(Collectors.toList());

		assert leftNodes.size() == 1;
		List<Node> cargo = leftNodes.get(0).getEdges("has-cargo");
		return cargo.size() > 0 && cargo.stream()
			.anyMatch(n -> n.hasMatchingLabelValue("goat"));
	}

	static boolean boatIsFull(final Graph state)
	{
		List<Node> boats = state.getNodes().stream()
			.filter(n -> n.hasMatchingLabelValue("boat"))
			.collect(Collectors.toList());

		assert boats.size() == 1;
		return boats.get(0).getEdges("has-cargo").size() == 3;
	}

	static boolean exactlyOneBoatExists(final Graph state)
	{
		return state.getNodes().stream()
			.filter(n -> n.hasMatchingLabelValue("boat"))
			.count() == 1;
	}

	static Graph createHostInLts()
	{
		Graph host = QueryTestUtil.createHost();

		// Use side-effects of computeLTS to add state siblings
		ReachabilityGraph.computeLTS(ImmutableSet.of(host), RulePriorityMap.ofSinglePriority(new MoveCargoRule()));
		return host;
	}

	public static Graph createHost()
	{
		Graph g = new Graph();
		Node left = g.createNode().withLabel("bank")
			.withAttr("location", "left");
		Node right = g.createNode().withLabel("bank")
			.withAttr("location", "right");

		Node boat = g.createNode().withLabel("boat");

		boat.createEdge("at", left);

		Node wolf = g.createNode().withLabel("wolf");
		Node goat = g.createNode().withLabel("goat");
		Node cabbage = g.createNode().withLabel("cabbage");

		left.createEdge("has-cargo", wolf, goat, cabbage);

		return g;
	}
}
