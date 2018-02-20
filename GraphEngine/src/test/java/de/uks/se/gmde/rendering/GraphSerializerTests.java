package de.uks.se.gmde.rendering;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.GraphUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class GraphSerializerTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullGraph()
	{
		GraphSerializer.toJson(null);
	}

	@Test(expected = ArgumentNullException.class)
	public void nullJson()
	{
		GraphSerializer.fromJsonString(null);
	}

	@Test
	public void deSerializeJson()
	{
		Graph expected = GraphUtils.createTestGraph();
		String json = "{\"0x1\":{\"nodes\":[\"0x2\",\"0x3\",\"0x4\",\"0x5\",\"0x6\",\"0x7\",\"0x8\",\"0x9\"]},\"0x2\":{\"attributes\":{\"label\":\"left-ravine\"},\"incomingEdges\":{\"at\":[\"0x4\",\"0x5\",\"0x6\",\"0x7\"],\"mounted-on\":[\"0x8\"]}},\"0x3\":{\"attributes\":{\"label\":\"right-ravine\"},\"incomingEdges\":{\"mounted-on\":[\"0x8\"]}},\"0x4\":{\"attributes\":{\"label\":\"expendable\",\"name\":\"Trench\",\"duration\":\"25min\"},\"edges\":{\"at\":[\"0x2\"],\"has\":[\"0x9\"]}},\"0x5\":{\"attributes\":{\"label\":\"expendable\",\"name\":\"Barney „The Schizo“ Ross\",\"duration\":\"20min\"},\"edges\":{\"at\":[\"0x2\"]}},\"0x6\":{\"attributes\":{\"label\":\"expendable\",\"name\":\"Lee Christmas\",\"duration\":\"10min\"},\"edges\":{\"at\":[\"0x2\"]}},\"0x7\":{\"attributes\":{\"label\":\"expendable\",\"name\":\"Yin Yang\",\"duration\":\"5min\"},\"edges\":{\"at\":[\"0x2\"]}},\"0x8\":{\"attributes\":{\"label\":\"rope\",\"length\":\"100m\"},\"edges\":{\"mounted-on\":[\"0x2\",\"0x3\"]}},\"0x9\":{\"attributes\":{\"label\":\"goggles\",\"battery-remaining\":\"60min\"},\"incomingEdges\":{\"has\":[\"0x4\"]}}}";
		System.out.println(json);

		Graph gRestored = GraphSerializer.fromJsonString(json);
		Assert.assertNotNull(gRestored);

		GraphUtils.assertGraphEquals(expected, gRestored);
	}

	@Test
	public void serializeJson()
	{
		Graph g = GraphUtils.createTestGraph();
		String json = GraphSerializer.toJson(g);
		Assert.assertNotNull(json);

		Graph gRestored = GraphSerializer.fromJsonString(json);
		Assert.assertNotNull(gRestored);

		GraphUtils.assertGraphEquals(g, gRestored);
	}

	@Test
	public void serializeToFile() throws IOException
	{
		Graph g = GraphUtils.createTestGraph();

		File tempFile = File.createTempFile("gmde-tests-", ".json.tmp");
		GraphSerializer.toJsonFile(g, tempFile.toPath());

		Graph gRestored = GraphSerializer.fromJsonFile(tempFile.toPath());
		Assert.assertNotNull(gRestored);

		GraphUtils.assertGraphEquals(g, gRestored);
	}
}
