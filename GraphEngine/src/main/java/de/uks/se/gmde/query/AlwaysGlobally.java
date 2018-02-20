package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;

import java.util.*;
import java.util.function.Predicate;

public class AlwaysGlobally implements GraphQuery<LtsPath>
{
	private final Predicate<Graph> p;

	public AlwaysGlobally(final Predicate<Graph> p)
	{
		if (p == null)
			throw new ArgumentNullException("p");

		this.p = p;
	}

	public Optional<LtsPath> test(Graph state)
	{
		LtsPath counterExample = LtsPath.start(state);
		boolean success = test(state, new HashSet<>(), counterExample);
		if (success)
			counterExample.clear();

		return success ? Optional.empty() : Optional.of(counterExample);
	}

	private boolean test(Graph state, Set<Graph> visited, LtsPath counterExample)
	{
		if (!p.test(state))
			return false;

		if (visited.contains(state))
			return true;

		visited.add(state);

		for (Map.Entry<String, List<Node>> e : state.getEdges().entrySet())
		{
			final String via = e.getKey();
			for (Node n : e.getValue())
			{
				assert n instanceof Graph;
				Graph nextGraph = (Graph) n;

				Destination destination = new Destination(via, nextGraph);
				counterExample.add(destination);

				boolean success = test(nextGraph, visited, counterExample);
				if (!success)
					return false;

				counterExample.remove(destination);
			}
		}

		return true;
	}

	public Predicate<Graph> getP()
	{
		return this.p;
	}
}
