package de.uks.se.gmde.problem.ferryman;

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

public class ExistUntilFerrymanTests
{
	@Test
	public void ferrymanSolutionExists()
		throws IOException
	{
		Tuple<ReachabilityGraph, Graph> s = FerrymansTest.createLtsHost();
		Graph ferrymanLtsStart = s.item1;

		ExistUntil solutionPossible = new ExistUntil(this::allCargoAlive, this::problemSolved);

		Optional<LtsPath> solution = solutionPossible.test(ferrymanLtsStart);
		Assert.assertNotNull(solution);
		Assert.assertTrue(solution.isPresent());

		LtsPath path = solution.get();
		Assert.assertNotNull(path);
		Assert.assertEquals(ferrymanLtsStart, path.getStart().get());
		Assert.assertEquals(3, cargoOnSide(path.getStart().get(), "left"));
		Assert.assertEquals(3, cargoOnSide(path.getEnd().get(), "right"));
		Assert.assertTrue(problemSolved(path.getEnd().get()));
		GraphDumper.dumpLtsPath(s.item0, path, "dumps/ferryman-solution.svg");
	}

	private boolean allCargoAlive(Graph state)
	{
		return state.getNodes().stream()
			.filter(n -> n.hasMatchingLabelValue("cargo"))
			.count() == 3;
	}

	private boolean problemSolved(Graph state)
	{
		return allCargoAlive(state) && cargoOnSide(state, "right") == 3;
	}

	private int cargoOnSide(Graph state, String location)
	{
		return (int) state.getNodes().stream()
			.filter(n -> n.hasMatchingLabelValue("cargo"))
			.filter(n -> location.equals(n.getEdges("at").get(0).getAttribute("location").orElse(null)))
			.count();
	}
}
