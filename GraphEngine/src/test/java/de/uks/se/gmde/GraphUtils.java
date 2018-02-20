package de.uks.se.gmde;

import org.junit.Assert;

import java.util.Optional;

public class GraphUtils
{
	public static void assertGraphEquals(final Graph expected, final Graph actual)
	{
		if (expected == null)
		{
			Assert.assertNull(actual);
			return;
		}
		String expectedCert = expected.computeCertificate();
		String actualCert = actual.computeCertificate();
		Assert.assertEquals(expectedCert, actualCert);

		boolean areIsomorph = expected.isIsomorphTo(actual);
		Assert.assertTrue(areIsomorph);
	}

	public static Graph createTestGraph()
	{
		Graph g = new Graph();
		Node left = g.createNode().withLabel("left-ravine");
		Node right = g.createNode().withLabel("right-ravine");

		Node e0 = g.createNode().withLabel("expendable");
		Node e1 = g.createNode().withLabel("expendable");
		Node e2 = g.createNode().withLabel("expendable");
		Node e3 = g.createNode().withLabel("expendable");

		e0.withAttr("name", "Trench");
		e0.withAttr("duration", "25min");
		e0.createEdge("at", left);

		e1.withAttr("name", "Barney „The Schizo“ Ross");
		e1.withAttr("duration", "20min");
		e1.createEdge("at", left);

		e2.withAttr("name", "Lee Christmas");
		e2.withAttr("duration", "10min");
		e2.createEdge("at", left);

		e3.withAttr("name", "Yin Yang");
		e3.withAttr("duration", "5min");
		e3.createEdge("at", left);

		Node rope = g.createNode().withLabel("rope");
		rope.withAttr("length", "100m");

		rope.createEdge("mounted-on", left, right);

		Node goggles = g.createNode().withLabel("goggles");
		goggles.withAttr("battery-remaining", "60min");

		e0.createEdge("has", goggles);
		return g;
	}


	public static Node getE0(Graph g)
	{
		Optional<Node> e0 = g.getNodes().stream()
			.filter(e -> e.getEdges("has").size() == 1)
			.findFirst();

		Assert.assertTrue(e0.isPresent());
		return e0.get();
	}
}
