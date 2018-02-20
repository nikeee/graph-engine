package de.uks.se.gmde;

import com.google.common.collect.ImmutableSet;
import de.uks.se.gmde.query.QueryTestUtil;
import de.uks.se.gmde.rendering.GraphDumper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class RuleTests
{
	@Test
	public void matching()
		throws Exception
	{
		Rule rule = new MoveCargoRule();
		Graph host = QueryTestUtil.createHost();

		Set<Map<Node, Node>> matches = rule.findMatches(host);
		Assert.assertNotNull(matches);
		Assert.assertEquals(3, matches.size());

		int c = 0;
		for (Map<Node, Node> match : matches)
		{
			Assert.assertNotNull(match);
			Assert.assertEquals(3, match.size());
			GraphDumper.dumpMatch(rule.getPattern(), match, host, "dumps/matching/match-" + c++ + ".svg");
		}
	}

	@Test
	public void ruleApplication()
	{
		Rule rule = new MoveCargoRule();
		Graph host = QueryTestUtil.createHost();

		ReachabilityGraph rg = ReachabilityGraph.computeLTS(
			ImmutableSet.of(host),
			RulePriorityMap.ofSinglePriority(rule)
		);

		Assert.assertEquals(8, rg.getLTSGraph().getNodes().size());
	}
}

