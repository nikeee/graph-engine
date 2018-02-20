package de.uks.se.gmde;

import com.google.common.collect.ImmutableSet;
import de.uks.se.ArgumentNullException;

import java.util.*;
import java.util.stream.Collectors;

public class Rule
{
	private final String name;
	private final Graph pattern;
	private final RuleOperation operation;

	public Rule(final String name, final Graph pattern, final /* @Nullable */ RuleOperation operation)
	{
		if (name == null)
			throw new ArgumentNullException("name");
		if (pattern == null)
			throw new ArgumentNullException("pattern");

		this.name = name;
		this.pattern = pattern;
		this.operation = operation;
	}

	public Graph applyOperation(final Graph host, final Map<Node, Node> match)
	{
		if (host == null)
			throw new ArgumentNullException("host");
		if (match == null)
			throw new ArgumentNullException("match");

		if (operation == null)
			throw new IllegalStateException("Attempt to apply a final state matching rule");

		DeepGraphCopy copy = host.copyDeep();

		// create defensive copy of the host nodes
		// (operation.accepts mutates the input parameters)
		Map<Node, Node> matchCopy = match.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				m -> copy.originalToClone.get(m.getValue())
			));

		operation.apply(this, copy.graph, matchCopy);

		// we return the deep copy of the input graph because "operation.accept" mutated its nodes
		return copy.graph;
	}

	static class SearchOperation
	{
		final Node patternNode;
		final Set<Node> hostCandidates;

		SearchOperation(Node patternNode, Set<Node> hostCandidates)
		{
			assert patternNode != null;
			assert hostCandidates != null;

			this.patternNode = patternNode;
			this.hostCandidates = hostCandidates;
		}
	}

	private Optional<SearchOperation> getNextSearchOperation(final Graph host, Map<Node, Node> matches)
	{
		SearchOperation result = null;
		for (Node patternNode : pattern.getNodes())
		{
			final Node hostNode = matches.get(patternNode);
			if (hostNode == null)
			{
				// is there a label attribute?
				// if yes -> get all nodes with the same label value
				// if no  -> just take all
				Optional<Object> label = patternNode.getAttribute(Node.LABEL_ATTRIBUTE_NAME);
				Set<Node> hostCandidates = label
					.map(host::getNodesWithLabelValue)
					.orElseGet(() -> new HashSet<>(host.getNodes()));

				result = result == null || hostCandidates.size() < result.hostCandidates.size()
					? new SearchOperation(patternNode, hostCandidates)
					: result;
			}
			else
			{
				// patternNode has a match
				// check whether there is an edge to an unmatched node
				for (Map.Entry<String, List<Node>> entry : patternNode.getEdges().entrySet())
				{
					for (Node targetPatternNode : entry.getValue())
					{
						final Node targetHostNode = matches.get(targetPatternNode);

						if (targetHostNode == null)
						{
							List<Node> hostCandidates = hostNode.getEdges(entry.getKey());
							result = result == null || hostCandidates.size() < result.hostCandidates.size()
								? new SearchOperation(targetPatternNode, ImmutableSet.copyOf(hostCandidates))
								: result;
						}
					}
				}

				for (Map.Entry<String, List<Node>> entry : patternNode.getIncomingEdges().entrySet())
				{
					for (Node targetPatternNode : entry.getValue())
					{
						final Node targetHostNode = matches.get(targetPatternNode);

						if (targetHostNode == null)
						{
							List<Node> hostCandidates = hostNode.getIncomingEdges(entry.getKey());
							result = result == null || hostCandidates.size() < result.hostCandidates.size()
								? new SearchOperation(targetPatternNode, ImmutableSet.copyOf(hostCandidates))
								: result;
						}
					}
				}
			}
		}
		return Optional.ofNullable(result);
	}

	public Set<Map<Node, Node>> findMatches(final Graph host)
	{
		if (host == null)
			throw new ArgumentNullException("host");

		host.rebuildLabelIndex();

		final Set<Map<Node, Node>> allMatches = new HashSet<>();
		final Map<Node, Node> matches = new HashMap<>();

		findMatch(host, matches, allMatches, false);

		return Collections.unmodifiableSet(allMatches);
	}

	protected boolean findMatch(final Graph host, Map<Node, Node> matches, Set<Map<Node, Node>> allMatches, final boolean fullMatch)
	{
		assert host != null;
		assert matches != null;
		assert allMatches != null;

		final Optional<SearchOperation> currentOp = getNextSearchOperation(host, matches);
		if (!currentOp.isPresent())
		{
			Map<Node, Node> completeMatch = new HashMap<>(matches);
			allMatches.add(completeMatch); // We're finished, add stuff

			// if fullMatch is what we want, we don't want to look out for other stuff
			return fullMatch;
		}

		final SearchOperation op = currentOp.get();

		hgNodeLoop:
		for (Node hgNode : op.hostCandidates)
		{
			// this node does not match entirely, try the next one
			if (fullMatch && op.patternNode.getAttributes().size() != hgNode.getAttributes().size())
				continue;

			// Check if the attributes are equal
			boolean allAttrsMatch = op.patternNode.attributesAreSubsetOf(hgNode);
			if (!allAttrsMatch)
				continue;

			// this node does not match entirely, try the next one
			if (fullMatch && op.patternNode.getEdgeTypeCount() != hgNode.getEdgeTypeCount())
				continue;

			for (Map.Entry<String, List<Node>> patternEntry : op.patternNode.getEdges().entrySet())
			{
				String patternEdgeLabel = patternEntry.getKey();
				List<Node> targets = patternEntry.getValue();

				// this node does not match entirely, try the next one
				if (fullMatch)
				{
					// Special case: A list can be not present or empty
					// In both cases, we don't have any edges, so these must be treated equally.
					List<Node> n = hgNode.getEdges().get(patternEdgeLabel);
					int size = n == null ? 0 : n.size();
					if (targets.size() != size)
						continue hgNodeLoop;
				}

				for (Node patternTarget : targets)
				{
					Node hostNode = matches.get(patternTarget);
					if (hostNode == null)
						continue;
					boolean contains = hgNode.getEdges(patternEdgeLabel).contains(hostNode);
					if (!contains)
						continue hgNodeLoop;
				}
			}

			// this node does not match entirely, try the next one
			if (fullMatch && op.patternNode.getIncomingEdgeTypeCount() != hgNode.getIncomingEdgeTypeCount())
				continue;

			for (Map.Entry<String, List<Node>> patternEntry : op.patternNode.getIncomingEdges().entrySet())
			{
				String patternEdgeLabel = patternEntry.getKey();
				List<Node> targets = patternEntry.getValue();

				// this node does not match entirely, try the next one
				if (fullMatch)
				{
					// Special case: A list can be not present or empty
					// In both cases, we don't have any edges, so these must be treated equally.
					List<Node> n = hgNode.getIncomingEdges().get(patternEdgeLabel);
					int size = n == null ? 0 : n.size();
					if (targets.size() != size)
						continue hgNodeLoop;
				}

				for (Node patternTarget : targets)
				{
					Node hostNode = matches.get(patternTarget);
					if (hostNode == null)
						continue;
					boolean contains = hgNode.getIncomingEdges(patternEdgeLabel).contains(hostNode);
					if (!contains)
						continue hgNodeLoop;
				}
			}

			// It is a good candidate
			matches.put(op.patternNode, hgNode);

			boolean found = findMatch(host, matches, allMatches, fullMatch);
			if (found)
				return true;

			// it actually was a bad candidate, so not an actual match
			matches.remove(op.patternNode);
		}
		return false;
	}

	public String getName()
	{
		return name;
	}

	public Graph getPattern()
	{
		return pattern;
	}

	public boolean isFinalStateRule()
	{
		return operation == null;
	}
}
