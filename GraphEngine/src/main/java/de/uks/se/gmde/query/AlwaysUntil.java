package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;

import java.util.*;
import java.util.function.Predicate;

public class AlwaysUntil implements GraphQuery<LtsPath>
{
	private final Predicate<Graph> p;
	private final Predicate<Graph> q;

	public AlwaysUntil(final Predicate<Graph> p, final Predicate<Graph> q)
	{
		if (p == null)
			throw new ArgumentNullException("p");
		if (q == null)
			throw new ArgumentNullException("q");

		this.p = p;
		this.q = q;
	}

	public Optional<LtsPath> test(Graph state)
	{
		LtsPath counterExample = LtsPath.start(state);
		boolean success = test(state, new HashSet<>(), new HashSet<>(), counterExample);
		if (success)
			counterExample.clear();

		assert (success && counterExample.size() == 0) || (!success && counterExample.size() > 0);
		return success ? Optional.empty() : Optional.of(counterExample);
	}

	private boolean test(Graph state, Set<Graph> visited, Set<Graph> successStates, LtsPath counterExample)
	{
		if (visited.contains(state))
			return successStates.contains(state);

		if (q.test(state))
			return true;

		if (!p.test(state))
			return false;

		visited.add(state);

		boolean hasSuccessor = false;
		for (Map.Entry<String, List<Node>> e : state.getEdges().entrySet())
		{
			final String via = e.getKey();
			for (Node n : e.getValue())
			{
				assert n instanceof Graph;
				Graph nextGraph = (Graph) n;

				hasSuccessor = true;

				Destination destination = new Destination(via, nextGraph);
				counterExample.add(destination);

				boolean result = test(nextGraph, visited, successStates, counterExample);
				if (!result)
					return false;

				successStates.add(nextGraph);
				counterExample.remove(destination);
			}
		}

		// Return whether the current state has a following sate
		return hasSuccessor;
	}

	public Predicate<Graph> getP()
	{
		return this.p;
	}

	public Predicate<Graph> getQ()
	{
		return this.q;
	}
}
