package de.uks.se.gmde;

import java.util.Map;

class DoNothingRule extends Rule
{
	DoNothingRule()
	{
		super("do-nothing", createPattern(), DoNothingRule::apply);
	}

	private static Graph createPattern()
	{
		Graph g = new Graph();
		g.createNode();
		return g;
	}

	private static void apply(Rule source, Graph host, Map<Node, Node> matches)
	{
		// This function is intentionally left blank.
	}
}
