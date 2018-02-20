package de.uks.se.gmde.problem.cannibal;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.Rule;

import java.util.Map;

class TransferRule extends Rule
{
	TransferRule()
	{
		super("transfer", createPattern(), TransferRule::apply);
	}

	private static Graph createPattern()
	{
		final Graph pattern = new Graph();

		Node boat = pattern.createNode()
			.withLabel("boat");
		Node side0 = pattern.createNode()
			.withLabel("bank");
		Node side1 = pattern.createNode()
			.withLabel("bank");

		Node driver = pattern.createNode()
			.withLabel("human")
			.withAttr("can-drive", true);
		Node passenger = pattern.createNode()
			.withLabel("human");

		side0.createEdge("not-equal", side1);
		side1.createEdge("not-equal", side0);

		boat.createEdge("at", side0);
		driver.createEdge("at", side0);
		passenger.createEdge("at", side0);

		return pattern;
	}

	private static void apply(Rule source, Graph host, Map<Node, Node> match)
	{
		Node boat = source.getPattern().getNodes().get(0);
		Node side0 = source.getPattern().getNodes().get(1);
		Node side1 = source.getPattern().getNodes().get(2);
		Node driver = source.getPattern().getNodes().get(3);
		Node passenger = source.getPattern().getNodes().get(4);

		Node boatMatch = match.get(boat);
		Node side0Match = match.get(side0);
		Node side1Match = match.get(side1);
		Node driverMatch = match.get(driver);
		Node passengerMatch = match.get(passenger);

		assert boatMatch != null;
		assert side0Match != null;
		assert side1Match != null;
		assert driverMatch != null;
		assert passengerMatch != null;

		boatMatch.removeEdge("at", side0Match);
		driverMatch.removeEdge("at", side0Match);
		// if(driverMatch != passengerMatch)
		passengerMatch.removeEdge("at", side0Match);

		boatMatch.createEdge("at", side1Match);
		driverMatch.createEdge("at", side1Match);
		// if(driverMatch != passengerMatch)
		passengerMatch.createEdge("at", side1Match);
	}
}
