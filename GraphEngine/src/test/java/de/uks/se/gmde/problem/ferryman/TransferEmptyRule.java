package de.uks.se.gmde.problem.ferryman;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.Rule;

import java.util.Map;

class TransferEmptyRule extends Rule
{
	TransferEmptyRule()
	{
		super("empty", createPattern(), TransferEmptyRule::apply);
	}

	private static Graph createPattern()
	{
		final Graph pattern = new Graph();

		Node boat = pattern.createNode().withLabel("boat");
		Node side0 = pattern.createNode().withLabel("side");
		Node side1 = pattern.createNode().withLabel("side");

		side0.createEdge("opposite", side1);
		side1.createEdge("opposite", side0);

		boat.createEdge("at", side1);

		return pattern;
	}

	private static void apply(Rule source, Graph host, Map<Node, Node> match)
	{
		Node boat = source.getPattern().getNodes().get(0);
		Node side0 = source.getPattern().getNodes().get(1);
		Node side1 = source.getPattern().getNodes().get(2);

		Node boatMatch = match.get(boat);
		Node side0Match = match.get(side0);
		Node side1Match = match.get(side1);

		assert boatMatch != null;
		assert side0Match != null;
		assert side1Match != null;

		boatMatch.removeEdge("at", side1Match);

		boatMatch.createEdge("at", side0Match);
	}
}
