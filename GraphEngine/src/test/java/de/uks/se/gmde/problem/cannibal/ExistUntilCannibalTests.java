package de.uks.se.gmde.problem.cannibal;

import de.uks.se.Tuple;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.ReachabilityGraph;
import de.uks.se.gmde.query.ExistUntil;
import de.uks.se.gmde.query.LtsPath;
import de.uks.se.gmde.rendering.GraphDumper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

public class ExistUntilCannibalTests
{
	@Test
	public void cannibalSolutionExists()
		throws IOException
	{
		Tuple<ReachabilityGraph, Graph> s = CannibalTest.createLtsHost();
		Graph cannibalLtsStart = s.item1;

		ExistUntil solutionPossible = new ExistUntil(this::allPeopleAlive, this::problemSolved);

		Optional<LtsPath> solution = solutionPossible.test(cannibalLtsStart);
		Assert.assertNotNull(solution);
		Assert.assertTrue(solution.isPresent());

		LtsPath path = solution.get();
		Assert.assertNotNull(path);
		Assert.assertEquals(cannibalLtsStart, path.getStart().get());
		Assert.assertEquals(6, humansOnSide(path.getStart().get(), "right"));
		Assert.assertEquals(6, humansOnSide(path.getEnd().get(), "left"));
		Assert.assertTrue(problemSolved(path.getEnd().get()));
		GraphDumper.dumpLtsPath(s.item0, path, "dumps/cannibal-solution.svg");
	}

	private boolean allPeopleAlive(Graph state)
	{
		return state.getNodes().stream()
			.filter(n -> n.hasMatchingLabelValue("human"))
			.count() == 6;
	}

	private boolean problemSolved(Graph state)
	{
		return allPeopleAlive(state) && humansOnSide(state, "left") == 6;
	}

	private int humansOnSide(Graph state, String location)
	{
		return (int) state.getNodes().stream()
			.filter(n -> n.hasMatchingLabelValue("human"))
			.filter(n -> location.equals(n.getEdges("at").get(0).getAttribute("location").orElse(null)))
			.count();
	}
}
