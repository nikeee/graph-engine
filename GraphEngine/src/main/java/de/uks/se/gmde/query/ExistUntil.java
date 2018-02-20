package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;

import java.util.*;
import java.util.function.Predicate;

public class ExistUntil implements GraphQuery<LtsPath>
{
	private final Predicate<Graph> p;
	private final Predicate<Graph> q;

	public ExistUntil(final Predicate<Graph> p, final Predicate<Graph> q)
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
		LtsPath example = LtsPath.start(state);
		boolean success = test(state, new HashSet<>(), example);
		if (!success)
			example.clear();

		assert (success && example.size() > 0) || (!success && example.size() == 0);
		return success ? Optional.of(example) : Optional.empty();
	}

	private boolean test(Graph state, Set<Graph> visited, LtsPath example)
	{
		if (visited.contains(state))
			return false;

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
				example.add(destination);

				boolean result = test(nextGraph, visited, example);
				visited.add(nextGraph);

				if (result)
					return true;

				example.remove(destination);
			}
		}

		return !hasSuccessor;
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
