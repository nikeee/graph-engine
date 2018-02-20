package de.uks.se.gmde;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class IsomorphyRule extends Rule
{
	IsomorphyRule(Graph pattern)
	{
		super("isomorphy-rule", pattern, null);
	}

	public boolean isFullMatch(final Graph host)
	{
		// Some early returns
		if (host == getPattern())
			return true;
		if (host.getNodes().size() != getPattern().getNodes().size())
			return false;

		final Set<Map<Node, Node>> allMatches = new HashSet<>(1);
		final Map<Node, Node> matches = new HashMap<>();

		super.findMatch(host, matches, allMatches, true);
		return allMatches.size() == 1;
	}
}
