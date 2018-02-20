package de.uks.se.gmde.rendering;

import de.uks.se.ArgumentNullException;
import de.uks.se.StringEx;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.ReachabilityGraph;
import de.uks.se.gmde.query.Destination;
import de.uks.se.gmde.query.LtsPath;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Label;
import guru.nidi.graphviz.model.MutableGraph;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GraphDumper
{
	public static void dumpGraph(final Graph graph, final String fileName)
		throws IOException
	{
		if (graph == null)
			throw new ArgumentNullException("graph");
		if (fileName == null)
			throw new ArgumentNullException("fileName");

		dumpGraph(graph, fileName, null);
	}

	public static void dumpGraph(final Graph graph, final String fileName, Set<? extends Node> startNodes)
		throws IOException
	{
		if (graph == null)
			throw new ArgumentNullException("graph");
		if (fileName == null)
			throw new ArgumentNullException("fileName");

		MutableGraph gv = createMutableGraphFromGraph(graph, new HashMap<>(), startNodes);
		saveGv(gv, fileName);
	}

	public static void dumpMatch(final Graph pattern, final Map<Node, Node> match, final Graph host, final String fileName)
		throws IOException
	{
		Map<Node, String> nodeIdMap = new HashMap<>();
		MutableGraph gv = Factory.mutGraph();

		MutableGraph patternGv = createMutableGraphFromGraph(pattern, nodeIdMap, null);
		MutableGraph hostGv = createMutableGraphFromGraph(host, nodeIdMap, null);

		gv.graphAttrs().add(Style.INVIS);
		gv.setDirected(true);
		gv.generalAttrs().add(RankDir.TOP_TO_BOTTOM);

		gv.add(patternGv);
		patternGv.setLabel(Label.of("Pattern"));
		patternGv.setCluster(true);
		patternGv.graphAttrs().add(Style.SOLID);
		patternGv.generalAttrs().add(RankDir.TOP_TO_BOTTOM);

		gv.add(hostGv);
		hostGv.setLabel(Label.of("Host"));
		hostGv.setCluster(true);
		hostGv.graphAttrs().add(Style.SOLID);
		hostGv.generalAttrs().add(RankDir.TOP_TO_BOTTOM);

		match.forEach((patternNode, hostNode) -> {
			guru.nidi.graphviz.model.Node patternNodeGv = Factory.node(nodeIdMap.get(patternNode));
			guru.nidi.graphviz.model.Node hostNodeGv = Factory.node(nodeIdMap.get(hostNode));
			gv.add(patternNodeGv.link(Factory.to(hostNodeGv).with(Color.rgb("ff0000"))));
		});
		saveGv(gv, fileName);
	}

	public static void dumpLts(final ReachabilityGraph lts, final String directory)
		throws IOException
	{
		if (lts == null)
			throw new ArgumentNullException("lts");
		if (directory == null)
			throw new ArgumentNullException("directory");
		Path destDir = Paths.get(directory);
		if (Files.isRegularFile(destDir))
			throw new IllegalArgumentException("Specified directory is a file");

		if (Files.isDirectory(destDir))
			FileUtils.cleanDirectory(destDir.toFile());
		else
			FileUtils.forceMkdir(destDir.toFile());

		Map<Node, String> nodeIdMap = new HashMap<>();
		MutableGraph ltsGv = createMutableGraphFromGraph(lts.getLTSGraph(), nodeIdMap, lts.getStartGraphs());

		Path ltsDir = Paths.get(directory, "LTS.svg");
		for (Node stateNode : lts.getLTSGraph().getNodes())
		{
			assert stateNode instanceof Graph;
			Graph state = (Graph) stateNode;

			String stateId = nodeIdMap.get(state);
			assert stateId != null;
			String fileName = stateId + ".svg";

			MutableGraph stateGv = createMutableGraphFromGraph(state, new HashMap<>(), null);
			guru.nidi.graphviz.model.Node ltsStateNodeGv = Factory.node(stateId);
			ltsStateNodeGv = ltsStateNodeGv
				.with(
					Attributes.attr("URL", fileName),
					Label.of(stateId)
				);
			ltsGv.add(ltsStateNodeGv);

			Path statePath = Paths.get(directory, fileName);
			saveGv(stateGv, statePath);
		}
		saveGv(ltsGv, ltsDir);
	}

	public static void dumpLtsPath(final ReachabilityGraph lts, final LtsPath path, final String fileName)
		throws IOException
	{
		if (lts == null)
			throw new ArgumentNullException("lts");
		if (fileName == null)
			throw new ArgumentNullException("fileName");

		Map<Node, String> nodeIdMap = new HashMap<>();
		MutableGraph gv = createMutableGraphFromGraph(lts.getLTSGraph(), nodeIdMap, path.getStartAndEnd());
		Node source = null;
		for (Destination hop : path)
		{
			assert hop != null;
			if (source == null)
			{
				assert hop.via == null;
				source = hop.destination;
				guru.nidi.graphviz.model.Node src = Factory.node(nodeIdMap.get(source));
				gv.add(src.with(Style.FILLED, Color.rgb("0000ff")));
				continue;
			}
			guru.nidi.graphviz.model.Node src = Factory.node(nodeIdMap.get(source));
			guru.nidi.graphviz.model.Node dest = Factory.node(nodeIdMap.get(hop.destination));
			gv.add(src.link(Factory.to(dest).with(Color.rgb("ff0000"), Label.of(hop.via))));
			gv.add(dest.with(Style.FILLED, Color.rgb("0000ff")));
			source = hop.destination;
		}
		saveGv(gv, fileName);
	}

	private static MutableGraph createMutableGraphFromGraph(final Graph graph, Map<Node, String> nodeIdMap, Set<? extends Node> startNodes)
	{
		assert graph != null;
		assert nodeIdMap != null;

		if (startNodes == null)
			startNodes = Collections.emptySet();

		GraphDumperUtils.fillNodeIdMap(graph, nodeIdMap);

		MutableGraph gv = createMutableGraph();
		gv.setStrict(true);
		for (Node n : graph.getNodes())
			gv.add(createGvNode(n, nodeIdMap, startNodes));

		return gv;
	}

	private static void saveGv(final MutableGraph gv, final Path fileName)
		throws IOException
	{
		saveGv(gv, fileName.toString());
	}

	private static void saveGv(final MutableGraph gv, final String fileName)
		throws IOException
	{
		assert gv != null;
		assert fileName != null;

		Format format = parseFormat(fileName);
		File file = new File(fileName);
		if (file.getParentFile() == null)
			file = file.getAbsoluteFile();
		Graphviz.fromGraph(gv).render(format).toFile(file);
	}

	private static MutableGraph createMutableGraph()
	{
		Attributes fontSize = Font.size(12);
		Attributes fontFamily = Font.name("Fira Mono Medium");

		MutableGraph gv = Factory.mutGraph();
		gv.setDirected(true);

		gv.graphAttrs().add(fontFamily, fontSize);
		gv.nodeAttrs().add(fontFamily, fontSize);
		gv.linkAttrs().add(fontFamily, Font.size(10));

		gv.generalAttrs().add(RankDir.LEFT_TO_RIGHT);
		return gv;
	}

	private static guru.nidi.graphviz.model.Node createGvNode(Node node, Map<Node, String> nodeIdMap, Set<? extends Node> startNodes)
	{
		assert node != null;

		guru.nidi.graphviz.model.Node src = Factory.node(nodeIdMap.get(node));
		if (startNodes.contains(node))
			src = src.with(Style.FILLED, Color.rgb("00ff00"));

		for (Map.Entry<String, List<Node>> entry : node.getEdges().entrySet())
		{
			for (Node dest : entry.getValue())
			{
				guru.nidi.graphviz.model.Node n = Factory.node(nodeIdMap.get(dest));
				src = src.link(Factory.to(n).with(Label.of(entry.getKey())));
			}
		}
		Label l = getNodeAttributeLabel(node);
		return l == null
			? src.with(Shape.RECTANGLE)
			: src.with(Shape.RECTANGLE, l);
	}

	private static Format parseFormat(String fileName)
	{
		String lowerFile = fileName.toLowerCase();
		if (lowerFile.endsWith(".png"))
			return Format.PNG;
		if (lowerFile.endsWith(".svg"))
			return Format.SVG_STANDALONE;
		throw new IllegalArgumentException("Unrecognized file type of file name: " + fileName);
	}

	private static Label getNodeAttributeLabel(final Node node)
	{
		assert node != null;
		StringJoiner sj = new StringJoiner("<br/>");
		node.getAttribute(Node.LABEL_ATTRIBUTE_NAME)
			.ifPresent(l -> sj.add("<b>" + l + "</b>"));

		node.getAttributes().forEach((key, value) -> {
			if (Node.LABEL_ATTRIBUTE_NAME.equals(key))
				return;
			assert value != null;
			sj.add("<i>" + key + ":</i> " + value);
		});

		String h = sj.toString();
		return StringEx.isNullOrWhiteSpace(h)
			? null
			: Label.html(h);
	}
}

