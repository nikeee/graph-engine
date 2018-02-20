package de.uks.se.gmde.rendering;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;
import de.uks.se.ArgumentNullException;
import de.uks.se.Lazy;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Utility class used to serialize a graph + nodes.
 * <p>
 * Resources used:
 * https://git.adktek.com/google/gson/commit/efde6674e19612241740f8775ad5011f84b84c24
 * https://stackoverflow.com/a/10046134
 */
public class GraphSerializer
{
	private static final Set<String> supportedFileTypes = ImmutableSet.of("json");

	private static final Lazy<Gson> serializer = new Lazy<>(() -> createSerializer(false));
	private static final Lazy<Gson> prettySerializer = new Lazy<>(() -> createSerializer(true));

	public static void toJsonFile(final Graph graph, Path fileName) throws IOException
	{
		if (fileName == null)
			throw new ArgumentNullException("fileName");

		toJsonFile(graph, fileName, false);
	}

	public static void toJsonFile(final Graph graph, Path fileName, boolean pretty) throws IOException
	{
		if (fileName == null)
			throw new ArgumentNullException("fileName");

		try (Writer writer = Files.newBufferedWriter(fileName))
		{
			final String jsonStr = toJson(graph, pretty);
			assert jsonStr != null;
			writer.write(jsonStr);
		}
	}

	public static String toJson(final Graph graph)
	{
		return toJson(graph, false);
	}

	public static String toJson(final Graph graph, boolean pretty)
	{
		if (graph == null)
			throw new ArgumentNullException("graph");

		Gson gson = pretty ? prettySerializer.get() : serializer.get();
		return gson.toJson(graph);
	}

	public static Graph fromJsonFile(final Path fileName) throws IOException
	{
		if (fileName == null)
			throw new ArgumentNullException("fileName");

		try (Reader reader = Files.newBufferedReader(fileName))
		{
			return serializer.get().fromJson(reader, Graph.class);
		}
	}

	public static Graph fromJsonString(final String value)
	{
		if (value == null)
			throw new ArgumentNullException("value");

		return serializer.get().fromJson(value, Graph.class);
	}

	private static Gson createSerializer(boolean pretty)
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		new GraphAdapterBuilder()
			.addType(Graph.class)
			.addType(Node.class)
			.registerOn(gsonBuilder);

		if (pretty)
			gsonBuilder.setPrettyPrinting();

		return gsonBuilder.create();
	}

	public Set<String> getSupportedFileTypes()
	{
		return supportedFileTypes;
	}
}
