package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class AlwaysNext implements GraphQuery<LtsPath>
{
	private final Predicate<Graph> p;

	public AlwaysNext(final Predicate<Graph> p)
	{
		if (p == null)
			throw new ArgumentNullException("p");

		this.p = p;
	}

	public Optional<LtsPath> test(Graph state)
	{
		Optional<Destination> counterExampleDest = testInner(state);
		return counterExampleDest.map(dest -> LtsPath.create(state, dest));
	}

	private Optional<Destination> testInner(Graph state)
	{
		for (Map.Entry<String, List<Node>> e : state.getEdges().entrySet())
		{
			final String via = e.getKey();
			for (Node n : e.getValue())
			{
				assert n instanceof Graph;
				Graph nextGraph = (Graph) n;

				if (!p.test(nextGraph))
					return Optional.of(new Destination(via, nextGraph));
			}
		}
		return Optional.empty();
	}

	public Predicate<Graph> getP()
	{
		return this.p;
	}
}
