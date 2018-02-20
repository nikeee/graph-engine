package de.uks.se.gmde;

import java.util.Map;

public class MoveCargoRule extends Rule
{
	public MoveCargoRule()
	{
		super("move-cargo", createPattern(), MoveCargoRule::applyRule);
	}

	private static Graph createPattern()
	{
		Graph pattern = new Graph();
		Node patternSide = pattern.createNode().withLabel("bank");
		Node patternBoat = pattern.createNode().withLabel("boat");
		Node patternThing = pattern.createNode();

		patternBoat.createEdge("at", patternSide);
		patternSide.createEdge("has-cargo", patternThing);
		return pattern;
	}

	private static void applyRule(Rule source, Graph host, Map<Node, Node> match)
	{
		Graph pattern = source.getPattern();

		Node bankPattern = pattern.getNodes().get(0);
		Node boatPattern = pattern.getNodes().get(1);
		Node thingPattern = pattern.getNodes().get(2);

		assert bankPattern != null;
		assert boatPattern != null;
		assert thingPattern != null;

		Node bankHost = match.get(bankPattern);
		Node boatHost = match.get(boatPattern);
		Node thingHost = match.get(thingPattern);

		assert boatHost != null;
		assert bankHost != null;
		assert thingHost != null;

		bankHost.removeEdge("has-cargo", thingHost);
		boatHost.createEdge("has-cargo", thingHost);
	}
}
