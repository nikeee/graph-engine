package de.uks.se.gmde.problem.ferryman;

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
			.withLabel("side")
			.withAttr("location", "left");
		Node right = res.createNode()
			.withLabel("side")
			.withAttr("location", "right");
		Node boat = res.createNode().withLabel("boat");

		Node cabbage = res.createNode()
			.withLabel("cargo")
			.withAttr("kind", "cabbage");
		Node goat = res.createNode()
			.withLabel("cargo")
			.withAttr("kind", "goat");
		Node wolf = res.createNode()
			.withLabel("cargo")
			.withAttr("kind", "wolf");

		cabbage.createEdge("at", right);
		goat.createEdge("at", right);
		wolf.createEdge("at", right);
		boat.createEdge("at", right);

		return res;
	}
}
