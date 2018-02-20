package de.uks.se.gmde;

import de.uks.se.ArgumentNullException;

import java.util.*;
import java.util.stream.Collectors;

public class Graph extends Node
{
	// We use List<Node> for convenience (so we can access via index)
	// We ensure that every element is only once in this list by checking manually.
	private List<Node> nodes = new ArrayList<>();

	private Map<Object, Set<Node>> labelValueIndex;

	/**
	 * Creates a new instance of a node and adds it to the graph's nodes.
	 *
	 * @return An instance to the created node.
	 */
	public Node createNode()
	{
		return addNode(new Node());
	}

	public Node addNode(final Node node)
	{
		if (node == null)
			throw new ArgumentNullException("node");
		nodes.add(node);
		return node;
	}

	/**
	 * Removes a node from the graph
	 *
	 * @param toRemove The node to remove
	 */
	public void removeNode(final Node toRemove)
	{
		if (toRemove == null)
			throw new ArgumentNullException("toRemove");

		nodes.remove(toRemove);

		toRemove.getEdges().forEach((label, oldTargets) -> {
			final List<Node> targets = new ArrayList<>(oldTargets);
			for (Node t : targets)
				toRemove.removeEdge(label, t);
		});

		toRemove.getIncomingEdges().forEach((label, oldTargets) -> {
			final List<Node> sources = new ArrayList<>(oldTargets);
			for (Node t : sources)
				t.removeEdge(label, toRemove);
		});
	}

	/**
	 * Returns a list of all nodes
	 * We use List&lt;Node&gt; for convenience (so we can access via index)
	 *
	 * @return A list of all nodes
	 */
	public List<Node> getNodes()
	{
		return nodes == null || nodes.size() == 0
			? Collections.emptyList()
			: Collections.unmodifiableList(nodes);
	}

	/**
	 * Rebuilds the index of the map, mapping LabelValues -> Set of Nodes
	 */
	public void rebuildLabelIndex()
	{
		labelValueIndex = createLabelIndex();
	}

	private Map<Object, Set<Node>> createLabelIndex()
	{
		final List<Node> allNodes = getNodes();
		if (allNodes.size() == 0)
			return Collections.emptyMap();

		// Basically, we just want a "GROUP BY Node.label"
		// We can just use the Java stream API for that.
		return allNodes.stream()
			.filter(Objects::nonNull)
			.filter(n -> n.getAttribute(Node.LABEL_ATTRIBUTE_NAME).isPresent())
			.collect(Collectors.groupingBy(
				n -> n.getAttribute(Node.LABEL_ATTRIBUTE_NAME).get(), // the .get() is safe here (but not thread-safe)
				Collectors.mapping(n -> n, Collectors.toSet())
			));
	}

	public Set<Node> getNodesWithLabelValue(final Object labelValue)
	{
		if (labelValue == null)
			throw new ArgumentNullException("labelValue");

		if (labelValueIndex == null)
			rebuildLabelIndex();
		assert labelValueIndex != null;

		Set<Node> result = labelValueIndex.get(labelValue);
		if (result == null || result.size() <= 0)
			return Collections.emptySet();
		return result;
	}

	/**
	 * Creates a deep copy of a graph (nodes, edges etc are also copied).
	 * Also returns a map which maps the original nodes to their respective copies.
	 * Node attributes are not deeply-copied.
	 */
	public DeepGraphCopy copyDeep()
	{
		final Graph graphCopy = new Graph();
		final Map<Node, Node> originalToClone = new HashMap<>(getNodes().size());

		// Copy all nodes of this (including the attributes)
		for (Node original : getNodes())
		{
			final Node nodeCopy = graphCopy.createNode();
			original.getAttributes().forEach(nodeCopy::withAttr);
			originalToClone.put(original, nodeCopy);
		}

		// After we copied all nodes, we can create edges between them
		for (Node original : getNodes())
		{
			final Node nodeCopy = originalToClone.get(original);
			assert nodeCopy != null;
			original.getEdges().forEach((label, originalTargets) -> {
				Node[] copyTargets = originalTargets.stream()
					.map(originalToClone::get)
					.toArray(Node[]::new);
				nodeCopy.createEdge(label, copyTargets);
			});
		}

		return new DeepGraphCopy(graphCopy, originalToClone);
	}

	public boolean isIsomorphTo(Graph other)
	{
		if (other == null)
			return false;
		// If this and other point to the same object, they must be isomorph
		// We can safely avoid the isomorphy check
		if (this == other)
			return true;

		// We assume that the certificates are equal
		// (if they are not, this check is redundant)
		final IsomorphyRule r = new IsomorphyRule(this);
		return r.isFullMatch(other);
	}

	@Override
	public String toString()
	{
		return this.nodes.stream()
			.map(Node::toString)
			.collect(Collectors.joining("\n"));
	}

	public String computeCertificate()
	{
		final Map<Node, Integer> nodeNumberMap = new HashMap<>();

		final SortedMap<String, Set<Node>> certNodeMap = new TreeMap<>();
		final SortedMap<String, Integer> certNumberMap = new TreeMap<>();

		final List<Node> nodes = this.getNodes();
		for (Node n : nodes)
		{
			String cert0 = n.computeAttributeCertificate();
			certNodeMap.computeIfAbsent(cert0, __ -> new LinkedHashSet<>()).add(n);
		}

		numberCerts(nodeNumberMap, certNodeMap, certNumberMap);

		while (true)
		{
			int oldCertCount = certNodeMap.size();
			certNodeMap.clear();

			for (Node node : nodes)
			{
				String newCert = node.computeCertificate(nodeNumberMap);
				certNodeMap.computeIfAbsent(newCert, __ -> new LinkedHashSet<>()).add(node);
			}

			int newCertCount = certNodeMap.size();
			numberCerts(nodeNumberMap, certNodeMap, certNumberMap);

			if (newCertCount <= oldCertCount || newCertCount == nodes.size())
				break;
		}

		// Create final graph certificate

		StringBuilder sb = new StringBuilder();
		certNodeMap.forEach((cert, nodeSet) -> {
			sb
				.append(certNumberMap.get(cert))
				.append(" * ")
				.append(nodeSet.size())
				.append('\n');
		});

		certNumberMap.forEach((cert, number) -> {
			sb.append(number).append(": ").append(cert).append('\n');
		});

		return sb.toString();
	}

	private void numberCerts(
		Map<Node, Integer> nodeNumberMap,
		SortedMap<String, Set<Node>> certNodeMap,
		SortedMap<String, Integer> certNumberMap
	)
	{
		certNodeMap.forEach((cert, nodeSet) -> {
			int numberOfCerts = certNumberMap.size() + 1;
			certNumberMap.put(cert, numberOfCerts);

			nodeSet.forEach(n -> nodeNumberMap.put(n, numberOfCerts));
		});
	}
}

