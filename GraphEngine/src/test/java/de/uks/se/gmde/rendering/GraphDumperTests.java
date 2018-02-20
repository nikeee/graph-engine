package de.uks.se.gmde.rendering;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.GraphUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GraphDumperTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullGraph()
		throws IOException
	{
		GraphDumper.dumpGraph(null, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownFormat()
		throws IOException
	{
		GraphDumper.dumpGraph(new Graph(), "lol.lol");
	}

	@Test
	public void visualizeExpendables()
		throws IOException
	{
		Graph g = GraphUtils.createTestGraph();
		String file = "dumps/expendables.png";
		GraphDumper.dumpGraph(g, file);

		// This is the only thing we can do as the generated graphs have a non-deterministic layout
		// and may result in different renderings.
		Assert.assertTrue(Files.exists(Paths.get(file)));
	}
}
