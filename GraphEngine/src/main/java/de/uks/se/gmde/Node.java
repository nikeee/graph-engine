package de.uks.se.gmde;

import de.uks.se.ArgumentNullException;
import de.uks.se.StringEx;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Non-Thread-Safe mutable class representing a node.
 */
public class Node
{
	public static final String LABEL_ATTRIBUTE_NAME = "label";

	private Map<String, Object> attributes;
	private Map<String, List<Node>> edges;
	private Map<String, List<Node>> incomingEdges;

	/**
	 * Returns a map of incoming edges, mapping EdgeLabel -> {Node}
	 * We use List&lt;Node&gt; for convenience.
	 *
	 * @return A map of incoming edges.
	 */
	public Map<String, List<Node>> getIncomingEdges()
	{
		return incomingEdges == null
			? Collections.emptyMap()
			: Collections.unmodifiableMap(incomingEdges);
	}

	/**
	 * Returns a map of outgoing edges, mapping EdgeLabel -> {Node}
	 * We use List&lt;Node&gt; for convenience.
	 *
	 * @return A map of outgoing edges.
	 */
	public Map<String, List<Node>> getEdges()
	{
		return edges == null
			? Collections.emptyMap()
			: Collections.unmodifiableMap(edges);
	}

	/**
	 * Returns a list of nodes that are connected via a specified label of an outgoing edge.
	 * We use List&lt;Node&gt; for convenience.
	 *
	 * @return A list of nodes that are connected via a specified label of an outgoing edge.
	 */
	public List<Node> getEdges(final String label)
	{
		if (label == null)
			throw new ArgumentNullException("label");

		final Map<String, List<Node>> myEdges = this.getEdges();
		final List<Node> arrayList = myEdges.get(label);

		if (arrayList == null)
			return Collections.emptyList();

		return Collections.unmodifiableList(arrayList);
	}

	/**
	 * Returns a list of nodes that are connected via a specified label of an incoming edge.
	 * We use List&lt;Node&gt; for convenience.
	 *
	 * @return A list of nodes that are connected via a specified label of an incoming edge.
	 */
	public List<Node> getIncomingEdges(final String key)
	{
		if (key == null)
			throw new ArgumentNullException("key");

		final Map<String, List<Node>> myEdges = this.getIncomingEdges();
		final List<Node> arrayList = myEdges.get(key);

		if (arrayList == null)
			return Collections.emptyList();

		return Collections.unmodifiableList(arrayList);
	}

	/**
	 * Sets an attribute of the node and returns itself.
	 *
	 * @param key   The attribute label
	 * @param value The value. If value is null, the attribute is removed.
	 * @return this
	 */
	public Node withAttr(final String key, final Object value)
	{
		if (key == null)
			throw new ArgumentNullException("key");

		if (attributes == null)
			attributes = new LinkedHashMap<>();

		if (value == null)
		{
			attributes.remove(key);
		}
		else
		{
			attributes.put(key, value);
		}

		return this;
	}

	public Node withLabel(final Object value)
	{
		return withAttr(LABEL_ATTRIBUTE_NAME, value);
	}

	/**
	 * Creates a new edge with one or more nodes as targets
	 *
	 * @param key        The edge label
	 * @param newTargets The targets
	 */
	public void createEdge(String key, Node... newTargets)
	{
		if (key == null)
			throw new ArgumentNullException("key");
		if (newTargets == null || newTargets.length == 0)
			return;

		if (edges == null)
			edges = new LinkedHashMap<>();

		List<Node> targets = edges
			.computeIfAbsent(key, k -> new ArrayList<>());

		for (Node t : newTargets)
		{
			if (!targets.contains(t))
			{
				targets.add(t);
				t.createIncomingEdge(key, this);
			}
		}
	}

	private void createIncomingEdge(final String key, final Node node)
	{
		if (key == null)
			throw new ArgumentNullException("key");
		if (node == null)
			throw new ArgumentNullException("node");

		if (incomingEdges == null)
			incomingEdges = new LinkedHashMap<>();

		List<Node> list = incomingEdges
			.computeIfAbsent(key, k -> new ArrayList<>());

		list.add(node);
	}

