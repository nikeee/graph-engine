package de.uks.se.gmde.problem.ferryman;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.Rule;

import java.util.Map;

class NomNomRule extends Rule
{
	NomNomRule()
	{
		super("nom-nom", createPattern(), NomNomRule::apply);
	}

	private static Graph createPattern()
	{
		final Graph pattern = new Graph();

		Node cargo0 = pattern.createNode().withLabel("cargo");
		Node cargo1 = pattern.createNode().withLabel("cargo"); // gets eaten

		Node side = pattern.createNode().withLabel("side");

		cargo0.createEdge("eats", cargo1);

		cargo0.createEdge("at", side);
		cargo1.createEdge("at", side);

		Node otherSide = pattern.createNode().withLabel("side");
		side.createEdge("opposite", otherSide);

		Node boat = pattern.createNode().withLabel("boat");
		boat.createEdge("at", otherSide);

		return pattern;
	}

	private static void apply(Rule source, Graph host, Map<Node, Node> match)
	{
		Node cargo1Pattern = source.getPattern().getNodes().get(1);

		Node cargo1Match = match.get(cargo1Pattern);
		assert cargo1Match != null;

		host.removeNode(cargo1Match);
	}
}
