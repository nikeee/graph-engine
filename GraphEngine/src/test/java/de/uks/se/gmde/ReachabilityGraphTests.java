package de.uks.se.gmde;

import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

public class ReachabilityGraphTests
{
	@Test
	public void isomorphyReduction()
	{
		// Test that isomorph graphs are filtered out

		Graph start1 = new Graph();
		start1.createNode().withLabel("hallo");

		Graph start2 = new Graph();
		start2.createNode().withLabel("hallo");

		ReachabilityGraph lts = ReachabilityGraph.computeLTS(
			ImmutableSet.of(start1, start2),
			RulePriorityMap.ofSinglePriority(new DoNothingRule())
		);

		Assert.assertNotNull(lts);
		Assert.assertEquals(1, lts.getLTSGraph().getNodes().size());
		Assert.assertEquals(1, lts.getStartGraphs().size());
		Assert.assertEquals(1, lts.getRules().size());
	}
}
