package de.uks.se.gmde;

import de.uks.se.ArgumentNullException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GraphStats
{
	private Map<String, Map<String, Map<String, Float>>> stats;

	private GraphStats(Map<String, Map<String, Map<String, Float>>> stats)
	{
		this.stats = stats == null
			? Collections.emptyMap()
			: Collections.unmodifiableMap(stats);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		stats.forEach((nodeAttribute, value) -> {
			value.forEach((attributeValue, edgeAverages) -> {
				edgeAverages.forEach((edgeLabel, average) -> {
					sb.append(nodeAttribute).append("=");
					sb.append(attributeValue).append(": ");
					sb.append("avg(").append(edgeLabel).append(")=");
					sb.append(average).append("\n");
				});
			});
		});

		return sb.toString();
	}

	public static GraphStats createFrom(final Graph... graphs)
	{
		if (graphs == null)
			throw new ArgumentNullException("graphs");
		if (graphs.length == 0)
			throw new IllegalArgumentException("No graphs provided");

		Map<String, Map<String, Map<String, Integer>>> counts = new HashMap<>();
		Map<String, Map<String, Integer>> occurrences = new HashMap<>();
		for (Graph g : graphs)
		{
			if (g != null)
				collectGraphStats(g, counts, occurrences);
		}

		Map<String, Map<String, Map<String, Float>>> result = new HashMap<>(counts.size());
		counts.forEach((attrKey, attrKeyStats) -> {
			Map<String, Integer> attrKeyOc = occurrences.get(attrKey);
			Map<String, Map<String, Float>> resultAttrKey = result.computeIfAbsent(attrKey, __ -> new HashMap<>(attrKeyStats.size()));

			assert attrKeyOc != null;
			attrKeyStats.forEach((attrValue, attrValueStats) -> {

				Integer attrValueCountNullable = attrKeyOc.get(attrValue);
				Map<String, Float> resultAttrValue = resultAttrKey.computeIfAbsent(attrValue, __ -> new HashMap<>(attrValueStats.size()));

				assert attrValueCountNullable != null;

				final int attrValueCount = attrValueCountNullable;
				attrValueStats.forEach((edgeLabel, edgeLabelCount) -> {
					resultAttrValue.put(edgeLabel, edgeLabelCount / (float) attrValueCount);
				});
			});
		});

		return new GraphStats(result);
	}

	private static void collectGraphStats(final Graph graph, final Map<String, Map<String, Map<String, Integer>>> counts, final Map<String, Map<String, Integer>> occurrences)
	{
		if (graph == null)
			return;
		assert counts != null;
		assert occurrences != null;

		for (Node node : graph.getNodes())
		{
			assert node != null;
			collectNodeStats(node, counts, occurrences);
		}
	}

	private static void collectNodeStats(final Node node, final Map<String, Map<String, Map<String, Integer>>> counts, final Map<String, Map<String, Integer>> occurrences)
	{
		if (node == null)
			return;
		assert counts != null;
		assert occurrences != null;

		node.getAttributes().forEach((key, value) -> {

			Map<String, Map<String, Integer>> valueEdgeLabelCount = counts.computeIfAbsent(key, __ -> new HashMap<>());
			Map<String, Integer> oc = occurrences.computeIfAbsent(key, __ -> new HashMap<>());
			collectAttributeValueStats(node, value, valueEdgeLabelCount, oc);
		});
	}

	private static void collectAttributeValueStats(final Node node, final Object av, final Map<String, Map<String, Integer>> counts, final Map<String, Integer> occurrences)
	{
		if (av == null)
			return;
		assert counts != null;
		assert occurrences != null;

		final String avValue = av.toString();

		Map<String, Integer> edgeLabelCount = counts.computeIfAbsent(avValue, __ -> new HashMap<>());
		occurrences.merge(avValue, 1, Integer::sum);

		node.getEdges().forEach((edgeLabel, nodes) -> {
			edgeLabelCount.merge(edgeLabel, nodes.size(), Integer::sum);
		});
	}
}
