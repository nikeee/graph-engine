package de.uks.se.gmde;

import com.google.common.collect.ImmutableSet;
import de.uks.se.ArgumentNullException;

import java.util.Set;
import java.util.TreeMap;

public class RulePriorityMap extends TreeMap<Integer, Set<Rule>> implements PriorityMap<Set<Rule>>
{
	public static RulePriorityMap ofSinglePriority(final Rule... rules)
	{
		final RulePriorityMap res = new RulePriorityMap();
		res.put(0, ImmutableSet.copyOf(rules));
		return res;
	}

	@SafeVarargs
	public static RulePriorityMap ofArray(final Set<Rule>... rules)
	{
		if (rules == null)
			throw new ArgumentNullException("rules");

		final RulePriorityMap res = new RulePriorityMap();
		for (int i = 0; i < rules.length; ++i)
			res.put(i, rules[i]);
		return res;
	}
}