	public void removeEdge(String key, Node node)
	{
		if (key == null)
			throw new ArgumentNullException("key");
		if (node == null)
			throw new ArgumentNullException("node");

		final Map<String, List<Node>> myEdges = getEdges();
		final List<Node> list = myEdges.get(key);

		list.remove(node);
		node.removeIncomingEdge(key, this);
	}

	private void removeIncomingEdge(String key, Node node)
	{
		if (key == null)
			throw new ArgumentNullException("key");
		if (node == null)
			throw new ArgumentNullException("node");

		final Map<String, List<Node>> myEdges = getIncomingEdges();
		final List<Node> list = myEdges.get(key);

		list.remove(node);
	}

	public Optional<Object> getAttribute(String key)
	{
		if (key == null)
			throw new ArgumentNullException("key");

		return attributes == null
			? Optional.empty()
			: Optional.ofNullable(attributes.get(key));
	}

	public Map<String, Object> getAttributes()
	{
		return attributes == null
			? Collections.emptyMap()
			: Collections.unmodifiableMap(attributes);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		getAttributes().forEach((key, value) -> {
			sb.append(key).append("=").append(StringEx.escapeNullString(value));
			sb.append("\n");
		});

		getEdges().forEach((key, value) -> {
			sb.append(key).append("->").append(value == this ? "this" : "other");
			sb.append("\n");
		});

		return sb.toString().trim();
	}

	public boolean attributesAreSubsetOf(final Node other)
	{
		if (other == null)
			return false;

		final Map<String, Object> attrs = getAttributes();

		return attrs.entrySet().stream()
			.allMatch(e -> {
				final String attrName = e.getKey();
				final Object value = e.getValue();
				// a value cannot be null, since setting an attribute to null removes it from the map
				// -> no check for value == null required
				final Optional<Object> attr = other.getAttribute(attrName);

				assert value != null;
				return attr.isPresent() && attr.get().equals(value);
			});
	}

	public boolean hasMatchingLabelValue(final Object labelValue)
	{
		assert labelValue != null;
		return getAttribute(LABEL_ATTRIBUTE_NAME)
			.map(labelValue::equals)
			.orElse(false);
	}

	public String computeAttributeCertificate()
	{
		return "N:\n" +
			getAttributes().entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry::getKey))
				.map(e -> "\t" + e.getKey() + ":" + e.getValue())
				.collect(Collectors.joining("\n"));
	}

	public int getEdgeTypeCount()
	{
		return getTypeCount(edges);
	}

	public int getIncomingEdgeTypeCount()
	{
		return getTypeCount(incomingEdges);
	}

	/**
	 * This function is needed because we cannot compare getEdges().size() to determine
	 * that two nodes have the same number of edge types.
	 * The problem is that if an edge is removed, the key of the map is not removed
	 * (so the key of the map holds an empty list).
	 * We cannot remove the key on edge removal because we get some concurrent modification exceptions.
	 *
	 * @return The number of Map keys that map to a list that's not null and not empty
	 */
	private int getTypeCount(Map<String, List<Node>> map)
	{
		if (map == null)
			return 0;

		int count = 0;
		for (Map.Entry<String, List<Node>> entry : map.entrySet())
		{
			if (entry.getValue() == null && !entry.getValue().isEmpty())
				count++;
		}
		return count;
	}

	String computeCertificate(Map<Node, Integer> nodeNumberMap)
	{
		final int lineCount = getEdges().size() + getIncomingEdges().size();

		List<String> lines = new ArrayList<>(lineCount);
		final String inPrefix = "\tin ";

		getIncomingEdges().forEach((label, targets) -> {
			String prefix = inPrefix + label;
			for (Node target : targets)
				lines.add(prefix + ": " + nodeNumberMap.get(target));
		});

		final String outPrefix = "\tout ";
		getEdges().forEach((label, targets) -> {
			String prefix = outPrefix + label;
			for (Node target : targets)
				lines.add(prefix + ": " + nodeNumberMap.get(target));
		});

		lines.sort(String::compareTo);
		return nodeNumberMap.get(this) + ":\n" + String.join("\n", lines);
	}
}
