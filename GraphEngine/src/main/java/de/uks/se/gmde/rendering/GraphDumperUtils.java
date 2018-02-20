package de.uks.se.gmde.rendering;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;

import java.util.Map;

class GraphDumperUtils
{
	static void fillNodeIdMap(Graph g, Map<Node, String> nodeIdMap)
	{
		assert nodeIdMap != null;

		int counter = nodeIdMap.values().stream()
			.map(s -> Integer.parseInt(s.split("_")[1]))
			.mapToInt(i -> i).max().orElse(-1);

		for (Node n : g.getNodes())
		{
			int newId = ++counter;
			nodeIdMap.putIfAbsent(n, "node_" + newId);
		}
	}
}
