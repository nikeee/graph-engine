package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class ExistGlobally implements GraphQuery<LtsPath>
{
	private final Predicate<Graph> p;

	public ExistGlobally(final Predicate<Graph> p)
	{
		if (p == null)
			throw new ArgumentNullException("p");

		this.p = p;
	}

	public Optional<LtsPath> test(Graph state)
	{
		LtsPath example = LtsPath.start(state);
		boolean success = test(state, example);
		if (!success)
			example.clear();

		assert (success && example.size() > 0) || (!success && example.size() == 0);
		return success ? Optional.of(example) : Optional.empty();
	}

	private boolean test(Graph state, LtsPath example)
	{
		if (!p.test(state))
			return false;

		boolean isPathFinished = true;
		for (Map.Entry<String, List<Node>> e : state.getEdges().entrySet())
		{
			final String via = e.getKey();
			for (Node n : e.getValue())
			{
				assert n instanceof Graph;
				Graph nextGraph = (Graph) n;
				isPathFinished = false;

				Destination destination = new Destination(via, nextGraph);
				example.add(destination);

				boolean result = test(nextGraph, example);
				if (result)
					return true;

				example.remove(destination);
			}
		}
		return isPathFinished;
	}

	public Predicate<Graph> getP()
	{
		return this.p;
	}
}
