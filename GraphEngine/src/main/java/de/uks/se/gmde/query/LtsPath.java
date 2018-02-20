package de.uks.se.gmde.query;

import de.uks.se.gmde.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class LtsPath extends ArrayList<Destination>
{
	public Optional<Graph> getStart()
	{
		return this.size() > 0
			? Optional.ofNullable(this.get(0).destination)
			: Optional.empty();
	}

	public Optional<Graph> getEnd()
	{
		return getLastOperation().map(e -> e.destination);
	}

	public Optional<Destination> getLastOperation()
	{
		return this.size() > 0
			? Optional.ofNullable(this.get(this.size() - 1))
			: Optional.empty();
	}

	public Set<Graph> getHops()
	{
		return this.stream()
			.map(e -> e.destination)
			.collect(Collectors.toSet());
	}

	/**
	 * Not thread-safe getter for start and end graphs/nodes.
	 */
	public Set<Graph> getStartAndEnd()
	{
		Set<Graph> startEndNodes = new HashSet<>();
		if (getStart().isPresent())
			startEndNodes.add(getStart().get());
		if (getEnd().isPresent())
			startEndNodes.add(getEnd().get());
		return Collections.unmodifiableSet(startEndNodes);
	}

	public static LtsPath start(Graph startNode)
	{
		LtsPath res = new LtsPath();
		res.add(new Destination(null, startNode));
		return res;
	}

	public static LtsPath create(Graph startNode, Destination... destinations)
	{
		LtsPath path = LtsPath.start(startNode);
		path.addAll(Arrays.asList(destinations));
		return path;
	}
}

