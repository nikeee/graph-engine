package de.uks.se.gmde.problem.ferryman;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.Rule;

import java.util.Map;

class TransferRule extends Rule
{
	TransferRule()
	{
		super("transfer", createPattern(), TransferRule::apply);
	}

	private static Graph createPattern()
	{
		final Graph pattern = new Graph();

		Node boat = pattern.createNode().withLabel("boat");
		Node cargo = pattern.createNode().withLabel("cargo");
		Node side0 = pattern.createNode().withLabel("side");
		Node side1 = pattern.createNode().withLabel("side");

		side0.createEdge("opposite", side1);
		side1.createEdge("opposite", side0);

		boat.createEdge("at", side0);
		cargo.createEdge("at", side0);

		return pattern;
	}

	private static void apply(Rule source, Graph host, Map<Node, Node> match)
	{
		Node boat = source.getPattern().getNodes().get(0);
		Node cargo = source.getPattern().getNodes().get(1);
		Node side0 = source.getPattern().getNodes().get(2);
		Node side1 = source.getPattern().getNodes().get(3);

		Node boatMatch = match.get(boat);
		Node cargoMatch = match.get(cargo);
		Node side0Match = match.get(side0);
		Node side1Match = match.get(side1);

		assert boatMatch != null;
		assert cargoMatch != null;
		assert side0Match != null;
		assert side1Match != null;

		boatMatch.removeEdge("at", side0Match);
		cargoMatch.removeEdge("at", side0Match);

		boatMatch.createEdge("at", side1Match);
		cargoMatch.createEdge("at", side1Match);
	}
}
