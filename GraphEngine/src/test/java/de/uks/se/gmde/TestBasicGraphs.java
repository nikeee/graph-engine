package de.uks.se.gmde;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;


public class TestBasicGraphs
{
	@Test
	public void testFerrymanGraph()
	{
		Graph fmg = new Graph();

		Node leftBank = fmg.createNode()
			.withAttr("label", "left");

		Node rightBank = fmg.createNode()
			.withAttr("label", "right");

		Node boat = fmg.createNode()
			.withAttr("label", "boat")
			.withAttr("tmp", 42);

		boat.createEdge("at", leftBank);
		boat.createEdge("at", leftBank);

		Optional<Object> value = boat.getAttribute("tmp");

		Assert.assertTrue(value.isPresent());
		Assert.assertEquals("lost attr value", 42, value.get());

		boat.withAttr("tmp", null);

		value = boat.getAttribute("tmp");

		Assert.assertFalse("garbage value", value.isPresent());
		Assert.assertNotNull(boat.getEdges());

		List<Node> targets = boat.getEdges("??");

		Assert.assertEquals("wrong number of targets", 0, targets.size());

		targets = boat.getEdges("at");

		Assert.assertEquals("wrong number of targets", 1, targets.size());

		List<Node> sources = leftBank.getIncomingEdges("at");

		Assert.assertEquals("wrong number of sources", 1, sources.size());

		fmg.removeNode(boat);

		sources = leftBank.getIncomingEdges("at");

		Assert.assertEquals("wrong number of sources", 0, sources.size());
	}
}
