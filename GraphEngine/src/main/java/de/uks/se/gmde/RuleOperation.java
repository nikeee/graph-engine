package de.uks.se.gmde;

import java.util.Map;

@FunctionalInterface
public interface RuleOperation
{
	void apply(Rule source, Graph host, Map<Node, Node> matches);
}
