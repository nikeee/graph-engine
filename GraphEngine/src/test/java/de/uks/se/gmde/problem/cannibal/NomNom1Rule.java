package de.uks.se.gmde.problem.cannibal;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.Rule;

import java.util.Map;

class NomNom1Rule extends Rule
{
	NomNom1Rule()
	{
		super("nom-nom-1", createPattern(), NomNom1Rule::apply);
	}

	private static Graph createPattern()
	{
		final Graph res = new Graph();

		Node side0 = res.createNode()
			.withLabel("bank");
		Node side1 = res.createNode()
			.withLabel("bank");

		side1.createEdge("not-equal", side0);
		side0.createEdge("not-equal", side1);

		Node cannibal0 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "cannibal");
		Node cannibal1 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "cannibal");
		Node cannibal2 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "cannibal");

		cannibal0.createEdge("not-equal", cannibal1);
		cannibal0.createEdge("not-equal", cannibal2);

		cannibal1.createEdge("not-equal", cannibal0);
		cannibal1.createEdge("not-equal", cannibal2);

		cannibal2.createEdge("not-equal", cannibal0);
		cannibal2.createEdge("not-equal", cannibal1);

		cannibal0.createEdge("at", side0);
		cannibal1.createEdge("at", side0);
		cannibal2.createEdge("at", side0);

		Node missionary0 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "missionary"); // gets eaten

		Node missionary1 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "missionary"); // gets eaten

		Node missionary2 = res.createNode()
			.withLabel("human")
			.withAttr("kind", "missionary");

		missionary0.createEdge("not-equal", missionary1);
//		missionary0.createEdge("not-equal", missionary2);

		missionary1.createEdge("not-equal", missionary0);
//		missionary1.createEdge("not-equal", missionary2);

//		missionary2.createEdge("not-equal", missionary0);
//		missionary2.createEdge("not-equal", missionary1);

		missionary0.createEdge("at", side0);
		missionary1.createEdge("at", side0);
		missionary2.createEdge("at", side1);

		return res;
	}

	private static void apply(Rule source, Graph host, Map<Node, Node> match)
	{
		Node missionary0 = source.getPattern().getNodes().get(5);
		Node missionary1 = source.getPattern().getNodes().get(6);

		Node missionary0Match = match.get(missionary0);
		Node missionary1Match = match.get(missionary1);

		assert missionary0Match != null;
		assert missionary1Match != null;

		host.removeNode(missionary0Match);
		host.removeNode(missionary1Match);
	}
}
