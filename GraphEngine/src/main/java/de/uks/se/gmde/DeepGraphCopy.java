package de.uks.se.gmde;

import de.uks.se.ArgumentNullException;

import java.util.Map;

class DeepGraphCopy
{
	public final Graph graph;
	public final Map<Node, Node> originalToClone;

	DeepGraphCopy(Graph graph, Map<Node, Node> originalToClone)
	{
		if (graph == null)
			throw new ArgumentNullException("graph");
		if (originalToClone == null)
			throw new ArgumentNullException("originalToClone");

		this.graph = graph;
		this.originalToClone = originalToClone;
	}
}
