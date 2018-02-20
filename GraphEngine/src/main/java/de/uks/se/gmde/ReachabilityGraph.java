package de.uks.se.gmde;

import de.uks.se.ArgumentNullException;

import java.util.*;
import java.util.stream.Collectors;

public class ReachabilityGraph
{
	private final Graph ltsGraph;
	private final Set<Graph> startGraphs;
	private final PriorityMap<Set<Rule>> rules;

	private ReachabilityGraph(final Graph stateGraph, final Set<Graph> startGraphs, final PriorityMap<Set<Rule>> rules)
	{
		this.ltsGraph = stateGraph;
		this.startGraphs = Collections.unmodifiableSet(startGraphs);
		this.rules = rules;
	}

	public static ReachabilityGraph computeLTS(Set<Graph> startGraphs, PriorityMap<Set<Rule>> rules)
	{
		if (startGraphs == null)
			throw new ArgumentNullException("startGraphs");
		if (rules == null)
			throw new ArgumentNullException("rules");
		if (rules.size() == 0)
			throw new IllegalArgumentException("No rules provided");

		startGraphs = reduceIsomorphGraphs(startGraphs);

		Map<String, Set<Graph>> certMap = new HashMap<>(startGraphs.size());

		// use queue for breadth-first search (stack for depth-first search, priority queue for A*)
		Queue<Graph> todoQueue = new LinkedList<>();

		Graph lts = new Graph();
		for (Graph g : startGraphs)
		{
			String cert = g.computeCertificate();
			assert cert != null;
			certMap.computeIfAbsent(cert, __ -> new LinkedHashSet<>()).add(g);
			lts.addNode(g);
			todoQueue.add(g);
		}

		while (!todoQueue.isEmpty())
		{
			Graph currentGraph = todoQueue.remove();
			for (Map.Entry<Integer, Set<Rule>> priorityLevel : rules.entrySet())
			{
				boolean hasMatched = false;
				for (Rule currentRule : priorityLevel.getValue())
				{
					Set<Map<Node, Node>> matches = currentRule.findMatches(currentGraph);

					assert matches != null;
					if (matches.size() <= 0) // early return
						continue;

					hasMatched = true;

					// If we have matches and the rule is a rule with no operation
					// -> We have reached a final state (this is an "early return")
					if (currentRule.isFinalStateRule())
					{
						// Mark the currentGraph as a final state
						currentGraph.createEdge(currentRule.getName(), currentGraph);
						continue; // do not apply the rule, since there isn't anything to apply
					}

					for (Map<Node, Node> match : matches)
					{

						final Graph newGraph = currentRule.applyOperation(currentGraph, match);
						final String cert = newGraph.computeCertificate();
						assert cert != null;

						Set<Graph> graphsOfCert = certMap.get(cert);
						assert graphsOfCert == null || graphsOfCert.size() > 0;
						Optional<Graph> isomorphGraph = graphsOfCert == null
							? Optional.empty()
							: graphsOfCert.stream()
							.filter(newGraph::isIsomorphTo)
							.findFirst();

						if (isomorphGraph.isPresent())
						{
							// We have an isomorph graph, just draw an edge between the existing ones
							currentGraph.createEdge(currentRule.getName(), isomorphGraph.get());
						}
						else
						{
							// We have a new state/graph
							Node ltsNode = lts.addNode(newGraph);
							currentGraph.createEdge(currentRule.getName(), ltsNode);
							certMap.computeIfAbsent(cert, __ -> new LinkedHashSet<>()).add(newGraph);

							boolean isNewToToDo = todoQueue.stream()
								.noneMatch(g -> cert.equals(g.computeCertificate()) && newGraph.isIsomorphTo(g));
							if (isNewToToDo)
							{
								todoQueue.add(newGraph);
							}
						}
					}
				}

				// if one of the rules of the last priority matched,
				// we do not process the rules with a lower priority (just like groove)
				if (hasMatched)
					break;
			}
		}
		return new ReachabilityGraph(lts, startGraphs, rules);
	}

	/**
	 * Filters out graphs that are isomorph, so there are no identical start graphs.
	 */
	private static Set<Graph> reduceIsomorphGraphs(Set<Graph> graphs)
	{
		assert graphs != null;

		if (graphs.size() <= 1)
			return graphs;

		Map<String, List<Graph>> certToGraph = graphs.stream()
			.collect(Collectors.groupingBy(Graph::computeCertificate));

		// Isomorph graphs have the same certificate, so they are stored in the same key
		// -> If certs are unequal, they cannot be isomorph

		Map<String, Set<Graph>> nonIsomorphGraphs = certToGraph.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> {
					Set<Graph> result = new HashSet<>(e.getValue().size());

					for (Graph g : e.getValue())
					{
						if (result.contains(g))
							continue;

						boolean foundIsomorph = false;
						for (Graph r : result)
						{
							if (g.isIsomorphTo(r))
							{
								foundIsomorph = true;
								break;
							}
						}
						if (!foundIsomorph)
							result.add(g);
					}
					return result;
				}
			));

		Collection<Set<Graph>> values = nonIsomorphGraphs.values();
		Set<Graph> result = new HashSet<>(values.size());
		for (Set<Graph> value : values)
			result.addAll(value);

		return Collections.unmodifiableSet(result);
	}

	public Graph getLTSGraph()
	{
		return ltsGraph;
	}

	public PriorityMap<Set<Rule>> getRules()
	{
		return rules;
	}

	public Set<Graph> getStartGraphs()
	{
		return startGraphs;
	}
}
