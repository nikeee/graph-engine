package de.uks.se.gmde.problem.cannibal;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.Rule;

class FinalStateRule extends Rule
{
	FinalStateRule()
	{
		// pass null as operation (we only want to match)
		super("final", createPattern(), null);
	}

	private static Graph createPattern()
	{
		final Graph res = new Graph();

		Node left = res.createNode()
			.withLabel("bank")
			.withAttr("location", "left");

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

		cannibal0.createEdge("at", left);
		cannibal1.createEdge("at", left);
		cannibal2.createEdge("at", left);

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

		missionary0.createEdge("at", left);
		missionary1.createEdge("at", left);
		missionary2.createEdge("at", left);

		return res;
	}
}
