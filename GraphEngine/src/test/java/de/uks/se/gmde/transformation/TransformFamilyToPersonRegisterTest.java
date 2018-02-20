package de.uks.se.gmde.transformation;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.rendering.GraphDumper;
import de.uks.se.gmde.transformation.family.Family;
import de.uks.se.gmde.transformation.family.PersonRegister;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static de.uks.se.gmde.inference.ClassModelCreator.genericToSpecific;
import static de.uks.se.gmde.inference.ClassModelCreator.specificToGeneric;

public class TransformFamilyToPersonRegisterTest
{
	@Test
	public void transformation() throws IOException
	{
		Family simpsons = SimpsonsUtil.createFamily();

		Graph generic = specificToGeneric(simpsons);
		GraphDumper.dumpGraph(generic, "dumps/family-to-person-register/generic-original.svg");

		Graph genericTransformed = familyToPersonRegisterTransformation(generic);
		GraphDumper.dumpGraph(genericTransformed, "dumps/family-to-person-register/generic-transformed.svg");

		Map<Node, Object> specificTransformed = genericToSpecific(genericTransformed, PersonRegister.class.getPackage().getName());
		Assert.assertNotNull(specificTransformed);
		Assert.assertEquals(6, specificTransformed.size()); // Marge, Lisa, Maggie, Homer, Bart + PersonRegister
	}

	public static Graph familyToPersonRegisterTransformation(Graph genericIn)
	{
		if (genericIn == null)
			throw new ArgumentNullException("genericIn");

		Node familyNode = genericIn.getNodes().stream()
			.filter(n -> {
				Optional<Object> attr = n.getAttribute(Node.LABEL_ATTRIBUTE_NAME);
				return attr.isPresent() && "family".equals(attr.get());
			})
			.findFirst().get();

		Optional<Object> name = familyNode.getAttribute("name");

		if (!name.isPresent())
			throw new IllegalArgumentException("No node of type Family that has a name.");
		String familyName = (String) name.get();

		Graph genericOut = new Graph();
		Node personRegister = genericOut.createNode().withLabel("person-register");

		for (Node n : genericIn.getNodes())
		{
			Optional<Object> type = n.getAttribute(Node.LABEL_ATTRIBUTE_NAME);
			assert type.isPresent();
			switch ((String) type.get())
			{
				case "family-member":
					Optional<Object> givenName = n.getAttribute("given-name");
					assert givenName.isPresent();

					Node transformedNode = genericOut.createNode();
					transformedNode.withAttr("full-name", givenName.get() + " " + familyName);

					personRegister.createEdge("persons", transformedNode);

					if (n.getIncomingEdges("father").size() > 0
						|| n.getIncomingEdges("sons").size() > 0)
						transformedNode.withLabel("man");
					else if (n.getIncomingEdges("mother").size() > 0
						|| n.getIncomingEdges("daughters").size() > 0)
						transformedNode.withLabel("woman");
					else
						throw new IllegalStateException("Unhandled node type instance: " + n);
					break;
			}
		}
		return genericOut;
	}
}
